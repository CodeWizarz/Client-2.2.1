/**************************************************
 * $Revision: 50931 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2015-11-30 13:39:08 +0700 (Mon, 30 Nov 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/environment/SwiftBuildEnvironmentValidationButtonAction.java $:
 * $Id: SwiftBuildEnvironmentValidationButtonAction.java 50931 2015-11-30 06:39:08Z jannarong.wadthong $:
 */
package com.rapidesuite.build.gui.environment;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.fileprotocol.FileProtocolManager;
import com.rapidesuite.build.core.ftp.FTPManager;
import com.rapidesuite.build.core.ssh.SecureShellClient;
import com.rapidesuite.build.gui.panels.SwiftBuildHtmlValidationPanel;
import com.rapidesuite.build.gui.panels.SwiftBuildPropertiesValidationPanel;
import com.rapidesuite.build.utils.CurrentBrowserTask;
import com.rapidesuite.build.utils.SwiftBuildUtils;
import com.rapidesuite.build.utils.TaskListUtils;
import com.rapidesuite.client.common.gui.EnvironmentValidationButtonAbstractAction;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.EnvironmentPropertyConstants;
import com.rapidesuite.reverse.ReverseMain;
public class SwiftBuildEnvironmentValidationButtonAction extends EnvironmentValidationButtonAbstractAction
{

	final SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel;
	EnvironmentValidationPanel environmentValidationPanel;

	public SwiftBuildEnvironmentValidationButtonAction(SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel)
	{
		this.swiftBuildPropertiesValidationPanel = swiftBuildPropertiesValidationPanel;
	}

	public boolean beforeExecuteAction(EnvironmentValidationPanel environmentValidationPanel)
	{
		return true;
	}

	public void afterExecuteAction(EnvironmentValidationPanel environmentValidationPanel,NextButtonWrapper nextButtonWrapper)
	{
		if ( !swiftBuildPropertiesValidationPanel.isValidationMandatory() )
		{
		    nextButtonWrapper.setNextButtonIsEnabled(true);
		    swiftBuildPropertiesValidationPanel.getBuildMain().setRunSqlScriptItemEnabled(true);
		}
	}

	public static final String BROWSER_LAUNCHING_FAILURE_ADDITIONAL_MESSAGE = ".<br/>Note:  this error can occur if you are already running the browser. Please close all open browser windows and try again.";

	private boolean serverAccessible = true;

	public void executeAction(EnvironmentValidationPanel environmentValidationPanel)
	{
		try
		{
			this.environmentValidationPanel = environmentValidationPanel;
			environmentValidationPanel.setPropertiesResetStatusLabel();
			environmentValidationPanel.setValidationSuccess(false);
			this.serverAccessible = true;
			environmentValidationPanel.setValidationSuccess(validate());
		}
		catch ( Throwable e )
		{
		    environmentValidationPanel.setValidationSuccess(false);
			FileUtils.printStackTrace(e);
			if ( environmentValidationPanel.isValidationStarted() )
			{
				GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), "Unable to validate the environment:<br/>" + CoreUtil.getAllThrowableMessagesHTML(e));
			}
		}
		finally
		{
			if ( environmentValidationPanel.isValidationStarted() )
			{
				swiftBuildPropertiesValidationPanel.enableComponents();
				environmentValidationPanel.getValidateButton().setText("Start validation");
				environmentValidationPanel.setValidationStartedFlag(false);
			}
		}
	}

	public boolean validate() throws Exception
	{
		SwiftBuildHtmlValidationPanel swiftBuildHtmlValidationPanel = swiftBuildPropertiesValidationPanel.getSwiftBuildHtmlValidationPanel();
		if (swiftBuildHtmlValidationPanel != null) {
			if (swiftBuildHtmlValidationPanel.getBuildSingleSignOn()) {
				if (StringUtils.isBlank(swiftBuildHtmlValidationPanel.getBuildSingleSignOnFieldIdentifierType())) {
					GUIUtils.showErrorMessage(swiftBuildHtmlValidationPanel.getMessageLabel(), "Build Single Sign On Field Identifier Type is mandatory");
					GUIUtils.showEmptyMessageAndDisableNextButton(environmentValidationPanel, swiftBuildHtmlValidationPanel);
					return false;
				} else if (StringUtils.isBlank(swiftBuildHtmlValidationPanel.getBuildSingleSignOnUsernameFieldIdentifierValue())) {
					GUIUtils.showErrorMessage(swiftBuildHtmlValidationPanel.getMessageLabel(), "Build Single Sign On Username Field Identifier Value is mandatory");
					GUIUtils.showEmptyMessageAndDisableNextButton(environmentValidationPanel, swiftBuildHtmlValidationPanel);
					return false;
				} else if (StringUtils.isBlank(swiftBuildHtmlValidationPanel.getBuildSingleSignOnPasswordFieldIdentifierValue())) {
					GUIUtils.showErrorMessage(swiftBuildHtmlValidationPanel.getMessageLabel(), "Build Single Sign On Password Field Identifier Value is mandatory");
					GUIUtils.showEmptyMessageAndDisableNextButton(environmentValidationPanel, swiftBuildHtmlValidationPanel);
					return false;
				} else if (StringUtils.isBlank(swiftBuildHtmlValidationPanel.getBuildSingleSignOnSubmitFieldIdentifier())) {
					GUIUtils.showErrorMessage(swiftBuildHtmlValidationPanel.getMessageLabel(), "Build Single Sign On Submit Field Identifier is mandatory");
					GUIUtils.showEmptyMessageAndDisableNextButton(environmentValidationPanel, swiftBuildHtmlValidationPanel);
					return false;
				} else if (StringUtils.isBlank(swiftBuildHtmlValidationPanel.getBuildSingleSignOnSubmitFieldIdentifierValue())) {
					GUIUtils.showErrorMessage(swiftBuildHtmlValidationPanel.getMessageLabel(), "Build Single Sign On Submit Field Identifier Value is mandatory");
					GUIUtils.showEmptyMessageAndDisableNextButton(environmentValidationPanel, swiftBuildHtmlValidationPanel);
					return false;
				}
			}
		}
		// Reset message of SwiftBuildHtmlValidationPanel if HTML validation passes
		GUIUtils.showStandardMessage(swiftBuildHtmlValidationPanel.getMessageLabel(), "");
		
		String userName = swiftBuildPropertiesValidationPanel.getFLDScriptsHostUserName();
		String password = swiftBuildPropertiesValidationPanel.getFLDScriptsHostPassword();
		String privateKeyFileLocation = swiftBuildPropertiesValidationPanel.getPrivateKeyFileName();
		String hostName = swiftBuildPropertiesValidationPanel.getFLDScriptsHostName();
		SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod = swiftBuildPropertiesValidationPanel.getFLDScriptsTransferProtocol();
		String res = null;

		GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), "Please wait, validating Connection to the host...");
		if ( connectionMethod == null )
		{
			throw new Exception("Missing transfer protocol.");
		}
		else if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FTP) )
		{
			FTPManager.setFTPES(false);
			res = validateHostFTPConnection(hostName, userName, password);
		}
		else if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FILE) )
		{
			//For file protocol, no access credential is needed
			res = SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
		}		
		else if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP) || connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.SFTPK) )
		{
			res = validateHostSSHConnection(hostName, userName, password, privateKeyFileLocation, 
					connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP));
		}
		else
		{
			throw new Exception("Unsupported transfer protocol: " + connectionMethod);
		}
		boolean isSuccess = res.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_SUCCESS_VALUE);
		String imageName = (isSuccess) ? GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME : GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME;
		if ( environmentValidationPanel.isValidationStarted() )
		{
			Assert.isTrue(environmentValidationPanel.getEnvironmentPropertiesMap().containsKey(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY), SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY+" is not set");
			final SwiftBuildConstants.TRANSFER_PROTOCOL transferProtocolValue = SwiftBuildConstants.TRANSFER_PROTOCOL.valueOfAcceptsNull(environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY));
			if (SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP.equals(transferProtocolValue)) {
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY, imageName);
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY, imageName);
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY, imageName);				
			} else if (SwiftBuildConstants.TRANSFER_PROTOCOL.FTP.equals(transferProtocolValue)) {
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY, imageName);
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY, imageName);
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY, imageName);				
			} else if (SwiftBuildConstants.TRANSFER_PROTOCOL.SFTPK.equals(transferProtocolValue)) {
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY, imageName);
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY, imageName);
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_SSH_PRIVATE_KEY_LOCATION_KEY, imageName);				
			} else if (SwiftBuildConstants.TRANSFER_PROTOCOL.FILE.equals(transferProtocolValue)) {
				//do nothing
			} else {
				throw new IllegalArgumentException(transferProtocolValue+" is not a valid value of "+SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY);
			}
			
			environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY, imageName);
		}
		if ( !isSuccess )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), res);
			return false;
		}

		if ( !environmentValidationPanel.isValidationStarted() )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), SwiftBuildConstants.VALIDATION_STOPPED_VALUE);
			return false;
		}
		GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), "Please wait, validating FLD scripts folder...");
		res = validateRemoteFolder(hostName, userName, password, privateKeyFileLocation, connectionMethod, swiftBuildPropertiesValidationPanel.getFLDScriptsFolder());
		isSuccess = res.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_SUCCESS_VALUE);
		imageName = (isSuccess) ? GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME : GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME;
		if ( environmentValidationPanel.isValidationStarted() )
		{
			environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_FOLDER_KEY, imageName);
		}
		if ( !isSuccess )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), res);
			return false;
		}

		if ( !environmentValidationPanel.isValidationStarted() )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), SwiftBuildConstants.VALIDATION_STOPPED_VALUE);
			return false;
		}
		GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), "Please wait, validating FLD scripts Logs folder...");
		res = validateRemoteFolder(hostName, userName, password, privateKeyFileLocation, connectionMethod, swiftBuildPropertiesValidationPanel.getFLDScriptsLogFolder());
		isSuccess = res.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_SUCCESS_VALUE);
		imageName = (isSuccess) ? GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME : GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME;
		if ( environmentValidationPanel.isValidationStarted() )
		{
			environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_LOG_FOLDER_KEY, imageName);
		}
		if ( !isSuccess )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), res);
			return false;
		}

		if ( !environmentValidationPanel.isValidationStarted() )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), SwiftBuildConstants.VALIDATION_STOPPED_VALUE);
			return false;
		}
		GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), "Please wait, validating FLD scripts URL...");
		res = validateFLDURL(swiftBuildPropertiesValidationPanel.getFLDScriptsURL());
		isSuccess = res.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_SUCCESS_VALUE);
		imageName = (isSuccess) ? GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME : GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME;
		if ( environmentValidationPanel.isValidationStarted() )
		{
			environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_URL_KEY, imageName);
		}
		if ( !isSuccess )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), res);
			return false;
		}

		if ( !environmentValidationPanel.isValidationStarted() )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), SwiftBuildConstants.VALIDATION_STOPPED_VALUE);
			return false;
		}
		GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), "Please wait, validating Database Connection...");
		res = validateDatabaseConnection(swiftBuildPropertiesValidationPanel.getDatabaseHostName(),
				swiftBuildPropertiesValidationPanel.getDatabasePortNumber(),
				swiftBuildPropertiesValidationPanel.getDatabaseSID(),
				swiftBuildPropertiesValidationPanel.getDatabaseUserName(),
				swiftBuildPropertiesValidationPanel.getDatabasePassword());
		isSuccess = res.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_SUCCESS_VALUE);
		imageName = (isSuccess) ? GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME : GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME;
		if ( environmentValidationPanel.isValidationStarted() )
		{
			environmentValidationPanel.setPropertyStatusLabel(EnvironmentPropertyConstants.DATABASE_HOST_NAME_KEY, imageName);
			environmentValidationPanel.setPropertyStatusLabel(EnvironmentPropertyConstants.DATABASE_PORT_NUMBER_KEY, imageName);
			environmentValidationPanel.setPropertyStatusLabel(EnvironmentPropertyConstants.DATABASE_SID_KEY, imageName);
			environmentValidationPanel.setPropertyStatusLabel(EnvironmentPropertyConstants.DATABASE_USER_NAME_KEY, imageName);
			environmentValidationPanel.setPropertyStatusLabel(EnvironmentPropertyConstants.DATABASE_PASSWORD_KEY, imageName);
		}
		if ( !isSuccess )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), res);
			return false;
		}

		final boolean databaseUserNameIsBlank = StringUtils.isBlank(swiftBuildPropertiesValidationPanel.getDatabaseUserName());
		final boolean databaseUserNameMatchesTheDefaultOne = databaseUserNameIsBlank ? false : Utils.matchesDefaultDatabaseUsername(swiftBuildPropertiesValidationPanel.getDatabaseUserName());
		UtilsConstants.DATABASE_USERNAME_STATUS databaseUserNameStatus =
				databaseUserNameIsBlank?
						UtilsConstants.DATABASE_USERNAME_STATUS.EMPTY:
						(databaseUserNameMatchesTheDefaultOne?UtilsConstants.DATABASE_USERNAME_STATUS.OK:UtilsConstants.DATABASE_USERNAME_STATUS.NOT_STANDARD);

		if ( !environmentValidationPanel.isValidationStarted() )
		{
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), SwiftBuildConstants.VALIDATION_STOPPED_VALUE +
					databaseUserNameStatus.getMessage());
			return false;
		}
		GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), "Please wait, initiating FLD Forms playback...");
		String formsUserName = swiftBuildPropertiesValidationPanel.getFLDFormUserName();
		String formsPassword = swiftBuildPropertiesValidationPanel.getFLDFormPassword();

		res = validateFLDFormsPlayback(null,hostName,
				userName,
				password,
				privateKeyFileLocation,
				connectionMethod,
				swiftBuildPropertiesValidationPanel.getFLDScriptsFolder(),
				swiftBuildPropertiesValidationPanel.getFLDScriptsLogFolder(),
				swiftBuildPropertiesValidationPanel.getFLDScriptsURL(),
				formsUserName,
				formsPassword,
				generateDefaultFldValidationScript(formsUserName, formsPassword),
				Utils.getUniqueFilename("testConnection", ".fld"));
		isSuccess = res.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_SUCCESS_VALUE);
		if ( res.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_WARNING_VALUE) )
		{
			swiftBuildPropertiesValidationPanel.setNextButton(true);
			swiftBuildPropertiesValidationPanel.getBuildMain().setRunSqlScriptItemEnabled(true);
			return true;
		}
		else if ( !isSuccess )
		{
			final String additionalMessage = databaseUserNameStatus.getMessage();
			FileUtils.println("Validation failed:\\"+res+additionalMessage);
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), res+additionalMessage);
			return false;
		}

		final String basicMessage = "BWE Validation Successful. Please click 'Next.'";
		if (this.serverAccessible) {
			if (!databaseUserNameIsBlank && databaseUserNameMatchesTheDefaultOne) {
				GUIUtils.showSuccessMessage(environmentValidationPanel.getMessageLabel(), basicMessage);
			} else {
				GUIUtils.showWarningMessage(environmentValidationPanel.getMessageLabel(), basicMessage
						+ databaseUserNameStatus.getMessage());
			}

		} else {
			GUIUtils.showWarningMessage(environmentValidationPanel.getMessageLabel(), basicMessage + "<br/>"
					+ "Warning:  Browser/JRE version retrieval and validation were unable to be performed.  More information can be found in the Build system log."
					+ databaseUserNameStatus.getMessage());
		}

		swiftBuildPropertiesValidationPanel.setNextButton(true);
		swiftBuildPropertiesValidationPanel.getSwiftBuildHtmlValidationPanel().setNextButton(true);
		swiftBuildPropertiesValidationPanel.getBuildMain().setRunSqlScriptItemEnabled(true);
		return true;
	}

	public static String validateHostFTPConnection(String hostName, String userName, String password)
	{
		try
		{
			String connectionValidation = FTPManager.validateCredentials(hostName, FTPManager.FTP_PORT, userName, password);
			if ( !connectionValidation.equals(SwiftBuildConstants.CONNECTION_SUCCESS_MESSAGE) )
			{
				return "Unable to connect to Host: '" + hostName + "'. </br>" + connectionValidation;
			}

			return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
		}
		catch ( Exception e )
		{
			return e.getMessage();
		}
	}

	public String validateHostSSHConnection(String hostName, String userName, String password, String privateKeyFileName, final boolean usePassword)
	{
		SecureShellClient client = null;
		try
		{
			if (usePassword) {
				client = new SecureShellClient.PasswordBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, password, true);
			} else {
				client = new SecureShellClient.PrivateKeyBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, privateKeyFileName, true);
			}
			
			if ( client.isConnected() )
			{
				return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
			}
			return "Unable to connect to Host: '" + hostName + "'. \n" + client.getStatus();
		} catch(Throwable e) {
			return CoreUtil.getAllThrowableMessagesHTML(e);
		} finally {
			if ( client != null )
			{
				client.close();
			}
		}
	}

	public String validateRemoteFolder(String hostName, String userName, String password, String privateKeyFileName, SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod, String remoteFolder)
	{
		SecureShellClient client = null;
		try
		{

			if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FTP) )
			{
				String msg = FTPManager.validateTargetFolder(hostName, FTPManager.FTP_PORT, userName, password, remoteFolder, false);
				if ( msg.equalsIgnoreCase(SwiftBuildConstants.REMOTE_FOLDER_SUCCESS_MESSAGE) )
				{
					return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
				}
			}
			else if (connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FILE) )
			{
				String msg = FileProtocolManager.validateTargetFolder(remoteFolder);
				if ( msg.equalsIgnoreCase(SwiftBuildConstants.REMOTE_FOLDER_SUCCESS_MESSAGE) )
				{
					return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
				} else {
					return msg;
				}
			}
			else
			{
				if (connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP)) {
					client = new SecureShellClient.PasswordBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, password, false);
				} else {
					client = new SecureShellClient.PrivateKeyBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, privateKeyFileName, false);
				}
				
				if ( !client.isConnected() )
				{
					return "Unable to connect to Host: '" + hostName + "'. \n" + client.getStatus();
				}
				boolean isSuccess = client.cd(remoteFolder);
				if ( isSuccess )
				{
					return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
				}
			}
			return "Unable to locate remote folder: '" + remoteFolder + "'.";
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return CoreUtil.getAllThrowableMessagesHTML(e);
		}
		finally
		{
			if ( client != null )
				client.close();
		}
	}

	public String validateFLDURL(String urlParam)
	{
		String res = validateURLFormat(urlParam);
		if ( !res.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_SUCCESS_VALUE) )
		{
			return res;
		}
		return validateURLConnection(urlParam);
	}

	public String validateURLFormat(String urlParam)
	{
		int playIndexOf = urlParam.indexOf("play=");
		if ( playIndexOf == -1 )
		{
			return "Invalid FLD URL format: The parameter 'play=' is missing.";
		}
		int recordIndexOf = urlParam.indexOf("record=");
		if ( recordIndexOf == -1 )
		{
			return "Invalid FLD URL format: The parameter 'record=' is missing.";
		}
		if ( recordIndexOf > playIndexOf )
		{
			return "Invalid FLD URL format: The parameter 'record=' must appear before 'play=' . " + "Example for Oracle R11:"
					+ " http://server.com:24604/dev60cgi/f60cgi?record=%s.log%20play=%s";
		}
		int indexOf = urlParam.indexOf("%20play=");
		if ( indexOf == -1 )
		{
			return "Invalid FLD URL format: The parameter 'play=' must start with %20 and not &. " + "Example: http://server.com:24604/dev60cgi/f60cgi?record=%s.log%20play=%s";
		}

		return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
	}

	public String validateURLConnection(String urlParam)
	{
		BufferedReader in = null;
		try
		{
			String temp = urlParam.replaceAll("%s", "testConnection");
			URL url = new URL(temp);
			URLConnection connection = url.openConnection();
			connection.connect();

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
		}
		catch ( IOException e )
		{
			FileUtils.printStackTrace(e);
			return "Unable to connect to the FLD URL. Invalid Host: " + e.getMessage();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return "Unable to connect to the FLD URL. " + e.getMessage();
		}
		finally
		{
			if ( in != null )
				try
				{
					in.close();
				}
				catch ( Exception e )
				{
					FileUtils.printStackTrace(e);
				}
		}
	}

	public String validateFLDFormsPlayback(
			ActionManager actionManager,
			String hostName,
			String userName,
			String password,
			String privateKeyFileName,
			SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod,
			String remoteScriptFolder,
			String remoteLogFolder,
			String url,
			String formsUserName,
			String formsPassword,
			String fldScriptContent,
			String fldScriptFilename)
	{
		try ( InputStream scriptInputStream = new ByteArrayInputStream(fldScriptContent.getBytes(com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING)); )
		{
			String urlTemp = url.replaceAll("%s", fldScriptFilename);

			if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FTP) )
			{
				FTPManager.uploadFile(hostName, FTPManager.FTP_PORT, userName, password, scriptInputStream, remoteScriptFolder, fldScriptFilename);
			}
			else if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FILE) )
			{
				FileProtocolManager.uploadFile(scriptInputStream, remoteScriptFolder, fldScriptFilename);
			}			
			else
			{
				SecureShellClient client = null;
				try
				{
					if (connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP)) {
						client = new SecureShellClient.PasswordBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, password, false);
					} else {
						client = new SecureShellClient.PrivateKeyBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, privateKeyFileName, false);
					}					
					
					if ( !client.isConnected() )
					{
						return "Unable to connect to Host: '" + hostName + "'. \n" + client.getStatus();
					}
					boolean isSuccess = client.cd(remoteScriptFolder);
					if ( !isSuccess )
					{
						return "Unable to locate remote folder: '" + remoteScriptFolder + "'.";
					}
					client.put(scriptInputStream, remoteScriptFolder, fldScriptFilename);
				}
				finally
				{
					if ( client != null )
					{
						client.close();
					}
				}
			}

			SwiftBuildUtils.pause(Config.getBuildPauseBetweenScriptUploadAndScriptExecute(),
					"before executing the FLD script...",
					environmentValidationPanel,
					null);

			GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), "Please wait, initiating FLD Forms playback... Waiting for the browser to start...");
			TaskListUtils.startBrowser(null, urlTemp, true);

			int openMaxIteration = -1;
			int closeMaxIteration = -1;

			closeMaxIteration = Config.getBuildFldFormCloseMaxIteration();
			try
			{
				openMaxIteration = Config.getBuildFldBweValidationOpenTimeout();
				closeMaxIteration = Config.getBuildFldBweValidationCloseTimeout();
			}
			catch ( Exception ex )
			{
				FileUtils.printStackTrace(ex);
			}

			try
			{
				TaskListUtils.waitingForFormToStart(actionManager,
						swiftBuildPropertiesValidationPanel,
						openMaxIteration,
						fldScriptFilename,
						hostName,
						userName,
						password,
						privateKeyFileName,
						connectionMethod,
						remoteLogFolder);
			}
			catch ( Exception ex )
			{
				FileUtils.printStackTrace(ex);
				CurrentBrowserTask.eliminateTask();
				return "Timeout waiting for Oracle forms to start.";
			}

			try
			{
				TaskListUtils.waitForValidationScriptToComplete(
						actionManager,
						swiftBuildPropertiesValidationPanel,
				        fldScriptFilename,
						hostName,
						userName,
						password,
						privateKeyFileName,
						connectionMethod,
						remoteLogFolder,
						closeMaxIteration);
			}
			catch ( Exception ex )
			{
				FileUtils.printStackTrace(ex);
				CurrentBrowserTask.eliminateTask();

				SwiftBuildUtils.pause(Config.getBuildPauseBetweenScriptCompletedAndLogFileRetrieve(),
						"before retrieving the FLD log file...",
						environmentValidationPanel,
						null);
				boolean hasFLDLogFile = hasFLDLogFile(hostName,
						userName,
						password,
						privateKeyFileName,
						connectionMethod,
						remoteScriptFolder,
						remoteLogFolder,
						url,
						formsUserName,
						formsPassword,
						fldScriptFilename);

				UtilsConstants.DATABASE_USERNAME_STATUS databaseUsernameStatus = UtilsConstants.DATABASE_USERNAME_STATUS.OK;
				if ( !(this instanceof EnableDiagnosticsButtonAction) )
				{
				    databaseUsernameStatus = StringUtils.isBlank(swiftBuildPropertiesValidationPanel.getDatabaseUserName()) ?
								UtilsConstants.DATABASE_USERNAME_STATUS.EMPTY :
								(UtilsConstants.DEFAULT_DATABASE_USER_NAME.equalsIgnoreCase(swiftBuildPropertiesValidationPanel.getDatabaseUserName()) ? UtilsConstants.DATABASE_USERNAME_STATUS.OK : UtilsConstants.DATABASE_USERNAME_STATUS.NOT_STANDARD);
				}


				if ( !hasFLDLogFile )
				{
					environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_FOLDER_KEY, GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME);
					environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_LOG_FOLDER_KEY,
							GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME);
					String errMsg = "Timeout waiting for FORMS to close: cannot find the FLD log file. Check that "
							+ "FLD Forms playback is enabled and that the values in 'FLD script folder'" + " and 'FLD log folder' are set to the value "
							+ "specified in the forms_trace_path XML tag of the context file " + "($CONTEXT_FILE variable)."+databaseUsernameStatus.getMessage();
					return errMsg;
				}

                if ( (this instanceof EnableDiagnosticsButtonAction) )
                {
                    return SwiftBuildConstants.VALIDATION_FAILED;
                }


				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_FORM_USER_NAME_KEY, GUIUtils.PROPERTY_WARNING_STATUS_IMAGE_NAME);
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_FORM_PASSWORD_KEY, GUIUtils.PROPERTY_WARNING_STATUS_IMAGE_NAME);
				final String lineSeparator = "<br/>";
				if (this.serverAccessible) {

					GUIUtils.showWarningMessage(environmentValidationPanel.getMessageLabel(), "BWE Validation Successful. Please click 'Next.'" + lineSeparator
							+ "Warning: Forms playback is enabled (as determined from the FLD log), but there was a timeout waiting for the form to close due to an invalid 'Oracle Forms user name' or 'Oracle Forms password'."+databaseUsernameStatus.getMessage());
				} else {
					GUIUtils.showWarningMessage(environmentValidationPanel.getMessageLabel(), "BWE Validation Successful. Please click 'Next.'" + lineSeparator
							+ "Warning: Forms playback is enabled (as determined from the FLD log), but there was a timeout waiting for the form to close due to an invalid 'Oracle Forms user name' or 'Oracle Forms password'." + lineSeparator
							+ "Warning:  Browser/JRE version retrieval and validation were unable to be performed.  More information can be found in the Build system log."
							+ databaseUsernameStatus.getMessage());
				}
				return SwiftBuildConstants.VALIDATION_WARNING_VALUE;
			}
			SwiftBuildUtils.pause(Config.getBuildPauseBetweenScriptCompletedAndBrowserTermination(),
					"before terminating the browser...",
					environmentValidationPanel,
					null);

			CurrentBrowserTask.eliminateTask();

			if ( environmentValidationPanel.isValidationStarted() )
			{
				if (Config.getBuildFldFormsInjectionCompletionBasedOnLogFile())
				{
				    File logFile = TaskListUtils.getFLDLogFileOrPauseIfNotFound(hostName,
							userName,
							password,
							privateKeyFileName,
							connectionMethod,
							remoteLogFolder,
							fldScriptFilename,
							null,
							environmentValidationPanel);
					if ( logFile == null )
					{
						environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_SCRIPTS_LOG_FOLDER_KEY,
								GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME);
						String errMsg = "FLD Forms playback is enabled but cannot find the FLD log file: '" + remoteLogFolder + UtilsConstants.FORWARD_SLASH + fldScriptFilename
								+ ".log' .</br> Please check your value in 'FLD scripts Logs folder'.";
						return errMsg;
					}
				}
			}

			if ( environmentValidationPanel.isValidationStarted() )
			{
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_FORM_USER_NAME_KEY, GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME);
				environmentValidationPanel.setPropertyStatusLabel(SwiftBuildConstants.FLD_FORM_PASSWORD_KEY, GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME);
				return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
			}
			else
			{
				return "Manual stop.";
			}
		}
		catch ( Exception e )
		{
		    FileUtils.printStackTrace(e);
			return "Error processing FLD playback:" + CoreUtil.getAllThrowableMessagesHTML(e);
		}
	}

    private String generateDefaultFldValidationScript(String formsUserName, String formsPassword)
    {
        StringBuffer fldCommands = new StringBuffer("");
        fldCommands.append("WINDOW FNDSCSGN SIGNON_WINDOW ACTIVATE 1\n");
        fldCommands.append("VALUE FNDSCSGN SIGNON USERNAME 1 " + formsUserName + " \n");
        fldCommands.append("KEY Next_item\n");
        fldCommands.append("VALUE FNDSCSGN SIGNON PASSWORD 1 " + formsPassword + " \n");
        fldCommands.append("CLICK FNDSCSGN SIGNON CONNECT_BUTTON 1 MOUSE \n");
        fldCommands.append("USER_EXIT LOV Responsibilities\n");
        fldCommands.append(CoreConstants.MENU_MAGIC_MAGIC_QUIT+"\n");
        fldCommands.append("USER_EXIT CHOICE OK\n");
        return fldCommands.toString();
    }

	public boolean hasFLDLogFile(String hostName,
			String userName,
			String password,
			String privateKeyFileName,
			SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod,
			String remoteScriptFolder,
			String remoteLogFolder,
			String url,
			String formsUserName,
			String formsPassword,
			String scriptName)
	{
		long size = TaskListUtils.getFLDLogFileSize(hostName, userName, password, privateKeyFileName, connectionMethod, remoteLogFolder, scriptName);
		return size != -1;
	}

	public String validateDatabaseConnection(String databaseHostName, String databasePortNumber, String databaseSID, String databaseUserName, String databasePassword)
	{
		if ( !(databaseHostName == null || databaseHostName.isEmpty()) || !(databasePortNumber == null || databasePortNumber.isEmpty())
				|| !(databaseSID == null || databaseSID.isEmpty()) || !(databaseUserName == null || databaseUserName.isEmpty())
				|| !(databasePassword == null || databasePassword.isEmpty()) )
		{
			try
			{
				Integer.valueOf(databasePortNumber);
			}
			catch ( Exception e )
			{
				environmentValidationPanel.setPropertyStatusLabel(EnvironmentPropertyConstants.DATABASE_PORT_NUMBER_KEY,
						GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME);
				return "Database port number must be a number";
			}

			try
			{
				Map<String, String> environmentProperties = environmentValidationPanel.getEnvironmentPropertiesMap();
				DatabaseUtils.testDatabaseConnection(environmentProperties);
				String ebsVersion = DatabaseUtils.getEBSVersion(environmentProperties);

				GUIUtils.showSuccessMessage(environmentValidationPanel.getMessageLabel(), "Database connection established. EBS version: " + ebsVersion);
			}
			catch ( Exception e )
			{
				String msg = "Unable to connect to the Database:<br/> " + e.getMessage();
				FileUtils.printStackTrace(e);
				GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), msg);
				return msg;
			}
		} else {
			String msg = "Invalid or missing database connection information, please provide correct information to proceed";
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), msg);
			return msg;
		}
		return SwiftBuildConstants.VALIDATION_SUCCESS_VALUE;
	}

}