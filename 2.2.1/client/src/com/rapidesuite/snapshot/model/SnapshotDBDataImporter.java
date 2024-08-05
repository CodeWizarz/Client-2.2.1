package com.rapidesuite.snapshot.model;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.controller.SnapshotImportController;

public class SnapshotDBDataImporter extends SnapshotXMLDataImporter{

	private SnapshotImportWorker snapshotImportWorker;
	private int sequenceNum;
	private boolean isDFFConversionType;

	public SnapshotDBDataImporter(SnapshotImportWorker snapshotImportWorker, boolean isDFFConversionType, int batchSize) {
		super(batchSize);
		this.snapshotImportWorker=snapshotImportWorker;
		this.isDFFConversionType=isDFFConversionType;
		sequenceNum=0;
	}

	@Override
	public void processBatch(List<String[]> tempList) throws Exception {
		snapshotImportWorker.getSnapshotInventoryGridRecord().setDownloadRemarks("DB Import ( batch size: "+
				Utils.formatNumberWithComma(Config.getSnapshotImportBatchSize())+" records )");

		if (snapshotImportWorker.getGenericController().isCancelled()) {
			return;
		}
		PreparedStatement preparedStatement = null;
		try
		{
			//FileUtils.println("buildSQLQuery: "+snapshotImportWorker.getSnapshotInventoryGridRecord().getInventoryName());
			StringBuffer sqlBuffer=buildSQLQuery();
			//FileUtils.println("sqlBuffer: "+sqlBuffer.toString());

			preparedStatement= snapshotImportWorker.getJDBCConnection().prepareStatement(sqlBuffer.toString());
			List<String> fieldNamesUsedForDataEntry=snapshotImportWorker.getSnapshotInventoryGridRecord().getInventory().getFieldNamesUsedForDataEntry();

			for (String[] values:tempList) {
				int parametersIndex=1;
				String rscOUId = "";
				String rscBGId = "";
				String rscInvOrgId = "";
				String rscLedgerId = "";
				String rscCOAId = "";				
				sequenceNum++;
				//FileUtils.println(parametersIndex+" : (seq) int : "+sequenceNum);

				preparedStatement.setInt(parametersIndex++,sequenceNum);	
				
				int fieldIndex=0;
				//int printIndex=1;
				for (@SuppressWarnings("unused") String fieldName:fieldNamesUsedForDataEntry) {
					String value=values[fieldIndex++];
					//FileUtils.println((printIndex++)+"/parametersIndex:"+parametersIndex+" : (data) "+value);
					preparedStatement.setString(parametersIndex++,value);
				}
				/* 
				<c>RSC Data Label</c>
				<c>Navigation Filter</c>
				<c>RSC last updated by name</c>
				<c>RSC last update date</c>
				<c>RSC created by name</c>
				<c>RSC creation date</c>
				<c>RSC OU ID</c>
				<c>RSC BG ID</c>
				<c>RSC INVORG ID</c>
				<c>RSC LEDGER ID</c>
				<c>RSC COA ID</c>
				 */
				fieldIndex++;
				if (isDFFConversionType) {
					String navMapper=values[fieldIndex];
					String[] splitText=navMapper.split(ModelUtils.REPLACEMENT_SEPARATOR);
					//FileUtils.println("navMapper: "+navMapper+" splitText:"+splitText+" splitText.length:"+splitText.length);
					//printIndex=1;
					for (String val:splitText) {
						//FileUtils.println((printIndex++)+"/parametersIndex:"+parametersIndex+" : (dff) "+val);
						preparedStatement.setString(parametersIndex++,val);
					}
				}
				fieldIndex++;
				//printIndex=1;
				String rscLastUpdateName=values[fieldIndex++];
				//FileUtils.println(printIndex+"/parametersIndex:"+parametersIndex+" : (rscLastUpdateName) "+rscLastUpdateName);
				preparedStatement.setString(parametersIndex++,rscLastUpdateName);

				String rscLastUpdateDate=values[fieldIndex++];
				Timestamp timeStamp=null;
				if (rscLastUpdateDate!=null && !rscLastUpdateDate.isEmpty() && !rscLastUpdateDate.equalsIgnoreCase("null")){
					timeStamp=ModelUtils.getTimeStamp(rscLastUpdateDate);
				}
				//FileUtils.println(parametersIndex+" : (rscLastUpdateDate) "+rscLastUpdateDate);
				preparedStatement.setTimestamp(parametersIndex++,timeStamp);

				String rscCreationName=values[fieldIndex++];
				//FileUtils.println(parametersIndex+" : (rscCreationName) "+rscCreationName);
				preparedStatement.setString(parametersIndex++,rscCreationName);

				String rscCreationDate=values[fieldIndex++];
				timeStamp=null;
				if (rscCreationDate!=null && !rscCreationDate.isEmpty() && !rscCreationDate.equalsIgnoreCase("null")){
					timeStamp=ModelUtils.getTimeStamp(rscCreationDate);
				}
				//FileUtils.println(parametersIndex+" : (rscCreationDate) "+rscCreationDate);
				preparedStatement.setTimestamp(parametersIndex++,timeStamp);

				rscOUId=values[fieldIndex++];
				//FileUtils.println(parametersIndex+" : (rscOUId) "+rscOUId);
				if(rscOUId.length()>0){
					preparedStatement.setInt(parametersIndex++,getInteger(rscOUId));
				}else{
					preparedStatement.setString(parametersIndex++,null);
				}
				
				rscBGId=values[fieldIndex++];
				//FileUtils.println(parametersIndex+" : (rscBGId) "+rscBGId);
				if(rscBGId.length()>0){
					preparedStatement.setInt(parametersIndex++,getInteger(rscBGId));
				}else{
					preparedStatement.setString(parametersIndex++,null);
				}				

				rscInvOrgId=values[fieldIndex++];
				//FileUtils.println(parametersIndex+" : (rscInvOrgId) "+rscInvOrgId);
				if(rscInvOrgId.length()>0){
					preparedStatement.setInt(parametersIndex++,getInteger(rscInvOrgId));
				}else{
					preparedStatement.setString(parametersIndex++,null);
				}

				rscLedgerId=values[fieldIndex++];
				//FileUtils.println(parametersIndex+" : (rscLedgerId) "+rscLedgerId);
				if(rscLedgerId.length()>0){
					preparedStatement.setInt(parametersIndex++,getInteger(rscLedgerId));
				}else{
					preparedStatement.setString(parametersIndex++,null);
				}

				rscCOAId=values[fieldIndex++];
				//FileUtils.println(parametersIndex+" : (rscCOAId) "+rscCOAId);
				if(rscCOAId.length()>0){
					preparedStatement.setInt(parametersIndex++,getInteger(rscCOAId));
				}else{
					preparedStatement.setString(parametersIndex++,null);
				}


				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
			snapshotImportWorker.getJDBCConnection().commit();
			snapshotImportWorker.getSnapshotInventoryGridRecord().setDownloadDownloadedRecordsCount(super.getRowCount());
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	private Integer getInteger(String val) {
		try{
			return Integer.valueOf(val);
		}
		catch(NumberFormatException e) {
			return new Integer(-777);
		}
	}
	
	@SuppressWarnings("unused")
	private StringBuffer buildSQLQuery() {
		String preColumnsText="SEQ,SNAPSHOT_ID,";
		String postColumnsText="rsc_last_updated_by,rsc_last_update_date,rsc_created_by,"+
				"rsc_creation_date,rsc_ou_id,rsc_bg_id,rsc_inv_org_id,rsc_ledger_id,RSC_COA_ID";

		StringBuffer sqlBuffer=new StringBuffer("");

		int snapshotId=snapshotImportWorker.getSnapshotImportController().getSnapshotGridRecord().getSnapshotId();

		sqlBuffer.append("INSERT INTO ").append(ModelUtils.DB_TABLES_PREFIX).append(
				snapshotImportWorker.getSnapshotInventoryGridRecord().getTableId());
		sqlBuffer.append("(").append(preColumnsText);
		List<String> fieldNamesUsedForDataEntry=snapshotImportWorker.getSnapshotInventoryGridRecord().getInventory().getFieldNamesUsedForDataEntry();
		int fieldIndex=1;
		for (String fieldName:fieldNamesUsedForDataEntry) {
			sqlBuffer.append("C").append(fieldIndex).append(",");
			fieldIndex++;
		}
		if (isDFFConversionType) {
			for (int i=1;i<=ModelUtils.DFF_FIELD_COUNT;i++) {
				sqlBuffer.append(ModelUtils.DFF_FIELD_NAME).append(i).append(",");
			}
		}
		sqlBuffer.append(postColumnsText).append(")");

		sqlBuffer.append(" values (");
		sqlBuffer.append("?,");
		sqlBuffer.append(snapshotId).append(",");
		//FileUtils.println("field names ?: ");
		int index=1;
		for (String fieldName:fieldNamesUsedForDataEntry) {
			sqlBuffer.append("?,");
			//FileUtils.println((index++)+": ?,");
		}
		/* 
			<c>RSC Data Label</c>
			<c>Navigation Filter</c>
			<c>RSC last updated by name</c>
			<c>RSC last update date</c>
			<c>RSC created by name</c>
			<c>RSC creation date</c>
			<c>RSC OU ID</c>
			<c>RSC BG ID</c>
			<c>RSC INVORG ID</c>
			<c>RSC LEDGER ID</c>
			<c>RSC COA ID</c>
		 */
		if (isDFFConversionType) {
			index=1;
			//FileUtils.println("dff names ?: ");
			for (int i=1;i<=ModelUtils.DFF_FIELD_COUNT;i++) {
				sqlBuffer.append("?,");
				//FileUtils.println((index++)+": ?,");
			}
		}
		//FileUtils.println("extra ?: 9 columns");
		sqlBuffer.append("?,");
		sqlBuffer.append("?,");
		sqlBuffer.append("?,");
		sqlBuffer.append("?,");
		sqlBuffer.append("?,");
		sqlBuffer.append("?,");
		sqlBuffer.append("?,");
		sqlBuffer.append("?,");
		sqlBuffer.append("?)");
		
		return sqlBuffer;
	}

}