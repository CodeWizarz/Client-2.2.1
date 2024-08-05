package com.rapidesuite.inject.selenium;

import java.util.Set;

public class NodesInfo {

	private Set<String> freeNodesList;
	private Set<String> busyNodesList;
	
	public NodesInfo(Set<String> freeNodesList,Set<String> busyNodesList) {
		this.freeNodesList=freeNodesList;
		this.busyNodesList=busyNodesList;
	}

	public Set<String> getFreeNodesList() {
		return freeNodesList;
	}

	public Set<String> getBusyNodesList() {
		return busyNodesList;
	}
	
}
