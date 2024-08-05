package com.rapidesuite.snapshot.view.convert;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.sql.SQLRecoverableException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.convert.ConvertController;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotViewerDownloadWorker;
import com.rapidesuite.snapshot.model.convert.ConvertWorker;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class ConvertFrame extends JFrame{

	private ConvertMainPanel convertMainPanel;
	private JButton analyzeButton;
	private JButton startButton;
	private JButton cancelButton;
	private JButton saveGridToExcelButton;
	private JButton closeButton;
	
	private JLabel targetKBProjectFileLabel;
	private JLabel targetKBProjectFilePathLabel;
	private JButton targetKBProjectFileButton;
	private JFileChooser targetKBProjectFileFileChooser;
	
	private JLabel targetMappingFileLabel;
	private JLabel targetMappingFilePathLabel;
	private JButton targetMappingFileButton;
	private JFileChooser targetMappingFileFileChooser;
	
	private JLabel sourceDataFolderLabel;
	private JLabel sourceDataFolderPathLabel;
	private JButton sourceDataFolderButton;
	private JFileChooser sourceDataFolderFileChooser;
	
	private ConvertController convertController;
	
	private JProgressBar progressBar;
	private JLabel executionTimeLabel;
	private JLabel executionTasksLabel;
	private JLabel executionStatusLabel;
	private JLabel failedTasksLabel;
	private Map<String, File> targetInventoryNameToInventoryFileMap;
	private Map<String, File> sourceInventoryNameToMappingFileMap;
	private JLabel convertedFolderLabel;
	private File convertedFolder;
	private JLabel failedTargetInventoriesLabel;
	private Map<String, String> snapshotEnvironmentProperties;
	private int workersCount;
	private File rootDownloadFolder;
	private Map<String, File> allInventoriesFileMap;
	private File sourceInventoriesFolder;
	private SnapshotGridRecord snapshotGridRecord;
	private boolean isDownloadDataChangesOnly;
	private Map<Integer, String> oracleSeededUserIdToUserNameMap;
	private Map<String, File> allDataFileMap;
	private Map<String, File> snapshotSQLFileNametoFileMap;
	private File plsqlPackageFile;
	private boolean isConversionSnapshot;
	private	List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationList;
	private MappingAnalyzerWorker mappingAnalyzerWorker;
	
	public static final int FRAME_WIDTH=1680;
	public static final int FRAME_HEIGHT=760;
	public static final String PREFERENCE_TARGET_KB_PROJECT_FILE_LOCATION="PREFERENCE_TARGET_KB_PROJECT_FILE_LOCATION";
	public static final String PREFERENCE_TARGET_MAPPING_FILE_LOCATION="PREFERENCE_TARGET_MAPPING_FILE_LOCATION";
	public static final String PREFERENCE_SOURCE_DATA_FOLDER_FILE_LOCATION="PREFERENCE_SOURCE_DATA_FOLDER_FILE_LOCATION";
	public static final String LABEL_EXECUTION_STATUS="Execution Status: ";
	
	public ConvertFrame(Map<String, String> snapshotEnvironmentProperties,int workersCount,File rootDownloadFolder,
			File sourceInventoriesFolder,SnapshotGridRecord snapshotGridRecord,boolean isDownloadDataChangesOnly,Map<Integer, String> oracleSeededUserIdToUserNameMap,
			List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationList,Map<String, File> snapshotSQLFileNametoFileMap,
			File plsqlPackageFile,boolean isConversionSnapshot) {
		this.isConversionSnapshot=isConversionSnapshot;
		this.snapshotEnvironmentProperties=snapshotEnvironmentProperties;
		this.snapshotGridRecord=snapshotGridRecord;
		this.plsqlPackageFile=plsqlPackageFile;
		this.workersCount=workersCount;
		this.snapshotSQLFileNametoFileMap=snapshotSQLFileNametoFileMap;
		this.isDownloadDataChangesOnly=isDownloadDataChangesOnly;
		this.oracleSeededUserIdToUserNameMap=oracleSeededUserIdToUserNameMap;
		this.rootDownloadFolder=rootDownloadFolder;
		this.sourceInventoriesFolder=sourceInventoriesFolder;
		this.convertSourceGridRecordInformationList=convertSourceGridRecordInformationList;
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		setTitle(UIConstants.FRAME_TITLE_PREFIX+" - Snapshot Converter");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		createComponents();
		getConvertMainPanel().getConvertSourcePanel().displayInventories(convertSourceGridRecordInformationList);
	}
	
	public void closeWindow() {
		if (convertController!=null && !convertController.isExecutionCompleted()) {
			GUIUtils.popupInformationMessage("You must wait for the conversion to complete before closing this window.");
			return;
		}
		setVisible(false);
		dispose();
	}
	
	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		add(northPanel,BorderLayout.NORTH);

		JPanel tempPanel=new JPanel();
		northPanel.add(tempPanel);
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		
		JPanel targetKBProjectFilePanel=new JPanel();
		targetKBProjectFilePanel.setOpaque(false);
		targetKBProjectFilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(targetKBProjectFilePanel);
		
		targetKBProjectFileLabel=new JLabel("Target KB project folder:");
		targetKBProjectFileLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(targetKBProjectFileLabel,InjectMain.FONT_SIZE_NORMAL);
		targetKBProjectFileLabel.setForeground(Color.white);
		targetKBProjectFilePanel.add(targetKBProjectFileLabel);
		
		targetKBProjectFilePathLabel=new JLabel("");
		targetKBProjectFilePathLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(targetKBProjectFilePathLabel,InjectMain.FONT_SIZE_NORMAL);
		targetKBProjectFilePathLabel.setForeground(Color.white);
		targetKBProjectFilePanel.add(targetKBProjectFilePathLabel);
		
		ImageIcon ii=null;
		URL iconURL =null;
		Preferences pref = Preferences.userRoot();

		iconURL = this.getClass().getResource("/images/snapshot/button_select.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		targetKBProjectFileButton = new JButton();
		targetKBProjectFileButton.setIcon(ii);
		targetKBProjectFileButton.setBorderPainted(false);
		targetKBProjectFileButton.setContentAreaFilled(false);
		targetKBProjectFileButton.setFocusPainted(false);
		targetKBProjectFileButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_select_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		targetKBProjectFileButton.setRolloverIcon(new RolloverIcon(ii));
		targetKBProjectFilePanel.add(targetKBProjectFileButton);
		targetKBProjectFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionTargetKBProjectFileSelection();
			}
		}
				);
		targetKBProjectFileFileChooser= new JFileChooser();
		targetKBProjectFileFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		targetKBProjectFileFileChooser.setAcceptAllFileFilterUsed(false);
		targetKBProjectFileFileChooser.setDialogTitle("Choose a Folder");
		String value = pref.get(PREFERENCE_TARGET_KB_PROJECT_FILE_LOCATION,"");
		if (!value.isEmpty()) {
			File file=new File(value);
			if (file.exists()) {
				targetKBProjectFileFileChooser.setSelectedFile(file);
				targetKBProjectFilePathLabel.setText(file.getAbsolutePath());
			}
		}
		
		JPanel targetMappingFilePanel=new JPanel();
		targetMappingFilePanel.setOpaque(false);
		targetMappingFilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(targetMappingFilePanel);
		targetMappingFileLabel=new JLabel("Target Mapping folder:");
		targetMappingFileLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(targetMappingFileLabel,InjectMain.FONT_SIZE_NORMAL);
		targetMappingFileLabel.setForeground(Color.white);
		targetMappingFilePanel.add(targetMappingFileLabel);
		targetMappingFilePathLabel=new JLabel("");
		targetMappingFilePathLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(targetMappingFilePathLabel,InjectMain.FONT_SIZE_NORMAL);
		targetMappingFilePathLabel.setForeground(Color.white);
		targetMappingFilePanel.add(targetMappingFilePathLabel);
		iconURL = this.getClass().getResource("/images/snapshot/button_select.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		targetMappingFileButton = new JButton();
		targetMappingFileButton.setIcon(ii);
		targetMappingFileButton.setBorderPainted(false);
		targetMappingFileButton.setContentAreaFilled(false);
		targetMappingFileButton.setFocusPainted(false);
		targetMappingFileButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_select_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		targetMappingFileButton.setRolloverIcon(new RolloverIcon(ii));
		targetMappingFilePanel.add(targetMappingFileButton);
		targetMappingFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionTargetMappingFileSelection();
			}
		}
				);		
		targetMappingFileFileChooser= new JFileChooser();
		targetMappingFileFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		targetMappingFileFileChooser.setAcceptAllFileFilterUsed(false);
		targetMappingFileFileChooser.setDialogTitle("Choose a Folder");	
		value = pref.get(PREFERENCE_TARGET_MAPPING_FILE_LOCATION,"");
		if (!value.isEmpty()) {
			File file=new File(value);
			if (file.exists()) {
				targetMappingFileFileChooser.setSelectedFile(file);
				targetMappingFilePathLabel.setText(file.getAbsolutePath());
			}
		}
		
		if (snapshotGridRecord.getSnapshotId()==-1) {
			JPanel sourceDataFolderPanel=new JPanel();
			sourceDataFolderPanel.setOpaque(false);
			sourceDataFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			tempPanel.add(sourceDataFolderPanel);
			sourceDataFolderLabel=new JLabel("Source Data folder:");
			sourceDataFolderLabel.setOpaque(false);
			InjectUtils.assignArialPlainFont(sourceDataFolderLabel,InjectMain.FONT_SIZE_NORMAL);
			sourceDataFolderLabel.setForeground(Color.white);
			sourceDataFolderPanel.add(sourceDataFolderLabel);
			sourceDataFolderPathLabel=new JLabel("");
			sourceDataFolderPathLabel.setOpaque(false);
			InjectUtils.assignArialPlainFont(sourceDataFolderPathLabel,InjectMain.FONT_SIZE_NORMAL);
			sourceDataFolderPathLabel.setForeground(Color.white);
			sourceDataFolderPanel.add(sourceDataFolderPathLabel);
			iconURL = this.getClass().getResource("/images/snapshot/button_select.png");
			try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
			sourceDataFolderButton = new JButton();
			sourceDataFolderButton.setIcon(ii);
			sourceDataFolderButton.setBorderPainted(false);
			sourceDataFolderButton.setContentAreaFilled(false);
			sourceDataFolderButton.setFocusPainted(false);
			sourceDataFolderButton.setRolloverEnabled(true);
			iconURL = this.getClass().getResource("/images/snapshot/button_select_rollover.png");
			try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
			sourceDataFolderButton.setRolloverIcon(new RolloverIcon(ii));
			sourceDataFolderPanel.add(sourceDataFolderButton);
			sourceDataFolderButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					processActionSourceDataFolderSelection();
				}
			}
					);		
			sourceDataFolderFileChooser= new JFileChooser();
			sourceDataFolderFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			sourceDataFolderFileChooser.setAcceptAllFileFilterUsed(false);
			sourceDataFolderFileChooser.setDialogTitle("Choose a Folder");	
			value = pref.get(PREFERENCE_SOURCE_DATA_FOLDER_FILE_LOCATION,"");
			if (!value.isEmpty()) {
				File file=new File(value);
				if (file.exists()) {
					sourceDataFolderFileChooser.setSelectedFile(file);
					sourceDataFolderPathLabel.setText(file.getAbsolutePath());
				}
			}
		}
						
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.red);//Color.decode("#dbdcdf"));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(centerPanel,BorderLayout.CENTER);
		
		convertMainPanel=new ConvertMainPanel(this);
		centerPanel.add(convertMainPanel);
	
		JPanel southPanel=new JPanel();
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#4b4f4e"));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		add(southPanel,BorderLayout.SOUTH);	
						
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		southPanel.add(tempPanel);
		tempPanel.setOpaque(true);
		tempPanel.setBackground(Color.decode("#4b4f4e"));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel label=new JLabel("Progress: ");
		label.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(label);
		
		progressBar=new JProgressBar();
		Dimension prefSize = progressBar.getPreferredSize();
		prefSize.width = 300;
		prefSize.height = 20;
		progressBar.setPreferredSize(prefSize);
		tempPanel.add(progressBar);
		progressBar.setOpaque(true);
		progressBar.setBackground(Color.decode("#4b4f4e"));
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(true);
		progressBar.setUI( new BasicProgressBarUI() {
		      protected Color getSelectionBackground() { return Color.white; }
		      protected Color getSelectionForeground() { return Color.white; }
		    });
		tempPanel.add(Box.createRigidArea(new Dimension(50, 15)));
		
		executionStatusLabel=new JLabel(LABEL_EXECUTION_STATUS);
		executionStatusLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(executionStatusLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(executionStatusLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(50, 15)));
		
		executionTimeLabel=new JLabel("Execution Time: ");
		executionTimeLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(executionTimeLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(executionTimeLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(50, 15)));
		
		executionTasksLabel=new JLabel("Total Source Inventories: ");
		executionTasksLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(executionTasksLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(executionTasksLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(50, 15)));
		
		failedTasksLabel=new JLabel("Total Failed Source Inventories: ");
		failedTasksLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(failedTasksLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(failedTasksLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(50, 15)));
		
		failedTargetInventoriesLabel=new JLabel("Total Failed Target Inventories: ");
		failedTargetInventoriesLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(failedTargetInventoriesLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(failedTargetInventoriesLabel);
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		southPanel.add(tempPanel);
		label=new JLabel("Output Folder: ");
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
		southPanel.add(tempPanel);
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.add(Box.createGlue());
		
		
		iconURL = this.getClass().getResource("/images/snapshot/button_analyze.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		analyzeButton = new JButton();
		if (ii==null) {
			analyzeButton.setText("Analyze");
		}
		else {
			analyzeButton.setIcon(ii);
		}
		analyzeButton.setBorderPainted(false);
		analyzeButton.setContentAreaFilled(false);
		analyzeButton.setFocusPainted(false);
		analyzeButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_analyze_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		analyzeButton.setRolloverIcon(new RolloverIcon(ii));		
		analyzeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionAnalyze();
			}
		}
				);
		tempPanel.add(analyzeButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/inject/button_start.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startButton = new JButton();
		if (ii==null) {
			startButton.setText("start");
		}
		else {
			startButton.setIcon(ii);
		}
		startButton.setBorderPainted(false);
		startButton.setContentAreaFilled(false);
		startButton.setFocusPainted(false);
		startButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_start_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startButton.setRolloverIcon(new RolloverIcon(ii));		
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionConvert();
			}
		}
				);
		tempPanel.add(startButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		
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
		tempPanel.add(cancelButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		
		
		iconURL = this.getClass().getResource("/images/snapshot/button_save_grid_to_excel.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveGridToExcelButton = new JButton();
		saveGridToExcelButton.setIcon(ii);
		saveGridToExcelButton.setBorderPainted(false);
		saveGridToExcelButton.setContentAreaFilled(false);
		saveGridToExcelButton.setFocusPainted(false);
		saveGridToExcelButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_grid_to_excel_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveGridToExcelButton.setRolloverIcon(new RolloverIcon(ii));
		saveGridToExcelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				saveGridToExcel();
			}
		}
				);
		tempPanel.add(saveGridToExcelButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_close.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		closeButton = new JButton();
		closeButton.setIcon(ii);
		closeButton.setBorderPainted(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setFocusPainted(false);
		closeButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_close_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		closeButton.setRolloverIcon(new RolloverIcon(ii));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				closeWindow();
			}
		}
				);
		tempPanel.add(closeButton);
		tempPanel.add(Box.createGlue());
	}

	protected void processActionAnalyze() {
		File mappingFolder=targetMappingFileFileChooser.getSelectedFile();
		if (mappingFolder==null || !mappingFolder.exists()) {
			GUIUtils.popupErrorMessage("You must select a Target Mapping location");
			return;
		}
		Map<String, File> sourceInventoryNameToMappingFileMap=ModelUtils.getFileNameToFileMap(mappingFolder,false);
		setSourceInventoryNameToMappingFileMap(sourceInventoryNameToMappingFileMap);			
		
		MappingAnalyzerWorker mappingAnalyzerWorker=new MappingAnalyzerWorker(this,true);
		final int width=450;
		final int height=150;
		UIUtils.displayOperationInProgressModalWindow(this,width,height,"Analyzing...",
				mappingAnalyzerWorker,SnapshotMain.getSharedApplicationIconPath());
	}

	protected void processActionSourceDataFolderSelection() {
		try{
			int returnVal = sourceDataFolderFileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Preferences pref = Preferences.userRoot();
				sourceDataFolderPathLabel.setText(sourceDataFolderFileChooser.getSelectedFile().getAbsolutePath());
				pref.put(PREFERENCE_SOURCE_DATA_FOLDER_FILE_LOCATION,sourceDataFolderFileChooser.getSelectedFile().getAbsolutePath());
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
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
	
	public void setConvertedFolderLabel(File convertedFolder) {
		this.convertedFolder=convertedFolder;
		String msg="<html><U>"+convertedFolder.getAbsolutePath()+"</U>";		
		convertedFolderLabel.setText(msg);
	}

	protected void processActionTargetKBProjectFileSelection() {
		try{	
			int returnVal = targetKBProjectFileFileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Preferences pref = Preferences.userRoot();
				targetKBProjectFilePathLabel.setText(targetKBProjectFileFileChooser.getSelectedFile().getAbsolutePath());
				pref.put(PREFERENCE_TARGET_KB_PROJECT_FILE_LOCATION,targetKBProjectFileFileChooser.getSelectedFile().getAbsolutePath());
				targetInventoryNameToInventoryFileMap=null;
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}
	
	protected void processActionTargetMappingFileSelection() {
		try{	
			int returnVal = targetMappingFileFileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Preferences pref = Preferences.userRoot();
				targetMappingFilePathLabel.setText(targetMappingFileFileChooser.getSelectedFile().getAbsolutePath());
				pref.put(PREFERENCE_TARGET_MAPPING_FILE_LOCATION,targetMappingFileFileChooser.getSelectedFile().getAbsolutePath());
				sourceInventoryNameToMappingFileMap=null;
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

	protected void processActionCancel() {
		ConvertControllerModalWindow convertControllerModalWindow=new ConvertControllerModalWindow(convertController);
		ModelUtils.startModalWindowInThread(this,convertControllerModalWindow);
		convertController.stopExecution();
	}

	protected void processActionConvert() {
		if (convertController!=null) {
			return;
		}
		
		File kbProjectFile=targetKBProjectFileFileChooser.getSelectedFile();
		if (kbProjectFile==null || !kbProjectFile.exists()) {
			GUIUtils.popupErrorMessage("You must select a Target KB Project location");
			return;
		}
		File mappingFolder=targetMappingFileFileChooser.getSelectedFile();
		if (mappingFolder==null || !mappingFolder.exists()) {
			GUIUtils.popupErrorMessage("You must select a Target Mapping location");
			return;
		}
		if (snapshotGridRecord.getSnapshotId()==-1) {
			File sourceDataFolder=sourceDataFolderFileChooser.getSelectedFile();
			if (sourceDataFolder==null) {
				GUIUtils.popupErrorMessage("You must select a Source Data location");
				return;
			}
		}
		processTargetKBFile(kbProjectFile);
		processMappingFolder(mappingFolder);
		closeButton.setEnabled(false);
		startButton.setEnabled(false);
		cancelButton.setEnabled(true);
		targetKBProjectFileButton.setEnabled(false);
		targetMappingFileButton.setEnabled(false);
		if (sourceDataFolderButton!=null) {
			sourceDataFolderButton.setEnabled(false);
		}
		
		mappingAnalyzerWorker=new MappingAnalyzerWorker(this,false);
		final int width=450;
		final int height=150;
		UIUtils.displayOperationInProgressModalWindow(this,width,height,"Analyzing...",
				mappingAnalyzerWorker,SnapshotMain.getSharedApplicationIconPath());
		
		convertController=new ConvertController(this,workersCount);
		setTotalSteps(convertMainPanel.getConvertSourcePanel().getSelectedConvertSourceGridRecordInformationList().size());
		convertController.startExecution();
	}
	
	public void processTargetKBFile(File kbProjectFile) {
		TargetKBProjectWorker targetKBProjectWorker=new TargetKBProjectWorker(this,kbProjectFile);
		targetKBProjectWorker.processAction();
		//final int width=450;
		//final int height=150;
		//UIUtils.displayOperationInProgressModalWindow(this,width,height,"Initialization...",targetKBProjectWorker,SnapshotMain.getSharedApplicationIconPath());
		
		List<ConvertTargetGridRecordInformation> convertTargetGridRecordInformationList=new ArrayList<ConvertTargetGridRecordInformation>();
		Iterator<String> iterator=targetInventoryNameToInventoryFileMap.keySet().iterator();
		int gridIndex=0;
		while (iterator.hasNext()) {
			String targetInventoryName=iterator.next();
			File targetInventoryFile=targetInventoryNameToInventoryFileMap.get(targetInventoryName);
			ConvertTargetGridRecordInformation convertTargetGridRecordInformation=new ConvertTargetGridRecordInformation(targetInventoryName,targetInventoryFile);
			convertTargetGridRecordInformation.setGridIndex(gridIndex++);
			convertTargetGridRecordInformationList.add(convertTargetGridRecordInformation);
		}
		convertMainPanel.getConvertTargetPanel().displayInventories(convertTargetGridRecordInformationList);
	}
	
	public void processMappingFolder(File mappingFile) {
		MappingFileWorker mappingFileWorker=new MappingFileWorker(this,mappingFile);
		mappingFileWorker.processAction();
		//final int width=450;
		///final int height=150;
		//UIUtils.displayOperationInProgressModalWindow(this,width,height,"Initialization...",mappingFileWorker,SnapshotMain.getSharedApplicationIconPath());
	}

	protected void saveGridToExcel() {
		String EXCEL_FILE_EXTENSION=".xlsx";
		JFileChooser fileChooser = new JFileChooser();
		
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		Date now = new Date();
		String strDate = format.format(now);
		File file=new File("rapidsnapshot-conversion-grid-"+strDate+EXCEL_FILE_EXTENSION);
		
		fileChooser.setSelectedFile(file);
		fileChooser.setDialogTitle("Specify a file to save");   

		int userSelection = fileChooser.showSaveDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			int indexOf=fileToSave.getName().toLowerCase().indexOf(EXCEL_FILE_EXTENSION);
			File outputFile=fileToSave;
			if (indexOf==-1) {
				String newFileName=fileToSave.getAbsolutePath()+EXCEL_FILE_EXTENSION;
				outputFile=new File(newFileName);
			}
			try {
				List<ConvertSourceGridRecordInformation> filteredConvertSourceGridRecordInformationList=convertMainPanel.getConvertSourcePanel().
						getFilteredConvertSourceGridRecordInformationList();
				List<ConvertTargetGridRecordInformation> filteredConvertTargetGridRecordInformationList=convertMainPanel.getConvertTargetPanel().
						getConvertTargetGridRecordInformationList();
				ModelUtils.saveConversionGridsToExcel(outputFile,filteredConvertSourceGridRecordInformationList,filteredConvertTargetGridRecordInformationList);
				GUIUtils.popupInformationMessage("File saved!");
			}
			catch (Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage("Unable to save the grid to Excel: "+e.getMessage());
			}
		}
	}
	
	public void updateProgressBar(int currentStep) {
		progressBar.setValue(currentStep);
	}
	
	public void setTotalSteps(int totalStepsCounter) {
		progressBar.setMaximum(totalStepsCounter);
	}

	public ConvertMainPanel getConvertMainPanel() {
		return convertMainPanel;
	}

	public JLabel getExecutionTimeLabel() {
		return executionTimeLabel;
	}

	public JButton getCloseButton() {
		return closeButton;
	}
	
	public JButton getCancelButton() {
		return cancelButton;
	}

	public JLabel getExecutionTasksLabel() {
		return executionTasksLabel;
	}
	
	public JLabel getFailedTargetInventoriesLabel() {
		return failedTargetInventoriesLabel;
	}
	
	public JLabel getFailedTasksLabel() {
		return failedTasksLabel;
	}

	public void setExecutionTimeLabel(JLabel executionTimeLabel) {
		this.executionTimeLabel = executionTimeLabel;
	}

	public JLabel getExecutionStatusLabel() {
		return executionStatusLabel;
	}

	public void setTargetInventoryNameToInventoryFileMap(Map<String, File> targetInventoryNameToInventoryFileMap) {
		this.targetInventoryNameToInventoryFileMap=targetInventoryNameToInventoryFileMap;		
	}

	public void setSourceInventoryNameToMappingFileMap(Map<String, File> sourceInventoryNameToMappingFileMap) {
		this.sourceInventoryNameToMappingFileMap=sourceInventoryNameToMappingFileMap;
	}

	public Map<String, File> getSourceInventoryNameToMappingFileMap() {
		return sourceInventoryNameToMappingFileMap;
	}

	public Map<String, File> getTargetInventoryNameToInventoryFileMap() {
		return targetInventoryNameToInventoryFileMap;
	}

	public ConvertController getConvertController() {
		return convertController;
	}

	public Map<String, File> getSourceInventoryNameToInventoryFileMap() throws Exception {
		if (allInventoriesFileMap==null) {
			allInventoriesFileMap=ModelUtils.getFileNameToFileMap(sourceInventoriesFolder,true);
		}
		return allInventoriesFileMap;
	}
	
	public File getMappingFolder() {
		return targetMappingFileFileChooser.getSelectedFile();
	}

	public Map<String, String> getSnapshotEnvironmentProperties() {
		return snapshotEnvironmentProperties;
	}

	public File getRootDownloadFolder() {
		return rootDownloadFolder;
	}

	public File downloadSourceDataToXMLFile(ConvertWorker convertWorker, ConvertSourceGridRecordInformation convertSourceGridRecordInformation) throws Exception {
		File sourceDataXMLFile=null;
		if (snapshotGridRecord.getSnapshotId()==-1) {
			File folder=sourceDataFolderFileChooser.getSelectedFile();
			if (allDataFileMap==null) {
				allDataFileMap=ModelUtils.getFileNameToFileMap(folder,true);
			}
			sourceDataXMLFile=allDataFileMap.get(convertSourceGridRecordInformation.getInventory().getName());
			if (sourceDataXMLFile==null) {
				throw new Exception("No data file!");
			}
		}
		else {
			Inventory sourceInventory=convertSourceGridRecordInformation.getInventory();
			StringBuffer additionalWhereClause=convertSourceGridRecordInformation.getWhereClauseFilter();
			int tableId=convertSourceGridRecordInformation.getTableId();
			
			StringBuffer sqlQuery=SnapshotViewerDownloadWorker.getSqlQuery(additionalWhereClause,tableId,snapshotGridRecord,
					isDownloadDataChangesOnly,
					oracleSeededUserIdToUserNameMap,false);
			
			File sourceXMLDownloadFolder=convertController.getSourceXMLDownloadFolder();
					
			File sqlFile=snapshotSQLFileNametoFileMap.get(sourceInventory.getName());
			if (sqlFile==null) {
				throw new Exception("cannot find SQL file");
			}
 			String sqlFileContent=FileUtils.readContentsFromSQLFile(sqlFile);
			String dffStructureQuery=ModelUtils.getDFFStructureQuery(sqlFileContent);
			
			boolean isConvertDFF=dffStructureQuery!=null && isConversionSnapshot;
			String message = "";			
			for(int i = 1; i <= DatabaseUtils.CONNECTION_RETRY_COUNT; i++) {
				try {
					sourceDataXMLFile=ModelUtils.exportTableToXML(convertWorker.getJDBCConnectionNoRetry(),sourceXMLDownloadFolder,
							sourceInventory,snapshotGridRecord,sqlQuery.toString(),convertSourceGridRecordInformation,isConvertDFF,true);
				}
				catch (SQLRecoverableException sqlRecoverableException) {
					// show the sleeping remark.
					if(i < DatabaseUtils.CONNECTION_RETRY_COUNT) {
						message = "Connection lost. Retries: " + i + " / " + DatabaseUtils.CONNECTION_RETRY_COUNT+
								". Waiting for " + DatabaseUtils.CONNECTION_RETRY_WAIT_SECONDS + " seconds before retry.";
						convertSourceGridRecordInformation.setRemarks(message);
						try {
							Thread.sleep(DatabaseUtils.CONNECTION_RETRY_WAIT_SECONDS * 1000);
						} catch (InterruptedException e) {
							FileUtils.printStackTrace(e);
						}
					} else {
						// show the permanently failed remark
						message = "Connection lost. "+DatabaseUtils.CONNECTION_RETRY_COUNT+" retries exhausted!";
						convertSourceGridRecordInformation.setRemarks(message);
						
						throw sqlRecoverableException;
					}
					continue;
				}
				// no need to show the remark if it was completed in the first try.
				if(i > 1) {
					// show the completed after x tries remark
					message = "Operation completed after " + i + " retries.";
					convertSourceGridRecordInformation.setRemarks(message);
				}
				
				break;
			}
		}
		return sourceDataXMLFile;
	}
	
	public Map<String, File> getSnapshotSQLFileNametoFileMap() {
		return snapshotSQLFileNametoFileMap;
	}

	public File getPlsqlPackageFile() {
		return plsqlPackageFile;
	}
	
	public List<ConvertSourceGridRecordInformation> getConvertSourceGridRecordInformationList() {
		return convertSourceGridRecordInformationList;
	}

	public MappingAnalyzerWorker getMappingAnalyzerWorker() {
		return mappingAnalyzerWorker;
	}

	public int getSnapshotId() {
		return snapshotGridRecord.getSnapshotId();
	}
	
}
