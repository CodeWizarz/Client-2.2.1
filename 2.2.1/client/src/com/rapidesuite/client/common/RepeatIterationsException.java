package com.rapidesuite.client.common;

import com.rapidesuite.build.core.controller.Injector;

public class RepeatIterationsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7565228299993427452L;
	
	private final Injector injector;
	private final Integer iterationNumber;
	private final Exception expectedException;
	private final boolean forceToGiveOneMoreChance;
	
	public RepeatIterationsException(final Injector injector, final Integer iterationNumber, final Exception expectedException, final boolean forceToGiveOneMoreChance) {
		super(expectedException);
		this.injector = injector;
		this.iterationNumber = iterationNumber;
		this.expectedException = expectedException;
		this.forceToGiveOneMoreChance = forceToGiveOneMoreChance;
	}	

	public RepeatIterationsException(final Injector injector, final Integer iterationNumber, final Exception excpectedException) {
		this(injector, iterationNumber, excpectedException, false);
	}

	public Injector getInjector() {
		return injector;
	}

	public Integer getIterationNumber() {
		return iterationNumber;
	}

	public Exception getExpectedException() {
		return expectedException;
	}

	public boolean isForceToGiveOneMoreChance() {
		return forceToGiveOneMoreChance;
	}
	
	
}
