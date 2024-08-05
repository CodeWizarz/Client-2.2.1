package com.rapidesuite.extract.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFFStructure {

	private List<DFFElement> globalElements;
	private Map<String,List<DFFElement>> contextCodeToDFFElementsMap;
	private String contextCodePrompt;
	private String contextCodeValueSetId;
	private String contextCodeValueSetCode;
	private Map<String,String> contextCodeToContextNameMap;
	
	public DFFStructure() {
		contextCodeToDFFElementsMap=new HashMap<String,List<DFFElement>>();
		contextCodeToContextNameMap=new HashMap<String,String>();
		globalElements=new ArrayList<DFFElement>();
	}
	
	public List<DFFElement> getGlobalElements() {
		return globalElements;
	}

	public Map<String,List<DFFElement>> getContextCodeToDFFElementsMap() {
		return contextCodeToDFFElementsMap;
	}

	public String getContextCodePrompt() {
		return contextCodePrompt;
	}

	public void setContextCodePrompt(String contextCodePrompt) {
		this.contextCodePrompt = contextCodePrompt;
	}

	public Map<String, String> getContextCodeToContextNameMap() {
		return contextCodeToContextNameMap;
	}

	public String getContextCodeValueSetId() {
		return contextCodeValueSetId;
	}

	public void setContextCodeValueSetId(String contextCodeValueSetId) {
		this.contextCodeValueSetId = contextCodeValueSetId;
	}

	public void setContextCodeValueSetCode(String contextCodeValueSetCode) {
		this.contextCodeValueSetCode=contextCodeValueSetCode;
	}

	public String getContextCodeValueSetCode() {
		return contextCodeValueSetCode;
	}

}
