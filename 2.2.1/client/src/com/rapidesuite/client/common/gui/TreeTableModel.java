package com.rapidesuite.client.common.gui;

import java.util.List;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class TreeTableModel extends DefaultTreeTableModel 
{
	
	private List<String> columnNames;
	private int hierarchicalColumn;
	
	public TreeTableModel(AbstractMutableTreeTableNode treeTableRootNode,List<String> columnNames,
			int hierarchicalColumn){
		super(treeTableRootNode);
		this.columnNames=columnNames;
		this.hierarchicalColumn=hierarchicalColumn;
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.size();
	}
	
	@Override
	public String getColumnName( int column ){
		return columnNames.get(column);
	}
	
	 public int  getHierarchicalColumn() {
		 return hierarchicalColumn;
	 }

	 public List<String> getColumnNames() {
		 return columnNames;
	 }

	 public boolean isCellEditable(java.lang.Object node, int column){
		 return false;
	 }
	
}