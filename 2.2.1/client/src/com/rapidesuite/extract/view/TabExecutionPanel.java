package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.inject.gui.ExecutionPanel;

@SuppressWarnings("serial")
public class TabExecutionPanel extends JPanel{

	private ExtractMain extractMain;
	private ExtractInventoryGridResultPanel extractInventoryGridResultPanel;
	private JPanel executionCenterPanel;
	private JPanel executionSouthPanel;
	
	public TabExecutionPanel(ExtractMain extractMain) {
		this.extractMain=extractMain;
		this.setLayout(new BorderLayout());
		
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

		extractInventoryGridResultPanel=new ExtractInventoryGridResultPanel(extractMain);
		executionCenterPanel.add(extractInventoryGridResultPanel,BorderLayout.CENTER);
	}

	public ExtractMain getExtractMain() {
		return extractMain;
	}

	public ExtractInventoryGridResultPanel getExtractInventoryGridResultPanel() {
		return extractInventoryGridResultPanel;
	}
	
}
