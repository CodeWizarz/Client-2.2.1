package com.rapidesuite.snapshot.model;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.SevenZipUtils;
import com.rapidesuite.client.snapshotMetadata0000.BgType;
import com.rapidesuite.client.snapshotMetadata0000.InvType;
import com.rapidesuite.client.snapshotMetadata0000.InventoriesType;
import com.rapidesuite.client.snapshotMetadata0000.InventoryType;
import com.rapidesuite.client.snapshotMetadata0000.LedgerType;
import com.rapidesuite.client.snapshotMetadata0000.LegalEntityType;
import com.rapidesuite.client.snapshotMetadata0000.OuBGMapType;
import com.rapidesuite.client.snapshotMetadata0000.OuInvMapType;
import com.rapidesuite.client.snapshotMetadata0000.OuToledgerMapType;
import com.rapidesuite.client.snapshotMetadata0000.OuTolegentMapType;
import com.rapidesuite.client.snapshotMetadata0000.OuType;
import com.rapidesuite.client.snapshotMetadata0000.OusToBGsType;
import com.rapidesuite.client.snapshotMetadata0000.OusToinvsType;
import com.rapidesuite.client.snapshotMetadata0000.OusToledgersType;
import com.rapidesuite.client.snapshotMetadata0000.OusTolegentsType;
import com.rapidesuite.client.snapshotMetadata0000.OusType;
import com.rapidesuite.client.snapshotMetadata0000.SnapshotInformationType;
import com.rapidesuite.client.snapshotMetadata0000.SnapshotMetadataDocument;
import com.rapidesuite.client.snapshotMetadata0000.SnapshotMetadataType;
import com.rapidesuite.client.snapshotMetadata0000.UserType;
import com.rapidesuite.client.snapshotMetadata0000.UsersType;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.controller.SnapshotController;
import com.rapidesuite.snapshot.controller.SnapshotImportController;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;

public class SnapshotImportSwingWorker extends SnapshotSwingWorker {

	private TabSnapshotsPanel tabSnapshotsPanel;
	private Map<String,String> snapshotEnvironmentProperties;
	private File exportFile;
	private Set<Integer> tableIds;
	private File tempRootFolder;
	private SnapshotGridRecord snapshotGridRecord;

	public static final String IMPORT_MODE="I";
	
	public static final String IMPORT_METADATA_TYPE_OU="OU";
	public static final String IMPORT_METADATA_TYPE_USER="USER";
	public static final String IMPORT_METADATA_TYPE_OU_TO_INV="OU_TO_INV";
	public static final String IMPORT_METADATA_TYPE_OU_TO_LEDGER="OU_TO_LEDGER";
	public static final String IMPORT_METADATA_TYPE_OU_TO_LEGAL_ENTITY="OU_TO_LEGAL_ENTITY";
	public static final String IMPORT_METADATA_TYPE_OU_TO_BG="OU_TO_BG";
	
	public SnapshotImportSwingWorker(TabSnapshotsPanel tabSnapshotsPanel, File exportFile) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.exportFile=exportFile;
		snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
	}

	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}

	protected void processAction() {
		File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
		File tempUnpackedFolder=new File(userhomeFolder,"exportUnzip");
		try {
			Connection connection=null;
			List<SnapshotInventoryGridRecord> recordsToImportList=null;
			try{
				String dbUserName=ModelUtils.getDBUserName(snapshotEnvironmentProperties);

				connection=DatabaseUtils.getJDBCConnection(
						ModelUtils.getJDBCString(snapshotEnvironmentProperties),
						dbUserName,
						ModelUtils.getDBPassword(snapshotEnvironmentProperties));
				connection.setAutoCommit(false);

				super.updateExecutionLabels("Please wait, unpacking...");
								
				if (tempUnpackedFolder.exists()) {
					FileUtils.deleteDirectory(tempUnpackedFolder);
				}
				tempUnpackedFolder.mkdirs();
				SevenZipUtils.decompressFile(exportFile,tempUnpackedFolder);

				File[] files=tempUnpackedFolder.listFiles();
				if (files==null || files.length!=1) {
					throw new Exception("Invalid export file!");
				}
				tempRootFolder=files[0];

				super.updateExecutionLabels("Retrieving list of inventories...");

				File metadataFile=new File(tempRootFolder,SnapshotExportSwingWorker.METADATA_FILE_NAME);
				SnapshotMetadataDocument snapshotMetadataDocument=validateSnapshotMetadataDocument(metadataFile);
				SnapshotMetadataType snapshotMetadataType=snapshotMetadataDocument.getSnapshotMetadata();
				SnapshotInformationType snapshotInformationType=snapshotMetadataType.getSnapshotInformation();

				snapshotGridRecord=new SnapshotGridRecord();
				snapshotGridRecord.setDescription(snapshotInformationType.getDescription());
				snapshotGridRecord.setName("(IMPORT) "+snapshotInformationType.getName());
				snapshotGridRecord.setCompletedOn(snapshotInformationType.getCompletedOn());
				snapshotGridRecord.setConversion(snapshotInformationType.getIsConversion());
				snapshotGridRecord.setStatus(snapshotInformationType.getStatus());
				snapshotGridRecord.setUserId(-1);
				snapshotGridRecord.setMode(IMPORT_MODE); // for Import type
				snapshotGridRecord.setCreatedOn(snapshotInformationType.getCreatedOn());
				snapshotGridRecord.setClientHostName(snapshotInformationType.getClientHostName());
				snapshotGridRecord.setOsUserName(snapshotInformationType.getOsUserName());
				snapshotGridRecord.setTemplateName(snapshotInformationType.getTemplateName());
				int snapshotId=ModelUtils.getNextSequenceIdConnection(connection,"SNAPSHOT_ID");
				snapshotGridRecord.setSnapshotId(snapshotId);
				ModelUtils.insertSnapshot(connection,snapshotGridRecord,false);

				List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList=new ArrayList<SnapshotInventoryGridRecord>();
				InventoriesType inventoriesType=snapshotMetadataType.getInventories();
				for (InventoryType inventoryType:inventoriesType.getInventoryArray()) {
					SnapshotInventoryGridRecord snapshotInventoryGridRecord=new SnapshotInventoryGridRecord(inventoryType.getName());			
					snapshotInventoryGridRecord.setRemarks(inventoryType.getRemarks());
					snapshotInventoryGridRecord.setStatus(inventoryType.getStatus());
					snapshotInventoryGridRecord.setTotalRecords((int)inventoryType.getTotalRecords());
					snapshotInventoryGridRecord.setDownloadTotalRecordsCount((int)inventoryType.getTotalRecords());
					snapshotInventoryGridRecord.setRawTimeInSecs(inventoryType.getRawTimeInSecs());
					snapshotInventoryGridRecord.setExecutionTime(inventoryType.getExecutionTime());
					snapshotInventoryGridRecord.setCreatedOn(inventoryType.getCreatedOn());
					snapshotInventoryGridRecord.setCompletedOn(inventoryType.getCompletedOn());	
					snapshotInventoryGridRecordsList.add(snapshotInventoryGridRecord);
				}			
				ModelUtils.insertNewInventories(connection,snapshotInventoryGridRecordsList);
				Map<String, Integer> inventoryNameToTableIdMap=ModelUtils.getInventoryNameToTableIdMap(connection);
				for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsList) {
					Integer inventoryId=inventoryNameToTableIdMap.get(snapshotInventoryGridRecord.getInventoryName());
					if (inventoryId==null) {
						throw new Exception("Unable to find the inventory DB record: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
					}
					snapshotInventoryGridRecord.setTableId(inventoryId);
				}			
				ModelUtils.insertInventoryToSnapshot(connection,snapshotId,snapshotInventoryGridRecordsList);
				connection.commit();

				super.updateExecutionLabels("Importing Oracle Users, OU's,...");
				applyImportPatch(connection,dbUserName);
				
				OusType ousType=snapshotMetadataType.getOus();
				OuType[] ouTypeArray=ousType.getOuArray();
				List<List<String>> valuesList=new ArrayList<List<String>>();
				for (OuType ouType:ouTypeArray) {
					List<String> values=new ArrayList<String>();
					valuesList.add(values);
					values.add(""+ouType.getId());
					values.add(ouType.getName());
				}
				insertMetadata(connection,snapshotId,IMPORT_METADATA_TYPE_OU,valuesList);
				
				UsersType usersType=snapshotMetadataType.getUsers();
				UserType[] userTypeArray=usersType.getUserArray();
				valuesList=new ArrayList<List<String>>();
				for (UserType userType:userTypeArray) {
					List<String> values=new ArrayList<String>();
					valuesList.add(values);
					values.add(""+userType.getId());
					values.add(userType.getName());
				}
				insertMetadata(connection,snapshotId,IMPORT_METADATA_TYPE_USER,valuesList);
				
				OusToinvsType ousToinvsType=snapshotMetadataType.getOusToinvs();
				OuInvMapType[] ouInvMapTypeArray=ousToinvsType.getOuInvMapArray();
				valuesList=new ArrayList<List<String>>();
				for (OuInvMapType ouInvMapType:ouInvMapTypeArray) {
					InvType[] invTypeArray=ouInvMapType.getInvArray();
					for (InvType invType:invTypeArray) {
						List<String> values=new ArrayList<String>();
						valuesList.add(values);
						values.add(""+ouInvMapType.getId());
						values.add(""+invType.getId());
						values.add(invType.getName());
					}
				}
				insertMetadata(connection,snapshotId,IMPORT_METADATA_TYPE_OU_TO_INV,valuesList);
				
				OusToledgersType ousToledgersType=snapshotMetadataType.getOusToledgers();
				OuToledgerMapType[] ouToledgerMapTypeArray=ousToledgersType.getOuToledgerMapArray();
				valuesList=new ArrayList<List<String>>();
				for (OuToledgerMapType ouToledgerMapType:ouToledgerMapTypeArray) {
					LedgerType[] ledgerTypeArray=ouToledgerMapType.getLedgerArray();
					for (LedgerType ledgerType:ledgerTypeArray) {
						List<String> values=new ArrayList<String>();
						valuesList.add(values);
						values.add(""+ouToledgerMapType.getId());
						values.add(""+ledgerType.getId());
						values.add(ledgerType.getName());
					}
				}
				insertMetadata(connection,snapshotId,IMPORT_METADATA_TYPE_OU_TO_LEDGER,valuesList);
				
				OusTolegentsType ousTolegentsType=snapshotMetadataType.getOusTolegents();
				OuTolegentMapType[] ouTolegentMapTypeArray=ousTolegentsType.getOuTolegentMapArray();
				valuesList=new ArrayList<List<String>>();
				for (OuTolegentMapType ouTolegentMapType:ouTolegentMapTypeArray) {
					LegalEntityType[] legalEntityTypeArray=ouTolegentMapType.getLegalEntityArray();
					for (LegalEntityType legalEntityType:legalEntityTypeArray) {
						List<String> values=new ArrayList<String>();
						valuesList.add(values);
						values.add(""+ouTolegentMapType.getId());
						values.add(""+legalEntityType.getId());
						values.add(legalEntityType.getName());
					}
				}
				insertMetadata(connection,snapshotId,IMPORT_METADATA_TYPE_OU_TO_LEGAL_ENTITY,valuesList);
				
				OusToBGsType ousToBGsType=snapshotMetadataType.getOusToBGs();
				OuBGMapType[] ouBGMapTypeArray=ousToBGsType.getOuBGMapArray();
				valuesList=new ArrayList<List<String>>();
				for (OuBGMapType ouBGMapType:ouBGMapTypeArray) {
					BgType[] bgTypeArray=ouBGMapType.getBgArray();
					for (BgType bgType:bgTypeArray) {
						List<String> values=new ArrayList<String>();
						valuesList.add(values);
						values.add(""+ouBGMapType.getId());
						values.add(""+bgType.getId());
						values.add(bgType.getName());
					}
				}
				insertMetadata(connection,snapshotId,IMPORT_METADATA_TYPE_OU_TO_BG,valuesList);
				
				
				recordsToImportList=new ArrayList<SnapshotInventoryGridRecord>();
				for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsList) {
					if (! (snapshotInventoryGridRecord.getStatus().equalsIgnoreCase("S") ||
							snapshotInventoryGridRecord.getStatus().equalsIgnoreCase("W"))
							) {
						continue;
					}
					recordsToImportList.add(snapshotInventoryGridRecord);
				}

				tableIds=ModelUtils.getTableIds(connection);
			}
			catch(Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage("Error: "+e.getMessage());
				if (connection!=null) {
					try {
						connection.rollback();
					} catch (SQLException e1) {
						FileUtils.printStackTrace(e1);
					}
				}
				return;
			}
			finally {
				DirectConnectDao.closeQuietly(connection);	  
			}

			SnapshotImportController snapshotImportController=new SnapshotImportController(tableIds,
					tempRootFolder,tabSnapshotsPanel,recordsToImportList,snapshotGridRecord);
			snapshotImportController.startExecution();

			while ( !snapshotImportController.isExecutionCompleted()) {
				Thread.sleep(2000);
			}
			SnapshotController snapshotController=new SnapshotController(tabSnapshotsPanel,false);
			snapshotController.startExecution();
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
		finally{
			FileUtils.deleteDirectory(tempUnpackedFolder);
		}
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public static SnapshotMetadataDocument validateSnapshotMetadataDocument(File xmlFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = InjectUtils.getXmlOptions(validationErrors);
		SnapshotMetadataDocument snapshotMetadataDocument =SnapshotMetadataDocument.Factory.parse(xmlFile,xmlOptions);
		InjectUtils.validateXMLBeanDocument(snapshotMetadataDocument,xmlOptions,validationErrors);
		return snapshotMetadataDocument;
	}

	public Set<Integer> getTableIds() {
		return tableIds;
	}
	
	private static void applyImportPatch(Connection connection,String dbSchema) throws Exception {
		boolean hasTable=ModelUtils.hasTable(dbSchema, connection, "IMPORT_METADATA");
		FileUtils.println("applyImportPatch, hasTable:"+hasTable);
		if (!hasTable) {
			applyPatchAddImportMetadata(connection);
		}
	}

	private static void applyPatchAddImportMetadata(Connection connection) throws Exception {
		String msg="Applying patch 'IMPORT_METADATA'...";
		FileUtils.println(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;
		try
		{	
			sql="CREATE TABLE IMPORT_METADATA "+
				"( "+
				" SNAPSHOT_ID NUMBER NOT NULL, "+
				" TYPE	  Varchar(100) Not Null, "+
				" COL1	  Varchar(4000), "+
				" COL2	  Varchar(4000), "+
				" COL3	  Varchar(4000), "+
				" COL4	  Varchar(4000), "+
				" COL5	  Varchar(4000) "+
				//" PRIMARY KEY (SNAPSHOT_ID,Type,COL1,COL2,COL3,COL4,COL5)"+
				")";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}

		try
		{
			sql="CREATE INDEX IMPORT_METADATA_IDX on IMPORT_METADATA (SNAPSHOT_ID,TYPE)";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();

			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
		FileUtils.println("Patch 'IMPORT_METADATA' applied.");
	}
	
	private static void insertMetadata(Connection connection,int snapshotId,String type,List<List<String>> valuesList) throws Exception {
		PreparedStatement preparedStatement = null;
		try
		{
			String sql ="insert into IMPORT_METADATA(SNAPSHOT_ID,TYPE,COL1,COL2,COL3,COL4,COL5) values(?,?,?,?,?,?,?)";
			preparedStatement= connection.prepareStatement(sql);
			for (List<String> values:valuesList) {
				int index=1;
				preparedStatement.setInt(index++, snapshotId);
				preparedStatement.setString(index++, type);
				for (int i=0;i<5;i++) {
					String value=null;
					if ( i < values.size() ) {
						value=values.get(i);
					}
					preparedStatement.setString(index++, value);
				}			
				preparedStatement.addBatch(); 
			}
			preparedStatement.executeBatch();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

}