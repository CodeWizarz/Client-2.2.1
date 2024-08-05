package com.rapidesuite.inject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JDialog;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.fusionScripts0000.ScriptType;
import com.rapidesuite.core.injectionPackageInformation.InjectionPackageInformationDocument.InjectionPackageInformation;
import com.rapidesuite.inject.gui.ExecutionRetryPanel;
import com.rapidesuite.inject.gui.RetryDialogWorker;
import com.rapidesuite.inject.gui.ScriptsGrid;
import com.rapidesuite.inject.selenium.HubRequestsManager;
import com.rapidesuite.inject.selenium.NodesInfo;
import com.rapidesuite.inject.selenium.SeleniumWorker;
import com.rapidesuite.inject.webservices.FusionWebServiceWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.UIUtils;
import com.erapidsuite.configurator.navigation0005.BlockType;
import com.erapidsuite.configurator.navigation0005.DependenciesType;
import com.erapidsuite.configurator.navigation0005.EngineType.Enum;
import com.erapidsuite.configurator.navigation0005.FusionNavigationType;
import com.erapidsuite.configurator.navigation0005.Navigation;
import com.erapidsuite.configurator.navigation0005.NavigationDependentType;
import com.erapidsuite.configurator.navigation0005.NavigationDocument;

public class ScriptManager {

	public static int BATCH_ID_RUNNING_NUMBER=1;
	public static int WORKER_ID_RUNNING_NUMBER=1;

	public static boolean IS_CLOSE_BROWSER_ON_SUCCESS=true;
	public static String FUSIONS_ZIP_TEMP_FOLDER="fusion_zips";

	public static String LOGIN_NAVIGATION_FILE_NAME="LOGIN_NAVIGATION";
	public static String TASK_VIA_DIRECT_SEARCH_NAVIGATION_FILE_NAME="TASK_VIA_DIRECT_SEARCH_NAVIGATION";
	public static String TASK_VIA_PROJECT_NAVIGATION_FILE_NAME="TASK_VIA_PROJECT_NAVIGATION";
	public static String INIT_NAVIGATIONS_FILE_NAME_SUFFIX="_NO_URL_REDIRECT";
	public static String INIT_NAVIGATIONS_FILE_EXTENSION=".xml";

	public static String KEY_TASK_NAME="###TASK_NAME###";
	public static String KEY_FUSION_USER_NAME="###FUSION_USER_NAME###";
	public static String KEY_FUSION_PASSWORD="###FUSION_PASSWORD###";
	public static String KEY_IMPLEMENTATION_PROJECT="###IMPLEMENTATION_PROJECT###";

	private InjectMain injectMain;
	
	private final Object lock = new Object();
	
	private int currentNumberOfSeleniumWorkersStarted=0;
	private int currentNumberOfWebServicesWorkersStarted=0;
	
	private List<BlockType> routinesBlockList;
	private List<ScriptGridTracker> scriptGridTrackersToProcessList;
	private List<ScriptGridTracker> scriptGridTrackersRemainingList;
	private boolean isExecutionStopped;
	private boolean isExecutionCompleted;
	
	private Map<Integer,List<BatchInjectionTracker>> scriptIdToBatchInjectionTrackerList;
	private List<Worker> workersList;
	private Map<Integer,BatchInjectionTracker> batchIdToBatchInjectionTrackerMap;
	
	public ScriptManager(InjectMain injectMain,List<ScriptGridTracker> scriptGridTrackersToProcessList) throws Exception{
		this.injectMain=injectMain;
		this.scriptGridTrackersToProcessList=scriptGridTrackersToProcessList;
		batchIdToBatchInjectionTrackerMap=new HashMap<Integer,BatchInjectionTracker>();
		scriptGridTrackersRemainingList=new ArrayList<ScriptGridTracker>();
		for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
			scriptGridTrackersRemainingList.add(scriptGridTracker);
		}
		routinesBlockList=new ArrayList<BlockType>();
	}

	public InjectMain getInjectMain() {
		return injectMain;
	}

	private boolean hasAvailableWorker(ScriptType script) throws Exception {
		com.rapidesuite.core.fusionScripts0000.ScriptType.Type.Enum type=script.getType();
		if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.HTML) {
			if (injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isServerMode()) {
				NodesInfo nodesInfo=HubRequestsManager.getNodesInfo(injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getHubID());
				injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().updateNodesList(nodesInfo);
								
				return !nodesInfo.getFreeNodesList().isEmpty();
			}
			else {
				synchronized (lock) {
					return currentNumberOfSeleniumWorkersStarted<injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getMaxFirefoxWorkers();
				}
			}
		}
		else 
		if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.API) {
			synchronized (lock) {
				return currentNumberOfWebServicesWorkersStarted<injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsWebServicesPanel().getMaxWebServicesWorkers();
			}
		}
		else {
			throw new Exception("Unsupported script type: '"+type+"'");
		}
	}

	private synchronized void reserveWorker(BatchInjectionTracker batchInjectionTracker) {
		ScriptType script=batchInjectionTracker.getScriptGridTracker().getScript();
		com.rapidesuite.core.fusionScripts0000.ScriptType.Type.Enum type=script.getType();
		if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.HTML) {
			if (!injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isServerMode()) {
				synchronized (lock) {
					if (currentNumberOfSeleniumWorkersStarted<injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getMaxFirefoxWorkers()) {
						currentNumberOfSeleniumWorkersStarted++;
					}
				}
			}
		}
		else 
			if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.API) {
				synchronized (lock) {
					if (currentNumberOfWebServicesWorkersStarted<injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsWebServicesPanel().getMaxWebServicesWorkers()) {
						currentNumberOfWebServicesWorkersStarted++;
					}
				}
			}
	}

	public synchronized void unreserveWorker(BatchInjectionTracker batchInjectionTracker) {
		ScriptType script=batchInjectionTracker.getScriptGridTracker().getScript();
		com.rapidesuite.core.fusionScripts0000.ScriptType.Type.Enum type=script.getType();
		if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.HTML) {
			if (!injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isServerMode()) {
				synchronized (lock) {
					if (currentNumberOfSeleniumWorkersStarted>=0) {
						currentNumberOfSeleniumWorkersStarted--;
					}
				}
			}
		}
		else 
			if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.API) {
				synchronized (lock) {
					if (currentNumberOfWebServicesWorkersStarted>=0) {
						currentNumberOfWebServicesWorkersStarted--;
					}
				}
			}
	}

	public void start() throws Exception {	
		final int waitTime=1000;
		scriptIdToBatchInjectionTrackerList=new HashMap<Integer,List<BatchInjectionTracker>> ();
		workersList=new ArrayList<Worker>();
		initRoutines();
		refreshExecutionScriptsGridTrackers();
		
		Thread thread = new Thread(){
			public void run(){
				if (injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsGeneralPanel().isRunInSequence()) {
					executeScriptsInSequence(waitTime);
				}
				else {
					executeScriptsInParallel(waitTime);
				}
			}
		};
		thread.start();	
	}
	
	private void checkFailAllBatchesOnError(ScriptGridTracker scriptGridTracker) throws Exception {
		boolean isFailAllBatchesOnError=injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isFailAllBatchesOnError();
		if (isFailAllBatchesOnError && hasAtLeastOneErrorBatch(scriptGridTracker)) {
			injectMain.getExecutionPanelUI().getExecutionTabPanel().stopExecution();
		}
	}
	
	public void refreshExecutionScriptsGridTrackers() {
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					while ( ! isExecutionCompleted()  ) {
						injectMain.getExecutionPanelUI().getExecutionTabPanel().updateProgressUI();
						refreshScriptGridAndTableGrids();
						Thread.sleep(1000);
					}	
					injectMain.getExecutionPanelUI().getExecutionTabPanel().updateProgressUI();
					refreshScriptGridAndTableGrids();
					injectMain.getExecutionPanelUI().unlockUI();
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}	
			}
		};
		t.start();
	}
	
	private boolean hasAtLeastOneScriptFailed() {	
		for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
			if (scriptGridTracker.getStatus().equals(ScriptsGrid.STATUS_FAILED)) {
				return true;
			}
		}
		return false;
	}	
	
	private void updateAllRemainingScriptsStatusToStopped() {	
		for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
			if (!scriptGridTracker.getStatus().equals(ScriptsGrid.STATUS_SUCCESS) &&
				!scriptGridTracker.getStatus().equals(ScriptsGrid.STATUS_FAILED) &&
				!scriptGridTracker.getStatus().equals(ScriptsGrid.STATUS_STOPPED)) {
				scriptGridTracker.setRemarks("Injection was stopped.");
				scriptGridTracker.setStatus(ScriptsGrid.STATUS_STOPPED);
			}
		}
	}	

	private void executeScriptsInSequence(final int waitTime) {
		try{
			int executionRetries=injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsGeneralPanel().getExecutionRetries();
			for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
				if (isExecutionStopped()) {
					break;
				}
				while ( !scriptGridTracker.isCompleted() ) {
					if (isExecutionStopped()) {
						scriptGridTracker.setRemarks("Injection was stopped.");
						scriptGridTracker.setStatus(ScriptsGrid.STATUS_STOPPED);
						break;
					}
					processStateMachine(scriptGridTracker,true);
					checkFailAllBatchesOnError(scriptGridTracker);
					Thread.sleep(waitTime);
				}
			
				if (hasAtLeastOneErrorBatch(scriptGridTracker) ) {
					if (executionRetries==0 && !isExecutionStopped() && scriptGridTrackersToProcessList.size()>1){
						GUIUtils.popupInformationMessage("The current script has failed! The next script will not be run.");
					}
					break;
				}
			}

			isExecutionCompleted=true;
			boolean hasAtLeastOneScriptFailed=hasAtLeastOneScriptFailed();
			if (hasAtLeastOneScriptFailed && !isExecutionStopped() ) {
				postActionExecutionRetry();
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			isExecutionCompleted=true;
			GUIUtils.popupErrorMessage(e.getMessage());
			injectMain.getExecutionPanelUI().getExecutionTabPanel().stopExecution();
		}
	}
		
	private void executeScriptsInParallel(final int waitTime) {	
		try{
			boolean hasAllScriptsCompleted=false;
			
			while ( !hasAllScriptsCompleted) {
				//System.out.println("### executeScriptsInParallel");
				if (isExecutionStopped()) {
					break;
				}
				
				hasAllScriptsCompleted=true;
				for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
					if (isExecutionStopped()) {
						break;
					}
					if (scriptGridTracker.isCompleted()) {
						/*
						String status=scriptGridTracker.getStatus();
						System.out.println("executeScriptsInParallel, isCompleted scriptGridTracker: "+scriptGridTracker.getScript().getName()+" status:"+status);
						if (status.equals(ScriptsGrid.STATUS_RETRY) || status.equals(ScriptsGrid.STATUS_PENDING) ) {
							scriptGridTracker.setCompleted(false);
							System.out.println("executeScriptsInParallel, scriptGridTracker: "+scriptGridTracker.getScript().getName()+" isRetryOn:true");
						}
						else {
							continue;
						}
						*/
						continue;
					}
					hasAllScriptsCompleted=false;
					//System.out.println("executeScriptsInParallel, scriptGridTracker: "+scriptGridTracker.getScript().getName()+" hasAllScriptsCompleted:false");
					
					processStateMachine(scriptGridTracker,false);
					checkFailAllBatchesOnError(scriptGridTracker);
				}
				Thread.sleep(waitTime);
			}
			isExecutionCompleted=true;
			
			if (isExecutionStopped()) {
				updateAllRemainingScriptsStatusToStopped();
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			isExecutionCompleted=true;
			GUIUtils.popupErrorMessage(e.getMessage());
			injectMain.getExecutionPanelUI().getExecutionTabPanel().stopExecution();
		}
	}
	
	private void processStateMachine(ScriptGridTracker scriptGridTracker,boolean isRunInSequence) throws Exception {
		String status=scriptGridTracker.getStatus();
		//System.out.println("### processStateMachine, scriptGridTracker:"+scriptGridTracker.getScript().getName()+" status:"+status);
		
		if (status.equals(ScriptsGrid.STATUS_RETRY)) {
			scriptGridTracker.setStatus(ScriptsGrid.STATUS_PENDING);
		}
		
		if (status.equals(ScriptsGrid.STATUS_PENDING)) {
			calculateBatchInjectionTrackerList(scriptGridTracker);
			scriptGridTracker.setRemarks("Waiting for available worker");
			scriptGridTracker.setStatus(ScriptsGrid.STATUS_QUEUED);
			status=scriptGridTracker.getStatus();
		}

		if (status.equals(ScriptsGrid.STATUS_QUEUED)) {
			//FileUtils.println("STATUS_QUEUED '"+scriptGridTracker.getScript().getName()+"' started...");
			List<String> scriptDependencies=null;
			
			InjectionPackageInformation injectionPackageInformation  = injectMain.getApplicationInfoPanel().getInjectionPackageInformationDocument().getInjectionPackageInformation();
			// if Running in Sequence, or if it is a single BWP, ignore dependencies.
			if (isRunInSequence || (injectionPackageInformation.getScenarioName().equalsIgnoreCase("N/A") && injectionPackageInformation.getProfileName().equalsIgnoreCase("N/A"))) {
				scriptDependencies=new ArrayList<String>();
			}
			else {
				scriptDependencies=getScriptDependencies(scriptGridTracker.getNavigation());
				//System.out.println("scriptDependencies: "+scriptDependencies);
			}
			if (!scriptDependencies.isEmpty()) {
				ScriptGridTracker firstFailedDependantScriptGridTracker=getFirstFailedDependantScriptGridTracker(scriptDependencies);
				
				if (firstFailedDependantScriptGridTracker!=null) {
					scriptGridTracker.setRemarks("Dependent script '"+firstFailedDependantScriptGridTracker.getScript().getName()+
							"' (# "+(firstFailedDependantScriptGridTracker.getGridIndex()+1)+") failed.");
					scriptGridTracker.setStatus(ScriptsGrid.STATUS_FAILED);
					scriptGridTracker.setCompleted(true);
				}
				else {
					ScriptGridTracker firstDependantScriptGridTracker=getFirstDependantScriptGridTracker(scriptDependencies);
					if (firstDependantScriptGridTracker!=null) {
						String remarks="Waiting for Dependent script '"+firstDependantScriptGridTracker.getScript().getName()+
								"' (# "+(firstDependantScriptGridTracker.getGridIndex()+1)+") to complete.";
						scriptGridTracker.setRemarks(remarks);
					}
					else {
						if ( hasAvailableWorker(scriptGridTracker.getScript()) ){
							if (scriptGridTracker.getStartTime()==0) {
								scriptGridTracker.setStartTime(System.currentTimeMillis());
							}
							scriptGridTracker.setStatus(ScriptsGrid.STATUS_PROCESSING);
						}
						else {
							// do nothing
							scriptGridTracker.setRemarks("Waiting for available worker");
						}
					}
				}
			}
			else {
				if ( hasAvailableWorker(scriptGridTracker.getScript()) ){
					scriptGridTracker.setStartTime(System.currentTimeMillis());
					scriptGridTracker.setStatus(ScriptsGrid.STATUS_PROCESSING);
				}
				else {
					// do nothing
					scriptGridTracker.setRemarks("Waiting for available worker");
				}
			}
			status=scriptGridTracker.getStatus();
			//FileUtils.println("STATUS_QUEUED '"+scriptGridTracker.getScript().getName()+"' completed...");
		}

		if (status.equals(ScriptsGrid.STATUS_PROCESSING)) {
			//FileUtils.println("STATUS_PROCESSING '"+scriptGridTracker.getScript().getName()+"' started...");
			scriptGridTracker.setRemarks("");
			if (hasMorePendingBatches(scriptGridTracker)) {					
				BatchInjectionTracker batchInjectionTracker=getNextBatchInjectionTracker(scriptGridTracker);
				ScriptType script=scriptGridTracker.getScript();
				if ( hasAvailableWorker(script) ){
					updateScriptExecutionStartTime(scriptGridTracker);
					launchWorker(batchInjectionTracker);
				}
				else {
					scriptGridTracker.setRemarks("Waiting for available worker");
				}
			}
			else {
				if (hasAllBatchesCompleted(scriptGridTracker)) {
					if ( !hasAtLeastOneErrorBatch(scriptGridTracker) ) {
						scriptGridTracker.setRemarks("");
						scriptGridTracker.setStatus(ScriptsGrid.STATUS_SUCCESS);
						scriptGridTracker.setCompleted(true);
					}
					else{
						int executionRetries=injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsGeneralPanel().getExecutionRetries();
						int scriptExecutionRetries=scriptGridTracker.getExecutionRetries();
						if (scriptExecutionRetries < executionRetries) {
							scriptGridTracker.setExecutionRetries(scriptExecutionRetries+1);
							scriptGridTracker.setStatus(ScriptsGrid.STATUS_RETRY);
							scriptGridTracker.setRemarks("Retrying...");
							quitWebBrowsers(scriptGridTracker);
							scriptIdToBatchInjectionTrackerList.remove(scriptGridTracker.getScript().getUniqueID().intValue());
							injectMain.getExecutionPanelUI().getExecutionTabPanel().resetExecutionScriptsGridTracker(scriptGridTracker,true);
						}
						else {
							scriptGridTracker.setStatus(ScriptsGrid.STATUS_FAILED);
							scriptGridTracker.setRemarks("Review the errors");
							scriptGridTracker.setCompleted(true);
						}
					}
				}					
			}
			//FileUtils.println("STATUS_PROCESSING '"+scriptGridTracker.getScript().getName()+"' completed...");
		}
	}
	
	private List<String> getScriptDependencies(Navigation navigation) throws Exception {
		Set<String> toReturn=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		addScriptDependenciesRecursive(toReturn,navigation);
		return new ArrayList<String>(toReturn);
	}
	
	private void addScriptDependenciesRecursive(Set<String> fullList,Navigation parentNavigation) throws Exception {
		if(parentNavigation==null) {
			throw new Exception("Internal Error: Navigation is null, please fix the Injection Package (ip).");
		}
		FusionNavigationType fusionNavigation=parentNavigation.getFusionNavigation();
		DependenciesType dependenciesType=fusionNavigation.getDependencies();
		if (dependenciesType!=null) {
			NavigationDependentType[] navigationTypeArray=dependenciesType.getNavigationArray();
			for (NavigationDependentType navigationType:navigationTypeArray) {
				String dependentNavigationName=navigationType.getName();
				if (fullList.contains(dependentNavigationName)) {
					//FileUtils.println("duplicate dependencies detected '" + dependentNavigationName + "' (Parent: '" + parentNavigation.getName() + "'). This will be ignored.");
					break;
				}
				fullList.add(dependentNavigationName);
				
				Map<String,Navigation> navigationNameToNavigationMap=injectMain.getExecutionPanelUI().getExecutionTabPanel().getNavigationNameToNavigationMap();
				Navigation navigation=navigationNameToNavigationMap.get(dependentNavigationName);
				if(navigation==null) {
					throw new Exception(String.format("Navigation(s) '%s' not found." + "\r\n" + 
							"Please review and fix the scenario.", dependentNavigationName));
				}
				addScriptDependenciesRecursive(fullList,navigation);
			}
		}
	}

	protected ScriptGridTracker getFirstFailedDependantScriptGridTracker(List<String> scriptDependencies) {
		for (String navigationName:scriptDependencies) {
			Map<String, ScriptGridTracker> scriptNameToScriptGridTrackerMap=injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptNameToScriptGridTrackerMap();
			ScriptGridTracker scriptGridTrackerTemp=scriptNameToScriptGridTrackerMap.get(navigationName);
			if (scriptGridTrackerTemp!=null) {
				String status=scriptGridTrackerTemp.getStatus();
				if ( status.equals(ScriptsGrid.STATUS_FAILED) ) {
					boolean isScriptSelected=injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptsGrid().isScriptSelected(scriptGridTrackerTemp.getGridIndex());
					if (!isScriptSelected) {
						// We ignore dependencies if those are not selected for injection.
						continue;
					}
					return scriptGridTrackerTemp;
				}
			}
		}
		return null;
	}
	
	protected ScriptGridTracker getFirstDependantScriptGridTracker(List<String> scriptDependencies) {
		for (String navigationName:scriptDependencies) {
			Map<String, ScriptGridTracker> scriptNameToScriptGridTrackerMap=injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptNameToScriptGridTrackerMap();
			ScriptGridTracker scriptGridTrackerTemp=scriptNameToScriptGridTrackerMap.get(navigationName);
			if (scriptGridTrackerTemp!=null) {
				boolean isScriptSelected=injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptsGrid().isScriptSelected(scriptGridTrackerTemp.getGridIndex());
				if (isScriptSelected) {
					String status=scriptGridTrackerTemp.getStatus();
					if ( !status.equals(ScriptsGrid.STATUS_SUCCESS) ) {
						return scriptGridTrackerTemp;
					}
				}
				else {
					// We ignore dependencies if those are not selected for injection.
				}
			}
		}
		return null;
	}
	
	private void postActionExecutionRetry() {
		int executionRetries=injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsGeneralPanel().getExecutionRetries();
		if (executionRetries > 0 ) {
			boolean hasScriptToRetry=false;
			for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
				if (scriptGridTracker.getStatus().equals(ScriptsGrid.STATUS_FAILED)) {
					int scriptExecutionRetries=scriptGridTracker.getExecutionRetries();
					if (scriptExecutionRetries < executionRetries) {
						hasScriptToRetry=true;
						break;
					}
				}
			}

			if (hasScriptToRetry) {
				//show dialog starting in X secs so can still cancel it before start.
				final int width=600;
				final int height=180;
				final ExecutionRetryPanel executionRetryPanel=new ExecutionRetryPanel();
				final RetryDialogWorker retryDialogWorker=new RetryDialogWorker(this,executionRetryPanel);

				Thread thread = new Thread(){
					public void run(){
						JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindow(injectMain.getRootFrame(),"Execution Retry Window",width,height,
								executionRetryPanel,retryDialogWorker,false,InjectMain.getSharedApplicationIconPath());
						((java.awt.Frame)dialog.getOwner()).setIconImage(GUIUtils.getImageIcon(this.getClass(), InjectMain.getSharedApplicationIconPath()).getImage());
						executionRetryPanel.setDialog(dialog);	
						dialog.setVisible(true);
						
						for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
							if (scriptGridTracker.getStatus().equals(ScriptsGrid.STATUS_FAILED)) {
								int scriptExecutionRetries=scriptGridTracker.getExecutionRetries();
								scriptGridTracker.setExecutionRetries(scriptExecutionRetries+1);
							}
						}
					}
				};
				thread.start();
			}
		}
	}
	
	protected BatchInjectionTracker getNextBatchInjectionTracker(ScriptGridTracker scriptGridTracker) throws Exception {
		List<BatchInjectionTracker> batchInjectionTrackerList=scriptIdToBatchInjectionTrackerList.get(scriptGridTracker.getScript().getUniqueID().intValue());
		for (BatchInjectionTracker batchInjectionTracker:batchInjectionTrackerList) {
			if (batchInjectionTracker.isPending()) {
				return batchInjectionTracker;
			}
		}
		throw new Exception("No pending batches to process.");
	}

	protected void calculateBatchInjectionTrackerList(ScriptGridTracker scriptGridTracker) throws Exception {
		//FileUtils.println("calculateBatchInjectionTrackerList...");
		int scriptId=scriptGridTracker.getScript().getUniqueID().intValue();
		List<BatchInjectionTracker> batchInjectionTrackerList=scriptIdToBatchInjectionTrackerList.get(scriptId);
		if (batchInjectionTrackerList!=null) {
			//FileUtils.println("batchInjectionTrackerList, batchInjectionTrackerList NOT EMPTY RETURNING: size: "+batchInjectionTrackerList.size());
			return;
		}
		NavigationDocument fusionCurrentScriptNavigationDocument=InjectUtils.getFusionNavigationDocument(
				injectMain,injectMain.getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());	
		Navigation navigation=fusionCurrentScriptNavigationDocument.getNavigation();	
		Enum engineType=navigation.getEngineType();
		boolean isBatchingAllowed=true;
		int batchSize=1;

		ScriptType script=scriptGridTracker.getScript();
		com.rapidesuite.core.fusionScripts0000.ScriptType.Type.Enum type=script.getType();
		if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.HTML) {
			isBatchingAllowed=navigation.getFusionNavigation().getIsBatchingAllowed();
			batchSize=injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getFirefoxBatchSizeLevel();
			int batchSizeAtScriptLevel=scriptGridTracker.getBatchSize();
			if (batchSizeAtScriptLevel>0) {
				batchSize=batchSizeAtScriptLevel;
			}
		}
		else 
		if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.API) {
			isBatchingAllowed=true;
			batchSize=injectMain.getExecutionPanelUI().getOptionsTabPanel().getOptionsWebServicesPanel().getWebServicesBatchSizeLevel();
		}
		else {
			throw new Exception("Unsupported engine type: '"+engineType+"'");
		}
		
		/*
		 * BUG MANTIS: 6900
		 * Batches running in parallel will screw up the checkbox state (example: unselected with Status SUCCESS but some child records are still checked
		 * and status FAILED).
		 * So, we need to reselect the chekboxes for all children rows selected.
		 */
		injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptsGrid().verifyInconsistenciesInSelectionRows(scriptGridTracker);
		
		boolean isBatchingBasedOnRootInventoryUniqueRecords=navigation.getFusionNavigation().getIsBatchingBasedOnRootInventoryUniqueRecords();
		if (!isBatchingBasedOnRootInventoryUniqueRecords) {
			batchInjectionTrackerList=getBatchInjectionTrackerBasedOnParentChildList(scriptGridTracker,isBatchingAllowed,batchSize);
		}
		else {
			batchInjectionTrackerList=getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList(scriptGridTracker,batchSize);
		}		
		
		scriptGridTracker.setTotalBatchCount(batchInjectionTrackerList.size());
		scriptGridTracker.setCompletedBatchCount(0);
		
		if (batchInjectionTrackerList.isEmpty()) {
			scriptGridTracker.setStartTime(System.currentTimeMillis());
			updateScriptExecutionStartTime(scriptGridTracker);
			scriptGridTracker.setStatus(ScriptsGrid.STATUS_SUCCESS);
		}
		//FileUtils.println("calculateBatchInjectionTrackerList COMPLETED");
	}
	
	private void validateParentChildInventories(ScriptGridTracker scriptGridTracker,Map<String, Inventory> inventoryNameToInventoryMap) throws Exception {
		Iterator<String> invIterator=inventoryNameToInventoryMap.keySet().iterator();
		Set<String> inventoriesSet=inventoryNameToInventoryMap.keySet();
		while ( invIterator.hasNext()) {
			String inventoryName=invIterator.next();
			Inventory inventory=inventoryNameToInventoryMap.get(inventoryName);
			String parentName=inventory.getParentName();
			//System.out.println("inventory: '"+inventory.getName()+"' parent: '"+parentName+"'");
			if (parentName!=null && !parentName.isEmpty()  && !inventoriesSet.contains(parentName)) {
				throw new Exception("Internal error: unable to find the inventory '"+parentName+"' in the script: '"+scriptGridTracker.getScript().getName()+"'");
			}
		}
	}
		
	private List<BatchInjectionTracker> getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList(ScriptGridTracker scriptGridTracker
			,int batchSize) throws Exception {
		Map<String,List<RecordTracker>> inventoryToRecordTrackerMap=scriptGridTracker.getInventoryToRecordTrackerMap();
		List<Map<String, RecordTracker>> dataLinks=InjectUtils.getDataLinks(injectMain,injectMain.getApplicationInfoPanel().getInjectionPackage()
				,scriptGridTracker.getScript(),inventoryToRecordTrackerMap);
		
		/*
		 * Logic: batches are computed based on root inventory unique records
		 * Assumption: all records in the root inventory are unique
		 */
		Map<String, Inventory> inventoryNameToInventoryMap=InjectUtils.getAllInventoryNameToInventoryMap(
				injectMain,injectMain.getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());
		Inventory rootInventory=InjectUtils.getRootInventory(scriptGridTracker,inventoryNameToInventoryMap);
				
		List<BatchInjectionTracker> batchInjectionTrackerList=new ArrayList<BatchInjectionTracker>();
		int scriptId=scriptGridTracker.getScript().getUniqueID().intValue();
		scriptIdToBatchInjectionTrackerList.put(scriptId, batchInjectionTrackerList);
		List<Map<String,RecordTracker>> dataLinksToExecuteList=new ArrayList<Map<String,RecordTracker>>();
		Map<Integer,List<Map<String,RecordTracker>>> rootGridIndexToDataLinksMap=new TreeMap<Integer,List<Map<String,RecordTracker>>>();
		
		int counter=0;
		int batchId=BATCH_ID_RUNNING_NUMBER;
		//FileUtils.println("getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList, scriptGridTracker: '"+scriptGridTracker.getDisplayName()+"' dataLinks: "+dataLinks.size());
		for (Map<String, RecordTracker> dataLink:dataLinks)  {
			RecordTracker recordTracker=dataLink.get(rootInventory.getName());
			if (recordTracker==null) {
				throw new Exception("Invalid package: missing root record in datalink (root inventory: '"+rootInventory.getName()+"' dataLink: "+counter+")");
			}
			counter++;
			//FileUtils.println("getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList, counter: "+counter);
			
			Iterator<String> iterator=dataLink.keySet().iterator();
			iterator=dataLink.keySet().iterator();
			boolean isSelected=true;
			while (iterator.hasNext()) {
				String inventoryName=iterator.next();
				RecordTracker recordTrackerTemp=dataLink.get(inventoryName);
				
				boolean isRecordSelected=injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptsGrid().isRecordSelected(
						recordTrackerTemp,scriptId,inventoryName);
									
				//FileUtils.println("getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList, recordTracker: "+recordTrackerTemp.getGridIndex()+
				//		" inventoryName: '"+inventoryName+"' isRecordSelected:"+isRecordSelected);
				if (!isRecordSelected) {
					isSelected=false;
					break;
				}
			}
			if (!isSelected) {
				//FileUtils.println("getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList, WARNING: NOT SELECTED!");
				continue;
			}
			int gridIndex=recordTracker.getGridIndex();
			List<Map<String, RecordTracker>> dataLinksList=rootGridIndexToDataLinksMap.get(gridIndex);
			if (dataLinksList==null) {
				dataLinksList=new ArrayList<Map<String, RecordTracker>>();
				rootGridIndexToDataLinksMap.put(gridIndex,dataLinksList);
			}
			dataLinksList.add(dataLink);
		}
		
		Iterator<Integer> iterator=rootGridIndexToDataLinksMap.keySet().iterator();
		batchId=BATCH_ID_RUNNING_NUMBER;
		dataLinksToExecuteList=new ArrayList<Map<String,RecordTracker>>();

		int counterBatch=0;
		while (iterator.hasNext()) {
			Integer gridIndex=iterator.next();
			counterBatch++;
			List<Map<String, RecordTracker>> dataLinksList=rootGridIndexToDataLinksMap.get(gridIndex);
			
			for (Map<String, RecordTracker> dataLink:dataLinksList)  {
				Iterator<String> iteratorTemp=dataLink.keySet().iterator();
				while (iteratorTemp.hasNext()) {
					String inventoryName=iteratorTemp.next();
					RecordTracker recordTrackerTemp=dataLink.get(inventoryName);
					recordTrackerTemp.setBatchId(batchId);
				}
				dataLinksToExecuteList.add(dataLink);
			}
			
			if (counterBatch % batchSize ==0) {
				// new batch:
				//FileUtils.println("getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList, NEW BATCH:"+batchId+" dataLinksToExecuteList:"+dataLinksToExecuteList.size());
				BatchInjectionTracker batchInjectionTracker=new BatchInjectionTracker(scriptGridTracker,dataLinksToExecuteList,batchId);
				batchInjectionTrackerList.add(batchInjectionTracker);
				BATCH_ID_RUNNING_NUMBER++;
				batchId=BATCH_ID_RUNNING_NUMBER;
				dataLinksToExecuteList=new ArrayList<Map<String,RecordTracker>>();
			}			
		}
		if (!dataLinksToExecuteList.isEmpty()) {
			// new batch:
			//FileUtils.println("getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList, LAST NEW BATCH:"+batchId+" dataLinksToExecuteList:"+dataLinksToExecuteList.size());
			BatchInjectionTracker batchInjectionTracker=new BatchInjectionTracker(scriptGridTracker,dataLinksToExecuteList,batchId);
			batchInjectionTrackerList.add(batchInjectionTracker);
		}
		
		//FileUtils.println("getBatchInjectionTrackerBasedOnRootInventoryUniqueRecordsList, TOTAL BATCHES: "+batchInjectionTrackerList.size());
		return batchInjectionTrackerList;
	}

	private List<BatchInjectionTracker> getBatchInjectionTrackerBasedOnParentChildList(ScriptGridTracker scriptGridTracker,boolean isBatchingAllowed,int batchSize) throws Exception {
		//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, ###########");
		List<BatchInjectionTracker> batchInjectionTrackerList=new ArrayList<BatchInjectionTracker>();
		int scriptId=scriptGridTracker.getScript().getUniqueID().intValue();
		
		Map<String, Inventory> inventoryNameToInventoryMap=InjectUtils.getAllInventoryNameToInventoryMap(
				injectMain,injectMain.getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());
		
		validateParentChildInventories(scriptGridTracker,inventoryNameToInventoryMap);
		
		Map<String,List<RecordTracker>> inventoryToRecordTrackerMap=scriptGridTracker.getInventoryToRecordTrackerMap();
		List<Map<String, RecordTracker>> dataLinks=InjectUtils.getDataLinks(injectMain,injectMain.getApplicationInfoPanel().getInjectionPackage()
				,scriptGridTracker.getScript(),inventoryToRecordTrackerMap);
		
		batchInjectionTrackerList=new ArrayList<BatchInjectionTracker>();
		scriptIdToBatchInjectionTrackerList.put(scriptId, batchInjectionTrackerList);
		List<Map<String,RecordTracker>> dataLinksToExecuteList=new ArrayList<Map<String,RecordTracker>>();

		int batchSizeCounter=0;
		//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, scriptGridTracker: '"+scriptGridTracker.getDisplayName()+"' dataLinks: "+dataLinks.size()+" batchSize: "+batchSize);
		//int counter=0;
		for (Map<String, RecordTracker> dataLink:dataLinks)  {
			Iterator<String> iterator=dataLink.keySet().iterator();
			//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, dataLink counter: "+counter);
			//counter++;
			boolean isSelected=true;
			while (iterator.hasNext()) {
				String key=iterator.next();
				RecordTracker recordTracker=dataLink.get(key);
				
				boolean isRecordSelected=injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptsGrid().isRecordSelected(
						recordTracker,scriptId,key);
				
				//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, recordTracker: "+recordTracker.getGridIndex()+" key: '"+key+"' isRecordSelected:"+isRecordSelected);
				if (!isRecordSelected) {
					isSelected=false;
					break;
				}
			}
			if (!isSelected) {
				//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, WARNING: NOT SELECTED!");
				continue;
			}
			iterator=dataLink.keySet().iterator();
			while (iterator.hasNext()) {
				String key=iterator.next();
				RecordTracker recordTracker=dataLink.get(key);
				int batchId=recordTracker.getBatchId();
				if (batchId==0) {
					recordTracker.setBatchId(BATCH_ID_RUNNING_NUMBER);
					batchSizeCounter++;
				}
				//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, recordTracker: "+recordTracker.getGridIndex()+" batchId: "+recordTracker.getBatchId());
			}
			
			//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, adding one datalink to execute.");
			dataLinksToExecuteList.add(dataLink);
			if (isBatchingAllowed && batchSizeCounter>=batchSize) {
				batchSizeCounter=0;
				int batchId=BATCH_ID_RUNNING_NUMBER;
				BatchInjectionTracker batchInjectionTracker=new BatchInjectionTracker(scriptGridTracker,dataLinksToExecuteList,batchId);
				batchInjectionTrackerList.add(batchInjectionTracker);
				
				batchIdToBatchInjectionTrackerMap.put(batchId, batchInjectionTracker);
				
				//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, BATCH CREATED. batchId: "+batchId);
				BATCH_ID_RUNNING_NUMBER++;
				dataLinksToExecuteList=new ArrayList<Map<String,RecordTracker>>();
			}
		}
		if (!dataLinksToExecuteList.isEmpty()) {
			int batchId=BATCH_ID_RUNNING_NUMBER;
			BatchInjectionTracker batchInjectionTracker=new BatchInjectionTracker(scriptGridTracker,dataLinksToExecuteList,batchId);
			batchInjectionTrackerList.add(batchInjectionTracker);
			
			batchIdToBatchInjectionTrackerMap.put(batchId, batchInjectionTracker);
			//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, BATCH CREATED. batchId: "+batchId);
		}

		//FileUtils.println("getBatchInjectionTrackerBasedOnParentChildList, TOTAL BATCHES: "+batchInjectionTrackerList.size());
		return batchInjectionTrackerList;
	}
			
	protected void launchWorker(BatchInjectionTracker batchInjectionTracker) throws Exception {
		reserveWorker(batchInjectionTracker);
		
		int workerId=WORKER_ID_RUNNING_NUMBER;
		WORKER_ID_RUNNING_NUMBER++;

		final Worker worker;
		ScriptType script=batchInjectionTracker.getScriptGridTracker().getScript();
		com.rapidesuite.core.fusionScripts0000.ScriptType.Type.Enum type=script.getType();
		if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.HTML) {
			worker=new SeleniumWorker(batchInjectionTracker,workerId);
			batchInjectionTracker.setWorker(worker);
		}
		else 
		if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.API) {
			worker=new FusionWebServiceWorker(batchInjectionTracker,workerId);
			batchInjectionTracker.setWorker(worker);
		}
		else {
			throw new Exception("Unsupported script type: '"+type+"'");
		}
				
		List<Map<String, RecordTracker>> inventoryToRecordTrackerList=batchInjectionTracker.getDataLinksToExecuteList();
		for (Map<String, RecordTracker> inventoryToRecordTrackerMap:inventoryToRecordTrackerList) {
			Iterator<String> iterator=inventoryToRecordTrackerMap.keySet().iterator();
			while(iterator.hasNext()){
				String key=iterator.next();
				RecordTracker recordTracker=inventoryToRecordTrackerMap.get(key);
				if (recordTracker.getWorkerId()==0) {
					recordTracker.setWorkerId(workerId);
					recordTracker.setRemarks("");
				}
			}
		}
	
		if (!isExecutionStopped() ) {
			workersList.add(worker);

			Thread thread = new Thread(){
				public void run(){
					worker.startExecution();
				}
			};
			thread.start();	
		}
	}

	protected boolean hasMorePendingBatches(ScriptGridTracker scriptGridTracker) {
		List<BatchInjectionTracker> batchInjectionTrackerList=scriptIdToBatchInjectionTrackerList.get(scriptGridTracker.getScript().getUniqueID().intValue());
		for (BatchInjectionTracker batchInjectionTracker:batchInjectionTrackerList) {
			if (batchInjectionTracker.isPending()) {
				return true;
			}
		}
		return false;
	}

	public boolean isExecutionCompleted()  {
		return isExecutionCompleted;
	}
	
	public boolean isScriptGridTrackerCompleted(ScriptGridTracker scriptGridTracker)  {
		List<BatchInjectionTracker> batchInjectionTrackerList=scriptIdToBatchInjectionTrackerList.get(scriptGridTracker.getScript().getUniqueID().intValue());
		if (batchInjectionTrackerList==null) {
			return false;
		}
		for (BatchInjectionTracker batchInjectionTracker:batchInjectionTrackerList) {
			if (!batchInjectionTracker.isCompleted()) {
				return false;
			}
		}
		return true;
	}
	
	public void stopExecution() throws Exception {
		isExecutionStopped=true;
		for (Worker worker:workersList) {
			worker.stopExecution(false);
		}
	}
	
	
	public boolean hasAtLeastOneHTMLTypeScriptToExecute() {
		for (Worker worker:workersList) {
			if (worker instanceof SeleniumWorker) {
				return true;
			}
		}
		return false;
	}

	public boolean isAllWorkersCompleted() {
		for (Worker worker:workersList) {
			if (!worker.getBatchInjectionTracker().isCompleted() ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isAllWorkersCleaned() {
		for (Worker worker:workersList) {
			if (worker instanceof SeleniumWorker) {
				SeleniumWorker seleniumWorker=(SeleniumWorker)worker;
				if (seleniumWorker.getBatchInjectionTracker().isStarted() && !seleniumWorker.isWebDriverQuit()) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isExecutionStopped() {
		return isExecutionStopped;
	}
		
	public int getCompletedScriptsCount() {
		int counter=0;
		for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
			String status=scriptGridTracker.getStatus();
			if ( status.equals(ScriptsGrid.STATUS_FAILED)||
				 status.equals(ScriptsGrid.STATUS_SUCCESS)
			) {
				counter++;
			}
		}
		return counter;
	}
	
	public int getTotalScriptsCount() {
		return scriptGridTrackersToProcessList.size();
	}

	public Worker getWorker(int workerId) {
		for (Worker worker:workersList) {
			int workerIdTemp=worker.getWorkerId();
			if (workerIdTemp==workerId) {
				return worker;
			}
		}
		return null;
	}
	
	private void updateScriptExecutionTotalTime(ScriptGridTracker scriptGridTracker) {
		Long startTime=scriptGridTracker.getStartTime();
		if (startTime==0) {
			return;
		}
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(startTime,currentTime);
		scriptGridTracker.setExecutionTotalTime(msg);
	}
	
	private void updateScriptExecutionStartTime(ScriptGridTracker scriptGridTracker) {
		if (scriptGridTracker.getExecutionStartTime()==null || scriptGridTracker.getExecutionStartTime().isEmpty()) {
			Long startTime=scriptGridTracker.getStartTime();
			Date date=new Date(startTime);
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			String dateText = sdfDate.format(date);
			scriptGridTracker.setExecutionStartTime(dateText);
		}
	}

	private void updateScriptExecutionEndTime(ScriptGridTracker scriptGridTracker) {
		Date date=new Date();
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		String dateText = sdfDate.format(date);
		scriptGridTracker.setExecutionEndTime(dateText);
	}

	protected synchronized void refreshScriptGridAndTableGrids() {
		List<ScriptGridTracker> listToRefresh=new ArrayList<ScriptGridTracker>();
		for (int i=scriptGridTrackersRemainingList.size()-1;i>=0;i--) {
			ScriptGridTracker scriptGridTracker=scriptGridTrackersRemainingList.get(i);
			String status=scriptGridTracker.getStatus();
			if ( status.equals(ScriptsGrid.STATUS_PENDING)  ) {
				continue;
			}
			if ( status.equals(ScriptsGrid.STATUS_FAILED) || status.equals(ScriptsGrid.STATUS_SUCCESS) || status.equals(ScriptsGrid.STATUS_STOPPED) ) {
				scriptGridTrackersRemainingList.remove(i);
				updateScriptExecutionEndTime(scriptGridTracker);
			}
			updateScriptExecutionTotalTime(scriptGridTracker);
			updateRecordsTotalsScriptGridTracker(scriptGridTracker);
			listToRefresh.add(scriptGridTracker);
		}
		injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptsGrid().updateTableGrids(listToRefresh,true);
		injectMain.getExecutionPanelUI().getExecutionTabPanel().getScriptsGrid().updateScriptGridTrackers(listToRefresh);
	}

	private boolean hasAtLeastOneErrorBatch(ScriptGridTracker scriptGridTracker) {
		int scriptId=scriptGridTracker.getScript().getUniqueID().intValue();
		List<BatchInjectionTracker> batchInjectionTrackerList=scriptIdToBatchInjectionTrackerList.get(scriptId);
		if (batchInjectionTrackerList==null) {
			return false;
		}
		for (BatchInjectionTracker batchInjectionTracker:batchInjectionTrackerList) {
			if (batchInjectionTracker.isError()) {
				return true;
			}
		}
		return false;
	}
	
	private void quitWebBrowsers(ScriptGridTracker scriptGridTracker) {
		int scriptId=scriptGridTracker.getScript().getUniqueID().intValue();
		List<BatchInjectionTracker> batchInjectionTrackerList=scriptIdToBatchInjectionTrackerList.get(scriptId);
		if (batchInjectionTrackerList==null) {
			return;
		}
		for (BatchInjectionTracker batchInjectionTracker:batchInjectionTrackerList) {
			Worker worker=batchInjectionTracker.getWorker();
			if (worker instanceof SeleniumWorker) {
				SeleniumWorker seleniumWorker=(SeleniumWorker)worker;
				seleniumWorker.quitWebDriver();
			}
		}
	}
	
	private boolean hasAllBatchesCompleted(ScriptGridTracker scriptGridTracker) {
		int scriptId=scriptGridTracker.getScript().getUniqueID().intValue();
		List<BatchInjectionTracker> batchInjectionTrackerList=scriptIdToBatchInjectionTrackerList.get(scriptId);
		if (batchInjectionTrackerList==null) {
			return true;
		}
		for (BatchInjectionTracker batchInjectionTracker:batchInjectionTrackerList) {
			if (!batchInjectionTracker.isCompleted() ) {
				return false;
			}
		}
		return true;
	}
			
	public static void updateRecordsTotalsScriptGridTracker(ScriptGridTracker scriptGridTracker) {
		Map<String, List<RecordTracker>> inventoryToRecordTrackerMap=scriptGridTracker.getInventoryToRecordTrackerMap(); 
		Iterator<String> iterator=inventoryToRecordTrackerMap.keySet().iterator();
		int totalSuccess=0;
		int totalFailed=0;
		int totalRemaining=0;
		while ( iterator.hasNext()) {
			String key=iterator.next();
			List<RecordTracker> recordTrackersList=inventoryToRecordTrackerMap.get(key);
			for (RecordTracker recordTracker:recordTrackersList) {		
				String status=recordTracker.getStatus();
				if (status.equals(ScriptsGrid.STATUS_SUCCESS)) {
					totalSuccess++;
				}
				else
					if (status.equals(ScriptsGrid.STATUS_FAILED)) {
						totalFailed++;
					}
					else {
						totalRemaining++;
					}
			}
		}	
		scriptGridTracker.setTotalRemainingRecords(totalRemaining);
		scriptGridTracker.setTotalFailedRecords(totalFailed);
		scriptGridTracker.setTotalSuccessRecords(totalSuccess);
	}

	public List<Worker> getWorkersList() {
		return workersList;
	}

	public Map<Integer, BatchInjectionTracker> getBatchIdToBatchInjectionTrackerMap() {
		return batchIdToBatchInjectionTrackerMap;
	}
	
	public void initRoutines() throws Exception {
		FileUtils.println("Parsing routines...");
		/*
		 * PARSING ALL ROUTINES
		 * runs once so that we don't reparse them for each script
		 */
		File routineFolder=new File(new File("fusion"),"ROUTINES");
		Map<String,File> routineNameToFileMap=ModelUtils.getFileNameToFileMap(routineFolder,false);
		Iterator<String> iterator=routineNameToFileMap.keySet().iterator();
		
		while (iterator.hasNext()) {
			String routineName=iterator.next();
			FileUtils.println("routineName: '"+routineName+"'");
			File file=routineNameToFileMap.get(routineName);
			NavigationDocument routineNavigationDocument=InjectUtils.getFusionNavigationDocument(file);
			List<XmlObject> xmlObjectsListTemp=InjectUtils.parseFusionNavigationDocument(routineNavigationDocument.getNavigation());
			
			// only blocks are accepted in routines.
			for (XmlObject xmlObject:xmlObjectsListTemp){
				if (xmlObject instanceof BlockType) {
					BlockType blockType=(BlockType)xmlObject;
					routinesBlockList.add(blockType);
				}
				else {
					throw new Exception("BLOCK only allowed in routines!");
				}
			}
		}
		FileUtils.println("Parsing routines completed!");
	}

	public List<BlockType> getRoutinesBlockList() {
		return routinesBlockList;
	}
		
}
