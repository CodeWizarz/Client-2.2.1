package com.rapidesuite.snapshot.model;

import java.util.Map;

public class TotalsSnapshotObject {

	private int tableId;
	private Map<Integer, Integer> snapshotIdToDefaultRecordsMap;
	private Map<Integer, Integer> snapshotIdToAddedRecordsMap;
	private Map<Integer, Integer> snapshotIdToUpdatedRecordsMap;
	
	public TotalsSnapshotObject(int tableId) {
		this.tableId=tableId;
	}

	public int getTableId() {
		return tableId;
	}

	public Map<Integer, Integer> getSnapshotIdToDefaultRecordsMap() {
		return snapshotIdToDefaultRecordsMap;
	}

	public Map<Integer, Integer> getSnapshotIdToAddedRecordsMap() {
		return snapshotIdToAddedRecordsMap;
	}

	public Map<Integer, Integer> getSnapshotIdToUpdatedRecordsMap() {
		return snapshotIdToUpdatedRecordsMap;
	}

	public void setSnapshotIdToDefaultRecordsMap(
			Map<Integer, Integer> snapshotIdToDefaultRecordsMap) {
		this.snapshotIdToDefaultRecordsMap = snapshotIdToDefaultRecordsMap;
	}

	public void setSnapshotIdToAddedRecordsMap(
			Map<Integer, Integer> snapshotIdToAddedRecordsMap) {
		this.snapshotIdToAddedRecordsMap = snapshotIdToAddedRecordsMap;
	}

	public void setSnapshotIdToUpdatedRecordsMap(
			Map<Integer, Integer> snapshotIdToUpdatedRecordsMap) {
		this.snapshotIdToUpdatedRecordsMap = snapshotIdToUpdatedRecordsMap;
	}

}
