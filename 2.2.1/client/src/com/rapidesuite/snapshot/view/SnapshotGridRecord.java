package com.rapidesuite.snapshot.view;

public class SnapshotGridRecord {
	
	private int snapshotId;
	private String name;
	private String status;
	private String description;
	private String createdOn;
	private String osUserName;
	private String clientHostName;
	private String completedOn;
	private int totalInventories;
	private int totalInventoriesSelected;
	private int totalInventoriesFailed;
	private int totalRecords;
	private int totalDefaultRecords;
	private int totalAddedRecords;
	private int totalUpdatedRecords;
	private boolean isConversion;
	private int userId;
	private String mode;
	private String templateName;

	public String getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}

	public int getSnapshotId() {
		return snapshotId;
	}

	public int getTotalInventories() {
		return totalInventories;
	}

	public int getTotalInventoriesSelected() {
		return totalInventoriesSelected;
	}
	
	public int getTotalInventoriesFailed() {
		return totalInventoriesFailed;
	}

	public String getDescription() {
		return description;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public String getCompletedOn() {
		return completedOn;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public void setCompletedOn(String completedOn) {
		this.completedOn = completedOn;
	}

	public void setTotalInventories(int totalInventories) {
		this.totalInventories = totalInventories;
	}

	public void setTotalInventoriesSelected(int totalInventoriesSelected) {
		this.totalInventoriesSelected = totalInventoriesSelected;
	}

	public void setTotalInventoriesFailed(int totalInventoriesFailed) {
		this.totalInventoriesFailed = totalInventoriesFailed;
	}

	public String getClientHostName() {
		return clientHostName;
	}

	public void setClientHostName(String clientHostName) {
		this.clientHostName = clientHostName;
	}
	
	public int getTotalRecords() {
		return totalRecords;
	}

	public int getTotalDefaultRecords() {
		return totalDefaultRecords;
	}

	public int getTotalAddedRecords() {
		return totalAddedRecords;
	}

	public int getTotalUpdatedRecords() {
		return totalUpdatedRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public void setTotalDefaultRecords(int totalDefaultRecords) {
		this.totalDefaultRecords = totalDefaultRecords;
	}

	public void setTotalAddedRecords(int totalAddedRecords) {
		this.totalAddedRecords = totalAddedRecords;
	}

	public void setTotalUpdatedRecords(int totalUpdatedRecords) {
		this.totalUpdatedRecords = totalUpdatedRecords;
	}

	public boolean isConversion() {
		return isConversion;
	}

	public void setConversion(boolean isConversion) {
		this.isConversion = isConversion;
	}

	public String getOsUserName() {
		return osUserName;
	}

	public void setOsUserName(String osUserName) {
		this.osUserName = osUserName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setSnapshotId(int snapshotId) {
		this.snapshotId = snapshotId;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

}
