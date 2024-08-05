package com.rapidesuite.snapshot.view;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ViewerSnapshotInventoryManagementTotalsPanel extends SnapshotInventoryTotalsPanel {

	private SnapshotInventoryManagementGridPanel snapshotInventoryManagementGridPanel;
	
	public ViewerSnapshotInventoryManagementTotalsPanel(SnapshotInventoryManagementGridPanel snapshotInventoryManagementGridPanel,int topBorderSpace) {
		super();
		this.snapshotInventoryManagementGridPanel=snapshotInventoryManagementGridPanel;
		JPanel panel=super.getPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(topBorderSpace, 0, 0, 0));
		int spaceWidth=50;
		panel.add(Box.createRigidArea(new Dimension(spaceWidth, 15)));
	}

	@Override
	public void updateTotalLabels() {
		
	}
	
}