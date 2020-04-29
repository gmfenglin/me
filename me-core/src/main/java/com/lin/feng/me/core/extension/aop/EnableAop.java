package com.lin.feng.me.core.extension.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableAop {
	ElementType value() default ElementType.TYPE;

	String aop() default "me";
}
