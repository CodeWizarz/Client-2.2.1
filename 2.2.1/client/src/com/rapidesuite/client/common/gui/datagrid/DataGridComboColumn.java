package com.rapidesuite.client.common.gui.datagrid;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

import org.openswing.swing.table.columns.client.Column;

public class DataGridComboColumn extends DataGridColumn {
	
	private List<SimpleEntry<String,String>> keyValuePairs;
	private Map<String, String> keyValueMap;
	
	public DataGridComboColumn(String attributeName,String attributeDescription,Class<?> attributeClass,
			Column column,List<SimpleEntry<String,String>> keyValuePairs,boolean isRequired,
			int columnSequence,boolean isUpdatable,boolean isResetable,boolean isPCColumn){
		super(attributeName,attributeDescription,attributeClass,column,isRequired,
				columnSequence,isUpdatable,isResetable,isPCColumn);
		this.keyValuePairs=keyValuePairs;
		keyValueMap=DataGridUtils.convertListToMap(keyValuePairs);
	}

	public List<SimpleEntry<String,String>> getKeyValuePairs() {
		return keyValuePairs;
	}

	public Map<String, String> getKeyValueMap() {
		return keyValueMap;
	}
	
}
