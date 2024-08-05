/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/BusinessGroupSelectionPanel.java $:
 * $Id: BusinessGroupSelectionPanel.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class BusinessGroupSelectionPanel
{

	private ExecutionPanel executionPanel;
	private JScrollPane scrollPane;
	private JPanel centerPanel;
	private List<JCheckBox> businessGroupCheckBoxes;
	private TreeMap<Long, String> businessGroupIdToNameMap;
	private TreeMap<Long, Long> operatingUnitIdToBusinessGroupIdMap;
	private JCheckBox selectUnselectCheckBox;
	private JPanel innerPanel;

	public BusinessGroupSelectionPanel(ExecutionPanel executionPanel)
	{
		this.executionPanel=executionPanel;
		createSubComponents();
	}

	public void createSubComponents()
	{
		innerPanel=new JPanel();
		innerPanel.setLayout(new BorderLayout());
		centerPanel=new JPanel();
		innerPanel.add(centerPanel, BorderLayout.CENTER);
	}

	public void initBusinessGroups(TreeMap<Long, String> businessGroupIdToNameMap,
			TreeMap<Long, Long> operatingUnitIdToBusinessGroupIdMap,int width,int height)
	{
		try{
			this.businessGroupIdToNameMap=businessGroupIdToNameMap;
			this.operatingUnitIdToBusinessGroupIdMap=operatingUnitIdToBusinessGroupIdMap;

			centerPanel.removeAll();
		
			businessGroupCheckBoxes=GUIUtils.getCheckBoxesListLong(businessGroupIdToNameMap,new Dimension(450,15));
		
			final List<JCheckBox> allCheckBoxes=new ArrayList<JCheckBox>();
			allCheckBoxes.addAll(businessGroupCheckBoxes);

			JPanel panel=GUIUtils.createJCheckBoxesVerticalListPanel(allCheckBoxes);

			selectUnselectCheckBox=GUIUtils.getSelectionAllCheckBox(false);
			selectUnselectCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					GUIUtils.setSelectedCheckBox(allCheckBoxes,selectUnselectCheckBox.isSelected());
				}
			}
			);
			GUIUtils.addToUserPanel(centerPanel,new JLabel(" - Business groups (Optional)"),selectUnselectCheckBox,null,null);
			
			scrollPane=GUIUtils.createJScrollPane(panel,width,height);
			centerPanel.add(scrollPane,BorderLayout.CENTER);
			GUIUtils.setEnabledOnComponents(getAllComponents(),false);
		}
		catch(Exception e){
			FileUtils.printStackTrace(e);
		}
	}

	public List<Long> getSelectedBusinessGroupIds()
	{
		return GUIUtils.getSelectedIdsLong(businessGroupCheckBoxes,businessGroupIdToNameMap);
	}

	public List<Component> getAllComponents() {
		List<Component> list=new ArrayList<Component>();
		list.add(selectUnselectCheckBox);
		list.addAll(businessGroupCheckBoxes);

		return list;
	}

	public void lockAll() {
		GUIUtils.setEnabledOnComponents(getAllComponents(),false);
	}

	public void unlockAll() {
		GUIUtils.setEnabledOnComponents(getAllComponents(),true);
	}

	public ExecutionPanel getExecutionPanel() {
		return executionPanel;
	}

	public JPanel getSubInnerPanel() {
		return innerPanel;
	}

	public Map<Long, String> getBusinessGroupIdToNameMap() {
		return businessGroupIdToNameMap;
	}

	public TreeMap<Long, Long> getOperatingUnitIdToBusinessGroupIdMap() {
		return operatingUnitIdToBusinessGroupIdMap;
	}

	
	public void updateStatus(OperatingUnitSelectionPanel ous)
	{
		Map<Long,String> selectedBusinessGroups = new HashMap<Long,String>(); 
		for ( Long ouid : ous.getSelectedOperatingUnitIds() )
		{
			Long buid = this.operatingUnitIdToBusinessGroupIdMap.get(ouid);
			if (buid==null){
				continue;
			}
			String buName= this.getBusinessGroupIdToNameMap().get(buid);
			if (buName==null){
				continue;
			}
			selectedBusinessGroups.put(buid,buName);
		}
		GUIUtils.setSelectedCheckBoxesLong(businessGroupCheckBoxes, selectedBusinessGroups);
	}
}