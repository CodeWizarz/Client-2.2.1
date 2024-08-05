package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULClickElementTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULClickElementTemplate(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;
		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"dispatchClickMouseEvent();\"; \n");
		XULUtils.addToBuffer(res);
	}

}