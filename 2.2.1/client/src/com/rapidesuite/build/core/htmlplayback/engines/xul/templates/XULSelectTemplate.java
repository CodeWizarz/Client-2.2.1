package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.utils.SwiftBuildUtils;

public class XULSelectTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULSelectTemplate(String templateName)
	{
		super(templateName);
	}

	public void setSelectValueByAttributeName(String attributeName, String attributeValue, String value, boolean isSelectOptionByText) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);
		checkParameter(value);

		StringBuffer res = new StringBuffer("");
		String tmp = attributeValue;
		tmp = XULUtils.replaceValueByRSCSequence(tmp);

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('select','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");

		tmp = value;
		tmp = SwiftBuildUtils.replaceSpecialCharacters(tmp);
		tmp = XULUtils.replaceValueByRSCSequence(tmp);

		res.append(engine.STEPS_LIST_VAR + " = \"setSelectOption('" + tmp + "'," + isSelectOptionByText + ");\"; \n");
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
		String parameter3 = getParameterValue(HTMLTemplate.PARAM3);
		boolean isSelectOptionByText = true;
		if ( parameter3 != null )
		{
			isSelectOptionByText = false;
		}
		setSelectValueByAttributeName(inputBy, parameter1, parameter2, isSelectOptionByText);
	}

}