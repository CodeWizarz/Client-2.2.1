package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.htmlplayback.HTMLRunnerConstants;
import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.client.common.util.FileUtils;

public class XULLoadURLTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULLoadURLTemplate(String templateName)
	{
		super(templateName);
	}

	public void loadURL(String url) throws Exception
	{
		checkParameter(url);

		String tmp = url;
		int startIndex = tmp.indexOf("://");
		if ( startIndex == -1 )
		{
			throw new Exception("The URL is incorrect: cannot find protocol :// ");
		}
		tmp = engine.getProtocol() + tmp.substring(startIndex);
		tmp = tmp.replaceFirst(HTMLRunnerConstants.MARKER_PORTAL_HOST_NAME, engine.getPortalHostName());
		tmp = tmp.replaceFirst(HTMLRunnerConstants.MARKER_PORTAL_PORT_NUMBER, engine.getPortalPortNumber());

		if (StringUtils.isNotBlank(this.engine.getRunner().getActionManager().getBuildMain().getEnvironmentProperties().get(SwiftBuildConstants.BUILD_HTML_URL_OVERRIDE)))
		{
			String oldUrl = tmp;
			tmp = this.engine.getRunner().getActionManager().getBuildMain().getEnvironmentProperties().get(SwiftBuildConstants.BUILD_HTML_URL_OVERRIDE);
			FileUtils.println("HTML URL OVERRIDE:\n"
					+ "OLD URL: " + oldUrl + "\n"
					+ "NEW URL: " + tmp);
		}
		
		StringBuffer res = new StringBuffer("");
		res.append("rscBrowser.contentDocument.location.href = '" + tmp + "';\n");
		res.append(engine.STEPS_LIST_VAR + " = \"ensureDOMisLoaded();\"; \n");
		res.append(engine.STEPS_LIST_VAR + " = \"ensureNoErrors();\"; \n");
		XULUtils.addToBuffer(res);
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;

		String url = "";
		String typeValue = getParameterValue(HTMLTemplate.LOAD_URL_TYPE);
		if ( typeValue == null )
			throw new Exception("The parameter: " + HTMLTemplate.LOAD_URL_TYPE + " is missing in the command");

		String uriValue = getParameterValue(HTMLTemplate.PARAM1);
		if ( uriValue == null )
			throw new Exception("The URI value is missing in the command");

		if ( typeValue.equalsIgnoreCase("URL") )
		{
			url = uriValue;
		}
		else if ( typeValue.equalsIgnoreCase("FILE") )
		{
			System.out.println("uri:" + uriValue);
			url = uriValue;
		}
		else
			throw new Exception("The type of URI: '" + typeValue + "' is unknown");

		loadURL(url);

	}

}