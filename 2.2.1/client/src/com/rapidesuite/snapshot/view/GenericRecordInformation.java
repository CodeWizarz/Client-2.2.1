package com.rapidesuite.snapshot.view;

import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.model.FormInformation;

public class GenericRecordInformation {

	private long downloadRawTimeInSecs;
	private long downloadStartTime;
	private String downloadTime;
	private int downloadDownloadedRecordsCount;
	private int tableId;
	private Inventory inventory;
	private StringBuffer whereClauseFilter;
	private FormInformation formInformation;
	protected String status;
	protected String inventoryName;
	protected String remarks;
	protected String executionTime;
	protected long startTime;
	protected long rawTimeInSecs;
	protected int gridIndex;
	private final Object lock = new Object();
	private String createdOn;
	private String completedOn;	
	private boolean isExecutable;
	private boolean isListable;
	public GenericRecordInformation(String inventoryName) {
		this.inventoryName=inventoryName;
		formInformation=new FormInformation();
	}
	
	public Long getStartTime() {
		return startTime;
	}

	public void setGridIndex(int gridIndex) {
		this.gridIndex = gridIndex;
	}

	public FormInformation getFormInformation() {
		return formInformation;
	}

	public void setFormInformation(FormInformation formInformation) {
		this.formInformation=formInformation;
	}
	
	public long getRawTimeInSecs() {
		return rawTimeInSecs;
	}

	public long getDownloadRawTimeInSecs() {
		return downloadRawTimeInSecs;
	}

	public void setDownloadRawTimeInSecs(long downloadRawTimeInSecs) {
		this.downloadRawTimeInSecs = downloadRawTimeInSecs;
	}

	public long getDownloadStartTime() {
		return downloadStartTime;
	}

	public void setDownloadStartTime(long downloadStartTime) {
		this.downloadStartTime = downloadStartTime;
	}

	public String getDownloadTime() {
		return downloadTime;
	}

	public void setDownloadTime(String downloadTime) {
		this.downloadTime = downloadTime;
	}	

	public int getDownloadDownloadedRecordsCount() {
		return downloadDownloadedRecordsCount;
	}

	public void setDownloadDownloadedRecordsCount(int downloadDownloadedRecordsCount) {
		this.downloadDownloadedRecordsCount = downloadDownloadedRecordsCount;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public void setTableId(int tableId) {
		this.tableId=tableId;
	}
	
	public int getTableId() {
		return tableId;
	}

	public void setWhereClauseFilter(StringBuffer whereClauseFilter) {
		this.whereClauseFilter=whereClauseFilter;
	}

	public StringBuffer getWhereClauseFilter() {
		return whereClauseFilter;
	}
	
	public int getGridIndex() {
		return gridIndex;
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

	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}

	public String getInventoryName() {
		return inventoryName;
	}
	
	public void setRawTimeInSecs(long rawTimeInSecs) {
		this.rawTimeInSecs=rawTimeInSecs;
	}
	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getCompletedOn() {
		return completedOn;
	}

	public void setCompletedOn(String completedOn) {
		this.completedOn = completedOn;
	}

	public boolean isExecutable() {
		return isExecutable;
	}

	public void setExecutable(boolean isExecutable) {
		this.isExecutable = isExecutable;
	}

	public boolean isListable() {
		return isListable;
	}

	public void setListable(boolean isListable) {
		this.isListable = isListable;
	}


	
}
