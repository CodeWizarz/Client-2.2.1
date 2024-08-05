package com.rapidesuite.snapshot.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;

public class ExcelReportTemplate {

	private Map<String, XSSFCellStyle> styles;
	private ExcelTab tocTab;
	private List<ExcelTab> dataTabList;
	private Map<String,ExcelTab> inventoryNameToDataTabMap;
	private File templateFile;
	
	public ExcelReportTemplate() {
		dataTabList=new ArrayList<ExcelTab>();
		inventoryNameToDataTabMap=new HashMap<String,ExcelTab>();
	}
	
	public Map<String, XSSFCellStyle> getStyles() {
		return styles;
	}

	public void setStyles(Map<String, XSSFCellStyle> styles) {
		this.styles = styles;
	}

	public ExcelTab getTocTab() {
		return tocTab;
	}

	public void setTocTab(ExcelTab tocTab) {
		this.tocTab = tocTab;
	}

	public void addDataTab(ExcelTab excelTab) {
		dataTabList.add(excelTab);
		inventoryNameToDataTabMap.put(excelTab.getSnapshotInventoryGridRecord().getInventoryName(), excelTab);
	}
	
	public ExcelTab getDataTab(String inventoryName) {
		return inventoryNameToDataTabMap.get(inventoryName);
	}

	public File getTemplateFile() {
		return templateFile;
	}

	public void setTemplateFile(File templateFile) {
		this.templateFile = templateFile;
	}

	public List<ExcelTab> getDataTabList() {
		return dataTabList;
	}
	
	public int getTotalGeneratedRecordsCount() {
		int toReturn=0;
		for (ExcelTab excelTab:dataTabList) {
			toReturn=toReturn+excelTab.getGeneratedRecordsCount();
		}
		return toReturn;
	}

}
