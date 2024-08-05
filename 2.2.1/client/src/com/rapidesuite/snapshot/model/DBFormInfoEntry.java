package com.rapidesuite.snapshot.model;

public class DBFormInfoEntry {

	private int functionId;
	private String formName;
	private String applicationName;
	private String fullPath;

	public int getFunctionId() {
		return functionId;
	}
	public void setFunctionId(int functionId) {
		this.functionId = functionId;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setFullPath(String fullPath) {
		this.fullPath=fullPath;
	}
	public String getFullPath() {
		return fullPath;
	}


}
