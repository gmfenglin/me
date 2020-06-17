package com.lin.feng.me.demo.service;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;

import com.lin.feng.me.core.extension.aop.AopListener;
import com.lin.feng.me.core.extension.aop.EnableAop;
import com.lin.feng.me.core.extension.runException.JobException;
import com.lin.feng.me.service.MeService;

@EnableAop(value=ElementType.METHOD)// AOP通过@EnableAop 开启,ElementType.METHOD表示需要具体指定方法，ElementType.TYPE表示所有方法
@MeService(SayHello.class)
public class Zhan implements SayHello {
	@EnableAop(aop = "imp")// aop 是实现AopListener类在META-INF/me/services/com.lin.feng.me.core.extension.aop.AopListener中的服务key
	public String say(String msg) {
		return "zhan:hello, " + msg;
	}

	@Override
	public void bye() {
		System.out.println("zhan bye");

	}

}
