package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class FilterOperatingUnitPanel extends FilterOperatingUnitCommon {

	private JCheckBox selectionCheckBox;
		
	public FilterOperatingUnitPanel(JFrame rootFrame,JLabel unappliedFiltersLabel,Map<String, String> snapshotEnvironmentProperties,
			SnapshotGridRecord snapshotGridRecord) {
		super(rootFrame,unappliedFiltersLabel,snapshotEnvironmentProperties, snapshotGridRecord);
		createComponents();
	}

	public void createComponents(){
		super.createComponents();
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectionPanel.add(tempPanel);
		selectionCheckBox=new JCheckBox("Enable Selection (All Operating Units are included by default)");
		tempPanel.add(selectionCheckBox);
		InjectUtils.assignArialPlainFont(selectionCheckBox,InjectMain.FONT_SIZE_NORMAL);
		selectionCheckBox.setForeground(Color.WHITE);
		selectionCheckBox.setOpaque(false);
		selectionCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSelection();
			}

		});
	}

	protected void processActionSelection() {
		filterSelectionPanel.setVisible(selectionCheckBox.isSelected());
		setUnappliedFilter();
	}
	
	public void resetFilter() {
		selectionCheckBox.setSelected(false);
		filterSelectionPanel.setVisible(false);
	}
	
	public boolean isOperatingUnitFilterEnabled() {
		return selectionCheckBox.isSelected();
	}
	
}
