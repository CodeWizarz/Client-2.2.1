package com.rapidesuite.build.core.exception;

import org.springframework.util.Assert;

public class SkipInjectionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7039336307100741098L;
	
	private final Exception exceptionToThrow;

	public SkipInjectionException(final Exception exceptionToThrow) {
		Assert.notNull(exceptionToThrow, "exceptionToThrow must not be null");
		this.exceptionToThrow = exceptionToThrow;
	}

	public Exception getExceptionToThrow() {
		return exceptionToThrow;
	}
}
