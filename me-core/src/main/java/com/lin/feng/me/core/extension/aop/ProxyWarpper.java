package com.lin.feng.me.core.extension.aop;

import java.lang.annotation.ElementType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.lin.feng.me.core.extension.ExtensionLoader;

public class ProxyWarpper implements InvocationHandler {
	private Object target;
	private AopListener listener;

	public ProxyWarpper(Object target) {
		super();
		this.target = target;
	}

	public Object getTarget() {
		return target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		ExtensionLoader<AopListener> loader = ExtensionLoader.getExtensionLoader(AopListener.class);
		Class<?> cls = target.getClass();
		EnableAop aop = cls.getAnnotation(EnableAop.class);
		Method current = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
		listener = null;
		if (aop.value().compareTo(ElementType.TYPE) == 0) {
			String aopServiceKey = aop.aop();
			listener = loader.getOrDefaultExtension(aopServiceKey);
		} else if (current.isAnnotationPresent(EnableAop.class)) {
			aop = current.getAnnotation(EnableAop.class);
			String aopServiceKey = aop.aop();
			listener = loader.getOrDefaultExtension(aopServiceKey);
		}
		if (listener != null) {
			try {
				listener.before(target, method, args);
				result = listener.after(target, method, args, method.invoke(target, args));
			} catch (Exception e) {
				listener.exception(target, method, args, e);
			}
		} else {
			result = method.invoke(target, args);
		}

		return result;
	}

	public Object createProxyObject() {
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}
}
