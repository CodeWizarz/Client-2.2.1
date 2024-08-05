package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULSwitchDOMTemplate extends HTMLTemplate {
	private XULEngine engine;
	
	public XULSwitchDOMTemplate(String templateName) {
		super(templateName);
	}

	@Override
	public void execute(HTMLEngine htmlEngine) throws Exception {
		engine = (XULEngine) htmlEngine;

		String parameter1 = HTMLTemplate.PARAM1;
		String param1 = getParameterValue(parameter1);
		if (param1 == null) {
			throw new Exception(String.format("The %s is missing in the command", parameter1));
		}
		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"switchDOM('"+param1+"');\"; \n");
		XULUtils.addToBuffer(res);
	}

}
