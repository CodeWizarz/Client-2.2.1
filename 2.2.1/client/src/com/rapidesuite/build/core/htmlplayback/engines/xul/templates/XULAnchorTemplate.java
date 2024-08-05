package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.utils.SwiftBuildUtils;

public class XULAnchorTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULAnchorTemplate(String templateName)
	{
		super(templateName);
	}

	public void clickAnchorByText(String value, String popupIframeId) throws Exception
	{
		checkParameter(value);

		StringBuffer res = new StringBuffer("");

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByTextNode('a','" + value + "'"+(popupIframeId==null ? "" : String.format(",'%s'", popupIframeId))+");\"; \n");
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

	public void clickAnchorByAttributeName(String attributeName, String attributeValue, boolean isSubString) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);

		StringBuffer res = new StringBuffer("");

		String tmp = SwiftBuildUtils.replaceSpecialCharacters(attributeValue);
		tmp = XULUtils.replaceValueByRSCSequence(tmp);

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('a','" + attributeName + "','" + tmp + "'," + (!isSubString) + ");\"; \n");

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

	public void clickAnchorByImageAttributeName(String attributeName, String attributeValue) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);
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
			String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
			clickAnchorByText(parameter1, parameter2);
		}
		else if ( clickBy.equalsIgnoreCase(CLICK_BY_IMAGE_ATTRIBUTE) )
		{
			String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
			clickAnchorByImageAttributeName(parameter1, parameter2);
		}
		else if ( clickBy.equalsIgnoreCase(CLICK_BY_ONCLICK_ATTRIBUTE) )
		{
			boolean isSubString = true;
			clickAnchorByAttributeName(clickBy, parameter1, isSubString);
		}
		else
		{
			boolean isSubString = true;
			clickAnchorByAttributeName(clickBy, parameter1, isSubString);
		}
	}

}