package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULTextAreaTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULTextAreaTemplate(String templateName)
	{
		super(templateName);
	}

	public void setValueByAttributeName(String attributeName, String attributeValue, String value) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);
		checkParameter(value);

		StringBuffer res = new StringBuffer("");

		String tmp = attributeValue;
		tmp = XULUtils.replaceValueByRSCSequence(tmp);

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('textarea','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");

		value = value.replaceAll("'", "\\\\\\\\'");
		value = value.replaceAll("\"", "\\\\\"");
		value = value.replaceAll("<br/>", "\\\\\\\\n");

		res.append(engine.STEPS_LIST_VAR + " = \"setInputValue('" + value + "');\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"generateHTMLEventOnCurrentElement('change');\"; \n");

		XULUtils.addToBuffer(res);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		String inputBy = getParameterValue(HTMLTemplate.INPUT_BY);
		if ( inputBy == null )
		{
			throw new Exception("The inputBy parameter is missing in the command");
		}

		String parameter1 = getParameterValue(HTMLTemplate.PARAM1);
		if ( parameter1 == null )
		{
			throw new Exception("The parameter1 is missing in the command");
		}

		String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
		if ( parameter2 == null )
		{
			throw new Exception("The parameter2 is missing in the command");
		}

		setValueByAttributeName(inputBy, parameter1, parameter2);
	}

}