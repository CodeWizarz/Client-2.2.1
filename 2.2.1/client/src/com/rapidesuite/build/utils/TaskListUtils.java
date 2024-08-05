package com.rapidesuite.build.utils;


import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.util.Assert;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.LogMismatchesException;
import com.rapidesuite.build.core.ManualStopException;
import com.rapidesuite.build.core.action.FLDAction;
import com.rapidesuite.build.core.action.RobotPasteAction;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.exception.FailToStartInjectionAppletException;
import com.rapidesuite.build.core.exception.FailedInjectionException;
import com.rapidesuite.build.core.exception.SkipInjectionException;
import com.rapidesuite.build.core.fileprotocol.FileProtocolManager;
import com.rapidesuite.build.core.ftp.FTPManager;
import com.rapidesuite.build.core.ssh.SecureShellClient;
import com.rapidesuite.build.gui.panels.InjectorsPackageExecutionPanel;
import com.rapidesuite.build.gui.panels.SwiftBuildPropertiesValidationPanel;
import com.rapidesuite.client.common.ForceToGoToTheNextIterationException;
import com.rapidesuite.client.common.RepeatIterationsException;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Portability;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.IterationLogger;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;

public class TaskListUtils
{

	private static void displayFormsPlaybackMessage(SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel, String message)
	{
		FileUtils.println(message);
		if ( swiftBuildPropertiesValidationPanel != null )
		{
			message = "Please wait, initiating Oracle Forms playback... " + message;
			GUIUtils.showInProgressMessage(swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().getMessageLabel(), message);
		}
	}

	public static boolean hasOracleFormsProcess() throws Exception
	{

		boolean output = CurrentBrowserTask.hasActiveForm();

		return output;
	}

	public static Process startBrowser(ActionManager actionManager, String url, boolean isThrowError) throws Exception
	{
		try
		{
			String fullCommand = Config.getBuildFldBrowserLauncherCommand() + " " + url;
			Process browserTask = Portability.startProcess(fullCommand);
			ProcessWatchdog.initializeProcessWatchdog(browserTask);
			CurrentBrowserTask.setTask(browserTask);

			return browserTask;
		}
		catch ( Throwable err )
		{
			FileUtils.printStackTrace(err);
			if ( isThrowError )
			{
				throw new Exception("Error launching web browser specified by the '" + UtilsConstants.ENGINE_PROPERTIES_FILE_NAME + "' property " + Config.getBuildFldBrowserLauncherCommandPropertyName() + ", value = " + Config.getBuildFldBrowserLauncherCommand(), err);
			}
			if ( actionManager != null )
			{
				actionManager.stopExecution();
			}
			if (Config.isAutomatedRun()) {
				throw new Exception(err);
			} else {
				FileUtils.printStackTrace(err);
				GUIUtils.popupErrorMessage("Browser cannot be launched, error: " + err.getMessage());
			}

		}
		return null;
	}

	public static void startFirefox(ActionManager actionManager, String url) throws Exception
	{
		try
		{
			String cmd = Config.getBuildFirefoxPath();
			cmd = cmd + " -new-window " + url;
			Process browserTask = Portability.startProcess(cmd);
			ProcessWatchdog.initializeProcessWatchdog(browserTask);
			CurrentBrowserTask.setTask(browserTask);
		}
		catch ( Exception err )
		{
			err.printStackTrace();
			if ( actionManager != null )
			{
				actionManager.stopExecution();
			}
			throw new Exception(err.getMessage());
		}
	}

	public static void waitingForCurrentBrowserTaskToClose(ActionManager actionManager) throws Exception
	{
		if ( actionManager != null )
		{
			actionManager.setOutput("Waiting for browser to close...");
		}
		boolean CLOSED = false;
		while ( !CLOSED )
		{
			if ( actionManager != null && actionManager.isExecutionStopped() )
			{
				CurrentBrowserTask.eliminateTask();
				break;
			}
			boolean isBrowserRunning = CurrentBrowserTask.isActive();
			if ( isBrowserRunning )
			{
				com.rapidesuite.client.common.util.Utils.sleep(1000);
			}
			else
			{
				CLOSED = true;
				String msg = "Browser has closed.";
				if ( actionManager != null )
				{
					actionManager.setOutput(msg);
				}
			}
		}
	}

	public static File getFLDLogFile(String hostName,
			String userName,
			String password,
			String privateKeyFileName,
			SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod,
			String remoteLogFolder,
			String scriptName,
			File localFile)
	{
		SecureShellClient client = null;
		try
		{
			if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FTP) )
			{
				if (localFile == null) {
					return FTPManager.downloadFile(hostName, FTPManager.FTP_PORT, userName, password, remoteLogFolder, scriptName + ".log", true);
				} else {
					return FTPManager.downloadFile(hostName, FTPManager.FTP_PORT, userName, password, remoteLogFolder, scriptName + ".log", true, localFile);
				}
				
			} else if (connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FILE)) {
				if (localFile == null) {
					return FileProtocolManager.downloadFile(remoteLogFolder, scriptName + ".log", true);
				} else {
					return FileProtocolManager.downloadFile(remoteLogFolder, scriptName + ".log", true, localFile);
				}				
			}
			
			if (connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP)) {
				client = new SecureShellClient.PasswordBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, password, false);
			} else {
				client = new SecureShellClient.PrivateKeyBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, privateKeyFileName, false);
			}
			if ( !client.isConnected() )
			{
				FileUtils.println("Error: unable to connect via SSH");
				return null;
			}
			boolean isSuccess = client.cd(remoteLogFolder);
			if ( !isSuccess )
			{
				FileUtils.println("Error: unable to CD to remote script folder...");
			}
			if (localFile == null) {
				return client.get(remoteLogFolder, scriptName + ".log");
			} else {
				return client.get(remoteLogFolder, scriptName + ".log", localFile);
			}
			
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return null;
		}
		finally
		{
			if ( client != null )
			{
				client.close();
			}
		}
	}

	public static long getFLDLogFileSize(String hostName,
			String userName,
			String password,
			String privateKeyFileName,
			SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod,
			String remoteLogFolder,
			String scriptName)
	{
		SecureShellClient client = null;
		try
		{
			if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FTP) )
			{
				return FTPManager.getSize(hostName, FTPManager.FTP_PORT, userName, password, remoteLogFolder, scriptName + ".log");
			}
			if ( connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.FILE) )
			{
				return FileProtocolManager.getSize(remoteLogFolder, scriptName + ".log");
			}
			
			if (connectionMethod.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP)) {
				client = new SecureShellClient.PasswordBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, password, false);
			} else {
				client = new SecureShellClient.PrivateKeyBasedSecureShellClient(hostName, SecureShellClient.SSH_DEFAULT_PORT, userName, privateKeyFileName, false);
			}
			if ( !client.isConnected() )
			{
				FileUtils.println("Error: unable to connect via SSH");
				return -1;
			}
			boolean isSuccess = client.cd(remoteLogFolder);
			if ( !isSuccess )
			{
				FileUtils.println("Error: unable to CD to remote script folder...");
			}
			return client.getSize(remoteLogFolder + UtilsConstants.FORWARD_SLASH + scriptName + ".log");
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return -1;
		}
		finally
		{
			if ( client != null )
			{
				client.close();
			}
		}
	}

    private static boolean confirmFldLogSize(final Long previousLogSize,
            String hostName,
            String userName,
            String password,
            String privateKeyFileName,
            SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod,
            String remoteLogFolder,
            String scriptName,
            ActionManager actionManager,
            final int sleepingTime)
    throws Exception
    {
    	Assert.isTrue(sleepingTime >= 0);

		if ( actionManager != null  )
		{
			actionManager.setOutput("Double-checking downloaded log file length for " + scriptName + ":  About to sleep for " + sleepingTime + " seconds...");
		}

        com.rapidesuite.client.common.util.Utils.sleep(sleepingTime * 1000);

        long newLogSize = getFLDLogFileSize(hostName, userName, password, privateKeyFileName, connectionMethod, remoteLogFolder, scriptName);
        if ( previousLogSize == null && newLogSize == -1 )
        {
            return true;
        }
        else if ( previousLogSize == null || newLogSize == -1 )
        {
            return false;
        }

    	final boolean logSizeHasntChanged = (newLogSize == previousLogSize.longValue());
		if ( actionManager != null )
		{
			actionManager.setOutput("Double-checking downloaded log file length for " + scriptName + ": previous log size = " + previousLogSize +
					", current size = " + newLogSize + ".  Equal = " + logSizeHasntChanged);
		}

		return logSizeHasntChanged;
    }
    
    private static boolean confirmIlLogSize(final File logFile, final String scriptName, final ActionManager actionManager, final int sleepingTime, final Long previousLogSize) throws InterruptedException {
    	Assert.isTrue(sleepingTime >= 0);
    	
		if ( actionManager != null  )
		{
			actionManager.setOutput("Double-checking log file length for " + scriptName + ":  About to sleep for " + sleepingTime + " seconds...");
		}

        com.rapidesuite.client.common.util.Utils.sleep(sleepingTime * 1000);
        
        final int newLogSize = (int) logFile.length();
        
        if ( previousLogSize == null && newLogSize == 0 )
        {
            return true;
        }
        else if ( previousLogSize == null || newLogSize == 0)
        {
            return false;
        }
        
        Assert.isTrue(previousLogSize <= newLogSize, "log size must not shrink");
        
    	final boolean logSizeHasntChanged = previousLogSize == newLogSize;
		if ( actionManager != null )
		{
			actionManager.setOutput("Double-checking log file length for " + scriptName + ": previous log size = " + previousLogSize +
					", current size = " + newLogSize + ".  Equal = " + logSizeHasntChanged);
		}        
        
        return logSizeHasntChanged;
    }

	public static void waitingForFormToStart(ActionManager actionManager,
			SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel,
			int maxIteration,
			String scriptName,
			String hostName,
			String hostUserName,
			String hostPassword,
			String privateKeyFileName,
			SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod,
			String fldLogFolder) throws Exception
	{
		boolean FORM_STARTED = false;
		int counter = 0;
		if ( actionManager != null )
		{
			actionManager.setOutput("Waiting for Oracle Forms to start...");
		}
		while ( !FORM_STARTED )
		{
			counter++;
			if ( swiftBuildPropertiesValidationPanel != null && !swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().isValidationStarted() )
			{
				return;
			}
			if ( actionManager != null && actionManager.isExecutionStopped() )
			{
				break;
			}

			if ( counter > maxIteration )
			{
				CurrentBrowserTask.eliminateTask();
				
				final String message = "Timeout waiting for Oracle Forms to start:\n" + "Please make sure you can start Oracle Apps from your browser then try again.";

				if (actionManager != null && actionManager.getCurrentInjectorIterationCount() > 0) {
					throw new ForceToGoToTheNextIterationException(actionManager.getLastExecutedIteration());
				} else {
					if (Config.getBuildContinueOnFailedInjector()) {
						throw new SkipInjectionException(new FailToStartInjectionAppletException(message));
					} else {
						throw new FailToStartInjectionAppletException(message);
					}
				}
			}
			boolean hasOracleFormsProcessStarted = hasOracleFormsProcess();
			boolean isLogFileHasLogin = false;
			if ( !hasOracleFormsProcessStarted )
			{
				/*
				 * Assumption: the script name is unique, so the log file is
				 * unique, else this logic fails. If the log file is found, then
				 * Oracle forms is started (or already completed and closed).
				 * Also the content must have the key as we can't ensure the log
				 * file is always created after Oracle Apps window appears on
				 * the client...
				 */
				String key = "VALUE FNDSCSGN SIGNON USERNAME 1";

				String msg = "Waiting for Oracle Forms to start... Downloading Script log file... Retry countdown: " + (maxIteration - counter);
				displayFormsPlaybackMessage(swiftBuildPropertiesValidationPanel, msg);

				File logFile = getFLDLogFile(hostName, hostUserName, hostPassword, privateKeyFileName, connectionMethod, fldLogFolder, scriptName, null);
				String content = null;
				if ( null != logFile )
				{
				    content = org.apache.commons.io.FileUtils.readFileToString(logFile, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
				}

				isLogFileHasLogin = (content != null) && content.indexOf(key) != -1;
				if ( isLogFileHasLogin )
				{
					msg = "Waiting for Oracle Forms to start... Script log file downloaded (Login info detected, Forms started)... Retry countdown: " + (maxIteration - counter);
					displayFormsPlaybackMessage(swiftBuildPropertiesValidationPanel, msg);
				}
				else
				{
					msg = "Waiting for Oracle Forms to start...";
					if ( content == null )
					{
						msg += " No script log file on server... ";
					}
					else
					{
						msg += " Script log file downloaded (No Login info)... ";
					}
					msg += "Retry countdown: " + (maxIteration - counter);
					displayFormsPlaybackMessage(swiftBuildPropertiesValidationPanel, msg);
				}
			}

			if ( hasOracleFormsProcessStarted || isLogFileHasLogin )
			{
				FORM_STARTED = true;
				String msg = "Oracle Forms has started.";
				if ( hasOracleFormsProcessStarted )
				{
					msg += " (Process)";
				}
				else
				{
					msg += " (Log file)";
				}
				displayFormsPlaybackMessage(swiftBuildPropertiesValidationPanel, msg);
				if ( actionManager != null )
				{
					actionManager.setOutput(msg);
				}
			}
			else
			{
				com.rapidesuite.client.common.util.Utils.sleep(1000);
			}
		}
	}

	public static File getFLDLogFileOrPauseIfNotFound(String hostName,
			String userName,
			String password,
			String privateKeyFileName,
			SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod,
			String remoteLogFolder,
			String scriptName,
			ActionManager actionManager,
			EnvironmentValidationPanel environmentValidationPanel)
	{
		int retryCounter = 0;
		while ( true )
		{
			if ( (actionManager != null && actionManager.isExecutionStopped()) || (environmentValidationPanel != null && !environmentValidationPanel.isValidationStarted()) )
			{
				return null;
			}
			File logFile = getFLDLogFile(hostName, userName, password, privateKeyFileName, connectionMethod, remoteLogFolder, scriptName, null);
			if ( logFile == null )
			{
				retryCounter++;
				SwiftBuildUtils.pause(Config.getBuildPauseBetweenScriptCompletedAndLogFileRetrieve(),
						"before trying to download the FLD log file (Retry " + retryCounter + " )...",
						environmentValidationPanel,
						actionManager);
			}
			else
			{
				return logFile;
			}
		}
	}
	
	private static class WaitForScriptToCompleteRunnable implements Runnable {
		
		private boolean hasBecomeBackgroundProcess;
		private Throwable throwableToBeHandledByTheMainThread;
		
		private final boolean initializedWithActionManager;
		
		private ActionManager actionManagerInstance;
		private final File iterationLogDirectory;	
		
		private final Semaphore semaphore;
		
		final int maxIterationFormsToClose;
		final Injector injector;
		final RobotPasteAction robotPasteAction;
		final InjectionTypeSpecifics injectionTypeSpecifics;
		final BufferedReader bufferedReaderForTracking;		
		
/*		How the semaphore is used here:
		1. Create a semaphore with 1 permit
		2. Acquire that 1 permit
		3. Start a thread which executes WaitForScriptToCompleteRunnable
		4. The main thread tries to acquire 1 permit, but this permit should NOT be immediately available, thus the main thread will wait
		5. WaitForScriptToCompleteRunnable is running, and it will release 1 permit if:
			a. It has completed
			b. The log file has not grown after certain duration (which is a case of error). It will halt the WaitForScriptToCompleteRunnable
			c. The user manually stopped the injection. WaitForScriptToCompleteRunnable will still continue, but it will no longer interact with the main thread.
				In this case, WaitForScriptToCompleteRunnable is becoming a background process
			d. The partition is so large that BUILD_<FLD OR HTML>_FORM_CLOSE_MAX_ITERATION limit is exceeded.
				Like in case "c", WaitForScriptToCompleteRunnable will still continue as a background process which will not interact with the main thread
			e. Any exception/error which halts the WaitForScriptToCompleteRunnable
		6. Because the WaitForScriptToCompleteRunnable has released its permit, now the main thread can finally get a permit
		7. The main thread checks if there is any exception to be propagated to the higher level by checking the "throwableToBeHandledByTheMainThread" in the WaitForScriptToCompleteRunnable
		8. The main thread releases its permit
		9. If there is an exception to be propagated, the main thread will re-throw it
*/				
		
		public WaitForScriptToCompleteRunnable(final Semaphore semaphore,
				final ActionManager actionManager, 
				final int maxIterationFormsToClose,
				final Injector injector,
				final RobotPasteAction robotPasteAction,
				final InjectionTypeSpecifics injectionTypeSpecifics,
				final BufferedReader bufferedReaderForTracking				
				) {
			
			this.semaphore = semaphore;
			this.throwableToBeHandledByTheMainThread = null;
			this.hasBecomeBackgroundProcess = false;			
			this.maxIterationFormsToClose = maxIterationFormsToClose;
			this.injector = injector;
			this.robotPasteAction = robotPasteAction;
			this.injectionTypeSpecifics = injectionTypeSpecifics;
			this.bufferedReaderForTracking = bufferedReaderForTracking;
			this.initializedWithActionManager = actionManager != null;
			this.actionManagerInstance = actionManager;
			this.iterationLogDirectory = actionManager == null? null : actionManager.getIterationLogDirectory();
			
		}
		
		public Throwable getThrowableToBeHandledByTheMainThread() {
			return this.throwableToBeHandledByTheMainThread;
		}			
		
		private void releaseSemaphore(Throwable throwable) {
			if (!hasBecomeBackgroundProcess) {
				if (throwable != null) {
					this.throwableToBeHandledByTheMainThread = throwable;
				}					
				actionManagerInstance = null;
				hasBecomeBackgroundProcess = true;
				
				//WARNING: semaphore.release() must be the final operation
				//anything after semaphore.release will be ignored by the main thread
				semaphore.release();				
			}
		}			
		
		@Override
		public void run() {
			Throwable throwable = null;
			try {
				doRun();
			} catch (Throwable t) {
				throwable = t;
			} finally {
				releaseSemaphore(throwable);
			}
		}
		
		private boolean waitForFormToCloseOrTimeout(int maxIteration) throws Exception
		{
			int counter = 0;
			int sleepTime = 1000;
			while ( true )
			{
				counter++;

				if ( counter > maxIteration )
				{
					if (actionManagerInstance != null) {
						actionManagerInstance.setOutput("Script completed but the Oracle form " + " did not close after " + (maxIteration * sleepTime) / 1000
								+ " secs. Forcing the Oracle Form to close and executing the next script...");							
					}
					return false;
				}

				boolean hasOracleFormsProcessStarted = hasOracleFormsProcess();

				if ( hasOracleFormsProcessStarted )
				{
					if (actionManagerInstance != null) {
						actionManagerInstance.setOutput("Script completed but the Oracle Form has not yet closed. Retrying (" + counter + UtilsConstants.FORWARD_SLASH + maxIteration + ")...");
					}
					com.rapidesuite.client.common.util.Utils.sleep(sleepTime);
				}
				else
				{
					if (actionManagerInstance != null) {
						actionManagerInstance.setOutput("Oracle Form has closed.");
					}
					return true;
				}
			}
		}			
		
		private void onManualStop(final Integer currentIterationIndex, final Injector injector, final StringBuffer currentIterationLog, final InjectionTypeSpecifics injectionTypeSpecifics, final boolean terminateThread) throws AWTException, IOException, ManualStopException {
			final Date now = new Date();
			if (actionManagerInstance != null) {
				if (currentIterationIndex != null) {
					actionManagerInstance.getFailedIterations().get(injector).add(currentIterationIndex);
					IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), currentIterationIndex, IterationLogger.ITERATION_STATUS.ITERATION_ERROR, now, currentIterationLog==null?"":currentIterationLog.toString());
					if (currentIterationLog != null) {
						currentIterationLog.delete(0, currentIterationLog.length());
					}
					actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().setLastFailedIteration(injector, currentIterationIndex, true, injectionTypeSpecifics.getLogFileForViewing());
					actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().displayExecutionRuntimeMeasurement(actionManagerInstance.getNumberOfExecutedIterations(), actionManagerInstance.getFailedIterations().get(injector).size());
				} else if (actionManagerInstance.isInjectorStartHasBeenRecorded()) {
					IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_ERROR, now, "");
				}				
			}


			
			CurrentBrowserTask.eliminateTask();
			if (terminateThread || injectionTypeSpecifics instanceof IlInjectionSpecifics) {
				throw new ManualStopException();
			} else {
				releaseSemaphore(new ManualStopException());
			}
		}
		
		private void onInjectorWithIterationLogDoesNotGrowErrorMarshaling(final Injector injector, final int currentIterationIndex, final String errorMessage, final InjectionTypeSpecifics injectionLogRelatedVariables) {	
			if ((actionManagerInstance.getNumberOfIterationExecutionAttemptRemaining() != null && actionManagerInstance.getNumberOfIterationExecutionAttemptRemaining().intValue() == 0) || injectionLogRelatedVariables.getBuildMaximumIterationExecutionAttempts() == 0) {
				if (actionManagerInstance.getMaxIterationNumber() != null && currentIterationIndex < actionManagerInstance.getMaxIterationNumber()) {
					throw new RepeatIterationsException(injector, currentIterationIndex, new Exception());
				} else if (injector.getIndex() < actionManagerInstance.getInjectorsToExecute().size()-1 && Config.getBuildContinueOnFailedInjector()) {
					throw new RepeatIterationsException(injector, currentIterationIndex, new Exception());
				} else if (Config.getBuildTerminateAfterFailedInjection()) {
					throw new RepeatIterationsException(injector, currentIterationIndex, new FailedInjectionException());
				} else {
					CurrentBrowserTask.eliminateTask();
					onOracleFormsCrash(actionManagerInstance);
				}
			} else {
				throw new RepeatIterationsException(injector, currentIterationIndex, new Exception(errorMessage));
			}
		}		
		
		private void matchFldLog(final String incomingLogSegment, final BufferedReader bufferedReaderForTracking, File logFile, InjectorsPackageExecutionPanel injectorsPackageExecutionPanel, Injector injector, MutableInt currentIterationForTracking) throws Exception {
			if (bufferedReaderForTracking == null || actionManagerInstance == null) {
				return;
			}
			final String incomingLogSegmentLinesRaw[] = incomingLogSegment.split("\\r?\\n");
			final List<String> incomingLogSegmentLines = new ArrayList<String>();
			String lineTempLog = null;
			for (int i = 0 ; i < incomingLogSegmentLinesRaw.length ; i++) {
				String line = incomingLogSegmentLinesRaw[i];
				line = line.trim();
				if (isIgnorableFldLine(line)) {
					continue;
				}
				StringBuffer lineTrimmedBfr = new StringBuffer(line);
				boolean continueToTheNextLine = normalizeLineForMatching(lineTrimmedBfr);
				line = lineTrimmedBfr.toString();
				if (lineTempLog == null) {
					lineTempLog = line;
				} else {
					lineTempLog += line;
				}
				if (!continueToTheNextLine) {
					lineTempLog = lineTempLog.trim();
					incomingLogSegmentLines.add(lineTempLog);				
					lineTempLog = null;
				}				
			}
			
			int logSegmentLinesIndex = 0;
			String scriptLine = null;
			String lineTempScript = null;
			while ((logSegmentLinesIndex < incomingLogSegmentLines.size()) && ((scriptLine = bufferedReaderForTracking.readLine()) != null)) {
				String line = scriptLine;
				line = line.trim();
				if (isIterationNumberLine(line)) {
					currentIterationForTracking.setValue(Utils.retrieveIterationNumberFromIterationText(line));
				}
				if (isIgnorableFldLine(line)) {
					continue;
				}
				StringBuffer lineBfr = new StringBuffer(line);
				boolean continueToTheNextLine = normalizeLineForMatching(lineBfr);
				line = lineBfr.toString();	
				if (lineTempScript == null) {
					lineTempScript = line;
				} else {
					lineTempScript += line;
				}	
				if (!continueToTheNextLine) {
					line = line.trim();
					String logLine = incomingLogSegmentLines.get(logSegmentLinesIndex);
					if (!isFldLineMatching(logLine, line)) {
						injectorsPackageExecutionPanel.updateStatus(injector.getIndex(), SwiftBuildConstants.INJECTOR_EXECUTION_LOG_MISMATCH_VALUE);
						injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_LOG_MISMATCH_VALUE);
						final String fldScriptLineAfterRedactingPassword = SwiftBuildFileUtils.removePasswordFromInputString(line, CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM).trim();
						final String logLineAfterRedactingPassword = SwiftBuildFileUtils.removePasswordFromInputString(logLine, CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM).trim();
						final String currentIterationNumber = currentIterationForTracking.intValue() < 1 ? "<At injection start>" : "<Iteration "+currentIterationForTracking.intValue()+">";
						final String message = 
								"----------------------------------------------------------------"+System.lineSeparator()+
								"Mismatch at injector "+injector.getName()+" "+currentIterationNumber+System.lineSeparator()+
								"FLD: "+fldScriptLineAfterRedactingPassword+System.lineSeparator()+
								"Log: "+logLineAfterRedactingPassword+System.lineSeparator()+
								"----------------------------------------------------------------"+System.lineSeparator();
						
						if (logFile != null) {
							logFile.getParentFile().mkdirs();
							CoreUtil.writeToFile(message, true, logFile.getParentFile(), logFile.getName());
						}
						
						if (Config.getBuildHaltFldInjectionIfLogMismatches()) {
							throw new LogMismatchesException("Log mismatch at "+injector.getName());
						}
					}
					logSegmentLinesIndex++;			
					lineTempScript = null;
				}
			}
		}			
		
		private void recordIterations(final Injector injector, final LogTrackingVariables logTrackingVariables, final String logContent, final BufferedReader bufferedReaderForTracking, MutableInt currentIterationForTracking, InjectionTypeSpecifics injectionTypeSpecifics) throws Exception {
			if (injector == null) {
				return;
			}
			
			final String SUFFIX = CoreConstants.RAPIDBUILD_ITERATION_NUMBER_ASTERISKS;
			final String PREFIX_END_OF_ITERATION = CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX;
			final String PREFIX_START_OF_ITERATION = CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX;
			
			final Integer initialLastLogIndex = logTrackingVariables.getLastLogIndex();
			final int initialLogContentOffset = initialLastLogIndex==null?0:initialLastLogIndex;
			int logContentOffset = 	initialLogContentOffset;
			while(logContentOffset < logContent.length()) {
				
				final int prefixEndOfIterationIndex = logContent.indexOf(PREFIX_END_OF_ITERATION, logContentOffset);
				final int prefixStartOfIterationIndex = logContent.indexOf(PREFIX_START_OF_ITERATION, logContentOffset);

				Assert.isTrue((prefixEndOfIterationIndex == -1 && prefixStartOfIterationIndex == -1) || (prefixEndOfIterationIndex != prefixStartOfIterationIndex));
				if (prefixEndOfIterationIndex == -1 && prefixStartOfIterationIndex == -1) {
					if (logTrackingVariables.getCurrentIterationIndex() == null) {
						if (logTrackingVariables.getCurrentNonIterationLog() == null) {
							logTrackingVariables.setCurrentNonIterationLog(new StringBuffer());
						}
						logTrackingVariables.getCurrentNonIterationLog().append(logContent.substring(logTrackingVariables.getLastLogIndex()==null?0:(logTrackingVariables.getLastLogIndex())));
						logTrackingVariables.setLastLogIndex(logContent.length());
						if (actionManagerInstance != null) {
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, logTrackingVariables.isFirstTimeEncounteringNonIterationLog()? IterationLogger.ITERATION_STATUS.INJECTOR_START : IterationLogger.ITERATION_STATUS.INJECTOR_CONTINUE, new Date(), logTrackingVariables.getCurrentNonIterationLog().toString());
						}
						if (actionManagerInstance != null && logTrackingVariables.isFirstTimeEncounteringNonIterationLog()) {
							actionManagerInstance.setInjectorStartHasBeenRecorded(true);
						}
						logTrackingVariables.setCurrentNonIterationLog(null);
						logTrackingVariables.setFirstTimeEncounteringNonIterationLog(false);
					} else if (logTrackingVariables.getCurrentIterationLog() != null && logTrackingVariables.getLastLogIndex() < logContent.length()) {
						logTrackingVariables.getCurrentIterationLog().append(logContent.substring(logTrackingVariables.getLastLogIndex(), logContent.length()));
						logTrackingVariables.setLastLogIndex(logContent.length());
						if (actionManagerInstance != null) {
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), logTrackingVariables.getCurrentIterationIndex(), IterationLogger.ITERATION_STATUS.ITERATION_CONTINUE, new Date(), logTrackingVariables.getCurrentIterationLog().toString());	
						}
						if (logTrackingVariables.getCurrentIterationLog() != null) {
							logTrackingVariables.getCurrentIterationLog().delete(0, logTrackingVariables.getCurrentIterationLog().length());
						}
					}
					break;
				} else {
					final boolean isEndMarker = prefixEndOfIterationIndex==-1?false:(prefixStartOfIterationIndex==-1?true:(prefixEndOfIterationIndex<prefixStartOfIterationIndex));

					Assert.isTrue(isEndMarker || prefixStartOfIterationIndex != -1);
					Assert.isTrue(!isEndMarker || prefixEndOfIterationIndex != -1);

					int suffixIndex = logContent.indexOf(SUFFIX, isEndMarker?(prefixEndOfIterationIndex+PREFIX_END_OF_ITERATION.length()):(prefixStartOfIterationIndex+PREFIX_START_OF_ITERATION.length()));
					Assert.isTrue(suffixIndex > -1);

					String iterationNumberStr = logContent.substring(isEndMarker?(prefixEndOfIterationIndex+PREFIX_END_OF_ITERATION.length()):(prefixStartOfIterationIndex+PREFIX_START_OF_ITERATION.length()), suffixIndex);
					Assert.notNull(iterationNumberStr);

					Integer iterationNumber = Integer.parseInt(iterationNumberStr);
					Assert.notNull(iterationNumber);

					if ((isEndMarker && logTrackingVariables.getCompletedIterations().contains(iterationNumber)) || (!isEndMarker && logTrackingVariables.getCurrentIterationIndex() != null && logTrackingVariables.getCurrentIterationIndex().intValue() == iterationNumber.intValue())) {

						logContentOffset = suffixIndex + SUFFIX.length();
						continue;
					}

					if (isEndMarker) {
						Assert.notNull(logTrackingVariables.getCurrentIterationIndex());
						Assert.isTrue(logTrackingVariables.getCurrentIterationIndex() == iterationNumber.intValue());
						Assert.isTrue(!logTrackingVariables.getCompletedIterations().contains(iterationNumber));
						logTrackingVariables.getCompletedIterations().add(iterationNumber);
						if (actionManagerInstance != null && actionManagerInstance.getFailedIterations().get(injector).contains(logTrackingVariables.getCurrentIterationIndex())) {
							actionManagerInstance.getFailedIterations().get(injector).remove(logTrackingVariables.getCurrentIterationIndex());
						}
						Assert.notNull(logTrackingVariables.getLastLogIndex());
						int firstLineBreakAfterSuffix = logContent.indexOf("\n", suffixIndex);
						if (firstLineBreakAfterSuffix == -1) {
							firstLineBreakAfterSuffix = logContent.length();
						}
						logTrackingVariables.getCurrentIterationLog().append(logContent.substring(logTrackingVariables.getLastLogIndex()+1, firstLineBreakAfterSuffix));
						logTrackingVariables.setLastLogIndex(firstLineBreakAfterSuffix);
						if (actionManagerInstance != null) {
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), logTrackingVariables.getCurrentIterationIndex(), IterationLogger.ITERATION_STATUS.ITERATION_COMPLETE, new Date(), logTrackingVariables.getCurrentIterationLog().toString());							
						}
						logTrackingVariables.setCurrentIterationLog(null);
						logTrackingVariables.setCurrentIterationIndex(null);
						logTrackingVariables.setCurrentIterationStartingTime(null);
						if (actionManagerInstance != null) {
							actionManagerInstance.addToExecutedIterations(iterationNumber);
							actionManagerInstance.setCurrentIterationNumber(null);
							actionManagerInstance.setLastExecutedIteration(iterationNumber);
							
							//note that build won't automatically re-execute successful iteration
							//but manual re-execution will cause actionManager to be re-initialized
							//so, don't worry about this iteration is re-executed (and failed), thus making this value outdated
							actionManagerInstance.getSuccessfulIterations().get(injector).add(iterationNumber);
							Assert.notNull(actionManagerInstance.getEffectiveNumberOfIterationsForThisInjector(), "effectiveNumberOfIterationsForThisInjector must have been set for injector with iteration enabled");
							if (actionManagerInstance.getInjectorsPackageExecutionPanel() != null) {
								if (actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement() != null) {
									actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().markIteration(iterationNumber);
									actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().setLastSuccessfulIteration(injector, iterationNumber, injectionTypeSpecifics.getLogFileForViewing());
								}
								
							}
							
							actionManagerInstance.setOutput("Iteration #" + iterationNumber + " has ended, "+(actionManagerInstance.getEffectiveNumberOfIterationsForThisInjector()-actionManagerInstance.getNumberOfExecutedIterations())+" iterations remaining.");
							
						}
	                    logContentOffset = firstLineBreakAfterSuffix;
					} else {
						Assert.isNull(logTrackingVariables.getCurrentIterationIndex());

						logTrackingVariables.setCurrentNonIterationLog(new StringBuffer());
						int lastLineBeforeIterationStartIndex = logContent.substring(0, prefixStartOfIterationIndex).lastIndexOf("\n");
						if (lastLineBeforeIterationStartIndex == -1) {
							lastLineBeforeIterationStartIndex = 0;
						}
						logTrackingVariables.getCurrentNonIterationLog().append(logContent.substring(logTrackingVariables.getLastLogIndex()==null?0:(logTrackingVariables.getLastLogIndex()), lastLineBeforeIterationStartIndex));
						logTrackingVariables.setLastLogIndex(lastLineBeforeIterationStartIndex);
						if (actionManagerInstance != null) {
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), logTrackingVariables.getCurrentIterationIndex(), logTrackingVariables.isFirstTimeEncounteringNonIterationLog()? IterationLogger.ITERATION_STATUS.INJECTOR_START : IterationLogger.ITERATION_STATUS.INJECTOR_CONTINUE, new Date(), logTrackingVariables.getCurrentNonIterationLog().toString());
							if (logTrackingVariables.isFirstTimeEncounteringNonIterationLog()) {
								actionManagerInstance.setInjectorStartHasBeenRecorded(true);
							}								
						}	
						logTrackingVariables.setCurrentNonIterationLog(null);
						logTrackingVariables.setFirstTimeEncounteringNonIterationLog(false);

						Assert.isTrue(!logTrackingVariables.getCompletedIterations().contains(iterationNumber));
						logTrackingVariables.setCurrentIterationIndex(iterationNumber);
						Assert.isNull(logTrackingVariables.getCurrentIterationLog());

						logTrackingVariables.setCurrentIterationLog(new StringBuffer());
						int firstLineBreakAfterSuffix = logContent.indexOf("\n", suffixIndex);
						if (firstLineBreakAfterSuffix == -1) {
							firstLineBreakAfterSuffix = logContent.length();
						}								
						logTrackingVariables.getCurrentIterationLog().append(logContent.substring(lastLineBeforeIterationStartIndex, firstLineBreakAfterSuffix));
						logTrackingVariables.setLastLogIndex(firstLineBreakAfterSuffix);

						logTrackingVariables.setCurrentIterationStartingTime(new Date());

						if (actionManagerInstance != null) {
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), logTrackingVariables.getCurrentIterationIndex(), IterationLogger.ITERATION_STATUS.ITERATION_START, logTrackingVariables.getCurrentIterationStartingTime(), logTrackingVariables.getCurrentIterationLog().toString());		
						}
						logTrackingVariables.getCurrentIterationLog().delete(0, logTrackingVariables.getCurrentIterationLog().length());
						if (actionManagerInstance != null) {
							actionManagerInstance.setFirstIntendedIterationAtCurrentPartitionHasBeenStarted(true);
							actionManagerInstance.setCurrentIterationNumber(iterationNumber);
							actionManagerInstance.setOutput("Iteration #" + iterationNumber + " has begun.");							
						}
	                    logContentOffset = firstLineBreakAfterSuffix;
					}

				}
			}
			if (actionManagerInstance != null && actionManagerInstance.getInjectorsPackageExecutionPanel() != null && actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement() != null) {
				actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().displayExecutionRuntimeMeasurement(actionManagerInstance.getNumberOfExecutedIterations(), actionManagerInstance.getFailedIterations().get(injector).size());
			}		
			
			logTrackingVariables.setLastLogIndex(logTrackingVariables.getLastLogIndex()==null?logContentOffset:Math.max(logTrackingVariables.getLastLogIndex(), logContentOffset));
		}
		
		private static Character getTheLastCharacter(final File fileToRead) throws IOException {
			if (fileToRead == null || !fileToRead.isFile()) {
				return null;
			}
			
			try (final Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToRead), CoreConstants.CHARACTER_SET_ENCODING))) {
				Character lastCharacter = null;
				
				char cbuf[] = new char[1024];
				int read = 0;
				while((read = reader.read(cbuf)) > 0) {
					lastCharacter = cbuf[read-1];
				}
				
				return lastCharacter;
			}
		}
		
		private void doRun() throws Exception {
			Assert.notNull(actionManagerInstance, "action manager must not be null for injection");
			Assert.notNull(injectionTypeSpecifics, "injectionLogRelatedVariables must not be null");
			if (!(injectionTypeSpecifics instanceof FldInjectionSpecifics) && !(injectionTypeSpecifics instanceof IlInjectionSpecifics)) {
				throw new UnsupportedOperationException("Only FLD and IL injector is supported");
			}
			
			final MutableInt currentIterationForTracking = new MutableInt();
			
			int counter = 0;
			actionManagerInstance.setOutput(injectionTypeSpecifics.getStartingMessage());
			
			int remainingSleepTimeSecondsForLogSizeChange = injectionTypeSpecifics.getBuildLogSizeMaxConfirmationDelaySeconds();
			int nextWaitingTimeExponentForLogSizeChange = 0;

			final LogTrackingVariables logTrackingVariables = new LogTrackingVariables();

			Long lastLogContentSize = null;
			int totalOffset = 0;
			File logFile = null;
			boolean logFileHasBeenReadToEof = true;
			while ( true )
			{
				counter++;
				String msg = "[" + counter + " / " + maxIterationFormsToClose + " retries].";
				if (checkIfManualStop(actionManagerInstance, null) )
				{
					onManualStop(logTrackingVariables.getCurrentIterationIndex(), injector, logTrackingVariables.getCurrentIterationLog(), injectionTypeSpecifics, false);
				}
				if (actionManagerInstance != null && counter > maxIterationFormsToClose )
				{
					final Date now = new Date();
					File screenshot = null;
					if (logTrackingVariables.getCurrentIterationIndex() != null) {
						actionManagerInstance.getFailedIterations().get(injector).add(logTrackingVariables.getCurrentIterationIndex());
						
						String iterationErrorScreenshotName = IterationLogger.getScreenshotFileName(injector.getNumberFromUserPerspective(), logTrackingVariables.getCurrentIterationIndex(), now);
						screenshot = GUIUtils.captureScreenshotAndSaveAsJpg(iterationLogDirectory, iterationErrorScreenshotName);
						IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), logTrackingVariables.getCurrentIterationIndex(), IterationLogger.ITERATION_STATUS.ITERATION_ERROR, now, logTrackingVariables.getCurrentIterationLog()==null?iterationErrorScreenshotName:logTrackingVariables.getCurrentIterationLog().toString()+"\n"+iterationErrorScreenshotName);
						if (logTrackingVariables.getCurrentIterationLog() != null) {
							logTrackingVariables.getCurrentIterationLog().delete(0, logTrackingVariables.getCurrentIterationLog().length());
						}
						actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().setLastFailedIteration(injector, logTrackingVariables.getCurrentIterationIndex(), false, injectionTypeSpecifics.getLogFileForViewing());
						actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().displayExecutionRuntimeMeasurement(actionManagerInstance.getNumberOfExecutedIterations(), actionManagerInstance.getFailedIterations().get(injector).size());								
						this.releaseSemaphore(new RepeatIterationsException(injector, logTrackingVariables.getCurrentIterationIndex(), new Exception(injectionTypeSpecifics.getFullCloseMaxIterationErrorMessage(msg))));
					} else if (actionManagerInstance.isJustStartingIteration()) {
						Assert.notNull(actionManagerInstance.getIterationStartNumber());
						actionManagerInstance.getFailedIterations().get(injector).add(actionManagerInstance.getIterationStartNumber());
						if (actionManagerInstance.isInjectorStartHasBeenRecorded()) {
							String injectorErrorScreenshotName = IterationLogger.getScreenshotFileName(injector.getNumberFromUserPerspective(), -1, now);
							screenshot = GUIUtils.captureScreenshotAndSaveAsJpg(iterationLogDirectory, injectorErrorScreenshotName);
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_ERROR, now, injectorErrorScreenshotName);
						}
						actionManagerInstance.setCurrentIterationNumber(actionManagerInstance.getIterationStartNumber());
						this.releaseSemaphore(new RepeatIterationsException(injector, actionManagerInstance.getIterationStartNumber(), new Exception(injectionTypeSpecifics.getFullCloseMaxIterationErrorMessage(msg))));
					} else {
						if (actionManagerInstance.isInjectorStartHasBeenRecorded()) {
							String injectorErrorScreenshotName = IterationLogger.getScreenshotFileName(injector.getNumberFromUserPerspective(), -1, now);
							screenshot = GUIUtils.captureScreenshotAndSaveAsJpg(iterationLogDirectory, injectorErrorScreenshotName);
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_ERROR, now, injectorErrorScreenshotName);
						}
						
						if (actionManagerInstance.getCurrentInjectorIterationCount() > 0) {
							this.releaseSemaphore(new ForceToGoToTheNextIterationException(actionManagerInstance.getLastExecutedIteration()));
		 				} else if (Config.getBuildContinueOnFailedInjector()) {
							this.releaseSemaphore(new SkipInjectionException(new Exception(injectionTypeSpecifics.getFullCloseMaxIterationErrorMessage(msg))));
						} else {
							this.releaseSemaphore(new Exception(injectionTypeSpecifics.getFullCloseMaxIterationErrorMessage(msg)));
						}								
					}
				}

				
				String logContent = null;
				Long logContentSize = null;
				while (true) { //try to retrieve the log file until the retrieval succeeds
					if (logFileHasBeenReadToEof ||
							// re-read the log file "if the reference is valid, but the file doesn't exist"
							// somehow the log file is being deleted somewhere.
							(logFile != null && !logFile.isFile())
							) {
						logFile = injectionTypeSpecifics.getLogFile(actionManagerInstance == null);
			            if ( logFile != null)
			            {
			            	if (actionManagerInstance != null && injectionTypeSpecifics instanceof FldInjectionSpecifics && Config.getBuildAssumeFldLogAlwaysAssumeWithNewLine()) {
			            		Character lastCharacterOfTheLogFile = getTheLastCharacter(logFile);
			            		if (lastCharacterOfTheLogFile != null && lastCharacterOfTheLogFile != '\n') {
			            			actionManagerInstance.setOutput("The downloaded log file is truncated; will redo log retrieval...");
									if (checkIfManualStop(actionManagerInstance, null)) {
										onManualStop(logTrackingVariables.getCurrentIterationIndex(), injector, logTrackingVariables.getCurrentIterationLog(), injectionTypeSpecifics, false);
									}
			            			com.rapidesuite.client.common.util.Utils.sleep(1000);
			            			continue;
			            		}
			            	}
			            	logContentSize = logFile.length();
			            }
					}
					
					if ( logFile != null) {
						final int maxLimitForLogReading = 3;
			            if (lastLogContentSize == null && logContentSize != null) {
			            	final MutableBoolean fileWasReadOfToEof = new MutableBoolean(true);
			            	logContent = SwiftBuildFileUtils.readFldFileToStringWithOffsetWithMaxLimit(logFile, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING, totalOffset, fileWasReadOfToEof, maxLimitForLogReading);
			            	logFileHasBeenReadToEof = fileWasReadOfToEof.getValue();
			            	break;
			            } else if (!logFileHasBeenReadToEof || (lastLogContentSize != null && logContentSize != null && lastLogContentSize <= logContentSize)) {
			            	if (actionManagerInstance != null && actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement() != null
			            			&& (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()) || 
			            					CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))) {
			            		actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().displayNoIterationHasBeenCompletedRecently();
			            	}
			        		
			        		Integer lastLogIndex = logTrackingVariables.getLastLogIndex();
			        		if (lastLogIndex == null) {
			        			lastLogIndex = 0;
			        		}          

			                totalOffset += lastLogIndex;
			                final MutableBoolean fileWasReadOfToEof = new MutableBoolean(true);
			                logContent = SwiftBuildFileUtils.readFldFileToStringWithOffsetWithMaxLimit(logFile, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING, totalOffset, fileWasReadOfToEof, maxLimitForLogReading);		                
			                logFileHasBeenReadToEof = fileWasReadOfToEof.getValue();
			                
			        		if (logTrackingVariables.getLastLogIndex() != null) {
			        			logTrackingVariables.setLastLogIndex(0);
			        		}           	
			            	
			            	break;
			            }
					}

		            
		            final int timeoutSecond = 3;
		            if (actionManagerInstance != null) {
		            	actionManagerInstance.setOutput("Failure during log retrieval. Will try again in "+timeoutSecond+" seconds");
		            }
		            
		            
					if ((actionManagerInstance == null && logFile == null) || checkIfManualStop(actionManagerInstance, null) )
					{
						onManualStop(logTrackingVariables.getCurrentIterationIndex(), injector, logTrackingVariables.getCurrentIterationLog(), injectionTypeSpecifics, logFile==null);
					}	            
		            
		            com.rapidesuite.client.common.util.Utils.sleep(timeoutSecond*1000);
				}
				if (logContentSize != null) {
					lastLogContentSize = logContentSize;
				}
				
				Assert.notNull(logContent, "logContent must be set after successful log retrieval");
				Assert.notNull(lastLogContentSize, "lastLogContentSize must be set after successful log retrieval");

				if (actionManagerInstance != null && robotPasteAction != null && !robotPasteAction.hasExecuted() && robotPasteAction.shouldExecute(logContent) )
				{
				    robotPasteAction.execute(actionManagerInstance);
				}

				if (logContent != null) {
					
					recordIterations(injector, logTrackingVariables, logContent, bufferedReaderForTracking, currentIterationForTracking, injectionTypeSpecifics);


				}

				if ( injectionTypeSpecifics instanceof FldInjectionSpecifics && SwiftBuildFileUtils.isEndOfScript(logContent) )
				{
					if ( hasOracleFormsProcess() )
					{
						waitForFormToCloseOrTimeout(((FldInjectionSpecifics) injectionTypeSpecifics).getMaxIterationFormsHanged());

						if (actionManagerInstance != null && injector != null) {
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_COMPLETE, new Date(), "");
						}

						return;
					}
					else
					{
						if (actionManagerInstance != null) {
							actionManagerInstance.setOutput("Oracle Forms has closed.");
							if (injector != null) {
								IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_COMPLETE, new Date(), "");
							}
						}
						
						return;
					}
				} else if (injectionTypeSpecifics instanceof IlInjectionSpecifics &&  ((IlInjectionSpecifics) injectionTypeSpecifics).getCompletionFile().exists()) {
					if (initializedWithActionManager)
					{
						if (actionManagerInstance != null) {
							actionManagerInstance.setOutput("Injection completion file is found!");
						}
						
						CurrentBrowserTask.eliminateTask();
						
						if (actionManagerInstance != null && injector != null) {
							IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), 
									injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_COMPLETE, new Date(), "");						
						}
					}
					
					return;			
				} else {
					boolean logSizeHasChanged = false;
					do {
						if (checkIfManualStop(actionManagerInstance, null)) {
							onManualStop(logTrackingVariables.getCurrentIterationIndex(), injector, logTrackingVariables.getCurrentIterationLog(), injectionTypeSpecifics, false);
						}

						Assert.isTrue(!(injectionTypeSpecifics instanceof FldInjectionSpecifics) || !SwiftBuildFileUtils.isEndOfScript(logContent));
						
						if (!logFileHasBeenReadToEof) {
							break;
						}

						final int sleepingTimeToDetectLogSizeChange = Math.min(remainingSleepTimeSecondsForLogSizeChange, (int) Math.pow(2, nextWaitingTimeExponentForLogSizeChange));
						remainingSleepTimeSecondsForLogSizeChange -= sleepingTimeToDetectLogSizeChange;
						nextWaitingTimeExponentForLogSizeChange++;

						
						logSizeHasChanged = !injectionTypeSpecifics.confirmLogSize(logContentSize, sleepingTimeToDetectLogSizeChange, actionManagerInstance != null);
						
						if (sleepingTimeToDetectLogSizeChange >= BuildMain.SLEEPING_TIME_TO_CAPTURE_SCREENSHOT_IN_SECOND) {
							File sceenshotTiketDirectory = new File(Config.getTempFolder() + File.separator + BuildMain.SCREENSHOT_FOR_OPEN_TICKET + File.separator + iterationLogDirectory.getName());
							sceenshotTiketDirectory.mkdirs();
							actionManagerInstance.setSceenshotTiketDirectory(sceenshotTiketDirectory);
							GUIUtils.captureScreenshotAndSaveAsJpg(sceenshotTiketDirectory, IterationLogger.getScreenshotFileName(injector.getNumberFromUserPerspective(), -1, new Date()));
						}
						
		            	if (actionManagerInstance != null && !logSizeHasChanged && actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement() != null
		            			&& (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()) || 
		            					CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))) {
		            		actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().displayNoIterationHasBeenCompletedRecently();
		            	}
		
		            	if (logSizeHasChanged) {
							remainingSleepTimeSecondsForLogSizeChange = injectionTypeSpecifics.getBuildLogSizeMaxConfirmationDelaySeconds();
							nextWaitingTimeExponentForLogSizeChange = 0;
							if (actionManagerInstance != null) {
								actionManagerInstance.setOutput("Log file is of a different size than expected; will redo log retrieval...");
							}
							
						} else if (!logSizeHasChanged && remainingSleepTimeSecondsForLogSizeChange > 0) {
							if (logFile == null) {
								if (actionManagerInstance != null) {
									actionManagerInstance.setOutput("Nothing is known about the log file; will redo log retrieval...");
								}
								
								break;
							} else {
								if (actionManagerInstance != null) {
									actionManagerInstance.setOutput("The log file is not known to have been changed, but the injector has not completed. Please wait for up to "+remainingSleepTimeSecondsForLogSizeChange+" seconds");
									
									if(actionManagerInstance.isStoppedManuallyByUser()){
										com.rapidesuite.client.common.util.Utils.sleep(sleepingTimeToDetectLogSizeChange);
										actionManagerInstance.stopExecution();
									}
								}
								
							}
							continue;
						} else {
							final String errorMessage = injectionTypeSpecifics.getFullLogDoesNotGrowErrorMessage(msg);
							final Date now = new Date();
							if (actionManagerInstance != null) {
								if (logTrackingVariables.getCurrentIterationIndex() != null) {
									actionManagerInstance.getFailedIterations().get(injector).add(logTrackingVariables.getCurrentIterationIndex());
									String iterationErrorScreenshotName = IterationLogger.getScreenshotFileName(injector.getNumberFromUserPerspective(), logTrackingVariables.getCurrentIterationIndex(), now);
									GUIUtils.captureScreenshotAndSaveAsJpg(iterationLogDirectory, iterationErrorScreenshotName);
									IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), logTrackingVariables.getCurrentIterationIndex(), IterationLogger.ITERATION_STATUS.ITERATION_ERROR, now, logTrackingVariables.getCurrentIterationLog()==null?iterationErrorScreenshotName:logTrackingVariables.getCurrentIterationLog().toString()+"\n"+iterationErrorScreenshotName);
									actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().setLastFailedIteration(injector, logTrackingVariables.getCurrentIterationIndex(), false, injectionTypeSpecifics.getLogFileForViewing());
									actionManagerInstance.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().displayExecutionRuntimeMeasurement(actionManagerInstance.getNumberOfExecutedIterations(), actionManagerInstance.getFailedIterations().get(injector).size());										

								} else {
									actionManagerInstance.getFailedIterations().get(injector).add(actionManagerInstance.getIterationStartNumber());
									String injectorErrorScreenshotName = IterationLogger.getScreenshotFileName(injector.getNumberFromUserPerspective(), -1, now);
									GUIUtils.captureScreenshotAndSaveAsJpg(iterationLogDirectory, injectorErrorScreenshotName);
									IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_ERROR, now, injectorErrorScreenshotName);
								}
								
								
								if (actionManagerInstance.getFailedIterations().containsKey(injector) && !actionManagerInstance.getFailedIterations().get(injector).isEmpty() &&
										(!actionManagerInstance.getSuccessfulIterations().containsKey(injector) || actionManagerInstance.getSuccessfulIterations().get(injector).isEmpty())) {
									injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE);
								} else {
									injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_PARTIAL_COMPLETE_VALUE);
								}									
							}
							
							if (actionManagerInstance == null) {
								throw new Exception();
							} else if (logTrackingVariables.getCurrentIterationIndex() != null) {
								if (logTrackingVariables.getCurrentIterationLog() != null) {
									logTrackingVariables.getCurrentIterationLog().delete(0, logTrackingVariables.getCurrentIterationLog().length());
								}
								onInjectorWithIterationLogDoesNotGrowErrorMarshaling(injector, logTrackingVariables.getCurrentIterationIndex(), errorMessage, injectionTypeSpecifics);
							} else {
								if (actionManagerInstance.isJustStartingIteration()) {
									Assert.notNull(actionManagerInstance.getIterationStartNumber());
									actionManagerInstance.getFailedIterations().get(injector).add(actionManagerInstance.getIterationStartNumber());
									actionManagerInstance.setCurrentIterationNumber(actionManagerInstance.getIterationStartNumber());
									onInjectorWithIterationLogDoesNotGrowErrorMarshaling(injector, actionManagerInstance.getIterationStartNumber(), errorMessage, injectionTypeSpecifics);							
								} else {
									if (Config.getBuildTerminateAfterFailedInjection()) {
										throw new FailedInjectionException();
									} else {
										onOracleFormsCrash(actionManagerInstance);
									}
								}
							}	
							
							return;
						}				
					} while(!logSizeHasChanged && remainingSleepTimeSecondsForLogSizeChange > 0);
					
					
					if ((injectionTypeSpecifics instanceof FldInjectionSpecifics && hasOracleFormsProcess()) || (injectionTypeSpecifics instanceof IlInjectionSpecifics))
					{
						if (logFileHasBeenReadToEof) {
							com.rapidesuite.client.common.util.Utils.sleep(1000);
						}
					}
					else if (injectionTypeSpecifics instanceof FldInjectionSpecifics &&  ((FldInjectionSpecifics) injectionTypeSpecifics).isSubmitSingleRequestJob() && SwiftBuildFileUtils.hasFLDLastCommand("USER_EXIT CHOICE OK", logContent) )
					{
						if (actionManagerInstance != null) {
							actionManagerInstance.setOutput("Oracle Forms has closed.");
							if (injector != null) {
								IterationLogWriter.appendInjectionLogToLogFile(iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_COMPLETE, new Date(), "");
							}								
						}
						

						return;
					}
				}
			}
		}
		
	}	
	
	/*
	 * 	Algorithm:
	 * 	- retrieve the FLD log file
	 * 	- IF "MENU MAGIC" and "USER EXIT CHOICE" are the LAST 2 lines in the log content
	 * 	  THEN
	 * 			IF there is a Form Window open
	 * 			THEN
	 * 				recheck Form window status until TIMEOUT (forms hang) or success
	 *  		ELSE
	 *  			success, return to continue the next script
	 * 	  ELSE
	 * 			IF there is a Form Window open
	 * 			THEN
	 * 				script still executing, cycle back.
	 * 			ELSE IF this is "Submit request" script and "USER EXIT CHOICE" is the LAST line in the log content
	 * 			THEN
	 * 				success
	 * 			ELSE IF log size has just changed or it is still within time tolerance limit
	 * 			THEN
	 * 				script still executing, cycle back.
	 *  		ELSE
	 *  			IE crashed.
	 */
	public static void waitForScriptToComplete(
			final ActionManager actionManager,
			final int maxIterationFormsToClose,
			final Injector injector,
			final RobotPasteAction robotPasteAction,
			final InjectionTypeSpecifics injectionTypeSpecifics,
			final BufferedReader bufferedReaderForTracking) throws Exception
	{	
		final Semaphore semaphore = new Semaphore(1);
		semaphore.acquireUninterruptibly();
		WaitForScriptToCompleteRunnable waitForScriptToCompleteRunnable = new WaitForScriptToCompleteRunnable(
				semaphore,
				actionManager,
				maxIterationFormsToClose,
				injector,
				robotPasteAction,
				injectionTypeSpecifics,
				bufferedReaderForTracking
				);
		Thread thread = new Thread(waitForScriptToCompleteRunnable);
		thread.start();
		semaphore.acquireUninterruptibly();
		final Throwable throwableToBePropagated = waitForScriptToCompleteRunnable.getThrowableToBeHandledByTheMainThread();
		semaphore.release();
		if (throwableToBePropagated != null) {
			if (throwableToBePropagated instanceof Exception) {
				throw (Exception) throwableToBePropagated;
			} else if (throwableToBePropagated instanceof Error) {
				throw (Error) throwableToBePropagated;
			} else {
				throw new Error(throwableToBePropagated);
			}
		}
	}	
	
	public static void waitForValidationScriptToComplete(
			ActionManager actionManager,
			SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel,
			String scriptName,
			String hostName,
			String hostUserName,
			String hostPassword,
			String privateKeyFileName,
			SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod,
			String fldLogFolder,
			int maxIterationFormsToClose) throws Exception
	{
		int counter = 0;
		while ( true )
		{
			counter++;
			String msg = "[" + counter + " / " + maxIterationFormsToClose + " retries].";
			
			boolean isManualStop=false;
			if (actionManager!=null) {
				isManualStop= checkIfManualStop(actionManager, null);
			}
			else {
				isManualStop= checkIfManualStop(null, swiftBuildPropertiesValidationPanel);
			}
			if ( isManualStop)
			{
				CurrentBrowserTask.eliminateTask();
				throw new ManualStopException();
			}
			if ( counter > maxIterationFormsToClose )
			{
				throw new Exception("Timeout waiting for Oracle Forms to close. " + msg);
			}

			displayFormsPlaybackMessage(swiftBuildPropertiesValidationPanel, "Waiting for Oracle Forms to close... Downloading script log file... " + msg);
			File logFile = getFLDLogFile(hostName, hostUserName, hostPassword, privateKeyFileName, connectionMethod, fldLogFolder, scriptName, null);
			String logContent = null;
			if ( logFile != null )
            {
                logContent = org.apache.commons.io.FileUtils.readFileToString(logFile, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
            }

			if ( SwiftBuildFileUtils.isEndOfScript(logContent) )
			{
				if ( hasOracleFormsProcess() )
				{
					return;
				}
				else
				{
					displayFormsPlaybackMessage(swiftBuildPropertiesValidationPanel, "Oracle Forms has closed. " + msg);
					return;
				}
			}
			else
			{
				if ( hasOracleFormsProcess() )
				{
					displayFormsPlaybackMessage(swiftBuildPropertiesValidationPanel,
							"Waiting for Oracle Forms to close... Script log file downloaded (script still in progress) " + msg);
					com.rapidesuite.client.common.util.Utils.sleep(1000);
				}
			}
		}
	}	
	
	private static boolean checkIfManualStop(ActionManager actionManager, SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel)
	{
		if ( swiftBuildPropertiesValidationPanel != null && !swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().isValidationStarted() )
		{
			return true;
		}
		
		if ( actionManager != null && actionManager.isExecutionStopped() )
		{
			return true;
		}
		return false;
	}	
	
	private static void onOracleFormsCrash(ActionManager actionManager)
	{
		if (Config.getBuildContinueOnFailedInjector()) {
			//this message will only be displayed after the last injector
			throw new SkipInjectionException(new Exception("The script did not complete successfully."));
		} else if (actionManager != null && !actionManager.isExecutionStopped() ) {
			actionManager.getLogCopier().copy(true);
			String message = "Warning, the script did not complete successfully.\nDo you wish to continue? If not, the FLD log file will be opened.";
			int reply = JOptionPane.showConfirmDialog(actionManager.getBuildMain().getRootFrame(), message, "Warning", JOptionPane.YES_NO_OPTION);
			if ( reply == JOptionPane.NO_OPTION )
			{
				actionManager.getInjectorsPackageExecutionPanel().stopExecution();
				actionManager.stopExecution();
				actionManager.getInjectorsPackageExecutionPanel().viewCurrentInjectorLog();
			}
		}
	}		
	
	private static class LogTrackingVariables {
		private Integer currentIterationIndex = null;
		private StringBuffer currentNonIterationLog = null;
		private Integer lastLogIndex = null;
		private boolean firstTimeEncounteringNonIterationLog = true;
		private StringBuffer currentIterationLog = null;
		private Set<Integer> completedIterations = new HashSet<Integer>();
		private Date currentIterationStartingTime = null;

		public Integer getCurrentIterationIndex() {
			return currentIterationIndex;
		}

		public void setCurrentIterationIndex(Integer currentIterationIndex) {
			this.currentIterationIndex = currentIterationIndex;
		}

		public StringBuffer getCurrentNonIterationLog() {
			return currentNonIterationLog;
		}

		public void setCurrentNonIterationLog(StringBuffer currentNonIterationLog) {
			this.currentNonIterationLog = currentNonIterationLog;
		}

		public Integer getLastLogIndex() {
			return lastLogIndex;
		}

		public void setLastLogIndex(Integer lastLogIndex) {
			this.lastLogIndex = lastLogIndex;
		}

		public boolean isFirstTimeEncounteringNonIterationLog() {
			return firstTimeEncounteringNonIterationLog;
		}

		public void setFirstTimeEncounteringNonIterationLog(
				boolean firstTimeEncounteringNonIterationLog) {
			this.firstTimeEncounteringNonIterationLog = firstTimeEncounteringNonIterationLog;
		}

		public StringBuffer getCurrentIterationLog() {
			return currentIterationLog;
		}

		public void setCurrentIterationLog(StringBuffer currentIterationLog) {
			this.currentIterationLog = currentIterationLog;
		}

		public Set<Integer> getCompletedIterations() {
			return completedIterations;
		}

		public Date getCurrentIterationStartingTime() {
			return currentIterationStartingTime;
		}

		public void setCurrentIterationStartingTime(
				Date currentIterationStartingTime) {
			this.currentIterationStartingTime = currentIterationStartingTime;
		}
	}
	
	private static boolean isFldLineMatching(final String logLine, final String scriptLine) {
		if (logLine.equals(scriptLine)) {
			return true;
		} else {
			final String USER_EXIT_RESPONSE_OK = "USER_EXIT RESPONSE OK ";
			final String VALUE_FNDSCRSP_RESPONSIBILITY_RESPONSIBILITY_KEY = "VALUE FNDSCRSP RESPONSIBILITY RESPONSIBILITY_KEY ";
			
			if (logLine.startsWith(USER_EXIT_RESPONSE_OK) && scriptLine.startsWith(USER_EXIT_RESPONSE_OK)) {
				return true;
			} else if (scriptLine.startsWith("LOV ") && logLine.startsWith(scriptLine)) {
				return true;
			} else if (scriptLine.startsWith("USER_EXIT LOV Responsibilities ") && logLine.startsWith(scriptLine)) {
				return true;
			} else if (scriptLine.startsWith(VALUE_FNDSCRSP_RESPONSIBILITY_RESPONSIBILITY_KEY) && logLine.startsWith(VALUE_FNDSCRSP_RESPONSIBILITY_RESPONSIBILITY_KEY)) {
				return scriptLine.equalsIgnoreCase(logLine);
			} else {
				return false;
			}
		}
	}
	
	private static boolean isIgnorableFldLine(String line) {
		line = line.trim();
		if (StringUtils.isBlank(line)) {
			return true;
		} else if (line.startsWith("#")) {
			return true;
		} else if (line.equals("WINDOW FNDSCSGN NAVIGATOR ACTIVATE 1")) {
			return true;
		} else {
			return false;
		}
	}
	
	private static boolean isIterationNumberLine(final String fldScriptLine) {
		return fldScriptLine.trim().startsWith(CoreConstants.ITERATION_SEPARATOR);
	}
	
	private static boolean normalizeLineForMatching(StringBuffer lineBfr) {
		final int lineBfrOriginalLength = lineBfr.toString().length();
		String line = lineBfr.toString();
		line = line.replace("\"\"", "\" \"");
		line = line.replace("\\\"", "'");
		line = line.replace("\"", "").trim();
		boolean continueToTheNextLine = false;
		if (line.endsWith("\\")) {
			line = line.replaceAll("\\\\$", "");
			continueToTheNextLine = true;
		}
		lineBfr.delete(0, lineBfrOriginalLength);
		lineBfr.append(line);
		return continueToTheNextLine;
	}
	

	

	
	private static interface InjectionTypeSpecifics {
		public String getFullCloseMaxIterationErrorMessage(final String msg);
		public String getStartingMessage();
		public String getFullLogDoesNotGrowErrorMessage(final String msg);
		public int getBuildLogSizeMaxConfirmationDelaySeconds();
		public int getBuildMaximumIterationExecutionAttempts();
		public File getLogFile(boolean generateLogForViewing) throws Exception;
		public boolean confirmLogSize(final Long previousFileSize, final int sleepingTime, boolean affectActionManager) throws Exception;
		public File getLogFileForViewing();
	}
	
	public static class FldInjectionSpecifics implements InjectionTypeSpecifics {
		private final String hostName;
		private final String hostUserName;
		private final String hostPassword;
		private final String privateKeyFileName;
		private final SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod;
		private final String fldLogFolder;	
		private final boolean isSubmitSingleRequestJob;
		private final int maxIterationFormsHanged;
		private final String scriptName;
		private final ActionManager actionManager;
		private final CoreConstants.INJECTOR_TYPE injectorType;
		private final File rawLogFile;
		
		public FldInjectionSpecifics(final String hostName, final String hostUserName, final String hostPassword, final String privateKeyFileName,
				final SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod, final String fldLogFolder, final boolean isSubmitSingleRequestJob, final int maxIterationFormsHanged, 
				final String scriptName, final ActionManager actionManager, final CoreConstants.INJECTOR_TYPE injectorType) {
			this.hostName = hostName;
			this.hostUserName = hostUserName;
			this.hostPassword = hostPassword;
			this.privateKeyFileName = privateKeyFileName;
			this.connectionMethod = connectionMethod;
			this.fldLogFolder = fldLogFolder;
			this.isSubmitSingleRequestJob = isSubmitSingleRequestJob;
			this.maxIterationFormsHanged = maxIterationFormsHanged;
			this.scriptName = scriptName;
			this.actionManager = actionManager;
			this.injectorType = injectorType;
			this.rawLogFile = new File(Config.getTempFolder(), this.scriptName+'.'+SwiftBuildConstants.INJECTOR_LOG_FILE_EXTENSION);
		}

		public String getHostUserName() {
			return hostUserName;
		}

		public String getHostName() {
			return hostName;
		}

		public String getHostPassword() {
			return hostPassword;
		}

		public String getPrivateKeyFileName() {
			return privateKeyFileName;
		}

		public SwiftBuildConstants.TRANSFER_PROTOCOL getConnectionMethod() {
			return connectionMethod;
		}

		public String getFldLogFolder() {
			return fldLogFolder;
		}

		public boolean isSubmitSingleRequestJob() {
			return isSubmitSingleRequestJob;
		}

		@Override
		public String getFullCloseMaxIterationErrorMessage(String msg) {
			return "Timeout waiting for Oracle Forms to close. " + msg;
		}

		@Override
		public String getStartingMessage() {
			return "Waiting for Oracle Forms to close...";
		}

		@Override
		public String getFullLogDoesNotGrowErrorMessage(String msg) {
			return "Oracle Forms has crashed. Script not completed. " + msg;
		}

		public int getMaxIterationFormsHanged() {
			return maxIterationFormsHanged;
		}

		@Override
		public int getBuildLogSizeMaxConfirmationDelaySeconds() {
			return Config.getBuildFldLogSizeMaxConfirmationDelaySeconds();
		}

		@Override
		public int getBuildMaximumIterationExecutionAttempts() {
			return Config.getBuildFldMaximumIterationExecutionAttempts();
		}

		@Override
		public File getLogFile(boolean generateLogForViewing) throws Exception {
				if (this.actionManager != null 
						&& this.actionManager.getInjectorsPackageExecutionPanel() != null 
						&& this.actionManager.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement() != null) {
					this.actionManager.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().markStart();
				}
				getFLDLogFile(hostName, hostUserName, hostPassword, privateKeyFileName, connectionMethod, fldLogFolder, scriptName, rawLogFile);
				if (rawLogFile.isFile()) {
					if (generateLogForViewing) {
						try {
							SwiftBuildFileUtils.createTempFileForViewer(rawLogFile, injectorType, getLogFileForViewing());
						} catch (Exception e) {
							FileUtils.printStackTrace(e);
						}					
					}
					this.actionManager.getLogCopier().addRawLogFileToBeCopied(rawLogFile);
					return rawLogFile;
				} else {
					return null;
				}				

		}

		@Override
		public boolean confirmLogSize(final Long previousFileSize, final int sleepingTime, boolean affectActionManager) throws Exception {
			return confirmFldLogSize(previousFileSize,
			        hostName,
			        hostUserName,
			        hostPassword,
			        privateKeyFileName,
			        connectionMethod,
			        fldLogFolder,
			        scriptName,
			        affectActionManager ? actionManager : null,
			        sleepingTime);

		}

		public ActionManager getActionManager() {
			return actionManager;
		}

		@Override
		public File getLogFileForViewing() {
			return FLDAction.getLogFileForViewing(this.actionManager.getBuildMain(), scriptName);
		}
	}
	
	public static class IlInjectionSpecifics implements InjectionTypeSpecifics {
		private final File completionFile;
		private final String partition;
		private final BuildMain buildMain;
		private final String scriptName;
		private final ActionManager actionManager;

		public IlInjectionSpecifics(final File completionFile, final String partition, final BuildMain buildMain, final String scriptName, final ActionManager actionManager) {
			this.completionFile = completionFile;
			this.partition = partition;
			this.buildMain = buildMain;
			this.scriptName = scriptName;
			this.actionManager = actionManager;

		}	

		public File getCompletionFile() {
			return completionFile;
		}

		@Override
		public String getFullCloseMaxIterationErrorMessage(String msg) {
			return "Timeout waiting for IL injection to complete. " + msg;
		}

		@Override
		public String getStartingMessage() {
			return "Waiting for IL injection to complete...";
		}

		@Override
		public String getFullLogDoesNotGrowErrorMessage(String msg) {
			return "Firefox has stopped injecting. Script not completed. " + msg;
		}

		@Override
		public int getBuildLogSizeMaxConfirmationDelaySeconds() {
			return Config.getBuildHtmlLogSizeMaxConfirmationDelaySeconds();
		}

		@Override
		public int getBuildMaximumIterationExecutionAttempts() {
			return Config.getBuildHtmlMaximumIterationExecutionAttempts();
		}

		@Override
		public File getLogFile(boolean generateLogForViewing) throws Exception {
			//the raw log is already fit for viewing (no redaction is needed and it is already in the log directory)
			if (this.actionManager != null 
					&& this.actionManager.getInjectorsPackageExecutionPanel() != null 
					&& this.actionManager.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement() != null) {
				this.actionManager.getInjectorsPackageExecutionPanel().getIterationRuntimeMeasurement().markStart();
			}			
			File logFile = SwiftBuildFileUtils.getLogFile(buildMain, partition);
			if (logFile != null && !logFile.isFile()) {
				logFile = null;
			}
			
			if (logFile != null && logFile.isFile()) {
				Assert.isTrue(
						SwiftBuildFileUtils.getLogFolder(buildMain).getAbsoluteFile().equals(logFile.getAbsoluteFile().getParentFile()), 
						"IL injector raw log ('"+logFile.getAbsolutePath()+"') is not in the expected log directory ('"+SwiftBuildFileUtils.getLogFolder(buildMain).getAbsolutePath()+"')");				
			}
			
			return logFile;
		}

		public String getPartition() {
			return partition;
		}

		@Override
		public boolean confirmLogSize(final Long previousFileSize, final int sleepingTime, boolean affectActionManager) throws Exception {
			return confirmIlLogSize(getLogFile(false), scriptName, affectActionManager ? actionManager : null, sleepingTime, previousFileSize);
		}
		
		public final ActionManager getActionManager() {
			return actionManager;
		}

		@Override
		public File getLogFileForViewing() {
			return SwiftBuildFileUtils.getLogFile(actionManager.getBuildMain(), partition);
		}		

	}	
}