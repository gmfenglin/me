package com.lin.feng.me.core.extension;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.lin.feng.me.core.extension.aop.AopListener;
import com.lin.feng.me.core.extension.aop.EnableAop;
import com.lin.feng.me.core.extension.aop.ProxyWarpper;
import com.lin.feng.me.core.extension.runException.CoreRunException;
import com.lin.feng.me.core.extension.runException.JobException;
import com.lin.feng.me.core.extension.runException.RunException;

public class ExtensionLoader<T> {
	private Class<?> cls;
	private String defaultName;
	private static RunException runException = new CoreRunException();

	private ExtensionLoader(Class<?> cls) {
		log(this, "ExtensionLoader", "starting");
		this.cls = cls;
		this.defaultName = cls.getAnnotation(Spi.class).value();
		try {
			loadResource();
		} catch (Exception e) {
			CAHCE_EXTENSION_CLASSES.clear();
			e.printStackTrace();
		}
	}

	public static final boolean DEBUG = false;

	public static void log(Object o, String methodName, String msg) {
		if (DEBUG) {
			System.out.println(System.currentTimeMillis() + ":" + o + "-->" + methodName + "():" + msg);
		}

	}

	private static final String[] DIRECTORY = {"META-INF/me/services/" };
	// 缓存扩展加载器
	private static ConcurrentHashMap<Class<?>, ExtensionLoader<?>> CAHCE_EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();
	// 缓存扩展实例
	private final ConcurrentHashMap<String, Holder<Object>> CAHCE_EXTENSION_INSTANCES = new ConcurrentHashMap<>();
	// 缓存扩展类
	private volatile ConcurrentHashMap<String, Class<?>> CAHCE_EXTENSION_CLASSES = new ConcurrentHashMap<>();

	private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

	public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> cls) {
		if (cls == null) {// 1. 不能为空
			runException.notNull("extension cls==null");
		}
		if (!cls.isInterface()) {// 2. 必须是接口
			runException.must("extension cls must be a interace");
		}
		if (!cls.isAnnotationPresent(Spi.class)) {// 3. 必须带有Spi注解
			runException.must("extension cls must with @Spi");
		}
		String[] names = NAME_SEPARATOR.split(cls.getAnnotation(Spi.class).value());
		if (names.length != 1) {// 4. spi 默认值只能一个
			runException.must("extension cls  @Spi value is only one");
		}

		return (ExtensionLoader<T>) CAHCE_EXTENSION_LOADERS.computeIfAbsent(cls, k -> new ExtensionLoader<T>(cls));
	}

	public T getExtension(String name) {
		return getExtension(name, null);

	}

	public T getDefaultExtension() {
		return getExtension("me");
	}

	public boolean hasExtension(String name) {
		return CAHCE_EXTENSION_INSTANCES.containsKey(name) ? (CAHCE_EXTENSION_INSTANCES.get(name).get() != null)
				: false;
	}

	public T getOrDefaultExtension(String name) {
		return CAHCE_EXTENSION_CLASSES.containsKey(name) ? getExtension(name) : getDefaultExtension();
	}

	public T replaceDependcy(T t, Map<String, String> dependMap) {
		if (dependMap != null && dependMap.size() > 0) {
			synchronized (t) {
				Class<?> cls = t.getClass();
				dependMap.forEach((k, v) -> {
					try {
						Field field = cls.getDeclaredField(k);
						try {
							field.setAccessible(true);
							if (field.get(t) != getExtension(v)) {
								field.set(t, getExtension(v));
							}
						} finally {
							field.setAccessible(false);
						}
					} catch (Exception e) {
						throw new JobException(e.getMessage());
					}
				});
			}

		}
		return t;
	}

	private Holder<Object> getOrCreateHolder(String name) {
		Holder<Object> holder = CAHCE_EXTENSION_INSTANCES.get(name);
		if (holder == null) {
			CAHCE_EXTENSION_INSTANCES.putIfAbsent(name, new Holder<>());
			holder = CAHCE_EXTENSION_INSTANCES.get(name);
		}
		return holder;
	}

	public T getExtension(String name, Map<String, String> dependMap) {
		if (name == null) {// 1. 不能为空
			runException.notNull("extension name==null");
		}

		if ("me".equals(name)) {// 2. 获取默认扩展
			name = this.defaultName;
		}
		if (!CAHCE_EXTENSION_CLASSES.containsKey(name)) {
			runException.notNull("extension cls not find");
		}
		String key = name;
		if (dependMap != null && dependMap.size() > 0) {
			Set<String> keySet = dependMap.keySet();
			List<String> listKey = new ArrayList<>(dependMap.size());
			for (String keyMap : keySet) {
				listKey.add(keyMap);
			}
			Collections.sort(listKey);
			StringBuffer sb = new StringBuffer(key);
			for (String keyl : listKey) {
				sb.append(keyl).append("=").append(dependMap.get(keyl));
			}
			key = sb.toString();
		}

		final Holder<Object> holder = getOrCreateHolder(key);
		Object instance = holder.get();
		if (instance == null) {
			synchronized (holder) {
				instance = holder.get();
				if (instance == null) {
					createExtension(name, dependMap, holder);
					instance = holder.get();
				}
			}
		}
		return (T) instance;

	}

	private void createExtension(String name, Map<String, String> dependMap, Holder<Object> holder) {

		Class<?> clsExtension = CAHCE_EXTENSION_CLASSES.get(name);
		log(this, "createExtension", "starting:" + clsExtension);
		if (clsExtension == null) {
			runException.notNull("extension name cls==null");
		}
		try {
			T instance = (T) clsExtension.newInstance();
			if (clsExtension.isAnnotationPresent(EnableAop.class)) {
				instance = (T) new ProxyWarpper(instance).createProxyObject();
			}
			holder.set(instance);
			inject(instance, name, dependMap);
			if (instance instanceof Lifecycle) {
				((Lifecycle) instance).initialize();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}

	}

	private T inject(T instance, String name, Map<String, String> dependMap) {

		for (Method method : instance.getClass().getMethods()) {
			if ((method.getName().startsWith("set") && method.getParameterTypes().length == 1
					&& Modifier.isPublic(method.getModifiers())) && method.isAnnotationPresent(EnableInject.class)) {
				Class<?> pt = method.getParameterTypes()[0];
				if (!pt.isInterface()) {
					runException.must("extension cls must be a interace");
				}
				if (!pt.isAnnotationPresent(Spi.class)) {// 3. 必须带有Spi注解
					runException.must("extension cls must with @Spi");
				}
				String[] names = NAME_SEPARATOR.split(pt.getAnnotation(Spi.class).value());
				if (names.length != 1) {// 4. spi 默认值只能一个
					runException.must("extension cls  @Spi value is only one");
				}
				String property = method.getName().length() > 3
						? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4)
						: "";
				String injectName = pt.getAnnotation(Spi.class).value();
				if (dependMap != null && dependMap.containsKey(property)) {
					String tmp = dependMap.get(property);
					if (tmp != null && tmp.trim().length() > 0) {
						injectName = tmp;
					}
				}
				log(this, "inject", "starting-" + property + "-" + instance);
				try {
					ExtensionLoader<?> loader = ExtensionLoader.getExtensionLoader(pt);

					Field field = CAHCE_EXTENSION_CLASSES.get(name).getDeclaredField(property);
					try {
						field.setAccessible(true);
						Object dependy = loader.getExtension(injectName);
						if (field.get(instance) != dependy) {
							method.invoke(instance, dependy);
						}
					} finally {
						field.setAccessible(false);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return instance;
	}

	private ClassLoader getClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
		}
		if (cl == null) {
			cl = ExtensionLoader.class.getClassLoader();
			if (cl == null) {
				try {
					cl = ClassLoader.getSystemClassLoader();
				} catch (Throwable ex) {
				}
			}
		}

		return cl;
	}

	private void loadResource(ConcurrentHashMap<String, Class<?>> cache, ClassLoader classLoader,
			java.net.URL resourceURL) {
		try {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					final int ci = line.indexOf('#');
					if (ci >= 0) {
						line = line.substring(0, ci);
					}
					line = line.trim();
					if (line.length() > 0) {
						try {
							String name = null;
							int i = line.indexOf('=');
							if (i > 0) {
								name = line.substring(0, i).trim();
								line = line.substring(i + 1).trim();
							}
							if (line.length() > 0) {
								final String clsLine = line;
								cache.computeIfAbsent(name, (k) -> {
									try {
										return Class.forName(clsLine, true, classLoader);
									} catch (ClassNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									return null;
								});
							}
						} catch (Throwable t) {
							IllegalStateException e = new IllegalStateException(
									"Failed to load extension class (interface: " + cls + ", class line: " + line
											+ ") in " + resourceURL + ", cause: " + t.getMessage(),
									t);
							throw e;
						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void loadResource() throws Exception {
		log(this, "loadResource", "starting");
		ClassLoader classLoader = getClassLoader();
		for (String dir : DIRECTORY) {
			String fileName = dir + cls.getName();
			ClassLoader extensionLoaderClassLoader = ExtensionLoader.class.getClassLoader();
			Enumeration<java.net.URL> urls = null;
			if (ClassLoader.getSystemClassLoader() != extensionLoaderClassLoader) {
				urls = extensionLoaderClassLoader.getResources(fileName);
			}
			if (urls == null || !urls.hasMoreElements()) {
				if (classLoader != null) {
					urls = classLoader.getResources(fileName);
				} else {
					urls = ClassLoader.getSystemResources(fileName);
				}
			}

			if (urls != null) {
				while (urls.hasMoreElements()) {
					java.net.URL resourceURL = urls.nextElement();
					loadResource(CAHCE_EXTENSION_CLASSES, classLoader, resourceURL);
				}
			}

		}
	}

}
