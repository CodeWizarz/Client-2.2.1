package com.rapidesuite.snapshot.view;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class ViewerRecordFrame extends RecordFrame {

	private StringBuffer sqlQueryDataRows;

	public ViewerRecordFrame(int frameWidth, int frameHeight,
			Map<String, String> snapshotEnvironmentProperties,
			Inventory inventory, List<SnapshotGridRecord> snapshotGridRecords,
			int totalRecordsCount, StringBuffer sqlQueryDataRows) {
		super(frameWidth, frameHeight, snapshotEnvironmentProperties, inventory,
				snapshotGridRecords, totalRecordsCount);
		this.sqlQueryDataRows=sqlQueryDataRows;
	}
	
	public List<Map<Integer, DataRow>> fetchRecords(Connection connection,
			Inventory inventory, int startRowNumToFetch, int endRowNumToFetch) throws Exception {
		
			return ModelUtils.getDataRowsForViewChanges(
						connection,
						inventory,
						sqlQueryDataRows.toString(),
						startRowNumToFetch,
						endRowNumToFetch);
	}

}
