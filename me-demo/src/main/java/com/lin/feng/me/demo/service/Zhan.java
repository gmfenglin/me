package com.lin.feng.me.demo.service;

import java.lang.reflect.Method;

import com.lin.feng.me.core.extension.aop.AopListener;

public class Zhan implements SayHello,AopListener {// AOP通过实现AopListener接口方法

	public String say(String msg) {
		// TODO Auto-generated method stub
		return "zhan:hello, " + msg;
	}

	public void exception(Object target, Method method, Object[] args, Exception e) {
		System.out.println("zhan 执行方法发生异常");
		AopListener.super.exception(target, method, args, e);
	}

	public Object after(Object target, Method method, Object[] args, Object result) {
		System.out.println("zhan 执行方法后，返回结果前");
		return AopListener.super.after(target, method, args, result);
	}

	public boolean before(Object target, Method method, Object[] args) {
		System.out.println("zhan 执行方法前");
		return AopListener.super.before(target, method, args);
	}

}
