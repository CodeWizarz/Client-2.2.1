package com.rapidesuite.snapshot.model;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.List;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.controller.SnapshotDownloadController;
import com.rapidesuite.snapshot.view.SnapshotComparisonAnalysisFrame;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class SnapshotComparisonDownloadWorker extends GenericWorker{

	private int totalDownloadedRecords;
	private boolean isComparisonReportGeneration;
	
	public SnapshotComparisonDownloadWorker(SnapshotDownloadController snapshotDownloadController, boolean isComparisonReportGeneration) {
		super(snapshotDownloadController,true);
		this.isComparisonReportGeneration=isComparisonReportGeneration;
	}
	
	@Override
	public void execute(Object task) {
		SnapshotInventoryGridRecord snapshotInventoryGridRecord=(SnapshotInventoryGridRecord)task;
		SnapshotDownloadController snapshotDownloadController=(SnapshotDownloadController)super.getGenericController();
		try {			
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_PROCESSING);
			snapshotInventoryGridRecord.setDownloadStartTime(System.currentTimeMillis());
			
			String currentConnectionOracleRelease=snapshotDownloadController.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
			File inventoryFile=snapshotDownloadController.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToInventoryFileMap(currentConnectionOracleRelease).get(snapshotInventoryGridRecord.getInventoryName());
			if (inventoryFile==null) {
				throw new Exception("Unable to find the inventory file: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			}
			Inventory inventory=FileUtils.getInventory(inventoryFile,snapshotInventoryGridRecord.getInventoryName());
			StringBuffer sqlQueryBuffer =new StringBuffer("");
		
			int comparisonId=snapshotDownloadController.getTabSnapshotsPanel().getSnapshotComparisonAnalysisFrame().getSnapshotComparisonController().getComparisonId();			
			if (isComparisonReportGeneration) {
				sqlQueryBuffer=getSQLQueryComparisonDownload(comparisonId,snapshotInventoryGridRecord);
			}
			else {
				sqlQueryBuffer=getSQLQueryComparisonDownload(comparisonId,snapshotInventoryGridRecord);
			}
			//System.out.println("sqlQueryBuffer:\n"+sqlQueryBuffer);
			
			int tableRowsCount=0;
			String message = "";			
			for(int i = 1; i <= DatabaseUtils.CONNECTION_RETRY_COUNT; i++) {
				
				try {
					if (isComparisonReportGeneration) {
						tableRowsCount=executeComparisonReportGenerationOperation(comparisonId,snapshotDownloadController,
								snapshotInventoryGridRecord,inventory);
					}
					else {
						tableRowsCount=executeComparisonDownloadOperation(snapshotDownloadController,
								snapshotInventoryGridRecord,inventory,sqlQueryBuffer);
					}				
				}
				catch (SQLRecoverableException sqlRecoverableException) {
					if(!snapshotDownloadController.isCancelled()) {
						// show the sleeping remark.
						if(i < DatabaseUtils.CONNECTION_RETRY_COUNT) {
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
		}
		finally  {
			UIUtils.setDownloadTime(snapshotInventoryGridRecord);
		}
	}

	private int executeComparisonReportGenerationOperation(int comparisonId,
			SnapshotDownloadController snapshotDownloadController,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord,
			Inventory inventory) throws ClassNotFoundException, SQLException, Exception {
		
		List<Integer> primaryKeysPositionList=ModelUtils.getPrimaryKeysPositionList(inventory);
		SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame=snapshotDownloadController.getTabSnapshotsPanel().getSnapshotComparisonAnalysisFrame();
				
		int tableRowsCount=ModelUtils.getComparisonDataRowsCount(getJDBCConnectionNoRetry(false),comparisonId,
				snapshotInventoryGridRecord.getTableId(),primaryKeysPositionList);
		snapshotInventoryGridRecord.setDownloadTotalRecordsCount(tableRowsCount);
		if (tableRowsCount==0) {
			return 0;
		}
		ExcelComparisonReport excelComparisonReport=snapshotDownloadController.getExcelComparisonReport();
		boolean isReportsVerticalWay=snapshotDownloadController.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isReportsVerticalWay();
		int generatedRowsCount=ExcelComparisonReportUtils.generateExcelXMLDataFileForComparisonReport(excelComparisonReport,comparisonId,snapshotInventoryGridRecord,
				getJDBCConnectionNoRetry(false),snapshotInventoryGridRecord.getTableId(),
				primaryKeysPositionList,snapshotComparisonAnalysisFrame.getSnapshotGridRecords(),isReportsVerticalWay);
	
		snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(generatedRowsCount);
		return generatedRowsCount;
	}

	private int executeComparisonDownloadOperation(SnapshotDownloadController snapshotDownloadController,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord,Inventory inventory,StringBuffer sqlQueryBuffer) throws Exception {
		int tableRowsCount=ModelUtils.getTotalRowsCount(getJDBCConnectionNoRetry(false),sqlQueryBuffer.toString());
		snapshotInventoryGridRecord.setDownloadTotalRecordsCount(tableRowsCount);
		
		File downloadFolder=snapshotDownloadController.getJobDownloadFolder();
		if (snapshotDownloadController.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isExcelFormat()) {
			ModelUtils.exportTableToXLSX(getJDBCConnectionNoRetry(false),downloadFolder,inventory,
					snapshotDownloadController.getSnapshotGridRecord(),
				sqlQueryBuffer.toString(),snapshotInventoryGridRecord);
		}
		else {
			ModelUtils.exportTableToXML(getJDBCConnectionNoRetry(false),downloadFolder,inventory
					,snapshotDownloadController.getSnapshotGridRecord(),
				sqlQueryBuffer.toString(),snapshotInventoryGridRecord,false,false);
		}
		return tableRowsCount;
	}

	private StringBuffer getSQLQueryComparisonDownload(int comparisonId,SnapshotInventoryGridRecord snapshotInventoryGridRecord) {
		StringBuffer sqlQueryBuffer =new StringBuffer("");
		sqlQueryBuffer.append("select target_baseline_id as snapshot_id,TC.*,'' AS rsc_inv_org_id,'' AS rsc_ou_id,'' AS rsc_ledger_id,'' AS rsc_bg_id,'' AS rsc_coa_id  from ").
		append(ModelUtils.DB_COMPARISON_TABLES_PREFIX).append(snapshotInventoryGridRecord.getTableId()).append(" TC ").
		append(" where COMPARISON_ID=").append(comparisonId).append(" and not (rsc_last_updated_by is null and rsc_last_update_date is null and rsc_created_by is null and rsc_creation_date is null)");
		
		sqlQueryBuffer=ModelUtils.addOracleUserNamesToQuery(sqlQueryBuffer.toString(),null);
		return sqlQueryBuffer;
	}

	public int getTotalDownloadedRecords() {
		return totalDownloadedRecords;
	}
	
}