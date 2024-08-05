package com.rapidesuite.snapshot.view;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class ComparisonRecordFrame extends RecordFrame {
	
	private int comparisonId;
	private int tableId;

	public ComparisonRecordFrame(int frameWidth, int frameHeight,
			Map<String, String> snapshotEnvironmentProperties,
			Inventory inventory, List<SnapshotGridRecord> snapshotGridRecords,
			int totalRecordsCount,int comparisonId,int tableId) {
		super(frameWidth, frameHeight, snapshotEnvironmentProperties, inventory,
				snapshotGridRecords,  totalRecordsCount);
		this.comparisonId=comparisonId;
		this.tableId=tableId;
	}
	
	public List<Map<Integer, DataRow>> fetchRecords(Connection connection,
			Inventory inventory, int startRowNumToFetch, int endRowNumToFetch) throws Exception {
		
		int totalInventoryDataEntryColumnsCount=inventory.getFieldNamesUsedForDataEntry().size();
		List<Integer> primaryKeysPositionList=ModelUtils.getPrimaryKeysPositionList(inventory);
		
			return ModelUtils.getDataRowsForComparisonChanges(connection,comparisonId,
					tableId,totalInventoryDataEntryColumnsCount,primaryKeysPositionList,
					super.snapshotGridRecords,startRowNumToFetch);
	}
	
}
