package com.rapidesuite.snapshot.controller.upgrade;

import java.io.File;
import java.sql.SQLRecoverableException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.model.ExcelBR100Report;
import com.rapidesuite.snapshot.model.ExcelReportTemplate;
import com.rapidesuite.snapshot.model.ExcelTab;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapidesuite.snapshot.view.upgrade.UpgradeMappingAnalyzer;

public class UpgradeMigrationReportWorker extends GenericWorker {
	
	private int totalDownloadedRecords;
	
	public UpgradeMigrationReportWorker(UpgradeMigrationReportController upgradeMigrationReportController) {
		super(upgradeMigrationReportController,true);
	}

	@Override
	public void execute(Object task) {
		SnapshotInventoryGridRecord snapshotInventoryGridRecord=(SnapshotInventoryGridRecord)task;
		UpgradeMigrationReportController upgradeMigrationReportController=(UpgradeMigrationReportController)super.getGenericController();
		try {
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_PROCESSING);
			snapshotInventoryGridRecord.setDownloadStartTime(System.currentTimeMillis());
			
			String currentConnectionOracleRelease=upgradeMigrationReportController.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
			File inventoryFile=upgradeMigrationReportController.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToInventoryFileMap(currentConnectionOracleRelease).get(snapshotInventoryGridRecord.getInventoryName());
			if (inventoryFile==null) {
				throw new Exception("Unable to find the inventory file: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			}
			
			Inventory inventory=FileUtils.getInventory(inventoryFile,snapshotInventoryGridRecord.getInventoryName());
			StringBuffer additionalWhereClause=snapshotInventoryGridRecord.getWhereClauseFilter();
			snapshotInventoryGridRecord.setInventory(inventory);
			
			StringBuffer sqlQueryBuffer=getSqlQuery(additionalWhereClause,snapshotInventoryGridRecord.getTableId(),
					upgradeMigrationReportController.getSnapshotGridRecord(),
					upgradeMigrationReportController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());

			String message = "";
			int tableRowsCount=0;
			for(int i = 1; i <= DatabaseUtils.CONNECTION_RETRY_COUNT; i++) {
				try {
					tableRowsCount=executeReportGenerationOperation(upgradeMigrationReportController,
								snapshotInventoryGridRecord,sqlQueryBuffer, upgradeMigrationReportController.getSnapshotGridRecord());									
				}
				catch (SQLRecoverableException sqlRecoverableException) {
					if(!upgradeMigrationReportController.isCancelled()) {
						if(i < DatabaseUtils.CONNECTION_RETRY_COUNT) {
							// show the sleeping remark.
							message = "Connection lost. Retries: " + i + " / " + DatabaseUtils.CONNECTION_RETRY_COUNT+
									". Waiting for " + DatabaseUtils.CONNECTION_RETRY_WAIT_SECONDS + " seconds before retry.";
							snapshotInventoryGridRecord.setDownloadRemarks(message);
							try {
								Thread.sleep(DatabaseUtils.CONNECTION_RETRY_WAIT_SECONDS * 1000);
							} catch (InterruptedException e) {
								FileUtils.printStackTrace(e);
							}
						} else {
							// show the permanently failed remark
							message = "Connection lost. "+DatabaseUtils.CONNECTION_RETRY_COUNT+" retries exhausted!";
							snapshotInventoryGridRecord.setDownloadRemarks(message);
							
							throw sqlRecoverableException;
						}
						continue;
					}
				}
				// no need to show the remark if it was completed in the first try.
				if(i > 1) {
					// show the completed after x tries remark
					message = "Operation completed after " + i + " retries.";
					snapshotInventoryGridRecord.setDownloadRemarks(message);
				}
				
				break;
			}
			
			totalDownloadedRecords=totalDownloadedRecords+tableRowsCount;
			if(upgradeMigrationReportController.isCancelled()) {
				snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_CANCELLED);
			} else {
				snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_COMPLETED);
			}
		}
		catch(Exception e) {
			FileUtils.println("Error for inventory: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			FileUtils.printStackTrace(e);
			
			ExcelBR100Report excelBR100Report=upgradeMigrationReportController.getExcelBR100Report();
			ExcelReportTemplate excelReportTemplate=excelBR100Report.getExcelReportTemplate();
			ExcelTab excelTab=excelReportTemplate.getDataTab(snapshotInventoryGridRecord.getInventoryName());
			excelTab.setError(true);
			
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_FAILED);
			snapshotInventoryGridRecord.setDownloadRemarks(e.getMessage());
			super.setTotalFailedTasks(super.getTotalFailedTasks()+1);
		}
		finally  {
			UIUtils.setDownloadTime(snapshotInventoryGridRecord);
		}
	}

	/*
	 * Logic:
	 *  1. Create 1 XML file for each Inventory (in parallel in the workers) - this XML is excel xlsx format but without the header and footer
	 *     Each XML will be one Excel tab.
	 *  2. POST Execution: The controller will create the workbook template which is a zip file. Note that the first Excel tab
	 *     will be a TOC with hyperlinks to other tabs.
	 *  3. Create a new XLXS archive by copying every entry in the template archive but replacing the 
	 *     entries related to the Excel tabs by the actual generated XML files.
	 */
	private int executeReportGenerationOperation(UpgradeMigrationReportController upgradeMigrationReportController,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord,StringBuffer sqlQueryBuffer, SnapshotGridRecord snapshotGridRecord) throws Exception {	
		int tableRowsCount=ModelUtils.getTotalRowsCount(getJDBCConnectionNoRetry(false),sqlQueryBuffer.toString());
		snapshotInventoryGridRecord.setDownloadTotalRecordsCount(tableRowsCount);
		if (tableRowsCount==0) {
			return 0;
		}
		Set<String> unsupportedInventoryFieldNames=new HashSet<String>();
		Set<String> supportedInventoryFieldNames = new HashSet<String>();

		String ebsInventoryName=snapshotInventoryGridRecord.getInventory().getName();
		Set<File> mappingFiles=upgradeMigrationReportController.getEbsInventoryNameToFusionMappingFileMap().get(ebsInventoryName);
		if (mappingFiles!=null) {
			Iterator<File> iteratorMap=mappingFiles.iterator();
			while (iteratorMap.hasNext()) {
				File mappingFile=iteratorMap.next();

				UpgradeMappingAnalyzer ugradeMappingAnalyzer=new UpgradeMappingAnalyzer();
				ugradeMappingAnalyzer.process(ebsInventoryName, mappingFile);
				supportedInventoryFieldNames.addAll(ugradeMappingAnalyzer.getColumnNamesInUseSet());
			}
		}
		Set<String> genericColumnNamesSupportedList=new HashSet<String>();
		genericColumnNamesSupportedList.add("[]");
		genericColumnNamesSupportedList.add("[ ]");
		genericColumnNamesSupportedList.add("( )");
		List<String> dataInventoryFields=snapshotInventoryGridRecord.getInventory().getFieldNamesUsedForDataEntry();
		for (String fieldName:dataInventoryFields) {
			if (!supportedInventoryFieldNames.contains(fieldName) && !genericColumnNamesSupportedList.contains(fieldName)) {
				unsupportedInventoryFieldNames.add(fieldName);
			}
		}
		ExcelBR100Report excelBR100Report=upgradeMigrationReportController.getExcelBR100Report();
		int generatedRowsCount=UpgradeMigrationReportController.generateExcelXMLDataFileForUpgradeReport(excelBR100Report,
				snapshotInventoryGridRecord,getJDBCConnectionNoRetry(false),
				sqlQueryBuffer.toString(),unsupportedInventoryFieldNames,snapshotGridRecord);
		
		snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(generatedRowsCount);
		return generatedRowsCount;
	}
		
	public static StringBuffer getSqlQuery(StringBuffer additionalWhereClause,int tableId,SnapshotGridRecord snapshotGridRecord,
			Map<Integer, String> oracleSeededUserIdToUserNameMap) {
		
		StringBuffer sqlQueryBuffer =new StringBuffer("");
		sqlQueryBuffer.append("select * from ").append(ModelUtils.DB_TABLES_PREFIX).append(tableId).
		append(" where SNAPSHOT_ID=").append(snapshotGridRecord.getSnapshotId());
		if (additionalWhereClause!=null) {
			sqlQueryBuffer.append(additionalWhereClause);
		}
		StringBuffer whereClauseSeededOracleUserIds=ModelUtils.getChangesOnlyWhereClause(oracleSeededUserIdToUserNameMap);
		sqlQueryBuffer.append(whereClauseSeededOracleUserIds);
		sqlQueryBuffer=ModelUtils.addOracleUserNamesToQuery(sqlQueryBuffer.toString(),snapshotGridRecord);
		sqlQueryBuffer.append(" ORDER BY SEQ ");
		
		//System.out.println("sqlQueryBuffer:"+sqlQueryBuffer);
		return sqlQueryBuffer;
	}
	
	public int getTotalDownloadedRecords() {
		return totalDownloadedRecords;
	}
	
}