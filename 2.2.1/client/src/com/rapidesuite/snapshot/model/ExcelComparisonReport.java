package com.rapidesuite.snapshot.model;

public class ExcelComparisonReport {

	private ExcelReportTemplate allChangesExcelReportTemplate;
	private ExcelReportTemplate addChangesExcelReportTemplate;
	private ExcelReportTemplate deleteChangesExcelReportTemplate;
	private ExcelReportTemplate updateChangesExcelReportTemplate;
	
	public ExcelComparisonReport() {
		allChangesExcelReportTemplate=new ExcelReportTemplate();
		addChangesExcelReportTemplate=new ExcelReportTemplate();
		deleteChangesExcelReportTemplate=new ExcelReportTemplate();
		updateChangesExcelReportTemplate=new ExcelReportTemplate();
	}
	
	public ExcelReportTemplate getAllChangesExcelReportTemplate() {
		return allChangesExcelReportTemplate;
	}
	
	public ExcelReportTemplate getAddChangesExcelReportTemplate() {
		return addChangesExcelReportTemplate;
	}
	
	public ExcelReportTemplate getDeleteChangesExcelReportTemplate() {
		return deleteChangesExcelReportTemplate;
	}
	
	public ExcelReportTemplate getUpdateChangesExcelReportTemplate() {
		return updateChangesExcelReportTemplate;
	}
		
}