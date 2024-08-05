package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULCheckBoxTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULCheckBoxTemplate(String templateName)
	{
		super(templateName);
	}

	public void clickCheckBoxByAttributeName(String attributeName, String attributeValue, boolean checked) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);

		StringBuffer res = new StringBuffer("");
		String tmp = attributeValue;
		tmp = XULUtils.replaceValueByRSCSequence(tmp);

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('input','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");

		res.append(engine.STEPS_LIST_VAR + " = \"setCheckBox(" + checked + ");\"; \n");
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

		String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
		if ( parameter2 == null )
			throw new Exception("The parameter2 is missing in the command");

		boolean checked = false;
		try
		{
			checked = Boolean.valueOf(parameter2).booleanValue();
		}
		catch ( Exception e )
		{
			throw new Exception("Cannot convert the parameter to a boolean.");
		}
		clickCheckBoxByAttributeName(clickBy, parameter1, checked);
	}

}