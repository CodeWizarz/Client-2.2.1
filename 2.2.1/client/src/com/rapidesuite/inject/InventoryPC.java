package com.rapidesuite.inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.inject.gui.ScriptsGrid;
import com.rapidesuite.inject.gui.TableGrid;

public class InventoryPC {
	
	private String inventoryName;
	private String parentInventoryName;
	private Map<Integer,Set<Integer>> parentRowIndexToRowIndexMap;
	private Map<Integer,Integer> rowIndexToParentRowIndexMap;
	private List<InventoryPC> childInventoryList;
	private Map<String , InventoryPC> allInventoryPCMap;
	
	public InventoryPC(String inventoryName,String parentInventoryName) {
		this.inventoryName = inventoryName;
		this.parentInventoryName = parentInventoryName;
		parentRowIndexToRowIndexMap=new HashMap<Integer,Set<Integer>>();
		rowIndexToParentRowIndexMap=new HashMap<Integer,Integer>();
		childInventoryList=new ArrayList<InventoryPC>();
	}

	public Map<Integer, Set<Integer>> getParentRowIndexToRowIndexMap() {
		return parentRowIndexToRowIndexMap;
	}

	public Map<Integer, Integer> getRowIndexToParentRowIndexMap() {
		return rowIndexToParentRowIndexMap;
	}

	public String getInventoryName() {
		return inventoryName;
	}

	public List<InventoryPC> getChildInventoryList() {
		return childInventoryList;
	}
	
	public void initMaps(Map<String,Integer> inventoryNameToRowIndexMap) {
		if (parentInventoryName!=null && !parentInventoryName.isEmpty()) {
			Integer rowIndex=inventoryNameToRowIndexMap.get(inventoryName);
			if (rowIndex!=null) {
				Integer parentRowIndex=inventoryNameToRowIndexMap.get(parentInventoryName);
				if (parentRowIndex!=null) {
					if (rowIndexToParentRowIndexMap.get(rowIndex)==null) {
						rowIndexToParentRowIndexMap.put(rowIndex, parentRowIndex);
					}
					Set<Integer> rowIndexList=parentRowIndexToRowIndexMap.get(parentRowIndex);
					if (rowIndexList==null) {
						rowIndexList=new HashSet<Integer>();
						parentRowIndexToRowIndexMap.put(parentRowIndex,rowIndexList);
					}
					rowIndexList.add(rowIndex);
				}
			}
		}
		for (InventoryPC child:childInventoryList) {
			child.initMaps(inventoryNameToRowIndexMap);
		}
	}
	
	public void debugMaps() {
		System.out.println("inventoryName: '"+inventoryName+"' parentInventoryName: '"+parentInventoryName+"'");
		Iterator<Integer> iterator=parentRowIndexToRowIndexMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key=iterator.next();
			Set<Integer> list=parentRowIndexToRowIndexMap.get(key);
			List<Integer> temp=new ArrayList<Integer>(list);
			System.out.println("parentRowIndex: "+key+" rowIndexes: "+temp);
		}
		iterator=rowIndexToParentRowIndexMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key=iterator.next();
			Integer parentRowIndex=rowIndexToParentRowIndexMap.get(key);
			System.out.println("rowIndex: "+key+" parentRowIndex: "+parentRowIndex);
		}
		for (InventoryPC child:childInventoryList) {
			child.debugMaps();
		}
	}

	public void setEnableOnChildRows(ScriptsGrid scriptsGrid,ScriptGridTracker scriptGridTracker,List<Integer> rowIndexesTableGrid, boolean isEnabled) throws Exception {
		Map<String,List<Integer>> inventoryNameToRowsListMap=new HashMap<String,List<Integer>>();
		//System.out.println("setEnableOnChildRows, inventoryName: '"+inventoryName+"' rowIndexesTableGrid: "+rowIndexesTableGrid);
		for (Integer rowIndex:rowIndexesTableGrid) {
			//System.out.println("setEnableOnChildRows, rowIndex: "+rowIndex);
			for (InventoryPC child:childInventoryList) {
				Map<Integer,Set<Integer>> childMap=child.getParentRowIndexToRowIndexMap();
				Set<Integer> rows=childMap.get(rowIndex);
				//System.out.println("setEnableOnChildRows, rows: "+rows);
				if (rows==null) {
					continue;
				}
				List<Integer> allRows=inventoryNameToRowsListMap.get(child.getInventoryName());
				if (allRows==null) {
					allRows=new ArrayList<Integer>();
					inventoryNameToRowsListMap.put(child.getInventoryName(),allRows);
				}
				allRows.addAll(rows);
			}
		}
		Map<String, TableGrid> inventoryToTableGridsMap=scriptsGrid.getScriptIdToTableGridMap().get(scriptGridTracker.getScript().getUniqueID().intValue());
		for (InventoryPC child:childInventoryList) {
			List<Integer> rows=inventoryNameToRowsListMap.get(child.getInventoryName());
			if (rows!=null) {
				TableGrid tableGridChild=inventoryToTableGridsMap.get(child.getInventoryName());
				tableGridChild.updateSelectionTableGrid(rows,isEnabled);
				//System.out.println("setEnableOnChildRows, child.getInventoryName(): '"+child.getInventoryName()+"'");
				child.setEnableOnChildRows(scriptsGrid,scriptGridTracker,rows,isEnabled);
			}
		}
	}
	
	public void setEnableOnParentRows(ScriptsGrid scriptsGrid,ScriptGridTracker scriptGridTracker,List<Integer> rowIndexesTableGrid,InventoryPC rootInventoryPC) throws Exception {
		List<Integer> parentRowsToSelectList=new ArrayList<Integer>();
		for (Integer rowIndex:rowIndexesTableGrid) {
			Integer parentRowIndex=rowIndexToParentRowIndexMap.get(rowIndex);
			if (parentRowIndex!=null) {
				parentRowsToSelectList.add(parentRowIndex);
			}
		}
		Map<String, TableGrid> inventoryToTableGridsMap=scriptsGrid.getScriptIdToTableGridMap().get(scriptGridTracker.getScript().getUniqueID().intValue());
		TableGrid parentTableGrid=inventoryToTableGridsMap.get(parentInventoryName);
		if (parentTableGrid!=null) {
			parentTableGrid.updateSelectionTableGrid(parentRowsToSelectList,true);
			InventoryPC parentInventoryPC=rootInventoryPC.getAllInventoryPCMap().get(parentInventoryName);
			parentInventoryPC.setEnableOnParentRows(scriptsGrid,scriptGridTracker,parentRowsToSelectList,rootInventoryPC);
		}
	}
	
	public InventoryPC getInventoryPCFromRoot(String name) {
		if (parentInventoryName!=null && !parentInventoryName.isEmpty()) {
			return null;
		}
		return allInventoryPCMap.get(name);
	}

	public Map<String, InventoryPC> getAllInventoryPCMap() {
		return allInventoryPCMap;
	}

	public void setAllInventoryPCMap(Map<String, InventoryPC> allInventoryPCMap) {
		this.allInventoryPCMap = allInventoryPCMap;
	}
	
	public void selectParentRowIfChildRowIsSelected(ScriptsGrid scriptsGrid,ScriptGridTracker scriptGridTracker) throws Exception {
		// Very important: we need to start from the leaves then go up otherwise we will miss some checkboxes!!!!
		for (InventoryPC child:childInventoryList) {
			child.selectParentRowIfChildRowIsSelected(scriptsGrid,scriptGridTracker);
		}
		
		if (parentInventoryName!=null && !parentInventoryName.isEmpty()) {
			Map<String, TableGrid> inventoryToTableGridsMap=scriptsGrid.getScriptIdToTableGridMap().get(scriptGridTracker.getScript().getUniqueID().intValue());
			TableGrid parentTableGrid=inventoryToTableGridsMap.get(parentInventoryName);
			if (parentTableGrid!=null) {
				Iterator<Integer> iterator=rowIndexToParentRowIndexMap.keySet().iterator();
				TableGrid tableGrid=inventoryToTableGridsMap.get(inventoryName);
				while (iterator.hasNext()) {
					Integer rowIndex=iterator.next();
					boolean isRowSelected=tableGrid.isSelected(rowIndex);
					if (isRowSelected) {
						Integer parentRowIndex=rowIndexToParentRowIndexMap.get(rowIndex);
						boolean isParentRowSelected=parentTableGrid.isSelected(parentRowIndex);
						if (!isParentRowSelected) {
							FileUtils.println("*** FIXING INCONSISTENCY IN SELECTION FOR THE ROW: "+parentRowIndex+" OF THE TABLE: '"+parentInventoryName+"' FOR"+
									" THE SCRIPT: '"+scriptGridTracker.getScript().getName()+"' child table: '"+inventoryName+"' row: "+rowIndex);
							parentTableGrid.updateSelectionRow(parentRowIndex,true);
						}
					}					
				}
			}
		}
	}
	

}
