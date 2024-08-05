package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.utils.SwiftBuildUtils;

public class XULAreaTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULAreaTemplate(String templateName)
	{
		super(templateName);
	}

	public void clickByText(String value) throws Exception
	{
		checkParameter(value);

		StringBuffer res = new StringBuffer("");

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByTextNode('area','" + value + "');\"; \n");
		if ( !engine.isNoWait )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"addSleep(" + XULEngine.resetDomLoadingTime + ");\"; \n");
			res.append("steps[steps.length] = \"resetDocumentLoaded();\"; \n");
		}
		res.append(engine.STEPS_LIST_VAR + " = \"generateClickAnchorOnCurrentElement();\"; \n");
		if ( !engine.isNoWait )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"ensureDOMisLoaded();\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"ensureNoErrors();\"; \n");
		}
		XULUtils.addToBuffer(res);
	}

	public void clickByAttributeName(String attributeName, String attributeValue, boolean isSubString) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);

		StringBuffer res = new StringBuffer("");

		String tmp = SwiftBuildUtils.replaceSpecialCharacters(attributeValue);

		String rscSequence = "";
		if ( tmp.indexOf(HTMLTemplate.LOOKUP_VALUE) != -1 )
		{
			tmp = tmp.replaceFirst(HTMLTemplate.LOOKUP_VALUE, "");
			rscSequence = "+rscSequence";
		}

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('area','" + attributeName + "','" + tmp + "'" + rscSequence + "," + (!isSubString) + ");\"; \n");

		if ( !engine.isNoWait )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"addSleep(" + XULEngine.resetDomLoadingTime + ");\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"resetDocumentLoaded();\"; \n");
		}
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

		String clickBy = getParameterValue(HTMLTemplate.CLICK_BY);
		if ( clickBy == null )
			throw new Exception("The CLICK_BY value is missing in the command");

		String parameter1 = getParameterValue(HTMLTemplate.PARAM1);
		if ( parameter1 == null )
			throw new Exception("The parameter1 is missing in the command");

		if ( clickBy.equalsIgnoreCase(CLICK_BY_TEXT) )
		{
			clickByText(parameter1);
		}
		else
		{
			boolean isSubString = true;
			clickByAttributeName(clickBy, parameter1, isSubString);
		}
	}

}