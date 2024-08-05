package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULSwitchBackDOMTemplate extends HTMLTemplate {
	private XULEngine engine;
	public XULSwitchBackDOMTemplate(String templateName) {
		super(templateName);
	}

	@Override
	public void execute(HTMLEngine htmlEngine) throws Exception {
		engine = (XULEngine) htmlEngine;
		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"switchBackDOM();\"; \n");
		XULUtils.addToBuffer(res);
	}

}
