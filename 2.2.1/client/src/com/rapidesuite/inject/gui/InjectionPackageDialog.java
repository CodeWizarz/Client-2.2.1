package com.rapidesuite.inject.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rapidesuite.build.gui.panels.InjectorsPackageSelectionPanel;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.injectionPackageInformation.InjectionPackageInformationDocument;
import com.rapidesuite.core.injectionPackageInformation.InjectionPackageInformationDocument.InjectionPackageInformation;

public class InjectionPackageDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;

	private JLabel projectNameLabel;
	private JLabel filtersLabel;
	private JLabel fusionImlProjectNameLabel;
	private JLabel serverNameLabel;
	private JLabel generatedByLabel;
	private JLabel generatedOnLabel;
	private JLabel scenarioNameLabel;
	private JLabel profileNameLabel;

	private JTextField projectNameValue;
	private JTextField filtersValue;
	private JTextField fusionImlProjectNameValue;
	private JTextField serverNameValue;
	private JTextField generatedByValue;
	private JTextField generatedOnValue;
	private JTextField scenarioNameValue;
	private JTextField profileNameValue;
	
	public InjectionPackageDialog(InjectionPackageInformationDocument doc) throws Exception {
		createComponents();
		setPackageContent(doc);
	}
	
	private void createComponents() {
		
		setTitle("Injection Package Information");
		setModal(true);
		setSize(550,300);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);		
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setForeground(Color.RED);

		projectNameLabel = GUIUtils.getLabel("Project name:", true);
		filtersLabel = GUIUtils.getLabel("Labels:", true);
		fusionImlProjectNameLabel = GUIUtils.getLabel("Fusion Project:", true);
		serverNameLabel = GUIUtils.getLabel("Server name:", true);
		generatedByLabel = GUIUtils.getLabel("Generated by:", true);
		generatedOnLabel = GUIUtils.getLabel("Generated on:", true);
		scenarioNameLabel = GUIUtils.getLabel("Scenario name:", true);
		profileNameLabel = GUIUtils.getLabel("Profile name:", true);

		Color inputFieldBackgroundColor = Color.decode("#DBDBDB");
		projectNameValue= GUIUtils.getInputField(true, false, false);
		projectNameValue.setBackground(inputFieldBackgroundColor);
		filtersValue = GUIUtils.getInputField(true, false, false);
		filtersValue.setBackground(inputFieldBackgroundColor);
		fusionImlProjectNameValue = GUIUtils.getInputField(true, false, false);
		fusionImlProjectNameValue.setBackground(inputFieldBackgroundColor);
		serverNameValue = GUIUtils.getInputField(true, false, false);
		serverNameValue.setBackground(inputFieldBackgroundColor);
		generatedByValue = GUIUtils.getInputField(true, false, false);
		generatedByValue.setBackground(inputFieldBackgroundColor);
		generatedOnValue = GUIUtils.getInputField(true, false, false);
		generatedOnValue.setBackground(inputFieldBackgroundColor);
		scenarioNameValue = GUIUtils.getInputField(true, false, false);
		scenarioNameValue.setBackground(inputFieldBackgroundColor);
		profileNameValue = GUIUtils.getInputField(true, false, false);
		profileNameValue.setBackground(inputFieldBackgroundColor);
		
		JPanel panel=new JPanel();
		add(panel);
		panel.setOpaque(true);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3); // put spacing around this field.
		GridBagLayout gl = new GridBagLayout();
		panel.setLayout(gl);
		int fill = GridBagConstraints.HORIZONTAL;
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, projectNameLabel, 0, 0, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, projectNameValue, 0, 1, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, filtersLabel, 1, 0, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, filtersValue, 1, 1, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, fusionImlProjectNameLabel, 2, 0, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, fusionImlProjectNameValue, 2, 1, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, serverNameLabel, 3, 0, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, serverNameValue, 3, 1, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, generatedByLabel, 4, 0, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, generatedByValue, 4, 1, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, generatedOnLabel, 5, 0, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, generatedOnValue, 5, 1, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, scenarioNameLabel, 6, 0, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, scenarioNameValue, 6, 1, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, profileNameLabel, 7, 0, fill);
		InjectorsPackageSelectionPanel.addConstraintToPanel(gl, gbc, panel, profileNameValue, 7, 1, fill);
		
	}
	
	private void setPackageContent(InjectionPackageInformationDocument doc) throws Exception {
	
		InjectionPackageInformation info = doc.getInjectionPackageInformation();
		projectNameValue.setText(info.getProjectName());
		filtersValue.setText(info.getLabel());
		serverNameValue.setText(info.getServerName());
		generatedByValue.setText(info.getGeneratedBy());
		generatedOnValue.setText(info.getGeneratedOn());
		scenarioNameValue.setText(info.getScenarioName());
		profileNameValue.setText(info.getProfileName());
		fusionImlProjectNameValue.setText(info.getFusionImplementationProject());
		
	}
	
}
