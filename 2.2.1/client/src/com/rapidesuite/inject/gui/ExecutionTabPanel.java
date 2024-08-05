package com.rapidesuite.inject.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import com.erapidsuite.configurator.navigation0005.Navigation;
import com.erapidsuite.configurator.navigation0005.NavigationDocument;
import com.erapidsuite.configurator.navigation0005.EngineType.Enum;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.fusionData0000.InventoryMapType;
import com.rapidesuite.core.fusionScripts0000.ScriptType;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.RecordTracker;
import com.rapidesuite.inject.ScriptGridTracker;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class ExecutionTabPanel extends JPanel {

	private ExecutionPanel executionPanel;
	private JPanel executionNorthPanel;
	private JPanel executionCenterPanel;
	private JPanel executionSouthPanel;
	private ScriptsGrid scriptsGrid;
	private JButton backButton;
	private JButton startButton;
	private JButton stopButton;
	private JLabel elapsedExecutionTimeLabel;
	private JLabel elapsedExecutionTimeValueLabel;
	private JLabel scriptExecutionCountLabel;
	private JLabel scriptExecutionCountValueLabel;
	private JLabel remarksLabel;
	private JLabel warningLabel;
	private JProgressBar progressBar;
	private long startTime;	
	private JLabel selectedPackageLabel;
	private JLabel selectedIELabel;

	private Map<String, ScriptType> allScriptNameToScriptTypeMap;
	private Map<Integer, ScriptGridTracker> scriptIdToScriptGridTrackerMap;
	private Map<String, ScriptGridTracker> scriptNameToScriptGridTrackerMap;
	private Map<String,Inventory> allInventoryNameToInventoryMap;
	private Map<Integer, Map<String,List<String[]>>>  scriptIdToInventoryNameToDataRowsMap;
	private ScriptManager scriptManager;
	private JButton saveGridToExcelButton;
	private Map<String, Navigation> navigationNameToNavigationMap;

	public ExecutionTabPanel(ExecutionPanel executionPanel) throws Exception {
		this.executionPanel=executionPanel;
		this.setLayout(new BorderLayout());

		ImageIcon ii=null;
		URL iconURL =null;

		executionNorthPanel = new JPanel();
		executionNorthPanel.setLayout(new BoxLayout(executionNorthPanel, BoxLayout.X_AXIS));
		executionNorthPanel.setOpaque(true);
		executionNorthPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
		executionNorthPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		this.add(executionNorthPanel, BorderLayout.NORTH);

		executionCenterPanel = new JPanel();
		executionCenterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		executionCenterPanel.setOpaque(true);
		executionCenterPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		executionCenterPanel.setLayout(new BoxLayout(executionCenterPanel, BoxLayout.Y_AXIS));
		this.add(executionCenterPanel, BorderLayout.CENTER);

		executionSouthPanel = new JPanel();
		executionSouthPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		executionSouthPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		executionSouthPanel.setLayout(new BorderLayout());
		this.add(executionSouthPanel, BorderLayout.SOUTH);

		JPanel tempPanel= new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		executionNorthPanel.add(tempPanel);
	
		selectedPackageLabel=new JLabel("");
		selectedPackageLabel.setForeground(Color.decode("#cccccc"));
		InjectUtils.assignArialPlainFont(selectedPackageLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(selectedPackageLabel);
		
		selectedIELabel=new JLabel("");
		selectedIELabel.setForeground(Color.decode("#cccccc"));
		InjectUtils.assignArialPlainFont(selectedIELabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(selectedIELabel);
		
		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(true);
		tempPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		remarksLabel=new JLabel();
		remarksLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(remarksLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(remarksLabel);
		executionCenterPanel.add(tempPanel);
		scriptsGrid=new ScriptsGrid(this);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(true);
		tempPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		executionCenterPanel.add(tempPanel);
		tempPanel.add(scriptsGrid);

		JPanel actionsPanel=new JPanel();
		actionsPanel.setOpaque(true);
		actionsPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
		actionsPanel.setAlignmentX( Component.LEFT_ALIGNMENT);
		executionSouthPanel.add(actionsPanel);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		//tempPanel.setBackground(Color.RED);
		actionsPanel.add(tempPanel);
		elapsedExecutionTimeLabel=new JLabel("Elapsed execution time: ");
		elapsedExecutionTimeLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(elapsedExecutionTimeLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(elapsedExecutionTimeLabel);
		elapsedExecutionTimeValueLabel=new JLabel("");
		elapsedExecutionTimeValueLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(elapsedExecutionTimeValueLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(elapsedExecutionTimeValueLabel);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		//tempPanel.setBackground(Color.BLUE);
		actionsPanel.add(tempPanel);
		scriptExecutionCountLabel=new JLabel("Execution script count: ");
		scriptExecutionCountLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(scriptExecutionCountLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(scriptExecutionCountLabel);
		scriptExecutionCountValueLabel=new JLabel("");
		scriptExecutionCountValueLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(scriptExecutionCountValueLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(scriptExecutionCountValueLabel);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
		//tempPanel.setBackground(Color.yellow);
		actionsPanel.add(tempPanel);
		JLabel label=new JLabel("Progress: ");
		label.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(50, 10)));
		progressBar = new JProgressBar();

		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);		
		tempPanel.add(progressBar);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		actionsPanel.add(tempPanel);
		warningLabel=new JLabel("Important: Do not resize Firefox while the injection is running as it may result in random injection failures!");
		warningLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(warningLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(warningLabel);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BorderLayout());
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		tempPanel.setOpaque(false);
		tempPanel.setBackground(Color.green);
		actionsPanel.add(tempPanel);
		JPanel tempPanelButton=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanelButton.setOpaque(false);
		tempPanelButton.setBackground(Color.red);
		tempPanelButton.add(Box.createGlue());
		tempPanel.add(tempPanelButton,BorderLayout.CENTER);
		
		iconURL = this.getClass().getResource("/images/inject/button_back.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		backButton = new JButton();
		backButton.setIcon(ii);
		backButton.setBorderPainted(false);
		backButton.setContentAreaFilled(false);
		backButton.setFocusPainted(false);
		backButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_back_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		backButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanelButton.add(backButton);	
		tempPanelButton.add(Box.createRigidArea(new Dimension(15, 15)));
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				previousPanel();
			}
		});

		iconURL = this.getClass().getResource("/images/inject/button_start.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startButton = new JButton();
		startButton.setIcon(ii);
		startButton.setBorderPainted(false);
		startButton.setContentAreaFilled(false);
		startButton.setFocusPainted(false);
		startButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_start_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanelButton.add(startButton);	
		tempPanelButton.add(Box.createRigidArea(new Dimension(15, 15)));
		ActionListener onStartButtonIsClicked = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				startExecution();
			}
		};
		startButton.addActionListener(onStartButtonIsClicked);


		iconURL = this.getClass().getResource("/images/inject/button_stop1.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		stopButton = new JButton();
		stopButton.setIcon(ii);
		stopButton.setBorderPainted(false);
		stopButton.setContentAreaFilled(false);
		stopButton.setFocusPainted(false);
		stopButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_stop1_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		stopButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanelButton.add(stopButton);	
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				stopExecution();
			}
		}
				);
		tempPanelButton.add(Box.createRigidArea(new Dimension(15, 15)));

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
		tempPanelButton.add(saveGridToExcelButton);
		
		tempPanelButton.add(Box.createGlue());	
	}
	
	protected void saveGridToExcel() {
		String EXCEL_FILE_EXTENSION=".xlsx";
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File("rapidinject-grid"+EXCEL_FILE_EXTENSION));
		fileChooser.setDialogTitle("Specify a file to save");   

		int userSelection = fileChooser.showSaveDialog(executionPanel.getInjectMain().getFrame());
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			int indexOf=fileToSave.getName().toLowerCase().indexOf(EXCEL_FILE_EXTENSION);
			File outputFile=fileToSave;
			if (indexOf==-1) {
				String newFileName=fileToSave.getAbsolutePath()+EXCEL_FILE_EXTENSION;
				outputFile=new File(newFileName);
			}
			try {
				Iterator<Integer> iterator=scriptIdToScriptGridTrackerMap.keySet().iterator();
				List<ScriptGridTracker> scriptGridTrackerList=new ArrayList<ScriptGridTracker>();
				while ( iterator.hasNext()) {
					Integer key=iterator.next();
					ScriptGridTracker scriptGridTracker=scriptIdToScriptGridTrackerMap.get(key);
					scriptGridTrackerList.add(scriptGridTracker);
				}
				InjectUtils.saveScriptsGridToExcel(outputFile,scriptGridTrackerList);
			}
			catch (Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage("Unable to save the grid to Excel: "+e.getMessage());
			}
		}
	}

	public void updateProgressUI() throws Exception {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int completedScriptsCount=scriptManager.getCompletedScriptsCount();
				int totalScriptsCount=scriptManager.getTotalScriptsCount();
				progressBar.setValue(completedScriptsCount);				
				
				String text=completedScriptsCount+" / "+totalScriptsCount;
				
				scriptExecutionCountValueLabel.setText(text);
				String msg=Utils.getExecutionTime(startTime,System.currentTimeMillis());
				elapsedExecutionTimeValueLabel.setText(msg);
			}
		});
	}

	protected void previousPanel() {
		executionPanel.getInjectMain().moveToPanel(InjectMain.APPLICATION_INFO_PANEL);
	}

	public void startExecution()
	{
		try
		{
			if (executionPanel.getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isServerMode() && executionPanel.getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getHubID().isEmpty()) {
				GUIUtils.popupErrorMessage("You must enter the server ID in the Options panel under 'Server Mode' before starting the execution.");
				return;
			}

			if (executionPanel.getInjectMain().getApplicationInfoPanel().getInjectionPackage()==null){
				GUIUtils.popupErrorMessage("Please select an injection package!");
				return;
			}
			List<ScriptGridTracker> scriptGridTrackersToProcessList=scriptsGrid.getSelectedScriptTackers();
			if (scriptGridTrackersToProcessList.isEmpty()) {
				GUIUtils.popupErrorMessage("Please select at least one script!");
				return;
			}

			executionPanel.lockUI();
			progressBar.setValue(0);
			progressBar.setMaximum(scriptGridTrackersToProcessList.size());
			startTime=System.currentTimeMillis();
			
			scriptExecutionCountValueLabel.setText("0 / "+scriptGridTrackersToProcessList.size());
			cleanupLastExecution();
			resetExecutionScriptsGridTrackers(scriptGridTrackersToProcessList,false);
			scriptsGrid.setIsExecutionStarted(true);

			scriptManager=new ScriptManager(executionPanel.getInjectMain(),scriptGridTrackersToProcessList);
			scriptManager.start();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}

	public void resetExecutionScriptsGridTrackers(List<ScriptGridTracker> scriptGridTrackersToProcessList,boolean isRetry) throws Exception {
		for (ScriptGridTracker scriptGridTracker:scriptGridTrackersToProcessList) {
			resetExecutionScriptsGridTracker(scriptGridTracker,isRetry);
		}
		getScriptsGrid().updateScriptGridTrackers(scriptGridTrackersToProcessList);
	}
	
	public void resetExecutionScriptsGridTracker(ScriptGridTracker scriptGridTracker,boolean isRetry) throws Exception {
		scriptGridTracker.setStatus(ScriptsGrid.STATUS_PENDING);
		scriptGridTracker.setTotalFailedRecords(0);
		scriptGridTracker.setTotalRemainingRecords(0);
		scriptGridTracker.setTotalSuccessRecords(0);
		scriptGridTracker.setExecutionEndTime("");
		scriptGridTracker.setRemarks("");
		if (isRetry) {
			
		}
		else {
			scriptGridTracker.setCompleted(false);
			scriptGridTracker.setExecutionRetries(0);
			scriptGridTracker.setStartTime(0);
			scriptGridTracker.setExecutionTotalTime("");
			scriptGridTracker.setExecutionStartTime("");
		}
		Map<String, List<RecordTracker>> inventoryToRecordTrackerMap=scriptGridTracker.getInventoryToRecordTrackerMap(); 
		Iterator<String> iterator=inventoryToRecordTrackerMap.keySet().iterator();
		while ( iterator.hasNext()) {
			String key=iterator.next();
			List<RecordTracker> recordTrackersList=inventoryToRecordTrackerMap.get(key);
			for (RecordTracker recordTracker:recordTrackersList) {
				recordTracker.setRemarks("");
				recordTracker.setWorkerId(0);
				recordTracker.setBatchId(0);
				recordTracker.setVncDisplayName("");
				recordTracker.setFieldName("");
				
				boolean isRecordSelected=getScriptsGrid().isRecordSelected(
						recordTracker,scriptGridTracker.getScript().getUniqueID().intValue(),key);
				if (isRecordSelected) {
					recordTracker.setStatus(ScriptsGrid.STATUS_PENDING);
					recordTracker.setExecutionTime("");
				}
				else {
					String status=recordTracker.getStatus();
					if ( status==null || !status.equalsIgnoreCase(ScriptsGrid.STATUS_SUCCESS) ) {
						recordTracker.setStatus(ScriptsGrid.STATUS_PENDING);
						recordTracker.setExecutionTime("");
					}
				}
				
			}
		}
		ScriptManager.updateRecordsTotalsScriptGridTracker(scriptGridTracker);
		getScriptsGrid().updateTableGrids(scriptGridTracker,true);
	}

	public void setProgressBar(int scriptCounter, int totalScriptsCounter)
	{
		try
		{
			progressBar.setValue(scriptCounter);
			progressBar.setMaximum(totalScriptsCounter);

			Rectangle progressRect = progressBar.getBounds();
			progressRect.x = 0;
			progressRect.y = 0;
			progressBar.paintImmediately(progressRect);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public void loadScripts(File injectionPackage,List<ScriptType> scripts) throws Exception{
		allScriptNameToScriptTypeMap=new TreeMap<String,ScriptType>(String.CASE_INSENSITIVE_ORDER);
		scriptIdToScriptGridTrackerMap=new TreeMap<Integer,ScriptGridTracker>();
		scriptNameToScriptGridTrackerMap=new TreeMap<String,ScriptGridTracker>(String.CASE_INSENSITIVE_ORDER);
		allInventoryNameToInventoryMap=new HashMap<String,Inventory>();
		scriptIdToInventoryNameToDataRowsMap=new HashMap<Integer, Map<String,List<String[]>>>();
		int index=0;
		int emptyScriptsCounter=0;
		String remarksMsg="";
		int packageTotalRecordsCount=0;
		List<ScriptGridTracker> filteredScriptGridTrackerList=new ArrayList<ScriptGridTracker>();
		navigationNameToNavigationMap=new HashMap<String, Navigation>();
		
		boolean isShowEmptyScripts=executionPanel.getOptionsTabPanel().getOptionsGeneralPanel().isShowEmptyScripts();
		for (ScriptType scriptType:scripts) {
			allScriptNameToScriptTypeMap.put(scriptType.getName(), scriptType);
			
			int gridIndex=index;
			int totalRecords=InjectUtils.getTotalDataRows(executionPanel.getInjectMain(),injectionPackage,scriptType);
			packageTotalRecordsCount=packageTotalRecordsCount+totalRecords;
			
			File navigationFile=InjectUtils.getNavigationFile(executionPanel.getInjectMain(),executionPanel.getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptType);
			NavigationDocument fusionNavigationDocument=InjectUtils.getFusionNavigationDocument(navigationFile);
			Navigation navigation=fusionNavigationDocument.getNavigation();	
			navigationNameToNavigationMap.put(navigation.getName(),navigation);
			
			if (totalRecords==0) {
				emptyScriptsCounter++;
				if (!isShowEmptyScripts) {
					continue;
				}
			}
			index++;	
			
			String addBatchingNoAllowed="";
			String displayType="N/A";
			int scriptBatchSize=-1;
			boolean isBatchingAllowed=false;
			try{
				Enum engineType=navigation.getEngineType();
				displayType=engineType.toString();
				
				com.rapidesuite.core.fusionScripts0000.ScriptType.Type.Enum type=scriptType.getType();
				if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.HTML) {
					isBatchingAllowed=navigation.getFusionNavigation().getIsBatchingAllowed();
					BigInteger defaultBatchSize=navigation.getFusionNavigation().getDefaultBatchSize();
					if (defaultBatchSize!=null) {
						scriptBatchSize=defaultBatchSize.intValue();
					}
					displayType="HTML";
				}
				else 
					if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.API) {
						displayType="API";
					isBatchingAllowed=true;
				}
				else {
					isBatchingAllowed=navigation.getFusionWebServiceNavigation().getIsBatchingAllowed();
				}
				if (!isBatchingAllowed) {
					addBatchingNoAllowed=" (*)";
				}
			}
			catch(Throwable e){
				e.printStackTrace();
				FileUtils.printStackTrace(e);
			}
			String displayName=scriptType.getName()+addBatchingNoAllowed;
			
			ScriptGridTracker scriptGridTracker=new ScriptGridTracker(executionPanel.getInjectMain(),scriptType,gridIndex,totalRecords);
			filteredScriptGridTrackerList.add(scriptGridTracker);

			scriptGridTracker.setTotalRemainingRecords(totalRecords);
			scriptGridTracker.setNavigation(navigation);

			boolean isBatchingBasedOnRootInventoryUniqueRecords=scriptGridTracker.getNavigation().getFusionNavigation().getIsBatchingBasedOnRootInventoryUniqueRecords();
			if (isBatchingBasedOnRootInventoryUniqueRecords || !isBatchingAllowed) {
				scriptGridTracker.setBatchSize(-1);
			}
			else {
				scriptGridTracker.setBatchSize(scriptBatchSize);
			}
			scriptGridTracker.setDisplayName(displayName);
			scriptGridTracker.setDisplayType(displayType);
			scriptGridTracker.setBatchingAllowed(isBatchingAllowed);
			
			scriptIdToScriptGridTrackerMap.put(scriptType.getUniqueID().intValue(),scriptGridTracker);
			scriptNameToScriptGridTrackerMap.put(scriptType.getName(),scriptGridTracker);
			
			List<InventoryMapType> inventoryMapTypeList=InjectUtils.getInventoryMapTypeList(executionPanel.getInjectMain(),injectionPackage,scriptType);
			Map<String, List<RecordTracker>> inventoryToRecordTrackerMap=new HashMap<String, List<RecordTracker>>();
			scriptGridTracker.setInventoryToRecordTrackerMap(inventoryToRecordTrackerMap);
			
			for (InventoryMapType inventoryMapType:inventoryMapTypeList) {
				String inventoryName=inventoryMapType.getName();
				int recordCount=inventoryMapType.getRecordsCount().intValue();

				List<RecordTracker> recordTrackerList=new ArrayList<RecordTracker>();
				for (int recordIndex=0;recordIndex<recordCount;recordIndex++) {
					RecordTracker recordTracker=new RecordTracker(scriptGridTracker,recordIndex);
					recordTrackerList.add(recordTracker);
					inventoryToRecordTrackerMap.put(inventoryName, recordTrackerList);
				}
			}
		}
		if (emptyScriptsCounter>0) {
			remarksMsg="There are "+emptyScriptsCounter+" empty scripts";
			if (!isShowEmptyScripts) {
				remarksMsg=remarksMsg+" (which were removed)";	
			}
			remarksMsg=remarksMsg+".";	
		}
		remarksMsg=remarksMsg+" There are "+Utils.formatNumberWithComma(packageTotalRecordsCount)+" records in the selected package.";
		remarksLabel.setText("Remarks: (*) Batching not allowed. "+remarksMsg);
		scriptsGrid.loadScripts(injectionPackage,filteredScriptGridTrackerList);
	}

	public void displaySelectedPackagePathName(String text) {
		selectedPackageLabel.setText("Selected Injection Package: "+text);
	}	
	
	public void displaySelectedIEPathName(String text) {
		selectedIELabel.setText("Selected Environment File: "+text);
	}
	
	public ScriptsGrid getScriptsGrid() {
		return scriptsGrid;
	}

	public void stopExecution()
	{
		try{
			SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					scriptManager.stopExecution();
					Thread.sleep(1000);
					while (!scriptManager.isAllWorkersCompleted()) {
						Thread.sleep(1000);
					}
					return null;
				}
			};

			String msg="Stopping execution, please wait...";
			final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			final JDialog dialog = new JDialog();
			dialog.setTitle("Stopping execution");
			dialog.setModal(true);
			dialog.setContentPane(optionPane);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.pack();

			mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("state")) {
						if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
							dialog.dispose();
						}
					}
				}
			});
			mySwingWorker.execute();

			dialog.setLocationRelativeTo(executionPanel.getInjectMain().getFrame());
			dialog.setVisible(true);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}
	
	public void cleanupLastExecution()
	{
		if (scriptManager==null) {
			return;
		}
		CleanupWorkerDialog swingWorker=new CleanupWorkerDialog(scriptManager);
		final int width=450;
		final int height=150;
		UIUtils.displayOperationInProgressModalWindow(executionPanel.getInjectMain().getFrame(),
				width,height,"Cleaning up last execution...",swingWorker,true,InjectMain.getSharedApplicationIconPath());
	}

	public void close() {
		if (scriptManager!=null) {
			stopExecution();
			cleanupLastExecution();
		}
	}

	public ScriptManager getScriptManager() {
		return scriptManager;
	}	


	public void unlockUI()
	{
		startButton.setEnabled(true);
		backButton.setEnabled(true);
		stopButton.setEnabled(false);		
		scriptsGrid.setIsExecutionStarted(false);
		scriptsGrid.repaint();
	}

	public void lockUI()
	{
		startButton.setEnabled(false);
		backButton.setEnabled(false);
		stopButton.setEnabled(true);
	}

	public Inventory getInventory(ScriptGridTracker scriptGridTracker,String inventoryName) throws Exception {
		Inventory inventory=allInventoryNameToInventoryMap.get(inventoryName);
		if (inventory==null) {
			Map<String, Inventory> inventoryNameToInventoryMap=InjectUtils.getAllInventoryNameToInventoryMap(
					executionPanel.getInjectMain(),executionPanel.getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());
			allInventoryNameToInventoryMap.putAll(inventoryNameToInventoryMap);
			return inventoryNameToInventoryMap.get(inventoryName);
		}
		return inventory;
	}

	public Map<String, Inventory>  getInventoryMap(ScriptGridTracker scriptGridTracker) throws Exception {
		Map<String, Inventory> inventoryNameToInventoryMap=InjectUtils.getAllInventoryNameToInventoryMap(
				executionPanel.getInjectMain(),executionPanel.getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());
		allInventoryNameToInventoryMap.putAll(inventoryNameToInventoryMap);
		return inventoryNameToInventoryMap;
	}	

	public ScriptGridTracker getScriptGridTracker(int gridIndex) throws Exception {	
		Iterator<Integer> iterator=scriptIdToScriptGridTrackerMap.keySet().iterator();
		while ( iterator.hasNext()) {
			Integer key=iterator.next();
			ScriptGridTracker scriptGridTracker=scriptIdToScriptGridTrackerMap.get(key);
			if (scriptGridTracker.getGridIndex()==gridIndex) {
				return scriptGridTracker;
			}
		}
		throw new Exception("Cannot find the script in the main table.");
	}

	public List<String[]> getDataRows(ScriptGridTracker scriptGridTracker,String inventoryName) throws Exception {
		int scriptId=scriptGridTracker.getScript().getUniqueID().intValue();
		Map<String, List<String[]>> inventoryNameToDataRowsMap=scriptIdToInventoryNameToDataRowsMap.get(scriptId);
		if (inventoryNameToDataRowsMap==null) {
			inventoryNameToDataRowsMap=InjectUtils.getAllInventoryNameToDataRowsMap(
					executionPanel.getInjectMain(),executionPanel.getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());
			scriptIdToInventoryNameToDataRowsMap.put(scriptId, inventoryNameToDataRowsMap);
		}
		return inventoryNameToDataRowsMap.get(inventoryName);
	}

	public Map<Integer, ScriptGridTracker> getScriptIdToScriptGridTrackerMap() {
		return scriptIdToScriptGridTrackerMap;
	}
	
	public Map<String, ScriptGridTracker> getScriptNameToScriptGridTrackerMap() {
		return scriptNameToScriptGridTrackerMap;
	}

	public ExecutionPanel getExecutionPanel() {
		return executionPanel;
	}

	public Map<String, ScriptType> getAllScriptNameToScriptTypeMap() {
		return allScriptNameToScriptTypeMap;
	}

	public Map<String, Navigation> getNavigationNameToNavigationMap() {
		return navigationNameToNavigationMap;
	}

}
