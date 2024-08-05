package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.List;
import java.util.Map;

public class ConvertSourceTypeGeneric {

	private List<String[]> dataRows;
	private Map<String, Integer> columnNameToPositionMap;
	
	public List<String[]> getDataRows() {
		return dataRows;
	}
	public void setDataRows(List<String[]> dataRows) {
		this.dataRows = dataRows;
	}
	public Map<String, Integer> getColumnNameToPositionMap() {
		return columnNameToPositionMap;
	}
	public void setColumnNameToPositionMap(
			Map<String, Integer> columnNameToPositionMap) {
		this.columnNameToPositionMap = columnNameToPositionMap;
	}
	
}
