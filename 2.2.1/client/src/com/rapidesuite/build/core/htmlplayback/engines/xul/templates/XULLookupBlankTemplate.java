package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.core.utility.CoreUtil;

public class XULLookupBlankTemplate extends HTMLTemplate
{

	private XULEngine engine;
	static final String RSC_LOOKUP_SEPARATOR = "###";

	public XULLookupBlankTemplate(String templateName)
	{
		super(templateName);
	}

	public void setRowIndexByAttributeName(String tagName, String attributeName, String attributeValue, String targetAttributeName, String sleepOrNot) throws Exception
	{
		checkParameter(tagName);
		checkParameter(attributeName);
		checkParameter(attributeValue);
		checkParameter(targetAttributeName);

		StringBuffer res = new StringBuffer("");

		final String SUB_STRING = "SUB_STRING";
		boolean isExactMatch = true;
		String tmp = attributeValue;
		if ( tmp != null )
		{
			int indexOf = tmp.indexOf(SUB_STRING);
			if ( indexOf != -1 )
			{
				isExactMatch = false;
				tmp = tmp.replaceAll(SUB_STRING, "");
			}
		}
		tmp = XULUtils.replaceValueByRSCSequence(tmp);

		boolean sleep = true;
		try {
			sleep = CoreUtil.parseBoolean(sleepOrNot);
		} catch (Exception ex) {
			// do nothing
		}
		if(sleep) {
			res.append(engine.STEPS_LIST_VAR + " = \"addSleep(" + XULEngine.resetDomLoadingTime + ");\"; \n");
		}
		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeBlankValue('" + tagName + "','" + attributeName + "','" + tmp + "'," + isExactMatch + ");\"; \n");

		int indexOfColumn = targetAttributeName.indexOf(RSC_LOOKUP_SEPARATOR);
		if ( indexOfColumn != -1 )
		{
			getSequenceByRSCLookupSeparator(engine, res, targetAttributeName);
		}
		else
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getAttributeValue('" + targetAttributeName + "');\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"getSequence();\"; \n");
		}
		XULUtils.addToBuffer(res);
	}

	public static void getSequenceByRSCLookupSeparator(XULEngine engine, StringBuffer res, String targetAttributeName)
	{
		int indexOfColumn = targetAttributeName.indexOf(RSC_LOOKUP_SEPARATOR);

		String prefix = getPrefix(targetAttributeName, indexOfColumn);
		String suffix = getSuffix(targetAttributeName, indexOfColumn);

		res.append(engine.STEPS_LIST_VAR + " = \"getAttributeValue('" + targetAttributeName.substring(0, indexOfColumn) + "');\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"getSequenceByToken('" + prefix + "','" + suffix + "');\"; \n");
	}

	public static String getPrefix(String targetAttributeName, int indexOfColumn)
	{
		String temp = targetAttributeName.substring(indexOfColumn + RSC_LOOKUP_SEPARATOR.length());
		int indexOfColumnPrefix = temp.indexOf(RSC_LOOKUP_SEPARATOR);
		String prefix = "";
		if ( indexOfColumnPrefix != -1 )
		{
			prefix = temp.substring(0, indexOfColumnPrefix);
		}
		else
		{
			prefix = temp;
		}
		return prefix;
	}

	public static String getSuffix(String targetAttributeName, int indexOfColumn)
	{
		String temp = targetAttributeName.substring(indexOfColumn + RSC_LOOKUP_SEPARATOR.length());
		int indexOfColumnPrefix = temp.indexOf(RSC_LOOKUP_SEPARATOR);
		String suffix = "";
		if ( indexOfColumnPrefix != -1 )
		{
			suffix = temp.substring(indexOfColumnPrefix + RSC_LOOKUP_SEPARATOR.length());
		}
		return suffix;
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		String lookupBy = getParameterValue(HTMLTemplate.LOOKUP_BY);
		if ( lookupBy == null )
		{
			throw new Exception("The LOOKUP_BY value is missing in the command");
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
			throw new Exception("The parameter3 is missing in the command");
		}

		String parameter4 = getParameterValue(HTMLTemplate.PARAM4);

		setRowIndexByAttributeName(parameter1, lookupBy, parameter2, parameter3, parameter4);
	}

}