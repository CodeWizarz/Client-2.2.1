/**************************************************
 * $Revision: 52361 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-02-02 11:53:48 +0700 (Tue, 02 Feb 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/DataExtractionJob.java $:
 * $Id: DataExtractionJob.java 52361 2016-02-02 04:53:48Z jannarong.wadthong $:
 */
package com.rapidesuite.reverse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;

import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.JobManager;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.reverse.gui.OracleDatesSelectionPanel;
import com.rapidesuite.reverse.utils.DFFManager;
import com.rapidesuite.reverse.utils.DataExtractionDatabaseUtils;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;
import com.rapidesuite.reverse.utils.DataExtractionUtils;

public class DataExtractionJob extends Job implements Comparable<DataExtractionJob>
{
    protected SwiftGUIMain swiftGUIMain;
	private int fetchSize;
	private String label;
	private File prefixOutputFolder;
	private int lastReadRow;
	private File reverseFile;
	private List<PreparedStatement> statementsList;
	private int retrySleepTimeInMS;
	private String isDuplicateOfJobMessage;
	private String plsqlPackageName;
	private boolean isAuditColumnsIncludedInDataFile;
	private int currentDataFileIndex;
	private int currentLabelIterator;
	private boolean prerequisiteObjectExists;

	public DataExtractionJob(
			JobManager executionJobManager,
			int taskId,
			Map<String,String> environmentProperties,
			String inventoryFileName,
			Inventory inventory,
			File prefixOutputFolder,
			String plsqlPackageName,
			 SwiftGUIMain swiftGUIMain)
	{
		super(executionJobManager,taskId,environmentProperties,inventoryFileName,inventory);
		this.prefixOutputFolder=prefixOutputFolder;
		this.lastReadRow=0;
		this.currentDataFileIndex=0;
		this.currentLabelIterator=0;
		this.plsqlPackageName=plsqlPackageName;
		retrySleepTimeInMS = Config.getReverseRetrySleepTime();
		fetchSize=Config.getReverseFetchSize();
		label=Config.getReverseDataLabel();
		statementsList=new ArrayList<PreparedStatement>();
		this.swiftGUIMain = swiftGUIMain;
	}

	public void setLastReadRow(int lastReadRow) {
		this.lastReadRow = lastReadRow;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public int getLastReadRow() {
		return lastReadRow;
	}

	public String getLabel() {
		return label;
	}

	public File getPrefixOutputFolder() {
		return prefixOutputFolder;
	}

	public void startExecution() throws Exception {
		try{
			startProcessingReverseFile();
		}
		catch ( Exception e ){
			DataExtractionFileUtils.logReverseError(e,jobId,inventoryFileName,reverseFile);
			// show error in the console to help debugging 
			e.printStackTrace();
			throw e;
		}
	}

	public void startProcessingReverseFile()
	throws Exception{
		if (executionJobManager.isManualStopped()) {
			executionJobManager.updateExecutionStatus(this,"ERROR: manual stop.");
			setJobComplete(true);
			return;
		}
		if (isDuplicateOfJobMessage!=null) {
			executionJobManager.updateExecutionStatus(this,"SUCCESS: "+isDuplicateOfJobMessage);
			setJobComplete(false);
			return;
		}
		List<Field> inventoryDataOnlyFields=FileUtils.getInventoryDataOnlyFields(inventory);
		reverseFile=getReverseFile(inventory);
		if (reverseFile==null) {
			executionJobManager.updateExecutionStatus(this,"WARNING: no records found.");
			// a waring message has no error
			setJobComplete(false);
			return;
		}
		if (prefixOutputFolder==null) {
			executionJobManager.updateExecutionStatus(this,"ERROR: unable to find the output prefix folder.");
			setJobComplete(true);
			return;
		}
		if (!inventory.isReversible()) {
			executionJobManager.updateExecutionStatus(this,"SUCCESS: unreversible.");
			setJobComplete(false);
			return;
		}

		int retriesCount=0;
		boolean isRestartAtRecord=false;
		boolean isSuccess=false;
		try{
			String sqlQuery=FileUtils.readContentsFromSQLFile(reverseFile);
			sqlQuery=processReplacementProperties(sqlQuery);
			sqlQuery = convertLastUpdatedToCreatedOn(sqlQuery);

			Map<String, Boolean> packageNameToContainsRscAuditColumnMap=this.swiftGUIMain.getPackageNameToContainsRscAuditColumnMap();
			Boolean hasPLSQLPackageNameBool=packageNameToContainsRscAuditColumnMap.get(plsqlPackageName);
			boolean hasPLSQLPackageName=false;
			if (hasPLSQLPackageNameBool!=null) {
				hasPLSQLPackageName=hasPLSQLPackageNameBool.booleanValue();
			}
			boolean hasAuditColumnsInSQLQuery=DataExtractionUtils.hasAuditColumns(sqlQuery,plsqlPackageName,hasPLSQLPackageName);
			boolean isAuditExtractionEnabled=hasAuditColumnsInSQLQuery&&
			((ReverseMain)executionJobManager.getSwiftGUIMain()).getDataExtractionPanel().getDataExtractionOptionsPanel().isAuditExtractionEnabled();
			isAuditColumnsIncludedInDataFile=hasAuditColumnsInSQLQuery&&isAuditExtractionEnabled;

			while (true) {
				try{
					processReverseFileContent(sqlQuery,inventoryDataOnlyFields,isRestartAtRecord,isAuditExtractionEnabled,hasAuditColumnsInSQLQuery);
					isSuccess=true;
					break;
				}
				catch(SQLException e) {
					String err=e.getMessage();
					if (err!=null && (err.toLowerCase().indexOf("io exception")!=-1 ||
						err.toLowerCase().indexOf("io error")!=-1)  && !executionJobManager.isManualStopped() )
					{
						retriesCount++;
						executionJobManager.updateExecutionStatus(this,"RETRY ("+retriesCount+") : lost connection...");
						isRestartAtRecord=true;
						try{
							com.rapidesuite.client.common.util.Utils.sleep(retrySleepTimeInMS);
						}
						catch(InterruptedException intEx){FileUtils.printStackTrace(intEx);}
					}
					else {
	                    // catch only Connection failure, other exception to propagate
						throw e;
					}
				}
			}
		}
		finally{
			// we remove the data file if the manual stop was done or any errors as the XML data file will be corrupt
			if (executionJobManager.isManualStopped() || !isSuccess) {
				executionJobManager.updateExecutionStatus(this,"ERROR: manual stop.");
				setJobComplete(true);
				File corruptedFile= DataExtractionFileUtils.getFinalXMLDataFile(this,getCurrentDataFileIndex());
				boolean success = corruptedFile.delete();
				if (!success){
					FileUtils.println("WARNING: Unable to remove file: '"+corruptedFile.getAbsolutePath()+"' it may be corrupted.");
				}
			}
			else {
				setJobComplete(false);
			}
		}
	}

	public File getReverseFile(Inventory inventory)
	throws Exception {
		SortedMap<String,SortedSet<File>> tablesSQLMapping=((ReverseMain)executionJobManager.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().getInventoryToSQLFileMap();
		SortedSet<File> sqlFiles=tablesSQLMapping.get(inventory.getName());
		if (sqlFiles==null) {
			throw new Exception("Unable to locate the SQL file.");
		}
		if (sqlFiles.size()==1) {
			return DataExtractionFileUtils.getSQLFile(sqlFiles.iterator().next());
		}
		FileUtils.println("Inventory: "+inventory.getName()+" is mapped to "+sqlFiles.size()+" SQL files.");
		for (File sqlFile:sqlFiles) {
			if (executionJobManager.isManualStopped()) {
				executionJobManager.updateExecutionStatus(this,"ERROR: manual stop.");
				setJobComplete(true);
				throw new Exception("WARNING: manual stop.");
			}
			Connection connection=null;
			PreparedStatement preparedStatementCount=null;
			try{
				File reverseFile=DataExtractionFileUtils.getSQLFile(sqlFile);
				if (reverseFile==null ) {
					setJobComplete(true);
					throw new Exception("Cannot find the SQL file (check the project XML file). No records extracted.");
				}
				FileUtils.println("Running query from SQL file: '"+reverseFile.getAbsolutePath()+"' ...");
				String reverseFileContent=FileUtils.readContentsFromSQLFile(reverseFile);
				String sqlQuery=reverseFileContent;
				Properties replacementsProperties=DataExtractionUtils.getReplacementsPropertiesClone(executionJobManager.getSwiftGUIMain().getReplacementsProperties());
				sqlQuery = DataExtractionUtils.addUserFiltering(this,replacementsProperties, sqlQuery);
				DataExtractionUtils.addEBSProperties(this,replacementsProperties);
				DataExtractionUtils.addDateFiltering(this,replacementsProperties);
				sqlQuery=DataExtractionUtils.replaceTokens(replacementsProperties,sqlQuery);
				connection=DataExtractionDatabaseUtils.getDatabaseConnectionForReverseQuery(this,this.getBweProperties());
				preparedStatementCount=connection.prepareStatement(DataExtractionDatabaseUtils.processSQLForRecordCountExtraction(sqlQuery));
				statementsList.add(preparedStatementCount);
				DataExtractionDatabaseUtils.getExpectedRecordCountToReverse(preparedStatementCount);
				FileUtils.println("Query returned so using this SQL file: '"+reverseFile.getAbsolutePath()+"' ...");
				return reverseFile;
			}
			catch (Exception e) {
				FileUtils.println("Failure: error: "+e.getMessage());
			}
			finally {
			    DirectConnectDao.closeQuietly(preparedStatementCount);
			    DirectConnectDao.closeQuietly(connection);
			}
		}
		return null;
	}

	private String convertLastUpdatedToCreatedOn(String sql)
	{
        boolean isDateFilteringByUpdateDate = ((ReverseMain) this.getReverseExecutionJobManager().getSwiftGUIMain()).getDataExtractionPanel().getOracleDatesSelectionPanel().isDateFilteringByUpdateDate();
        if ( isDateFilteringByUpdateDate )
        {
            return sql;
        }

        String toReturn = sql.replaceAll("(?i)" + OracleDatesSelectionPanel.LAST_UPDATE_DATE, OracleDatesSelectionPanel.CREATION_DATE);

        final String RSC_CREATION_DATE = "RSC_CREATION_DATE";
        final String RSC_LAST_UPDATE_DATE = "RSC_LAST_UPDATE_DATE";
        toReturn = toReturn.replaceAll("(?i)" + RSC_CREATION_DATE, RSC_LAST_UPDATE_DATE);

        final String CREATION_DATE_OPERATOR = "CREATION_DATE_OPERATOR";
        final String LAST_UPDATE_DATE_OPERATOR = "LAST_UPDATE_DATE_OPERATOR";
        toReturn = toReturn.replaceAll("(?i)" + CREATION_DATE_OPERATOR, LAST_UPDATE_DATE_OPERATOR);
        return toReturn;
	}


	private void processReverseFileContent(
			String sqlQuery,
			List<Field> inventoryDataOnlyFields,
			boolean isRestartAtRecord,
			boolean isAuditExtractionEnabled,
			boolean hasAuditColumnsInSQLQuery)
	throws Exception
	{
		ResultSet resultSet = null;
		PreparedStatement preparedStatementExecute=null;
		PreparedStatement preparedStatementCount=null;
		Connection connection=null;
		try
		{
			if (!DataExtractionUtils.validatePrerequisiteObjects(this,sqlQuery)) {
				this.setPrerequisiteObjectExists(false);
				return;
			}
			if (isRestartAtRecord) {
				sqlQuery=DataExtractionUtils.getQueryFromLastReadRow(sqlQuery,getLastReadRow());
			}
			DataExtractionFileUtils.logQueryToExecute(this,sqlQuery);
			connection=DataExtractionDatabaseUtils.getDatabaseConnectionForReverseQuery(this,environmentProperties);
			int expectedRecordCountToReverse=0;
			if (!executionJobManager.isManualStopped()) {
				executionJobManager.updateExecutionStatus(this,"PROCESSING: counting records to extract...");
				String countQuery=DataExtractionDatabaseUtils.processSQLForRecordCountExtraction(sqlQuery);
				preparedStatementCount=connection.prepareStatement(countQuery);
				statementsList.add(preparedStatementCount);
				expectedRecordCountToReverse=DataExtractionDatabaseUtils.getExpectedRecordCountToReverse(preparedStatementCount);
				if (isRestartAtRecord) {
					expectedRecordCountToReverse=getLastReadRow()+expectedRecordCountToReverse;
				}

				if (((ReverseMain)executionJobManager.getSwiftGUIMain()).isAnalyseMode()) {
					String msg="SUCCESS: "+Utils.formatNumberWithComma(expectedRecordCountToReverse)+" records found (analyse mode).";
					executionJobManager.updateExecutionStatus(this,msg);
					setJobComplete(false);
					return;
				}
			}

			boolean isDFF=DataExtractionDatabaseUtils.hasDFFTableQueryKeyword(sqlQuery);
			DFFManager dffManager=null;
			if (isDFF) {
				String dffTableQuery=DataExtractionDatabaseUtils.getDFFTableQuery(sqlQuery);
				dffManager=new DFFManager(this);
				executionJobManager.updateExecutionStatus(this,"PROCESSING: loading DFF table...");
				dffManager.loadDFFTable(environmentProperties,dffTableQuery);
			}

			if (!executionJobManager.isManualStopped()) {
				preparedStatementExecute=DataExtractionDatabaseUtils.getStatementForReverseQueryConnection(this,connection,sqlQuery);
				statementsList.add(preparedStatementExecute);
				executionJobManager.updateExecutionStatus(this,"PROCESSING: executing query...");
				resultSet = preparedStatementExecute.executeQuery();

				if (isDFF && dffManager!=null) {
					dffManager.initializeDFFColumnNamesCollection(resultSet);
				}
				executionJobManager.updateExecutionStatus(this,"PROCESSING: query executed. Extracting data... "+
						Utils.formatNumberWithComma(expectedRecordCountToReverse)+" estimated records.");

				if (isDFF && dffManager!=null) {
					DataExtractionDatabaseUtils.processResultSet(this,dffManager,resultSet,expectedRecordCountToReverse,inventoryDataOnlyFields,isRestartAtRecord,
							isAuditExtractionEnabled,hasAuditColumnsInSQLQuery);
				}
				else {
					ResultSetMetaData rsMetaData = resultSet.getMetaData();
					
					Boolean hasPLSQLPackageNameBool= this.swiftGUIMain.getPackageNameToContainsRscAuditColumnMap().get(plsqlPackageName);
					boolean hasPLSQLPackageName=false;
					if (hasPLSQLPackageNameBool!=null) {
						hasPLSQLPackageName=hasPLSQLPackageNameBool.booleanValue();
					}
					
					DataExtractionUtils.validateReverseQueryWithInventoryColumns(inventoryDataOnlyFields,sqlQuery,rsMetaData.getColumnCount(),plsqlPackageName,isRestartAtRecord,
							hasPLSQLPackageName);
					DataExtractionDatabaseUtils.processResultSet(this,null,resultSet,expectedRecordCountToReverse,inventoryDataOnlyFields,isRestartAtRecord,
							isAuditExtractionEnabled,hasAuditColumnsInSQLQuery);
				}
			}
		}
		finally
		{
		    DirectConnectDao.closeQuietly(resultSet);
		    DirectConnectDao.closeQuietly(preparedStatementCount);
		    DirectConnectDao.closeQuietly(preparedStatementExecute);
		    DirectConnectDao.closeQuietly(connection);
		}
	}

	public void forceStopExecution()  {
		try {
			for (PreparedStatement ps:statementsList) {
				if (ps!=null && !ps.isClosed()) {
					ps.cancel();
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public DataExtractionJobManager getReverseExecutionJobManager() {
		return (DataExtractionJobManager)executionJobManager;
	}

	public void setIsDuplicateOfJobMessage(String isDuplicateOfJobMessage) {
		this.isDuplicateOfJobMessage = isDuplicateOfJobMessage;
	}

	private String processReplacementProperties(String sqlQuery) throws Exception
	{
		Properties replacementsProperties=DataExtractionUtils.getReplacementsPropertiesClone(
				executionJobManager.getSwiftGUIMain().getReplacementsProperties());
		sqlQuery = DataExtractionUtils.addUserFiltering(this,replacementsProperties, sqlQuery);
		DataExtractionUtils.addEBSProperties(this,replacementsProperties);
		DataExtractionUtils.addDateFiltering(this,replacementsProperties);
		return DataExtractionUtils.replaceTokens(replacementsProperties,sqlQuery);
	}

	public boolean isAuditColumnsIncludedInDataFile() {
		return isAuditColumnsIncludedInDataFile;
	}

	public int getCurrentDataFileIndex() {
		return currentDataFileIndex;
	}

	public void setCurrentDataFileIndex(int currentDataFileIndex) {
		this.currentDataFileIndex = currentDataFileIndex;
	}

	public int getCurrentLabelIterator() {
		return currentLabelIterator;
	}

	public void setCurrentLabelIterator(int currentLabelIterator) {
		this.currentLabelIterator = currentLabelIterator;
	}

    @Override
    public int compareTo(DataExtractionJob o)
    {
        return this.getInventory().getName().compareTo(o.getInventory().getName());
    }

	public boolean isPrerequisiteObjectExists() {
		return prerequisiteObjectExists;
	}

	public void setPrerequisiteObjectExists(boolean prerequisiteObjectExists) {
		this.prerequisiteObjectExists = prerequisiteObjectExists;
	}

}