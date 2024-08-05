package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class ViewerInventoryTotalsPanel extends SnapshotInventoryTotalsPanel {

	private SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel;
	private JLabel totalRecordsCountLabel;
	private JLabel totalUpdatedRecordsCountLabel;
	private JLabel totalAddedRecordsCountLabel;
	private JLabel totalDefaultRecordsCountLabel;
	
	public ViewerInventoryTotalsPanel(SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel,int topBorderSpace) {
		super();
		this.snapshotInventoryDetailsGridPanel=snapshotInventoryDetailsGridPanel;
		JPanel panel=super.getPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(topBorderSpace, 0, 0, 0));
		int spaceWidth=50;
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));

		totalRecordsCountLabel=new JLabel(UIConstants.LABEL_GRID_TOTAL_RECORDS);
		panel.add(totalRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalDefaultRecordsCountLabel=new JLabel(UIConstants.LABEL_GRID_TOTAL_DEFAULT_RECORDS);
		panel.add(totalDefaultRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalDefaultRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalDefaultRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalAddedRecordsCountLabel=new JLabel(UIConstants.LABEL_GRID_TOTAL_UPDATED_RECORDS);
		panel.add(totalAddedRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalAddedRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalAddedRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalUpdatedRecordsCountLabel=new JLabel(UIConstants.LABEL_GRID_TOTAL_ADDED_RECORDS);
		panel.add(totalUpdatedRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalUpdatedRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalUpdatedRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
	}
	
	public JLabel getTotalRecordsCountLabel() {
		return totalRecordsCountLabel;
	}

	public JLabel getTotalUpdatedRecordsCountLabel() {
		return totalUpdatedRecordsCountLabel;
	}

	public JLabel getTotalAddedRecordsCountLabel() {
		return totalAddedRecordsCountLabel;
	}
	
	public JLabel getTotalDefaultRecordsCountLabel() {
		return totalDefaultRecordsCountLabel;
	}

	@Override
	public void updateTotalLabels() {
		int sumTotalRecords=0;
		int sumTotalAddedRecords=0;
		int sumTotalUpdatedRecords=0;
		int sumTotalDefaultRecords=0;
		boolean isDisplayTotalAddedRecords=true;
		boolean isDisplayTotalDefaultRecords=true;
		boolean isDisplayTotalUpdatedRecords=true;
		for(int row = 0;row < snapshotInventoryDetailsGridPanel.getTable().getRowCount();row++) {
			int modelRowIndex=snapshotInventoryDetailsGridPanel.getTable().convertRowIndexToModel(row);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotInventoryDetailsGridPanel.getSnapshotInventoryGridRecordsList().get(modelRowIndex);
			sumTotalRecords=sumTotalRecords+snapshotInventoryGridRecord.getTotalRecords();
			sumTotalAddedRecords=sumTotalAddedRecords+snapshotInventoryGridRecord.getTotalAddedRecords();
			sumTotalUpdatedRecords=sumTotalUpdatedRecords+snapshotInventoryGridRecord.getTotalUpdatedRecords();
			sumTotalDefaultRecords=sumTotalDefaultRecords+snapshotInventoryGridRecord.getTotalDefaultRecords();
			
			isDisplayTotalAddedRecords=snapshotInventoryGridRecord.isDisplayTotalAddedRecords();
			isDisplayTotalDefaultRecords=snapshotInventoryGridRecord.isDisplayTotalDefaultRecords();
			isDisplayTotalUpdatedRecords=snapshotInventoryGridRecord.isDisplayTotalUpdatedRecords();
        }
		String formattedNumber="";
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_RECORDS+formattedNumber);
		
		if (isDisplayTotalAddedRecords) {
			formattedNumber="";
			try {
				formattedNumber = Utils.formatNumberWithComma(sumTotalAddedRecords);
			} catch (Exception e) {
				FileUtils.printStackTrace(e);
			}
			getTotalAddedRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_ADDED_RECORDS+formattedNumber);
		}
		else {
			getTotalAddedRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_ADDED_RECORDS+UIConstants.UI_NA);
		}
		
		if (isDisplayTotalUpdatedRecords) {
			formattedNumber="";
			try {
				formattedNumber = Utils.formatNumberWithComma(sumTotalUpdatedRecords);
			} catch (Exception e) {
				FileUtils.printStackTrace(e);
			}
			getTotalUpdatedRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_UPDATED_RECORDS+formattedNumber);
		}
		else {
			getTotalUpdatedRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_UPDATED_RECORDS+UIConstants.UI_NA);
		}
		
		if (isDisplayTotalDefaultRecords) {
			formattedNumber="";
			try {
				formattedNumber = Utils.formatNumberWithComma(sumTotalDefaultRecords);
			} catch (Exception e) {
				FileUtils.printStackTrace(e);
			}
			getTotalDefaultRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_DEFAULT_RECORDS+formattedNumber);
		}
		else {
			getTotalDefaultRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_DEFAULT_RECORDS+UIConstants.UI_NA);
		}
		
	}
	
}