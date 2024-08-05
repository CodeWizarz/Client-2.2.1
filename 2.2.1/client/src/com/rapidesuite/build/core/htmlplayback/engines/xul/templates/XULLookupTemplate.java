package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import java.util.ArrayList;
import java.util.List;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.utils.SwiftBuildUtils;

public class XULLookupTemplate extends HTMLTemplate
{

	private XULEngine engine;

	private List<String> lookups;
	public final static String LOOKUP_BY_ROW = "ROW";
	public final static String LOOKUP_BY_VISUAL_ROW = "VISUAL_ROW";
	public final static String LOOKUP_BY_SIBLINGS = "SIBLINGS";

	public XULLookupTemplate(String templateName)
	{
		super(templateName);
	}

	public void setRowIndexByTagTextValue(String tagName, String nodeValue, String targetAttributeName) throws Exception
	{
		checkParameter(tagName);
		checkParameter(nodeValue);
		checkParameter(targetAttributeName);

		if ( nodeValue == null )
		{
			throw new Exception("The nodeValue is missing");
		}

		String tmp = nodeValue;
		tmp = SwiftBuildUtils.replaceSpecialCharacters(nodeValue);

		StringBuffer res = new StringBuffer("");

		int indexOfColumn = targetAttributeName.indexOf(XULLookupBlankTemplate.RSC_LOOKUP_SEPARATOR);
		if ( indexOfColumn != -1 )
		{
			String attributeName = targetAttributeName.substring(0, indexOfColumn);
			String prefix = XULLookupBlankTemplate.getPrefix(targetAttributeName, indexOfColumn);
			res.append(engine.STEPS_LIST_VAR + " = \"getElementByTextNodeSelectiveAttributeValue('" + tagName + "','" + tmp + "','" + attributeName + "','" + prefix + "');\"; \n");
			XULLookupBlankTemplate.getSequenceByRSCLookupSeparator(engine, res, targetAttributeName);
		}
		else if ( targetAttributeName.equalsIgnoreCase(CLICK_BY_TEXT) )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getElementByTextNode('" + tagName + "','" + tmp + "');\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"getSequenceByTextNode('" + tmp + "');\"; \n");
		}
		else
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getElementByTextNodeSelective('" + tagName + "','" + tmp + "','" + targetAttributeName + "');\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"getAttributeValue('" + targetAttributeName + "');\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"getSequence();\"; \n");
		}
		XULUtils.addToBuffer(res);
	}

	public void setLookupSequenceByTextNode() throws Exception
	{
		StringBuffer res = new StringBuffer("");
		res.append(engine.STEPS_LIST_VAR + " = \"getLookupSequenceManyColumns(");

		for ( int i = 0; i < lookups.size(); i++ )
		{
			String param = lookups.get(i);
			res.append("'");
			param=SwiftBuildUtils.replaceSpecialCharacters(param);
			res.append(param);
			res.append("'");

			if ( (i + 1) < lookups.size() )
				res.append(",");
		}
		res.append(");\"; \n");
		lookups = null;
		XULUtils.addToBuffer(res);
	}

	public void setRowIndexByAttributeName(String tagName, String attributeName, String attributeValue, String targetAttributeName) throws Exception
	{
		checkParameter(tagName);
		checkParameter(attributeName);
		checkParameter(attributeValue);
		checkParameter(targetAttributeName);

		StringBuffer res = new StringBuffer("");
		String tmp = SwiftBuildUtils.replaceSpecialCharacters(attributeValue);
		tmp = XULUtils.replaceValueByRSCSequence(tmp);
		res.append(engine.STEPS_LIST_VAR + " = \"getElementByAttributeValue('" + tagName + "','" + attributeName + "','" + tmp + "'," + XULUtils.isExactMatch(tmp) + ");\"; \n");
		int indexOfColumn = targetAttributeName.indexOf(XULLookupBlankTemplate.RSC_LOOKUP_SEPARATOR);
		if ( indexOfColumn != -1 )
		{
			XULLookupBlankTemplate.getSequenceByRSCLookupSeparator(engine, res, targetAttributeName);
		}
		else
		{
			tmp = SwiftBuildUtils.replaceSpecialCharacters(targetAttributeName);
			res.append(engine.STEPS_LIST_VAR + " = \"getAttributeValue('" + tmp + "');\"; \n");
			res.append(engine.STEPS_LIST_VAR + " = \"getSequence();\"; \n");
		}
		XULUtils.addToBuffer(res);
	}

	public void getRowSequenceByTextNodePosition(String tagName, String textNode) throws Exception
	{
		getRowSequenceByType(LOOKUP_BY_ROW, tagName, textNode);
	}

	public void getElementAndSetSiblingsFlag(String tagName, String textNode) throws Exception
	{
		getRowSequenceByType(LOOKUP_BY_SIBLINGS, tagName, textNode);
	}

	public void getVisualRowSequenceByTextNodePosition(String tagName, String textNode) throws Exception
	{
		getRowSequenceByType(LOOKUP_BY_VISUAL_ROW, tagName, textNode);
	}

	public void getRowSequenceByType(String type, String tagName, String textNode) throws Exception
	{
		checkParameter(tagName);
		checkParameter(textNode);

		textNode = SwiftBuildUtils.replaceSpecialCharacters(textNode);

		StringBuffer res = new StringBuffer("");
		
		if (!USE_CURRENTLY_REFERRED_ELEMENT_KEYWORD.equals(tagName)) {
			res.append(engine.STEPS_LIST_VAR + " = \"getElementByTextNode('" + tagName + "','" + textNode + "');\"; \n");
		}
		
		if ( type.equalsIgnoreCase(LOOKUP_BY_ROW) )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getRowSequence();\"; \n");
		}
		else if ( type.equalsIgnoreCase(LOOKUP_BY_SIBLINGS) )
		{
			res.append(engine.STEPS_LIST_VAR + " = \"setSiblingsFlag(true);\"; \n");
		}
		else
		{
			res.append(engine.STEPS_LIST_VAR + " = \"getVisualSequence();\"; \n");
		}
		XULUtils.addToBuffer(res);
	}

	public List<String> getLookupParameters() throws Exception
	{
		List<String> res = new ArrayList<String>();

		String lookupBy = getParameterValue(HTMLTemplate.LOOKUP_BY);
		if ( lookupBy == null )
			throw new Exception("The LOOKUP_BY value is missing in the command");
		res.add(lookupBy);

		String parameter1 = getParameterValue(HTMLTemplate.PARAM1);
		if ( parameter1 == null )
			throw new Exception("The parameter1 is missing in the command");
		res.add(parameter1);

		String parameter2 = getParameterValue(HTMLTemplate.PARAM2);
		if ( parameter2 == null )
			throw new Exception("The parameter2 is missing in the command");
		res.add(parameter2);

		String parameter3 = getParameterValue(HTMLTemplate.PARAM3);
		if ( parameter3 != null )
		{
			res.add(parameter3);
		}
		return res;
	}

	public void addParams(List<String> params)
	{
		if ( lookups == null )
			lookups = new ArrayList<String>();

		lookups.addAll(params);
	}
	
	public static final String USE_CURRENTLY_REFERRED_ELEMENT_KEYWORD = "USE_CURRENTLY_REFERRED_ELEMENT";

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		if ( lookups != null && lookups.size() > 3 && !USE_CURRENTLY_REFERRED_ELEMENT_KEYWORD.equals(lookups.get(3)))
		{
			setLookupSequenceByTextNode();
			return;
		}
		lookups = null;

		List<String> list = getLookupParameters();

		if ( list.get(0).trim().equalsIgnoreCase(CLICK_BY_TEXT) )
		{
			setRowIndexByTagTextValue(list.get(1), list.get(2), list.get(3));
		}
		else if ( list.get(0).trim().equalsIgnoreCase(LOOKUP_BY_ROW) )
		{
			getRowSequenceByTextNodePosition(list.get(1), list.get(2));
		}
		else if ( list.get(0).trim().equalsIgnoreCase(LOOKUP_BY_VISUAL_ROW) )
		{
			getVisualRowSequenceByTextNodePosition(list.get(1), list.get(2));
		}
		else if ( list.get(0).trim().equalsIgnoreCase(LOOKUP_BY_SIBLINGS) )
		{
			getElementAndSetSiblingsFlag(list.get(1), list.get(2));
		}
		else
		{
			setRowIndexByAttributeName(list.get(1), list.get(0), list.get(2), list.get(3));
		}
	}

}