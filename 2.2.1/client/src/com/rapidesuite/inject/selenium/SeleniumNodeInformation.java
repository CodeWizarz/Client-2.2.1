package com.rapidesuite.inject.selenium;

public class SeleniumNodeInformation {

	private String displayName;
	private String hostName;
	private Integer vncPort;
	private String vncPassword;
	private Integer websockifyPort;
	private String sshUser;
	private String sshPassword;
	
	public SeleniumNodeInformation(String displayName,String hostName,Integer vncPort,String vncPassword,Integer websockifyPort,String sshUser,String sshPassword) {
		this.displayName=displayName;
		this.hostName=hostName;
		this.vncPort=vncPort;
		this.vncPassword=vncPassword;
		this.websockifyPort=websockifyPort;
		this.sshUser=sshUser;
		this.sshPassword=sshPassword;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getHostName() {
		return hostName;
	}

	public Integer getVncPort() {
		return vncPort;
	}

	public String getVncPassword() {
		return vncPassword;
	}

	public Integer getWebsockifyPort() {
		return websockifyPort;
	}

	public String getSshUser() {
		return sshUser;
	}

	public String getSshPassword() {
		return sshPassword;
	}
	
}
