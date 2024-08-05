package com.rapidesuite.build.core;

public class ManualStopException extends Exception
{
    private static final long serialVersionUID = 195240656617322895L;

    public ManualStopException() {
		super();
	}

	public ManualStopException(final String message) {
		super(message);
	}
}
