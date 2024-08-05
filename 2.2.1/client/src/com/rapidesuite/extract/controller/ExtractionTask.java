package com.rapidesuite.extract.controller;

import java.io.File;

import com.rapidesuite.extract.model.ExtractInventoryRecord;

public class ExtractionTask {

	private ExtractInventoryRecord extractInventoryRecord;
	
	/**
	 * {@link Deprecated}
	 */
	private File sqlFile;

	/**
	 * {@link Deprecated}
	 */
	public ExtractionTask(ExtractInventoryRecord extractInventoryRecord,File sqlFile) {
		this.extractInventoryRecord=extractInventoryRecord;
		this.sqlFile=sqlFile;
	}
	
	public ExtractionTask(ExtractInventoryRecord extractInventoryRecord) {
		this.extractInventoryRecord=extractInventoryRecord;
	}

	public ExtractInventoryRecord getExtractInventoryRecord() {
		return extractInventoryRecord;
	}

	/**
	 * {@link Deprecated}
	 */
	public File getSqlFile() {
		return sqlFile;
	}
	
}
