package com.lin.feng.me.core.extension.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.lin.feng.me.core.extension.runException.JobException;

public class ProxyWarpper implements InvocationHandler {
	private Object target;

	public ProxyWarpper(Object target) {
		super();
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		if (target instanceof AopListener) {
			AopListener listener = (AopListener) target;
			try {
				if (!listener.before(target, method, args)) {
					throw new JobException("Aop listener before return false");
				}
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
