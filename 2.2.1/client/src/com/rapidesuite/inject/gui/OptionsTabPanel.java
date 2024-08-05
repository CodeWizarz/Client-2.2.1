package com.rapidesuite.inject.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.view.RolloverIcon;

@SuppressWarnings("serial")
public class OptionsTabPanel extends JPanel{
	
	private ExecutionPanel executionPanel;
	private OptionsHTMLPlaybackPanel optionsHTMLPlaybackPanel;
	private OptionsGeneralPanel optionsGeneralPanel;
	private OptionsWebServicesPanel optionsWebServicesPanel;
	private JButton saveOptions;
	private JButton resetOptions;
	
	/**
	 * to keep track of the initial state of the 'Including Empty Record Steps' check-box.
	 */
	private boolean isShowEmptyScriptsInitialStatus;
	
	public OptionsTabPanel(ExecutionPanel executionPanel) {
		this.executionPanel=executionPanel;
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		setOpaque(true);
		setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);

		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new GridLayout(0 ,3));
		add(centerPanel,BorderLayout.CENTER);
				
		optionsGeneralPanel=new OptionsGeneralPanel(this);
		optionsHTMLPlaybackPanel=new OptionsHTMLPlaybackPanel(this);
		optionsWebServicesPanel=new OptionsWebServicesPanel(this);
		
		// get the initial state of the check-box
		isShowEmptyScriptsInitialStatus = optionsGeneralPanel.isShowEmptyScripts();
		
		centerPanel.add(optionsGeneralPanel);
		centerPanel.add(optionsHTMLPlaybackPanel);
		centerPanel.add(optionsWebServicesPanel);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_save.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveOptions = new JButton();
		saveOptions.setIcon(ii);
		saveOptions.setBorderPainted(false);
		saveOptions.setContentAreaFilled(false);
		saveOptions.setFocusPainted(false);
		saveOptions.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveOptions.setRolloverIcon(new RolloverIcon(ii));
		saveOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionSaveOptions();
			}
		}
				);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_reset.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetOptions = new JButton();
		resetOptions.setIcon(ii);
		resetOptions.setBorderPainted(false);
		resetOptions.setContentAreaFilled(false);
		resetOptions.setFocusPainted(false);
		resetOptions.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_reset_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetOptions.setRolloverIcon(new RolloverIcon(ii));
		resetOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionResetOptions();
			}
		}
				);
		
		JPanel southPanel=new JPanel();
		southPanel.setOpaque(false);
		southPanel.setLayout(new BorderLayout());
		add(southPanel,BorderLayout.SOUTH);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		southPanel.add(tempPanel);
		
		tempPanel.add(saveOptions);
		tempPanel.add(resetOptions);
	}
	
	protected void processActionSaveOptions() {
		
		// if the option has been changed
		if(isShowEmptyScriptsInitialStatus != optionsGeneralPanel.isShowEmptyScripts()) {
			// show a confirmation prompt
			int response = JOptionPane.showConfirmDialog(null, 
					"<html><body>Changes to 'Including Empty Record Steps' require the injection package to be reloaded.<br/><br/>"+
							"<b>Do you want to change the option and reload now?</b>", "Confirmation",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			// if NO or CLOSE nothing to do
			if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
				// tell the user that nothing is saved
				GUIUtils.popupInformationMessage("No changes have been saved.");
				// get out.
				return;
			}
			// if YES then do some stuff
			else {
				// reload the initial status
				isShowEmptyScriptsInitialStatus = optionsGeneralPanel.isShowEmptyScripts();
				
				// reload the package
				executionPanel.getInjectMain().getApplicationInfoPanel().startInjectionPackageLoadDialog(
						executionPanel.getInjectMain().getApplicationInfoPanel().getInjectionPackage());
			}
		}
		
		// default saves, has to happen - except in the case of return earlier. 
		optionsHTMLPlaybackPanel.saveOptionsToDisk();
		optionsGeneralPanel.saveOptionsToDisk();
		optionsWebServicesPanel.saveOptionsToDisk();
	}
	
	protected void processActionResetOptions() {
		int response = JOptionPane.showConfirmDialog(null, 
				"<html><body>This will reset all the options to their default values.<br/>You will lose your current setting.<br/><br/>"+
						"<b>Are you sure to continue with the reset?</b>", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
			return;
		}
		
		// default saves, has to happen - except in the case of return earlier. 
		optionsHTMLPlaybackPanel.resetOptions();
		optionsGeneralPanel.resetOptions();
		optionsWebServicesPanel.resetOptions();
	}
		
	public ExecutionPanel getExecutionPanel() {
		return executionPanel;
	}

	public void unlockUI() {
		optionsHTMLPlaybackPanel.unlockUI();
		optionsGeneralPanel.unlockUI();
		optionsWebServicesPanel.unlockUI();
	}

	public void lockUI() {
		optionsHTMLPlaybackPanel.lockUI();
		optionsGeneralPanel.lockUI();
		optionsWebServicesPanel.lockUI();
	}

	public OptionsHTMLPlaybackPanel getOptionsHTMLPlaybackPanel() {
		return optionsHTMLPlaybackPanel;
	}

	public OptionsGeneralPanel getOptionsGeneralPanel() {
		return optionsGeneralPanel;
	}

	public OptionsWebServicesPanel getOptionsWebServicesPanel() {
		return optionsWebServicesPanel;
	}
	
	
}
