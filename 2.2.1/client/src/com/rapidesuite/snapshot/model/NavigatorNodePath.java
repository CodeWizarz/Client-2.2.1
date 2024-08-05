package com.rapidesuite.snapshot.model;

import java.util.ArrayList;
import java.util.List;

public class NavigatorNodePath {
	
	private List<String> fullPath;
	private String functionId;
	private String formName;
	
	public NavigatorNodePath() {
		fullPath=new ArrayList<String>();
	}	
	
	public List<String> getFullPath() {
		return fullPath;
	}
	
	public String getFunctionId() {
		return functionId;
	}
	
	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}
	
	public void addPath(String path) {
		fullPath.add(path);
	}
	
	public void addAllPath(List<String> pathList) {
		fullPath.addAll(pathList);
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}
	
}
