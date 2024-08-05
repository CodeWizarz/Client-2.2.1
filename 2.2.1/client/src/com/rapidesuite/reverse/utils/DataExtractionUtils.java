/**************************************************
 * $Revision: 54560 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-04-21 13:45:36 +0700 (Thu, 21 Apr 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/utils/DataExtractionUtils.java $:
 * $Id: DataExtractionUtils.java 54560 2016-04-21 06:45:36Z jannarong.wadthong $:
 */

package com.rapidesuite.reverse.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.swing.JOptionPane;

import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.reverse.DataExtractionBusinessGroupLevelJob;
import com.rapidesuite.reverse.DataExtractionConstants;
import com.rapidesuite.reverse.DataExtractionJob;
import com.rapidesuite.reverse.DataExtractionJobManager;
import com.rapidesuite.reverse.DataExtractionOperatingUnitLevelJob;
import com.rapidesuite.reverse.ReverseMain;
import com.rapidesuite.reverse.gui.DataExtractionPanel;
import com.rapidesuite.reverse.gui.DataExtractionStatusPanel;
import com.rapidesuite.reverse.gui.DataExtractionStatusTreeTableNode;

public class DataExtractionUtils
{

	public static boolean isContinueReversing()
	{
		int n = JOptionPane.showConfirmDialog(null,
				"Warning, no operating unit name selected, do you wish to continue?",
				"Warning",
				JOptionPane.YES_NO_OPTION);
		if ( n==JOptionPane.YES_OPTION )
		{
			FileUtils.println("wish to continue");
			return true;
		}
		return false;
	}

	public static String replaceTokens(Properties replacementsProperties,String source)
	{
		if ( replacementsProperties==null ) {
			return source;
		}

		Iterator<Object> iterator=replacementsProperties.keySet().iterator();
		String res=source;
		while ( iterator.hasNext() )
		{
			String key=(String) iterator.next();
			String value=(String)replacementsProperties.get(key);
			if ( value!=null ) {
				value=value.trim();
			}

			res=replaceToken(res,key,value);
		}
		return res;
	}

	public static String replaceToken(String text,String source,String target)
	{
		String newTarget=Matcher.quoteReplacement(target);
		return text.replaceAll(UtilsConstants.REPLACEMENTS_DELIMITER+source+
				UtilsConstants.REPLACEMENTS_DELIMITER,newTarget);
	}

	public static String getTextAfterKeyword(String text,String keyword)
	{
		int startIndex=text.indexOf(keyword);
		if (startIndex==-1) {
			return null;
		}
		String startTokens=text.substring(startIndex+keyword.length()+1);
		int endIndex=startTokens.indexOf("\n");
		if (endIndex==-1) {
			return null;
		}
		return startTokens.substring(0,endIndex);
	}

	public static Properties getReplacementsPropertiesClone(Properties replacementsPropertiesParameter)
	{
		Properties replacementsProperties=null;
		if (replacementsPropertiesParameter==null) {
			replacementsProperties=new Properties();
		}
		else {
			replacementsProperties=(Properties)replacementsPropertiesParameter.clone();
		}
		return replacementsProperties;
	}


	public static String addUserFiltering(DataExtractionJob reverseExecutionJob,Properties replacementsProperties, String sqlQuery)
	throws Exception
	{
		return DataExtractionDatabaseUtils.addSelectedUserIdsToQuery(reverseExecutionJob.getReverseExecutionJobManager().isIncludeSelectedUsers(),
				reverseExecutionJob.getReverseExecutionJobManager().getSelectedUserIds(),replacementsProperties, sqlQuery,
				reverseExecutionJob.getReverseExecutionJobManager().getAllOracleUserIdToNameSynchronizedMap());
	}

	public static void addEBSProperties(DataExtractionJob job,Properties replacementsProperties)
	throws Exception
	{
		if (job instanceof DataExtractionBusinessGroupLevelJob) {
			DataExtractionBusinessGroupLevelJob bgJob=(DataExtractionBusinessGroupLevelJob)job;
			replacementsProperties.setProperty(UtilsConstants.BUSINESS_GROUP_NAME_PROPERTY,bgJob.getBusinessGroupName());
			replacementsProperties.setProperty(UtilsConstants.BUSINESS_GROUP_ID_PROPERTY,String.valueOf(bgJob.getBusinessGroupId()));
		}
		else
		if (job instanceof DataExtractionOperatingUnitLevelJob) {
			DataExtractionOperatingUnitLevelJob ouJob=(DataExtractionOperatingUnitLevelJob)job;
			replacementsProperties.setProperty(UtilsConstants.BUSINESS_GROUP_NAME_PROPERTY,ouJob.getBusinessGroupName());
			replacementsProperties.setProperty(UtilsConstants.BUSINESS_GROUP_ID_PROPERTY,String.valueOf(ouJob.getBusinessGroupId()));
			replacementsProperties.setProperty(UtilsConstants.OPERATING_UNIT_NAME_PROPERTY,ouJob.getOperatingUnitName());
			replacementsProperties.setProperty(UtilsConstants.OPERATING_UNIT_ID_PROPERTY,String.valueOf(ouJob.getOperatingUnitId()));
			replacementsProperties.setProperty(UtilsConstants.BUSINESS_UNIT_NAME_PROPERTY,ouJob.getOperatingUnitName());
			replacementsProperties.setProperty(UtilsConstants.BUSINESS_UNIT_ID_PROPERTY,String.valueOf(ouJob.getOperatingUnitId()));
			

		}
	}

	public static void addDateFiltering(DataExtractionJob reverseExecutionJob,Properties replacementsProperties)
	throws Exception
	{
		String formattedFromDate=reverseExecutionJob.getReverseExecutionJobManager().getFormattedFromDate();
		String formattedToDate=reverseExecutionJob.getReverseExecutionJobManager().getFormattedToDate();
		boolean isFromDateEnabled=((ReverseMain)reverseExecutionJob.getReverseExecutionJobManager().getSwiftGUIMain()).getDataExtractionPanel().getOracleDatesSelectionPanel().isFromDateEnabled();
		boolean isToDateEnabled=((ReverseMain)reverseExecutionJob.getReverseExecutionJobManager().getSwiftGUIMain()).getDataExtractionPanel().getOracleDatesSelectionPanel().isToDateEnabled();


		if ( !DataExtractionDatabaseUtils.hasOracleDateCondition(formattedFromDate) &&
			 !DataExtractionDatabaseUtils.hasOracleDateCondition(formattedToDate)) {
			throw new Exception("At least the 'From' or 'To' Date must be specified in the Date filtering panel.");
		}

		if ( DataExtractionDatabaseUtils.hasOracleDateCondition(formattedFromDate) && isFromDateEnabled &&
			 DataExtractionDatabaseUtils.hasOracleDateCondition(formattedToDate) && isToDateEnabled
			 )
		{
			String fromDateTime=getDateTime(formattedFromDate);
			replacementsProperties.setProperty(DataExtractionConstants.RSC_LAST_UPDATE_DATE_OPERATOR,"BETWEEN to_date('"+fromDateTime+"') AND ");
			replacementsProperties.setProperty(DataExtractionConstants.SEEDED_DATE,getDateTime(formattedToDate));
			return;
		}

		if ( DataExtractionDatabaseUtils.hasOracleDateCondition(formattedFromDate) && isFromDateEnabled) {
			replacementsProperties.setProperty(DataExtractionConstants.RSC_LAST_UPDATE_DATE_OPERATOR,">=");
			replacementsProperties.setProperty(DataExtractionConstants.SEEDED_DATE,getDateTime(formattedFromDate));
		}
		else
		if ( DataExtractionDatabaseUtils.hasOracleDateCondition(formattedToDate) && isToDateEnabled) {
			replacementsProperties.setProperty(DataExtractionConstants.RSC_LAST_UPDATE_DATE_OPERATOR,"<=");
			replacementsProperties.setProperty(DataExtractionConstants.SEEDED_DATE,getDateTime(formattedToDate));
		}
	}

	private static String getDateTime(String dateValue)
	{
		if (dateValue.indexOf(":")!=-1) {
			return dateValue+"','"+UtilsConstants.ORACLE_DATE_TIME_FORMAT;
		}
		return dateValue;
	}

	public static boolean validatePrerequisiteObjects(DataExtractionJob reverseExecutionJob,String content)
	throws Exception
	{
		if (DataExtractionDatabaseUtils.hasRSCPrerequisiteObjects(reverseExecutionJob.getReverseExecutionJobManager(),content)) {
			boolean isFound=DataExtractionDatabaseUtils.
			verifyPrerequisiteObjects(reverseExecutionJob.getReverseExecutionJobManager(),content,reverseExecutionJob.getBweProperties());
			if (!isFound) {
				reverseExecutionJob.getExecutionJobManager().updateExecutionStatus(reverseExecutionJob,"WARNING: no records found.");
				reverseExecutionJob.setJobComplete(false);
				return false;
			}
		}
		return true;
	}

	public static boolean hasAuditColumns(String sqlQuery,String plsqlPackageName, boolean packageHasAuditColumns)
	throws Exception
	{
		return sqlQuery.toLowerCase().indexOf(DataExtractionConstants.RSC_LAST_UPDATED_BY_COLUMN_NAME.toLowerCase())!=-1 ||
		(packageHasAuditColumns && sqlQuery.toLowerCase().indexOf(plsqlPackageName+".GET_ALL_ORGANIZATION".toLowerCase())!=-1);
	}


	public static void validateReverseQueryWithInventoryColumns(
			List<Field> inventoryDataOnlyFields,String sqlQuery,int numberOfColumnsInTheSQLQuery,String plsqlPackageName,boolean isRestartAtRecord,
			boolean packageSqlContainsAuditColumn)
	throws Exception
	{
		int inventoryFieldsCount=inventoryDataOnlyFields.size();
		int numberOfApplicableColumnsInTheSQLQuery=numberOfColumnsInTheSQLQuery-1; // nav mapper
		if (hasAuditColumns(sqlQuery, plsqlPackageName,packageSqlContainsAuditColumn)) {
			 numberOfApplicableColumnsInTheSQLQuery=numberOfApplicableColumnsInTheSQLQuery-2; // audit info.
		}
		if (isRestartAtRecord) {
			numberOfApplicableColumnsInTheSQLQuery=numberOfApplicableColumnsInTheSQLQuery-1; // rownum column at the end of the query
		}
		//System.out.println("inventoryFieldsCount:"+inventoryFieldsCount+" numberOfApplicableColumnsInTheSQLQuery:"+numberOfApplicableColumnsInTheSQLQuery+"" +
		//		" hasAuditColumns:"+hasAuditColumns(sqlQuery));
		if ( numberOfApplicableColumnsInTheSQLQuery!=inventoryFieldsCount) {
			throw new Exception("Mismatch between number of columns returned "+
					"by the SQL query ("+ numberOfApplicableColumnsInTheSQLQuery+") and the inventory ("+
					inventoryFieldsCount+").\n Inventory columns: "+getInventoryDataOnlyFieldNames(inventoryDataOnlyFields) );
		}
	}

	public static List<String> getInventoryDataOnlyFieldNames(List<Field> inventoryDataOnlyFields)
	{
		List<String> toReturn=new ArrayList<String>();
		for (Field field: inventoryDataOnlyFields) {
			toReturn.add(field.getName());
		}
		return toReturn;
	}

	public static boolean skipUntilLastReadRecord(
			Job worker,int recordsCount,int lastReadRow,int expectedRecordCountToReverse)
	throws Exception
	{
		if (recordsCount<=lastReadRow) {
			int currentRecordBatchIndex=recordsCount%50;
			if ( currentRecordBatchIndex==0 )
			{
				worker.getExecutionJobManager().updateExecutionStatus(worker,
						"PROCESSING: skipping until record "+
						Utils.formatNumberWithComma(lastReadRow)+"/"+
				Utils.formatNumberWithComma(expectedRecordCountToReverse)+" ("+
				Utils.formatNumberWithComma(recordsCount)+")");
			}
			return true;
		}
		return false;
	}

	public static String getQueryFromLastReadRow(String sqlQuery,int lastReadRow)
	throws Exception
	{
		String toReturn="select t2.* from ( select t.*, rownum resrownum from (\n";
		toReturn+=sqlQuery+"\n";
		toReturn+=") t ) t2 where t2.resrownum > "+lastReadRow+"\n";
		return toReturn;
	}

	public static void updateTaskStatus(DataExtractionJob worker,int recordNumber)
	throws Exception
	{
		if (recordNumber==0) {
			worker.getExecutionJobManager().updateExecutionStatus(worker,"WARNING: no records found.");
		}
		else {
			((DataExtractionJobManager) worker.getExecutionJobManager()).updateSuccessfulExecutionStatus(worker, recordNumber);
		}
	}

	public static boolean isStopProcessingTask(Job worker){
		if ( worker.isManualStopped() ){
			worker.setJobComplete(true);
			worker.getExecutionJobManager().updateExecutionStatus(worker,UtilsConstants.MANUAL_STOP_MSG);
			return true;
		}
		return false;
	}

	public static boolean updateTaskStatus(Job worker,int recordNumber,int expectedRecordCountToReverse,boolean isRestartAtRecord,int restartAtRecordNumber) throws Exception{
		String msg="PROCESSING: "+Utils.formatNumberWithComma(recordNumber)+"/"+Utils.formatNumberWithComma(expectedRecordCountToReverse)+" records read.";
		if (isRestartAtRecord) {
			msg+=" (restarted from record "+Utils.formatNumberWithComma(restartAtRecordNumber)+" after connection reestablished)";
		}
		worker.getExecutionJobManager().updateExecutionStatus(worker,msg);
		return false;
	}
	
	public static void exportReverse_tree(DataExtractionPanel extractionPanel, String xlsxFileName, boolean exportOnlyError) {
		DataExtractionStatusPanel statusPanel = (DataExtractionStatusPanel) extractionPanel.getExecutionStatusPanel();
		DataExtractionStatusTreeTableNode executionStatusTreeTableNode = ((DataExtractionStatusTreeTableNode)statusPanel.getExecutionAllStatusTreeTableNode());
		try {
			if (exportOnlyError) {
				extractionPanel.setReverseTreeExportOnlyErrorFile(
						new File(Config.getTempFolder()+File.separator+"xls_ticket",
								xlsxFileName+"-"+DataExtractionFileUtils.generateDefualtDateStringForReverse_tree_export()+".xlsx"));
				statusPanel.createExecutionErrorStatusTreeTable();
				DataExtractionStatusPanel.createReverse_tree_exportFile(null, null, executionStatusTreeTableNode, true, extractionPanel.getReverseTreeExportOnlyErrorFile());
			} else {
				extractionPanel.setReverseTreeExportAllStatusesFile(
						new File(Config.getTempFolder()+File.separator+"xls_ticket",
								xlsxFileName+"-"+DataExtractionFileUtils.generateDefualtDateStringForReverse_tree_export()+".xlsx"));
				DataExtractionStatusPanel.createReverse_tree_exportFile(null, null, executionStatusTreeTableNode, true, extractionPanel.getReverseTreeExportAllStatusesFile());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			FileUtils.printStackTrace(e);
		}
	}
}