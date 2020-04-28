package com.lin.feng.me.core.extension.aop;

import java.lang.reflect.Method;

import com.lin.feng.me.core.extension.runException.JobException;

public interface AopListener {

	default void exception(Object target, Method method, Object[] args, Exception e) {
		System.err.println(target + "->" + method.getName() + ":" + (e != null ? e.getMessage() : "null"));
	}

	default Object after(Object target, Method method, Object[] args, Object result) {
		return result;
	}

	default  void before(Object target, Method method, Object[] args) throws JobException{
		
	}
}
