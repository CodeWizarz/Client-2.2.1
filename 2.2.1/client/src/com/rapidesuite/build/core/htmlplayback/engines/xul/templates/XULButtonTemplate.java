package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.utils.SwiftBuildUtils;

public class XULButtonTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULButtonTemplate(String templateName)
	{
		super(templateName);
	}

	public void clickButtonByText(String value) throws Exception
	{
		checkParameter(value);

		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"getButtonElementByTextNode('button','" + value + "');\"; \n");
		if ( !engine.isNoWait )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"addSleep(" + XULEngine.resetDomLoadingTime + ");\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"resetDocumentLoaded();\"; \n");
		}
		res.append(engine.STEPS_LIST_VAR + " = \"generateClickEventOnCurrentElement();\"; \n");
		if ( !engine.isNoWait )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"ensureDOMisLoaded();\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"ensureNoErrors();\"; \n");
		}
		XULUtils.addToBuffer(res);
	}

	public void clickButtonByAttributeName(String attributeName, String attributeValue, boolean isSubString, String popupIframeId) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);

		StringBuffer res = new StringBuffer("");

		if ( attributeName.equalsIgnoreCase("ID") )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getElementById('" + attributeValue + "');\"; \n");
		}
		else
		{
			String tmp = SwiftBuildUtils.replaceSpecialCharacters(attributeValue);

			res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('button','" + attributeName + "','" + tmp + "'," + (!isSubString) + ", "+(popupIframeId==null?"null":("'"+popupIframeId+"'"))+");\"; \n");
		}
		if ( !engine.isNoWait )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"addSleep(" + XULEngine.resetDomLoadingTime + ");\"; \n");		
			res.append(engine.STEPS_LIST_VAR + " = \"resetDocumentLoaded();\"; \n");		
		}
		res.append(engine.STEPS_LIST_VAR + " = \"generateClickEventOnCurrentElement();\"; \n");
		if ( !engine.isNoWait )
		{			
			res.append(engine.STEPS_LIST_VAR + " = \"ensureDOMisLoaded();\"; \n");		
			res.append(engine.STEPS_LIST_VAR + " = \"ensureNoErrors();\"; \n");	
		}
		XULUtils.addToBuffer(res);

	}
	
	public void triggerBlurEvent() throws Exception {
		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"generateHTMLEventOnCurrentElement('blur');\"; \n");
		XULUtils.addToBuffer(res);
	}
	
	public static final String LAST_REFERRED_KEYWORD_AND_TRIGGER_BLUR_EVENT_KEYWORD = "LAST_REFERRED_KEYWORD_AND_TRIGGER_BLUR_EVENT";

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		String clickBy = getParameterValue(HTMLTemplate.CLICK_BY);
		if ( clickBy == null )
		{
			throw new Exception("The CLICK_BY value is missing in the command");
		}
		String parameter1 = getParameterValue(HTMLTemplate.PARAM1);
		if ( parameter1 == null )
		{
			throw new Exception("The parameter1 is missing in the command");
		}
		String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
		if (StringUtils.isBlank(parameter2)) {
			parameter2 = null;
		}
		if ( clickBy.trim().equalsIgnoreCase(CLICK_BY_TEXT) )
		{
			clickButtonByText(parameter1);
		}
		else if (clickBy.trim().equalsIgnoreCase(LAST_REFERRED_KEYWORD_AND_TRIGGER_BLUR_EVENT_KEYWORD))
		{
			triggerBlurEvent();
		}
		else
		{
			boolean isSubString = false;
			if ( clickBy.equalsIgnoreCase(CLICK_BY_ONCLICK_ATTRIBUTE) )
				isSubString = true;
			clickButtonByAttributeName(clickBy, parameter1, isSubString, parameter2);
		}
	}

}