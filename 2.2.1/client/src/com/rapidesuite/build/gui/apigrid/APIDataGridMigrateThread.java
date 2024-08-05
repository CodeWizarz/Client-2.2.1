package com.rapidesuite.build.gui.apigrid;

import java.sql.Connection;
import java.sql.Savepoint;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.erapidsuite.configurator.apiscript0000.ApiScript;
import com.erapidsuite.configurator.apiscript0000.ApiScriptDocument;
import com.erapidsuite.configurator.apiscript0000.ApiScriptMode;
import com.erapidsuite.configurator.apiscript0000.ApiScriptModes;
import com.erapidsuite.configurator.apiscript0000.ApiScriptPackage;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.client.common.PLSQLFunctionParameter;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.gui.datagrid.DataGridColumn;
import com.rapidesuite.client.common.gui.datagrid.DataGridComboColumn;
import com.rapidesuite.client.common.gui.datagrid.DataGridConstants;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridConstants;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.DbmsOutput;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.core.inventory0007.FldFormFieldData;
import com.rapidesuite.core.utility.EnvironmentPropertyConstants;

public class APIDataGridMigrateThread implements Runnable
{

	private APIDataGridController apiDataGridController;
	private List<String[]> allDataGridRows;
	private boolean isProcessCompletedWithErrors;
	private boolean isExecutionFromPanel;
	private Injector injector;
	private	String statusMessage;
	private	int errorCount;
	private	int totalCounter;
	private	int totalEnabledRowsCount;
	private	String errorMessage;

	public APIDataGridMigrateThread(Injector injector, APIDataGridController apiDataGridController, boolean isExecutionFromPanel)
	{
		this.apiDataGridController = apiDataGridController;
		this.isExecutionFromPanel = isExecutionFromPanel;
		this.injector = injector;
		errorCount = 0;
		totalCounter = 0;
		totalEnabledRowsCount = 0;
	}

	public void run()
	{
		migrateData();
	}

	public List<com.rapidesuite.client.common.PLSQLFunctionParameter> getDataRow(List<DataGridColumn> dataGridColumns, String[] dataGridRow) throws Exception
	{
		List<PLSQLFunctionParameter> res = new ArrayList<PLSQLFunctionParameter>();
		DataGridColumn modeColumn = apiDataGridController.getDataGridInventoryColumns().get(0);
		String mode = apiDataGridController.getDataRowValue(modeColumn.getAttributeDescription(), dataGridRow);
		boolean isUpdateMode = (mode != null && mode.equalsIgnoreCase(APIDataGridConstants.MODE_UPDATE));
		boolean breakLoopOnNextIteration = false;
		for ( DataGridColumn dataGridColumn : dataGridColumns )
		{
			try
			{
				if ( dataGridColumn.getAttributeDescription().equals(APIDataGridConstants.MODE_COLUMN_ATTRIBUTE_DESCRIPTION) )
				{
					continue;
				}
				String value = apiDataGridController.getDataRowValue(dataGridColumn.getAttributeDescription(), dataGridRow);
				DataGridColumn dgc = apiDataGridController.getDataGridColumnsResetValuesOnMap().get(dataGridColumn.getAttributeName());
				Field field = apiDataGridController.getAPIDataAction().getInventory().getField(dataGridColumn.getAttributeDescription());
				boolean isFieldMandatory = field.getMandatory();

				if ( isFieldMandatory && (value == null || value.isEmpty()) )
				{
					throw new Exception("Value required");
				}

				FldFormFieldData fldFieldData = field.getFldFormFieldData();
				String fieldType = "string";
				if ( fldFieldData != null )
				{
					fieldType = fldFieldData.getOracleFieldType();
				}

				if ( breakLoopOnNextIteration )
				{
					break;
				}
				if ( dataGridColumn.getAttributeDescription().equals(InventoryDataGridConstants.ERROR_ID_COLUMN_NAME) && mode.equals(InventoryDataGridConstants.ERROR_GET_MODE) )
				{
					res.clear();  //only include the current column value.
					breakLoopOnNextIteration = true;
				}


				if ( field.getSubstitution() != null && !field.getSubstitution().trim().isEmpty() )
				{
					DataGridComboColumn dgcc = (DataGridComboColumn) dataGridColumn;
					Map<String, String> keyValueMap = dgcc.getKeyValueMap();
					value = keyValueMap.get(value);
					if ( dgc != null && isUpdateMode )
					{
						value = APIDataGridConstants.ORACLE_RESET_VALUE_KEYWORD;
					}
					if ( value != null && value.isEmpty() )
					{
						value = null;
					}
					PLSQLFunctionParameter param = new PLSQLFunctionParameter(dataGridColumn.getAttributeDescription(), value, java.sql.Types.VARCHAR);
					res.add(param);
				}
				else if ( fieldType.equalsIgnoreCase("number") )
				{
					PLSQLFunctionParameter param = null;
					if ( value == null || value.isEmpty() )
					{
						param = new PLSQLFunctionParameter(dataGridColumn.getAttributeDescription(), null, java.sql.Types.DOUBLE);
					}
					else
					{
						double valueDouble = Double.valueOf(value).doubleValue();
						if ( dgc != null && isUpdateMode )
						{
							valueDouble = APIDataGridConstants.ORACLE_RESET_VALUE_NUMBER;
						}
						param = new PLSQLFunctionParameter(dataGridColumn.getAttributeDescription(), valueDouble, java.sql.Types.DOUBLE);
					}
					res.add(param);
				}
				else if ( fieldType.equalsIgnoreCase("datetime") || fieldType.equalsIgnoreCase("date") )
				{
					PLSQLFunctionParameter param = null;
					if ( value == null || value.isEmpty() )
					{
						param = new PLSQLFunctionParameter(dataGridColumn.getAttributeDescription(), null, java.sql.Types.DATE);
					}
					else
					if ( value.indexOf(":") == -1 ) {
						DateFormat df = new SimpleDateFormat(DataGridConstants.ORACLE_DATE_FORMAT);
						java.util.Date utilDate = df.parse(value);
						java.sql.Date sqlValue = new java.sql.Date(utilDate.getTime());
						param = new PLSQLFunctionParameter(dataGridColumn.getAttributeDescription(), sqlValue, java.sql.Types.DATE);
					}
					else {
						DateFormat df = new SimpleDateFormat(DataGridConstants.ORACLE_DATE_FORMAT + " " + DataGridConstants.ORACLE_TIME_FORMAT);
						java.util.Date utilDate = df.parse(value);
						java.sql.Timestamp sqlValue = new java.sql.Timestamp(utilDate.getTime());
						param = new PLSQLFunctionParameter(dataGridColumn.getAttributeDescription(), sqlValue, java.sql.Types.TIMESTAMP);
					}
					res.add(param);
				}
				else
				{
					if ( value != null && value.isEmpty() )
					{
						value = null;
					}
					PLSQLFunctionParameter param = new PLSQLFunctionParameter(dataGridColumn.getAttributeDescription(), value, java.sql.Types.VARCHAR);
					res.add(param);
				}
			}
			catch ( Exception e )
			{
				FileUtils.printStackTrace(e);
				throw new Exception("Error for the column '" + dataGridColumn.getColumnTitle() + "' : " + e.getMessage());
			}
		}
		return res;
	}

	private void validateApiScriptDocument(ApiScriptDocument apiScriptDocument) throws Exception
	{
		ApiScript apiScript = apiScriptDocument.getApiScript();
		ApiScriptPackage apiInjectorsPackage = apiScript.getApiScriptPackage();
		if ( apiInjectorsPackage == null )
		{
			throw new Exception("Invalid script, package tag missing.");
		}
		String packageName = apiInjectorsPackage.getName();
		if ( packageName == null )
		{
			throw new Exception("Invalid script, package name missing.");
		}
		ApiScriptModes apiScriptModes = apiInjectorsPackage.getApiScriptModes();
		if ( apiScriptModes == null )
		{
			throw new Exception("Invalid script, modes tag missing.");
		}
		ApiScriptMode[] apiScriptModeList = apiScriptModes.getApiScriptModeArray();
		if ( apiScriptModeList == null )
		{
			throw new Exception("Invalid script, mode tag missing.");
		}
	}

	private void migrateData()
	{
		Connection connection = null;
		statusMessage="";
		try
		{
			allDataGridRows = apiDataGridController.getDataGridRows();
			for ( String[] dataRow : allDataGridRows )
			{
				if ( dataRow[InventoryDataGridConstants.ENABLED_COLUMN_GRID_INDEX].equalsIgnoreCase("true") )
				{
					totalEnabledRowsCount++;
				}
			}
			if ( totalEnabledRowsCount == 0 )
			{
				setDataGridStatusLabel(errorCount, totalCounter);
				return;
			}

			ApiScriptDocument apiScriptDocument = apiDataGridController.getAPIDataAction().getApiAction().getApiScriptDocument();
			ApiScript apiScript = apiScriptDocument.getApiScript();
			final ApiScriptPackage apiInjectorsPackage = apiScript.getApiScriptPackage();
			ApiScriptModes apiScriptModes = apiInjectorsPackage.getApiScriptModes();
			ApiScriptMode[] apiScriptModeList = apiScriptModes.getApiScriptModeArray();
			validateApiScriptDocument(apiScriptDocument);

			final Map<String, String> modesToFunctionMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
			for ( ApiScriptMode apiScriptMode : apiScriptModeList )
			{
				modesToFunctionMap.put(apiScriptMode.getName(), apiScriptMode.getProcedureName());
			}

			connection = getConnection();
			ExecutorService threadPool = Executors.newFixedThreadPool(Config.getBuildApiInjectionWorkerCount());

            int batchIndex = 0;
            while ( true )
			{
                if ( !apiDataGridController.isProcessStarted() || (apiDataGridController.getAPIDataAction().isExecutionStopped() && isExecutionFromPanel) )
                {
                    break;
                }
			    int batchSize = Config.getBuildApiInjectionCommitAfterXRows();
			    int startIndex = batchIndex * batchSize;
			    if ( startIndex >= allDataGridRows.size() )
			    {
			        break;
			    }
			    int endIndex = batchIndex * batchSize + batchSize;
			    endIndex = Math.min(endIndex, allDataGridRows.size());
			    batchIndex++;
				final List<String[]> batchRows = allDataGridRows.subList(startIndex, endIndex);

				threadPool.execute(new Runnable(){
				    public void run()
				    {
				        executeBatch(apiInjectorsPackage, modesToFunctionMap, batchRows);
				    }
                });
			}
			threadPool.shutdown();
			threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);

			Map<String, DataGridColumn> map = apiDataGridController.getDataGridColumnsResetValuesOnMap();
			map.clear();

			String message = totalCounter + " / " + totalEnabledRowsCount+" ( errors: "+ errorCount+" )";
			if ( !apiDataGridController.isProcessStarted() )
			{
				message = message + ". Manual stop.";
			}
			if ( errorCount == 0 )
			{
				GUIUtils.showSuccessMessage(apiDataGridController.getInventoryDataGridFrame().getStatusLabel(), message);
			}
			else
			{
				GUIUtils.showErrorMessage(apiDataGridController.getInventoryDataGridFrame().getStatusLabel(), message);
			}
			isProcessCompletedWithErrors = errorCount != 0;
			connection.commit();

		}
		catch ( Exception e )
		{
			errorMessage=e.getMessage();
			FileUtils.printStackTrace(e);
            try
            {
                if ( connection != null ) connection.rollback();
            }
            catch(Throwable t)
            {
                FileUtils.printStackTrace(t);
            }
			isProcessCompletedWithErrors = true;
			GUIUtils.showErrorMessage(apiDataGridController.getInventoryDataGridFrame().getStatusLabel(), e.getMessage());
		}
		finally
		{
			if ( isProcessCompletedWithErrors )
			{
				statusMessage = ExecutionStatusTreeTableNode.STATUS_ERROR + ": " + totalCounter + " rows ( errors: "+errorCount+" )";
				apiDataGridController.setFilteringForDataGridRowErrors();
			}
			else
			{
				statusMessage = ExecutionStatusTreeTableNode.STATUS_SUCCESS + ": " + totalCounter + " rows";
				apiDataGridController.resetFiltering();
			}
			if (apiDataGridController.getAPIDataAction().getBuildMain()!=null) {
				apiDataGridController.getAPIDataAction().getBuildMain().getInjectorsExecutionPanel().updateStatus(apiDataGridController.getAPIDataAction().getApiAction().getInjector().getIndex(),statusMessage);
			}
			injector.setStatus(statusMessage);
			DirectConnectDao.closeQuietly(connection);
			apiDataGridController.setProcessButtontoStoppedAndUnLockGrid();
		}
	}

    private Connection getConnection() throws Exception
    {
        if ( Config.getBuildApiJdbcString() == null) {
        	return DatabaseUtils.getDatabaseConnection(apiDataGridController.getAPIDataAction().getEnvironmentProperties());
        }
        else{
        	String userName=apiDataGridController.getAPIDataAction().getEnvironmentProperties().get(EnvironmentPropertyConstants.DATABASE_USER_NAME_KEY);
        	String password=apiDataGridController.getAPIDataAction().getEnvironmentProperties().get(EnvironmentPropertyConstants.DATABASE_PASSWORD_KEY);
        	return DatabaseUtils.getJDBCConnection(Config.getBuildApiJdbcString(),userName,password);
        }
    }


    private synchronized void incrementTotalCount()
    {
        totalCounter++;
        int DISPLAY_UPDATE_BATCH_SIZE = 5;
        if ( totalCounter % DISPLAY_UPDATE_BATCH_SIZE == 0 )
        {
            String msg = totalCounter + " / " + totalEnabledRowsCount;
            statusMessage="Processing: " + msg +" ( errors: "+ errorCount+" )";
            if (apiDataGridController.getAPIDataAction().getBuildMain()!=null) {
                apiDataGridController.getAPIDataAction()
                .getBuildMain()
                .getInjectorsExecutionPanel()
                .updateStatus(apiDataGridController.getAPIDataAction().getApiAction().getInjector().getIndex(),msg);
            }
            apiDataGridController.updateInProgressStatusLabel(statusMessage);
        }
    }

    private synchronized void incrementErrorCount(String[] dataRowToValidate, String message)
    {
        errorCount++;
        if ( message == null )
        {
            message = "";
        }
        setRowStatusAndMessage(dataRowToValidate, InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE, message);
    }


    private void executeBatch(final ApiScriptPackage apiInjectorsPackage, final Map<String, String> modesToFunctionMap, List<String[]> batchRows)
    {
        //System.err.println("ExecuteBatch: rowcount = " + batchRows.size() + ", thread = " + Thread.currentThread().getId() + " " + new Date());
        Connection connection = null;
        try
        {
            connection = APIDataGridMigrateThread.this.getConnection();
            connection.setAutoCommit(false);
            final DbmsOutput dbmsOutput;
            if (Config.getBuildApiDbmsOutputEnabled())
            {
                dbmsOutput = new DbmsOutput( connection );
                dbmsOutput.enable( 1000000 );
            }
            else
            {
                dbmsOutput=null;
            }

            Savepoint savePoint = null;
            for ( int i = 0; i < batchRows.size(); i++ )
            {
                if ( !apiDataGridController.isProcessStarted() || (apiDataGridController.getAPIDataAction().isExecutionStopped() && isExecutionFromPanel) )
                {
//                    System.err.println("Breaking in batch loop. thread = " + Thread.currentThread().getId() + ", apiDataGridController.isProcessStarted() = " + apiDataGridController.isProcessStarted() +
//                            ", apiDataGridController.getAPIDataAction().isExecutionStopped() = " + apiDataGridController.getAPIDataAction().isExecutionStopped() +
//                            ", isExecutionFromPanel " + isExecutionFromPanel);
                    break;
                }

                final String[] dataRowToValidate = batchRows.get(i);
                if ( dataRowToValidate[InventoryDataGridConstants.ENABLED_COLUMN_GRID_INDEX].equalsIgnoreCase("false") )
                {
                    continue;
                }
                try
                {
                    String mode = apiDataGridController.getDataRowValue(apiDataGridController.getModeColumn().getAttributeDescription(), dataRowToValidate);
                    if ( mode == null )
                    {
                        mode = APIDataGridConstants.MODE_CREATE;
                    }
                    final String plsqlFunctionToCall = modesToFunctionMap.get(mode);
                    if ( plsqlFunctionToCall == null )
                    {
                        setRowStatusAndMessage(dataRowToValidate, InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE, "Unsupported mode: '" + mode + "'");
                        continue;
                    }

                    List<PLSQLFunctionParameter> plsqlFunctionParameters = getDataRow(apiDataGridController.getDataGridInventoryColumns(), dataRowToValidate);
                    String plsqlFullFunctionNameToCall = apiInjectorsPackage.getName() + "." + plsqlFunctionToCall;
                    //System.err.println("About to callPLSQLFunction = plsqlFullFunctionNameToCall, thread id = " + Thread.currentThread().getId());

                    savePoint = connection.setSavepoint("save" + System.nanoTime());
                    String result = DatabaseUtils.callPLSQLFunction(connection, plsqlFullFunctionNameToCall, plsqlFunctionParameters,dbmsOutput);

                    String status = result;
                    String errorId = "";
                    String message = result;
                    if ( result != null && result.indexOf(InventoryDataGridConstants.ERROR_ID_BRACKETING_DELIMITER_IN_RETURN_VALUE) != -1 )
                    {
                        int delimiterCount = StringUtils.countMatches(result, InventoryDataGridConstants.ERROR_ID_BRACKETING_DELIMITER_IN_RETURN_VALUE);
                        if ( 2 != delimiterCount )
                        {
                            throw new Error("Malformed response from PL/SQL; expected two instances of " + InventoryDataGridConstants.ERROR_ID_BRACKETING_DELIMITER_IN_RETURN_VALUE
                                    + ", but found " + delimiterCount + ".  Response was: \"" + result + "\"");
                        }
                        String[] tokens = result.split(InventoryDataGridConstants.ERROR_ID_BRACKETING_DELIMITER_IN_RETURN_VALUE);
                        int index = 0;
                        status = tokens[index++];
                        errorId = tokens[index++];
                        message = tokens[index++];

                        if ( !apiDataGridController.containsColumnName(InventoryDataGridConstants.ERROR_ID_COLUMN_NAME) )
                        {
                            throw new Exception("Error: API call returned an error ID, but the current inventory does not have the column named: "
                                    + InventoryDataGridConstants.ERROR_ID_COLUMN_NAME);
                        }
                        APIDataGridMigrateThread.this.apiDataGridController.setDataRowValue(InventoryDataGridConstants.ERROR_ID_COLUMN_NAME, errorId, dataRowToValidate);
                        result = status + " " + message;
                    }

                    if ( status != null && status.equalsIgnoreCase("SUCCESS") )
                    {
                        setRowStatusAndMessage(dataRowToValidate, InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE, "");
                    }
                    else
                    {
                        incrementErrorCount(dataRowToValidate, message);
                        rollbackSavepoint(connection, savePoint);
                    }
                    //System.err.println("end of processing block - status = " + status + ", message = " + message);
                    //System.err.println("row = " + Arrays.asList(dataRowToValidate));
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    FileUtils.printStackTrace(e);
                    incrementErrorCount(dataRowToValidate, e.getMessage());
                    rollbackSavepoint(connection, savePoint);
                }
                incrementTotalCount();
            }


            if (dbmsOutput!=null)
            {
                dbmsOutput.close();
            }
            connection.commit();
        }
        catch ( Exception e )
        {
            FileUtils.printStackTrace(e);
        }
        finally
        {
            DirectConnectDao.closeQuietly(connection);
        }

    }


    // ORA-01086: savepoint 'ORACLE_SVPT_1' never established
    private static final int ERROR_SAVEPOINT_NEVER_ESTABLISHED = 1086;
    private static void rollbackSavepoint(Connection connection, Savepoint savePoint)
    {
        if ( null != savePoint )
        {
            try
            {
                connection.rollback(savePoint);
            }
            catch(java.sql.SQLException e)
            {
                //ignore this error, as it shows up if the underlying SQL forced a commit (thus releasing the savepoint)
                if ( e.getErrorCode() != ERROR_SAVEPOINT_NEVER_ESTABLISHED )
                {
                    throw new Error(e);
                }
            }
        }
    }


	public List<String[]> getAllDataGridRowsAfterValidation()
	{
		return allDataGridRows;
	}

	public boolean isProcessCompletedWithErrors()
	{
		return isProcessCompletedWithErrors;
	}

	private synchronized void setDataGridStatusLabel(int errorCount, int totalCounter)
	{
		String message = errorCount + " error(s). ( " + totalCounter + " row(s) processed )";
		if ( !apiDataGridController.isProcessStarted() )
		{
			message = message + ". Manual stop.";
		}
		if ( errorCount == 0 )
		{
			GUIUtils.showSuccessMessage(apiDataGridController.getInventoryDataGridFrame().getStatusLabel(), message);
		}
		else
		{
			isProcessCompletedWithErrors = true;
			GUIUtils.showErrorMessage(apiDataGridController.getInventoryDataGridFrame().getStatusLabel(), message);
		}
	}

	private synchronized void setRowStatusAndMessage(String[] row, String status, String message)
	{
		if ( status.equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE) )
		{
			row[InventoryDataGridConstants.ENABLED_COLUMN_GRID_INDEX] = "false";
		}
		row[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX] = status;
		row[InventoryDataGridConstants.MESSAGE_COLUMN_GRID_INDEX] = message;
	}

	public void setAllDataGridRows(List<String[]> allDataGridRows)
	{
		this.allDataGridRows = allDataGridRows;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getTotalCounter() {
		return totalCounter;
	}

	public int getTotalEnabledRowsCount() {
		return totalEnabledRowsCount;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}