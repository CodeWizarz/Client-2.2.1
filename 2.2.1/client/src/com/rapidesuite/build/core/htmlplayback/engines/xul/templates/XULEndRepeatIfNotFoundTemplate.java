package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULEndRepeatIfNotFoundTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULEndRepeatIfNotFoundTemplate(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		engine.STEPS_LIST_VAR = "steps[steps.length]";

		engine.isRepeat = false;
	}

}