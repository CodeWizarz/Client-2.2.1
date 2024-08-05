package com.rapidesuite.build.core.htmlplayback;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.core.action.AbstractAction;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplateManager;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;

public class HTMLRunner extends AbstractAction
{

	private String ilFileName;
	private InputStream ilFileInputStream;
	private String portalHostName;
	private String portalPortNumber;
	private int engineToLoad;
	private File reverseOutputFolder;
	private boolean IS_TEST;
	private String protocol;

	public HTMLRunner(Injector injector, String ilFileName, InputStream ilFileInputStream, Map<String, String> environmentProperties, ActionManager actionManager) throws Exception
	{
		super(injector, ilFileInputStream, environmentProperties, actionManager);
		this.ilFileName = ilFileName;
		this.ilFileInputStream = ilFileInputStream;
		IS_TEST = false;

		String fldURL = actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsURL();

		int startIndex = fldURL.indexOf("://");
		if ( startIndex == -1 )
		{
			throw new Exception("The URL is incorrect: cannot find protocol :// ");
		}
		String tempFLDURL = fldURL.substring(startIndex + 3);
		protocol = fldURL.substring(0, startIndex);
		startIndex = tempFLDURL.indexOf(":");

		if ( startIndex == -1 )
		{
			Assert.notNull(this.protocol);
			Assert.isTrue("http".equalsIgnoreCase(this.protocol) ^ "https".equalsIgnoreCase(this.protocol));

			if ("http".equalsIgnoreCase(this.protocol)) {
				portalPortNumber = "80";
			} else if ("https".equalsIgnoreCase(this.protocol)) {
				portalPortNumber = "443";
			}


			startIndex = tempFLDURL.indexOf("/");
			if ( startIndex == -1 )
			{
				throw new Exception("The URL is incorrect: cannot find host name.");
			}
			portalHostName = tempFLDURL.substring(0, startIndex);
		}
		else
		{
			portalHostName = tempFLDURL.substring(0, startIndex);
			tempFLDURL = tempFLDURL.substring(startIndex + 1);
			startIndex = tempFLDURL.indexOf("/");
			if ( startIndex != -1 )
			{
				portalPortNumber = tempFLDURL.substring(0, startIndex);
			}
		}
		this.engineToLoad = HTMLRunnerConstants.HTML_ENGINE_XUL;
		Assert.isTrue(NumberUtils.isDigits(portalPortNumber), "portal port number must be integer");
	}

	public HTMLRunner(Properties properties) throws Exception
	{
		super(null, null, null, null);
		IS_TEST = true;
		ilFileName = Config.getBuildTestIlFile();
		portalHostName = Config.getBuildTestIlHostName();
		portalPortNumber = Config.getBuildTestIlPortNumber();
		protocol = "http";
		this.engineToLoad = HTMLRunnerConstants.HTML_ENGINE_XUL;
	}

	public HTMLRunner(Properties properties, String ilFileName) throws Exception
	{
		super(null, null, null, null);
		IS_TEST = true;
		this.ilFileName = ilFileName;
		this.engineToLoad = HTMLRunnerConstants.HTML_ENGINE_XUL;
	}

	public InputStream getILFileInputStream()
	{
		return ilFileInputStream;
	}

	public String getILFileName()
	{
		return ilFileName;
	}

	public String getPortalHostName()
	{
		return portalHostName;
	}

	public String getPortalPortNumber()
	{
		return portalPortNumber;
	}

	public File getReverseOutputFolder()
	{
		return this.reverseOutputFolder;
	}

	public ActionManager getActionManager()
	{
		return actionManager;
	}

	public void processILcommands() throws Exception
	{
		if ( engineToLoad == HTMLRunnerConstants.HTML_ENGINE_XUL )
		{

			if ( IS_TEST )
			{
				System.out.println("Using temp file: " + ilFileName);
				ilFileInputStream = new FileInputStream(ilFileName);
			}
			XULEngine xulEngine = new XULEngine(this);
			HTMLTemplateManager templateManager = new HTMLTemplateManager();
			templateManager.initialize(HTMLRunnerConstants.HTML_ENGINE_XUL);
			xulEngine.initialize(templateManager);
			xulEngine.start(injector);
			while ( !xulEngine.isCompleted() )
			{
				com.rapidesuite.client.common.util.Utils.sleep(1000);
			}
			fireActionCompletedEvent();

			return;
		}
		throw new Exception("Unsupported HTML engine: '" + engineToLoad + "'");
	}

	public static void main(String[] args)
	{
		try
		{
            FileUtils.init(Config.getLogFolder(),CoreConstants.SHORT_APPLICATION_NAME.build,false);
			SwiftBuildFileUtils.cleanupTempFolder();
			Properties properties = new Properties();
			FileInputStream is = null;
			try
			{
				is = new FileInputStream(UtilsConstants.ENGINE_PROPERTIES_FILE_NAME);
				properties.load(is);
			}
			finally
			{
				if ( is != null )
				{
					is.close();
				}
			}

			HTMLRunner htmlRunner = null;
			if ( args.length > 0 )
			{
				htmlRunner = new HTMLRunner(properties, args[0]);
			}
			else
			{
				htmlRunner = new HTMLRunner(properties);
			}
			htmlRunner.processILcommands();
			System.exit(0);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	public String getProtocol()
	{
		return protocol;
	}

    @Override
    public void join() throws InterruptedException
    {
        //nothing

    }
}