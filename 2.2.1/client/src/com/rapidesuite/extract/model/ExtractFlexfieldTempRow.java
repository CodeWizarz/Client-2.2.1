package com.rapidesuite.extract.model;

import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class ExtractFlexfieldTempRow extends ExtractGenericDataRow{

	private Map<String,String> foreignKeyHashNameToValueMap;
	private String contextCode;
	private Map<String,String> attributeNameToValueMap;
	
	public ExtractFlexfieldTempRow() {
		foreignKeyHashNameToValueMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		attributeNameToValueMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
	}
	
	public Map<String,String> getForeignKeyHashNameToValueMap() {
		return foreignKeyHashNameToValueMap;
	}
	
	public String getContextCode() {
		return contextCode;
	}
	public void setContextCode(String contextCode) {
		this.contextCode = contextCode;
	}
	public Map<String,String> getAttributeNameToValueMap() {
		return attributeNameToValueMap;
	}
		
}
