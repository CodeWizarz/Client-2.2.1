package com.rapidesuite.snapshot.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.view.AdminPasswordPanel;

public class PatchUtils {

	private static List<String> getDataTableNameWithNoSequenceColumnList(Connection connection,String user) throws Exception {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement=null;
		try {
			String sqlQuery="select TABLE_NAME from ( "+
					"Select Distinct Table_Name From All_Tab_Columns Where upper(OWNER) = '"+user.toUpperCase()+"' And Table_Name Like '"+ModelUtils.DB_TABLES_PREFIX+"%'"+
					" And Table_Name not Like '"+ModelUtils.DB_DFF_VALUE_SET_TABLES_PREFIX+"%' ) "+
					"where Table_Name Not In "+
					"(select TABLE_NAME from ALL_TAB_COLUMNS where upper(OWNER) = '"+user.toUpperCase()+"' and TABLE_NAME like '"+ModelUtils.DB_TABLES_PREFIX+"%' AND COLUMN_NAME='SEQ')";
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			List<String> toReturn=new ArrayList<String>();
			while (resultSet.next()) {
				String tableName=resultSet.getString(1);
				toReturn.add(tableName);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	private static void grantCreateSequenceToUser(Map<String, String> snapshotEnvironmentProperties,String user,String sysPassword) throws Exception {
		Connection connection=null;
		try {			
			String dbUser=AdminPasswordPanel.ADMIN_DB_NAME+" as sysdba";
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					dbUser,
					sysPassword);

			String sql="GRANT CREATE SEQUENCE TO "+user;
			ModelUtils.executeStatement(connection,sql);
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}			
	}

	public static void verifySoftwareVersion(Connection connection,String user,ControllerModalWindow controllerModalWindow) throws Exception {
		String msg="Analyzing Software version...";
		FileUtils.println(msg);

		msg="Analyzing Software version: 'SEQUENCE COLUMNS'...";
		controllerModalWindow.updateExecutionLabels(msg);
		FileUtils.println(msg);
		List<String> dataTableNameWithNoSequenceColumnList=getDataTableNameWithNoSequenceColumnList(connection,user);
		if (dataTableNameWithNoSequenceColumnList.isEmpty()) {
			msg="Analysis done!";
			FileUtils.println(msg);
			return;
		}
		FileUtils.println("dataTableNameWithNoSequenceColumnList:"+dataTableNameWithNoSequenceColumnList);
		throw new Exception("<html><body>This connection cannot be used with this version of RapidSnapshot.<br/>Please install an older version"+
				" of RapidSnapshot: <b>2.1.0.55075 or less</b> and try again.<br/><br/>If you wish continuing using this version of RapidSnapshot then create a new schema.");
	}

	public static void applyPatchAddSequenceColumn(JFrame frame,Connection connection,String user,
			ControllerModalWindow controllerModalWindow,Map<String, String> snapshotEnvironmentProperties) throws Exception {
		String msg="Analyzing patch 'SEQUENCE COLUMNS'...";
		FileUtils.println(msg);
		List<String> dataTableNameWithNoSequenceColumnList=getDataTableNameWithNoSequenceColumnList(connection,user);
		if (dataTableNameWithNoSequenceColumnList.isEmpty()) {
			msg="Patch already applied or no need of patching!";
			FileUtils.println(msg);
			return;
		}

		// prompt the DBA for the sys password in order to grant Sequence creation to the user.
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("<html><body>Please enter the <b>SYS</b> password in order to grant the following privilege:<br/>"+
				"<br/>\"<b>GRANT CREATE SEQUENCE TO "+user+"</b>\"<br/><br/>");
		JPasswordField password = new JPasswordField(10);
		panel.add(label);
		panel.add(password);
		int result = JOptionPane.showConfirmDialog(frame, panel,"Patching in progress...", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			String passwordValue = new String(password.getPassword());
			grantCreateSequenceToUser(snapshotEnvironmentProperties,user,passwordValue);
		}
		else {
			throw new Exception("Cancelling Operation.");
		}

		msg="Applying patch 'SEQUENCE COLUMNS' on "+dataTableNameWithNoSequenceColumnList.size()+" tables.";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		int index=0;
		for (String tableName:dataTableNameWithNoSequenceColumnList) {
			index++;

			String tableId=tableName.substring(ModelUtils.DB_TABLES_PREFIX.length());
			FileUtils.println("tableName:"+tableName);
			msg="Patching in progress. Altering DB tables: "+index+" / "+dataTableNameWithNoSequenceColumnList.size()+" ...";
			controllerModalWindow.updateExecutionLabels(msg);

			addSequenceColumnToTable(connection,user,tableName);
			addSequenceIndexToTable(connection,tableId);
		}
		FileUtils.println("Patch 'SEQUENCE COLUMNS' applied.");
	}

	private static void addSequenceIndexToTable(Connection connection,String tableId) throws SQLException {
		StringBuffer sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_SEQI ON ").append(ModelUtils.DB_TABLES_PREFIX).append(tableId).append("(seq)");
		ModelUtils.createIndex(connection,sqlBuffer.toString());
	}

	private static void addSequenceColumnToTable(Connection connection, String user, String tableName) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{	
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("ALTER TABLE ").append(user).append(".").append(tableName).append(" ADD (SEQ INT)");

			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static void applyPatches(JFrame frame,Connection connection,String dbSchema,
			ControllerModalWindow controllerModalWindow,Map<String, String> snapshotEnvironmentProperties) throws Exception {
		boolean hasConversionColumn=ModelUtils.hasColumnName(connection,dbSchema,"SNAPSHOT","IS_CONVERSION");
		FileUtils.println("applyPatches, hasConversionColumn:"+hasConversionColumn);
		if (!hasConversionColumn) {
			ModelUtils.alterTableAddIsConversionColumn(connection,dbSchema);
		}
		boolean hasUserTable=ModelUtils.hasTable(dbSchema, connection, "USERS");
		FileUtils.println("applyPatches, hasUserTable:"+hasUserTable);
		if (!hasUserTable) {
			applyPatchAddUserFunctionality(connection,controllerModalWindow);
		}
		
		boolean hasDummyFndSessionsTable=ModelUtils.hasTable(dbSchema, connection, "DUMMY_FND_SESSIONS");
		FileUtils.println("applyPatches, hasDummyFndSessionsTable:"+hasDummyFndSessionsTable);
		if (!hasDummyFndSessionsTable) {
			applyPatchAddDummyFndSessionsTable(connection,controllerModalWindow);
		}
		
		boolean dummyFndSessionsTableHasData=ModelUtils.tableHasData(dbSchema, connection, "DUMMY_FND_SESSIONS");
		FileUtils.println("applyPatches, dummyFndSessionsTableHasData:"+dummyFndSessionsTableHasData);
		if (!hasDummyFndSessionsTable) {
			applyPatchAddDataDummyFndSessionsTable(connection,controllerModalWindow);
		}
		boolean hasCreatedOnColumn = ModelUtils.hasColumnName(connection,dbSchema,"INVENTORY_TO_SNAPSHOT","CREATED_ON");
		FileUtils.println("applyPatches, hasCreatedOnColumn column:"+hasCreatedOnColumn);
		if(!hasCreatedOnColumn){
			applyPatchAddColumnCreatedOn(connection,controllerModalWindow);
		}
		boolean hasCompletedOnColumn = ModelUtils.hasColumnName(connection,dbSchema,"INVENTORY_TO_SNAPSHOT","COMPLETED_ON");
		FileUtils.println("applyPatches, hasCompletedOnColumn :"+hasCompletedOnColumn);
		if(!hasCompletedOnColumn){
			applyPatchAddColumnCompletedOn(connection,controllerModalWindow);
		}
				
		String tableName = "INVENTORY";
		String dataTypeClause = "VARCHAR(1)";
		String defaultValueClause = "DEFAULT 'Y'";
		applyPatchForTable(connection,dbSchema,tableName,"IS_EXECUTABLE",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"IS_LISTABLE",controllerModalWindow,dataTypeClause,defaultValueClause);

		tableName = "USERS";
		defaultValueClause = "DEFAULT 'N'";
		applyPatchForTable(connection,dbSchema,tableName,"IS_ENABLED_WORKERS",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"IS_ENABLED_CONNECT_FOLDR",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"IS_ENABLED_DOWNLD_FOLDR",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"IS_ENABLED_TMP_FOLDR",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"IS_ENABLED_SNP_PRFX",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"IS_ENABLED_RESET_OPTN",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"IS_ENABLED_DELETE_OPTION",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"IS_ENABLED_TOTAL_DETAIL_OPTION",controllerModalWindow,dataTypeClause,defaultValueClause);
		
		dataTypeClause = "TIMESTAMP";
		defaultValueClause = "";
		applyPatchForTable(connection,dbSchema,tableName,"OS_AUTH_LOGIN_DATE",controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,"LAST_PSWD_UPDATE_DATE",controllerModalWindow,dataTypeClause,defaultValueClause);
		
		
		defaultValueClause = "DEFAULT 'Y'";
		dataTypeClause = "VARCHAR2(1)";
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_OVERRIDES_EXISTS_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		defaultValueClause = "";
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_DISPLAY_UNSUPPORTED_INVENTORIES_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_DISPLAY_TOTAL_DETAILS_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_SHOW_HELPER_BALLOONS_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_BR100_VERTICAL_WAY_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_INC_SOFT_DELETED_OPTION_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		dataTypeClause = "VARCHAR2(5)";
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_DEFAULT_PARALLEL_WORKERS_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_DOWNLOAD_FORMAT_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);	
		dataTypeClause = "VARCHAR2(15)";
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_SNAPSHOT_DELETE_OPTION_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		dataTypeClause = "VARCHAR2(2000)";
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_SERVER_CONNECTIONS_FOLDER_LOCATION_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_DOWNLOAD_FOLDER_LOCATION_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_TEMPLATE_FOLDER_LOCATION_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);		
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_SNAPSHOT_PREFIX_NAME_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);		
		applyPatchForTable(connection,dbSchema,tableName,ModelUtils.PREF_SEEDED_USERS_DEFINITION_KEY,controllerModalWindow,dataTypeClause,defaultValueClause);			
		
		
		boolean hasTemplateColumn=ModelUtils.hasColumnName(connection,dbSchema,"SNAPSHOT","TEMPLATE_NAME");
		FileUtils.println("applyPatches, hasTemplateColumn:"+hasTemplateColumn);
		if (!hasTemplateColumn) {
			alterTableAddTemplateNameColumn(connection,dbSchema);
		}
		
		boolean hasTimeLimitColumn=ModelUtils.hasColumnName(connection,dbSchema,"SNAPSHOT","TIME_LIMIT");
		FileUtils.println("applyPatches, hasTimeLimitColumn:"+hasTimeLimitColumn);
		if (!hasTimeLimitColumn) {
			alterTableAddTimeLimitColumn(connection,dbSchema);
		}
		
	}
	public static void applyPatchForTable(Connection connection,String dbSchema,String tableName, String columnName,
			ControllerModalWindow controllerModalWindow,String dataType, String defaultValueOrNullCondition) throws Exception{
		boolean hasColumn = ModelUtils.hasColumnName(connection,dbSchema,tableName,columnName);
		FileUtils.println("applyPatches, has column"+columnName+" :"+hasColumn);
		if(!hasColumn){
			applyPatchAddColumn(connection,controllerModalWindow,tableName,columnName,dataType,defaultValueOrNullCondition);
		}		
	}
	
	public static void applyPatchAddDataDummyFndSessionsTable(Connection connection,
			ControllerModalWindow controllerModalWindow) throws Exception {
		String msg="Applying data patch 'DUMMY_FND_SESSIONS'...";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;
		
		try
		{
			sql="INSERT INTO DUMMY_FND_SESSIONS (SESSION_ID, EFFECTIVE_DATE) VALUES(USERENV('SESSIONID'), ADD_MONTHS(TO_DATE(TRUNC(SYSDATE)), 120)-1)";
			
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
		
		FileUtils.println("Data patch 'DUMMY_FND_SESSIONS' applied.");
		
	}
	
	public static void applyPatchAddDummyFndSessionsTable(Connection connection,
			ControllerModalWindow controllerModalWindow) throws Exception {
		String msg="Applying patch 'DUMMY_FND_SESSIONS'...";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;
		try
		{	
			sql="CREATE TABLE DUMMY_FND_SESSIONS "+
					"("+
					"  SESSION_ID			NUMBER			NOT NULL,"+
					"  EFFECTIVE_DATE		DATE			NOT NULL"+
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
			sql="INSERT INTO DUMMY_FND_SESSIONS (SESSION_ID, EFFECTIVE_DATE) VALUES(USERENV('SESSIONID'), ADD_MONTHS(TO_DATE(TRUNC(SYSDATE)), 120)-1)";
			
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
		
		FileUtils.println("Patch 'DUMMY_FND_SESSIONS' applied.");
		
	}

	public static void applyPatchAddUserFunctionality(Connection connection,
			ControllerModalWindow controllerModalWindow) throws Exception {
		String msg="Applying patch 'USERS'...";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;
		try
		{	
			sql="CREATE TABLE USERS "+
					"("+
					"  ID          NUMBER          NOT NULL,"+
					"  LOGIN_NAME		  VARCHAR(50)	  NOT NULL,"+
					"  PASSWORD		  VARCHAR(1000)	  NOT NULL,"+
					"  FULL_NAME VARCHAR(2000)	  NULL,"+
					"  IS_DELETED  VARCHAR2(1 CHAR)         DEFAULT 'N' NOT NULL,"+
					"  CREATED_ON  TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,"+
					"  SNAPSHOT_CREATION  varchar2(1 char) default 'N' not null,"+
					"  IS_ENABLED  varchar2(1 char) default 'N' not null,"+
					"  IS_MANAGER  varchar2(1 char) default 'N' not null,"+
					"  PRIMARY KEY (LOGIN_NAME,IS_DELETED)"+
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
			sql="CREATE SEQUENCE USER_ID_SEQ "+
					"    MAXVALUE 999999999999999999999999999 "+
					"    MINVALUE 1 "+
					"    NOCYCLE "+
					"    NOCACHE "+
					"    NOORDER";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
		
		try
		{
			sql="CREATE INDEX USERS_ID on USERS (id)";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();

			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
		
		try
		{
			sql="ALTER TABLE SNAPSHOT ADD USER_ID NUMBER";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
			connection.commit();			
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}		

		try
		{
			sql="ALTER TABLE SNAPSHOT RENAME COLUMN CREATED_BY TO OS_USER_NAME";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
			connection.commit();			
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
		
		FileUtils.println("Patch 'USERS' applied.");
	}
	public static void applyPatchAddColumnCreatedOn(Connection connection,ControllerModalWindow controllerModalWindow) throws Exception{
		String msg="Applying patch 'AddColumnCreatedOn'...";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;			
		try{
			sql="ALTER TABLE INVENTORY_TO_SNAPSHOT ADD CREATED_ON TIMESTAMP";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
			connection.commit();			
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}			
	}	
	public static void applyPatchAddColumnCompletedOn(Connection connection,ControllerModalWindow controllerModalWindow) throws Exception{
		String msg="Applying patch 'AddColumnCompletedOn'...";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;			
		try{
			sql="ALTER TABLE INVENTORY_TO_SNAPSHOT ADD COMPLETED_ON TIMESTAMP";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
			connection.commit();			
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}			
	}
	public static void applyPatchAddColumnIsExecutable(Connection connection,ControllerModalWindow controllerModalWindow) throws Exception{
		String msg="Applying patch 'AddColumnIsExecutable'...";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;			
		try{
			sql="ALTER TABLE INVENTORY ADD IS_EXECUTABLE VARCHAR(1)	DEFAULT 'Y'";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
			connection.commit();			
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}			
	}
	public static void applyPatchAddColumnIsListable(Connection connection,ControllerModalWindow controllerModalWindow) throws Exception{
		String msg="Applying patch 'AddColumnIsListable'...";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;			
		try{
			sql="ALTER TABLE INVENTORY ADD IS_LISTABLE VARCHAR(1)	DEFAULT 'Y'";
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
			connection.commit();			
		}
		finally{
			DirectConnectDao.closeQuietly(preparedStatement);
		}			
	}
	public static void applyPatchAddColumn(Connection connection,ControllerModalWindow controllerModalWindow,String tableName, String columnName, String dataType, String defaultValueOrNullCondition) throws Exception{
		String msg="Applying patch 'Add "+columnName+"' column...";
		FileUtils.println(msg);
		controllerModalWindow.updateExecutionLabels(msg);
		PreparedStatement preparedStatement = null;
		String sql=null;			
		try{
			sql="ALTER TABLE "+tableName+" ADD "+columnName+" "+dataType+" "+defaultValueOrNullCondition;
			preparedStatement = connection.prepareStatement(sql);	
			preparedStatement.executeUpdate();
			connection.commit();			
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}			
	}
	public static void alterTableAddTemplateNameColumn(Connection connection,String dbSchema) {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("ALTER TABLE ").append(dbSchema).append(".SNAPSHOT").append(" add(TEMPLATE_NAME  VARCHAR2(1000))");
						
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	public static void alterTableAddTimeLimitColumn(Connection connection,String dbSchema) {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("ALTER TABLE ").append(dbSchema).append(".SNAPSHOT").append(" add(TIME_LIMIT  NUMBER)");
						
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}	
	
}
