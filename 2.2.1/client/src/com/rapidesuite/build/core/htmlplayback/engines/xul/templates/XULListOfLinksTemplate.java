/**************************************************
 * $Revision: 31060 $:
 * $Revision: 31060 $::
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/htmlplayback/engines/xul/templates/XULListOfLinksTemplate.java $:
 * $Id: XULListOfLinksTemplate.java 31060 2013-01-09 05:42:28Z john.snell $:
 *
 */

package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULListOfLinksTemplate extends HTMLTemplate
{

	public XULListOfLinksTemplate(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		String id = getParameterValue("ID");
		String linkNamePrefix = getParameterValue("LINK_NAME_PREFIX");
		String postClickFunction = getParameterValue("POST_CLICK_FUNCTION");
		String nextXOnlickID = getParameterValue("NEXT_X_LINK_ONCLICK_ID");
		String selectNextXOnChangeID = getParameterValue("SELECT_NEXT_X_ONCHANGE_ID");
		String initialNextLinkClickDepth = getParameterValue("INITIAL_NEXT_LINK_CLICK_DEPTH");
		String paginationSize = getParameterValue("PAGINATION_SIZE");
		String maximumResultsToRetrieve = getParameterValue("MAXIMUM_RESULTS_TO_RETRIEVE");
		String strInsertInvocation = getParameterValue("INSERT_INVOCATION");
		String nextFunctionName = getParameterValue("NEXT_FUNCTION_NAME");

		if ( null == strInsertInvocation || (!strInsertInvocation.trim().equals("true") && !strInsertInvocation.trim().equals("false")) )
		{
			throw new IllegalArgumentException("INSERT_INVOCATION must be a valid boolean value; instead, it was " + strInsertInvocation);
		}
		boolean insertInvocation = strInsertInvocation.trim().equals("true");

		if ( insertInvocation && nextFunctionName != null )
		{
			throw new IllegalArgumentException("nextFunctionName must be null (unspecified) if insertInvocation = true");
		}

		if ( linkNamePrefix == null || postClickFunction == null )
		{
			throw new NullPointerException("linkNamePrefix = " + linkNamePrefix + ", postClickFunction = " + postClickFunction);
		}

		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['prefix'] = '" + linkNamePrefix + "';\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['desiredNextLinkClickDepth'] = " + initialNextLinkClickDepth + ";\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['actualNextLinkClickDepth'] = 0;\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['paginationSize'] = " + paginationSize + ";\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['maximumResultsToRetrieve'] = " + maximumResultsToRetrieve + ";\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['numberOfResultsRetrieved'] = 0;\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['counter'] = 0;\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['postClickFunction'] = " + postClickFunction + ";\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['nextOnclickFunction'] = '" + nextXOnlickID + "';\n");
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['selectNextOnchangeFunction'] = '" + selectNextXOnChangeID + "';\n");

		if ( insertInvocation )
		{
			XULUtils.addToBuffer("steps[steps.length] = \"list_of_links('" + id + "');\";\n ");
			XULUtils.addToBuffer("steps[steps.length] = \"waitUntilGlobalFinishedFlagSet();\";\n ");
		}
		else
		{
			XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "']['nextFunctionName'] = " + nextFunctionName + ";\n");
		}

		// always inserting at the head means that the definition must be the
		// last command
		XULUtils.insertAtHeadOfBuffer("GLOBAL_iterationStateMatrix['" + id + "'] = new Array();\n");
	}
}