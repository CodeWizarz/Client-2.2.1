package com.rapidesuite.snapshot.view.upgrade;

import java.awt.Dimension;

import javax.swing.Box;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.view.FilterOperatingUnitCommon;
import com.rapidesuite.snapshot.view.FiltersPanel;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;

@SuppressWarnings({ "serial",})
public class EBSFiltersPanel extends FiltersPanel {

	private TabUpgradeMainPanel tabUpgradeMainPanel;
	private UpgradeFilterOperatingUnitPanel upgradeFilterOperatingUnitPanel;

	public EBSFiltersPanel(TabUpgradeMainPanel tabUpgradeMainPanel,SnapshotGridRecord snapshotGridRecord) {
		super(tabUpgradeMainPanel.getUpgradeFrame(),tabUpgradeMainPanel.getUpgradeFrame().getTabSnapshotsPanel(),
				false,false,false,false,false,snapshotGridRecord);
		this.tabUpgradeMainPanel=tabUpgradeMainPanel;
		
		upgradeFilterOperatingUnitPanel=new UpgradeFilterOperatingUnitPanel(tabUpgradeMainPanel,unappliedFiltersLabel,
				super.snapshotEnvironmentProperties,snapshotGridRecord);

		centerPanel.add(upgradeFilterOperatingUnitPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterLevelSelectionPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterUserPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterDatePanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		
		filterLevelSelectionPanel.setVisible(true);
		filterUserPanel.setVisible(true);
		filterDatePanel.setVisible(true);
		getResetFilteringButton().setVisible(false);
	}
	
	public boolean hasUnappliedChanges() {
		return filterLevelSelectionPanel.hasUnappliedChanges()||
				upgradeFilterOperatingUnitPanel.hasUnappliedChanges()||
				filterUserPanel.hasUnappliedChanges() ||
				filterDatePanel.hasUnappliedChanges() ;
	}
	
	public void setHasUnappliedFilters(boolean hasUnappliedChanges) {
		filterLevelSelectionPanel.setHasUnappliedChanges(hasUnappliedChanges);
		upgradeFilterOperatingUnitPanel.setHasUnappliedChanges(hasUnappliedChanges);
		filterUserPanel.setHasUnappliedChanges(hasUnappliedChanges);
		filterDatePanel.setHasUnappliedChanges(hasUnappliedChanges);
	}
	
	public void resetFilterSelections() {
		filterLevelSelectionPanel.resetFilter();
		upgradeFilterOperatingUnitPanel.resetFilter();
		filterUserPanel.resetFilter();
		filterDatePanel.resetFilter();
	}
	
	public void setComponentsEnabled(boolean isEnabled) {
		filterLevelSelectionPanel.setComponentsEnabled(isEnabled);
		upgradeFilterOperatingUnitPanel.setComponentsEnabled(isEnabled);
		filterUserPanel.setComponentsEnabled(isEnabled);
		filterDatePanel.setComponentsEnabled(isEnabled);
	}
	
	public void mainFilteringProcess() {
		try {
			getApplyFilteringButton().setEnabled(false);
			tabUpgradeMainPanel.getUpgradeMainFilterFrame().setVisible(false);
			tabUpgradeMainPanel.startFiltering();
			
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}

	public UpgradeFilterOperatingUnitPanel getUpgradeFilterOperatingUnitPanel() {
		return upgradeFilterOperatingUnitPanel;
	}
	
	public FilterOperatingUnitCommon getFilterOperatingUnitPanel() {
		return upgradeFilterOperatingUnitPanel;
	}

}