package com.rapidesuite.inject.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class TableGridFrame extends JFrame  {

	public TableGridFrame(TableGrid tableGrid) throws Exception {
		String title=(tableGrid.getScriptGridTracker().getGridIndex()+1)+". "+tableGrid.getScriptGridTracker().getScript().getName()+
				" - Table: '"+tableGrid.getInventory().getName()+"'";
		this.setIconImage(GUIUtils.getImageIcon(this.getClass(), InjectMain.getSharedApplicationIconPath()).getImage());
		this.setTitle(title);
		this.setSize(InjectMain.FRAME_WIDTH, InjectMain.FRAME_HEIGHT);
		UIUtils.setFramePosition(tableGrid.getScriptsGrid().getExecutionTabPanel().getExecutionPanel().getInjectMain().getFrame() ,this);
		this.setLayout(new BorderLayout());
	
		String parentInventoryName=tableGrid.getInventory().getParentName();
		if (parentInventoryName!=null && !parentInventoryName.isEmpty()) {
			JPanel tempPanel=InjectUtils.getYPanel(Component.LEFT_ALIGNMENT);
			tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			add(tempPanel,BorderLayout.NORTH);
			
			JLabel label=new JLabel("Parent table: '"+parentInventoryName+"'");
			label.setFont(new Font("Serif", Font.BOLD, 16));
			tempPanel.add(label);
		}
		
		JScrollPane variableScroll = new JScrollPane(tableGrid.getTable(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};
		Vector<String> columnNames =TableGrid.getColumns(tableGrid.getInventory());
		InjectFilteringTable fixedTable=new InjectFilteringTable(columnNames,variableScroll,tableGrid.getTable(),33,true);

		add(fixedTable.getFixedScrollPane(),BorderLayout.NORTH);	
		add(variableScroll,BorderLayout.CENTER);
	}
	
}
