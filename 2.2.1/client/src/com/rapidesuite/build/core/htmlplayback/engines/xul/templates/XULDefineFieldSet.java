/**************************************************
 * $Revision: 31060 $:
 * $Revision: 31060 $::
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/htmlplayback/engines/xul/templates/XULDefineFieldSet.java $:
 * $Id: XULDefineFieldSet.java 31060 2013-01-09 05:42:28Z john.snell $:
 *
 */

package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULDefineFieldSet extends HTMLTemplate
{

	public XULDefineFieldSet(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		Map<String, String> parameters = getParameters();

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String functionName = getParameterValue("NAME");
		String nextXOnlickID = getParameterValue("NEXT_X_LINK_ONCLICK_ID");
		String nextFunctionName = getParameterValue("NEXT_FUNCTION_NAME");
		String fileID = getParameterValue("FILEID");
		String strInsertInvocation = getParameterValue("INSERT_INVOCATION");
		if ( null == strInsertInvocation || (!strInsertInvocation.trim().equals("true") && !strInsertInvocation.trim().equals("false")) )
		{
			throw new IllegalArgumentException("INSERT_INVOCATION must be a valid boolean value; instead, it was " + strInsertInvocation);
		}
		boolean insertInvocation = strInsertInvocation.trim().equals("true");

		List<ReverseFunctionExtractRow> listOfReverseFunctionExtractRows = ReverseFunctionExtractRow.parseReverseFunctionExtractRows(getParameters());

		pw.println("function " + functionName + "()");
		pw.println("{");
		pw.println("   netscape.security.PrivilegeManager.enablePrivilege(\"UniversalXPConnect\");");
		pw.println("   setDocument();");
		pw.println("   ");
		pw.println("   var counter = 0;");
		pw.println("   var name = null;");
		pw.println("   var value = null;");
		pw.println("   while ( true )");
		pw.println("   {");

		ReverseFunctionExtractRow row0 = listOfReverseFunctionExtractRows.get(0);
		String rowSubFunction0 = getFunctionNameForElementType(row0.getElementType());
		pw.println("      " + rowSubFunction0 + "( \"" + row0.getHTMLID() + "\" + counter )");
		pw.println("      if ( null == rscElt )");
		pw.println("      {");
		pw.println("         break;");
		pw.println("      }");

		pw.println("      closeRowInDataXMLOutputFile('" + fileID + "');");
		pw.println("      openRowInDataXMLOutputFile('" + fileID + "');");

		List<String> foreignKeyNames = XULUtils.extractParamtersWithIncreasingNumericSuffix("FOREIGN_KEY_", parameters);
		for ( String foreignKeyName : foreignKeyNames )
		{
			pw.println("      storeColumnValue('" + fileID + "', " + "'" + foreignKeyName + "', " + XULDataXMLFileTemplate.PRIMARY_KEY_ARRAY_NAME + "['" + foreignKeyName + "']);");
		}

		for ( ReverseFunctionExtractRow row : listOfReverseFunctionExtractRows )
		{
			String rowSubFunction = getFunctionNameForElementType(row.getElementType());
			pw.println("      " + rowSubFunction + "( \"" + row.getHTMLID() + "\" + counter )");
			pw.println("      storeColumnValue('" + fileID + "', '" + row.getRSCColumnName() + "', rscElt);");
		}
		pw.println("      counter++;");
		pw.println("   }");

		pw.println("   getNextXLinkByOnclickID('" + nextXOnlickID + "');");
		pw.println("   if ( null != rscElt ) ");
		pw.println("   {");
		pw.println("      generateClickEventOnCurrentElement();");
		pw.println("      setTimeoutWrapper(" + functionName + ");");
		pw.println("   }");
		pw.println("   else");
		pw.println("   {");
		pw.println("      setTimeoutWrapper(" + nextFunctionName + ");");
		pw.println("   }");
		pw.println("   return true;");
		pw.println("}");

		pw.flush();
		XULUtils.insertAtHeadOfBuffer(sw.getBuffer());

		if ( insertInvocation )
		{
			XULUtils.addToBuffer("steps[steps.length] = \"" + functionName + "();\";\n ");
			XULUtils.addToBuffer("steps[steps.length] = \"waitUntilGlobalFinishedFlagSet();\";\n ");
		}
	}

	private static final String getFunctionNameForElementType(XULExtractTemplate.Element elementType)
	{
		String toReturn = null;
		if ( elementType == XULExtractTemplate.Element.CHECKBOX )
		{
			toReturn = "getCheckBoxValueByID";
		}
		else if ( elementType == XULExtractTemplate.Element.DROPDOWN )
		{
			toReturn = "getDropDownSelectedValueByID";
		}
		else if ( elementType == XULExtractTemplate.Element.INPUT )
		{
			toReturn = "getInputFieldTextByID";
		}
		else if ( elementType == XULExtractTemplate.Element.SPAN )
		{
			toReturn = "getSpanTextByID";
		}
		else
		{
			throw new IllegalArgumentException("Unrecognized elementType = " + elementType.toString());
		}
		return toReturn;
	}

}

class ReverseFunctionExtractRow
{
	private static final String PREFIX_ELEMENT_TYPE = "ELEMENT_TYPE_";
	private static final String PREFIX_HTML_ID = "HTML_ID_";
	private static final String PREFIX_RSC_COLUMN_NAME = "RSC_COLUMN_NAME_";

	private XULExtractTemplate.Element elementType = null;

	public XULExtractTemplate.Element getElementType()
	{
		return this.elementType;
	}

	private String htmlID = null;

	public String getHTMLID()
	{
		return this.htmlID;
	}

	private String rscColumnName = null;

	public String getRSCColumnName()
	{
		return this.rscColumnName;
	}

	public ReverseFunctionExtractRow(XULExtractTemplate.Element elementType, String htmlID, String rscColumnName)
	{
		this.elementType = elementType;
		this.htmlID = htmlID;
		this.rscColumnName = rscColumnName;
	}

	public static List<ReverseFunctionExtractRow> parseReverseFunctionExtractRows(Map<String, String> parameters)
	{
		List<ReverseFunctionExtractRow> toReturn = new ArrayList<ReverseFunctionExtractRow>();

		DecimalFormat df = new DecimalFormat("000");
		int counter = 0;
		while ( true )
		{
			String key = PREFIX_ELEMENT_TYPE + df.format(counter);
			String value = parameters.get(key);
			if ( null == value )
			{
				break;
			}

			String elementType = value;

			key = PREFIX_HTML_ID + df.format(counter);
			value = parameters.get(key);
			if ( null == value )
			{
				throw new IllegalArgumentException("Found a elementType but not an htmlID.  Type: " + elementType);
			}
			String htmlID = value;

			key = PREFIX_RSC_COLUMN_NAME + df.format(counter);
			value = parameters.get(key);
			if ( null == value )
			{
				throw new IllegalArgumentException("Found a elementType but not a rscColumnName.  Type: " + elementType);
			}
			String rscColumnName = value;

			toReturn.add(new ReverseFunctionExtractRow(XULExtractTemplate.getElementEnumByName(elementType), htmlID, rscColumnName));

			counter++;
		}
		return toReturn;
	}
}