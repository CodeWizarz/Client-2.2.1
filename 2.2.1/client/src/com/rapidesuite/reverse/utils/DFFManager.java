/**************************************************
 * $Revision: 59033 $:
 * $Author: hassan.jamil $:
 * $Date: 2016-10-18 14:46:24 +0700 (Tue, 18 Oct 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/utils/DFFManager.java $:
 * $Id: DFFManager.java 59033 2016-10-18 07:46:24Z hassan.jamil $:
 */
package com.rapidesuite.reverse.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.util.FileUtils;

public class DFFManager
{

	private Job worker;
	private Map<String,Integer> dffColumnNamesToPositionMap;
	private List<String> dffColumnNamesList;
	
	private List<DFFRow> globalDFFRows;
	private Map<String,List<DFFRow>> contextCodeToDFFRowsMap;
	private Map<String,Map<String,String>> valueSetQueryToDataMap;
	
	private static final String CONTEXT_CODE_COLUMN_NAME="CONTEXT_CODE"; 
	
	public DFFManager(Job worker){
		this.worker=worker;
		valueSetQueryToDataMap=new HashMap<String,Map<String,String>>();
	}
	
	public void loadDFFTable(Map<String,String> environmentProperties,String query) 
	throws Exception
	{
		List<DFFRow> allDFFRows=DataExtractionDatabaseUtils.getDFFRows(environmentProperties,query);
		contextCodeToDFFRowsMap=new TreeMap<String,List<DFFRow>>(String.CASE_INSENSITIVE_ORDER);
		globalDFFRows=new ArrayList<DFFRow>();
		for (int i=0;i<allDFFRows.size();i++) {
			DFFRow dffRow=allDFFRows.get(i);
			
			if (dffRow.isGlobal()) {
				globalDFFRows.add(i,dffRow);
			}
			else {
				if (dffRow.getContextCode()!=null) {
					List<DFFRow> rows=contextCodeToDFFRowsMap.get(dffRow.getContextCode());
					if (rows==null) {
						rows=new ArrayList<DFFRow>();
						contextCodeToDFFRowsMap.put(dffRow.getContextCode(),rows);
					}
					rows.add(dffRow);
				}
			}	
		}
	}

	public List<String[]> processRowsForDFF(List<String[]> rows) throws Exception
	{
		List<String[]> rowsForDFF=new ArrayList<String[]>();
		for (String[] row:rows) {
			String[] rowForDFF=processRowForDFF(row);
			if (rowForDFF!=null) {
				rowsForDFF.add(rowForDFF);
			}
		}
		return rowsForDFF;
	}
	
	private String[] processRowForDFF(String[] row) throws Exception
	{
		//System.out.println("NEW ROW ######################");
		int inventoryFieldsCount=worker.getInventory().getFieldsUsedForDataEntry().size();
		// adding +1 for the navigation mapper.
		String[] rowForDFF=new String[inventoryFieldsCount+1];
		boolean hasAtLeastOneValue=false;
		/*
		 * Processing all parent/ child elements first.
		 */
		int inventoryDFFFieldIndex=0;
		int contextCodeColumnIndex=getContextCodeColumnIndex();
		for (int i=0;i<contextCodeColumnIndex;i++) {	
			rowForDFF[inventoryDFFFieldIndex]=row[inventoryDFFFieldIndex];
			//System.out.println(inventoryDFFFieldIndex+" pc:"+row[inventoryDFFFieldIndex]);
			inventoryDFFFieldIndex++;
		}

		/*
		 * Processing all GLOBAL elements.
		 */
		for (int i=0;i<globalDFFRows.size();i++) {
			DFFRow globalDFFRow=globalDFFRows.get(i);
			String columnName=globalDFFRow.getColumnName();
			if (columnName==null) {
				throw new Exception("GLOBAL column name missing for element # "+(i+1));
			}
			Integer columnIndex=dffColumnNamesToPositionMap.get(columnName);
			if (columnIndex==null) {
				throw new Exception("GLOBAL column missing in the DFF Query for column name: "+columnName);
			}
			String value=row[columnIndex];
			if (globalDFFRow.hasValueSetQuery()) {
				if(globalDFFRow.valueSetQueryContainsPlaceHolders()) {
					FileUtils.println("WARNING: valueSetQuery has place-holders. This is not a blocker, but should be resolved. Look into the REGEXes in DFFRow class and fix them or add new");
					FileUtils.println(globalDFFRow.getValueSetQuery());
				}
				Map<String,String> valueSetCodeToDisplayNameMap=getDataForValueSetQuery(globalDFFRow.getValueSetQuery());
				if (valueSetCodeToDisplayNameMap!=null) {
					value=valueSetCodeToDisplayNameMap.get(value);
				}
			}
			rowForDFF[inventoryDFFFieldIndex]=value;
			//System.out.println(inventoryDFFFieldIndex+" global:"+value);
			inventoryDFFFieldIndex++;
			if (value!=null && !value.isEmpty()) {
				hasAtLeastOneValue=true;
			}
		}
		
		/*
		 * Processing the CONTEXT elements.
		 */
		String contextCodeValue=row[contextCodeColumnIndex];
		List<DFFRow> contextValueDFFRows=contextCodeToDFFRowsMap.get(contextCodeValue);
		if (contextValueDFFRows!=null) {
			if (!contextValueDFFRows.isEmpty()) {
				DFFRow dffRow=contextValueDFFRows.get(0);
				if (dffRow.isContextCodeToDisplay()) {
					rowForDFF[inventoryDFFFieldIndex]=dffRow.getContextDisplayName();
					//System.out.println(inventoryDFFFieldIndex+" context Code:"+dffRow.getContextDisplayName());
					inventoryDFFFieldIndex++;
					if (contextCodeValue!=null && !contextCodeValue.isEmpty()) {
						hasAtLeastOneValue=true;
					}
				}
			}
			for (int i=0;i<contextValueDFFRows.size();i++) {
				DFFRow contextValueDFFRow=contextValueDFFRows.get(i);
				String columnName=contextValueDFFRow.getColumnName();
				if (columnName==null) {
					throw new Exception("CONTEXT column name missing for element # "+(i+1));
				}
				
				Integer columnIndex=dffColumnNamesToPositionMap.get(columnName);
				if (columnIndex==null) {
					throw new Exception("CONTEXT column missing in the DFF Query for column name: "+columnName);
				}
				String value=row[columnIndex];
				if (contextValueDFFRow.hasValueSetQuery()) {
					Map<String,String> valueSetCodeToDisplayNameMap=getDataForValueSetQuery(contextValueDFFRow.getValueSetQuery());
					if (valueSetCodeToDisplayNameMap!=null) {
						value=valueSetCodeToDisplayNameMap.get(value);
					}
				}
				/*
				 * Stop when we reached the number of allowed columns in the inventory; as more context rows
				 * can be defined than what we have in our inventory.
				 */
				if ( (inventoryDFFFieldIndex+1) >= inventoryFieldsCount) {
					continue;
				}
				rowForDFF[inventoryDFFFieldIndex]=value;
				//System.out.println(inventoryDFFFieldIndex+" context value columnName:"+columnName+" value:"+value);
				inventoryDFFFieldIndex++;
				if (value!=null && !value.isEmpty()) {
					hasAtLeastOneValue=true;
				}
			}
		}
		
		if (!hasAtLeastOneValue) {
			return null;
		}
		rowForDFF[inventoryFieldsCount]="N/A";
		
		return rowForDFF;
	}
	
	private int getContextCodeColumnIndex() throws Exception
	{
		Integer contextCodeColumnIndex=dffColumnNamesToPositionMap.get(CONTEXT_CODE_COLUMN_NAME);
		if (contextCodeColumnIndex==null) {
			throw new Exception("incorrect SQL defined for DFF, no '"+CONTEXT_CODE_COLUMN_NAME+"' column found.");
		}
		return contextCodeColumnIndex;
	}
	
	public void initializeDFFColumnNamesCollection(ResultSet resultSet) throws SQLException
	{
		dffColumnNamesToPositionMap=new HashMap<String,Integer>();
		dffColumnNamesList=new ArrayList<String>();
		ResultSetMetaData rsMetaData = resultSet.getMetaData();
		int numberOfColumnsInResultSet=rsMetaData.getColumnCount();
		for ( int i = 0; i < numberOfColumnsInResultSet; i++ )
		{
			String colName=rsMetaData.getColumnName(i+1);
			dffColumnNamesToPositionMap.put(colName,i);
			dffColumnNamesList.add(colName);
		}
	}
	
	private Map<String,String> getDataForValueSetQuery(String valueSetQuery) {
		// Load the Data for the value set query if not already done.
		Map<String,String> valueSetCodeToDisplayNameMap=valueSetQueryToDataMap.get(valueSetQuery);
		if (valueSetCodeToDisplayNameMap==null) {
			valueSetCodeToDisplayNameMap=DataExtractionDatabaseUtils.getDataForValueSetQuery(worker.getBweProperties(),valueSetQuery);
			valueSetQueryToDataMap.put(valueSetQuery,valueSetCodeToDisplayNameMap);
		}
		return valueSetCodeToDisplayNameMap;
	}
	
}