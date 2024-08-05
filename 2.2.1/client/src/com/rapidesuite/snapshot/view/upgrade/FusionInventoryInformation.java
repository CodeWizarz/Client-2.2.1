package com.rapidesuite.snapshot.view.upgrade;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

public class FusionInventoryInformation {

	private String taskName;
	private Set<String> navigationNameSet;
	private File tempInventoryFile;
	private File mappingFile;
	private Set<String> ebsInventoryNameSet;
	
	public FusionInventoryInformation() {
		navigationNameSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	}
	
	public Set<String> getNavigationNameSet() {
		return navigationNameSet;
	}
	
	public void addNavigationName(String navigationName) {
		navigationNameSet.add(navigationName);
	}
	
	public File getTempInventoryFile() {
		return tempInventoryFile;
	}
	public void setTempInventoryFile(File tempInventoryFile) {
		this.tempInventoryFile = tempInventoryFile;
	}
	
	public String getConcatenatedNavigationNames() {
		int i=0;
		StringBuffer toReturn=new StringBuffer("");
		for (String navigationName:navigationNameSet) {
			toReturn.append(navigationName);
			if ( (i+1) < navigationNameSet.size()) {
				toReturn.append(",\n");
			}
			i++;
		}
		return toReturn.toString();
	}
	
	public File getMappingFile() {
		return mappingFile;
	}
	public void setMappingFile(File mappingFile) {
		this.mappingFile = mappingFile;
	}
	public void setEBSInventorySet(Set<String> ebsInventoryNameSet) {
		this.ebsInventoryNameSet=ebsInventoryNameSet;
	}
	public Set<String> getEbsInventoryNameSet() {
		return ebsInventoryNameSet;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	
}
