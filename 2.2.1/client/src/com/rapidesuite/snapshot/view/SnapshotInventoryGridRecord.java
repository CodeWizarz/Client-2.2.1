package com.rapidesuite.snapshot.view;

import java.util.List;

public class SnapshotInventoryGridRecord extends GenericRecordInformation {

	private int totalRecords;
	private int totalDefaultRecords;
	private int totalUpdatedRecords;
	private int totalAddedRecords;
	private boolean isDefaultSelected;
	private boolean isDisplayTotalRecords;
	private boolean isDisplayTotalDefaultRecords;
	private boolean isDisplayTotalAddedRecords;
	private boolean isDisplayTotalUpdatedRecords;
	private String filteringResult;
	private List<ComparisonChangesRecord>  comparisonChangesRecordList;
	
	private String downloadStatus;
	private int downloadTotalRecordsCount;
	private String downloadRemarks;
	
	public SnapshotInventoryGridRecord(String inventoryName) {
		super(inventoryName);
		isDisplayTotalRecords=true;
		isDisplayTotalDefaultRecords=true;
		isDisplayTotalAddedRecords=true;
		isDisplayTotalUpdatedRecords=true;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public void setTotalDefaultRecords(int totalDefaultRecords) {
		this.totalDefaultRecords=totalDefaultRecords;
	}
	
	public int getTotalDefaultRecords() {
		return totalDefaultRecords;
	}

	public void setTotalAddedRecords(int totalAddedRecords) {
		this.totalAddedRecords=totalAddedRecords;
	}

	public void setTotalUpdatedRecords(int totalUpdatedRecords) {
		this.totalUpdatedRecords=totalUpdatedRecords;
	}

	public int getTotalUpdatedRecords() {
		return totalUpdatedRecords;
	}

	public int getTotalAddedRecords() {
		return totalAddedRecords;
	}

	public boolean isDefaultSelected() {
		return isDefaultSelected;
	}

	public void setDefaultSelected(boolean isDefaultSelected) {
		this.isDefaultSelected = isDefaultSelected;
	}

	public boolean isDisplayTotalDefaultRecords() {
		return isDisplayTotalDefaultRecords;
	}
	
	public void setDisplayTotalDefaultRecords(boolean isDisplayTotalDefaultRecords) {
		this.isDisplayTotalDefaultRecords = isDisplayTotalDefaultRecords;
	}
		
	public String getFilteringResult() {
		return filteringResult;
	}
	
	public void setFilteringResult(String filteringResult) {
		this.filteringResult=filteringResult;
	}
	
	public List<ComparisonChangesRecord> getComparisonChangesRecordList() {
		return comparisonChangesRecordList;
	}

	public void setComparisonChangesRecordList(
			List<ComparisonChangesRecord> comparisonChangesRecordList) {
		this.comparisonChangesRecordList = comparisonChangesRecordList;
	}

	public boolean isDisplayTotalRecords() {
		return isDisplayTotalRecords;
	}

	public void setDisplayTotalRecords(boolean isDisplayTotalRecords) {
		this.isDisplayTotalRecords = isDisplayTotalRecords;
	}

	public boolean isDisplayTotalAddedRecords() {
		return isDisplayTotalAddedRecords;
	}

	public void setDisplayTotalAddedRecords(boolean isDisplayTotalAddedRecords) {
		this.isDisplayTotalAddedRecords = isDisplayTotalAddedRecords;
	}

	public boolean isDisplayTotalUpdatedRecords() {
		return isDisplayTotalUpdatedRecords;
	}

	public void setDisplayTotalUpdatedRecords(boolean isDisplayTotalUpdatedRecords) {
		this.isDisplayTotalUpdatedRecords = isDisplayTotalUpdatedRecords;
	}

	public String getDownloadStatus() {
		return downloadStatus;
	}

	public void setDownloadStatus(String downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public int getDownloadTotalRecordsCount() {
		return downloadTotalRecordsCount;
	}

	public void setDownloadTotalRecordsCount(int downloadTotalRecordsCount) {
		this.downloadTotalRecordsCount = downloadTotalRecordsCount;
	}

	public String getDownloadRemarks() {
		return downloadRemarks;
	}

	public void setDownloadRemarks(String downloadRemarks) {
		this.downloadRemarks = downloadRemarks;
	}

	public void reset() {
		totalRecords=0;
		totalDefaultRecords=0;
		totalUpdatedRecords=0;
		totalAddedRecords=0;
		filteringResult="";
		status="";
		remarks="";
		executionTime="";
		startTime=-1;
		rawTimeInSecs=-1;
	}

}
