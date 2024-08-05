package com.rapidesuite.inject.gui;

import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.rapidesuite.snapshot.view.FilteringTable;

public class InjectFilteringTable extends FilteringTable{

	public InjectFilteringTable(Vector<String> columnNames,
			JScrollPane variableScroll, JTable dataTable, int headerHeight,
			boolean isRefreshEveryListOnUse) {
		super(columnNames, variableScroll, dataTable, headerHeight, isRefreshEveryListOnUse);
	}

	public void sorterChangedAction(int newRowCount) {
	
	}
	
	@Override
	protected void showBalloons(int rowIndex,int columnIndex, JTable fixedTable) {
		
	}
	
}