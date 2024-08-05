package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public abstract class SnapshotInventoryTotalsPanel extends JPanel {

	private JLabel gridRowsCountLabel;
	private JPanel panel;
	
	public SnapshotInventoryTotalsPanel() {
		setLayout(new BorderLayout());
		
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setOpaque(true);
		panel.setBackground(Color.decode("#dbdcdf"));
		add(panel,BorderLayout.SOUTH);
		gridRowsCountLabel=new JLabel(UIConstants.LABEL_GRID_ROWS);
		panel.add(gridRowsCountLabel);
		InjectUtils.assignArialPlainFont(gridRowsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		gridRowsCountLabel.setBackground(Color.decode("#343836"));
	}
	
	public JLabel getGridRowsCountLabel() {
		return gridRowsCountLabel;
	}

	public abstract void updateTotalLabels();

	public JPanel getPanel() {
		return panel;
	}

}