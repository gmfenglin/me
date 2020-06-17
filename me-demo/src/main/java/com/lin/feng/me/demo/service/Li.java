package com.lin.feng.me.demo.service;

import com.lin.feng.me.service.MeService;

@MeService(SayHello.class)
public class Li implements SayHello {

	@Override
	public String say(String msg) {
		// TODO Auto-generated method stub
		return "li:hello, " + msg;
	}

	@Override
	public void bye() {
		// TODO Auto-generated method stub
		
	}

}
