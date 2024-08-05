package com.rapidesuite.client.common;

public class PLSQLFunctionParameter {

	private String parameterName;
	private Object parameterValue;
	private int parameterType;

	public PLSQLFunctionParameter(
			String parameterName,
			Object parameterValue,
			int parameterType) {
		this.parameterName=parameterName;
		this.parameterValue=parameterValue;
		this.parameterType=parameterType;
	}

	public String getParameterName() {
		return parameterName;
	}

	public Object getParameterValue() {
		return parameterValue;
	}

	public int getParameterType() {
		return parameterType;
	}

	@Override
	public String toString()
	{
		return "PLSQLFunctionParameter [parameterName=" + parameterName + ", parameterValue=" + parameterValue + ", parameterType=" + parameterType + "]";
	}

}