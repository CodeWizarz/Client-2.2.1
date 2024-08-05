/**************************************************************
 * $Revision: 55792 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-06-08 17:58:57 +0700 (Wed, 08 Jun 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/controller/ActionManager.java $:
 * $Id: ActionManager.java 55792 2016-06-08 10:58:57Z jannarong.wadthong $:
 **************************************************************/
package com.rapidesuite.build.core.controller;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipFile;

import javax.crypto.CipherInputStream;
import javax.swing.JFrame;
import javax.swing.event.EventListenerList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.LogMismatchesException;
import com.rapidesuite.build.core.ManualStopException;
import com.rapidesuite.build.core.action.APIAction;
import com.rapidesuite.build.core.action.ActionInterface;
import com.rapidesuite.build.core.action.FLDAction;
import com.rapidesuite.build.core.action.RobotPasteAction;
import com.rapidesuite.build.core.action.SQLAction;
import com.rapidesuite.build.core.action.SSHAction;
import com.rapidesuite.build.core.action.StaticHTMLAction;
import com.rapidesuite.build.core.action.TelnetAction;
import com.rapidesuite.build.core.action.TimerAction;
import com.rapidesuite.build.core.exception.FailedInjectionException;
import com.rapidesuite.build.core.exception.SkipInjectionException;
import com.rapidesuite.build.core.htmlplayback.HTMLRunner;
import com.rapidesuite.build.gui.panels.EnableDiagnosticsPanel;
import com.rapidesuite.build.gui.panels.EnableDiagnosticsPanel.ECB_STATUS;
import com.rapidesuite.build.gui.panels.EnableDiagnosticsPanel.USER_STATUS;
import com.rapidesuite.build.gui.panels.InjectorsPackageExecutionPanel;
import com.rapidesuite.build.utils.CurrentBrowserTask;
import com.rapidesuite.build.utils.InjectorsPackageUtils;
import com.rapidesuite.build.utils.IterationLogWriter;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.EbsServerUrlAndBwpCombination;
import com.rapidesuite.client.common.ForceToGoToTheNextIterationException;
import com.rapidesuite.client.common.RepeatIterationsException;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.IterationLogger;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;

public class ActionManager extends Thread implements ActionCompleteEventListener
{
	private InjectorsPackageExecutionPanel injectorsPackageExecutionPanel;
	protected EventListenerList listenerList;

	private volatile boolean EXECUTION_STOPPED;  //written/read by competing threads; ensure latest value is always read by using 'volatile'
	private boolean CURRENT_INJECTOR_COMPLETED;
	private List<Injector> injectorsToExecute;
	private boolean hasReplacementTokens;
	private Properties replacementTokens;
	private Map<String, String> environmentProperties;
	private int currentInjectorIndex;
	private final Map<Integer, List<String>> injectorIndexToPartitionNameMap;
	private Set<Integer> executedIterations;
	private Integer effectiveNumberOfIterationsForThisInjector = null;
	private File iterationLogDirectory = null;
	private File sceenshotTiketDirectory;
	private EbsServerUrlAndBwpCombination bweBwpCombination = null;
	private Long historyId = null;
	private Integer numberOfIterationExecutionAttemptRemaining = null;
	private Integer currentIterationNumber = null;
	private Integer lastExecutedIteration = null;
	private Integer iterationStartNumber = null;
	private boolean firstIntendedIterationAtCurrentPartitionHasBeenStarted = false;
	private Map<Injector, Set<Integer>> failedIterations;
	private Map<Injector, Set<Integer>> successfulIterations;
	private Integer maxIterationNumber = null;
	private boolean injectorStartHasBeenRecorded = false;
	private JFrame enableUsersForInjectionWindowReference=null;
	private boolean stoppedManuallyByUser = false;
	
	public abstract class LogCopier {
		protected Set<File> rawLogFilesToBeCopied = new HashSet<File>();
		
		public abstract void copy(boolean clearRawLogFiles);
		
		public void addRawLogFileToBeCopied(final File f) {
			this.rawLogFilesToBeCopied.add(f);
		}
	}

	private LogCopier logCopier;	

	public ActionManager(InjectorsPackageExecutionPanel injectorsPackageExecutionPanel)
	{
		this.injectorsPackageExecutionPanel = injectorsPackageExecutionPanel;
		listenerList = new EventListenerList();
		environmentProperties = getBuildMain().getEnvironmentProperties();
		executedIterations = new HashSet<Integer>();
		failedIterations = new HashMap<Injector, Set<Integer>>();
		successfulIterations = new HashMap<Injector, Set<Integer>>();
		injectorIndexToPartitionNameMap = new HashMap<Integer, List<String>>();
		logCopier = this.getDefaultLogCopier();
	}

	public int getCurrentInjectorIndex()
	{
		return currentInjectorIndex;
	}

	public BuildMain getBuildMain()
	{
		return injectorsPackageExecutionPanel.getBuildMain();
	}

	public File getInjectorsPackageFile()
	{
		return getBuildMain().getInjectorsPackageSelectionPanel().getInjectorsPackageFile();
	}

	public List<Injector> getInjectorsToExecute()
	{
		return injectorsToExecute;
	}

	public InjectorsPackageExecutionPanel getInjectorsPackageExecutionPanel()
	{
		return injectorsPackageExecutionPanel;
	}

	public void setOutput(String output)
	{
		injectorsPackageExecutionPanel.setExecutionLog(output);
	}

	public void updateStatus(String status)
	{
		injectorsPackageExecutionPanel.updateStatus(currentInjectorIndex, status);
	}

	public String getOutput()
	{
		String message = injectorsPackageExecutionPanel.getExecutionLog();
		return message;
	}

	private int currentInjectorIterationCount = -1;

	public int getCurrentInjectorIterationCount()
	{
		return this.currentInjectorIterationCount;
	}

	public void run()
	{
		long totalExecutionStartTime = System.currentTimeMillis();
		long totalExecutionEndTime = 0;
		long injectorExecutionStartTime = 0;
		long injectorExecutionEndTime = 0;
		ActionInterface action = null;
		boolean allInjectionsSucceed = true;
		boolean isManuallyStopped = false;
		boolean hasInjector = true;
		try
		{
			injectorsToExecute = injectorsPackageExecutionPanel.getSelectedInjectors();
			if ( injectorsToExecute == null || injectorsToExecute.isEmpty() )
			{
				String errorMessage = "There are no injectors to execute, please select at least one injector.";
				injectorsPackageExecutionPanel.setExecutionLog(errorMessage);
				injectorsPackageExecutionPanel.stopExecution();
				hasInjector = false;
				return;
			}
			EXECUTION_STOPPED = false;
			hasReplacementTokens = SwiftBuildFileUtils.hasReplacementTokens(environmentProperties);
			if ( hasReplacementTokens )
			{
				replacementTokens = SwiftBuildFileUtils.getReplacementTokens(environmentProperties);
			}
			int injectorsCounter = 0;
			Set<Injector> attemptedInjectors = new HashSet<Injector>();
			bweBwpCombination = new EbsServerUrlAndBwpCombination(environmentProperties.get("FLD_SCRIPTS_URL"), getInjectorsPackageFile());

			synchronized (ActionManager.class) {
				final Map<String, String> bwpSpecs = InjectorsPackageUtils.getSpecificationProperties(bweBwpCombination.getBwp());
				final String specGenerationIdStr = bwpSpecs.get(CoreConstants.SPECIFICATION_GENERATION_ID);
				if (StringUtils.isBlank(specGenerationIdStr)) {
					this.historyId = null;
				} else {
					try {
						this.historyId = Long.parseLong(specGenerationIdStr);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException(CoreConstants.SPECIFICATION_GENERATION_ID+" must be a number");
					}
				}

				iterationLogDirectory = getBuildMain().getIterationLogDirectory(bweBwpCombination, historyId);

			}

			Integer iterationIndexToGo = null;
			Injector lastReportedFailedInjectionToRepeat = null;
			Integer lastReportedFailedIterationToRepeat = null;
			Integer previousInjectorIndex = null;

			final Set<Injector> stoppedInjectorsToHaveTheirStatusCleared = new HashSet<Injector>();
			
			for ( int injectorIndex = 0 ; injectorIndex < injectorsToExecute.size() ; injectorIndex++) {
				injectorsPackageExecutionPanel.clearOldStatisticsFromInjectorRowBeforeExecution(injectorsToExecute.get(injectorIndex).getIndex());
				if (SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE.equals(injectorsToExecute.get(injectorIndex).getStatus())) {
					stoppedInjectorsToHaveTheirStatusCleared.add(injectorsToExecute.get(injectorIndex));
				}				
			}

			for ( int injectorIndex = 0 ; injectorIndex < injectorsToExecute.size() ; injectorIndex++)
			{
				this.setLogCopier(this.getDefaultLogCopier());
				
				synchronized(this) {
					setLastExecutedIteration(null);
					iterationStartNumber = null;
					injectorStartHasBeenRecorded = false;
				}

				EXECUTION_STOPPED = false;
				Injector injector = injectorsToExecute.get(injectorIndex);
				if (stoppedInjectorsToHaveTheirStatusCleared.contains(injector)) {
					injectorsPackageExecutionPanel.updateStatus(injector.getIndex(), "");
					injector.setStatus(null);		
					stoppedInjectorsToHaveTheirStatusCleared.remove(injector);
				}				
				CURRENT_INJECTOR_COMPLETED = false;
				currentInjectorIndex = injector.getIndex();
				injectorsPackageExecutionPanel.initiateNewIterationRuntimeMeasurement(injector, currentInjectorIndex, true);

				injectorsPackageExecutionPanel.updateScrollPane(currentInjectorIndex);
				injectorsCounter++;
				attemptedInjectors.add(injector);

				injectorsPackageExecutionPanel.setInjectorsCountValue(attemptedInjectors.size(), injectorsToExecute.size());
				injectorsPackageExecutionPanel.displayCurrentInjectorName(injector.getName());

				boolean toInitSelectedIterations = false;

				if (previousInjectorIndex == null || previousInjectorIndex.intValue() != injectorIndex) {
					injectorExecutionStartTime = System.currentTimeMillis();
					previousInjectorIndex = injectorIndex;
					toInitSelectedIterations = true;
					executedIterations.clear();
				}

				if (!failedIterations.containsKey(injector)) {
					failedIterations.put(injector, new HashSet<Integer>());
				}
				
				if (!successfulIterations.containsKey(injector)) {
					successfulIterations.put(injector, new HashSet<Integer>());
				}
				
				if (SwiftBuildConstants.INJECTOR_EXECUTION_LOG_MISMATCH_VALUE.equals(injector.getStatus())) {
					injector.setStatus(null);
					this.injectorsPackageExecutionPanel.updateStatus(injector.getIndex(), "");
				}

				ZipFile zipFile = null;
				InputStream inputStream = null;
				boolean dontProceedToNextInjector = false;
				try
				{
					currentInjectorIterationCount = -1;
					if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(getInjectorsPackageFile()) )
					{
						inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(getInjectorsPackageFile(), injector.getName());
					}
					else
					{
						zipFile = new ZipFile(getInjectorsPackageFile());
						inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, injector.getName());
					}
					this.injectorsPackageExecutionPanel.setNAIfInjectorHasNoActiveIterationMarker(injector);
					if ( (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) )
					{
						countIterationsInInjector(inputStream);
						this.effectiveNumberOfIterationsForThisInjector = this.currentInjectorIterationCount;
						if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(getInjectorsPackageFile()) )
						{
							inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(getInjectorsPackageFile(), injector.getName());
						}
						else
						{
							inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, injector.getName());
						}

						action = executeFLDAction(injector, inputStream, iterationIndexToGo!=null?iterationIndexToGo:null, toInitSelectedIterations);
					}
					else if ( CoreConstants.INJECTOR_TYPE.TYPE_TELNET.equals(injector.getType()) )
					{
						action = executeTelnetAction(injector, inputStream);
					}
					else if (CoreConstants.INJECTOR_TYPE.TYPE_STATIC_HTML.equals(injector.getType()))
					{
						action = executeStaticHTMLAction(injector, inputStream);
					}
					else if (CoreConstants.INJECTOR_TYPE.TYPE_TIMER.equals(injector.getType()))
					{
						action = executeTimerAction(injector, inputStream);
					} else if ( CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()) ) {
						countIterationsInInjector(inputStream);
						this.effectiveNumberOfIterationsForThisInjector = this.currentInjectorIterationCount;
						if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(getInjectorsPackageFile()) )
						{
							inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(getInjectorsPackageFile(), injector.getName());
						}
						else
						{
							inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, injector.getName());
						}
						action = executeILAction(injector, inputStream, iterationIndexToGo!=null?iterationIndexToGo:null, toInitSelectedIterations);
					}
					else if ( CoreConstants.INJECTOR_TYPE.TYPE_API.equals(injector.getType()) )
					{
						action = executeAPIAction(injector);
					}
					else if (CoreConstants.INJECTOR_TYPE.TYPE_SQL.equals(injector.getType()))
					{
					    action = executeSQLAction(injector, inputStream);
					}
					else
					{
						throw new Exception("Invalid script type: '" + injector.getType() + "'");
					}

					waitUntilInjectorHasCompleted(action);
					verifyPauseExecution();

					if (SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE.equals(injector.getStatus())) {
						throw new ManualStopException();
					}

					numberOfIterationExecutionAttemptRemaining = null;
					iterationIndexToGo = null;
					lastReportedFailedInjectionToRepeat = null;
					lastReportedFailedIterationToRepeat = null;

					if (!failedIterations.get(injector).isEmpty()) {
						if (Config.getBuildContinueOnFailedInjector()) {
							throw new SkipInjectionException(new Exception("Not all attempted iterations have been successfully executed"));
						} else {
							throw new FailedInjectionException("Injection terminated with error");
						}
					}
				}
				catch (SkipInjectionException ex) {
					CurrentBrowserTask.eliminateTask();
					if (injectorIndex >= injectorsToExecute.size()-1) {
						throw ex.getExceptionToThrow();
					} else {
						numberOfIterationExecutionAttemptRemaining = null;
						iterationIndexToGo = null;
						lastReportedFailedInjectionToRepeat = null;
						lastReportedFailedIterationToRepeat = null;
					}
				}
				catch (RepeatIterationsException ex) {
					CurrentBrowserTask.eliminateTask();
					try {
						com.rapidesuite.client.common.util.Utils.sleep(1000*getBuildWaitingTimeBeforeIterationRetrySeconds(injector));
					} catch (InterruptedException e) {
						FileUtils.printStackTrace(e);
					}

					final boolean encounteringFreshNewError = (lastReportedFailedInjectionToRepeat == null || !lastReportedFailedInjectionToRepeat.equals(ex.getInjector())) ||
							(lastReportedFailedIterationToRepeat == null || lastReportedFailedIterationToRepeat.intValue() != ex.getIterationNumber().intValue());

					if (encounteringFreshNewError) {
						numberOfIterationExecutionAttemptRemaining = getBuildMaximumIterationExecutionAttempts(injector)-1;
						lastReportedFailedInjectionToRepeat = ex.getInjector();
						lastReportedFailedIterationToRepeat = ex.getIterationNumber();
					} else {
						if (ex.isForceToGiveOneMoreChance()) {
							if (numberOfIterationExecutionAttemptRemaining == null) {
								numberOfIterationExecutionAttemptRemaining = 1;
							}
						} else {
							numberOfIterationExecutionAttemptRemaining--;
						}
					}

					if (numberOfIterationExecutionAttemptRemaining > 0) {
						injectorsCounter--;
						injectorIndex--;
						dontProceedToNextInjector = true;
						iterationIndexToGo = ex.getIterationNumber();
					} else {
						numberOfIterationExecutionAttemptRemaining = null;
						iterationIndexToGo = null;
						Assert.notNull(ex.getExpectedException());
						Assert.isTrue(!(ex.getExpectedException() instanceof ManualStopException), "Manual stop should not trigger automatic iteration repetition");
						CurrentBrowserTask.eliminateTask();

						final Set<Integer> executableIterations = this.injectorsPackageExecutionPanel.getExecutableIterations();
						final Integer nextSelectedIteration = this.currentIterationNumber==null?null:this.injectorsPackageExecutionPanel.getNextSelectedIteration(this.currentIterationNumber);

						if (!(executableIterations != null && nextSelectedIteration == null) && this.currentIterationNumber != null && this.getMaxIterationNumber() != null && this.currentIterationNumber.intValue() < this.getMaxIterationNumber() && Config.getBuildContinueOnFailedIteration()) {
							iterationIndexToGo = nextSelectedIteration==null?this.currentIterationNumber+1:nextSelectedIteration;
							injectorsCounter--;
							injectorIndex--;
							dontProceedToNextInjector = true;
						} else if (this.currentIterationNumber != null && injectorIndex < injectorsToExecute.size()-1 && Config.getBuildContinueOnFailedInjector()) {
							this.currentIterationNumber = null;
							this.injectorsPackageExecutionPanel.deleteExecutableIterations(false);
						} else {
							this.injectorsPackageExecutionPanel.deleteExecutableIterations(true);
							if (ex.getExpectedException() instanceof FailedInjectionException) {
								Assert.isTrue(Config.getBuildTerminateAfterFailedInjection());
								allInjectionsSucceed=false;
								throw (FailedInjectionException) ex.getExpectedException();
							} else {
								throw ex.getExpectedException();
							}
						}
					}

				}
				catch (ForceToGoToTheNextIterationException ex)
				{
					final Set<Integer> executableIterations = this.injectorsPackageExecutionPanel.getExecutableIterations();
					final Integer nextSelectedIteration = executableIterations==null||ex.getLastExecutedIteration()==null?null:this.injectorsPackageExecutionPanel.getNextSelectedIteration(ex.getLastExecutedIteration());

					if (ex.getLastExecutedIteration() == null || (executableIterations == null && this.getMaxIterationNumber() != null && ex.getLastExecutedIteration().intValue() < this.getMaxIterationNumber()) || (nextSelectedIteration != null)) {
						CurrentBrowserTask.eliminateTask();
						injectorIndex--;
						dontProceedToNextInjector = true;
						injectorsCounter--;

						if (ex.getLastExecutedIteration() == null) {
							if (this.iterationStartNumber == null) {
								iterationIndexToGo = null;
							} else {
								iterationIndexToGo = this.iterationStartNumber;
							}
						} else if (executableIterations != null) {
							iterationIndexToGo = nextSelectedIteration;
						} else {
							iterationIndexToGo = ex.getLastExecutedIteration()+1;
						}

						try {
							com.rapidesuite.client.common.util.Utils.sleep(1000*getBuildWaitingTimeBeforeIterationRetrySeconds(injector));
						} catch (InterruptedException e) {
							FileUtils.printStackTrace(e);
						}
					} else {
						this.stopExecution();
					}
				}
				catch (FailedInjectionException ex)
				{
					allInjectionsSucceed=false;
					throw ex;
				}
				catch (LogMismatchesException ex) {
					this.injectorsPackageExecutionPanel.openLogMismatchesLog(injector);
					throw ex;
				}
				catch ( ManualStopException ex )
				{
					injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE);
					isManuallyStopped = true;
				}
				catch ( Throwable t )
				{
					FileUtils.printStackTrace(t);
					injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE);
					allInjectionsSucceed = false;
					throw t;
				}
				finally
				{
					IOUtils.closeQuietly(inputStream);
					if ( zipFile != null )
					{
						zipFile.close();
					}
					injectorExecutionEndTime = System.currentTimeMillis();
					
					this.logCopier.copy(true);
					this.setLogCopier(this.getDefaultLogCopier());
					
					try {
						updateInjectorsGrid(injector, injectorsCounter, injectorExecutionStartTime, injectorExecutionEndTime, dontProceedToNextInjector);
					} catch (FailedInjectionException ex) {
						allInjectionsSucceed = false;
						throw ex;
					}
				}
				if ( EXECUTION_STOPPED )
				{
					injectorsPackageExecutionPanel.stopExecution();
		    		DatabaseUtils.forciblyTerminateAllActivePreparedStatements();
					allInjectionsSucceed = false;
					break;
				}
			}
			if ( injectorsCounter == injectorsToExecute.size() )
			{
				injectorsPackageExecutionPanel.stopExecution();
			}
		}
		catch ( Throwable e )
		{
			this.stopExecution();
			injectorsPackageExecutionPanel.stopExecution();
			allInjectionsSucceed = false;
			
			FileUtils.printStackTrace(e);
			if (!Config.getBuildTerminateAfterFailedInjection()) {
				e.printStackTrace();
				GUIUtils.popupErrorMessage("Error during execution: " + e.getMessage());
			}
		}
		finally
		{
			totalExecutionEndTime = System.currentTimeMillis();
			setOutput("************** SCENARIO EXECUTION COMPLETED *****************");
			setOutput("Approximate time processing this scenario: " + CoreUtil.getDaysHoursMinutesSecondsFromMilliseconds(totalExecutionStartTime, totalExecutionEndTime));
			this.injectorsPackageExecutionPanel.enableStartButtonAndZipIterationLogsMenu();

			if (!hasInjector) {
				this.injectorsPackageExecutionPanel.focusOnStartButton();
			}

			if (!isManuallyStopped && Config.isAutomatedRun()) {
				if (allInjectionsSucceed || (Config.getBuildTerminateAfterFailedInjection() && !allInjectionsSucceed)) {
					System.exit(0);
				} else if (Config.getBuildTerminateAfterFailedInjection() && !allInjectionsSucceed) {
					System.exit(1);
				}
			}

		}
	}

	private final void waitUntilInjectorHasCompleted(ActionInterface action) throws InterruptedException
	{
		while ( !CURRENT_INJECTOR_COMPLETED )
		{
			com.rapidesuite.client.common.util.Utils.sleep(500);
			if ( EXECUTION_STOPPED )
			{
				if(action != null && action instanceof TimerAction) {
					((TimerAction) action).interrupt();
				}
				else if(action != null && action instanceof SQLAction) {
                    ((SQLAction) action).stop();
                }
                else if ( action != null && action instanceof TelnetAction )
				{
					TelnetAction tempAction = (TelnetAction) action;
					tempAction.manualStop();
				}
				break;
			}
		}
		if ( action != null )
		{
	        action.join();
		}
	}

	private final void verifyPauseExecution()
	{
		if ( !EXECUTION_STOPPED && injectorsPackageExecutionPanel.isPauseBetweenEachInjector() )
		{
			int result = injectorsPackageExecutionPanel.showDialogPausePopup();
			if ( result != 0 )
			{
				injectorsPackageExecutionPanel.stopExecution();
			}
		}
	}

	private void countIterationsInInjector(InputStream inputStream)
	{
		this.currentInjectorIterationCount = -1;
		if ( null != inputStream )
		{
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new InputStreamReader(inputStream, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING));

				String strLine = br.readLine();
				if(strLine !=null){
					this.currentInjectorIterationCount = 0;
					//Read File Line By Line
					while (strLine != null)   {
						String strLineCopy = new String(strLine);
						while(strLineCopy.contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX)) {
							final int firstPrefixIndex = StringUtils.indexOf(strLineCopy, CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX);
							strLineCopy = strLineCopy.substring(firstPrefixIndex+CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX.length());
							final String iterationNumberStr = strLineCopy.substring(0, StringUtils.indexOf(strLineCopy, CoreConstants.RAPIDBUILD_ITERATION_NUMBER_ASTERISKS));
							Assert.isTrue(iterationNumberStr.matches("^\\d+$"), "iterationNumberStr must be a number");
							final int iterationNumber = Integer.parseInt(iterationNumberStr);
							if (this.maxIterationNumber == null || this.maxIterationNumber < iterationNumber) {
								this.maxIterationNumber = iterationNumber;
							}
							this.currentInjectorIterationCount++;
						}


						strLine = br.readLine();
					}
				}
			}
			catch ( Throwable t )
			{
				t.printStackTrace();
			}
			finally{
				IOUtils.closeQuietly(br);
				IOUtils.closeQuietly(inputStream);
			}
		}
	}

	private final void updateInjectorsGrid(Injector injector, int injectorsCounter, long injectorExecutionStartTime, long injectorExecutionEndTime, final boolean dontProceedToNextInjector) throws FailedInjectionException
	{
		if (dontProceedToNextInjector) {
			injector.setStatus(null);
			return;
		}

		String status = injector.getStatus();
		boolean terminate=false;
		if ( status == null || status.isEmpty() )
		{
			if ( EXECUTION_STOPPED )
			{
				status = SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE;
			}
			else
			{
				status = SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE;
				if (((this.numberOfIterationExecutionAttemptRemaining != null && this.numberOfIterationExecutionAttemptRemaining.intValue() == 0)
						|| (!CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) && !CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()) && !CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))
						|| getBuildMaximumIterationExecutionAttempts(injector) == 0) &&
						!(this.currentIterationNumber != null && this.getMaxIterationNumber() != null && this.currentIterationNumber.intValue() < this.getMaxIterationNumber()) &&
						!(this.currentIterationNumber != null && this.getCurrentInjectorIndex() < injectorsToExecute.size()-1 && Config.getBuildContinueOnFailedInjector())) {
					if (Config.getBuildTerminateAfterFailedInjection()) {
						terminate = true;
					}
				}
			}
		}
		else if ( status.startsWith(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE) )
		{
			injectorsPackageExecutionPanel.deselectInjector(injector, injectorsCounter, injectorsToExecute.size());
		}
		else if ( status.startsWith(SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE) )
		{
			setOutput("Error while processing injector: " + injector.getName());

			if (((this.numberOfIterationExecutionAttemptRemaining != null && this.numberOfIterationExecutionAttemptRemaining.intValue() == 0)
					|| (!CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) && !CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()) && !CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))
					|| getBuildMaximumIterationExecutionAttempts(injector) == 0) &&
					!(this.currentIterationNumber != null && this.getMaxIterationNumber() != null && this.currentIterationNumber.intValue() < this.getMaxIterationNumber()) &&
					!(this.currentIterationNumber != null && this.getCurrentInjectorIndex() < injectorsToExecute.size()-1 && Config.getBuildContinueOnFailedInjector())) {
				if (Config.getBuildTerminateAfterFailedInjection()) {
					terminate = true;
				} else {
					this.stopExecution();
				}
			}
		}

		injectorsPackageExecutionPanel.updateStatus(currentInjectorIndex, status);
		String currentInjectorExecutionTime = CoreUtil.getDaysHoursMinutesSecondsFromMilliseconds(injectorExecutionStartTime, injectorExecutionEndTime);
		setOutput("*******************************");
		setOutput("Injector: '" + injector.getName() + "' completed in " + currentInjectorExecutionTime);
		setOutput("*******************************");
		injectorsPackageExecutionPanel.updateExecutionTime(currentInjectorIndex, currentInjectorExecutionTime);

		if (terminate) {
			throw new FailedInjectionException();
		}
	}

	public synchronized void stopExecution()
	{
		if ( !EXECUTION_STOPPED )
		{
			EXECUTION_STOPPED = true;
			Utils.awakenAllSleepers();
			this.setOutput("Stopping execution. Please wait while process cleanup occurs...");
		}
	}

	public synchronized boolean isExecutionStopped()
	{
		return EXECUTION_STOPPED;
	}

	public void registerListener(ActionCompleteEventListener listener)
	{
		listenerList.add(ActionCompleteEventListener.class, listener);
	}

	public void fireEvent(ActionCompleteEvent evt)
	{
		Object[] listeners = listenerList.getListenerList();
		// Each listener occupies two elements - the first is the listener class
		// and the second is the listener instance
		for ( int i = 0; i < listeners.length; i += 2 )
		{
			if ( listeners[i] == ActionCompleteEventListener.class )
			{
				((ActionCompleteEventListener) listeners[i + 1]).actionCompleted(evt);
			}
		}
	}

	public void actionCompleted(ActionCompleteEvent evt)
	{
		CURRENT_INJECTOR_COMPLETED = true;
	}

	private ActionInterface executeFLDAction(Injector injector, InputStream inputStream, Integer requestedStartingIterationNumber, final boolean toInitSelectedIterations) throws Exception
	{
		ActionInterface output = executePlayBackInjectionAction(injector, inputStream, requestedStartingIterationNumber, true, toInitSelectedIterations);
		if (injector.getStatus() == null && (!getFailedIterations().containsKey(injector) || getFailedIterations().get(injector).isEmpty())) {
			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE);
		} else if (injector.getStatus() == null) {
			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_PARTIAL_COMPLETE_VALUE);
		}
		return output;			
	}

	public ActionInterface executeILAction(Injector injector, InputStream inputStream, Integer requestedStartingIterationNumber, final boolean toInitSelectedIterations) throws Exception
	{
		ActionInterface output = executePlayBackInjectionAction(injector, inputStream, requestedStartingIterationNumber, false, toInitSelectedIterations);
		if (injector.getStatus() == null) {
			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE);
		}
		return output;
	}


	private ActionInterface executePlayBackInjectionAction(Injector injector, InputStream inputStream, Integer requestedStartingIterationNumber, final boolean isFldInjection, final boolean toInitSelectedIterations) throws Exception
	{
		boolean isEmptyScript = SwiftBuildFileUtils.isEmptyInjector(injector.getName(), getInjectorsPackageFile());
		if ( isEmptyScript )
		{
			CURRENT_INJECTOR_COMPLETED = true;
			setOutput("Empty injector: " + injector.getName());
			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE);
			final Date now = new Date();
			IterationLogWriter.appendInjectionLogToLogFile(this.iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_START, now, "");
			IterationLogWriter.appendInjectionLogToLogFile(this.iterationLogDirectory, injector.getNumberFromUserPerspective(), injector.getNameWithoutExtension(), -1, IterationLogger.ITERATION_STATUS.INJECTOR_COMPLETE, now, "");
			return null;
		}
		boolean isSubmitSingleRequestJob = SwiftBuildFileUtils.isSubmitSingleRequestJob(getInjectorsPackageFile(), injector.getName());
		setOutput("Start processing injector: '" + injector.getName() + "'");

		boolean isSplitEnabled = injectorsPackageExecutionPanel.getBuildMain().isSplitEnabled(injector.getType());
		boolean isRestartAtIteration = requestedStartingIterationNumber!=null;
		Integer selectedIterationNumber = requestedStartingIterationNumber;
		boolean isExecutingIterations = injectorsPackageExecutionPanel.isExecutingIterations();
		if (isRestartAtIteration) {
			if (isExecutingIterations) {
				final Set<Integer> executableIterations = injectorsPackageExecutionPanel.getExecutableIterations();
				final Iterator<Integer> it = executableIterations.iterator();
				while(it.hasNext()) {
					final Integer thisIterationNumber = it.next();
					if (thisIterationNumber < selectedIterationNumber) {
						it.remove();
						this.injectorsPackageExecutionPanel.refreshIterationSelectionFrameContent(injector);
					}
				}
			} else {
				this.injectorsPackageExecutionPanel.markAnIterationAndOnwardsForExecution(injector, selectedIterationNumber);
			}
			isExecutingIterations = true;
		}

		if (selectedIterationNumber == null && this.injectorsPackageExecutionPanel.hasIteration(injector.getIndex()) && injectorsToExecute.size() > 1) {
			this.injectorsPackageExecutionPanel.markAnIterationAndOnwardsForExecution(injector, 1);
		}

		if (injectorsPackageExecutionPanel.getExecutableIterations() != null) {
			Set<Integer> selectedIterations = new HashSet<Integer>();
			selectedIterations.addAll(this.injectorsPackageExecutionPanel.getExecutableIterations());
			selectedIterations.addAll(this.failedIterations.get(injector));

			if (toInitSelectedIterations) {
				injectorsPackageExecutionPanel.updateTotalIterationsToExecute(injector.getIndex(), selectedIterations.size());
			}

		} else if (toInitSelectedIterations) {
			injectorsPackageExecutionPanel.updateTotalIterationsToExecute(injector.getIndex(), injectorsPackageExecutionPanel.getNumberOfIterations(injector.getIndex()));
		}

		InputStream inputStreamTemp = inputStream;
		List<CipherInputStream> scriptTexts = new ArrayList<CipherInputStream>();
		List<CipherInputStream> scriptTextsForTracking = new ArrayList<CipherInputStream>();
		Map<Integer,RobotPasteAction> iterationToRobotPasteMap;
		try
		{
    		if (isExecutingIterations) {
    			final Set<Integer> executableIterations = injectorsPackageExecutionPanel.getExecutableIterations();
    			Assert.notNull(executableIterations, "executableIterations must have been set");

    			this.effectiveNumberOfIterationsForThisInjector = executableIterations.size();

    			final List<Integer> executableIterationsSorted = new ArrayList<Integer>(executableIterations);
    			Collections.sort(executableIterationsSorted);
    			final String message = "Executing iterations "+StringUtils.join(executableIterationsSorted, ',');

    			setOutput(message);
    			FileUtils.println(message);

    			inputStreamTemp = InjectorsManager.getInputStreamAtMultipleIterationNumbers(inputStreamTemp, executableIterations, getInjectorsPackageFile().getName(), injector.getNameWithoutExtension(), injector.getIndex());
    		}

    		if ( !isSplitEnabled )
    		{
    			int indexOfFileExtension = injector.getName().lastIndexOf(".");
    			String shortinjectorName = injector.getName().substring(0, indexOfFileExtension);
    			String fileExtension = injector.getName().substring(indexOfFileExtension + 1);
    			String injectorNamePartition = shortinjectorName + "-" + Utils.getUniqueFilenameSuffix() + "." + fileExtension;

    			List<String> partitions = injectorIndexToPartitionNameMap.get(injector.getIndex());
    			if ( partitions == null )
    			{
    				partitions = new ArrayList<String>();
    				injectorIndexToPartitionNameMap.put(injector.getIndex(), partitions);
    			}
    			partitions.add(injectorNamePartition);
    			injectorsPackageExecutionPanel.initiateNewIterationRuntimeMeasurement(injector, currentInjectorIndex, true);
    			return executePlayBackInjection(injector, inputStreamTemp, injectorNamePartition, isSubmitSingleRequestJob, null, null);
    		}

    		int splitThreshold = injectorsPackageExecutionPanel.getBuildMain().getSplitThreshold(injector.getType());
    		iterationToRobotPasteMap = new HashMap<Integer,RobotPasteAction>();
    		Map<String, String> userNamesToPasswordFoundInInjector = new HashMap<String,String>();
            InjectorsManager.splitInjectorToFiles(inputStreamTemp,
                    splitThreshold,
                    injector.getName(),
                    getInjectorsPackageFile().getName(),
                    iterationToRobotPasteMap,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    scriptTexts,
                    scriptTextsForTracking,
                    userNamesToPasswordFoundInInjector);    		

            enableUsersForInjection(userNamesToPasswordFoundInInjector);
            
    		int partitionMaskDigitCount = String.valueOf(scriptTexts.size()).length();


    		DecimalFormat dfPartitionNumber = new DecimalFormat(StringUtils.repeat("0", partitionMaskDigitCount));
    		for ( int i = 0; i < scriptTexts.size(); i++ )
    		{
    			if (EXECUTION_STOPPED) {
    				throw new ManualStopException();
    			}
    			this.setFirstIntendedIterationAtCurrentPartitionHasBeenStarted(false);
    			long scriptSplitStartTime = System.currentTimeMillis();
    			int indexOfFileExtension = injector.getName().lastIndexOf(".");
    			String shortinjectorName = injector.getName().substring(0, indexOfFileExtension);
    			String fileExtension = injector.getName().substring(indexOfFileExtension + 1);
    			String injectorNamePartition = shortinjectorName + "-p" + dfPartitionNumber.format(i+1) + Utils.getUniqueFilenameSuffix() + "." + fileExtension;
                RobotPasteAction robotPasteAction = iterationToRobotPasteMap.get(i);

    			if ( scriptTexts.size() > 1 )
    			{
    				setOutput(injector.getName() + " is split to " + scriptTexts.size() + " partitions.");
    				String msg = "Start processing partition " + (i + 1) + UtilsConstants.FORWARD_SLASH + scriptTexts.size();
    				setOutput(msg);
    				FileUtils.println(msg);
    			}

    			List<String> partitions = injectorIndexToPartitionNameMap.get(injector.getIndex());
    			if ( partitions == null )
    			{
    				partitions = new ArrayList<String>();
    				injectorIndexToPartitionNameMap.put(injector.getIndex(), partitions);
    			}
    			partitions.add(injectorNamePartition);

    			InputStream scriptTextIntputStream = scriptTexts.get(i);
    			injectorsPackageExecutionPanel.initiateNewIterationRuntimeMeasurement(injector, currentInjectorIndex, i==0);  			
    			executePlayBackInjection(injector, scriptTextIntputStream, injectorNamePartition, isSubmitSingleRequestJob, robotPasteAction, scriptTextsForTracking.get(i));

    			long scriptSplitEndTime = System.currentTimeMillis();
    			if ( scriptTexts.size() > 1 )
    			{
    				setOutput("Partition completed in " + CoreUtil.getDaysHoursMinutesSecondsFromMilliseconds(scriptSplitStartTime, scriptSplitEndTime));
    			}
    		}
        }
        finally
        {
            IOUtils.closeQuietly(inputStreamTemp);
            if ( null != scriptTexts )
            {
                for ( CipherInputStream cis : scriptTexts )
                {
                    IOUtils.closeQuietly(cis);
                }
            }
            if ( null != scriptTextsForTracking )
            {
                for ( CipherInputStream cis : scriptTextsForTracking )
                {
                    IOUtils.closeQuietly(cis);
                }
            }
        }

		return null;
	}

	

	private void enableUsersForInjection(Map<String, String> userNamesToPasswordFoundInInjector) throws Exception
    {
		Map<String, String>  userNamesToPasswordCheckStatusOn = new HashMap<String, String>();
        Properties replacementTokens = SwiftBuildFileUtils.getReplacementTokens(this.getBuildMain().getEnvironmentProperties());
        
        Iterator<String> iterator=userNamesToPasswordFoundInInjector.keySet().iterator();
        while (iterator.hasNext() ) {
        	String rawUserName=iterator.next();
        	String rawPassword=userNamesToPasswordFoundInInjector.get(rawUserName);
        	String finalUserName = InjectorsManager.replaceTokens(rawUserName, replacementTokens).toUpperCase();
        	String finalPassword = InjectorsManager.replaceTokens(rawPassword, replacementTokens);
        	FileUtils.println("enableUsersForInjection: rawUserName = " + rawUserName + ", finalUserName = " + finalUserName);
        	//FileUtils.println("enableUsersForInjection: rawPassword = " + rawPassword + ", finalPassword = " + finalPassword);
        	userNamesToPasswordCheckStatusOn.put(finalUserName,finalPassword);
        }

        SortedMap<String, USER_STATUS> userMenuDiagnosticsEnabledMap = EnableDiagnosticsPanel.getUserMenuDiagnosticsEnabledMap(userNamesToPasswordCheckStatusOn, this.getBuildMain());
        final SortedMap<String,String> userNamesPasswordsToEnable = new TreeMap<String,String>();
        Set<String> userNamesSet=new TreeSet<String>();
        for ( Entry<String, USER_STATUS> entry : userMenuDiagnosticsEnabledMap.entrySet() )
        {
            if ( entry.getValue().equals(USER_STATUS.DOES_NOT_EXIST) )
            {
                throw new Error("Error:  Injector wants to login using username: " + entry.getKey() + ", but this user does not exist in the EBS database.");
            }
            else if ( entry.getValue().equals(USER_STATUS.DISABLED) )
            {
                userNamesPasswordsToEnable.put(entry.getKey(), userNamesToPasswordCheckStatusOn.get(entry.getKey()) );
                userNamesSet.add(entry.getKey());
            }
            else
            {
                Assert.isTrue(entry.getValue().equals(USER_STATUS.ENABLED), "Unrecognized status for user: " + entry.getKey());
            }
        }
        
        if ( userNamesPasswordsToEnable.size() == 0 )
        {
            return;
        }
        setOutput("Injector wants to login with user names: " + userNamesSet + ", which have not yet been enabled for injection.  Now enabling...");

        
        final ECB_STATUS[] status = new ECB_STATUS[1];
        final JFrame enableUsersForInjectionWindow = new JFrame();
        enableUsersForInjectionWindowReference=enableUsersForInjectionWindow;
        Runnable r = new Runnable()
        {
            public void run()
            {
                try
                {
                    final EnableDiagnosticsPanel enableDiagnosticsPanel = new EnableDiagnosticsPanel(ActionManager.this.getBuildMain(), userNamesPasswordsToEnable);
                    enableUsersForInjectionWindow.setContentPane(enableDiagnosticsPanel);
                    enableUsersForInjectionWindow.setSize(new Dimension(350, 450));
                    enableUsersForInjectionWindow.requestFocus();
                    enableUsersForInjectionWindow.setVisible(true);
                    enableUsersForInjectionWindow.setLocationRelativeTo(SwiftGUIMain.getInstance().getRootFrame());
                    enableUsersForInjectionWindow.addWindowListener(new WindowListener(){
                        public void windowClosing(WindowEvent e)
                        {
                            //TODO: need to trigger a 'stop enabling' button press
                            synchronized ( status )
                            {
                                if ( status[0] == null || !status[0].equals(ECB_STATUS.SUCCESS) )
                                {
                                    status[0] = ECB_STATUS.ABORT;
                                    status.notifyAll();
                                }
                                enableUsersForInjectionWindow.dispose();
                            }
                        }
                        public void windowOpened(WindowEvent e)
                        {
                        }
                        public void windowClosed(WindowEvent e)
                        {
                        }
                        public void windowIconified(WindowEvent e)
                        {
                        }
                        public void windowDeiconified(WindowEvent e)
                        {
                        }
                        public void windowActivated(WindowEvent e)
                        {
                        }
                        public void windowDeactivated(WindowEvent e)
                        {
                        }
                    });

                    enableDiagnosticsPanel.refreshFormsUserList(true, status);
                }
                catch (Exception e)
                {
                	e.printStackTrace();
                    FileUtils.printStackTrace(e);
                    GUIUtils.popupErrorMessage("Error: " + CoreUtil.getAllThrowableMessagesHTML(e));
                }
            }
        };
        Thread t = new Thread(r);
        t.start();

        while ( true )
        {
        	if (isExecutionStopped()) {
				break;
			}
            synchronized ( status )
            {
                status.wait();
                if ( null == status[0] )
                {
                    status[0] = ECB_STATUS.ABORT;
                }
                if ( status[0].equals(ECB_STATUS.SUCCESS) ||  status[0].equals(ECB_STATUS.ABORT) )
                {
                    enableUsersForInjectionWindow.dispose();
                    enableUsersForInjectionWindowReference=null;
                    break;
                }
            }
        }
        
        Assert.isTrue(status[0] == ECB_STATUS.SUCCESS, "Failed to enable user names " + userNamesSet + " for injection; cannot proceed with injector.");
    }	
	
	public void disposeEnableDiagnosticsPanel() {
		if (enableUsersForInjectionWindowReference!=null) {
			enableUsersForInjectionWindowReference.dispose();
		}
	}
		
	public Map<Integer, List<String>> getInjectorIndexToPartitionNameMap()
	{
		return injectorIndexToPartitionNameMap;
	}

	private ActionInterface executePlayBackInjection(Injector injector, InputStream inputStream, String injectorNamePartition, boolean isSubmitSingleRequestJob,
	        RobotPasteAction robotPasteAction, InputStream inputStreamForTracking) throws Exception
	{
		InputStream inputStreamTemp = inputStream;
		BufferedReader bufferedReaderForTracking = null;
		try {
			if ( hasReplacementTokens )
			{
				inputStreamTemp = InjectorsManager.replaceTokensInScript(inputStreamTemp, replacementTokens, injectorNamePartition);
				if (inputStreamForTracking != null) {
					inputStreamForTracking = InjectorsManager.replaceTokensInScript(inputStreamForTracking, replacementTokens, injectorNamePartition);
				}
			}
			if (inputStreamForTracking != null) {
				bufferedReaderForTracking = new BufferedReader(new InputStreamReader(inputStreamForTracking, CoreConstants.CHARACTER_SET_ENCODING));
			}		
			injectorsPackageExecutionPanel.getIterationRuntimeMeasurement().markStart();
			if ((CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())))
			{
				FLDAction fldAction = new FLDAction(injector, injectorNamePartition, inputStreamTemp, environmentProperties, this, isSubmitSingleRequestJob, robotPasteAction, bufferedReaderForTracking);
				fldAction.start();
				return fldAction;
			} else if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType())) {
				HTMLRunner htmlRunner = new HTMLRunner(injector, injectorNamePartition, inputStreamTemp, environmentProperties, this);
				htmlRunner.processILcommands();
				return htmlRunner;
			}
			else {
				throw new Exception("Unsupported playback injection type: '" + injector.getType() + "'");
			}			
		} finally {
			IOUtils.closeQuietly(bufferedReaderForTracking);
			this.logCopier.copy(true);
		}
	}

	private ActionInterface executeTelnetAction(Injector injector, InputStream inputStream) throws Exception
	{
		ActionInterface action = null;
		if ( getBuildMain().isSSHProtocol() )
		{
			action = new SSHAction(injector, inputStream, environmentProperties, this);
			injectorsPackageExecutionPanel.getInjectorIndexToActionMap().put(injector.getIndex(), action);
			((SSHAction) action).start();
		}
		else
		{
			action = new TelnetAction(injector, inputStream, environmentProperties, this);
			((TelnetAction) action).start();
		}
		return action;
	}

	private ActionInterface executeStaticHTMLAction(Injector injector, InputStream inputStream) throws Exception
	{
		StaticHTMLAction staticHTMLAction = new StaticHTMLAction(injector, inputStream, environmentProperties, this);
		staticHTMLAction.start();

		return staticHTMLAction;
	}

	private ActionInterface executeTimerAction(Injector injector, InputStream inputStream) throws Exception
	{
		TimerAction timerAction = new TimerAction(injector, inputStream, environmentProperties, this);
		timerAction.start();
		injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE);

		return timerAction;
	}

	public ActionInterface executeAPIAction(Injector injector) throws Exception
	{
		injectorsPackageExecutionPanel.updateStatus(injector.getIndex(), "In progress...");
		APIAction apiAction = (APIAction) injectorsPackageExecutionPanel.getInjectorIndexToActionMap().get(injector.getIndex());
		if ( apiAction == null )
		{

			apiAction = injectorsPackageExecutionPanel.createNewAPIAction(getBuildMain(), this, injector, environmentProperties, getInjectorsPackageFile());
			injectorsPackageExecutionPanel.getInjectorIndexToActionMap().put(injector.getIndex(), apiAction);
		}
		else
		{
			apiAction.setActionManager(this);
		}
		apiAction.start();

		return apiAction;
	}

	public File getIterationLogDirectory() {
		return iterationLogDirectory;
	}

	public Integer getNumberOfIterationExecutionAttemptRemaining() {
		return numberOfIterationExecutionAttemptRemaining;
	}


    private ActionInterface executeSQLAction(Injector injector, InputStream scriptInputStream) throws Exception
    {
        SQLAction sqlAction = new SQLAction(injector, scriptInputStream, environmentProperties, this, this.getBuildMain().getReplacementsProperties());
        Thread t = new Thread(sqlAction);
        t.start();
        return sqlAction;
    }

	public final synchronized void addToExecutedIterations(Integer arg) {
		Assert.notNull(arg);
		executedIterations.add(arg);
	}

	public final synchronized int getNumberOfExecutedIterations() {
		return executedIterations.size();
	}

	public final synchronized Integer getEffectiveNumberOfIterationsForThisInjector() {
		return effectiveNumberOfIterationsForThisInjector;
	}

	public final synchronized Integer getCurrentIterationNumber() {
		return currentIterationNumber;
	}

	public final synchronized void setCurrentIterationNumber(Integer currentIterationNumber) {
		this.currentIterationNumber = currentIterationNumber;
	}

	public final synchronized Integer getLastExecutedIteration() {
		return lastExecutedIteration;
	}

	public final synchronized void setLastExecutedIteration(Integer lastExecutedIteration) {
		this.lastExecutedIteration = lastExecutedIteration;
	}

	public final Integer getIterationStartNumber() {
		return iterationStartNumber;
	}

	public final void setIterationStartNumber(Integer iterationStartNumber) {
		this.iterationStartNumber = iterationStartNumber;
	}

	public Map<Injector, Set<Integer>> getFailedIterations() {
		return failedIterations;
	}
	
	public Map<Injector, Set<Integer>> getSuccessfulIterations() {
		return this.successfulIterations;
	}

	public void setFirstIntendedIterationAtCurrentPartitionHasBeenStarted(
			boolean firstIntendedIterationAtCurrentPartitionHasBeenStarted) {
		this.firstIntendedIterationAtCurrentPartitionHasBeenStarted = firstIntendedIterationAtCurrentPartitionHasBeenStarted;
	}

	public boolean isJustStartingIteration() {
		return ((this.iterationStartNumber != null) & (!this.firstIntendedIterationAtCurrentPartitionHasBeenStarted));
	}

	public Integer getMaxIterationNumber() {
		return maxIterationNumber;
	}

	public boolean isInjectorStartHasBeenRecorded() {
		return injectorStartHasBeenRecorded;
	}

	public void setInjectorStartHasBeenRecorded(boolean injectorStartHasBeenRecorded) {
		this.injectorStartHasBeenRecorded = injectorStartHasBeenRecorded;
	}

	private int getBuildWaitingTimeBeforeIterationRetrySeconds(final Injector injector) {
		int buildWaitingTimeBeforeIterationRetrySeconds;
		if ((CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()))) {
			buildWaitingTimeBeforeIterationRetrySeconds = Config.getBuildFldWaitingTimeBeforeIterationRetrySeconds();
		} else if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))  {
			buildWaitingTimeBeforeIterationRetrySeconds = Config.getBuildHtmlWaitingTimeBeforeIterationRetrySeconds();
		} else {
			throw new UnsupportedOperationException("Only FLD and IL injector are supported");
		}
		return buildWaitingTimeBeforeIterationRetrySeconds;
	}

	private int getBuildMaximumIterationExecutionAttempts(final Injector injector) {
		int buildMaximumIterationExecutionAttempts;
		if ((CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()))) {
			buildMaximumIterationExecutionAttempts = Config.getBuildFldMaximumIterationExecutionAttempts();
		} else if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType())) {
			buildMaximumIterationExecutionAttempts = Config.getBuildHtmlMaximumIterationExecutionAttempts();
		} else {
			throw new UnsupportedOperationException("Only FLD and IL injector are supported");
		}
		return buildMaximumIterationExecutionAttempts;
	}
	
	public void setLogCopier(final LogCopier logCopier) {
		this.logCopier = logCopier;
	}
	
	public LogCopier getLogCopier() {
		return this.logCopier;
	}
	
	public LogCopier getDefaultLogCopier() {
		return new LogCopier() {
			public void copy(boolean clearRawLogFiles) {
				if (clearRawLogFiles) {
					rawLogFilesToBeCopied.clear();
				}
			}
		};
	}

	public boolean isStoppedManuallyByUser() {
		return stoppedManuallyByUser;
	}

	public void setStoppedManuallyByUser(boolean stoppedManuallyByUser) {
		this.stoppedManuallyByUser = stoppedManuallyByUser;
	}

	public File getSceenshotTiketDirectory() {
		return sceenshotTiketDirectory;
	}

	public void setSceenshotTiketDirectory(File sceenshotTiketDirectory) {
		this.sceenshotTiketDirectory = sceenshotTiketDirectory;
	}

}
