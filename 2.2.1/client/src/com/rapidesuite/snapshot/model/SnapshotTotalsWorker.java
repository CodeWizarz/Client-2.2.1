package com.rapidesuite.snapshot.model;

import java.util.Map;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.snapshot.controller.SnapshotController;

public class SnapshotTotalsWorker extends GenericWorker{

	protected int tableId;
	
	public SnapshotTotalsWorker(SnapshotController snapshotController) {
		super(snapshotController,true);
	}
		
	@Override
	public void execute(Object task) {
		SnapshotTotalsTask snapshotTotalsTask=(SnapshotTotalsTask)task;
		SnapshotController snapshotController=(SnapshotController)super.getGenericController();
		try {
			tableId=snapshotTotalsTask.getTableId();

			if (!snapshotController.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowTotalDetails()) {
				return;
			}
						
			boolean isTableAlreadyCreated=snapshotController.getTableIds().contains(tableId);
			if (isTableAlreadyCreated) {
				Map<Integer, Integer> snapshotIdToDefaultRecordsMap= ModelUtils.getSnapshotTotalDefaultRecordsCount(getJDBCConnection(),tableId,
						snapshotController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
				Map<Integer, Integer> snapshotIdToAddedRecordsMap= ModelUtils.getSnapshotTotalAddedRecordsCount(getJDBCConnection(),tableId,
						snapshotController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
				Map<Integer, Integer> snapshotIdToUpdatedRecordsMap= ModelUtils.getSnapshotTotalUpdatedRecordsCount(getJDBCConnection(),tableId,
						snapshotController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());

				Map<Integer,TotalsSnapshotObject> tableIdToTotalsSnapshotObjectMap=snapshotController.getTableIdToTotalsSnapshotObjectMap();
				TotalsSnapshotObject totalsSnapshotObject=new TotalsSnapshotObject(tableId);
				totalsSnapshotObject.setSnapshotIdToDefaultRecordsMap(snapshotIdToDefaultRecordsMap);
				totalsSnapshotObject.setSnapshotIdToAddedRecordsMap(snapshotIdToAddedRecordsMap);
				totalsSnapshotObject.setSnapshotIdToUpdatedRecordsMap(snapshotIdToUpdatedRecordsMap);
				tableIdToTotalsSnapshotObjectMap.put(tableId, totalsSnapshotObject);
			}
		}
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
		}
	}

	public int getTableId() {
		return tableId;
	}
	
}