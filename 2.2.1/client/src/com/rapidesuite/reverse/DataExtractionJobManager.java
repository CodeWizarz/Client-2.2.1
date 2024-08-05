/**************************************************
 * $Revision: 57085 $:
 * $Author: hassan.jamil $:
 * $Date: 2016-08-03 13:54:24 +0700 (Wed, 03 Aug 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/DataExtractionJobManager.java $:
 * $Id: DataExtractionJobManager.java 57085 2016-08-03 06:54:24Z hassan.jamil $:
 */
package com.rapidesuite.reverse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.JobManager;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.FileType;
import com.rapidesuite.reverse.gui.DataExtractionInventoryTreeNode;
import com.rapidesuite.reverse.gui.DataExtractionPanel;
import com.rapidesuite.reverse.gui.DataExtractionStatusTreeTableNode;
import com.rapidesuite.reverse.session.DataExtractionSession;
import com.rapidesuite.reverse.session.InventoryReverseResult;
import com.rapidesuite.reverse.session.InventoryReverseStatus;
import com.rapidesuite.reverse.session.ReverseOutputDetails;
import com.rapidesuite.reverse.session.ReverseOutputZipFile;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;
import com.rapidesuite.reverse.utils.DataExtractionTemplateUtils;
import com.sun.jna.Platform;

public class DataExtractionJobManager extends JobManager
{

	private boolean isIncludeSelectedUsers;
	private List<Integer> selectedUserIds;
	private String formattedFromDate;
	private String formattedToDate;
	private String rscPrerequisiteObjectsKeyword;
	private Map<Integer,String> allOracleUserIdToNameSynchronizedMap;
	private List<Long> selectedOperatingUnitIds;
	private List<Long> selectedBusinessGroupIds;
	private DataExtractionPanel dataExtractionPanel;
	private int executionJobsCounter;
	private int workersCount;
	private int maximumRecordsPerXMLFile;
	
	private final Map<DataExtractionJob, Integer> successfulJobRecordExtractionCount = new HashMap<DataExtractionJob, Integer>();
	private final Map<DataExtractionJob, String> warningJobRecordStatus = new HashMap<DataExtractionJob, String>();
	private final Map<DataExtractionJob, String> errorJobRecordStatus = new HashMap<DataExtractionJob, String>();
	//track ongoing processing based on insertion order - maintain consistent display
	private final LinkedHashMap<DataExtractionJob, String> processingJobRecordStatus = new LinkedHashMap<DataExtractionJob, String>();
	
	public DataExtractionJobManager(
			ReverseMain reverseMain,
			DataExtractionPanel dataExtractionPanel,
			boolean isIncludeSelectedUsers,
			List<Integer> selectedUserIds,
			String formattedFromDate,
			String formattedToDate,
			Map<Integer,String> allOracleUserIdToNameSynchronizedMap,
			int workersCount,
			List<Long> selectedOperatingUnitIds,
			List<Long> selectedBusinessGroupIds)
	throws Exception{
		super(reverseMain);
		this.dataExtractionPanel=dataExtractionPanel;
		this.isIncludeSelectedUsers=isIncludeSelectedUsers;
		this.selectedUserIds=selectedUserIds;
		this.formattedFromDate=formattedFromDate;
		this.formattedToDate=formattedToDate;
		this.allOracleUserIdToNameSynchronizedMap=allOracleUserIdToNameSynchronizedMap;
		this.selectedOperatingUnitIds=selectedOperatingUnitIds;
		this.selectedBusinessGroupIds=selectedBusinessGroupIds;
		this.workersCount=workersCount;
		rscPrerequisiteObjectsKeyword = Config.getRscPrerequisiteObjectsKeyword();
		maximumRecordsPerXMLFile=Config.getMaximumRecordsPerDataFile();
	}

	public void execute(List<InventoryTreeNode> inventoryTreeNodes)
	throws Exception {
		org.apache.commons.io.FileUtils.deleteDirectory(Config.getReverseOutputFolder());
		Config.getReverseOutputFolder().mkdirs();
		File tempReverseFolder=new File(com.rapidesuite.client.common.util.Config.getTempFolder(),"reverse");
		org.apache.commons.io.FileUtils.deleteDirectory(tempReverseFolder);

		List<InventoryTreeNode> inventoriesForSelectedOperatingUnitsList=new ArrayList<InventoryTreeNode>();
		List<InventoryTreeNode> inventoriesForSelectedBusinessGroupsList=new ArrayList<InventoryTreeNode>();
		List<InventoryTreeNode> inventoriesForInstanceLevelOnlyList=new ArrayList<InventoryTreeNode>();
		List<InventoryTreeNode> inventoriesNoSQLList=new ArrayList<InventoryTreeNode>();

		boolean isInstanceLevelReverseSelected=((ReverseMain)swiftGUIMain).getDataExtractionPanel().getDataExtractionEBSLevelSelectionPanel().isInstanceLevelSelected();
		boolean isOULevelReverseSelected=((ReverseMain)swiftGUIMain).getDataExtractionPanel().getDataExtractionEBSLevelSelectionPanel().isOperatingUnitLevelSelected();

		for (InventoryTreeNode inventoryTreeNode:inventoryTreeNodes) 
		{	
			boolean isInstanceLevel=((ReverseMain)swiftGUIMain).getDataExtractionPackagesSelectionPanel().isInstanceLevel(inventoryTreeNode.getName());
			boolean isOULevel=((ReverseMain)swiftGUIMain).getDataExtractionPackagesSelectionPanel().isOperatingUnitLevel(inventoryTreeNode.getName());
			boolean isBGLevel=((ReverseMain)swiftGUIMain).getDataExtractionPackagesSelectionPanel().isBusinessGroupLevel(inventoryTreeNode.getName());
			boolean isOULevelOnly=isOULevel && !isBGLevel;
			boolean isBGLevelOnly=!isOULevel && isBGLevel;

			if ( ((DataExtractionInventoryTreeNode)inventoryTreeNode).isUnreversible()) {
				continue;
			}

			if ( ((DataExtractionInventoryTreeNode)inventoryTreeNode).isInvalidNode()) {
				inventoriesNoSQLList.add(inventoryTreeNode);
				continue;
			}

			if (isOULevelReverseSelected)
			{
				if (isOULevel)
				{
					inventoriesForSelectedOperatingUnitsList.add(inventoryTreeNode);
				}
				if (isBGLevelOnly)
				{
					inventoriesForSelectedBusinessGroupsList.add(inventoryTreeNode);
				}
			}

			if (isInstanceLevelReverseSelected)
			{
				if (isInstanceLevel)
				{
					inventoriesForInstanceLevelOnlyList.add(inventoryTreeNode);
				}
				if (!selectedBusinessGroupIds.isEmpty())
				{
					if (isBGLevel && !isOULevelReverseSelected)
					{
						inventoriesForSelectedBusinessGroupsList.add(inventoryTreeNode);
					}
				}
			}
		}
		List<Job> executionJobs=new ArrayList<Job>();

		Map<String, File> packageMap = ((ReverseMain)getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().getPackageNameToFileMap();
		String plsqlPackageName="";
		if (!ReverseMain.IS_FUSION_DB ) {
			Assert.isTrue(packageMap.size() == 1);
			plsqlPackageName=packageMap.keySet().iterator().next();
		}		

		List<String> columnNames=new ArrayList<String>();
		columnNames.add("Job Id");
		columnNames.add("Path");
		columnNames.add("Status");
		columnNames.add("Execution time");
		DataExtractionStatusTreeTableNode rootNode=DataExtractionFileUtils.getDataExecutionStatusTreeTableNode("Root");

		String ebsVersion="("+((ReverseMain)swiftGUIMain).getDataExtractionEBSPropertiesValidationPanel().getDbEBSVersion()+")";
		Map<Long, String> operatingUnitIdToNameMap=dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getOperatingUnitSelectionPanel().getOperatingUnitIdToNameMap();
		Map<Long, String> businessGroupIdToNameMap=dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getBusinessGroupSelectionPanel().getBusinessGroupIdToNameMap();
		Map<Long, Long> operatingUnitIdToBusinessGroupIdMap=dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getBusinessGroupSelectionPanel().getOperatingUnitIdToBusinessGroupIdMap();

		DataExtractionStatusTreeTableNode rootInstanceLevelTreeTableNode=DataExtractionFileUtils.getDataExecutionStatusTreeTableNode(DataExtractionConstants.INSTANCE_LEVEL_FOLDER_NAME+" "+ebsVersion);
		if (!inventoriesForInstanceLevelOnlyList.isEmpty() ||
			!inventoriesForSelectedBusinessGroupsList.isEmpty()
		) {
			rootNode.add(rootInstanceLevelTreeTableNode);
		}
		DataExtractionStatusTreeTableNode rootBGSpecificTreeTableNode=DataExtractionFileUtils.getDataExecutionStatusTreeTableNode("Business Groups "+ebsVersion);
		if (!inventoriesForSelectedBusinessGroupsList.isEmpty()
		) {
			rootInstanceLevelTreeTableNode.add(rootBGSpecificTreeTableNode);
		}
		DataExtractionStatusTreeTableNode rootOperatingUnitSpecificTreeTableNode=DataExtractionFileUtils.getDataExecutionStatusTreeTableNode("Operating Units "+ebsVersion);
		if (!inventoriesForSelectedOperatingUnitsList.isEmpty()) {
			rootNode.add(rootOperatingUnitSpecificTreeTableNode);
		}

		if (!inventoriesForInstanceLevelOnlyList.isEmpty()) {
			recreatePrefixOutputFolder(DataExtractionConstants.INSTANCE_LEVEL_FOLDER);
			executionJobs.addAll(createInstanceLevelJobs(rootInstanceLevelTreeTableNode,inventoriesForInstanceLevelOnlyList,DataExtractionConstants.INSTANCE_LEVEL_FOLDER,plsqlPackageName));
		}
		if (!inventoriesForSelectedBusinessGroupsList.isEmpty()) {
			for (Long businessGroupId:selectedBusinessGroupIds){
				String businessGroupName=businessGroupIdToNameMap.get(businessGroupId);
				DataExtractionStatusTreeTableNode rootBGTreeTableNode=DataExtractionFileUtils.getDataExecutionStatusTreeTableNode(businessGroupName);
				rootBGSpecificTreeTableNode.add(rootBGTreeTableNode);
				File prefixOutputFolder=Utils.getPrefixOutputFolder(businessGroupName,businessGroupId);
				new File(Config.getReverseOutputFolder(),prefixOutputFolder.getName()).mkdir();
				executionJobs.addAll(createBusinessGroupJobs(rootBGTreeTableNode,inventoriesForSelectedBusinessGroupsList,prefixOutputFolder,plsqlPackageName,businessGroupName,businessGroupId));
			}
		}
		if (!inventoriesForSelectedOperatingUnitsList.isEmpty()) {
			for (Long operatingUnitId:selectedOperatingUnitIds){
				String operatingUnitName=operatingUnitIdToNameMap.get(operatingUnitId);
				Long businessGroupId=operatingUnitIdToBusinessGroupIdMap.get(operatingUnitId);
				String businessGroupName="";
				if (businessGroupId!=null) {
					businessGroupName=businessGroupIdToNameMap.get(businessGroupId);
				}
				DataExtractionStatusTreeTableNode rootOperatingUnitNameTreeTableNode=DataExtractionFileUtils.getDataExecutionStatusTreeTableNode(operatingUnitName);
				rootOperatingUnitSpecificTreeTableNode.add(rootOperatingUnitNameTreeTableNode);
				File prefixOutputFolder=Utils.getPrefixOutputFolder(operatingUnitName,operatingUnitId);
				new File(Config.getReverseOutputFolder(),prefixOutputFolder.getName()).mkdir();
				executionJobs.addAll(createOperatingUnitJobs(rootOperatingUnitNameTreeTableNode,inventoriesForSelectedOperatingUnitsList,prefixOutputFolder,plsqlPackageName,businessGroupName,businessGroupId,operatingUnitName,operatingUnitId));
			}
		}

		if (!inventoriesNoSQLList.isEmpty()) {
			DataExtractionStatusTreeTableNode rootNoSQLTreeTableNode=DataExtractionFileUtils.getDataExecutionStatusTreeTableNode("Unsupported");
			List<Job> jobsNoSQL=createInstanceLevelJobs(rootNoSQLTreeTableNode,inventoriesNoSQLList,null,plsqlPackageName);
			executionJobs.addAll(jobsNoSQL);
			rootNode.add(rootNoSQLTreeTableNode);
		}
		Map<Integer,Job> taskIdToExecutionJobMap=Utils.convertListToMap(executionJobs);

		rootNode.setExecutionJob(taskIdToExecutionJobMap);
		dataExtractionPanel.getReverseExecutionStatusPanel().createExecutionAllStatusTreeTable(rootNode,columnNames);
		startExecution(executionJobs,new DataExtactionJobStatusManager(this,executionJobs),workersCount);
	}

	public void ensureNoDuplicateJobs(List<Job> executionJobs) {
		Map<String,Job> inventoryNameToExecutionJobMap=new HashMap<String,Job>();
		for (Job executionJob:executionJobs){
			Job tempJob=inventoryNameToExecutionJobMap.get(executionJob.getInventory().getName());
			if (tempJob==null) {
				inventoryNameToExecutionJobMap.put(executionJob.getInventory().getName(),executionJob);
			}
			else {
				((DataExtractionJob)executionJob).setIsDuplicateOfJobMessage("This table will be reversed in the job id: "+tempJob.getJobId()+
						" path: '"+tempJob.getInventoryFileName()+"'");
			}
		}
	}

	public void recreatePrefixOutputFolder(File prefixOutputFolder) throws IOException {
		File absFolder = new File(Config.getReverseOutputFolder(),prefixOutputFolder.getName());
		org.apache.commons.io.FileUtils.deleteDirectory(absFolder);
		absFolder.mkdirs();
	}

	public List<Job> createBusinessGroupJobs(DataExtractionStatusTreeTableNode rootTreeTableNode,List<InventoryTreeNode> list,File prefixOutputFolder,String plsqlPackageName,
			String businessGroupName,Long businessGroupId) throws Exception {
		List<Job> toReturn=new ArrayList<Job>();
		for (InventoryTreeNode inventoryTreeNode:list) {
			executionJobsCounter++;
			DataExtractionJob job= new DataExtractionBusinessGroupLevelJob(
					this,
					executionJobsCounter,
					((ReverseMain)swiftGUIMain).getDataExtractionEBSPropertiesValidationPanel().getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
					prefixOutputFolder,
					inventoryTreeNode.getArchiveItemName(),
					((ReverseMain)swiftGUIMain).getDataExtractionPackagesSelectionPanel().getInventoriesMap().get(inventoryTreeNode.getName()),
					businessGroupName,
					businessGroupId,
					plsqlPackageName,
					swiftGUIMain);
			toReturn.add(job);
			Utils.convertInventoryTreeNodesToTreeTableNode(rootTreeTableNode,inventoryTreeNode,executionJobsCounter);
		}
		ensureNoDuplicateJobs(toReturn);
		return toReturn;
	}

	public List<Job> createOperatingUnitJobs(DataExtractionStatusTreeTableNode rootTreeTableNode,List<InventoryTreeNode> list,File prefixOutputFolder,String plsqlPackageName,
			String businessGroupName,Long businessGroupId,String operatingUnitName,Long operatingUnitId) throws Exception {
		List<Job> toReturn=new ArrayList<Job>();
		for (InventoryTreeNode inventoryTreeNode:list) {
			executionJobsCounter++;
			DataExtractionJob job= new DataExtractionOperatingUnitLevelJob(
					this,
					executionJobsCounter,
					((ReverseMain)swiftGUIMain).getDataExtractionEBSPropertiesValidationPanel().getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
					prefixOutputFolder,
					inventoryTreeNode.getArchiveItemName(),
					((ReverseMain)swiftGUIMain).getDataExtractionPackagesSelectionPanel().getInventoriesMap().get(inventoryTreeNode.getName()),
					businessGroupName,
					businessGroupId,
					operatingUnitName,
					operatingUnitId,
					plsqlPackageName,
					swiftGUIMain);
			toReturn.add(job);
			Utils.convertInventoryTreeNodesToTreeTableNode(rootTreeTableNode,inventoryTreeNode,executionJobsCounter);
		}
		ensureNoDuplicateJobs(toReturn);
		return toReturn;
	}

	public List<Job> createInstanceLevelJobs(DataExtractionStatusTreeTableNode rootTreeTableNode,List<InventoryTreeNode> list,File prefixOutputFolder,String plsqlPackageName) throws Exception {
		List<Job> toReturn=new ArrayList<Job>();
		for (InventoryTreeNode inventoryTreeNode: list) {
			executionJobsCounter++;

			DataExtractionInstanceLevelJob job= new DataExtractionInstanceLevelJob(
					this,
					executionJobsCounter,
					((ReverseMain)swiftGUIMain).getDataExtractionEBSPropertiesValidationPanel().getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
					prefixOutputFolder,
					inventoryTreeNode.getArchiveItemName(),
					((ReverseMain)swiftGUIMain).getDataExtractionPackagesSelectionPanel().getInventoriesMap().get(inventoryTreeNode.getName()),
					plsqlPackageName,
					this.swiftGUIMain);
			toReturn.add(job);

			Utils.convertInventoryTreeNodesToTreeTableNode(
					rootTreeTableNode,
					inventoryTreeNode,
					executionJobsCounter);
		}
		ensureNoDuplicateJobs(toReturn);
		return toReturn;
	}

	public String getFormattedFromDate() {
		return formattedFromDate;
	}

	public String getFormattedToDate() {
		return formattedToDate;
	}

	public boolean isIncludeSelectedUsers() {
		return isIncludeSelectedUsers;
	}

	public List<Integer> getSelectedUserIds() {
		return selectedUserIds;
	}

	public String getRscPrerequisiteObjectsKeyword() {
		return rscPrerequisiteObjectsKeyword;
	}

	public void zipOutputFoldersAndCreateReverseOutputDetails(long startTimeInMS) {
		try{
			GUIUtils.showInProgressMessage(((ReverseMain)swiftGUIMain).getDataExtractionPanel().getStatusLabel(),
					"Compressing output sub-folders..., execution time: "+
					Utils.getExecutionTime(startTimeInMS,System.currentTimeMillis()));
			FileUtils.println("Compressing output sub-folders....");

			Date today=new Date();
			String formattedDate=CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE.getDateFormat().format(today);

			File zipFolder=null;
			if (dataExtractionPanel.getDataExtractionOptionsPanel().getSelectedZipFolder()!=null) {
				zipFolder=dataExtractionPanel.getDataExtractionOptionsPanel().getSelectedZipFolder();
			}
			else {
				zipFolder=new File(".");
			}
			zipFolder.mkdirs();
			
			final Map<File, DataExtractionJob> outputFileToJobMap = new HashMap<File, DataExtractionJob>();
			for (final DataExtractionJob job : this.successfulJobRecordExtractionCount.keySet()) {
				String prefixOutputFolder = FileUtils.removeTrailingInvalidCharactersForWindows(job.getPrefixOutputFolder().getName());
				final File f = new File(new File(Config.getReverseOutputFolder(), prefixOutputFolder), job.getInventory().getName() + "." + SwiftBuildConstants.XML_FILE_EXTENSION);
				outputFileToJobMap.put(f, job);
			}
			for (final DataExtractionJob job : this.warningJobRecordStatus.keySet()) {
				String prefixOutputFolder = FileUtils.removeTrailingInvalidCharactersForWindows(job.getPrefixOutputFolder().getName());
				final File f = new File(new File(Config.getReverseOutputFolder(), prefixOutputFolder), job.getInventory().getName() + "." + SwiftBuildConstants.XML_FILE_EXTENSION);
				outputFileToJobMap.put(f, job);
			}
			for (final DataExtractionJob job : this.errorJobRecordStatus.keySet()) {
				String prefixOutputFolder = FileUtils.removeTrailingInvalidCharactersForWindows(job.getPrefixOutputFolder().getName());
				final File f = new File(new File(Config.getReverseOutputFolder(), prefixOutputFolder), job.getInventory().getName() + "." + SwiftBuildConstants.XML_FILE_EXTENSION);
				outputFileToJobMap.put(f, job);
			}			
			
			final Map<File, Set<Job>> zipFileToJobs = new HashMap<File, Set<Job>>();
			final Set<DataExtractionJob> unzippedJobs = new HashSet<DataExtractionJob>();

			File[] outputFolderContents=Config.getReverseOutputFolder().listFiles();
			for (File outputFolderContent:outputFolderContents) {
				File targetZipFile=new File(zipFolder,outputFolderContent.getName()+"-"+formattedDate+".zip");
				FileUtils.zipFolder(outputFolderContent,targetZipFile);
				final File dataFiles[] = outputFolderContent.listFiles();
				for (final File dataFile : dataFiles) {
					final String tableName = CoreUtil.getTableName(dataFile.getName());
					Assert.isTrue(outputFileToJobMap.containsKey(dataFile), "tableNameToJobMap does not contain inventory '"+tableName+"' ('"+dataFile.getPath()+"')");
					final Job associatedJob = outputFileToJobMap.get(dataFile);
					if (!zipFileToJobs.containsKey(targetZipFile)) {
						zipFileToJobs.put(targetZipFile, new HashSet<Job>());
					}
					zipFileToJobs.get(targetZipFile).add(associatedJob);
					outputFileToJobMap.remove(dataFile);
				}
			}
			unzippedJobs.addAll(outputFileToJobMap.values());
			
			FileUtils.println("Compressing output sub-folders completed.");
			
			
			final ReverseOutputDetails reverseOutputDetails = new ReverseOutputDetails();
			final DataExtractionSession dataExtractionSession = DataExtractionTemplateUtils.createDataExtractionSession(dataExtractionPanel);
			reverseOutputDetails.setDataExtractionSession(dataExtractionSession);
			
			for (final Map.Entry<File, Set<Job>> entry : zipFileToJobs.entrySet()) {
				ReverseOutputZipFile reverseOutputZipFile = new ReverseOutputZipFile();
				reverseOutputZipFile.setName(entry.getKey().getName());
				
				for (final Job job : entry.getValue()) {
					InventoryReverseResult inventoryReverseResult = null;
					if (successfulJobRecordExtractionCount.containsKey(job)) {
						inventoryReverseResult = new InventoryReverseResult();
						inventoryReverseResult.setExtractedRecordCount(BigInteger.valueOf(successfulJobRecordExtractionCount.get(job)));
						inventoryReverseResult.setStatusType(InventoryReverseStatus.SUCCESS);
					} else if (warningJobRecordStatus.containsKey(job)) {
						inventoryReverseResult = new InventoryReverseResult();
						inventoryReverseResult.setMessage(warningJobRecordStatus.get(job));
						inventoryReverseResult.setStatusType(InventoryReverseStatus.WARNING);
					} else if (errorJobRecordStatus.containsKey(job)) {
						inventoryReverseResult = new InventoryReverseResult();
						inventoryReverseResult.setMessage(errorJobRecordStatus.get(job));
						inventoryReverseResult.setStatusType(InventoryReverseStatus.ERROR);
					} else {
						throw new IllegalArgumentException("Inventory '"+job.getInventory().getName()+"' (job id = "+job.getJobId()+", file name = '"+job.getInventoryFileName()+"') does not have its final status set");
					}
					
					if (inventoryReverseResult != null) {
						inventoryReverseResult.setName(job.getInventory().getName());
						inventoryReverseResult.setExecutionTime(CoreUtil.getDaysHoursMinutesSecondsFromMilliseconds(job.getExecutionTime()));							
						reverseOutputZipFile.getInventoryReverseResult().add(inventoryReverseResult);							
					}

				}
				
				reverseOutputDetails.getReverseOutputZipFile().add(reverseOutputZipFile);
			}
			
			final ReverseOutputZipFile reverseOutputWithoutZipFile = new ReverseOutputZipFile();
			for (final DataExtractionJob job : unzippedJobs) {
				InventoryReverseResult inventoryReverseResult = null;
				if (successfulJobRecordExtractionCount.containsKey(job)) {
					throw new IllegalArgumentException("Inventory '"+job.getInventory().getName()+"' has no output file, even though the processing was successful");
				} else if (warningJobRecordStatus.containsKey(job)) {
					if(job.isPrerequisiteObjectExists()){
						throw new IllegalArgumentException("Inventory '"+job.getInventory().getName()+"' has no output file, even though the processing only yielded warning and the prerequisite object was found");
					}
				} else if (errorJobRecordStatus.containsKey(job)) {
					inventoryReverseResult = new InventoryReverseResult();
					inventoryReverseResult.setMessage(errorJobRecordStatus.get(job));
					inventoryReverseResult.setStatusType(InventoryReverseStatus.ERROR);
				} else {
					throw new IllegalArgumentException("Inventory '"+job.getInventory().getName()+"' (job id = "+job.getJobId()+", file name = '"+job.getInventoryFileName()+"') does not have its final status set");
				}
				
				if (inventoryReverseResult != null) {
					inventoryReverseResult.setName(job.getInventory().getName());	
					inventoryReverseResult.setExecutionTime(CoreUtil.getDaysHoursMinutesSecondsFromMilliseconds(job.getExecutionTime()));						
					reverseOutputWithoutZipFile.getInventoryReverseResult().add(inventoryReverseResult);						
				}
			}
			
			if (!reverseOutputWithoutZipFile.getInventoryReverseResult().isEmpty()) {
				reverseOutputDetails.getReverseOutputZipFile().add(reverseOutputWithoutZipFile);
			}

			final File bweFile = ((ReverseMain)this.dataExtractionPanel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().getEnvironmentValidationPanel().getLoadedFile();
			if (bweFile != null) {
				reverseOutputDetails.setBweFile(bweFile.getName());
			}
			reverseOutputDetails.setPackageFile(((ReverseMain)this.dataExtractionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().getInventoriesPackageFile().getName());
			reverseOutputDetails.setTimestamp(CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_PRECISE.getDateFormat().format(today));
            
            JAXBContext jaxbContext = JAXBContext.newInstance(ReverseOutputDetails.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal((new com.rapidesuite.reverse.session.ObjectFactory()).createReverseOutputDetails(reverseOutputDetails), sw);
            final String xmlContent = sw.toString();
	            
	        
            CoreUtil.writeToFile(xmlContent, false, FileUtils.getLogFolder(), 
            	"reverse-"+
            			CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE.getDateFormat().format(today)+
            			"-execute"+
            			FileType.XML.getFileExtension());
            
            if (Config.getReverseOutputDetailsFile() != null) {
    			File tempDir = new File(Config.getTempFolder(), UUID.randomUUID().toString());
    			tempDir.mkdirs();        	
				CoreUtil.writeToFile(xmlContent, false, tempDir, CoreConstants.REVERSE_OUTPUT_DETAILS_XML_FILE_NAME);
				
				File reverseOutputDetailsZipFile = new File(Config.getReverseOutputDetailsFile());
				if (reverseOutputDetailsZipFile.isFile()) {
					reverseOutputDetailsZipFile.delete();
					FileUtils.println("Deleting pre-existing file '"+reverseOutputDetailsZipFile.getPath()+"'");
				} else if (reverseOutputDetailsZipFile.isDirectory()) {
					org.apache.commons.io.FileUtils.deleteDirectory(reverseOutputDetailsZipFile);
					FileUtils.println("Deleting pre-existing directory '"+reverseOutputDetailsZipFile.getPath()+"'");
				}

				final Set<File> tempDirContents = new HashSet<File>();
				tempDirContents.add(new File(tempDir, CoreConstants.REVERSE_OUTPUT_DETAILS_XML_FILE_NAME));
				for (final File zipFile : zipFileToJobs.keySet()) {
					tempDirContents.add(zipFile);
				}
				reverseOutputDetailsZipFile.getAbsoluteFile().getParentFile().mkdirs();
				FileUtils.zipFiles(tempDirContents, reverseOutputDetailsZipFile);
				org.apache.commons.io.FileUtils.deleteDirectory(tempDir);
			}
		}
		catch (Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

	public Map<Integer, String> getAllOracleUserIdToNameSynchronizedMap() {
		return allOracleUserIdToNameSynchronizedMap;
	}

	public int getMaximumRecordsPerXMLFile() {
		return maximumRecordsPerXMLFile;
	}
	
	
	@Override
	public void updateExecutionStatus(Job task,String status) {
		super.updateExecutionStatus(task, status);
		
		if (status != null) {
			if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_WARNING)) {
				
				Assert.isTrue(!this.successfulJobRecordExtractionCount.containsKey(task), "Failed to write warning status because there is pre-existing success status for inventory '"
						+task.getInventory().getName()+"' (job id = "+task.getJobId()+", file name = '"+task.getInventoryFileName()+"')");
				Assert.isTrue(!this.errorJobRecordStatus.containsKey(task), "Failed to write warning status because there is pre-existing error status for inventory '"
						+task.getInventory().getName()+"' (job id = "+task.getJobId()+", file name = '"+task.getInventoryFileName()+"')");				
				Assert.isTrue(task instanceof DataExtractionJob, "task must be an instance of DataExtractionJob. Instead, task is of class "+task.getClass().getCanonicalName());
				String statusTruncated = status.substring(ExecutionStatusTreeTableNode.STATUS_WARNING.length()).replaceAll("^\\s*:\\s*", "");
				this.warningJobRecordStatus.put((DataExtractionJob) task, statusTruncated);
                this.processingJobRecordStatus.remove((DataExtractionJob) task);
			} else if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_ERROR)) {
				
				Assert.isTrue(!this.successfulJobRecordExtractionCount.containsKey(task), "Failed to write error status because there is pre-existing success status for inventory '"
						+task.getInventory().getName()+"' (job id = "+task.getJobId()+", file name = '"+task.getInventoryFileName()+"')");
				Assert.isTrue(!this.warningJobRecordStatus.containsKey(task), "Failed to write error status because there is pre-existing warning status for inventory '"
						+task.getInventory().getName()+"' (job id = "+task.getJobId()+", file name = '"+task.getInventoryFileName()+"')");				
				Assert.isTrue(task instanceof DataExtractionJob, "task must be an instance of DataExtractionJob. Instead, task is of class "+task.getClass().getCanonicalName());
				String statusTruncated = status.substring(ExecutionStatusTreeTableNode.STATUS_ERROR.length()).replaceAll("^\\s*:\\s*", "");
				this.errorJobRecordStatus.put((DataExtractionJob) task, statusTruncated);
				this.processingJobRecordStatus.remove((DataExtractionJob) task);
			} else if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_PROCESSING)) {
			    this.processingJobRecordStatus.put((DataExtractionJob) task, status);
            } else if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_SUCCESS) || status.startsWith(ExecutionStatusTreeTableNode.STATUS_DONE) ) {
                this.processingJobRecordStatus.remove((DataExtractionJob) task);
            }
		}
	}	
	
	public void updateSuccessfulExecutionStatus(DataExtractionJob task,int numberOfRecords) throws Exception {
		updateExecutionStatus(task, "SUCCESS: "+Utils.formatNumberWithComma(numberOfRecords)+"/"+Utils.formatNumberWithComma(numberOfRecords)+" records extracted.");
		
		Assert.isTrue(!this.warningJobRecordStatus.containsKey(task), "Failed to write success status because there is pre-existing warning status for inventory '"
				+task.getInventory().getName()+"' (job id = "+task.getJobId()+", file name = '"+task.getInventoryFileName()+"')");
		Assert.isTrue(!this.errorJobRecordStatus.containsKey(task), "Failed to write success status because there is pre-existing error status for inventory '"
				+task.getInventory().getName()+"' (job id = "+task.getJobId()+", file name = '"+task.getInventoryFileName()+"')");		
		
		this.successfulJobRecordExtractionCount.put(task, numberOfRecords);
	}
	
	
	public void updateStatusMessage(long startTimeInMS)
	{
	    File file = new File(Config.getLogFolder(), CoreConstants.REVERSE_STATUS_OUTPUT_FILE);
	    try ( PrintWriter pw = new PrintWriter(file) )
	    {
	        String statusMessage = ((DataExtactionJobStatusManager)this.executionWorkerStatusManager).generateStatusMessage();
            pw.print("Reverse: " + statusMessage + " Status: " + this.errorJobRecordStatus.size() + " errors, " + this.warningJobRecordStatus.size() + " warnings, "
                    + this.successfulJobRecordExtractionCount.size() + " tasks complete.");
            pw.print("<table>");
            for ( Entry<DataExtractionJob, String> entry : this.processingJobRecordStatus.entrySet() )
            {
                pw.print("<tr><td>&nbsp;&nbsp;&nbsp; <b>" + entry.getKey().getInventory().getName() + "</b>&nbsp;&nbsp;&nbsp; </td><td>" + entry.getValue() + "</td></tr>");
            }
            pw.print("</table>");
	    }
	    catch(Exception e)
	    {
	        throw new Error(e);
	    }
	}

	
	
	
	
}