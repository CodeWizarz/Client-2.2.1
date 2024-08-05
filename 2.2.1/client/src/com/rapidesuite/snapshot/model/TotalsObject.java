package com.rapidesuite.snapshot.model;

public class TotalsObject {

	private int tableId;
	private int totalRecords;
	private int totalDefaultRecords;
	private int totalAddedRecords;
	private int totalUpdatedRecords;
	
	public TotalsObject(int tableId) {
		this.tableId=tableId;
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

	public int getTableId() {
		return tableId;
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
	
}
