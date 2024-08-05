package com.rapidesuite.client.common.util;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JLabel;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.util.Assert;

import com.rapidesuite.build.core.ManualStopException;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.client.common.PLSQLFunctionParameter;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.EnvironmentPropertyConstants;
import com.rapidesuite.reverse.DataExtractionConstants;
import com.rapidesuite.reverse.ReverseMain;
import com.rapidesuite.reverse.utils.DataExtractionDatabaseUtils.OracleUserRetrievalExecutionMonitor;
import com.rapidesuite.reverse.utils.DataExtractionUtils;

public class DatabaseUtils
{
	/**
	 * Number of retries when we encounter a java.sql.SQLRecoverableException 
	 */
	public static final Integer CONNECTION_RETRY_COUNT = 5;
	
	/**
	 * Seconds to wait before retrying when we encounter a java.sql.SQLRecoverableException 
	 */
	public static final Integer CONNECTION_RETRY_WAIT_SECONDS = 60;
	public static final Integer PREPARED_STATEMENT_TIMEOUT_SECONDS = 300;
	

	public static void testDatabaseConnection(Map<String,String> environmentProperties) throws Exception
	{
		Connection connection=null;
		try{
			connection=getDatabaseConnection(environmentProperties);
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static void testDatabaseConnection(String hostName,String portNumber,String sid,String userName,
			String password) throws Exception
	{
		Connection connection=null;
		try{
			connection=getJDBCConnection(hostName,portNumber,sid,userName,password);
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static Connection getDatabaseConnection(Map<String,String> environmentProperties)
	throws Exception
	{
		String hostName=environmentProperties.get(EnvironmentPropertyConstants.DATABASE_HOST_NAME_KEY);
		String portNumber=environmentProperties.get(EnvironmentPropertyConstants.DATABASE_PORT_NUMBER_KEY);
		String sid=environmentProperties.get(EnvironmentPropertyConstants.DATABASE_SID_KEY);
		String userName=environmentProperties.get(EnvironmentPropertyConstants.DATABASE_USER_NAME_KEY);
		String password=environmentProperties.get(EnvironmentPropertyConstants.DATABASE_PASSWORD_KEY);
		if ( hostName==null || hostName.isEmpty()){
			throw new Exception("Invalid Database Hostname, please fix your Environment.");
		}
		if ( portNumber==null || portNumber.isEmpty()){
			throw new Exception("Invalid Database port Number, please fix your Environment.");
		}
		if ( sid==null || sid.isEmpty()){
			throw new Exception("Invalid Database SID, please fix your Environment.");
		}
		if ( userName==null || userName.isEmpty()){
			throw new Exception("Invalid Database user name, please fix your Environment.");
		}
		if ( password==null || password.isEmpty()){
			throw new Exception("Invalid Database password, please fix your Environment.");
		}
		return getJDBCConnection(hostName,portNumber,sid,userName,password);
	}

	public static Connection getJDBCConnection(
			String hostName,String portNumber,String sid,String userName,
			String password) throws ClassNotFoundException, SQLException
	{
		return getJDBCConnection("jdbc:oracle:thin:@"+hostName+":"+portNumber+":"+sid,userName,password);
	}
	
	public static Connection getJDBCConnection(String jdbcString,String userName,String password) throws SQLException, ClassNotFoundException
	{
		return getJDBCConnectionGeneric(jdbcString,userName,password,true);
	}
	
	public static Connection getJDBCConnectionGeneric(String jdbcString,String userName,String password,boolean isRetryOn) throws SQLException, ClassNotFoundException
	{
		Connection con=null;
		Class.forName("oracle.jdbc.OracleDriver");
		
		int maxRetries=1;
		if (isRetryOn) {
			 maxRetries=CONNECTION_RETRY_COUNT;
		}
		for(int i = 1; i <= maxRetries; i++) {
			try {
				con=DriverManager.getConnection(jdbcString,userName,password);
			} catch (SQLRecoverableException sqlRecoverableException) {
				if(i < maxRetries) {
					FileUtils.println("getJDBCConnectionGeneric, connection lost. Retries: " + i+" / "+maxRetries+" . Waiting for " + CONNECTION_RETRY_WAIT_SECONDS + " seconds before retry");
					try {
						Thread.sleep(CONNECTION_RETRY_WAIT_SECONDS * 1000);
					} catch (InterruptedException e) {
						FileUtils.printStackTrace(e);
					}
				} else {
					throw sqlRecoverableException;
				}
				continue;
			}
			break;
		}		
		return con;
	}

	public static boolean isInternalOracleEbsServer(Map<String,String> environmentProperties) throws Exception {
		Connection connection=null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			connection = getDatabaseConnection(environmentProperties);

			statement= connection.prepareStatement("select 1 from v$instance where INSTANCE_NAME = 'ERPP'");
			statement.execute();
			resultSet=statement.executeQuery();

			if ( resultSet.next() )
			{
				BigDecimal sqlResult = resultSet.getBigDecimal("1");
				return sqlResult != null && sqlResult.intValueExact() == 1;
			} else {
				return false;
			}
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static String getRscSourceKey(Map<String,String> environmentProperties)
	throws Exception
	{
		Connection connection=null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String resError="Instance name not available";
		try
		{
			String sql = " select instance_name from v$instance";

			connection =getDatabaseConnection(environmentProperties);

			statement= connection.prepareStatement(sql);
			statement.execute();
			resultSet=statement.executeQuery();

			if ( resultSet.next() )
			{
				String instance_name= resultSet.getString("instance_name");
				return instance_name;
			}
			return resError;
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return resError;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static String getRscPLSQLPackageTargetVersion(Connection connection,String packageName)
			throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			statement = connection.prepareStatement("SELECT count(*) FROM user_OBJECTS WHERE OBJECT_TYPE IN ('PACKAGE') and upper(object_name) = upper(?)");
			statement.setString(1, packageName);
			statement.execute();
			resultSet=statement.executeQuery();
			Assert.isTrue(resultSet.next());
			int count = resultSet.getInt(1);
			if ( count == 0 )
			{
				//package not installed
				return null;
			}
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);

			statement = connection.prepareStatement("select " + packageName + ".get_rsc_package_version() as res from dual");
			statement.execute();
			resultSet=statement.executeQuery();
			Assert.isTrue(resultSet.next(), "Get package version should return a value.");
			String version= resultSet.getString("res");
			return FileUtils.replaceSVNRevisionKeywords(version);
		}
		catch(SQLException e)
		{
			//ORA-04063: package body "APPS.XX_RSC_SUPPLIER_MIGRATION_PKG" has errors
			final int PACKAGE_BODY_HAS_ERRORS = 4063;
			//ORA-06575: Package or function XX_RSC_UTILITY_MIGRATION_PKG is in an invalid state
			final int PACKAGE_BODY_INVALID_STATE = 6575;
			if ( e.getErrorCode() == PACKAGE_BODY_HAS_ERRORS || e.getErrorCode() == PACKAGE_BODY_INVALID_STATE )
			{
				return null;
			}
			else
			{
				throw new Exception(e);
			}
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static String getRscPLSQLPackageTargetVersion(Map<String,String> environmentProperties,String packageName)
	throws Exception
	{
		Connection connection=null;
		try
		{
		    connection =getDatabaseConnection(environmentProperties);
		    return getRscPLSQLPackageTargetVersion(connection,packageName);
		}
		finally
		{
            DirectConnectDao.closeQuietly(connection);
		}
	}

	public static TreeMap<Long,String> getOperatingUnitIdToNameMap(Map<String,String> environmentProperties,boolean isDebug,
			JLabel messageLabel)
	throws Exception
	{
		String sqlQuery= "select organization_id, name from hr_operating_units order by name desc";
		String message="Operating Unit Names";
		if (ReverseMain.IS_FUSION_DB) {
			sqlQuery= "select BU_ID,BU_NAME from FUSION.FUN_ALL_BUSINESS_UNITS_V  ORDER BY BU_NAME ASC";
			message="Business Unit Names";
		}		

		return getIdToNameMap(environmentProperties,isDebug,messageLabel,sqlQuery,message);
	}

	public static TreeMap<Long,String> getBusinessGroupIdToNameMap(Map<String,String> environmentProperties,boolean isDebug,
			JLabel messageLabel)
	throws Exception
	{
		if (ReverseMain.IS_FUSION_DB) {
			return new TreeMap<Long,String>();
		}
		String sqlQuery="select BG.business_group_id,BG.name from PER_BUSINESS_GROUPS BG Order by 1";

		return getIdToNameMap(environmentProperties,isDebug,messageLabel,sqlQuery,"Business Group Names");
	}

	public static TreeMap<Long,Long> getOperatingUnitIdToBusinessGroupIdMap(Map<String,String> environmentProperties,boolean isDebug,
			JLabel messageLabel)
	throws Exception
	{
		if (ReverseMain.IS_FUSION_DB) {
			return new TreeMap<Long,Long>();
		}
		String sqlQuery="select OU.organization_id,bg.business_group_id from hr_operating_units OU , PER_BUSINESS_GROUPS BG "+
		"WHERE bg.business_group_id = OU.business_group_id";

		TreeMap<Long, String> sourceMap=getIdToNameMap(environmentProperties,isDebug,messageLabel,sqlQuery,"Business Group Names");
		return convertMapValuesFromStringToIdMap(sourceMap);
	}

	public static void checkClientInfo(Connection connection)
	throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql = " SELECT USERENV('CLIENT_INFO') CI FROM DUAL";
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();

			if ( resultSet.next() )
			{
				resultSet.getString("CI");
			}
			else throw new Exception("ERROR: The Client Info was not set.");
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static void setClientInfo(Connection connection,int operatingUnitId)
	throws Exception
	{
		PreparedStatement statement = null;

		try
		{
			String clientInfo="{CALL dbms_application_info.set_client_info(?)} ";
			statement= connection.prepareStatement(clientInfo);
			statement.setInt(1, operatingUnitId);
			statement.execute();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static void setPolicyContextServer(Connection connection,int operatingUnitId)
	{
		PreparedStatement statement = null;
		try
		{
			String sql="{CALL mo_global.set_policy_context_server('S',?)} ";
			statement= connection.prepareStatement(sql);
			statement.setInt(1, operatingUnitId);
			statement.execute();
			connection.commit();
		}
		catch(Exception e){
			FileUtils.printStackTrace(e);
		}
		finally
		{
			DirectConnectDao.closeQuietly(statement);
		}
	}

	
	
	public static String[] getSidAndSerial(Connection connection)
	throws Exception
	{
		String sql = "SELECT dbms_debug_jdwp.current_session_id sid, dbms_debug_jdwp.current_session_serial serial# from dual";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.prepareStatement(sql);
			stmt.execute();
			rs = stmt.getResultSet();
			rs.next();
			String sid = rs.getString(1);
			String serial = rs.getString(2);
			return new String[]{sid,serial};
		}
		catch(Throwable t)
		{
			FileUtils.println("Failed to retrieve sid/serial using sql = " + sql);
			FileUtils.printStackTrace(t);
			return new String[]{"N/A", "N/A"};
		}
		finally
		{
			DirectConnectDao.closeQuietly(rs);
			DirectConnectDao.closeQuietly(stmt);
		}
	}
	
	
	private static Set<PreparedStatement> globalSetOfAllActivePreparedStatements = new HashSet<PreparedStatement>();
	public static void forciblyTerminateAllActivePreparedStatements()
	{
		synchronized(globalSetOfAllActivePreparedStatements)
		{
			for ( PreparedStatement ps : globalSetOfAllActivePreparedStatements )
			{
				try 
				{
					FileUtils.println("Terminating PreparedStatement: " + ps.toString());
					if ( !ps.isClosed() ) 
					{
						ps.cancel();
					}
				}
				catch (SQLException e) 
				{
					FileUtils.printStackTrace(e);
				}
			}
			globalSetOfAllActivePreparedStatements.clear();
		}
	}
	
	/**
	 *
	 * @param connection
	 * @param sql
	 * @return record count updated
	 * @throws Exception
	 */
	public static int executeStatement(Connection connection,String sql)
	throws Exception
	{
		PreparedStatement stmt = null;
		try
		{
			stmt = connection.prepareStatement(sql);
			synchronized(globalSetOfAllActivePreparedStatements)
			{
				globalSetOfAllActivePreparedStatements.add(stmt);
			}
			int toReturn = stmt.executeUpdate();
			return toReturn;
		}
		finally
		{
			synchronized(globalSetOfAllActivePreparedStatements)
			{
				globalSetOfAllActivePreparedStatements.remove(stmt);
			}
			DirectConnectDao.closeQuietly(stmt);
		}
	}

	public static void dropPLSQLPackage(Map<String,String> environmentProperties,File plsqlPackageFile)
	throws Exception
	{
		Connection connection=null;
		try
		{
			String plsqlPackageName=CoreUtil.getFileNameWithoutExtension(plsqlPackageFile);
			connection =getDatabaseConnection(environmentProperties);
			String statement="DROP PACKAGE "+plsqlPackageName;
			executeStatement(connection,statement);
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static boolean hasViewObject(Map<String,String> environmentProperties,String object)
	throws Exception
	{
		Connection connection=null;
		try
		{
			connection =getDatabaseConnection(environmentProperties);
			String statement="select count(*) from "+object;
			executeStatement(connection,statement);
			return true;
		}
		catch(Exception e) {
			//FileUtils.printStackTrace(e);
			return false;
		}
		finally
		{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	private static void writeToActionManagerConsole(final ActionManager actionManager, final String output) {
		if (actionManager == null) {
			return;
		}
		actionManager.setOutput(output.trim());
	}

	/**
	 *
	 * @param environmentProperties
	 * @param plsqlFile
	 * @param replacementProperties
	 * @return true if RSC Audit columns are detected in the package SQL statements.
	 * @throws Exception
	 */
	public static void executeSqlStatements(Map<String,String> environmentProperties,File plsqlFile, Properties replacementProperties,
	        boolean continueOnError, Map<PrintWriter, Boolean> outputLogWriterToPrintSqlStatementsMap, MutableBoolean terminationCheck, ActionManager actionManager)
	throws Exception
	{
		if (outputLogWriterToPrintSqlStatementsMap == null) {
			outputLogWriterToPrintSqlStatementsMap = new HashMap<PrintWriter, Boolean>();
		}

		Connection connection=null;
		try
		{	
			List<String> statements=getPLSQLStatementsWithReplacements(environmentProperties, plsqlFile, replacementProperties);
			boolean[] hasDeleteCommands=new boolean[statements.size()];
			boolean[] hasTruncateCommands=new boolean[statements.size()];
			boolean[] hasDropCommands=new boolean[statements.size()];
			for (int i=0;i<statements.size();i++)
			{
				String sql=statements.get(i);
				/*
				 * Block all DELETE, DROP, TRUNCATE commands except if in the XX_RES schema.				 * 
				 */
				hasDeleteCommands[i]=CoreUtil.hasDeleteCommands(sql);
		    	hasTruncateCommands[i]=CoreUtil.hasTruncateCommands(sql);
		    	hasDropCommands[i]=CoreUtil.hasDropCommands(sql);
			}
			
			connection =getDatabaseConnection(environmentProperties);
			connection.setAutoCommit(false);
			
			int sqlStatementIndex = 0;
			for (int i=0;i<statements.size();i++)
			{
				String sql=statements.get(i);
				sqlStatementIndex++;
			    if ( null != terminationCheck )
			    {
    		        synchronized ( terminationCheck )
    		        {
    		            if ( terminationCheck.booleanValue() )
    		            {
    		            	final String message = "Manual stop requested. Stopping...\n";
    	                    FileUtils.log(null, message, true, true, outputLogWriterToPrintSqlStatementsMap.keySet());
    	                    writeToActionManagerConsole(actionManager, message);
                            throw new ManualStopException();
    		            }
    		        }
			    }

			    try
			    {
			        sql = CoreUtil.stripCStyleComments(sql);
			        //NOT removing SQL-style comments, as they embed '--' in their files as a string.
//			        sql = sql.replaceAll("--.*?\n", " ");
//			        sql = sql.replaceAll("--.*?\r", " ");
//			        sql = sql.replaceAll("--.*?$", " "); //some files require a different end of line?  \r doesn't work, but $ does; $ doesn't work, but \n does (some files)
			        
                    sql = sql.trim();

		            String sqlOnlySpace = sql.replaceAll("\\s+", " ").toUpperCase();
		            boolean isPackage = sqlOnlySpace.contains("CREATE OR REPLACE PACKAGE") || sqlOnlySpace.contains("CREATE PACKAGE");
		            boolean isDeclaration = sqlOnlySpace.startsWith("DECLARE ");
		            boolean isCall = sqlOnlySpace.startsWith("BEGIN");
                    if ( sql.length() == 0 )
                    {
                        continue;
                    }
                    
                    boolean hasRemoveCommand=hasDeleteCommands[i]||hasTruncateCommands[i]||hasDropCommands[i];
                	if (hasRemoveCommand && !isPackage) {
			    		String msg="";
			    		if (hasDeleteCommands[i]) {
			    			msg="!!!! ERROR: the word 'DELETE' was detected in the SQL script to run. "+
				        			"Delete commands in APPS schema are prohibited to run in BUILD!";
			    		}
			    		else
			    			if (hasDropCommands[i]) {
				    			msg="!!!! ERROR: the words 'DROP TABLE' were detected in the SQL script to run. "+
					        			"Drop tables commands in APPS schema are prohibited to run in BUILD!";
				    		}
			    			else
			    				if (hasTruncateCommands[i]) {
					    			msg="!!!! ERROR: the words 'TRUNCATE TABLE' were detected in the SQL script to run. "+
						        			"Truncate commands in APPS schema are prohibited to run in BUILD!";
					    		}			    		
			    		throw new Exception(msg);
			    	}
                    
                    
		            if ( sql.charAt(sql.length() - 1) == ';' && !isDeclaration && !isPackage && !isCall )
		            {
		                sql = sql.substring(0, sql.length() - 1);
		            }

                	String[] sidAndSerial = getSidAndSerial(connection);
                	for (final Map.Entry<PrintWriter, Boolean> entry : outputLogWriterToPrintSqlStatementsMap.entrySet()) {
                		FileUtils.log(null, "About to use [Sid,Serial#]=[" + sidAndSerial[0] + "," + sidAndSerial[1] + "] to execute SQL statement "+sqlStatementIndex+" / "+statements.size()+":" + (entry.getValue() ? ("\n\""+sql+"\"") : " *REDACTED*")+"\n", true, true, entry.getKey());
                	}
                	String sqlToWriteToActionManagerConsole = sql.trim();
                	if (sqlToWriteToActionManagerConsole.length() > 50) {
                		sqlToWriteToActionManagerConsole = sqlToWriteToActionManagerConsole.substring(0, 50) + "...";
                	}
                	writeToActionManagerConsole(actionManager, "About to use [Sid,Serial#]=[" + sidAndSerial[0] + "," + sidAndSerial[1] + "] to execute SQL statement "+sqlStatementIndex+" / "+statements.size()+": " + (Utils.hasAccessToInternalStaffsOnlyFeatures() ? sqlToWriteToActionManagerConsole : "*REDACTED*"));
			        final long executionStartTime = System.currentTimeMillis();
                	int retVal = executeStatement(connection,sql);
                	final long executionCompletionTime = System.currentTimeMillis();
                	final String executionCompletionMessage = "Execution of statement "+sqlStatementIndex+" / "+statements.size()+" was completed in "+CoreUtil.getDaysHoursMinutesSecondsFromMilliseconds(executionStartTime, executionCompletionTime)+". Return value = " + retVal + "\n";
			        FileUtils.log(null, executionCompletionMessage, true, true, outputLogWriterToPrintSqlStatementsMap.keySet());
			    	writeToActionManagerConsole(actionManager, executionCompletionMessage);
			    }
			    catch(Throwable t)
			    {
			        final int TABLE_OR_VIEW_DOES_NOT_EXIST = 942; //SQL Error: ORA-00942: table or view does not exist
                    FileUtils.printStackTrace(t);
                    final String message = "Error: : " + CoreUtil.getAllThrowableMessages(t);
			        FileUtils.log(null, message, true, true, outputLogWriterToPrintSqlStatementsMap.keySet());
			        writeToActionManagerConsole(actionManager, message);
			        if ( !continueOnError )
			        {
			            throw new Error(t);
			        }
			        else if ( !(t instanceof SQLException && ((SQLException)t).getErrorCode() == TABLE_OR_VIEW_DOES_NOT_EXIST && sql.toLowerCase().startsWith("drop")) )
			        {
                        throw new Error(t);
			        }
			    }
			}
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static final boolean isPackageValid(String packageName, Map<String,String> environmentProperties) throws Exception
	{
		Connection connection=null;
		try{
			connection =getDatabaseConnection(environmentProperties);
			return isPackageValid( connection, packageName);
		}
		finally
		{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static final boolean isPackageValid(Connection connection,String packageName) throws Exception
	{
		final String GET_PACKAGE_STATUS_SQL = "select object_name, object_type, status from dba_objects  where upper(object_name) = upper(?)";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try {
			pstmt = connection.prepareStatement(GET_PACKAGE_STATUS_SQL);
			pstmt.setString(1, packageName);
			rs = pstmt.executeQuery();
			int count = 0;
			boolean toReturn=true;
			while ( rs.next() )
			{
				count++;
				String status = rs.getString("status");
				Assert.isTrue(status != null && (status.toUpperCase().equals("VALID") || status.toUpperCase().equals("INVALID")), 
						"Invalid status returned for '" + packageName + "': " + status);
				if ( status.toUpperCase().equals("INVALID") )
				{
					toReturn=false;
					break;
				}
			}
			if ( count != 2 ) //one record for package, one for header
			{
				throw new Exception("InvalidState: did not find exactly 2 records in 'dba_objects' for status of package '" + packageName + "'; instead, found: " + count);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(rs);
			DirectConnectDao.closeQuietly(pstmt);
		}
	}
	
    private static List<String> getPLSQLStatementsWithReplacements(Map<String,String> environmentProperties,File plsqlFile, Properties replacementProperties)
    throws Exception
    {
        List<String> statements=FileUtils.readContentsFromPLSQLFile(plsqlFile);
        for ( int i = 0; i < statements.size(); i++ )
        {
            String statement = statements.get(i);
            if ( null != replacementProperties )
            {
                statements.set(i, DataExtractionUtils.replaceTokens(replacementProperties,statement));
            }
        }
        return statements;
    }


    public static boolean isPLSQLFileContainAuditColumns(Map<String,String> environmentProperties,File plsqlFile, Properties replacementProperties)
            throws Exception
    {
        boolean toReturn = false;
        List<String> statements=getPLSQLStatementsWithReplacements(environmentProperties, plsqlFile, replacementProperties);
        for ( String statement:statements)
        {
            toReturn |= statement.toLowerCase().indexOf(DataExtractionConstants.RSC_LAST_UPDATED_BY_COLUMN_NAME.toLowerCase())!=-1;
        }
        return toReturn;
    }




	public static String getEBSVersion(Map<String,String> environmentProperties)
	throws Exception
	{
		Connection connection=null;
		try
		{
			connection =getDatabaseConnection(environmentProperties);
			return getEBSVersion(connection);
		}
		finally
		{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static String getEBSVersion(Connection connection)
	throws Exception
	{
		String fusionVersion=getFusionVersion(connection);
		ReverseMain.IS_FUSION_DB=(fusionVersion!=null);
		if (ReverseMain.IS_FUSION_DB) {
			return fusionVersion;
		}
		
		String query="select release_name from apps.fnd_product_groups";
		String toReturn=getSingleStringValue(connection,query);
		
		if (toReturn==null){
			throw new Exception("EBS version cannot be found. (no records in the table fnd_product_groups)");
		}
		return toReturn;
	}

	public static String getFusionVersion(Connection connection)
			throws Exception
	{
		try {
			String query="select version FROM system.SCHEMA_VERSION_REGISTRY WHERE COMP_ID= 'FUSIONAPPS'";
			String toReturn=getSingleStringValue(connection,query);
			return toReturn;
		}
		catch(Exception e) {
			return null;
		}
	}

	public static String getSingleStringValue(Connection connection,String query)
			throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			statement= connection.prepareStatement(query);
			statement.execute();
			resultSet=statement.executeQuery();

			if (resultSet.next()){
				return resultSet.getString(1);
			}
			else {
				return null;
			}
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static int getRowsCount(Map<String,String> environmentProperties,String sqlStatement, List<Object> sqlArgs, OracleUserRetrievalExecutionMonitor oracleUserRetrievalExecutionMonitor)
	throws Exception
	{
		Connection connection=null;
		try
		{
			connection =getDatabaseConnection(environmentProperties);
			return getRowsCount(connection,sqlStatement, sqlArgs, oracleUserRetrievalExecutionMonitor);
		}
		finally
		{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static int getRowsCount(Connection connection,String sqlStatement, List<Object> sqlArgs, OracleUserRetrievalExecutionMonitor oracleUserRetrievalExecutionMonitor)
	throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="select count(*) res from ( "+sqlStatement+" )";

			statement= connection.prepareStatement(sql);
			if (sqlArgs != null) {
				for (int i = 0 ; i < sqlArgs.size() ; i++) {
					statement.setObject(i+1, sqlArgs.get(i));
				}
			}
			if (oracleUserRetrievalExecutionMonitor != null) {
				if (oracleUserRetrievalExecutionMonitor.isCancelled()) {
					throw new OracleUserRetrievalExecutionMonitor.OracleUserRetrievalIsCalcelledException();
				}
				oracleUserRetrievalExecutionMonitor.setStatement(statement);
			}
			statement.execute();
			resultSet=statement.executeQuery();
			resultSet.next();
			return resultSet.getInt("res");
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}



	public static String callPLSQLFunction(Connection connection,
			String plsqlPackageFunctionToCall,
			List<PLSQLFunctionParameter> plsqlFunctionParameters,DbmsOutput dbmsOutput)
	throws Exception
	{
        StringBuffer callText=new StringBuffer("");
        callText.append("{ call ? := ").append(plsqlPackageFunctionToCall).append("(");
        for (int i=0;i<plsqlFunctionParameters.size();i++) {
            if (i!=0) {
                callText.append(",?");
            }
            else {
                callText.append("?");
            }
        }
        callText.append(") }");

		try(CallableStatement statement = connection.prepareCall(callText.toString()))
		{
			for (int i=0;i<plsqlFunctionParameters.size();i++) {
				PLSQLFunctionParameter plsqFunctionParameter=plsqlFunctionParameters.get(i);
				String parameterName=plsqFunctionParameter.getParameterName();
				Object parameterValue=plsqFunctionParameter.getParameterValue();
				int parameterType=plsqFunctionParameter.getParameterType();

				//System.out.println( (i+1)+" parameterName:"+parameterName+
				//		" parameterValue:'"+parameterValue+"' ");

				if (parameterType==Types.VARCHAR) {
					//System.out.println("VARCHAR");
					statement.setString(i+2,(String)parameterValue);
				}
				else
				if (parameterType==Types.INTEGER) {
					if (parameterValue==null) {
						statement.setNull(i+2,Types.INTEGER);
					}
					else {
						statement.setInt(i+2,(Integer)parameterValue);
					}
				}
				else
				if (parameterType==Types.DOUBLE) {
					if (parameterValue==null) {
						statement.setNull(i+2,Types.DOUBLE);
					}
					else {
						statement.setDouble(i+2,(Double)parameterValue);
					}
				}
				else
				if (parameterType==Types.DATE) {
					if (parameterValue==null) {
						statement.setNull(i+2,Types.DATE);
					}
					else {
						statement.setDate(i+2,(java.sql.Date)parameterValue);
					}
				}
				else
				if (parameterType==Types.TIMESTAMP) {
					if (parameterValue==null) {
						statement.setNull(i+2,Types.TIMESTAMP);
					}
					else {
						statement.setTimestamp(i+2,(java.sql.Timestamp)parameterValue);
					}
				}
				else {
					throw new Exception("Internal error: unsupported parameter type: '"+
							parameterType+"' for the parameter: '"+parameterName+"'");
				}
			}
			statement.registerOutParameter(1,Types.VARCHAR);
			statement.execute();
			String result=statement.getString(1);

			if (dbmsOutput!=null) {
				dbmsOutput.show();
			}

			connection.commit();

			return result;
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return e.getMessage();
		}
	}

	public static TreeMap<Long,String> getIdToNameMap(Map<String,String> environmentProperties,boolean isDebug,
			JLabel messageLabel,String sqlQuery,String messageType)
	throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection=null;

		try
		{
			int rowsCount=getRowsCount(environmentProperties,sqlQuery, null, null);
			if ( messageLabel != null )
			{
				GUIUtils.showInProgressMessage(messageLabel,"Please wait, gathering "+messageType+" ( 0 / "+rowsCount+" ) ...");
			}
			connection =getDatabaseConnection(environmentProperties);

			if ( isDebug ){
				FileUtils.println("Executing the SQL:\n'"+sqlQuery+"'");
			}

			statement= connection.prepareStatement(sqlQuery);
			statement.execute();
			resultSet=statement.executeQuery();

			Map<Long,String> res=new HashMap<Long,String>();
			int LOG_BATCH_SIZE=10;
			int counter=0;
			while ( resultSet.next() )
			{
				counter++;
				if (counter%LOG_BATCH_SIZE==0 && messageLabel != null) {
					GUIUtils.showInProgressMessage(messageLabel,"Please wait, gathering "+messageType+" ( "+counter+" / "+rowsCount+" ) ...");
				}
				long id= resultSet.getLong(1);
				String name= resultSet.getString(2);
				if (name!=null) {
					name= name.replaceAll("\n", "");
				}
				res.put(id,name);
			}
			return new TreeMap<Long,String>(res);
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static TreeMap<Long,Long> convertMapValuesFromStringToIdMap(TreeMap<Long,String> sourceMap)
	throws Exception
	{
		TreeMap<Long,Long> toReturn=new TreeMap<Long,Long>();

		Iterator<Long> iterator=sourceMap.keySet().iterator();
		while (iterator.hasNext()) {
			Long key=iterator.next();
			String value=sourceMap.get(key);

			toReturn.put(key,Long.valueOf(value));
		}
		return toReturn;
	}
	
	public static void alterSession(Connection connection) throws Exception
	{
		if (ReverseMain.IS_FUSION_DB) {
			alterSession(connection,"FUSION");
		}
		else {
			alterSession(connection,"APPS");
		}
	}
	
	public static void alterSession(Connection connection,String user) throws Exception
	{
		PreparedStatement statement = null;
		try
		{
			statement = connection.prepareStatement("ALTER SESSION SET CURRENT_SCHEMA = "+user);
			statement.executeQuery();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(statement);
		}
	}
	public static Connection getJDBCConnectionFromJDBCStringGeneric(String jdbcString,boolean isRetryOn) throws SQLException, ClassNotFoundException
	{
		Connection con=null;
		Class.forName("oracle.jdbc.OracleDriver");
		
		int maxRetries=1;
		if (isRetryOn) {
			 maxRetries=CONNECTION_RETRY_COUNT;
		}
		for(int i = 1; i <= maxRetries; i++) {
			try {
				con=DriverManager.getConnection(jdbcString);
			} catch (SQLRecoverableException sqlRecoverableException) {
				if(i < maxRetries) {
					FileUtils.println("getJDBCConnectionGeneric, connection lost. Retries: " + i+" / "+maxRetries+" . Waiting for " + CONNECTION_RETRY_WAIT_SECONDS + " seconds before retry");
					try {
						Thread.sleep(CONNECTION_RETRY_WAIT_SECONDS * 1000);
					} catch (InterruptedException e) {
						FileUtils.printStackTrace(e);
					}
				} else {
					throw sqlRecoverableException;
				}
				continue;
			}
			break;
		}		
		return con;
	}
}