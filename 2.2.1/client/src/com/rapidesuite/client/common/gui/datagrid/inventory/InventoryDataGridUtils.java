package com.rapidesuite.client.common.gui.datagrid.inventory;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.openswing.swing.export.java.ExportOptions;
import org.openswing.swing.table.columns.client.Column;
import org.openswing.swing.util.client.ClientSettings;

import com.erapidsuite.configurator.navigation0003.NavigationConstants;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.client.common.XmlDataParser;
import com.rapidesuite.client.common.gui.datagrid.DataGridColumn;
import com.rapidesuite.client.common.gui.datagrid.DataGridComboColumn;
import com.rapidesuite.client.common.gui.datagrid.DataGridLookupController;
import com.rapidesuite.client.common.gui.datagrid.DataGridUtils;
import com.rapidesuite.client.common.gui.datagrid.ListExternalizable;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.DataFactory;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.domain.DataRow;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.core.inventory0007.FldFormFieldData;

public class InventoryDataGridUtils {

	/*
	 *  We need to increase the column number otherwise it will conflict when multiple grids 
	 *  are opened at the same time.
	 */
	private static int inventoryColumnDataGridColumnIndex=0;
	
	public static synchronized int increment() {
        return inventoryColumnDataGridColumnIndex++;
    }

	public static List<DataGridColumn> getControlDataGridColumns(String domainIdPrefix)
	{
		List<DataGridColumn> res=new ArrayList<DataGridColumn>();
		
		String attributeName=InventoryDataGridConstants.EXTRA_COLUMN_ROW_SEQUENCE_ATTRIBUTE_NAME;
		String attributeDescription=InventoryDataGridConstants.EXTRA_COLUMN_ROW_SEQUENCE_ATTRIBUTE_DESCRIPTION;
		Class<?> attributeClass=java.math.BigDecimal.class;
		Column column=DataGridUtils.getDecimalColumn(attributeName,-1,false,false,false,false,0,true);
		column.setTextAlignment(SwingConstants.CENTER);
		DataGridColumn dataGridColumn=new DataGridColumn(attributeName,attributeDescription,attributeClass,column,false,false,false,false);
		res.add(dataGridColumn);
		
		attributeName=InventoryDataGridConstants.EXTRA_COLUMN_ENABLED_ATTRIBUTE_NAME;
		attributeDescription=InventoryDataGridConstants.EXTRA_COLUMN_ENABLED_ATTRIBUTE_DESCRIPTION;
		attributeClass=Boolean.class;
		column=DataGridUtils.getCheckBoxColumn(attributeName,true,false,false,true,false);
		column.setMinWidth(80);
		column.setMaxWidth(80);
		dataGridColumn=new DataGridColumn(attributeName,attributeDescription,attributeClass,column,false,true,false,false);
		res.add(dataGridColumn);
		
		attributeName=InventoryDataGridConstants.EXTRA_COLUMN_STATUS_ATTRIBUTE_NAME;
		attributeDescription=InventoryDataGridConstants.EXTRA_COLUMN_STATUS_ATTRIBUTE_DESCRIPTION;
		attributeClass=String.class;
		column=DataGridUtils.getComboColumn(domainIdPrefix,attributeName,true,false,false,false,false);
		column.setMinWidth(70);
		column.setMaxWidth(70);
		List<SimpleEntry<String,String>> keyValuePairs =new ArrayList<SimpleEntry<String,String>>();
		SimpleEntry<String,String> se=new SimpleEntry<String,String>(InventoryDataGridConstants.STATUS_COLUMN_PENDING_VALUE,InventoryDataGridConstants.STATUS_COLUMN_PENDING_VALUE);
		keyValuePairs.add(se);
		se=new SimpleEntry<String,String>(InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE,InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE);
		keyValuePairs.add(se);
		se=new SimpleEntry<String,String>(InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE,InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE);
		keyValuePairs.add(se);
		dataGridColumn=new DataGridComboColumn(attributeName,attributeDescription,attributeClass,column,keyValuePairs,false,-1,false,false,false);
		res.add(dataGridColumn);
		
		attributeName=InventoryDataGridConstants.EXTRA_COLUMN_MESSAGE_ATTRIBUTE_NAME;
		attributeDescription=InventoryDataGridConstants.EXTRA_COLUMN_MESSAGE_ATTRIBUTE_DESCRIPTION;
		attributeClass=String.class;
		column=DataGridUtils.getTextColumn(attributeName,4000,true,false,false,false,false);
		column.setMinWidth(150);
		column.setPreferredWidth(150);
		column.setMaxWidth(150);
		dataGridColumn=new DataGridColumn(attributeName,attributeDescription,attributeClass,column,false,false,false,false);
		res.add(dataGridColumn);
		
		return res;
	}
	
	public static void initClientSettings() {		
		ClientSettings.ALLOW_OR_OPERATOR = false;
		/** define if IN/NOT IN operator must be included in quick filter and filter panel; default value: <code>true</code> */
		ClientSettings.INCLUDE_IN_OPERATOR = false;
		/** border color of an editable grid cell */
		ClientSettings.GRID_EDITABLE_CELL_BACKGROUND = Color.white;
		/** border color of the grid that currently has the focus */
		ClientSettings.GRID_FOCUS_BORDER = Color.black;
		/** define whether showing filtering symbol in column header which is currently filtered */
		ClientSettings.SHOW_FILTER_SYMBOL = true;
		/** height of grid rows */
		ClientSettings.CELL_HEIGHT = 19;
		/** height of grid headers */
		ClientSettings.HEADER_HEIGHT = 60;
		/** maximum number of exportable rows */
		ClientSettings.MAX_EXPORTABLE_ROWS = 100000;
		/** flag used to add a filter panel on top of the exported grid, in order to show filtering conditions; this pane is visible only whether there is at least one filtering condition applied; default value: <code>false</code> */
		ClientSettings.SHOW_FILTERING_CONDITIONS_IN_EXPORT = false;
		/** default document formats when exporting the grid content */
		ClientSettings.EXPORTING_FORMATS = new String[]{ExportOptions.XML_FORMAT};
		/** define if must be show the mandatory symbol "*" in mandatory input controls */
		ClientSettings.VIEW_MANDATORY_SYMBOL = true;
		/** define if must be set a background color on focusing an input control */
		ClientSettings.VIEW_BACKGROUND_SEL_COLOR = true;
		/** color to set as background in the focused input control (only if VIEW_BACKGROUND_SEL_COLOR is set to <code>true</code>) */
		ClientSettings.BACKGROUND_SEL_COLOR = ClientSettings.GRID_ACTIVE_CELL_BACKGROUND;
		/** define if showing the status panel on bottom of the lookup grid; default value: <code>false</code> */
		ClientSettings.VISIBLE_STATUS_PANEL = true;
		/** flag used to auto fit column sizes, according to text headers; default value: <code>false</code> */
		ClientSettings.AUTO_FIT_COLUMNS = true;
		/** define the behavior of numeric components in case of zero decimal digits: show or hide zeros; default value: <code>false</code>, i.e. shows zero digits */
		ClientSettings.HIDE_ZERO_DIGITS = true;
		/** define where new rows must be added: true at the top of the grid or false at the bottom; default value: true */
		ClientSettings.INSERT_ROWS_ON_TOP =false;
		
		ClientSettings.SELECT_DATA_IN_EDITABLE_GRID  = true;
		ClientSettings.DISABLED_INPUT_CONTROLS_FOCUSABLE  = true;
		ClientSettings.FIRST_CELL_RECEIVE_FOCUS =false;
		ClientSettings.LOOKUP_AUTO_COMPLETITION_WAIT_TIME =2000;
		
		ClientSettings.COPY_BUTTON_KEY=KeyStroke.getKeyStroke("alt shift released P");
	}
	
	public static synchronized List<DataGridColumn> getInventoryDataGridColumns(
			InventoryDataGridController inventoryDataGridController,Inventory inventory,String domainIdPrefix,boolean isRequiredInformationOn) throws Exception
	{
		List<DataGridColumn> res=new ArrayList<DataGridColumn>();
		List<Field> fields=inventory.getFieldsUsedForDataEntry();
		inventory.initSubstitution();
		int colWidth=110;
		for (int i=0;i<fields.size();i++) {
			Field field=fields.get(i);

			/*
			 * Due to OpenSwing having a weird behavior about Uppercase for getter/setter.
			 * we need to tweak to use a Number for the getter/setter. Example: get0() set0() ...
			 */
			String attributeName=Integer.valueOf(increment()).toString();
			String attributeDescription=field.getName();
			boolean isPCColumn=field.getParentName()!=null && !field.getParentName().trim().isEmpty();
			Class<?> attributeClass=null;
			Column column=null;
			DataGridColumn dataGridColumn=null;

			int fieldMaxSize=field.getMaxSize();
			if (fieldMaxSize<=0) {
				fieldMaxSize=4000;
			}
			boolean isFieldMandatory=isRequiredInformationOn && field.getMandatory();

			FldFormFieldData fldFieldData=field.getFldFormFieldData();
			boolean isUpdatable=true;
			boolean isResetable=false;
			String fieldType="string";
			if (fldFieldData!=null) {
				isUpdatable=fldFieldData.getUpdatable();
				isResetable=fldFieldData.getQueryable();
				fieldType=fldFieldData.getOracleFieldType();
			}
						
			//System.out.println(" field.getName():"+field.getName()+" fieldType:"+fieldType+
			//		" fieldMaxSize:"+fieldMaxSize+" isFieldMandatory:"+isFieldMandatory);
			//System.out.println("validationRulesXMLManager:"+validationRulesXMLManager);
			DataGridLookupController dataGridLookupController=null;
			if (inventoryDataGridController!=null) {
				dataGridLookupController=inventoryDataGridController.getDataGridLookupController(attributeName,field);
			}
			
			if (dataGridLookupController!=null) {
				attributeClass=String.class;
				column=DataGridUtils.getLookupColumn(attributeName,true,isFieldMandatory,false,true,false,dataGridLookupController);
				dataGridColumn=new DataGridColumn(attributeName,attributeDescription,attributeClass,column,isFieldMandatory,(i+1),isUpdatable,isResetable,isPCColumn);
				dataGridColumn.setDataGridLookupController(dataGridLookupController);
				res.add(dataGridColumn);
				column.setPreferredWidth(colWidth);
				column.setMinWidth(colWidth);
				continue;
			}
			if (field.getSubstitution()!=null && !field.getSubstitution().trim().isEmpty()) {
				attributeClass=String.class;
				List<SimpleEntry<String,String>> keyValuePairs=parseSubstitution(field.getSubstitution());
				column=DataGridUtils.getComboColumn(domainIdPrefix,attributeName,true,false,false,true,false);
				dataGridColumn=new DataGridComboColumn(attributeName,attributeDescription,attributeClass,
						column,keyValuePairs,isFieldMandatory,(i+1),isUpdatable,isResetable,false);
			}
			else
					if (fieldType.equalsIgnoreCase("string") || fieldType.equalsIgnoreCase("internal")
							|| fieldType.equalsIgnoreCase("radio button")
							|| fieldType.equalsIgnoreCase("DFF")
							|| fieldType.equalsIgnoreCase("dff-text")
							|| fieldType.equalsIgnoreCase("list item")
							|| fieldType.equalsIgnoreCase("datetime")
							|| fieldType.equalsIgnoreCase("date")
							|| fieldType.equalsIgnoreCase("number")
					) {
						attributeClass=String.class;
						column=DataGridUtils.getTextColumn(attributeName,fieldMaxSize,true,false,false,true,false);
					}
					else
						if (fieldType.equalsIgnoreCase("checkbox") ||
								fieldType.equalsIgnoreCase("check box")) {
							attributeClass=Boolean.class;
							column=DataGridUtils.getCheckBoxColumn(attributeName,true,false,false,true,false);
						}
						else {
							throw new Exception("Unsupported field type: '"+fieldType+"' field name: '"+field.getName()+"'");
						}

			if (column==null) {
				throw new Exception("Internal error, unsupported field: '"+field.getName()+"'");
			}
			column.setPreferredWidth(colWidth);
			column.setMinWidth(colWidth);
			if (dataGridColumn==null) {
				dataGridColumn=new DataGridColumn(attributeName,attributeDescription,attributeClass,column,isFieldMandatory,(i+1),isUpdatable,isResetable,isPCColumn);
			}

			res.add(dataGridColumn);
		}
		return res;
	}
	
	private static List<SimpleEntry<String,String>> parseSubstitution(String substitution) 
	{
		String temp = substitution;
		int endIndex = temp.indexOf(NavigationConstants.NAVIGATION_SUBSTITUTION_SEPARATOR);
		String pair = null;
		List<SimpleEntry<String,String>> toReturn =new ArrayList<SimpleEntry<String,String>>();
		while ( endIndex != -1 )
		{
			pair = temp.substring(0, endIndex);
			temp = temp.substring(endIndex + 3);
			final int index = pair.indexOf("=");
			final String from = pair.substring(0, index);
			final String to = pair.substring(index + 1);
			SimpleEntry<String,String> se=new SimpleEntry<String,String>(from, to);
			toReturn.add(se);
			endIndex = temp.indexOf(NavigationConstants.NAVIGATION_SUBSTITUTION_SEPARATOR);
		}
		// last pair:

		final int index = temp.indexOf("=");
		if ( index != -1 )
		{
			final String from = temp.substring(0, index);
			final String to = temp.substring(index + 1);
			SimpleEntry<String,String> se=new SimpleEntry<String,String>(from, to);
			toReturn.add(se);
		}
		return toReturn;
	}
	
	public static DataGridColumn getDataGridNavigationComboColumn(List<String> list,String domainIdPrefix)
	throws Exception
	{
		String attributeName=InventoryDataGridConstants.EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_NAME;
		String attributeDescription=InventoryDataGridConstants.EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_DESCRIPTION;
		Class<?> attributeClass=String.class;
		Column column=DataGridUtils.getComboColumn(domainIdPrefix,attributeName,true,false,false,true,false);
		column.setMinWidth(300);
		column.setPreferredWidth(300);
		List<SimpleEntry<String,String>> keyValuePairs =new ArrayList<SimpleEntry<String,String>>();
		for (String navigationName:list){
			SimpleEntry<String,String> se=new SimpleEntry<String,String>(navigationName.toLowerCase(),navigationName.toLowerCase());
			keyValuePairs.add(se);
		}
		return new DataGridComboColumn(attributeName,attributeDescription,attributeClass,column,keyValuePairs,false,-1,false,false,false);
	}
	
	public static DataGridColumn getDataGridLabelColumn()
	throws Exception
	{
		String attributeName=InventoryDataGridConstants.EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_NAME;
		String attributeDescription=InventoryDataGridConstants.EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_DESCRIPTION;
		Class<String> attributeClass=String.class;
		Column column=DataGridUtils.getTextColumn(attributeName,4000,true,false,false,false,false);
		column.setMinWidth(100);
		column.setPreferredWidth(100);
		//column.setMaxWidth(100);
		return new DataGridColumn(attributeName,attributeDescription,attributeClass,column,false,false,false,false);
	}
	
	public static void saveInventoryDataToXMLFile(
			File file,
			List<String> headerColumnsList,
			Set<String> mapperNames,
			String tableName,
			List<String[]> dataRows) throws Exception {
		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter(file, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
			List<DataRow> data=new ArrayList<DataRow>();
			for (String[] row:dataRows) {
				DataRow dr=new DataRow(row);
				data.add(dr);
			}
			DataFactory.exportRSCXML(headerColumnsList,data,tableName,new ArrayList<String>(mapperNames),pw);
		}
		finally	{
			IOUtils.closeQuietly(pw);
		}
	}
	
	public static List<String[]> parseInventoryDataRows(InputStream inputStream,int allDataGridColumnsCount,boolean isConvertDataRowsToDataGridRows) throws Exception
	{
		InventoryDataGridLoadThread inventoryDataGridLoadThread=new InventoryDataGridLoadThread(allDataGridColumnsCount,inputStream,isConvertDataRowsToDataGridRows);
		inventoryDataGridLoadThread.start();
		while(!inventoryDataGridLoadThread.isProcessingCompleted()) {
			com.rapidesuite.client.common.util.Utils.sleep(500);
		}
		return inventoryDataGridLoadThread.getDataRows();
	}
	
	public static List<String> getColumnAttributeDescriptions(List<DataGridColumn> dataGridColumns)
	{
		List<String> toReturn=new ArrayList<String>();
		for (DataGridColumn dataGridColumn:dataGridColumns) {
			toReturn.add(dataGridColumn.getAttributeDescription());
		}
		return toReturn;
	}
	
	public static List<String[]> convertDataRowsToDataGridRows(List<String[]> dataRows,int allDataGridColumnsCount){
		List<String[]> toReturn=new ArrayList<String[]>();
		for (int i=0;i<dataRows.size();i++) {
			String[] xmlRow=dataRows.get(i);
			String[] dataRow=new String[allDataGridColumnsCount];
			int counter=0;
			dataRow[counter++]="";
			dataRow[counter++]="true";
			dataRow[counter++]=InventoryDataGridConstants.STATUS_COLUMN_PENDING_VALUE;
			dataRow[counter++]="";
			
			for (int j=0;j< xmlRow.length;j++) {
				dataRow[counter++]=xmlRow[j];
			}
			toReturn.add(dataRow);	
		}
		return toReturn;
	}
	
	public static Map<String,List<String[]>> getNavigationToDataGridRowsMap(List<String[]> dataGridRows,String overrideLabel) {
		Map<String,List<String[]>> toReturn=new TreeMap<String,List<String[]>>(String.CASE_INSENSITIVE_ORDER);
		if (dataGridRows==null) {
			return toReturn;
		}
		for (int i=0;i<dataGridRows.size();i++) {
			String[] dataGridRow=dataGridRows.get(i);
			String navigationMapper=dataGridRow[dataGridRow.length-1];
			if (navigationMapper==null || navigationMapper.isEmpty()) {
				navigationMapper=InventoryDataGridConstants.NAVIGATION_NA_VALUE;
			}
			dataGridRow[dataGridRow.length-1]=navigationMapper.toLowerCase();
			if (overrideLabel!=null && !overrideLabel.isEmpty()) {
				dataGridRow[dataGridRow.length-2]=overrideLabel;
			}
			List<String[]> list=toReturn.get(navigationMapper);
			if (list==null) {
				list=new ArrayList<String[]>();	
				toReturn.put(navigationMapper.toLowerCase(),list);	
			}
			list.add(dataGridRow);	
		}
		return toReturn;
	}
	
	public static List<String[]> parseInventoryDataRows(InputStream inputStream) throws Exception
	{
		try{
			XmlDataParser xmlDataParser=getXMLParserAfterParsingInventoryDataRows(inputStream);
			return xmlDataParser.getRowList();
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
		
	public static XmlDataParser getXMLParserAfterParsingInventoryDataRows(InputStream inputStream) throws Exception
	{
		try{
			XmlDataParser xmlDataParser= new XmlDataParser(false);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(inputStream, xmlDataParser);
			return xmlDataParser;
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
		
	public static int getColumnIndex(List<String> headerList,String columnName) {
		for (int i=0;i<headerList.size();i++) {
			String column=headerList.get(i);
			if (column.equalsIgnoreCase(columnName)) {
				return i;
			}
		}
		return -1;
	}
	
	public static List<String[]> parseInventoryDataRows(InputStream inputStream,String tableName,int allDataGridColumnsCount,String overrideLabel) throws Exception
	{
		try{
			XmlDataParser xmlDataParser=getXMLParserAfterParsingInventoryDataRows(inputStream);
			List<String[]> dataRows=xmlDataParser.getRowList();
			
			return processXMLData(dataRows,tableName,allDataGridColumnsCount,overrideLabel);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	public static List<String[]> processXMLData(InputStream inputStream,String tableName,int allDataGridColumnsCount,String overrideLabel) throws Exception {
		List<String[]> dataRows=parseInventoryDataRows(inputStream);
		return processXMLData(dataRows,tableName,allDataGridColumnsCount,overrideLabel);
	}
	
	public static List<String[]> processXMLData(List<String[]> dataRows,String tableName,int allDataGridColumnsCount,String overrideLabel) throws Exception {
		List<String[]> dataGridRows=convertDataRowsToDataGridRows(dataRows,allDataGridColumnsCount);
		Map<String,List<String[]>> navigationToDataGridRowsMapToWrite=getNavigationToDataGridRowsMap(dataGridRows,overrideLabel);
		writeDataGridRows(tableName,navigationToDataGridRowsMapToWrite,true,false);				
		return dataGridRows;
	}
	
	public static List<String[]> readDataGridRows(String tableName,String navigationNameToRead,boolean isReadAllNavigation) throws Exception {
		return readWriteDataGridRows(tableName,true,navigationNameToRead,isReadAllNavigation,null,false,false); 
	}
					
	public static void writeDataGridRows(String tableName,Map<String,List<String[]>> navigationToDataGridRowsMapToWrite,boolean isOverride,
			boolean isRemoveAllSerializedFiles) throws Exception {
		readWriteDataGridRows(tableName,false,null,false,navigationToDataGridRowsMapToWrite,isOverride,isRemoveAllSerializedFiles); 
	}
			
	/*
	 * some parent lookup values may be done in parallel of that parent table being validated
	 * resulting in some errors when the file is accessed during write.
	 * So, synchronizing any access between serialize and unserialize.
	 */
	private synchronized static List<String[]> readWriteDataGridRows(
			String tableName,
			boolean isRead,
			String navigationNameToRead,
			boolean isReadAllNavigation,
			Map<String,List<String[]>> navigationToDataGridRowsMapToWrite,
			boolean isOverride,
			boolean isRemoveAllSerializedFiles) 
	throws Exception
	{
		if (!isRead) {
			serializeDataGridRows(tableName,navigationToDataGridRowsMapToWrite,isOverride,isRemoveAllSerializedFiles);
			return null;
		}
		return getUnserializedDataGridRows(tableName,navigationNameToRead,isReadAllNavigation);
	}
	
	/*
	 * - for the table: delete any existing serialized file for that navigation being validated.
	 * - re-create the serialized file as per the navigations and path:
	 * 		- DATA - T1 - N1
	 * - also keep track of the table being overriden if it is not unpack BR100
	 */
	private static void serializeDataGridRows(String tableName,Map<String,List<String[]>> navigationToDataGridRowsMap,boolean isOverride,
			boolean isRemoveAllSerializedFiles) throws Exception
	{
		//System.out.println("######WRITE: ON "+new java.util.Date()+" tableName:"+tableName);
		File tableFolder=new File(InventoryDataGridConstants.DATA_TEMP_FOLDER,tableName);
		if (isRemoveAllSerializedFiles) {
			org.apache.commons.io.FileUtils.deleteDirectory(tableFolder);
			//System.out.println(" removing all serialized files");
		}
		tableFolder.mkdirs();
		Iterator<String> iterator=navigationToDataGridRowsMap.keySet().iterator();
		if (isOverride) {
			InventoryDataGridConstants.OVERRIDES_TEMP_FOLDER.mkdirs();
		}
		
		while (iterator.hasNext()) {
			String navigationNameKey=iterator.next();
			String serializedNavigationFileName=null;
			if (navigationNameKey.equalsIgnoreCase(InventoryDataGridConstants.NAVIGATION_NA_VALUE)) {
				serializedNavigationFileName="na-serialized.xml";
			}
			else {
				serializedNavigationFileName=navigationNameKey+"-serialized.xml";;
			}
			List<String[]> list=navigationToDataGridRowsMap.get(navigationNameKey);
			//System.out.println("nav: "+navigationNameKey);
			//for (String[] row:list) {
			//	for (String val:row) {
			//		System.out.print(val+",");
			//	}
			//	System.out.println();
			//}
			
			File serializedNavigationFile=new File(tableFolder,serializedNavigationFileName);
			if (serializedNavigationFile.exists()) {
				serializedNavigationFile.delete();
			}
			serialize(list,serializedNavigationFile);
			//System.out.println("isUnpackArchive: "+isUnpackArchive);
			if (isOverride) {
				File overrideTrackerFile=new File(InventoryDataGridConstants.OVERRIDES_TEMP_FOLDER,tableName);
				overrideTrackerFile.createNewFile();
			}
		}
	}
	
	public static void serialize(List<String[]> list,File outputFile) throws FileNotFoundException, IOException
	{
		ListExternalizable listExternalizable=new ListExternalizable(list);
		ObjectOutputStream output=null;
		try
		{
			output = new  ObjectOutputStream(new FileOutputStream(outputFile));
			output.writeObject(listExternalizable);
			output.flush();
		}
		finally{
			IOUtils.closeQuietly(output);
		}
	}
	
	public static List<String[]> unserialize(File inputFile) throws IOException, ClassNotFoundException
	{
		ObjectInputStream input=null;
		try
		{
			input = new ObjectInputStream(new FileInputStream(inputFile));
			ListExternalizable listExternalizable = (ListExternalizable)input.readObject();
			List<String[]> toReturn=listExternalizable.getList();
			return toReturn;
		}
		finally{
			IOUtils.closeQuietly(input);
		}
	}

		
	/*
	 * for reading rows for the table and navigation:
	 * read from STD:
	 * 		- DATA - T1 - N1
	 */
	private static List<String[]> getUnserializedDataGridRows(String tableName,String navigationName,boolean isAllNavigation) throws Exception {
		File tableFolder=new File(InventoryDataGridConstants.DATA_TEMP_FOLDER,tableName);
		List<String[]> toReturn=new ArrayList<String[]>();
		if (isAllNavigation) {
			if (tableFolder.exists()) {
				List<File> files=getFiles(tableFolder,null);
				toReturn=getDataGridRows(files);
			}
		}
		else {
			String serializedNavigationFileName=null;
			if (navigationName.equalsIgnoreCase(InventoryDataGridConstants.NAVIGATION_NA_VALUE)) {
				serializedNavigationFileName="na-serialized.xml";
			}
			else {
				serializedNavigationFileName=navigationName+"-serialized.xml";;
			}
			File serializedNavigationFile=new File(tableFolder,serializedNavigationFileName);
			List<File> list=new ArrayList<File>();
			if (serializedNavigationFile.exists()) {
				list.add(serializedNavigationFile);
				toReturn=getDataGridRows(list);
			}
		}
		
		//System.out.println("######READDDD: ON "+new java.util.Date());
		//for (String[] row:toReturn) {
		//	System.out.println(" nav: "+navigationName+" : "+row[0]+"/"+row[1]+"/"+row[2]+"/"+row[3]);	
		//}
		
		return toReturn;
	}
	
	public static List<String> getOverridesTableNames(){
		File overrideTrackerFolder=InventoryDataGridConstants.OVERRIDES_TEMP_FOLDER;
		List<File> files=getFiles(overrideTrackerFolder,null);
		List<String> toReturn=new ArrayList<String>();
		for ( File file : files ) {
			toReturn.add(file.getName());
		}
		return toReturn;
	}
	
	public static Set<String> getNavigationNames(String tableName){
		File tableFolder=new File(InventoryDataGridConstants.DATA_TEMP_FOLDER,tableName);
		List<File> files=getFiles(tableFolder,null);
		Set<String> toReturn=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		for ( File file : files ) {
			toReturn.add(file.getName().replaceFirst("-serialized.xml",""));
		}
		return toReturn;
	}
	
	public static Set<String> getTableForNANavigationSet() throws Exception{
		Set<String> toReturn=new HashSet<String>();
		File[] files=InventoryDataGridConstants.DATA_TEMP_FOLDER.listFiles();
		if (files==null) {
			return toReturn;
		}
		for ( File file : files ) {
            if ( file.isDirectory() ) {
            	File naFile=new File(file.getAbsolutePath(),"na-serialized.xml");
            	if (naFile.exists()) {
    				toReturn.add(file.getName());
            	}
            }
		}
		return toReturn;
	}
	
	public static void exportGridsToInventoryFormat(List<String> overridesTableNames,File targetFolder,Map<String,Inventory> inventoriesMap,
			Map<String, Set<String>> tableNameToNavigationMapperNameMap) throws Exception {
		for ( String overridesTableName:overridesTableNames) {
			Inventory inventory=inventoriesMap.get(overridesTableName);
			if (inventory==null) {
				throw new Exception("Cannot find the serialized file for the table: '"+overridesTableName+"'");
			}
			List<String[]> dataGridRows=getDataGridRowsForAllNavigations(overridesTableName);
			List<String[]> inventoryDataRows=convertDataGridRowsToInventoryDataRows(dataGridRows); 
		
			Set<String> validNavigationMappers=tableNameToNavigationMapperNameMap.get(inventory.getName());
			if (validNavigationMappers==null) {
				validNavigationMappers=new HashSet<String>();
			}
			File outputFile=new File(targetFolder,inventory.getName()+"."+SwiftBuildConstants.XML_FILE_EXTENSION);
			InventoryDataGridUtils.exportInventoryData(
					outputFile,
					inventory.getName(),
					inventory.getFieldNamesUsedForDataEntry(),
					validNavigationMappers,
					inventoryDataRows);
		}
	}
	
	public static List<File> getFiles(File rootFolder,final String fileNameToFind) {
		List<File> toReturn=new ArrayList<File>();
		File[] files=rootFolder.listFiles();
		if (files==null) {
			return toReturn;
		}
		for ( File file : files ) {
            if ( file.isDirectory() ) {
            	toReturn.addAll(getFiles( file ,fileNameToFind));
            }
            else {
            	if (fileNameToFind==null || file.getName().equalsIgnoreCase(fileNameToFind)) {
            		toReturn.add(file);
            	}
            }
        }
		return toReturn;
	}	
	
	public static Set<String> getAllNavigationNamesInUse(String tableName) {
		File tableFolder=new File(InventoryDataGridConstants.DATA_TEMP_FOLDER,tableName);
		Set<String> toReturn=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		List<String> stdList=getAllNavigationNamesInUse(tableFolder);
		toReturn.addAll(stdList);
		return toReturn;
	}	
	
	private static List<String> getAllNavigationNamesInUse(File tableNameFolder) {
		List<String> toReturn=new ArrayList<String>();
		File[] files=tableNameFolder.listFiles();
		if (files==null) {
			return toReturn;
		}
		for ( File file : files ) {
            if ( file.isDirectory() ) {
            	continue;
            }
            String navigationInUse=file.getName().replaceFirst("-serialized.xml","");
            if (navigationInUse.equalsIgnoreCase("na")) {
            	 navigationInUse=InventoryDataGridConstants.NAVIGATION_NA_VALUE;
            }
            toReturn.add(navigationInUse);
        }
		return toReturn;
	}	
	
	private static List<String[]> getDataGridRows(List<File> files) throws Exception {
		List<String[]> toReturn=new ArrayList<String[]>();
		for (File file:files) {
			try{
				List<String[]> rows=unserialize(file);
				toReturn.addAll(rows);
			}
			catch(Exception e) {
				FileUtils.printStackTrace(e);
				throw new Exception("Unable to read serialized file: '"+file.getAbsolutePath()+"' : "+e.getMessage());
			}
		}
		return toReturn;
	}
	
	public static void exportInventoryData(File outputFile,String tableName,List<String> inventoryDataColumnList,Set<String> validNavigationMappers,List<String[]> dataRows) {
		PrintWriter pw=null;
		try {
			List<String> columns=new ArrayList<String>(inventoryDataColumnList);
			columns.add(InventoryDataGridConstants.EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_DESCRIPTION);
			columns.add(InventoryDataGridConstants.EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_DESCRIPTION);

			InventoryDataGridUtils.saveInventoryDataToXMLFile(
					outputFile,
					columns, 
					validNavigationMappers,
					tableName,
					dataRows);
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
		finally {
			if (pw!=null) {
				pw.close();
			}
		}
	}
		
	public static List<String[]> convertDataGridRowsToInventoryDataRows(List<String[]> dataGridRows) 
	throws Exception {
		List<String[]> toReturn=new ArrayList<String[]>();
		for (String[] dataGridRow:dataGridRows) {
			String[] dataRow=convertDataGridRowToInventoryDataRow(dataGridRow); 
			toReturn.add(dataRow);
		}
		return toReturn;
	}
	
	public static String[] convertDataGridRowToInventoryDataRow(String[] dataGridRow) 
	throws Exception {
		String[] dataRow=new String[dataGridRow.length-InventoryDataGridConstants.EXTRA_COLUMN_CONTROL_COUNT];
		int cnt=0;
		for (int i=InventoryDataGridConstants.EXTRA_COLUMN_CONTROL_COUNT;i<dataGridRow.length;i++) {
			dataRow[cnt++]=dataGridRow[i];
		}
		return dataRow;
	}
		
	public static List<String> getColumnNameList(List<DataGridColumn> dataGridInventoryColumns) 
	throws Exception {
		List<String> toReturn=new ArrayList<String>();		
		for (DataGridColumn dgc:dataGridInventoryColumns) {
			toReturn.add(dgc.getAttributeDescription());
		}
		return toReturn;
	}
	
	public static List<String[]> getDataGridRowsForAllNavigations(String tableName) throws Exception {
		return InventoryDataGridUtils.readDataGridRows(tableName,null,true);
	}
	
	public static void validateColumns(Inventory inventory,List<String> headerList)
	throws Exception{
		List<String> inventoryColumnNames=inventory.getFieldNamesUsedForDataEntry();
		if (inventoryColumnNames.size()!=headerList.size()-2) {
			throw new Exception("Incompatible version of inventory and data files, mismatch columns count: data file: "+
				(headerList.size()-2)+" data columns, inventory file: "+inventoryColumnNames.size()+" data columns, for the table: '"+inventory.getName()+
				"' , data file columns: "+headerList+" , inventory columns: "+inventoryColumnNames);
		}
		
		for (int i=0;i<headerList.size();i++) {
			String dataColumnName=headerList.get(i);
			if (dataColumnName.equalsIgnoreCase(InventoryDataGridConstants.EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_DESCRIPTION) ||
				dataColumnName.equalsIgnoreCase(InventoryDataGridConstants.EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_DESCRIPTION)) {
				continue;
			}
			String errorMsg="Incompatible version of inventory and data files: mismatch for the data file column # "+(i+1)+": '"+dataColumnName+"' of the table: '"+
			inventory.getName()+"' , data file columns: "+headerList+" , inventory columns: "+inventoryColumnNames;
			String inventoryColumnName=inventoryColumnNames.get(i);
			if (!dataColumnName.equalsIgnoreCase(inventoryColumnName)) {
				throw new Exception(errorMsg);
			}
		}
		for (int i=0;i<inventoryColumnNames.size();i++) {
			String inventoryColumnName=inventoryColumnNames.get(i);
			String errorMsg="Incompatible version of inventory and data files: mismatch for the inventory file column # "+(i+1)+": '"+inventoryColumnName+"' of the table: '"+
			inventory.getName()+"' , data file columns: "+headerList+" , inventory columns: "+inventoryColumnNames;
			String dataColumnName=headerList.get(i);
			if (!dataColumnName.equalsIgnoreCase(inventoryColumnName)) {
				throw new Exception(errorMsg);
			}
		}
	}
	
}
