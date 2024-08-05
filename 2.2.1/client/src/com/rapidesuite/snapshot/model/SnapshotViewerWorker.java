package com.rapidesuite.snapshot.model;

import java.util.Map;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.snapshot.controller.SnapshotViewerController;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;

public class SnapshotViewerWorker extends GenericWorker{

	public SnapshotViewerWorker(SnapshotViewerController snapshotViewerController) {
		super(snapshotViewerController,true);
	}
		
	@Override
	public void execute(Object task) {
		SnapshotInventoryGridRecord snapshotInventoryGridRecord=(SnapshotInventoryGridRecord)task;
		SnapshotViewerController snapshotViewerController=(SnapshotViewerController)super.getGenericController();
		try {

			if (snapshotInventoryGridRecord.getStatus()==null || !snapshotInventoryGridRecord.getStatus().equalsIgnoreCase(ModelUtils.DB_STATUS_SUCCESS)) {
				snapshotInventoryGridRecord.setDisplayTotalRecords(true);
				snapshotInventoryGridRecord.setDisplayTotalAddedRecords(false);
				snapshotInventoryGridRecord.setDisplayTotalDefaultRecords(false);
				snapshotInventoryGridRecord.setDisplayTotalUpdatedRecords(false);
				snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_OUT_PREFIX+"Invalid Status");
				return;
			}

			int tableId=snapshotInventoryGridRecord.getTableId();
			if (snapshotInventoryGridRecord.getTotalRecords()!=0) {
				Map<Integer, TotalsObject> tableIdToTotalsObjectMap=snapshotViewerController.getTableIdToTotalsObjectMap();
				TotalsObject totalsObject=tableIdToTotalsObjectMap.get(tableId);
				if (totalsObject==null) {
					int totalDefaultRecords= ModelUtils.getTotalDefaultRecordsCount(getJDBCConnection(),snapshotViewerController.getSnapshotId()
							,tableId,snapshotViewerController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
					int totalAddedRecords= ModelUtils.getTotalAddedRecordsCount(getJDBCConnection(),snapshotViewerController.getSnapshotId()
							,tableId,snapshotViewerController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
					int totalUpdatedRecords= ModelUtils.getTotalUpdatedRecordsCount(getJDBCConnection(),snapshotViewerController.getSnapshotId()
							,tableId,snapshotViewerController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
					totalsObject=new TotalsObject(tableId);
					totalsObject.setTotalAddedRecords(totalAddedRecords);
					totalsObject.setTotalDefaultRecords(totalDefaultRecords);
					totalsObject.setTotalUpdatedRecords(totalUpdatedRecords);

					tableIdToTotalsObjectMap.put(tableId, totalsObject);
				}
				snapshotInventoryGridRecord.setTotalDefaultRecords(totalsObject.getTotalDefaultRecords());
				snapshotInventoryGridRecord.setTotalAddedRecords(totalsObject.getTotalAddedRecords());
				snapshotInventoryGridRecord.setTotalUpdatedRecords(totalsObject.getTotalUpdatedRecords());
			}
			
			boolean isShowOnlyFormsWithChangesCheckBox=snapshotViewerController.getFiltersPanel().getFilterGeneralPanel().isShowOnlyFormsWithChangesCheckBox();
			if (isShowOnlyFormsWithChangesCheckBox && hasTableNoChanges(snapshotInventoryGridRecord)) {
				snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_OUT_PREFIX+"No Changes");
				return;
			}
					
			StringBuffer additionalwhereClauseFilter=new StringBuffer("");
			if(!snapshotInventoryGridRecord.getFormInformation().isDisableOUAndLevelFiltering()) {
				additionalwhereClauseFilter.append(snapshotViewerController.getOperatingUnitWhereClauseFiltering(snapshotInventoryGridRecord.getFormInformation(),
						snapshotInventoryGridRecord.getFormInformation().hasBusinessGroupId()));
			}
			
			// FILTERING DATES:
			boolean isCreationDateEnabled=snapshotViewerController.getFiltersPanel().getFilterDatePanel().isCreationDateEnabled();
			boolean isLastUpdateDateEnabled=snapshotViewerController.getFiltersPanel().getFilterDatePanel().isLastUpdateDateEnabled();
			if (isCreationDateEnabled || isLastUpdateDateEnabled) {
				additionalwhereClauseFilter.append(snapshotViewerController.getFilteringDatesWhereClauseFiltering());
			}
			
			// FILTERING BY USERS:
			additionalwhereClauseFilter.append(snapshotViewerController.getCreatedByUsersWhereClauseFiltering());
			additionalwhereClauseFilter.append(snapshotViewerController.getLastUpdatedByUsersWhereClauseFiltering());
						
			// optimization: if there are no added where clause it means that all records from the table will match so we don't
			// really need to query the table as the Total counters are already set in the inventory_to_snapshot table.
			snapshotInventoryGridRecord.setWhereClauseFilter(additionalwhereClauseFilter);
			//FileUtils.println("snapshotInventoryGridRecord.getTableId():"+snapshotInventoryGridRecord.getTableId()+" additionalwhereClauseFilter:"+additionalwhereClauseFilter);
			if (additionalwhereClauseFilter==null || additionalwhereClauseFilter.toString().isEmpty()) {
				snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_IN_PREFIX);
				return;
			}
			
			boolean isTablePassFilteringCriteria=ModelUtils.isTablePassFilteringCriteria(super.getJDBCConnection(),
					snapshotViewerController.getSnapshotId(),
					snapshotInventoryGridRecord.getTableId(),snapshotInventoryGridRecord,snapshotViewerController.getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap()
					,additionalwhereClauseFilter.toString());
			if (isTablePassFilteringCriteria) {
				snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_IN_PREFIX);
			}
			else {
				snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_OUT_PREFIX+"by end-user selection");
				return;
			}
		}
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
			snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_OUT_PREFIX+"error: "+e.getMessage().replaceAll("\\s+", " "));
		}
	}
	
	private boolean hasTableNoChanges(SnapshotInventoryGridRecord snapshotInventoryGridRecord) {
		int totalAddedRecords=snapshotInventoryGridRecord.getTotalAddedRecords();
		int totalUpdatedRecords=snapshotInventoryGridRecord.getTotalUpdatedRecords();
		boolean hasTableNoChanges=totalAddedRecords==0 && totalUpdatedRecords==0;
		return hasTableNoChanges;
	}
	
}