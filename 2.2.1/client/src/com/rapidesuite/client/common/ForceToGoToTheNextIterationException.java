package com.rapidesuite.client.common;

public class ForceToGoToTheNextIterationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6191590588344836608L;

	private final Integer lastExecutedIteration;
	
	public ForceToGoToTheNextIterationException(final Integer lastExecutedIteration) {
		this.lastExecutedIteration = lastExecutedIteration;
	}

	public Integer getLastExecutedIteration() {
		return lastExecutedIteration;
	}	
}
