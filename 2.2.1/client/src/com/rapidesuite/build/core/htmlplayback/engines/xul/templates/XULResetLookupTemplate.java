package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULResetLookupTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULResetLookupTemplate(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"resetSequence();\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"setSiblingsFlag(false);\"; \n");

		XULUtils.addToBuffer(res);
	}

}