/**************************************************
 * $Revision: 39019 $:
 * $Revision: 39019 $::
 * $Date: 2014-02-13 17:01:40 +0700 (Thu, 13 Feb 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/htmlplayback/engines/xul/templates/XULAlphabeticIterator.java $:
 * $Id: XULAlphabeticIterator.java 39019 2014-02-13 10:01:40Z john.snell $:
 *
 */

package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.client.common.PlatformNotSupportedError;
import com.sun.jna.Platform;

public class XULAlphabeticIterator extends HTMLTemplate
{

	public XULAlphabeticIterator(String templateName)
	{
		super(templateName);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		String inputFieldID = getParameterValue("INPUT_FIELD_ID");
		String nextFunctionName = getParameterValue("NEXT_FUNCTION_NAME");
		String nextFunctionArgument = getParameterValue("NEXT_FUNCTION_ARGUMENT");
		String letterRangesURLEncoded = getParameterValue("LETTER_RANGES");
		String letterRanges = URLDecoder.decode(letterRangesURLEncoded, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
		
		PrintWriter pw  = null;
		if(Platform.isWindows()) {
			pw = new PrintWriter("c:/sb.temp.txt", com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
		} else if(Platform.isLinux()) {
			pw = new PrintWriter("/tmp/sb.temp.txt", com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
		} else {
			throw new PlatformNotSupportedError();
		}
		pw.println(letterRanges);

		int counter = -1;
		for ( StringTokenizer st = new StringTokenizer(letterRanges, ","); st.hasMoreTokens(); )
		{
			counter++;
			String range = st.nextToken();
			Character start = null;
			Character end = null;
			if ( range.indexOf("-") != -1 )
			{
				StringTokenizer st2 = new StringTokenizer(range, "-");
				String strStart = st2.nextToken();
				String strEnd = st2.nextToken();
				start = strStart.charAt(0);
				end = strEnd.charAt(0);
				if ( strStart.length() != 1 || strEnd.length() != 1 )
				{
					System.err.println("strStart = '" + strStart + "', strEnd = '" + strEnd + "', start = '" + start + "', end = '" + end + "'");
					// throw new
					// IllegalArgumentException("XSD failed to enforce range specification; received start/end range greater than one character in length.  range = '"
					// + range + "'");
				}
				start = strStart.charAt(0);
				end = strEnd.charAt(0);

				if ( start.compareTo(end) > 0 )
				{
					// they wrote the range backwards; fix that.
					Character temp = start;
					start = end;
					end = temp;
				}
			}
			else
			{
				// single character
				if ( range.length() != 1 )
				{
					// throw new
					// IllegalArgumentException("XSD failed to enforce range specification; received single character greater than one character in length.  range = '"
					// + range + "'");
				}
				start = range.charAt(0);
				end = range.charAt(0);
			}

			int characterRangeLength = (getUnicodeNumberForCharacter(end) - getUnicodeNumberForCharacter(start)) + 1;

			pw.println(start);
			pw.println(getUnicodeNumberForCharacter(start));
			pw.println(end);
			pw.println(characterRangeLength);

			XULUtils.insertAtHeadOfBuffer("GLOBAL_al_letterRanges_start[" + counter + "] = " + getUnicodeNumberForCharacter(start) + ";\n");
			XULUtils.insertAtHeadOfBuffer("GLOBAL_al_letterRanges_length[" + counter + "] = " + characterRangeLength + ";\n");
			XULUtils.insertAtHeadOfBuffer("GLOBAL_al_letterRanges_current_offset[" + counter + "] = 0;\n");
		}
		pw.close();

		if ( inputFieldID == null || nextFunctionName == null )
		{
			throw new NullPointerException("inputFieldID = " + inputFieldID + ", nextFunctionName = " + nextFunctionName);
		}
		XULUtils.insertAtHeadOfBuffer("var GLOBAL_al_letterRanges_start = new Array();\n");
		XULUtils.insertAtHeadOfBuffer("var GLOBAL_al_letterRanges_length = new Array();\n");
		XULUtils.insertAtHeadOfBuffer("var GLOBAL_al_letterRanges_current_offset = new Array();\n");
		XULUtils.insertAtHeadOfBuffer("var GLOBAL_al_currentRangeIndex = 0;\n");

		XULUtils.insertAtHeadOfBuffer("var GLOBAL_al_inputField = \'" + inputFieldID + "\';\n");
		XULUtils.insertAtHeadOfBuffer("var GLOBAL_al_nextFunction = " + nextFunctionName + ";\n");
		XULUtils.insertAtHeadOfBuffer("var GLOBAL_al_nextFunctionArgument = '" + nextFunctionArgument + "';\n");

		XULUtils.addToBuffer("steps[steps.length] = \"alphabetic_iterator();\";\n ");
		XULUtils.addToBuffer("steps[steps.length] = \"waitUntilGlobalFinishedFlagSet();\";\n ");
	}

	private static int getUnicodeNumberForCharacter(char c)
	{
		return Character.codePointAt(new char[] { c }, 0, 1);
	}
}