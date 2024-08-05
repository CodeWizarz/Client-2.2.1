package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplateManager;
import com.rapidesuite.build.utils.SwiftBuildUtils;

public class XULInputTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULInputTemplate(String templateName)
	{
		super(templateName);
	}

	public void setInputValueByAttributeName(String attributeName, String attributeValue, String value) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);
		checkParameter(value);

		StringBuffer res = new StringBuffer("");
		String tmp = attributeValue;
		tmp = XULUtils.replaceValueByRSCSequence(tmp);

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('input','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");

		tmp = value;
		if ( tmp.equalsIgnoreCase("\"\"") )
		{
			tmp = "";
		}
		tmp = SwiftBuildUtils.replaceSpecialCharacters(tmp);
		res.append(engine.STEPS_LIST_VAR + " = \"setInputValue('" + tmp + "');\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"generateHTMLEventOnCurrentElement('change');\"; \n");

		XULUtils.addToBuffer(res);
	}

	public void clickInputByAttributeName(String attributeName, String attributeValue) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);
		
		final String MOUSE_CLICK_EVENT_KEYWORD = "MOUSE_CLICK_EVENT";
		boolean forceToUseMouseClickEvent = false;
		if (attributeValue.endsWith(MOUSE_CLICK_EVENT_KEYWORD)) {
			forceToUseMouseClickEvent = true;
			attributeValue = attributeValue.replaceAll(MOUSE_CLICK_EVENT_KEYWORD+"$", "");
		}

		StringBuffer res = new StringBuffer("");
		String tmp = attributeValue;
		tmp = XULUtils.replaceValueByRSCSequence(tmp);
		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('input','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");

		if ( !engine.isNoWait )
		{
			res.append("steps[steps.length] = \"resetDocumentLoaded();\"; \n");
		}
		
		if (forceToUseMouseClickEvent) {
			res.append(engine.STEPS_LIST_VAR + " = \"generateClickEventOnCurrentElementForceUseMouseClickEvent();\"; \n");
		} else {
			res.append(engine.STEPS_LIST_VAR + " = \"generateClickEventOnCurrentElement();\"; \n");
		}
		
		if ( !engine.isNoWait )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"ensureDOMisLoaded();\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"ensureNoErrors();\"; \n");
		}

		XULUtils.addToBuffer(res);
	}

	public void clickUploadFile(String attributeName, String attributeValue) throws Exception
	{
		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('input','" + attributeName + "','" + attributeValue + "',true);\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"clickUploadFile();\"; \n");
		XULUtils.addToBuffer(res);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		String inputBy = getParameterValue(HTMLTemplate.INPUT_BY);
		if ( inputBy == null )
			throw new Exception("The inputBy parameter is missing in the command");

		String parameter1 = getParameterValue(HTMLTemplate.PARAM1);
		if ( parameter1 == null )
			throw new Exception("The parameter1 is missing in the command");

		if ( this.getName().equalsIgnoreCase(HTMLTemplateManager.TEMPLATE_INPUT_SUBMIT) )
		{
			clickInputByAttributeName(inputBy, parameter1);
		}
		else if ( inputBy.equalsIgnoreCase("FILE") )
		{
			clickUploadFile(parameter1, getParameterValue(HTMLTemplate.PARAM2));
		}
		else
		{
			String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
			if ( parameter2 == null )
				throw new Exception("The parameter2 is missing in the command");

			setInputValueByAttributeName(inputBy, parameter1, parameter2);
		}
	}

}