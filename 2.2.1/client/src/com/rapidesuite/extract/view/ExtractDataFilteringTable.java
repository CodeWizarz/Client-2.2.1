package com.rapidesuite.extract.view;

import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.rapidesuite.snapshot.view.FilteringTable;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class ExtractDataFilteringTable  extends FilteringTable{

	private TableGridFrame tableGridFrame;

	public ExtractDataFilteringTable(TableGridFrame tableGridFrame,Vector<String> columnNames,
			JScrollPane variableScroll, TableGrid tableGrid, int headerHeight,
			boolean isRefreshEveryListOnUse) {
		super(columnNames, variableScroll, tableGrid.getTable(), headerHeight, isRefreshEveryListOnUse);
		this.tableGridFrame=tableGridFrame;
	}

	public void sorterChangedAction(int newRowCount) {
		final String text=UIConstants.LABEL_GRID_ROWS + UIUtils.getFormattedNumber(newRowCount);
		tableGridFrame.getGridRowsCountLabel().setText(text);
	}
	
	@Override
	protected void showBalloons(int rowIndex,int columnIndex, JTable fixedTable) {
		
	}
	
}