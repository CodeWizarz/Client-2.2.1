package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULRepeatIfNotFoundTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULRepeatIfNotFoundTemplate(String templateName)
	{
		super(templateName);
	}

	public static void setRepeat(boolean isRepeat) throws Exception
	{
		StringBuffer res = new StringBuffer("");
		res.append("steps[steps.length] = \"setRepeat(" + isRepeat + ");\"; \n");
		XULUtils.addToBuffer(res);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		engine.STEPS_LIST_VAR = "repeat_steps[repeat_steps.length]";

		StringBuffer res = new StringBuffer("");
		res.append("repeat_steps_indexes[repeat_steps_indexes.length] = " + "repeat_steps.length; \n");

		XULUtils.addToBuffer(res);
	}

}