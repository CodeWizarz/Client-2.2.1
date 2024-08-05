/**************************************************************
 * $Revision: 61612 $:
 * $Author: hassan.jamil $:
 * $Date: 2017-03-31 13:59:34 +0700 (Fri, 31 Mar 2017) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/action/FLDAction.java $:
 * $Id: FLDAction.java 61612 2017-03-31 06:59:34Z hassan.jamil $:
 **************************************************************/
package com.rapidesuite.build.core.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.LogMismatchesException;
import com.rapidesuite.build.core.ManualStopException;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.exception.FailToStartInjectionAppletException;
import com.rapidesuite.build.core.exception.FailedInjectionException;
import com.rapidesuite.build.core.exception.SkipInjectionException;
import com.rapidesuite.build.core.fileprotocol.FileProtocolManager;
import com.rapidesuite.build.core.ftp.FTPManager;
import com.rapidesuite.build.core.ssh.SecureShellClient;
import com.rapidesuite.build.utils.CurrentBrowserTask;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.build.utils.SwiftBuildUtils;
import com.rapidesuite.build.utils.TaskListUtils;
import com.rapidesuite.client.common.ForceToGoToTheNextIterationException;
import com.rapidesuite.client.common.RepeatIterationsException;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class FLDAction extends AbstractAction
{

	private String fldScriptsHostName;
	private String fldScriptsHostUserName;
	private String fldScriptsHostPassword;
	private String fldScriptsFolder;
	private SwiftBuildConstants.TRANSFER_PROTOCOL fldScriptsTransferProtocol;
	private String finalFLDScriptsURL;
	private String port;
	private String privateKeyFileName;
	private boolean isSubmitSingleRequestJob;
	private String injectorNamePartition;
	private RobotPasteAction robotPasteAction;
	private BufferedReader bufferedReaderForTracking;

	public FLDAction(Injector injector, String injectorNamePartition, InputStream injectorInputStream, Map<String, String> environmentProperties, ActionManager actionManager,
			boolean isSubmitSingleRequestJob, RobotPasteAction robotPasteAction, BufferedReader bufferedReaderForTracking)
	{
		super(injector, injectorInputStream, environmentProperties, actionManager);
		this.injectorNamePartition = injectorNamePartition;
		fldScriptsHostName = actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostName();
		fldScriptsHostUserName = actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostUserName();
		fldScriptsHostPassword = actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostPassword();
		fldScriptsFolder = actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsFolder();
		String tempURL = actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsURL();
		fldScriptsTransferProtocol = actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsTransferProtocol();
		port = SecureShellClient.SSH_DEFAULT_PORT;
		privateKeyFileName = actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getPrivateKeyFileName();
		tempURL = tempURL.replaceAll("&", "&&");
		finalFLDScriptsURL = tempURL.replaceAll("%s", injectorNamePartition);
		this.isSubmitSingleRequestJob = isSubmitSingleRequestJob;
		this.robotPasteAction = robotPasteAction;
		this.bufferedReaderForTracking = bufferedReaderForTracking;
	}
	
	public static File getLogFileForViewing(final BuildMain buildMain, final String partitionName) {
		return new File(SwiftBuildFileUtils.getLogFolder(buildMain), partitionName+'.'+SwiftBuildConstants.INJECTOR_LOG_FILE_EXTENSION);
	}

	public void start() throws Exception
	{
		try
		{
			super.start();

			uploadScript();
			SwiftBuildUtils.pause(Config.getBuildPauseBetweenScriptUploadAndScriptExecute(),
					"before executing the FLD script...",
					null,
					actionManager);
			startBrowser();

			this.actionManager.setLogCopier(this.actionManager.new LogCopier() {
				
				@Override
				public void copy(boolean clearRawLogFiles) {
					for (final File f : this.rawLogFilesToBeCopied) {
				    	if (f != null && f.isFile()) {
							try {
								final File target = getLogFileForViewing(FLDAction.this.actionManager.getBuildMain(), FLDAction.this.injectorNamePartition);
								SwiftBuildFileUtils.createTempFileForViewer(f, FLDAction.this.injector.getType(), target);
							} catch (Exception e) {
								FileUtils.printStackTrace(e);
							}	    		
				    	}						
					}
					if (clearRawLogFiles) {
						rawLogFilesToBeCopied.clear();
					}					
				}
			});
			
			int openMaxIteration = Config.getBuildFldFormOpenMaxIteration();
			int closeMaxIteration = Config.getBuildFldFormCloseMaxIteration();
			int waitTimeInSecsForOracleFormToClose = Config.getBuildWaitTimeForOracleFormToClose();			

			try {
				TaskListUtils.waitingForFormToStart(actionManager,
						null,
						openMaxIteration,
						injectorNamePartition,
						fldScriptsHostName,
						fldScriptsHostUserName,
						fldScriptsHostPassword,
						privateKeyFileName,
						fldScriptsTransferProtocol,
						actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsLogFolder());
			} catch (FailToStartInjectionAppletException e) {
				throw new RepeatIterationsException(injector, actionManager.getCurrentIterationNumber()==null?actionManager.getCurrentIterationNumber():actionManager.getCurrentIterationNumber()+1, e, true);
			}
			
			final TaskListUtils.FldInjectionSpecifics fldInjectionSpecifics = new TaskListUtils.FldInjectionSpecifics(fldScriptsHostName, 
					fldScriptsHostUserName, 
					fldScriptsHostPassword, 
					privateKeyFileName,
					actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsTransferProtocol(), 
					actionManager.getBuildMain().getSwiftBuildPropertiesValidationPanel().getFLDScriptsLogFolder(), 
					isSubmitSingleRequestJob, 
					waitTimeInSecsForOracleFormToClose,
					injectorNamePartition,
					actionManager,
					injector.getType());			
			
			TaskListUtils.waitForScriptToComplete(actionManager,
					closeMaxIteration,
					injector,
					this.robotPasteAction,
					fldInjectionSpecifics, 
					bufferedReaderForTracking);	
			SwiftBuildUtils.pause(Config.getBuildPauseBetweenScriptCompletedAndBrowserTermination(),
					"before terminating the browser...",
					null,
					actionManager);
			CurrentBrowserTask.eliminateTask();
		}
		catch (LogMismatchesException e) {
			throw e;
		}
		catch ( ManualStopException e )
		{
			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE);
		}
		catch (ForceToGoToTheNextIterationException e) {
			throw e;
		}
		catch (RepeatIterationsException e) {
			throw e;
		}
		catch (SkipInjectionException e) {
			throw e;
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE);
			actionManager.getInjectorsPackageExecutionPanel().stopExecution();
			actionManager.stopExecution();
			if (Config.getBuildTerminateAfterFailedInjection()) {
				throw new FailedInjectionException();
			} else {
				JOptionPane.showMessageDialog(actionManager.getBuildMain().getRootFrame(), "Error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}

		}		
		finally
		{
			removeScript();
			fireActionCompletedEvent();
		}
	}

	public void uploadScript() throws Exception
	{
		actionManager.setOutput("Uploading script: '" + injectorNamePartition + "'");
		try
		{
			String characterSet = environmentProperties.get("EXTRA_PROPERTY_CHARACTER_SET");
			if ( characterSet == null )
				characterSet = "";
			FileUtils.println("Checking if there is a character set to apply: '" + characterSet + "' ...");
			if ( !characterSet.equals("") )
			{
				injectorInputStream = FileUtils.convertUTF8StreamToCharacterSet(injectorInputStream, characterSet, injectorNamePartition);
			}

			if ( actionManager.getBuildMain().isSSHProtocol() )
			{
				SecureShellClient client = null;
				try
				{
					if (actionManager.getBuildMain().isPasswordBasedSSHProtocol()) {
						client = new SecureShellClient.PasswordBasedSecureShellClient(fldScriptsHostName, port, fldScriptsHostUserName, fldScriptsHostPassword, false);
					} else {
						client = new SecureShellClient.PrivateKeyBasedSecureShellClient(fldScriptsHostName, port, fldScriptsHostUserName, privateKeyFileName, false);
					}
					
					client.cd(fldScriptsFolder);
					client.put(injectorInputStream, fldScriptsFolder, injectorNamePartition);
				}
				finally
				{
					if ( client != null )
					{
						client.close();
					}
				}
			}
			else if (actionManager.getBuildMain().isFileProtocol()) {
				FileProtocolManager.uploadFile(injectorInputStream, fldScriptsFolder, injectorNamePartition);
			}
			else
			{
				FTPManager.uploadFile(fldScriptsHostName,
						FTPManager.FTP_PORT,
						fldScriptsHostUserName,
						fldScriptsHostPassword,
						injectorInputStream,
						fldScriptsFolder,
						injectorNamePartition);
			}
			actionManager.setOutput("Script uploaded.");
		}
		catch ( Throwable ex )
		{
			FileUtils.printStackTrace(ex);
			actionManager.setOutput("Error: " + ex.getMessage());
			GUIUtils.popupErrorMessage("Error: " + ex.getMessage());
			throw new Exception(ex);
		}
		finally
		{
			IOUtils.closeQuietly(injectorInputStream);
		}
	}

	public void removeScript()
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				actionManager.setOutput("Removing script on the server...");
				boolean statusDel = true;
				try
				{
					if ( actionManager.getBuildMain().isSSHProtocol() )
					{
						SecureShellClient client = null;
						try
						{
							if (actionManager.getBuildMain().isPasswordBasedSSHProtocol()) {
								client = new SecureShellClient.PasswordBasedSecureShellClient(fldScriptsHostName, port, fldScriptsHostUserName, fldScriptsHostPassword, false);
							} else {
								client = new SecureShellClient.PrivateKeyBasedSecureShellClient(fldScriptsHostName, port, fldScriptsHostUserName, privateKeyFileName, false);
							}							
							client.cd(fldScriptsFolder);
							client.delete(fldScriptsFolder, injectorNamePartition);

						} finally {
							if ( client != null ) {
								client.close();
							}
						}
					}
					else if (actionManager.getBuildMain().isFileProtocol())
					{
						statusDel = FileProtocolManager.deleteFile(
								fldScriptsFolder,
								injectorNamePartition);						
					}
					else
					{
						statusDel = FTPManager.deleteFile(fldScriptsHostName,
								FTPManager.FTP_PORT,
								fldScriptsHostUserName,
								fldScriptsHostPassword,
								fldScriptsFolder,
								injectorNamePartition);
					}
					if ( statusDel )
					{
						actionManager.setOutput("Script removed.");
					}
					else
					{
						actionManager.setOutput("Script not removed. Please check the log file.");
					}
				} catch ( Throwable ex ) {
					FileUtils.printStackTrace(ex);
					actionManager.setOutput("Unable to remove script on the server. See log file for errors.");
				}
			}
		}).start();
	}

	public void startBrowser() throws Exception
	{
		actionManager.setOutput("URL to launch: \n" + finalFLDScriptsURL);
		FileUtils.println(finalFLDScriptsURL);
		if ( !actionManager.isExecutionStopped() )
		{
			TaskListUtils.startBrowser(actionManager, finalFLDScriptsURL, false);
		}
	}

    @Override
    public void join() throws InterruptedException
    {
        // TODO Auto-generated method stub

    }

}