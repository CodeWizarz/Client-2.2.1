/**************************************************
 * $Revision: 40330 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-04-01 16:20:01 +0700 (Tue, 01 Apr 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/TemplateSelectionPanel.java $:
 * $Id: TemplateSelectionPanel.java 40330 2014-04-01 09:20:01Z fajrian.yunus $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.reverse.utils.DataExtractionDatabaseUtils.OracleUserRetrievalExecutionMonitor;

@SuppressWarnings("serial")
public abstract class TemplateSelectionPanel extends JPanel
{

	private JComboBox templateSelectionJComboBox;
	private JLabel templateSelectionStatusJLabel;
	private JButton templateSelectionSaveJButton;
	private String selectedTemplate;
	protected ExecutionPanel executionPanel;
	private ItemListener itemListener;
	private JButton cancelTemplateSelection;
	
	public TemplateSelectionPanel(ExecutionPanel executionPanel)
	{
		this.executionPanel=executionPanel;
		createComponents();
	}

	public void createComponents()
	{
		templateSelectionStatusJLabel=new JLabel("");
		templateSelectionStatusJLabel.setForeground(Color.RED);
		templateSelectionStatusJLabel.setFont(templateSelectionStatusJLabel.getFont().deriveFont(Font.BOLD));
		templateSelectionStatusJLabel.setHorizontalAlignment(SwingConstants.CENTER);
		templateSelectionStatusJLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel templatesJLabel= new JLabel("Selected template: ");

		templateSelectionSaveJButton=new JButton("Save template");
		templateSelectionSaveJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
			    	  saveTemplate();
				}
				catch(Exception ex)
				{
					FileUtils.printStackTrace(ex);
				}
			}
		}
		);

		/*
		 * adding components to panels:
		 */
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

		JPanel panel= new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  
		
		add(panel);

		panel.add(templateSelectionSaveJButton);
		panel.add(Box.createRigidArea(new Dimension(10, 0)));
		panel.add(templatesJLabel);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		templateSelectionJComboBox=new JComboBox();
		GUIUtils.setComponentDimension(templateSelectionJComboBox, 300, 25);
		panel.add(templateSelectionJComboBox);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		cancelTemplateSelection = GUIUtils.getButton(SwiftGUIMain.class, "Stop", "/images/stop16.gif");
		cancelTemplateSelection.setEnabled(false);
		final OracleUserRetrievalExecutionMonitor cancelFlag = new OracleUserRetrievalExecutionMonitor();
		cancelTemplateSelection.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					cancelFlag.cancel();
				} catch (SQLException e1) {
					throw new Error(e1);
				}
			}
			
		});
		panel.add(cancelTemplateSelection);
		final JLabel templateLoadingProgressLabel = new JLabel();
		panel.add(templateLoadingProgressLabel);
		
		add(templateSelectionStatusJLabel);
		
		this.itemListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() ==  ItemEvent.SELECTED) {
                	try{
                		if (!templateSelectionJComboBox.isEnabled()) {
                			return;
                		}
                		templateSelectionJComboBox.setEnabled(false);
    					if (getSelectedTemplate()==null) {
    						return;
    					}
    					if (selectedTemplate==null ||!getSelectedTemplate().equalsIgnoreCase(selectedTemplate)) {
    						final String previouslySelectedTemplate = selectedTemplate;
    						selectedTemplate=getSelectedTemplate();
    						final Runnable postCancellation = new Runnable() {

								@Override
								public void run() {
									selectedTemplate=previouslySelectedTemplate==null?UtilsConstants.NO_TEMPLATE_CHOSEN_OPTION:previouslySelectedTemplate;
									templateSelectionJComboBox.setSelectedItem(selectedTemplate);
								}
    							
    						};
    						restoreTemplate(selectedTemplate, cancelFlag, postCancellation, templateLoadingProgressLabel);   
    					}
    				}
    				catch(Exception ex)
    				{
    					FileUtils.printStackTrace(ex);
    				} finally {
    					templateSelectionJComboBox.setEnabled(true);
    				}
                }
            }
        };
	}

	public String getSelectedTemplate() {
		return (String)templateSelectionJComboBox.getSelectedItem();	
	}

	public void lockAll() {
		templateSelectionSaveJButton.setEnabled(false);
		templateSelectionJComboBox.setEnabled(false);
	}

	public void unlockAll() {
		templateSelectionSaveJButton.setEnabled(true);
		templateSelectionJComboBox.setEnabled(true);
	}
	
	public void resetSelectedTemplate() {
		selectedTemplate=null;
	}

	public void loadTemplate(String configTemplateName)  {
		try{
			String defaultTemplateName=UtilsConstants.NO_TEMPLATE_CHOSEN_OPTION;
			if (selectedTemplate==null) {
				defaultTemplateName=configTemplateName;
			}
			initTemplateSelectionJComboBox(defaultTemplateName);
			restoreTemplate(defaultTemplateName, null, null, null);
		}
		catch(Exception e){
			GUIUtils.popupErrorMessage("ERROR: unable to load template, message: "+e.getMessage());
		}
	}

	public abstract List<File> getTemplates();
	
	public void initTemplateSelectionJComboBox(String templateNameParam) 
	{
		try{
			List<File> templatesNames=getTemplates();	
			templateSelectionJComboBox.removeItemListener(itemListener);
			templateSelectionJComboBox.removeAllItems();
			templateSelectionJComboBox.addItem(UtilsConstants.NO_TEMPLATE_CHOSEN_OPTION);
			int index=-1;
			for (int i=0;i<templatesNames.size();i++){
				File template=templatesNames.get(i);

				String templateName=template.getName();
				int indexOf=templateName.lastIndexOf(".");
				if (indexOf!=-1) {
					templateName=templateName.substring(0,indexOf);
				}

				if (templateNameParam!=null && templateNameParam.equalsIgnoreCase(templateName)) {
					index=i+1;
				}
				templateSelectionJComboBox.addItem(templateName);
			}
			if (index!=-1) {
				templateSelectionJComboBox.setSelectedIndex(index);
			}
			else {
				templateSelectionJComboBox.setSelectedIndex(0);
			}
			templateSelectionJComboBox.addItemListener(itemListener);
		}
		catch(Exception e) 
		{
			FileUtils.printStackTrace(e);
		}
	}
	
	public boolean hasTemplate(String templateNameParam) 
	{
		try{
			List<File> templatesNames=getTemplates();	
			for (int i=0;i<templatesNames.size();i++){
				File template=templatesNames.get(i);

				String templateName=template.getName();
				int indexOf=templateName.lastIndexOf(".");
				if (indexOf!=-1) {
					templateName=templateName.substring(0,indexOf);
				}

				if (templateNameParam!=null && templateNameParam.equalsIgnoreCase(templateName)) {
					return true;
				}
			}
			return false;
		}
		catch(Exception e) 
		{
			FileUtils.printStackTrace(e);
			return false;
		}
	}

	public void setTemplateStatus(String message )
	{
		templateSelectionStatusJLabel.setText(message);
	}

	public void saveTemplate()
	throws Exception
	{
		final String templateName = JOptionPane.showInputDialog(
				executionPanel.getSwiftGUIMain().getRootFrame(),"Enter the template name:\n","Template_name");
		if ( templateName==null ||	templateName.isEmpty()) {
			return;
		}
		if (Utils.hasSpecialCharacters(templateName)){
			JOptionPane.showMessageDialog(null,"Special characters are not allowed in the template name","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		setTemplateStatus("Saving template... Please wait...");
		Thread t = new Thread(new Runnable() {
			public void run() {
				lockAll();
				try{
					saveSession(templateName);
					initTemplateSelectionJComboBox(templateName);
				}
				catch(Exception e) {
					FileUtils.printStackTrace(e);
					GUIUtils.popupErrorMessage("Error: "+e.getMessage());
				}
				unlockAll();
				setTemplateStatus("");
			}
		});
		t.start();
	}
			
	public void restoreTemplate(final String templateName, final OracleUserRetrievalExecutionMonitor cancelFlag, final Runnable postCancellation, final JLabel progressLabel)
	{
		Thread t = new Thread(new Runnable() {
			public void run() {
				lockAll();
				try{
					cancelTemplateSelection.setEnabled(true);
					restoreSession(templateName, cancelFlag, progressLabel);
				} catch (OracleUserRetrievalExecutionMonitor.OracleUserRetrievalIsCalcelledException e) {
					
				} catch(Exception e) {
					if ((e instanceof SQLTimeoutException) && e.getMessage().contains("user requested cancel of current operation")) {
						//do nothing
					} else {
						FileUtils.printStackTrace(e);
						GUIUtils.popupErrorMessage("Error: "+e.getMessage());						
					}
				} finally {
					if (cancelFlag != null && cancelFlag.isCancelled()) {
						cancelFlag.reset();
						if (postCancellation != null) {
							postCancellation.run();
						}
					}
					if (progressLabel != null) {
						progressLabel.setText("");
						progressLabel.setIcon(null);
						progressLabel.setVisible(true);
					}					
					cancelTemplateSelection.setEnabled(false);
				}
				setTemplateStatus("");
				unlockAll();
			}
		});
		t.start();
	}
	
	public abstract void saveSession(final String templateName) throws Exception;
			
	public abstract void restoreSession(final String templateName, final OracleUserRetrievalExecutionMonitor cancelFlag, final JLabel progressLabel) throws Exception;
	
	protected abstract File getTemplateFolder();
	
}