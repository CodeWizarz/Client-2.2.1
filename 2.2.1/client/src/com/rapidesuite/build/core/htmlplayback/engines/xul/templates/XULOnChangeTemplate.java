package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULOnChangeTemplate extends HTMLTemplate {

	private XULEngine engine;
	
	public XULOnChangeTemplate(String templateName) {
		super(templateName);
	}

	public void setSelectValueByAttributeName(String attributeName, String attributeValue) throws Exception{
		setSelectValueByAttributeName(attributeName, attributeValue, "", true);
	}
	
	public void setSelectValueByAttributeName(String attributeName, String attributeValue, String value, boolean isSelectOptionByText) throws Exception
	{
		checkParameter(attributeName);
		checkParameter(attributeValue);
		checkParameter(value);

		StringBuffer res = new StringBuffer("");
		String tmp = attributeValue;
		tmp = XULUtils.replaceValueByRSCSequence(tmp);
		res.append(engine.STEPS_LIST_VAR + " = \"getElementByMatchingSearchAttributeValue('select','onchange','" + attributeName + "');\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"setSelectOption('" + tmp + "'," + isSelectOptionByText + ");\"; \n");
		XULUtils.addToBuffer(res);
	}

	@Override
	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		String containString = getParameterValue(HTMLTemplate.CONTAINSSTRING);
		if ( containString == null )
		{
			throw new Exception("The containString parameter is missing in the command");
		}

		String parameter1 = getParameterValue(HTMLTemplate.PARAM1);
		if ( parameter1 == null )
		{
			throw new Exception("The parameter1 is missing in the command");
		}

		setSelectValueByAttributeName(containString, parameter1);
	}

}
