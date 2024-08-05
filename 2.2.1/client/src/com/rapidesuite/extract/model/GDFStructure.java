package com.rapidesuite.extract.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GDFStructure {

	private Map<String,List<GDFElement>> contextCodeToGDFElementsMap;
		
	public GDFStructure() {
		contextCodeToGDFElementsMap=new HashMap<String,List<GDFElement>>();
	}

	public Map<String, List<GDFElement>> getContextCodeToGDFElementsMap() {
		return contextCodeToGDFElementsMap;
	}

	public void setContextCodeToGDFElementsMap(Map<String, List<GDFElement>> contextCodeToGDFElementsMap) {
		this.contextCodeToGDFElementsMap = contextCodeToGDFElementsMap;
	}
	
}
