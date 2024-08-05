package com.rapidesuite.snapshot.model;

import java.sql.SQLRecoverableException;
import java.text.DecimalFormat;
import java.util.List;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.snapshot.controller.SnapshotPhysicalDeleteController;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;



public class SnapshotPhysicalDeleteWorker extends GenericWorker {
	protected SnapshotPhysicalDeleteController snapshotPhysicalDeleteSnapshotController;	
	protected String errMsg;
	protected boolean isDeleteSuccess;
	private int totalDeletedRecords;
	
	SnapshotInventoryGridRecord snapshotInventoryGridRecord;
	public SnapshotPhysicalDeleteWorker(SnapshotPhysicalDeleteController snapshotPhysicalDeleteSnapshotController) {
		super(snapshotPhysicalDeleteSnapshotController,false);
		this.snapshotPhysicalDeleteSnapshotController = snapshotPhysicalDeleteSnapshotController;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public static String formatNumberWithComma(int number)throws Exception {
		DecimalFormat myFormatter = new DecimalFormat("###,###,###,###");
		return myFormatter.format(number);
   }
	
	@Override
	public void execute(Object task) {
		snapshotInventoryGridRecord=(SnapshotInventoryGridRecord)task;
		snapshotPhysicalDeleteSnapshotController=(SnapshotPhysicalDeleteController)super.getGenericController();
		String message = "";
		try {
			List<Integer> snapshotIdList = snapshotPhysicalDeleteSnapshotController.getSnapshotIdList();
			String tableIdStr = String.valueOf(snapshotInventoryGridRecord.getTableId());
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_PROCESSING);
			snapshotInventoryGridRecord.setDownloadStartTime(System.currentTimeMillis());
			int totalDeletedRow = 0;
			for(int i = 1; i <= DatabaseUtils.CONNECTION_RETRY_COUNT; i++) {
				try {
					int range = 1;
					String formattedTotal = formatNumberWithComma(snapshotInventoryGridRecord.getDownloadTotalRecordsCount());
					while (range <= snapshotInventoryGridRecord.getDownloadTotalRecordsCount()) {
						String formattedRange= formatNumberWithComma((range-1));
						snapshotInventoryGridRecord.setDownloadRemarks("Deleting "+formattedRange+" / "+formattedTotal+" records.");
						int totalDeletedRowInBatch = ModelUtils.deleteSnapshotDataFromInventoryTableByBatch(getJDBCConnectionNoRetry(),snapshotIdList,tableIdStr,Config.getSnapshotDeleteBatchSize());
						totalDeletedRow+=totalDeletedRowInBatch;
						snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(totalDeletedRow);
						range += Config.getSnapshotDeleteBatchSize();	
					}	
					totalDeletedRecords+=totalDeletedRow;
					//Delete the record in INVENTORY_TO_SNAPSHOT 
					ModelUtils.deleteInventoryToSnapshotData(getJDBCConnectionNoRetry(),snapshotIdList,tableIdStr);					
					if(snapshotPhysicalDeleteSnapshotController.isCancelled()) {
						snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_CANCELLED);
					}
					else {
						snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_COMPLETED);
						snapshotInventoryGridRecord.setDownloadRemarks("");
					}
				}catch (SQLRecoverableException sqlRecoverableException) {
					if(i < DatabaseUtils.CONNECTION_RETRY_COUNT) {
						snapshotInventoryGridRecord.setDownloadRemarks("Retrying "+i+" / "+DatabaseUtils.CONNECTION_RETRY_COUNT+".");
						try {
							Thread.sleep(DatabaseUtils.CONNECTION_RETRY_WAIT_SECONDS * 1000);
						} catch (InterruptedException e) {
							FileUtils.printStackTrace(e);
						}
					} else {
						FileUtils.printStackTrace(sqlRecoverableException);
						message = "Connection lost. "+DatabaseUtils.CONNECTION_RETRY_COUNT+" retries exhausted!";
						snapshotInventoryGridRecord.setDownloadRemarks(message);
						throw sqlRecoverableException;
					}
					continue;
				}
				break;
			}	
		}catch(Throwable e) {
			FileUtils.printStackTrace(e);
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_FAILED);
		    if(message.equals("")){
		    	snapshotInventoryGridRecord.setDownloadRemarks(e.getMessage());
			}
			super.setTotalFailedTasks(super.getTotalFailedTasks()+1);
		}
		finally  {
			UIUtils.setDownloadTime(snapshotInventoryGridRecord);
		}
	}
	public boolean isDeleteSuccess() {
		return isDeleteSuccess;
	}
	public int getTotalDeletedRecords() {
		return totalDeletedRecords;
	}
}
