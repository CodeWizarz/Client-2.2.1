package com.rapidesuite.extract.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BIRowInformation {

	private Map<String,String> tagNameToValueMap;
	private List<String> orderedTagNamesList;
	
	public BIRowInformation() {
		tagNameToValueMap=new HashMap<String,String>();
		orderedTagNamesList=new ArrayList<String>();
	}

	public Map<String, String> getTagNameToValueMap() {
		return tagNameToValueMap;
	}

	public List<String> getOrderedTagNamesList() {
		return orderedTagNamesList;
	}
	
}
