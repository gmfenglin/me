package com.lin.feng.me.core.extension.aop;

import com.lin.feng.me.service.MeService;

@MeService(value = AopListener.class, name = "base")
public final class BaseAopLIstener implements AopListener {

}
