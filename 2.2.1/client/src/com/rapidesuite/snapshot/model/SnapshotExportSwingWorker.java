package com.rapidesuite.snapshot.model;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.xmlbeans.XmlOptions;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
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
import com.rapidesuite.extract.view.UIConstants;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;

public class SnapshotExportSwingWorker extends SnapshotSwingWorker {

	private TabSnapshotsPanel tabSnapshotsPanel;
	private Map<String,String> snapshotEnvironmentProperties;
	private List<SnapshotInventoryGridRecord> exportInventoryGridRecordList;
	private SnapshotGridRecord snapshotGridRecord;
	private File downloadFolder;
	public static final String METADATA_FILE_NAME="metadata.xml";
	
	public SnapshotExportSwingWorker(TabSnapshotsPanel tabSnapshotsPanel, SnapshotGridRecord snapshotGridRecord, File downloadFolder) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.snapshotGridRecord=snapshotGridRecord;
		this.downloadFolder=downloadFolder;
		snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processActionGetInventoriesList();
		return null;
	}
	
	protected void processActionGetInventoriesList() {
		Connection connection=null;
		try {
			String dbUserName=ModelUtils.getDBUserName(snapshotEnvironmentProperties);
			
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					dbUserName,
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));

			super.updateExecutionLabels("Retrieving Metadata...");
					
			List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList= ModelUtils.getSnapshotInventoryGridRecordsList(
					connection,snapshotGridRecord.getSnapshotId(),null);
			exportInventoryGridRecordList=new ArrayList<SnapshotInventoryGridRecord>();
			for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
				String uiStatus=ModelUtils.getUIStatusFromDBStatus(snapshotInventoryGridRecord.getStatus());
				if (uiStatus!=null && (uiStatus.equalsIgnoreCase(UIConstants.UI_STATUS_COMPLETED)||
						uiStatus.equalsIgnoreCase(UIConstants.UI_STATUS_WARNING)
						)) {
					exportInventoryGridRecordList.add(snapshotInventoryGridRecord);
				}
			}
			
			SnapshotMetadataDocument snapshotMetadataDocument=SnapshotMetadataDocument.Factory.newInstance();
			SnapshotMetadataType snapshotMetadataType=snapshotMetadataDocument.addNewSnapshotMetadata();
			SnapshotInformationType snapshotInformationType=snapshotMetadataType.addNewSnapshotInformation();
			snapshotInformationType.setDescription(snapshotGridRecord.getDescription());
			snapshotInformationType.setName(
					StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(snapshotGridRecord.getName())));
			snapshotInformationType.setCompletedOn(snapshotGridRecord.getCompletedOn());
			snapshotInformationType.setIsConversion(snapshotGridRecord.isConversion());
			snapshotInformationType.setStatus(snapshotGridRecord.getStatus());
			snapshotInformationType.setClientHostName(snapshotGridRecord.getClientHostName());
			snapshotInformationType.setOsUserName(snapshotGridRecord.getOsUserName());
			snapshotInformationType.setCreatedOn(snapshotGridRecord.getCreatedOn());
			snapshotInformationType.setTemplateName(snapshotGridRecord.getTemplateName());
			
			// export all inventories statuses into a xml file as we need to show even failed inventories or
			// unprocessed ones.
			InventoriesType inventoriesType=snapshotMetadataType.addNewInventories();
			for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
				InventoryType inventoryType=inventoriesType.addNewInventory();
				inventoryType.setName(
						StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(snapshotInventoryGridRecord.getInventoryName())));
				inventoryType.setRemarks(
						StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(snapshotInventoryGridRecord.getRemarks())));
				inventoryType.setStatus(snapshotInventoryGridRecord.getStatus());
				inventoryType.setTotalRecords(snapshotInventoryGridRecord.getTotalRecords());
				inventoryType.setRawTimeInSecs(snapshotInventoryGridRecord.getRawTimeInSecs());
				inventoryType.setExecutionTime(snapshotInventoryGridRecord.getExecutionTime());
				inventoryType.setCreatedOn(snapshotInventoryGridRecord.getCreatedOn());
				inventoryType.setCompletedOn(snapshotInventoryGridRecord.getCompletedOn());				
			}
			
			OusType ousType=snapshotMetadataType.addNewOus();
			List<OperatingUnit> operatingUnitsList=ModelUtils.getOperatingUnitsList(connection);
			for (int i = 0; i < operatingUnitsList.size(); i++) {
				OperatingUnit operatingUnit = operatingUnitsList.get(i);
				OuType ouType=ousType.addNewOu();
				ouType.setId(operatingUnit.getId());
				ouType.setName(StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(operatingUnit.getName())));
			}
			
			Map<Long, List<InventoryOrganization>> operatingUnitIdToInventoryOrganizationsList = 
					ModelUtils.getOperatingUnitIdToInventoryOrganizationsList(connection);
			Iterator<Long> iterator=operatingUnitIdToInventoryOrganizationsList.keySet().iterator();
			OusToinvsType ousToinvsType=snapshotMetadataType.addNewOusToinvs();
			while (iterator.hasNext()) {
				Long key=iterator.next();
				OuInvMapType ouInvMapType=ousToinvsType.addNewOuInvMap();
				ouInvMapType.setId(key);
				List<InventoryOrganization> list=operatingUnitIdToInventoryOrganizationsList.get(key);
				for (InventoryOrganization obj:list) {
					InvType invType=ouInvMapType.addNewInv();
					invType.setId(obj.getId());
					invType.setName(
							StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(obj.getName())));
				}
			}
			
			Map<Long, List<BusinessGroup>> operatingUnitIdToBusinessGroupList = 
					ModelUtils.getOperatingUnitIdToBusinessGroupList(connection);
			iterator=operatingUnitIdToBusinessGroupList.keySet().iterator();
			OusToBGsType ousToBGsType=snapshotMetadataType.addNewOusToBGs();
			while (iterator.hasNext()) {
				Long key=iterator.next();
				OuBGMapType ouBGMapType=ousToBGsType.addNewOuBGMap();
				ouBGMapType.setId(key);
				List<BusinessGroup> list=operatingUnitIdToBusinessGroupList.get(key);
				for (BusinessGroup obj:list) {
					BgType bgType=ouBGMapType.addNewBg();
					bgType.setId(obj.getId());
					bgType.setName(
							StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(obj.getName())));
				}
			}			
			
			Map<Long, List<Ledger>> operatingUnitToLedgersMap = ModelUtils.getOperatingUnitToLedgersMap(connection);
			iterator=operatingUnitToLedgersMap.keySet().iterator();
			OusToledgersType ousToledgersType=snapshotMetadataType.addNewOusToledgers();
			while (iterator.hasNext()) {
				Long key=iterator.next();
				OuToledgerMapType ouToledgerMapType=ousToledgersType.addNewOuToledgerMap();
				ouToledgerMapType.setId(key);
				List<Ledger> list=operatingUnitToLedgersMap.get(key);
				for (Ledger obj:list) {
					LedgerType ledgerType=ouToledgerMapType.addNewLedger();
					ledgerType.setId(obj.getId());
					ledgerType.setName(
							StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(obj.getName())));
				}
			}			
			
			Map<Long, List<LegalEntity>> operatingUnitToLegalEntityMap = ModelUtils.getOperatingUnitIdToLegalEntityList(connection);
			iterator=operatingUnitToLegalEntityMap.keySet().iterator();
			OusTolegentsType ousTolegentsType=snapshotMetadataType.addNewOusTolegents();
			while (iterator.hasNext()) {
				Long key=iterator.next();
				OuTolegentMapType ouTolegentMapType=ousTolegentsType.addNewOuTolegentMap();
				ouTolegentMapType.setId(key);
				List<LegalEntity> list=operatingUnitToLegalEntityMap.get(key);
				for (LegalEntity obj:list) {
					LegalEntityType legalEntityType=ouTolegentMapType.addNewLegalEntity();
					legalEntityType.setId(obj.getId());
					legalEntityType.setName(
							StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(obj.getName())));
				}
			}
			
			Map<Integer, String> oracleSeededUserIdToUserNameMap=ModelUtils.getOracleUserIdToUserNameMap(connection,null,false,null,null,null);
			Iterator<Integer> iteratorUser=oracleSeededUserIdToUserNameMap.keySet().iterator();
			UsersType usersType=snapshotMetadataType.addNewUsers();
			while (iteratorUser.hasNext()) {
				Integer key=iteratorUser.next();
				UserType userType=usersType.addNewUser();
				userType.setId(key);
				String userName=oracleSeededUserIdToUserNameMap.get(key);
				userType.setName(
						StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(userName)));
			}			
			/*
			String xml=CoreUtil.getXMLFromXMLBean(fusionDataDocument);
			CoreUtil.writeToFile(xml, false, scriptGenerationFolder,"dataLinkage.xml");	
			*/
			XmlOptions xmlOptions = new XmlOptions();
			xmlOptions.setSavePrettyPrint();
			File metadataFile=new File(downloadFolder,METADATA_FILE_NAME);
			snapshotMetadataDocument.save(metadataFile,xmlOptions);			
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
		finally {
			DirectConnectDao.closeQuietly(connection);	  
		}
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public List<SnapshotInventoryGridRecord> getSnapshotInventoryGridRecordList() {
		return exportInventoryGridRecordList;
	}
	
}