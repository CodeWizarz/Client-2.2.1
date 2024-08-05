package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.utils.SwiftBuildUtils;
import com.rapidesuite.client.common.util.FileUtils;

public class XULRadioButtonTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULRadioButtonTemplate(String templateName)
	{
		super(templateName);
	}

	public void clickRadioButtonByAttributeName(String attributeName, String attributeValue, String valueAttributeValue) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);
		checkParameter(valueAttributeValue);

		StringBuffer res = new StringBuffer("");
		String tmp = attributeValue;
		tmp = XULUtils.replaceValueByRSCSequence(tmp);
		if ( attributeName.equalsIgnoreCase("ID") )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getElementById('" + tmp + "');\"; \n");
		}
		else if ( attributeName.equalsIgnoreCase("value") )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('input','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");
		}
		else
		{
			int number = -1;
			try
			{
				number = Integer.valueOf(valueAttributeValue).intValue();
			}
			catch ( Exception nfe )
			{
				FileUtils.printStackTrace(nfe);
			}

			if ( number == -1 )
			{
				res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeNameValue('input','" + attributeName + "','" + tmp + "','" + valueAttributeValue + "',false);\"; \n");
			}
			else
			{
				// we must select the N element:
				res.append(engine.STEPS_LIST_VAR + " = \"getXElementByAttributeValue('input','" + attributeName + "','" + tmp + "',true," + number + ");\"; \n");
			}
		}

		res.append(engine.STEPS_LIST_VAR + " = \"setChecked(true);\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"generateClickEventOnParentOfCurrentElement();\"; \n");

		XULUtils.addToBuffer(res);
	}

	public void clickRadioButtonBySibling(String clickBy, String parameter1, String parameter2) throws Exception
	{
		checkParameter(clickBy);
		checkParameter(parameter1);
		checkParameter(parameter2);

		StringBuffer res = new StringBuffer("");

		String tmp = SwiftBuildUtils.replaceSpecialCharacters(parameter2);

		res.append(engine.STEPS_LIST_VAR + " = \"getElementByTextNode('");
		res.append(parameter1);
		res.append("','");
		res.append(tmp);
		res.append("');\"; \n");

		res.append(engine.STEPS_LIST_VAR + " = \"getPreviousSiblingOfCurrentElement('input')==true?true:true;\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"setChecked(true);\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"generateClickEventOnParentOfCurrentElement();\"; \n");

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

		String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
		if ( parameter2 == null )
		{
			throw new Exception("The parameter2 is missing in the command");
		}

		String parameter3 = getParameterValue(HTMLTemplate.PARAM3);
		if ( parameter3 == null )
		{
			clickRadioButtonByAttributeName(clickBy, parameter1, parameter2);
		}
		else
		{
			clickRadioButtonBySibling(clickBy, parameter1, parameter2);
		}
	}

}