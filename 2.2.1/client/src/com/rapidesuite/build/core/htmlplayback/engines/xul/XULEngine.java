package com.rapidesuite.build.core.htmlplayback.engines.xul;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.htmlplayback.HTMLRunner;
import com.rapidesuite.build.core.htmlplayback.HTMLRunnerConstants;
import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULLookupTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULRepeatIfNotFoundTemplate;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplateManager;
import com.rapidesuite.build.core.htmlplayback.utils.HTMLUtils;
import com.rapidesuite.build.utils.CurrentBrowserTask;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.build.utils.TaskListUtils;
import com.rapidesuite.client.common.PlatformNotSupportedError;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.sun.jna.Platform;

public class XULEngine implements HTMLEngine
{
	private boolean isCompleted;
	private HTMLRunner runner;
	private int commandLineId;
	private HTMLTemplateManager templateManager;
	public File javascriptFile;
	private File xulCompletedFile;

	public boolean isPopupWindowOpened;
	public boolean isNoWait;

	public String STEPS_LIST_VAR;
	public static final String STEPS_LIST_VAR_DEFAULT = "steps[steps.length]";

	public int currentRepeatStepIndex;
	public boolean isRepeat;
	public static int resetDomLoadingTime;
	private HTMLTemplate lookupTemplate;
	private File tempHTMLFolder;
	private boolean isSSOEnabled;
	private String ssoFieldIdentifierType;
	private String ssoUserNameFieldName;
	private String ssoPasswordFieldName;
	private String ssoSubmitButtonIdentifier;
	private String ssoSubmitButtonValue;
	
	public static final String HTML_FOLDER_NAME = "html";
	
	public XULEngine(HTMLRunner runner) throws Exception
	{
		isCompleted = false;
		isNoWait = false;
		isRepeat = false;
		currentRepeatStepIndex = -1;
		this.runner = runner;
		STEPS_LIST_VAR = STEPS_LIST_VAR_DEFAULT;
		commandLineId = 0;

		FileUtils.getTemporaryFolder();
		File tempFolderPrefix = FileUtils.getTemporaryFolder();
		tempHTMLFolder = new File(tempFolderPrefix, HTML_FOLDER_NAME);
		if ( !tempHTMLFolder.exists() )
		{
			tempHTMLFolder.mkdirs();
		}
		javascriptFile = new File(tempHTMLFolder, "rsc-script.js");
		javascriptFile.delete();
		xulCompletedFile = new File(tempHTMLFolder, "completed.txt");
		xulCompletedFile.delete();
		resetDomLoadingTime = Config.getBuildResetDomLoadingTimer();
	}

	public File getReverseOutputFolder()
	{
		return runner.getReverseOutputFolder();
	}

	public void addHeaderToFile() throws Exception
	{
		StringBuffer res = new StringBuffer("");
		res.append("try {\n");
		File logsFolder = getLogsFolder();
		String logsfolderAbsolutePath = logsFolder.getAbsolutePath();
		String logFileName = null, deguggingFileName = null;
		if(Platform.isWindows()) {
			logFileName = logsfolderAbsolutePath + "\\" + runner.getILFileName();
			logFileName = logFileName.replaceAll("\\\\", "\\\\\\\\") + ".log";

			deguggingFileName = logsfolderAbsolutePath + "\\" + runner.getILFileName();
			deguggingFileName = deguggingFileName.replaceAll("\\\\", "\\\\\\\\") + ".debug";
		} else if(Platform.isLinux()) {
			logFileName = logsfolderAbsolutePath + UtilsConstants.FORWARD_SLASH + runner.getILFileName();
			logFileName = logFileName + ".log";

			deguggingFileName = logsfolderAbsolutePath + UtilsConstants.FORWARD_SLASH + runner.getILFileName();
			deguggingFileName = deguggingFileName + ".debug";
		} else {
			throw new PlatformNotSupportedError();
		}

		File logFileT = new File(logFileName);
		if ( logFileT.exists() )
		{
			logFileT.delete();
		}
		File deguggingFile = new File(deguggingFileName);
		if ( deguggingFile.exists() )
		{
			deguggingFile.delete();
		}

		res.append("logFileName='" + logFileName + "';\n");
		res.append("deguggingFileName='" + deguggingFileName + "';\n");
		res.append("isDebug=" + Config.getBuildHtmlDebug() + ";\n");
		res.append("netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');\n");

		XULUtils.addToBuffer(res);
	}

	public File getLogsFolder()
	{
		File logsFolder = null;

		ActionManager det = runner.getActionManager();
		if ( det != null )
		{
			File injectorsPackageFile = det.getBuildMain().getInjectorsPackageSelectionPanel().getInjectorsPackageFile();
			String injectorsPackageFileName = injectorsPackageFile.getName();
			logsFolder = SwiftBuildFileUtils.getLogsFolderFromName(injectorsPackageFileName);
		}
		else
		{
			logsFolder = new File("test");
			logsFolder.mkdirs();
		}

		return logsFolder;
	}

	public void addFooterToFile() throws Exception
	{
		StringBuffer res = new StringBuffer("");

		String completedFileURL = this.xulCompletedFile.getAbsolutePath();
		if(Platform.isWindows()) {
		completedFileURL = completedFileURL.replaceAll("\\\\.\\\\", "\\\\");
		completedFileURL = completedFileURL.replaceAll(UtilsConstants.FORWARD_SLASH, "\\\\");
		completedFileURL = completedFileURL.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
		}
		res.append(STEPS_LIST_VAR + " = \"scriptCompleted('" + completedFileURL + "');\"; \n");

		if (Config.getBuildForceCloseBrowser())
		{
			res.append("forceCloseWindows=false;\n");
		}
		res.append("if (forceCloseWindows) " + STEPS_LIST_VAR + " = \"closeWindows();\"; \n");
		res.append("execute();\n");
		// removed to Firefox 16.0.2 testing.
//		res.append("netscape.security.PrivilegeManager.disablePrivilege('UniversalXPConnect');\n");
		res.append("}\n");
		res.append("catch (e) {\n");
		res.append("      alert(\"INJECTION ERROR: An exception occurred in the file: 'rsc-script.js' . Error name: \" + e.name+ \". Error message: \" + e.message);\n");
		res.append("}\n");

		XULUtils.addToBuffer(res);
	}

	public String getPortalHostName()
	{
		return runner.getPortalHostName();
	}

	public String getPortalPortNumber()
	{
		return runner.getPortalPortNumber();
	}

	public void finalize() throws Exception
	{
		// do nothing;
	}

	public void start(final Injector injector) throws Exception
	{
		ActionManager actionManager = runner.getActionManager();

		if ( CurrentBrowserTask.isActive() )
		{
			isCompleted = true;
			System.out.println("FIREFOX IS RUNNING... MUST BE CLOSED BEFORE CONTINUING");
			if ( actionManager != null )
			{
				actionManager.stopExecution();
			}

			throw new Exception("Firefox is already opened, you must close it before executing this injector.");
		}

		/****************************************************
		 * CREATE JAVASCRIPT FROM IL TO TEMP FOLDER
		 * **************************************************
		 */
		if ( javascriptFile.exists() )
		{
			boolean isFileDeleted = javascriptFile.delete();
			if ( !isFileDeleted )
			{
				throw new Exception("Cannot delete the temp file '" + javascriptFile + "' located in the temp folder.");
			}
		}
		XULUtils.resetBuffer();
		addHeaderToFile();
		convertToXUL();
		addFooterToFile();
		XULUtils.writeFinalToFile(javascriptFile, true);

		/****************************************************
		 * CREATE STARTUP XUL TO TEMP FOLDER
		 * **************************************************
		 */
		XULUtils.createStartupXULFile(new File(tempHTMLFolder, "startup.xul"));

		/****************************************************
		 * COPY RSC JAVASCRIPT APIS TO TEMP FOLDER
		 * **************************************************
		 */
		org.apache.commons.io.FileUtils.copyFile(new File("xul", "rsc.js"), new File(tempHTMLFolder + UtilsConstants.FORWARD_SLASH + "xul", "rsc.js"));

		/****************************************************
		 * STARTS EXECUTION **************************************************
		 */
		String xulFileURL = new File(tempHTMLFolder.getAbsoluteFile(), "/startup.xul").toURI().toString();
		Config.getBuildFirefoxPath();
		TaskListUtils.startFirefox(actionManager, xulFileURL);
		
		final List<String> partitions = actionManager.getInjectorIndexToPartitionNameMap().get(injector.getIndex());
		Assert.notNull(partitions, "partitions must not be null");
		Assert.isTrue(!partitions.isEmpty(), "there must be partition");
		
		final TaskListUtils.IlInjectionSpecifics ilInjectionSpecifics = new TaskListUtils.IlInjectionSpecifics(xulCompletedFile, partitions.get(partitions.size()-1), actionManager.getBuildMain(),
				injector.getName(), actionManager);
		
		TaskListUtils.waitForScriptToComplete(actionManager,
				Config.getBuildHtmlFormCloseMaxIteration(),
				injector,
				null,
				ilInjectionSpecifics,
				null);	

		TaskListUtils.waitingForCurrentBrowserTaskToClose(runner.getActionManager());

		if ( runner.getActionManager() != null && runner.getActionManager().isExecutionStopped() )
		{
			String msg = "Forcefully closing Browser.";
			runner.getActionManager().setOutput(msg);
			CurrentBrowserTask.eliminateTask();
		}

		isCompleted = true;
		org.apache.commons.io.FileUtils.deleteDirectory(tempHTMLFolder);
	}

	public void convertToXUL() throws Exception
	{
		processILCommands();
	}

	public void processILCommands() throws Exception
	{
		BufferedReader bufferReader = null;
		List<String> ilCommands = new ArrayList<String>();
		try
		{
			InputStream ilFileInputStream = runner.getILFileInputStream();
			bufferReader = new BufferedReader(new InputStreamReader(ilFileInputStream, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING));
			ilCommands = HTMLUtils.readLines(bufferReader);
		}
		finally
		{
			if ( bufferReader != null )
				bufferReader.close();
		}
		processILCommands(ilCommands);
	}

	public boolean isNextTemplate(List<String> ilCommands, int i, String templateName) throws Exception
	{
		if ( (i + 1) >= ilCommands.size() )
		{
			return false;
		}
		for ( int index = (i + 1); index < ilCommands.size(); index++ )
		{
			String command = ilCommands.get(index);
			if ( command == null || command.startsWith(HTMLRunnerConstants.COMMENT_LINE) || command.equals("") )
			{
				continue;
			}

			if ( command.startsWith(templateName) )
			{
				return true;
			}
			return false;
		}
		return false;
	}

	public void processILCommands(List<String> ilCommands) throws Exception
	{
		int commandsCount = ilCommands.size();
		if ( isSSOEnabled )
		{
			ilCommands = replaceLoginILCommandsBySSOLogin(ilCommands);
		}
		for ( int i = 0; i < commandsCount; i++ )
		{
			String command = null;
			HTMLTemplate template = null;
			try
			{
				command = ilCommands.get(i);
				if ( command == null || command.equals("") )
				{
					continue;
				}
				if ( command.startsWith(HTMLRunnerConstants.COMMENT_LINE) )
				{
					StringBuffer tmp = new StringBuffer("");
					tmp.append("// ");
					tmp.append(command);
					tmp.append("\n");
					XULUtils.addToBuffer(tmp);

					tmp = new StringBuffer("");
					command = command.replaceAll("'", "\\\\\"");
					tmp.append(STEPS_LIST_VAR + " = \"logMsg('" + command + "');\"; \n");
					XULUtils.addToBuffer(tmp);

					continue;
				}

				commandLineId++;
				template = templateManager.getTemplate(command);

				boolean isNextTemplateDOMLoading = isNextTemplate(ilCommands, i, HTMLTemplateManager.TEMPLATE_DOM_LOADING);
				if ( isNextTemplateDOMLoading )
				{
					StringBuffer res = new StringBuffer("");
					res.append(STEPS_LIST_VAR + " = \"addSleep(" + resetDomLoadingTime + ");\"; \n");
					res.append(STEPS_LIST_VAR + " = \"resetDocumentLoaded();\"; \n");
					XULUtils.addToBuffer(res);
				}

				isNoWait = isNextTemplate(ilCommands, i, HTMLTemplateManager.TEMPLATE_NO_DOM_LOADING);

				if ( command.startsWith(HTMLTemplateManager.TEMPLATE_REPEAT_IF_NOT_FOUND) )
				{
					isRepeat = true;
				}

				boolean isNextTemplateRepeat = isNextTemplate(ilCommands, i, HTMLTemplateManager.TEMPLATE_REPEAT_IF_NOT_FOUND);
				if ( isNextTemplateRepeat )
				{
					XULRepeatIfNotFoundTemplate.setRepeat(true);
				}

				if ( template instanceof XULLookupTemplate )
				{
					addLookupCommand(template);
					continue;
				}
				if ( hasLookupCommmands() )
				{
					executeLookupCommands();
				}
				template.execute(this);
				isNoWait = false;
				XULUtils.addToBuffer(new StringBuffer("\n"));
			}
			catch ( Exception e )
			{
				FileUtils.printStackTrace(e);
				String msg = e.getMessage();
				if ( msg == null )
				{
					msg = "N/A";
				}
				msg = "ERROR: " + msg + ". COMMAND_LINE_ID:" + commandLineId + " command: '" + command + "'";

				throw new Exception(msg);
			}
		}
	}

	public void addLookupCommand(HTMLTemplate template) throws Exception
	{
		lookupTemplate = template;

		XULLookupTemplate temp = (XULLookupTemplate) template;
		List<String> res = new ArrayList<String>();
		List<String> params = temp.getLookupParameters();
		res.add(params.get(1));
		res.add(params.get(2));
		if ( params.size() == 4 )
		{
			res.add(params.get(3));
		}
		temp.addParams(res);
	}

	public boolean hasLookupCommmands()
	{
		return lookupTemplate != null;
	}

	public void executeLookupCommands() throws Exception
	{
		lookupTemplate.execute(this);
		lookupTemplate = null;
	}

	public boolean isCompleted()
	{
		return isCompleted;
	}

	public void initialize(HTMLTemplateManager templateManager) throws Exception
	{
		this.templateManager = templateManager;
		FileUtils.println("initializing the engine...");

		try
		{
			this.isSSOEnabled= Boolean.TRUE.toString().equalsIgnoreCase(
					this.runner.getActionManager().getBuildMain().getEnvironmentProperties().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON));
			
			this.ssoUserNameFieldName = this.runner.getActionManager().getBuildMain().getEnvironmentProperties().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_USERNAME_FIELD_IDENTIFIER_VALUE);
			this.ssoPasswordFieldName = this.runner.getActionManager().getBuildMain().getEnvironmentProperties().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_PASSWORD_FIELD_IDENTIFIER_VALUE);
			this.ssoFieldIdentifierType = this.runner.getActionManager().getBuildMain().getEnvironmentProperties().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE);
			this.ssoSubmitButtonIdentifier = this.runner.getActionManager().getBuildMain().getEnvironmentProperties().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER);
			this.ssoSubmitButtonValue = this.runner.getActionManager().getBuildMain().getEnvironmentProperties().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER_VALUE);
						
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public List<String> replaceLoginILCommandsBySSOLogin(List<String> ilCommands) throws Exception
	{	
		// userName field REGEX
		// matches case insensitive, looks for the start string "TEMPLATE_INPUT INPUT_BY=ID##%!%##PARAM1=" plus also looks for a "u" (to differentiate between username and password fields)
		// then looks for a free string using the ".*", then looks for the string "##%!%##PARAM2=" plus whatever the username might be using the ".*".
		final String userNameCommandRegex = "(?i)^(TEMPLATE_INPUT INPUT_BY=ID##%!%##PARAM1=u.*##%!%##PARAM2=.*)$";
		// password field REGEX, same as userName field REGEX.
		final String passwordCommandRegex = "(?i)^(TEMPLATE_INPUT INPUT_BY=ID##%!%##PARAM1=p.*##%!%##PARAM2=.*)$";
		// submit button REGEX, same as userName and password field REGEX except for TEMPLATE_BUTTON.
		final String submitButtonValueRegex = "(?i)^(TEMPLATE_BUTTON CLICK_BY=ID##%!%##PARAM1=s.*##%!%##PARAM2=.*)$";

		for ( int i = 0; i < ilCommands.size(); i++ )
		{
			String command = ilCommands.get(i);

			if ( command.equals("#END_HEADER#") )
			{
				break;
			}

			// find using REGEX
			if(command.matches(userNameCommandRegex))
			{
				// extract the userName based on the last index of '=' 
				String userName = command.substring(command.lastIndexOf("=") + 1);
				// recreate the ilCommand using the SSO HTML identifier, SSO HTML value and the userName
				command = "TEMPLATE_INPUT INPUT_BY=" + this.ssoFieldIdentifierType + "##%!%##PARAM1=" + this.ssoUserNameFieldName + "##%!%##PARAM2=" + userName;
				ilCommands.set(i, command);
			}
			// find using REGEX
			else if ( command.matches(passwordCommandRegex) )
			{
				// extract the password based on the last index of '='
				String password = command.substring(command.lastIndexOf("=") + 1);
				// recreate the ilCommand using the SSO HTML identifier, SSO HTML value and the password
				command = "TEMPLATE_INPUT INPUT_BY=" + this.ssoFieldIdentifierType + "##%!%##PARAM1=" + this.ssoPasswordFieldName + "##%!%##PARAM2=" + password;
				ilCommands.set(i, command);
			}
			// find using REGEX
			else if ( command.matches(submitButtonValueRegex) )
			{
				// recreate the ilCommand using the SSO HTML identifier and SSO HTML value
				command = HTMLTemplateManager.TEMPLATE_BUTTON+" "+HTMLTemplate.CLICK_BY +"="+this.ssoSubmitButtonIdentifier +"##%!%##PARAM1=" + this.ssoSubmitButtonValue;
				ilCommands.set(i, command);
			}
		}
		return ilCommands;
	}

	public String getProtocol()
	{
		return runner.getProtocol();
	}
	
	public HTMLRunner getRunner() {
		return this.runner;
	}

}
