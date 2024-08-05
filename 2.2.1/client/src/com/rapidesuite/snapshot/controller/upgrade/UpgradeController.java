package com.rapidesuite.snapshot.controller.upgrade;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.sql.SQLRecoverableException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.dataConversionShared0000.ConstantType;
import com.rapidesuite.client.dataConversionShared0000.ConstantsType;
import com.rapidesuite.client.dataConversionShared0000.DataConversionShared;
import com.rapidesuite.client.dataConversionShared0000.DataConversionSharedDocument;
import com.rapidesuite.client.dataConversionShared0000.ValueMapType;
import com.rapidesuite.client.dataConversionShared0000.ValuesMapType;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.controller.GenericController;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotViewerDownloadWorker;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapidesuite.snapshot.view.upgrade.FusionInventoryRow;
import com.rapidesuite.snapshot.view.upgrade.TabUpgradeMainPanel;
import com.rapidesuite.snapshot.view.upgrade.UpgradeFrame;

public class UpgradeController extends GenericController{

	private List<FusionInventoryRow> selectedFusionInventoryRowList;
	private List<FusionInventoryRow> fusionInventoryRowToRefreshList;
	private UpgradeFrame upgradeFrame;
	private File ebsDataXMLDownloadFolder;
	private File convertedFolder;
	private Map<String,ValueMapType[]> sharedMappingNameToValueMapArrayMap;
	private Map<String,String> constantNameToValueMap;
	public static int TARGET_FILES_DOWNLOAD_SEQUENCE_NUMBER=1;
	private Map<String, Set<String>> targetInventoryNameToMandatoryColumnNamesToIgnoreMap;
	private int sequenceNumber;
	private final Object lockInt = new Object();
	private final Object lockTemp = new Object();
	private Map<String, SnapshotInventoryGridRecord> ebsInventoryNameToSnapshotInventoryGridRecordMap;
	private Map<String, File> ebsInventoryNameToDataFileMap;
	private Set<String> fusionInventoryNameInProcessSet;
	
	private boolean isDownloadDataChangesOnly;
	
	public UpgradeController(UpgradeFrame upgradeFrame,int workerCount, List<SnapshotInventoryGridRecord> selectedSnapshotInventoryGridRecordsList) {
		this(upgradeFrame, workerCount, selectedSnapshotInventoryGridRecordsList, true);
	}

	public UpgradeController(UpgradeFrame upgradeFrame,int workerCount, List<SnapshotInventoryGridRecord> selectedSnapshotInventoryGridRecordsList, boolean isDownloadDataChangesOnly) {
		super.setSnapshotEnvironmentProperties(ModelUtils.getSnapshotEnvironmentProperties(upgradeFrame.getTabSnapshotsPanel()));
		super.setWorkersCount(workerCount);
		this.upgradeFrame=upgradeFrame;
		sharedMappingNameToValueMapArrayMap=new HashMap<String,ValueMapType[]>();
		constantNameToValueMap=new HashMap<String,String>();
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		Date now = new Date();
		String strDate = format.format(now);		
		ebsDataXMLDownloadFolder=new File(upgradeFrame.getRootDownloadFolder(),"upgrade-ebs-"+strDate);
		ebsDataXMLDownloadFolder.mkdirs();
		targetInventoryNameToMandatoryColumnNamesToIgnoreMap=new HashMap<String,Set<String>>();
		ebsInventoryNameToDataFileMap=new HashMap<String,File>();
		fusionInventoryNameInProcessSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		ebsInventoryNameToSnapshotInventoryGridRecordMap=new HashMap<String,SnapshotInventoryGridRecord>();
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedSnapshotInventoryGridRecordsList) {
			ebsInventoryNameToSnapshotInventoryGridRecordMap.put(snapshotInventoryGridRecord.getInventoryName(),snapshotInventoryGridRecord);
		}
		this.isDownloadDataChangesOnly = isDownloadDataChangesOnly;
	}
	
	public boolean hasFusionInventoryTaskStartedAndStoreIfSo(String name) {
		synchronized(lockTemp) {
			boolean hasStarted=fusionInventoryNameInProcessSet.contains(name);
			if (!hasStarted) {
				fusionInventoryNameInProcessSet.add(name);
			}
			return hasStarted;
		}
	}

	public Map<String, Set<String>> getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap() {
		return targetInventoryNameToMandatoryColumnNamesToIgnoreMap;
	}

	@Override
	public GenericWorker getImplementedWorker() {
		UpgradeWorker upgradeWorker=new UpgradeWorker(this);
		return upgradeWorker;
	}	

	@Override
	public void preExecution() {
		try{
			String msg=Utils.getExecutionTime(super.getStartTime(),System.currentTimeMillis());
			upgradeFrame.getTabUpgradeMainPanel().getFusionExecutionTimeLabel().setText("Execution time: "+msg);

			selectedFusionInventoryRowList=upgradeFrame.getTabUpgradeMainPanel().getFusionInventoryGridPanel().getSelectedFusionInventoryRowList();
			super.setTasksList(selectedFusionInventoryRowList);
			fusionInventoryRowToRefreshList = new ArrayList<FusionInventoryRow>(selectedFusionInventoryRowList);
			for (FusionInventoryRow fusionInventoryRow:selectedFusionInventoryRowList) {
				fusionInventoryRow.setStatus(UIConstants.UI_STATUS_PENDING);
			}

			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
			Date now = new Date();
			String strDate = format.format(now);
			convertedFolder=new File(upgradeFrame.getRootDownloadFolder(),"upgrade-fusion-"+strDate);
			convertedFolder.mkdirs();

			String sharedFileName="value_maps.xml";
			File mappingFolder=upgradeFrame.getTabUpgradeMainPanel().getSelectedFusionMappingFolder();
			File sharedMappingsFolder=new File(mappingFolder,"SHARED");
			File sharedMappingsFile=new File(sharedMappingsFolder,sharedFileName);
			if (sharedMappingsFile.exists()) {
				DataConversionSharedDocument dataConversionSharedDocument=ModelUtils.getDataConversionSharedDocument(sharedMappingsFile);
				DataConversionShared dataConversionSharedType=dataConversionSharedDocument.getDataConversionShared();
				ValuesMapType[] valuesMapTypeArray=dataConversionSharedType.getValuesMapArray();
				if (valuesMapTypeArray!=null) {
					for (ValuesMapType valuesMapType:valuesMapTypeArray) {
						String name=valuesMapType.getName();
						if (sharedMappingNameToValueMapArrayMap.get(name)!=null) {
							throw new Exception("Invalid Shared mapping file '"+sharedFileName+"' - the value map name: '"+name+"' already exists!");
						}
						ValueMapType[] valueMapArray=valuesMapType.getValueMapArray();
						sharedMappingNameToValueMapArrayMap.put(name, valueMapArray);
					}
				}
			}

			String constantsFileName="constants.xml";
			File constantsFile=new File(sharedMappingsFolder,constantsFileName);
			if (constantsFile.exists()) {
				DataConversionSharedDocument dataConversionSharedDocument=ModelUtils.getDataConversionSharedDocument(constantsFile);
				DataConversionShared dataConversionSharedType=dataConversionSharedDocument.getDataConversionShared();
				ConstantsType constantsType=dataConversionSharedType.getConstants();
				if (constantsType!=null) {
					ConstantType[] constantsTypeArray=constantsType.getConstantArray();
					for (ConstantType constantType:constantsTypeArray) {
						String name=constantType.getName();
						if (sharedMappingNameToValueMapArrayMap.get(name)!=null) {
							throw new Exception("Invalid Shared Constants file '"+constantsFileName+"' - the constant name: '"+name+"' already exists!");
						}
						String value=constantType.getValue();
						constantNameToValueMap.put(name, value);
					}
				}
			}			
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}
	}

	@Override
	public void updateWhileExecution() {
		try{
			refreshGrid();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void postExecution() {
		upgradeFrame.getTabUpgradeMainPanel().unlockUI();
		upgradeFrame.getTabUpgradeMainPanel().setAutoRefreshFilteringOn(false);
		updateUI();

		File parentFolder=convertedFolder.getParentFile();
		File zippedFile=new File(parentFolder,convertedFolder.getName()+".zip");
		try {
			//upgradeFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"Please wait compressing Download Folder...");
			FileUtils.zipFolder(convertedFolder, zippedFile);
			if (this.isExecutionStopped()) {
				//convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"Cancelled");
			}
			else {
				//convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"Completed!");
			}
			upgradeFrame.getTabUpgradeMainPanel().setConvertedFolderLabel(convertedFolder);
			FileUtils.deleteDirectory(ebsDataXMLDownloadFolder);

			GUIUtils.popupInformationMessage("<html><body>Conversion completed!<br/><br/>"+
					"Please click on the 'Output Folder' link to browse the converted files.");
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			//convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"Failed ("+e.getMessage()+")");
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}		
	}

	public void updateUI() {
		String msg=Utils.getExecutionTime(super.getStartTime(),System.currentTimeMillis());
		//convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"In Progress");

		upgradeFrame.getTabUpgradeMainPanel().getFusionExecutionTimeLabel().setText(TabUpgradeMainPanel.FUSION_EXEC_TIME+msg);
		upgradeFrame.getTabUpgradeMainPanel().getFusionExecutionStatusLabel().setText(TabUpgradeMainPanel.FUSION_EXEC_STATUS+super.getTotalCompletedTasks()+" / "+super.getTotalTasks());

		int totalFailedTasks=super.getTotalFailedTasks();
		if (totalFailedTasks>0) {
			upgradeFrame.getTabUpgradeMainPanel().getFusionExecutionFailedRowsCountLabelValue().setForeground(Color.red);
			upgradeFrame.getTabUpgradeMainPanel().getFusionExecutionFailedRowsCountLabelValue().setBackground(Color.white);
			upgradeFrame.getTabUpgradeMainPanel().getFusionExecutionFailedRowsCountLabelValue().setOpaque(true);
			upgradeFrame.getTabUpgradeMainPanel().getFusionExecutionFailedRowsCountLabelValue().setFont(new Font ("Arial", Font.BOLD, 14));
		}
	}

	protected void refreshGrid() throws Exception {
		updateUI();

		List<FusionInventoryRow> revisedListToRefresh=new ArrayList<FusionInventoryRow>();
		List<FusionInventoryRow> itemsToUpdateInThisCycleList=new ArrayList<FusionInventoryRow>();
		for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowToRefreshList) {
			String status=fusionInventoryRow.getStatus();
			if (status.equals(UIConstants.UI_STATUS_PENDING) ) {
				revisedListToRefresh.add(fusionInventoryRow);
				itemsToUpdateInThisCycleList.add(fusionInventoryRow);
			}
			else
				if ( status.equals(UIConstants.UI_STATUS_UNSELECTED)) {
					itemsToUpdateInThisCycleList.add(fusionInventoryRow);
				}
				else
					if ( status.equals(UIConstants.UI_STATUS_FAILED) || status.equals(UIConstants.UI_STATUS_COMPLETED) 
							|| status.equals(UIConstants.UI_STATUS_WARNING) || status.equals(UIConstants.UI_STATUS_CANCELLED)) {
						itemsToUpdateInThisCycleList.add(fusionInventoryRow);
					}
					else
						if ( status.equals(UIConstants.UI_STATUS_PROCESSING)) {
							revisedListToRefresh.add(fusionInventoryRow);
							itemsToUpdateInThisCycleList.add(fusionInventoryRow);
							setTime(fusionInventoryRow);
							fusionInventoryRow.setRawTimeInSecs(UIUtils.getRawTimeInSecs(fusionInventoryRow.getStartTime()));
						}
		}
		fusionInventoryRowToRefreshList=revisedListToRefresh;

		upgradeFrame.getTabUpgradeMainPanel().getFusionInventoryGridPanel().refreshGrid(itemsToUpdateInThisCycleList);
	}

	public static void setTime(FusionInventoryRow fusionInventoryRow) {
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(fusionInventoryRow.getStartTime(),currentTime);
		fusionInventoryRow.setExecutionTime(msg);
		fusionInventoryRow.setRawTimeInSecs(UIUtils.getRawTimeInSecs(fusionInventoryRow.getStartTime()));
	}

	public File getConvertedFolder() {
		return convertedFolder;
	}
	
	public int getNextSequenceNumber() {
		synchronized(lockInt) {
			sequenceNumber++;
			return sequenceNumber;
		}
	}

	public ValueMapType[] getSharedMappings(String sharedMapName) {
		return sharedMappingNameToValueMapArrayMap.get(sharedMapName);
	}

	public Map<String, String> getConstantNameToValueMap() {
		return constantNameToValueMap;
	}

	public File getSourceXMLDownloadFolder() {
		return ebsDataXMLDownloadFolder;
	}

	public UpgradeFrame getUpgradeFrame() {
		return upgradeFrame;
	}

	public boolean isEBSInventorySelected(String inventoryName) {
		return ebsInventoryNameToSnapshotInventoryGridRecordMap.get(inventoryName)!=null;
	}

	public List<String[]> downloadEBSInventoryData(UpgradeWorker upgradeWorker,SnapshotInventoryGridRecord snapshotInventoryGridRecord,
			FusionInventoryRow fusionInventoryRow,SnapshotGridRecord snapshotGridRecord) throws Exception {
		Inventory sourceInventory=snapshotInventoryGridRecord.getInventory();
		StringBuffer additionalWhereClause=snapshotInventoryGridRecord.getWhereClauseFilter();
		int tableId=snapshotInventoryGridRecord.getTableId();

		StringBuffer sqlQuery=SnapshotViewerDownloadWorker.getSqlQuery(additionalWhereClause,tableId,snapshotGridRecord,
				isDownloadDataChangesOnly,
				upgradeFrame.getOracleSeededUserIdToUserNameMap(),false);
		
		String currentConnectionOracleRelease=upgradeFrame.getTabSnapshotsPanel().getMainPanel().getTabSnapshotsPanel().
				getServerSelectionPanel().getCurrentConnectionOracleRelease();
		Map<String, File> inventoryNameToReverseSQLFileMap=upgradeFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().getInventoryNameToReverseSQLFileMap(
				currentConnectionOracleRelease);
		//FileUtils.println("inventoryNameToReverseSQLFileMap:"+inventoryNameToReverseSQLFileMap+" sourceInventory:"+sourceInventory);
		File sqlFile=inventoryNameToReverseSQLFileMap.get(sourceInventory.getName());
		if (sqlFile==null) {
			throw new Exception("cannot find SQL file");
		}
		String sqlFileContent=FileUtils.readContentsFromSQLFile(sqlFile);
		String dffStructureQuery=ModelUtils.getDFFStructureQuery(sqlFileContent);

		boolean isConvertDFF=dffStructureQuery!=null;
		String message = "";
		File outputFile=new File(ebsDataXMLDownloadFolder,sourceInventory.getName()+"."+getNextSequenceNumber()+".xml");
		
		for(int i = 1; i <= DatabaseUtils.CONNECTION_RETRY_COUNT; i++) {
			try {
				ModelUtils.exportTableToXMLGeneric(upgradeWorker.getJDBCConnectionNoRetry(),outputFile,
						sourceInventory,snapshotGridRecord,sqlQuery.toString(),snapshotInventoryGridRecord,isConvertDFF,true,false);
			}
			catch (SQLRecoverableException sqlRecoverableException) {
				// show the sleeping remark.
				if(i < DatabaseUtils.CONNECTION_RETRY_COUNT) {
					message = "Connection lost. Retries: " + i + " / " + DatabaseUtils.CONNECTION_RETRY_COUNT+
							". Waiting for " + DatabaseUtils.CONNECTION_RETRY_WAIT_SECONDS + " seconds before retry.";
					fusionInventoryRow.setRemarks(message);
					try {
						Thread.sleep(DatabaseUtils.CONNECTION_RETRY_WAIT_SECONDS * 1000);
					} catch (InterruptedException e) {
						FileUtils.printStackTrace(e);
					}
				} else {
					// show the permanently failed remark
					message = "Connection lost. "+DatabaseUtils.CONNECTION_RETRY_COUNT+" retries exhausted!";
					fusionInventoryRow.setRemarks(message);

					throw sqlRecoverableException;
				}
				continue;
			}
			// no need to show the remark if it was completed in the first try.
			if(i > 1) {
				// show the completed after x tries remark
				message = "Operation completed after " + i + " retries.";
				fusionInventoryRow.setRemarks(message);
			}

			break;
		}
		if (!ebsInventoryNameToDataFileMap.containsKey(sourceInventory.getName())) {
			ebsInventoryNameToDataFileMap.put(sourceInventory.getName(), outputFile);
		}
		List<String[]> dataRows=InjectUtils.parseXMLDataFile(outputFile);
		/*
		for (String[] row:dataRows) {
			int index=0;
			System.out.println("ROW!!!!!");
			for (String val:row) {
				System.out.println("index:"+(index++)+" val:"+val);
			}
		}
		*/
		return dataRows;
	}

	public Map<String, SnapshotInventoryGridRecord> getEbsInventoryNameToSnapshotInventoryGridRecordMap() {
		return ebsInventoryNameToSnapshotInventoryGridRecordMap;
	}

	public Map<String, File> getEbsInventoryNameToDataFileMap() {
		return ebsInventoryNameToDataFileMap;
	}

}