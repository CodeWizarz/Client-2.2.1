package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class TableGridFrame extends JFrame  {

	protected JLabel gridRowsCountLabel;
	
	public TableGridFrame(ExtractMain extractMain,TableGrid tableGrid) throws Exception {
		String title=tableGrid.getInventory().getName();
		this.setIconImage(GUIUtils.getImageIcon(this.getClass(), ExtractMain.getSharedApplicationIconPath()).getImage());
		this.setTitle(title);
		this.setSize(UIConstants.TABLE_GRID_FRAME_WIDTH, UIConstants.TABLE_GRID_FRAME_HEIGHT);
		UIUtils.setFramePosition(extractMain.getRootFrame(),this);
		this.setLayout(new BorderLayout());
		
		JPanel mainPanel=new JPanel();
		add(mainPanel,BorderLayout.CENTER);
		mainPanel.setOpaque(true);
		mainPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		mainPanel.setLayout(new BorderLayout());
		
		JScrollPane variableScroll = new JScrollPane(tableGrid.getTable(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};
		Vector<String> columnNames =tableGrid.getAllColumns();
		ExtractDataFilteringTable fixedTable=new ExtractDataFilteringTable(this,columnNames,variableScroll,tableGrid,33,true);

		mainPanel.add(fixedTable.getFixedScrollPane(),BorderLayout.NORTH);	
		mainPanel.add(variableScroll,BorderLayout.CENTER);
		
		gridRowsCountLabel=new JLabel(UIConstants.LABEL_GRID_ROWS);
		InjectUtils.assignArialPlainFont(gridRowsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		gridRowsCountLabel.setForeground(Color.decode("#2F3436"));
		mainPanel.add(gridRowsCountLabel,BorderLayout.SOUTH);
		final String text=UIConstants.LABEL_GRID_ROWS + UIUtils.getFormattedNumber(tableGrid.getExtractDataRows().size());
		gridRowsCountLabel.setText(text);
	}
	
	public JLabel getGridRowsCountLabel() {
		return gridRowsCountLabel;
	}
	
}