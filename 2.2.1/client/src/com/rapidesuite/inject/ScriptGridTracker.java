package com.rapidesuite.inject;

import java.util.List;
import java.util.Map;
import com.erapidsuite.configurator.navigation0005.Navigation;
import com.rapidesuite.core.fusionScripts0000.ScriptType;
import com.rapidesuite.inject.gui.ScriptsGrid;

public class ScriptGridTracker {

	private InjectMain injectMain;
	private ScriptType script;
	private String displayName;
	private String displayType;
	private String status;
	private String remarks;
	private String executionTotalTime;
	private long startTime;
	private int gridIndex;
	private int totalRecords;
	private int totalRemainingRecords;
	private int totalFailedRecords;
	private int totalSuccessRecords;
	private int batchSize;
	private int totalBatchCount;
	private int completedBatchCount;
	private Map<String,List<RecordTracker>> inventoryToRecordTrackerMap; 
	private String executionStartTime;
	private String executionEndTime;
	private int executionRetries;
	private Navigation navigation;
	private boolean isCompleted;
	private boolean isBatchingAllowed;
	private double percentComplete;

	private final Object lock = new Object();

	public ScriptGridTracker(InjectMain injectMain,ScriptType script,int gridIndex,int totalRecords) throws Exception {
		this.injectMain=injectMain;
		this.script=script;
		this.totalRecords=totalRecords;
		this.gridIndex=gridIndex;
		status=ScriptsGrid.STATUS_PENDING;
	}

	public ScriptType getScript() {
		return script;
	}

	public InjectMain getInjectMain() {
		return injectMain;
	}

	public Long getStartTime() {
		return startTime;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public int getTotalRemainingRecords() {
		synchronized(lock) {
			return totalRemainingRecords;
		}
	}

	public void setTotalRemainingRecords(int totalRemainingRecords) {
		synchronized(lock) {
			this.totalRemainingRecords = totalRemainingRecords;
		}
	}

	public int getTotalFailedRecords() {
		synchronized(lock) {
			return totalFailedRecords;
		}
	}

	public void setTotalFailedRecords(int totalFailedRecords) {
		synchronized(lock) {
			this.totalFailedRecords = totalFailedRecords;
		}
	}

	public int getTotalSuccessRecords() {
		synchronized(lock) {
			return totalSuccessRecords;
		}
	}

	public void setTotalSuccessRecords(int totalSuccessRecords) {
		synchronized(lock) {
			this.totalSuccessRecords = totalSuccessRecords;
		}
	}

	public Map<String, List<RecordTracker>> getInventoryToRecordTrackerMap() {
		return inventoryToRecordTrackerMap;
	}

	public void setInventoryToRecordTrackerMap(Map<String, List<RecordTracker>> inventoryToRecordTrackerMap) {
		this.inventoryToRecordTrackerMap = inventoryToRecordTrackerMap;
	}

	public int getGridIndex() {
		return gridIndex;
	}
	
	public String getLogFolderName() {
		return "script"+(gridIndex+1);
	}

	public String getStatus() {
		synchronized(lock) {
			return status;
		}
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setStatus(String status) {
		synchronized(lock) {
			this.status=status;
		}
	}

	public void setRemarks(String remarks) {
		this.remarks=remarks;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getExecutionTotalTime() {
		return executionTotalTime;
	}

	public void setExecutionTotalTime(String executionTotalTime) {
		this.executionTotalTime = executionTotalTime;
	}

	public int getTotalBatchCount() {
		return totalBatchCount;
	}

	public void setTotalBatchCount(int totalBatchCount) {
		this.totalBatchCount = totalBatchCount;
	}

	public int getCompletedBatchCount() {
		return completedBatchCount;
	}

	public void setCompletedBatchCount(int completedBatchCount) {
		this.completedBatchCount = completedBatchCount;
	}

	public void incrementCompletedBatchCount() {
		synchronized(lock) {
			completedBatchCount++;
		}
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public String getExecutionStartTime() {
		return executionStartTime;
	}

	public void setExecutionStartTime(String executionStartTime) {
		this.executionStartTime = executionStartTime;
	}

	public String getExecutionEndTime() {
		return executionEndTime;
	}

	public void setExecutionEndTime(String executionEndTime) {
		this.executionEndTime = executionEndTime;
	}

	public int getExecutionRetries() {
		return executionRetries;
	}

	public void setExecutionRetries(int executionRetries) {
		this.executionRetries = executionRetries;
	}

	public Navigation getNavigation() {
		return navigation;
	}

	public void setNavigation(Navigation navigation) {
		this.navigation = navigation;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public boolean isBatchingAllowed() {
		return isBatchingAllowed;
	}

	public void setBatchingAllowed(boolean isBatchingAllowed) {
		this.isBatchingAllowed = isBatchingAllowed;
	}

	public void setPercentageComplete(double percentComplete) {
		this.percentComplete=percentComplete;
	}

	public double getPercentageComplete() {
		return percentComplete;
	}


}
