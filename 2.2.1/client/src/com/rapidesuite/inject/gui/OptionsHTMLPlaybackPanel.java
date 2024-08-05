package com.rapidesuite.inject.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.HubRequestsManager;
import com.rapidesuite.inject.selenium.NodesInfo;

@SuppressWarnings("serial")
public class OptionsHTMLPlaybackPanel extends JPanel {

	public static final String DEFAULT_HTMLPLAYBACK_WORKERS_COUNT="DEFAULT_HTMLPLAYBACK_WORKERS_COUNT";
	public static final String DEFAULT_HTMLPLAYBACK_IS_HIGHLIGHT="DEFAULT_HTMLPLAYBACK_IS_HIGHLIGHT";
	public static final String DEFAULT_HTMLPLAYBACK_BROWSER_TIMEOUT="DEFAULT_HTMLPLAYBACK_BROWSER_TIMEOUT";
	public static final String DEFAULT_HTMLPLAYBACK_BATCH_SIZE_LEVEL="DEFAULT_HTMLPLAYBACK_BATCH_SIZE_LEVEL";
	public static final String DEFAULT_HTMLPLAYBACK_HUB_ID="DEFAULT_HTMLPLAYBACK_HUB_ID";
	public static final String DEFAULT_HTMLPLAYBACK_SERVER_MODE="DEFAULT_HTMLPLAYBACK_SERVER_MODE";
	public static final String DEFAULT_HTMLPLAYBACK_LOGIN_PAGE_REDIRECT_PAUSE="DEFAULT_HTMLPLAYBACK_LOGIN_PAGE_REDIRECT_PAUSE";
	public static final String DEFAULT_FAIL_BATCHES_ON_ERROR="DEFAULT_FAIL_BATCHES_ON_ERROR";

	public static final int DEFAULT_VALUE_HTMLPLAYBACK_WORKERS_COUNT=1;
	public static final boolean DEFAULT_VALUE_HTMLPLAYBACK_IS_HIGHLIGHT=true;
	public static final int DEFAULT_VALUE_HTMLPLAYBACK_BROWSER_TIMEOUT=2;
	public static final int DEFAULT_VALUE_HTMLPLAYBACK_BATCH_SIZE_LEVEL=10;
	public static final String DEFAULT_VALUE_HTMLPLAYBACK_HUB_ID="127.0.0.1:4444";
	public static final boolean DEFAULT_VALUE_HTMLPLAYBACK_SERVER_MODE=true;
	public static final int DEFAULT_VALUE_HTMLPLAYBACK_LOGIN_PAGE_REDIRECT_PAUSE=2;
	public static final boolean DEFAULT_VALUE_FAIL_BATCHES_ON_ERROR=false;
	
	public static final String TEXT_BUSY_NODES="Busy Nodes: ";
	public static final String TEXT_FREE_NODES="Free Nodes: ";
	
	private JCheckBox highlightWebElementsCheckBox;
	private JTextField timeoutTextField;
	private JTextField maxRecordsPerWorkerTextField;
	private JTextField maxWorkersTextField;
	private JTextField hubIdTextField;
	private JTextField pauseTextField;
	private JRadioButton standaloneRadioButton;
	private JRadioButton hubRadioButton;
	private DefaultListModel<String> busyNodeslistModel;
	private DefaultListModel<String> freeNodeslistModel;
	private JLabel busyNodeslabel;
	private JLabel freeNodesLabel;
	private OptionsTabPanel optionsTabPanel;
	private JCheckBox failBatchesOnErrorCheckBox;

	private CardLayout cardLayout;
	private JPanel cardComponent;
	
	public OptionsHTMLPlaybackPanel(OptionsTabPanel optionsTabPanel){
		this.optionsTabPanel=optionsTabPanel;
		this.setOpaque(false);
		//this.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		//this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setLayout(new BorderLayout());
		this.setBorder(InjectUtils.getTitleBorder("HTML Playback") );
		
		JPanel topPanel=new JPanel();
		this.add(topPanel,BorderLayout.NORTH);
		topPanel.setBorder(BorderFactory.createEmptyBorder(5,10, 5, 5));
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		
		JPanel tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		topPanel.add(tempPanel);
		highlightWebElementsCheckBox=new JCheckBox("Highlight Web elements");
		highlightWebElementsCheckBox.setOpaque(false);
		highlightWebElementsCheckBox.setContentAreaFilled(false);
		InjectUtils.assignArialPlainFont(highlightWebElementsCheckBox,ExecutionPanel.FONT_SIZE_SMALL);
		tempPanel.add(highlightWebElementsCheckBox);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 5)));
			
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		topPanel.add(tempPanel);
		JLabel label=new JLabel("Firefox Timeout in mins:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_SMALL);
		timeoutTextField = new JTextField();
		InjectUtils.assignArialPlainFont(timeoutTextField,ExecutionPanel.FONT_SIZE_SMALL);
		InjectUtils.setSize(timeoutTextField,50,25);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		tempPanel.add(timeoutTextField);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		topPanel.add(tempPanel);
		label=new JLabel("Login Redirection pause in secs:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_SMALL);
		pauseTextField = new JTextField();
		InjectUtils.assignArialPlainFont(pauseTextField,ExecutionPanel.FONT_SIZE_SMALL);
		InjectUtils.setSize(pauseTextField,50,25);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		tempPanel.add(pauseTextField);
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		topPanel.add(tempPanel);
		label=new JLabel("Data Batch size:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_SMALL);
		maxRecordsPerWorkerTextField = new JTextField();
		InjectUtils.assignArialPlainFont(maxRecordsPerWorkerTextField,ExecutionPanel.FONT_SIZE_SMALL);
		InjectUtils.setSize(maxRecordsPerWorkerTextField,50,25);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		tempPanel.add(maxRecordsPerWorkerTextField);
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		topPanel.add(tempPanel);
		failBatchesOnErrorCheckBox=new JCheckBox("Fail All remaining Data Batches on error");
		failBatchesOnErrorCheckBox.setOpaque(false);
		failBatchesOnErrorCheckBox.setContentAreaFilled(false);
		InjectUtils.assignArialPlainFont(failBatchesOnErrorCheckBox,ExecutionPanel.FONT_SIZE_SMALL);
		tempPanel.add(failBatchesOnErrorCheckBox);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		
		cardLayout = new CardLayout();
		cardComponent = new JPanel(cardLayout);
		cardComponent.setOpaque(true);
		cardComponent.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		standaloneRadioButton = new JRadioButton("Standalone Mode");
		standaloneRadioButton.setOpaque(false);
		standaloneRadioButton.setContentAreaFilled(false);
		InjectUtils.assignArialPlainFont(standaloneRadioButton,ExecutionPanel.FONT_SIZE_SMALL);
		standaloneRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cardLayout.show(cardComponent,"STANDALONE");
			}
		});		
		hubRadioButton = new JRadioButton("Server Mode");
		hubRadioButton.setOpaque(false);
		hubRadioButton.setContentAreaFilled(false);
		InjectUtils.assignArialPlainFont(hubRadioButton,ExecutionPanel.FONT_SIZE_SMALL);
		hubRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cardLayout.show(cardComponent,"SERVER");
			}
		});	

		ButtonGroup bG = new ButtonGroup();
		bG.add(standaloneRadioButton);
		bG.add(hubRadioButton);
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
		tempPanel.setOpaque(false);
		topPanel.add(tempPanel);
		tempPanel.add(standaloneRadioButton);
		tempPanel.add(hubRadioButton);
		
		JPanel centerPanel=new JPanel();
		this.add(centerPanel,BorderLayout.CENTER);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5,10, 5, 5));
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
				
		centerPanel.add(cardComponent);
		
		final JPanel standalonePanel = new JPanel(cardLayout);
		standalonePanel.setLayout(new BoxLayout(standalonePanel, BoxLayout.Y_AXIS));
		standalonePanel.setOpaque(true);
		standalonePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		standalonePanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		final JPanel serverPanel = new JPanel(cardLayout);
		serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));
		serverPanel.setOpaque(true);
		serverPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		serverPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		cardComponent.add(standalonePanel, "STANDALONE");
		cardComponent.add(serverPanel, "SERVER");
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		standalonePanel.add(tempPanel);
		label=new JLabel("Number of Workers (Firefox instances)");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_SMALL);
		maxWorkersTextField = new JTextField();
		InjectUtils.assignArialPlainFont(maxWorkersTextField,ExecutionPanel.FONT_SIZE_SMALL);
		InjectUtils.setSize(maxWorkersTextField,50,25);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		tempPanel.add(maxWorkersTextField);
		
		
		// SERVER OPTIONS
		tempPanel=InjectUtils.getYPanel(Component.LEFT_ALIGNMENT);
		//tempPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
		tempPanel.setOpaque(true);
		tempPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		serverPanel.add(tempPanel);
		JPanel tempPanelX=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanelX.setOpaque(true);
		tempPanelX.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		tempPanel.add(tempPanelX);
		label=new JLabel("Server ID:"){
			public JToolTip createToolTip() {
				JToolTip tip = super.createToolTip();
				return InjectUtils.getCustomToolTip(tip);
			}
		};
		label.setForeground(Color.decode("#2F3436") );
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_SMALL);
		String tooltip="Example: 192.168.10.1:4444";
		label.setToolTipText(tooltip);
		tempPanelX.add(label);
		
		tempPanelX.add(Box.createRigidArea(new Dimension(10, 10)));
		hubIdTextField = new JTextField()
		{
			public JToolTip createToolTip() {
				JToolTip tip = super.createToolTip();
				return InjectUtils.getCustomToolTip(tip);
			}
		};
		InjectUtils.assignArialPlainFont(hubIdTextField,ExecutionPanel.FONT_SIZE_SMALL);
		hubIdTextField.setToolTipText(tooltip);
		InjectUtils.setSize(hubIdTextField,200,25);
		tempPanelX.add(hubIdTextField);
		tempPanelX.add(Box.createRigidArea(new Dimension(10, 20)));		
		
		JButton refreshButton=new JButton("Refresh Nodes");
		tempPanelX.add(refreshButton);
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				refreshNodesButton();
			}
		});
		tempPanelX.add(Box.createRigidArea(new Dimension(10, 10)));
		
		JLabel link=InjectUtils.linkify(hubIdTextField,null,"Click to open RAPIDController","/observed.shtml",
				"click to view all the VNC sessions connected to the server.");			
		tempPanel.add(link);
		
		tempPanelX=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.add(tempPanelX);
		tempPanelX.setOpaque(true);
		tempPanelX.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		tempPanelX.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		//tempPanelX.add(Box.createRigidArea(new Dimension(300, 20)));
		
		JPanel tempPanelBusyNodesY=InjectUtils.getYPanel(Component.LEFT_ALIGNMENT);
		tempPanelBusyNodesY.setOpaque(true);
		tempPanelBusyNodesY.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		tempPanelX.add(tempPanelBusyNodesY);
		busyNodeslabel=new JLabel(TEXT_BUSY_NODES);
		busyNodeslabel.setForeground(Color.decode("#2F3436") );
		InjectUtils.assignArialPlainFont(busyNodeslabel,ExecutionPanel.FONT_SIZE_SMALL);
		
		JPanel tempPanelLabel1=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanelLabel1.setOpaque(false);
		tempPanelLabel1.add(busyNodeslabel);
		tempPanelBusyNodesY.add(tempPanelLabel1);
		
		tempPanelBusyNodesY.add(Box.createRigidArea(new Dimension(10, 5)));
		busyNodeslistModel = new DefaultListModel<String>();
		JList<String> busyNodeslist = new JList<String> (busyNodeslistModel);
		busyNodeslist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		busyNodeslist.setVisibleRowCount(-1);
		JScrollPane busyNodeslistScroller = new JScrollPane(busyNodeslist, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tempPanelBusyNodesY.add(busyNodeslistScroller);
		
		tempPanelX.add(Box.createRigidArea(new Dimension(5, 5)));
		
		JPanel tempPanelFreeNodesY=InjectUtils.getYPanel(Component.LEFT_ALIGNMENT);
		tempPanelFreeNodesY.setOpaque(true);
		tempPanelFreeNodesY.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		tempPanelX.add(tempPanelFreeNodesY);
		freeNodesLabel=new JLabel(TEXT_FREE_NODES);
		freeNodesLabel.setForeground(Color.decode("#2F3436") );
		InjectUtils.assignArialPlainFont(freeNodesLabel,ExecutionPanel.FONT_SIZE_SMALL);
		JPanel tempPanelLabel2=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanelLabel2.setOpaque(false);
		tempPanelLabel2.add(freeNodesLabel);
		tempPanelFreeNodesY.add(tempPanelLabel2);
		
		tempPanelFreeNodesY.add(Box.createRigidArea(new Dimension(10, 5)));
		freeNodeslistModel = new DefaultListModel<String>();
		JList<String> freeNodeslist = new JList<String> (freeNodeslistModel);
		freeNodeslist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		freeNodeslist.setVisibleRowCount(-1);
		JScrollPane freeNodeslistScroller = new JScrollPane(freeNodeslist, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tempPanelFreeNodesY.add(freeNodeslistScroller);		
		
		setValues();
	}	
	
	private void setValues() {
		Preferences pref = Preferences.userRoot();
		
		boolean isHighlight= pref.getBoolean(DEFAULT_HTMLPLAYBACK_IS_HIGHLIGHT,DEFAULT_VALUE_HTMLPLAYBACK_IS_HIGHLIGHT);
		highlightWebElementsCheckBox.setSelected(isHighlight);

		int browserTimeout = pref.getInt(DEFAULT_HTMLPLAYBACK_BROWSER_TIMEOUT,DEFAULT_VALUE_HTMLPLAYBACK_BROWSER_TIMEOUT);
		timeoutTextField.setText(""+browserTimeout);

		int loginPageRedirectPause = pref.getInt(DEFAULT_HTMLPLAYBACK_LOGIN_PAGE_REDIRECT_PAUSE,DEFAULT_VALUE_HTMLPLAYBACK_LOGIN_PAGE_REDIRECT_PAUSE);
		pauseTextField.setText(""+loginPageRedirectPause);
		
		int maxRecordsPerWorker = pref.getInt(DEFAULT_HTMLPLAYBACK_BATCH_SIZE_LEVEL,DEFAULT_VALUE_HTMLPLAYBACK_BATCH_SIZE_LEVEL);
		maxRecordsPerWorkerTextField.setText(""+maxRecordsPerWorker);

		boolean isFailBatchesOnError= pref.getBoolean(DEFAULT_FAIL_BATCHES_ON_ERROR,DEFAULT_VALUE_FAIL_BATCHES_ON_ERROR);
		failBatchesOnErrorCheckBox.setSelected(isFailBatchesOnError);

		int maxWorkers = pref.getInt(DEFAULT_HTMLPLAYBACK_WORKERS_COUNT,DEFAULT_VALUE_HTMLPLAYBACK_WORKERS_COUNT);
		maxWorkersTextField.setText(""+maxWorkers);

		String hubId =pref.get(DEFAULT_HTMLPLAYBACK_HUB_ID,DEFAULT_VALUE_HTMLPLAYBACK_HUB_ID);
		hubIdTextField.setText(hubId);

		boolean isServerMode =pref.getBoolean(DEFAULT_HTMLPLAYBACK_SERVER_MODE,DEFAULT_VALUE_HTMLPLAYBACK_SERVER_MODE);
		
		if (isServerMode) {
			cardLayout.show(cardComponent,"SERVER");
			hubRadioButton.setSelected(true);
		}
		else {
			cardLayout.show(cardComponent,"STANDALONE");
			standaloneRadioButton.setSelected(true);
		}
		
	}

	protected void refreshNodesButton() {
		try {
			if (getHubID().isEmpty()) {
				GUIUtils.popupErrorMessage("You must enter the server ID first.");
				return;
			}
			NodesInfo nodesInfo = HubRequestsManager.getNodesInfo(getHubID());
			updateNodesList(nodesInfo);
		} 
		catch (Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}		
	}
	
	public String getHubID() {
		String text=hubIdTextField.getText();
		return text;
	}

	public void updateNodesList(NodesInfo nodesInfo) {
		busyNodeslistModel.removeAllElements();
		busyNodeslabel.setText(TEXT_BUSY_NODES+nodesInfo.getBusyNodesList().size());
		for (String node:nodesInfo.getBusyNodesList()) {
			busyNodeslistModel.addElement(node);
		}
		freeNodeslistModel.removeAllElements();
		freeNodesLabel.setText(TEXT_FREE_NODES+nodesInfo.getFreeNodesList().size());
		for (String node:nodesInfo.getFreeNodesList()) {
			freeNodeslistModel.addElement(node);
		}
	}

	public boolean isServerMode() {
		return hubRadioButton.isSelected();
	}

	public int getMaxFirefoxWorkers() {
		String text=maxWorkersTextField.getText();
		int value=Integer.valueOf(text).intValue();
		return value;
	}

	public int getLoginRedirectPause() {
		String text=pauseTextField.getText();
		int value=Integer.valueOf(text).intValue();
		return value;
	}
	
	public int getFirefoxBatchSizeLevel() {
		String text=maxRecordsPerWorkerTextField.getText();
		int value=Integer.valueOf(text).intValue();
		return value;
	}

	public boolean isWebElementHighlight() {
		return highlightWebElementsCheckBox.isSelected();
	}

	public int getTimeoutInMins() {
		String text=timeoutTextField.getText();
		int value=Integer.valueOf(text).intValue();
		return value;
	}

	private void setComponentsEnabled(boolean isEnabled) {
		maxWorkersTextField.setEnabled(isEnabled);
		highlightWebElementsCheckBox.setEnabled(isEnabled);
		timeoutTextField.setEnabled(isEnabled);
		maxRecordsPerWorkerTextField.setEnabled(isEnabled);
		hubIdTextField.setEnabled(isEnabled);
		standaloneRadioButton.setEnabled(isEnabled);
		hubRadioButton.setEnabled(isEnabled);
		pauseTextField.setEnabled(isEnabled);
		failBatchesOnErrorCheckBox.setEnabled(isEnabled);
	}
	
	public void unlockUI() {
		setComponentsEnabled(true);
	}

	public void lockUI() {
		setComponentsEnabled(false);
	}
	
	public void saveOptionsToDisk()
	{
		Preferences pref = Preferences.userRoot();
		
		pref.putInt(DEFAULT_HTMLPLAYBACK_WORKERS_COUNT,getMaxFirefoxWorkers());
		pref.putBoolean(DEFAULT_HTMLPLAYBACK_IS_HIGHLIGHT,isWebElementHighlight());
		pref.putInt(DEFAULT_HTMLPLAYBACK_BROWSER_TIMEOUT,getTimeoutInMins());
		pref.putInt(DEFAULT_HTMLPLAYBACK_BATCH_SIZE_LEVEL,getFirefoxBatchSizeLevel());
		pref.putInt(DEFAULT_HTMLPLAYBACK_LOGIN_PAGE_REDIRECT_PAUSE,getLoginRedirectPause());
		pref.putBoolean(DEFAULT_HTMLPLAYBACK_SERVER_MODE,isServerMode());
		pref.put(DEFAULT_HTMLPLAYBACK_HUB_ID,getHubID());
		pref.putBoolean(DEFAULT_FAIL_BATCHES_ON_ERROR,isFailAllBatchesOnError());
	}
	
	public void resetOptions()
	{
		Preferences pref = Preferences.userRoot();
		
		pref.remove(DEFAULT_HTMLPLAYBACK_WORKERS_COUNT);
		pref.remove(DEFAULT_HTMLPLAYBACK_IS_HIGHLIGHT);
		pref.remove(DEFAULT_HTMLPLAYBACK_BROWSER_TIMEOUT);
		pref.remove(DEFAULT_HTMLPLAYBACK_BATCH_SIZE_LEVEL);
		pref.remove(DEFAULT_HTMLPLAYBACK_LOGIN_PAGE_REDIRECT_PAUSE);
		pref.remove(DEFAULT_HTMLPLAYBACK_SERVER_MODE);
		pref.remove(DEFAULT_HTMLPLAYBACK_HUB_ID);
		pref.remove(DEFAULT_FAIL_BATCHES_ON_ERROR);
		
		setValues();
	}
	
	public boolean isFailAllBatchesOnError() {
		return failBatchesOnErrorCheckBox.isSelected();
	}

	public OptionsTabPanel getOptionsTabPanel() {
		return optionsTabPanel;
	}
	
}
