package com.lin.feng.me.core.extension.runException;

public abstract class MeException extends RuntimeException {

	private static final long serialVersionUID = -4500799033652374746L;

	public MeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
