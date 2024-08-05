package com.rapidesuite.snapshot.view;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;

@SuppressWarnings("serial")
public abstract class SnapshotInventoryGridFrame extends JFrame{

	protected TabSnapshotsPanel tabSnapshotsPanel;
	private SnapshotInventoryGridActionPanel snapshotInventoryGridActionPanel;
	
	public SnapshotInventoryGridFrame(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
	}
	
	public SnapshotInventoryGridActionPanel getSnapshotInventoryGridActionPanel() {
		return snapshotInventoryGridActionPanel;
	}

	public abstract void viewChanges(int viewRow);
	
	public abstract void runDefaultProcess();

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}
	
	public void updateTotalLabels(SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel,
		JLabel executionFailedRowsCountLabelValue) {
		int sumTotalFailedRows=0;
		for(int row = 0;row < snapshotInventoryDetailsGridPanel.getTable().getRowCount();row++) {
			int modelRowIndex=snapshotInventoryDetailsGridPanel.getTable().convertRowIndexToModel(row);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotInventoryDetailsGridPanel.getSnapshotInventoryGridRecordsList().get(modelRowIndex);
			if (snapshotInventoryGridRecord.getStatus().equals(UIConstants.UI_STATUS_FAILED)) {
				sumTotalFailedRows++;
			}
        }
		String formattedSumTotalFailedRowsNumber="";
		try {
			formattedSumTotalFailedRowsNumber = Utils.formatNumberWithComma(sumTotalFailedRows);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		if (sumTotalFailedRows>0) {
			executionFailedRowsCountLabelValue.setOpaque(true);
			executionFailedRowsCountLabelValue.setBackground(UIConstants.COLOR_RED);
		}
		else {
			executionFailedRowsCountLabelValue.setOpaque(false);
		}
		executionFailedRowsCountLabelValue.setText(" "+formattedSumTotalFailedRowsNumber+" ");
	}

	public void setSnapshotInventoryGridActionPanel(SnapshotInventoryGridActionPanel snapshotInventoryGridActionPanel) {
		this.snapshotInventoryGridActionPanel = snapshotInventoryGridActionPanel;
	}
	
}
