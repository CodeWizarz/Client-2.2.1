package com.rapidesuite.snapshot.view.upgrade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FusionInventoryRow extends GenericInventoryRow {

	private int totalRecords;
	private int totalRecordsConfiguration;
	private int totalRecordsPostConfiguration;
	private int totalRecordsPostImplementation;
	private int totalRecordsPostImplementationObsolete;

	private FusionInventoryInformation fusionInventoryInformation;
	private int totalRecordsMissingRequiredColumns;
	private int totalRecordsInvalidValues;
	private String reportRemarks;
	private String reportStatus;
	
	private Map<Integer,List<FusionInventoryRow>> sortedChildrenMap;
	
	public FusionInventoryRow(String inventoryName) {
		super(inventoryName);
		sortedChildrenMap=new TreeMap<Integer,List<FusionInventoryRow>>();
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

	public FusionInventoryInformation getFusionInventoryInformation() {
		return fusionInventoryInformation;
	}

	public void setFusionInventoryInformation(FusionInventoryInformation fusionInventoryInformation) {
		this.fusionInventoryInformation = fusionInventoryInformation;
	}

	public void reset() {
		totalRecords=0;
		totalRecordsConfiguration=0;
		totalRecordsPostConfiguration=0;
		totalRecordsPostImplementation=0;
		totalRecordsMissingRequiredColumns=0;
		totalRecordsInvalidValues=0;
		status="";
		remarks="";
		executionTime="";
		startTime=-1;
		rawTimeInSecs=-1;
	}

	public String getReportRemarks() {
		return reportRemarks;
	}
	
	public String getReportStatus() {
		return reportStatus;
	}

	public void setReportRemarks(String reportRemarks) {
		this.reportRemarks = reportRemarks;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}
	
	public void addChild(Integer sequence,FusionInventoryRow fusionInventoryRow) {
		List<FusionInventoryRow> fusionInventoryRowList=sortedChildrenMap.get(sequence);
		if (fusionInventoryRowList==null) {
			fusionInventoryRowList=new ArrayList<FusionInventoryRow>();
			sortedChildrenMap.put(sequence,fusionInventoryRowList);
		}
		fusionInventoryRowList.add(fusionInventoryRow);
	}

	public Map<Integer, List<FusionInventoryRow>> getSortedChildrenMap() {
		return sortedChildrenMap;
	}
	
	public List<FusionInventoryRow> getSortedChildrenList() {
		List<FusionInventoryRow> toReturn=new ArrayList<FusionInventoryRow>();
		Iterator<Integer> iterator=sortedChildrenMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer sequence=iterator.next();
			List<FusionInventoryRow> fusionInventoryRowList=sortedChildrenMap.get(sequence);
			for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowList) {
				toReturn.add(fusionInventoryRow);
				toReturn.addAll(fusionInventoryRow.getSortedChildrenList());
			}			
		}
		return toReturn;
	}

	public int getTotalRecordsPostImplementationObsolete() {
		return totalRecordsPostImplementationObsolete;
	}
	
	public void setTotalRecordsPostImplementationObsolete(int totalRecordsPostImplementationObsolete) {
		this.totalRecordsPostImplementationObsolete=totalRecordsPostImplementationObsolete;
	}

}