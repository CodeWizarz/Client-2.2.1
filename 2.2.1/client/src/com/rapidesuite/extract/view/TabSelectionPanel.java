package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;

@SuppressWarnings("serial")
public class TabSelectionPanel extends JPanel{

	private ExtractMain extractMain;
	private JLabel selectedPackageLabel;
	private JPanel executionNorthPanel;
	private JPanel executionCenterPanel;
	private JPanel executionSouthPanel;
	private ExtractInventoryGridSelectionPanel extractInventoryGridSelectionPanel;
	
	public TabSelectionPanel(ExtractMain extractMain) {
		this.extractMain=extractMain;
		this.setLayout(new BorderLayout());
		
		executionNorthPanel = new JPanel();
		executionNorthPanel.setLayout(new BoxLayout(executionNorthPanel, BoxLayout.X_AXIS));
		executionNorthPanel.setOpaque(true);
		executionNorthPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
		executionNorthPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		this.add(executionNorthPanel, BorderLayout.NORTH);

		executionCenterPanel = new JPanel();
		executionCenterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		executionCenterPanel.setOpaque(true);
		executionCenterPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		executionCenterPanel.setLayout(new BoxLayout(executionCenterPanel, BoxLayout.Y_AXIS));
		this.add(executionCenterPanel, BorderLayout.CENTER);

		executionSouthPanel = new JPanel();
		executionSouthPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		executionSouthPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		executionSouthPanel.setLayout(new BorderLayout());
		this.add(executionSouthPanel, BorderLayout.SOUTH);

		selectedPackageLabel=new JLabel("");
		selectedPackageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectedPackageLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		selectedPackageLabel.setForeground(Color.decode("#cccccc"));
		InjectUtils.assignArialPlainFont(selectedPackageLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		executionNorthPanel.add(selectedPackageLabel);
		
		extractInventoryGridSelectionPanel=new ExtractInventoryGridSelectionPanel(extractMain);
		
		executionCenterPanel.add(extractInventoryGridSelectionPanel,BorderLayout.CENTER);
	}

	public void displaySelectedPackagePathName(String text) {
		selectedPackageLabel.setText("Selected Extraction Package: "+text);
	}

	public ExtractMain getExtractMain() {
		return extractMain;
	}

	public ExtractInventoryGridSelectionPanel getExtractInventoryGridSelectionPanel() {
		return extractInventoryGridSelectionPanel;
	}

	public void lockUI() {
		
	}

	public void unlockUI() {
		// TODO Auto-generated method stub
		
	}

}
