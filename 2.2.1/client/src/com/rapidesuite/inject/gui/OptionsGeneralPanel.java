package com.rapidesuite.inject.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings({ "serial"})
public class OptionsGeneralPanel extends JPanel {
	
	public static final String DEFAULT_TABLE_GRID_TABBED_MODE="DEFAULT_TABLE_GRID_TABBED_MODE";
	public static final String DEFAULT_IS_RUN_IN_SEQUENCE="DEFAULT_IS_RUN_IN_SEQUENCE";
	public static final String DEFAULT_IS_SHOW_EMPTY_SCRIPTS="DEFAULT_IS_SHOW_EMPTY_SCRIPTS";
	public static final String DEFAULT_IS_REDIRECT_AFTER_LOGIN="DEFAULT_IS_REDIRECT_AFTER_LOGIN";
	public static final String DEFAULT_EXECUTION_RETRIES="DEFAULT_EXECUTION_RETRIES";

	public static final boolean DEFAULT_VALUE_IS_RUN_IN_SEQUENCE=false;
	public static final boolean DEFAULT_VALUE_IS_SHOW_EMPTY_SCRIPTS=false;
	public static final boolean DEFAULT_VALUE_IS_REDIRECT_AFTER_LOGIN=true;
	public static final int DEFAULT_VALUE_EXECUTION_RETRIES=3;
		
	private OptionsTabPanel optionsTabPanel;
	private JCheckBox runInSequenceCheckBox;
	private JCheckBox showEmptyScriptsCheckBox;
	private JCheckBox isRedirectAfterLoginCheckBox;
	private JTextField executionRetriesTextField;
	
	public OptionsGeneralPanel(OptionsTabPanel optionsTabPanel){
		this.optionsTabPanel=optionsTabPanel;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setOpaque(false);
		//this.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		this.setBorder(InjectUtils.getTitleBorder("General") );
		
		JPanel mainPanel=new JPanel();
		this.add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				
		JPanel tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		mainPanel.add(tempPanel);
		runInSequenceCheckBox=new JCheckBox("Run Scripts in sequence");
		runInSequenceCheckBox.setOpaque(false);
		runInSequenceCheckBox.setContentAreaFilled(false);
		InjectUtils.assignArialPlainFont(runInSequenceCheckBox,ExecutionPanel.FONT_SIZE_SMALL);
		tempPanel.add(runInSequenceCheckBox);
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		mainPanel.add(tempPanel);
		showEmptyScriptsCheckBox=new JCheckBox("Including Empty Record Steps");
		showEmptyScriptsCheckBox.setOpaque(false);
		showEmptyScriptsCheckBox.setContentAreaFilled(false);
		InjectUtils.assignArialPlainFont(showEmptyScriptsCheckBox,ExecutionPanel.FONT_SIZE_SMALL);
		tempPanel.add(showEmptyScriptsCheckBox);
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		mainPanel.add(tempPanel);
		isRedirectAfterLoginCheckBox=new JCheckBox("Redirect After Login");
		isRedirectAfterLoginCheckBox.setOpaque(false);
		isRedirectAfterLoginCheckBox.setContentAreaFilled(false);
		InjectUtils.assignArialPlainFont(isRedirectAfterLoginCheckBox,ExecutionPanel.FONT_SIZE_SMALL);
		tempPanel.add(isRedirectAfterLoginCheckBox);
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		mainPanel.add(tempPanel);
		JLabel label=new JLabel("Number of Retries after execution failure:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_SMALL);
		executionRetriesTextField = new JTextField();
		InjectUtils.assignArialPlainFont(executionRetriesTextField,ExecutionPanel.FONT_SIZE_SMALL);
		InjectUtils.setSize(executionRetriesTextField,50,25);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		tempPanel.add(executionRetriesTextField);
		
		setValues();
	}
	
	private void setValues() {
		Preferences pref = Preferences.userRoot();
		
		boolean runInSequence= pref.getBoolean(DEFAULT_IS_RUN_IN_SEQUENCE, DEFAULT_VALUE_IS_RUN_IN_SEQUENCE);
		runInSequenceCheckBox.setSelected(runInSequence);
		
		boolean isShowEmptyScripts= pref.getBoolean(DEFAULT_IS_SHOW_EMPTY_SCRIPTS, DEFAULT_VALUE_IS_SHOW_EMPTY_SCRIPTS);
		showEmptyScriptsCheckBox.setSelected(isShowEmptyScripts);

		boolean isRedirectAfterLogin= pref.getBoolean(DEFAULT_IS_REDIRECT_AFTER_LOGIN, DEFAULT_VALUE_IS_REDIRECT_AFTER_LOGIN);
		isRedirectAfterLoginCheckBox.setSelected(isRedirectAfterLogin);

		int executionRetries = pref.getInt(DEFAULT_EXECUTION_RETRIES, DEFAULT_VALUE_EXECUTION_RETRIES);
		executionRetriesTextField.setText(""+executionRetries);
	}
	
	public int getExecutionRetries() {
		String text=executionRetriesTextField.getText();
		int value=Integer.valueOf(text).intValue();
		return value;
	}
	
	public boolean isShowEmptyScripts() {
		return showEmptyScriptsCheckBox.isSelected();
	}
	
	public boolean isRedirectAfterLogin() {
		return isRedirectAfterLoginCheckBox.isSelected();
	}
	
	public void unlockUI() {
		setComponentsEnabled(true);
	}

	public void lockUI() {
		setComponentsEnabled(false);
	}
	
	private void setComponentsEnabled(boolean isEnabled) {
		runInSequenceCheckBox.setEnabled(isEnabled);
		showEmptyScriptsCheckBox.setEnabled(isEnabled);
		isRedirectAfterLoginCheckBox.setEnabled(isEnabled);
		executionRetriesTextField.setEnabled(isEnabled);
	}
	
	public void saveOptionsToDisk()
	{
		Preferences pref = Preferences.userRoot();
		
		pref.putBoolean(DEFAULT_IS_RUN_IN_SEQUENCE,isRunInSequence());
		pref.putBoolean(DEFAULT_IS_SHOW_EMPTY_SCRIPTS,isShowEmptyScripts());
		pref.putBoolean(DEFAULT_IS_REDIRECT_AFTER_LOGIN,isRedirectAfterLogin());
		pref.putInt(DEFAULT_EXECUTION_RETRIES,getExecutionRetries());
	}
	
	public void resetOptions()
	{
		Preferences pref = Preferences.userRoot();
		
		pref.remove(DEFAULT_IS_RUN_IN_SEQUENCE);
		pref.remove(DEFAULT_IS_SHOW_EMPTY_SCRIPTS);
		pref.remove(DEFAULT_IS_REDIRECT_AFTER_LOGIN);
		pref.remove(DEFAULT_EXECUTION_RETRIES);
		
		setValues();
	}

	public boolean isRunInSequence() {
		return runInSequenceCheckBox.isSelected();
	}	
	
	public OptionsTabPanel getOptionsTabPanel() {
		return optionsTabPanel;
	}
	
}
