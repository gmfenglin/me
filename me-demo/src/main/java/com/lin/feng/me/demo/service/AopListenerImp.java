package com.lin.feng.me.demo.service;

import java.lang.reflect.Method;

import com.lin.feng.me.core.extension.aop.AopListener;
import com.lin.feng.me.core.extension.runException.JobException;
import com.lin.feng.me.service.MeService;
@MeService(value=AopListener.class,name="imp")
public class AopListenerImp implements AopListener {

	@Override
	public void exception(Object target, Method method, Object[] args, Exception e) {
		System.out.println("imp exception");
	}

	@Override
	public Object after(Object target, Method method, Object[] args, Object result) {
		System.out.println("imp after");
		return result;
	}

	@Override
	public void before(Object target, Method method, Object[] args) throws JobException {
		System.out.println("imp before");
	}

}
