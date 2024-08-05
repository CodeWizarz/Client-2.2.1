/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/DataExtractionEBSLevelSelectionPanel.java $:
 * $Id: DataExtractionEBSLevelSelectionPanel.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
 */

package com.rapidesuite.reverse.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.client.common.gui.BusinessGroupSelectionPanel;
import com.rapidesuite.client.common.gui.OperatingUnitSelectionPanel;
import com.rapidesuite.client.common.gui.OperatingUnitSelectionPanel.OperatingUnitSelectionStateChangeHandler;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;

public class DataExtractionEBSLevelSelectionPanel
{
	
	private JCheckBox instanceLevelSetupsCheckBox;
	private JCheckBox operatingUnitSetupsCheckBox;
	private boolean isTemplateRestoreInProcess;
	private JPanel innerPanel;
	private DataExtractionPanel dataExtractionPanel;
	private JLabel ouType;
	private OperatingUnitSelectionPanel operatingUnitSelectionPanel;
	private BusinessGroupSelectionPanel businessGroupSelectionPanel;
	
	public DataExtractionEBSLevelSelectionPanel(DataExtractionPanel dataExtractionPanel)
	{
		this.dataExtractionPanel=dataExtractionPanel;
		createComponents();
	}

	public void createComponents()
	{
		innerPanel=new JPanel();
		
		JPanel northPanel=new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));  
		// (int top, int left, int bottom, int right) 
		northPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 0));
		JLabel tempLabel=new JLabel(" - Select the type of Setups:");
		tempLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northPanel.add(tempLabel);
		JPanel tempPanel = new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(3, 10, 0, 0));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northPanel.add(tempPanel);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
				
		instanceLevelSetupsCheckBox=new JCheckBox("Instance level setups");
		instanceLevelSetupsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				actionPerformedInstanceLevelSetups();
			}
		}
		);
		
		operatingUnitSetupsCheckBox=new JCheckBox("Operating Unit specific setups");
		operatingUnitSetupsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				actionPerformedOperatingUnitSetups();
			}
		}
		);
		
		operatingUnitSetupsCheckBox.setSelected(true);
		instanceLevelSetupsCheckBox.setSelected(true);
		
		tempPanel.add(operatingUnitSetupsCheckBox);
		tempPanel.add(Box.createHorizontalStrut(30));
		tempPanel.add(instanceLevelSetupsCheckBox);
		tempPanel.add(Box.createHorizontalStrut(30));
		
		northPanel.add(Box.createVerticalStrut(20));
		tempLabel=new JLabel("   Inventories color coding:");
		tempLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northPanel.add(tempLabel);
		JPanel colorNotesPanel = new JPanel();
		colorNotesPanel.setLayout(new BoxLayout(colorNotesPanel, BoxLayout.X_AXIS));
		colorNotesPanel.setBorder(BorderFactory.createEmptyBorder(3, 10, 0, 0));
		colorNotesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northPanel.add(colorNotesPanel);
		
		ouType=new JLabel("OU level");
		ouType.setOpaque(true);
		ouType.setBackground(UtilsConstants.OPERATING_UNIT_SPECIFIC_COLOR);
		JLabel instanceType=new JLabel("Instance level");
		instanceType.setOpaque(true);
		instanceType.setBackground(UtilsConstants.INSTANCE_LEVEL_COLOR);
		JLabel unsupportedType=new JLabel("Unsupported");
		unsupportedType.setOpaque(true);
		unsupportedType.setBackground(UtilsConstants.INVALID_OR_ERROR_COLOR);
		
		colorNotesPanel.add(ouType );
		colorNotesPanel.add(Box.createHorizontalStrut(30));
		colorNotesPanel.add(instanceType );
		colorNotesPanel.add(Box.createHorizontalStrut(30));
		colorNotesPanel.add(unsupportedType );
		
		operatingUnitSelectionPanel=new OperatingUnitSelectionPanel(dataExtractionPanel, new OperatingUnitSelectionStateChangeHandler(){
            public void onStateChange()
            {
            	businessGroupSelectionPanel.updateStatus(operatingUnitSelectionPanel);
            }
		});
		businessGroupSelectionPanel=new BusinessGroupSelectionPanel(dataExtractionPanel);
		
		/*
		 * adding components to panels:
		 */
		innerPanel.setLayout(new BorderLayout());
		innerPanel.add(northPanel, BorderLayout.NORTH);
		innerPanel.add(operatingUnitSelectionPanel.getSubInnerPanel(), BorderLayout.CENTER);
		innerPanel.add(businessGroupSelectionPanel.getSubInnerPanel(), BorderLayout.SOUTH);
		
	}
	
	public void updateLabelsForFusion() {
		operatingUnitSetupsCheckBox.setText("Business Unit specific setups");
		ouType.setText("BU level");
		businessGroupSelectionPanel.getSubInnerPanel().setVisible(false);
		operatingUnitSelectionPanel.getOuLabel().setText(" - Business Units:");
	}

	public void actionPerformedInstanceLevelSetups()
	{
		if (isTemplateRestoreInProcess) {
			return;
		}
		if (!operatingUnitSetupsCheckBox.isSelected() && !instanceLevelSetupsCheckBox.isSelected()) {
			GUIUtils.popupErrorMessage("You must select at least one check box.");
			instanceLevelSetupsCheckBox.setSelected(true);
			return;
		}
		if (operatingUnitSetupsCheckBox.isSelected() && instanceLevelSetupsCheckBox.isSelected()) {
			GUIUtils.setEnabledOnComponents(businessGroupSelectionPanel.getAllComponents(),false);
			return;
		}
		GUIUtils.setEnabledOnComponents(businessGroupSelectionPanel.getAllComponents(),instanceLevelSetupsCheckBox.isSelected());
	}
	
	public void actionPerformedOperatingUnitSetups()
	{
		if (isTemplateRestoreInProcess) {
			return;
		}
		if (!instanceLevelSetupsCheckBox.isSelected()) {
			GUIUtils.popupErrorMessage("You must select at least one check box.");
			operatingUnitSetupsCheckBox.setSelected(true);
			return;
		}
		if (instanceLevelSetupsCheckBox.isSelected()) {
			if (operatingUnitSetupsCheckBox.isSelected()) {
				GUIUtils.setEnabledOnComponents(businessGroupSelectionPanel.getAllComponents(),false);
			}
			else {
				GUIUtils.setEnabledOnComponents(businessGroupSelectionPanel.getAllComponents(),true);
			}
		}
		GUIUtils.setEnabledOnComponents(operatingUnitSelectionPanel.getAllComponents(),operatingUnitSetupsCheckBox.isSelected());
	}
	
	public void restoreTypes(boolean isInstanceLevelSetups,boolean isOperatingUnitSetups)
	{
		isTemplateRestoreInProcess=true;
		instanceLevelSetupsCheckBox.setSelected(isInstanceLevelSetups);
		if (instanceLevelSetupsCheckBox.isSelected()) {
			if (isOperatingUnitSetups){
				GUIUtils.setEnabledOnComponents(businessGroupSelectionPanel.getAllComponents(),false);
			}
			else {
				GUIUtils.setEnabledOnComponents(businessGroupSelectionPanel.getAllComponents(),true);
			}
		}
		else {
			GUIUtils.setEnabledOnComponents(businessGroupSelectionPanel.getAllComponents(),false);
		}
		operatingUnitSetupsCheckBox.setSelected(isOperatingUnitSetups);
		GUIUtils.setEnabledOnComponents(operatingUnitSelectionPanel.getAllComponents(),operatingUnitSetupsCheckBox.isSelected());
		isTemplateRestoreInProcess=false;
	}
	
	public List<Component> getAllComponents() {
		List<Component> list=new ArrayList<Component>();
		list.addAll(operatingUnitSelectionPanel.getAllComponents());
		list.addAll(businessGroupSelectionPanel.getAllComponents());
		list.add(instanceLevelSetupsCheckBox);
		list.add(operatingUnitSetupsCheckBox);
		
		return list;
	}
		
	public boolean isInstanceLevelSelected() {
		return instanceLevelSetupsCheckBox.isSelected();
	}
	
	public boolean isOperatingUnitLevelSelected() {
		return operatingUnitSetupsCheckBox.isSelected();
	}

	public JPanel getInnerPanel() {
		return innerPanel;
	}

	public JCheckBox getSpecificSetupsCheckBox() {
		return operatingUnitSetupsCheckBox;
	}

	public OperatingUnitSelectionPanel getOperatingUnitSelectionPanel() {
		return operatingUnitSelectionPanel;
	}

	public BusinessGroupSelectionPanel getBusinessGroupSelectionPanel() {
		return businessGroupSelectionPanel;
	}
	
}