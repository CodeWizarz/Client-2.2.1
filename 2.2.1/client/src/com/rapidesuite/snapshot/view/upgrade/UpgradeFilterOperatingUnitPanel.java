package com.rapidesuite.snapshot.view.upgrade;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.OperatingUnit;
import com.rapidesuite.snapshot.view.FilterOperatingUnitCommon;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;

@SuppressWarnings("serial")
public class UpgradeFilterOperatingUnitPanel  extends FilterOperatingUnitCommon {

	private JRadioButton allOUButton;
	private JRadioButton specifyOUButton;
	private TabUpgradeMainPanel tabUpgradeMainPanel;
		
	public UpgradeFilterOperatingUnitPanel(TabUpgradeMainPanel tabUpgradeMainPanel,JLabel unappliedFiltersLabel,
			Map<String, String> snapshotEnvironmentProperties,SnapshotGridRecord snapshotGridRecord) {
		super(tabUpgradeMainPanel.getUpgradeFrame(),unappliedFiltersLabel,snapshotEnvironmentProperties,snapshotGridRecord);
		this.tabUpgradeMainPanel=tabUpgradeMainPanel;
		createComponents();
	}

	public void createComponents(){
		super.createComponents();
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectionPanel.add(tempPanel);
		
		allOUButton = new JRadioButton("All Operating Unit");
		InjectUtils.assignArialPlainFont(allOUButton,InjectMain.FONT_SIZE_NORMAL);
		allOUButton.setForeground(Color.WHITE);
		allOUButton.setOpaque(false);
		allOUButton.setSelected(true);
		allOUButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSelection();
			}
		});
		
		specifyOUButton = new JRadioButton("Specify Operating Unit");
		InjectUtils.assignArialPlainFont(specifyOUButton,InjectMain.FONT_SIZE_NORMAL);
		specifyOUButton.setForeground(Color.WHITE);
		specifyOUButton.setOpaque(false);
		specifyOUButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSelection();
			}

		});
		
		ButtonGroup group = new ButtonGroup();
	    group.add(allOUButton);
	    group.add(specifyOUButton);
	    tempPanel.add(allOUButton);
	    tempPanel.add(specifyOUButton);
	}

	protected void processActionSelection() {
		filterSelectionPanel.setVisible(specifyOUButton.isSelected());
		setUnappliedFilter();
		tabUpgradeMainPanel.getEbsFiltersPanel().getApplyFilteringButton().setEnabled(true);
		/*
		if (specifyOUButton.isSelected()) {
			tabUpgradeMainPanel.getVerticalSplitPane().setDividerLocation(0.38);
		}
		else {
			tabUpgradeMainPanel.getVerticalSplitPane().setDividerLocation(0.19);
		}
		*/
	}
	
	public void resetFilter() {
		allOUButton.setSelected(true);
		specifyOUButton.setSelected(false);
	}
	
	public void setComponentsEnabled(boolean isEnabled) {
		allOUButton.setEnabled(isEnabled);
		specifyOUButton.setEnabled(isEnabled);
	}
	
	public void setUnappliedFilter() {
		setHasUnappliedChanges(true);
		unappliedFiltersLabel.setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
		tabUpgradeMainPanel.getEbsFiltersPanel().getApplyFilteringButton().setEnabled(true);
	}

	public boolean isOperatingUnitFilterEnabled() {
		List<OperatingUnit> selectedOperatingUnits=getSelectedOperatingUnits();
		return specifyOUButton.isSelected() && !selectedOperatingUnits.isEmpty();
	}
	
}