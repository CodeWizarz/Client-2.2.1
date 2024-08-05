package com.rapidesuite.snapshot.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.controller.SnapshotCreationCommonController;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class SnapshotCreationWorker extends GenericWorker{

	public SnapshotCreationWorker(SnapshotCreationCommonController snapshotCreationCommonController) {
		super(snapshotCreationCommonController,false);
	}
		
	@Override
	public void execute(Object task) {
		SnapshotInventoryGridRecord snapshotInventoryGridRecord=(SnapshotInventoryGridRecord)task;
		SnapshotCreationCommonController snapshotCreationCommonController=(SnapshotCreationCommonController)super.getGenericController();
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Integer tableId=snapshotCreationCommonController.getInventoryNameToTableIdMap().get(snapshotInventoryGridRecord.getInventoryName());
		String connectionOracleRelease=snapshotCreationCommonController.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
		try {
			snapshotInventoryGridRecord.setStartTime(System.currentTimeMillis());
			snapshotInventoryGridRecord.setTotalRecords(0);
			
			int snapshotId=snapshotCreationCommonController.getSnapshotGridRecord().getSnapshotId();
			
			if (snapshotInventoryGridRecord.getStatus()!=null && snapshotInventoryGridRecord.getStatus().equalsIgnoreCase(UIConstants.UI_STATUS_UNSELECTED)) {
				return;
			}
			
			if (snapshotCreationCommonController.isExecutionStopped()) {
				snapshotInventoryGridRecord.setStatus(UIConstants.UI_STATUS_CANCELLED);
				ModelUtils.updateInventoryToSnapshot(getJDBCConnection(),tableId,snapshotId,ModelUtils.DB_STATUS_CANCELLED,"","",0,0);
				return;
			}
			Date createdOn = new Date();
			String createdDateToParse = format.format(createdOn);
			snapshotInventoryGridRecord.setCreatedOn(createdDateToParse);			
			snapshotInventoryGridRecord.setStatus(UIConstants.UI_STATUS_PROCESSING);
			ModelUtils.updateInventoryToSnapshot(getJDBCConnection(),tableId,snapshotId,ModelUtils.DB_STATUS_PROCESSING,"","",0,0,createdDateToParse);
						
			File inventoryFile=snapshotCreationCommonController.getTabSnapshotsPanel().
					getSnapshotPackageSelectionPanel().getInventoryNameToInventoryFileMap(connectionOracleRelease).get(
					snapshotInventoryGridRecord.getInventoryName());
			if (inventoryFile==null) {
				throw new Exception("Unable to find the inventory file: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			}

			File reverseSQLFile=snapshotCreationCommonController.getTabSnapshotsPanel().
					getSnapshotPackageSelectionPanel().getInventoryNameToReverseSQLFileMap(connectionOracleRelease).get(
					snapshotInventoryGridRecord.getInventoryName());
			if (reverseSQLFile==null) {
				throw new SnapshotWarningException("Unsupported (SQL not available)");
			}
			String formType=snapshotInventoryGridRecord.getFormInformation().getFormType();
			if (formType==null || formType.isEmpty()) {
				throw new Exception("Missing Form Level");
			}
			if (formType.equals(UIConstants.UI_NA)) {
				throw new Exception("Invalid Form Level");
			}
			Inventory inventory=FileUtils.getInventory(inventoryFile,snapshotInventoryGridRecord.getInventoryName());
			boolean isTableAlreadyCreated=snapshotCreationCommonController.getTableIds().contains(tableId);
			File plsqlPackageFile=snapshotCreationCommonController.getTabSnapshotsPanel().
					getSnapshotPackageSelectionPanel().getPLSQLPackageFile(connectionOracleRelease);
			
			List<File> alternateReverseSQLFileList=snapshotCreationCommonController.getTabSnapshotsPanel().
					getSnapshotPackageSelectionPanel().getInventoryNameToAlternateReverseSQLFilesMap(connectionOracleRelease).get(
					snapshotInventoryGridRecord.getInventoryName());
			boolean isSnapshotForConversion=snapshotCreationCommonController.isSnapshotForConversionSelected();
			boolean isAutomationMode = snapshotCreationCommonController.getTabSnapshotsPanel().getMainPanel().getSnapshotMain().isAutomationMode();
			int timeLimit = 0;
			if(isAutomationMode){
				timeLimit = snapshotCreationCommonController.getTabSnapshotsPanel().getMainPanel().getSnapshotMain().getSnapshotArgumentsDocument()
				.getSnapshotArguments().getTimeLimit();
			}
			
			ModelUtils.executeSnapshotInventory(
					snapshotCreationCommonController.getTabSnapshotsPanel().getMainPanel().getSnapshotMain(),
					ModelUtils.getDBUserName(snapshotCreationCommonController.getSnapshotEnvironmentProperties()),
					plsqlPackageFile,
					this,
					inventory,
					tableId,
					isTableAlreadyCreated,
					snapshotId,
					reverseSQLFile,
					snapshotInventoryGridRecord,
					alternateReverseSQLFileList,
					isSnapshotForConversion,
					timeLimit
					);			

			snapshotInventoryGridRecord.setTableId(tableId);
						
			int totalRecords= ModelUtils.getTotalRecordsCount(getJDBCConnection(),snapshotId,tableId,null);
			snapshotInventoryGridRecord.setTotalRecords(totalRecords);

			if (snapshotCreationCommonController.isShowTotalDetails()) {
				int totalDefaultRecords= ModelUtils.getTotalDefaultRecordsCount(getJDBCConnection(),snapshotId,tableId,
						snapshotCreationCommonController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
				int totalAddedRecords= ModelUtils.getTotalAddedRecordsCount(getJDBCConnection(),snapshotId,tableId,
						snapshotCreationCommonController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
				int totalUpdatedRecords= ModelUtils.getTotalUpdatedRecordsCount(getJDBCConnection(),snapshotId,tableId,
						snapshotCreationCommonController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());

				snapshotInventoryGridRecord.setTotalAddedRecords(totalAddedRecords);
				snapshotInventoryGridRecord.setTotalDefaultRecords(totalDefaultRecords);
				snapshotInventoryGridRecord.setTotalUpdatedRecords(totalUpdatedRecords);
			}
			
			// validate accross all data that there are no duplicates - otherwise the primary keys are wrong
			if (Config.getSnapshotShowWarningDuplicateData()) {

				String reverseFileContent=FileUtils.readContentsFromSQLFile(reverseSQLFile);
				if (reverseFileContent.indexOf("IGNORE_DUPLICATE_DATA_VALIDATION")==-1) {
					List<Integer> primaryKeysPositionList=ModelUtils.getPrimaryKeysPositionList(inventory);
					if (primaryKeysPositionList==null || primaryKeysPositionList.isEmpty()) {
						throw new SnapshotWarningException("This Inventory has no Primary Keys defined!");
					}
					StringBuffer snapshotWhereClause=new StringBuffer("");
					snapshotWhereClause.append(snapshotId);
					String dbUserName=ModelUtils.getDBUserName(genericController.getSnapshotEnvironmentProperties());
					ModelUtils.validateDuplicateData(dbUserName,inventory,					
							getJDBCConnection(),snapshotWhereClause.toString(),tableId,primaryKeysPositionList);
				}
			}

			UIUtils.setTime(snapshotInventoryGridRecord);
			Date completedOn = new Date();
			String completedOnDateToParse = format.format(completedOn);
			snapshotInventoryGridRecord.setCompletedOn(completedOnDateToParse);			
			ModelUtils.updateInventoryToSnapshot(getJDBCConnection(),tableId,
					snapshotId,ModelUtils.DB_STATUS_SUCCESS,"",
						snapshotInventoryGridRecord.getExecutionTime(),snapshotInventoryGridRecord.getRawTimeInSecs(),totalRecords,completedOnDateToParse);
			snapshotInventoryGridRecord.setStatus(UIConstants.UI_STATUS_COMPLETED);
		}
		catch (SnapshotWarningException e) {
			processException(e,snapshotCreationCommonController,snapshotInventoryGridRecord,tableId,ModelUtils.DB_STATUS_WARNING);
		}
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
			processException(e,snapshotCreationCommonController,snapshotInventoryGridRecord,tableId,ModelUtils.DB_STATUS_FAILED);
		}
	}
	
	private void processException(Throwable e,SnapshotCreationCommonController snapshotCreationCommonController,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord,Integer tableId,String dbStatus) {
		UIUtils.setTime(snapshotInventoryGridRecord);
		try {
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			Date completedOn = new Date();
			String completedOnDateToParse = format.format(completedOn);
			snapshotInventoryGridRecord.setCompletedOn(completedOnDateToParse);
			ModelUtils.updateInventoryToSnapshot(getJDBCConnection(),tableId,
					snapshotCreationCommonController.getSnapshotGridRecord().getSnapshotId(),
					dbStatus,e.getMessage(),snapshotInventoryGridRecord.getExecutionTime(),snapshotInventoryGridRecord.getRawTimeInSecs(),snapshotInventoryGridRecord.getTotalRecords(),completedOnDateToParse);
		}
		catch (Throwable ex) {
			FileUtils.printStackTrace(ex);
		}
		snapshotInventoryGridRecord.setRemarks(e.getMessage());
		String uiStatus=ModelUtils.getUIStatusFromDBStatus(dbStatus);
		snapshotInventoryGridRecord.setStatus(uiStatus);
	}
	
}
