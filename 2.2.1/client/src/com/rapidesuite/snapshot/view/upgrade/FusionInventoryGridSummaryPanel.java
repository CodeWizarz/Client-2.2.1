package com.rapidesuite.snapshot.view.upgrade;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.SnapshotInventoryTotalsPanel;
import com.rapidesuite.snapshot.view.UIConstants;

@SuppressWarnings("serial")
public class FusionInventoryGridSummaryPanel extends SnapshotInventoryTotalsPanel {

	private JLabel totalRecordsCountLabel;
	private JLabel totalConfigurationRecordsCountLabel;
	private JLabel totalPostConfigurationRecordsCountLabel;
	private JLabel totalPostImplementationRecordsCountLabel;
	private JLabel totalPostImplementationObsoleteRecordsCountLabel;
	protected GenericInventoryGridPanel genericInventoryGridPanel;
	
	public FusionInventoryGridSummaryPanel(GenericInventoryGridPanel genericInventoryGridPanel) {
		super();
		this.genericInventoryGridPanel=genericInventoryGridPanel;
		JPanel panel=super.getPanel();
		
		int spaceWidth=50;
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 10)));
		
		totalRecordsCountLabel=new JLabel();
		panel.add(totalRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 10)));
		
		totalConfigurationRecordsCountLabel=new JLabel();
		panel.add(totalConfigurationRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalConfigurationRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalConfigurationRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 10)));
		
		totalPostConfigurationRecordsCountLabel=new JLabel();
		panel.add(totalPostConfigurationRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalPostConfigurationRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalPostConfigurationRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 10)));
		
		totalPostImplementationRecordsCountLabel=new JLabel();
		panel.add(totalPostImplementationRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalPostImplementationRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalPostImplementationRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 10)));
		
		totalPostImplementationObsoleteRecordsCountLabel=new JLabel();
		panel.add(totalPostImplementationObsoleteRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalPostImplementationObsoleteRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalPostImplementationObsoleteRecordsCountLabel.setBackground(Color.decode("#343836"));
	}
	
	public JLabel getTotalRecordsCountLabel() {
		return totalRecordsCountLabel;
	}

	public JLabel getTotalConfigurationRecordsCountLabel() {
		return totalConfigurationRecordsCountLabel;
	}

	public JLabel getTotalPostConfigurationRecordsCountLabel() {
		return totalPostConfigurationRecordsCountLabel;
	}

	public JLabel getTotalPostImplementationRecordsCountLabel() {
		return totalPostImplementationRecordsCountLabel;
	}
	
	public JLabel getTotalPostImplementationObsoleteRecordsCountLabel() {
		return totalPostImplementationObsoleteRecordsCountLabel;
	}
	
	public void updateTotalLabels() {
		int sumTotalRecords=0;
		int sumTotalConfigurationRecords=0;
		int sumTotalPostConfigurationRecords=0;
		int sumTotalPostImplementationRecords=0;
		int sumTotalPostImplementationObsoleteRecords=0;
		for(int row = 0;row < genericInventoryGridPanel.getTable().getRowCount();row++) {
			int modelRowIndex=genericInventoryGridPanel.getTable().convertRowIndexToModel(row);
			FusionInventoryRow fusionInventoryRow=((FusionInventoryGridPanel)genericInventoryGridPanel).getFusionInventoryRowList()
					.get(modelRowIndex);
			sumTotalRecords=sumTotalRecords+fusionInventoryRow.getTotalRecords();
			sumTotalConfigurationRecords=sumTotalConfigurationRecords+fusionInventoryRow.getTotalRecordsConfiguration();
			sumTotalPostConfigurationRecords=sumTotalPostConfigurationRecords+fusionInventoryRow.getTotalRecordsPostConfiguration();
			sumTotalPostImplementationRecords=sumTotalPostImplementationRecords+fusionInventoryRow.getTotalRecordsPostImplementation();
			sumTotalPostImplementationObsoleteRecords=sumTotalPostImplementationObsoleteRecords+fusionInventoryRow.getTotalRecordsPostImplementationObsolete();
        }
		String formattedNumber="";
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_RECORDS_CREATED+formattedNumber);
		
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalConfigurationRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalConfigurationRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_CONFIGURATION_RECORDS_CREATED+formattedNumber);
		
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalPostConfigurationRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalPostConfigurationRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_POST_CONFIGURATION_RECORDS_CREATED+formattedNumber);
		
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalPostImplementationRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalPostImplementationRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_POST_IMPLEMENTATION_RECORDS_CREATED+formattedNumber);
		
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalPostImplementationObsoleteRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalPostImplementationObsoleteRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_POST_IMPLEMENTATION_OBSOLETE_RECORDS_CREATED+formattedNumber);
	}
	
}