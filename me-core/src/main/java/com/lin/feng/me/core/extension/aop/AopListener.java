package com.lin.feng.me.core.extension.aop;

import java.lang.reflect.Method;

public interface AopListener {

	void exception(Object target, Method method, Object[] args, Exception e);

	void after(Object target, Method method, Object[] args, Object result);

	void before(Object target, Method method, Object[] args);
}
