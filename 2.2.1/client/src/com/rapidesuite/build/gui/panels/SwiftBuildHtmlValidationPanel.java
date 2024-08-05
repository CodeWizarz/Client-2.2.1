package com.rapidesuite.build.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.gui.environment.SwiftBuildEnvironmentSaveButtonAction;
import com.rapidesuite.client.common.gui.CopyPasteByRightClickCapableJTextField;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.gui.EnvironmentValidationProperty;
import com.rapidesuite.client.common.gui.EnvironmentValidationButtonAbstractAction.NextButtonWrapper;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class SwiftBuildHtmlValidationPanel extends SwiftBuildPropertiesValidationPanel implements
		NextButtonWrapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6176843633587045473L;
	protected SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel;
	private SwiftBuildEnvironmentSaveButtonAction swiftBuildEnvironmentSaveButtonAction;
	
	private CopyPasteByRightClickCapableJTextField buildHtmlOverrideComponent;
	private JCheckBox buildSingleSignOnComponent;
	private CopyPasteByRightClickCapableJTextField buildSingleSignOnFieldIdentifierTypeComponent;
	private CopyPasteByRightClickCapableJTextField buildSingleSignOnUsernameFieldIdentifierValueComponent;
	private CopyPasteByRightClickCapableJTextField buildSingleSignOnPasswordFieldIdentifierValueComponent;
	private CopyPasteByRightClickCapableJTextField buildSingleSignOnSubmitFieldIdentifierComponent;
	private CopyPasteByRightClickCapableJTextField buildSingleSignOnSubmitFieldIdentifierValueComponent;
	
	public SwiftBuildHtmlValidationPanel(SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel) {
		super(swiftBuildPropertiesValidationPanel.getBuildMain(), false);
		this.swiftBuildPropertiesValidationPanel = swiftBuildPropertiesValidationPanel;
		this.createComponents();
		this.swiftBuildPropertiesValidationPanel.setSwiftBuildHtmlValidationPanel(this);
	}
	
	@Override
	public void createComponents() {
		super.createComponents();
		
		this.previousButton.setEnabled(true);
		this.previousButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BuildMain.switchToPanel(SwiftBuildConstants.PANEL_ENVIRONMENT_VALIDATION);
			}
			
		});
		
		mainPanel.remove(this.environmentValidationPanel);
		
		this.environmentValidationPanel.removeAll();
		
		environmentProperties.clear();
		

		this.buildHtmlOverrideComponent = new CopyPasteByRightClickCapableJTextField();
		EnvironmentValidationProperty environmentProperty=new EnvironmentValidationProperty(SwiftBuildConstants.BUILD_HTML_URL_OVERRIDE, GUIUtils.getLabel("(*) HTML URL Override:",true),this.buildHtmlOverrideComponent,true);
		environmentProperties.add(environmentProperty);
		
		environmentProperty=new EnvironmentValidationProperty("DUMMY_EXTRA_SPACE_1",GUIUtils.getLabel(" ",true),GUIUtils.getLabel(" ",true),true);
		environmentProperties.add(environmentProperty);			

		this.buildSingleSignOnComponent = new JCheckBox();
		environmentProperty=new EnvironmentValidationProperty(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON,GUIUtils.getLabel("(*) Single Sign On:",true),this.buildSingleSignOnComponent,true);
		environmentProperties.add(environmentProperty);	
		
		this.buildSingleSignOnFieldIdentifierTypeComponent = new CopyPasteByRightClickCapableJTextField();
		this.buildSingleSignOnFieldIdentifierTypeComponent.setEnabled(false);
		environmentProperty=new EnvironmentValidationProperty(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE,GUIUtils.getLabel("(*) Single Sign On Field Identifier Type:",true),buildSingleSignOnFieldIdentifierTypeComponent,true);
		environmentProperties.add(environmentProperty);

		this.buildSingleSignOnUsernameFieldIdentifierValueComponent = new CopyPasteByRightClickCapableJTextField();
		this.buildSingleSignOnUsernameFieldIdentifierValueComponent.setEnabled(false);
		environmentProperty=new EnvironmentValidationProperty(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_USERNAME_FIELD_IDENTIFIER_VALUE,GUIUtils.getLabel("(*) Single Sign On Username Field Identifier Value:",true),this.buildSingleSignOnUsernameFieldIdentifierValueComponent,true);
		environmentProperties.add(environmentProperty);

		this.buildSingleSignOnPasswordFieldIdentifierValueComponent = new CopyPasteByRightClickCapableJTextField();
		this.buildSingleSignOnPasswordFieldIdentifierValueComponent.setEnabled(false);
		environmentProperty=new EnvironmentValidationProperty(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_PASSWORD_FIELD_IDENTIFIER_VALUE,GUIUtils.getLabel("(*) Single Sign On Password Field Identifier Value:",true),this.buildSingleSignOnPasswordFieldIdentifierValueComponent,true);
		environmentProperties.add(environmentProperty);		
		
		this.buildSingleSignOnSubmitFieldIdentifierComponent = new CopyPasteByRightClickCapableJTextField();
		this.buildSingleSignOnSubmitFieldIdentifierComponent.setEnabled(false);
		environmentProperty=new EnvironmentValidationProperty(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER,GUIUtils.getLabel("(*) Single Sign On Submit Field Identifier:",true),this.buildSingleSignOnSubmitFieldIdentifierComponent,true);
		environmentProperties.add(environmentProperty);
		
		this.buildSingleSignOnSubmitFieldIdentifierValueComponent = new CopyPasteByRightClickCapableJTextField();
		this.buildSingleSignOnSubmitFieldIdentifierValueComponent.setEnabled(false);
		environmentProperty=new EnvironmentValidationProperty(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER_VALUE,GUIUtils.getLabel("(*) Single Sign On Submit Field Identifier Value:",true),this.buildSingleSignOnSubmitFieldIdentifierValueComponent,true);
		environmentProperties.add(environmentProperty);				
		
		buildSingleSignOnComponent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateBuildSingleSignOnTextFieldEnabledProperty();
			}
			
		});
		
		this.addComponentListener(new ComponentListener(){

			@Override
			public void componentHidden(ComponentEvent arg0) {
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}

			@Override
			public void componentResized(ComponentEvent arg0) {
			}

			@Override
			public void componentShown(ComponentEvent arg0) {
				updateBuildSingleSignOnTextFieldEnabledProperty();
			}
			
		});
		
		swiftBuildEnvironmentSaveButtonAction = new SwiftBuildEnvironmentSaveButtonAction(this);
		
		this.environmentValidationPanel = new EnvironmentValidationPanel("Oracle Standard Single Sign On",
				environmentProperties,
				filesExtensionAllowed,
				null,
				swiftBuildEnvironmentSaveButtonAction,
				null,
				optionalLabel,
				scrollPaneDimension,
				this,
				this.BuildMain,
				this.swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().getAllEnvironmentProperties()) {

			/**
					 * 
					 */
					private static final long serialVersionUID = -2063136989770776507L;

			@Override
			public Map<String,String> getEnvironmentPropertiesMap() {
				Map<String,String> toReturn=new HashMap<String,String>();
				
				Map<String, String> firstScreenProperties = swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().getEnvironmentPropertiesMap();
				firstScreenProperties.putAll(swiftBuildPropertiesValidationPanel.getEnvironmentExtraPropertiesFrame().getExtraProperties());			
				toReturn.putAll(firstScreenProperties);
				
				for (EnvironmentValidationProperty environmentValidationProperty:SwiftBuildHtmlValidationPanel.this.environmentProperties) {
					String value;
					try {
						value = getValueFromComponent(environmentValidationProperty);
						toReturn.put(environmentValidationProperty.getKey(),value);
					} catch (Exception e) {
						FileUtils.printStackTrace(e);
					}
				}

				return toReturn;
			}			
			
		};
		
		loadSaveValidateButtonsPanel.removeAll();
		if (environmentValidationPanel.getSaveFileButton() != null) {
			loadSaveValidateButtonsPanel.add(environmentValidationPanel.getSaveFileButton());
		}

		// createEmptyBorder(int top, int left, int bottom, int right)
		environmentValidationPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
		mainPanel.add(environmentValidationPanel);		
	}
	private void updateBuildSingleSignOnTextFieldEnabledProperty() {
		boolean isSelected = buildSingleSignOnComponent.isSelected();
		buildSingleSignOnFieldIdentifierTypeComponent.setEnabled(isSelected);
		buildSingleSignOnUsernameFieldIdentifierValueComponent.setEnabled(isSelected);
		buildSingleSignOnPasswordFieldIdentifierValueComponent.setEnabled(isSelected);
		buildSingleSignOnSubmitFieldIdentifierComponent.setEnabled(isSelected);
		buildSingleSignOnSubmitFieldIdentifierValueComponent.setEnabled(isSelected);
	}
	
	@Override
	public void clickNextButtonActionPerformed()
	{
		BuildMain.switchToPanel(SwiftBuildConstants.PANEL_INJECTORS_PACKAGE_SELECTION);
		BuildMain.getInjectorsPackageSelectionPanel().loadInjectorsPackage();
	}	
	
	public void setFieldValues() {
		for (final Map.Entry<String, String> entry : this.environmentValidationPanel.getAllEnvironmentProperties().entrySet()) {
			if (SwiftBuildConstants.BUILD_HTML_URL_OVERRIDE.equals(entry.getKey())) {
				this.buildHtmlOverrideComponent.setText(entry.getValue());
			} else if (SwiftBuildConstants.BUILD_SINGLE_SIGN_ON.equals(entry.getKey())) {
				boolean isSelected = Boolean.TRUE.toString().equalsIgnoreCase(entry.getValue());
				this.buildSingleSignOnComponent.setSelected(isSelected);
				if (isSelected) {
					this.buildSingleSignOnUsernameFieldIdentifierValueComponent.setEnabled(isSelected);
					this.buildSingleSignOnPasswordFieldIdentifierValueComponent.setEnabled(isSelected);
					this.buildSingleSignOnSubmitFieldIdentifierComponent.setEnabled(isSelected);
					this.buildSingleSignOnSubmitFieldIdentifierValueComponent.setEnabled(isSelected);
				}
			} else if (SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE.equals(entry.getKey())) {
				this.buildSingleSignOnFieldIdentifierTypeComponent.setText(entry.getValue());
			} else if (SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_USERNAME_FIELD_IDENTIFIER_VALUE.equals(entry.getKey())) {
				this.buildSingleSignOnUsernameFieldIdentifierValueComponent.setText(entry.getValue());
			} else if (SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_PASSWORD_FIELD_IDENTIFIER_VALUE.equals(entry.getKey())) {
				this.buildSingleSignOnPasswordFieldIdentifierValueComponent.setText(entry.getValue());
			} else if (SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER.equals(entry.getKey())) {
				this.buildSingleSignOnSubmitFieldIdentifierComponent.setText(entry.getValue());
			} else if (SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER_VALUE.equals(entry.getKey())) {
				this.buildSingleSignOnSubmitFieldIdentifierValueComponent.setText(entry.getValue());
			}
		}
	}
	
	public String getBuildHtmlOverride() {
		if (this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().containsKey(SwiftBuildConstants.BUILD_HTML_URL_OVERRIDE)) {
			return this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().get(SwiftBuildConstants.BUILD_HTML_URL_OVERRIDE);
		} else {
			return null;
		}
	}
	
	public boolean getBuildSingleSignOn() {
		if (this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().containsKey(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON)) {
			return Boolean.TRUE.toString().equalsIgnoreCase(this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON));
		} else {
			return false;
		}		
	}

	public String getBuildSingleSignOnFieldIdentifierType() {
		if (this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().containsKey(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE)) {
			return this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE);
		} else {
			return null;
		}
	}
	
	public String getBuildSingleSignOnUsernameFieldIdentifierValue() {
		if (this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().containsKey(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_USERNAME_FIELD_IDENTIFIER_VALUE)) {
			return this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_USERNAME_FIELD_IDENTIFIER_VALUE);
		} else {
			return null;
		}
	}
	
	public String getBuildSingleSignOnPasswordFieldIdentifierValue() {
		if (this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().containsKey(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_PASSWORD_FIELD_IDENTIFIER_VALUE)) {
			return this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_PASSWORD_FIELD_IDENTIFIER_VALUE);
		} else {
			return null;
		}
	}	

	public String getBuildSingleSignOnSubmitFieldIdentifier() {
		if (this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().containsKey(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER)) {
			return this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER);
		} else {
			return null;
		}
	}
	
	public String getBuildSingleSignOnSubmitFieldIdentifierValue() {
		if (this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().containsKey(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER_VALUE)) {
			return this.getEnvironmentValidationPanel().getEnvironmentPropertiesMap().get(SwiftBuildConstants.BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER_VALUE);
		} else {
			return null;
		}
	}
	
}
