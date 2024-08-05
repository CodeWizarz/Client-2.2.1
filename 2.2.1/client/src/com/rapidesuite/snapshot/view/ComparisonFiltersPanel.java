package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

@SuppressWarnings("serial")
public abstract class ComparisonFiltersPanel extends AbstractFiltersPanel {

	protected JLabel unappliedFiltersLabel;
	private FilterOperatingUnitPanel filterOperatingUnitPanel;
	private FilterLevelSelectionPanel filterLevelSelectionPanel;
	private FilterComparisonGeneralPanel filterComparisonGeneralPanel;
	
	public ComparisonFiltersPanel(JFrame rootFrame,TabSnapshotsPanel tabSnapshotsPanel) {
		this.rootFrame=rootFrame;
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#4b4f4e"));
		createComponents();
	}

	public void createComponents(){
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#4b4f4e"));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(centerPanel,BorderLayout.CENTER);

		unappliedFiltersLabel=new JLabel();
		unappliedFiltersLabel.setFont(new Font("Arial", Font.BOLD, 15));
		unappliedFiltersLabel.setForeground(Color.ORANGE);
		
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		filterLevelSelectionPanel=new FilterLevelSelectionPanel(this,unappliedFiltersLabel);
		filterOperatingUnitPanel=new FilterOperatingUnitPanel(rootFrame,unappliedFiltersLabel,snapshotEnvironmentProperties,null);
		filterComparisonGeneralPanel=new FilterComparisonGeneralPanel(unappliedFiltersLabel);
		
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterOperatingUnitPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterLevelSelectionPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterComparisonGeneralPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		
		//centerPanel.add(Box.createHorizontalGlue());
		
		JPanel panel=new JPanel();
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		panel.setBackground(Color.decode("#4b4f4e"));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panel,BorderLayout.SOUTH);
				
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
		panel.add(Box.createRigidArea(new Dimension(15, 15)));
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
		panel.add(Box.createRigidArea(new Dimension(15, 15)));
		panel.add(unappliedFiltersLabel);
	}

	public abstract void mainFilteringProcess();
	
	private boolean hasUnappliedChanges() {
		return filterOperatingUnitPanel.hasUnappliedChanges() || filterComparisonGeneralPanel.hasUnappliedChanges();
	}
	
	private void setHasUnappliedFilters(boolean hasUnappliedChanges) {
		filterOperatingUnitPanel.setHasUnappliedChanges(hasUnappliedChanges);
		filterComparisonGeneralPanel.setHasUnappliedChanges(hasUnappliedChanges);
	}
	
	private void resetFilterSelections() {
		filterOperatingUnitPanel.resetFilter();
		filterComparisonGeneralPanel.resetFilter();
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

	public FilterOperatingUnitPanel getFilterOperatingUnitPanel() {
		return filterOperatingUnitPanel;
	}

	public FilterLevelSelectionPanel getFilterLevelSelectionPanel() {
		return filterLevelSelectionPanel;
	}

	public FilterComparisonGeneralPanel getFilterComparisonGeneralPanel() {
		return filterComparisonGeneralPanel;
	}
	
}