package com.rapidesuite.snapshot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidesuite.reverse.utils.DFFRow;

public class DFFStructure {

	private List<DFFRow> dffGlobalFieldToDFFRowList;
	private Map<String,List<DFFRow>> dffContextCodeToDFFRowsMap;
	private Map<String,String> dffContextCodeToDisplayNameMap;
	private boolean isContextFieldDisplayed;
	private Map<String,Map<String,List<String>>> attributeNameToValueSetQueryToContextCodesMap;
	private Set<String> distinctDFFAttributeNames;
	
	public DFFStructure() {
		dffGlobalFieldToDFFRowList=new ArrayList<DFFRow>();
		dffContextCodeToDFFRowsMap=new HashMap<String,List<DFFRow>>();
		dffContextCodeToDisplayNameMap=new HashMap<String,String>();
		attributeNameToValueSetQueryToContextCodesMap=new HashMap<String,Map<String,List<String>>>();
		distinctDFFAttributeNames=new HashSet<String>();
	}

	public Map<String, List<DFFRow>> getDffContextCodeToDFFRowsMap() {
		return dffContextCodeToDFFRowsMap;
	}

	public void setDffContextCodeToDFFRowsMap(
			Map<String, List<DFFRow>> dffContextCodeToDFFRowsMap) {
		this.dffContextCodeToDFFRowsMap = dffContextCodeToDFFRowsMap;
	}

	public Map<String, String> getDffContextCodeToDisplayNameMap() {
		return dffContextCodeToDisplayNameMap;
	}

	public void setDffContextCodeToDisplayNameMap(
			Map<String, String> dffContextCodeToDisplayNameMap) {
		this.dffContextCodeToDisplayNameMap = dffContextCodeToDisplayNameMap;
	}

	public boolean isContextFieldDisplayed() {
		return isContextFieldDisplayed;
	}

	public void setContextFieldDisplayed(boolean isContextFieldDisplayed) {
		this.isContextFieldDisplayed = isContextFieldDisplayed;
	}

	public List<DFFRow> getDffGlobalFieldToDFFRowList() {
		return dffGlobalFieldToDFFRowList;
	}

	public void setDffGlobalFieldToDFFRowList(
			List<DFFRow> dffGlobalFieldToDFFRowList) {
		this.dffGlobalFieldToDFFRowList = dffGlobalFieldToDFFRowList;
	}
	
	public boolean hasContextValuesAttributesDefined() {
		return !dffContextCodeToDFFRowsMap.isEmpty();
	}
	
	public Set<String> getDistinctDFFAttributeNames() {
		return distinctDFFAttributeNames;
	}

	public void setDistinctDFFAttributeNames(Set<String> distinctDFFAttributeNames) {
		this.distinctDFFAttributeNames = distinctDFFAttributeNames;
	}

	public Map<String, Map<String, List<String>>> getAttributeNameToValueSetQueryToContextCodesMap() {
		return attributeNameToValueSetQueryToContextCodesMap;
	}
	
}
