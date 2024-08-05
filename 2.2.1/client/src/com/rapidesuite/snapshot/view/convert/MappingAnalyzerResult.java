package com.rapidesuite.snapshot.view.convert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MappingAnalyzerResult {
	
	private String inventoryName;
	private Set<String> columnNamesNotInAnyMappingFileSet;
	private Set<String> columnNamesUsedInMappingFileSet;
	private List<String> allColumnNamesUsedInDataEntry;
	private Set<String> targetNamesSet;
	
	public MappingAnalyzerResult() {
		columnNamesNotInAnyMappingFileSet=new TreeSet<String>();
		columnNamesUsedInMappingFileSet=new TreeSet<String>();
		allColumnNamesUsedInDataEntry=new ArrayList<String>();
		targetNamesSet=new TreeSet<String>();
	}

	public String getInventoryName() {
		return inventoryName;
	}

	public void setInventoryName(String inventoryName) {
		this.inventoryName = inventoryName;
	}

	public Set<String> getColumnNamesNotInAnyMappingFileSet() {
		return columnNamesNotInAnyMappingFileSet;
	}

	public List<String> getAllColumnNamesUsedInDataEntry() {
		return allColumnNamesUsedInDataEntry;
	}

	public Set<String> getColumnNamesUsedInMappingFileSet() {
		return columnNamesUsedInMappingFileSet;
	}

	public Set<String> getTargetNamesSet() {
		return targetNamesSet;
	}
	
	public String getFormattedTargetNamesSet() {
		Iterator<String> iterator=targetNamesSet.iterator();
		StringBuffer toReturn=new StringBuffer("");
		while (iterator.hasNext()) {
			String value=iterator.next();
			toReturn.append(value);
			if (iterator.hasNext()){
				toReturn.append("\n");
			}
		}
		return toReturn.toString();
	}
}
