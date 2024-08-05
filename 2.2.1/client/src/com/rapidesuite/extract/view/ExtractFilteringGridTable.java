package com.rapidesuite.extract.view;

import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.java.balloontip.BalloonTip;

import com.rapidesuite.snapshot.view.FilteringTable;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class ExtractFilteringGridTable extends FilteringTable{

	private boolean isShowBalloons;
	private ExtractInventoryGridPanelGeneric extractInventoryGridPanelGeneric;
	
	public ExtractFilteringGridTable(ExtractInventoryGridPanelGeneric extractInventoryGridPanelGeneric,
			Vector<String> columnNames,
			JScrollPane variableScroll, JTable dataTable, int headerHeight,
			boolean isRefreshEveryListOnUse, boolean isShowBalloons) {
		super(columnNames, variableScroll, dataTable, headerHeight,isRefreshEveryListOnUse);
		this.isShowBalloons=isShowBalloons;
		this.extractInventoryGridPanelGeneric=extractInventoryGridPanelGeneric;
	}

	public void sorterChangedAction(int newRowCount) {
		final String text=UIConstants.LABEL_GRID_ROWS + UIUtils.getFormattedNumber(newRowCount);
		extractInventoryGridPanelGeneric.getGridRowsCountLabel().setText(text);
		extractInventoryGridPanelGeneric.updateTotalLabels();
	}

	@Override
	protected void showBalloons(int rowIndex,int columnIndex, JTable fixedTable) {
		int colViewIndex=fixedTable.getColumnModel().getColumnIndex(ExtractInventoryGridSelectionPanel.COLUMN_HEADING_MODULE_NAME);
		if (columnIndex==colViewIndex && rowIndex==0 && !UIConstants.BALLOON_GRID_FILTERING_TRIGGERED) {
			UIConstants.BALLOON_GRID_FILTERING_TRIGGERED=true;
			UIUtils.showCellBalloon("<html>You can use the light blue Filtering bar to search for specific information.<br/>"
	    			+"- For Text columns, you can use the '%' sign as a wildcard.<br/>"
	    			+"- For Number columns, you can use the '>' or '<' signs for more combinations.<br/>"
	    			+"<br/><b>Press ENTER or righ-click to apply your filters.</b>",
					BalloonTip.Orientation.RIGHT_BELOW,fixedTable,rowIndex,columnIndex,isShowBalloons);
		}
	}
	
}