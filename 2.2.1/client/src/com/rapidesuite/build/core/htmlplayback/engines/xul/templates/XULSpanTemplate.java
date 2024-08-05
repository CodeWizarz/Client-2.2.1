package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULSpanTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULSpanTemplate(String templateName)
	{
		super(templateName);
	}

	public void clickSpanByAttributeName(String attributeName, String attributeValue) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);

		StringBuffer res = new StringBuffer("");

		String rscSequence = "";
		String tmp = attributeValue;
		if ( tmp.indexOf(HTMLTemplate.LOOKUP_VALUE) != -1 )
		{
			tmp = tmp.replaceFirst(HTMLTemplate.LOOKUP_VALUE, "");
			rscSequence = "+rscSequence";
		}
		if ( attributeName.equalsIgnoreCase("ID") )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getElementById('" + tmp + "'" + rscSequence + ");\"; \n");
		}
		else
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('span','" + attributeName + "','" + attributeValue + "'" + rscSequence + ",true);\"; \n");
		}
		res.append(engine.STEPS_LIST_VAR + " = \"generateClickEventOnCurrentElement();\"; \n");
		XULUtils.addToBuffer(res);
	}

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

		clickSpanByAttributeName(clickBy, parameter1);
	}

}