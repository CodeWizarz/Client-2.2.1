package com.rapidesuite.inject;

import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.gui.ScriptsGrid;

public class RecordTracker {

	private ScriptGridTracker scriptGridTracker;
	private int gridIndex;
	private long startTime;

	/*
	 * Use this to check if the injection should wait until the record is defined in a prior batch.
	 * If record.workerId <> SeleniumWorker.Id 
	 * then 
	 * 		if record.status <> SUCCESS
	 * 		then WAIT 
	 */
	private int workerId;
	private int batchId;
	private String status;
	private String fieldName;
	private String remarks;
	private String vncDisplayName;
	private String executionTime;
	private boolean isSelected = true;
	
	public RecordTracker(ScriptGridTracker scriptGridTracker,int gridIndex) {
		this.scriptGridTracker=scriptGridTracker;
		this.gridIndex=gridIndex;
		status=ScriptsGrid.STATUS_PENDING;
	}

	public int getWorkerId() {
		return workerId;
	}

	public void setWorkerId(int workerId) {
		this.workerId = workerId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ScriptGridTracker getScriptGridTracker() {
		return scriptGridTracker;
	}

	public int getGridIndex() {
		return gridIndex;
	}

	public int getBatchId() {
		return batchId;
	}

	public void setBatchId(int batchId) {
		this.batchId = batchId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getVncDisplayName() {
		return vncDisplayName;
	}

	public void setVncDisplayName(String vncDisplayName) {
		this.vncDisplayName = vncDisplayName;
	}
	
	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public Long getStartTime() {
		return startTime;
	}
	
	public void updateRecordExecutionTime() {
		if (startTime==0) {
			return;
		}
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(startTime,currentTime);
		setExecutionTime(msg);
	}
	
	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public boolean getIsSelected() {
		return this.isSelected;
	}
	
}
