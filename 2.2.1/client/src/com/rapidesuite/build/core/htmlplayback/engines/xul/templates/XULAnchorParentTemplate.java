package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULAnchorParentTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULAnchorParentTemplate(String templateName)
	{
		super(templateName);
	}

	public void getAnchorParentByAttributeName(String tagName, String attributeName, String attributeValue) throws Exception
	{
		checkParameter(tagName);
		checkParameter(attributeName);
		checkParameter(attributeValue);
		
		String tmp = attributeValue;
		tmp = XULUtils.replaceValueByRSCSequence(tmp);

		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"getParentElementByAttributeValue('" + tagName + "','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"addSleep(" + XULEngine.resetDomLoadingTime + ");\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"resetDocumentLoaded();\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"generateClickAnchorOnCurrentElement();\"; \n");
		if ( !engine.isNoWait )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"ensureDOMisLoaded();\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"ensureNoErrors();\"; \n");
		}

		XULUtils.addToBuffer(res);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		String lookupBy = getParameterValue(HTMLTemplate.LOOKUP_BY);
		if ( lookupBy == null )
			throw new Exception("The LOOKUP_BY value is missing in the command");

		String parameter1 = getParameterValue(HTMLTemplate.PARAM1);
		if ( parameter1 == null )
			throw new Exception("The parameter1 is missing in the command");

		String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
		if ( parameter2 == null )
			throw new Exception("The parameter2 is missing in the command");

		getAnchorParentByAttributeName(parameter1, lookupBy, parameter2);
	}

}