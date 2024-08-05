package com.rapidesuite.snapshot.view.convert;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.SnapshotInventoryTotalsPanel;

@SuppressWarnings("serial")
public abstract class ConvertGridTotalsPanel extends SnapshotInventoryTotalsPanel {

	private JLabel totalRecordsCountLabel;
	private JLabel totalConfigurationRecordsCountLabel;
	private JLabel totalPostConfigurationRecordsCountLabel;
	private JLabel totalPostImplementationRecordsCountLabel;
	protected ConvertPanelGeneric convertPanelGeneric;
	
	public ConvertGridTotalsPanel(ConvertPanelGeneric convertPanelGeneric) {
		super();
		this.convertPanelGeneric=convertPanelGeneric;
		JPanel panel=super.getPanel();
		
		int spaceWidth=50;
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalRecordsCountLabel=new JLabel();
		panel.add(totalRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalConfigurationRecordsCountLabel=new JLabel();
		panel.add(totalConfigurationRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalConfigurationRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalConfigurationRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalPostConfigurationRecordsCountLabel=new JLabel();
		panel.add(totalPostConfigurationRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalPostConfigurationRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalPostConfigurationRecordsCountLabel.setBackground(Color.decode("#343836"));
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
		
		totalPostImplementationRecordsCountLabel=new JLabel();
		panel.add(totalPostImplementationRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalPostImplementationRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalPostImplementationRecordsCountLabel.setBackground(Color.decode("#343836"));
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
	
}
