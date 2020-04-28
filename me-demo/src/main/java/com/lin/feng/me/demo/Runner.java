package com.lin.feng.me.demo;

import java.util.HashMap;
import java.util.Map;

import com.lin.feng.me.core.extension.ExtensionLoader;
import com.lin.feng.me.demo.service.SayHello;

public class Runner {

	public static void main(String[] args) {
		ExtensionLoader<SayHello> loader = ExtensionLoader.getExtensionLoader(SayHello.class);
		System.out.println("-------------------调用默认实现，aop实现------------------------------------");
		String msg = loader.getDefaultExtension().say("fenglin");
		System.out.println(msg);
		System.out.println("-------------------按名字调用------------------------------------");
		msg = loader.getExtension("li").say("fenglin");
		System.out.println(msg);
		System.out.println("--------------------------ioc -----------------------------");
		msg = loader.getExtension("web").say("fenglin");
		System.out.println(msg);
		System.out.println("---------------------------ioc 依赖传入,key 为属性名，value为服务key----------------------------");
		Map<String, String> dependMap = new HashMap<>();
		dependMap.put("zhan", "li");
		dependMap.put("li", "web");
		msg = loader.getExtension("web", dependMap).say("fenglin");
		System.out.println(msg);
		System.out.println("-------------------------------------------------------");

	}

}
