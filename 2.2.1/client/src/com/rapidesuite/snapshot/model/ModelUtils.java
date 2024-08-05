package com.rapidesuite.snapshot.model;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.springframework.util.Assert;

import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.client.dataConversion0000.DataConversionDocument;
import com.rapidesuite.client.dataConversionShared0000.DataConversionSharedDocument;
import com.rapidesuite.client.licenseClient0001.License;
import com.rapidesuite.client.licenseClient0001.LicenseDocument;
import com.rapidesuite.client.licenseClient0001.Plugin;
import com.rapidesuite.client.licenseClient0001.Plugins;
import com.rapidesuite.configurator.DataFactory;
import com.rapidesuite.configurator.client.SharedUtil;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.configurator.utility.LicenseEncryptor;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.Encryption;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.reverse.utils.DFFRow;
import com.rapidesuite.reverse.utils.DataExtractionUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotComparisonController;
import com.rapidesuite.snapshot.controller.convert.ConvertController;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;
import com.rapidesuite.snapshot.view.GenericRecordInformation;
import com.rapidesuite.snapshot.view.RegistrationAppliancePanel;
import com.rapidesuite.snapshot.view.RegistrationPanel;
import com.rapidesuite.snapshot.view.RegistrationShopPanel;
import com.rapidesuite.snapshot.view.SchemaManagementPanel;
import com.rapidesuite.snapshot.view.ServerFrame;
import com.rapidesuite.snapshot.view.SnapshotDownloadGridPanel;
import com.rapidesuite.snapshot.view.SnapshotInventoryDetailsGridPanel;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.SnapshotPackageSelectionPanel;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapidesuite.snapshot.view.convert.ConvertSourceGridRecordInformation;
import com.rapidesuite.snapshot.view.convert.ConvertSourcePanel;
import com.rapidesuite.snapshot.view.convert.ConvertTargetGridRecordInformation;
import com.rapidesuite.snapshot.view.convert.ConvertTargetPanel;
import com.rsc.rapidsnapshot.inventoryFormInfo0000.FormInfo;
import com.rsc.rapidsnapshot.inventoryFormInfo0000.Inventories;
import com.rsc.rapidsnapshot.inventoryFormInfo0000.InventoriesDocument;
import com.rsc.rapidsnapshot.snapshotKBMatrix0000.Entry;
import com.rsc.rapidsnapshot.snapshotKBMatrix0000.MatrixDocument;
import com.rsc.rapidsnapshot.snapshotKBMatrix0000.Snapshot;
import com.sun.jna.Platform;

public class ModelUtils {

	public static final String DB_STATUS_PROCESSING="P";
	public static final String DB_STATUS_PENDING="Q";
	public static final String DB_STATUS_SUCCESS="S";
	public static final String DB_STATUS_FAILED="E";
	public static final String DB_STATUS_WARNING="W";
	public static final String DB_STATUS_CANCELLED="C";
	public static final String DB_STATUS_UNSELECTED="U";
	public static final String DB_STATUS_RESET="R";
	public static final String DB_STATUS_TIME_LIMIT_EXCEEDED="T";
	
	public static final String FORM_LEVEL_START_TAG="<FORM_TYPE>";
	public static final String FORM_LEVEL_END_TAG="</FORM_TYPE>";
	public static final String GLOBAL_LEVEL="GLOBAL";
	public static final String OU_LEVEL="OU";
	public static final String BG_LEVEL="BG";
	public static final String IO_LEVEL="IO";
	public static final String LE_LEVEL="LE";
	
	public static final String DB_TABLES_PREFIX="XX_RSC_T";
	public static final String DB_DFF_TABLES_PREFIX=DB_TABLES_PREFIX+"DFF";
	public static final String DB_DFF_VALUE_SET_TABLES_PREFIX=DB_TABLES_PREFIX+"VS";
	public static final String DFF_GLOBAL_DATA_ELEMENTS_KEYWORD="Global Data Elements";
	public static final String DB_COMPARISON_TABLES_PREFIX="XX_RSC_C";
	public static final String DB_KB_VERSION_TABLE="VERSION_KB";
	
	public static final String SYSTEM_DATE="rssd";
	public static final String EK="r7OZBozv8kMhfQhX"; // 1234567891234567 r7OZBozv8kMhfQhX
	public static final String RSC_PREREQUISITE_OBJECTS_KEYWORD ="RSC_PREREQUISITE_OBJECTS";

	public static final String LARGE_COLUMN_LONG_TYPE="LONG";
	public static final String LARGE_COLUMN_CLOB_TYPE="CLOB";
	
	public static final boolean IS_ENABLE_EXCEL_CELL_FORMATTING=false;
	public final static String REPLACEMENT_SEPARATOR="##R4C##";
	public final static String DFF_FIELD_NAME="DFFN";
	public final static int DFF_FIELD_COUNT=51;
	
	public final static String LEGEL_ENTITY_FILTERING_IDENIFIER = "RSC_LE_FILTERING";
	public final static String LEGEL_ENTITY_FILTERING_IDENIFIER_REGEX = "--"  + LEGEL_ENTITY_FILTERING_IDENIFIER + "=[\\d]+";
	
	public final static String DISABLE_OU_AND_LEVEL_FILTER_IDENIFIER = "DISABLE_OU_AND_LEVEL_FILTER";
	public final static String DISABLE_OU_AND_LEVEL_FILTER_REMARK = "Operating Unit and Level Selection filtering is disabled for this Inventory. ";
	
	public final static String IS_ENABLED_WORKERS = "IS_ENABLED_WORKERS";
	public final static String IS_ENABLED_CONNECT_FOLDR = "IS_ENABLED_CONNECT_FOLDR";
	public final static String IS_ENABLED_DOWNLD_FOLDR = "IS_ENABLED_DOWNLD_FOLDR";
	public final static String IS_ENABLED_TMP_FOLDR = "IS_ENABLED_TMP_FOLDR";
	public final static String IS_ENABLED_SNP_PRFX = "IS_ENABLED_SNP_PRFX";
	public final static String IS_ENABLED_RESET_OPTN = "IS_ENABLED_RESET_OPTN";
	public final static String IS_ENABLED_DELETE_OPTION = "IS_ENABLED_DELETE_OPTION";
	public final static String IS_ENABLED_TOTAL_DETAIL_OPTION = "IS_ENABLED_TOTAL_DETAIL_OPTION";

	
	public final static String PREF_OVERRIDES_EXISTS_KEY = "PREF_OVERRIDES_EXISTS";
	public final static String PREF_DEFAULT_PARALLEL_WORKERS_KEY = "PREF_DEFAULT_PARALLEL_WORKERS";
	public final static String PREF_SERVER_CONNECTIONS_FOLDER_LOCATION_KEY = "PREF_CONNECTIONS_FOLDER_LOC";
	public final static String PREF_DOWNLOAD_FOLDER_LOCATION_KEY = "PREF_DOWNLOAD_FOLDER_LOCATION";
	public final static String PREF_TEMPLATE_FOLDER_LOCATION_KEY = "PREF_TEMPLATE_FOLDER_LOCATION";
	public final static String PREF_SNAPSHOT_PREFIX_NAME_KEY = "PREF_SNAPSHOT_PREFIX_NAME";
	public final static String PREF_DOWNLOAD_FORMAT_KEY = "PREF_DOWNLOAD_FORMAT";
	public final static String PREF_SNAPSHOT_DELETE_OPTION_KEY = "PREF_SNAPSHOT_DELETE_OPTION";
	public final static String PREF_DISPLAY_UNSUPPORTED_INVENTORIES_KEY = "PREF_DISPLAY_UNSUPPORTED_INV";
	public final static String PREF_DISPLAY_TOTAL_DETAILS_KEY = "PREF_DISPLAY_TOTAL_DETAILS";
	public final static String PREF_SHOW_HELPER_BALLOONS_KEY = "PREF_SHOW_HELPER_BALLOONS";
	public final static String PREF_BR100_VERTICAL_WAY_KEY = "PREF_BR100_VERTICAL_WAY";
	public final static String PREF_SEEDED_USERS_DEFINITION_KEY = "PREF_SEEDED_USERS_DEFINITION";
	public final static String PREF_INC_SOFT_DELETED_OPTION_KEY = "PREF_INC_SOFT_DELETED_OPTION";
	
	
	
	/*
	,  mac.last_updated_by  as rsc_last_updated_by
	,  mac.last_update_date  as rsc_last_update_date
	,  mac.created_by  as rsc_created_by 
	,  mac.creation_date  as rsc_creation_date
	,  mac.ORGANIZATION_ID as rsc_inv_org_id
	,  null as rsc_ou_id
	,  null as rsc_ledger_id
	,  null as rsc_bg_id
	,  null as rsc_coa_id
	*/
	public static final int INVENTORY_EXTRA_COLUMNS_COUNT=9;
	
	public static final String ORACLE_DEFAULT_SEEDED_USER_NAMES="'ANONYMOUS','AUTOINSTALL'"+
			",'CONCURRENT MANAGER','FEEDER SYSTEM','GUEST','INITIAL SETUP','APPSMGR','WIZARD'"+
			",'ORACLE12.0.0','ORACLE12.1.0','ORACLE12.2.0','ORACLE12.3.0','ORACLE12.4.0','ORACLE12.5.0'"+
			",'ORACLE12.6.0','ORACLE12.7.0','ORACLE12.8.0','ORACLE12.9.0','SYSADMIN'";
	
	/**
	 * 
	 * ENUM denoting the levels of data in EBS
	 * NOTE: MIND THE ORDER
	 * 
	 * @author hassan.jamil
	 */
	public enum SELECTION_LEVEL {
		GLOBAL("Global"), 
		BUSINESS_GROUP("Business Group"),
		LEDGER("Ledger / Legal Entity"),
		OPERATING_UNIT("Operating Unit"),
		INVENTORY_ORGANIZATION("Inventory Organization");
		
		private String level;
		
		SELECTION_LEVEL(String level) {
			this.level = level;
		}
		
		public String getLevel() {
			return this.level;
		}
	}
	
	public static File getPLSQLPackageFile(File folder) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".sql".toLowerCase());
			}
		};
		File toReturn=null;
		File[] listOfFiles = folder.listFiles(filter);
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			//System.out.println("file:"+file.getName());
			if (file.getName().toLowerCase().endsWith("_pkg.sql")) {
				return file;
			}
		}
		return toReturn;
	}
	
	public static String getUIStatusFromDBStatus(String dbStatus) {
		if (dbStatus==null) {
			return "";
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_PROCESSING)) {
			return UIConstants.UI_STATUS_PROCESSING;
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_WARNING)) {
			return UIConstants.UI_STATUS_WARNING;
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_FAILED)) {
			return  UIConstants.UI_STATUS_FAILED;
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_CANCELLED)) {
			return  UIConstants.UI_STATUS_CANCELLED;
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_SUCCESS)) {
			return  UIConstants.UI_STATUS_COMPLETED;
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_UNSELECTED)) {
			return  UIConstants.UI_STATUS_UNSELECTED;
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_PENDING)) {
			return UIConstants.UI_STATUS_PENDING;
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_RESET)) {
			return UIConstants.UI_STATUS_RESET;
		}
		if (dbStatus.equalsIgnoreCase(DB_STATUS_TIME_LIMIT_EXCEEDED)) {
			return UIConstants.UI_STATUS_TIME_LIMIT_EXCEEDED;
		}		
		return dbStatus;
	}
	
	public static int executeStatement(Connection connection,String sql) throws Exception {
		PreparedStatement stmt = null;
		try
		{
			//FileUtils.println("executeStatement, sql: '"+sql+"'");
			stmt = connection.prepareStatement(sql);
			int toReturn = stmt.executeUpdate();
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(stmt);
		}
	}
	
	public static final boolean isPackageValid(Connection connection,String schemaName,String packageName) throws Exception
	{
		final String sql = "select object_name, object_type, status from dba_objects  where upper(object_name) = upper(?)"+
			" and upper(OWNER) = '"+schemaName.toUpperCase()+"'";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try {
			pstmt = connection.prepareStatement(sql);
			pstmt.setString(1, packageName);
			FileUtils.println("isPackageValid, sql:"+sql);
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
			if (!toReturn) {
				return false;
			}
			if ( count != 2 ) //one record for package, one for header
			{
				FileUtils.println("Invalid State: did not find exactly 2 records in 'dba_objects' for status of package '" + packageName + "'; instead, found: " + count);
				throw new Exception("Invalid State!");
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(rs);
			DirectConnectDao.closeQuietly(pstmt);
		}
	}

	public static Map<String,File> getFileNameToFileMap(File rootFolder,boolean isRecursive)
	{
		Map<String,File> toReturn=new TreeMap<String,File>(String.CASE_INSENSITIVE_ORDER);
		File[] files=rootFolder.listFiles();
		if (files==null) {
			return toReturn;
		}
		for ( File file : rootFolder.listFiles())
		{
			if(file.isDirectory())
	        {
				if (isRecursive) {
					Map<String,File> tempMap=getFileNameToFileMap(file,isRecursive);
					toReturn.putAll(tempMap);
				}
	        }
	        else
	        {
	        	toReturn.put(getFileNameNoExtension(file),file);
	        }			
		}
		return toReturn;
	}
	
	public static List<File> getFileNameList(File folder,String fileNameWithoutExtension,boolean isRecursive,boolean isMatchFileName)
	{
		List<File> toReturn=new ArrayList<File>();
		File[] files=folder.listFiles();
		if (files==null) {
			return toReturn;
		}
		for ( File file : folder.listFiles())
		{
			if (file.isDirectory()) {
				if (isRecursive) {
					List<File> tempList=getFileNameList(file,fileNameWithoutExtension,true,isMatchFileName);
					toReturn.addAll(tempList);
				}
				else {
					continue;
				}
			}
			else {
				int extensionIndex = file.getName().indexOf(".");
				if (extensionIndex==-1) {
					continue;
				}
				String fileNameWithoutExtensionTemp = file.getName().substring(0, extensionIndex);
				if (isMatchFileName) {
					if(fileNameWithoutExtension.equalsIgnoreCase(fileNameWithoutExtensionTemp)) {
						toReturn.add(file);
					}
				}
				else {
					toReturn.add(file);
				}
			}
		}
		return toReturn;
	}

	public static String getFileNameNoExtension(File file)
	{
		int extensionIndex = file.getName().lastIndexOf(".");
		String name=file.getName();
		if (extensionIndex!=-1) {
			name=file.getName().substring(0, extensionIndex);
		}
		return name;
	}
	
	public static SnapshotGridRecord createSnapshot(Connection connection,String snapshotName,
			String snapshotDescription,boolean isConversion,int userId,String templateName)
			throws Exception
	{
		int snapshotId=getNextSequenceIdConnection(connection,"SNAPSHOT_ID");
		String mode="A";
		String osUserName=System.getProperty("user.name");
		String clientHostName=InetAddress.getLocalHost().getHostName();
		
		SnapshotGridRecord snapshotGridRecord=new SnapshotGridRecord();
		snapshotGridRecord.setName(snapshotName);
		snapshotGridRecord.setSnapshotId(snapshotId);
		snapshotGridRecord.setStatus(DB_STATUS_PROCESSING);
		snapshotGridRecord.setMode(mode);
		snapshotGridRecord.setOsUserName(osUserName);
		snapshotGridRecord.setClientHostName(clientHostName);
		snapshotGridRecord.setUserId(userId);
		snapshotGridRecord.setDescription(snapshotDescription);
		snapshotGridRecord.setConversion(isConversion);
		snapshotGridRecord.setTemplateName(templateName);
		
		insertSnapshot(connection,snapshotGridRecord,true);

		return snapshotGridRecord;
	}

	public static void insertSnapshot(Connection connection,SnapshotGridRecord snapshotGridRecord,boolean isUseCurrentTimestamp) 
	throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql=null;
			if (isUseCurrentTimestamp) {
				sql ="insert into SNAPSHOT(ID,NAME,DESCRIPTION,IS_DELETED,CREATED_ON,OS_USER_NAME,CREATION_MODE,"
					+ "status,CLIENT_HOSTNAME,is_conversion,user_id,COMPLETED_ON,TEMPLATE_NAME) "+
					"values(?,?,?,'N',CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?)";
			}
			else {
				sql ="insert into SNAPSHOT(ID,NAME,DESCRIPTION,IS_DELETED,CREATED_ON,OS_USER_NAME,CREATION_MODE,"
						+ "status,CLIENT_HOSTNAME,is_conversion,user_id,COMPLETED_ON,TEMPLATE_NAME) "+
						"values(?,?,?,'N',?,?,?,?,?,?,?,?,?)";
			}
			statement= connection.prepareStatement(sql);
			int index=1;
			statement.setInt(index++, snapshotGridRecord.getSnapshotId());
			statement.setString(index++, snapshotGridRecord.getName());
			statement.setString(index++, snapshotGridRecord.getDescription());
			if (!isUseCurrentTimestamp) {
				String createdOn=snapshotGridRecord.getCreatedOn();
				Timestamp timeStamp=getTimeStamp(createdOn);
				statement.setTimestamp(index++, timeStamp);
			}
			statement.setString(index++, snapshotGridRecord.getOsUserName());
			statement.setString(index++, snapshotGridRecord.getMode());
			statement.setString(index++, snapshotGridRecord.getStatus() );
			statement.setString(index++, snapshotGridRecord.getClientHostName());
			if (snapshotGridRecord.isConversion()) {
				statement.setString(index++,"Y");
			}
			else {
				statement.setString(index++,"N");
			}
			statement.setInt(index++,snapshotGridRecord.getUserId());
			String completedOn=snapshotGridRecord.getCompletedOn();
			if (completedOn==null) {
				completedOn="";
				statement.setString(index++,completedOn);
			}
			else {
				Timestamp timeStamp=getTimeStamp(completedOn);
				statement.setTimestamp(index++, timeStamp);
			}
			
			statement.setString(index++, snapshotGridRecord.getTemplateName());
			statement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static Timestamp getTimeStamp(String dateStr) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Date date = format.parse(dateStr);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		Timestamp timeStamp=new Timestamp(cal.getTimeInMillis());
		return timeStamp;
	}
	
	public static String getSQLQueryAfterKeywordsReplacement(
			SwiftGUIMain swiftGUIMain,File sqlQueryFile,File plsqlPackageFile,String dbSchema) throws Exception {
		String reverseFileContent=FileUtils.readContentsFromSQLFile(sqlQueryFile);
		Properties replacementsProperties=DataExtractionUtils.getReplacementsPropertiesClone(
				swiftGUIMain.getReplacementsProperties());
		reverseFileContent=DataExtractionUtils.replaceTokens(replacementsProperties,reverseFileContent);
		 
		String plsqlPackageName=ModelUtils.getPLSQLPackageName(plsqlPackageFile);
		String reverseFileContentNoKeywords= replacePLSQLPackageNameKeywords(reverseFileContent,plsqlPackageName,dbSchema);
		return reverseFileContentNoKeywords;
	}
	
	public static void createTables(Connection connection, String dbSchema, Inventory inventory, int tableId,
			int inventoryColumnsCount, boolean isConvertDFF, int largeColumnPosition) throws Exception {
		String preColumnsText="SEQ INT,SNAPSHOT_ID INT,";
		String postColumnsText="rsc_last_updated_by number,rsc_last_update_date DATE,rsc_created_by number,"+
				"rsc_creation_date DATE,rsc_inv_org_id number,rsc_ou_id number,rsc_ledger_id number,rsc_bg_id number,rsc_coa_id number";
		createTableDefinition(connection,inventoryColumnsCount,tableId,DB_TABLES_PREFIX,preColumnsText,postColumnsText,largeColumnPosition,
				isConvertDFF);
		createDataTableIndexes(connection,tableId,DB_TABLES_PREFIX);
		initComparisonTable(dbSchema,connection,inventory,tableId);
	}

	public static void executeSnapshotInventory(
			SwiftGUIMain swiftGUIMain,
			String dbSchema,
			File plsqlPackageFile,
			SnapshotCreationWorker snapshotCreationWorker,
			Inventory inventory,
			int tableId,
			boolean isTableAlreadyCreated,
			int snapshotId,
			File mainSQLQueryFile,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord,
			List<File> alternateReverseSQLFileList,
			boolean isSnapshotForConversion,
			int timeLimit
			) throws Exception {
		
		String sqlQuery=getSQLQueryAfterKeywordsReplacement(swiftGUIMain,mainSQLQueryFile,plsqlPackageFile,dbSchema);
		int largeColumnPosition=getLargeColumnPosition(sqlQuery);
		String largeColumnType=getLargeColumnType(sqlQuery);
		String dffStructureQuery=getDFFStructureQuery(sqlQuery);
		int inventoryColumnsCount=inventory.getFieldsUsedForDataEntry().size();
		boolean isConvertDFF=dffStructureQuery!=null && isSnapshotForConversion;
		String dffFormTitle=null;
		if (isConvertDFF) {
			dffFormTitle=getDFFFormTitle(dffStructureQuery);
		}
		
		//http://oracleapps4u.blogspot.com/2011/08/how-to-set-org-context-in-oracle-apps.html
		try{
			if (!isTableAlreadyCreated) {
				createTables(snapshotCreationWorker.getJDBCConnection(), 
						dbSchema,inventory,tableId,inventoryColumnsCount,isConvertDFF,largeColumnPosition);
			}
			else {
				String tableName=DB_TABLES_PREFIX+tableId;
				if (isConvertDFF) {
					boolean hasColumn=hasColumnName(snapshotCreationWorker.getJDBCConnection(),dbSchema,tableName,DFF_FIELD_NAME+"1");
					if (!hasColumn) {
						alterTableAddDFFColumnNames(snapshotCreationWorker.getJDBCConnection(),dbSchema,tableName);
					}
				}
				else {
					int xmlInventoryColumnsCount=inventoryColumnsCount;
					validateInventoryChanges(snapshotCreationWorker.getJDBCConnection(),tableName,xmlInventoryColumnsCount);
				}
			}

			boolean isCancel=false;
			boolean hasRSCPrerequisiteObjects=hasRSCPrerequisiteObjectsKeyword(sqlQuery);
			if (hasRSCPrerequisiteObjects) {
				isCancel=!hasPrerequisiteObjects(snapshotCreationWorker.getJDBCConnection(),sqlQuery);
			}
			if (isCancel) {
				snapshotInventoryGridRecord.setRemarks("No table found (custom table created by Oracle patches)");
				return;
			}
			
			String sequenceName="SEQ"+tableId;
			if (hasSequence(snapshotCreationWorker.getJDBCConnection(),sequenceName)) {
				dropSequence(dbSchema,snapshotCreationWorker.getJDBCConnection(),tableId);
			}
			createSequence(dbSchema,snapshotCreationWorker.getJDBCConnection(),tableId);

			if (dffStructureQuery==null) {
				Connection connection=snapshotCreationWorker.getJDBCConnection();
				DatabaseUtils.alterSession(connection,"APPS");
				runReverseSQLs(swiftGUIMain,inventory,plsqlPackageFile,dbSchema,connection,inventoryColumnsCount,mainSQLQueryFile,snapshotId,
						tableId,largeColumnPosition,largeColumnType,alternateReverseSQLFileList,timeLimit);
				snapshotInventoryGridRecord.setRemarks("Snapshot Completed.");
			}
			else {
				/*
				 * Creating a Temporary table to store all the columns from the query.
				 * Later on, we will only pick the columns (attributeX) that we need in the correct order as per the form:
				 * example: ATTRIBUTE4,ATTRIBUTE9,ATTRIBUTE_CATEGORY,ATTRIBUTE1,ATTRIBUTE2.
				 * Those selected columns will be inserted in the real RSC_TXX table in the correct order.
				 * 
				 * - create DFF TEMP table.
				 * - remove all rows from the DFF TEMP table where all attributes and attribute_category are empty.
				 * - update all rows from the DFF TEMP where there are IDs by running the value sets queries.
				 * - copy all rows from the DFF temp by reordering the columns in the correct order.
				 */
				String dffTempTableName=DB_DFF_TABLES_PREFIX+tableId;
				Connection connection=snapshotCreationWorker.getJDBCConnection();
				try{
					snapshotInventoryGridRecord.setRemarks("Executing DFF SQL Query.");
					runReverseDFFSQLs(swiftGUIMain,dbSchema,connection,mainSQLQueryFile,snapshotId,tableId,alternateReverseSQLFileList,dffTempTableName,plsqlPackageFile,timeLimit);
					snapshotInventoryGridRecord.setRemarks("DFF SQL Queries executed.");

					snapshotInventoryGridRecord.setRemarks("Building DFF Structure.");
					DFFStructure dffStructure=getDFFStructure(connection,dffStructureQuery);
					snapshotInventoryGridRecord.setRemarks("DFF Structure built.");
					/*
					 * If encountering the error:
					 * ORA-00904: "TOWN_OR_CITY": invalid identifier
					 * 
					 * THIS MEANS THE TITLE 'Location Address' IN THE DFF QUERY IS INCORRECT - IT IS FETCHING INCORRECT COLUMNS
					 * 
					 *   <RSC_PRE_STEPS_DFF_TABLE_QUERY>select * from table(##RES_PLSQL_PACKAGE_NAME##.get_table_for_dff((
					 *   SELECT APPLICATION_ID FROM FND_APPLICATION_VL WHERE BASEPATH LIKE '%PER_TOP%' and rownum = 1), 'Location Address'))
					 *   </RSC_PRE_STEPS_DFF_TABLE_QUERY>
					 */
					snapshotInventoryGridRecord.setRemarks("Deleting DFF Rows with empty attributes.");
					deleteDFFRowsWithEmptyAttributes(dbSchema,connection,tableId,snapshotId,dffStructure);
					snapshotInventoryGridRecord.setRemarks("DFF Rows with empty attributes deleted.");

					snapshotInventoryGridRecord.setRemarks("Executing value set queries.");
					updateDFFRowsForValueSets(dbSchema,connection,snapshotId,tableId,dffStructure, snapshotInventoryGridRecord);
					snapshotInventoryGridRecord.setRemarks("Value set queries executed.");

					DatabaseUtils.alterSession(connection,dbSchema);

					snapshotInventoryGridRecord.setRemarks("Copying DFF Rows in Sequence.");
					copyDFFRowsInSequence(connection,snapshotId,tableId,inventory,inventoryColumnsCount,dffStructure,isConvertDFF,dffFormTitle);
					snapshotInventoryGridRecord.setRemarks("DFF Rows in Sequence copied.");
				}
				finally {
					if (hasDFFTable(dbSchema,connection,tableId)) {
						snapshotInventoryGridRecord.setRemarks("Dropping temporary tables.");
						dropTable(dbSchema,connection,dffTempTableName);
						snapshotInventoryGridRecord.setRemarks("Temporary tables dropped.");
					}
					snapshotInventoryGridRecord.setRemarks("DFF Snapshot Completed.");
				}
			}
		}
		finally {
			String sequenceName="SEQ"+tableId;
			if (hasSequence(snapshotCreationWorker.getJDBCConnection(),sequenceName)) {
				dropSequence(dbSchema,snapshotCreationWorker.getJDBCConnection(),tableId);
			}
			DatabaseUtils.alterSession(snapshotCreationWorker.getJDBCConnection(),dbSchema);
		}
	}
		
	private static String getDFFFormTitle(String dffStructureQuery) throws Exception {
		//System.out.println("dffStructureQuery: $"+dffStructureQuery+"$");
		int indexOfComma=dffStructureQuery.indexOf(",");
		if (indexOfComma==-1) {
			throw new Exception("Invalid text in the <RSC_PRE_STEPS_DFF_TABLE_QUERY> tag of the SQL file. (Missing comma)");
		}
		int indexOfLastApostrophe=dffStructureQuery.lastIndexOf("'");
		if (indexOfLastApostrophe==-1) {
			throw new Exception("Invalid text in the <RSC_PRE_STEPS_DFF_TABLE_QUERY> tag of the SQL file. (Missing Last Apostrophe)");
		}
		String temp=dffStructureQuery.substring(indexOfComma+1,indexOfLastApostrophe).trim();
		int indexOfFirstApostrophe=temp.indexOf("'");
		if (indexOfFirstApostrophe==-1) {
			throw new Exception("Invalid text in the <RSC_PRE_STEPS_DFF_TABLE_QUERY> tag of the SQL file. (Missing First Apostrophe)");
		}
		temp=temp.substring(indexOfFirstApostrophe+1);
		temp=temp.replaceAll("''","'");
		String formTitle=temp;
		
		return formTitle;
	}

	private static void alterTableAddDFFColumnNames(Connection connection,String dbSchema,String tableName) {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("ALTER TABLE ").append(dbSchema).append(".").append(tableName).append(" add( ");
			for (int i=1;i<=DFF_FIELD_COUNT;i++) {
				sqlBuffer.append(DFF_FIELD_NAME).append(i).append(" varchar2(4000)");
				if (i<DFF_FIELD_COUNT) {
					sqlBuffer.append(",");
				}
			}
			sqlBuffer.append(")");
			
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

	private static void createDataTableIndexes(Connection connection,int tableId,String dbTablesPrefix) throws SQLException {
		StringBuffer sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_SID ON ").append(dbTablesPrefix).append(tableId).append("(SNAPSHOT_ID)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_LUB ON ").append(dbTablesPrefix).append(tableId).append("(rsc_last_updated_by)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_CB ON ").append(dbTablesPrefix).append(tableId).append("(rsc_created_by)");
		createIndex(connection,sqlBuffer.toString());
				
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_CD ON ").append(dbTablesPrefix).append(tableId).append("(rsc_creation_date)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_LD ON ").append(dbTablesPrefix).append(tableId).append("(rsc_last_update_date)");
		createIndex(connection,sqlBuffer.toString());
				
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_IOI ON ").append(dbTablesPrefix).append(tableId).append("(rsc_inv_org_id)");
		createIndex(connection,sqlBuffer.toString());
				
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_OUI ON ").append(dbTablesPrefix).append(tableId).append("(rsc_ou_id)");
		createIndex(connection,sqlBuffer.toString());
				
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_LI ON ").append(dbTablesPrefix).append(tableId).append("(rsc_ledger_id)");
		createIndex(connection,sqlBuffer.toString());
				
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_BGI ON ").append(dbTablesPrefix).append(tableId).append("(rsc_bg_id)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_COAI ON ").append(dbTablesPrefix).append(tableId).append("(rsc_coa_id)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" IT").append(tableId).append("_SEQI ON ").append(dbTablesPrefix).append(tableId).append("(seq)");
		createIndex(connection,sqlBuffer.toString());
	}
	
	private static void createComparisonTableIndexes(Connection connection,int tableId,String dbTablesPrefix,List<Integer> primaryKeysPositionList) throws SQLException {
		StringBuffer sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" ICT").append(tableId).append("_CID ON ").append(dbTablesPrefix).append(tableId).append("(COMPARISON_ID)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" ICT").append(tableId).append("_SBI ON ").append(dbTablesPrefix).append(tableId).append("(SOURCE_BASELINE_ID)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" ICT").append(tableId).append("_TBI ON ").append(dbTablesPrefix).append(tableId).append("(TARGET_BASELINE_ID)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" ICT").append(tableId).append("_LUB ON ").append(dbTablesPrefix).append(tableId).append("(rsc_last_updated_by)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" ICT").append(tableId).append("_CB ON ").append(dbTablesPrefix).append(tableId).append("(rsc_created_by)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" ICT").append(tableId).append("_LD ON ").append(dbTablesPrefix).append(tableId).append("(rsc_last_update_date)");
		createIndex(connection,sqlBuffer.toString());
		
		sqlBuffer=new StringBuffer("");
		sqlBuffer.append(" ICT").append(tableId).append("_PKS").append(" ON ").append(dbTablesPrefix).append(tableId).append("(");
		int counter=0;
		for (int primaryKeyPosition:primaryKeysPositionList) {
			sqlBuffer.append("C").append(primaryKeyPosition);
			if ( (counter+1) <primaryKeysPositionList.size()) {
				sqlBuffer.append(",");
			}
			counter++;
		}
		sqlBuffer.append(")");
		/*
		 * TODO:
		 * This is throwing an Oracle error because the length of all the Data columns exceeds the limit! (4k columns)
		 * ORA-01450: maximum key length (6398) exceeded
		 */
		//createIndex(connection,sqlBuffer.toString());
	}
	
	public static void createIndex(Connection connection,String indexSQL) throws SQLException {
		PreparedStatement preparedStatement=null;
		try {
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("CREATE INDEX ").append(indexSQL);
			
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally {
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static int getLargeColumnPosition(String reverseFileContent) {
		String startKeyword="<RSC_PRE_STEPS_LARGE_COLUMN_POSITION>";
		String endKeyword="</RSC_PRE_STEPS_LARGE_COLUMN_POSITION>";
		
		String content=getTagContent(reverseFileContent,startKeyword,endKeyword);
		if (content!=null) {
			//A table can contain only one LARGE (LONG) column. (from oracle)
			return Integer.valueOf(content);
		}
		return -1;
	}
	
	private static String getLargeColumnType(String reverseFileContent) {
		String startKeyword="<RSC_PRE_STEPS_LARGE_COLUMN_TYPE>";
		String endKeyword="</RSC_PRE_STEPS_LARGE_COLUMN_TYPE>";
		
		String content=getTagContent(reverseFileContent,startKeyword,endKeyword);
		if (content!=null) {
			return content;
		}
		return "";
	}
	
	public static String getTagContent(String reverseFileContent,String startKeyword,String endKeyword) {
		int startIndexOf=reverseFileContent.indexOf(startKeyword);
		if (startIndexOf!=-1) {
			int endIndexOf=reverseFileContent.indexOf(endKeyword);
			String content=reverseFileContent.substring(startIndexOf+startKeyword.length(), endIndexOf);
			return content;
		}
		return null;
	}
	
	public static String getDFFStructureQuery(String reverseFileContent) {
		String startKeyword="<RSC_PRE_STEPS_DFF_TABLE_QUERY>";
		String endKeyword="</RSC_PRE_STEPS_DFF_TABLE_QUERY>";
		
		String content=getTagContent(reverseFileContent,startKeyword,endKeyword);
		return content;
	}
	
	public static int getNextSequenceIdConnection(Connection connection,String sequenceName) throws ClassNotFoundException, SQLException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="SELECT "+sequenceName+".NEXTVAL FROM DUAL";

			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			resultSet.next();
			return resultSet.getInt(1);
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	private static void createTableDefinition(Connection connection,int inventoryColumnsCount,int tableId,String tablePrefix,
			String preColumnsText,String postColumnsText,int longColumnPosition,boolean isConvertDFF) throws Exception {
		PreparedStatement preparedStatement=null;
		try {
			StringBuffer sqlQueryBuffer=new StringBuffer("");

			sqlQueryBuffer.append("CREATE TABLE ").append(tablePrefix).
				append(tableId).append("(").append(preColumnsText);
			for (int i=1;i<=inventoryColumnsCount;i++) {
				if (longColumnPosition!=-1 && i==longColumnPosition) {
					sqlQueryBuffer.append("C").append(i).append(" clob,");
				}
				else {
					sqlQueryBuffer.append("C").append(i).append(" varchar2(4000),");
				}
			}
			if (isConvertDFF) {
				for (int i=1;i<=DFF_FIELD_COUNT;i++) {
					sqlQueryBuffer.append(DFF_FIELD_NAME).append(i).append(" varchar2(4000),");
				}
			}
			sqlQueryBuffer.append(postColumnsText).append(")");
		
			//System.out.println("%%%%% create table query: \n"+sqlQueryBuffer.toString());

			preparedStatement = connection.prepareStatement(sqlQueryBuffer.toString());
			preparedStatement.executeUpdate();
			connection.commit();
			preparedStatement.close();
		}
		finally {
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	private static String replacePLSQLPackageNameKeywords(String reverseFileContent,String plsqlPackageName,String dbSchema){
		/*
		sqlQueryReformatted=sqlQueryReformatted.replaceAll("##USER_ID_LIST##", "-999999");
		sqlQueryReformatted=sqlQueryReformatted.replaceAll("##LAST_UPDATED_BY_OPERATOR##", "not in");
		sqlQueryReformatted=sqlQueryReformatted.replaceAll("##OPERATING_UNIT_NAME##", "hello");
		sqlQueryReformatted=sqlQueryReformatted.replaceAll("##LAST_UPDATE_DATE_OPERATOR##", ">=");
		sqlQueryReformatted=sqlQueryReformatted.replaceAll("##SEEDED_DATE##", "01-JAN-1950");
		sqlQueryReformatted=sqlQueryReformatted.replaceAll("##BUSINESS_GROUP_ID##", "-1");
		*/
		return reverseFileContent.replaceAll("##RES_PLSQL_PACKAGE_NAME##", dbSchema+"."+plsqlPackageName);
	}
	
	private static void createDFFTempDataTable(String dbSchema,Connection connection,String reverseFileContent,int snapshotId,String tableName) throws Exception {
		PreparedStatement preparedStatement=null;
		try {
			StringBuffer sqlQueryBuffer=new StringBuffer("");
			sqlQueryBuffer.append("CREATE TABLE ").append(dbSchema).append(".").append(tableName).append(" as select ").
			append(" -1 as SEQ,").
			append(snapshotId).append(" as SNAPSHOT_ID,T.* FROM (").append(reverseFileContent).append(" ) T where 1=0 ");
			//FileUtils.println("createDFFTempDataTable: \n"+sqlQueryBuffer.toString());
			
			preparedStatement = connection.prepareStatement(sqlQueryBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally {
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	public static void updateInventoryToSnapshot(Connection connection,int tableId,int snapshotId,String status,String remarks,
			String executionTime,long rawTime,int totalRecords)throws SQLException, ParseException{
		updateInventoryToSnapshot(connection,tableId,snapshotId,status,remarks,executionTime,rawTime,totalRecords,null);
	}
	public static void updateInventoryToSnapshot(Connection connection,int tableId,int snapshotId,String status,String remarks,
			String executionTime,long rawTime,int totalRecords,String conditionedDate)
	throws SQLException,ParseException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String sql = null;
		String dateStatement = "";
		try
		{
			if(ModelUtils.DB_STATUS_PROCESSING.equals(status) && conditionedDate!=null){
				dateStatement = ",CREATED_ON=? ";				
			}else if(ModelUtils.DB_STATUS_SUCCESS.equals(status)&& conditionedDate!=null){
				dateStatement = ",COMPLETED_ON=? ";					
			}else if(ModelUtils.DB_STATUS_FAILED.equals(status)&& conditionedDate!=null){
				dateStatement = ",COMPLETED_ON=? ";	
			}
			sql ="update INVENTORY_TO_SNAPSHOT set "+
					"STATUS=?,MSG=?,EXECUTION_TIME_STRING=?,EXECUTION_RAW_TIME=?,TOTAL_RECORD=? "+dateStatement+ 
					"where SNAPSHOT_ID=? and INVENTORY_ID=?";

			statement= connection.prepareStatement(sql);

			int index=1;
			statement.setString(index++, status);
			statement.setString(index++, remarks);
			statement.setString(index++, executionTime);
			statement.setLong(index++, rawTime);
			statement.setInt(index++, totalRecords);
			if((ModelUtils.DB_STATUS_PROCESSING.equals(status) && conditionedDate!=null) 
					|| (ModelUtils.DB_STATUS_SUCCESS.equals(status)&& conditionedDate!=null)
					|| (ModelUtils.DB_STATUS_FAILED.equals(status)&& conditionedDate!=null)){
				SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
				Date date = format.parse(conditionedDate);
				java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
				statement.setTimestamp(index++, timeStampDate);
			}
			statement.setInt(index++, snapshotId);
			statement.setInt(index++, tableId);
			
			statement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	private static void runReverseDFFSQL(String dbSchema,Connection connection,String reverseFileContent,int snapshotId,int tableId,int timeLimit) throws Exception {
		PreparedStatement preparedStatement=null;
		try {
			StringBuffer sqlQueryBuffer=new StringBuffer("");
			sqlQueryBuffer.append("INSERT /*+ APPEND NOLOGGING */ INTO ").append(dbSchema).append(".").append(DB_DFF_TABLES_PREFIX).
			append(tableId).append(" select ").
			append(dbSchema).append(".SEQ").append(tableId).append(".nextval,").
			append(snapshotId).append(",T.* FROM (").append(reverseFileContent).
			append(" ) T");
			//FileUtils.println("runReverseDFFSQL: \n"+sqlQueryBuffer.toString());
			
			preparedStatement = connection.prepareStatement(sqlQueryBuffer.toString());	
			if(timeLimit>0){
				preparedStatement.setQueryTimeout(timeLimit*60);
			}
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally {
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	private static void validateInventoryChanges(Connection connection,String tableName,int xmlInventoryColumnsCount) throws Exception {
		int dbInventoryColumnsCount=getDBInventoryColumnsCount(connection,tableName);
		//FileUtils.println("tableName: "+tableName+" dbInventoryColumnsCount:"+dbInventoryColumnsCount+" xmlInventoryColumnsCount:"+xmlInventoryColumnsCount);
		if (dbInventoryColumnsCount!=xmlInventoryColumnsCount) {
			throw new Exception("A newer version of the Inventory has been detected!\nYou will need first to delete the Snapshot history"+
			" for that Inventory ('Reset' button) then try again.\nDB inventory columns: "+dbInventoryColumnsCount+" / XML inventory columns: "+xmlInventoryColumnsCount);
		}
	}
	
	private static void runReverseSQLs(SwiftGUIMain swiftGUIMain,Inventory inventory,File plsqlPackageFile,String dbSchema,Connection connection,int inventoryColumnsCount,File mainSQLQueryFile,
			int snapshotId,int tableId,int largeColumnPosition,String largeColumnType,List<File> alternateReverseSQLFileList,int timeLimit) throws Exception {
		File sqlQueryFile=mainSQLQueryFile;
		int indexOfAlternativeSQL=0;
		while (true) {
			try{
				runReverseSQL(swiftGUIMain,inventory,plsqlPackageFile,dbSchema,connection,inventoryColumnsCount,sqlQueryFile,snapshotId,tableId,largeColumnPosition,largeColumnType,timeLimit);
				break;
			}
			catch(Exception ex) {
				String message=ex.getMessage();
				if (message!=null && message.toLowerCase().indexOf("invalid identifier")!=-1 && alternateReverseSQLFileList!=null) {
					// let's retry the next alternative query if any else reraise the exception
					if ( (indexOfAlternativeSQL+1) <= alternateReverseSQLFileList.size()) {
						sqlQueryFile=alternateReverseSQLFileList.get(indexOfAlternativeSQL);
						FileUtils.println("Failed SQL query, retrying the alternate file: '"+sqlQueryFile.getAbsolutePath()+"' ...");

						indexOfAlternativeSQL++;
					}
					else {
						throw ex;
					}
				}
				else {
					throw ex;
				}
			}
		}
	}
	
	private static void runReverseDFFSQLs(SwiftGUIMain swiftGUIMain,String dbSchema,Connection connection,File mainSQLQueryFile,int snapshotId,int tableId
			,List<File> alternateReverseSQLFileList,String dffTempTableName,File plsqlPackageFile,int timeLimit) throws Exception {
		File sqlQueryFile=mainSQLQueryFile;
		int indexOfAlternativeSQL=0;
		while (true) {
			try{
				DatabaseUtils.alterSession(connection,"APPS");
				String sqlQuery=getSQLQueryAfterKeywordsReplacement(swiftGUIMain,sqlQueryFile,plsqlPackageFile,dbSchema);
				if (!hasDFFTable(dbSchema,connection,tableId)) {
					createDFFTempDataTable(dbSchema,connection,sqlQuery,snapshotId,dffTempTableName);
				}
				runReverseDFFSQL(dbSchema,connection,sqlQuery,snapshotId,tableId,timeLimit);
				break;
			}
			catch(Exception ex) {
				String message=ex.getMessage();
				if (message!=null && message.toLowerCase().indexOf("invalid identifier")!=-1 && alternateReverseSQLFileList!=null) {
					// let's retry the next alternative query if any else reraise the exception
					if ( (indexOfAlternativeSQL+1) <= alternateReverseSQLFileList.size()) {
						sqlQueryFile=alternateReverseSQLFileList.get(indexOfAlternativeSQL);
						FileUtils.println("Failed SQL query, retrying the alternate file: '"+sqlQueryFile.getAbsolutePath()+"' ...");

						indexOfAlternativeSQL++;
					}
					else {
						throw ex;
					}
				}
				else {
					throw ex;
				}
			}
		}
	}

	private static void runReverseSQL(
			SwiftGUIMain swiftGUIMain, Inventory inventory,
			File plsqlPackageFile,String dbSchema,Connection connection,int inventoryColumnsCount,File sqlQueryFile,int snapshotId,int tableId,
			int largeColumnPosition,String largeColumnType,int timeLimit) throws Exception {
		
		Assert.isTrue(inventory.getFieldsUsedForDataEntry().size() == inventoryColumnsCount, "ERROR: inventoryColumnsCount != FieldsUsedForDataEntry count.");
		
		PreparedStatement preparedStatement=null;
		try {
			String sqlQuery=getSQLQueryAfterKeywordsReplacement(swiftGUIMain,sqlQueryFile,plsqlPackageFile,dbSchema);
			String tableName=DB_TABLES_PREFIX+tableId;	
			
			
			
			int totalDataTableColumnsCount=inventoryColumnsCount+INVENTORY_EXTRA_COLUMNS_COUNT;
			if(isRunningSnapshotByPLBatching(sqlQuery)){
				//
				String fullTableName = dbSchema.concat(".").concat(tableName);
				sqlQuery = sqlQuery.replace("##SNAPSHOT_ID_TO_REPLACE##", snapshotId+"");
				sqlQuery = sqlQuery.replace("##TARGET_TABLE_TO_REPLACE##", fullTableName);
				
				//FileUtils.println("runReverseSQL(PL/SQL): \n"+sqlQuery.toString());
				preparedStatement = connection.prepareStatement(sqlQuery.toString());	
				preparedStatement.executeUpdate();
				connection.commit();
				FileUtils.println("(PL/SQL)INVENTORY : "+inventory.getName()+",,    after commit()");
			}else{
				//System.out.println("sqlQueryReformatted:\n"+sqlQueryReformatted);
				
				/*
				 * SKIP THIS FOR 'LONG' TABLES  as it throws a 
				 * ORA-00932: inconsistent datatypes: expected - got LONG
				 * 00932. 00000 -  "inconsistent datatypes: expected %s got %s"
				 */
				if (largeColumnPosition==-1) {
					int sqlQueryColumnCount=getQueryColumnsCount(connection,sqlQuery);
					//System.out.println("columns count from sql query: "+sqlQueryColumnCount+" totalDataTableColumnsCount:"+totalDataTableColumnsCount+
					//		" inventoryColumnsCount:"+inventoryColumnsCount);
					if (totalDataTableColumnsCount!=sqlQueryColumnCount) {
						throw new Exception("Mismatch between the number of inventory columns: "+inventoryColumnsCount
								+" and the number of SQL columns: "+(sqlQueryColumnCount-INVENTORY_EXTRA_COLUMNS_COUNT));
					}
				}
				StringBuffer sqlQueryBuffer=new StringBuffer("");
				sqlQueryBuffer.append("INSERT /*+ APPEND NOLOGGING */ INTO ").append(dbSchema).append(".").append(tableName).append(" (SEQ,SNAPSHOT_ID");	
				for (int i=1;i<=inventoryColumnsCount;i++) {
					sqlQueryBuffer.append(",C").append(i);
				}
				sqlQueryBuffer.append(",rsc_last_updated_by");
				sqlQueryBuffer.append(",rsc_last_update_date");
				sqlQueryBuffer.append(",rsc_created_by");
				sqlQueryBuffer.append(",rsc_creation_date");
				sqlQueryBuffer.append(",rsc_inv_org_id");
				sqlQueryBuffer.append(",rsc_ou_id");
				sqlQueryBuffer.append(",rsc_ledger_id");
				sqlQueryBuffer.append(",rsc_bg_id");
				sqlQueryBuffer.append(",rsc_coa_id");
				sqlQueryBuffer.append(") select ").append(dbSchema).append(".SEQ").append(tableId).append(".nextval,").append(snapshotId);
				
				if (largeColumnPosition!=-1) {
					List<Field> fields = inventory.getFieldsUsedForDataEntry();
					for (int i=0;i<inventoryColumnsCount;i++) {
						String nameHash = fields.get(i).getNameHash();
						if(StringUtils.isEmpty(nameHash)) {
							throw new Exception("Missing column NameHash");
						}
						if (largeColumnPosition!=-1 && i==largeColumnPosition-1) {
							if (largeColumnType.equalsIgnoreCase(LARGE_COLUMN_LONG_TYPE)) {
								sqlQueryBuffer.append(",TO_LOB(T.").append(nameHash).append(")");
							}
							else {
								sqlQueryBuffer.append(",T.").append(nameHash);
							}
						}
						else {
							sqlQueryBuffer.append(",T.").append(nameHash);
						}
					}
					sqlQueryBuffer.append(",T.rsc_last_updated_by,T.rsc_last_update_date,T.rsc_created_by,"+
					"T.rsc_creation_date,T.rsc_inv_org_id,T.rsc_ou_id,T.rsc_ledger_id,T.rsc_bg_id,T.rsc_coa_id");
				}
				else {
					sqlQueryBuffer.append(",T.* ");
				}
				sqlQueryBuffer.append(" from ( ").append(sqlQuery).append(" ) T");
				//sqlQueryBuffer.append(") select "+baselineId+",1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16 from dual");
				
				//FileUtils.println("runReverseSQL: \n"+sqlQueryBuffer.toString());
				preparedStatement = connection.prepareStatement(sqlQueryBuffer.toString());	
				
				if(timeLimit>0){
					preparedStatement.setQueryTimeout(timeLimit*60);
				}
				FileUtils.println("INVENTORY : "+inventory.getName()+"    Before preparedStatement.executeUpdate()");
				preparedStatement.executeUpdate();
				FileUtils.println("INVENTORY : "+inventory.getName()+"    After preparedStatement.executeUpdate()");
				connection.commit();
				FileUtils.println("INVENTORY : "+inventory.getName()+"    After commit()");
				
			}
			
			
			
		}catch(Exception e ){
			e.printStackTrace();
			throw e;
		}
		finally {
			FileUtils.println("INVENTORY : "+inventory.getName()+"    Before close connection.");
			DirectConnectDao.closeQuietly(preparedStatement);
			FileUtils.println("INVENTORY : "+inventory.getName()+"    After close connection.");
		}
	}
	
	private static void deleteDFFRowsWithEmptyAttributes(String dbSchema,Connection connection,
			int tableId,int snapshotId,DFFStructure dffStructure) throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("delete from ").append(dbSchema).append(".").
			append(DB_DFF_TABLES_PREFIX).append(tableId).append(" where SNAPSHOT_id=").append(snapshotId);
			
			Iterator<String> iterator=dffStructure.getDistinctDFFAttributeNames().iterator();
			while (iterator.hasNext()) {
				String attributeName=iterator.next();
				sqlBuffer.append(" and ").append(attributeName).append(" is null");
			}
			// Some records may have only the context code selected and nothing else (GLOBAL and context fields empty!)
			sqlBuffer.append(" and CONTEXT_CODE is null");
			//System.out.println("deleteDFFRowsWithEmptyAttributes:\n"+sqlBuffer.toString());
			statement= connection.prepareStatement(sqlBuffer.toString());
			statement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	private static int getQueryColumnsCount(Connection connection,String reverseFileContent) throws Exception {
		PreparedStatement preparedStatement=null;
		try {
			preparedStatement = connection.prepareStatement(reverseFileContent);	
			ResultSetMetaData rsmd = preparedStatement.getMetaData();
			return rsmd.getColumnCount();
		}
		finally {
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static DFFStructure getDFFStructure(Connection connection,String reverseFileContent) throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{			
			statement= connection.prepareStatement(reverseFileContent);
			
			//FileUtils.println("getDFFStructure:\n"+reverseFileContent);
			resultSet=statement.executeQuery();
			
			boolean hasFormLeftPromptColumn=false;
			ResultSetMetaData resultSetMetaData=resultSet.getMetaData();
			int columnCount=resultSetMetaData.getColumnCount();
			for (int i=1;i<=columnCount;i++) {
				String name=resultSetMetaData.getColumnName(i);
				if (name.equalsIgnoreCase("form_left_prompt")) {
					hasFormLeftPromptColumn=true;
					break;
				}
			}
			DFFStructure dffStructure=new DFFStructure();
			while ( resultSet.next() )
			{
				String contextCode=resultSet.getString("context_code");
				if (contextCode==null) {
					contextCode="";
				}
				String formLeftPromptValue=null;
				if (hasFormLeftPromptColumn) {
					formLeftPromptValue=resultSet.getString("form_left_prompt");
				}
				DFFRow dffRow=new DFFRow(
						resultSet.getString("column_name"),
						contextCode,
						SharedUtil.getYesOrNoAsBoolean(resultSet.getString("context_code_display_flag")),
						SharedUtil.getYesOrNoAsBoolean(resultSet.getString("global_flag")),
						resultSet.getString("context_display_name"),
						resultSet.getString("value_set_query"),
						formLeftPromptValue
						);
				
				dffStructure.getDistinctDFFAttributeNames().add(dffRow.getColumnName());
				
				if (dffRow.hasValueSetQuery() ) {
					if(dffRow.valueSetQueryContainsPlaceHolders()) {
						FileUtils.println("WARNING: valueSetQuery has place-holders. This is not a blocker, but should be resolved. Look into the REGEXes in DFFRow class and fix them or add new");
						//FileUtils.println(dffRow.getValueSetQuery());
					}
					String attributeName=dffRow.getColumnName();
					String valueSetQuery=dffRow.getValueSetQuery();
					//FileUtils.println("dffRow.getColumnName(): "+attributeName+" contextCode:"+contextCode+" dffRow.getValueSetQuery():"+dffRow.getValueSetQuery());
								
					Map<String,Map<String,List<String>>> attributeNameTovalueSetQueryToContextCodesMap=dffStructure.getAttributeNameToValueSetQueryToContextCodesMap();
					Map<String,List<String>> valueSetQueryToContextCodesMap=attributeNameTovalueSetQueryToContextCodesMap.get(attributeName);
					if (valueSetQueryToContextCodesMap==null) {
						valueSetQueryToContextCodesMap=new HashMap<String,List<String>>();
						attributeNameTovalueSetQueryToContextCodesMap.put(attributeName, valueSetQueryToContextCodesMap);
					}
					List<String> contextCodeList=valueSetQueryToContextCodesMap.get(valueSetQuery);
					if (contextCodeList==null) {
						contextCodeList=new ArrayList<String>();
						valueSetQueryToContextCodesMap.put(valueSetQuery, contextCodeList);
					}
					contextCodeList.add(contextCode);
				}
				
				if (contextCode.equalsIgnoreCase(DFF_GLOBAL_DATA_ELEMENTS_KEYWORD)) {
					dffStructure.getDffGlobalFieldToDFFRowList().add(dffRow);
				}
				else {
					List<DFFRow> dffContextFieldsRows=dffStructure.getDffContextCodeToDFFRowsMap().get(contextCode);
					if (dffContextFieldsRows==null ){
						dffContextFieldsRows=new ArrayList<DFFRow>();
						dffStructure.getDffContextCodeToDFFRowsMap().put(contextCode, dffContextFieldsRows);
					}
					dffContextFieldsRows.add(dffRow);
					
					if (dffRow.isContextCodeToDisplay()) {
						dffStructure.getDffContextCodeToDisplayNameMap().put(contextCode, dffRow.getContextDisplayName());
						dffStructure.setContextFieldDisplayed(true);
					}
				}
				
			}
			return dffStructure;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	/*
	 *  LOGIC IS:
	 *  update XX_TEST.RSC_TDFF111 set attribute1=(select NAME from TEMPVALUESET1XXX WHERE CODE=ATTRIBUTE1), 
	 * 							   set attribute2=(select NAME from TEMPVALUESET2XXX WHERE CODE=ATTRIBUTE2)
	 *  where baseline_id=1;
	 */
	private static void updateDFFRowsForValueSets(String dbSchema,Connection connection,int snapshotId,int tableId,DFFStructure dffStructure, 
			SnapshotInventoryGridRecord snapshotInventoryGridRecord)
			throws SQLException, ClassNotFoundException {
		PreparedStatement preparedStatement = null;
		List<String> tempDFFTables=new ArrayList<String>();
		try
		{			
			Map<String, Map<String, List<String>>> attributeNameToValueSetQueryToContextCodesMap=dffStructure.getAttributeNameToValueSetQueryToContextCodesMap();
			if (attributeNameToValueSetQueryToContextCodesMap.isEmpty()) {
				return;
			}

			String baseRemark = snapshotInventoryGridRecord.getRemarks();

			snapshotInventoryGridRecord.setRemarks(String.format("%s Getting required distinct Context Codes.", baseRemark));
			List<String> requiredDistinctContextCodes = new ArrayList<String>();
			StringBuffer sqlQueryBuffer=new StringBuffer("");
			sqlQueryBuffer.append("SELECT DISTINCT T.CONTEXT_CODE FROM ").append(dbSchema).append(".").append(DB_DFF_TABLES_PREFIX).append(tableId).append(" T ");
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			statement= connection.prepareStatement(sqlQueryBuffer.toString());
			resultSet=statement.executeQuery();
			while ( resultSet.next() )
			{
				String contextCode=resultSet.getString("CONTEXT_CODE");
				if (contextCode!=null) {
					requiredDistinctContextCodes.add(contextCode);
				}
			}
			snapshotInventoryGridRecord.setRemarks(String.format("%s Getting required distinct Context Codes Completed.", baseRemark));
			
			sqlQueryBuffer=new StringBuffer("");
			sqlQueryBuffer.append("update ").append(dbSchema).append(".").append(DB_DFF_TABLES_PREFIX).append(tableId).append(" T ");
			int tableSequence=1;
			boolean hasAtLeastOneColumnToUpdate=false;
			
			Iterator<String> iterator=attributeNameToValueSetQueryToContextCodesMap.keySet().iterator();
			while (iterator.hasNext()) {
				String attributeName=iterator.next();
				snapshotInventoryGridRecord.setRemarks(String.format("%s Attribute: '%s' (%d/%d).", baseRemark, attributeName, tableSequence, attributeNameToValueSetQueryToContextCodesMap.size()));
				//FileUtils.println("updateDFFRowsForValueSets, attributeName:"+attributeName);
				
				String tableName=DB_DFF_VALUE_SET_TABLES_PREFIX+tableId+"_"+tableSequence;
				tempDFFTables.add(tableName);
				if (!hasTable(dbSchema,connection,tableName)) {
					buildValueSetTempTable(dbSchema,connection,tableName);
				}
				Map<String, List<String>> valueSetQueryToContextCodesMap=attributeNameToValueSetQueryToContextCodesMap.get(attributeName);
				insertIntoValueSetTempTable(dbSchema,connection,tableName,attributeName,valueSetQueryToContextCodesMap, snapshotInventoryGridRecord, requiredDistinctContextCodes);

				if (tableSequence==1) {
					// Only one keyword SET even if many columns to update
					// otherwise: ORA-01747: invalid user.table.column, table.column, or column specification
					sqlQueryBuffer.append(" set ");
				} else {
					// else set ','
					sqlQueryBuffer.append(",");
				}
				
				sqlQueryBuffer.append(attributeName).append(" = ( Select Nvl( (Select Display ").append(" FROM ").append(dbSchema).append(".").
				append(tableName).append(" T1 where T1.CONTEXT_CODE=T.CONTEXT_CODE AND T1.CODE=T.").append(attributeName).append(" and rownum=1 ),T.").append(attributeName).append(") From Dual )");

				sqlQueryBuffer.append("\n");
				tableSequence++;
				hasAtLeastOneColumnToUpdate=true;
			}
			sqlQueryBuffer.append(" where SNAPSHOT_id=").append(snapshotId);

			if (hasAtLeastOneColumnToUpdate) {
				snapshotInventoryGridRecord.setRemarks(String.format("%s Copying data to DFF.", baseRemark));

				preparedStatement= connection.prepareStatement(sqlQueryBuffer.toString());
				preparedStatement.execute();
				connection.commit();
				
				snapshotInventoryGridRecord.setRemarks(String.format("%s Copyied data to DFF.", baseRemark));
			}			
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
			dropValueSetTempTables(dbSchema,connection,tempDFFTables);
		}
	}

	private static void buildValueSetTempTable(String dbSchema,Connection connection,String tableName) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("CREATE TABLE ").append(dbSchema).append(".").append(tableName).append(" ( ").
			append("CONTEXT_CODE").append(" varchar2(500),").
			append("CODE").append(" varchar2(500),").
			append("DISPLAY").append(" varchar2(4000) )");
			
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			
			sqlBuffer=new StringBuffer("");
			sqlBuffer.append(dbSchema).append(".").append("IVS_").append(tableName).append("_CID ON ").
			append(dbSchema).append(".").append(tableName).append("(CONTEXT_CODE, CODE)");			
			createIndex(connection, sqlBuffer.toString());
			
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	private static void insertIntoValueSetTempTable(String dbSchema,Connection connection,String tableName,
			String attributeName,Map<String, List<String>> valueSetQueryToContextCodesMap, 
			SnapshotInventoryGridRecord snapshotInventoryGridRecord, 
			List<String> requiredDistinctContextCodes) throws SQLException {
		
		String baseRemark = snapshotInventoryGridRecord.getRemarks();
		int valueSetQuerySequence = 1;
		
		Iterator<String> iterator=valueSetQueryToContextCodesMap.keySet().iterator();
		//FileUtils.println("### insertIntoValueSetTempTable, attributeName: "+attributeName+" tableName: "+tableName+" valueSetQueryToContextCodesMap:"+valueSetQueryToContextCodesMap.size());
		while (iterator.hasNext()) {
			String valueSetQuery=iterator.next();
			snapshotInventoryGridRecord.setRemarks(String.format("%s Value Set Query (%d/%d).", baseRemark, valueSetQuerySequence, valueSetQueryToContextCodesMap.size()));
			List<String> contextCodeList=valueSetQueryToContextCodesMap.get(valueSetQuery);			
			//FileUtils.println("insertIntoValueSetTempTable, valueSetQuery:"+valueSetQuery);
			//FileUtils.println("insertIntoValueSetTempTable, contextCodeList:"+contextCodeList.size());

			if (valueSetQuery.indexOf(":")!=-1) {
				// The Value set query contains some placeholder that Oracle will use to put dynamic values...
				// We don't know what to substitute those for so unsupported.
				// The reversed value will be the ID or CODE instead of the displayed value.
				continue;
			}
			
			String baseRemarkIterative = snapshotInventoryGridRecord.getRemarks();			
			int contextCodeSequence = 1;
			for (String contextCode:contextCodeList) {
				if(!requiredDistinctContextCodes.contains(contextCode)) {
					continue;
				}
				snapshotInventoryGridRecord.setRemarks(String.format("%s ContextCode: '%s' (%d/%d).", baseRemarkIterative, contextCode, contextCodeSequence, contextCodeList.size()));
				insertIntoValueSetTempTableSingle(dbSchema,connection,tableName,
						attributeName,contextCode,valueSetQuery);
				contextCodeSequence++;
			}
			valueSetQuerySequence++;
		}
		//FileUtils.println("### COMPLETED ###");
	}

	private static void insertIntoValueSetTempTableSingle(String dbSchema,Connection connection,String tableName,
		String attributeName,String contextCode,String valueSetQuery)  throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{		
			valueSetQuery=valueSetQuery.replaceAll("FND_PROFILE.","APPS.FND_PROFILE.");
			
			valueSetQuery=valueSetQuery.replaceAll("DUMMY_FND_SESSIONS",dbSchema + ".DUMMY_FND_SESSIONS");

			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("INSERT INTO ").append(dbSchema).append(".").append(tableName).append("(CONTEXT_CODE,CODE,DISPLAY) ");
			sqlBuffer.append("SELECT distinct * from (");
			sqlBuffer.append("select '").append(contextCode).append("',Q.* from (").append(valueSetQuery).append(") Q");
			sqlBuffer.append(")");
				
			//FileUtils.println("insertIntoValueSetTempTableSingle: \n"+sqlBuffer.toString());
			preparedStatement= connection.prepareStatement(sqlBuffer.toString());
			preparedStatement.execute();
			connection.commit();
		}
		catch (Exception e) {
			FileUtils.println("Error executing Value Set query for table: '"+tableName+"' attributeName:'"+attributeName+"' contextCode: '"+
					contextCode+"' query: \n'"+valueSetQuery+"'");
			FileUtils.printStackTrace(e);
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	private static void dropValueSetTempTables(String dbSchema,Connection connection,List<String> tableNames) throws SQLException {
		for (String tableName:tableNames) {
			dropTable(dbSchema,connection,tableName);
		}
	}
	
	public static void dropTable(String dbSchema,Connection connection,String tableName) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("DROP TABLE ").append(dbSchema).append(".").append(tableName);
			
			//System.out.println("dropTempTable: \n"+sqlBuffer.toString());
			
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
	
	
	private static boolean hasSequence(Connection connection,String sequenceName) throws Exception {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement=null;
		try {
			String sqlQuery="SELECT COUNT(*) FROM user_sequences WHERE sequence_name = '"+sequenceName.toUpperCase()+"'";
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			resultSet.next();
			int cnt=resultSet.getInt(1);
			return cnt==1;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static void hasSequence(String dbSchema,Connection connection,int tableId) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("; ").append(dbSchema).append(".SEQ").append(tableId);
			
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static void createSequence(String dbSchema,Connection connection,int tableId) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("CREATE SEQUENCE ").append(dbSchema).append(".SEQ").append(tableId);
			
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static void dropSequence(String dbSchema,Connection connection,int tableId) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("DROP SEQUENCE ").append(dbSchema).append(".SEQ").append(tableId);
						
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	/*
	 * LOGIC:
	 * 
	 * run the <XML> tag DFF query from the Reverse SQL file comment section which will return the structure of the DFF.
	 * 
	 * On the form, it must be the following sequence:
	 * 
	 * - (Optional) GLOBAL fields first (Global data elements)
	 * - (Optional) a context_code (Dropdown list to select)
	 * - (Optional) context values dependent of the context code selected above:
	 * 		- context code A may have 2 context fields and B has 3 context fields.
	 * 
		ATTRIBUTE1	Global Data Elements	Y	Y	Global Data Elements	
		ATTRIBUTE3	Global Data Elements	Y	Y	Global Data Elements	
		ATTRIBUTE5	Global Data Elements	Y	Y	Global Data Elements	
		ATTRIBUTE4	Global Data Elements	Y	Y	Global Data Elements	
		ATTRIBUTE9	84	Y	N	84	
		ATTRIBUTE8	83	Y	N	83	
		ATTRIBUTE6	83	Y	N	83	
		ATTRIBUTE6	84	Y	N	84	
		ATTRIBUTE7	84	Y	N	84	
		ATTRIBUTE7	83	Y	N	83	

	- We need to copy the columns we need from the TEMP DFF table to the main DFF inventory table.
    - The order is controlled by the query above doing:
    		- Use all the Global Data Elements rows first in the same order if any
    		- Then if there is a value in the context_code process the context values rows.
    - The copy is done with a CASE on the context_code value because a different context code value may result in a 
    different attribute column being copied.
    Example:
    	
    	SOURCE DFF TABLE:
    	=================
    	CONTEXT_CODE ATT1 ATT2 ATT3 ATT4 ATT5 ATT6 ATT7 ATT8 ATT9
    		83		  G1        G2   G4   G3   C2   C3   C1
    		84		  G1        G2   G4   G3   C2   C3   	  C1
    		
    	INV DFF TARGET AFTER COPY:
    	=========================
    	FIELD0	F1	F2	F3	F4	F5	F5	F6	F7	F8	F9
    	  G1	G2	G3	G4	83	C1	C2	C3
    	  G1	G2	G3	G4	84	C1	C2	C3
	 */
	private static void copyDFFRowsInSequence(Connection connection,int snapshotId,int tableId,Inventory inventory,
			int inventoryColumnsCount,DFFStructure dffStructure,boolean isConvertDFF, String dffFormTitle) throws Exception {
		PreparedStatement preparedStatement=null;
		try {
			Set<String> distinctContextCode = getDistinctContextCodeForDFF(connection,snapshotId,tableId,inventory);
			Map<String, String> dffContextCodeToDisplayNameMap = getDffContextCodeToDisplayNameMap(distinctContextCode,dffStructure.getDffContextCodeToDisplayNameMap());
			Map<String,List<DFFRow>> dffContextCodeToDFFRowsMap = getDffContextCodeToDFFRowsMap(distinctContextCode,dffStructure.getDffContextCodeToDFFRowsMap());
			//FileUtils.println("dffContextCodeToDisplayNameMap's size : "+dffContextCodeToDisplayNameMap.size());
			//FileUtils.println("dffContextCodeToDFFRowsMap's size : "+dffContextCodeToDFFRowsMap.size());			
			String contextPromptName=null;
			if (isConvertDFF) {
				contextPromptName=getDFFContextPromptName(connection,dffFormTitle);
			}
			int currentInventoryColumnIndex=0;
			StringBuffer sqlQueryBuffer=new StringBuffer("");
			
			StringBuffer sqlColumnsBuffer=new StringBuffer("");
			StringBuffer sqlValuesBuffer=new StringBuffer("");
			StringBuffer sqlColumnsDFFPromptNamesBuffer=new StringBuffer("");
			StringBuffer sqlValuesDFFPromptNamesBuffer=new StringBuffer("");
			
			sqlColumnsBuffer.append("INSERT INTO ").append(DB_TABLES_PREFIX).
			append(tableId).append(" (SEQ,SNAPSHOT_ID");
			sqlValuesBuffer.append(" select SEQ,").append(snapshotId);
			
			// ADD ALL THE PRIMARY KEYS COLUMNS FIRST.
			List<Field> fieldsPK=inventory.getFieldsUsedForPrimaryKey();
			for (int i=1;i<=fieldsPK.size();i++) {
				sqlValuesBuffer.append(", PK").append(i);
				currentInventoryColumnIndex++;
				sqlColumnsBuffer.append(", C").append(currentInventoryColumnIndex);
			}		
			
			// ADD ALL THE GLOBAL ATTRIBUTES COLUMNS FIRST.
			List<DFFRow> globalDFFRows=dffStructure.getDffGlobalFieldToDFFRowList();
			int dffFieldInventoryIndex=1;
			if (globalDFFRows!=null) {
				for (DFFRow globalDFFRow:globalDFFRows) {
					if (isConvertDFF){
						sqlColumnsDFFPromptNamesBuffer.append(",").append(DFF_FIELD_NAME).append(dffFieldInventoryIndex);
						sqlValuesDFFPromptNamesBuffer.append(", '").append(globalDFFRow.getFormLeftPrompt().replaceAll("'", "''")).append("'").
						append(" as ").append(DFF_FIELD_NAME).append(dffFieldInventoryIndex);
					}
					sqlValuesBuffer.append(", ").append(globalDFFRow.getColumnName().replaceAll("'", "''")).append(" as FIELD").append(dffFieldInventoryIndex);
										
					currentInventoryColumnIndex++;
					sqlColumnsBuffer.append(", C").append(currentInventoryColumnIndex);
					dffFieldInventoryIndex++;
				}
			}
		
			if (dffStructure.hasContextValuesAttributesDefined()) {
				//skipping one field or not depending of the need to show the context name (is_displayable).
				boolean isContextFieldDisplayed=dffStructure.isContextFieldDisplayed();
				if (isContextFieldDisplayed) {
					Iterator<String> iterator=dffContextCodeToDisplayNameMap.keySet().iterator();
					sqlValuesBuffer.append(",\n(case CONTEXT_CODE\n");
					if (isConvertDFF){
						sqlColumnsDFFPromptNamesBuffer.append(",").append(DFF_FIELD_NAME).append(dffFieldInventoryIndex);
						sqlValuesDFFPromptNamesBuffer.append(", '").append(contextPromptName.replaceAll("'", "''")).append("'").
						append(" as ").append(DFF_FIELD_NAME).append(dffFieldInventoryIndex);
					}
					if(!dffContextCodeToDisplayNameMap.isEmpty() && dffContextCodeToDisplayNameMap.size()>0){
						while (iterator.hasNext()) {
							String contextCode=iterator.next();
							String displayName=dffContextCodeToDisplayNameMap.get(contextCode);

							sqlValuesBuffer.append(" when '").append(contextCode.replaceAll("'", "''")).append("' then '").append(displayName.replaceAll("'", "''")).append("'\n");
						}						
					}else{
						sqlValuesBuffer.append(" when '' then ").append("''").append("\n");
					}					
					sqlValuesBuffer.append(" else '' end ) as FIELD").append(dffFieldInventoryIndex).append("\n");
					currentInventoryColumnIndex++;
					sqlColumnsBuffer.append(", C").append(currentInventoryColumnIndex);
										
					dffFieldInventoryIndex++;
				}
				
				
				// ADD ALL THE CONTEXT VALUES ATTRIBUTES COLUMNS FIRST.
				for (int i=currentInventoryColumnIndex;i<inventoryColumnsCount;i++) { 
					Iterator<String> iterator=dffContextCodeToDFFRowsMap.keySet().iterator();				
					
					sqlValuesBuffer.append(",\n(case CONTEXT_CODE\n");
					if (isConvertDFF){
						sqlValuesDFFPromptNamesBuffer.append(",\n(case CONTEXT_CODE\n");
					}
					if(!dffContextCodeToDFFRowsMap.isEmpty() && dffContextCodeToDFFRowsMap.size()>0){
						while (iterator.hasNext()) {
							String key=iterator.next();
							List<DFFRow> contextDFFRows=dffContextCodeToDFFRowsMap.get(key);
												
							if (contextDFFRows.isEmpty()) {
								sqlValuesBuffer.append(" when '' then ").append("''").append("\n");
								if (isConvertDFF){
									sqlValuesDFFPromptNamesBuffer.append(" when '' then ").append("''").append("\n");
								}
							}
							else {
								// We process the first attribute then remove it.
								DFFRow contextDFFRow=contextDFFRows.get(0);
								contextDFFRows.remove(0);
								sqlValuesBuffer.append(" when '").append(key.replaceAll("'", "''")).append("' then ").append(contextDFFRow.getColumnName().replaceAll("'", "''")).append("\n");
								if (isConvertDFF){
									sqlValuesDFFPromptNamesBuffer.append(" when '").append(key.replaceAll("'", "''")).append("' then '").append(contextDFFRow.getFormLeftPrompt().replaceAll("'", "''")).append("'\n");
								}
							}
						}						
					}else{
						sqlValuesBuffer.append(" when '' then ").append("''").append("\n");
						if (isConvertDFF){
							sqlValuesDFFPromptNamesBuffer.append(" when '' then ").append("''").append("\n");
						}
					}
					sqlValuesBuffer.append(" else '' end ) as FIELD").append(dffFieldInventoryIndex).append("\n");
					if (isConvertDFF){
						sqlValuesDFFPromptNamesBuffer.append(" else '' end ) as ").append(DFF_FIELD_NAME).append(dffFieldInventoryIndex).append("\n");
						sqlColumnsDFFPromptNamesBuffer.append(",").append(DFF_FIELD_NAME).append(dffFieldInventoryIndex);
					}
					currentInventoryColumnIndex++;
					sqlColumnsBuffer.append(", C").append(currentInventoryColumnIndex);
					dffFieldInventoryIndex++;
				}
			}
			
			sqlValuesBuffer.append(",rsc_last_updated_by");
			sqlValuesBuffer.append(",rsc_last_update_date");
			sqlValuesBuffer.append(",rsc_created_by");
			sqlValuesBuffer.append(",rsc_creation_date");
			sqlValuesBuffer.append(",rsc_inv_org_id");
			sqlValuesBuffer.append(",rsc_ou_id");
			sqlValuesBuffer.append(",rsc_ledger_id");
			sqlValuesBuffer.append(",rsc_bg_id");
			sqlValuesBuffer.append(",rsc_coa_id");
			
			sqlColumnsBuffer.append(",rsc_last_updated_by");
			sqlColumnsBuffer.append(",rsc_last_update_date");
			sqlColumnsBuffer.append(",rsc_created_by");
			sqlColumnsBuffer.append(",rsc_creation_date");
			sqlColumnsBuffer.append(",rsc_inv_org_id");
			sqlColumnsBuffer.append(",rsc_ou_id");
			sqlColumnsBuffer.append(",rsc_ledger_id");
			sqlColumnsBuffer.append(",rsc_bg_id");
			sqlColumnsBuffer.append(",rsc_coa_id");
			
			if (isConvertDFF){
				sqlColumnsBuffer.append(sqlColumnsDFFPromptNamesBuffer);
			}
			sqlColumnsBuffer.append(") ");
			
			sqlQueryBuffer.append(sqlColumnsBuffer).append(sqlValuesBuffer);
			if (isConvertDFF){
				sqlQueryBuffer.append(sqlValuesDFFPromptNamesBuffer);
			}			
			sqlQueryBuffer.append(" from ").append(DB_DFF_TABLES_PREFIX).append(tableId).append(" where SNAPSHOT_id=").append(snapshotId);
			
			//FileUtils.println("copyDFFRowsInSequence: \n"+sqlQueryBuffer.toString());
			
			preparedStatement = connection.prepareStatement(sqlQueryBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally {
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	private static Set<String> getDistinctContextCodeForDFF(Connection connection,int snapshotId,int tableId,Inventory inventory) throws Exception{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		Set<String> toReturn = new HashSet<String>();
		try{
			StringBuffer sqlQueryBuffer=new StringBuffer("");
			sqlQueryBuffer.append("SELECT DISTINCT CONTEXT_CODE ");
			sqlQueryBuffer.append(" FROM ").append(DB_DFF_TABLES_PREFIX).append(tableId).append(" WHERE SNAPSHOT_id=").append(snapshotId);
			preparedStatement = connection.prepareStatement(sqlQueryBuffer.toString());	
			resultSet=preparedStatement.executeQuery();
			while (resultSet.next()){
				toReturn.add(resultSet.getString("CONTEXT_CODE"));				
			}
			return toReturn;
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Unable to get all of context code from snapshot_id : '"+snapshotId+"', inventory : "+inventory.getName());
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}	
	}
	private static Map<String, String> getDffContextCodeToDisplayNameMap(Set<String> distinctContextCode,Map<String, String> dffContextCodeToDisplayNameMap) throws Exception{
		Map<String, String> toReturn = new HashMap<String, String>();
		try{
			Iterator<String> iterator=dffContextCodeToDisplayNameMap.keySet().iterator();
			while (iterator.hasNext()) {
				String contextCode=iterator.next();
				if(distinctContextCode.contains(contextCode)){
					toReturn.put(contextCode, dffContextCodeToDisplayNameMap.get(contextCode));
				}
			}			
			
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Unable to get DFF Context Code to display name map");
		}
		return toReturn;		
	}
	
	private static Map<String,List<DFFRow>> getDffContextCodeToDFFRowsMap(Set<String> distinctContextCode,Map<String,List<DFFRow>> dffContextCodeToDFFRowsMap) throws Exception{
		Map<String,List<DFFRow>> toReturn = new HashMap<String,List<DFFRow>>();
		try{
			Iterator<String> iterator=dffContextCodeToDFFRowsMap.keySet().iterator();
			while (iterator.hasNext()) {
				String contextCode=iterator.next();
				if(distinctContextCode.contains(contextCode)){
					toReturn.put(contextCode, dffContextCodeToDFFRowsMap.get(contextCode));
				}
			}			
			
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Unable to get DFF Context Code to DFF Rows Map");
		}
		return toReturn;		
	}	
	
	private static String getDFFContextPromptName(Connection connection, String dffFormTitle) throws Exception {
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {	
			StringBuffer sqlQueryBuffer=new StringBuffer("");
			
			sqlQueryBuffer.append("SELECT form_context_prompt FROM APPS.FND_DESCRIPTIVE_FLEXS_VL where title = ? and rownum=1");
			//FileUtils.println("getDFFContextPromptName, dffFormTitle:'"+dffFormTitle+"' sql:\n"+sqlQueryBuffer.toString());
			
			preparedStatement = connection.prepareStatement(sqlQueryBuffer.toString());	
			preparedStatement.setString(1, dffFormTitle);
			
			resultSet=preparedStatement.executeQuery();
			if (resultSet.next()) {
				return resultSet.getString(1);
			}
			throw new Exception("Cannot retrieve the form context prompt name.");
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static int getTotalRecordsCount(Connection connection,int snapshotId,int tableId,String additionalWhereClause) throws SQLException
	{
		String sql ="select * from "+DB_TABLES_PREFIX+tableId+" where SNAPSHOT_ID="+snapshotId;
		if (additionalWhereClause!=null) {
			sql =sql +" "+additionalWhereClause;
		}
		//FileUtils.println("getTotalRecordsCount, sql:\n"+sql);
		return getTotalRowsCount(connection,sql);
	}

	public static int getTotalRowsCount(Connection connection,String sql) throws SQLException
	{
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlQueryBuffer=new StringBuffer("");
			sqlQueryBuffer.append("select count(*) from ( ").append(sql).append(" )");
			preparedStatement= connection.prepareStatement(sqlQueryBuffer.toString());
			resultSet=preparedStatement.executeQuery();
			resultSet.next();
			return resultSet.getInt(1);
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static int getTotalDefaultRecordsCount(Connection connection,int snapshotId, int tableId, Map<Integer, String> oracleSeededUserIdToUserNameMap)  throws SQLException {
		String additionalWhereClause=" AND "+getDefaultUsersWhereClause(oracleSeededUserIdToUserNameMap);
		return getTotalRecordsCount(connection,snapshotId,tableId,additionalWhereClause);
	}
	
	public static int getTotalAddedRecordsCount(Connection connection,int snapshotId, int tableId, Map<Integer, String> oracleSeededUserIdToUserNameMap)  throws SQLException {
		String additionalWhereClause=" AND "+getAddedUsersWhereClause(oracleSeededUserIdToUserNameMap);
		return getTotalRecordsCount(connection,snapshotId,tableId,additionalWhereClause);
	}
	
	public static int getTotalUpdatedRecordsCount(Connection connection,int snapshotId, int tableId, Map<Integer, String> oracleSeededUserIdToUserNameMap)  throws SQLException {
		String additionalWhereClause=" AND "+getUpdatedUsersWhereClause(oracleSeededUserIdToUserNameMap);
		return getTotalRecordsCount(connection,snapshotId,tableId,additionalWhereClause);
	}

	public static Map<Integer,Integer> getSnapshotTotalRecordsCount(Connection connection,int tableId,String additionalWhereClause) throws SQLException
	{
		StringBuffer sqlBuffer=new StringBuffer("");
		sqlBuffer.append("select * from ").append(DB_TABLES_PREFIX).append(tableId);
		if (additionalWhereClause!=null) {
			sqlBuffer.append(" where ").append(additionalWhereClause);
		}
		return getSnapshotTableRowsCount(connection,sqlBuffer.toString());
	}
	
	public static String getDefaultUsersWhereClause(Map<Integer, String> oracleSeededUserIdToUserNameMap) {
		StringBuffer whereClauseSeededOracleUserIds=getWhereClauseSeededOracleUserIds(oracleSeededUserIdToUserNameMap);
		String additionalWhereClause=" (rsc_created_by IN ("+whereClauseSeededOracleUserIds.toString()+") OR rsc_created_by IS NULL)";
		return additionalWhereClause;
	}
	
	public static String getAddedUsersWhereClause(Map<Integer, String> oracleSeededUserIdToUserNameMap) {
		StringBuffer whereClauseSeededOracleUserIds=getWhereClauseSeededOracleUserIds(oracleSeededUserIdToUserNameMap);
		String additionalWhereClause=" (rsc_created_by NOT IN ("+whereClauseSeededOracleUserIds.toString()+") and rsc_created_by is not null)";
		return additionalWhereClause;
	}
	
	public static String getUpdatedUsersWhereClause(Map<Integer, String> oracleSeededUserIdToUserNameMap) {
		StringBuffer whereClauseSeededOracleUserIds=getWhereClauseSeededOracleUserIds(oracleSeededUserIdToUserNameMap);
		String additionalWhereClause=" rsc_last_updated_by NOT IN ("+whereClauseSeededOracleUserIds.toString()+") AND (rsc_created_by IN ("+
				whereClauseSeededOracleUserIds.toString()+") or rsc_created_by is null)";
		return additionalWhereClause;
	}
	
	public static Map<Integer,Integer> getSnapshotTotalDefaultRecordsCount(Connection connection, int tableId, Map<Integer, String> oracleSeededUserIdToUserNameMap)  throws SQLException {
		String additionalWhereClause=getDefaultUsersWhereClause(oracleSeededUserIdToUserNameMap);
		return getSnapshotTotalRecordsCount(connection,tableId,additionalWhereClause);
	}
	
	public static Map<Integer,Integer> getSnapshotTotalAddedRecordsCount(Connection connection, int tableId, Map<Integer, String> oracleSeededUserIdToUserNameMap)  throws SQLException {
		String additionalWhereClause=getAddedUsersWhereClause(oracleSeededUserIdToUserNameMap);
		return getSnapshotTotalRecordsCount(connection,tableId,additionalWhereClause);
	}

	public static Map<Integer,Integer> getSnapshotTotalUpdatedRecordsCount(Connection connection, int tableId, Map<Integer, String> oracleSeededUserIdToUserNameMap)  throws SQLException {
		String additionalWhereClause=getUpdatedUsersWhereClause(oracleSeededUserIdToUserNameMap);
		return getSnapshotTotalRecordsCount(connection,tableId,additionalWhereClause);
	}
	
	public static Map<Integer,Integer> getSnapshotTableRowsCount(Connection connection,String sql) throws SQLException
	{
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlQueryBuffer=new StringBuffer("");
			sqlQueryBuffer.append("select SNAPSHOT_ID,count(*) cnt from ( ").append(sql).append(" ) group by SNAPSHOT_ID");
			preparedStatement= connection.prepareStatement(sqlQueryBuffer.toString());
			resultSet=preparedStatement.executeQuery();
			Map<Integer,Integer> toReturn=new HashMap<Integer,Integer>();
			while (resultSet.next()) {
				int snapshotId=resultSet.getInt("SNAPSHOT_ID");
				int cnt=resultSet.getInt("cnt");
				toReturn.put(snapshotId, cnt);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
		
	public static StringBuffer getWhereClauseSeededOracleUserIds( Map<Integer, String> oracleSeededUserIdToUserNameMap)  {
		StringBuffer toReturn=new StringBuffer("");
		Iterator<Integer> iterator=oracleSeededUserIdToUserNameMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key=iterator.next();
			toReturn.append("'").append(key).append("'");
			if (iterator.hasNext()) {
				toReturn.append(",");
			}
		}
		return toReturn;
	}
	
	public static String getPLSQLPackageName(File plsqlPackageFile)  {
		String packageName=plsqlPackageFile.getName().toLowerCase().replace(".sql","");
		return packageName;
	}
		
	public static void updateSnapshot(String jdbcString,String user,String password,int snapshotId,String status,int timeLimit) throws SQLException, ClassNotFoundException 
	{
		Connection connection=null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="update SNAPSHOT set status=?,";
			if (!status.equalsIgnoreCase(DB_STATUS_PROCESSING)) {
				sql =sql +" completed_on=SYSTIMESTAMP ";
			}
			if(timeLimit>0){
				sql =sql +", TIME_LIMIT="+timeLimit;
			}
			sql =sql +" where id="+snapshotId;

			connection=DatabaseUtils.getJDBCConnection(jdbcString,user,password);
			statement= connection.prepareStatement(sql);
			statement.setString(1, status);
			statement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
			DirectConnectDao.closeQuietly(connection);
		}
	}
	
	public static List<SnapshotGridRecord> getSnapshotGridRecords(Connection connection) throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
/*			String sql =
					"select b.*,"+
							"(select COUNT(*) from INVENTORY_TO_SNAPSHOT IB where IB.SNAPSHOT_ID = B.id ) TOTAL_INVENTORIES"+
							",(select COUNT(*) from INVENTORY_TO_SNAPSHOT IB where IB.SNAPSHOT_ID = B.id AND status<>'U' ) TOTAL_INVENTORIES_SELECTED"+
							",(select COUNT(*) from INVENTORY_TO_SNAPSHOT IB where IB.SNAPSHOT_ID = B.id AND status='E' ) TOTAL_INVENTORIES_FAILED"+
							",(select sum(TOTAL_RECORD) from INVENTORY_TO_SNAPSHOT IB where IB.SNAPSHOT_ID = B.id ) TOTAL_RECORD"+
							" from SNAPSHOT b where b.IS_DELETED='N' order by id desc";*/
			String sql =
					"select b.*,"+
							"(select COUNT(*) from INVENTORY ) TOTAL_INVENTORIES"+
							",(select COUNT(*) from INVENTORY_TO_SNAPSHOT IB where IB.SNAPSHOT_ID = B.id AND status<>'U' ) TOTAL_INVENTORIES_SELECTED"+
							",(select COUNT(*) from INVENTORY_TO_SNAPSHOT IB where IB.SNAPSHOT_ID = B.id AND status='E' ) TOTAL_INVENTORIES_FAILED"+
							",(select sum(TOTAL_RECORD) from INVENTORY_TO_SNAPSHOT IB where IB.SNAPSHOT_ID = B.id ) TOTAL_RECORD"+
							" from SNAPSHOT b where b.IS_DELETED='N' order by id desc";

			//System.out.println("getBaselineRecords sql:"+sql);
			
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			List<SnapshotGridRecord> toReturn=new ArrayList<SnapshotGridRecord>();
			while (resultSet.next()) {
				SnapshotGridRecord snapshotGridRecord=getSnapshotGridRecord(resultSet);
				
				int totalInventories=resultSet.getInt("TOTAL_INVENTORIES");
				int totalInventoriesSelected=resultSet.getInt("TOTAL_INVENTORIES_SELECTED");
				int totalInventoriesFailed=resultSet.getInt("TOTAL_INVENTORIES_FAILED");
				int totalRecords=resultSet.getInt("TOTAL_RECORD");
				
				snapshotGridRecord.setTotalInventories(totalInventories);
				snapshotGridRecord.setTotalInventoriesFailed(totalInventoriesFailed);
				snapshotGridRecord.setTotalInventoriesSelected(totalInventoriesSelected);
				snapshotGridRecord.setTotalRecords(totalRecords);
				
				toReturn.add(snapshotGridRecord);
			}
			
			
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	private static SnapshotGridRecord getSnapshotGridRecord(ResultSet resultSet) throws Exception
	{
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		int snapshotId=resultSet.getInt("id");
		int userId=resultSet.getInt("user_id");
		String name=resultSet.getString("name");
		String status=resultSet.getString("status");
		String osUserName=resultSet.getString("OS_USER_NAME");
		String clientHostName=resultSet.getString("CLIENT_HOSTNAME");
		String description=resultSet.getString("DESCRIPTION");
		String mode=resultSet.getString("CREATION_MODE");
		Timestamp createdOn=resultSet.getTimestamp("CREATED_ON");
		String createdOnFormattedDate=format.format(createdOn);				
		Timestamp completedOn=resultSet.getTimestamp("COMPLETED_ON");
		String completedOnFormattedDate="";
		if (completedOn!=null) {
			completedOnFormattedDate=format.format(completedOn);
		}
		String isConversionStr=resultSet.getString("is_conversion");
		boolean isConversion=false;
		if (isConversionStr!=null && isConversionStr.equals("Y")) {
			isConversion=true;
		}
		String templateName = resultSet.getString("TEMPLATE_NAME");
		
		SnapshotGridRecord snapshotGridRecord=new SnapshotGridRecord();
		snapshotGridRecord.setStatus(status);
		snapshotGridRecord.setDescription(description);
		snapshotGridRecord.setCreatedOn(createdOnFormattedDate);
		snapshotGridRecord.setCompletedOn(completedOnFormattedDate);
		snapshotGridRecord.setOsUserName(osUserName);
		snapshotGridRecord.setClientHostName(clientHostName);
		snapshotGridRecord.setConversion(isConversion);
		snapshotGridRecord.setSnapshotId(snapshotId);
		snapshotGridRecord.setUserId(userId);
		snapshotGridRecord.setName(name);
		snapshotGridRecord.setMode(mode);
		snapshotGridRecord.setTemplateName(templateName);

		return snapshotGridRecord;
	}

	public static Map<Integer,String> getOracleUserIdToUserNameMap(Connection connection,
			String oracleSeededUserNames,boolean isSeededUsersOnly,SnapshotSwingWorker snapshotSwingWorker,
			String prefixMessage,String suffixMessage)
			throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql =getOracleUserSQL(oracleSeededUserNames,isSeededUsersOnly);

			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			Map<Integer,String> toReturn=new TreeMap<Integer,String>();
			int counter=0;
			while (resultSet.next()) {
				String name=resultSet.getString("user_name");
				int id=resultSet.getInt("user_id");
				toReturn.put(id,name);
				if (snapshotSwingWorker!=null) {
					if (counter % 50 ==0) {
						snapshotSwingWorker.updateExecutionLabels(prefixMessage+" "+Utils.formatNumberWithComma(counter)+" "+suffixMessage);
					}
				}
				counter++;
			}
			if (snapshotSwingWorker!=null) {
				snapshotSwingWorker.updateExecutionLabels(prefixMessage+" "+Utils.formatNumberWithComma(counter)+" "+suffixMessage);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
		
	private static String getOracleUserSQL(String oracleSeededUserNames,boolean isSeededUsersOnly)
	{
		String sql ="select user_name,user_id from apps.fnd_user";
		if (isSeededUsersOnly) {
			sql =sql+" where user_name in ( "+oracleSeededUserNames+" )";
		}
		return sql;
	}

	public static int getOracleUserCount(Connection connection,String oracleSeededUserNames,boolean isSeededUsersOnly)
			throws SQLException, ClassNotFoundException
	{
		String sql =getOracleUserSQL( oracleSeededUserNames,isSeededUsersOnly);
		return getTotalRowsCount(connection,sql);
	}	

	public static List<SnapshotTotalsTask> getSnapshotTotalsTaskList(Connection connection)
			throws SQLException, ClassNotFoundException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="select id from INVENTORY";
			
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			List<SnapshotTotalsTask> toReturn=new ArrayList<SnapshotTotalsTask>();
			while (resultSet.next()) {
				int tableId=resultSet.getInt("id");
				SnapshotTotalsTask snapshotTotalsTask=new SnapshotTotalsTask(tableId);
				toReturn.add(snapshotTotalsTask);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static Map<String,Integer> getInventoryNameToTableIdMap(Connection connection)
			throws SQLException, ClassNotFoundException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="select NAME,id from INVENTORY";
			
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			Map<String,Integer> toReturn=new HashMap<String,Integer>();
			while (resultSet.next()) {
				String name=resultSet.getString("NAME");
				int id=resultSet.getInt("id");
				toReturn.put(name,id);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static void insertInventoryDBRecords(Connection connection,List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList) 
			throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{
			String sql ="insert into INVENTORY(ID ,NAME) values(INVENTORY_ID.NEXTVAL,?) ";

			preparedStatement= connection.prepareStatement(sql);
			
			for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsList) {
				int index=1;
				preparedStatement.setString(index++,snapshotInventoryGridRecord.getInventoryName()); 
				preparedStatement.addBatch();      
			}
			preparedStatement.executeBatch();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	
	
	public static void insertInventoryToSnapshotDBRecords(Connection connection,int snapshotId,Map<String,Integer> allInventoryNameToTableIdMap,
			Set<String> selectedTableNamesSet)
			throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{
			String sql ="insert into INVENTORY_TO_SNAPSHOT(INVENTORY_ID,SNAPSHOT_ID,STATUS) values(?,?,?)";

			preparedStatement= connection.prepareStatement(sql);
			
			Iterator<String> iterator=allInventoryNameToTableIdMap.keySet().iterator();
			while (iterator.hasNext()) {
				String inventoryName=iterator.next();
				Integer tableId=allInventoryNameToTableIdMap.get(inventoryName);
				boolean isTableSelected=selectedTableNamesSet.contains(inventoryName);
				int index=1;
				if(isTableSelected){
					preparedStatement.setInt(index++, tableId);
					preparedStatement.setInt(index++, snapshotId);
					preparedStatement.setString(index++, DB_STATUS_PENDING);
					preparedStatement.addBatch();   
				}   
			}
			preparedStatement.executeBatch();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
		
	public static Set<Integer> getTableIds(Connection connection) throws SQLException	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="select object_name from user_objects where object_type='TABLE' AND object_name like '"+DB_TABLES_PREFIX+"%'";
			
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			Set<Integer> toReturn=new HashSet<Integer>();
			while (resultSet.next()) {
				String name=resultSet.getString("object_name");
				if (name.startsWith(DB_DFF_VALUE_SET_TABLES_PREFIX)) {
					continue;
				}
				String tableIdStr=name.replaceAll(DB_DFF_TABLES_PREFIX,"");
				tableIdStr=tableIdStr.replaceAll(DB_TABLES_PREFIX,"");
				int tableId=Integer.valueOf(tableIdStr);
				toReturn.add(tableId);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static void insertNewInventories(Connection connection,List<SnapshotInventoryGridRecord> selectedSnapshotCreationGridRecordsList)
			throws SQLException, ClassNotFoundException	{
		Map<String,Integer> inventoryNames=ModelUtils.getInventoryNameToTableIdMap(connection);
		List<SnapshotInventoryGridRecord> inventoriesToAddList=new ArrayList<SnapshotInventoryGridRecord>();
		for (SnapshotInventoryGridRecord snapshotCreationGridRecord:selectedSnapshotCreationGridRecordsList) {
			Integer tableId=inventoryNames.get(snapshotCreationGridRecord.getInventoryName());
			if (tableId==null) {
				inventoriesToAddList.add(snapshotCreationGridRecord);
			}
		}
		ModelUtils.insertInventoryDBRecords(connection,inventoriesToAddList);
	}

	public static List<SnapshotGridRecord> getSnapshotsInProgress(Connection connection) throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql =
					"select * from SNAPSHOT WHERE IS_DELETED='N' and status='P'";
			
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			List<SnapshotGridRecord> toReturn=new ArrayList<SnapshotGridRecord>();
			while (resultSet.next()) {
				SnapshotGridRecord snapshotGridRecord=getSnapshotGridRecord(resultSet);
				
				toReturn.add(snapshotGridRecord);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static List<SnapshotInventoryGridRecord> getSnapshotInventoryGridRecordsList(
			String jdbcString,String user,String password,int snapshotId,String status) throws Exception
	{
		Connection connection=null;
		try
		{
			connection=DatabaseUtils.getJDBCConnection(jdbcString,user,password);
			return getSnapshotInventoryGridRecordsList(connection,snapshotId,status);
		}
		finally
		{
			DirectConnectDao.closeQuietly(connection);
		}
	}
	
	public static List<SnapshotInventoryGridRecord> getSnapshotInventoryGridRecordsList(Connection connection,int snapshotId
			,String dbStatus) throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		try
		{
			String sql ="select ISN.*,I.NAME,I.IS_EXECUTABLE,I.IS_LISTABLE from INVENTORY_TO_SNAPSHOT ISN, INVENTORY I where ISN.SNAPSHOT_ID = "+snapshotId+
					" AND I.ID=ISN.INVENTORY_ID";
			if (dbStatus!=null) {
				sql=sql+" and status='"+dbStatus+"'";
			}
			sql=sql+" order by INVENTORY_ID";
			
			//System.out.println("getSnapshotInventoryGridRecordsList: "+sql);
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			List<SnapshotInventoryGridRecord> toReturn=new ArrayList<SnapshotInventoryGridRecord>();
			while (resultSet.next()) {
				int tableId=resultSet.getInt("INVENTORY_ID");
				String inventoryName=resultSet.getString("NAME");
				String status=resultSet.getString("status");
				String remarks=resultSet.getString("msg");
				String executionTime=resultSet.getString("EXECUTION_TIME_STRING");
				int rawTimeInSecs=resultSet.getInt("EXECUTION_RAW_TIME");
				int totalRecords=resultSet.getInt("TOTAL_RECORD");
				Timestamp createdOn = resultSet.getTimestamp("CREATED_ON");
				Timestamp completedOn = resultSet.getTimestamp("COMPLETED_ON");	
				String createdOnFormattedDate="";
				String completedOnFormattedDate="";	
				if (createdOn!=null) {
					createdOnFormattedDate=format.format(createdOn);
				}	
				if (completedOn!=null) {
					completedOnFormattedDate=format.format(completedOn);
				}				
				String isExecutableStr = resultSet.getString("IS_EXECUTABLE");
				String isListableStr = resultSet.getString("IS_LISTABLE");
				boolean isExecutable = false;
				boolean isListable = false;
				if(isExecutableStr==null || isExecutableStr.equals("Y")){
					isExecutable = true;
				}
				if(isListableStr==null || isListableStr.equals("Y")){
					isListable = true;
				}				
				SnapshotInventoryGridRecord snapshotInventoryGridRecord=new SnapshotInventoryGridRecord(inventoryName);
				snapshotInventoryGridRecord.setTableId(tableId);
				snapshotInventoryGridRecord.setStatus(status);
				if (remarks!=null) {
					remarks=remarks.replaceAll("\\s+", " ");
				}
				snapshotInventoryGridRecord.setRemarks(remarks);
				snapshotInventoryGridRecord.setExecutionTime(executionTime);
				snapshotInventoryGridRecord.setRawTimeInSecs(rawTimeInSecs);
				snapshotInventoryGridRecord.setTotalRecords(totalRecords);
				snapshotInventoryGridRecord.setCreatedOn(createdOnFormattedDate);
				snapshotInventoryGridRecord.setCompletedOn(completedOnFormattedDate);
				snapshotInventoryGridRecord.setExecutable(isExecutable);
				snapshotInventoryGridRecord.setListable(isListable);
				toReturn.add(snapshotInventoryGridRecord);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static String getJDBCString(Map<String,String> snapshotEnvironmentProperties) {
		String hostName=snapshotEnvironmentProperties.get(ServerFrame.HOST_NAME_PROPERTY);
		String port=snapshotEnvironmentProperties.get(ServerFrame.PORT_NUMBER_PROPERTY);
		String sid=snapshotEnvironmentProperties.get(ServerFrame.SID_PROPERTY);
		String serviceType =snapshotEnvironmentProperties.get(ServerFrame.SERVICE_TYPE_PROPERTY); 
		String serviceName =snapshotEnvironmentProperties.get(ServerFrame.SERVICE_NAME_PROPERTY); 
		
		return  getJDBCString( hostName, port, sid, serviceType,serviceName);
	}
	
	public static String getJDBCString(String hostName,String port,String sid, String serviceType, String serviceName) {
		StringBuffer toReturn=new StringBuffer("");
		toReturn.append("jdbc:oracle:thin:@");
		if((serviceType!=null && serviceType.equals(UtilsConstants.SID_DATABASE_SERVICE_TYPE)) || serviceType==null){
			toReturn.append(hostName).append(":").append(port).append(":").append(sid);
		}else if(serviceType!=null && serviceType.equals(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE)){
			toReturn.append("//").append(hostName).append(":").append(port).append("/").append(serviceName);
		}
		//FileUtils.println("JDBC String : "+toReturn.toString());
		return toReturn.toString();
	}

	public static String getDBUserName(Map<String,String> snapshotEnvironmentProperties) {
		return snapshotEnvironmentProperties.get(ServerFrame.USER_NAME_PROPERTY);
	}

	public static String getDBPassword(Map<String,String> snapshotEnvironmentProperties) {
		return snapshotEnvironmentProperties.get(ServerFrame.PASSWORD_PROPERTY);
	}

	public static String getConnectionName(Map<String,String> snapshotEnvironmentProperties) {
		return snapshotEnvironmentProperties.get(ServerFrame.CONNECTION_NAME_PROPERTY);
	}
	
	public static Map<String, String> getSnapshotEnvironmentProperties(TabSnapshotsPanel tabSnapshotsPanel) {
		String connectionName=tabSnapshotsPanel.getServerSelectionPanel().getSelectedServerConnection();
		File serverConnectionsFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
		File seFile=new File(serverConnectionsFolder,connectionName+SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION);
		return UIUtils.readSnapshotEnvironmentProperties(seFile);
	}
	
	public static void saveSnapshotInventoryDetailsGridToExcel(File outputFile,
			List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList,boolean isShowFilteringResultColumn) throws Exception {
		List<String[]> rows=new ArrayList<String[]>();
		int counter=0;
		int totalColumnsToDisplay=16;
		if (isShowFilteringResultColumn) {
			totalColumnsToDisplay++;
		}
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
			counter++;
			int index=0;
			String[] row=new String[totalColumnsToDisplay];
			row[index++]=""+counter;
			String applicationName=snapshotInventoryGridRecord.getFormInformation().getFormattedApplicationNames();
			if (applicationName==null) {
				applicationName="";
			}
			row[index++]=applicationName;
			String menuPath=snapshotInventoryGridRecord.getFormInformation().getFormattedFormPaths();
			if (menuPath==null) {
				menuPath="";
			}
			row[index++]=menuPath;
			String formName=snapshotInventoryGridRecord.getFormInformation().getFormName();
			if (formName==null) {
				formName="";
			}
			row[index++]=formName;
			row[index++]=snapshotInventoryGridRecord.getInventoryName();
			String formType=snapshotInventoryGridRecord.getFormInformation().getFormType();
			if (formType==null) {
				formType="";
			}
			row[index++]=formType;
			String status=snapshotInventoryGridRecord.getStatus();
			if (status==null) {
				status="";
			}
			status=ModelUtils.getUIStatusFromDBStatus(status);
			row[index++]=status;
			if (isShowFilteringResultColumn) {
				String filteringResult=snapshotInventoryGridRecord.getFilteringResult();
				if (filteringResult==null) {
					filteringResult="";
				}
				row[index++]=filteringResult;
			}
			int totalRecords=snapshotInventoryGridRecord.getTotalRecords();
			row[index++]=""+totalRecords;
			int totalDefaultRecords=snapshotInventoryGridRecord.getTotalDefaultRecords();
			row[index++]=""+totalDefaultRecords;
			int totalAddedRecords=snapshotInventoryGridRecord.getTotalAddedRecords();
			row[index++]=""+totalAddedRecords;
			int totalUpdatedRecords=snapshotInventoryGridRecord.getTotalUpdatedRecords();
			row[index++]=""+totalUpdatedRecords;

			String remarks=snapshotInventoryGridRecord.getRemarks();
			if (remarks==null) {
				remarks="";
			}
			row[index++]=remarks;
			String executionTime=snapshotInventoryGridRecord.getExecutionTime();
			if (executionTime==null) {
				executionTime="";
			}
			row[index++]=executionTime;

			row[index++]=""+snapshotInventoryGridRecord.getRawTimeInSecs();
			String createdOn = snapshotInventoryGridRecord.getCreatedOn();
			if (createdOn==null) {
				createdOn="";
			}
			row[index++]=createdOn;
			String completedOn = snapshotInventoryGridRecord.getCompletedOn();
			if (completedOn==null) {
				completedOn="";
			}
			row[index++]=completedOn;
			rows.add(row);
		}
		String[] headerRow=new String[totalColumnsToDisplay];
		int index=0;
		String NEW_LINE="\n";
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_ROW_NUM.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_MODULE_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_FORM_PATH.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_FORM_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_INVENTORY_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_FORM_TYPE.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_STATUS.replaceAll(NEW_LINE," ");
		if (isShowFilteringResultColumn) {
			headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_FILTERING_RESULT.replaceAll(NEW_LINE," ");
		}
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_TOTAL_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_TOTAL_DEFAULT_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_TOTAL_ADDED_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_TOTAL_UPDATED_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_REMARKS.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_EXECUTION_TIME.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_RAW_TIME.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_CREATED_ON.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_COMPLETED_ON.replaceAll(NEW_LINE," ");
		rows.add(0,headerRow);

		String sheetTitle="Status";
		doCreateAndSaveXLSXExcelFile(sheetTitle,rows,outputFile, false, false);
	}
	
	public static void doCreateAndSaveXLSXExcelFile(String sheetTitle,final List<String[]> dataGridRows, final File outputFile,
			final boolean openFileAfterCreation, boolean showFileCreationSuccessfulMessage) throws Exception {
		FileUtils.createAndSaveRowsToXLSXFile(sheetTitle,outputFile,dataGridRows);
		if (showFileCreationSuccessfulMessage) {
			GUIUtils.popupInformationMessage("'"+outputFile.getPath()+"' was successfully created");
		}
		if (openFileAfterCreation) {
			try{
				Desktop.getDesktop().open(outputFile);
			}
			catch (Exception e) {
				GUIUtils.popupErrorMessage("Unable to open the status file. Error: "+e.getMessage());
			}
		}		
	}
	
	public static List<String> getTableNamesList(Connection connection,String user) throws Exception {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement=null;
		try {
			String sqlQuery="SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'TABLE' AND UPPER(OWNER) = '"+user.toUpperCase()+"'"+
					" and ( OBJECT_NAME like '"+DB_TABLES_PREFIX+"%' or OBJECT_NAME like '"+DB_COMPARISON_TABLES_PREFIX+"%' ) ";
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

	public static void truncateTable(Connection connection,String tableName) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("TRUNCATE TABLE ").append(tableName);
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
	
	public static boolean isTablePassFilteringCriteria(Connection connection,int snapshotId,int tableId,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord, Map<Integer, String> oracleSeededUserIdToUserNameMap,String additionalWhereClause) 
			throws Exception {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement=null;
		try {
			StringBuffer sqlQuery=new StringBuffer("");
			
			StringBuffer totalAddedCondition=new StringBuffer("");
			totalAddedCondition.append(" AND ").append(getAddedUsersWhereClause(oracleSeededUserIdToUserNameMap));
			
			StringBuffer totalUpdatedCondition=new StringBuffer("");
			totalUpdatedCondition.append(" AND ").append(getUpdatedUsersWhereClause(oracleSeededUserIdToUserNameMap));
			
			StringBuffer sqlCommon=new StringBuffer("");
			sqlCommon.append(" FROM ").append(DB_TABLES_PREFIX).append(tableId).append(" WHERE snapshot_id=").append(snapshotId);
			
			//StringBuffer sqlQueryTotal=new StringBuffer("");
			//sqlQueryTotal.append("SELECT count(*) ").append(sqlCommon);// Total never changes! .append(additionalWhereClause);
			
			StringBuffer sqlQueryTotalAdded=new StringBuffer("");
			sqlQueryTotalAdded.append("SELECT count(*) ").append(sqlCommon).append(totalAddedCondition).append(additionalWhereClause);
			
			StringBuffer sqlQueryTotalUpdated=new StringBuffer("");
			sqlQueryTotalUpdated.append("SELECT count(*) ").append(sqlCommon).append(totalUpdatedCondition).append(additionalWhereClause);
			
			sqlQuery.append(" SELECT ");
			//sqlQuery.append(" (").append(sqlQueryTotal).append(") TOTAL, ");
			sqlQuery.append(" (").append(sqlQueryTotalAdded).append(") TOTAL_ADDED, ");
			sqlQuery.append(" (").append(sqlQueryTotalUpdated).append(") TOTAL_UPDATED ");
			sqlQuery.append(" FROM DUAL");
			
			preparedStatement = connection.prepareStatement(sqlQuery.toString());
			resultSet=preparedStatement.executeQuery();
			resultSet.next();
			//int total=resultSet.getInt("TOTAL");
			int totalAdded=resultSet.getInt("TOTAL_ADDED");
			int totalUpdated=resultSet.getInt("TOTAL_UPDATED");
			
			//snapshotInventoryGridRecord.setTotalRecords(total);
			snapshotInventoryGridRecord.setTotalAddedRecords(totalAdded);
			snapshotInventoryGridRecord.setTotalUpdatedRecords(totalUpdated);
			
			boolean result= totalAdded!=0 || totalUpdated!=0;
			if (result) {
				//FileUtils.println("isTablePassFilteringCriteria, sql:\n"+sqlQuery.toString());
			}
			return result;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}		
	}
	
	public static List<OperatingUnit> getOperatingUnitsList(Connection connection)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery=			
			"select h.organization_id, h.name,(select distinct CHART_OF_ACCOUNTS_ID from apps.Org_Organization_Definitions t "+
			"where t.operating_unit =h.organization_id ) CHART_OF_ACCOUNTS_ID from apps.hr_operating_units h order by name asc";
			
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			List<OperatingUnit> toReturn=new ArrayList<OperatingUnit>();
			while (resultSet.next()) {
				long id= resultSet.getLong("organization_id");
				String name= resultSet.getString("name");
				int coaId= resultSet.getInt("CHART_OF_ACCOUNTS_ID");
				OperatingUnit operatingUnit=new OperatingUnit(name,id,coaId);
				toReturn.add(operatingUnit);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	


	public static Map<Long,List<LegalEntity>> getOperatingUnitIdToLegalEntityList(Connection connection)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= "SELECT OU.ORGANIZATION_ID as OPERATING_UNIT_ID,OU.name as OPERATING_UNIT_NAME, HRORGINFO.ORG_INFORMATION2 as LEGAL_ENTITY_ID,XFI.name as LEGAL_ENTITY_NAME" 
		      + " FROM apps.HR_ORGANIZATION_INFORMATION HRORGINFO" 
		      + " ,apps.HR_OPERATING_UNITS OU" 
		      + " ,apps.XLE_ENTITY_PROFILES XFI" 
		      + " WHERE HRORGINFO.org_information_context = 'Operating Unit Information'" 
		      + " AND OU.organization_id = HRORGINFO.organization_id" 
		      + " and HRORGINFO.ORG_INFORMATION2 = XFI.LEGAL_ENTITY_ID"
		      ;
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			Map<Long,List<LegalEntity>> toReturn=new HashMap<Long,List<LegalEntity>>();
			while (resultSet.next()) {
				long operatingUnitId= resultSet.getLong("OPERATING_UNIT_ID");
				long legalEntityId= resultSet.getLong("LEGAL_ENTITY_ID");
				String legalEntityName= resultSet.getString("LEGAL_ENTITY_NAME");
				
				List<LegalEntity> legalEntitiesList=toReturn.get(operatingUnitId);
				if (legalEntitiesList==null) {
					legalEntitiesList=new ArrayList<LegalEntity>();
					toReturn.put(operatingUnitId,legalEntitiesList);
				}
				LegalEntity legalEntity=new LegalEntity(legalEntityName,legalEntityId);
				legalEntitiesList.add(legalEntity);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	@SuppressWarnings("resource")
	public static Map<Long,List<InventoryOrganization>> getOperatingUnitIdToInventoryOrganizationsList(Connection connection)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= "select operating_Unit,ORGANIZATION_NAME,ORGANIZATION_ID from apps.ORG_ORGANIZATION_DEFINITIONS";
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			Map<Long,List<InventoryOrganization>> toReturn=new HashMap<Long,List<InventoryOrganization>>();
			while (resultSet.next()) {
				long operatingUnitId= resultSet.getLong("operating_Unit");
				long inventoryOrganizationId= resultSet.getLong("ORGANIZATION_ID");
				String inventoryOrganizationName= resultSet.getString("ORGANIZATION_NAME");
				
				List<InventoryOrganization> inventoryOrganizationsList=toReturn.get(operatingUnitId);
				if (inventoryOrganizationsList==null) {
					inventoryOrganizationsList=new ArrayList<InventoryOrganization>();
					toReturn.put(operatingUnitId,inventoryOrganizationsList);
				}
				InventoryOrganization inventoryOrganization=new InventoryOrganization(inventoryOrganizationName,inventoryOrganizationId);
				inventoryOrganizationsList.add(inventoryOrganization);
			}
			
			sqlQuery= "select -1 as operating_Unit, ORGANIZATION_NAME, ORGANIZATION_ID from apps.ORG_ORGANIZATION_DEFINITIONS where ORGANIZATION_ID " + 
					" in ( select ORGANIZATION_ID from apps.ORG_ORGANIZATION_DEFINITIONS where operating_Unit is null )";
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			while (resultSet.next()) {
				long operatingUnitId= resultSet.getLong("operating_Unit");
				long inventoryOrganizationId= resultSet.getLong("ORGANIZATION_ID");
				String inventoryOrganizationName= resultSet.getString("ORGANIZATION_NAME");
				
				List<InventoryOrganization> inventoryOrganizationsList=toReturn.get(operatingUnitId);
				if (inventoryOrganizationsList==null) {
					inventoryOrganizationsList=new ArrayList<InventoryOrganization>();
					toReturn.put(operatingUnitId,inventoryOrganizationsList);
				}
				InventoryOrganization inventoryOrganization=new InventoryOrganization(inventoryOrganizationName,inventoryOrganizationId);
				inventoryOrganizationsList.add(inventoryOrganization);
			}
			
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	@SuppressWarnings("resource")
	public static Map<Long,List<Ledger>> getOperatingUnitToLedgersMap(Connection connection)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= 
			"SELECT Ou.Organization_Id, RSHIP.TARGET_LEDGER_ID LEDGER_ID , RSHIP.TARGET_LEDGER_NAME NAME FROM " + 
			"(SELECT RSHIP.TARGET_LEDGER_ID, RSHIP.PRIMARY_LEDGER_ID, RSHIP.TARGET_LEDGER_NAME, RSHIP.TARGET_LEDGER_category_code " + 
			"FROM APPS.GL_LEDGER_RELATIONSHIPS RSHIP WHERE (RSHIP.RELATIONSHIP_TYPE_CODE  <> 'NONE' AND RSHIP.TARGET_LEDGER_CATEGORY_CODE = 'SECONDARY') " + 
			"OR (RSHIP.RELATIONSHIP_TYPE_CODE = 'NONE' AND RSHIP.TARGET_LEDGER_CATEGORY_CODE = 'PRIMARY') ) RSHIP, apps.Hr_Operating_Units Ou , " + 
			"apps.Gl_Ledgers Ledger, apps.Hr_Organization_Information Hrorginfo WHERE RSHIP.PRIMARY_LEDGER_ID = Ou.Set_Of_Books_Id AND " + 
			"Ledger.LEDGER_ID = Ou.Set_Of_Books_Id and Hrorginfo.Org_Information_Context = 'Operating Unit Information' " + 
			"AND Ou.Organization_Id = Hrorginfo.Organization_Id";
			
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			Map<Long,List<Ledger>> toReturn=new HashMap<Long,List<Ledger>>();
			while (resultSet.next()) {
				long operatingUnitId= resultSet.getLong("ORGANIZATION_ID");
				long ledgerId= resultSet.getLong("LEDGER_ID");
				String ledgerName = resultSet.getString("NAME");

				List<Ledger> ledgerList=toReturn.get(operatingUnitId);
				if (ledgerList==null) {
					ledgerList=new ArrayList<Ledger>();
					toReturn.put(operatingUnitId,ledgerList);
				}
				Ledger ledger=new Ledger(ledgerName,ledgerId);
				ledgerList.add(ledger);
			}

			sqlQuery= 
			"SELECT -1 as Organization_Id, LEDGER.LEDGER_ID, LEDGER.NAME FROM apps.Gl_Ledgers Ledger WHERE Ledger.Ledger_Id Not IN " + 
			"(SELECT RSHIP.TARGET_LEDGER_ID FROM (SELECT RSHIP.TARGET_LEDGER_ID, RSHIP.PRIMARY_LEDGER_ID, " + 
			"RSHIP.TARGET_LEDGER_NAME, RSHIP.TARGET_LEDGER_category_code FROM APPS.GL_LEDGER_RELATIONSHIPS RSHIP " + 
			"WHERE (RSHIP.RELATIONSHIP_TYPE_CODE <> 'NONE' AND RSHIP.TARGET_LEDGER_CATEGORY_CODE = 'SECONDARY') " + 
			"OR (RSHIP.RELATIONSHIP_TYPE_CODE = 'NONE' AND RSHIP.TARGET_LEDGER_CATEGORY_CODE = 'PRIMARY') ) RSHIP, " + 
			"apps.Hr_Operating_Units Ou, apps.Hr_Organization_Information Hrorginfo WHERE RSHIP.PRIMARY_LEDGER_ID = Ou.Set_Of_Books_Id " + 
			"and Hrorginfo.Org_Information_Context = 'Operating Unit Information' AND Ou.Organization_Id = Hrorginfo.Organization_Id)";
			
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			while (resultSet.next()) {
				long operatingUnitId= resultSet.getLong("ORGANIZATION_ID");
				long ledgerId= resultSet.getLong("LEDGER_ID");
				String ledgerName = resultSet.getString("NAME");

				List<Ledger> ledgerList=toReturn.get(operatingUnitId);
				if (ledgerList==null) {
					ledgerList=new ArrayList<Ledger>();
					toReturn.put(operatingUnitId,ledgerList);
				}
				Ledger ledger=new Ledger(ledgerName,ledgerId);
				ledgerList.add(ledger);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	@SuppressWarnings("resource")
	public static Map<Long,List<BusinessGroup>> getOperatingUnitIdToBusinessGroupList(Connection connection)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= "SELECT DISTINCT OU.BUSINESS_GROUP_ID, INV.OPERATING_UNIT " + 
					" FROM APPS.HR_ORGANIZATION_UNITS_V OU, APPS.ORG_ORGANIZATION_DEFINITIONS INV " + 
					" WHERE OU.ORGANIZATION_ID = INV.ORGANIZATION_ID";
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			Map<Long,List<BusinessGroup>> toReturn=new HashMap<Long,List<BusinessGroup>>();
			while (resultSet.next()) {
				long operatingUnitId= resultSet.getLong("OPERATING_UNIT");
				long businessGroupId= resultSet.getLong("BUSINESS_GROUP_ID");
				
				List<BusinessGroup> businessGroupsList=toReturn.get(operatingUnitId);
				if (businessGroupsList==null) {
					businessGroupsList=new ArrayList<BusinessGroup>();
					toReturn.put(operatingUnitId,businessGroupsList);
				}
				BusinessGroup businessGroup=new BusinessGroup(businessGroupId);
				businessGroupsList.add(businessGroup);
			}

			sqlQuery= "SELECT OU.BUSINESS_GROUP_ID, -1 as OPERATING_UNIT FROM APPS.HR_ORGANIZATION_UNITS_V OU, " + 
					" APPS.ORG_ORGANIZATION_DEFINITIONS INV WHERE OU.ORGANIZATION_ID = INV.ORGANIZATION_ID and OU.BUSINESS_GROUP_ID " + 
					" not IN (select INV.BUSINESS_GROUP_ID from APPS.ORG_ORGANIZATION_DEFINITIONS INV where INV.Operating_Unit is not null )";
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			while (resultSet.next()) {
				long operatingUnitId= resultSet.getLong("OPERATING_UNIT");
				long businessGroupId= resultSet.getLong("BUSINESS_GROUP_ID");
				
				List<BusinessGroup> businessGroupsList=toReturn.get(operatingUnitId);
				if (businessGroupsList==null) {
					businessGroupsList=new ArrayList<BusinessGroup>();
					toReturn.put(operatingUnitId,businessGroupsList);
				}
				BusinessGroup businessGroup=new BusinessGroup(businessGroupId);
				businessGroupsList.add(businessGroup);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	private static StringBuffer addOracleUserNamesToQuery(String sqlQuery) {
		StringBuffer toReturn =new StringBuffer("");
		
		toReturn.append("Select T1.*,").
		append("(Select U.User_Name From Apps.Fnd_User U Where U.User_Id=T1.Rsc_Last_Updated_By) As Last_Updated_By_Name,").
		append("(select U.USER_NAME from APPS.FND_USER U where U.USER_ID=T1.Rsc_created_By) AS CREATED_BY_NAME").
		append(" from ( ");
		toReturn.append(sqlQuery);
		toReturn.append(") T1");
		
		return toReturn;
	}

	public static StringBuffer addOracleUserNamesToQuery(String sqlQuery,SnapshotGridRecord snapshotGridRecord) {
		if (snapshotGridRecord!=null && snapshotGridRecord.getMode().equals(SnapshotImportSwingWorker.IMPORT_MODE)) {
			return addOracleUserNamesToQuery(sqlQuery,snapshotGridRecord.getSnapshotId());
		}
		else {
			return addOracleUserNamesToQuery(sqlQuery);
		}
	}
	
	private static StringBuffer addOracleUserNamesToQuery(String sqlQuery,int snapshotId) {
		StringBuffer toReturn =new StringBuffer("");
			
		toReturn.append("Select T1.*,").
		append("(select COL2 from IMPORT_METADATA where snapshot_id="+snapshotId+" and type='"+
		SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_USER+"' and col1=T1.Rsc_Last_Updated_By) As Last_Updated_By_Name,").
		append("(select COL2 from IMPORT_METADATA where snapshot_id="+snapshotId+" and type='"+
		SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_USER+"' and col1=T1.Rsc_created_By) AS CREATED_BY_NAME").
		append(" from ( ");
		toReturn.append(sqlQuery);
		toReturn.append(") T1");
		
		return toReturn;
	}
	
	public static List<DataRow> getDataRows(Connection connection,Inventory inventory,String sqlQuery,int startRowNumToFetch,
			int endRowNumToFetch) throws Exception {
		return getDataRows(connection,inventory,sqlQuery,startRowNumToFetch,endRowNumToFetch,null,null);
	}

	public static List<DataRow> getDataRows(Connection connection,Inventory inventory,String sqlQuery,int startRowNumToFetch,
			int endRowNumToFetch,
			SnapshotSwingWorker snapshotSwingWorker,SnapshotGridRecord snapshotGridRecord) 
			throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{			
			int inventoryColumnsCount=inventory.getFieldNamesUsedForDataEntry().size();
					
			StringBuffer sqlBuffer =new StringBuffer("");
			
			sqlBuffer.append("SELECT T.* FROM (SELECT T.*, ROWNUM as rnum FROM (");
			sqlBuffer.append(addOracleUserNamesToQuery(sqlQuery,snapshotGridRecord));
						
			sqlBuffer.append(") T) T  WHERE T.rnum BETWEEN ").append(startRowNumToFetch).append(" AND ").append(endRowNumToFetch);

			//System.out.println("getDataRows: \n"+sqlBuffer.toString());
			statement= connection.prepareStatement(sqlBuffer.toString());
			resultSet=statement.executeQuery();

			List<DataRow> toReturn=new ArrayList<DataRow>();
			DataRow dataRow=null;
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			
			int counter=0;
			int batchSize=500;
			while (resultSet.next()) {
				counter++;
				if (snapshotSwingWorker!=null && (counter % batchSize ==0)) {
					snapshotSwingWorker.updateExecutionLabels("Loading data: "+Utils.formatNumberWithComma(counter)+" / "+
							Utils.formatNumberWithComma(endRowNumToFetch)+" records (Max)");
					snapshotSwingWorker.incrementStep(batchSize);
				}
				dataRow=getDataRow(resultSet,inventoryColumnsCount,format);
				toReturn.add(dataRow);
			}

			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static DataRow getDataRow(ResultSet resultSet,int inventoryColumnsCount,SimpleDateFormat format) throws SQLException {
		return getDataRowGeneric(resultSet,inventoryColumnsCount,format,false);
	}
	
	public static DataRow getDataRowGeneric(ResultSet resultSet,int inventoryColumnsCount,SimpleDateFormat format,boolean hasDFFFieldNames) throws SQLException {
		DataRow	dataRow=new DataRow();
		String formattedDate=null;
		
		int rowBaselineId=resultSet.getInt("SNAPSHOT_ID");
		int index=0;
		String[] dataValues= new String[inventoryColumnsCount];
		dataRow.setDataValues(dataValues);
		for (int i=1;i<=inventoryColumnsCount;i++){
			String value=resultSet.getString("C"+i);
			dataValues[index]=value;
			index++;
		}
		dataRow.setSnapshotId(rowBaselineId);
		dataRow.setRscLastUpdatedBy(resultSet.getInt("rsc_last_updated_by"));

		Timestamp timestamp = resultSet.getTimestamp("rsc_last_update_date");
		if (timestamp!=null) {
			formattedDate=format.format(timestamp);
			dataRow.setRscLastUpdateDate(formattedDate);
		}
		dataRow.setRscCreatedBy(resultSet.getInt("rsc_created_by"));

		timestamp = resultSet.getTimestamp("rsc_creation_date");
		if (timestamp!=null) {
			formattedDate=format.format(timestamp);
			dataRow.setRscCreationDate(formattedDate);
		}
		dataRow.setRscInvOrgId(resultSet.getString("rsc_inv_org_id"));
		dataRow.setRscOUid(resultSet.getString("rsc_ou_id"));
		dataRow.setRscLedgerId(resultSet.getString("rsc_ledger_id"));
		dataRow.setRscBGId(resultSet.getString("rsc_bg_id"));
		dataRow.setRscCOAId(resultSet.getString("rsc_coa_id"));
		dataRow.setRscCreatedByName(resultSet.getString("CREATED_BY_NAME"));
		dataRow.setRscLastUpdatedByName(resultSet.getString("Last_Updated_By_Name"));
		if (hasDFFFieldNames) {
			String[] dffFieldNames= new String[DFF_FIELD_COUNT];
			dataRow.setExtraColumns(dffFieldNames);
			for (int i=1;i<=DFF_FIELD_COUNT;i++) {
				try{
					dffFieldNames[i-1]=resultSet.getString(DFF_FIELD_NAME+i);
				}
				catch(Exception e) {
					dffFieldNames[i-1]="";
				}
			}
		}
		return dataRow;
	}
	
	public static void updateReverseSQLWithFormTypeInformation(File sourceFolder) throws Exception {
		Map<String, File> inventoryNameToReverseSQLFileMap=ModelUtils.getFileNameToFileMap(sourceFolder,false);
		Iterator<String> iterator=inventoryNameToReverseSQLFileMap.keySet().iterator();
		int counter=0;
	
		String END_OF_COMMENT="\\*/";
		String END_OF_LINE="\r\n";
		System.out.println("Processing "+inventoryNameToReverseSQLFileMap.size()+" files...");
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			File sqlFile=inventoryNameToReverseSQLFileMap.get(inventoryName);
			String reverseFileContent=FileUtils.readContentsFromSQLFile(sqlFile);
			int indexOf=reverseFileContent.indexOf(FORM_LEVEL_START_TAG);
			if (indexOf==-1) {
				counter++;
				System.out.println("Files processed: "+counter);
				String formType=computeFormTypeFromReverseSQLFile(sqlFile);
				String xmlFormTypeTag=FORM_LEVEL_START_TAG+formType+FORM_LEVEL_END_TAG;
				reverseFileContent=reverseFileContent.replaceFirst(END_OF_COMMENT, END_OF_COMMENT+END_OF_LINE+"--"+xmlFormTypeTag+END_OF_LINE);
				writeToFile(sqlFile,reverseFileContent,false); 
			}
		}
	}
	
	public static void updateReverseSQLRemoveFormTypeInformation(File sourceFolder) throws Exception {
		Map<String, File> inventoryNameToReverseSQLFileMap=ModelUtils.getFileNameToFileMap(sourceFolder,false);
		Iterator<String> iterator=inventoryNameToReverseSQLFileMap.keySet().iterator();
		int counter=0;
		System.out.println("Processing "+inventoryNameToReverseSQLFileMap.size()+" files...");
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			File sqlFile=inventoryNameToReverseSQLFileMap.get(inventoryName);
			String reverseFileContent=FileUtils.readContentsFromSQLFile(sqlFile);
			int startOfLineIndex=reverseFileContent.indexOf("--"+FORM_LEVEL_START_TAG);
			if (startOfLineIndex!=-1) {
				counter++;
				System.out.println("inventoryName: "+inventoryName+" Files processed: "+counter);
				String regex="--"+FORM_LEVEL_START_TAG+".*."+FORM_LEVEL_END_TAG;
				reverseFileContent=reverseFileContent.replaceAll(regex,"");
				writeToFile(sqlFile,reverseFileContent,false);
			}
		}
		System.out.println("Completed. Total updated: "+counter+" / "+inventoryNameToReverseSQLFileMap.size());
	}

	public static boolean isFormType(String formType,String content) {
		String xmlTag=FORM_LEVEL_START_TAG+formType+FORM_LEVEL_END_TAG;
		return content.indexOf(xmlTag)!=-1;
	}
	
	public static boolean isFormType(String formTypeParam,SnapshotInventoryGridRecord snapshotInventoryGridRecord) {
		String formType=snapshotInventoryGridRecord.getFormInformation().getFormType();
		return formType!=null && formType.equalsIgnoreCase(formTypeParam);
	}
	
	public static String getFormType(String content) {
		if (hasFormType(content)) {
			int startIndex=content.indexOf(FORM_LEVEL_START_TAG);
			int endIndex=content.indexOf(FORM_LEVEL_END_TAG);
			return content.substring(startIndex+FORM_LEVEL_START_TAG.length(), endIndex);
		}
		return "";
	}
	
	public static boolean hasFormType(String content) {
		return content.indexOf(FORM_LEVEL_START_TAG)!=-1 && content.indexOf(FORM_LEVEL_END_TAG)!=-1;
	}
	
	public static void writeToFile(File file,String content,boolean isAppend) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, isAppend)));
			out.println(content);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(out != null){
				out.close();
			}
		} 
	}
	
	public static String getReverseSQLFileContent(Map<String,File> inventoryNameToReverseSQLFileMap,String inventoryName) {
		File reverseSQLFile=inventoryNameToReverseSQLFileMap.get(inventoryName);	
		return getReverseSQLFileContent(reverseSQLFile);
	}
	
	public static String getReverseSQLFileContent(File reverseSQLFile) {
		if (reverseSQLFile==null) {
			return null;
		}
		String reverseFileContent;
		try {
			reverseFileContent = FileUtils.readContentsFromSQLFile(reverseSQLFile);
			return reverseFileContent;
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		return null;
	}
	
	public static File exportTableToXML(Connection connection,File downloadFolder,Inventory inventory,SnapshotGridRecord snapshotGridRecord,String sql,
			GenericRecordInformation genericRecordInformation,boolean isConvertDFF,boolean isConversion)throws Exception {
		return exportTableToXML(connection,downloadFolder,inventory,snapshotGridRecord,sql,genericRecordInformation,isConvertDFF,isConversion,false);
	}
	
	public static File exportTableToXML(Connection connection,File downloadFolder,Inventory inventory,SnapshotGridRecord snapshotGridRecord,String sql,
			GenericRecordInformation genericRecordInformation,boolean isConvertDFF,boolean isConversion,boolean isExportSnapshotInfo)throws Exception {
		File outputFile=new File(downloadFolder,inventory.getName()+".xml");
		return exportTableToXMLGeneric(connection,outputFile,inventory,snapshotGridRecord,sql,genericRecordInformation,isConvertDFF,
				isConversion,isExportSnapshotInfo);
	}
		
	public static File exportTableToXMLGeneric(Connection connection,File outputFile,Inventory inventory,SnapshotGridRecord snapshotGridRecord,String sql,
			GenericRecordInformation genericRecordInformation,boolean isConvertDFF,boolean isConversion,boolean isExportSnapshotInfo)
					throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		PrintWriter pw=null;
		try
		{
			File file =outputFile;
			file.delete();
			pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,true), "UTF8"));
			
			String label="SNAPSHOT"+snapshotGridRecord.getSnapshotId();
			int inventoryColumnsCount=inventory.getFieldNamesUsedForDataEntry().size();
			List<Field> inventoryFieldsDataOnly=inventory.getFieldsUsedForDataEntry();
			FileUtils.writeFileHeader(pw,inventory.getName(),inventoryFieldsDataOnly,true,isExportSnapshotInfo);
			
			//FileUtils.println("exportTableToXML, sql:\n"+sql);
			statement= connection.prepareStatement(sql);
			if(Config.getReverseFetchSize() > 0) {
				statement.setFetchSize(Config.getReverseFetchSize());
			}
			resultSet=statement.executeQuery();

			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			int recordNumber=0;
			List<DataRow> dataRows=new ArrayList<DataRow>();
			while (resultSet.next()) {
				recordNumber++;
				DataRow dataRow=null;
				if (isConvertDFF) {
					dataRow=getDataRowGeneric(resultSet,inventoryColumnsCount,format,true);
				}
				else {
					dataRow=getDataRow(resultSet,inventoryColumnsCount,format);
				}
				dataRows.add(dataRow);
				if ( (recordNumber % Config.getSnapshotRecordProcessingBatchSize())==0 )
				{	
					writeDataRows(pw,dataRows,label,isExportSnapshotInfo);
					dataRows=new ArrayList<DataRow>();
					genericRecordInformation.setDownloadDownloadedRecordsCount(recordNumber);
					UIUtils.setDownloadTime(genericRecordInformation);
					if (isConversion) {
						genericRecordInformation.setRemarks("Downloaded "+Utils.formatNumberWithComma(recordNumber));
					}
				}
			}
			writeDataRows(pw,dataRows,label,isExportSnapshotInfo);
			FileUtils.writeDataFileFooter(pw);
			genericRecordInformation.setDownloadDownloadedRecordsCount(recordNumber);
			
			return file;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
			IOUtils.closeQuietly(pw);
		}
	}
	
	public static void exportTableToXLSX(Connection connection,File downloadFolder,Inventory inventory,SnapshotGridRecord snapshotGridRecord,String sql,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord)
					throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String XLSX_EXTENSION = "xlsx";
		try
		{			
			int index = 0;	
			int extraColumnsCount=7;
			File file =new File(downloadFolder,inventory.getName()+"."+XLSX_EXTENSION);
			List<File> listFiles = getFilesByName(downloadFolder,file.getName(), XLSX_EXTENSION);
			deleteFiles(listFiles);		
			String label="SNAPSHOT"+snapshotGridRecord.getSnapshotId();
			int inventoryColumnsCount=inventory.getFieldNamesUsedForDataEntry().size();
			List<Field> inventoryFieldsDataOnly=inventory.getFieldsUsedForDataEntry();
			
			//System.out.println("exportTable: \n"+sqlBuffer.toString());
			statement= connection.prepareStatement(sql);
			if(Config.getReverseFetchSize() > 0) {
				statement.setFetchSize(Config.getReverseFetchSize());
			}
			resultSet=statement.executeQuery();

			List<String[]> dataRows=new ArrayList<String[]>();
			String[] headerRow=getHeaderRow(extraColumnsCount,inventoryFieldsDataOnly);
			dataRows.add(headerRow);
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			int recordNumber=0;	
			int fileCnt =0;
			while (resultSet.next()) {
				recordNumber++;
				index=0;
				
				if ( (recordNumber % Config.getSnapshotRecordProcessingBatchSize())==0 ){	
					snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(recordNumber);
					UIUtils.setDownloadTime(snapshotInventoryGridRecord);
				}
				
				DataRow dataRow=getDataRow(resultSet,inventoryColumnsCount,format);
				String[] row=new String[extraColumnsCount+inventoryFieldsDataOnly.size()];
				dataRows.add(row);
				row[index++]=""+recordNumber;
				for ( String column : dataRow.getDataValues() ) {
					if (column==null) {
						column="";
					}
					row[index++]=column;
				}
				row[index++]=label;
				row[index++]="";
				
				String userName=dataRow.getRscLastUpdatedByName();
				if (userName==null) {
					userName="";
				}
				row[index++]=userName;
				
				String str=dataRow.getRscLastUpdateDate();
				if (str==null) {
					str="";
				}
				row[index++]=str;
				
				userName=dataRow.getRscCreatedByName();
				if (userName==null) {
					userName="";
				}
				row[index++]=userName;
				
				str=dataRow.getRscCreationDate();
				if (str==null) {
					str="";
				}
				row[index++]=str;
				
				if((recordNumber % Config.getSnapshotRecordProcessingXlsxBatchSize())==0){
					String fileName = (fileCnt>0 ? inventory.getName()+"."+fileCnt+"."+XLSX_EXTENSION:file.getName());
					file =new File(downloadFolder,fileName);
				    FileUtils.createAndSaveRowsToXLSXFile("Data",file,dataRows);
				    dataRows=new ArrayList<String[]>();
				    headerRow=getHeaderRow(extraColumnsCount,inventoryFieldsDataOnly);
				    dataRows.add(headerRow);
				    fileCnt++;
				}
			}
			snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(recordNumber);
			String fileName = (fileCnt>0 ? inventory.getName()+"."+fileCnt+"."+XLSX_EXTENSION:file.getName());
			file =new File(downloadFolder,fileName);
			FileUtils.createAndSaveRowsToXLSXFile("Data",file,dataRows);
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static void updateProgressLabel(JLabel progressLabel,int recordNumber,int totalRecordsCount)	{
		String formattedNumberCurrent=UIUtils.getFormattedNumber(recordNumber);
		String formattedNumberTotal=UIUtils.getFormattedNumber(totalRecordsCount);
		progressLabel.setText("Downloading "+formattedNumberCurrent+" / "+formattedNumberTotal+" records...");
	}
	
	public static void writeDataRows(PrintWriter pw,List<DataRow> dataRows,String label,boolean isExportSnapshotInfo)	{
		for ( DataRow dataRow:dataRows ){
			writeDataRow(pw,dataRow,label,isExportSnapshotInfo);
		}
		pw.flush();
	}
	
	private static void writeDataRow(PrintWriter pw,DataRow dataRow,String label,boolean isExportSnapshotInfo) {
		pw.println("<r>");
		for ( String column : dataRow.getDataValues() ) {
			if ( column == null || column.trim().length() == 0 ){
				pw.println("<c/>");
			}
			else{
				pw.println("<c>" + StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(column)) + "</c>");
			}
		}
		pw.println("<c>" + StringEscapeUtils.escapeXml10(label) + "</c>");
		if (dataRow.getExtraColumns()!=null) {
			String[] array=dataRow.getExtraColumns();
			StringBuffer values=new StringBuffer("");
			for (String val:array) {
				if (val==null || val.isEmpty()) {
					values.append(" ").append(REPLACEMENT_SEPARATOR);
				}
				else {
					values.append(StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(val))).append(REPLACEMENT_SEPARATOR);
				}
			}
			pw.println("<c>" + values + "</c>");
		}
		else {
			pw.println("<c/>");
		}
		
		if (!isExportSnapshotInfo) {
			String userName=dataRow.getRscLastUpdatedByName();
			pw.println("<c>" + userName+ "</c>");
			pw.println("<c>" + dataRow.getRscLastUpdateDate() + "</c>");
		}
		else {
			pw.println("<c>" + dataRow.getRscLastUpdatedBy()+ "</c>");
			pw.println("<c>" + dataRow.getRscLastUpdateDate() + "</c>");
			pw.println("<c>" + dataRow.getRscCreatedBy()+ "</c>");
			pw.println("<c>" + dataRow.getRscCreationDate() + "</c>");
			if(dataRow.getRscOUid()!=null){
				pw.println("<c>" + dataRow.getRscOUid() + "</c>");
			}else{
				pw.println("<c/>");
			}
			if(dataRow.getRscBGId()!=null){
				pw.println("<c>" + dataRow.getRscBGId() + "</c>");
			}else{
				pw.println("<c/>");
			}
			if(dataRow.getRscInvOrgId()!=null){
				pw.println("<c>" + dataRow.getRscInvOrgId() + "</c>");
			}else{
				pw.println("<c/>");
			}			
			if(dataRow.getRscLedgerId()!=null){
				pw.println("<c>" + dataRow.getRscLedgerId() + "</c>");
			}else{
				pw.println("<c/>");
			}	
			if(dataRow.getRscCOAId()!=null){
				pw.println("<c>" + dataRow.getRscCOAId() + "</c>");
			}else{
				pw.println("<c/>");
			}	
		}
		pw.println("</r>");
	}

	public static StringBuffer getChangesOnlyWhereClause(Map<Integer, String> oracleSeededUserIdToUserNameMap) {
		StringBuffer additionalwhereClauseFilter=new StringBuffer("");
		StringBuffer whereClauseSeededOracleUserIds=ModelUtils.getWhereClauseSeededOracleUserIds(oracleSeededUserIdToUserNameMap);
		additionalwhereClauseFilter.append(" AND ( ").
		append(" rsc_last_updated_by NOT IN (").append(whereClauseSeededOracleUserIds.toString()).append(" ) ").
		append(" Or Rsc_Created_By NOT IN (").append(whereClauseSeededOracleUserIds.toString()).append(") )");
				
		return additionalwhereClauseFilter;
	}
	
	public static void flattenFolders(File sourceFolder,File targetFolder) throws Exception {
		targetFolder.mkdirs();
		Map<String, File> fileNameToFileMap=ModelUtils.getFileNameToFileMap(sourceFolder,true);
		Iterator<String> iterator=fileNameToFileMap.keySet().iterator();
		//int counter=0;
		System.out.println("Processing "+fileNameToFileMap.size()+" files...");
		while (iterator.hasNext()) {
			//counter++;
			String fileName=iterator.next();
			File file=fileNameToFileMap.get(fileName);
			//System.out.println("Processing "+counter+" / "+fileNameToFileMap.size()+" files...");
			File targetFile=new File(targetFolder,file.getName());
			Files.copy(file.toPath(), targetFile.toPath());
		}
		System.out.println("Processing "+fileNameToFileMap.size()+" files COMPLETED.");
	}
	
	public static String computeFormTypeFromReverseSQLFile(File sqlFile) throws Exception {
		String reverseFileContent=FileUtils.readContentsFromSQLFile(sqlFile);
		boolean hasOU=
				(reverseFileContent.indexOf(",cast(null as number)  as rsc_ou_id")==-1 &&
				reverseFileContent.indexOf(",  null as rsc_ou_id") ==-1 ) ||
				reverseFileContent.toLowerCase().indexOf("VALIDATE_OU_NAME".toLowerCase())!=-1;
		
		boolean hasIO=
				(reverseFileContent.indexOf(",cast(null as number)  as rsc_inv_org_id")==-1 &&
				reverseFileContent.indexOf(",  null as rsc_inv_org_id")==-1) ||
				reverseFileContent.toLowerCase().indexOf("validate_io_name".toLowerCase())!=-1;
		
		boolean hasLE=
				reverseFileContent.indexOf(",cast(null as number)  as rsc_ledger_id")==-1 &&
				reverseFileContent.indexOf(",  null as rsc_ledger_id")==-1;
		
		boolean hasBG=
				reverseFileContent.indexOf(",cast(null as number)  as rsc_bg_id")==-1 &&
				reverseFileContent.indexOf(",  null as rsc_bg_id")==-1;
		
		String formType="";
		if (hasOU) {
			formType=OU_LEVEL;
		}
		else 	
			if (hasIO ) {
				formType=IO_LEVEL;
			}
			else 
				if (hasLE) {
					formType=LE_LEVEL;
				}
				else 
					if (hasBG) {
						formType=BG_LEVEL;
					}
					else {
						formType=GLOBAL_LEVEL;
					}
		return formType;
	}

	private static Map<Integer, List<DBFormInfoEntry>> getFunctionIdToDBFormInfoEntryMap(Connection connection)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
/*
drop table TEMPMENU;
create table TEMPMENU AS 
select * from (
select T.*,(select application_name from fnd_responsibility R,fnd_application_tl APP WHERE MENU_id=T.TOP_MENU_ID AND R.APPLICATION_ID=APP.APPLICATION_ID  and language='US' and rownum=1) APPLICATION_NAME,
(select application_ID from fnd_responsibility R WHERE MENU_id=T.TOP_MENU_ID and rownum=1) APPLICATION_ID
from (
select FUNCTION_ID, SYS_CONNECT_BY_PATH(PROMPT,' > ') as FULL_PATH, SYS_CONNECT_BY_PATH(MENU_ID,' > ') as FULL_MENU_IDS,
CONNECT_BY_ROOT MENU_ID AS TOP_MENU_ID
  from FND_MENU_ENTRIES_VL   where  prompt is not null  -- start with MENU_ID =78119
  connect by prior SUB_MENU_ID = MENU_ID ) T where t.function_id is not null
  ) where application_id is not null;
CREATE INDEX TEMPMENU_FID  ON TEMPMENU (FUNCTION_ID);


  */
			String sqlQuery= " Select T.*, "+
					" (Select Distinct T1.User_Form_Name From Fnd_Form_Vl T1 Where T1.Form_Id = T.Form_Id AND ROWNUM=1) Form_Name"+
					" FROM ("+
					" Select Distinct Function_Id,(Select Full_Path From Tempmenu T1 Where T1.Function_Id =T2.Function_Id And T1.Application_Id =T2.Application_Id And Rownum=1 ) Full_Path,"+
					" Application_Name, (SELECT FORM_ID FROM FND_FORM_FUNCTIONS_VL T1 WHERE T1.FUNCTION_ID =T2.FUNCTION_ID ) FORM_ID"+
					" , (Select USER_FUNCTION_NAME From FND_FORM_FUNCTIONS_VL T1 Where T1.Function_Id =T2.Function_Id ) USER_FUNCTION_NAME"+
					" From Tempmenu T2 Where Application_Name is not null Order By Function_Id"+
					") T";
			System.out.println("sqlQuery:\n"+sqlQuery);
			
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			Map<Integer,List<DBFormInfoEntry>> toReturn=new HashMap<Integer,List<DBFormInfoEntry>>();
			int cnt=0;
			while (resultSet.next()) {
				cnt++;
				if (cnt % 10000 ==0) {
					System.out.println("Loading menu paths... "+cnt);
				}
				
				int functionId= resultSet.getInt("FUNCTION_ID");
				String formName= resultSet.getString("FORM_NAME");
				String applicationName= resultSet.getString("APPLICATION_NAME");
				//String secondApplicationName= resultSet.getString("SEC_APPLICATION_NAME");
				String userFunctionName= resultSet.getString("USER_FUNCTION_NAME");
				String fullPath= resultSet.getString("FULL_PATH");
				
				DBFormInfoEntry dbFormInfoEntry=new DBFormInfoEntry();
				dbFormInfoEntry.setFunctionId(functionId);
				if (formName==null) {
					dbFormInfoEntry.setFormName(userFunctionName);
				}
				else {
					dbFormInfoEntry.setFormName(formName);
				}
				dbFormInfoEntry.setApplicationName(applicationName);
				dbFormInfoEntry.setFullPath(fullPath);

				List<DBFormInfoEntry> list=toReturn.get(functionId);
				if (list==null) {
					list=new ArrayList<DBFormInfoEntry>();
					toReturn.put(functionId,list);
				}
				list.add(dbFormInfoEntry);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	private static Map<Integer, DBFormInfoEntry> getFunctionIdToDBFormInfoEntryNoMenuPathMap(Connection connection)
			throws Exception
			{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= " Select Function_Id,"+
        " (Select Application_Name From Fnd_Application_Tl T1 Where T1.Application_Id=T2.Application_Id And Language='US') Application_Name,"+
        " (Select Distinct T1.User_Form_Name From Fnd_Form_Vl T1 Where T1.Form_Id = T2.Form_Id And T1.Application_Id =T2.Application_Id) Form_Name,Type,"+
        " User_Function_Name"+
        " From Fnd_Form_Functions_Vl T2";
			System.out.println("sqlQuery:\n"+sqlQuery);

			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			Map<Integer,DBFormInfoEntry> toReturn=new HashMap<Integer,DBFormInfoEntry>();
			int cnt=0;
			while (resultSet.next()) {
				cnt++;
				if (cnt % 10000 ==0) {
					System.out.println("Loading getFunctionIdToFormInformationNoMenuPathMap... "+cnt);
				}

				int functionId= resultSet.getInt("FUNCTION_ID");
				String formName= resultSet.getString("FORM_NAME");
				String applicationName= resultSet.getString("APPLICATION_NAME");
				String userFunctionName= resultSet.getString("USER_FUNCTION_NAME");

				DBFormInfoEntry dbFormInfoEntry=new DBFormInfoEntry();
				dbFormInfoEntry.setFunctionId(functionId);
				if (formName==null) {
					dbFormInfoEntry.setFormName(userFunctionName);
				}
				else {
					dbFormInfoEntry.setFormName(formName);
				}
				dbFormInfoEntry.setApplicationName(applicationName);

				toReturn.put(functionId,dbFormInfoEntry);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
			}

	public static void generateInventoryFormInformation(File inventoriesFolder,String jdbcString,File outputFile) throws Exception {
		Connection connection=null;
		try {
			if (!inventoriesFolder.exists()) {
				throw new Exception("FOLDER DOES NOT EXIST: "+inventoriesFolder.getAbsolutePath());
			}
			Map<String, File> inventoryNameToInventoryFileMap=ModelUtils.getFileNameToFileMap(inventoriesFolder,true);
		
			connection=DatabaseUtils.getJDBCConnection(jdbcString,"APPS","APPS");
			System.out.println("generateXMLInventorytoFormNameMappings, before query function ids");
			Map<Integer, List<DBFormInfoEntry>> functionIdToDBFormInfoEntryMap=getFunctionIdToDBFormInfoEntryMap(connection);
			System.out.println("generateXMLInventorytoFormNameMappings, functionIdToDBFormInfoEntryMap:"+functionIdToDBFormInfoEntryMap.size());
			Map<Integer,DBFormInfoEntry> functionIdToDBFormInfoEntryNoMenuPathMap=getFunctionIdToDBFormInfoEntryNoMenuPathMap(connection);
			System.out.println("generateXMLInventorytoFormNameMappings, functionIdToDBFormInfoEntryNoMenuPathMap:"+functionIdToDBFormInfoEntryNoMenuPathMap.size());
			
			InventoriesDocument inventoriesDocument=InventoriesDocument.Factory.newInstance();
			Inventories inventories=inventoriesDocument.addNewInventories();
									
			Iterator<String> iterator=inventoryNameToInventoryFileMap.keySet().iterator();
			System.out.println("INVENTORIES SIZE: "+inventoryNameToInventoryFileMap.size());
			int counter=0;
			while (iterator.hasNext()) {
				String inventoryName=iterator.next();
				counter++;
				
				if (inventoryName.startsWith("R11 - ") ) {
					continue;
				}
				
				if (counter % 1000 ==0) {
					System.out.println(counter+" / "+inventoryNameToInventoryFileMap.size());
				}
				File inventoryFile=inventoryNameToInventoryFileMap.get(inventoryName);
				Inventory inventory=FileUtils.getInventory(inventoryFile,inventoryName);
				String functionIds=inventory.getFunctionIds();
				int index=functionIds.indexOf(",");
				if (index!=-1) {
					functionIds=functionIds.substring(0,index);
				}
				int functionId=-1;
				try{
					functionId=Integer.valueOf(functionIds);
				}
				catch (Exception e) {
					System.out.println("inv: '"+inventoryName+"' inventory.getFunctionIds(): '"+inventory.getFunctionIds()+"' ERROR: "+e.getMessage());
					//e.printStackTrace();
					continue;
				}	
				
				List<DBFormInfoEntry> dbFormInfoEntryList=functionIdToDBFormInfoEntryMap.get(functionId);
				if (dbFormInfoEntryList==null || dbFormInfoEntryList.isEmpty()) {
					DBFormInfoEntry dbFormInfoEntry=functionIdToDBFormInfoEntryNoMenuPathMap.get(functionId);
					if (dbFormInfoEntry==null) {
						System.out.println("inv: '"+inventoryName+"' inventory.getFunctionIds(): '"+inventory.getFunctionIds()+"' NOT FOUND!!!");
						continue;
					}
					dbFormInfoEntryList=new ArrayList<DBFormInfoEntry>();
					dbFormInfoEntryList.add(dbFormInfoEntry);
				}			
				
				for (DBFormInfoEntry dbFormInfoEntry:dbFormInfoEntryList) {
					FormInfo formInfo=inventories.addNewFormInfo();
					formInfo.setFunctionId(BigInteger.valueOf(functionId));
					formInfo.setInventoryName(inventoryName);
					formInfo.setApplicationName(dbFormInfoEntry.getApplicationName());
					String formNameXML=Utils.stripNonValidXMLCharacters(dbFormInfoEntry.getFormName());
					formInfo.setFormName(formNameXML);
					String menuPathXML=Utils.stripNonValidXMLCharacters(dbFormInfoEntry.getFullPath());
					
					String temp=menuPathXML;
					StringBuffer newPath=new StringBuffer("");
					for (int i = 0; i <temp.length(); i++){
					    char c = temp.charAt(i);        
					    if (c=='>' || c==' ') {
					    	// skip those characters at the starta
					    }
					    else {
					    	newPath.append(temp.substring(i));
					    	break;
					    }
					}
									
					formInfo.setMenuPath(newPath.toString());
				}
				
				if (inventoryName.equalsIgnoreCase("Accounting Setups - Intercompany Accounts")) {
					System.out.println("inv: '"+inventoryName+"' dbFormInfoEntryList: "+dbFormInfoEntryList);
				}
			}
			XmlOptions xmlOptions = new XmlOptions();
		    xmlOptions.setSavePrettyPrint();
		    inventoriesDocument.save(outputFile,xmlOptions);
		    System.out.println("generateXMLInventorytoFormNameMappings, COMPLETED!");
		}
		finally {
			DirectConnectDao.closeQuietly(connection);	  
		}
	}
	
	public static Map<String, List<FormInfo>> getInventoryNameToFormInfoMap(TabSnapshotsPanel tabSnapshotsPanel, String connectionOracleRelease) 
			throws Exception {
		File inventoriesFormInfoFile=tabSnapshotsPanel.getSnapshotPackageSelectionPanel().getInventoriesFormInfoFile(
				connectionOracleRelease);
		InventoriesDocument inventoriesDocument=ModelUtils.getInventoriesDocument(inventoriesFormInfoFile);
		Inventories inventories=inventoriesDocument.getInventories();
		Map<String, List<FormInfo>> toReturn=new  HashMap<String, List<FormInfo>>();
		FormInfo[] formInfoArray=inventories.getFormInfoArray();
		for (FormInfo formInfo:formInfoArray) {
			List<FormInfo> list=toReturn.get(formInfo.getInventoryName());
			if (list==null) {
				list=new ArrayList<FormInfo>();
				toReturn.put(formInfo.getInventoryName(),list);
			}
			list.add(formInfo);
		}
		return toReturn;
	}
	
	public static InventoriesDocument validateInventoriesDocument(File xmlFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = InjectUtils.getXmlOptions(validationErrors);
		InventoriesDocument inventoriesDocument =InventoriesDocument.Factory.parse(xmlFile,xmlOptions);
		InjectUtils.validateXMLBeanDocument(inventoriesDocument,xmlOptions,validationErrors);
		return inventoriesDocument;
	}	

	public static InventoriesDocument getInventoriesDocument(File xmlFile) throws Exception {
		InventoriesDocument inventoriesDocument =validateInventoriesDocument(xmlFile);
		return inventoriesDocument;
	}

	public static void startModalWindowInThread(final JFrame frame,final ControllerModalWindow controllerModalWindow) {
		final int width=450;
		final int height=150;
		Thread thread = new Thread(){
			public void run(){
				UIUtils.displayOperationInProgressModalWindow(frame,width,height,"Operation in Progress...",controllerModalWindow,SnapshotMain.getSharedApplicationIconPath());
			}
		};

		thread.start();				
	}
	
	public static List<SnapshotInventoryGridRecord> getComparisonSnapshotInventoryGridRecordsList(Connection connection,List<SnapshotGridRecord> list) throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlBuffer =new StringBuffer("");
			
			SnapshotGridRecord baseSnapshotGridRecord=list.get(0);
			
			sqlBuffer.append("select ISN.INVENTORY_ID,I.name from INVENTORY_TO_SNAPSHOT ISN, INVENTORY I where I.id=ISN.INVENTORY_ID and ");
			sqlBuffer.append("ISN.SNAPSHOT_ID=").append(baseSnapshotGridRecord.getSnapshotId());
			sqlBuffer.append(" AND ISN.INVENTORY_ID NOT IN ( select DISTINCT INVENTORY_ID from INVENTORY_TO_SNAPSHOT where SNAPSHOT_ID in (");
			int counter=0;
			for (SnapshotGridRecord snapshotGridRecord:list) {
				sqlBuffer.append(snapshotGridRecord.getSnapshotId());
				if ( (counter+1) <list.size()) {
					sqlBuffer.append(",");
				}
				counter++;
			}
			sqlBuffer.append(") And (Status Not In ('S','W') or msg = 'Unsupported (SQL not available)') ) order by INVENTORY_ID");
			//FileUtils.println("sqlBuffer:\n"+sqlBuffer.toString());
					
			statement= connection.prepareStatement(sqlBuffer.toString());
			resultSet=statement.executeQuery();
			List<SnapshotInventoryGridRecord> toReturn=new ArrayList<SnapshotInventoryGridRecord>();
			while (resultSet.next()) {
				int tableId=resultSet.getInt("INVENTORY_ID");
				String inventoryName=resultSet.getString("NAME");
			
				SnapshotInventoryGridRecord snapshotInventoryGridRecord=new SnapshotInventoryGridRecord(inventoryName);
				snapshotInventoryGridRecord.setTableId(tableId);
								
				toReturn.add(snapshotInventoryGridRecord);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static List<Integer> getPrimaryKeysPositionList(Inventory inventory) {
		List<Integer> toReturn=new ArrayList<Integer>();
		List<Field> fieldsDataEntry=inventory.getFieldsUsedForDataEntry();
		for (int i=0;i<fieldsDataEntry.size();i++) {
			Field field=fieldsDataEntry.get(i);
			if (field.getComponentOfPrimaryKey()) {
				//System.out.println("field:"+field.getName()+" i:"+i+" IS PRIMARY KEY!!!!!");
				toReturn.add( i+1 );
			}
		}
		return toReturn;
	}
	
	public static int generateComparisonTableRecords(SnapshotComparisonController snapshotComparisonController,String connectionOracleRelease,
			String dbSchema,Connection connection,Inventory inventory,int comparisonId,
			int tableId,int sourceSnapshotId,int targetSnapshotId, String additionalwhereClauseFilter)
	throws Exception
	{
		File reverseSQLFile=snapshotComparisonController.getSnapshotComparisonAnalysisFrame().getTabSnapshotsPanel().
				getSnapshotPackageSelectionPanel().getInventoryNameToReverseSQLFileMap(connectionOracleRelease).get(inventory.getName());	
		if (reverseSQLFile==null) {
			throw new Exception("Unable to find the SQL query file.");
		}
		String reverseFileContent=FileUtils.readContentsFromSQLFile(reverseSQLFile);
		int largeColumnPosition=getLargeColumnPosition(reverseFileContent);
		
		if (largeColumnPosition==-1) {
			return generateComparisonTableRecordsNoCLOB(dbSchema,connection,inventory,comparisonId,tableId,sourceSnapshotId,targetSnapshotId,additionalwhereClauseFilter);
		}
		return generateComparisonTableRecordsClob(dbSchema,connection,inventory,comparisonId,tableId,sourceSnapshotId,targetSnapshotId,additionalwhereClauseFilter,largeColumnPosition);
	}
	
	public static int generateComparisonTableRecordsNoCLOB(String dbSchema,Connection connection,Inventory inventory,int comparisonId,
			int tableId,int sourceSnapshotId,int targetSnapshotId, String additionalwhereClauseFilter)
	throws Exception
	{
		PreparedStatement statement = null;
		StringBuffer sqlBuffer=new StringBuffer("");
		try
		{
			initComparisonTable(dbSchema,connection,inventory,tableId);
			
			/*
			 * 
			 * COMPARE ALL COLUMNS FOR ALL RECORDS FROM B2 TO B1 ONLY => THIS WILL GIVE ALL RECORDS FROM B2 WHICH HAVE BEEN ADDED OR UPDATED OR PK CHANGED.
			 * THEN ADD
			 * COMPARE COLUMNS PK FOR ALL RECORDS FROM B1 TO B2 ONLY => THIS WILL GIVE ALL RECORDS FROM B1 WHERE THE PKS HAVE CHANGED OR DELETED 
			 * 
			 * select * from (  select 38 ,1 ,2,C1,C2,C3,C4,C5,C6,C7 from ( select C1,C2,C3,C4,C5,C6,C7 from
			 * XX_TEST.T3326 where BASELINE_ID=2 minus select C1,C2,C3,C4,C5,C6,C7 from XX_TEST.T3326 where BASELINE_ID=1 ) 
			 * union
			 * select 38 ,1 ,2,C1,C2,C3,'','','','' from ( select C1,C2,C3 from XX_TEST.T3326 where BASELINE_ID=1 minus select
			 * C1,C2,C3 from XX_TEST.T3326 where BASELINE_ID=2 ) 
			 * )
			 */
			String tablePrefix=DB_COMPARISON_TABLES_PREFIX;
			List<Integer> primaryKeysPositionList=ModelUtils.getPrimaryKeysPositionList(inventory);
			int totalInventoryDataEntryColumnsCount=inventory.getFieldNamesUsedForDataEntry().size();

			sqlBuffer.append("insert into ");
			sqlBuffer.append(tablePrefix);
			sqlBuffer.append(tableId);
			sqlBuffer.append(" select * from ( ");

			StringBuffer sqlBufferTargetMinusSource=new StringBuffer("");
			sqlBufferTargetMinusSource.append(" select ");
			sqlBufferTargetMinusSource.append(comparisonId).append(" as COMPARISON_ID");
			sqlBufferTargetMinusSource.append(" ,");
			sqlBufferTargetMinusSource.append(sourceSnapshotId).append(" as SOURCE_SNAPSHOT_ID");
			sqlBufferTargetMinusSource.append(" ,");
			sqlBufferTargetMinusSource.append(targetSnapshotId).append(" as TARGET_SNAPSHOT_ID");
			for (int i=1;i<=totalInventoryDataEntryColumnsCount;i++) {
				sqlBufferTargetMinusSource.append(",C"+i);
			}
			sqlBufferTargetMinusSource.append(",rsc_last_updated_by");
			sqlBufferTargetMinusSource.append(",rsc_last_update_date");
			sqlBufferTargetMinusSource.append(",rsc_created_by");
			sqlBufferTargetMinusSource.append(",rsc_creation_date");
			sqlBufferTargetMinusSource.append(" from ( ");
			String targetSQL=getSQLComparisonAllColumns(tableId,targetSnapshotId,totalInventoryDataEntryColumnsCount,additionalwhereClauseFilter);
			sqlBufferTargetMinusSource.append(targetSQL);
			sqlBufferTargetMinusSource.append(" minus ");
			String sourceSQL=getSQLComparisonAllColumns(tableId,sourceSnapshotId,totalInventoryDataEntryColumnsCount,additionalwhereClauseFilter);		
			sqlBufferTargetMinusSource.append(sourceSQL);
			sqlBufferTargetMinusSource.append(" ) ");
			sqlBuffer.append(sqlBufferTargetMinusSource);

			sqlBuffer.append(" union ");

			StringBuffer sqlBufferSourceMinusTarget=new StringBuffer("");
			sqlBufferSourceMinusTarget.append(" select ");
			sqlBufferSourceMinusTarget.append(comparisonId).append(" as COMPARISON_ID");
			sqlBufferSourceMinusTarget.append(" ,");
			sqlBufferSourceMinusTarget.append(sourceSnapshotId).append(" as SOURCE_SNAPSHOT_ID");
			sqlBufferSourceMinusTarget.append(" ,");
			sqlBufferSourceMinusTarget.append(targetSnapshotId).append(" as TARGET_SNAPSHOT_ID");
			sqlBufferSourceMinusTarget.append(" ,");
			String sqlComparisonPKColumnsSelectClauseOnly=getSQLComparisonPKColumnsSelectClauseOnly(primaryKeysPositionList,totalInventoryDataEntryColumnsCount);
			sqlBufferSourceMinusTarget.append(sqlComparisonPKColumnsSelectClauseOnly);
			sqlBufferSourceMinusTarget.append(" from ( ");
			sourceSQL=getSQLComparisonPKColumns(tableId,sourceSnapshotId,primaryKeysPositionList,totalInventoryDataEntryColumnsCount,additionalwhereClauseFilter);
			sqlBufferSourceMinusTarget.append(sourceSQL);
			sqlBufferSourceMinusTarget.append(" minus ");
			targetSQL=getSQLComparisonPKColumns(tableId,targetSnapshotId,primaryKeysPositionList,totalInventoryDataEntryColumnsCount,additionalwhereClauseFilter);
			sqlBufferSourceMinusTarget.append(targetSQL);
			sqlBufferSourceMinusTarget.append(" ) ");
			sqlBuffer.append(sqlBufferSourceMinusTarget);

			sqlBuffer.append(" ) ");

			statement= connection.prepareStatement(sqlBuffer.toString());
			//FileUtils.println("generateComparisonTableRecords, sql:\n"+sqlBuffer.toString());
			
			int total=statement.executeUpdate();
			connection.commit();
			
			return total;
		}
		catch(Exception e) {
			FileUtils.println("ERROR generateComparisonTableRecords, inventory: '"+inventory.getName()+"' tableId: "+tableId+" sql:\n"+sqlBuffer.toString());
			FileUtils.printStackTrace(e);
			throw e;
		}
		finally
		{
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	/*
	 * TODO: ignore CLOB columns for now as unsupported because this is very complex to implement see below:
	 * 
	 * http://www.dba-oracle.com/t_compare_clob_columns_dbms_lob.htm
	 * 
	 *  Select * From Xx_Rs_Josefinah01.Xx_Rsc_T795 Where Snapshot_Id=1
		Select * From Xx_Rs_Josefinah01.Xx_Rsc_T795 Where Snapshot_Id=2
		UPDATE  Xx_Rs_Josefinah01.Xx_Rsc_T795 SET C3='odtest - '||C3 where c1='AX_INVSDOSI_GL_ACCOUNT_TO' and c2='Load' and Snapshot_Id=2
		Select
   		a.C1,A.C2,
   		dbms_lob.compare(nvl(a.c3,'Null'),nvl(b.c3,'Null'))
		from
   		Xx_Rs_Josefinah01.XX_RSC_T795   a,
   		Xx_Rs_Josefinah01.XX_RSC_T795   b
		Where
		A.Snapshot_Id=1 And B.Snapshot_Id=2
		AND A.C1 = B.C1 AND A.C2 = B.C2
		
		
		LOGIC: 
		ASSUMPTION: THIS LOGIC BREAKS DOWN IF THERE ARE DUPLICATE RECORDS BASED ON THE PK.
		1. JOIN SOURCE, TARGET BASED ON PK AND USE dbms_lob.compare ON ALL non PK columns
		2. SELECT ALL FROM SOURCE WHERE PK DON'T EXIST IN TARGET
		3. SELECT ALL FROM TARGET WHERE PK DON'T EXIST IN SOURCE
		4. UNION SET 1, 2 A AND 3.
	 */
	public static int generateComparisonTableRecordsClob(String dbSchema,Connection connection,Inventory inventory,int comparisonId,
			int tableId,int sourceSnapshotId,int targetSnapshotId, String additionalwhereClauseFilter,int largeColumnPosition)
	throws Exception
	{
		PreparedStatement statement = null;
		StringBuffer sqlBuffer=new StringBuffer("");
		try
		{
			initComparisonTable(dbSchema,connection,inventory,tableId);
			
			String tablePrefix=DB_COMPARISON_TABLES_PREFIX;
			List<Integer> primaryKeysPositionList=ModelUtils.getPrimaryKeysPositionList(inventory);
			int totalInventoryDataEntryColumnsCount=inventory.getFieldNamesUsedForDataEntry().size();

			sqlBuffer.append("insert into ");
			sqlBuffer.append(tablePrefix);
			sqlBuffer.append(tableId);
			sqlBuffer.append(" select * from ( ");

			StringBuffer sqlBufferTargetMinusSource=new StringBuffer("");
			sqlBufferTargetMinusSource.append(" select ");
			sqlBufferTargetMinusSource.append(comparisonId);
			sqlBufferTargetMinusSource.append(" ,");
			sqlBufferTargetMinusSource.append(sourceSnapshotId);
			sqlBufferTargetMinusSource.append(" ,");
			sqlBufferTargetMinusSource.append(targetSnapshotId);
			for (int i=1;i<=totalInventoryDataEntryColumnsCount;i++) {
				if (i==largeColumnPosition) {
					// ignore this column
					sqlBufferTargetMinusSource.append(",''");
				}
				else {
					sqlBufferTargetMinusSource.append(",C"+i);
				}
			}
			sqlBufferTargetMinusSource.append(",rsc_last_updated_by");
			sqlBufferTargetMinusSource.append(",rsc_last_update_date");
			sqlBufferTargetMinusSource.append(",rsc_created_by");
			sqlBufferTargetMinusSource.append(",rsc_creation_date");
			sqlBufferTargetMinusSource.append(" from ( ");
			
			
			String targetSQL=getSQLComparisonAllColumnsClob(tableId,targetSnapshotId,totalInventoryDataEntryColumnsCount,additionalwhereClauseFilter,largeColumnPosition);
			sqlBufferTargetMinusSource.append(targetSQL);
			sqlBufferTargetMinusSource.append(" minus ");
			String sourceSQL=getSQLComparisonAllColumnsClob(tableId,sourceSnapshotId,totalInventoryDataEntryColumnsCount,additionalwhereClauseFilter,largeColumnPosition);		
			sqlBufferTargetMinusSource.append(sourceSQL);
			sqlBufferTargetMinusSource.append(" ) ");
			sqlBuffer.append(sqlBufferTargetMinusSource);

			sqlBuffer.append(" union ");

			StringBuffer sqlBufferSourceMinusTarget=new StringBuffer("");
			sqlBufferSourceMinusTarget.append(" select ");
			sqlBufferSourceMinusTarget.append(comparisonId);
			sqlBufferSourceMinusTarget.append(" ,");
			sqlBufferSourceMinusTarget.append(sourceSnapshotId);
			sqlBufferSourceMinusTarget.append(" ,");
			sqlBufferSourceMinusTarget.append(targetSnapshotId);
			sqlBufferSourceMinusTarget.append(" ,");
			String sqlComparisonPKColumnsSelectClauseOnly=getSQLComparisonPKColumnsSelectClauseOnly(primaryKeysPositionList,totalInventoryDataEntryColumnsCount);
			sqlBufferSourceMinusTarget.append(sqlComparisonPKColumnsSelectClauseOnly);
			sqlBufferSourceMinusTarget.append(" from ( ");
			sourceSQL=getSQLComparisonPKColumns(tableId,sourceSnapshotId,primaryKeysPositionList,totalInventoryDataEntryColumnsCount,additionalwhereClauseFilter);
			sqlBufferSourceMinusTarget.append(sourceSQL);
			sqlBufferSourceMinusTarget.append(" minus ");
			targetSQL=getSQLComparisonPKColumns(tableId,targetSnapshotId,primaryKeysPositionList,totalInventoryDataEntryColumnsCount,additionalwhereClauseFilter);
			sqlBufferSourceMinusTarget.append(targetSQL);
			sqlBufferSourceMinusTarget.append(" ) ");
			sqlBuffer.append(sqlBufferSourceMinusTarget);

			sqlBuffer.append(" ) ");

			statement= connection.prepareStatement(sqlBuffer.toString());
			int total=statement.executeUpdate();
			connection.commit();
			
			return total;
		}
		catch(Exception e) {
			FileUtils.println("ERROR generateComparisonTableRecordsClob, inventory: '"+inventory.getName()+"' tableId: "+tableId+" sql:\n"+sqlBuffer.toString());
			FileUtils.printStackTrace(e);
			throw e;
		}
		finally
		{
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static void initComparisonTable(String dbSchema,Connection connection,Inventory inventory,int tableId) throws Exception
	{
		String comparisonTablePrefix=DB_COMPARISON_TABLES_PREFIX;
		String comparisonTableName=DB_COMPARISON_TABLES_PREFIX+tableId;
		boolean hasComparisonTable=hasComparisonTable(dbSchema,connection,comparisonTableName);
		
		List<Integer> primaryKeysPositionList=ModelUtils.getPrimaryKeysPositionList(inventory);
		if (!hasComparisonTable) {
			// compare the inventories with the Std table first.
			String dataTableName=DB_TABLES_PREFIX+tableId;
			int inventoryColumnsCount=inventory.getFieldsUsedForDataEntry().size();
			int xmlInventoryColumnsCount=inventoryColumnsCount;
			validateInventoryChanges(connection,dataTableName,xmlInventoryColumnsCount);

			String preColumnsText="COMPARISON_ID int, SOURCE_BASELINE_ID int, TARGET_BASELINE_ID int,";
			String postColumnsText="rsc_last_updated_by number,rsc_last_update_date DATE,rsc_created_by number,"+
					"rsc_creation_date DATE";

			createTableDefinition(connection,inventoryColumnsCount,tableId,comparisonTablePrefix,preColumnsText,postColumnsText,-1,false);
			createComparisonTableIndexes(connection,tableId,DB_COMPARISON_TABLES_PREFIX,primaryKeysPositionList);
		}
	}
	
	private static String getSQLComparisonAllColumns(int inventoryId,int snapshotId,int totalInventoryDataEntryColumnsCount,String additionalwhereClauseFilter)
	{
		if (totalInventoryDataEntryColumnsCount==0) {
			return null;
		}
		StringBuffer sqlQueryBuffer=new StringBuffer("");
		sqlQueryBuffer.append("select ");
		for (int i=1;i<=totalInventoryDataEntryColumnsCount;i++) {
			sqlQueryBuffer.append("C").append(i).append(",");
		}
		sqlQueryBuffer.append("rsc_last_updated_by");
		sqlQueryBuffer.append(",rsc_last_update_date");
		sqlQueryBuffer.append(",rsc_created_by");
		sqlQueryBuffer.append(",rsc_creation_date");
		
		sqlQueryBuffer.append(" from ");
		sqlQueryBuffer.append(DB_TABLES_PREFIX);
		sqlQueryBuffer.append(inventoryId);
		sqlQueryBuffer.append(" where SNAPSHOT_ID=");
		sqlQueryBuffer.append(snapshotId);
		
		if (additionalwhereClauseFilter!=null) {
			sqlQueryBuffer.append(additionalwhereClauseFilter);
		}
		
		return sqlQueryBuffer.toString();
	}
	
	private static String getSQLComparisonAllColumnsClob(int inventoryId,int snapshotId,int totalInventoryDataEntryColumnsCount,String additionalwhereClauseFilter
			,int largeColumnPosition)
	{
		if (totalInventoryDataEntryColumnsCount==0) {
			return null;
		}
		StringBuffer sqlQueryBuffer=new StringBuffer("");
		sqlQueryBuffer.append("select ");
		for (int i=1;i<=totalInventoryDataEntryColumnsCount;i++) {
			if (i==largeColumnPosition) {
				// ignore this column
				sqlQueryBuffer.append("''").append(",");
			}
			else {
				sqlQueryBuffer.append("C").append(i).append(",");
			}
		}
		sqlQueryBuffer.append("rsc_last_updated_by");
		sqlQueryBuffer.append(",rsc_last_update_date");
		sqlQueryBuffer.append(",rsc_created_by");
		sqlQueryBuffer.append(",rsc_creation_date");
		
		sqlQueryBuffer.append(" from ");
		sqlQueryBuffer.append(DB_TABLES_PREFIX);
		sqlQueryBuffer.append(inventoryId);
		sqlQueryBuffer.append(" where SNAPSHOT_ID=");
		sqlQueryBuffer.append(snapshotId);
		
		if (additionalwhereClauseFilter!=null) {
			sqlQueryBuffer.append(additionalwhereClauseFilter);
		}
		
		return sqlQueryBuffer.toString();
	}

	private static String getSQLComparisonPKColumns(int inventoryId,int snapshotId,
			List<Integer> primaryKeysPositionList,int totalInventoryDataEntryColumnsCount,String additionalwhereClauseFilter)
	{
		/*
		 * Primary keys may be defined like this in the inventory:  C1,..,C3,..,C7
		 * so not in sequence.
		 * Also this query is used for comparison so we need to add '' for non PK columns so that the total numbers of columns
		 * is represented and can be UNION'ed with the rest.
		 */
		Set<Integer> pkMap=new HashSet<Integer>();
		for (Integer primaryKeysPosition:primaryKeysPositionList){
			pkMap.add(primaryKeysPosition);
		}
		StringBuffer sqlBuffer=new StringBuffer("");
		sqlBuffer.append("select ");
		String sqlComparisonPKColumnsSelectClauseOnly=getSQLComparisonPKColumnsSelectClauseOnly(primaryKeysPositionList,totalInventoryDataEntryColumnsCount);
		sqlBuffer.append(sqlComparisonPKColumnsSelectClauseOnly);
		sqlBuffer.append(" from ");
		sqlBuffer.append(DB_TABLES_PREFIX);
		sqlBuffer.append(inventoryId);
		sqlBuffer.append(" where SNAPSHOT_ID=");
		sqlBuffer.append(snapshotId);
		
		if (additionalwhereClauseFilter!=null) {
			sqlBuffer.append(additionalwhereClauseFilter);
		}

		return sqlBuffer.toString();
	}
	
	private static String getSQLComparisonPKColumnsSelectClauseOnly(List<Integer> primaryKeysPositionList,int totalInventoryDataEntryColumnsCount)
	{
		Set<Integer> pkMap=new HashSet<Integer>();
		for (Integer primaryKeysPosition:primaryKeysPositionList){
			pkMap.add(primaryKeysPosition);
		}
		StringBuffer sqlBuffer=new StringBuffer("");
		for (int i=1;i<=totalInventoryDataEntryColumnsCount;i++){
			String columnName="C"+i;
			if (pkMap.contains(i)) {
				sqlBuffer.append(columnName).append(",");
			}
			else {
				sqlBuffer.append("'' as ").append(columnName).append(",");
			}
		}
		sqlBuffer.append("NULL").append(" as rsc_last_updated_by");
		sqlBuffer.append(",NULL").append(" as rsc_last_update_date");
		sqlBuffer.append(",NULL").append(" as rsc_created_by");
		sqlBuffer.append(",NULL").append(" as rsc_creation_date");
		
		return sqlBuffer.toString();
	}	

	private static boolean hasComparisonTable(String dbSchema,Connection connection,String comparisonTableName) throws ClassNotFoundException, SQLException
	{
		return hasTable(dbSchema,connection,comparisonTableName);
	}
	
	private static boolean hasDataTable(String dbSchema,Connection connection,int tableId) throws ClassNotFoundException, SQLException
	{
		String tableName=DB_TABLES_PREFIX+tableId;
		return hasTable(dbSchema,connection,tableName);
	}
	
	private static boolean hasDFFTable(String dbSchema,Connection connection,int tableId) throws ClassNotFoundException, SQLException
	{
		String tableName=DB_DFF_TABLES_PREFIX+tableId;
		return hasTable(dbSchema,connection,tableName);
	}
	
	public static boolean hasTable(String dbSchema,Connection connection,String tableName)throws ClassNotFoundException, SQLException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{	
			String sql ="SELECT count(*) FROM all_tables where upper(OWNER) = '"+dbSchema.toUpperCase()+"' and table_name = '"+tableName+"'"; 
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			resultSet.next();
			int count=resultSet.getInt(1);
			return count!=0;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static boolean tableHasData(String dbSchema,Connection connection,String tableName) throws ClassNotFoundException, SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{	
			String sql ="SELECT count(*) FROM " + dbSchema + "." + tableName; 
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			resultSet.next();
			int count=resultSet.getInt(1);
			return count!=0;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
		
	}
	
	private static StringBuffer getPrimaryKeysColumnsStringBuffer(List<Integer> primaryKeysPositionList,boolean isAlias)
	{
		StringBuffer pkColumnsStringBuffer=new StringBuffer("");
		for (int i=0;i<primaryKeysPositionList.size();i++){
			Integer primaryKeysPosition=primaryKeysPositionList.get(i);
			pkColumnsStringBuffer.append("C").append(primaryKeysPosition);
			if (isAlias) {
				pkColumnsStringBuffer.append(" as C").append(primaryKeysPosition);
			}
			if ( (i+1)<primaryKeysPositionList.size() ) {
				pkColumnsStringBuffer.append(",");
			}
		}
		return pkColumnsStringBuffer;
	}
	
	public static String concatenatePKValues(List<Integer> primaryKeysPositionList,ResultSet resultSet) throws SQLException
	{
		StringBuffer toReturn=new StringBuffer("");
		String separator="##RESSEP##";
		for (int i=0;i<primaryKeysPositionList.size();i++){
			Integer primaryKeysPosition=primaryKeysPositionList.get(i);
			String value=resultSet.getString("C"+primaryKeysPosition);
			if (value==null) {
				value="";
			}
			toReturn.append(value).append(separator);
		}
		return toReturn.toString();
	}
	
	public static StringBuffer getSQLQueryViewerDataRows(int snapshotId,int tableId,StringBuffer whereClauseFilter) 
					throws SQLException, ClassNotFoundException {
		StringBuffer sqlBuffer =new StringBuffer("");
		
		sqlBuffer.append("select * from ").append(DB_TABLES_PREFIX).append(tableId).append(" where SNAPSHOT_ID=").append(snapshotId);
		if (whereClauseFilter!=null) {
			sqlBuffer.append(whereClauseFilter);
		}
		sqlBuffer.append(" ORDER BY SEQ ");

		return sqlBuffer;
	}

	/*
	 * select all the changes meaning:
	 * - all records which have been updated or added (changed PK or new PK) with last_updated_by NOT IN ( seeded user ids).
	 * - we cannot use LAST_UPDATED_ON because some records show in 2007 also what about R11 upgrades where seeded data is before 2005?
	 * select * from XX_TEST.XX_RSC_T5916 where BASELINE_ID=1 AND rsc_last_updated_by NOT IN ( seeded user ids)
	 */
	public static List<Map<Integer,DataRow>> getDataRowsForViewChanges(Connection connection,
			Inventory inventory,String sqlQuery,int startRowNumToFetch,	int endRowNumToFetch) 
					throws Exception {
		List<DataRow> dataRows=getDataRows(connection,inventory,sqlQuery,startRowNumToFetch,endRowNumToFetch);

		List<Map<Integer,DataRow>> toReturn=new ArrayList<Map<Integer,DataRow>>();
		Map<Integer,DataRow> snapshotIdToRecordMap=new HashMap<Integer,DataRow>();
		for (DataRow dataRow:dataRows) {
			int rowSnapshotId=dataRow.getSnapshotId();
			snapshotIdToRecordMap=new HashMap<Integer,DataRow>();
			toReturn.add(snapshotIdToRecordMap);
			snapshotIdToRecordMap.put(rowSnapshotId, dataRow);
		}
		return toReturn;
	}
		
	private static StringBuffer getComparisonPrimaryKeysDistinctValuesSQL(int comparisonId,
			int tableId,List<Integer> primaryKeysPositionList) 
			throws SQLException, ClassNotFoundException {
		StringBuffer sqlBuffer =new StringBuffer("");
		
		StringBuffer primaryKeysBuffer=getPrimaryKeysColumnsStringBuffer(primaryKeysPositionList,false);
		sqlBuffer.append("SELECT DISTINCT ").append(primaryKeysBuffer);
		sqlBuffer.append(" FROM ").append(DB_COMPARISON_TABLES_PREFIX).append(tableId);
		sqlBuffer.append(" WHERE COMPARISON_ID= ").append(comparisonId).append(" order by ").append(primaryKeysBuffer);
		
		return sqlBuffer;
	}
	
	public static int getComparisonDataRowsCount(Connection connection,int comparisonId,
			int tableId,List<Integer> primaryKeysPositionList) 
	throws SQLException, ClassNotFoundException {
		StringBuffer sqlBuffer =getComparisonPrimaryKeysDistinctValuesSQL(comparisonId,tableId,primaryKeysPositionList);
				
		//System.out.println("getComparisonDataRowsCount: \n"+sqlBuffer); 
		return getTotalRowsCount(connection,sqlBuffer.toString());
	}
	
	public static List<List<String>> getComparisonDataRowsDistinctPKValues(Connection connection,int comparisonId,
			int tableId,List<Integer> primaryKeysPositionList,int recordNumToFetch) 
	throws SQLException, ClassNotFoundException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlBuffer =new StringBuffer("");
			StringBuffer sqlQuery=getComparisonPrimaryKeysDistinctValuesSQL(comparisonId,tableId,primaryKeysPositionList);
			if (recordNumToFetch>=1) {
				sqlBuffer.append("SELECT T.* FROM (SELECT T.*, ROWNUM as rnum FROM (");
			}
			sqlBuffer.append(sqlQuery);
			if (recordNumToFetch>=1) {
				sqlBuffer.append(") T) T  WHERE T.rnum BETWEEN ").append(recordNumToFetch).append(" AND ").append(recordNumToFetch);
			}
			
			List<List<String>> toReturn=new ArrayList<List<String>>();
			statement= connection.prepareStatement(sqlBuffer.toString());
			resultSet=statement.executeQuery();
			while (resultSet.next()) {
				List<String> list=new ArrayList<String>();
				toReturn.add(list);
				for (Integer primaryKeysPosition:primaryKeysPositionList) {
					String pkValue=resultSet.getString("C"+primaryKeysPosition);
					if (pkValue==null) {
						pkValue="";
					}
					list.add(pkValue);
				}
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static StringBuffer getSQLDataRowsComparisonChanges(int comparisonId,
			int tableId,List<Integer> primaryKeysPositionList,List<SnapshotGridRecord> snapshotGridRecords)
	throws SQLException, ClassNotFoundException
	{	
			//   select T1.* from XX_TEST.T3319 T1,(select distinct C1,C2 from XX_TEST.C3319 where COMPARISON_ID=3) T2 
			// 	 WHERE BASELINE_ID IN (1,2,3,4) AND T1.C1=T2.C1 AND T1.C2=T2.C2  ORDER BY T1.C1,T1.C2,BASELINE_ID
			 
			StringBuffer sqlBuffer =new StringBuffer("");
			sqlBuffer.append("select T1.* from ").append(DB_TABLES_PREFIX).append(tableId).append(" T1,");
			sqlBuffer.append(" (SELECT DISTINCT ").append(getPrimaryKeysColumnsStringBuffer(primaryKeysPositionList,false));
			sqlBuffer.append(" from ").append(DB_COMPARISON_TABLES_PREFIX).append(tableId).append(" T2 ");
			sqlBuffer.append(" where COMPARISON_ID= ").append(comparisonId).append(" ) T2 ");
			sqlBuffer.append(" where SNAPSHOT_ID IN ( ");
			for (int i=0;i<snapshotGridRecords.size();i++){
				SnapshotGridRecord snapshotGridRecord=snapshotGridRecords.get(i);
				sqlBuffer.append(snapshotGridRecord.getSnapshotId());
				if ( (i+1)<snapshotGridRecords.size() ) {
					sqlBuffer.append(",");
				}
			}
			sqlBuffer.append(" ) ");
			for (int i=0;i<primaryKeysPositionList.size();i++){
				Integer primaryKeysPosition=primaryKeysPositionList.get(i);
				sqlBuffer.append(" and ( T1.C").append(primaryKeysPosition).append("=").append("T2.C").append(primaryKeysPosition).append(" or ")
				.append(" ( T1.C").append(primaryKeysPosition).append(" is null and ").append("T2.C").append(primaryKeysPosition).append(" is null ) )");
			}
			sqlBuffer.append(" ORDER BY ");
			for (int i=0;i<primaryKeysPositionList.size();i++){
				Integer primaryKeysPosition=primaryKeysPositionList.get(i);
				sqlBuffer.append(" T1.C").append(primaryKeysPosition).append(",");
			}
			sqlBuffer.append(" SNAPSHOT_ID ");
			return sqlBuffer;
	}

	public static List<Map<Integer, DataRow>> getDataRowsForComparisonChanges(Connection connection,int comparisonId,
			int tableId,int totalInventoryDataEntryColumnsCount,List<Integer> primaryKeysPositionList,List<SnapshotGridRecord> snapshotGridRecords,
			int recordNumToFetch)
	throws SQLException, ClassNotFoundException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlQuery = getSQLDataRowsComparisonChanges(comparisonId,
					tableId,primaryKeysPositionList,snapshotGridRecords);
			
			List<List<String>> distinctPKValuesList=getComparisonDataRowsDistinctPKValues(connection,comparisonId,
					tableId,primaryKeysPositionList,recordNumToFetch);
			List<String> preparedStatementValues=new ArrayList<String>();
			StringBuffer sqlBuffer=getComparisonSQLQuery(sqlQuery,primaryKeysPositionList,distinctPKValuesList,preparedStatementValues);
			 			
			//FileUtils.println("getDataRowsForComparisonChanges, sql:\n"+sqlBuffer.toString());
			statement= connection.prepareStatement(sqlBuffer.toString());
			int parameterIndex=1;
			for (String value:preparedStatementValues) {
				//FileUtils.println("getDataRowsForComparisonChanges, parameterIndex:"+parameterIndex+" value:"+value);
				statement.setString(parameterIndex++,value);
			}
			resultSet=statement.executeQuery();

			// Map of forms record with all their baselines versions
			List<Map<Integer,DataRow>> toReturn=new ArrayList<Map<Integer,DataRow>>();

			String previousCompositeKeyValue=null;
			Map<Integer,DataRow> snapshotIdToRecordMap=new HashMap<Integer,DataRow>();
			DataRow dataRow=null;
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			while (resultSet.next()) {
				int snapshotId=resultSet.getInt("SNAPSHOT_ID");

				String compositKeyValue=concatenatePKValues(primaryKeysPositionList,resultSet);
				if (previousCompositeKeyValue==null || !previousCompositeKeyValue.equals(compositKeyValue) ) {
					// this is a new form record
					snapshotIdToRecordMap=new HashMap<Integer,DataRow>();
					toReturn.add(snapshotIdToRecordMap);
					previousCompositeKeyValue=compositKeyValue;
				}
				// new baseline record
				dataRow=getDataRow(resultSet,totalInventoryDataEntryColumnsCount,format);
				snapshotIdToRecordMap.put(snapshotId, dataRow);
			}
			//FileUtils.println("getDataRowsForComparisonChanges, toReturn: "+toReturn.size());
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static MemoryInformation getMemoryInformation(Connection connection,String dbUser) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlBuffer =new StringBuffer("");
			
			/*
			 * http://mtaborsky.blogspot.com/2012/03/slow-monitoring-of-oracle-tablespace.html
			 * 
			 * May need to run this:
			 * 
			 * PURGE DBA_RECYCLEBIN
			 */
			
			sqlBuffer.append("select (DF.TOTALSPACE - FS.FREESPACE) USED,FS.FREESPACE FREE,df.totalspace TOTAL ");
			sqlBuffer.append("From ");
			sqlBuffer.append("(Select Tablespace_Name,Round(Sum(Bytes)/1024/1024/1024 ,2) Totalspace From ");
			sqlBuffer.append("(Select Tablespace_Name,Bytes From Dba_Data_Files Where Tablespace_Name='"+dbUser+"') Group By Tablespace_Name) Df, ");
			sqlBuffer.append("(Select Tablespace_Name,Round(Sum(Bytes)/1024/1024/1024 ,2) Freespace From ");
			sqlBuffer.append("(Select Tablespace_Name,Bytes From Dba_Free_Space Where Tablespace_Name='"+dbUser+"') Group By Tablespace_Name) Fs ");
			sqlBuffer.append("where   df.tablespace_name = fs.tablespace_name");
			
			MemoryInformation memoryInformation=new MemoryInformation();

			//System.out.println("getMemoryInformation: \n"+sqlBuffer.toString());
			statement= connection.prepareStatement(sqlBuffer.toString());
			resultSet=statement.executeQuery();
			if (resultSet.next()) {
				double totalSpaceInGB=resultSet.getDouble("TOTAL");
				double totalUsedSpaceInGB=resultSet.getDouble("USED");
				double totalFreeSpaceInGB=resultSet.getDouble("FREE");
			
				memoryInformation.setTotalSpaceInGB(totalSpaceInGB);
				memoryInformation.setTotalUsedSpaceInGB(totalUsedSpaceInGB);
				memoryInformation.setTotalFreeSpaceInGB(totalFreeSpaceInGB);
			}	
						
			return memoryInformation;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static String enc(String input, String k){
		byte[] crypted = null;
		try{
			SecretKeySpec sk = new SecretKeySpec(k.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, sk);
			crypted = cipher.doFinal(input.getBytes());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return new String(Base64.encodeBase64(crypted));
	}
	
	public static String dec(String input, String k) throws Exception{
		byte[] output = null;
		try{
			SecretKeySpec sk = new SecretKeySpec(k.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, sk);
			output = cipher.doFinal(Base64.decodeBase64(input));
		}
		catch(Exception e){
			e.printStackTrace();
			throw new Exception("Cannot decrypt text");
		}
		return new String(output);
	}
		
	public static List<SchemaInformation> getSnapshotSchemaInformationList(Connection connection)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= 
			"select USERNAME,"+
			"(select COUNT(*) from DBA_OBJECTS where (OBJECT_TYPE = 'TABLE' or OBJECT_TYPE = 'VIEW' or OBJECT_TYPE = 'SYNONYM')"+
			" and upper(OWNER) = upper(username)) objectsCount"+
			" FROM dba_users where upper(username) like '"+SchemaManagementPanel.USER_NAME_PREFIX+"%' ORDER BY username";
					
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			List<SchemaInformation> toReturn=new ArrayList<SchemaInformation>();
			while (resultSet.next()) {
				String userName= resultSet.getString("username");
				int objectsCount= resultSet.getInt("objectsCount");
				SchemaInformation schemaInformation= new SchemaInformation();
				schemaInformation.setSchemaName(userName);
				schemaInformation.setObjectsCount(objectsCount);
				toReturn.add(schemaInformation);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static int getRemainingObjectsCount(Connection connection,String dbUser)
	throws Exception
	{
		return getAppsNecessaryObjectsCount(connection,dbUser,true);
	}
	
	public static int getAppsNecessaryObjectsCount(Connection connection,String dbUser,boolean isRemainingOnly)
			throws Exception
			{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= 
			"SELECT COUNT(*) FROM ("+
			"		SELECT OBJECT_NAME FROM DBA_OBJECTS WHERE (object_type = 'TABLE' OR object_type = 'VIEW' ) AND owner = 'APPS'"+
			"		UNION"+
			"		SELECT SYNONYM_NAME FROM ("+
			"		  SELECT S.*,(sELECT OBJECT_TYPE FROM DBA_OBJECTS T WHERE T.OWNER=S.TABLE_OWNER AND T.OBJECT_NAME=S.TABLE_NAME AND (object_type = "+
			"		  'TABLE' OR object_type = 'VIEW') ) SYN_OBJ_TYPE"+
			"		  FROM ALL_SYNONYMS S  WHERE owner = 'APPS'"+
			"		) "+
			"		WHERE SYN_OBJ_TYPE IS NOT NULL"+
			"		) T ";
			
			if (isRemainingOnly) {
				sqlQuery=sqlQuery+" WHERE OBJECT_NAME NOT IN (SELECT OBJECT_NAME FROM dba_objects WHERE upper(OWNER) = '"+dbUser.toUpperCase()+"')";
			}
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			resultSet.next();
			return resultSet.getInt(1);
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
			}
	
	public static int getDBInventoryColumnsCount(Connection connection,String tableName)
			throws Exception
	{
		PreparedStatement preparedStatement=null;
				ResultSet resultSet = null;
				try {
					String sql ="SELECT count(*) FROM user_tab_columns where table_name = '"+tableName+"' and COLUMN_NAME like 'C%'";
					
					preparedStatement = connection.prepareStatement(sql);
					resultSet=preparedStatement.executeQuery();
					resultSet.next();
					return resultSet.getInt(1);
				}
				finally {
					DirectConnectDao.closeQuietly(resultSet);
					DirectConnectDao.closeQuietly(preparedStatement);
				}
			}
	
	public static void dropSnapshotSchemaAndTablespace(Connection connection,String schemaName) throws Exception {
		if (!schemaName.startsWith(SchemaManagementPanel.USER_NAME_PREFIX)) {
			throw new Exception("You can only delete a Snapshot schema");
		}
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("DROP USER ").append(schemaName).append(" CASCADE");
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}	
		PreparedStatement preparedStatement2 = null;
		try
		{	
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("DROP TABLESPACE ").append(schemaName).append(" INCLUDING CONTENTS AND DATAFILES");
			preparedStatement2 = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement2.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement2);
		}
	}
	
	public static LicenseDocument getPKDocument(File file) throws Exception{
		FileInputStream fis = new FileInputStream(file);
		final byte[] eBytes = IOUtils.toByteArray(fis);
		final String licText = new String(eBytes);
		final String decText =dec(licText,EK);
		LicenseDocument licenseDocument = LicenseDocument.Factory.parse(decText);
		return licenseDocument;
	}
	
	public static LicenseDocument getPKDocument() throws Exception{
		return getPKDocument(new File(RegistrationPanel.LIC_FILE_NAME));
	}

	public static String getEncLicInformation(){
		try{
			LicenseDocument licenseDocument=generateTemporaryLic();
			String content=StringEscapeUtils.unescapeXml(licenseDocument.toString());
			String encText= com.rapidesuite.snapshot.model.ModelUtils.enc(content,EK);
			return encText;
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return "";
		}
	}
	
	public static LicenseDocument generateTemporaryLic()
	{
		try
		{
			LicenseDocument licenseDocument = LicenseDocument.Factory.newInstance();
			License license = licenseDocument.addNewLicense();
			InetAddress ip=InetAddress.getLocalHost();
			String hostname = ip.getHostName();
			license.addHostname(hostname);
			String macAddress=getMacAddress();
			license.setMacAddress(macAddress);
			String osUserName=System.getProperty("user.name");
			license.setOsUserName(osUserName);
			license.setCompanyName("PLEASE SPECIFY COMPANY NAME");
			license.setValidateHardware("true");
			
			license.setClientMajorVersion(FileUtils.getMajorApplicationVersion(ModelUtils.class.getClassLoader(),UtilsConstants.APPLICATION_VERSION_FILE_NAME));
			
			boolean hasXULFile=new File("xul"+File.separator+"rsc.js").exists();
			boolean isUnixOS=FileUtils.isUnix();
			boolean isSnapshotOnly= !(isUnixOS || hasXULFile);
			if (isSnapshotOnly) {
				license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString().toUpperCase());
			}
			else {
				license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString().toUpperCase());
				license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.build.toString().toUpperCase());
				license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.inject.toString().toUpperCase());
				license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.reverse.toString().toUpperCase());
				license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.extract.toString().toUpperCase());
			}
			license.setExpirationDate(LicenseEncryptor.formatExpirationDate(new Date(System.currentTimeMillis() + -1 * 24 * 60 * 60 * 1000)));
			String currentProcCpuInfo = getContentsOfProcCpuInfoIfPresent();
			if ( null != currentProcCpuInfo )
			{
				license.setProcCpuinfoContents(currentProcCpuInfo);
			}

			String osProdId=getOSProdId();
			if (osProdId==null) {
				osProdId="";
			}
			license.setOsProductId(osProdId);
			
			return licenseDocument;
		}
		catch(Throwable t)
		{
			FileUtils.printStackTrace(t);
			return null;
		}
	}
	
	public static String getContentsOfProcCpuInfoIfPresent() throws Exception
	{
		StringBuffer toReturn=new StringBuffer("");
		
		if (FileUtils.isWindows()) {
			toReturn.append("#PROCESSOR_IDENTIFIER=").append(System.getenv("PROCESSOR_IDENTIFIER"));
			toReturn.append("#PROCESSOR_ARCHITECTURE=").append(System.getenv("PROCESSOR_ARCHITECTURE"));
			String val=System.getenv("PROCESSOR_ARCHITEW6432");
			if (val==null) {
				val="";
			}
			toReturn.append("#NUMBER_OF_PROCESSORS=").append(System.getenv("NUMBER_OF_PROCESSORS"));
			toReturn.append("#PROCESSOR_ARCHITEW6432=").append(val);
		}
		else
		if (FileUtils.isUnix()) {
			toReturn.append(LicenseEncryptor.getContentsOfProcCpuInfoIfPresent());
		}
		else 
		if (FileUtils.isMac()) {
			toReturn.append(getContentsOfProcCpuInfoOSX(false));	
		}
		else {
			String OS = System.getProperty("os.name").toLowerCase();
			throw new Exception("Invalid OS - Windows and Linux are only supported. Detected: '"+OS+"'");
		}
		
		return toReturn.toString();
	}

	public static ProductKeyInformation getProductKeyInformation(String productName) {
		ProductKeyInformation productKeyInformation=new ProductKeyInformation();
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		try{
			boolean isSystemDateManuallyUpdated=ModelUtils.isSystemDateManuallyUpdated();
			if (isSystemDateManuallyUpdated) {
				productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
				productKeyInformation.setErrorMessage("Product Key has expired! (Error code: 001)");
				return productKeyInformation;
			}
			LicenseDocument licenseDocument=getPKDocument();
			
			String activationDateStr=licenseDocument.getLicense().getActivationDate();
			if (activationDateStr==null || activationDateStr.isEmpty()) {
				productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
				productKeyInformation.setErrorMessage("Invalid Product Key! (Error code: 002)");
				return productKeyInformation;
			}
			
			String osProductId=licenseDocument.getLicense().getOsProductId();
			if (osProductId!=null && !osProductId.isEmpty()) {
				String osProdId=getOSProdId();
				if (!osProdId.equals(osProductId)) {
					productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
					productKeyInformation.setErrorMessage("Invalid Product Key! (Error code: 003)");
					return productKeyInformation;
				}
			}
			
			String[] products=licenseDocument.getLicense().getProductArray();
			if (products==null) {
				productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
				productKeyInformation.setErrorMessage("Invalid Product Key! (Error code: 600)");
				return productKeyInformation;
			}
			boolean isLicensedProduct=false;
			for (String product:products) {
				if (product.equalsIgnoreCase(productName)) {
					isLicensedProduct=true;
					break;
				}
			}
			if (!isLicensedProduct) {
				productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
				productKeyInformation.setErrorMessage("Invalid Product Key! (Error code: 601)");
				return productKeyInformation;
			}
						
			// For RapidSnapshot, we need to check if the product key was generated for the standalone package
			// or for the RapidClient Linux appliance, as we don't want the cheap RapidSnapshot license to be used to activate the Appliance if the
			// customer did not pay for it.
			// RS Standalone is not for Linux or BUILD should not be installed (check one file exists)
			boolean isProductKeyForRapidSnapshotOnly=true;
			for (String product:products) {
				if (product.equalsIgnoreCase(CoreConstants.SHORT_APPLICATION_NAME.build.toString()) ||
					product.equalsIgnoreCase(CoreConstants.SHORT_APPLICATION_NAME.reverse.toString()) ||
					product.equalsIgnoreCase(CoreConstants.SHORT_APPLICATION_NAME.inject.toString()) ||
					product.equalsIgnoreCase(CoreConstants.SHORT_APPLICATION_NAME.extract.toString())
						) {
					isProductKeyForRapidSnapshotOnly=false;
					break;
				}
			}
			if (isProductKeyForRapidSnapshotOnly) {
				boolean isApplianceInstallation=isApplianceInstallation();
				if (isApplianceInstallation) {
					productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
					productKeyInformation.setErrorMessage("Invalid Product Key! (Error code: 615)");
					return productKeyInformation;
				}
			}

			Date now = new Date();
			String formattedTodayDate = sdfDate.format(now);
			Date activationDate = LicenseEncryptor.parseExpirationDate(activationDateStr);
			final long LICENSE_WARNING_INTERVAL_MS = 7 * 24 * 60 * 60 * 1000;
			Date expirationDate = LicenseEncryptor.parseExpirationDate(licenseDocument.getLicense().getExpirationDate());
			String formattedExpirationDate = sdfDate.format(expirationDate);
			String formattedActivationDate = sdfDate.format(activationDate);
			
			/*
			 Nasty bug with GMT delays if generated from the USA - this condition will be true
			 but actually is valid!!!
			if ( System.currentTimeMillis() < activationDate.getTime() )
			{
				productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
				productKeyInformation.setErrorMessage("Product Key has expired!" +
						"<br/>Current date: " + formattedTodayDate +
						"<br/>Activation date: " + formattedActivationDate+
						"<br/>Expiration date: " + formattedExpirationDate);
				return productKeyInformation;
			}
			*/
			
			
			validateLicenseOnLicensedMachine(licenseDocument,productName);
			/*
			// why check RAPIDSnapshot only?
			String product=CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString().toUpperCase();
			List<String> licensedProductNames = new ArrayList<String>(Arrays.asList(licenseDocument.getLicense().getProductArray()));
			if ( !licensedProductNames.contains(product) ){
				productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
				productKeyInformation.setErrorMessage("Invalid Product Key! (Error code: 004)");
				return productKeyInformation;
			}
			*/

			if ( null != licenseDocument.getLicense().getExpirationDate() && !"".equals(licenseDocument.getLicense().getExpirationDate()) )
			{
				if ( System.currentTimeMillis() > expirationDate.getTime() )
				{
					productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
					productKeyInformation.setErrorMessage("Product Key has expired!" +
							"<br/>Current date: " + formattedTodayDate +
							"<br/>Activation date: " + formattedActivationDate+
							"<br/>Expiration date: " + formattedExpirationDate);
					return productKeyInformation;
				}
			}
			productKeyInformation.setStatus(RegistrationPanel.VALID_PRODUCT_KEY_STATUS);
			productKeyInformation.setValidMessage("Activated Product Key!<br/>Expiration Date set to "+formattedExpirationDate);
			if (System.currentTimeMillis() + LICENSE_WARNING_INTERVAL_MS > expirationDate.getTime() )
			{
				productKeyInformation.setWarningMessage("Product Key expiration is imminent!" +
						"<br/>Current date    = " +formattedTodayDate +
						"<br/>Expiration date = " + formattedExpirationDate);
			}
			return productKeyInformation;
		}
		catch(Throwable e) {
			//FileUtils.printStackTrace(e);
			productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
			productKeyInformation.setErrorMessage("Invalid Product Key! "+e.getMessage());
			return productKeyInformation;
		}		
	}
	
	public static boolean isApplianceInstallation() {
		boolean hasXULFile=new File("xul"+File.separator+"rsc.js").exists();
		boolean hasFFAddonFile=new File("ff-addon"+File.separator+"rsc.js").exists();
		boolean isUnixOS=FileUtils.isUnix();
		if (isUnixOS || hasXULFile || hasFFAddonFile) {
			return true;
		}
		return false;
	}
	
	
	public static void validateLicenseOnLicensedMachine(LicenseDocument licenseDocument ,String productName)
	throws Exception
	{
		try
		{
			StringBuffer toReturn=new StringBuffer("");
			// if the "validate_hardware" parameter is set to false, DO NOT VALIDATE
			if(StringUtils.isNotBlank(licenseDocument.getLicense().getValidateHardware()) && !licenseDocument.getLicense().getValidateHardware().equalsIgnoreCase("false")) {
				// default is true, so if the string is NULL, EMPTY or WHITESPACE ONLY this code will get executed.
				String licensedProcCpuInfo = licenseDocument.getLicense().getProcCpuinfoContents();
				if ( licensedProcCpuInfo != null )
				{
					String currentProcCpuInfo = getContentsOfProcCpuInfoIfPresent();
					if (currentProcCpuInfo==null) {
						toReturn.append("(Error code: 005)");
					}
					else
					if ( !currentProcCpuInfo.equals(licensedProcCpuInfo) )
					{
						toReturn.append("(Error code: 006)");
					}
				}
			}
	        
			String licensedClientMajorVersion = null;
			try {
				licensedClientMajorVersion = licenseDocument.getLicense().getClientMajorVersion();
			} catch (Throwable t) {
				FileUtils.println("licensedClientMajorVersion not found, suppressing the warning.");
			}
			
		if(productName.equals(CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString()) && !isApplianceInstallation()){
			//skip major version validation
		}
		else{
	        if(!StringUtils.isBlank(licensedClientMajorVersion)) {
	        	String clientMajorVersion = FileUtils.getMajorApplicationVersion(ModelUtils.class.getClassLoader(),UtilsConstants.APPLICATION_VERSION_FILE_NAME);
		        if(!StringUtils.isBlank(licenseDocument.getLicense().getClientMajorVersion()) && !licenseDocument.getLicense().getClientMajorVersion().equals(clientMajorVersion)) {
		        	toReturn.append(String.format(
		        			"the server does not host the registered major version of the application.<br/>Current Major Version = '%s', Registered Major Version = '%s' (Error code: 010).", 
		        			clientMajorVersion, licenseDocument.getLicense().getClientMajorVersion()));
		        }
	        }
	        
		}
			// Disable licensed hostname and port validation.
	        // 0008101: Disable Hardware validation for RAPIDConfigurator license
//			if( productName.equalsIgnoreCase(SHORT_APPLICATION_NAME.snapshot.toString()) && !isApplianceInstallation() && Platform.isMac() ) {
//				// no need to validate hostName
//			} else {
//				List<String> licensedHostNames = new ArrayList<String>(Arrays.asList(licenseDocument.getLicense().getHostnameArray()));
//				InetAddress addr = InetAddress.getLocalHost();
//				String hostName = addr.getHostName();
//				if (hostName==null) {
//					toReturn.append("(Error code: 007)");
//				}
//				if ( !licensedHostNames.contains(hostName) )
//				{
//					toReturn.append("(Error code: 008)");
//				}
//			}

			if (!toReturn.toString().isEmpty()) {
				throw new Exception(toReturn.toString());
			}
		}
		catch(Throwable t)
		{
			String msg=CoreUtil.getAllThrowableMessages(t);
			//FileUtils.println(msg);
			throw new Exception(msg);
		}
		finally{
			/*
			FileUtils.println("License validation information: \n" + 
					"\n\n=============CURRENT LICENSE==================\n\n" +
					licenseDocument.toString() +
					"\n\n=============NEW LICENSE TEMPLATE===============\n\n" +
					ModelUtils.generateTemporaryLic().toString() +
					"\n\n=============================\n\n");
					*/
		}
	}
	
	public static boolean isSystemDateManuallyUpdated() throws Exception {
		Preferences pref = Preferences.userRoot();

		String encValue=pref.get(SYSTEM_DATE,"");	
		if (encValue!=null && !encValue.isEmpty()) {
			final String decValue =dec(encValue,EK);
			long storedSystemDateInMs=Long.valueOf(decValue);
			Date storedDate= new Date(storedSystemDateInMs);
			//System.out.println("stored date: "+storedDate);
			
			long currentDateInMs=System.currentTimeMillis();
			Date currentDate = new Date(currentDateInMs);
			//System.out.println("current Date:"+currentDate);
			
			if (storedSystemDateInMs > currentDateInMs) {
				FileUtils.println("SD: '"+storedDate+"' CD: '"+currentDate+"'");
				return true;
			}
		}
		return false;
	}
	
	public static void saveSystemDateThread() {
		new Thread( new Runnable() {
		    @Override
		    public void run() {
		    	saveSystemDate();
		    	try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		}).start();
	}
	
	private  static void saveSystemDate() {
		Preferences pref = Preferences.userRoot();
		long currentDateInMs=System.currentTimeMillis();
		final String encCurrentDate =enc(""+currentDateInMs,EK);
		pref.put(SYSTEM_DATE,encCurrentDate);
	}
	
	public static void resetSystemDate() {
		Preferences pref = Preferences.userRoot();
		pref.remove(SYSTEM_DATE);
	}

	public static void deleteSnapshotSoft(Map<String, String> snapshotEnvironmentProperties,List<Integer> snapshotIdList) throws ClassNotFoundException, SQLException {
		Connection connection=null;
		try {			
			connection=DatabaseUtils.getJDBCConnection(
						ModelUtils.getJDBCString(snapshotEnvironmentProperties),
						ModelUtils.getDBUserName(snapshotEnvironmentProperties),
						ModelUtils.getDBPassword(snapshotEnvironmentProperties));
			for (int snapshotId:snapshotIdList) {
				deleteSnapshotSoft(connection,snapshotId);
			}
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}			
	}
		
	private static void deleteSnapshotSoft(Connection connection,int snapshotId) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("UPDATE SNAPSHOT set IS_DELETED='Y' WHERE ID=").append(snapshotId);
						
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static void cancelSnapshotSoft(Map<String, String> snapshotEnvironmentProperties,List<Integer> snapshotIdList) throws ClassNotFoundException, SQLException {
		Connection connection=null;
		try {			
			connection=DatabaseUtils.getJDBCConnection(
						ModelUtils.getJDBCString(snapshotEnvironmentProperties),
						ModelUtils.getDBUserName(snapshotEnvironmentProperties),
						ModelUtils.getDBPassword(snapshotEnvironmentProperties));
			for (int snapshotId:snapshotIdList) {
				cancelSnapshotSoft(connection,snapshotId);
			}
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}			
	}
	
	private static void cancelSnapshotSoft(Connection connection,int snapshotId) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{		
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("UPDATE SNAPSHOT set STATUS='").append(DB_STATUS_CANCELLED).append("' WHERE ID=").append(snapshotId);
						
			preparedStatement = connection.prepareStatement(sqlBuffer.toString());	
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static boolean hasPKFile() {
		try {
			getPKDocument();
			return true;
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			return false;
		}
	}

	public static void viewScript(String content) throws Exception {
		File file = File.createTempFile("rapidsnapshot", ".sql"); 
		ModelUtils.writeToFile(file,content,false); 
		if ( Platform.isMac() ) {
			Desktop.getDesktop().edit(file);
		}
		else {
			FileUtils.startTextEditor(Config.getCmdTextEditor(), file);
		}
	}

	public static void cleanupInventory(String dbSchema,Connection connection,String inventoryName) throws ClassNotFoundException, SQLException {
		int tableId=getTableId(connection,inventoryName);
		if (tableId==-1) {
			return;
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("update INVENTORY_TO_SNAPSHOT set status='R',msg=null,execution_time_string=null,execution_raw_time=null where inventory_id=").append(tableId);
			statement= connection.prepareStatement(sqlBuffer.toString());
			statement.executeUpdate();
			
			String comparisonTableName=DB_COMPARISON_TABLES_PREFIX+tableId;
			String dataTableName=DB_TABLES_PREFIX+tableId;

			if (hasComparisonTable(dbSchema,connection,comparisonTableName)){
				ModelUtils.dropTable( dbSchema,connection,comparisonTableName);
			}
			if (hasDataTable(dbSchema,connection,tableId)){
				ModelUtils.dropTable( dbSchema,connection,dataTableName);
			}
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static int getTableId(Connection connection,String inventoryName)
			throws SQLException, ClassNotFoundException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="select id from INVENTORY where name=?";
			
			statement= connection.prepareStatement(sql);
			statement.setString(1, inventoryName);
			resultSet=statement.executeQuery();
			if (resultSet.next()) {
				int id=resultSet.getInt("id");
				return id;
			}
			return -1;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static String getContentsOfProcCpuInfoOSX(boolean isReturnSerialNumberOnly)
	{
		try {
			String line;
			Process p = Runtime.getRuntime().exec(
					new String[]{"system_profiler", "SPHardwareDataType"});

			StringBuffer buff = new StringBuffer();
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader (new InputStreamReader(p.getErrorStream()));
			//System.out.println("STD INPUT...");
			while ((line = bri.readLine()) != null) {
				//System.out.println(line);
				line = line.toUpperCase();
				if ( 
						line.trim().isEmpty() ||
						line.contains("Hardware:".toUpperCase()) ||
						line.contains("Hardware Overview:".toUpperCase())  ||
						line.contains("Memory:".toUpperCase()) ||
						line.contains("Boot ROM Version:".toUpperCase()) ||
						line.contains("SMC Version".toUpperCase()) ||
						line.contains("L2 Cache".toUpperCase())||
						line.contains("L3 Cache".toUpperCase())       
						)
				{
					continue;
				}
				
				if (line.contains("Serial Number".toUpperCase())) {
					if (isReturnSerialNumberOnly) {
						buff.append(line);
						break;
					}
					else {
						continue;
					}
				}
				buff.append(line).append(" ");
			}
			bri.close();
			//System.out.println("ERROR INPUT...");
			while ((line = bre.readLine()) != null) {
				System.out.println("ERROR INPUT:"+ line);
			}
			bre.close();
			p.waitFor();
			//System.out.println("Done.");

			String toReturn = buff.toString().replaceAll("\\s+", " ").toUpperCase().trim();
	        if ( toReturn.length() == 0 )
	        {
	            toReturn = null;
	        }
	        //System.out.println("toReturn:"+toReturn);
	        return toReturn;
		}
		catch (Exception err) {
			err.printStackTrace();
			FileUtils.printStackTrace(err);
			return null;
		}
	}
	
	public static String getOSProdId() throws Exception
	{	
		String val="";
		if (FileUtils.isWindows()) {
			return readRegistry("HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion","ProductId");
		}
		else
		if (FileUtils.isUnix()) {
			return "";
		}
		else 
		if (FileUtils.isMac()) {
			return getContentsOfProcCpuInfoOSX(true);
		}
		return val;
	}

	
	public static final String readRegistry(String location, String key)
	{
		try
		{
			// Run reg query, then read output with StreamReader (internal class)
			Process process = Runtime.getRuntime().exec("reg query " + 
					'"'+ location + "\" /v " + key);

			InputStream is = process.getInputStream();
			StringBuilder sw = new StringBuilder();

			try
			{
				int c;
				while ((c = is.read()) != -1)
					sw.append((char)c);
			}
			catch (IOException e)
			{ 
			}

			String output = sw.toString();

			// Output has the following format:
				// \n<Version information>\n\n<key>    <registry type>    <value>\r\n\r\n
			int i = output.indexOf("REG_SZ");
			if (i == -1)
			{
				return null;
			}

			sw = new StringBuilder();
			i += 6; // skip REG_SZ

			// skip spaces or tabs
			for (;;)
			{
				if (i > output.length())
					break;
				char c = output.charAt(i);
				if (c != ' ' && c != '\t')
					break;
				++i;
			}

			// take everything until end of line
			for (;;)
			{
				if (i > output.length())
					break;
				char c = output.charAt(i);
				if (c == '\r' || c == '\n')
					break;
				sw.append(c);
				++i;
			}

			return sw.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getMacAddress() throws UnknownHostException, SocketException {
		InetAddress ip = InetAddress.getLocalHost();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		byte[] mac = network.getHardwareAddress();
		if (mac==null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
		}
		return sb.toString();
	}
	
	public static boolean hasRSCPrerequisiteObjectsKeyword(String content)
	{
		return content.indexOf(RSC_PREREQUISITE_OBJECTS_KEYWORD)!=-1;
	}
	
	public static boolean hasPrerequisiteObjects(Connection connection,String content)
	throws Exception
	{
		String textAfterKeyword=DataExtractionUtils.getTextAfterKeyword(content,RSC_PREREQUISITE_OBJECTS_KEYWORD);
		List<String> preRequisiteTokens = CoreUtil.splitCommaDelimitedStrings(textAfterKeyword);
		for (String viewOrTableName:preRequisiteTokens) {
			boolean isExist=hasAppsViewOrTableObject(connection,viewOrTableName);
			if (!isExist) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean hasAppsViewOrTableObject(Connection connection,String viewOrTableName)
	throws Exception
	{
		try{
			String statement="select count(*) from APPS."+viewOrTableName;
			executeStatement(connection,statement);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public static Map<String,OperatingUnit> getOperatingUnitsSearchResults(Connection connection,String inputValue)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {

			String sqlQuery=			
					"select h.organization_id, h.name,(select distinct CHART_OF_ACCOUNTS_ID from apps.Org_Organization_Definitions t "+
					"where t.operating_unit =h.organization_id ) CHART_OF_ACCOUNTS_ID from apps.hr_operating_units h "+
					" where upper(h.name) like upper(?) order by name asc";
			preparedStatement = connection.prepareStatement(sqlQuery);
			preparedStatement.setString(1, inputValue);
			
			resultSet=preparedStatement.executeQuery();
			Map<String,OperatingUnit> toReturn=new TreeMap<String,OperatingUnit>();
			while (resultSet.next()) {
				long id= resultSet.getLong("organization_id");
				String name= resultSet.getString("name");
				int coaId= resultSet.getInt("CHART_OF_ACCOUNTS_ID");
				OperatingUnit operatingUnit=new OperatingUnit(name,id,coaId);
				toReturn.put(name,operatingUnit);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static int getOracleUsersCountSearchResults(Connection connection, String inputValue)
			throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {

			String sqlQuery=			
					"select count(*) cnt from apps.fnd_user"+
					" where upper(user_name) like upper(?) order by user_name asc";
						
			preparedStatement = connection.prepareStatement(sqlQuery);
			preparedStatement.setString(1, inputValue);
			resultSet=preparedStatement.executeQuery();
			resultSet.next();
			return resultSet.getInt("cnt");
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static Map<String, OracleUser> getOracleUsersSearchResults(Connection connection, String inputValue,int maxLimit)
			throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {

			String sqlQuery=			
					"select user_name,user_id from apps.fnd_user"+
					" where upper(user_name) like upper(?) order by user_name asc";
						
			preparedStatement = connection.prepareStatement(sqlQuery);
			preparedStatement.setString(1, inputValue);
			
			resultSet=preparedStatement.executeQuery();
			Map<String,OracleUser> toReturn=new TreeMap<String,OracleUser>();
			int counter=0;
			while (resultSet.next()) {
				long id= resultSet.getLong("user_id");
				String name= resultSet.getString("user_name");
				OracleUser oracleUser=new OracleUser(name,id);
				toReturn.put(name,oracleUser);
				counter++;
				if (counter==maxLimit) {
					break;
				}
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	public static void createRegistrationWindow(JFrame rootFrame,String productName,boolean isViewScriptButton,boolean isManuallyOpened,
			String registrationURL,String iconPath){
		if("snapshot".equals(productName)){
			createRegistrationWindowForSnapshot(rootFrame,productName,isViewScriptButton,isManuallyOpened,registrationURL,iconPath);
		}else{
			createRegistrationWindowGerenic(rootFrame,productName,isViewScriptButton,isManuallyOpened,registrationURL,iconPath);
		}	
	}
	public static void createRegistrationWindowForSnapshot(JFrame rootFrame,String productName,boolean isViewScriptButton,boolean isManuallyOpened,
			String registrationURL,String iconPath){
		int width=-1;
		int height=-1;
		RegistrationPanel registrationPanel=null;
		boolean isApplianceInstallation=ModelUtils.isApplianceInstallation();
		if (isApplianceInstallation) {
			width=920;
			height=510;
			registrationPanel=new RegistrationAppliancePanel(productName,rootFrame);
		}
		else {
			width=920;
			height=510;
			registrationPanel=new RegistrationAppliancePanel(productName,rootFrame);
		}
		
		JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindowSnap(rootFrame,"Registration",width,height,
				registrationPanel,null,false,iconPath);
		registrationPanel.setCustomWindowClosingListener(dialog);
		
		ProductKeyInformation productKeyInformation=null;
		boolean isShowWindow=true;
		boolean hasPKFile=ModelUtils.hasPKFile();
		if (!hasPKFile) {
			isShowWindow=false;
		}
		else {
			Set<String> activatedPlugins=ModelUtils.getActivatedPlugins();
			boolean isUpgrade = false;
			if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
				isUpgrade = true;
			}else{
				isShowWindow=false;
			}
			if(isUpgrade){
				productKeyInformation=ModelUtils.getProductKeyInformation(productName);
				if (productKeyInformation.getStatus().equalsIgnoreCase(RegistrationPanel.VALID_PRODUCT_KEY_STATUS)) {
					String warningMessage=productKeyInformation.getWarningMessage();
					if (warningMessage!=null) {
						isShowWindow=true;
					}
					else {
						isShowWindow=false;
					}
				}				
			}
		}
		ModelUtils.saveSystemDateThread();
		if(isManuallyOpened && hasPKFile){
			productKeyInformation=ModelUtils.getProductKeyInformation(productName);
			
		}else if(isManuallyOpened && !hasPKFile){
			productKeyInformation=new ProductKeyInformation();
			productKeyInformation.setStatus(RegistrationPanel.WARNING_PRODUCT_KEY_STATUS);
			productKeyInformation.setWarningMessage("Missing Product Key for RAPIDUpgrade!");			
		}
		if (isShowWindow || isManuallyOpened) {
			registrationPanel.showPopup(productKeyInformation);
		}		
	}
	public static void createRegistrationWindowGerenic(JFrame rootFrame,String productName,boolean isViewScriptButton,boolean isManuallyOpened,
			String registrationURL,String iconPath) {	
		int width=-1;
		int height=-1;
		
		RegistrationPanel registrationPanel=null;
		boolean isApplianceInstallation=ModelUtils.isApplianceInstallation();
		if (isApplianceInstallation) {
			width=920;
			height=510;
			registrationPanel=new RegistrationAppliancePanel(productName,rootFrame);
		}
		else {
			width=920;
			height=760;
			registrationPanel=new RegistrationShopPanel(productName,isViewScriptButton,registrationURL);
		}
		
		JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindowSnap(rootFrame,"Registration",width,height,
				registrationPanel,null,false,iconPath);
		registrationPanel.setCustomWindowClosingListener(dialog);
		
		ProductKeyInformation productKeyInformation=null;
		boolean isShowWindow=true;
		boolean hasPKFile=ModelUtils.hasPKFile();
		if (!hasPKFile) {
			productKeyInformation=new ProductKeyInformation();
			productKeyInformation.setStatus(RegistrationPanel.ERROR_PRODUCT_KEY_STATUS);
			productKeyInformation.setErrorMessage("Missing Product Key!");
		}
		else {
			productKeyInformation=ModelUtils.getProductKeyInformation(productName);
			if (productKeyInformation.getStatus().equalsIgnoreCase(RegistrationPanel.VALID_PRODUCT_KEY_STATUS)) {
				String warningMessage=productKeyInformation.getWarningMessage();
				if (warningMessage!=null) {
					isShowWindow=true;
				}
				else {
					isShowWindow=false;
				}
			}
		}
		ModelUtils.saveSystemDateThread();
		if (isShowWindow || isManuallyOpened) {
			registrationPanel.showPopup(productKeyInformation);
		}
	}

	public static void generateXMLFileInventoryNameToFunctionIds(File inventoryFolder,File outputFile) throws Exception {
		System.out.println("generateXMLFileInventoryNameToFunctionIds, START...");
		Map<String, File> inventoryNameToInventoryFileMap=ModelUtils.getFileNameToFileMap(inventoryFolder,true);
		Iterator<String> iterator=inventoryNameToInventoryFileMap.keySet().iterator();

		InventoriesDocument inventoriesDocument=InventoriesDocument.Factory.newInstance();
		Inventories inventories=inventoriesDocument.addNewInventories();

		System.out.println("INVENTORIES SIZE: "+inventoryNameToInventoryFileMap.size());
		int counter=0;
		while (iterator.hasNext()) {
			counter++;
			if (counter % 500 ==0) {
				System.out.println(counter+" / "+inventoryNameToInventoryFileMap.size());
			}
			String inventoryName=iterator.next();
			File inventoryFile=inventoryNameToInventoryFileMap.get(inventoryName);
			Inventory inventory=FileUtils.getInventory(inventoryFile,inventoryName);

			String functionIds=inventory.getFunctionIds();
			String[] functionIdsArray=functionIds.split(",");
			for (String functionId:functionIdsArray) {
				FormInfo formInfo=inventories.addNewFormInfo();
				formInfo.setFunctionId(BigInteger.valueOf(Integer.valueOf(functionId)));
				formInfo.setInventoryName(inventoryName);
			}
		}
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		inventoriesDocument.save(outputFile,xmlOptions);
		System.out.println("generateXMLInventorytoFormNameMappings, COMPLETED!");
	}
	
	public static Map<String, List<String>> getInventoryNameToFunctionIdsMap(TabSnapshotsPanel tabSnapshotsPanel, String connectionOracleRelease) 
			throws Exception {
		File inventoriesFormInfoFile=tabSnapshotsPanel.getSnapshotPackageSelectionPanel().getInventoriesFormInfoFile(
				connectionOracleRelease);
		InventoriesDocument inventoriesDocument=ModelUtils.getInventoriesDocument(inventoriesFormInfoFile);
		Inventories inventories=inventoriesDocument.getInventories();
		Map<String, List<String>> toReturn=new  HashMap<String, List<String>>();
		FormInfo[] formInfoArray=inventories.getFormInfoArray();
		for (FormInfo formInfo:formInfoArray) {
			List<String> list=toReturn.get(formInfo.getInventoryName());
			if (list==null) {
				list=new ArrayList<String>();
				toReturn.put(formInfo.getInventoryName(),list);
			}
			list.add(formInfo.getFunctionId().toString());
		}
		return toReturn;
	}

	public static void saveDownloadGridToExcel(File outputFile,List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList) throws Exception {
		List<String[]> rows=new ArrayList<String[]>();
		int counter=0;
		int totalColumnsToDisplay=8;
		
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
			counter++;
			int index=0;
			String[] row=new String[totalColumnsToDisplay];
			
			row[index++]=""+counter;
			row[index++]=snapshotInventoryGridRecord.getInventoryName();
			String downloadStatus=snapshotInventoryGridRecord.getDownloadStatus();
			if (downloadStatus==null) {
				downloadStatus="";
			}
			row[index++]=downloadStatus;
			int downloadDownloadedRecordsCount=snapshotInventoryGridRecord.getDownloadDownloadedRecordsCount();
			row[index++]=""+downloadDownloadedRecordsCount;
			int downloadTotalRecordsCount=snapshotInventoryGridRecord.getDownloadTotalRecordsCount();
			row[index++]=""+downloadTotalRecordsCount;
			String downloadRemarks=snapshotInventoryGridRecord.getDownloadRemarks();
			if (downloadRemarks==null) {
				downloadRemarks="";
			}
			row[index++]=downloadRemarks;
			String downloadTime=snapshotInventoryGridRecord.getDownloadTime();
			if (downloadTime==null) {
				downloadTime="";
			}
			row[index++]=downloadTime;

			row[index++]=""+snapshotInventoryGridRecord.getDownloadRawTimeInSecs();

			rows.add(row);
		}
		String[] headerRow=new String[totalColumnsToDisplay];
		int index=0;
		String NEW_LINE="\n";
				
		headerRow[index++]=SnapshotDownloadGridPanel.COLUMN_HEADING_ROW_NUM.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotDownloadGridPanel.COLUMN_HEADING_INVENTORY_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotDownloadGridPanel.COLUMN_HEADING_STATUS.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotDownloadGridPanel.COLUMN_HEADING_TOTAL_DOWNLOADED_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotDownloadGridPanel.COLUMN_HEADING_TOTAL_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotDownloadGridPanel.COLUMN_HEADING_REMARKS.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotDownloadGridPanel.COLUMN_HEADING_DOWNLOAD_TIME.replaceAll(NEW_LINE," ");
		headerRow[index++]=SnapshotDownloadGridPanel.COLUMN_HEADING_RAW_TIME.replaceAll(NEW_LINE," ");
		rows.add(0,headerRow);

		String sheetTitle="Status";
		doCreateAndSaveXLSXExcelFile(sheetTitle,rows,outputFile, false, false);
	}

	public static void validateDuplicateData(String dbUserName,Inventory inventory,
			Connection connection,String snapshotWhereClause, int tableId, List<Integer> primaryKeysPositionList) throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlBuffer =new StringBuffer("");
			
			StringBuffer pkColumnsBuffer =new StringBuffer("");
			int counter=0;
			for (Integer primaryKeysPosition:primaryKeysPositionList) {
				pkColumnsBuffer.append("C"+primaryKeysPosition);
				if ( (counter+1) < primaryKeysPositionList.size() ) {
					pkColumnsBuffer.append(",");
				}
				counter++;
			}
			
			sqlBuffer.append("SELECT SNAPSHOT_ID,SUM(cnt) TOT FROM (\n").
			append("SELECT SNAPSHOT_ID,").append(pkColumnsBuffer.toString()).append(",count(*) cnt FROM ").append(dbUserName).append(".").
			append(DB_TABLES_PREFIX).append(tableId).
			append(" where snapshot_id in (").append(snapshotWhereClause).append(")").
			append(" GROUP BY SNAPSHOT_ID,").append(pkColumnsBuffer.toString()).append(" HAVING COUNT(*) > 1").
			append("\n) Group By SNAPSHOT_ID,cnt");
			
			statement= connection.prepareStatement(sqlBuffer.toString());
			resultSet=statement.executeQuery();
			if (resultSet.next()) {
				int cnt=resultSet.getInt("TOT");
				
				FileUtils.println("!!!!!!!!!!!!!!!! Duplicate data found !!!!!!!!!!!!!!!!!!!!!!!!");
				FileUtils.println("- Inventory: '"+inventory.getName()+"'");
				List<String> pkList=new ArrayList<String>();
				for (Field field:inventory.getFieldsUsedForPrimaryKey()) {
					pkList.add(field.getName());
				}
				FileUtils.println("- Primary keys: "+pkList);
				FileUtils.println("- SQL:\n"+sqlBuffer);		
				FileUtils.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						
				throw new SnapshotWarningException("Duplicate data found: "+cnt+" records! Please recheck the Primary Keys of the Inventory or the SQL query.");
			}
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static void validateKBVersion(SnapshotMain snapshotMain,Connection connection,String userName,String oracleRelease,String connectionName) throws Exception {
		// If the table does not exist then create it and init with 2.0.2.0 for existing KB releases 12.1.3 and 12.2.4
		String tableName=DB_KB_VERSION_TABLE;
		if (!hasTable(userName,connection,tableName)) {
			createKBVersionTable(connection);
			String kbVersion="2.0.2.0"; // this was the first released version
			String kbProjectFileName="N/A";
			insertKBVersionTable(connection,kbVersion,kbProjectFileName);
		}
		// If the table is empty init it with the KB version file name and returns valid.
		// else validate that it matches with the version file on disk.
		KBVersionInformation dbKBVersionInformation=getKBVersionInformation(connection);
		String kbVersion=getKBVersion(oracleRelease);
		if (dbKBVersionInformation==null) {
			String kbProjectFileName=getKBProjectFile(oracleRelease).getName();
			insertKBVersionTable(connection,kbVersion,kbProjectFileName);
		}
		else {
			if (!dbKBVersionInformation.getKbVersion().equalsIgnoreCase(kbVersion)) {
				File xmlFile=new File("snapshot-kb-matrix.xml");
				MatrixDocument matrixDocument=getMatrixDocument(xmlFile);
				Snapshot[] snapshotArray=matrixDocument.getMatrix().getSnapshotArray();
				Map<String,List<SnapshotMatrixInformation>> oracleReleaseToSnapshotMatrixInformationMap=new HashMap<String,List<SnapshotMatrixInformation>>();
				for (Snapshot snapshot:snapshotArray) {
					Entry[] entryArray=snapshot.getEntryArray();
					for (Entry entry:entryArray) {
						String kbRelease=entry.getKbRelease();
						String oracleReleaseEntry=entry.getOracleRelease();
						List<SnapshotMatrixInformation> list=oracleReleaseToSnapshotMatrixInformationMap.get(oracleReleaseEntry);
						if (list==null) {
							list=new ArrayList<SnapshotMatrixInformation>();
							oracleReleaseToSnapshotMatrixInformationMap.put(oracleReleaseEntry, list);
						}
						SnapshotMatrixInformation snapshotMatrixInformation=new SnapshotMatrixInformation();
						snapshotMatrixInformation.setKbRelease(kbRelease);
						snapshotMatrixInformation.setSnapshotRelease(snapshot.getRelease());
						list.add(snapshotMatrixInformation);
					}
				}
				
				List<SnapshotMatrixInformation> list=oracleReleaseToSnapshotMatrixInformationMap.get(oracleRelease);
				StringBuffer listOfSupportedReleases=new StringBuffer("");
				boolean hasAtLeastOneSupportedRelease=false;
				if (list!=null) {
					for (SnapshotMatrixInformation snapshotMatrixInformation:list) {
						if (snapshotMatrixInformation.getKbRelease().equalsIgnoreCase(dbKBVersionInformation.getKbVersion())) {
							hasAtLeastOneSupportedRelease=true;
							listOfSupportedReleases.append("&nbsp;&nbsp;&nbsp;&nbsp;<b>-&nbsp;rapidsnapshot-"+snapshotMatrixInformation.getSnapshotRelease()).append("</b><br/>");
						}
					}
				}
				String postMsg="";
				if (hasAtLeastOneSupportedRelease) {
					postMsg="<br/><br/>If you wish to access this Connection's Snapshots, then"+
							" you must install one of the following RapidSnapshot releases:<br/><br/>"+listOfSupportedReleases;
				}
				else {
					postMsg="<br/><br/>If you wish to access this Connection's Snapshots, then"+
							" you must install the latest release of RapidSnapshot.";
				}
				throw new Exception("<html><body>This version of RapidSnapshot (<b>"+
						snapshotMain.getApplicationVersion().replaceAll("\n","")+"</b>) cannot be used with the selected Connection: '"+connectionName+"' ("+oracleRelease+")"
						+"<br/>because the KB version on disk: <b>'"+kbVersion+"'</b> is different from the KB version used in the Connection: <b>'"+
						dbKBVersionInformation.getKbVersion()+"'</b>.<br/>"+
						"Please create a new RapidSnapshot user (schema) and a new Connection then try again."+postMsg+"</body></html>");
			}
		}
	}
	
	public static File getFirst7zFile(File folder) throws Exception {
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith((".7z"))) {
				return file;
			}
		}
		throw new Exception("Error: cannot find the KB project file in folder: '"+folder.getAbsolutePath()+"'");
	}
	
	public static File getKBProjectFile(String oracleRelease) throws Exception {
		File oracleReleaseFolder=SnapshotPackageSelectionPanel.getOracleReleaseFolder(oracleRelease);
		File file=getFirst7zFile(oracleReleaseFolder);
		if (!file.exists()) {
			throw new Exception("Internal error: cannot find the KB project file for Oracle release "+oracleRelease);
		}
		return file;
	}
	
	public static String getKBVersion(String oracleRelease) throws Exception {
		File oracleReleaseFolder=SnapshotPackageSelectionPanel.getOracleReleaseFolder(oracleRelease);
		File file=new File(oracleReleaseFolder,"kbVersion.txt");
		if (!file.exists()) {
			throw new Exception("Internal error: cannot find the KB version file for Oracle release "+oracleRelease);
		}
		return readFile(file);
	}
	
	public static String readFile(File file) throws Exception {
		InputStream is=null;
		try
		{
			is=new FileInputStream(file);
			return IOUtils.toString(is);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}
	
	public static void createKBVersionTable(Connection connection) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{
			String sql = "CREATE TABLE VERSION_KB"+
					"(VERSION_TEXT  VARCHAR(255) NOT NULL,"+
					"KB_FILE_NAME VARCHAR(2000) NOT NULL)"; 
			
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeUpdate(sql);
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static void insertKBVersionTable(Connection connection,String kbVersion,String kbFileName) throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{
			String sql = "INSERT INTO VERSION_KB(VERSION_TEXT,KB_FILE_NAME) VALUES(?,?)"; 
			
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, kbVersion);
			preparedStatement.setString(2, kbFileName);
			
			preparedStatement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static KBVersionInformation getKBVersionInformation(Connection connection) throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			StringBuffer sqlBuffer =new StringBuffer("");
			sqlBuffer.append("SELECT VERSION_TEXT,KB_FILE_NAME FROM VERSION_KB");
		
			statement= connection.prepareStatement(sqlBuffer.toString());
			resultSet=statement.executeQuery();
			if (resultSet.next()) {
				KBVersionInformation kbVersionInformation=new KBVersionInformation();
				String kbVersion=resultSet.getString("VERSION_TEXT");
				String kbFileName=resultSet.getString("KB_FILE_NAME");
				
				kbVersionInformation.setKbVersion(kbVersion);
				kbVersionInformation.setKbFileName(kbFileName);
				return kbVersionInformation;
			}
			return null;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static MatrixDocument validateMatrixDocument(File xmlFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = InjectUtils.getXmlOptions(validationErrors);
		MatrixDocument matrixDocument =MatrixDocument.Factory.parse(xmlFile,xmlOptions);
		InjectUtils.validateXMLBeanDocument(matrixDocument,xmlOptions,validationErrors);
		return matrixDocument;
	}	

	public static MatrixDocument getMatrixDocument(File xmlFile) throws Exception {
		MatrixDocument matrixDocument =validateMatrixDocument(xmlFile);
		return matrixDocument;
	}

	public static List<String> readContentsFromTemplateFile(File file)
	throws Exception
	{
		List<String> toReturn=new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line=null;
		    while ((line = br.readLine()) != null) {
		    	if (line==null || line.trim().isEmpty()) {
		    		continue;
		    	}
		    	toReturn.add(line);
		    }
		}
		return toReturn;
	}

	public static Set<String> getActivatedPlugins(){
		Set<String> toReturn=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

		LicenseDocument licenseDocument=null;
		try {
			licenseDocument = getPKDocument();
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			return toReturn;
		}
		Plugins plugins=licenseDocument.getLicense().getPlugins();
		if (plugins==null) {
			return toReturn;
		}
		Plugin[] pluginArray=plugins.getPluginArray();
		if (pluginArray!=null) {
			for (Plugin plugin:pluginArray) {
				toReturn.add(plugin.getName());
			}
		}
		return toReturn;
	}

	public static DataConversionDocument validateDataConversionDocument(File xmlFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = InjectUtils.getXmlOptions(validationErrors);
		DataConversionDocument dataConversionDocument =DataConversionDocument.Factory.parse(xmlFile,xmlOptions);
		InjectUtils.validateXMLBeanDocument(dataConversionDocument,xmlOptions,validationErrors);
		return dataConversionDocument;
	}	

	public static DataConversionDocument getDataConversionDocument(File xmlFile) throws Exception {
		DataConversionDocument dataConversionDocument =validateDataConversionDocument(xmlFile);
		return dataConversionDocument;
	}
	
	public static DataConversionSharedDocument validateDataConversionSharedDocument(File xmlFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = InjectUtils.getXmlOptions(validationErrors);
		DataConversionSharedDocument dataConversionSharedDocument =DataConversionSharedDocument.Factory.parse(xmlFile,xmlOptions);
		InjectUtils.validateXMLBeanDocument(dataConversionSharedDocument,xmlOptions,validationErrors);
		return dataConversionSharedDocument;
	}	

	public static DataConversionSharedDocument getDataConversionSharedDocument(File xmlFile) throws Exception {
		DataConversionSharedDocument dataConversionSharedDocument =validateDataConversionSharedDocument(xmlFile);
		return dataConversionSharedDocument;
	}
	
	public static void exportToXML(File downloadFolder,Inventory inventory,List<DataRow> dataRows)	throws Exception {
		BufferedWriter out=null;
		try
		{			
			File file =new File(downloadFolder,inventory.getName()+".xml");
			if (file.exists()) {
				file =new File(downloadFolder,inventory.getName()+"."+ConvertController.TARGET_FILES_DOWNLOAD_SEQUENCE_NUMBER+".xml");
				ConvertController.TARGET_FILES_DOWNLOAD_SEQUENCE_NUMBER++;
				//throw new Exception("Converted XML data file already exists for this Inventory");
			}
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF8"),32768);
			String label="CONVERSION";
			
			List<Field> fieldsUsedForDataEntry=inventory.getFieldsUsedForDataEntry();
			writeFileHeader(out,inventory.getName(),fieldsUsedForDataEntry,true);
			
			//System.out.println(new java.util.Date()+" dataRows: "+dataRows.size()+" to export");
			writeDataRowsConversion(out,dataRows,label);
			//System.out.println(new java.util.Date()+" exporting done");
			
			writeDataFileFooter(out);
		}
		finally
		{
			IOUtils.closeQuietly(out);
		}
	}
	
	public static void writeDataFileFooter(BufferedWriter out) throws Exception
	{
		StringBuffer str=new StringBuffer("");
		str.append(DataFactory.DATA_FILE_FOOTER).append("\n");
		out.write(str.toString());
	}
	
	public static void writeFileHeader(
			BufferedWriter out,
			String tableName,
			List<Field> inventoryFields,
			boolean hasAuditColumns) throws IOException
	{
		StringBuffer str=new StringBuffer("");
		str.append("<?xml version=\"1.0\" encoding=\"").append(com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING).append("\"?>\n");
		str.append("<data name=\"").append(tableName).append("\" xmlns=\"http://data0000.configurator.erapidsuite.com\">\n");
		str.append("<m/>\n");
		str.append("<h>\n");
		for ( Field field : inventoryFields )
		{
			str.append("<c>").append(StringEscapeUtils.escapeXml10(field.getName())).append("</c>\n");
		}
		str.append("<c>").append(StringEscapeUtils.escapeXml10("RSC Data Label")).append("</c>\n");
		str.append("<c>").append(StringEscapeUtils.escapeXml10("Navigation Filter")).append("</c>\n");
		if (hasAuditColumns) {
			str.append("<c>").append(StringEscapeUtils.escapeXml10("RSC last updated by name")).append("</c>\n");
			str.append("<c>").append(StringEscapeUtils.escapeXml10("RSC last update date")).append("</c>\n");
		}
		str.append("</h>\n");
		out.write(str.toString());
	}
		
	public static void validation(
			ConvertTargetGridRecordInformation convertTargetGridRecordInformation,
			Set<String> columnNamesToIgnore,Inventory inventory,List<DataRow> dataRows)	throws Exception {
		List<Field> fieldsUsedForDataEntry=inventory.getFieldsUsedForDataEntry();	
		
		List<Integer> mandatoryFieldIndexList=new ArrayList<Integer>();
		Map<Integer,Set<String>> fieldIndexToSubstitutionsMap=new TreeMap<Integer,Set<String>>();
		int colIndex=0;
		for ( Field field : fieldsUsedForDataEntry ) {
			if (fieldsUsedForDataEntry.get(colIndex).getMandatory()) {
				if (columnNamesToIgnore!=null && columnNamesToIgnore.contains(field.getName())) {
					// to ignore
				}
				else {
					mandatoryFieldIndexList.add(colIndex);
				}
			}
			Set<String> substitutionKeysSet=ModelUtils.getSubstitutionKeysSet(field.getSubstitution());
			if (!substitutionKeysSet.isEmpty()) {
				fieldIndexToSubstitutionsMap.put(colIndex, substitutionKeysSet);
			}
			colIndex++;
		}
		
		int totalRecordsMissingDataForRequiredColumns=0;
		int totalRecordsInvalidValues=0;
		for ( DataRow dataRow:dataRows ){
			String[] dataValues=dataRow.getDataValues();			
			boolean hasMissingDataForRequiredColumns=false;
			boolean hasInvalidValues=false;
			for ( Integer mandatoryFieldIndex : mandatoryFieldIndexList ) {
				String value=dataValues[mandatoryFieldIndex];
				if ( value == null || value.isEmpty()){
					hasMissingDataForRequiredColumns=true;
					break;
				}
			}
			
			Iterator<Integer> iterator=fieldIndexToSubstitutionsMap.keySet().iterator();
			while (iterator.hasNext()) {
				Integer fieldIndex=iterator.next();
				String value=dataValues[fieldIndex];
				if (value!=null && !value.isEmpty()) {
					Set<String> substitutionKeysSet=fieldIndexToSubstitutionsMap.get(fieldIndex);
					boolean hasValue=substitutionKeysSet.contains(value);
					if (!hasValue) {
						hasInvalidValues=true;
						break;
					}
				}	
			}
			if (hasMissingDataForRequiredColumns) {
				totalRecordsMissingDataForRequiredColumns++;
			}
			if (hasInvalidValues) {
				totalRecordsInvalidValues++;
			}
		}
		convertTargetGridRecordInformation.setTotalRecordsMissingRequiredColumns(
				convertTargetGridRecordInformation.getTotalRecordsMissingRequiredColumns()+totalRecordsMissingDataForRequiredColumns);
		convertTargetGridRecordInformation.setTotalRecordsInvalidValues(
				convertTargetGridRecordInformation.getTotalRecordsInvalidValues()+totalRecordsInvalidValues);
	}
	
	public static void writeDataRowsConversion(BufferedWriter out,List<DataRow> dataRows,String label) throws IOException	{
		for ( DataRow dataRow:dataRows ){
			writeDataRowConversion(out,dataRow,label);
		}
	}
	
	private static void writeDataRowConversion(BufferedWriter out,DataRow dataRow,String label) throws IOException {
		StringBuffer str=new StringBuffer("");
		str.append("<r>\n");
		for ( String value : dataRow.getDataValues() ) {
			if ( value == null || value.isEmpty()){
				str.append("<c/>\n");
			}
			else{
				if (  value.indexOf("<")!=-1 ||
						value.indexOf(">")!=-1 ||
						value.indexOf("\"")!=-1 ||
						value.indexOf("&")!=-1 ||
						value.indexOf("\\")!=-1) {
					str.append("<c>").append(StringEscapeUtils.escapeXml10(value)).append("</c>\n");
				}
				else {
					str.append("<c>").append(value).append("</c>\n");
				}
			}
		}
		String labelDataRow=dataRow.getLabel();
		if (labelDataRow!=null && !labelDataRow.isEmpty()) {
			str.append("<c>").append(labelDataRow).append("</c>\n");
		}
		else {
			str.append("<c>").append(label).append("</c>\n");
		}
		str.append("<c/>\n");
		String userName=dataRow.getRscLastUpdatedByName();
		str.append("<c>").append(userName).append("</c>\n");
		str.append("<c>").append(dataRow.getRscLastUpdateDate()).append("</c>\n");
		str.append("</r>\n");
		
		out.write(str.toString());
	}

	public static void saveConversionGridsToExcel(
			File outputFile,
			List<ConvertSourceGridRecordInformation> filteredConvertSourceGridRecordInformationList,
			List<ConvertTargetGridRecordInformation> filteredConvertTargetGridRecordInformationList) throws Exception {
		List<String[]> sourceRows=new ArrayList<String[]>();
		int counter=0;
		int totalColumnsToDisplay=14;
		for (ConvertSourceGridRecordInformation convertSourceGridRecordInformation:filteredConvertSourceGridRecordInformationList) {
			counter++;
			int index=0;
			String[] row=new String[totalColumnsToDisplay];
			
			row[index++]=""+counter;
			
			String applicationName=convertSourceGridRecordInformation.getFormInformation().getFormattedApplicationNames();
			if (applicationName==null) {
				applicationName="";
			}
			row[index++]=applicationName;
			
			String menuPath=convertSourceGridRecordInformation.getFormInformation().getFormattedFormPaths();
			if (menuPath==null) {
				menuPath="";
			}
			row[index++]=menuPath;
			
			String formName=convertSourceGridRecordInformation.getFormInformation().getFormName();
			if (formName==null) {
				formName="";
			}
			row[index++]=formName;
			
			String supportText=convertSourceGridRecordInformation.getSupportText();
			if (supportText==null) {
				supportText="";
			}
			row[index++]=supportText;
			
			String inventoryName=convertSourceGridRecordInformation.getInventoryName();
			if (inventoryName==null) {
				inventoryName="";
			}
			row[index++]=inventoryName;
			
			String formType=convertSourceGridRecordInformation.getFormInformation().getFormType();
			if (formType==null) {
				formType="";
			}
			row[index++]=formType;
			
			String status=convertSourceGridRecordInformation.getStatus();
			if (status==null) {
				status="";
			}
			row[index++]=status;
			
			int totalRecordsToConvert=convertSourceGridRecordInformation.getTotalRecordsToConvert();
			if (totalRecordsToConvert==-1) {
				totalRecordsToConvert=0;
			}
			row[index++]=""+totalRecordsToConvert;
			
			//int totalRecordsNotFullyConverted=convertSourceGridRecordInformation.getTotalRecordsNotFullyConverted();
			//if (totalRecordsNotFullyConverted==-1) {
			//	totalRecordsNotFullyConverted=0;
			//}
			//row[index++]=""+totalRecordsNotFullyConverted;
			
			//String columnNamesNotFullyConverted=convertSourceGridRecordInformation.getColumnNamesNotFullyConverted();
			//if (columnNamesNotFullyConverted==null) {
			//	columnNamesNotFullyConverted="";
			//}
			//row[index++]=columnNamesNotFullyConverted;
			
			String remarks=convertSourceGridRecordInformation.getRemarks();
			if (remarks==null) {
				remarks="";
			}
			row[index++]=remarks;
			
			String executionTime=convertSourceGridRecordInformation.getExecutionTime();
			if (executionTime==null) {
				executionTime="";
			}
			row[index++]=executionTime;
			
			long rawTimeInSecs=convertSourceGridRecordInformation.getRawTimeInSecs();
			row[index++]=""+rawTimeInSecs;
			
			sourceRows.add(row);
		}
		String[] headerRow=new String[totalColumnsToDisplay];
		int index=0;
		String NEW_LINE="\n";
		
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_ROW_NUM.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_MODULE_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_FORM_PATH.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_FORM_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_IS_SUPPORTED.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_INVENTORY_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_FORM_TYPE.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_STATUS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_TOTAL_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		//headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_COUNT.replaceAll(NEW_LINE," ");
		//headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_COLUMN_NAMES_PARTIAL.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_REMARKS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_EXECUTION_TIME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertSourcePanel.COLUMN_HEADING_RAW_TIME.replaceAll(NEW_LINE," ");
		sourceRows.add(0,headerRow);

		List<String[]> targetRows=new ArrayList<String[]>();
		counter=0;
		totalColumnsToDisplay=10;
		if (filteredConvertTargetGridRecordInformationList==null) {
			filteredConvertTargetGridRecordInformationList=new ArrayList<ConvertTargetGridRecordInformation>();
		}
		for (ConvertTargetGridRecordInformation convertTargetGridRecordInformation:filteredConvertTargetGridRecordInformationList) {
			counter++;
			index=0;
			String[] row=new String[totalColumnsToDisplay];
			
			row[index++]=""+counter;
			
			row[index++]=convertTargetGridRecordInformation.getInventoryName();
			
			String status=convertTargetGridRecordInformation.getStatus();
			if (status==null) {
				status="";
			}
			row[index++]=status;
			
			int totalRecords=convertTargetGridRecordInformation.getTotalRecords();
			row[index++]=""+totalRecords;
			int totalRecordsConfiguration=convertTargetGridRecordInformation.getTotalRecordsConfiguration();
			row[index++]=""+totalRecordsConfiguration;
			int totalRecordsPostConfiguration=convertTargetGridRecordInformation.getTotalRecordsPostConfiguration();
			row[index++]=""+totalRecordsPostConfiguration;
			int totalRecordsPostImplementation=convertTargetGridRecordInformation.getTotalRecordsPostImplementation();
			row[index++]=""+totalRecordsPostImplementation;
			
			int totalRecordsMissingRequiredColumns=convertTargetGridRecordInformation.getTotalRecordsMissingRequiredColumns();
			row[index++]=""+totalRecordsMissingRequiredColumns;
			
			int totalRecordsInvalidValues=convertTargetGridRecordInformation.getTotalRecordsInvalidValues();
			row[index++]=""+totalRecordsInvalidValues;
			
			String remarks=convertTargetGridRecordInformation.getRemarks();
			if (remarks==null) {
				remarks="";
			}
			row[index++]=remarks;
			
			targetRows.add(row);
		}
		headerRow=new String[totalColumnsToDisplay];
		index=0;
		
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_ROW_NUM.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_INVENTORY_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_STATUS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_TOTAL_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION.replaceAll(NEW_LINE," ");
		headerRow[index++]=ConvertTargetPanel.COLUMN_HEADING_REMARKS.replaceAll(NEW_LINE," ");
		targetRows.add(0,headerRow);
		
		String firstSheetTitle="Source Grid";
		String secondSheetTitle="Target Grid";
		createAndSaveMultipleXLSXSheets(firstSheetTitle,secondSheetTitle,outputFile,sourceRows,targetRows);
	}
	
	public static void createAndSaveMultipleXLSXSheets(String firstSheetTitle,String secondSheetTitle,File outputFile,List<String[]> listFirstSheet
			,List<String[]> listSecondSheet) throws Exception{
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet firstSheet = wb.createSheet(firstSheetTitle) ;
		generate(firstSheet,listFirstSheet);
		XSSFSheet secondSheet = wb.createSheet(secondSheetTitle) ;
		generate(secondSheet,listSecondSheet);
		
		FileOutputStream fileOut = new FileOutputStream(outputFile);
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
	}
	
	private static void generate(XSSFSheet sheet,List<String[]> list) throws Exception {
		for(int i =0; i< list.size();i++) {
			String[] gridRow=list.get(i);
			XSSFRow row = sheet.createRow(i);
			for(int j =0; j< gridRow.length;j++) {
				XSSFCell cell = row.createCell(j);
				cell.setCellValue(gridRow[j]);
			}
		}
	}

	public static Set<String> getSubstitutionKeysSet(String substitution) {
		Set<String> toReturn=new TreeSet<String>();
		if (substitution==null || substitution.isEmpty()) {
			return toReturn;
		}
		String[] tokens=substitution.split("###");
		for (String token:tokens) {
			String[] keyValueArray=token.split("=");
			String key=keyValueArray[0];
			toReturn.add(key);
		}
		return toReturn;
	}
	
	public static StringBuffer getComparisonSQLQuery(StringBuffer sqlQuery,List<Integer> primaryKeysPositionList,List<List<String>> distinctPKValuesList,
			List<String> preparedStatementValues) {
		StringBuffer sqlBuffer =new StringBuffer("");
		sqlBuffer.append("SELECT T.* FROM (");			 
		sqlBuffer.append(sqlQuery);
		sqlBuffer.append(") T WHERE ");
		int counter=0;
		for (List<String> distinctPKValuesRow:distinctPKValuesList) {
			sqlBuffer.append(" ( ");
			int pkCounter=0;
			for (String distinctPKValue:distinctPKValuesRow) {
				Integer pkPosition=primaryKeysPositionList.get(pkCounter);
				if (distinctPKValue.isEmpty()) {
					sqlBuffer.append(" C").append(pkPosition).append(" is null ");
				}
				else {
					sqlBuffer.append(" C").append(pkPosition).append(" = ? ");
					preparedStatementValues.add(distinctPKValue);
				}
				if (pkCounter+1<distinctPKValuesRow.size()) {
					sqlBuffer.append(" AND ");
				}
				pkCounter++;
			}
			sqlBuffer.append(" ) ");
			if (counter+1<distinctPKValuesList.size()) {
				sqlBuffer.append(" OR ");
			}
			counter++;
		}
		sqlBuffer=ModelUtils.addOracleUserNamesToQuery(sqlBuffer.toString());
		return sqlBuffer;
	}
	
	public static List<String> getComparisonTableNameList(Connection connection,String user) throws Exception {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement=null;
		try {
			String sqlQuery="SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'TABLE' AND UPPER(OWNER) = '"+user.toUpperCase()+"'"+
					" and OBJECT_NAME like '"+DB_COMPARISON_TABLES_PREFIX+"%'";
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
	
	public static void dropComparisonTables(Connection connection,String dbUserName,ControllerModalWindow controllerModalWindow) {
		try {
			controllerModalWindow.updateExecutionLabels("Cleaning up temporary Comparison tables... Retrieving list of tables");
			List<String> tableNamesList=ModelUtils.getComparisonTableNameList(connection,dbUserName);
			controllerModalWindow.updateExecutionLabels("Cleaning up temporary Comparison tables... 0 / "+tableNamesList.size()+" tables.");
			int index=0;
			for (String tableName:tableNamesList) {
				index++;
				if (index % 20 ==0) {
					controllerModalWindow.updateExecutionLabels("Cleaning up temporary Comparison tables... "+index+" / "+tableNamesList.size()+" tables.");
				}
				ModelUtils.dropTable(dbUserName,connection,tableName);
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to drop the temporary Comparison tables. Error: "+e.getMessage());
		}
	}
	
	public static boolean hasColumnName(Connection connection,String user,String tableName,String columnName) throws Exception {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement=null;
		try {
			String sqlQuery=
					"Select count(*) res From All_Tab_Columns Where Owner = '"+user.toUpperCase()+"' And Table_Name = '"+tableName+"'"+
					" and COLUMN_NAME='"+columnName+"'";
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			resultSet.next();
			int cnt=resultSet.getInt(1);
			
			return cnt==1;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static void alterTableAddIsConversionColumn(Connection connection,String dbSchema) {
		PreparedStatement preparedStatement = null;
		try
		{			
			StringBuffer sqlBuffer=new StringBuffer("");
			sqlBuffer.append("ALTER TABLE ").append(dbSchema).append(".SNAPSHOT").append(" add(IS_CONVERSION  VARCHAR(1))");
						
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
	
	public static List<UserInformation> getSnapshotUserInformationList(Map<String, String> snapshotEnvironmentProperties)
			throws ClassNotFoundException, SQLException {
		Connection connection=null;
		try {
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
			
			return ModelUtils.getSnapshotUserInformationList(connection);
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public static List<UserInformation> getSnapshotUserInformationList(Connection connection) throws SQLException {
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= 
			"select * from USERS where IS_DELETED='N' ORDER BY login_name";
					
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			List<UserInformation> toReturn=new ArrayList<UserInformation>();
			while (resultSet.next()) {
				int id= resultSet.getInt("id");
				String loginName= resultSet.getString("login_name");
				String fullName= resultSet.getString("full_name");
				String password= resultSet.getString("password");
				String snapshotCreation= resultSet.getString("SNAPSHOT_CREATION");
				String isEnabled= resultSet.getString("IS_ENABLED");
				String isManager= resultSet.getString("IS_MANAGER");
				String isEnableDefaultParallelWorkersField = resultSet.getString(IS_ENABLED_WORKERS);
				String isEnableServerConnectionsDefaultFolderButton = resultSet.getString(IS_ENABLED_CONNECT_FOLDR);
				String isEnableDownloadFolderButton = resultSet.getString(IS_ENABLED_DOWNLD_FOLDR);
				String isEnableTemplateFolderButton = resultSet.getString(IS_ENABLED_TMP_FOLDR);
				String isEnableDefaultSnapshotNamePrefixField = resultSet.getString(IS_ENABLED_SNP_PRFX);
				String isEnableResetOptionButton = resultSet.getString(IS_ENABLED_RESET_OPTN);
				String isEnableDeleteOptionButton = resultSet.getString(IS_ENABLED_DELETE_OPTION);
				String isEnableDisplayTotalDetailCheckBox = resultSet.getString(IS_ENABLED_TOTAL_DETAIL_OPTION);
				
				/*Preference part*/
				String prefOverrideExist = resultSet.getString(PREF_OVERRIDES_EXISTS_KEY);
				String prefDefaultParallelWorkers = resultSet.getString(PREF_DEFAULT_PARALLEL_WORKERS_KEY);
				String prefServerConnectionFolderLocation = resultSet.getString(PREF_SERVER_CONNECTIONS_FOLDER_LOCATION_KEY);
				String prefDownloadFolderLocation = resultSet.getString(PREF_DOWNLOAD_FOLDER_LOCATION_KEY);
				String prefTemplateFolderLocation = resultSet.getString(PREF_TEMPLATE_FOLDER_LOCATION_KEY);
				String prefSnapshotPrefixName = resultSet.getString(PREF_SNAPSHOT_PREFIX_NAME_KEY);
				String prefDownloadFormat = resultSet.getString(PREF_DOWNLOAD_FORMAT_KEY);
				String prefDeleteOption = resultSet.getString(PREF_SNAPSHOT_DELETE_OPTION_KEY);		
				
				String prefDisplayUnsupportedInvOption = resultSet.getString(PREF_DISPLAY_UNSUPPORTED_INVENTORIES_KEY);
				String prefDisplayTotalDetailOption = resultSet.getString(PREF_DISPLAY_TOTAL_DETAILS_KEY);
				String prefDisplayHelperBalloon = resultSet.getString(PREF_SHOW_HELPER_BALLOONS_KEY);
				String prefDisplayBR100 = resultSet.getString(PREF_BR100_VERTICAL_WAY_KEY);
				
				String prefSeededUsersInfoOption = resultSet.getString(PREF_SEEDED_USERS_DEFINITION_KEY);	
				String prefIncludeSoftDeletedSnapshots = resultSet.getString(PREF_INC_SOFT_DELETED_OPTION_KEY);	
				
				
				

				/*Timestamp osAuthLogin=resultSet.getTimestamp("OS_AUTH_LOGIN_DATE");
				Timestamp lastPwdUpdatedDate=resultSet.getTimestamp("LAST_PSWD_UPDATE_DATE");*/

				UserInformation userInformation= new UserInformation();
				userInformation.setId(id);
				userInformation.setLoginName(loginName);
				userInformation.setFullName(fullName);
				
				String decText=null;
				String fakePassword="DUMMY PASSWORD AS IT CANNOT BE DECRYPTED!!!";
				try{
					decText= com.rapidesuite.snapshot.model.ModelUtils.dec(password,EK);
					if (decText.isEmpty()) {
						decText=fakePassword;
					}
				}
				catch(Exception e) {
					decText=fakePassword;
				}
				userInformation.setPassword(decText);
				
				userInformation.setHasSnapshotCreationPermission(snapshotCreation.equals("Y"));
				userInformation.setEnabled(isEnabled.equals("Y"));
				userInformation.setUserManager(isManager.equals("Y"));
				
				userInformation.setEnableDefaultParallelWorkersField("Y".equals(isEnableDefaultParallelWorkersField));
				userInformation.setEnableServerConnectionsDefaultFolderButton("Y".equals(isEnableServerConnectionsDefaultFolderButton));
				userInformation.setEnableDownloadFolderButton("Y".equals(isEnableDownloadFolderButton));
				userInformation.setEnableTemplateFolderButton("Y".equals(isEnableTemplateFolderButton));
				userInformation.setEnableDefaultSnapshotNamePrefixField("Y".equals(isEnableDefaultSnapshotNamePrefixField));
				userInformation.setEnableResetOptionButton("Y".equals(isEnableResetOptionButton));
				userInformation.setEnableDeleteOptionCheckBox("Y".equals(isEnableDeleteOptionButton));
				userInformation.setEnableDisplayTotalDetailOption("Y".equals(isEnableDisplayTotalDetailCheckBox));
				
				userInformation.setPrefOverrideExist("Y".equals(prefOverrideExist));
				if(prefDefaultParallelWorkers!="" && prefDefaultParallelWorkers!=null){
					userInformation.setPrefDefaultParallelWorkers(Integer.parseInt(prefDefaultParallelWorkers));
				}else{
					userInformation.setPrefDefaultParallelWorkers(16);
				}
				userInformation.setPrefServerConnectionFolderLocation(prefServerConnectionFolderLocation);
				userInformation.setPrefDownloadFolderLocation(prefDownloadFolderLocation);
				userInformation.setPrefTemplateFolderLocation(prefTemplateFolderLocation);
				userInformation.setPrefSnapshotPrefixName(prefSnapshotPrefixName);
				userInformation.setPrefDownloadFormat(prefDownloadFormat);
				userInformation.setPrefDeleteOption(prefDeleteOption);
				userInformation.setPrefDisplayUnsupportedInvOption("Y".equals(prefDisplayUnsupportedInvOption));
				userInformation.setPrefDisplayTotalDetailOption("Y".equals(prefDisplayTotalDetailOption));
				userInformation.setPrefDisplayHelperBalloon("Y".equals(prefDisplayHelperBalloon));
				userInformation.setPrefDisplayBR100("Y".equals(prefDisplayBR100));
				userInformation.setPrefSeededUsersInfoOption(prefSeededUsersInfoOption);
				userInformation.setPrefIncludeSoftDeletedSnapshot("Y".equals(prefIncludeSoftDeletedSnapshots));
				
				/*if(osAuthLogin!=null){
					Date osAuthLoginDate = new Date(osAuthLogin.getTime());
					userInformation.setOsAuthLoginDate(osAuthLoginDate);
				}
				if(lastPwdUpdatedDate!=null){
					Date lastPasswordUpdatedDate = new Date(lastPwdUpdatedDate.getTime());
					userInformation.setLastPasswordUpdateDate(lastPasswordUpdatedDate);
				}*/
				toReturn.add(userInformation);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static void createUser(Connection connection,UserInformation userInformation) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
/*			String sql ="insert into USERS(ID,LOGIN_NAME,PASSWORD,FULL_NAME,CREATED_ON,SNAPSHOT_CREATION,IS_MANAGER,IS_ENABLED "+
					",IS_ENABLED_WORKERS,IS_ENABLED_CONNECT_FOLDR,IS_ENABLED_DOWNLD_FOLDR,IS_ENABLED_TMP_FOLDR "+
					",IS_ENABLED_SNP_PRFX,IS_ENABLED_RESET_OPTN,IS_ENABLED_DELETE_OPTION,IS_ENABLED_TOTAL_DETAIL_OPTION,LAST_PSWD_UPDATE_DATE)"+
					"values(USER_ID_SEQ.NEXTVAL,?,?,?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";*/
			String sql ="insert into USERS(ID,LOGIN_NAME,PASSWORD,FULL_NAME,CREATED_ON,SNAPSHOT_CREATION,IS_MANAGER,IS_ENABLED "+
					",IS_ENABLED_WORKERS,IS_ENABLED_CONNECT_FOLDR,IS_ENABLED_DOWNLD_FOLDR,IS_ENABLED_TMP_FOLDR "+
					",IS_ENABLED_SNP_PRFX,IS_ENABLED_RESET_OPTN,IS_ENABLED_DELETE_OPTION,IS_ENABLED_TOTAL_DETAIL_OPTION,PREF_OVERRIDES_EXISTS)"+
					"values(USER_ID_SEQ.NEXTVAL,?,?,?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?)";

			statement= connection.prepareStatement(sql);
			int index=1;
			statement.setString(index++, userInformation.getLoginName());
			
			String encText= com.rapidesuite.snapshot.model.ModelUtils.enc(userInformation.getPassword(),EK);
			
			statement.setString(index++,encText);
			statement.setString(index++, userInformation.getFullName());
			String permission="N";
			if (userInformation.hasSnapshotCreationPermission()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			boolean isUserManager=userInformation.isUserManager();
			permission="N";
			if (isUserManager) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			boolean isEnabled=userInformation.isEnabled();
			permission="N";
			if (isEnabled) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			boolean isEnableDefaultParallelWorkersField = userInformation.isEnableDefaultParallelWorkersField();
			permission="N";
			if (isEnableDefaultParallelWorkersField) {
				permission="Y";
			}
			statement.setString(index++, permission);
			boolean isEnableServerConnectionsDefaultFolderButton = userInformation.isEnableServerConnectionsDefaultFolderButton();
			permission="N";
			if (isEnableServerConnectionsDefaultFolderButton) {
				permission="Y";
			}
			statement.setString(index++, permission);
			boolean isEnableDownloadFolderButton = userInformation.isEnableDownloadFolderButton();
			permission="N";
			if (isEnableDownloadFolderButton) {
				permission="Y";
			}
			statement.setString(index++, permission);
			boolean isEnableTemplateFolderButton = userInformation.isEnableTemplateFolderButton();
			permission="N";
			if (isEnableTemplateFolderButton) {
				permission="Y";
			}
			statement.setString(index++, permission);
			boolean isEnableDefaultSnapshotNamePrefixField = userInformation.isEnableDefaultSnapshotNamePrefixField();
			permission="N";
			if (isEnableDefaultSnapshotNamePrefixField) {
				permission="Y";
			}
			statement.setString(index++, permission);
			boolean isEnableResetOptionButton = userInformation.isEnableResetOptionButton(); 		
			permission="N";
			if (isEnableResetOptionButton) {
				permission="Y";
			}
			statement.setString(index++, permission);
			boolean isEnableDeleteOptionCheckBox = userInformation.isEnableDeleteOptionCheckBox(); 		
			permission="N";
			if (isEnableDeleteOptionCheckBox) {
				permission="Y";
			}
			statement.setString(index++, permission);			
			boolean isEnableDisplayTotalDetailCheckBox = userInformation.isEnableDisplayTotalDetailOption();		
			permission="N";
			if (isEnableDisplayTotalDetailCheckBox) {
				permission="Y";
			}
			statement.setString(index++, permission);				
			//OVERRIDE PREF EXIST
			statement.setString(index++, "Y");
			
			statement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static void resetAllUsersCreationPermission(Connection connection) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="update USERS set SNAPSHOT_CREATION='N'";

			statement= connection.prepareStatement(sql);
			statement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static void resetAllUsersManagerPermission(Connection connection) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="update USERS set IS_MANAGER='N'";

			statement= connection.prepareStatement(sql);
			statement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static void updateUser(Connection connection,UserInformation userInformation) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			String sql ="update USERS set FULL_NAME=?,SNAPSHOT_CREATION=?,PASSWORD=?,IS_ENABLED=?,IS_MANAGER=? ,"+
						" IS_ENABLED_WORKERS=?,IS_ENABLED_CONNECT_FOLDR=?,IS_ENABLED_DOWNLD_FOLDR=?,IS_ENABLED_TMP_FOLDR=?, "+
						" IS_ENABLED_SNP_PRFX=?,IS_ENABLED_RESET_OPTN=?,IS_ENABLED_DELETE_OPTION=?,IS_ENABLED_TOTAL_DETAIL_OPTION=?,  "+
						" PREF_DEFAULT_PARALLEL_WORKERS=?,PREF_CONNECTIONS_FOLDER_LOC=?,PREF_DOWNLOAD_FOLDER_LOCATION=?,PREF_TEMPLATE_FOLDER_LOCATION=?,  "+
						" PREF_SNAPSHOT_PREFIX_NAME=?,PREF_DOWNLOAD_FORMAT=?,PREF_SNAPSHOT_DELETE_OPTION=?,PREF_INC_SOFT_DELETED_OPTION=?,  "+
						" PREF_DISPLAY_UNSUPPORTED_INV=?,PREF_DISPLAY_TOTAL_DETAILS=?,PREF_SHOW_HELPER_BALLOONS=?,PREF_BR100_VERTICAL_WAY=?,  "+
						" PREF_SEEDED_USERS_DEFINITION=?  "+
		                "where id=?";

			String encText= com.rapidesuite.snapshot.model.ModelUtils.enc(userInformation.getPassword(),EK);
			
			statement= connection.prepareStatement(sql);
			int index=1;
			statement.setString(index++, userInformation.getFullName());
			String permission="N";
			if (userInformation.hasSnapshotCreationPermission()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			statement.setString(index++,encText);
			
			permission="N";
			if (userInformation.isEnabled()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			permission="N";
			if (userInformation.isUserManager()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			permission="N";
			if (userInformation.isEnableDefaultParallelWorkersField()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			permission="N";
			if (userInformation.isEnableServerConnectionsDefaultFolderButton()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			permission="N";
			if (userInformation.isEnableDownloadFolderButton()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			permission="N";
			if (userInformation.isEnableTemplateFolderButton()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			permission="N";
			if (userInformation.isEnableDefaultSnapshotNamePrefixField()) {
				permission="Y";
			}
			statement.setString(index++, permission);
		
			permission="N";
			if (userInformation.isEnableResetOptionButton()) {
				permission="Y";
			}
			statement.setString(index++, permission);
			
			permission="N";
			if (userInformation.isEnableDeleteOptionCheckBox()) {
				permission="Y";
			}
			statement.setString(index++, permission);

			permission="N";
			if (userInformation.isEnableDisplayTotalDetailOption()) {
				permission="Y";
			}
			statement.setString(index++, permission);	
			
			//SET PREFERENCE
			statement.setInt(index++, userInformation.getPrefDefaultParallelWorkers());
			statement.setString(index++, userInformation.getPrefServerConnectionFolderLocation());
			statement.setString(index++, userInformation.getPrefDownloadFolderLocation());
			statement.setString(index++, userInformation.getPrefTemplateFolderLocation());
			
			statement.setString(index++, userInformation.getPrefSnapshotPrefixName());
			statement.setString(index++, userInformation.getPrefDownloadFormat());
			statement.setString(index++, userInformation.getPrefDeleteOption());			
			statement.setString(index++, ((userInformation.isPrefIncludeSoftDeletedSnapshot()) ? "Y" : "N"));
			
			statement.setString(index++, ((userInformation.isPrefDisplayUnsupportedInvOption()) ? "Y" : "N"));
			statement.setString(index++, ((userInformation.isPrefDisplayTotalDetailOption()) ? "Y" : "N"));
			statement.setString(index++, ((userInformation.isPrefDisplayHelperBalloon()) ? "Y" : "N"));
			statement.setString(index++, ((userInformation.isPrefDisplayBR100()) ? "Y" : "N"));
			statement.setString(index++, userInformation.getPrefSeededUsersInfoOption());	
			
			statement.setInt(index++, userInformation.getId());
	
			statement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}

	public static void deleteUser(Connection connection, UserInformation userInformation) throws SQLException {
		PreparedStatement statement = null;
		try
		{
			String sql ="UPDATE USERS SET IS_DELETED='Y' WHERE ID="+userInformation.getId();
			statement= connection.prepareStatement(sql);
			statement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static void updateUserPassword(Connection connection,int userId,String password) throws SQLException {
		PreparedStatement statement = null;
		try
		{
			String sql ="UPDATE USERS SET PASSWORD=? WHERE ID="+userId;
			statement= connection.prepareStatement(sql);
			
			String encText= com.rapidesuite.snapshot.model.ModelUtils.enc(password,EK);
			int index=1;
			statement.setString(index++,encText);			
			statement.executeUpdate();
		}
		finally
		{
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	public static Map<Integer,UserInformation> getUserIdToUserInformationMap(List<UserInformation> userInformationList) {
		Map<Integer,UserInformation> toReturn=new HashMap<Integer,UserInformation>();
		for (UserInformation userInformation:userInformationList) {
			toReturn.put(userInformation.getId(),userInformation);
		}
		return toReturn;
	}
	
	@SuppressWarnings("unused")
	public static List<DataRow> convert(Inventory inventory,List<String[]> dataRows) {
		List<DataRow> toReturn=new ArrayList<DataRow>();
		final List<Field> fields=inventory.getFieldsUsedForDataEntry();
		for (String[] dataRowArray:dataRows) {
			DataRow	dataRow=new DataRow();
			toReturn.add(dataRow);
			String[] dataValues= new String[fields.size()];
			dataRow.setDataValues(dataValues);
			
			int colIndex=0;
			for (Field field:fields) {
				String sourceValue=dataRowArray[colIndex];
				dataValues[colIndex]=sourceValue;
				colIndex++;
			}
			/*
			<c>RSC Data Label</c>
			<c>Navigation Filter</c>
			<c>RSC last updated by name</c>
			<c>RSC last update date</c>
			*/
			colIndex++;
			String lastUpdatedBy=dataRowArray[colIndex++];
			dataRow.setRscLastUpdatedByName(lastUpdatedBy);
			String lastUpdateDate=dataRowArray[colIndex++];
			dataRow.setRscLastUpdateDate(lastUpdateDate);
		}
		return toReturn;
	}
	
	public static void insertInventoryToSnapshot(Connection connection,int snapshotId,
			List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList)
	throws SQLException, ParseException
	{
		PreparedStatement preparedStatement = null;
		try
		{
			String sql ="insert into INVENTORY_TO_SNAPSHOT(INVENTORY_ID,SNAPSHOT_ID,STATUS,MSG,EXECUTION_TIME_STRING,"+
					"EXECUTION_RAW_TIME,TOTAL_RECORD,CREATED_ON,COMPLETED_ON) values(?,?,?,?,?,?,?,?,?)";
		
			preparedStatement= connection.prepareStatement(sql);
			
			for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsList) {
				if(!DB_STATUS_UNSELECTED.equals(snapshotInventoryGridRecord.getStatus())){
					int index=1;
					preparedStatement.setInt(index++, snapshotInventoryGridRecord.getTableId());
					preparedStatement.setInt(index++, snapshotId);
					preparedStatement.setString(index++, snapshotInventoryGridRecord.getStatus());
					preparedStatement.setString(index++, snapshotInventoryGridRecord.getRemarks());
					preparedStatement.setString(index++, snapshotInventoryGridRecord.getExecutionTime());
					preparedStatement.setLong(index++, snapshotInventoryGridRecord.getRawTimeInSecs());
					preparedStatement.setInt(index++, snapshotInventoryGridRecord.getTotalRecords());
					if(snapshotInventoryGridRecord.getCreatedOn()!="" && snapshotInventoryGridRecord.getCreatedOn()!=null){
						String createdOn=snapshotInventoryGridRecord.getCreatedOn();
						Timestamp timeStamp=getTimeStamp(createdOn);
						preparedStatement.setTimestamp(index++, timeStamp);					
					}else{
						preparedStatement.setTimestamp(index++, null);	
					}
					if(snapshotInventoryGridRecord.getCompletedOn()!="" && snapshotInventoryGridRecord.getCompletedOn()!=null){
						String createdOn=snapshotInventoryGridRecord.getCompletedOn();
						Timestamp timeStamp=getTimeStamp(createdOn);
						preparedStatement.setTimestamp(index++, timeStamp);					
					}else{
						preparedStatement.setTimestamp(index++, null);	
					}
					preparedStatement.addBatch(); 					
				}     
			}
			preparedStatement.executeBatch();
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
	
	public static Map<Long,List<MapGeneric>> getMapGeneric3ColumnsImport(Connection connection,int snapshotId,String type)
	throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= "select COL1,COL2,COL3 from IMPORT_METADATA where snapshot_id="+snapshotId+" and type='"+type+"'";
			preparedStatement = connection.prepareStatement(sqlQuery);
			resultSet=preparedStatement.executeQuery();
			Map<Long,List<MapGeneric>> toReturn=new HashMap<Long,List<MapGeneric>>();
			while (resultSet.next()) {
				long col1= resultSet.getLong("COL1");
				long col2= resultSet.getLong("COL2");
				String name= resultSet.getString("COL3");

				List<MapGeneric> list=toReturn.get(col1);
				if (list==null) {
					list=new ArrayList<MapGeneric>();
					toReturn.put(col1,list);
				}
				MapGeneric mapGeneric=new MapGeneric(name,col2);
				list.add(mapGeneric);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static Map<Long,MapGeneric> getMapGeneric2ColumnsImport(Connection connection,int snapshotId,String type,String searchValue)
			throws Exception
	{
		PreparedStatement preparedStatement=null;
		ResultSet resultSet = null;
		try {
			String sqlQuery= "select COL1,COL2 from IMPORT_METADATA where snapshot_id="+snapshotId+" and type='"+type+"'"+
				" and upper(COL2) like upper(?) order by COL2 asc";
			preparedStatement = connection.prepareStatement(sqlQuery);
			preparedStatement.setString(1, searchValue);
			
			resultSet=preparedStatement.executeQuery();
			Map<Long,MapGeneric> toReturn=new HashMap<Long,MapGeneric>();
			while (resultSet.next()) {
				long col1= resultSet.getLong("COL1");
				String name= resultSet.getString("COL2");
				MapGeneric mapGeneric=new MapGeneric(name,col1);
				toReturn.put(col1,mapGeneric);
			}
			return toReturn;
		}
		finally {
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}

	public static Map<Long, List<InventoryOrganization>> getOperatingUnitIdToInventoryOrganizationsListImport(
			Connection connection, int snapshotId) throws Exception {
		Map<Long,List<MapGeneric>> mapGenericImport=getMapGeneric3ColumnsImport(
				connection,snapshotId,SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_OU_TO_INV);
		
		Map<Long,List<InventoryOrganization>> toReturn=new HashMap<Long,List<InventoryOrganization>>();
		
		Iterator<Long> iterator=mapGenericImport.keySet().iterator();
		while (iterator.hasNext()) {
			Long key=iterator.next();
			List<MapGeneric> mapGenericList=mapGenericImport.get(key);
			List<InventoryOrganization> list=new ArrayList<InventoryOrganization>();
			toReturn.put(key,list);
			for (MapGeneric mapGeneric:mapGenericList) {
				InventoryOrganization obj=new InventoryOrganization(mapGeneric.getName(),mapGeneric.getId());
				list.add(obj);
			}
		}
		return toReturn;
	}

	public static Map<Long, List<Ledger>> getOperatingUnitToLedgersMapImport(Connection connection, int snapshotId) throws Exception {
		Map<Long,List<MapGeneric>> mapGenericImport=getMapGeneric3ColumnsImport(
				connection,snapshotId,SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_OU_TO_LEDGER);
		
		Map<Long,List<Ledger>> toReturn=new HashMap<Long,List<Ledger>>();
		
		Iterator<Long> iterator=mapGenericImport.keySet().iterator();
		while (iterator.hasNext()) {
			Long key=iterator.next();
			List<MapGeneric> mapGenericList=mapGenericImport.get(key);
			List<Ledger> list=new ArrayList<Ledger>();
			toReturn.put(key,list);
			for (MapGeneric mapGeneric:mapGenericList) {
				Ledger obj=new Ledger(mapGeneric.getName(),mapGeneric.getId());
				list.add(obj);
			}
		}
		return toReturn;
	}

	public static Map<Long, List<LegalEntity>> getOperatingUnitIdToLegalEntityListImport(Connection connection,
			int snapshotId) throws Exception {
		Map<Long,List<MapGeneric>> mapGenericImport=getMapGeneric3ColumnsImport(
				connection,snapshotId,SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_OU_TO_LEGAL_ENTITY);
		
		Map<Long,List<LegalEntity>> toReturn=new HashMap<Long,List<LegalEntity>>();
		
		Iterator<Long> iterator=mapGenericImport.keySet().iterator();
		while (iterator.hasNext()) {
			Long key=iterator.next();
			List<MapGeneric> mapGenericList=mapGenericImport.get(key);
			List<LegalEntity> list=new ArrayList<LegalEntity>();
			toReturn.put(key,list);
			for (MapGeneric mapGeneric:mapGenericList) {
				LegalEntity obj=new LegalEntity(mapGeneric.getName(),mapGeneric.getId());
				list.add(obj);
			}
		}
		return toReturn;
	}

	public static Map<Long, List<BusinessGroup>> getOperatingUnitIdToBusinessGroupListImport(Connection connection,
			int snapshotId) throws Exception {
		Map<Long,List<MapGeneric>> mapGenericImport=getMapGeneric3ColumnsImport(
				connection,snapshotId,SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_OU_TO_BG);
		
		Map<Long,List<BusinessGroup>> toReturn=new HashMap<Long,List<BusinessGroup>>();
		
		Iterator<Long> iterator=mapGenericImport.keySet().iterator();
		while (iterator.hasNext()) {
			Long key=iterator.next();
			List<MapGeneric> mapGenericList=mapGenericImport.get(key);
			List<BusinessGroup> list=new ArrayList<BusinessGroup>();
			toReturn.put(key,list);
			for (MapGeneric mapGeneric:mapGenericList) {
				BusinessGroup obj=new BusinessGroup(mapGeneric.getName(),mapGeneric.getId());
				list.add(obj);
			}
		}
		return toReturn;
	}
	
	public static Map<String, OperatingUnit> getOperatingUnitsSearchResults(Connection connection,
			int snapshotId,String inputValue) throws Exception {
		Map<Long, MapGeneric> mapGenericImport=getMapGeneric2ColumnsImport(
				connection,snapshotId,SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_OU,inputValue);
		
		Map<String,OperatingUnit> toReturn=new HashMap<String,OperatingUnit>();
		
		Iterator<Long> iterator=mapGenericImport.keySet().iterator();
		while (iterator.hasNext()) {
			Long key=iterator.next();
			MapGeneric mapGeneric=mapGenericImport.get(key);
			OperatingUnit operatingUnit=new OperatingUnit(mapGeneric.getName(),mapGeneric.getId());
			toReturn.put(mapGeneric.getName(),operatingUnit);
		}
		return toReturn;
	}
		
	public static int getOracleUsersCountSearchResults(Connection connection,int snapshotId, String inputValue)
			throws Exception
	{
		Map<Long, MapGeneric> mapGenericImport=getMapGeneric2ColumnsImport(
				connection,snapshotId,SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_USER,inputValue);
		
		return mapGenericImport.size();
	}

	public static Map<String, OracleUser> getOracleUsersSearchResults(Connection connection, String inputValue,int maxLimit, int snapshotId)
			throws Exception
	{
		Map<Long, MapGeneric> mapGenericImport=getMapGeneric2ColumnsImport(
				connection,snapshotId,SnapshotImportSwingWorker.IMPORT_METADATA_TYPE_USER,inputValue);
		
		Map<String,OracleUser> toReturn=new TreeMap<String,OracleUser>();
		
		Iterator<Long> iterator=mapGenericImport.keySet().iterator();
		int counter=0;
		while (iterator.hasNext()) {
			Long key=iterator.next();
			MapGeneric mapGeneric=mapGenericImport.get(key);
			OracleUser oracleUser=new OracleUser(mapGeneric.getName(),mapGeneric.getId());
			toReturn.put(mapGeneric.getName(),oracleUser);
			counter++;
			if (counter==maxLimit) {
				break;
			}
		}
		return toReturn;
	}
	public static String removeHTMLTagsFromString(String string) {
		Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
	    if (string == null || string.length() == 0) {
	        return string;
	    }
	    Matcher m = REMOVE_TAGS.matcher(string);
	    return m.replaceAll("").replace("&nbsp;", "");
	}
	public static String getEnvironmentNextToken(String text,StringBuffer remainingText){
		String SEPARATOR = "##RES##";
		int indexOf=text.indexOf(SEPARATOR);
		if (indexOf!=-1) {
			String value=text.substring(0,indexOf);
			remainingText.append(text.substring(indexOf+SEPARATOR.length()));
			return value;
		}
		return null;
	}
	public static List<File> getFilesByName(File outputFolder, String name, String fileExtension) throws Exception{
		List<File> toReturn = new ArrayList<File>();
		String fileExt = "";
		if(outputFolder.isDirectory()){
			File[] listOfFiles = outputFolder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++){
				if (listOfFiles[i].isFile()){
				    fileExt = listOfFiles[i].getName().substring(listOfFiles[i].getName().lastIndexOf(".") + 1);
					if(listOfFiles[i].getName().contains(name) && fileExtension.equals(fileExt)){
						toReturn.add(listOfFiles[i]);
					}
				}
			}
		}
		return toReturn;
	}
	public static void deleteFiles(List<File> listOfFilesToDelete) throws Exception{
		for (int i = 0; i < listOfFilesToDelete.size(); i++){
			if(listOfFilesToDelete.get(i).isFile()){
				listOfFilesToDelete.get(i).delete();
			}
		}
	}
	public static String[] getHeaderRow(int extraColumnsCount,List<Field> inventoryFieldsDataOnly){
		String[] toReturn=new String[extraColumnsCount+inventoryFieldsDataOnly.size()];
		int index=0;
		toReturn[index++]="Seq";
		for ( Field field : inventoryFieldsDataOnly )
		{
			String fieldName=field.getName();
			if (fieldName==null) {
				fieldName="";
			}
			toReturn[index++]=fieldName;
		}
		toReturn[index++]="RSC Data Label";
		toReturn[index++]="Navigation Filter";
		toReturn[index++]="RSC last updated by name";
		toReturn[index++]="RSC last update date";
		toReturn[index++]="RSC Created by name";
		toReturn[index++]="RSC Creation date";		
		return toReturn;
	}
	
	public static String getListSnapshotIdAsString(List<Integer> snapshotIdList){
		String toReturn = "";
		for(int snapshotId : snapshotIdList){
			toReturn+= String.valueOf(snapshotId)+",";
		}
		toReturn = toReturn.substring(0, toReturn.length()-1);
		return toReturn;
	}
	public static int deleteInventoryToSnapshotData(Connection connection,List<Integer> snapshotIdList,String invIdStr) throws Exception, SQLException{
		StringBuffer sql = new StringBuffer("");
		int totalRow = 0;
		try {			
				sql.append("DELETE FROM INVENTORY_TO_SNAPSHOT ");
				sql.append("WHERE SNAPSHOT_ID IN (").append(getListSnapshotIdAsString(snapshotIdList)).append(")");
				if(invIdStr!=null){
					sql.append(" AND INVENTORY_ID  = ").append(invIdStr);
				}
			totalRow = executeQueryForSnapshotPhysicalDelete(connection,sql);
			return totalRow;
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}		
	}		
	public static int deleteSnapshotDataFromSnapshotTable(Connection connection,List<Integer> snapshotIdList) throws Exception, SQLException{
		StringBuffer sql = new StringBuffer("");
		int totalRow = 0;
		try {			
			sql.append("DELETE FROM SNAPSHOT");
			sql.append(" WHERE ID IN (").append(getListSnapshotIdAsString(snapshotIdList)).append(")");
			totalRow = executeQueryForSnapshotPhysicalDelete(connection,sql);
			return totalRow;
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}		
	}	
	
	public static int deleteSnapshotDataFromInventoryTableByBatch(Connection connection,List<Integer> snapshotIdList,String tableId,int batchSize) throws Exception, SQLException {
		StringBuffer sql = new StringBuffer("");
		String fullTableName = DB_TABLES_PREFIX+tableId;
		try{
			sql.append("DELETE FROM ").append(fullTableName);
			sql.append(" WHERE ROWID IN ( ");
			sql.append("     SELECT ROWID FROM ( ");
			sql.append("          SELECT ROWID FROM ").append(fullTableName).append(" WHERE SNAPSHOT_ID IN ( ").append(getListSnapshotIdAsString(snapshotIdList)).append(")").append(" ) ");
			sql.append("     WHERE ROWNUM <= ").append(batchSize).append(" ) ");
			return executeQueryForSnapshotPhysicalDelete(connection,sql);	
		}finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}		
	public static int executeQueryForSnapshotPhysicalDelete(Connection connection,StringBuffer sql) throws Exception, SQLException {
		PreparedStatement preparedStatement = null;
		try{
			preparedStatement = connection.prepareStatement(sql.toString());	
			preparedStatement.setQueryTimeout(DatabaseUtils.PREPARED_STATEMENT_TIMEOUT_SECONDS);
			int totalRow = preparedStatement.executeUpdate();
			connection.commit();
			return totalRow;
			
		}catch(SQLException sqlE){
			int retCode = sqlE.getErrorCode(); 
			sqlE.printStackTrace();
			System.out.println("RET CODE : "+retCode);
			//FileUtils.printStackTrace(sqlE);
			//FileUtils.println("RET CODE : "+retCode);
			throw sqlE;
		}
		catch(Exception e){
			e.printStackTrace();
			connection.rollback();
			throw e;
		}
		finally{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}		
	public static List<SnapshotInventoryGridRecord> getSnapshotInventoryGridRecordsList(Connection connection,List<Integer> snapshotIdList) throws Exception{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<SnapshotInventoryGridRecord> toReturn=new ArrayList<SnapshotInventoryGridRecord>();
		try{
			String sql ="select isn.inventory_id,I.NAME,sum(isn.total_record) total_record from INVENTORY_TO_SNAPSHOT ISN, INVENTORY I "+
		            " where ISN.SNAPSHOT_ID IN ("+getListSnapshotIdAsString(snapshotIdList)+")"+
					" AND I.ID=ISN.INVENTORY_ID";
			sql=sql+" and status IN ('"+DB_STATUS_WARNING+"','"+DB_STATUS_SUCCESS+"')";
			sql= sql+" group by isn.inventory_id,I.NAME ";
			sql=sql+" order by INVENTORY_ID";
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			while (resultSet.next()) {
				int tableId=resultSet.getInt("INVENTORY_ID");
				String inventoryName=resultSet.getString("NAME");
				int totalRecords=resultSet.getInt("TOTAL_RECORD");	
				SnapshotInventoryGridRecord snapshotInventoryGridRecord=new SnapshotInventoryGridRecord(inventoryName);
				snapshotInventoryGridRecord.setTableId(tableId);
				snapshotInventoryGridRecord.setTotalRecords(totalRecords);
				toReturn.add(snapshotInventoryGridRecord);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	public static List<Integer> getListOfDeletedSnapshotId(Connection connection) throws Exception{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<Integer> toReturn=new ArrayList<Integer>();
		try{
			String sql = "SELECT ID FROM SNAPSHOT WHERE IS_DELETED = 'Y'";
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			while (resultSet.next()) {
				int snapshotId=resultSet.getInt("ID");
				toReturn.add(snapshotId);
			}			
			return toReturn;
		}finally{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	public static Map<String,SnapshotInventoryGridRecord> getInventoryToSnapshotInventoryMap(Connection connection)
			throws SQLException, ClassNotFoundException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql ="select NAME,id,IS_EXECUTABLE,IS_LISTABLE from INVENTORY";
			
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			Map<String, SnapshotInventoryGridRecord> toReturn=new HashMap<String,SnapshotInventoryGridRecord>();
			while (resultSet.next()) {
				String name=resultSet.getString("NAME");
				int id=resultSet.getInt("ID");
				String isExecutableStr = resultSet.getString("IS_EXECUTABLE");
				String isListableStr = resultSet.getString("IS_LISTABLE");
				boolean isExecutable = false;
				boolean isListable = false;
				if(isExecutableStr==null || isExecutableStr.equals("Y")){
					isExecutable = true;
				}
				if(isListableStr==null || isListableStr.equals("Y")){
					isListable = true;
				}
				SnapshotInventoryGridRecord obj = new SnapshotInventoryGridRecord(name);
				obj.setTableId(id);
				obj.setExecutable(isExecutable);
				obj.setListable(isListable);
				toReturn.put(name,obj);
			}
			return toReturn;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	public static int insertNewInventoryToDB(Connection connection,List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList) 
			throws SQLException {
		PreparedStatement preparedStatement = null;
		try
		{
			String sql ="insert into INVENTORY(ID ,NAME,IS_EXECUTABLE,IS_LISTABLE) values(INVENTORY_ID.NEXTVAL,?,?,?) ";
			preparedStatement= connection.prepareStatement(sql);
			for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsList) {
				int index=1;
				preparedStatement.setString(index++,snapshotInventoryGridRecord.getInventoryName()); 
				String executable = ((snapshotInventoryGridRecord.isExecutable() == true) ? "Y" : "N");
				String listable = ((snapshotInventoryGridRecord.isListable() == true) ? "Y" : "N");
				preparedStatement.setString(index++,executable); 
				preparedStatement.setString(index++,listable); 
				preparedStatement.addBatch();      
			}
			int totalRecords[] = preparedStatement.executeBatch();
			connection.commit();
			return totalRecords.length;
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}	
	public static int executeUpdateInventoryDetail(Connection connection, List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList) throws Exception{
		PreparedStatement preparedStatement = null;
		try
		{
			String sql ="UPDATE INVENTORY SET IS_EXECUTABLE = ? , IS_LISTABLE = ? WHERE ID = ? ";
			preparedStatement= connection.prepareStatement(sql);
			for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsList) {
				int index=1;
				String executable = ((snapshotInventoryGridRecord.isExecutable() == true) ? "Y" : "N");
				String listable = ((snapshotInventoryGridRecord.isListable() == true) ? "Y" : "N");
				preparedStatement.setString(index++,executable); 
				preparedStatement.setString(index++,listable); 
				preparedStatement.setString(index++, String.valueOf(snapshotInventoryGridRecord.getTableId()));
				preparedStatement.addBatch();      
			}
			int totalRecords[] = preparedStatement.executeBatch();
			connection.commit();
			return totalRecords.length;
		}
		finally
		{
			DirectConnectDao.closeQuietly(preparedStatement);
		}
	}
		
	public static List<SnapshotInventoryGridRecord> getSnapshotInventoryGridRecordListToDisplay (TabSnapshotsPanel tabSnapshotsPanel) throws Exception{
		List<SnapshotInventoryGridRecord> toReturn =new ArrayList<SnapshotInventoryGridRecord>();
		String connectionOracleRelease=tabSnapshotsPanel.getServerSelectionPanel().getCurrentConnectionOracleRelease();
		Map<String, String> snapshotEnvironmentProperties = ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		Connection connection=DatabaseUtils.getJDBCConnection(
				ModelUtils.getJDBCString(snapshotEnvironmentProperties),
				ModelUtils.getDBUserName(snapshotEnvironmentProperties),
				ModelUtils.getDBPassword(snapshotEnvironmentProperties));
		connection.setAutoCommit(false);

		Map<String, File> inventoryNameToInventoryFileMap=tabSnapshotsPanel.getSnapshotPackageSelectionPanel().
				getInventoryNameToInventoryFileMap(connectionOracleRelease);
		Map<String, File> inventoryNameToReverseSQLFileMap=tabSnapshotsPanel.getSnapshotPackageSelectionPanel().
				getInventoryNameToReverseSQLFileMap(connectionOracleRelease);
		boolean isShowUnsupportedInventories=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().isShowUnsupportedInventories();
		Map<String, FormInformation> inventoryNameToFormInformationMap=tabSnapshotsPanel.getSnapshotPackageSelectionPanel().
				getInventoryNameToFormInformation(connectionOracleRelease);
		
		Iterator<String> iterator=inventoryNameToInventoryFileMap.keySet().iterator();
		int gridIndex=0;
		Set<String> moduleSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			File reverseSQLFile=inventoryNameToReverseSQLFileMap.get(inventoryName);	
			if (!isShowUnsupportedInventories) {
				if (reverseSQLFile==null) {
					continue;
				}
			}
				SnapshotInventoryGridRecord snapshotInventoryGridRecord=new SnapshotInventoryGridRecord(inventoryName);
				FormInformation formInformation=inventoryNameToFormInformationMap.get(snapshotInventoryGridRecord.getInventoryName());
				if (formInformation!=null) {
					snapshotInventoryGridRecord.setFormInformation(formInformation);
				}
				Set<String> applicationNameSet=snapshotInventoryGridRecord.getFormInformation().getApplicationNameSet();
				if (applicationNameSet!=null) {
					moduleSet.addAll(applicationNameSet);
				}
				snapshotInventoryGridRecord.setDefaultSelected(true);
				snapshotInventoryGridRecord.setGridIndex(gridIndex);
				toReturn.add(snapshotInventoryGridRecord);
				gridIndex++;					
		}

		Map<String,SnapshotInventoryGridRecord> inventoryNameMapDetail = getInventoryToSnapshotInventoryMap(connection);
		for (SnapshotInventoryGridRecord snapshotCreationGridRecord:toReturn) {
			SnapshotInventoryGridRecord info =inventoryNameMapDetail.get(snapshotCreationGridRecord.getInventoryName());
			if (info!=null) {
				snapshotCreationGridRecord.setTableId(info.getTableId());
				snapshotCreationGridRecord.setExecutable(info.isExecutable());
				snapshotCreationGridRecord.setListable(info.isListable());
			}else{
				snapshotCreationGridRecord.setTableId(-1);
				snapshotCreationGridRecord.setExecutable(true);
				snapshotCreationGridRecord.setListable(true);					
			}
		}
		return toReturn;		
	}
	public static Connection getConnection(TabSnapshotsPanel tabSnapshotsPanel) throws Exception{
		Connection connection;
		try{
			Map<String, String> snapshotEnvironmentProperties = ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));
			connection.setAutoCommit(false);
			return connection;
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Cannot get the JDBC Connection, error : "+e.getMessage());
		}
	}
	
	
	public static void deleteSnapshotByAge(String jdbcString,int age, String deleteType, boolean isIncludeSoftDeletedSnapshots) throws Exception{
		try{
			List<SnapshotGridRecord> snapshotList = getListOfDeletedSnapshotIdByAge(DatabaseUtils.getJDBCConnectionFromJDBCStringGeneric(jdbcString,true),age,deleteType,isIncludeSoftDeletedSnapshots);
			List<Integer> snapshotIdList = new ArrayList<Integer>();
			for(SnapshotGridRecord rec : snapshotList){
				snapshotIdList.add(rec.getSnapshotId());
			}
			if(!snapshotList.isEmpty()){
				if("P".equals(deleteType)){
						ModelUtils.deleteInventoryToSnapshotData(DatabaseUtils.getJDBCConnectionFromJDBCStringGeneric(jdbcString,true),snapshotIdList,null);	
						ModelUtils.deleteSnapshotDataFromSnapshotTable(DatabaseUtils.getJDBCConnectionFromJDBCStringGeneric(jdbcString,true),snapshotIdList);	
				}else if("S".equals(deleteType)){
					for (int snapshotId:snapshotIdList) {
						deleteSnapshotSoft(DatabaseUtils.getJDBCConnectionFromJDBCStringGeneric(jdbcString,true),snapshotId);
					}
				}
			}else{
				System.out.println("NO SNAPSHOI IS OLDER THAN "+age+" DAYS, NOTHING TO DELETE.");
			}
			int ind=1;
			for(SnapshotGridRecord rec : snapshotList){
				System.out.println(ind+") "+rec.getSnapshotId()+" : "+rec.getName());
				ind++;
			}
			System.out.println("DELETING PROCESS WAS COMPLETED, DELETED "+snapshotList.size()+" SNAPSHOT(S).");
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("CANNOT DELETE SNAPSHOTS, ERROR : "+e.getMessage());
		}
	}
	public static List<SnapshotGridRecord> getListOfDeletedSnapshotIdByAge(Connection connection, int age, String deleteType,boolean isIncludeSoftDeletedSnapshots) throws Exception{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<SnapshotGridRecord> toReturn=new ArrayList<SnapshotGridRecord>();
		try{
			String sql = "SELECT ID,NAME FROM SNAPSHOT WHERE (IS_DELETED = 'N' AND TRUNC(CREATED_ON)<=TRUNC(SYSDATE)-"+age+")";
			if("P".equals(deleteType) && isIncludeSoftDeletedSnapshots){
				sql+= " OR (IS_DELETED = 'Y')";
			}
			statement= connection.prepareStatement(sql);
			resultSet=statement.executeQuery();
			while (resultSet.next()) {
				int snapshotId=resultSet.getInt("ID");
				String snapshotName = resultSet.getString("NAME");
				SnapshotGridRecord obj = new SnapshotGridRecord();
				obj.setSnapshotId(snapshotId);
				obj.setName(snapshotName);
				toReturn.add(obj);
			}			
			return toReturn;
		}finally{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	public static void updateUserOsAuthLogInDate(Connection connection,UserInformation userInformation, boolean isResetToNull) throws SQLException {
		PreparedStatement statement = null;
		try{
			String sql ="";
			if(isResetToNull){
				sql = "UPDATE USERS SET OS_AUTH_LOGIN_DATE=NULL WHERE ID="+userInformation.getId();
			}else{
				sql = "UPDATE USERS SET OS_AUTH_LOGIN_DATE=CURRENT_TIMESTAMP WHERE ID="+userInformation.getId();
			}
			statement= connection.prepareStatement(sql);
			int totalRow = statement.executeUpdate();
			connection.commit();
		}
		finally
		{
			DirectConnectDao.closeQuietly(statement);
		}
	}
	public static boolean isRunningSnapshotByPLBatching(String sqlContent) throws Exception{
		boolean toReturn = false;
		if(sqlContent.contains(UtilsConstants.SNAPSHOT_PLSQL_VOLUME_BATCHING_KEYWORD)){
			toReturn = true;
		}
		return toReturn;
	}
}