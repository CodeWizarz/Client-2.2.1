package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULOpenWindowTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULOpenWindowTemplate(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		StringBuffer res = new StringBuffer("");

		res.append(engine.STEPS_LIST_VAR + " = \"ensureWindowOpened();\"; \n");
		engine.isPopupWindowOpened = true;
		XULUtils.addToBuffer(res);
	}

}