package com.rapidesuite.snapshot.view.convert;

import java.io.File;

public class ConvertTargetGridRecordInformation extends ConvertGenericGridRecordInformation {

	private int totalRecords;
	private int totalRecordsConfiguration;
	private int totalRecordsPostConfiguration;
	private int totalRecordsPostImplementation;
	
	private File inventoryFile;
	private int totalRecordsMissingRequiredColumns;
	private int totalRecordsInvalidValues;
	
	public ConvertTargetGridRecordInformation(String inventoryName,File inventoryFile) {
		super(inventoryName);
		this.inventoryFile=inventoryFile;
	}
	
	public File getInventoryFile() {
		return inventoryFile;
	}

	public int getTotalRecordsMissingRequiredColumns() {
		return totalRecordsMissingRequiredColumns;
	}

	public void setTotalRecordsMissingRequiredColumns(int totalRecordsMissingRequiredColumns) {
		this.totalRecordsMissingRequiredColumns = totalRecordsMissingRequiredColumns;
	}

	public void setTotalRecordsInvalidValues(int totalRecordsInvalidValues) {
		this.totalRecordsInvalidValues=totalRecordsInvalidValues;
	}

	public int getTotalRecordsInvalidValues() {
		return totalRecordsInvalidValues;
	}

	public int getTotalRecordsConfiguration() {
		return totalRecordsConfiguration;
	}

	public void setTotalRecordsConfiguration(int totalRecordsConfiguration) {
		this.totalRecordsConfiguration = totalRecordsConfiguration;
	}

	public int getTotalRecordsPostConfiguration() {
		return totalRecordsPostConfiguration;
	}

	public void setTotalRecordsPostConfiguration(int totalRecordsPostConfiguration) {
		this.totalRecordsPostConfiguration = totalRecordsPostConfiguration;
	}

	public int getTotalRecordsPostImplementation() {
		return totalRecordsPostImplementation;
	}

	public void setTotalRecordsPostImplementation(int totalRecordsPostImplementation) {
		this.totalRecordsPostImplementation = totalRecordsPostImplementation;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	
}