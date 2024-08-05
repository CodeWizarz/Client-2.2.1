package com.rapidesuite.inject.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class OptionsWebServicesPanel  extends JPanel {

	public static final String DEFAULT_WEBSERVICES_MAX_WORKERS="DEFAULT_WEBSERVICES_MAX_WORKERS";
	public static final String DEFAULT_WEBSERVICES_MAX_RECORDS_PER_WORKER="DEFAULT_WEBSERVICES_MAX_RECORDS_PER_WORKER";

	public static final int DEFAULT_VALUE_WEBSERVICES_MAX_WORKERS=10;
	public static final int DEFAULT_VALUE_WEBSERVICES_MAX_RECORDS_PER_WORKER=50;
	
	private OptionsTabPanel optionsTabPanel;
	private JTextField maxWorkersTextField;
	private JTextField maxRecordsPerWorkerTextField;
	
	public OptionsWebServicesPanel(OptionsTabPanel optionsTabPanel){
		this.optionsTabPanel=optionsTabPanel;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setOpaque(false);
		//this.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		this.setBorder(InjectUtils.getTitleBorder("Web services (APIs)") );
			
		JPanel mainPanel=new JPanel();
		this.add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		mainPanel.add(tempPanel);
		JLabel label=new JLabel("Number of Workers");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_SMALL);
		maxWorkersTextField = new JTextField();
		InjectUtils.assignArialPlainFont(maxWorkersTextField,ExecutionPanel.FONT_SIZE_SMALL);
		InjectUtils.setSize(maxWorkersTextField,50,25);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		tempPanel.add(maxWorkersTextField);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		mainPanel.add(tempPanel);
		label=new JLabel("Number of records per Worker");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_SMALL);
		maxRecordsPerWorkerTextField = new JTextField();
		InjectUtils.assignArialPlainFont(maxRecordsPerWorkerTextField,ExecutionPanel.FONT_SIZE_SMALL);
		InjectUtils.setSize(maxRecordsPerWorkerTextField,50,25);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		tempPanel.add(maxRecordsPerWorkerTextField);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		
		setValues();
	}
	
	private void setValues() {
		Preferences pref = Preferences.userRoot();

		int maxWorkers = pref.getInt(DEFAULT_WEBSERVICES_MAX_WORKERS, DEFAULT_VALUE_WEBSERVICES_MAX_WORKERS);
		maxWorkersTextField.setText(""+maxWorkers);

		int maxRecordsPerWorker = pref.getInt(DEFAULT_WEBSERVICES_MAX_RECORDS_PER_WORKER, DEFAULT_VALUE_WEBSERVICES_MAX_RECORDS_PER_WORKER);
		maxRecordsPerWorkerTextField.setText(""+maxRecordsPerWorker);
	}
	
	public void unlockUI() {
		setComponentsEnabled(true);
	}

	public void lockUI() {
		setComponentsEnabled(false);
	}
	
	private void setComponentsEnabled(boolean isEnabled) {
		maxWorkersTextField.setEnabled(isEnabled);
		maxRecordsPerWorkerTextField.setEnabled(isEnabled);
	}
	
	public void saveOptionsToDisk()
	{
		Preferences pref = Preferences.userRoot();
		pref.putInt(DEFAULT_WEBSERVICES_MAX_WORKERS, getMaxWebServicesWorkers());
		pref.putInt(DEFAULT_WEBSERVICES_MAX_RECORDS_PER_WORKER,getWebServicesBatchSizeLevel());
	}	
	
	public void resetOptions()
	{
		Preferences pref = Preferences.userRoot();
		
		pref.remove(DEFAULT_WEBSERVICES_MAX_WORKERS);
		pref.remove(DEFAULT_WEBSERVICES_MAX_RECORDS_PER_WORKER);
		
		setValues();
	}	

	public int getMaxWebServicesWorkers() {
		String text=maxWorkersTextField.getText();
		int value=Integer.valueOf(text).intValue();
		return value;
	}

	public int getWebServicesBatchSizeLevel() {
		String text=maxRecordsPerWorkerTextField.getText();
		int value=Integer.valueOf(text).intValue();
		return value;
	}

	public JTextField getMaxRecordsPerWorkerTextField() {
		return maxRecordsPerWorkerTextField;
	}

	public OptionsTabPanel getOptionsTabPanel() {
		return optionsTabPanel;
	}
	
}
