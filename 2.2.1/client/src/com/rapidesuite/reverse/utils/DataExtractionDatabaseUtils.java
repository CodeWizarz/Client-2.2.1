/**************************************************
 * $Revision: 56366 $:
 * $Author: olivier.deruelle $:
 * $Date: 2016-06-29 14:05:59 +0700 (Wed, 29 Jun 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/utils/DataExtractionDatabaseUtils.java $:
 * $Id: DataExtractionDatabaseUtils.java 56366 2016-06-29 07:05:59Z olivier.deruelle $:
 */

package com.rapidesuite.reverse.utils;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.client.SharedUtil;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.dao.util.DaoUtil;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.reverse.DataExtractionConstants;
import com.rapidesuite.reverse.DataExtractionJob;
import com.rapidesuite.reverse.DataExtractionJobManager;
import com.rapidesuite.reverse.DataExtractionOperatingUnitLevelJob;
import com.rapidesuite.reverse.ReverseMain;

public class DataExtractionDatabaseUtils
{

	public static List<DFFRow> getDFFRows(Map<String,String> environmentProperties,String sql)
	throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection=null;
		try
		{
			connection =DatabaseUtils.getDatabaseConnection(environmentProperties);

			if (Config.getReversePrintSqlToLog()) {
				FileUtils.println("Loading DFF table, executing the SQL:\n'"+sql+"'");
			}
			

			statement= connection.prepareStatement(sql);
			statement.execute();
			resultSet=statement.executeQuery();

			List<DFFRow> dffRows=new ArrayList<DFFRow>();
			while ( resultSet.next() )
			{
				DFFRow dffRow=new DFFRow(
						resultSet.getString("column_name"),
						resultSet.getString("context_code"),
						SharedUtil.getYesOrNoAsBoolean(resultSet.getString("context_code_display_flag")),
						SharedUtil.getYesOrNoAsBoolean(resultSet.getString("global_flag")),
						resultSet.getString("context_display_name"),
						resultSet.getString("value_set_query"),
						null);
				dffRows.add(dffRow);
			}
			return dffRows;
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			throw new Exception("DFF table query is invalid");
		}
		finally
		{
		    DirectConnectDao.closeQuietly(resultSet);
		    DirectConnectDao.closeQuietly(statement);
		    DirectConnectDao.closeQuietly(connection);
		}
	}

	public static Connection getDatabaseConnectionForReverseQuery(
			DataExtractionJob reverseExecutionJob,Map<String,String> environmentProperties)
	throws Exception
	{
		Connection connection=DatabaseUtils.getDatabaseConnection(environmentProperties);
		if (ReverseMain.IS_FUSION_DB) {
			DatabaseUtils.alterSession(connection);
		}
		if (reverseExecutionJob instanceof DataExtractionOperatingUnitLevelJob) {
			DataExtractionOperatingUnitLevelJob ouLevelWorker=(DataExtractionOperatingUnitLevelJob)reverseExecutionJob;
			DatabaseUtils.setClientInfo(connection, ouLevelWorker.getOperatingUnitId().intValue());
			
			DatabaseUtils.checkClientInfo(connection);
			if (  ((ReverseMain)reverseExecutionJob.getExecutionJobManager().getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().getDbEBSVersion().startsWith("12.")){
				DatabaseUtils.setPolicyContextServer(connection, ouLevelWorker.getOperatingUnitId().intValue());
			}
		}
		return connection;
	}

	public static PreparedStatement getStatementForReverseQueryConnection(
			DataExtractionJob reverseExecutionJob,Connection connection,String sqlQuery)
	throws Exception
	{
		PreparedStatement statement = connection.prepareStatement(sqlQuery);
		if (reverseExecutionJob.getFetchSize()> 0 )
		{
			statement.setFetchSize(reverseExecutionJob.getFetchSize());
		}
		return statement;
	}
	
	public static class OracleUserRetrievalExecutionMonitor {
		
		private Statement statement = null;
		private boolean isCancelled = false;
		
		public synchronized void setStatement(final Statement statement) {
			this.statement = statement;
		}
		
		public synchronized void cancel() throws SQLException {
			this.isCancelled = true;
			if (this.statement != null && !this.statement.isClosed()) {
				this.statement.cancel();
			}
		}

		public synchronized boolean isCancelled() {
			return this.isCancelled;
		}
		
		public synchronized void reset() {
			this.isCancelled = false;
			this.statement = null;
		}
		
		public static class OracleUserRetrievalIsCalcelledException extends RuntimeException {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2456176094733920160L;
			
		}
	}
	
	private static class SqlCommandAndArguments {
		private final String command;
		private final List<Object> arguments;
		
		public SqlCommandAndArguments(final String command, final List<Object> arguments) {
			this.command = command;
			this.arguments = arguments;
		}
		
		public String getCommand() {
			return command;
		}
		
		public List<Object> getArguments() {
			return arguments;
		}
	}
	
	private static SqlCommandAndArguments obtainSqlCommandAndSqlArguments(boolean isSeededOnly, final String userNameFilter, 
			Set<Integer> includeOnlyTheseUsersId, final Set<String> includeAnyUserNamesOtherThanTheseNoMatterWhoCreatedThem, 
			final Set<String> onlyIncludeTheseUserNames, final Set<String> excludeTheseUserNames) {
		
		String tableName="fnd_user";
		String createdBy="created_by";
		String userId="user_id";
		String userName="user_name";
		String whereExtraClause="";
		if (ReverseMain.IS_FUSION_DB) {
			tableName="FA_OIM.USR";
			createdBy="USR_CREATEBY";
			userId="USR_KEY";
			userName="USR_LOGIN"; 
			whereExtraClause="'FAADMIN','WEBLOGIC_IDM','OBLIXANONYMOUS','OAMADMIN','FUSION_APPS_HCM_SOA_SPML_APPID','XELSYSADM',"+
			"'XELOPERATOR','WEBLOGIC','OIMINTERNAL'";// union select -9999,'SEED_DATA_FROM_APPLICATION' from dual 
		}
		
		StringBuffer sqlBfr = new StringBuffer();
		List<Object> sqlArgsList = new ArrayList<Object>();
		
		final String basicSql = "select "+userId+","+userName+" from "+tableName;
		sqlBfr.append(basicSql);
		if (isSeededOnly) {
			if (ReverseMain.IS_FUSION_DB) {
				sqlBfr.append(" where (USR_LOGIN in (" + whereExtraClause + ") ");
			}
			else {
				sqlBfr.append(" where ("+createdBy+" in (" + Config.getCreatedByUserIdsForSeededUserCalculation() + ") ");
			}
		}
		else {
			if (ReverseMain.IS_FUSION_DB) {
				sqlBfr.append(" where (USR_LOGIN not in (" + whereExtraClause + ") ");
			}
			else {
				sqlBfr.append(" where ("+createdBy+" not in (" + Config.getCreatedByUserIdsForSeededUserCalculation() + ") ");
			}
		}
		if (includeAnyUserNamesOtherThanTheseNoMatterWhoCreatedThem != null) {
			
			if (includeAnyUserNamesOtherThanTheseNoMatterWhoCreatedThem.isEmpty()) {
				sqlBfr.append("or 1=1 ");
			} else {
				sqlBfr.append("or ");
				appendInOrNotInClausesToSqlAndTheAssociatedArgumentsList(includeAnyUserNamesOtherThanTheseNoMatterWhoCreatedThem,
						userName,
			            sqlBfr,
			            sqlArgsList,
			            false,
			            false);
			}
		}
		sqlBfr.append(") ");
		
		if (onlyIncludeTheseUserNames != null) {
			if (onlyIncludeTheseUserNames.isEmpty()) {
				sqlBfr.append(" and 1=0 ");
			} else {
				appendInOrNotInClausesToSqlAndTheAssociatedArgumentsList(onlyIncludeTheseUserNames,
						userName,
			            sqlBfr,
			            sqlArgsList,
			            true,
			            true);
			}
		}
		
		if (excludeTheseUserNames != null && !excludeTheseUserNames.isEmpty()) {
			appendInOrNotInClausesToSqlAndTheAssociatedArgumentsList(excludeTheseUserNames,
					userName,
		            sqlBfr,
		            sqlArgsList,
		            true,
		            false);
		}
		
		if (userNameFilter != null) {
			sqlBfr.append(" and lower("+userName+") like lower(?) ");
    		sqlArgsList.add(userNameFilter);
		}
		if (includeOnlyTheseUsersId != null) {
			if (includeOnlyTheseUsersId.isEmpty()) {
				sqlBfr.append(" and 0=1 ");
			} else {
				appendInOrNotInClausesToSqlAndTheAssociatedArgumentsList(includeOnlyTheseUsersId,
						userId,
			            sqlBfr,
			            sqlArgsList,
			            true,
			            true);			
			}
		}	
		
		return new SqlCommandAndArguments(sqlBfr.toString(), sqlArgsList);
	}
	
	public static int countOracleUserIdToUserNameMap(
			Map<String,String> environmentProperties,JLabel messageLabel,boolean isSeededOnly, final String userNameFilter, 
			Set<Integer> includeOnlyTheseUsersId, final OracleUserRetrievalExecutionMonitor oracleUserRetrievalExecutionMonitor, 
			final Set<String> includeAnyUserNamesOtherThanTheseNoMatterWhoCreatedThem, final Set<String> onlyIncludeTheseUserNames, 
			final Set<String> excludeTheseUserNames) throws Exception {
		
		final SqlCommandAndArguments sqlCommandAndArguments = obtainSqlCommandAndSqlArguments(isSeededOnly, userNameFilter, 
				includeOnlyTheseUsersId, includeAnyUserNamesOtherThanTheseNoMatterWhoCreatedThem, 
				onlyIncludeTheseUserNames, excludeTheseUserNames);
		
		String sql = sqlCommandAndArguments.getCommand();
				
		if (Config.getReversePrintSqlToLog()){
			FileUtils.println("Executing the SQL:\n'"+sql+"'");
		}
		if (messageLabel != null) {
			GUIUtils.showInProgressMessage(messageLabel, "Please wait, retrieving the number of users");
		}
		
		int rowsCount=DatabaseUtils.getRowsCount(environmentProperties,sql, sqlCommandAndArguments.getArguments(), oracleUserRetrievalExecutionMonitor);
		return rowsCount;
		
	}

	public static Map<Integer,String> getOracleUserIdToUserNameMap(
			Map<String,String> environmentProperties,JLabel messageLabel,boolean isSeededOnly, final String userNameFilter, 
			Set<Integer> includeOnlyTheseUsersId, final boolean hideMessageLabelBeforeEnteringSuccessMessage, 
			final OracleUserRetrievalExecutionMonitor oracleUserRetrievalExecutionMonitor, final Set<String> includeAnyUserNamesOtherThanTheseNoMatterWhoCreatedThem, 
			final Set<String> onlyIncludeTheseUserNames, final Set<String> excludeTheseUserNames)
	throws Exception
	{
		final SqlCommandAndArguments sqlCommandAndArguments = obtainSqlCommandAndSqlArguments(isSeededOnly, userNameFilter, 
				includeOnlyTheseUsersId, includeAnyUserNamesOtherThanTheseNoMatterWhoCreatedThem, 
				onlyIncludeTheseUserNames, excludeTheseUserNames);
		
		String sql = sqlCommandAndArguments.getCommand();
				
		if (Config.getReversePrintSqlToLog()){
			FileUtils.println("Executing the SQL:\n'"+sql+"'");
		}
		if (messageLabel != null) {
			GUIUtils.showInProgressMessage(messageLabel, "Please wait, retrieving the number of users");
		}
		
		int rowsCount=DatabaseUtils.getRowsCount(environmentProperties,sql, sqlCommandAndArguments.getArguments(), oracleUserRetrievalExecutionMonitor);
		final NumberFormat numberFormatForProgressDisplay = new DecimalFormat();
		if (messageLabel != null) {
			GUIUtils.showInProgressMessage(messageLabel, "Please wait, gathering Oracle Users " + CoreUtil.generateDataRowImportRateDisplayString(numberFormatForProgressDisplay, 0, rowsCount, System.currentTimeMillis(), null));
		}
		
		try (Connection connection =DatabaseUtils.getDatabaseConnection(environmentProperties);
				PreparedStatement statement = connection.prepareStatement(sql);)
		{

			for (int i = 0 ; i < sqlCommandAndArguments.getArguments().size() ; i++) {
				statement.setObject(i+1, sqlCommandAndArguments.getArguments().get(i));
			}
			if (oracleUserRetrievalExecutionMonitor != null) {
				oracleUserRetrievalExecutionMonitor.setStatement(statement);
			}
			
			Map<Integer,String> tm=new TreeMap<Integer,String>();
			
			try (ResultSet resultSet=statement.executeQuery()) {	
				int LOG_BATCH_SIZE=10;
				int counter=0;
				final long startTime = System.currentTimeMillis();
				
				String userIdColumn="user_id";
				String userNameColumn="user_name";
				if (ReverseMain.IS_FUSION_DB) {
					userIdColumn="USR_KEY";
					userNameColumn="USR_LOGIN";
				}
				
				while (resultSet.next())
				{
					if (oracleUserRetrievalExecutionMonitor != null && oracleUserRetrievalExecutionMonitor.isCancelled()) {
						throw new OracleUserRetrievalExecutionMonitor.OracleUserRetrievalIsCalcelledException();
					}				
					counter++;
					if (counter%LOG_BATCH_SIZE==0 && messageLabel != null) {
						GUIUtils.showInProgressMessage(messageLabel, "Please wait, gathering Oracle Users " + CoreUtil.generateDataRowImportRateDisplayString(numberFormatForProgressDisplay, counter, rowsCount, startTime, null));
					}
					int userId= resultSet.getInt(userIdColumn);
					String userName= resultSet.getString(userNameColumn);
					tm.put(userId,userName);
				}				
			}


			if (messageLabel != null) {
				if (hideMessageLabelBeforeEnteringSuccessMessage) {
					messageLabel.setVisible(false);
				}
				GUIUtils.showSuccessMessage(messageLabel,rowsCount+" Oracle Users loaded.");
			}
			return tm;
		}
	}

	public static Map<String,String> getDataForValueSetQuery(Map<String,String> environmentProperties,String sql)
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection=null;
		Map<String,String> res=new HashMap<String,String>();
		try
		{
			connection =DatabaseUtils.getDatabaseConnection(environmentProperties);

			if (Config.getReversePrintSqlToLog()) {
				FileUtils.println("Loading data for Value set table, executing the SQL:\n'"+sql+"'");
			}
			

			statement= connection.prepareStatement(sql);
			statement.execute();
			resultSet=statement.executeQuery();

			while ( resultSet.next() )
			{
				res.put(resultSet.getString(1), resultSet.getString(2));
			}
			return res;
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return res;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static String[] processResultSetRow(ResultSet resultSet,Map<Integer,String> allOracleUserIdToNameSynchronizedMap,boolean isRestartAtRecord)
	throws SQLException
	{
		ResultSetMetaData rsMetaData = resultSet.getMetaData();
		int numberOfColumnsInResultSet=rsMetaData.getColumnCount();
		if (isRestartAtRecord) {
			numberOfColumnsInResultSet=numberOfColumnsInResultSet-1; // skipping the row num column (always at the end) used for restarting from a specific row.
		}
		String[] row=new String[numberOfColumnsInResultSet];
		for ( int i = 1; i <= numberOfColumnsInResultSet; i++ )
		{
			String columnName=rsMetaData.getColumnName(i);
			int colType=rsMetaData.getColumnType(i);
			String data="";
			if ( colType==Types.NUMERIC )
			{
				BigDecimal bd=resultSet.getBigDecimal(i);
				if ( bd==null ) {
					data="";
				}
				else {
					data=bd.toString();
					if ( columnName!=null &&  columnName.equalsIgnoreCase(DataExtractionConstants.RSC_LAST_UPDATED_BY_COLUMN_NAME)) {
						String userName=allOracleUserIdToNameSynchronizedMap.get(bd.intValue());
						if (userName!=null) {
							data=userName;
						}
					}
				}
			}
			else
			if ( colType==Types.TIMESTAMP )
			{
				Timestamp timestamp=resultSet.getTimestamp(i);
				if ( timestamp==null ) {
					data="";
				}
				else {
					data=Utils.getFormattedDate(timestamp);
				}
			}
			else
			{
				data=resultSet.getString(i);
				if ( data==null ) {
					data="";
				}
			}
			row[i-1]=data;
		}
		return row;
	}

	public static final String processSQLForRecordCountExtraction(String sqlStatement)
	{
		return "SELECT COUNT(*) from ( " + sqlStatement + " ) ";
	}

	public static final boolean hasOracleDateCondition(String formattedDate)
	{
		return formattedDate!=null && !formattedDate.isEmpty();
	}

	public static final String addTimeComponentToFormattedDate(String formattedDate)
	{
		int indexOf=formattedDate.indexOf(":");
		if ( indexOf==-1 ) {
			return formattedDate+" 00:00:00";
		}
		return formattedDate;
	}

	public static int getExpectedRecordCountToReverse(PreparedStatement statement)
	throws Exception
	{
		ResultSet resultSet = null;
		try
		{
			resultSet = statement.executeQuery();
			resultSet.next();
			int res=resultSet.getInt(1);
			return res;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static String addSelectedUserIdsToQuery(
			boolean isIncludeSelectedUsers,
			List<Integer> selectedUserIds,
			Properties replacementsProperties, String sqlQuery, Map<Integer, String> allOracleUserIdToNameSynchronizedMap)
	{
		if (isIncludeSelectedUsers) {
			replacementsProperties.put(DataExtractionConstants.RSC_LAST_UPDATED_BY_OPERATOR,DataExtractionConstants.RSC_IN_OPERATOR);
		}
		else {
			replacementsProperties.put(DataExtractionConstants.RSC_LAST_UPDATED_BY_OPERATOR,DataExtractionConstants.RSC_NOT_IN_OPERATOR);
		}

		if (selectedUserIds.size()>=DaoUtil.MAX_IN_CLAUSE_SIZE) {
			// workaround for Oracle limitation of 1000 elements in a IN clause -
			// Split into multiple IN clause

			sqlQuery = UserIdQueryUtil.replaceUserIdInQuery(sqlQuery, UtilsConstants.REPLACEMENTS_DELIMITER + DataExtractionConstants.RSC_USER_ID_LIST
					+ UtilsConstants.REPLACEMENTS_DELIMITER, replacementsProperties, selectedUserIds);
			sqlQuery = UserIdQueryUtil.replaceUserIdInQuery(sqlQuery, UtilsConstants.REPLACEMENTS_DELIMITER + DataExtractionConstants.RSC_USER_ID_LIST_OLD
					+ UtilsConstants.REPLACEMENTS_DELIMITER, replacementsProperties, selectedUserIds);

		}

		StringBuffer userIdList=new StringBuffer("");
		if (ReverseMain.IS_FUSION_DB) {
			if (!selectedUserIds.isEmpty()) {
				for (int i=0;i<selectedUserIds.size();i++) {
					Integer selectedUserId=selectedUserIds.get(i);
					String userName=allOracleUserIdToNameSynchronizedMap.get(selectedUserId);
					userIdList.append("'"+userName+"'");
					if ( i+1 < selectedUserIds.size() ) {
						userIdList.append(",");
					}
				}		
			}
			else {
				userIdList.append("'res_zzzzzz'");
			}
		}	
		else {
			String unusedId="-99999";
			if (!selectedUserIds.isEmpty()) {
				userIdList.append(SharedUtil.getCommaDelimitedList(selectedUserIds));
			}
			else {
				userIdList.append(unusedId);
			}
		}
		replacementsProperties.put(DataExtractionConstants.RSC_USER_ID_LIST,userIdList.toString());
		replacementsProperties.put(DataExtractionConstants.RSC_USER_NAME_LIST,userIdList.toString());
		replacementsProperties.put(DataExtractionConstants.RSC_USER_ID_LIST_OLD,userIdList.toString());

		return sqlQuery;
	}


	public static boolean hasRSCPrerequisiteObjects(DataExtractionJobManager reverseExecutionJobManager,String sqlStatement)
	{
		if (reverseExecutionJobManager.getRscPrerequisiteObjectsKeyword()==null) {
			return false;
		}
		return sqlStatement.indexOf(reverseExecutionJobManager.getRscPrerequisiteObjectsKeyword())!=-1;
	}

	public static boolean hasDFFTableQueryKeyword(String sqlStatement)
	{
		return sqlStatement.indexOf(DataExtractionConstants.DFF_TABLE_QUERY_KEYWORD)!=-1;
	}

	public static String getDFFTableQuery(String sqlStatement)
	{
		return DataExtractionUtils.getTextAfterKeyword(sqlStatement,DataExtractionConstants.DFF_TABLE_QUERY_KEYWORD);
	}

	public static boolean verifyPrerequisiteObjects(
			DataExtractionJobManager reverseExecutionJobManager,String sqlStatement,Map<String,String> environmentProperties)
	throws Exception
	{
		String textAfterKeyword=DataExtractionUtils.getTextAfterKeyword(sqlStatement,reverseExecutionJobManager.getRscPrerequisiteObjectsKeyword());
		List<String> preRequisiteTokens = CoreUtil.splitCommaDelimitedStrings(textAfterKeyword);

		FileUtils.println("Verifying the Prerequisite objects...");
		for (String preRequisiteToken:preRequisiteTokens) {
			FileUtils.println("Verifying the Prerequisite object: '"+preRequisiteToken+"'");
			boolean hasViewObject=DatabaseUtils.hasViewObject(environmentProperties,preRequisiteToken);
			if (!hasViewObject) {
				FileUtils.println("Object not found: '"+preRequisiteToken+"'");
				return false;
			}
		}
		return true;
	}

	public static void processResultSet(
			DataExtractionJob dataExtractionJob,
			DFFManager dffManager,
			ResultSet resultSet,
			int expectedRecordCountToReverse,
			List<Field> inventoryFields,
			boolean isRestartAtRecord,
			boolean isAuditExtractionEnabled,
			boolean hasAuditColumnsInSQLQuery)
	throws Exception
	{
		List<String[]> rows=new ArrayList<String[]>();
		int restartAtRecordNumber=dataExtractionJob.getLastReadRow();
		int recordNumber=dataExtractionJob.getLastReadRow();
		int recordNumberBeforeProcessing=0;
		int extractedRows = 0;
		while ( resultSet.next() )
		{
			recordNumber++;
			rows.add(processResultSetRow(resultSet,dataExtractionJob.getReverseExecutionJobManager().getAllOracleUserIdToNameSynchronizedMap(),isRestartAtRecord));
			if ( (recordNumber%DataExtractionConstants.RECORD_PROCESSING_BATCH_SIZE)==0 )
			{
				if ( DataExtractionUtils.isStopProcessingTask(dataExtractionJob) ){
					DataExtractionUtils.updateTaskStatus(dataExtractionJob,recordNumber);
					return;
				}
				extractedRows += process(dataExtractionJob,dffManager,inventoryFields,isAuditExtractionEnabled,hasAuditColumnsInSQLQuery,rows,recordNumberBeforeProcessing,false);
				DataExtractionUtils.updateTaskStatus(dataExtractionJob,extractedRows,expectedRecordCountToReverse,isRestartAtRecord,restartAtRecordNumber);
				rows=new ArrayList<String[]>();
				recordNumberBeforeProcessing=recordNumber;
				dataExtractionJob.setLastReadRow(recordNumber);
			}
		}
		extractedRows += process(dataExtractionJob,dffManager,inventoryFields,isAuditExtractionEnabled,hasAuditColumnsInSQLQuery,rows,recordNumberBeforeProcessing,true);
		dataExtractionJob.setLastReadRow(recordNumber);
		if(dffManager != null && extractedRows==0) {
			((DataExtractionJobManager) dataExtractionJob.getExecutionJobManager()).updateSuccessfulExecutionStatus(dataExtractionJob, extractedRows);
		}else {
			DataExtractionUtils.updateTaskStatus(dataExtractionJob,extractedRows);
		}
	}

	private static int process(
			DataExtractionJob dataExtractionJob,
			DFFManager dffManager,
			List<Field> inventoryFields,
			boolean isAuditExtractionEnabled,
			boolean hasAuditColumnsInSQLQuery,
			List<String[]> rows,
			int recordNumberBeforeProcessing,
			boolean isLastBatchToProcess)
	throws Exception
	{
		List<String[]> rowsToProcess=rows;
		if(dffManager != null) {
			rowsToProcess=dffManager.processRowsForDFF(rows);
		}
		DataExtractionFileUtils.writeDataRows(dataExtractionJob,rowsToProcess,inventoryFields,dataExtractionJob.getLabel(),hasAuditColumnsInSQLQuery&&isAuditExtractionEnabled,recordNumberBeforeProcessing,isLastBatchToProcess);
		return rowsToProcess.size();
	}

	static class UserIdQueryUtil{

		private static String replaceUserIdInQuery(String sqlQuery, String placeHolder, Properties replacementsProperties, List<Integer> selectedUserIds) {
			StringBuffer toReturn = new StringBuffer();
			String[] placeHolderTokens = StringUtils.splitByWholeSeparator(sqlQuery, placeHolder);

			for (int i = 0; i < placeHolderTokens.length; i++)
			{
				String token = null;
				if (i < placeHolderTokens.length - 1)
				{
					// Appending placeHolder is required for subsequent methods to identify the clause and column name
					token = placeHolderTokens[i] + placeHolder;
				} else
				{
					// Last token do not append placeHolder value
					token = placeHolderTokens[i];
				}

				// Add ( to the column name
				HashMap<SQLSTRINGMAP, String> map = identifyClauseAndColumnName(token, placeHolder);
				token = prefixColumnNameWithBracket(map, token, i);

				// Add ) to the start for second token and subsequent tokens
				// cater for scenario such as mipr.created_by IN
				// (##USER_ID_LIST##,12)
				// where ,## is after the placeholder
				if (i != 0 && placeHolderTokens.length > 1)
				{
					int pos = StringUtils.indexOf(token, ")");
					token = StringUtils.substring(token, 0, pos) + ")" + StringUtils.substring(token, pos, token.length());
				}

				String newTarget = Matcher.quoteReplacement(placeHolder);
				toReturn.append(StringUtils.replace(token, placeHolder, breakSQLIntoChunk(token, replacementsProperties, newTarget, selectedUserIds)));
			}
			return toReturn.toString();
		}

		private static String prefixColumnNameWithBracket(HashMap<SQLSTRINGMAP,String> map , String sql, int pos){
			String clause = map.get(SQLSTRINGMAP.CLAUSE);
			String columnName = map.get(SQLSTRINGMAP.COLUMN);
			if(clause != null){
				int columnIndex = -1;
				columnIndex = StringUtils.lastIndexOf(sql, columnName + " ");
				if(columnIndex != -1){
					String front =StringUtils.substring(sql, 0, columnIndex);
					String end =StringUtils.substring(sql, columnIndex, sql.length());
					return front+"("+end;
				}
			}
			return sql;
		}

		private static String breakSQLIntoChunk(String sql, Properties replacementsProperties, String placeHolder, List<Integer> selectedUserIds){
			HashMap<SQLSTRINGMAP,String> map = identifyClauseAndColumnName(sql, placeHolder);
			String CLAUSE = map.get(SQLSTRINGMAP.CLAUSE);
			String condition =" OR ";
			if (CLAUSE == null){
				return sql;
			}

			// if clause is placeholder then need to dynamically change the condition based on its value
			if(CLAUSE.startsWith(UtilsConstants.REPLACEMENTS_DELIMITER) && CLAUSE.endsWith(UtilsConstants.REPLACEMENTS_DELIMITER)){
				String value=(String)replacementsProperties.get(StringUtils.substringBetween(CLAUSE, UtilsConstants.REPLACEMENTS_DELIMITER, UtilsConstants.REPLACEMENTS_DELIMITER));
				if(StringUtils.equalsIgnoreCase(StringUtils.trim(value), "NOT IN")){
					condition =" AND ";
				}
			}
			else{
				if(StringUtils.equalsIgnoreCase(StringUtils.trim(CLAUSE), "NOT IN")){
					condition =" AND ";
				}
			}

			String COLUMN = map.get(SQLSTRINGMAP.COLUMN);
			List<List<Integer>> fullList =  CoreUtil.splitList(selectedUserIds, DaoUtil.MAX_IN_CLAUSE_SIZE);

			StringBuffer outSql = new StringBuffer();
			if (CLAUSE!= null){
				for(int i=0;i< fullList.size();i++){
					if(i == 0){
						StringBuffer subSql = new StringBuffer();
						for(Integer string: fullList.get(i)){
							if(subSql.length()!=0){
								subSql.append(",");
							}
							subSql.append(string);
						}
						subSql.append(")");
						outSql.append(subSql);
					}else{
						StringBuffer subSql = new StringBuffer();
						for(Integer string: fullList.get(i)){
							if(subSql.length()!=0){
								subSql.append(",");
							}
							subSql.append(string);
						}
						outSql.append(condition);
						outSql.append(COLUMN + " " + CLAUSE + " (");
						outSql.append(subSql);
						// To not append ) because to cater for mipr.created_by IN (##USER_ID_LIST##,12) scenario
						if(i != fullList.size() -1){
							outSql.append(") ");
						}
					}
				}
			}
			return outSql.toString();
		}
		private static HashMap<SQLSTRINGMAP,String> identifyClauseAndColumnName(String sql, String searchToken) {
			String sqlOnlySpace = sql.replaceAll("\\s+", " ");

			HashMap<SQLSTRINGMAP,String> map = new HashMap<SQLSTRINGMAP,String>();

			int firstToken = StringUtils.indexOf(sqlOnlySpace.toUpperCase(), searchToken.toUpperCase());
			if (firstToken != -1)
			{
				String str = StringUtils.trim(StringUtils.substring(sqlOnlySpace, 0, firstToken));
				String clause = findClause(str);
//				System.out.println("clause: " + clause);
				map.put(SQLSTRINGMAP.CLAUSE, clause);

				if(clause == null){
					return map;
				}
				str = StringUtils.substring(str, 0, StringUtils.lastIndexOf(str.toUpperCase(), clause.toUpperCase()));
				String cloumn = findColumnName(str);
//				System.out.println("cloumn: " + cloumn);
				map.put(SQLSTRINGMAP.COLUMN, cloumn);

			}
			return map;
		}

		private static String findColumnName(String str) {
			String column = null;

			// Column should be separated by at least a space before or after
			String[] tokens = StringUtils.split(str);
			column = tokens[tokens.length - 1];
			if (column.startsWith("("))
			{
				column = StringUtils.replace(column, "(", "");
			} else {
				if (column.startsWith("."))
				{

					// Handle scenario like APPOLHEADERSEO .created_by in
					// (##USER_ID_LIST##)
					column = tokens[tokens.length - 2] + " " + tokens[tokens.length - 1];
				}
				// Get the last word after (
				int pos = StringUtils.lastIndexOf(column, "(");
				if (pos != -1)
				{
					return StringUtils.substring(column, pos + 1, column.length());
				}
			}
			return column;
		}

		private static String findClause(String str) {
			String[] tokens = StringUtils.split(str, "(");
			tokens = StringUtils.split(tokens[tokens.length - 1]);
			// The last token nearest to ( should be the IN/NOT IN clause
			String lastToken = tokens[tokens.length - 1];
			String clause = null;
			if (StringUtils.isAlpha(lastToken))
			{
				clause = lastToken;
				if (StringUtils.equalsIgnoreCase(clause, "IN"))
				{
					if (StringUtils.equalsIgnoreCase("NOT", tokens[tokens.length - 2]))
					{
						clause="NOT IN";
					}
				}
			}else{
				// Clause can also be placeholder
				if(lastToken.startsWith(UtilsConstants.REPLACEMENTS_DELIMITER) && lastToken.endsWith(UtilsConstants.REPLACEMENTS_DELIMITER)){
					clause = lastToken;
				}
			}
			return clause;
		}

	}
	enum SQLSTRINGMAP{
		CLAUSE,
		COLUMN;
	}
	
    public static <E> void appendInOrNotInClausesToSqlAndTheAssociatedArgumentsList(final Set<E> elements,
    		final String databaseColumnName,
    		final StringBuffer sql,
            final List<Object> sqlStatementParameterValues,
            final boolean preceedByAnd,
            final boolean trueForInFalseForNotIn)
    {
        if (elements == null || elements.isEmpty())
        {
            return;
        }
        if (preceedByAnd) {
        	sql.append(" and");
        }
        sql.append(" (");
        
        List<List<E>> subLists = CoreUtil.splitList(new ArrayList<E>(elements), DaoUtil.MAX_IN_CLAUSE_SIZE );
        boolean firstTimeInOuterLoop = true;
        for (final List<E> subList : subLists) {
        	if (!firstTimeInOuterLoop) {
        		if (trueForInFalseForNotIn) {
        			sql.append(" or ");
        		} else {
        			sql.append(" and ");
        		}
        	}
        	sql.append(databaseColumnName);
        	if (trueForInFalseForNotIn) {
        		sql.append(" in (");
        	} else {
        		sql.append(" not in (");
        	}
        	boolean firstTimeInInnerLoop = true;
        	for (final E el : subList) {
        		if (!firstTimeInInnerLoop) {
        			sql.append(',');
        		}
        		sql.append('?');
        		sqlStatementParameterValues.add(el);
        		firstTimeInInnerLoop = false;
        	}
        	sql.append(") ");
        	firstTimeInOuterLoop = false;
        }
        sql.append(") ");
    }	
}