package com.rapidesuite.snapshot.view.upgrade;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.rapidesuite.client.common.gui.SevenZipPackageFileFilter;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotDownloadController;
import com.rapidesuite.snapshot.controller.SnapshotViewerController;
import com.rapidesuite.snapshot.controller.upgrade.SnapshotCreationUpgradeController;
import com.rapidesuite.snapshot.controller.upgrade.SnapshotViewerUpgradeController;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeController;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeFusionReportController;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeMigrationReportController;
import com.rapidesuite.snapshot.model.FormInformation;
import com.rapidesuite.snapshot.model.GenericControllerCancellationWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.NavigatorNodePath;
import com.rapidesuite.snapshot.model.SnapshotImportSwingWorker;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.SnapshotCreationInformationWindow;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryDetailsGridPanel;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class TabUpgradeMainPanel extends JPanel {

	private JLabel upgradeNameLabel;
	private JLabel upgradeNameValueLabel;
	private JLabel upgradeIdLabel;
	private JLabel upgradeIdValueLabel;
	
	public static final String REPORT_DATA_NOT_MIGRATED="Data non migrated";
	public static final String REPORT_DEFAULT_VALUES="Default Values";
	public static final String PREFERENCE_FUSION_MAPPING_FOLDER="PREFERENCE_FUSION_MAPPING_FOLDER";
	
	private JLabel fusionMappingFolderLabel;
	private JLabel fusionMappingFolderPathLabel;
	private JButton fusionMappingFolderButton;
	private JFileChooser fusionMappingFolderFileChooser;
	
	private UpgradeFrame upgradeFrame;
	private FusionInventoryGridPanel fusionInventoryGridPanel;
	
	private JLabel fusionScenarioFileLabel;
	private JLabel fusionScenarioFilePathLabel;
	private JButton fusionScenarioFileButton;
	private JFileChooser fusionScenarioFileFileChooser;
	private Map<String, File> ebsInventoryNameToInventoryFileMap;
		
	private JLabel ebsExecutionTimeLabel;
	private JLabel ebsExecutionStatusLabel;
	private JLabel ebsExecutionFailedRowsCountLabel;
	private JLabel ebsExecutionFailedRowsCountLabelValue;
	
	private JButton ebsFilteringButton;
	private JButton ebsSnapshotButton;
	private JButton cancelButton;
	private JButton downloadButton;
	
	private JLabel fusionExecutionTimeLabel;
	private JLabel fusionExecutionStatusLabel;
	private JLabel fusionExecutionFailedRowsCountLabel;
	private JLabel fusionExecutionFailedRowsCountLabelValue;
	private JButton fusionUpgradeButton;

	private JCheckBox viewDataChangesOnlyCheckBox;
	private JCheckBox downloadDataChangesOnlyCheckBox;
	private JCheckBox convertDataChangesOnlyCheckBox;
	
	private JPanel northPanel;
	private JPanel centerPanel;
	private JPanel southPanel;
	
	private JPanel fusionPanel;
	private JPanel ebsPanel;
	private SnapshotCreationUpgradeController snapshotCreationUpgradeController;
	private UpgradeController upgradeController;
	private File convertedFolder;
	private JLabel convertedFolderLabel;
	private Map<String, File> fusionInventoryNameToMappingFileMap;
	protected boolean autoRefreshFilteringOn;
	private JButton reportButton;
	private Map<String, Set<File>> ebsInventoryNameToFusionMappingFileMap;
	private String upgradeScenarioExplodedFolderPath;
	private JSplitPane verticalSplitPane;
	private Map<String, List<NavigatorNodePath>> functionIdToFunctionIdNavigatorNodesMap;
	private boolean disableNavigationNameSelection;
	private UpgradeMainFilterFrame upgradeMainFilterFrame;
	private JComboBox<String> reportsComboBox;
	
	public static final String FUSION_EXEC_TIME="Conversion Execution time: ";
	public static final String FUSION_EXEC_STATUS="Conversion Status: ";
	public static final String FUSION_EXEC_FAILED="Conversion Failed rows: ";
	
	public static final String EBS_EXEC_TIME="Snapshot Execution time: ";
	public static final String EBS_EXEC_STATUS="Snapshot Status: ";
	public static final String EBS_EXEC_FAILED="Snapshot Failed rows: ";
	
	public static final String PREFERENCE_FUSION_SCENARIO_PACKAGE="PREFERENCE_FUSION_SCENARIO_PACKAGE";

	public TabUpgradeMainPanel(UpgradeFrame upgradeFrame) throws Exception {
		this.upgradeFrame=upgradeFrame;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#4b4f4e"));
		try{
			upgradeScenarioExplodedFolderPath=Config.getUpgradeScenarioExplodedFolderPath();
		}
		catch(Throwable e) {
			FileUtils.println(Config.UPGRADE_SCENARIO_EXPLODED_FOLDER+" property not set in engine.properties.");
		}
		
		try{
			Boolean disableNavigationNameSelectionBool=Config.getUpgradeDisableNavigationNameSelection();
			disableNavigationNameSelection=disableNavigationNameSelectionBool.booleanValue();
		}
		catch(Throwable e) {
			FileUtils.println(Config.UPGRADE_DISABLE_NAVIGATION_NAME_SELECTION+" property not set in engine.properties.");
		}
		
		createComponents();
		
		if (upgradeFrame.getTabSnapshotsPanel()!=null) {
			String connectionOracleRelease=upgradeFrame.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
			ebsInventoryNameToInventoryFileMap=upgradeFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToInventoryFileMap(connectionOracleRelease);
		}
	}
	
	public void createComponents(){
		northPanel=new JPanel();
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		add(northPanel,BorderLayout.NORTH);
		
		centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#4b4f4e"));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(centerPanel,BorderLayout.CENTER);
		
		southPanel=new JPanel();
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#4b4f4e"));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		add(southPanel,BorderLayout.SOUTH);

		upgradeMainFilterFrame=new UpgradeMainFilterFrame(this);
		
		JPanel topPanel=new JPanel();
		//headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		topPanel.setOpaque(true);
		topPanel.setBackground(Color.decode("#4b4f4e"));
		topPanel.setLayout(new BorderLayout());
		
		JPanel headerPanel=new JPanel();
		//headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		headerPanel.setOpaque(true);
		headerPanel.setBackground(Color.decode("#4b4f4e"));
		headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(headerPanel, BorderLayout.NORTH);
		
		JPanel leftPanel=new JPanel();
		leftPanel.setOpaque(true);
		leftPanel.setBorder(BorderFactory.createTitledBorder(""));
		leftPanel.setBackground(Color.decode("#4b4f4e"));
		leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		headerPanel.add(leftPanel);
		
		JPanel foldersSelectionPanel=new JPanel();
		foldersSelectionPanel.setOpaque(true);
		foldersSelectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		foldersSelectionPanel.setBackground(Color.decode("#4b4f4e"));
		foldersSelectionPanel.setLayout(new BoxLayout(foldersSelectionPanel, BoxLayout.Y_AXIS));
		leftPanel.add(foldersSelectionPanel);
				
		JPanel infoPanel=new JPanel();
		infoPanel.setOpaque(true);
		infoPanel.setBackground(Color.decode("#4b4f4e"));
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		foldersSelectionPanel.add(infoPanel);
		
		JPanel upgNamePanel=new JPanel();
		infoPanel.add(upgNamePanel);
		upgNamePanel.setOpaque(true);
		upgNamePanel.setBackground(Color.decode("#4b4f4e"));
		upgNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		upgradeNameLabel=new JLabel("Upgrade name:");
		InjectUtils.setSize(upgradeNameLabel, 90,15);
		InjectUtils.assignArialPlainFont(upgradeNameLabel,InjectMain.FONT_SIZE_NORMAL);
		upgradeNameLabel.setForeground(Color.white);
		upgNamePanel.add(upgradeNameLabel);
		upgNamePanel.add(Box.createRigidArea(new Dimension(3, 1)));
		upgradeNameValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(upgradeNameValueLabel,InjectMain.FONT_SIZE_NORMAL);
		upgradeNameValueLabel.setForeground(Color.white);
		upgNamePanel.add(upgradeNameValueLabel);
		upgNamePanel.add(Box.createRigidArea(new Dimension(50, 11)));
		
		JPanel upgIdPanel=new JPanel();
		infoPanel.add(upgIdPanel);
		upgIdPanel.setOpaque(true);
		upgIdPanel.setBackground(Color.decode("#4b4f4e"));
		upgIdPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		upgradeIdLabel=new JLabel("Upgrade ID:");
		InjectUtils.setSize(upgradeIdLabel, 90,15);
		InjectUtils.assignArialPlainFont(upgradeIdLabel,InjectMain.FONT_SIZE_NORMAL);
		upgradeIdLabel.setForeground(Color.white);
		upgIdPanel.add(upgradeIdLabel);
		upgIdPanel.add(Box.createRigidArea(new Dimension(3, 1)));
		upgradeIdValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(upgradeIdValueLabel,InjectMain.FONT_SIZE_NORMAL);
		upgradeIdValueLabel.setForeground(Color.white);
		upgIdPanel.add(upgradeIdValueLabel);
		if (upgradeFrame.getSnapshotGridRecord()!=null) {
			upgradeNameValueLabel.setText(upgradeFrame.getSnapshotGridRecord().getName());
			upgradeIdValueLabel.setText(""+upgradeFrame.getSnapshotGridRecord().getSnapshotId());
		}
		
		JPanel pkgScnPanel=new JPanel();
		pkgScnPanel.setOpaque(true);
		pkgScnPanel.setBackground(Color.decode("#4b4f4e"));
		pkgScnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		foldersSelectionPanel.add(pkgScnPanel);
		
		fusionScenarioFileLabel=new JLabel("Fusion Scenario package:");
		fusionScenarioFileLabel.setOpaque(false);
		InjectUtils.setSize(fusionScenarioFileLabel, 150,15);
		InjectUtils.assignArialPlainFont(fusionScenarioFileLabel,InjectMain.FONT_SIZE_NORMAL);
		fusionScenarioFileLabel.setForeground(Color.white);
		pkgScnPanel.add(fusionScenarioFileLabel);
						
		fusionScenarioFilePathLabel=new JLabel("");
		fusionScenarioFilePathLabel.setOpaque(false);
		InjectUtils.setSize(fusionScenarioFilePathLabel, 120,15);
		InjectUtils.assignArialPlainFont(fusionScenarioFilePathLabel,InjectMain.FONT_SIZE_NORMAL);
		fusionScenarioFilePathLabel.setForeground(Color.white);
		pkgScnPanel.add(fusionScenarioFilePathLabel);
		
		ImageIcon ii=null;
		URL iconURL =null;
		Preferences pref = Preferences.userRoot();

		iconURL = this.getClass().getResource("/images/snapshot/button_browse.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		fusionScenarioFileButton = new JButton();
		pkgScnPanel.add(fusionScenarioFileButton);
		fusionScenarioFileButton.setIcon(ii);
		fusionScenarioFileButton.setBorderPainted(false);
		fusionScenarioFileButton.setContentAreaFilled(false);
		fusionScenarioFileButton.setFocusPainted(false);
		fusionScenarioFileButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_browse_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		fusionScenarioFileButton.setRolloverIcon(new RolloverIcon(ii));
		fusionScenarioFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionFusionScenarioPackageSelection();
			}
		}
				);
		if (upgradeScenarioExplodedFolderPath!=null) {
			fusionScenarioFileButton.setEnabled(false);
			fusionScenarioFilePathLabel.setText("Exploded folder!");
			fusionScenarioFilePathLabel.setForeground(Color.RED);
			fusionScenarioFilePathLabel.setBackground(Color.white);
			fusionScenarioFilePathLabel.setOpaque(true);
		}
		fusionScenarioFileFileChooser= new JFileChooser();
		fusionScenarioFileFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fusionScenarioFileFileChooser.setFileFilter(new SevenZipPackageFileFilter());
		fusionScenarioFileFileChooser.setDialogTitle("Choose a File");
		
		JPanel mappingFolderPanel=new JPanel();
		mappingFolderPanel.setOpaque(false);
		mappingFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		foldersSelectionPanel.add(mappingFolderPanel);
		
		fusionMappingFolderLabel=new JLabel("Fusion Mapping folder:");
		InjectUtils.setSize(fusionMappingFolderLabel, 150,15);
		fusionMappingFolderLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(fusionMappingFolderLabel,InjectMain.FONT_SIZE_NORMAL);
		fusionMappingFolderLabel.setForeground(Color.white);
		mappingFolderPanel.add(fusionMappingFolderLabel);
		
		fusionMappingFolderPathLabel=new JLabel("");
		fusionMappingFolderPathLabel.setOpaque(false);
		InjectUtils.setSize(fusionMappingFolderPathLabel, 120,15);
		fusionMappingFolderPathLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(fusionMappingFolderPathLabel,InjectMain.FONT_SIZE_NORMAL);
		mappingFolderPanel.add(fusionMappingFolderPathLabel);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_browse.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		fusionMappingFolderButton = new JButton();
		fusionMappingFolderButton.setIcon(ii);
		fusionMappingFolderButton.setBorderPainted(false);
		fusionMappingFolderButton.setContentAreaFilled(false);
		fusionMappingFolderButton.setFocusPainted(false);
		fusionMappingFolderButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_browse_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		fusionMappingFolderButton.setRolloverIcon(new RolloverIcon(ii));
		mappingFolderPanel.add(fusionMappingFolderButton);
		fusionMappingFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionSelectFusionKBFolder();
			}
		}
				);
		fusionMappingFolderFileChooser= new JFileChooser();
		fusionMappingFolderFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fusionMappingFolderFileChooser.setAcceptAllFileFilterUsed(false);
		fusionMappingFolderFileChooser.setDialogTitle("Choose a Folder");
		String value = pref.get(PREFERENCE_FUSION_MAPPING_FOLDER,"");
		if (!value.isEmpty()) {
			File file=new File(value);
			if (file.exists()) {
				fusionMappingFolderFileChooser.setSelectedFile(file);
				fusionMappingFolderPathLabel.setText(getTruncatedFileName(file));
			}
		}

		if ( upgradeScenarioExplodedFolderPath!=null) {
			processActionFusionScenarioPackageSelectionThread();
		}
		else {
			value = pref.get(PREFERENCE_FUSION_SCENARIO_PACKAGE,"");
			if (!value.isEmpty()) {
				File file=new File(value);
				if (file.exists()) {
					fusionScenarioFileFileChooser.setSelectedFile(file);
					processActionFusionScenarioPackageSelectionThread();
				}
			}
		}
				
		
		/*
		JScrollPane scenarioSelectionScrollPane = new JScrollPane(tempPanel);
		scenarioSelectionScrollPane.setOpaque(false);
		scenarioSelectionScrollPane.setPreferredSize(new Dimension(300, 80));
		scenarioSelectionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scenarioSelectionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		*/
	
		/*
		JScrollPane ebsFiltersPanelScrollPane = new JScrollPane(ebsFiltersPanel);
		ebsFiltersPanelScrollPane.setOpaque(true);
		ebsFiltersPanelScrollPane.setPreferredSize(new Dimension(550, 150));
		ebsFiltersPanelScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ebsFiltersPanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);	
		tempPanel.add(ebsFiltersPanelScrollPane);
		*/
		
		headerPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_take_snapshot.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		ebsSnapshotButton = new JButton();
		ebsSnapshotButton.setIcon(ii);
		ebsSnapshotButton.setBorderPainted(false);
		ebsSnapshotButton.setContentAreaFilled(false);
		ebsSnapshotButton.setFocusPainted(false);
		ebsSnapshotButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_take_snapshot_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		ebsSnapshotButton.setRolloverIcon(new RolloverIcon(ii));		
		ebsSnapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionSnapshot();
			}
		}
				);
		headerPanel.add(ebsSnapshotButton);
		headerPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		if (upgradeFrame.getSnapshotGridRecord()!=null && 
				upgradeFrame.getSnapshotGridRecord().getMode().equals(SnapshotImportSwingWorker.IMPORT_MODE)) {
			ebsSnapshotButton.setEnabled(false);
		}
		
		iconURL = this.getClass().getResource("/images/snapshot/button_filters.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		ebsFilteringButton = new JButton();
		ebsFilteringButton.setIcon(ii);
		ebsFilteringButton.setBorderPainted(false);
		ebsFilteringButton.setContentAreaFilled(false);
		ebsFilteringButton.setFocusPainted(false);
		ebsFilteringButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_filters_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		ebsFilteringButton.setRolloverIcon(new RolloverIcon(ii));
		ebsFilteringButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionEBSFilters();
			}
		}
				);
		headerPanel.add(ebsFilteringButton);
		headerPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		
		
		iconURL = this.getClass().getResource("/images/snapshot/button_convert.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		fusionUpgradeButton = new JButton();
		fusionUpgradeButton.setIcon(ii);
		fusionUpgradeButton.setBorderPainted(false);
		fusionUpgradeButton.setContentAreaFilled(false);
		fusionUpgradeButton.setFocusPainted(false);
		fusionUpgradeButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_convert_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		fusionUpgradeButton.setRolloverIcon(new RolloverIcon(ii));
		fusionUpgradeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionConvert();
			}
		}
				);
		headerPanel.add(fusionUpgradeButton);
		headerPanel.add(Box.createRigidArea(new Dimension(15, 15)));	

		Set<String> activatedPlugins=ModelUtils.getActivatedPlugins();
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_UPGRADE_REPORTS)) {

			JLabel label=new JLabel("Report Name:");
			label.setOpaque(false);
			InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
			label.setForeground(Color.white);
			headerPanel.add(label);
			headerPanel.add(Box.createRigidArea(new Dimension(5, 15)));
			reportsComboBox=new JComboBox<String>();
			reportsComboBox.addItem(REPORT_DATA_NOT_MIGRATED);
			reportsComboBox.addItem(REPORT_DEFAULT_VALUES);
			headerPanel.add(reportsComboBox);
			headerPanel.add(Box.createRigidArea(new Dimension(5, 15)));

			iconURL = this.getClass().getResource("/images/snapshot/button_run_report.png");
			try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
			reportButton = new JButton();
			reportButton.setIcon(ii);
			reportButton.setBorderPainted(false);
			reportButton.setContentAreaFilled(false);
			reportButton.setFocusPainted(false);
			reportButton.setRolloverEnabled(true);
			iconURL = this.getClass().getResource("/images/snapshot/button_run_report_rollover.png");
			try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
			reportButton.setRolloverIcon(new RolloverIcon(ii));
			reportButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try{
						String selectedReportName=(String)reportsComboBox.getSelectedItem();
						if (selectedReportName.equals(REPORT_DATA_NOT_MIGRATED)) {
							processActionEBSDataNotMigrated();
						}
						else 
							if (selectedReportName.equals(REPORT_DEFAULT_VALUES)) {
								processActionDefaultFusionValuesGeneration();
							}
					}
					catch(Exception ex) {
						FileUtils.printStackTrace(ex);
						GUIUtils.popupErrorMessage(ex.getMessage());
					}
				}
			}
					);
			headerPanel.add(reportButton);
			headerPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		}
		
		iconURL = this.getClass().getResource("/images/snapshot/button_cancel_snapshot.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		cancelButton = new JButton();
		cancelButton.setIcon(ii);
		cancelButton.setBorderPainted(false);
		cancelButton.setContentAreaFilled(false);
		cancelButton.setFocusPainted(false);
		cancelButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_cancel_snapshot_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		cancelButton.setRolloverIcon(new RolloverIcon(ii));		
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionCancel();
			}
		}
				);
		headerPanel.add(cancelButton);
		headerPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_viewer_download.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadButton = new JButton();
		downloadButton.setIcon(ii);
		downloadButton.setBorderPainted(false);
		downloadButton.setContentAreaFilled(false);
		downloadButton.setFocusPainted(false);
		downloadButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_viewer_download_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadButton.setRolloverIcon(new RolloverIcon(ii));
		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionDownload();
			}
		}
				);
		headerPanel.add(downloadButton);
		headerPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		
		JPanel tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(tempPanel,BorderLayout.CENTER);
		JLabel label=new JLabel("Output Folder: ");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		convertedFolderLabel=new JLabel();
		InjectUtils.assignArialPlainFont(convertedFolderLabel,InjectMain.FONT_SIZE_NORMAL);
		convertedFolderLabel.setForeground(Color.white);
		convertedFolderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		convertedFolderLabel.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	            	processActionOpenConvertedFolder();
	            }
	        });
		tempPanel.add(convertedFolderLabel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(true);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
		tempPanel.setBackground(Color.decode("#dbdcdf"));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		topPanel.add(tempPanel,BorderLayout.SOUTH);
		viewDataChangesOnlyCheckBox=new JCheckBox("View Data Option: Changes only");
		viewDataChangesOnlyCheckBox.setOpaque(false);
		viewDataChangesOnlyCheckBox.setSelected(true);
		viewDataChangesOnlyCheckBox.setFocusPainted(false);
		tempPanel.add(viewDataChangesOnlyCheckBox);
		downloadDataChangesOnlyCheckBox=new JCheckBox("Download Data Option: Changes only");
		downloadDataChangesOnlyCheckBox.setOpaque(false);
		downloadDataChangesOnlyCheckBox.setSelected(true);
		downloadDataChangesOnlyCheckBox.setFocusPainted(false);
		tempPanel.add(downloadDataChangesOnlyCheckBox);
		convertDataChangesOnlyCheckBox=new JCheckBox("Convert Data Option: Changes only");
		convertDataChangesOnlyCheckBox.setOpaque(false);
		convertDataChangesOnlyCheckBox.setSelected(true);
		convertDataChangesOnlyCheckBox.setFocusPainted(false);
		tempPanel.add(convertDataChangesOnlyCheckBox);
		
		createFusionComponents();
		createEBSComponents();
		
		fusionPanel.setPreferredSize(new Dimension(200,200));
		JPanel fusionOuterPanel = new JPanel();
		fusionOuterPanel.setLayout(new BorderLayout());
		fusionOuterPanel.add( fusionPanel,BorderLayout.CENTER);
		JScrollPane fusionPanelScrollPane = new JScrollPane(fusionOuterPanel);
		fusionPanelScrollPane.setOpaque(false);
		fusionPanelScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		fusionPanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		ebsPanel.setPreferredSize(new Dimension(200,200));
		JPanel ebsOuterPanel = new JPanel();
		ebsOuterPanel.setLayout(new BorderLayout());
		ebsOuterPanel.add( ebsPanel,BorderLayout.CENTER);
		JScrollPane ebsPanelScrollPane = new JScrollPane(ebsOuterPanel);
		ebsPanelScrollPane.setOpaque(false);
		ebsPanelScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		ebsPanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,ebsPanelScrollPane,fusionPanelScrollPane);
		splitPane.setOpaque(false);
		splitPane.setDividerSize(10);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
				
		JScrollPane listScrollPane = new JScrollPane(topPanel);
		listScrollPane.setOpaque(true);
		listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
		verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,listScrollPane,splitPane);
		verticalSplitPane.setOpaque(false);
		verticalSplitPane.setDividerSize(10);
		verticalSplitPane.setOneTouchExpandable(true);
		//mainSplitPane.setDividerLocation(0.9);
		verticalSplitPane.setResizeWeight(0d);
		centerPanel.add(verticalSplitPane);
	}
	
	protected void processActionDownload() {
		if (upgradeFrame.getSnapshotGridRecord()==null) {
			GUIUtils.popupErrorMessage("You must take a snapshot first!");
			return;
		}
		List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList=
				upgradeFrame.getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList();
		boolean isDownloadFromViewer=true;
		boolean isComparisonReportGeneration=false;
		boolean isBR100ReportGeneration=false;
		boolean isDownloadDataChangesOnly=downloadDataChangesOnlyCheckBox.isSelected();
		boolean isExport=false;
		
		SnapshotDownloadController snapshotDownloadController=new SnapshotDownloadController(
				upgradeFrame.getTabSnapshotsPanel(),snapshotInventoryGridRecordList,
				upgradeFrame.getSnapshotGridRecord(),isDownloadFromViewer,isComparisonReportGeneration,isBR100ReportGeneration,
				isDownloadDataChangesOnly,isExport);
		snapshotDownloadController.startExecution();
	}

	protected void processActionEBSFilters() {
		upgradeMainFilterFrame.setVisible(true);
	}

	protected void processActionDefaultFusionValuesGeneration() {
		List<FusionInventoryRow> fusionInventoryRowList=fusionInventoryGridPanel.getFusionInventoryRowList();
		if (fusionInventoryRowList.isEmpty()) {
			GUIUtils.popupErrorMessage("You must select a Fusion mapping folder!");
			return;
		}
		String workersCountStr=upgradeFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		int workersCount=Integer.valueOf(workersCountStr);
		File parentFolder=upgradeFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getDownloadFolder();
		UpgradeFusionReportController upgradeFusionReportController=new UpgradeFusionReportController(upgradeFrame,
				fusionInventoryRowList,workersCount,parentFolder);
		upgradeFusionReportController.startExecution();
	}

	protected void processActionEBSDataNotMigrated() throws Exception {
		if (upgradeFrame.getSnapshotGridRecord()==null) {
			throw new Exception("You must create a snapshot first");
		}
		List<SnapshotInventoryGridRecord> selectedRecordsList=upgradeFrame.getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList();
		if (selectedRecordsList.isEmpty()) {
			GUIUtils.popupErrorMessage("You must select at least one EBS Inventory!");
			return;
		}
		
		List<SnapshotInventoryGridRecord> recordsList=new ArrayList<SnapshotInventoryGridRecord>();
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedRecordsList) {
			if (ModelUtils.getUIStatusFromDBStatus(snapshotInventoryGridRecord.getStatus()).equalsIgnoreCase(UIConstants.UI_STATUS_COMPLETED)) {
				recordsList.add(snapshotInventoryGridRecord);
			}
		}		
		UpgradeMigrationReportController upgradeMigrationReportController=new UpgradeMigrationReportController(upgradeFrame.getTabSnapshotsPanel(),
				recordsList,upgradeFrame.getSnapshotGridRecord(),ebsInventoryNameToFusionMappingFileMap);
		upgradeMigrationReportController.startExecution();
	}

	public void createFusionComponents(){
		fusionPanel=new JPanel();
		fusionPanel.setOpaque(true);
		fusionPanel.setBackground(Color.decode("#4b4f4e"));
		fusionPanel.setLayout(new BoxLayout(fusionPanel, BoxLayout.Y_AXIS));
		fusionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel labelPanel=new JPanel();
		labelPanel.setBorder(BorderFactory.createEmptyBorder(0,0, 0, 0));
		labelPanel.setOpaque(false);
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		fusionPanel.add(labelPanel);
		JLabel titleLabel=new JLabel("FUSION Inventories");
		titleLabel.setForeground(Color.white);
		InjectUtils.assignArialBoldFont(titleLabel,18);
		labelPanel.add(titleLabel);
		
		fusionInventoryGridPanel=new FusionInventoryGridPanel(this);
		fusionPanel.add(fusionInventoryGridPanel);
		
		labelPanel=new JPanel();
		labelPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 0));
		labelPanel.setOpaque(false);
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		fusionPanel.add(labelPanel);
		
		fusionExecutionTimeLabel=new JLabel(FUSION_EXEC_TIME);
		fusionExecutionTimeLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(fusionExecutionTimeLabel,InjectMain.FONT_SIZE_NORMAL);
		fusionExecutionStatusLabel=new JLabel(FUSION_EXEC_STATUS);
		fusionExecutionStatusLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(fusionExecutionStatusLabel,InjectMain.FONT_SIZE_NORMAL);
		labelPanel.add(fusionExecutionTimeLabel);
		labelPanel.add(Box.createRigidArea(new Dimension(100, 15)));
		labelPanel.add(fusionExecutionStatusLabel);
		
		fusionExecutionFailedRowsCountLabel=new JLabel(FUSION_EXEC_FAILED);
		fusionExecutionFailedRowsCountLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(fusionExecutionFailedRowsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		fusionExecutionFailedRowsCountLabelValue=new JLabel();
		fusionExecutionFailedRowsCountLabelValue.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(fusionExecutionFailedRowsCountLabelValue,InjectMain.FONT_SIZE_NORMAL);
		labelPanel.add(Box.createRigidArea(new Dimension(100, 15)));
		fusionExecutionFailedRowsCountLabelValue.setOpaque(true);
		fusionExecutionFailedRowsCountLabelValue.setBackground(Color.decode("#dbdcdf"));
		
		labelPanel.add(fusionExecutionFailedRowsCountLabel);
		labelPanel.add(fusionExecutionFailedRowsCountLabelValue);
	}		
	
	protected void processActionOpenConvertedFolder() {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(convertedFolder);
			}
			else {
				SeleniumUtils.startLinuxFileBrowser(convertedFolder);
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}

	public void createEBSComponents(){
		ebsPanel=new JPanel();
		ebsPanel.setOpaque(true);
		ebsPanel.setBackground(Color.decode("#4b4f4e"));
		ebsPanel.setLayout(new BoxLayout(ebsPanel, BoxLayout.Y_AXIS));
		ebsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel labelPanel=new JPanel();
		labelPanel.setBorder(BorderFactory.createEmptyBorder(0,0, 0, 0));
		labelPanel.setOpaque(false);
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		ebsPanel.add(labelPanel);
		JLabel titleLabel=new JLabel("EBS Inventories");
		titleLabel.setForeground(Color.white);
		InjectUtils.assignArialBoldFont(titleLabel,18);
		labelPanel.add(titleLabel);
		
		boolean hasSelectionColumn=true;
		boolean hasViewChangesColumn=true;
		boolean hasFilteringResultColumn=true;
		boolean hasCleanupColumn=false;
		EBSInventoryGridPanel ebsInventoryGridPanel=new EBSInventoryGridPanel(this,upgradeFrame,hasSelectionColumn,
				hasViewChangesColumn,hasFilteringResultColumn,true,hasCleanupColumn,true,false,true,0);
		upgradeFrame.setSnapshotInventoryDetailsGridPanel(ebsInventoryGridPanel);
		
		ebsPanel.add(ebsInventoryGridPanel);
				
		labelPanel=new JPanel();
		labelPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 0));
		labelPanel.setOpaque(false);
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		ebsPanel.add(labelPanel);
		
		ebsExecutionTimeLabel=new JLabel(EBS_EXEC_TIME);
		ebsExecutionTimeLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(ebsExecutionTimeLabel,InjectMain.FONT_SIZE_NORMAL);
		ebsExecutionStatusLabel=new JLabel(EBS_EXEC_STATUS);
		ebsExecutionStatusLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(ebsExecutionStatusLabel,InjectMain.FONT_SIZE_NORMAL);
		labelPanel.add(ebsExecutionTimeLabel);
		labelPanel.add(Box.createRigidArea(new Dimension(100, 15)));
		labelPanel.add(ebsExecutionStatusLabel);
		
		ebsExecutionFailedRowsCountLabel=new JLabel(EBS_EXEC_FAILED);
		ebsExecutionFailedRowsCountLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(ebsExecutionFailedRowsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		ebsExecutionFailedRowsCountLabelValue=new JLabel();
		ebsExecutionFailedRowsCountLabelValue.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(ebsExecutionFailedRowsCountLabelValue,InjectMain.FONT_SIZE_NORMAL);
		labelPanel.add(Box.createRigidArea(new Dimension(100, 15)));
		ebsExecutionFailedRowsCountLabelValue.setOpaque(true);
		ebsExecutionFailedRowsCountLabelValue.setBackground(Color.decode("#dbdcdf"));
		
		labelPanel.add(ebsExecutionFailedRowsCountLabel);
		labelPanel.add(ebsExecutionFailedRowsCountLabelValue);
	}
	
	protected void processActionCancel() {
		int response = JOptionPane.showConfirmDialog(null, "Are you sure to cancel the current operation?", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			cancelButton.setEnabled(false);

			if (snapshotCreationUpgradeController!=null && !snapshotCreationUpgradeController.isExecutionCompleted()) {
				GenericControllerCancellationWorker swingWorker=new GenericControllerCancellationWorker(snapshotCreationUpgradeController);
				final int width=450;
				final int height=150;
				UIUtils.displayOperationInProgressModalWindow(upgradeFrame,width,height,"Cancel in progress...",
						swingWorker,SnapshotMain.getSharedApplicationIconPath());
			}
			else
				if (upgradeController!=null && !upgradeController.isExecutionCompleted()) {
					GenericControllerCancellationWorker swingWorker=new GenericControllerCancellationWorker(upgradeController);
					final int width=450;
					final int height=150;
					UIUtils.displayOperationInProgressModalWindow(upgradeFrame,width,height,"Cancel in progress...",
							swingWorker,SnapshotMain.getSharedApplicationIconPath());
				}
		}		
	}
	
	protected void autoRefreshFiltering() {
		autoRefreshFilteringOn=true;
		new Thread( new Runnable() {
			@Override
			public void run() {
				while (autoRefreshFilteringOn) {
					try {
						upgradeFrame.getSnapshotInventoryDetailsGridPanel().getFilteringTable().applyFiltering();
						fusionInventoryGridPanel.getFilteringTable().applyFiltering();
						Thread.sleep(1000);
					}
					catch (InterruptedException e) {
						FileUtils.printStackTrace(e);
						break;
					}
				}
			}
		}).start();		
	}

	public UpgradeFrame getUpgradeFrame() {
		return upgradeFrame;
	}

	public FusionInventoryGridPanel getFusionInventoryGridPanel() {
		return fusionInventoryGridPanel;
	}
	
	private void processActionFusionScenarioPackageSelectionThread() {
		Thread t = new Thread()
		{
		     public void run()
		     {
		    	try {
					Thread.sleep(1000);
					processActionFusionScenarioPackage();
				} 
		    	catch(Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}	    	
		     }
		};
		t.start();
	}

	private void processActionFusionScenarioPackageSelection() {
		try{	
			int returnVal = fusionScenarioFileFileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Preferences pref = Preferences.userRoot();
				pref.put(PREFERENCE_FUSION_SCENARIO_PACKAGE,fusionScenarioFileFileChooser.getSelectedFile().getAbsolutePath());

				processActionFusionScenarioPackage();
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}	

	protected void processActionFusionScenarioPackage() throws Exception {
		File mappingFolder=getFusionMappingFolderFileChooser().getSelectedFile();
		if (mappingFolder==null) {
			throw new Exception("You must select a Mapping folder!");
		}
		if (upgradeScenarioExplodedFolderPath==null) {
			fusionScenarioFilePathLabel.setText(getTruncatedFileName(fusionScenarioFileFileChooser.getSelectedFile()));
		}
		FusionScenarioPackageWorker fusionScenarioPackageWorker=new FusionScenarioPackageWorker(upgradeFrame);
		final int width=450;
		final int height=150;
		UIUtils.displayOperationInProgressModalWindow(this,width,height,"Initialization...",fusionScenarioPackageWorker,
				SnapshotMain.getSharedApplicationIconPath());
		
		ebsInventoryNameToFusionMappingFileMap=fusionScenarioPackageWorker.getEbsInventoryNameToFusionMappingFileMap();
	}
	
	public JFileChooser getFusionScenarioFileFileChooser() {
		return fusionScenarioFileFileChooser;
	}

	public void selectEBSInventoriesRelatedToSelectedFusionInventories(boolean isSetSelectedFilteringOn) {
		if (ebsInventoryNameToInventoryFileMap==null) {
			return;
		}
		List<FusionInventoryRow> fusionInventoryRowList=fusionInventoryGridPanel.getSelectedFusionInventoryRowList();
		
		Set<String> ebsInventoryNameFullSet=new TreeSet<String>();
		for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowList) {
			Set<String> ebsInventoryNameSet=fusionInventoryRow.getFusionInventoryInformation().getEbsInventoryNameSet();
			if (ebsInventoryNameSet==null) {
				continue;
			}
			Iterator<String> iterator=ebsInventoryNameSet.iterator();

			while (iterator.hasNext()) {
				String ebsInventoryName=iterator.next();
				ebsInventoryNameFullSet.add(ebsInventoryName);
			}
		}
		upgradeFrame.getSnapshotInventoryDetailsGridPanel().setSelectionForInventories(ebsInventoryNameFullSet);
		if (isSetSelectedFilteringOn) {
			upgradeFrame.getSnapshotInventoryDetailsGridPanel().getFilteringTable().setColumnFilterValue(
					SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_SELECTION,"Selected");
			upgradeFrame.getSnapshotInventoryDetailsGridPanel().getFilteringTable().applyFiltering();
		}
	}
	
	public void displayEBSInventories() {
		if (upgradeFrame.getSnapshotGridRecord()==null) {
			if ( upgradeFrame.getTabSnapshotsPanel()==null) {
				return;
			}
			Iterator<String> iterator=ebsInventoryNameToInventoryFileMap.keySet().iterator();
			List<SnapshotInventoryGridRecord> ebsInventoryRowList=new ArrayList<SnapshotInventoryGridRecord>();
			int gridIndex=0;
			String connectionOracleRelease=upgradeFrame.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
			Map<String, FormInformation> inventoryNameToFormInformationMap=upgradeFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToFormInformation(connectionOracleRelease);
			while (iterator.hasNext()) {
				String ebsInventoryName=iterator.next();
				SnapshotInventoryGridRecord ebsInventoryRow=new SnapshotInventoryGridRecord(ebsInventoryName);
				ebsInventoryRow.setGridIndex(gridIndex++);
				FormInformation formInformation=inventoryNameToFormInformationMap.get(ebsInventoryRow.getInventoryName());
				if (formInformation!=null) {
					ebsInventoryRow.setFormInformation(formInformation);
				}	
				ebsInventoryRowList.add(ebsInventoryRow);
			}
			upgradeFrame.getSnapshotInventoryDetailsGridPanel().displayInventories(ebsInventoryRowList);
		}
		else {
			SnapshotViewerUpgradeController snapshotViewerUpgradeController=new SnapshotViewerUpgradeController(
					upgradeFrame,
					upgradeFrame.getTabSnapshotsPanel(),
					upgradeFrame.getSnapshotInventoryDetailsGridPanel(),
					upgradeFrame,
					upgradeFrame.getSnapshotGridRecord(),
					upgradeMainFilterFrame.getEbsFiltersPanel());
			snapshotViewerUpgradeController.startExecution();
		}	
	}

	public JLabel getEbsExecutionTimeLabel() {
		return ebsExecutionTimeLabel;
	}

	public JLabel getEbsExecutionStatusLabel() {
		return ebsExecutionStatusLabel;
	}

	public SnapshotInventoryDetailsGridPanel getSnapshotInventoryDetailsGridPanel() {
		return upgradeFrame.getSnapshotInventoryDetailsGridPanel();
	}

	public JLabel getSnapshotIdValueLabel() {
		return new JLabel();
	}

	public JLabel getEbsExecutionFailedRowsCountLabelValue() {
		return ebsExecutionFailedRowsCountLabelValue;
	}

	public void startFiltering() throws Exception {
		if (upgradeFrame.getSnapshotGridRecord()==null) {
			throw new Exception("You must create a snapshot first");
		}
		SnapshotViewerController snapshotViewerController=new SnapshotViewerController(upgradeFrame.getTabSnapshotsPanel(),
				upgradeFrame.getSnapshotInventoryDetailsGridPanel(),
				upgradeFrame,
				upgradeFrame.getSnapshotGridRecord(),
				upgradeMainFilterFrame.getEbsFiltersPanel());
		
		snapshotViewerController.startExecution();
	}
	
	public void lockUI() {
		ebsSnapshotButton.setEnabled(false);
		upgradeMainFilterFrame.getEbsFiltersPanel().setComponentsEnabled(false);
		fusionScenarioFileButton.setEnabled(false);
		if (reportButton!=null) {
			reportButton.setEnabled(false);
		}
		fusionUpgradeButton.setEnabled(false);
		
		cancelButton.setEnabled(true);
	}
	
	public void unlockUI() {
		if (upgradeFrame.getSnapshotGridRecord()!=null && 
				upgradeFrame.getSnapshotGridRecord().getMode().equals(SnapshotImportSwingWorker.IMPORT_MODE)) {
			ebsSnapshotButton.setEnabled(false);
		}
		else {
			ebsSnapshotButton.setEnabled(true);
		}
		upgradeMainFilterFrame.getEbsFiltersPanel().setComponentsEnabled(true);
		fusionScenarioFileButton.setEnabled(true);
		if (reportButton!=null) {
			reportButton.setEnabled(true);
		}
		fusionUpgradeButton.setEnabled(true);
		
		cancelButton.setEnabled(false);
	}

	public EBSFiltersPanel getEbsFiltersPanel() {
		return upgradeMainFilterFrame.getEbsFiltersPanel();
	}

	public JLabel getFusionExecutionTimeLabel() {
		return fusionExecutionTimeLabel;
	}

	public JLabel getFusionExecutionStatusLabel() {
		return fusionExecutionStatusLabel;
	}

	public JLabel getFusionExecutionFailedRowsCountLabelValue() {
		return fusionExecutionFailedRowsCountLabelValue;
	}

	public void setConvertedFolderLabel(File convertedFolder) {
		this.convertedFolder=convertedFolder;
		String msg="<html><U>"+convertedFolder.getAbsolutePath()+"</U>";		
		convertedFolderLabel.setText(msg);
	}

	public void setFusionInventoryNameToMappingFileMap(Map<String, File> fusionInventoryNameToMappingFileMap) {
		this.fusionInventoryNameToMappingFileMap=fusionInventoryNameToMappingFileMap;
	}

	public Map<String, File> getFusionInventoryNameToMappingFileMap() {
		return fusionInventoryNameToMappingFileMap;
	}

	public Map<String, File> getEbsInventoryNameToInventoryFileMap() {
		return ebsInventoryNameToInventoryFileMap;
	}
	
	protected void processActionConvert() {
		List<FusionInventoryRow> selectedFusionInventoryRowList=fusionInventoryGridPanel.getSelectedFusionInventoryRowList();
		if (selectedFusionInventoryRowList.isEmpty()) {
			GUIUtils.popupErrorMessage("You must select at least one Fusion inventory!");
			return;
		}
		if (!upgradeMainFilterFrame.getEbsFiltersPanel().getUpgradeFilterOperatingUnitPanel().isOperatingUnitFilterEnabled()) {
			int dialogButton = JOptionPane.YES_NO_OPTION;
			int dialogResult = JOptionPane.showConfirmDialog (upgradeFrame, "No Operating Units were selected. Do you wish to continue?","Warning",dialogButton);
			if(dialogResult == JOptionPane.NO_OPTION){
				return;
			}
		}
		lockUI();
		
		List<FusionInventoryRow> allFusionInventoryRowList=fusionInventoryGridPanel.getFusionInventoryRowList();
		for (FusionInventoryRow fusionInventoryRow:allFusionInventoryRowList) {
			fusionInventoryRow.reset();
			fusionInventoryRow.setStatus("");
		}
		for (FusionInventoryRow fusionInventoryRow:selectedFusionInventoryRowList) {
			fusionInventoryRow.reset();
			fusionInventoryRow.setStatus(UIConstants.UI_STATUS_PENDING);
		}
		fusionInventoryGridPanel.refreshGrid(allFusionInventoryRowList);
		
		int workersCount=Integer.valueOf(upgradeFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getWorkersTextField().getText());
		upgradeController=new UpgradeController(upgradeFrame,workersCount,upgradeFrame.getTabUpgradeMainPanel().
				getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList(), convertDataChangesOnlyCheckBox.isSelected());
		autoRefreshFiltering();
		upgradeController.startExecution();
	}
	
	protected void processActionSnapshot() {
		List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList=
				upgradeFrame.getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList();
		if (snapshotInventoryGridRecordList.isEmpty()) {
			GUIUtils.popupErrorMessage("You must select at least one EBS Inventory!");
			return;
		}
		int width=600;
		int height=300;
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		String defaultSnapshotName="Upgrade ("+strDate+")";
		
		SnapshotCreationInformationWindow snapshotCreationInformationWindow=new SnapshotCreationInformationWindow(upgradeFrame,defaultSnapshotName);
		JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindow(upgradeFrame.getTabSnapshotsPanel().
				getMainPanel().getSnapshotMain().getRootFrame(),"Snapshot Information",width,height,
				snapshotCreationInformationWindow,null,true,SnapshotMain.getSharedApplicationIconPath());
		snapshotCreationInformationWindow.setDialog(dialog);
		dialog.setVisible(true);
	}

	public void setAutoRefreshFilteringOn(boolean isEnabled) {
		autoRefreshFilteringOn=isEnabled;
	}

	public void processActionTakeSnapshot(String snapshotName, String snapshotDescription) {
		ebsSnapshotButton.setEnabled(false);
		upgradeMainFilterFrame.getEbsFiltersPanel().setComponentsEnabled(false);
		fusionUpgradeButton.setEnabled(false);
		cancelButton.setEnabled(true);
		
		List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList=
				upgradeFrame.getSnapshotInventoryDetailsGridPanel().getSnapshotInventoryGridRecordsList();
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
			snapshotInventoryGridRecord.reset();
		}
		List<SnapshotInventoryGridRecord> selectedSnapshotInventoryGridRecordsList=
				upgradeFrame.getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList();
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedSnapshotInventoryGridRecordsList) {
			snapshotInventoryGridRecord.reset();
			snapshotInventoryGridRecord.setStatus(UIConstants.UI_STATUS_PENDING);
		}
		upgradeFrame.getSnapshotInventoryDetailsGridPanel().refreshGrid(snapshotInventoryGridRecordList);
		snapshotCreationUpgradeController=new SnapshotCreationUpgradeController(upgradeFrame,snapshotName,snapshotDescription);
		snapshotCreationUpgradeController.startExecution();
	}
	
	public SnapshotGridRecord getSnapshotGridRecord() throws Exception{
		if (snapshotCreationUpgradeController!=null) {
			return snapshotCreationUpgradeController.getSnapshotGridRecord();
		}
		if (upgradeFrame.getSnapshotGridRecord()!=null) {
			return upgradeFrame.getSnapshotGridRecord();
		}
		throw new Exception("No available Snapshot.");
	}

	public UpgradeController getUpgradeController() {
		return upgradeController;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}
	
	public JButton getFusionScenarioFileButton() {
		return fusionScenarioFileButton;
	}

	public JLabel getUpgradeNameValueLabel() {
		return upgradeNameValueLabel;
	}

	public JLabel getUpgradeIdValueLabel() {
		return upgradeIdValueLabel;
	}
	
	public static String getTruncatedFileName(File file) {
		int maxLength=20;
		String name=file.getName();
		if (name.length() > maxLength) {
			name = name.substring(0, maxLength)+"...";
		}
		return name;
	}
	
	private void processActionSelectFusionKBFolder() {
		try{	
			int returnVal = fusionMappingFolderFileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Preferences pref = Preferences.userRoot();
				fusionMappingFolderPathLabel.setText(getTruncatedFileName(fusionMappingFolderFileChooser.getSelectedFile()));
				pref.put(PREFERENCE_FUSION_MAPPING_FOLDER,fusionMappingFolderFileChooser.getSelectedFile().getAbsolutePath());
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}
		
	public File getSelectedFusionMappingFolder() {
		return fusionMappingFolderFileChooser.getSelectedFile();
	}

	public JFileChooser getFusionMappingFolderFileChooser() {
		return fusionMappingFolderFileChooser;
	}

	public String getUpgradeScenarioExplodedFolderPath() {
		return upgradeScenarioExplodedFolderPath;
	}

	public JSplitPane getVerticalSplitPane() {
		return verticalSplitPane;
	}

	public void setFunctionIdToFunctionIdNavigatorNodesMap(
			Map<String, List<NavigatorNodePath>> functionIdToFunctionIdNavigatorNodesMap) {
		this.functionIdToFunctionIdNavigatorNodesMap=functionIdToFunctionIdNavigatorNodesMap;
	}

	public Map<String, List<NavigatorNodePath>> getFunctionIdToFunctionIdNavigatorNodesMap() {
		return functionIdToFunctionIdNavigatorNodesMap;
	}

	public boolean isDisableNavigationNameSelection() {
		return disableNavigationNameSelection;
	}

	public UpgradeMainFilterFrame getUpgradeMainFilterFrame() {
		return upgradeMainFilterFrame;
	}
	
	public boolean isDownloadDataChangesOnly() {
		return downloadDataChangesOnlyCheckBox.isSelected();
	}

	public boolean isViewDataChangesOnlyCheckBox() {
		return viewDataChangesOnlyCheckBox.isSelected();
	}

	public boolean isConvertDataChangesOnlyCheckBox() {
		return convertDataChangesOnlyCheckBox.isSelected();
	}

}
