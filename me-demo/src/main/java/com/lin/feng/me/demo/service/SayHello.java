package com.lin.feng.me.demo.service;

import com.lin.feng.me.core.extension.Spi;

@Spi("zhan")// 设置默认实现服务key,------>:在META-INF/me/services/com.lin.feng.me.demo.service.SayHello文件中"="左边为服务key
public interface SayHello {
	String say(String msg);
}
