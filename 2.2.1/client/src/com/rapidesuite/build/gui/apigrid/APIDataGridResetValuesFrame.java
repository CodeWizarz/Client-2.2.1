/**************************************************
 * $Revision: 31696 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:39:15 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/apigrid/APIDataGridResetValuesFrame.java $:
 * $Id: APIDataGridResetValuesFrame.java 31696 2013-03-04 06:39:15Z john.snell $:
 */

package com.rapidesuite.build.gui.apigrid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.rapidesuite.client.common.gui.datagrid.DataGridColumn;
import com.rapidesuite.client.common.gui.datagrid.DataGridConstants;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class APIDataGridResetValuesFrame extends JDialog
{

	private APIDataGridController apiDataGridController;
	private Map<String, JCheckBox> attributeNameToCheckBoxMap;

	public APIDataGridResetValuesFrame(APIDataGridController apiDataGridController) throws Exception
	{
		super(new JFrame(), true);
		this.apiDataGridController = apiDataGridController;
		createComponents();
	}

	public void createComponents() throws Exception
	{
		this.setLayout(new BorderLayout());
		List<DataGridColumn> dataGridUserDefinedColumns = apiDataGridController.getDataGridInventoryColumns();
		attributeNameToCheckBoxMap = new HashMap<String, JCheckBox>();
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		for ( DataGridColumn dataGridUserDefinedColumn : dataGridUserDefinedColumns )
		{
			if ( !dataGridUserDefinedColumn.isResetable() )
			{
				continue;
			}
			JCheckBox checkBox = new JCheckBox(dataGridUserDefinedColumn.getColumnTitle());
			tempPanel.add(checkBox);
			attributeNameToCheckBoxMap.put(dataGridUserDefinedColumn.getAttributeName(), checkBox);
		}
		JScrollPane filterScrollPane = new JScrollPane(tempPanel);
		filterScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		filterScrollPane.setBorder(BorderFactory.createEtchedBorder());
		this.add(filterScrollPane, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					resetValues();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		});
		applyButton.setMaximumSize(new Dimension(100, 50));
		southPanel.add(applyButton);
		this.add(southPanel, BorderLayout.SOUTH);

		setSize(DataGridConstants.FRAME_MASS_UPDATE_WIDTH, DataGridConstants.FRAME_MASS_UPDATE_HEIGHT);
		setTitle("Reset values");
	}

	public void resetValues()
	{
		this.setVisible(false);
		Map<String, DataGridColumn> map = apiDataGridController.getDataGridColumnsResetValuesOnMap();
		map.clear();
		Map<String, DataGridColumn> dataGridUserDefinedColumns = apiDataGridController.getDataGridColumnAttributeNameToDataGridColumnMap();
		Iterator<String> iterator = attributeNameToCheckBoxMap.keySet().iterator();
		while ( iterator.hasNext() )
		{
			String key = iterator.next();
			JCheckBox checkBox = attributeNameToCheckBoxMap.get(key);
			if ( checkBox.isSelected() )
			{
				map.put(key, dataGridUserDefinedColumns.get(key));
				checkBox.setSelected(false);
			}
		}
	}

}