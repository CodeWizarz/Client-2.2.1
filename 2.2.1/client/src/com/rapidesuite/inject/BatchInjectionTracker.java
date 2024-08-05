package com.rapidesuite.inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidesuite.configurator.domain.Inventory;

public class BatchInjectionTracker {

	private ScriptGridTracker scriptGridTracker;
	private List<Map<String,RecordTracker>> dataLinksToExecuteList; 
	private Worker worker;
	private int batchId;
	protected boolean isStarted;
	protected boolean isCompleted;
	protected boolean isError;
	private String batchVNCDisplayName;
	
	public BatchInjectionTracker(ScriptGridTracker scriptGridTracker,List<Map<String,RecordTracker>> dataLinksToExecuteList, int batchId) throws Exception {
		this.scriptGridTracker=scriptGridTracker;
		this.dataLinksToExecuteList=dataLinksToExecuteList;
		this.batchId=batchId;
	}
	
	public int getCurrentDataRowAbsoluteIndex(String inventoryName) throws Exception {
		Map<String, RecordTracker> inventoryToCurentRecordTrackerMap=worker.getInventoryToCurrentRecordTrackerMap();
		RecordTracker recordTracker=inventoryToCurentRecordTrackerMap.get(inventoryName);
		if (recordTracker==null) {
			throw new Exception("The navigation is trying to access the record outside of a repeating group for the inventory: '"+inventoryName+"'");
		}
		return recordTracker.getGridIndex();
	}
		
	public ScriptGridTracker getScriptGridTracker() {
		return scriptGridTracker;
	}
	
	public boolean isPending() {
		return worker==null;
	}

	public void setWorker(Worker worker) {
		this.worker=worker;
	}
	
	public Worker getWorker() {
		return worker;
	}

	public List<Map<String, RecordTracker>> getDataLinksToExecuteList() {
		return dataLinksToExecuteList;
	}

	public int getBatchId() {
		return batchId;
	}

	/*
	 * only retrieve the records for the current parent (P/C)
	 */
	public List<RecordTracker> getRecordTrackerListToExecute(Map<String, RecordTracker> inventoryToCurrentRecordTrackerMap,String inventoryName) throws Exception {
		List<RecordTracker> toReturn=new ArrayList<RecordTracker>();
		Inventory inventory=scriptGridTracker.getInjectMain().getExecutionPanelUI().getExecutionTabPanel().getInventory(scriptGridTracker,inventoryName);
		if (inventory==null) {
			throw new Exception("Cannot find inventory: '"+inventoryName+"'");
		}
		String parentName=inventory.getParentName();
		RecordTracker parentRecordTracker=inventoryToCurrentRecordTrackerMap.get(parentName);
		if (parentRecordTracker==null) {
			// THIS IS THE ROOT INVENTORY SO WE WILL BUILD THE LIST AS ALL THE RECORDS FOR THE INVENTORY BASED ON 
			// THE DATALINKS BUT ONLY THE UNIQUE IDS AS THE DATALINKS ARE BASED ON PARENT/CHILD SO ONE PARENT RECORD CAN APPEAR
			// IN MANY DATALINKS.
			int previousRecordIndex=-1;
			worker.println("getRecordTrackerListToExecute, inventoryName:'"+inventoryName+"' dataLinksToExecuteList.size(): "+dataLinksToExecuteList.size());
			for (int i=0;i<dataLinksToExecuteList.size();i++) {
				Map<String,RecordTracker> inventoryToRecordTrackerMap=dataLinksToExecuteList.get(i);
				worker.println("getRecordTrackerListToExecute, i:"+i+" inventoryToRecordTrackerMap.size(): "+inventoryToRecordTrackerMap.size());
				RecordTracker recordTracker=inventoryToRecordTrackerMap.get(inventoryName);
				if (recordTracker==null) {
					worker.println("getRecordTrackerListToExecute, NO recordTracker!");
					// If we cannot find the root record in the datalink then it is because of it was unchecked in the UI grid (Either manually
					// or after a success run). So we cannot throw an error but instead ignore that record and continue to the next.
					//throw new Exception("Internal error: cannot find the datalink record for the inventory name: '"+inventoryName+"' (dataLink index: "+i+")");
					continue;
				}
				worker.println("getRecordTrackerListToExecute, recordTracker: "+recordTracker.getGridIndex());

				int recordIndex=recordTracker.getGridIndex();
				if (previousRecordIndex!=recordIndex) {
					previousRecordIndex=recordIndex;
					toReturn.add(recordTracker);
				}
			}
		}
		else {
			int parentGridIndex=parentRecordTracker.getGridIndex();
			//System.out.println("######## getRecordTrackerListToExecute(), inventoryName:'"+inventoryName+"' parentGridIndex:"+parentGridIndex+" dataLinksToExecuteList.size():"+
			//		dataLinksToExecuteList.size());
			Set<Integer> alreadyAddedGridIndexesSet=new HashSet<Integer>();
			for (int i=0;i<dataLinksToExecuteList.size();i++) {
				Map<String,RecordTracker> inventoryToRecordTrackerMap=dataLinksToExecuteList.get(i);
				RecordTracker recordTrackerCurrentRepeatInventory=inventoryToRecordTrackerMap.get(inventoryName);
				RecordTracker recordTrackerParent=inventoryToRecordTrackerMap.get(parentName);
				//System.out.println("getRecordTrackerListToExecute, loop index: "+i+" parentName: '"+parentName+"' inventoryToRecordTrackerMap:"+inventoryToRecordTrackerMap
				//		+" recordTrackerParent: "+recordTrackerParent+" recordTrackerCurrentRepeatInventory:"+recordTrackerCurrentRepeatInventory);
				if (recordTrackerCurrentRepeatInventory==null) {
					// This datalink refers to a sibling inventory so we can ignore it
					continue;
				}				
				if (recordTrackerParent==null) {
					throw new Exception("Internal error: cannot find parent record tracker for table: '"+parentName+"'");
				}
				RecordTracker recordTrackerChild=inventoryToRecordTrackerMap.get(inventoryName);
				int gridIndexParent=recordTrackerParent.getGridIndex();
				//System.out.println("getRecordTrackerListToExecute, parentName:'"+parentName+"' gridIndexParent:"+gridIndexParent);
				if (gridIndexParent==parentGridIndex && recordTrackerChild!=null) {
					if (!alreadyAddedGridIndexesSet.contains(recordTrackerChild.getGridIndex())) {
						//System.out.println("getRecordTrackerListToExecute,  ADDED childGridIndex: "+recordTrackerChild.getGridIndex());
						toReturn.add(recordTrackerChild);
						alreadyAddedGridIndexesSet.add(recordTrackerChild.getGridIndex());
					}
				}
			}
		}
		return toReturn;
	}
	
	public void updateVNCDisplayName(String vncDisplayName) {
		this.batchVNCDisplayName=vncDisplayName;
		for (Map<String,RecordTracker> dataLinksToExecuteMap:dataLinksToExecuteList) {
			Iterator<String> iterator=dataLinksToExecuteMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key=iterator.next();
				RecordTracker recordTracker=dataLinksToExecuteMap.get(key);
				recordTracker.setVncDisplayName(vncDisplayName);
			}
		}
	}

	public void updateFirstRecordStatusAndRemarks(String status,Inventory rootInventory, String remarks) {
		for (Map<String,RecordTracker> dataLinksToExecuteMap:dataLinksToExecuteList) {
			Iterator<String> iterator=dataLinksToExecuteMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key=iterator.next();
				if (!key.equalsIgnoreCase(rootInventory.getName())) {
					continue;
				}
				RecordTracker recordTracker=dataLinksToExecuteMap.get(key);
				recordTracker.setRemarks(remarks);
				recordTracker.setStatus(status);
				break;
			}
			break;
		}
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public boolean isError() {
		return isError;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public String getBatchVNCDisplayName() {
		return batchVNCDisplayName;
	}
	

}
