package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class ComparisonInventoryTotalsPanel extends SnapshotInventoryTotalsPanel {

	private SnapshotComparisonAnalysisGridPanel snapshotComparisonAnalysisGridPanel;
	private JLabel totalRecordsCountLabel;
	private JLabel totalChangesCountLabel;
	
	public ComparisonInventoryTotalsPanel(SnapshotComparisonAnalysisGridPanel snapshotComparisonAnalysisGridPanel) {
		super();
		this.snapshotComparisonAnalysisGridPanel=snapshotComparisonAnalysisGridPanel;
		JPanel panel=super.getPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
		int spaceWidth=50;
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalRecordsCountLabel=new JLabel(UIConstants.LABEL_GRID_TOTAL_RECORDS);
		InjectUtils.assignArialPlainFont(totalRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(totalRecordsCountLabel);
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalChangesCountLabel=new JLabel(UIConstants.LABEL_GRID_TOTAL_CHANGES);
		panel.add(totalChangesCountLabel);
		InjectUtils.assignArialPlainFont(totalChangesCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalChangesCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
	}
	
	public JLabel getTotalRecordsCountLabel() {
		return totalRecordsCountLabel;
	}

	public JLabel getTotalChangesCountLabel() {
		return totalChangesCountLabel;
	}

	@Override
	public void updateTotalLabels() {
		int sumTotalRecords=0;
		int sumTotalChanges=0;
		for(int row = 0;row < snapshotComparisonAnalysisGridPanel.getTable().getRowCount();row++) {
			int modelRowIndex=snapshotComparisonAnalysisGridPanel.getTable().convertRowIndexToModel(row);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotComparisonAnalysisGridPanel.getSnapshotInventoryGridRecordsList().get(modelRowIndex);
			sumTotalRecords=sumTotalRecords+snapshotInventoryGridRecord.getTotalRecords();
			List<ComparisonChangesRecord> comparisonChangesRecords=snapshotInventoryGridRecord.getComparisonChangesRecordList();
			for (ComparisonChangesRecord comparisonChangesRecord:comparisonChangesRecords) {
				sumTotalChanges=sumTotalChanges+comparisonChangesRecord.getTotalChanges();
			}
        }
		String formattedNumber="";
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_RECORDS+formattedNumber);
		formattedNumber="";
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalChanges);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalChangesCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_CHANGES+formattedNumber);
	}

}