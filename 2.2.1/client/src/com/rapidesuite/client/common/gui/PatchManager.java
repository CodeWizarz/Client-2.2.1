package com.rapidesuite.client.common.gui;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.client.common.util.Config;

public class PatchManager {

	private boolean isUpdateInProgress;
	private String executableFileName;
	private String downloadURL;
	private String userName;
	private String password;

	public PatchManager(String executableFileName) {
		
		this.executableFileName = executableFileName;
		downloadURL=Config.getPatchUrl();
    	userName=Config.getPatchUrlUserName();
    	password=Config.getPatchUrlPassword();
    	
    	if (StringUtils.isBlank(downloadURL)) {
    		throw new RuntimeException(Config.PATCH_URL+" is blank");
    	}
    	
    	if (!downloadURL.contains("://")) {
    		downloadURL = "http://"+downloadURL; //default to HTTP
    	}
	}

	public synchronized boolean isUpdateInProgress() {
		return isUpdateInProgress;
	}

	public synchronized void setUpdateInProgress(boolean isUpdateInProgress) {
		this.isUpdateInProgress = isUpdateInProgress;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getExecutableFileName() {
		return executableFileName;
	}
	
	    
}
