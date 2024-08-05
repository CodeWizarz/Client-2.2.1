/**************************************************
 * $Revision: 31060 $:
 * $Revision: 31060 $::
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/htmlplayback/engines/xul/templates/XULFunctionTemplate.java $:
 * $Id: XULFunctionTemplate.java 31060 2013-01-09 05:42:28Z john.snell $:
 *
 */

package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULFunctionTemplate extends HTMLTemplate
{
	private XULEngine engine;

	public XULFunctionTemplate(String templateName)
	{
		super(templateName);
	}

	public static enum Command {
		FUNCTION_BEGIN, FUNCTION_END, FUNCTION_CALL,
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		this.engine = (XULEngine) htmlEngine;
		String command = getParameterValue("COMMAND");

		if ( Command.FUNCTION_BEGIN.toString().equals(command) )
		{
			functionBegin();
		}
		else if ( Command.FUNCTION_END.toString().equals(command) )
		{
			functionEnd();
		}
		else if ( Command.FUNCTION_CALL.toString().equals(command) )
		{
			functionCall();
		}
	}

	private static final String KEY_NAME = "NAME";

	private void functionCall()
	{
		String functionName = getParameterValue(KEY_NAME);
		String argument = getParameterValue("ARGUMENT");
		XULUtils.addToBuffer(engine.STEPS_LIST_VAR + " = \"setTimeoutWrapper(curry(" + functionName + ", '" + argument + "'));\"; \n");
	}

	private void functionBegin()
	{
		XULUtils.registerTemporaryAlternateBuffer(new StringBuffer());
	}

	private void functionEnd() throws Exception
	{
		String functionName = getParameterValue(KEY_NAME);

		String rawCommandsToBeInsertedIntoFunction = XULUtils.deRegisterAndRetrieveTemporaryAlternateBuffer();

		// Buffer now consists of:
		// steps[steps.length] = "getElementById('SubmitButton');";
		// steps[steps.length] = "addSleep(1500);";
		// steps[steps.length] = "resetDocumentLoaded();";
		// steps[steps.length] = "generateClickEventOnCurrentElement();";
		// steps[steps.length] = "ensureDOMisLoaded();";

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		pw.println("function " + functionName + "()");
		pw.println("{");
		pw.println("   netscape.security.PrivilegeManager.enablePrivilege(\"UniversalXPConnect\");");
		pw.println("   setDocument();");

		BufferedReader br = new BufferedReader(new StringReader(rawCommandsToBeInsertedIntoFunction));
		String line = br.readLine();
		Pattern p = Pattern.compile("^.*?\\s*?\\=\\s*?\"(.*?)\"\\s*;$");
		while ( line != null )
		{
			line = line.trim();

			// System.err.println("line = " + line);
			if ( line.startsWith("#") || line.length() == 0 )
			{
				line = br.readLine();
				continue;
			}
			Matcher m = p.matcher(line);
			String loc = null;
			if ( m.find() )
			{
				MatchResult mr = m.toMatchResult();
				loc = mr.group(1);
			}
			// System.err.println("loc = " + loc);

			// Now,
			// steps[steps.length] = "getElementById('SubmitButton');";
			// has been converted to
			// getElementById('SubmitButton');

			if ( null != loc && loc.indexOf("addSleep") == -1 && loc.indexOf("ensureDOMisLoaded") == -1 )
			{
				pw.println("   " + loc);
			}

			line = br.readLine();
		}

		pw.println("}");

		XULUtils.insertAtHeadOfBuffer(sw.toString());
	}

}