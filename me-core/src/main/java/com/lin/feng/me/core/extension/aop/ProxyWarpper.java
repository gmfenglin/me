package com.lin.feng.me.core.extension.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyWarpper implements InvocationHandler {
	private Object target;

	public ProxyWarpper(Object target) {
		super();
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		try {
			before(target, method, args);
			result = method.invoke(target, args);
			after(target, method, args, result);
		} catch (Exception e) {
			exception(target, method, args, e);
		}

		return result;
	}

	private void exception(Object target, Method method, Object[] args, Exception e) {
		if (target instanceof AopListener) {
			AopListener listener = (AopListener) target;
			listener.exception(target, method, args, e);
		}

	}

	private void after(Object target, Method method, Object[] args, Object result) {
		if (target instanceof AopListener) {
			AopListener listener = (AopListener) target;
			listener.after(target, method, args, result);
		}
	}

	private void before(Object target, Method method, Object[] args) {
		if (target instanceof AopListener) {
			AopListener listener = (AopListener) target;
			listener.before(target, method, args);
		}
	}

	public Object createProxyObject() {
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}
}
