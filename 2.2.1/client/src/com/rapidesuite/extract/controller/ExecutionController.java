package com.rapidesuite.extract.controller;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.oracle.xmlns.oxp.service.v2.CatalogService;
import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;
import com.rapidesuite.extract.model.ExtractInventoryRecord;
import com.rapidesuite.extract.model.HookGetCodeCombination;
import com.rapidesuite.extract.model.HookGetSystemProfileValue;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class ExecutionController {

	private ExtractMain extractMain;
	private List<ExtractInventoryRecord> selectedExtractInventoryGridRecordsList;
	private List<ExtractInventoryRecord> extractInventoryGridRecordsToRefreshList;
	private boolean isExecutionStopped;
	private final Object lock = new Object();
	private int workersCount;
	private int batchSize;
	private List<ExtractionTask> tasksList;
	private List<ExecutionWorker> workersList;
	private int currentTaskIndex;
	private boolean postExecutionCompleted;
	protected int CURRENT_STEP_COUNTER;
	protected int TOTAL_STEPS_COUNTER;
	private static String executionBIPublisherPath;
	private File tempExecutionFolder;
	private File executionDataFolder;
	private File outputDataFolder;
	private boolean isBIPublisherInitConnectionError;
	private String formattedStartDate;
	private Set<String> dataSetIdentifierSet;
	private Set<String> duplicateDataSetIdentifierSet;
	private Set<String> reportIdentifierSet;
	private int workerId;
	
	protected void initMaps() {
		try {
			ExtractUtils.selectionLevelIDAndNameMapping = ExtractUtils.getIDAndNamesMapping(extractMain.getApplicationInfoPanel().getDynamicNonSQLReportPath(), 
					extractMain.getApplicationInfoPanel().getReportWebServiceInfo(), 
					extractMain.getApplicationInfoPanel().getReportService());
		} catch (Exception ex) {
			FileUtils.printStackTrace(ex);
		}
	}
	
	public ExecutionController(ExtractMain extractMain,List<ExtractInventoryRecord> selectedExtractInventoryGridRecordsList) throws Exception {
		this.extractMain=extractMain;
		this.selectedExtractInventoryGridRecordsList=selectedExtractInventoryGridRecordsList;
		extractInventoryGridRecordsToRefreshList=selectedExtractInventoryGridRecordsList;
		String workersCountStr=extractMain.getExtractMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		workersCount=Integer.valueOf(workersCountStr);
		String batchSizeStr=extractMain.getExtractMainPanel().getTabOptionsPanel().getBatchSizeTextField().getText();
		batchSize=Integer.valueOf(batchSizeStr);
		tasksList=new ArrayList<ExtractionTask>();
		workersList=Collections.synchronizedList(new ArrayList<ExecutionWorker>());	
    	
		SimpleDateFormat format = new SimpleDateFormat(ExtractConstants.CATALOG_BI_PUBLISHER_DATE_FORMAT);
		Date now = new Date();
		formattedStartDate = format.format(now);
		
		executionBIPublisherPath=extractMain.getApplicationInfoPanel().getExecutionBIPublisherPath();
    	tempExecutionFolder = new File(Config.getTempFolder(),formattedStartDate);
    	tempExecutionFolder.mkdirs();
    	    	
		executionDataFolder=new File(getExtractMain().getExtractMainPanel().getTabOptionsPanel().getDownloadFolder(),"data-"+formattedStartDate);
		executionDataFolder.mkdirs();
		
		dataSetIdentifierSet=new TreeSet<String>();
		reportIdentifierSet=new TreeSet<String>();
		duplicateDataSetIdentifierSet=new TreeSet<String>();
	}	
	
	public ExtractionTask getNextTask()  {
		synchronized (lock) {
			if (currentTaskIndex >= tasksList.size() ) {
				return null;
			}
			ExtractionTask extractionTask=tasksList.get(currentTaskIndex);
			currentTaskIndex++;
			return extractionTask;
		}
	}

	public boolean isWorkerExecutionCompleted()  {
		if (workersList.isEmpty()) {
			return false;
		}
		for (ExecutionWorker executionWorker:workersList) {
			if (!executionWorker.isCompleted() && !executionWorker.isAborted() ) {
				return false;
			}
		}
		return true;
	}
	
	public int getTotalTasks() {
		return tasksList.size();
	}

	public int getTotalFailedTasks()  {
		int total=0;
		for (ExecutionWorker executionWorker:workersList) {
			total=total+executionWorker.getTotalFailedTasks();
		}
		return total;
	}
	
	public int getTotalCompletedTasks()  {
		int total=0;
		for (ExecutionWorker executionWorker:workersList) {
			total=total+executionWorker.getTotalCompletedTasks();
		}
		return total;
	}	

	public boolean isExecutionStopped() {
		return isExecutionStopped;
	}

	public void stopExecution() {
		isExecutionStopped=true;
		for (ExecutionWorker executionWorker:workersList) {
			executionWorker.abort();
		}
	}

	public void monitorExecution() {
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					/*
					 * We need to wait because the list of workers may not be populated yet which will cause the isExecutionCompleted
					 * to return true!
					 */
					while (workersList.size() < workersCount) {
						Thread.sleep(1000);
					}
					while (!isWorkerExecutionCompleted()) {
						updateWhileExecution();
						Thread.sleep(500);
					}
					updateWhileExecution();
					postExecutionInternal();
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		};
		t.start();
	}
	
	protected void updateWhileExecution() {
		try{
			refreshGrid();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void refreshGrid() throws Exception {	
		List<ExtractInventoryRecord> revisedListToRefresh=new ArrayList<ExtractInventoryRecord>();
		List<ExtractInventoryRecord> itemsToUpdateInThisCycleList=new ArrayList<ExtractInventoryRecord>();
		for (ExtractInventoryRecord extractInventoryRecord:extractInventoryGridRecordsToRefreshList) {
			String status=extractInventoryRecord.getStatus();
			if (status.equals(UIConstants.UI_STATUS_PENDING) ) {
				revisedListToRefresh.add(extractInventoryRecord);
				itemsToUpdateInThisCycleList.add(extractInventoryRecord);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_UNSELECTED)) {
				itemsToUpdateInThisCycleList.add(extractInventoryRecord);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_FAILED) || status.equals(UIConstants.UI_STATUS_COMPLETED) 
					|| status.equals(UIConstants.UI_STATUS_WARNING) || status.equals(UIConstants.UI_STATUS_CANCELLED)) {
				itemsToUpdateInThisCycleList.add(extractInventoryRecord);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_PROCESSING)) {
				revisedListToRefresh.add(extractInventoryRecord);
				itemsToUpdateInThisCycleList.add(extractInventoryRecord);
				
				Long currentTime=System.currentTimeMillis();
				String msg=Utils.getExecutionTime(extractInventoryRecord.getStartTime(),currentTime);
				extractInventoryRecord.setExecutionTime(msg);
				extractInventoryRecord.setRawTimeInSecs(UIUtils.getRawTimeInSecs(extractInventoryRecord.getStartTime()));				
			}
		}
		extractInventoryGridRecordsToRefreshList=revisedListToRefresh;
		extractMain.getExtractMainPanel().getTabExecutionPanel().getExtractInventoryGridResultPanel().refreshGrid(itemsToUpdateInThisCycleList);
		extractMain.getExtractMainPanel().getTabExecutionPanel().getExtractInventoryGridResultPanel().updateTotalLabels();
	}

	public void postExecutionInternal() {
		postExecution();
		postExecutionCompleted=true;
	}

	private void postExecution() {
		extractMain.getExtractMainPanel().getTabExecutionPanel().getExtractInventoryGridResultPanel().updateTotalLabels();
		zipDataFilesThread();
		extractMain.getExtractMainPanel().unlockUI();
	}

	public int getCurrentTaskIndex() {
		return currentTaskIndex;
	}
	
	public int getBatchSize() {
		return batchSize;
	}

	public List<ExecutionWorker> getWorkersList() {
		return workersList;
	}

	public boolean isExecutionCompleted() {
		return postExecutionCompleted;
	}	

	public void initConnectionsToBIPublisher()
	{
		try
		{
			SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					WebServiceInfo catalogWebServiceInfo=extractMain.getApplicationInfoPanel().getCatalogWebServiceInfo();
					try {
						CatalogService catalogService=extractMain.getApplicationInfoPanel().getCatalogService();

						boolean isExecutionFolderExist=ExtractUtils.objectExistBIWebService(catalogWebServiceInfo, catalogService,executionBIPublisherPath);
						if (!isExecutionFolderExist) {
							ExtractUtils.createCatalogFolderBIWebService(catalogWebServiceInfo,catalogService,getBIPublisherDataSetPath());
							ExtractUtils.createCatalogFolderBIWebService(catalogWebServiceInfo,catalogService,getBIPublisherReportPath());
						}
						else {
							dataSetIdentifierSet=ExtractUtils.getFolderContents(catalogWebServiceInfo,catalogService,getBIPublisherDataSetPath());
							reportIdentifierSet=ExtractUtils.getFolderContents(catalogWebServiceInfo,catalogService,getBIPublisherReportPath());
						}
						boolean isStaticUseOfFunctionValues=extractMain.getExtractMainPanel().getTabOptionsPanel().isStaticUseOfFunctionValues();
						if (!isStaticUseOfFunctionValues) {
							WebServiceInfo reportWebServiceInfo=extractMain.getApplicationInfoPanel().getReportWebServiceInfo();
							ReportService reportService=extractMain.getApplicationInfoPanel().getReportService();
							
							HookGetCodeCombination hookGetCodeCombination=extractMain.getApplicationInfoPanel().getHookGetCodeCombination();
							HookGetSystemProfileValue hookGetSystemProfileValue=extractMain.getApplicationInfoPanel().getHookGetSystemProfileValue();
							
							String dynamicSQLReportPath=extractMain.getApplicationInfoPanel().getSessionBIPublisherPath()+"/"+ExtractConstants.BI_PUBLISHER_DYNAMIC_SQL_DATASET_NAME+".xdo";
							hookGetCodeCombination.init(dynamicSQLReportPath, reportWebServiceInfo, reportService);
							hookGetSystemProfileValue.init();
						}						
					} 
					catch (Throwable e) {
						isBIPublisherInitConnectionError=true;
						for (ExtractionTask extractionTask:tasksList){
							extractionTask.getExtractInventoryRecord().setStatus(UIConstants.UI_STATUS_CANCELLED);
							extractionTask.getExtractInventoryRecord().setRemarks("Initialization failed!");
						}
						stopExecution();
						GUIUtils.popupErrorMessage(e.getMessage());
					}
					
					return null;
				}
			};

			String msg="Connecting to BI Publisher, please wait...";
			final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			final JDialog dialog = new JDialog();
			dialog.setTitle("Connecting...");
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

			dialog.setLocationRelativeTo(extractMain.getRootFrame());
			dialog.setVisible(true);		
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}
		
	public void startExecution() throws Exception {
		for (ExtractInventoryRecord extractInventoryRecord:selectedExtractInventoryGridRecordsList) {
			ExtractionTask extractionTask=new ExtractionTask(extractInventoryRecord);
			tasksList.add(extractionTask);
		}
		
		Thread thread = new Thread(){
			public void run(){
				for (int i=1;i<=workersCount;i++) {
					createWorkerThread();
				}
				preExecution();
				for (ExecutionWorker executionWorker:workersList) {
					if (isExecutionStopped()) {
						break;
					}
					launchWorkerThread(executionWorker);
				}
			}
		};
		thread.start();
		monitorExecution();
	}
	
	private void createWorkerThread() {
		final ExecutionWorker executionWorker=new ExecutionWorker(this,workerId++);
		workersList.add(executionWorker);
	}	
	
	private void launchWorkerThread( final ExecutionWorker executionWorker) {
		Thread thread = new Thread(){
			public void run(){
				executionWorker.startExecution();
			}
		};
		thread.start();	
	}	
	
	public Set<String> getDataSetIdentifierSet() {
		return dataSetIdentifierSet;
	}
	
	public Set<String> getReportIdentifierSet() {
		return reportIdentifierSet;
	}
	
	public String getBIPublisherReportPath() {
		return executionBIPublisherPath+"/"+ExtractConstants.BI_PUBLISHER_REPORT_FOLDER_PATH;
	}
	
	public String getBIPublisherDataSetPath() {
		return executionBIPublisherPath+"/"+ExtractConstants.BI_PUBLISHER_DATASET_FOLDER_PATH;
	}	
	
	protected void preExecution() {
		initConnectionsToBIPublisher();
		initMaps();
	}	
	
	public File getTempDatasetFolder(String dataSetIdentifier) {
		return getTempTypeFolder(dataSetIdentifier,"DS");
	}
	
	public File getTempReportFolder(String dataSetIdentifier) {
		return getTempTypeFolder(dataSetIdentifier,"RE");
	}
	
	public File getTempDataFolder(String dataSetIdentifier) {
		return getTempTypeFolder(dataSetIdentifier,"DA");
	}
	
	public File getExecutionDataFolder() {
		return executionDataFolder;
	}
	
	public File getTempTypeFolder(String dataSetIdentifier,String type) {
		File folder=new File(tempExecutionFolder,dataSetIdentifier);
		File typeFolder=new File(folder,type);
		if (!typeFolder.exists()) {
			typeFolder.mkdirs();
		}
		return typeFolder;
	}
		
	public void zipDataFilesThread()
	{
		try{
			SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					zipDataFiles();
					return null;
				}
			};

			String msg="Compressing Data files, please wait...";
			final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			final JDialog dialog = new JDialog();
			dialog.setTitle("Compressing...");
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

			dialog.setLocationRelativeTo(extractMain.getRootFrame());
			dialog.setVisible(true);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}

	protected void zipDataFiles() {
		File[] folders=null;
		if(extractMain.getExtractMainPanel().getTabOptionsPanel().isDownloadCompletePackage()) {
			folders= new File[]{ executionDataFolder };
			outputDataFolder = executionDataFolder.getParentFile();
		} else {
			folders=executionDataFolder.listFiles();
			outputDataFolder = executionDataFolder;
		}
		for (File file : folders) {
			File zippedFile=new File(file.getParent(), file.getName()+".zip");
			try {
				File[] subFiles=file.listFiles();
				if (subFiles==null || subFiles.length==0) {
					return;
				}
				FileUtils.zipFolder(file, zippedFile);
			} 
			catch (Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
			}
			finally {
				FileUtils.deleteDirectory(file);
			}
		}
		extractMain.getExtractMainPanel().getTabExecutionPanel().getExtractInventoryGridResultPanel().getLastExecutionDownloadFolderComponent().setText(
				"<HTML><a href=\"\">"+outputDataFolder.getAbsolutePath()+"</a></html>");
	}
	
	public void openDownloadFolder() {
        try {
        	if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(outputDataFolder);
			}
			else {
				SeleniumUtils.startLinuxFileBrowser(outputDataFolder);
			}
			
		} catch (IOException e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}

	public ExtractMain getExtractMain() {
		return extractMain;
	}

	public boolean isBIPublisherInitConnectionError() {
		return isBIPublisherInitConnectionError;
	}

	public String getFormattedStartDate() {
		return formattedStartDate;
	}
	
	public synchronized String generateDataSetIdentifier(String inventoryName) throws Exception {
		String dataSetIdentifier=""+inventoryName.hashCode();
		if (duplicateDataSetIdentifierSet.contains(dataSetIdentifier)) {
			throw new Exception("Internal error: DataSet Identifier already used: "+dataSetIdentifier);
		}
		duplicateDataSetIdentifierSet.add(dataSetIdentifier);
		return dataSetIdentifier;
	}
					
}
