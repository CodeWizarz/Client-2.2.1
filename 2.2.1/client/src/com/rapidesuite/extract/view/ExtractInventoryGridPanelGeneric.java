package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.snapshot.view.UIConstants;

@SuppressWarnings("serial")
public abstract class ExtractInventoryGridPanelGeneric extends JPanel {

	protected JLabel gridRowsCountLabel;
	protected JLabel totalRecordsCountLabel;
	protected ExtractMain extractMain;
	protected JPanel northPanel;
	protected JPanel centerPanel;
	protected JPanel southPanel;
	
	public ExtractInventoryGridPanelGeneric(ExtractMain extractMain) {
		this.extractMain=extractMain;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		createComponentsGeneric();
	}
	
	private void createComponentsGeneric(){
		northPanel=new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setOpaque(true);
		northPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		add(northPanel,BorderLayout.NORTH);
		
		centerPanel=new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		add(centerPanel,BorderLayout.CENTER);

		southPanel=new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		southPanel.setOpaque(false);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		southPanel.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
		add(southPanel,BorderLayout.SOUTH);
		
		gridRowsCountLabel=new JLabel(UIConstants.LABEL_GRID_ROWS);
		southPanel.add(gridRowsCountLabel);
		InjectUtils.assignArialPlainFont(gridRowsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		gridRowsCountLabel.setForeground(Color.decode("#2F3436"));
		
		southPanel.add(Box.createRigidArea(new Dimension(50, 15)));
		
		totalRecordsCountLabel=new JLabel();
		southPanel.add(totalRecordsCountLabel);
		InjectUtils.assignArialPlainFont(totalRecordsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalRecordsCountLabel.setForeground(Color.decode("#2F3436"));
	}

	public ExtractMain getExtractMain() {
		return extractMain;
	}
	
	public JLabel getGridRowsCountLabel() {
		return gridRowsCountLabel;
	}

	public abstract void updateTotalLabels();
	
}
