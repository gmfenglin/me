package com.lin.feng.me.demo.service;

import com.lin.feng.me.core.extension.EnableInject;
import com.lin.feng.me.service.MeService;
@MeService(SayHello.class)
public class Web implements SayHello {
	private SayHello zhan;
	private SayHello li;

	public SayHello getZhan() {
		return zhan;
	}

	@EnableInject // ioc 通过set方法注入(注入必须是@Spi的接口),注入的默认实现服务
	public void setZhan(SayHello zhan) {
		this.zhan = zhan;
	}

	public SayHello getLi() {
		return li;
	}

	@EnableInject
	public void setLi(SayHello li) {
		this.li = li;
	}

	@Override
	public String say(String msg) {
		// TODO Auto-generated method stub
		return "web: " + zhan.say(msg) + " and " + li.say(msg);
	}

	@Override
	public void bye() {
		// TODO Auto-generated method stub
		
	}

}
