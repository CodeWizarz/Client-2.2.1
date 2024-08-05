package com.rapidesuite.snapshot.model;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.Map;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.controller.SnapshotDownloadController;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.SnapshotViewerFrame;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class SnapshotViewerDownloadWorker extends GenericWorker{

	private int totalDownloadedRecords;
	private boolean isBR100ReportGeneration;
	
	public SnapshotViewerDownloadWorker(SnapshotDownloadController snapshotDownloadController,boolean isBR100ReportGeneration) {
		super(snapshotDownloadController,true);
		this.isBR100ReportGeneration=isBR100ReportGeneration;
	}
		
	@Override
	public void execute(Object task) {
		SnapshotInventoryGridRecord snapshotInventoryGridRecord=(SnapshotInventoryGridRecord)task;
		SnapshotDownloadController snapshotDownloadController=(SnapshotDownloadController)super.getGenericController();
		Inventory inventory = null;
		try {
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_PROCESSING);
			snapshotInventoryGridRecord.setDownloadStartTime(System.currentTimeMillis());
			
			String currentConnectionOracleRelease=snapshotDownloadController.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
			File inventoryFile=snapshotDownloadController.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToInventoryFileMap(currentConnectionOracleRelease).get(snapshotInventoryGridRecord.getInventoryName());
			if (inventoryFile==null) {
				throw new Exception("Unable to find the inventory file: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			}
			
			inventory=FileUtils.getInventory(inventoryFile,snapshotInventoryGridRecord.getInventoryName());
			StringBuffer additionalWhereClause=snapshotInventoryGridRecord.getWhereClauseFilter();
			snapshotInventoryGridRecord.setInventory(inventory);
			
			StringBuffer sqlQueryBuffer=getSqlQuery(additionalWhereClause,snapshotInventoryGridRecord.getTableId(),
					snapshotDownloadController.getSnapshotGridRecord(),
					snapshotDownloadController.isDownloadDataChangesOnly(),
					snapshotDownloadController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap(),
					isBR100ReportGeneration
					);

			int tableRowsCount=0;
			String message = "";
			for(int i = 1; i <= DatabaseUtils.CONNECTION_RETRY_COUNT; i++) {

				try {
					if (isBR100ReportGeneration) {
						tableRowsCount=executeBR100ReportGenerationOperation(snapshotDownloadController,
								snapshotInventoryGridRecord,sqlQueryBuffer);
					}
					else {
						tableRowsCount=executeDownloadOperation(snapshotDownloadController,
								snapshotInventoryGridRecord,inventory,sqlQueryBuffer);
					}					
				}
				catch (SQLRecoverableException sqlRecoverableException) {
					if(!snapshotDownloadController.isCancelled()) {
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
			if(snapshotDownloadController.isCancelled()) {
				snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_CANCELLED);
			} else {
				snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_COMPLETED);
			}
		}
		catch(Exception e) {
			FileUtils.println("Error for inventory: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			FileUtils.printStackTrace(e);
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_FAILED);
			snapshotInventoryGridRecord.setDownloadRemarks(e.getMessage());
			super.setTotalFailedTasks(super.getTotalFailedTasks()+1);
			if(inventory!=null && !isBR100ReportGeneration){
				File outputFile=new File(snapshotDownloadController.getJobDownloadFolder(),inventory.getName()+".xml");
				if(outputFile.exists()){
					outputFile.delete();
				}
			}
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
	private int executeBR100ReportGenerationOperation(SnapshotDownloadController snapshotDownloadController,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord,StringBuffer sqlQueryBuffer) throws Exception {
		SnapshotViewerFrame snapshotViewerFrame=snapshotDownloadController.getTabSnapshotsPanel().getSnapshotViewerFrame();
		
		int tableRowsCount=ModelUtils.getTotalRowsCount(getJDBCConnectionNoRetry(false),sqlQueryBuffer.toString());
		snapshotInventoryGridRecord.setDownloadTotalRecordsCount(tableRowsCount);
		if (tableRowsCount==0) {
			return 0;
		}
		ExcelBR100Report excelBR100Report=snapshotDownloadController.getExcelBR100Report();
		boolean isReportsVerticalWay=snapshotDownloadController.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isReportsVerticalWay();
		int generatedRowsCount=ExcelBR100ReportUtils.generateExcelXMLDataFileForBR100Report(excelBR100Report,
				snapshotInventoryGridRecord,getJDBCConnectionNoRetry(false),
			sqlQueryBuffer.toString(),snapshotViewerFrame.getSnapshotGridRecord(),isReportsVerticalWay);
		
		snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(generatedRowsCount);
		return generatedRowsCount;
	}

	private int executeDownloadOperation(
			SnapshotDownloadController snapshotDownloadController,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord,
			Inventory inventory, StringBuffer sqlQueryBuffer) throws SQLException, Exception {
		int tableRowsCount=ModelUtils.getTotalRowsCount(getJDBCConnectionNoRetry(false),sqlQueryBuffer.toString());
		snapshotInventoryGridRecord.setDownloadTotalRecordsCount(tableRowsCount);
		File downloadFolder=snapshotDownloadController.getJobDownloadFolder();
	
		if (snapshotDownloadController.isExport()) {
			ModelUtils.exportTableToXML(getJDBCConnectionNoRetry(false),downloadFolder,inventory,
					snapshotDownloadController.getSnapshotGridRecord(),
					sqlQueryBuffer.toString(),snapshotInventoryGridRecord,true,true,true);
		}
		else {
			if (snapshotDownloadController.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isExcelFormat()) {
				ModelUtils.exportTableToXLSX(getJDBCConnectionNoRetry(false),downloadFolder,inventory,
						snapshotDownloadController.getSnapshotGridRecord(),
					sqlQueryBuffer.toString(),snapshotInventoryGridRecord);
			}
			else {
				ModelUtils.exportTableToXML(getJDBCConnectionNoRetry(false),downloadFolder,inventory,
						snapshotDownloadController.getSnapshotGridRecord(),
						sqlQueryBuffer.toString(),snapshotInventoryGridRecord,false,false);
			}
		}
		
		return tableRowsCount;
	}

	public int getTotalDownloadedRecords() {
		return totalDownloadedRecords;
	}
		
	public static StringBuffer getSqlQuery(StringBuffer additionalWhereClause,int tableId,SnapshotGridRecord snapshotGridRecord,boolean isDownloadDataChangesOnly,
			Map<Integer, String> oracleSeededUserIdToUserNameMap,boolean isBR100ReportGeneration) {
		
		StringBuffer sqlQueryBuffer =new StringBuffer("");
		sqlQueryBuffer.append("select * from ").append(ModelUtils.DB_TABLES_PREFIX).append(tableId).
		append(" where SNAPSHOT_ID=").append(snapshotGridRecord.getSnapshotId());
		if (additionalWhereClause!=null) {
			sqlQueryBuffer.append(additionalWhereClause);
		}
		
		if (isBR100ReportGeneration) {
			if (isDownloadDataChangesOnly) {
				StringBuffer whereClauseSeededOracleUserIds=ModelUtils.getChangesOnlyWhereClause(oracleSeededUserIdToUserNameMap);
				sqlQueryBuffer.append(whereClauseSeededOracleUserIds);
			}
		}
		else {
			if (isDownloadDataChangesOnly) {
				StringBuffer whereClauseSeededOracleUserIds=ModelUtils.getChangesOnlyWhereClause(oracleSeededUserIdToUserNameMap);
				sqlQueryBuffer.append(whereClauseSeededOracleUserIds);
			}
		}		
		
		sqlQueryBuffer=ModelUtils.addOracleUserNamesToQuery(sqlQueryBuffer.toString(),snapshotGridRecord);
		sqlQueryBuffer.append(" ORDER BY SEQ ");
		
		//System.out.println("sqlQueryBuffer:"+sqlQueryBuffer);
		return sqlQueryBuffer;
	}
	
}