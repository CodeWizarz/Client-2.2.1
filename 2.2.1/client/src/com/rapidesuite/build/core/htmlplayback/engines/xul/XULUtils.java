package com.rapidesuite.build.core.htmlplayback.engines.xul;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;

public class XULUtils
{

	private static StringBuffer primaryFinalOutput = new StringBuffer(1024);
	private static StringBuffer temporaryAlternateBuffer = null;

	private static StringBuffer getCurrentBuffer()
	{
		StringBuffer toReturn = null;
		if ( temporaryAlternateBuffer != null )
		{
			toReturn = temporaryAlternateBuffer;
		}
		else
		{
			toReturn = primaryFinalOutput;
		}
		return toReturn;
	}

	public static void registerTemporaryAlternateBuffer(StringBuffer alternate)
	{
		temporaryAlternateBuffer = alternate;
	}

	public static String deRegisterAndRetrieveTemporaryAlternateBuffer()
	{
		String toReturn = temporaryAlternateBuffer.toString();
		temporaryAlternateBuffer = null;
		return toReturn;
	}

	public static void insertAtHeadOfBuffer(StringBuffer output)
	{
		getCurrentBuffer().insert(0, output);
	}

	public static void insertAtHeadOfBuffer(String output)
	{
		getCurrentBuffer().insert(0, output);
	}

	public static void addToBuffer(StringBuffer output)
	{
		getCurrentBuffer().append(output);
	}

	public static void addToBuffer(String output)
	{
		getCurrentBuffer().append(output);
	}

	public static void resetBuffer() throws Exception
	{
		primaryFinalOutput = new StringBuffer("");
	}

	public static void createStartupXULFile(File file) throws Exception
	{
		String str = 
		"<?xml version='1.0' ?>\n" +
		"<?xml-stylesheet href='chrome://global/skin/' type='text/css'?>\n" +
		"<window\n" +
		"    id='mywindow'\n" +
		"    title='RES HTML playback'\n" +
		"    orient='horizontal'\n" +
		"   xmlns='http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul'>\n" +
		"\n" +
		"<grid flex='1'>\n" +
		"  <columns>\n" +
		"    <column flex='1'/>\n" +
		"    <column/>\n" +
		"  </columns>\n" +
		"  <rows>\n" +
		"    <row>\n" +
		"        <box height='500'>\n" +
		"                <textbox id='blog' flex='1'  multiline='true' readonly='readonly'/>\n" +
		"        </box>\n" +
		"    </row>\n" +
		"    <row>\n" +
		"      <box width='100'>\n" +
		"        <button label='Manual Stop'\n" +
		"          oncommand='stopInjection();'/>\n" +
		"      </box>\n" +
		"   </row>\n" +
		"  </rows>\n" +
		"</grid>\n" +
		"\n" +
		"<script src='xul/prototype_xul_1_6_0_2.js'/>\n" +
		"<script src='xul/rsc.js'/>\n" +
		"<script src='xul/rsc-reverse.js'/>\n" +
		"<script src='rsc-script.js'/>\n" +
		"\n" +
		"\n" +
		"</window>\n";

		SwiftBuildFileUtils.saveToFile(file, str, false);
	}

	/**
	 * Extracts all the parameters, in order, of the form PREFIX000 PREFIX001
	 * ... PREFIXn
	 * 
	 * @param parameterNamePrefix
	 * @param parameters
	 * 
	 * @return
	 */
	public static List<String> extractParamtersWithIncreasingNumericSuffix(String parameterNamePrefix, Map<String, String> parameters)
	{
		List<String> toReturn = new ArrayList<String>();
		DecimalFormat df = new DecimalFormat("000");
		int counter = 0;
		while ( true )
		{
			String key = parameterNamePrefix + df.format(counter);
			String value = parameters.get(key);
			if ( null == value )
			{
				break;
			}
			toReturn.add(value);

			counter++;
		}
		return toReturn;
	}

	public static boolean isExactMatch(String attributeValue)
	{
		return attributeValue.indexOf("SUB_STRING") == -1;
	}

	public static String replaceValueByRSCSequence(String attributeValue)
	{
		if ( attributeValue.indexOf(HTMLTemplate.LOOKUP_VALUE) == -1 )
		{
			return attributeValue;
		}
		String rscSequence = "'+(null == rscSequence? '' : rscSequence)+'";
		String res = attributeValue.replaceFirst(HTMLTemplate.LOOKUP_VALUE, rscSequence);
		return res;
	}

	public static void writeFinalToFile(File file, boolean append) throws Exception
	{
		Writer out = null;
		try
		{
			FileOutputStream fos = new FileOutputStream(file, append);
			out = new OutputStreamWriter(fos, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
			out.write(primaryFinalOutput.toString());
		}
		finally
		{
			if ( out != null )
				out.close();
		}
	}

}