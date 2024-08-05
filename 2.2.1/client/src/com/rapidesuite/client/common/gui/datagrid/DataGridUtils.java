package com.rapidesuite.client.common.gui.datagrid;

import java.awt.Desktop;
import java.io.File;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.openswing.swing.internationalization.java.Resources;
import org.openswing.swing.lookup.client.LookupController;
import org.openswing.swing.table.columns.client.CheckBoxColumn;
import org.openswing.swing.table.columns.client.CodLookupColumn;
import org.openswing.swing.table.columns.client.Column;
import org.openswing.swing.table.columns.client.ComboColumn;
import org.openswing.swing.table.columns.client.DateColumn;
import org.openswing.swing.table.columns.client.DateTimeColumn;
import org.openswing.swing.table.columns.client.DecimalColumn;
import org.openswing.swing.table.columns.client.MultiLineTextColumn;
import org.openswing.swing.table.columns.client.TextColumn;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;

public class DataGridUtils {
	
	public static Column getTextColumn(String columnName,int maxCharacters,boolean isFilterable,
			boolean isRequired,boolean isSortable,boolean isUpdatable,boolean isAutoFit) {
		TextColumn column= new TextColumn();
		setStandardPropertiesOnColumn(column,columnName,isFilterable,isRequired,isSortable,isUpdatable,isAutoFit);
		column.setMaxCharacters(maxCharacters);
		return column;
	}
	
	public static Map<String, String> convertListToMap(List<SimpleEntry<String,String>> keyValuePairs) 
    {
		Map<String,String> toReturn = new HashMap<String,String>();
		for (SimpleEntry<String,String> keyValuePair:keyValuePairs){
			toReturn.put(keyValuePair.getKey(),keyValuePair.getValue());
		}
		return toReturn;
    }
	
	public static Column getMultiLineTextColumn(String columnName,int maxCharacters,boolean isFilterable,
			boolean isRequired,boolean isSortable,boolean isUpdatable,boolean isAutoFit) {
		MultiLineTextColumn column= new MultiLineTextColumn();
		setStandardPropertiesOnColumn(column,columnName,isFilterable,isRequired,isSortable,isUpdatable,isAutoFit);
		column.setMaxCharacters(maxCharacters);
		return column;
	}
	
	public static Column getDecimalColumn(String columnName,int maxCharacters,boolean isFilterable,
			boolean isRequired,boolean isSortable,boolean isUpdatable,int decimals,boolean isAutoFit) {
		DecimalColumn column= new DecimalColumn();
		setStandardPropertiesOnColumn(column,columnName,isFilterable,isRequired,isSortable,isUpdatable,isAutoFit);
		column.setMaxCharacters(maxCharacters);
		column.setDecimals(decimals);
		column.setMinValue(Integer.MIN_VALUE);
		column.setHideZeroDigits(true);
		return column;
	}
	
	public static Column getCheckBoxColumn(String columnName,boolean isFilterable,
			boolean isRequired,boolean isSortable,boolean isUpdatable,boolean isAutoFit) {
		CheckBoxColumn column= new CheckBoxColumn();
		setStandardPropertiesOnColumn(column,columnName,isFilterable,isRequired,isSortable,isUpdatable,isAutoFit);
		return column;
	}
	
	public static Column getDateColumn(String columnName,boolean isFilterable,
			boolean isRequired,boolean isSortable,boolean isUpdatable,boolean isAutoFit) {
		DateColumn column= new DateColumn();
		column.setFormat(Resources.DMY);
		setStandardPropertiesOnColumn(column,columnName,isFilterable,isRequired,isSortable,isUpdatable,isAutoFit);
		return column;
	}
	
	public static Column getDateTimeColumn(String columnName,boolean isFilterable,
			boolean isRequired,boolean isSortable,boolean isUpdatable,boolean isAutoFit) {
		DateTimeColumn column= new DateTimeColumn();
		column.setTimeFormat(Resources.HH_MM_SS);
		column.setFormat(Resources.DMY);
		setStandardPropertiesOnColumn(column,columnName,isFilterable,isRequired,isSortable,isUpdatable,isAutoFit);
		return column;
	}
	
	public static Column getComboColumn(String domainIdPrefix,String columnName,boolean isFilterable,
			boolean isRequired,boolean isSortable,boolean isUpdatable,boolean isAutoFit) {
		ComboColumn column= new ComboColumn();
		setStandardPropertiesOnColumn(column,columnName,isFilterable,isRequired,isSortable,isUpdatable,isAutoFit);
		column.setDomainId(domainIdPrefix+columnName);
		return column;
	}
	
	public static Column getLookupColumn(String columnName,boolean isFilterable,
			boolean isRequired,boolean isSortable,boolean isUpdatable,boolean isAutoFit,LookupController lookupController) {
		CodLookupColumn column= new CodLookupColumn();
		column.setUpperCase(false);
		column.setTrimText(false);
		setStandardPropertiesOnColumn(column,columnName,isFilterable,isRequired,isSortable,isUpdatable,isAutoFit);
		column.setLookupController(lookupController);
		column.setAutoCompletitionWaitTime(-1); 
		return column;
	}
	
	private static void setStandardPropertiesOnColumn(Column column,String columnName,
			boolean isFilterable,boolean isRequired,boolean isSortable,boolean isUpdatable,
			boolean isAutoFit) { 
		column.setColumnFilterable(isFilterable);
		column.setColumnName(columnName);
		column.setColumnRequired(isRequired);
		column.setColumnSortable(isSortable);
		column.setEditableOnEdit(isUpdatable);
		column.setEditableOnInsert(isUpdatable);
		column.setAutoFitColumn(isAutoFit);
		column.setColumnSelectable(false);
		column.setColumnDuplicable(true);
	}
	
	public static void addKeyValuePairsToAttributeNamesToDescriptionMap(
			DataGridComboColumn dataGridColumn,
			Map<String,String> attributeNamesToDescriptionMap) throws Exception {
		List<SimpleEntry<String,String>> keyValuePairs=dataGridColumn.getKeyValuePairs();
		for (SimpleEntry<String,String> keyValuePair:keyValuePairs) {
			if (keyValuePair.getKey()==null) {
				throw new Exception("cannot add empty data key value pair for the column: '"+dataGridColumn.getColumnTitle()+"'");
			}
			attributeNamesToDescriptionMap.put(keyValuePair.getKey(),keyValuePair.getKey());
		}
	}
	
	public static List<Column> getColumns(List<DataGridColumn> dataGridColumns)
	{
		List<Column> res=new ArrayList<Column>();
		for (DataGridColumn dataGridColumn:dataGridColumns) {
			res.add(dataGridColumn.getColumn());
		}
		return res;
	}
	
	public static Map<String,String> getAttributeNamesToColumnTitleMap(List<DataGridColumn> dataGridColumns) throws Exception
	{
		Map<String,String> res=new HashMap<String,String>();
		for (DataGridColumn dataGridColumn:dataGridColumns) {
			if (dataGridColumn.getAttributeName()==null) {
				throw new Exception("cannot add empty data column attribute name");
			}
			if (dataGridColumn.getColumnTitle()==null) {
				throw new Exception("cannot add empty data column title");
			}
			res.put(dataGridColumn.getAttributeName(),dataGridColumn.getColumnTitle());
			if (dataGridColumn instanceof DataGridComboColumn) {
				addKeyValuePairsToAttributeNamesToDescriptionMap((DataGridComboColumn)dataGridColumn,res);
			}
			else 
			if (dataGridColumn.getColumn() instanceof CodLookupColumn) {
				//String domainLookup=dataGridColumn.getAttributeDescription()+" Lookup"+randomNumber;
				List<String> columnDescriptions=dataGridColumn.getDataGridLookupController().getColumnDescriptions();
				for (String columnDescription:columnDescriptions) {
					if (columnDescription==null) {
						throw new Exception("cannot add empty data column description");
					}
					res.put(columnDescription,columnDescription);
				}
				//System.out.println("getAttributeNamesToColumnTitleMap : "+domainLookup);
				//res.put(domainLookup,domainLookup);
			}
		}
		return res;
	}
	
	public static Map<String,List<SimpleEntry<String,String>>> getAttributeNamesToKeyValuePairsMap(
			List<DataGridColumn> dataGridColumns)
	{
		Map<String,List<SimpleEntry<String,String>>> res=new HashMap<String,List<SimpleEntry<String,String>>>();
		for (DataGridColumn dataGridColumn:dataGridColumns) {
			if (dataGridColumn instanceof DataGridComboColumn) {
				res.put(dataGridColumn.getAttributeName(),
						((DataGridComboColumn)dataGridColumn).getKeyValuePairs());
			}
		}
		return res;
	}
	
	public static Map<String,DataGridColumn> getDataGridColumnAttributeNameToDataGridColumnMap(List<DataGridColumn> dataGridColumns)
	{
		Map<String,DataGridColumn> res=new HashMap<String,DataGridColumn>();
		for (DataGridColumn dataGridColumn:dataGridColumns) {
			res.put(dataGridColumn.getAttributeName(),dataGridColumn);
		}
		return res;
	}
	
	public static Map<String,DataGridColumn> getDataGridColumnAttributeDescriptionToDataGridColumnMap(List<DataGridColumn> dataGridColumns)
	{
		Map<String,DataGridColumn> res=new HashMap<String,DataGridColumn>();
		for (DataGridColumn dataGridColumn:dataGridColumns) {
			res.put(dataGridColumn.getAttributeDescription(),dataGridColumn);
		}
		return res;
	}
	
	public static Map<String,Integer> getDataGridColumnAttributeDescriptionToDataPositionMap(List<DataGridColumn> dataGridColumns)
	{
		Map<String,Integer> res=new HashMap<String,Integer>();
		int counter=0;
		for (DataGridColumn dataGridColumn:dataGridColumns) {
			res.put(dataGridColumn.getAttributeDescription(),counter++);
		}
		return res;
	}
	
	public static boolean include(
			String filterOperator,
			Object filterValue,
			String sourceValue,
			boolean isBooleanColumn) 
	{
		if (isOperatorToIgnore(filterOperator)) {
			return false;
		}
		if (isBooleanColumn) {
			return applyCheckBoxOperator(filterOperator,filterValue,sourceValue);	
		}
		return applyTextOperator(filterOperator,filterValue,sourceValue);
	}
	
	public static boolean isOperatorToIgnore(String filterOperator) 
	{
		return 	filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_LESS_OR_EQUALS_TO) ||
					filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_LESS_THAN)||
					filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_GREATER_OR_EQUALS_TO)||
					filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_GREATER_THAN);
	}
	
	public static boolean applyNumberOperator(
			String filterOperator,
			Object filterValueParameter,
			Object sourceValueParameter) 
	{
		BigDecimal filterValue=(BigDecimal)filterValueParameter;
		BigDecimal sourceValue=(BigDecimal)sourceValueParameter;
		//System.out.println("filterValue:"+filterValue.longValue());
		//System.out.println("dataGridRowValue:"+dataGridRowValue.longValue());
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_EQUALS_TO)) {
			return filterValue.longValue()==sourceValue.longValue();
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_NOT_EQUALS_TO)) {
			return filterValue.longValue()!=sourceValue.longValue();
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_GREATER_OR_EQUALS_TO)) {
			return sourceValue.longValue()>=filterValue.longValue();
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_GREATER_THAN)) {
			return sourceValue.longValue()>filterValue.longValue();
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_LESS_OR_EQUALS_TO)) {
			return sourceValue.longValue()<=filterValue.longValue();
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_LESS_THAN)) {
			return sourceValue.longValue()<filterValue.longValue();
		}
		return false;
	}
	
	public static boolean applyTextOperator(
			String filterOperator,
			Object filterValueParameter,
			String sourceValueParameter) 
	{
		String filterValue=(String)filterValueParameter;
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_IS_FILLED)) {
			return sourceValueParameter!=null && !sourceValueParameter.isEmpty();
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_IS_NOT_FILLED)) {
			return sourceValueParameter==null || sourceValueParameter.isEmpty();
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_EQUALS_TO)) {
			return sourceValueParameter.toLowerCase().equals(filterValue.toLowerCase());
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_NOT_EQUALS_TO)) {
			return !sourceValueParameter.toLowerCase().equals(filterValue.toLowerCase());
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_CONTAINS)) {
			return sourceValueParameter.toLowerCase().indexOf(filterValue.toLowerCase())!=-1;
		}
		
		return false;
	}
	
	public static boolean applyCheckBoxOperator(
			String filterOperator,
			Object filterValueParameter,
			String sourceValueParameter) 
	{
		Boolean filterValue=(Boolean)filterValueParameter;
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_EQUALS_TO)) {
			return Boolean.valueOf(sourceValueParameter).booleanValue()==filterValue.booleanValue();
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_NOT_EQUALS_TO)) {
			return !Boolean.valueOf(sourceValueParameter).booleanValue()==filterValue.booleanValue();
		}
		return false;
	}
	
	public static boolean applyDateOperator(
			String filterOperator,
			Object filterValueParameter,
			Object sourceValueParameter) 
	{
		java.sql.Date filterValue=(java.sql.Date)filterValueParameter;
		java.sql.Date sourceValue=(java.sql.Date)sourceValueParameter;
				
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_IS_FILLED)) {
			return sourceValue!=null;
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_IS_NOT_FILLED)) {
			return sourceValue==null;
		}
		if (sourceValue==null) {
			return false;
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_EQUALS_TO)) {
			return sourceValue.equals(filterValue);
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_NOT_EQUALS_TO)) {
			return !sourceValue.equals(filterValue);
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_GREATER_OR_EQUALS_TO)) {
			return  sourceValue.equals(filterValue)||
					sourceValue.after(filterValue);
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_GREATER_THAN)) {
			return sourceValue.after(filterValue);
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_LESS_OR_EQUALS_TO)) {
			return  sourceValue.equals(filterValue)||
					sourceValue.before(filterValue);
		}
		if (filterOperator.equalsIgnoreCase(DataGridConstants.FILTER_LESS_THAN)) {
			return sourceValue.before(filterValue);
		}
		
		return false;
	}
	
	public static void doCreateAndSaveXLSXExcelFile(final List<String[]> dataGridRows, final File outputFile, final boolean openFileAfterCreation, boolean showFileCreationSuccessfulMessage) throws Exception {
		final File outputFolder = outputFile.getParentFile();
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		FileUtils.createAndSaveRowsToXLSXFile("Execution Status grid",outputFile,dataGridRows);
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
	
	public static File askAndCreateAndSaveXLSXExcelFile(List<String[]> dataGridRows,String fileName,boolean isOpenFile, boolean showFileCreationSuccessfulMessage) throws Exception
	{
		JFileChooser fileChooser=Utils.initializeJFileChooserWithTheLastPath("FILE_CHOOSER_DATA_GRID_UTILS");
	    FileSystemView fw = fileChooser.getFileSystemView();
	    File outputFile=new File(fw.getDefaultDirectory(),fileName);
		fileChooser.setSelectedFile(outputFile);

		int returnVal = fileChooser.showDialog(null,"Save file");
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			outputFile = fileChooser.getSelectedFile();
			doCreateAndSaveXLSXExcelFile(dataGridRows, outputFile, isOpenFile, showFileCreationSuccessfulMessage);
		}
		return outputFile;
	}

}
