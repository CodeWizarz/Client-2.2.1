package com.rapidesuite.snapshot.controller;

import java.awt.Color;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.PatchUtils;
import com.rapidesuite.snapshot.model.SnapshotTotalsTask;
import com.rapidesuite.snapshot.model.SnapshotTotalsWorker;
import com.rapidesuite.snapshot.model.TotalsSnapshotObject;
import com.rapidesuite.snapshot.model.UserInformation;
import com.rapidesuite.snapshot.view.ServerSelectionPanel;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.model.MemoryInformation;
import com.rapidesuite.client.common.util.Utils;

public class SnapshotController extends GenericController{

	private TabSnapshotsPanel tabSnapshotsPanel;
	private ControllerModalWindow controllerModalWindow;
	private List<SnapshotGridRecord> snapshotGridRecordList;
	private Map<Integer,TotalsSnapshotObject> tableIdToTotalsSnapshotObjectMap;
	private Set<Integer> tableIds;
	private boolean isFetchServerInfo;
	private Connection connection;
	
	public SnapshotController(TabSnapshotsPanel tabSnapshotsPanel,boolean isFetchServerInfo) {
		super();
		super.setSnapshotEnvironmentProperties(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel));
		String workersCountStr=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		int workersCount=Integer.valueOf(workersCountStr);
		super.setWorkersCount(workersCount);
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.isFetchServerInfo=isFetchServerInfo;
		tableIdToTotalsSnapshotObjectMap=new HashMap<Integer,TotalsSnapshotObject>();
		controllerModalWindow=new ControllerModalWindow(this,true);
		TOTAL_STEPS_COUNTER=11;
		controllerModalWindow.setTotalSteps(TOTAL_STEPS_COUNTER);
	}

	public Connection getJDBCConnection() throws ClassNotFoundException, SQLException {
		if (connection==null) {
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(getSnapshotEnvironmentProperties()),
					ModelUtils.getDBUserName(getSnapshotEnvironmentProperties()),
					ModelUtils.getDBPassword(getSnapshotEnvironmentProperties()),true
				);
		}
		return connection;
	}

	@Override
	public GenericWorker getImplementedWorker() {
		SnapshotTotalsWorker snapshotTotalsWorker=new SnapshotTotalsWorker(this);
		return snapshotTotalsWorker;
	}
	public void processGenericExceptionForAutomationMode(String specialMessage,Exception e) throws Exception{
		String errMsg = null;
		if(specialMessage!=null){
			errMsg = specialMessage;
		}else{
			errMsg = e.getMessage();
		}
		tabSnapshotsPanel.getMainPanel().getSnapshotMain().processGenericException(ModelUtils.removeHTMLTagsFromString(errMsg),e);
	}

	@Override
	public void preExecution() {
		boolean isAutomationMode = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode();
		try{
			ServerSelectionPanel serverSelectionPanel=tabSnapshotsPanel.getServerSelectionPanel();			
			try {
				ModelUtils.startModalWindowInThread(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),controllerModalWindow);
				String dbUserName=ModelUtils.getDBUserName(getSnapshotEnvironmentProperties());
				if (isFetchServerInfo) {
					serverSelectionPanel.setCurrentConnectionOracleRelease(null);
					controllerModalWindow.updateExecutionLabels("Connecting to the Database...",CURRENT_STEP_COUNTER++);
					
					try {				
						connection=getJDBCConnection();
						serverSelectionPanel.getConnectionStatusValueLabel().setForeground(Color.decode("#08bc08"));
						serverSelectionPanel.getConnectionStatusValueLabel().setText(SnapshotMain.SERVER_CONNECTED_STATUS);
						serverSelectionPanel.setConnected(true);
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
						serverSelectionPanel.getConnectionStatusValueLabel().setForeground(UIConstants.COLOR_RED);
						serverSelectionPanel.getConnectionStatusValueLabel().setText("Unable to connect!");
						serverSelectionPanel.setConnected(false);
						super.stopExecution();
						if(isAutomationMode){
							processGenericExceptionForAutomationMode(null,e);
						}else{
							GUIUtils.popupErrorMessage(e.getMessage());
							return;
						}
					}

					String currentConnectionOracleRelease=null;
					try {
						controllerModalWindow.updateExecutionLabels("Retrieving Database version...",CURRENT_STEP_COUNTER++);
						currentConnectionOracleRelease=DatabaseUtils.getEBSVersion(connection);
						serverSelectionPanel.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().validateOracleRelease(currentConnectionOracleRelease);
						serverSelectionPanel.setCurrentConnectionOracleRelease(currentConnectionOracleRelease);
						serverSelectionPanel.getServerVersionStatusValueLabel().setForeground(Color.decode("#08bc08"));
						serverSelectionPanel.getServerVersionStatusValueLabel().setText(currentConnectionOracleRelease);
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
						serverSelectionPanel.getServerVersionStatusValueLabel().setForeground(UIConstants.COLOR_RED);
						String errMsg = null;
						if (currentConnectionOracleRelease!=null) {
							errMsg = currentConnectionOracleRelease+". Unsupported Release!";
						}
						else {
							errMsg = "N/A. Invalid DB!";
						}
						serverSelectionPanel.getServerVersionStatusValueLabel().setText(errMsg);
						super.stopExecution();
						if(isAutomationMode){
							processGenericExceptionForAutomationMode(errMsg,e);
						}else{
							return;
						}
					}
					
					try {
						controllerModalWindow.updateExecutionLabels("Validating KB version...",CURRENT_STEP_COUNTER++);
						ModelUtils.validateKBVersion(tabSnapshotsPanel.getMainPanel().getSnapshotMain(),connection, dbUserName,currentConnectionOracleRelease,
								serverSelectionPanel.getSelectedServerConnection());
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
						super.stopExecution();
						if(isAutomationMode){
							processGenericExceptionForAutomationMode(null,e);
						}else{
							GUIUtils.popupErrorMessage(e.getMessage());
							return;
						}
					}
					
					try {
						controllerModalWindow.updateExecutionLabels("Verifying Software version...",CURRENT_STEP_COUNTER++);
						PatchUtils.verifySoftwareVersion(connection,dbUserName,controllerModalWindow);
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
						super.stopExecution();
						if(isAutomationMode){
							processGenericExceptionForAutomationMode(null,e);
						}else{
							GUIUtils.popupErrorMessage(e.getMessage());
							return;
						}
					}
					
					try {
						controllerModalWindow.updateExecutionLabels("Verifying Patches...",CURRENT_STEP_COUNTER++);
						PatchUtils.applyPatches(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),
								connection,dbUserName,controllerModalWindow,
								getSnapshotEnvironmentProperties());
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
						super.stopExecution();
						if(isAutomationMode){
							processGenericExceptionForAutomationMode(null,e);
						}else{
							GUIUtils.popupErrorMessage(e.getMessage());
							return;
						}
					}
					/*
					try {
						controllerModalWindow.updateExecutionLabels("Cleaning up Temporary Comparison tables...",CURRENT_STEP_COUNTER++);
						ModelUtils.dropComparisonTables(connection,dbUserName,controllerModalWindow);
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
						super.stopExecution();
						GUIUtils.popupErrorMessage(e.getMessage());
						return;
					}
					*/
					
					try {
						controllerModalWindow.updateExecutionLabels("Initializing Inventories...",CURRENT_STEP_COUNTER++);
						tabSnapshotsPanel.getSnapshotPackageSelectionPanel().initInventories(currentConnectionOracleRelease,controllerModalWindow);
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
						super.stopExecution();
						if(isAutomationMode){
							processGenericExceptionForAutomationMode(null,e);
						}else{
							GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
							return;
						}
					}

					serverSelectionPanel.postConnection();
					try{
						File plsqlPackageFile=serverSelectionPanel.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().getPLSQLPackageFile(currentConnectionOracleRelease);
						if (plsqlPackageFile==null) {
							throw new Exception("Internal Error (no package)!");
						}
						String sourcePLSQLPackageVersion=FileUtils.getRscPLSQLPackageSourceVersion(plsqlPackageFile);
						String plsqlPackageName=ModelUtils.getPLSQLPackageName(plsqlPackageFile);

						controllerModalWindow.updateExecutionLabels("Retrieving PLSQL package version on Database...",CURRENT_STEP_COUNTER++);
						String targetPLSQLPackageVersion=DatabaseUtils.getRscPLSQLPackageTargetVersion(connection,plsqlPackageName);
						if (targetPLSQLPackageVersion==null || !sourcePLSQLPackageVersion.equalsIgnoreCase(targetPLSQLPackageVersion)) {
							installPLSQLPackage(connection,plsqlPackageFile);
							if (!ModelUtils.isPackageValid(connection,dbUserName,plsqlPackageName) )
							{
								throw new Exception("Invalid package");
							}
							targetPLSQLPackageVersion=DatabaseUtils.getRscPLSQLPackageTargetVersion(connection,plsqlPackageName);
						}
						serverSelectionPanel.getPLSQLPackageVersionValueLabel().setForeground(Color.decode("#08bc08"));
						serverSelectionPanel.getPLSQLPackageVersionValueLabel().setText(targetPLSQLPackageVersion);
						serverSelectionPanel.setConnectedAndInstalledPLSQLCompletely(true);
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
						serverSelectionPanel.getPLSQLPackageVersionValueLabel().setForeground(UIConstants.COLOR_RED);
						serverSelectionPanel.getPLSQLPackageVersionValueLabel().setText(e.getMessage());
						serverSelectionPanel.setConnectedAndInstalledPLSQLCompletely(false);
						if(isAutomationMode){
							processGenericExceptionForAutomationMode(null,e);
						}
					}
					
					String oracleSeededUserNames=serverSelectionPanel.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getOracleSeededUserNames();
					int oracleUserCount=ModelUtils.getOracleUserCount(connection,oracleSeededUserNames,false);
					
					controllerModalWindow.updateExecutionLabels("",CURRENT_STEP_COUNTER++);
					
					Map<Integer, String> oracleSeededUserIdToUserNameMap=
							ModelUtils.getOracleUserIdToUserNameMap(connection,oracleSeededUserNames, true,controllerModalWindow,
							"Retrieving ","/ "+Utils.formatNumberWithComma(oracleUserCount)+" seeded users...");
					serverSelectionPanel.setOracleSeededUserIdToUserNameMap(oracleSeededUserIdToUserNameMap);
					
					controllerModalWindow.updateExecutionLabels("",CURRENT_STEP_COUNTER++);
					/*
					Map<Integer, String> oracleUserIdToUserNameMap=
							ModelUtils.getOracleUserIdToUserNameMap(connection,oracleSeededUserNames, false,controllerModalWindow,
							"Retrieving ","/ "+Utils.formatNumberWithComma(oracleUserCount)+" standard users...",true);
					serverSelectionPanel.setOracleUserIdToUserNameMap(oracleUserIdToUserNameMap);
					*/
				}
				else {
					connection=DatabaseUtils.getJDBCConnectionGeneric(
							ModelUtils.getJDBCString(getSnapshotEnvironmentProperties()),
							dbUserName,
							ModelUtils.getDBPassword(getSnapshotEnvironmentProperties()),false);
				}
				tableIds=ModelUtils.getTableIds(connection);
				this.snapshotGridRecordList=ModelUtils.getSnapshotGridRecords(connection);				
				List<SnapshotTotalsTask> snapshotTotalsTaskList=ModelUtils.getSnapshotTotalsTaskList(connection);
				super.setTasksList(snapshotTotalsTaskList);
			}
			catch(Exception e) {
				FileUtils.printStackTrace(e);
				super.stopExecution();
				if (connection!=null) connection.rollback();
				throw e;
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			if(isAutomationMode){
				try {
					processGenericExceptionForAutomationMode(null,e);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}else{
				GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
			}	
		}
	}

	@Override
	public void updateWhileExecution() {
		controllerModalWindow.updateExecutionLabels("Analyzing tables: "+super.getTotalCompletedTasks()+" / "+super.getTasksList().size()+" ...");
	}

	@Override
	public void postExecution() {
		try {
			int grandTotalRecords=0;
			int counter=0;
			controllerModalWindow.updateExecutionLabels("",CURRENT_STEP_COUNTER++);
			for (SnapshotGridRecord snapshotGridRecord:snapshotGridRecordList) {
				counter++;
				int snapshotId=snapshotGridRecord.getSnapshotId();
				controllerModalWindow.updateExecutionLabels("Computing Records count: "+counter+" / "+snapshotGridRecordList.size()+" ...");
				
				Iterator<Integer> iterator=tableIdToTotalsSnapshotObjectMap.keySet().iterator();
				int sumTotalDefaultRecords=0;
				int sumTotalAddedRecords=0;
				int sumTotalUpdatedRecords=0;
				while (iterator.hasNext()) {
					Integer tableId=iterator.next();
					TotalsSnapshotObject totalsSnapshotObject=tableIdToTotalsSnapshotObjectMap.get(tableId);
					if (totalsSnapshotObject==null) {
						continue;
					}
					Integer num=totalsSnapshotObject.getSnapshotIdToDefaultRecordsMap().get(snapshotId);
					if (num!=null) {
						sumTotalDefaultRecords=sumTotalDefaultRecords+num;
					}
					num=totalsSnapshotObject.getSnapshotIdToAddedRecordsMap().get(snapshotId);
					if (num!=null) {
						sumTotalAddedRecords=sumTotalAddedRecords+num;
					}
					num=totalsSnapshotObject.getSnapshotIdToUpdatedRecordsMap().get(snapshotId);
					if (num!=null) {
						sumTotalUpdatedRecords=sumTotalUpdatedRecords+num;
					}
				}
				grandTotalRecords=grandTotalRecords+snapshotGridRecord.getTotalRecords();
				snapshotGridRecord.setTotalDefaultRecords(sumTotalDefaultRecords);
				snapshotGridRecord.setTotalAddedRecords(sumTotalAddedRecords);
				snapshotGridRecord.setTotalUpdatedRecords(sumTotalUpdatedRecords);
			}
			
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
			Map<Integer, UserInformation> userIdToUserInformationMap=ModelUtils.getUserIdToUserInformationMap(userInformationList);
			
			tabSnapshotsPanel.getSnapshotsGridPanel().displaySnapshots(snapshotGridRecordList,userIdToUserInformationMap);
			updateMemoryBackground(grandTotalRecords);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			boolean isAutomationMode = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode();
			if(isAutomationMode){
				try {
					tabSnapshotsPanel.getMainPanel().getSnapshotMain().processGenericException(ModelUtils.removeHTMLTagsFromString(e.getMessage()),e);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}else{
				GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
			}
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}
		
	private void updateMemoryBackground(final int grandTotalRecords) {
		 new Thread() {
			    @Override 
			    public void run() {
			    	Connection connection=null;
			    	MemoryInformation memoryInformation=null;
			    	try {
			    		String dbUserName=ModelUtils.getDBUserName(getSnapshotEnvironmentProperties());				
			    		
			    		connection=DatabaseUtils.getJDBCConnectionGeneric(
			    				ModelUtils.getJDBCString(getSnapshotEnvironmentProperties()),
			    				ModelUtils.getDBUserName(getSnapshotEnvironmentProperties()),
			    				ModelUtils.getDBPassword(getSnapshotEnvironmentProperties()),false
			    				);
			    		memoryInformation = ModelUtils.getMemoryInformation(connection,dbUserName);
			    	} 
			    	catch (Exception e) {
			    		FileUtils.printStackTrace(e);
			    	}
			    	finally{
			    		DirectConnectDao.closeQuietly(connection);
			    	}
			    	tabSnapshotsPanel.getSnapshotsGridPanel().displayTotals(grandTotalRecords,memoryInformation);
			    }
		 }.start();
	}

	public List<SnapshotGridRecord> getSnapshotGridRecordList() {
		return snapshotGridRecordList;
	}

	public Map<Integer, TotalsSnapshotObject> getTableIdToTotalsSnapshotObjectMap() {
		return tableIdToTotalsSnapshotObjectMap;
	}

	private void installPLSQLPackage(Connection connection,File packageFile) throws Exception {
		controllerModalWindow.updateExecutionLabels("Retrieving PL/SQL commands...</body></HTML>");
		List<String> statements=FileUtils.readContentsFromPLSQLFile(packageFile);
		for (int i=0;i<statements.size();i++)
		{
			String sql=statements.get(i);
			controllerModalWindow.updateExecutionLabels("Installing PL/SQL package: "+i+" / "+statements.size()+" commands.");
			ModelUtils.executeStatement(connection,sql);
		}
		connection.commit();
		controllerModalWindow.updateExecutionLabels("PL/SQL package installed.");
	}

	public Set<Integer> getTableIds() {
		return tableIds;
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

}

