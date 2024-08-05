package com.rapidesuite.extract.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.inject.gui.ExecutionPanel;

@SuppressWarnings({ "serial",})
public class FiltersPanel extends JPanel {

	private JLabel unappliedFiltersLabel;
	private JFrame rootFrame;
	private ExtractInventoryGridSelectionPanel extractInventoryGridSelectionPanel;
	
	private FilterGeneralPanel filterGeneralPanel;
	private FilterBusinessUnit filterBusinessUnit;
	private FilterUserPanel filterUserPanel;
	private FilterDatePanel filterDatePanel;
	
	public FiltersPanel(JFrame rootFrame,ExtractInventoryGridSelectionPanel extractInventoryGridSelectionPanel) {
		this.rootFrame=rootFrame;
		this.extractInventoryGridSelectionPanel=extractInventoryGridSelectionPanel;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		createComponents();
	}

	public void createComponents(){
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		add(centerPanel);

		unappliedFiltersLabel=new JLabel();
		unappliedFiltersLabel.setFont(new Font("Arial", Font.BOLD, 15));
		unappliedFiltersLabel.setForeground(Color.ORANGE);
		
		filterGeneralPanel=new FilterGeneralPanel(this);
		filterBusinessUnit=new FilterBusinessUnit(this);
		filterUserPanel=new FilterUserPanel(this);
		filterDatePanel=new FilterDatePanel(this);
		
		centerPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		centerPanel.add(filterBusinessUnit);
		centerPanel.add(Box.createRigidArea(new Dimension(7, 5)));
		centerPanel.add(filterUserPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(7, 5)));
		centerPanel.add(filterDatePanel);
		centerPanel.add(Box.createRigidArea(new Dimension(7, 5)));
		centerPanel.add(filterGeneralPanel);
		centerPanel.add(Box.createRigidArea(new Dimension(7, 5)));
		
		JPanel panel=new JPanel();
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		panel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panel);
			
		panel.add(Box.createRigidArea(new Dimension(15, 15)));
		panel.add(unappliedFiltersLabel);
	}
		
	public FilterGeneralPanel getFilterGeneralPanel() {
		return filterGeneralPanel;
	}

	public JFrame getRootFrame() {
		return rootFrame;
	}

	public ExtractInventoryGridSelectionPanel getExtractInventoryGridSelectionPanel() {
		return extractInventoryGridSelectionPanel;
	}

	public JLabel getUnappliedFiltersLabel() {
		return unappliedFiltersLabel;
	}

	public void lockUI() {
		filterGeneralPanel.lockUI();
	}

	public void unlockUI() {
		filterGeneralPanel.unlockUI();
	}

	public FilterBusinessUnit getFilterBusinessUnit() {
		return filterBusinessUnit;
	}

	public FilterUserPanel getFilterUserPanel() {
		return filterUserPanel;
	}

	public FilterDatePanel getFilterDatePanel() {
		return filterDatePanel;
	}
	
}