package com.rapidesuite.snapshot.model;

import java.io.File;
import com.rapidesuite.client.common.util.SpreadsheetWriter;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;

public class ExcelTab {

	private SnapshotInventoryGridRecord snapshotInventoryGridRecord;
	private File sheetFile;
	private SpreadsheetWriter spreadsheetWriter;
	private int excelRowIndex;
	private String sheetRefName;
	private int generatedRecordsCount;
	private boolean isTruncated;
	private boolean isError;
	private int sheetIndex;
	
	public String getSheetRefName() {
		return sheetRefName;
	}
	public void setSheetRefName(String sheetRefName) {
		this.sheetRefName = sheetRefName;
	}
	public SpreadsheetWriter getSpreadsheetWriter() {
		return spreadsheetWriter;
	}
	public void setSpreadsheetWriter(SpreadsheetWriter spreadsheetWriter) {
		this.spreadsheetWriter = spreadsheetWriter;
	}
	public int getExcelRowIndex() {
		return excelRowIndex;
	}
	public void setExcelRowIndex(int excelRowIndex) {
		this.excelRowIndex = excelRowIndex;
	}
	public File getSheetFile() {
		return sheetFile;
	}
	public void setSheetFile(File sheetFile) {
		this.sheetFile = sheetFile;
	}	

	public SnapshotInventoryGridRecord getSnapshotInventoryGridRecord() {
		return snapshotInventoryGridRecord;
	}

	public void setSnapshotInventoryGridRecord(
			SnapshotInventoryGridRecord snapshotInventoryGridRecord) {
		this.snapshotInventoryGridRecord = snapshotInventoryGridRecord;
	}
	public int getGeneratedRecordsCount() {
		return generatedRecordsCount;
	}
	public void setGeneratedRecordsCount(int generatedRecordsCount) {
		this.generatedRecordsCount = generatedRecordsCount;
	}
	
	public boolean isTruncated() {
		return isTruncated;
	}
	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}
	public boolean isError() {
		return isError;
	}
	public void setError(boolean isError) {
		this.isError = isError;
	}
	public void setSheetIndex(int sheetIndex) {
		this.sheetIndex=sheetIndex;
	}
	public int getSheetIndex() {
		return sheetIndex;
	}
	
}
