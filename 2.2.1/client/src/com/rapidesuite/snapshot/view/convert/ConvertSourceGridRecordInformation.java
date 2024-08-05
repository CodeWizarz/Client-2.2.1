package com.rapidesuite.snapshot.view.convert;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ConvertSourceGridRecordInformation extends ConvertGenericGridRecordInformation {
	
	private int totalRecordsToConvert;
	private int totalRecordsNotFullyConverted;
	private File sourceDataXMLFile;
	private String columnNamesNotFullyConverted;
	private String supportText;
	private List<String[]> dataRows;
	private boolean hasColumnValidation;
	private Set<String> columnNamesConvertedSet;
	
	public ConvertSourceGridRecordInformation(String inventoryName) {
		super(inventoryName);
	}

	public String getSupportText() {
		return supportText;
	}

	public void setSupportText(String supportText) {
		this.supportText = supportText;
	}	

	public int getTotalRecordsToConvert() {
		return totalRecordsToConvert;
	}

	public void setTotalRecordsToConvert(int totalRecordsToConvert) {
		this.totalRecordsToConvert = totalRecordsToConvert;
	}

	public void setDataFile(File sourceDataXMLFile) {
		this.sourceDataXMLFile=sourceDataXMLFile;
	}

	public File getSourceDataXMLFile() {
		return sourceDataXMLFile;
	}

	public int getTotalRecordsNotFullyConverted() {
		return totalRecordsNotFullyConverted;
	}

	public void setTotalRecordsNotFullyConverted(int totalRecordsNotFullyConverted) {
		this.totalRecordsNotFullyConverted = totalRecordsNotFullyConverted;
	}

	public String getColumnNamesNotFullyConverted() {
		return columnNamesNotFullyConverted;
	}

	public void setColumnNamesNotFullyConverted(String columnNamesNotFullyConverted) {
		this.columnNamesNotFullyConverted = columnNamesNotFullyConverted;
	}

	public void setDataRows(List<String[]> dataRows) {
		this.dataRows=dataRows;
	}

	public List<String[]> getDataRows() {
		return dataRows;
	}


	public boolean isHasColumnValidation() {
		return hasColumnValidation;
	}


	public void setHasColumnValidation(boolean hasColumnValidation) {
		this.hasColumnValidation = hasColumnValidation;
	}

	public void setColumnNamesConvertedSet(Set<String> columnNamesConvertedSet) {
		this.columnNamesConvertedSet=columnNamesConvertedSet;
	}

	public Set<String> getColumnNamesConvertedSet() {
		return columnNamesConvertedSet;
	}

}
