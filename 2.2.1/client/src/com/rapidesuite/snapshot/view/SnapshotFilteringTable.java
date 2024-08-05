package com.rapidesuite.snapshot.view;

import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.rapidesuite.client.common.util.FileUtils;

import net.java.balloontip.BalloonTip;

public class SnapshotFilteringTable extends FilteringTable{

	private SnapshotInventoryTotalsPanel snapshotInventoryTotalsPanel;
	private boolean isShowBalloons;
	
	public SnapshotFilteringTable(Vector<String> columnNames,
			JScrollPane variableScroll, JTable dataTable, int headerHeight,
			SnapshotInventoryTotalsPanel snapshotInventoryTotalsPanel,
			boolean isRefreshEveryListOnUse, boolean isShowBalloons) {
		super(columnNames, variableScroll, dataTable, headerHeight,isRefreshEveryListOnUse);
		this.snapshotInventoryTotalsPanel=snapshotInventoryTotalsPanel;
		this.isShowBalloons=isShowBalloons;
	}

	public void sorterChangedAction(int newRowCount) {
		if (snapshotInventoryTotalsPanel==null) {
			return;
		}
		final String text=UIConstants.LABEL_GRID_ROWS + UIUtils.getFormattedNumber(newRowCount);
    	snapshotInventoryTotalsPanel.getGridRowsCountLabel().setText(text);
    	snapshotInventoryTotalsPanel.updateTotalLabels();
	}

	@Override
	protected void showBalloons(int rowIndex,int columnIndex, JTable fixedTable) {
		try{
			int colViewIndex=fixedTable.getColumnModel().getColumnIndex(SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_INVENTORY_NAME);
			if (columnIndex==colViewIndex && rowIndex==0 && !UIConstants.BALLOON_GRID_FILTERING_TRIGGERED) {
				UIConstants.BALLOON_GRID_FILTERING_TRIGGERED=true;
				UIUtils.showCellBalloon("<html>You can use the light blue Filtering bar to search for specific information.<br/>"
						+"- For Text columns, you can use the '%' sign as a wildcard.<br/>"
						+"- For Number columns, you can use the '>' or '<' signs for more combinations.<br/>"
						+"<br/><b>Press ENTER or righ-click to apply your filters.</b>",
						BalloonTip.Orientation.RIGHT_BELOW,fixedTable,rowIndex,columnIndex, isShowBalloons);
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
		}
	}
	
}
