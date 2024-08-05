package com.rapidesuite.snapshot.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.controller.SnapshotComparisonController;
import com.rapidesuite.snapshot.view.ComparisonChangesRecord;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;

public class SnapshotComparisonWorker extends GenericWorker{

	public SnapshotComparisonWorker(SnapshotComparisonController snapshotComparisonController) {
		super(snapshotComparisonController,true);
	}
		
	@Override
	public void execute(Object task) {
		SnapshotInventoryGridRecord snapshotInventoryGridRecord=(SnapshotInventoryGridRecord)task;
		SnapshotComparisonController snapshotComparisonController=(SnapshotComparisonController)super.getGenericController();
		try {
			List<SnapshotGridRecord> snapshotGridRecords=snapshotComparisonController.getSnapshotComparisonAnalysisFrame().getSnapshotGridRecordsInDateOrder();
			int sourceSnapshotId=snapshotGridRecords.get(0).getSnapshotId();
			int tableId=snapshotInventoryGridRecord.getTableId();
			List<ComparisonChangesRecord> comparisonChangesRecordList=new ArrayList<ComparisonChangesRecord>();
			snapshotInventoryGridRecord.setComparisonChangesRecordList(comparisonChangesRecordList);
					
			int totalRecords= ModelUtils.getTotalRecordsCount(getJDBCConnection(),sourceSnapshotId,tableId,null);
			snapshotInventoryGridRecord.setTotalRecords(totalRecords);
			
			StringBuffer additionalwhereClauseFilter=new StringBuffer("");
			if(!snapshotInventoryGridRecord.getFormInformation().isDisableOUAndLevelFiltering()) {
				additionalwhereClauseFilter.append(snapshotComparisonController.getOperatingUnitWhereClauseFiltering(snapshotInventoryGridRecord.getFormInformation(),
						snapshotInventoryGridRecord.getFormInformation().hasBusinessGroupId()));
			}
			
			String connectionOracleRelease=snapshotComparisonController.getSnapshotComparisonAnalysisFrame().getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
			File inventoryFile=snapshotComparisonController.getSnapshotComparisonAnalysisFrame().getTabSnapshotsPanel().
					getSnapshotPackageSelectionPanel().getInventoryNameToInventoryFileMap(connectionOracleRelease).get(
					snapshotInventoryGridRecord.getInventoryName());
			if (inventoryFile==null) {
				throw new Exception("Unable to find the inventory file: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			}
			Inventory inventory=FileUtils.getInventory(inventoryFile,snapshotInventoryGridRecord.getInventoryName());
			snapshotInventoryGridRecord.setInventory(inventory);
			
			List<Integer> primaryKeysPositionList=ModelUtils.getPrimaryKeysPositionList(inventory);
			if (primaryKeysPositionList==null || primaryKeysPositionList.isEmpty()) {
				snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_OUT_PREFIX+"This Inventory has no Primary Keys defined!");
				return;
			}
			
			boolean isIgnoreSeededUserChanges=snapshotComparisonController.getSnapshotComparisonAnalysisFrame().getSnapshotComparisonFiltersPanel().getFilterComparisonGeneralPanel().isIgnoreSeededUserChanges();
			if (isIgnoreSeededUserChanges) {
				StringBuffer whereClauseSeededOracleUserIds=ModelUtils.getChangesOnlyWhereClause(
						snapshotComparisonController.getSnapshotComparisonAnalysisFrame().getTabSnapshotsPanel().getServerSelectionPanel().
						getOracleSeededUserIdToUserNameMap());
				additionalwhereClauseFilter.append(whereClauseSeededOracleUserIds);
			}
			int targetSnapshotId=-1;
			boolean hasChanges=false;

			for (int i=1;i<snapshotGridRecords.size();i++) {
				SnapshotGridRecord snapshotGridRecord=snapshotGridRecords.get(i);
				targetSnapshotId=snapshotGridRecord.getSnapshotId();
				ComparisonChangesRecord comparisonChangesRecord=new ComparisonChangesRecord();
				comparisonChangesRecordList.add(comparisonChangesRecord);

				//FileUtils.println("analyze, tableId:"+tableId+" sourceSnapshotId:"+sourceSnapshotId+" with targetSnapshotId:"+targetSnapshotId);
				int totalChanges=ModelUtils.generateComparisonTableRecords( snapshotComparisonController,connectionOracleRelease,
						ModelUtils.getDBUserName(ModelUtils.getSnapshotEnvironmentProperties(
								snapshotComparisonController.getSnapshotComparisonAnalysisFrame().getTabSnapshotsPanel())),
						getJDBCConnection(),inventory,snapshotComparisonController.getComparisonId() ,
						tableId, sourceSnapshotId,targetSnapshotId,additionalwhereClauseFilter.toString());
				
				if (totalChanges!=0) {
					hasChanges=true;
					//FileUtils.println("analyze, tableId:"+tableId+" totalChanges:"+totalChanges);
				}
				comparisonChangesRecord.setTotalChanges(totalChanges);
				sourceSnapshotId=targetSnapshotId;
			}
			if (hasChanges) {
				snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_IN_PREFIX);
			}
			else {
				snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_OUT_PREFIX+"No changes");
			}
		}
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
			snapshotInventoryGridRecord.setFilteringResult(UIConstants.FILTERED_OUT_PREFIX+"error: "+e.getMessage().replaceAll("\\s+", " "));
		}
	}
	
}