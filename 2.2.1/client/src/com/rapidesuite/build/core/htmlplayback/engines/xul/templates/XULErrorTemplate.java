package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULErrorTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULErrorTemplate(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;
		String tagName = getParameterValue(HTMLTemplate.PARAM1);
		String attributeName = getParameterValue(HTMLTemplate.PARAM2);
		String attributeValue = getParameterValue(HTMLTemplate.PARAM3);
		StringBuffer res = new StringBuffer("");

		if ( attributeName.equalsIgnoreCase(CLICK_BY_TEXT) )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"raiseErrorByTextNode('" + tagName + "','" + attributeValue + "');\"; \n");
		}
		else
		{
			String tmp = attributeValue;
			tmp = XULUtils.replaceValueByRSCSequence(tmp);
			res.append(engine.STEPS_LIST_VAR + " = \"raiseErrorByAttributeValue('" + tagName + "','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");
		}
		XULUtils.addToBuffer(res);
	}

}