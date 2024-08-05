package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings({ "serial",})
public abstract class FiltersPanel extends AbstractFiltersPanel {

	protected JLabel unappliedFiltersLabel;
	protected FilterLevelSelectionPanel filterLevelSelectionPanel;
	protected FilterOperatingUnitPanel filterOperatingUnitPanel;
	protected FilterUserPanel filterUserPanel;
	protected FilterDatePanel filterDatePanel;
	private FilterGeneralPanel filterGeneralPanel;
	private boolean hasFilterLevelSelectionPanel;
	private boolean hasFilterOperatingUnitPanel;
	private boolean hasFilterUserPanel;
	private boolean hasFilterDatePanel;
	private boolean hasFilterGeneralPanel;
	protected Map<String, String> snapshotEnvironmentProperties;
	protected JPanel centerPanel;
	private SnapshotGridRecord snapshotGridRecord;
	
	public FiltersPanel(JFrame rootFrame,TabSnapshotsPanel tabSnapshotsPanel,SnapshotGridRecord snapshotGridRecord) {
		this(rootFrame,tabSnapshotsPanel,true,true,true,true,true,snapshotGridRecord);
	}
	
	public FiltersPanel(JFrame rootFrame,TabSnapshotsPanel tabSnapshotsPanel,boolean hasFilterOperatingUnitPanel,boolean hasFilterLevelSelectionPanel,
			boolean hasFilterUserPanel,boolean hasFilterDatePanel,boolean hasFilterGeneralPanel,SnapshotGridRecord snapshotGridRecord) {
		this.hasFilterLevelSelectionPanel=hasFilterLevelSelectionPanel;
		this.hasFilterOperatingUnitPanel=hasFilterOperatingUnitPanel;
		this.hasFilterUserPanel=hasFilterUserPanel;
		this.hasFilterDatePanel=hasFilterDatePanel;
		this.hasFilterGeneralPanel=hasFilterGeneralPanel;
		this.rootFrame=rootFrame;
		this.snapshotGridRecord=snapshotGridRecord;
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		setBackground(Color.decode("#4b4f4e"));
		createComponents();
	}

	public void createComponents(){
		centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#4b4f4e"));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		add(centerPanel);

		unappliedFiltersLabel=new JLabel();
		unappliedFiltersLabel.setFont(new Font("Arial", Font.BOLD, 15));
		unappliedFiltersLabel.setForeground(Color.ORANGE);
		unappliedFiltersLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		
		filterGeneralPanel=new FilterGeneralPanel(unappliedFiltersLabel);
		if (tabSnapshotsPanel!=null) {
			snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		}
		filterLevelSelectionPanel=new FilterLevelSelectionPanel(this, unappliedFiltersLabel);
		filterOperatingUnitPanel=new FilterOperatingUnitPanel(rootFrame,unappliedFiltersLabel,snapshotEnvironmentProperties, snapshotGridRecord);
		filterUserPanel=new FilterUserPanel(this,rootFrame,unappliedFiltersLabel,snapshotEnvironmentProperties,snapshotGridRecord);
		filterDatePanel=new FilterDatePanel(this,unappliedFiltersLabel);
		
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterGeneralPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(7, 5)));
		centerPanel.add(filterOperatingUnitPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterLevelSelectionPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(7, 5)));
		centerPanel.add(filterUserPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(7, 5)));
		centerPanel.add(filterDatePanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		
		JPanel panel=new JPanel();
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		panel.setBackground(Color.decode("#4b4f4e"));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panel);
				
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_viewer_apply_filtering.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		applyFilteringButton = new JButton();
		applyFilteringButton.setIcon(ii);
		applyFilteringButton.setBorderPainted(false);
		applyFilteringButton.setContentAreaFilled(false);
		applyFilteringButton.setFocusPainted(false);
		applyFilteringButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_viewer_apply_filtering_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		applyFilteringButton.setRolloverIcon(new RolloverIcon(ii));
		panel.add(applyFilteringButton);
		panel.add(Box.createRigidArea(new Dimension(5, 15)));
		applyFilteringButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				
				applyFiltering();
			}
		}
				);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_viewer_reset_filtering.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetFilteringButton = new JButton();
		resetFilteringButton.setIcon(ii);
		resetFilteringButton.setBorderPainted(false);
		resetFilteringButton.setContentAreaFilled(false);
		resetFilteringButton.setFocusPainted(false);
		resetFilteringButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_viewer_reset_filtering_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetFilteringButton.setRolloverIcon(new RolloverIcon(ii));
		panel.add(resetFilteringButton);
		resetFilteringButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				resetFiltering();
			}
		}
				);
		panel.add(Box.createRigidArea(new Dimension(5, 15)));
		panel.add(unappliedFiltersLabel);
		
		filterLevelSelectionPanel.setVisible(hasFilterLevelSelectionPanel);
		filterOperatingUnitPanel.setVisible(hasFilterOperatingUnitPanel);
		filterUserPanel.setVisible(hasFilterUserPanel);
		filterDatePanel.setVisible(hasFilterDatePanel);
		filterGeneralPanel.setVisible(hasFilterGeneralPanel);
	}

	public abstract void mainFilteringProcess();
	
	public boolean hasUnappliedChanges() {
		return filterGeneralPanel.hasUnappliedChanges() ||
		filterLevelSelectionPanel.hasUnappliedChanges() ||
		filterOperatingUnitPanel.hasUnappliedChanges() ||
		filterUserPanel.hasUnappliedChanges() ||
		filterDatePanel.hasUnappliedChanges() ;
	}
	
	public void setHasUnappliedFilters(boolean hasUnappliedChanges) {
		filterGeneralPanel.setHasUnappliedChanges(hasUnappliedChanges);
		filterLevelSelectionPanel.setHasUnappliedChanges(hasUnappliedChanges);
		filterOperatingUnitPanel.setHasUnappliedChanges(hasUnappliedChanges);
		filterUserPanel.setHasUnappliedChanges(hasUnappliedChanges);
		filterDatePanel.setHasUnappliedChanges(hasUnappliedChanges);
	}
	
	public void resetFilterSelections() {
		filterGeneralPanel.resetFilter();
		filterLevelSelectionPanel.resetFilter();
		filterOperatingUnitPanel.resetFilter();
		filterUserPanel.resetFilter();
		filterDatePanel.resetFilter();
	}
	
	public void applyFiltering() {
		if (!hasUnappliedChanges()) {
			GUIUtils.popupInformationMessage("No unapplied Filters!");
			return;
		}
		setHasUnappliedFilters(false);
		unappliedFiltersLabel.setText("");
		mainFilteringProcess();
	}
	
	private void resetFiltering() {
		setHasUnappliedFilters(false);
		resetFilterSelections();
		unappliedFiltersLabel.setText("");
		mainFilteringProcess();
	}

	public FilterLevelSelectionPanel getFilterLevelSelectionPanel() {
		return filterLevelSelectionPanel;
	}

	public abstract FilterOperatingUnitCommon getFilterOperatingUnitPanel();

	public FilterUserPanel getFilterUserPanel() {
		return filterUserPanel;
	}

	public FilterDatePanel getFilterDatePanel() {
		return filterDatePanel;
	}

	public FilterGeneralPanel getFilterGeneralPanel() {
		return filterGeneralPanel;
	}
	
	public void setComponentsEnabled(boolean isEnabled) {
		filterGeneralPanel.setComponentsEnabled(isEnabled);
		filterLevelSelectionPanel.setComponentsEnabled(isEnabled);
		filterOperatingUnitPanel.setComponentsEnabled(isEnabled);
		filterUserPanel.setComponentsEnabled(isEnabled);
		filterDatePanel.setComponentsEnabled(isEnabled);
		applyFilteringButton.setEnabled(isEnabled);
		resetFilteringButton.setEnabled(isEnabled);
	}
	
}
