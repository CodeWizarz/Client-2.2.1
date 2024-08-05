/**************************************************
 * $Revision: 51396 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2015-12-18 18:46:03 +0700 (Fri, 18 Dec 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/EnvironmentValidationPanel.java $:
 * $Id: EnvironmentValidationPanel.java 51396 2015-12-18 11:46:03Z jannarong.wadthong $:
 */

package com.rapidesuite.client.common.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.gui.environment.EnableDiagnosticsButtonAction;
import com.rapidesuite.build.gui.panels.SwiftBuildHtmlValidationPanel;
import com.rapidesuite.build.gui.panels.SwiftBuildPropertiesValidationPanel;
import com.rapidesuite.client.common.ProgrammaticallyOperable;
import com.rapidesuite.client.common.gui.EnvironmentValidationButtonAbstractAction.NextButtonWrapper;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;


@SuppressWarnings("serial")
public class EnvironmentValidationPanel extends JPanel implements ProgrammaticallyOperable
{
	private final Map<String,String> filesExtensionAllowed;
	private final EnvironmentValidationButtonAbstractAction validateButtonAction;
	private final EnvironmentValidationButtonAbstractAction loadEnvironmentFileButtonAction;
	private final EnvironmentValidationButtonAbstractAction saveEnvironmentFileButtonAction;
	protected final List<EnvironmentValidationProperty> environmentValidationProperties;
	private Map<String,EnvironmentValidationProperty> environmentValidationPropertiesMap;
	private Map<String,JLabel> keyToStatusLabelMap;
	private JLabel messageLabel;
	private volatile boolean isValidationStarted = false;
	private JButton validateButton;
	private JButton loadFileButton;
	private JButton saveFileButton;
	private Map<String,String> environmentValuesOld;
	private volatile boolean isValidationSuccess;
	private File loadedFile;
	private File savedFile;
	private NextButtonWrapper nextButtonWrapper;
	public NextButtonWrapper getNextButtonWrapper()
	{
	    return this.nextButtonWrapper;
	}

	private final ActionListener onValidateBwe;
	private final SwiftGUIMain swiftGuiMain;
	
	private final Map<String, String> allEnvironmentProperties;
	
	public EnvironmentValidationPanel(
			String panelTitle,
			List<EnvironmentValidationProperty> environmentValidationProperties,
			final Map<String,String> filesExtensionAllowed,
			final EnvironmentValidationButtonAbstractAction loadEnvironmentFileButtonAction,
			final EnvironmentValidationButtonAbstractAction saveEnvironmentFileButtonAction,
			final EnvironmentValidationButtonAbstractAction validateButtonAction,
			JLabel bottomLabel,
			Dimension scrollPaneDimension,
			NextButtonWrapper nextButtonWrapper,
			final SwiftGUIMain swiftGuiMain) {
		this(panelTitle,
				environmentValidationProperties,
				filesExtensionAllowed,
				loadEnvironmentFileButtonAction,
				saveEnvironmentFileButtonAction,
				validateButtonAction,
				bottomLabel,
				scrollPaneDimension,
				nextButtonWrapper,
				swiftGuiMain,
				new HashMap<String, String>());
	}

	public EnvironmentValidationPanel(
			String panelTitle,
			List<EnvironmentValidationProperty> environmentValidationProperties,
			final Map<String,String> filesExtensionAllowed,
			final EnvironmentValidationButtonAbstractAction loadEnvironmentFileButtonAction,
			final EnvironmentValidationButtonAbstractAction saveEnvironmentFileButtonAction,
			final EnvironmentValidationButtonAbstractAction validateButtonAction,
			JLabel bottomLabel,
			Dimension scrollPaneDimension,
			NextButtonWrapper nextButtonWrapper,
			final SwiftGUIMain swiftGuiMain,
			final Map<String, String> environmentProperties)
	{
		this.swiftGuiMain = swiftGuiMain;
		//Integer.MAX_VALUE is a de facto infinite value
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, SwiftBuildConstants.ENVIRONMENT_VALIDATION_PANEL_MAX_HEIGHT));
		this.filesExtensionAllowed=filesExtensionAllowed;
		this.environmentValidationProperties=environmentValidationProperties;
		this.loadEnvironmentFileButtonAction=loadEnvironmentFileButtonAction;
		this.saveEnvironmentFileButtonAction=saveEnvironmentFileButtonAction;
		this.validateButtonAction=validateButtonAction;
		this.nextButtonWrapper = nextButtonWrapper;
		isValidationStarted=false;
		environmentValidationPropertiesMap=new HashMap<String,EnvironmentValidationProperty>();
		for (EnvironmentValidationProperty environmentValidationProperty:environmentValidationProperties) {
			if (environmentValidationProperty.getKey()!=null) {
				environmentValidationPropertiesMap.put(environmentValidationProperty.getKey(),environmentValidationProperty);
			}
		}

		this.setLayout(new GridLayout(1,1));
		JPanel mainPanel= new JPanel();
	    add(mainPanel);

		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(panelTitle),
                BorderFactory.createEmptyBorder(5,5,5,5)));

		// 3 columns: title, value(s), status label.
		JPanel panel = new JPanel();
		panel.setBackground(new Color(223,223,223));
		final GridBagLayout gl=new GridBagLayout();
		panel.setLayout(gl);

	    if (this.validateButtonAction == null || !(this.validateButtonAction instanceof EnableDiagnosticsButtonAction) )
	    {
    		JScrollPane scrollPane = new JScrollPane(panel,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
    				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    		scrollPane.setPreferredSize(scrollPaneDimension);
    		mainPanel.add(scrollPane);
	    }
		final GridBagConstraints gbc = new GridBagConstraints();

		final int leftMargin = 3;
		final int rightMargin = 3;
		gbc.insets = new Insets(3,leftMargin,3,rightMargin);  // put spacing around this field.
		keyToStatusLabelMap=new HashMap<String,JLabel>();

		final Map<JLabel, JComponent> labelToComponentMap = new HashMap<JLabel, JComponent>();
		final Map<JLabel, JLabel> labelToStatusMap = new HashMap<JLabel, JLabel>();

		// a JPanel can be passed as a VALUE so that we can display buttons,...
		int row=0;
		for (EnvironmentValidationProperty environmentValidationProperty:environmentValidationProperties) {
			
			JLabel statusLabel=GUIUtils.getLabel("",true);
			if (environmentValidationProperty.getKey()!=null) {
				keyToStatusLabelMap.put(environmentValidationProperty.getKey(),statusLabel);
			}
			GUIUtils.setGridBagConstraints(gbc,row, 0,1, 1, GridBagConstraints.HORIZONTAL);
			gl.setConstraints(environmentValidationProperty.getLabelComponent(), gbc);
			panel.add(environmentValidationProperty.getLabelComponent());

			if (environmentValidationProperty.getValueComponent() instanceof JButton) {
				GUIUtils.setGridBagConstraints(gbc,row, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
			} else {
				GUIUtils.setGridBagConstraints(gbc,row, 1, 1, 1, GridBagConstraints.HORIZONTAL);
			}


			gl.setConstraints(environmentValidationProperty.getValueComponent(), gbc);
			panel.add(environmentValidationProperty.getValueComponent());

			GUIUtils.setGridBagConstraints(gbc,row, 2, 1, 1, GridBagConstraints.HORIZONTAL);
			gl.setConstraints(statusLabel, gbc);
			panel.add(statusLabel);

			labelToComponentMap.put(environmentValidationProperty.getLabelComponent(), environmentValidationProperty.getValueComponent());
			labelToStatusMap.put(environmentValidationProperty.getLabelComponent(), statusLabel);

			row++;
		}

		onValidateBwe = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				actionPerformedValidationButton(false);
			}
		};

		class ComponentListenerImpl implements ComponentListener {

			private final JPanel panel;
			private final Map<JLabel, JComponent> labelToComponentMap;
			private final Map<JLabel, JLabel> labelToStatusMap;

			public ComponentListenerImpl(final JPanel panel, final Map<JLabel, JComponent> labelToComponentMap, final Map<JLabel, JLabel> labelToStatusMap) {
				this.panel = panel;
				this.labelToComponentMap = labelToComponentMap;
				this.labelToStatusMap = labelToStatusMap;
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent arg0) {
				Dimension panelSize = this.panel.getSize();
				panelSize.setSize(arg0.getComponent().getSize().getWidth(), panelSize.getHeight());
				this.panel.setSize(panelSize);

				for (Map.Entry<JLabel, JComponent> entry : this.labelToComponentMap.entrySet()) {

					if (entry.getValue() instanceof JButton) {
						continue;
					}

					Dimension labelSize = entry.getKey().getSize();
					Dimension fieldSize = entry.getValue().getSize();
					Dimension statusSize = this.labelToStatusMap.get(entry.getKey()).getSize();

					int newFieldWidth = Math.max(SwiftBuildConstants.ENVIRONMENT_VALIDATION_PANEL_MIN_TEXT_FIELD_WIDTH, (int) (this.panel.getVisibleRect().getWidth() - labelSize.getWidth() - statusSize.getWidth() - 3*leftMargin - 3*rightMargin) - SwiftBuildConstants.ENVIRONMENT_VALIDATION_PANEL_MAX_SIDE_PADDING);

					Dimension newFieldSize = new Dimension(newFieldWidth, (int) fieldSize.getHeight());
					entry.getValue().setPreferredSize(newFieldSize);
					entry.getValue().setSize(newFieldSize);

					entry.getValue().repaint();
				}
			}

			@Override
			public void componentShown(ComponentEvent arg0) {
				// TODO Auto-generated method stub

			}

		}

		mainPanel.addComponentListener(new ComponentListenerImpl(panel, labelToComponentMap, labelToStatusMap));

		if (this.loadEnvironmentFileButtonAction != null) {
			loadFileButton=GUIUtils.getButton(this.getClass(),"Load from","/images/open16.gif");
			loadFileButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					actionPerformedLoadFileButton();
				}
			});
		}

		if (this.saveEnvironmentFileButtonAction != null) {
			saveFileButton=GUIUtils.getButton(this.getClass(),"Save to","/images/save16.gif");
			saveFileButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					actionPerformedSaveFileButton();
				}
			});
		}

		if (this.validateButtonAction != null) {
			validateButton=GUIUtils.getButton(this.getClass(),"Start validation","/images/validate.gif");
			validateButton.addActionListener(onValidateBwe);			
		}

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel , BoxLayout.Y_AXIS));
		southPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		mainPanel.add(southPanel,BorderLayout.SOUTH);

		panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		southPanel.add(panel);
		if (bottomLabel!=null) {
			panel.add(bottomLabel);
			panel.add(Box.createRigidArea(new Dimension(15,6)));
		}
		messageLabel=GUIUtils.getLabel("",true);
		messageLabel.setFont(GUIUtils.BOLD_SYSTEM_FONT);
		panel.add(messageLabel);
		
		this.allEnvironmentProperties = environmentProperties;
	}

	public JButton getLoadFileButton() {
		return loadFileButton;
	}

	public JButton getSaveFileButton() {
		return saveFileButton;
	}

	public void setPropertiesSuccessStatusLabel() throws Exception {
		setPropertiesStatusLabel(GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME);
	}

	public void setPropertiesErrorStatusLabel() throws Exception {
		setPropertiesStatusLabel(GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME);
	}

	public void setPropertiesWarningStatusLabel() throws Exception {
		setPropertiesStatusLabel(GUIUtils.PROPERTY_WARNING_STATUS_IMAGE_NAME);
	}

	public void setPropertiesResetStatusLabel() throws Exception {
		setPropertiesStatusLabel(null);
	}

	private void setPropertiesStatusLabel(String imageName) throws Exception {
		Map<String,String> properties=getEnvironmentPropertiesMap();
		Iterator<String> iterator=properties.keySet().iterator();
		ImageIcon ii=null;
		if (imageName!=null) {
			ii=GUIUtils.getImageIcon(this.getClass(),imageName);
		}
		while (iterator.hasNext()) {
			String key=iterator.next();
			JLabel statusLabel=keyToStatusLabelMap.get(key);
			statusLabel.setIcon(ii);
		}
	}

	public void setPropertyStatusLabel(String propertyName,String imageName) {
		ImageIcon ii=null;
		if (imageName!=null) {
			ii=GUIUtils.getImageIcon(this.getClass(),imageName);
		}
		JLabel statusLabel=keyToStatusLabelMap.get(propertyName);
		if ( null != statusLabel )
		{
	        statusLabel.setIcon(ii);
		}
	}

	public void actionPerformedLoadFileButton()
	{
		if (loadEnvironmentFileButtonAction == null) {
			return;
		}
		
		try{
			JFileChooser fileChooser=Utils.initializeJFileChooserWithTheLastPath("FILE_CHOOSER_ENVIRONMENT_VALIDATION_PANEL_ACTION_PERFORMED_LOAD_FILE_BUTTON");
			if (loadedFile!=null) {
				fileChooser.setSelectedFile(loadedFile);
			}
			CustomFileFilter filter=new CustomFileFilter(filesExtensionAllowed);
			fileChooser.setFileFilter(filter);

			int returnVal = fileChooser.showDialog(null, "Select a file");
			if (returnVal == JFileChooser.APPROVE_OPTION){
				loadedFile = fileChooser.getSelectedFile();
				setPropertiesResetStatusLabel();
				boolean isActionToBeExecuted=loadEnvironmentFileButtonAction.beforeExecuteAction(this);
				if (isActionToBeExecuted) {
					loadEnvironmentFileButtonAction.executeAction(this);
					loadEnvironmentFileButtonAction.afterExecuteAction(this, nextButtonWrapper);
				}
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(messageLabel,"Error loading file: "+e.getMessage());
		}
	}

	public File getLoadedFile() {
		return loadedFile;
	}

	public void actionPerformedSaveFileButton()
	{
		if (this.saveEnvironmentFileButtonAction == null) {
			return;
		}
		
		try{
			JFileChooser fileChooser=Utils.initializeJFileChooserWithTheLastPath("FILE_CHOOSER_ENVIRONMENT_VALIDATION_PANEL_ACTION_PERFORMED_SAVE_FILE_BUTTON");
			if (loadedFile!=null) {
				fileChooser.setSelectedFile(loadedFile);
			}
			else {
				if (savedFile!=null) {
					fileChooser.setSelectedFile(savedFile);
				}
			}
			CustomFileFilter filter=new CustomFileFilter(filesExtensionAllowed);
			fileChooser.setFileFilter(filter);

			int returnVal = fileChooser.showDialog(null, "Select a file");
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				savedFile = fileChooser.getSelectedFile();
				boolean isActionToBeExecuted=saveEnvironmentFileButtonAction.beforeExecuteAction(this);
				if (isActionToBeExecuted) {
					saveEnvironmentFileButtonAction.executeAction(this);
					saveEnvironmentFileButtonAction.afterExecuteAction(this, nextButtonWrapper);
				}
			}
		}
		catch (Exception e) {
			GUIUtils.showErrorMessage(messageLabel,"Error saving file: "+e.getMessage());
		}
	}

	public void actionPerformedValidationButton(boolean isSynchronous) {
		if (this.validateButtonAction == null) {
			return;
		}
		
		if (isValidationStarted) {
			isValidationStarted=false;
			validateButton.setText("Start validation");
			enableComponents();
			GUIUtils.showErrorMessage(messageLabel,"Manual stop.");
		}
		else {
			isValidationStarted=true;
			validateButton.setText("Stop validation");
			disableComponents();
			GUIUtils.showInProgressMessage(messageLabel,"Working...");

			EnvironmentValidationThread thread=new EnvironmentValidationThread(this);

			Thread t = new Thread(thread);
			t.start();
			if (Config.isAutomatedRun() || isSynchronous) {
				try {
					t.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					FileUtils.printStackTrace(e1);
				}
			}


		}
	}

	public void disableComponents() {
		setComponentsStatus(false);
	}

	public void enableComponents() {
		setComponentsStatus(true);
	}

	public void setComponentsStatus(boolean isEnabled) {
		for (EnvironmentValidationProperty environmentValidationProperty:environmentValidationProperties) {
			environmentValidationProperty.getValueComponent().setEnabled(isEnabled);
		}
		try {
			loadFileButton.setEnabled(isEnabled);
			saveFileButton.setEnabled(isEnabled);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public void storeValues() throws Exception {
		environmentValuesOld=new HashMap<String,String>();
		for (EnvironmentValidationProperty environmentValidationProperty:environmentValidationProperties) {
			String value=getValueFromComponent(environmentValidationProperty);
			environmentValuesOld.put(environmentValidationProperty.getKey(),value);
		}
	}

	public boolean hasChangedValues() throws Exception {
		if (environmentValuesOld==null) {
			return false;
		}
		Map<String,String> properties=getEnvironmentPropertiesMap();
		Iterator<String> iterator=properties.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			String value=properties.get(key);
			String oldValue=environmentValuesOld.get(key);
			if (!value.equals(oldValue)) {
				return true;
			}
		}
		return false;
	}

	public void loadEnvironment(File file) throws Exception {
		loadedFile=file;
		loadEnvironment(FileUtils.loadBwe(loadedFile));
		setValidationSuccess(false);
	}

	public void loadEnvironment(Map<String,String> properties) throws Exception
	{
		this.allEnvironmentProperties.clear();
		this.allEnvironmentProperties.putAll(properties);
		
		Iterator<String> iterator=environmentValidationPropertiesMap.keySet().iterator();
		int mandatoryPropertiesLoadedCounter=0;
		int mandatoryPropertiesCounter=0;
		while (iterator.hasNext()) {
			String key=iterator.next();
			EnvironmentValidationProperty environmentValidationProperty=environmentValidationPropertiesMap.get(key);
			if (!environmentValidationProperty.isOptional()) {
				mandatoryPropertiesCounter++;
			}

			String value=properties.get(environmentValidationProperty.getKey());

			if ( value!=null) {
				setValueFromComponent(environmentValidationProperty,value);
				if (!environmentValidationProperty.isOptional()) {
					mandatoryPropertiesLoadedCounter++;
				}
			}
			else {
				if (!environmentValidationProperty.isOptional()) {
					setPropertyStatusLabel(environmentValidationProperty.getKey(),GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME);
				}
			}
		}

		if (mandatoryPropertiesLoadedCounter==mandatoryPropertiesCounter) {
			GUIUtils.showSuccessMessage(messageLabel,"Properties loaded.");
		}
		else {
			GUIUtils.showErrorMessage(messageLabel,"Incomplete file: missing "+
					(mandatoryPropertiesCounter-mandatoryPropertiesLoadedCounter)+" required properties.");
		}
		// Reset message of SwiftBuildHtmlValidationPanel every time user loads BWE file
		if(getNextButtonWrapper() instanceof SwiftBuildPropertiesValidationPanel){
			SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel = (SwiftBuildPropertiesValidationPanel) getNextButtonWrapper();
			SwiftBuildHtmlValidationPanel swiftBuildHtmlValidationPanel = swiftBuildPropertiesValidationPanel.getSwiftBuildHtmlValidationPanel();
			GUIUtils.showStandardMessage(swiftBuildHtmlValidationPanel.getMessageLabel(), "");
		}
	}

	public void saveEnvironment(File file) throws Exception {
		savedFile=file;
	}

	public void saveEnvironment(Map<String,String> properties) throws Exception
	{
		FileUtils.savePropertiesToFile(null,savedFile,properties,true);
		GUIUtils.showSuccessMessage(messageLabel,"All properties saved.");
	}

	public JLabel getMessageLabel() {
		return messageLabel;
	}

	public Map<String, EnvironmentValidationProperty> getEnvironmentValidationPropertiesMap() {
		return environmentValidationPropertiesMap;
	}

	public Map<String,String> getEnvironmentPropertiesMap() {
		Map<String,String> toReturn=new HashMap<String,String>();
		for (EnvironmentValidationProperty environmentValidationProperty:environmentValidationProperties) {
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

	public String getValueFromComponent(EnvironmentValidationProperty environmentValidationProperty) throws Exception {
		JComponent valueComponent=environmentValidationProperty.getValueComponent();
		if (valueComponent instanceof JTextComponent) {
			return ((JTextComponent)valueComponent).getText();
		}
		else
		if (valueComponent instanceof JPasswordField) {
			char[] res=((JPasswordField)valueComponent).getPassword();
			return new String(res);
		}
		else
		if (valueComponent instanceof JComboBox) {
			String value=(String) ((JComboBox)valueComponent).getSelectedItem();
			if (environmentValidationProperty.getMappings()!=null) {
				value=environmentValidationProperty.getMappings().get(value);
			}
			return value;
		}
		else
		if (valueComponent instanceof JPanel) {
			JPanel panel=((JPanel)valueComponent);
			JTextComponent comp=(JTextComponent)panel.getComponent(0);
			return comp.getText();
		}
		else
		if (valueComponent instanceof JCheckBox) {
			Boolean value=((JCheckBox)valueComponent).isSelected();
			return value.toString();
		}
		return null;
	}

	public void setValueFromComponent(EnvironmentValidationProperty environmentValidationProperty,String value) throws Exception {
		setOrClearValueFromComponent(environmentValidationProperty, value, false);
	}

	public void clearValueFromComponents() {
		for (final EnvironmentValidationProperty environmentValidationProperty : this.environmentValidationPropertiesMap.values()) {
			setOrClearValueFromComponent(environmentValidationProperty, null, true);
		}
	}

	private void setOrClearValueFromComponent(final EnvironmentValidationProperty environmentValidationProperty, final String value, final boolean isClearing) {
		JComponent valueComponent=environmentValidationProperty.getValueComponent();
		if (valueComponent instanceof JTextComponent ) {
			((JTextComponent)valueComponent).setText(isClearing?"":value);
		}
		else
		if (valueComponent instanceof JPasswordField) {
			((JPasswordField)valueComponent).setText(isClearing?"":value);
		}
		else
		if (valueComponent instanceof JPanel) {
			JPanel panel=((JPanel)valueComponent);
			JTextComponent comp=(JTextComponent)panel.getComponent(0);
			comp.setText(isClearing?"":value);
		}
		else
		if (valueComponent instanceof JComboBox) {
			if (isClearing) {
				((JComboBox)valueComponent).setSelectedIndex(-1);
			} else {
				String val=value;
				if (environmentValidationProperty.getMappings()!=null) {
					val=environmentValidationProperty.getMappingValue(value);
				}
				((JComboBox)valueComponent).setSelectedItem(val);
			}

		}
	}

	public JButton getValidateButton() {
		return validateButton;
	}

	public boolean isValidationStarted() {
		return isValidationStarted;
	}

	public void setValidationStartedFlag(boolean isValidationStarted) {
		this.isValidationStarted=isValidationStarted;
	}

	public boolean isValidationSuccess() {
		return isValidationSuccess;
	}

	public void setValidationSuccess(boolean isValidationSuccess) {
		this.isValidationSuccess = isValidationSuccess;
	}

	public EnvironmentValidationButtonAbstractAction getValidateButtonAction() {
		return validateButtonAction;
	}

	public EnvironmentValidationButtonAbstractAction getLoadEnvironmentFileButtonAction() {
		return this.loadEnvironmentFileButtonAction;
	}


	@Override
	public boolean operate() throws Throwable {
		Assert.isTrue(Config.isAutomatedRun(), "must be in automated run mode");

		this.getLoadEnvironmentFileButtonAction().beforeExecuteAction(this);
		boolean isActionToBeExecuted=this.getLoadEnvironmentFileButtonAction().beforeExecuteAction(this);
		if (isActionToBeExecuted) {
			this.getLoadEnvironmentFileButtonAction().executeAction(this);
			this.getLoadEnvironmentFileButtonAction().afterExecuteAction(this, this.getNextButtonWrapper());
		}

		if (Config.isEnvironmentValidationMandatory()) {
			this.onValidateBwe.actionPerformed(null); //bwe validation is in this thread, so practically the thread is "waiting" here
		}

		if (this.getNextButtonWrapper().isNextButtonEnabled()) {
			this.getNextButtonWrapper().simulateClickingNext();
			return true;
		} else {
			return false;
		}
	}
	
	public SwiftGUIMain getSwiftGuiMain() {
		return this.swiftGuiMain;
	}
	
	public Map<String, String> getAllEnvironmentProperties() {
		return this.allEnvironmentProperties;
	}	

}