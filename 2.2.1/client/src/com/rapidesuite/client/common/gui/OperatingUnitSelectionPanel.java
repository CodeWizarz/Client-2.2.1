/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/OperatingUnitSelectionPanel.java $:
 * $Id: OperatingUnitSelectionPanel.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class OperatingUnitSelectionPanel
{
	public static interface OperatingUnitSelectionStateChangeHandler
	{
		public void onStateChange();
	}

	private JLabel ouLabel;
	private ExecutionPanel executionPanel;
	private JScrollPane scrollPane;
	private JPanel centerPanel;
	private List<JCheckBox> operatingUnitCheckBoxes;
	private TreeMap<Long, String> operatingUnitIdToNameMap;
	private JCheckBox selectUnselectCheckBox;
	private JPanel innerPanel;
	private final OperatingUnitSelectionStateChangeHandler operatingUnitSelectionStateChangeHandler;

	public OperatingUnitSelectionPanel(ExecutionPanel executionPanel, OperatingUnitSelectionStateChangeHandler operatingUnitSelectionStateChangeHandler)
	{
		this.executionPanel=executionPanel;
		this.operatingUnitSelectionStateChangeHandler = operatingUnitSelectionStateChangeHandler;
		createSubComponents();
	}

	public void createSubComponents()
	{
		innerPanel=new JPanel();
		innerPanel.setLayout(new BorderLayout());
		centerPanel=new JPanel();
		innerPanel.add(centerPanel, BorderLayout.CENTER);
	}

	public void initOperatingUnits(TreeMap<Long, String> operatingUnitIdToNameMap,int width,int height)
	{
		try{
			this.operatingUnitIdToNameMap=operatingUnitIdToNameMap;
			centerPanel.removeAll();
			
			Map<Long, String> ouMap=new TreeMap<Long, String>();
			Iterator<Long> iterator=operatingUnitIdToNameMap.keySet().iterator();
			while (iterator.hasNext()) {
				Long operatingUnitId=iterator.next();
				String operatingUnitName=operatingUnitIdToNameMap.get(operatingUnitId);
				ouMap.put(operatingUnitId,operatingUnitName);
			}
			operatingUnitCheckBoxes=GUIUtils.getCheckBoxesListLong(ouMap,new Dimension(450,15));

			final List<JCheckBox> allCheckBoxes=new ArrayList<JCheckBox>();
			allCheckBoxes.addAll(operatingUnitCheckBoxes);
			JPanel panel=GUIUtils.createJCheckBoxesVerticalListPanel(allCheckBoxes);
			
			for ( JCheckBox jcb : allCheckBoxes )
			{
				jcb.addChangeListener(new ChangeListener(){

					@Override
                    public void stateChanged(ChangeEvent e)
                    {
						operatingUnitSelectionStateChangeHandler.onStateChange();
                    }
				});
			}

			selectUnselectCheckBox=GUIUtils.getSelectionAllCheckBox(false);
			selectUnselectCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					GUIUtils.setSelectedCheckBox(allCheckBoxes,selectUnselectCheckBox.isSelected());
				}
			}
			);
			ouLabel=new JLabel(" - Operating Units:");
			GUIUtils.addToUserPanel(centerPanel,ouLabel,selectUnselectCheckBox,null,null);

			scrollPane=GUIUtils.createJScrollPane(panel,width,height);
			centerPanel.add(scrollPane,BorderLayout.CENTER);
		}
		catch(Exception e){
			FileUtils.printStackTrace(e);
		}
	}

	public List<Long> getSelectedOperatingUnitIds()
	{
		return GUIUtils.getSelectedIdsLong(operatingUnitCheckBoxes,operatingUnitIdToNameMap);
	}

	public boolean hasOperatingUnits()
	{
		return !operatingUnitIdToNameMap.isEmpty();
	}

	public Map<Long, String> getOperatingUnitIdToNameMap()
	{
		return operatingUnitIdToNameMap;
	}

	public void restoreOperatingUnits(Map<Integer,String> selectedOperatingUnitIdToNameMap)
	{
		GUIUtils.setSelectedCheckBoxes(operatingUnitCheckBoxes,selectedOperatingUnitIdToNameMap);
	}

	public List<Component> getAllComponents() {
		List<Component> list=new ArrayList<Component>();
		list.addAll(getAllOUComponents());

		return list;
	}

	protected List<Component> getAllOUComponents() {
		List<Component> list=new ArrayList<Component>();
		list.add(selectUnselectCheckBox);
		list.addAll(operatingUnitCheckBoxes);

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

	public JLabel getOuLabel() {
		return ouLabel;
	}

}