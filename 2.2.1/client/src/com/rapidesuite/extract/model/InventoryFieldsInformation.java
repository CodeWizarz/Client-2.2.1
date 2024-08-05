package com.rapidesuite.extract.model;

import java.util.HashMap;
import java.util.Map;

public class InventoryFieldsInformation {

	private Map<String,FieldInformation> hashNameToFieldInformationMap;
	
	public InventoryFieldsInformation() {
		hashNameToFieldInformationMap=new HashMap<String,FieldInformation>();
	}
	
	public Map<String, FieldInformation> getHashNameToFieldInformationMap() {
		return hashNameToFieldInformationMap;
	}
	
}
