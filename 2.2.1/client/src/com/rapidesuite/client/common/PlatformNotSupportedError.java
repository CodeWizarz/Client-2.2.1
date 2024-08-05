package com.rapidesuite.client.common;

public class PlatformNotSupportedError extends Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2042113140581881520L;
	
	public PlatformNotSupportedError() {
		super("Platform is not supported.  OS = " + System.getProperty("os.name"));
	}

}
