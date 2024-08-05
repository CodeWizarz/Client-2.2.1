package com.rapidesuite.snapshot.model;

import java.util.Date;

public class UserInformation {

	private int id;
	private String loginName;
	private String fullName;
	private String password;
	private boolean hasSnapshotCreationPermission;
	private boolean isUserManager;
	private boolean isEnabled;
	boolean isEnableDefaultParallelWorkersField;
	boolean isEnableServerConnectionsDefaultFolderButton;
	boolean isEnableDownloadFolderButton;
	boolean isEnableTemplateFolderButton;	
	boolean isEnableDefaultSnapshotNamePrefixField;
	boolean isEnableResetOptionButton;
	boolean isEnableDeleteOptionCheckBox;
	boolean isEnableDisplayTotalDetailOption;
	Date osAuthLoginDate;
	Date lastPasswordUpdateDate;
	
	private boolean prefOverrideExist;
	private int prefDefaultParallelWorkers;
	private String prefServerConnectionFolderLocation;
	private String prefDownloadFolderLocation;	
	private String prefTemplateFolderLocation;
	private String prefSnapshotPrefixName;
	private String prefDownloadFormat;
	private String prefDeleteOption;
	private boolean prefDisplayUnsupportedInvOption;
	private boolean prefDisplayTotalDetailOption;
	private boolean prefDisplayHelperBalloon;	
	private boolean prefDisplayBR100;		
	private String prefSeededUsersInfoOption;	
	private boolean prefIncludeSoftDeletedSnapshot;		
	
	
	public String getLoginName() {
		return loginName;
	}
	
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean hasSnapshotCreationPermission() {
		return hasSnapshotCreationPermission;
	}

	public void setHasSnapshotCreationPermission(boolean hasSnapshotCreationPermission) {
		this.hasSnapshotCreationPermission = hasSnapshotCreationPermission;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setUserManager(boolean isUserManager) {
		this.isUserManager=isUserManager;
	}

	public boolean isUserManager() {
		return isUserManager;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled=isEnabled;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isEnableDefaultParallelWorkersField() {
		return isEnableDefaultParallelWorkersField;
	}

	public void setEnableDefaultParallelWorkersField(boolean isEnableDefaultParallelWorkersField) {
		this.isEnableDefaultParallelWorkersField = isEnableDefaultParallelWorkersField;
	}

	public boolean isEnableServerConnectionsDefaultFolderButton() {
		return isEnableServerConnectionsDefaultFolderButton;
	}

	public void setEnableServerConnectionsDefaultFolderButton(boolean isEnableServerConnectionsDefaultFolderButton) {
		this.isEnableServerConnectionsDefaultFolderButton = isEnableServerConnectionsDefaultFolderButton;
	}

	public boolean isEnableDownloadFolderButton() {
		return isEnableDownloadFolderButton;
	}

	public void setEnableDownloadFolderButton(boolean isEnableDownloadFolderButton) {
		this.isEnableDownloadFolderButton = isEnableDownloadFolderButton;
	}

	public boolean isEnableTemplateFolderButton() {
		return isEnableTemplateFolderButton;
	}

	public void setEnableTemplateFolderButton(boolean isEnableTemplateFolderButton) {
		this.isEnableTemplateFolderButton = isEnableTemplateFolderButton;
	}

	public boolean isEnableDefaultSnapshotNamePrefixField() {
		return isEnableDefaultSnapshotNamePrefixField;
	}

	public void setEnableDefaultSnapshotNamePrefixField(boolean isEnableDefaultSnapshotNamePrefixField) {
		this.isEnableDefaultSnapshotNamePrefixField = isEnableDefaultSnapshotNamePrefixField;
	}

	public boolean isEnableResetOptionButton() {
		return isEnableResetOptionButton;
	}

	public void setEnableResetOptionButton(boolean isEnableResetOptionButton) {
		this.isEnableResetOptionButton = isEnableResetOptionButton;
	}

	public boolean isEnableDeleteOptionCheckBox() {
		return isEnableDeleteOptionCheckBox;
	}

	public void setEnableDeleteOptionCheckBox(boolean isEnableDeleteOptionCheckBox) {
		this.isEnableDeleteOptionCheckBox = isEnableDeleteOptionCheckBox;
	}

	public boolean isEnableDisplayTotalDetailOption() {
		return isEnableDisplayTotalDetailOption;
	}

	public void setEnableDisplayTotalDetailOption(boolean isEnableDisplayTotalDetailOption) {
		this.isEnableDisplayTotalDetailOption = isEnableDisplayTotalDetailOption;
	}

	public Date getOsAuthLoginDate() {
		return osAuthLoginDate;
	}

	public void setOsAuthLoginDate(Date osAuthLoginDate) {
		this.osAuthLoginDate = osAuthLoginDate;
	}

	public Date getLastPasswordUpdateDate() {
		return lastPasswordUpdateDate;
	}

	public void setLastPasswordUpdateDate(Date lastPasswordUpdateDate) {
		this.lastPasswordUpdateDate = lastPasswordUpdateDate;
	}

	public boolean isPrefOverrideExist() {
		return prefOverrideExist;
	}

	public void setPrefOverrideExist(boolean prefOverrideExist) {
		this.prefOverrideExist = prefOverrideExist;
	}



	public int getPrefDefaultParallelWorkers() {
		return prefDefaultParallelWorkers;
	}

	public void setPrefDefaultParallelWorkers(int prefDefaultParallelWorkers) {
		this.prefDefaultParallelWorkers = prefDefaultParallelWorkers;
	}

	public String getPrefServerConnectionFolderLocation() {
		return prefServerConnectionFolderLocation;
	}

	public void setPrefServerConnectionFolderLocation(String prefServerConnectionFolderLocation) {
		this.prefServerConnectionFolderLocation = prefServerConnectionFolderLocation;
	}

	public String getPrefDownloadFolderLocation() {
		return prefDownloadFolderLocation;
	}

	public void setPrefDownloadFolderLocation(String prefDownloadFolderLocation) {
		this.prefDownloadFolderLocation = prefDownloadFolderLocation;
	}

	public String getPrefTemplateFolderLocation() {
		return prefTemplateFolderLocation;
	}

	public void setPrefTemplateFolderLocation(String prefTemplateFolderLocation) {
		this.prefTemplateFolderLocation = prefTemplateFolderLocation;
	}

	public String getPrefSnapshotPrefixName() {
		return prefSnapshotPrefixName;
	}

	public void setPrefSnapshotPrefixName(String prefSnapshotPrefixName) {
		this.prefSnapshotPrefixName = prefSnapshotPrefixName;
	}

	public String getPrefDeleteOption() {
		return prefDeleteOption;
	}

	public void setPrefDeleteOption(String prefDeleteOption) {
		this.prefDeleteOption = prefDeleteOption;
	}

	public boolean isPrefDisplayUnsupportedInvOption() {
		return prefDisplayUnsupportedInvOption;
	}

	public void setPrefDisplayUnsupportedInvOption(boolean prefDisplayUnsupportedInvOption) {
		this.prefDisplayUnsupportedInvOption = prefDisplayUnsupportedInvOption;
	}

	public boolean isPrefDisplayTotalDetailOption() {
		return prefDisplayTotalDetailOption;
	}

	public void setPrefDisplayTotalDetailOption(boolean prefDisplayTotalDetailOption) {
		this.prefDisplayTotalDetailOption = prefDisplayTotalDetailOption;
	}

	public boolean isPrefDisplayHelperBalloon() {
		return prefDisplayHelperBalloon;
	}

	public void setPrefDisplayHelperBalloon(boolean prefDisplayHelperBalloon) {
		this.prefDisplayHelperBalloon = prefDisplayHelperBalloon;
	}

	public boolean isPrefDisplayBR100() {
		return prefDisplayBR100;
	}

	public void setPrefDisplayBR100(boolean prefDisplayBR100) {
		this.prefDisplayBR100 = prefDisplayBR100;
	}

	public String getPrefSeededUsersInfoOption() {
		return prefSeededUsersInfoOption;
	}

	public void setPrefSeededUsersInfoOption(String prefSeededUsersInfoOption) {
		this.prefSeededUsersInfoOption = prefSeededUsersInfoOption;
	}

	public String getPrefDownloadFormat() {
		return prefDownloadFormat;
	}

	public void setPrefDownloadFormat(String prefDownloadFormat) {
		this.prefDownloadFormat = prefDownloadFormat;
	}

	public boolean isPrefIncludeSoftDeletedSnapshot() {
		return prefIncludeSoftDeletedSnapshot;
	}

	public void setPrefIncludeSoftDeletedSnapshot(boolean prefIncludeSoftDeletedSnapshot) {
		this.prefIncludeSoftDeletedSnapshot = prefIncludeSoftDeletedSnapshot;
	}
	
}
