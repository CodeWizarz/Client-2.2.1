package com.rapidesuite.inject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.inject.gui.ScriptsGrid;

public abstract class Worker {

	protected int workerId;
	protected File logFile;
	protected File logFriendlyFile;
	protected File workerLogFolder;
	protected BatchInjectionTracker batchInjectionTracker;
	
	protected Map<String,RecordTracker> inventoryToCurrentRecordTrackerMap;
	protected Map<String,List<XmlObject>> blockNameToCommandsMap;
	protected Set<String> parsedExtraNavigationsSet;
	protected String currentRepeatInventoryName;
	protected boolean isStopped;

	public final static String LOG_FILE_PREFIX="LOG-WORKER-";
	public final static String LOG_FRIENDLY_FILE_PREFIX="LOG-FRIENDLY-WORKER-";
	
	public Worker(BatchInjectionTracker batchInjectionTracker,
			int workerId) {
		this.workerId=workerId;
		this.batchInjectionTracker=batchInjectionTracker;
		File logsFolder = SwiftBuildFileUtils.getLogsFolderFromName(
				batchInjectionTracker.getScriptGridTracker().getInjectMain().getApplicationInfoPanel().getInjectionPackage().getName());
		String relativeLogFolderName=batchInjectionTracker.getScriptGridTracker().getLogFolderName();
		workerLogFolder=new File(logsFolder,relativeLogFolderName);
		workerLogFolder.mkdir();
		String logFileName=LOG_FILE_PREFIX+workerId+".txt";
		logFile=new File(workerLogFolder,logFileName);
		String logFriendlyFileName=LOG_FRIENDLY_FILE_PREFIX+workerId+".txt";
		logFriendlyFile=new File(workerLogFolder,logFriendlyFileName);
		InjectUtils.log(logFile,null,"WORKER LOG FILE...",false);
		InjectUtils.log(logFriendlyFile,null,"INJECTION STARTED.",false);
		inventoryToCurrentRecordTrackerMap=new HashMap<String,RecordTracker>();
		parsedExtraNavigationsSet=new TreeSet<String>();
	}
	
	public abstract void stopExecution(boolean isQuitBrowser) throws InterruptedException;

	public abstract void startExecution();

	public abstract void processNavigationXMLObjects(List<XmlObject> xmlObjectsList) throws Exception;
	
	public Map<String,RecordTracker> getInventoryToCurrentRecordTrackerMap() {
		return inventoryToCurrentRecordTrackerMap;
	}
	
	public void println(String message,boolean isFriendly)
	{
		InjectUtils.log(logFile,null,message,true);
		if (isFriendly) {
			InjectUtils.log(logFriendlyFile,null,message,true);
		}
	}

	public void println(String message)
	{
		InjectUtils.log(logFile,null,message,true);
	}

	public void printStackTrace(Throwable tr)
	{
		InjectUtils.log(logFile,tr,null,true);
	}
	
	public int getWorkerId() {
		return workerId;
	}

	public File getWorkerLogFolder() {
		return workerLogFolder;
	}

	public boolean isStopped() {
		return isStopped;
	}

	public ScriptManager getScriptManager() {
		return batchInjectionTracker.getScriptGridTracker().getInjectMain().getScriptManager();
	}

	public BatchInjectionTracker getBatchInjectionTracker() {
		return batchInjectionTracker;
	}

	public Map<String,List<XmlObject>> getBlockNameToCommandsMap() {
		return blockNameToCommandsMap;
	}
	
	public void setCurrentRepeatInventoryName(String currentRepeatInventoryName) {
		this.currentRepeatInventoryName = currentRepeatInventoryName;
	}

	public void updateAllRemainingRecordsToFailed(String errorMsg) {
		BatchInjectionTracker batchInjectionTracker=getBatchInjectionTracker();
		List<Map<String, RecordTracker>> inventoryToRecordTrackerList=batchInjectionTracker.getDataLinksToExecuteList();
		for (Map<String, RecordTracker> inventoryToRecordTrackerMap:inventoryToRecordTrackerList) {
			Iterator<String> iterator=inventoryToRecordTrackerMap.keySet().iterator();
			while(iterator.hasNext()){
				String key=iterator.next();
				RecordTracker recordTracker=inventoryToRecordTrackerMap.get(key);
				String statusRec=recordTracker.getStatus();
				if (recordTracker.getWorkerId()==workerId && (
						statusRec.equals(ScriptsGrid.STATUS_PENDING) ||
						statusRec.equals(ScriptsGrid.STATUS_PROCESSING) ||
						statusRec.equals(ScriptsGrid.STATUS_QUEUED)
						)) {
					recordTracker.setStatus(ScriptsGrid.STATUS_FAILED);
					if (getScriptManager().isExecutionStopped()) {
						recordTracker.setRemarks("Manual stop!");
					}
					else {
						recordTracker.setRemarks(errorMsg);
					}
				}
			}
		}
		getScriptManager().getInjectMain().getExecutionPanelUI().getExecutionTabPanel().getScriptsGrid().updateTableGrids(batchInjectionTracker.getScriptGridTracker(),true);
	}

	public Set<String> getParsedExtraNavigationsSet() {
		return parsedExtraNavigationsSet;
	}

	public String getCurrentRepeatInventoryName() {
		return currentRepeatInventoryName;
	}
	
}
