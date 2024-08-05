/**************************************************
 * $Revision: 31696 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:39:15 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/datagrid/DataGridMassUpdateFrame.java $:
 * $Id: DataGridMassUpdateFrame.java 31696 2013-03-04 06:39:15Z john.snell $:
 */

package com.rapidesuite.client.common.gui.datagrid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class DataGridMassUpdateFrame extends JDialog {

	private DataGridController dataGridController;
	private DataGridMassUpdatePanel dataGridMassUpdatePanel;
	private JCheckBox massUpdateGridsCheckBox;
	
	public DataGridMassUpdateFrame(DataGridController dataGridController) throws Exception
	{
		super(new JFrame(),true);
		this.dataGridController=dataGridController;
		createComponents();
	}
	
	public void createComponents() throws Exception
	{
		this.setLayout(new BorderLayout());
		dataGridMassUpdatePanel=new DataGridMassUpdatePanel(dataGridController.getDataGridUserDefinedColumns());
		JScrollPane filterScrollPane = new JScrollPane(dataGridMassUpdatePanel);
		filterScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		filterScrollPane.setBorder(BorderFactory.createEtchedBorder());		
		this.add(filterScrollPane,BorderLayout.CENTER);
		
		JPanel southPanel=new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		JButton applyButton=new JButton("Apply");
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					massUpdateDataGridRows();
				}
				catch(Exception ex)
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		}
		);
		applyButton.setMaximumSize(new Dimension(100,50));
		massUpdateGridsCheckBox = new JCheckBox("Mass update all grids");
		//southPanel.add(massUpdateGridsCheckBox );
		southPanel.add(applyButton);
		this.add(southPanel,BorderLayout.SOUTH);
		
		setSize(DataGridConstants.FRAME_MASS_UPDATE_WIDTH,DataGridConstants.FRAME_MASS_UPDATE_HEIGHT);
		setTitle("Mass update");
	}
	
	public void massUpdateDataGridRows() throws Exception
	{
		Map<DataGridColumn,String> dataGridColumnsToValueMap=dataGridMassUpdatePanel.getDataGridColumnsToValueMap();
		dataGridController.massUpdateDataGridRows(dataGridColumnsToValueMap);
		this.setVisible(false);
		
		if (massUpdateGridsCheckBox.isSelected()) {
			dataGridController.massUpdateGrids(dataGridColumnsToValueMap); 
		}
	}
	
}