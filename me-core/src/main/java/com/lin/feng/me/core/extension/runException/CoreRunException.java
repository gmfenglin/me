package com.lin.feng.me.core.extension.runException;

public class CoreRunException implements RunException {

	@Override
	public void notNull(String msg) {
		throw new JobException(msg);

	}

	@Override
	public void must(String msg) {
		throw new JobException(msg);

	}

}
