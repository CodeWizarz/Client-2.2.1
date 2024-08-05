package com.rapidesuite.snapshot.view;

import java.sql.Connection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.OperatingUnit;
import com.rapidesuite.snapshot.model.SnapshotImportSwingWorker;

public class SearchWindowOperatingUnits extends SearchWindow{

	private Map<String, String> snapshotEnvironmentProperties;
	private FilterOperatingUnitCommon filterOperatingUnitCommon;
	private SnapshotGridRecord snapshotGridRecord;
	
	public SearchWindowOperatingUnits(JFrame rootFrame,FilterOperatingUnitCommon filterOperatingUnitCommon, 
			String title,Map<String, String> snapshotEnvironmentProperties,Map<String,Object> selectedResultsKeyToObjectMap,
			SnapshotGridRecord snapshotGridRecord) {
		super(rootFrame, title ,SearchWindow.WILDCARD_KEYWORD,selectedResultsKeyToObjectMap);
		this.snapshotEnvironmentProperties=snapshotEnvironmentProperties;
		this.filterOperatingUnitCommon=filterOperatingUnitCommon;
		this.snapshotGridRecord=snapshotGridRecord;
		processActionSearch(SearchWindow.WILDCARD_KEYWORD);
		dialog.setVisible(true);
	}

	@Override
	public Map<String,Object> search(String inputValue)  throws Exception {
		Connection connection=null;
		try {
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));
			
			Map<String,OperatingUnit> operatingUnitNameToObjectMap=null;
			if (snapshotGridRecord!=null && snapshotGridRecord.getMode().equals(SnapshotImportSwingWorker.IMPORT_MODE)) {
				operatingUnitNameToObjectMap=ModelUtils.getOperatingUnitsSearchResults(connection,snapshotGridRecord.getSnapshotId(),inputValue);
			}
			else {
				operatingUnitNameToObjectMap=ModelUtils.getOperatingUnitsSearchResults(connection,inputValue);
			}
			
			Map<String,Object> toReturn = new TreeMap<String,Object>();
			toReturn.putAll(operatingUnitNameToObjectMap);
			return toReturn;
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public void apply(Map<String, Object> selectedResultsKeyToObjectMap) {
		filterOperatingUnitCommon.setUnappliedFilter();
		filterOperatingUnitCommon.setSelectedMap(selectedResultsKeyToObjectMap);
		dialog.dispose();
	}
	
}