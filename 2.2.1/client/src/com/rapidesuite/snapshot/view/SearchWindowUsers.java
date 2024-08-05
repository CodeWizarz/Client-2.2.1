package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.sql.Connection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.OracleUser;
import com.rapidesuite.snapshot.model.SnapshotImportSwingWorker;

public class SearchWindowUsers extends SearchWindow{

	private Map<String, String> snapshotEnvironmentProperties;
	private FilterUserPanel filterUserPanel;
	private boolean isLastUpdateBy;
	private SnapshotGridRecord snapshotGridRecord;
	private static final int MAX_QUERY_COUNT=20;

	public SearchWindowUsers(JFrame rootFrame,FilterUserPanel filterUserPanel, 
			String title,Map<String, String> snapshotEnvironmentProperties,Map<String,Object> selectedResultsKeyToObjectMap,
			boolean isLastUpdateBy,SnapshotGridRecord snapshotGridRecord) {
		super(rootFrame, title ,null,selectedResultsKeyToObjectMap);
		this.snapshotEnvironmentProperties=snapshotEnvironmentProperties;
		this.filterUserPanel=filterUserPanel;
		this.isLastUpdateBy=isLastUpdateBy;
		this.snapshotGridRecord=snapshotGridRecord;
		dialog.setVisible(true);
	}

	@Override
	public Map<String,Object> search(String inputValue)  throws Exception {
		Connection connection=null;
		try {
			if (inputValue.equals(SearchWindow.WILDCARD_KEYWORD) ) {
				throw new Exception("You must use \""+SearchWindow.WILDCARD_KEYWORD+"\" with other characters. Search ALL is not allowed!");
			}
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));
			
			int totalCount=-1;
			boolean isImportMode=snapshotGridRecord!=null && snapshotGridRecord.getMode().equals(SnapshotImportSwingWorker.IMPORT_MODE);
			if (isImportMode) {
				totalCount=ModelUtils.getOracleUsersCountSearchResults(connection,snapshotGridRecord.getSnapshotId(),inputValue);
			}
			else {
				totalCount=ModelUtils.getOracleUsersCountSearchResults(connection,inputValue);
			}
			
			
			if (totalCount>MAX_QUERY_COUNT) {
				warningLabel.setText(" "+totalCount+" values were found but the maximum allowed is "+MAX_QUERY_COUNT+" so "+
						"displaying the first "+MAX_QUERY_COUNT+" values!");
			}
			
			Map<String, OracleUser> oracleUserMap=null;
			if (isImportMode) {
				 oracleUserMap=ModelUtils.getOracleUsersSearchResults(connection,inputValue,MAX_QUERY_COUNT,snapshotGridRecord.getSnapshotId());
			}
			else {
				 oracleUserMap=ModelUtils.getOracleUsersSearchResults(connection,inputValue,MAX_QUERY_COUNT);
			}			
			Map<String,Object> toReturn = new TreeMap<String,Object>();
			toReturn.putAll(oracleUserMap);
			return toReturn;
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public void apply(Map<String, Object> selectedResultsKeyToObjectMap) {
		if (selectedResultsKeyToObjectMap.size()>MAX_QUERY_COUNT) {
			statusLabel.setText("The maximum number of values that is allowed to select is "+MAX_QUERY_COUNT+" . Please remove some selected values!");
			statusLabel.setBackground(Color.decode("#ee3630"));
			return;
		}
		filterUserPanel.setUnappliedFilter();
		filterUserPanel.setSelectedMap(isLastUpdateBy,selectedResultsKeyToObjectMap);
		dialog.dispose();

	}	
	
}