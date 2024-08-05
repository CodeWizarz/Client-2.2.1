package com.rapidesuite.extract.controller;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.oracle.xmlns.oxp.service.v2.CatalogService;
import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.extract.DataSet;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.ExtractUtils.SELECTION_LEVEL;
import com.rapidesuite.extract.WebServiceInfo;
import com.rapidesuite.extract.model.DFFEngine;
import com.rapidesuite.extract.model.ExtractDataRow;
import com.rapidesuite.extract.model.ExtractFlexfieldTempRow;
import com.rapidesuite.extract.model.ExtractInventoryRecord;
import com.rapidesuite.extract.model.GDFEngine;
import com.rapidesuite.extract.model.HookGetCodeCombination;
import com.rapidesuite.extract.model.HookGetSystemProfileValue;
import com.rapidesuite.extract.model.KFFEngine;
import com.rapidesuite.extract.model.ParameterNameValue;
import com.rapidesuite.extract.view.ApplicationInfoPanel;
import com.rapidesuite.extract.view.UIConstants;
import com.rapidesuite.extract.model.MapGeneric;

public class ExecutionWorker {

	private ExecutionController executionController;
	private boolean isCompleted;
	private int totalFailedTasks;
	private int totalCompletedTasks;
	private CatalogService catalogService;
	private ReportService reportService;
	private boolean isAborted;
	private boolean switchOn;
	private Throwable executeReportThrowable;
	private List<ExtractDataRow> extractDataRowsFromBIPublisher;
	private int id;
	private ExtractionTask extractionTask;
	private boolean isReportExecutionViaNonSQL;
	
	/**
	 * same as extractDataRowsFromBIPublisher, but splited among Level and ID and Name
	 */
	private Map<SELECTION_LEVEL, Map<MapGeneric, List<ExtractDataRow>>> allLevelSelectionLevelToIDToDatarowMap;

	public ExecutionWorker(ExecutionController executionController,int id) {
		this.executionController=executionController;
		this.id=id;
		totalCompletedTasks=0;
	}

	public void startExecution() {
		try{
			isCompleted=false;
			while (true) {
				extractionTask=executionController.getNextTask();
				if (extractionTask==null) {
					break;
				}
				if (isAborted) {
					ExtractInventoryRecord extractInventoryRecord=extractionTask.getExtractInventoryRecord();
					extractInventoryRecord.setRemarks("Aborted!");
					extractInventoryRecord.setStatus(UIConstants.UI_STATUS_CANCELLED);
				}
				else {
					execute(extractionTask);
				}
				totalCompletedTasks++;
			}
		}
		finally{
			isCompleted=true;
		}
	}
	
	public void execute(ExtractionTask extractionTask) {
		ExtractInventoryRecord extractInventoryRecord=extractionTask.getExtractInventoryRecord();

		extractDataRowsFromBIPublisher=new ArrayList<ExtractDataRow>();
		
		try {
			WebServiceInfo catalogWebServiceInfo=executionController.getExtractMain().getApplicationInfoPanel().getCatalogWebServiceInfo();
			WebServiceInfo reportWebServiceInfo=executionController.getExtractMain().getApplicationInfoPanel().getReportWebServiceInfo();
			
			switchOn=true;
			extractInventoryRecord.setStartTime(System.currentTimeMillis());
			extractInventoryRecord.setStatus(UIConstants.UI_STATUS_PROCESSING);

			if (catalogService==null) {
				extractInventoryRecord.setRemarks("Connecting to BI Publisher");
				catalogService=ExtractUtils.getCatalogService(catalogWebServiceInfo);
			}
			if (reportService==null) {
				reportService=ExtractUtils.getReportService(reportWebServiceInfo);
			}
			
			ApplicationInfoPanel infoPanel = executionController.getExtractMain().getApplicationInfoPanel();
			String reportAbsolutePath = infoPanel.getSessionBIPublisherPath()+"/"+ExtractConstants.BI_PUBLISHER_DYNAMIC_SQL_DATASET_NAME+".xdo";
			File sqlFile = ExtractUtils.getExtractSQLFile(infoPanel.getReportWebServiceInfo(), reportService, reportAbsolutePath, 
					infoPanel.getInventoryNameToSQLFileMap(), extractInventoryRecord.getInventoryName());
			
			if (sqlFile==null) {
				extractInventoryRecord.setRemarks("Unsupported");
				extractInventoryRecord.setStatus(UIConstants.UI_STATUS_FAILED);
				return;
			}
			
			String dataSetIdentifier=executionController.generateDataSetIdentifier(extractInventoryRecord.getInventory().getName());
			DataSet dataSet=ExtractUtils.createDataSetObject(sqlFile,extractInventoryRecord,dataSetIdentifier);
			extractInventoryRecord.setDataSet(dataSet);
			
			//TODO: check the size of the SQL query with parameters (less than 32k)
			isReportExecutionViaNonSQL=true;
			
			processExtraction(extractInventoryRecord);
			if (isAborted) {
				extractInventoryRecord.setRemarks("Aborted!");
				extractInventoryRecord.setStatus(UIConstants.UI_STATUS_CANCELLED);
			}
			else {
				int totalRecords=extractDataRowsFromBIPublisher.size();
				extractInventoryRecord.setTotalRecords(totalRecords);
				if (totalRecords==0) {
					extractInventoryRecord.setRemarks("There is no record found!");
				}
				else {
					extractInventoryRecord.setRemarks("");
				}				
				extractInventoryRecord.setStatus(UIConstants.UI_STATUS_COMPLETED);
			}
		}
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
			if (isAborted) {
				extractInventoryRecord.setRemarks("Aborted!");
				extractInventoryRecord.setStatus(UIConstants.UI_STATUS_CANCELLED);
			}
			else {
				extractInventoryRecord.setRemarks(e.getMessage());
				extractInventoryRecord.setStatus(UIConstants.UI_STATUS_FAILED);
			}
		}
	}

	private void processExtraction(ExtractInventoryRecord extractInventoryRecord) throws Exception {
		try{
			if (!isAborted && !isReportExecutionViaNonSQL) {
				boolean isDataSetAlreadyExist=executionController.getDataSetIdentifierSet().contains(extractInventoryRecord.getDataSet().getIdentifier());
				FileUtils.println("isDataSetAlreadyExist:"+isDataSetAlreadyExist+" extractInventoryRecord.getDataSet().getIdentifier():"+
						extractInventoryRecord.getDataSet().getIdentifier());
				if (!isDataSetAlreadyExist) {
					extractInventoryRecord.setRemarks("Managing Dataset");
					createDataSet(extractInventoryRecord);
				}
			}
			if (!isAborted && !isReportExecutionViaNonSQL) {
				boolean isReportAlreadyExist=executionController.getReportIdentifierSet().contains(extractInventoryRecord.getDataSet().getIdentifier());
				FileUtils.println("isReportAlreadyExist:"+isReportAlreadyExist+" extractInventoryRecord.getDataSet().getIdentifier():"+
						extractInventoryRecord.getDataSet().getIdentifier());
				if (!isReportAlreadyExist) {
					extractInventoryRecord.setRemarks("Managing Report");
					createReport(extractInventoryRecord);
				}
			}
			if (!isAborted) {
				extractInventoryRecord.setRemarks("Executing Report");
				executeReportInThread(extractInventoryRecord);
			}
		}
		finally {
			if (ExtractMain.IS_CLEANUP_REPORTS_ON_THE_FLY) {
				if (isReportExecutionViaNonSQL) {
					return;
				}
				try{
					extractInventoryRecord.setRemarks("Cleaning up Report");
					deleteReport(extractInventoryRecord);
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
				try{
					extractInventoryRecord.setRemarks("Cleaning up Dataset");
					deleteDataSet(extractInventoryRecord);
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		}
	}
	
	
	/*
	 * The call to executeReport may be running forever depending of the SQL query running,
	 * and since there is no way to cancel the Web request, we then just ignore it as if it was cancelled.
	 */
	public void executeReportInThread(final ExtractInventoryRecord extractInventoryRecord) throws Exception {
		executeReportThrowable=null;
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					executeReport(extractInventoryRecord);
				}
				catch (Throwable e) {
					FileUtils.printStackTrace(e);
					executeReportThrowable=e;
				}
				finally {
					switchOn=false;
				}
			}
		};
		t.start();
		while ( switchOn) {
			if (isAborted) {
				break;
			}
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
		}
		
		if (executeReportThrowable!=null) {
			throw new Exception(executeReportThrowable);
		}
	
		if (!isAborted) {
			extractInventoryRecord.setRemarks("Finalizing");
			DataSet dataSet=extractInventoryRecord.getDataSet();
			String dataSetIdentifier=dataSet.getIdentifier();
			
			allLevelSelectionLevelToIDToDatarowMap = ExtractUtils.getSelectionLevelAndExtractDataRowsMap(extractDataRowsFromBIPublisher);
			
			for (Entry<SELECTION_LEVEL, Map<MapGeneric, List<ExtractDataRow>>> selectionLevelToIDToDatarowMap : allLevelSelectionLevelToIDToDatarowMap.entrySet()) {
				if(selectionLevelToIDToDatarowMap.getValue() == null) {
					continue;
				}
				for (Entry<MapGeneric, List<ExtractDataRow>> idToDatarowMap : selectionLevelToIDToDatarowMap.getValue().entrySet()) {
					
					List<ExtractDataRow> tempExtractDataRowsFromBIPublisher = idToDatarowMap.getValue();
					
					if (tempExtractDataRowsFromBIPublisher != null && tempExtractDataRowsFromBIPublisher.size()>0) {
						String fileName=null;
						File rscOutputFile=null;
						
						String entityLevel = selectionLevelToIDToDatarowMap.getKey().getLevel().trim();
						String entityName = idToDatarowMap.getKey().getName().trim() + "-" + idToDatarowMap.getKey().getId();

						File tempDataFolder = new File(executionController.getTempDataFolder(dataSetIdentifier), entityLevel);
						if(!entityName.equals("0")) { // happens in GLOBAL's case, no need for a sub folder in that case.
							tempDataFolder = new File(tempDataFolder, entityName);
						}
						if(!tempDataFolder.exists()) {
							tempDataFolder.mkdirs();
						}

						if (executionController.getExtractMain().getExtractMainPanel().getTabOptionsPanel().isExcelFormat()) {
							fileName=extractInventoryRecord.getInventoryName()+".xlsx";
							String rscDataFileName="DA"+dataSetIdentifier+".xlsx";
							rscOutputFile=new File(tempDataFolder,rscDataFileName);
							ExtractUtils.saveExtractDataRowsToRSCExcelFormat(extractInventoryRecord.getInventory(),tempExtractDataRowsFromBIPublisher,rscOutputFile);
						}
						else {
							fileName=extractInventoryRecord.getInventoryName()+".xml";
							String rscDataFileName="DA-RSC.xml";
							rscOutputFile=new File(tempDataFolder,rscDataFileName);
							ExtractUtils.saveExtractDataRowsToRSCXMLFormat(extractInventoryRecord.getInventory(),tempExtractDataRowsFromBIPublisher,rscOutputFile);
						}
						// copy the data file to the Download folder:
						File destFolder = new File(executionController.getExecutionDataFolder(), entityLevel);
						if(!entityName.equals("0")) { // happens in GLOBAL's case, no need for a sub folder in that case.
							destFolder = new File(destFolder, entityName);
						}
						if (!destFolder.exists()) {
							destFolder.mkdirs();
						}
						File destFile=new File(destFolder,fileName);
						Files.copy(rscOutputFile.toPath(),destFile.toPath());
					}
				}
			}
		}
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public int getTotalFailedTasks() {
		return totalFailedTasks;
	}

	public void setTotalFailedTasks(int totalFailedTasks) {
		this.totalFailedTasks = totalFailedTasks;
	}

	public int getTotalCompletedTasks() {
		return totalCompletedTasks;
	}

	public void abort() {
		if (extractionTask!=null) {
			ExtractInventoryRecord extractInventoryRecord=extractionTask.getExtractInventoryRecord();
			extractInventoryRecord.setRemarks("Aborted!");
			extractInventoryRecord.setStatus(UIConstants.UI_STATUS_CANCELLED);
		}
		isAborted=true;
	}	
	
	private void deleteDataSet(ExtractInventoryRecord extractInventoryRecord) throws Exception {
		DataSet dataSet=extractInventoryRecord.getDataSet();
		String dataSetIdentifier=dataSet.getIdentifier();
		WebServiceInfo catalogWebServiceInfo=executionController.getExtractMain().getApplicationInfoPanel().getCatalogWebServiceInfo();
		String datasetObjectAbsolutePathURL=executionController.getBIPublisherDataSetPath()+id+".xdm";
		FileUtils.println("deleteDataSet(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' datasetObjectAbsolutePathURL:"+datasetObjectAbsolutePathURL);
		boolean isDeleted=ExtractUtils.deleteObjectBIWebService(catalogWebServiceInfo,catalogService,datasetObjectAbsolutePathURL);
		FileUtils.println("deleteDataSet(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' isDeleted:"+isDeleted);
	}

	private void deleteReport(ExtractInventoryRecord extractInventoryRecord) throws Exception {
		DataSet dataSet=extractInventoryRecord.getDataSet();
		String dataSetIdentifier=dataSet.getIdentifier();
		WebServiceInfo catalogWebServiceInfo=executionController.getExtractMain().getApplicationInfoPanel().getCatalogWebServiceInfo();
		String reportObjectAbsolutePathURL=executionController.getBIPublisherReportPath()+id+".xdo";
		FileUtils.println("deleteDataSet(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' reportObjectAbsolutePathURL:"+reportObjectAbsolutePathURL);
		boolean isDeleted=ExtractUtils.deleteObjectBIWebService(catalogWebServiceInfo,catalogService,reportObjectAbsolutePathURL);
		FileUtils.println("deleteReport(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' isDeleted:"+isDeleted);
	}
	
	private void createDataSet(ExtractInventoryRecord extractInventoryRecord) throws Exception {
		DataSet dataSet=extractInventoryRecord.getDataSet();
		String dataSetIdentifier=dataSet.getIdentifier();
		String datasetObjectAbsolutePathURL=executionController.getBIPublisherDataSetPath()+dataSetIdentifier;
		boolean isDataSetExist=executionController.getDataSetIdentifierSet().contains(dataSetIdentifier);
		WebServiceInfo catalogWebServiceInfo=executionController.getExtractMain().getApplicationInfoPanel().getCatalogWebServiceInfo();

		FileUtils.println("createDataSet(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' isDataSetExist:"+
				isDataSetExist);
		if (isDataSetExist) {
			FileUtils.println("createDataSet(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' Deleting DataSet...");
			boolean isDeleted=ExtractUtils.deleteObjectBIWebService(catalogWebServiceInfo,catalogService,datasetObjectAbsolutePathURL);
			FileUtils.println("createDataSet(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' isDeleted:"+isDeleted);
		}

		File tempDatasetFolder=executionController.getTempDatasetFolder(dataSetIdentifier);
	
		String objectType="xdmz";
		String xdmzFileName="DS."+objectType;
		File xdmzFile=new File(tempDatasetFolder.getParentFile(),xdmzFileName);
		ExtractUtils.createXDMZFile(xdmzFile,tempDatasetFolder,dataSet); 
		
		FileUtils.println("createDataSet(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' Uploading DataSet archive...");
		String returnValue=ExtractUtils.uploadObjectBIWebService(catalogWebServiceInfo,catalogService,datasetObjectAbsolutePathURL,xdmzFile,objectType);
		FileUtils.println("createDataSet(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' returnValue:"+returnValue);
	}
	
	private void createReport(ExtractInventoryRecord extractInventoryRecord) throws Exception {
		DataSet dataSet=extractInventoryRecord.getDataSet();
		String dataSetIdentifier=dataSet.getIdentifier();
		String reportObjectAbsolutePathURL=executionController.getBIPublisherReportPath()+dataSetIdentifier;
		boolean isReportExist=executionController.getReportIdentifierSet().contains(dataSetIdentifier);
		FileUtils.println("createReport(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' isReportExist:"+
				isReportExist);
		WebServiceInfo catalogWebServiceInfo=executionController.getExtractMain().getApplicationInfoPanel().getCatalogWebServiceInfo();

		if (isReportExist) {
			FileUtils.println("createReport(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' Deleting Report...");
			boolean isDeleted=ExtractUtils.deleteObjectBIWebService(catalogWebServiceInfo,catalogService,reportObjectAbsolutePathURL);
			FileUtils.println("createReport(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' isDeleted:"+isDeleted);
		}
		
		File tempReportFolder=executionController.getTempReportFolder(dataSetIdentifier);
		
		String objectType="xdoz";
		String xdozFileName="RE."+objectType;
		File xdozFile=new File(tempReportFolder.getParentFile(),xdozFileName);
		
		String datasetObjectAbsolutePathURL=executionController.getBIPublisherDataSetPath()+dataSetIdentifier;
		ExtractUtils.createXDOZFile(xdozFile,tempReportFolder,datasetObjectAbsolutePathURL);
	
		FileUtils.println("createReport(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' Uploading Report archive...");
		String returnValue=ExtractUtils.uploadObjectBIWebService(catalogWebServiceInfo,catalogService,reportObjectAbsolutePathURL,xdozFile,objectType);
		FileUtils.println("createReport(), inventory: '"+extractInventoryRecord.getInventory().getName()+"' dataSetIdentifier: '"+dataSetIdentifier+"' returnValue:"+returnValue);
	}

	private void executeReport(ExtractInventoryRecord extractInventoryRecord) throws Exception {
		if (isReportExecutionViaNonSQL) {
			executeReportViaNonSQLDataset(extractInventoryRecord);
		}
		else {
			throw new Exception("Unsupported exrtraction mode");
		}
	}
		
	private void executeReportViaNonSQLDataset(ExtractInventoryRecord extractInventoryRecord) throws Exception {
		String dynamicNonSQLReportPath=executionController.getExtractMain().getApplicationInfoPanel().getSessionBIPublisherPath()+"/"+ExtractConstants.BI_PUBLISHER_DYNAMIC_SQL_DATASET_NAME+".xdo";
		WebServiceInfo reportWebServiceInfo=executionController.getExtractMain().getApplicationInfoPanel().getReportWebServiceInfo();
		String reformattedSQLCOUNTQueryForSQLDataset=ExtractUtils.getReformattedSQLQueryForNonSQLDataset(extractInventoryRecord.getDataSet(), true);
		String reformattedSQLQueryForSQLDataset=ExtractUtils.getReformattedSQLQueryForNonSQLDataset(extractInventoryRecord.getDataSet());
		
		StringBuffer whereConditions=new StringBuffer("");
		boolean isExcludeSeededData=executionController.getExtractMain().getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel().getFilterGeneralPanel().isExcludeSeededData();		
		if (isExcludeSeededData) {
			String whereConditionExcludeSeededUsers=ExtractUtils.getWhereConditionExcludeSeededUsers(executionController.getExtractMain());
			whereConditions.append(whereConditionExcludeSeededUsers);
		}
		// Include seeded data: no need to add it as a condition because it means we extract everything.
		
		boolean isCreatedByUserNameEnabled=executionController.getExtractMain().getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterUserPanel().isCreatedByUserNameEnabled();
		if (isCreatedByUserNameEnabled) {
			String whereConditionCreatedByUsers=ExtractUtils.getWhereConditionCreatedByUsers(executionController.getExtractMain());
			whereConditions.append(whereConditionCreatedByUsers);
		}		
		boolean isLastUpdateByUserNameEnabled=executionController.getExtractMain().getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterUserPanel().isLastUpdateByUserNameEnabled();
		if (isLastUpdateByUserNameEnabled) {
			String whereConditionLastUpdatedByUsers=ExtractUtils.getWhereConditionLastUpdatedByUsers(executionController.getExtractMain());
			whereConditions.append(whereConditionLastUpdatedByUsers);
		}
		
		boolean isCreationDateEnabled=executionController.getExtractMain().getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
		.getFilterDatePanel().isCreationDateEnabled();
		if (isCreationDateEnabled) {
			String whereConditionCreationDate=ExtractUtils.getWhereConditionCreationDate(executionController.getExtractMain());
			whereConditions.append(whereConditionCreationDate);
		}
		boolean isLastUpdateDateEnabled=executionController.getExtractMain().getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().isLastUpdateDateEnabled();
		if (isLastUpdateDateEnabled) {
			String whereConditionLastUpdateDate=ExtractUtils.getWhereConditionLastUpdateDate(executionController.getExtractMain());
			whereConditions.append(whereConditionLastUpdateDate);
		}
		
		String sqlQueryToRun=reformattedSQLQueryForSQLDataset+whereConditions.toString();
		//FileUtils.println("sqlQueryToRun:"+sqlQueryToRun);
		String sqlCOUNTQueryToRun=reformattedSQLCOUNTQueryForSQLDataset+whereConditions.toString();
		
		List<ParameterNameValue> parametersCOUNTList=new ArrayList<ParameterNameValue>();
		ParameterNameValue parameterCOUNTNameValue=new ParameterNameValue();
		parametersCOUNTList.add(parameterCOUNTNameValue);
		parameterCOUNTNameValue.setName("param1");
		parameterCOUNTNameValue.setValue(sqlCOUNTQueryToRun);

		byte[] outputCOUNTBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicNonSQLReportPath,parametersCOUNTList);
		int totalRecordsQuery = processOutputCount(outputCOUNTBytes);
		int batchSize = executionController.getBatchSize();
				
		String formattedTotal = Utils.formatNumberWithComma(totalRecordsQuery);
		int range = 1;
		int fileNum = 1;
		while (range <= totalRecordsQuery) {
			String formattedRange= Utils.formatNumberWithComma((range-1));
			if (extractInventoryRecord.hasDFFLogicToApply() || extractInventoryRecord.hasGDFLogicToApply()) {
				extractInventoryRecord.setRemarks(formattedRange+" / ? records.");
			}
			else {
				extractInventoryRecord.setRemarks(formattedRange+" / "+formattedTotal+" records.");
			}
			
			String rangeWhereCondition = " AND rnum BETWEEN " + range + " AND " +(range+batchSize-1);
			String sqlQueryRangedToRun = sqlQueryToRun + rangeWhereCondition;
			
			List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
			ParameterNameValue parameterNameValue=new ParameterNameValue();
			parametersList.add(parameterNameValue);
			parameterNameValue.setName("param1");
			parameterNameValue.setValue(sqlQueryRangedToRun);
			
			byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicNonSQLReportPath,parametersList);
			processOutputBytes(extractInventoryRecord,reportWebServiceInfo,outputBytes,fileNum);

			range += batchSize;
			fileNum++;
		}
	}
		
	private int processOutputCount(byte[] outputBytes) throws Exception {
		//File file=new File("test.xml");
		//ExtractUtils.saveOutput(outputBytes, file);
		return ExtractUtils.getExtractedCountFromBIPubliser(outputBytes);
	}
		
	private void processOutputBytes(ExtractInventoryRecord extractInventoryRecord,WebServiceInfo reportWebServiceInfo,byte[] outputBytes
			, int fileNum) throws Exception {
		if (!isAborted) {
			DataSet dataSet=extractInventoryRecord.getDataSet();
			String dataSetIdentifier=dataSet.getIdentifier();
			File tempDataFolder=executionController.getTempDataFolder(dataSetIdentifier);
			String biXMLDataFileName="DA." + fileNum + ".xml";
				
			File biXMLOutputFile=new File(tempDataFolder,biXMLDataFileName);
			ExtractUtils.saveOutput(outputBytes, biXMLOutputFile);
			//FileUtils.println("processOutputBytes, biXMLOutputFile:"+biXMLOutputFile.getAbsolutePath());
						
			if (extractInventoryRecord.hasDFFLogicToApply() || extractInventoryRecord.hasGDFLogicToApply()) {
				List<ExtractFlexfieldTempRow> extractFlexfieldTempRowList=ExtractUtils.getExtractFlexfieldsTempRowsFromBIPublisher(extractInventoryRecord,biXMLOutputFile);
				int tempTotalRecords=extractFlexfieldTempRowList.size();
				if (tempTotalRecords>0) {
					String dynamicNonSQLReportPath=executionController.getExtractMain().getApplicationInfoPanel().getDynamicNonSQLReportPath();
					String getValueSetTableNonSQLReportPath=executionController.getExtractMain().getApplicationInfoPanel().getSessionBIPublisherPath()+"/"+
							ExtractConstants.BI_PUBLISHER_GET_VALUE_TABLE_TYPE_SQL_DATASET_NAME+".xdo";
					
					if (extractInventoryRecord.hasDFFLogicToApply()) {
						DFFEngine dffEngine=new DFFEngine(reportWebServiceInfo,reportService,dynamicNonSQLReportPath,getValueSetTableNonSQLReportPath);
						extractDataRowsFromBIPublisher.addAll(
								dffEngine.apply(extractInventoryRecord,extractFlexfieldTempRowList)
								);
					}
					else {
						GDFEngine gdfEngine=new GDFEngine(reportWebServiceInfo,reportService,dynamicNonSQLReportPath,getValueSetTableNonSQLReportPath);

						extractDataRowsFromBIPublisher.addAll(
								gdfEngine.apply(extractInventoryRecord,extractFlexfieldTempRowList)
								);
					}
				}
				else {
					extractDataRowsFromBIPublisher=new ArrayList<ExtractDataRow>();
				}
			}
			else {
				extractDataRowsFromBIPublisher.addAll(
						ExtractUtils.getExtractDataRowsFromBIPublisher(extractInventoryRecord.getInventory(),biXMLOutputFile));
				String dynamicSQLReportPath=executionController.getExtractMain().getApplicationInfoPanel().getSessionBIPublisherPath()+"/"+ExtractConstants.BI_PUBLISHER_DYNAMIC_SQL_DATASET_NAME+".xdo";
				if (extractDataRowsFromBIPublisher.size()>0 && !extractInventoryRecord.getFunctionNamesSet().isEmpty()) {
					HookGetCodeCombination hookGetCodeCombination=executionController.getExtractMain().getApplicationInfoPanel().getHookGetCodeCombination();
					HookGetSystemProfileValue hookGetSystemProfileValue=executionController.getExtractMain().getApplicationInfoPanel().getHookGetSystemProfileValue();

					extractDataRowsFromBIPublisher=ExtractUtils.applyFunctions(hookGetCodeCombination,hookGetSystemProfileValue,dynamicSQLReportPath,
							reportWebServiceInfo,reportService,extractInventoryRecord,extractDataRowsFromBIPublisher);
				}
				if (extractInventoryRecord.hasKFFLogicToApply()) {
					KFFEngine kffEngine=new KFFEngine(extractInventoryRecord.getDataSet().getSqlQuery(),extractInventoryRecord.getInventory());
					kffEngine.init(dynamicSQLReportPath,reportWebServiceInfo,reportService);
					extractDataRowsFromBIPublisher=kffEngine.apply(extractDataRowsFromBIPublisher);
				}
			}
			File serializedDataFile=new File(tempDataFolder,"DAS.xml");
			ExtractUtils.saveExtractDataRows(serializedDataFile,extractDataRowsFromBIPublisher);
		}
	}

	public boolean isAborted() {
		return isAborted;
	}

	public int getId() {
		return id;
	}
	
}