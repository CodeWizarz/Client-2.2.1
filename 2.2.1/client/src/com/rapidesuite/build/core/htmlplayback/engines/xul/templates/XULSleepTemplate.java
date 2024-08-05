package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULSleepTemplate extends HTMLTemplate
{

	public XULSleepTemplate(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		String sleepTime = getParameterValue(HTMLTemplate.SLEEP_TIME);
		if ( sleepTime == null )
			throw new Exception("The TIME value is missing in the command");

		StringBuffer res = new StringBuffer("");
		res.append("steps[steps.length] = \"addSleep(" + sleepTime + ");\"; \n");
		XULUtils.addToBuffer(res);
	}

}