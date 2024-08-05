package com.rapidesuite.build.core.htmlplayback.templates;

import java.util.Map;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;

public abstract class HTMLTemplate
{

	private String templateName;
	private Map<String, String> parameters;

	public final static String CLICK_BY_TEXT = "TEXTNODE";
	public final static String LOAD_URL_TYPE = "LOAD_URL_TYPE";
	public final static String LOOKUP_VALUE = "LOOKUP_VALUE";
	public final static String INPUT_BY = "INPUT_BY";
	public final static String CLICK_BY = "CLICK_BY";
	public final static String LOOKUP_BY = "LOOKUP_BY";
	public final static String NO_WAIT = "NO_WAIT";
	public final static String CLICK_BY_IMAGE_ATTRIBUTE = "IMAGE_ATTRIBUTE";
	public final static String CLICK_BY_ONCLICK_ATTRIBUTE = "ONCLICK";
	public final static String SLEEP_TIME = "TIME";
	public final static String CONTAINSSTRING = "CONTAINS_STRING";

	public final static String PARAM1 = "PARAM1";
	public final static String PARAM2 = "PARAM2";
	public final static String PARAM3 = "PARAM3";
	public final static String PARAM4 = "PARAM4";
	public final static String PARAM5 = "PARAM5";
	public final static String PARAM6 = "PARAM6";
	public final static String PARAM7 = "PARAM7";

	public HTMLTemplate(String templateName)
	{
		this.templateName = templateName;
	}

	public String getName()
	{
		return templateName;
	}

	public void setParameters(Map<String, String> parameters)
	{
		this.parameters = parameters;
	}

	public Map<String, String> getParameters()
	{
		return parameters;
	}

	public String getParameterValue(String parameterName)
	{
		String value = parameters.get(parameterName);
		return value;
	}

	public void checkParameter(String value) throws Exception
	{
		if ( value == null )
			throw new Exception("A parameter is missing");
	}

	public abstract void execute(HTMLEngine htmlEngine) throws Exception;

}