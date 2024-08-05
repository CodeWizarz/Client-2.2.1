package com.rapidesuite.build.gui.apigrid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipFile;

import javax.swing.JLabel;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;

import com.erapidsuite.configurator.apiscript0000.ApiScriptDocument;
import com.erapidsuite.configurator.navigation0003.NavigationConstants;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.action.APIAction;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.utils.InjectorsPackageUtils;
import com.rapidesuite.client.common.XmlDataParser;
import com.rapidesuite.client.common.gui.datagrid.DataGridColumn;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridConstants;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridUtils;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.CoreConstants;

public class APIDataGridUtils
{

	public static List<SimpleEntry<String, String>> parseSubstitution(String substitution)
	{
		String temp = substitution;
		int endIndex = temp.indexOf(NavigationConstants.NAVIGATION_SUBSTITUTION_SEPARATOR);
		String pair = null;
		List<SimpleEntry<String, String>> toReturn = new ArrayList<SimpleEntry<String, String>>();
		while ( endIndex != -1 )
		{
			pair = temp.substring(0, endIndex);
			temp = temp.substring(endIndex + 3);
			final int index = pair.indexOf("=");
			final String from = pair.substring(0, index);
			final String to = pair.substring(index + 1);
			SimpleEntry<String, String> se = new SimpleEntry<String, String>(from, to);
			toReturn.add(se);
			endIndex = temp.indexOf(NavigationConstants.NAVIGATION_SUBSTITUTION_SEPARATOR);
		}
		// last pair:

		final int index = temp.indexOf("=");
		if ( index != -1 )
		{
			final String from = temp.substring(0, index);
			final String to = temp.substring(index + 1);
			SimpleEntry<String, String> se = new SimpleEntry<String, String>(from, to);
			toReturn.add(se);
		}
		return toReturn;
	}

	public static Map<String, DataGridColumn> convertListToDescriptionMap(List<DataGridColumn> dataGridInventoryColumns) throws Exception
	{
		Map<String, DataGridColumn> toReturn = new HashMap<String, DataGridColumn>();
		for ( DataGridColumn dgc : dataGridInventoryColumns )
		{
			toReturn.put(dgc.getAttributeDescription(), dgc);
		}
		return toReturn;
	}

	public static ApiScriptDocument parseAPIScriptDocument(InputStream inputStream) throws XmlException, IOException
	{
		try
		{
			return ApiScriptDocument.Factory.parse(inputStream);
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}

	public static List<ApiScriptDocument> getAPIScriptDocuments(File injectorsPackageFile) throws Exception
	{
		List<ApiScriptDocument> toReturn = new ArrayList<ApiScriptDocument>();
		Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
		String countStr = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_COUNT);
		int scriptCount = Integer.valueOf(countStr).intValue();
		for ( int i = 0; i < scriptCount; i++ )
		{
			CoreConstants.INJECTOR_TYPE scriptType = CoreConstants.INJECTOR_TYPE.forString(specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_TYPE_PREFIX + (i + 1)));
			if ( scriptType.equals(CoreConstants.INJECTOR_TYPE.TYPE_API) )
			{
				String scriptName = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_NAME_PREFIX + (i + 1));

				InputStream inputStream = null;
				ZipFile zipFile = null;
				try
				{
					if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(injectorsPackageFile) )
					{
						inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(injectorsPackageFile, scriptName);
					}
					else
					{
						zipFile = new ZipFile(injectorsPackageFile);
						inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, scriptName);
					}
					ApiScriptDocument apiScriptDocument = parseAPIScriptDocument(inputStream);
					toReturn.add(apiScriptDocument);
				}
				finally
				{
					if ( zipFile != null )
					{
						zipFile.close();
					}
				}
			}
		}
		return toReturn;
	}

	public static void unpackAPIDataFiles(File injectorsPackageFile, List<Injector> injectors, JLabel progressLabel) throws Exception
	{
		File dataTempFolder = InventoryDataGridConstants.DATA_TEMP_FOLDER;
		org.apache.commons.io.FileUtils.deleteDirectory(dataTempFolder);
		dataTempFolder.mkdirs();

		Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
		String injectorsCount = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_COUNT);
		ZipFile zipFile = null;
		int counter = 0;
		XmlDataParser xmlDataParser = null;
		try
		{
			for ( Injector injector : injectors )
			{
				counter++;
				if ( progressLabel != null )
				{
					GUIUtils.showInProgressMessage(progressLabel, "Unpacking API data files, in progress: " + counter + " / " + injectorsCount + " ...");
				}
				if ( injector.getType().equals(CoreConstants.INJECTOR_TYPE.TYPE_API) )
				{
					String scriptGenerationIdAPI = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_API_PREFIX + (injector.getIndex() + 1));
					String inventoryFileName = scriptGenerationIdAPI + "." + APIAction.GENERATION_API_INVENTORY_SUFFIX + "." + SwiftBuildConstants.XML_FILE_EXTENSION;
					Inventory inventory = APIDataGridUtils.getInventory(injectorsPackageFile, inventoryFileName);
					String tableName = inventory.getName();
					String dataFileName = scriptGenerationIdAPI + "." + APIAction.GENERATION_API_DATA_SUFFIX + "." + SwiftBuildConstants.XML_FILE_EXTENSION;
					int allDataGridColumnsCount = InventoryDataGridConstants.EXTRA_COLUMN_CONTROL_COUNT + inventory.getFieldNamesUsedForDataEntry().size() + 2;
					Map<String, Set<String>> tableNameToNavigationMapperNameMap = new TreeMap<String, Set<String>>(String.CASE_INSENSITIVE_ORDER);
					InputStream inputStream = null;
					try
					{
						if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(injectorsPackageFile) )
						{
							inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(injectorsPackageFile, dataFileName);
						}
						else
						{
							zipFile = new ZipFile(injectorsPackageFile);
							inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, dataFileName);
						}
						xmlDataParser = InventoryDataGridUtils.getXMLParserAfterParsingInventoryDataRows(inputStream);
						List<String> mapperList = xmlDataParser.getMapperList();

						InventoryDataGridUtils.validateColumns(inventory, xmlDataParser.getHeaderList());

						Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
						set.addAll(mapperList);
						tableNameToNavigationMapperNameMap.put(tableName, set);

						List<String[]> dataGridRows = InventoryDataGridUtils.convertDataRowsToDataGridRows(xmlDataParser.getRowList(), allDataGridColumnsCount);
						Map<String, List<String[]>> navigationToDataGridRowsMapToWrite = InventoryDataGridUtils.getNavigationToDataGridRowsMap(dataGridRows, null);

						InventoryDataGridUtils.writeDataGridRows(tableName, navigationToDataGridRowsMapToWrite, true, true);
					}
					finally
					{
						IOUtils.closeQuietly(inputStream);
					}
				}
			}
			GUIUtils.resetLabel(progressLabel);
		}
		finally
		{
			if ( zipFile != null )
			{
				zipFile.close();
			}
		}
	}

	public static Inventory getInventory(File injectorsPackageFile, String entryName) throws Exception
	{
		InputStream inputStream = null;
		ZipFile zipFile = null;
		try
		{
			if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(injectorsPackageFile) )
			{
				inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(injectorsPackageFile, entryName);
			}
			else
			{
				zipFile = new ZipFile(injectorsPackageFile);
				inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, entryName);
			}
			return FileUtils.getInventory(entryName, inputStream);
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
			if ( zipFile != null )
			{
				zipFile.close();
			}
		}
	}

	static Map<String, Integer> getOrganizationCodeToIdMap(Connection connection) throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql = "SELECT organization_code,ORGANIZATION_ID FROM  MTL_PARAMETERS";

			statement = connection.prepareStatement(sql);
			statement.execute();
			resultSet = statement.executeQuery();

			Map<String, Integer> res = new HashMap<String, Integer>();
			while ( resultSet.next() )
			{
				String organizationCode = resultSet.getString("organization_code");
				int organizationId = resultSet.getInt("ORGANIZATION_ID");

				res.put(organizationCode, organizationId);
			}
			TreeMap<String, Integer> tm = new TreeMap<String, Integer>(res);
			return tm;
		}
		finally
		{
		    DirectConnectDao.closeQuietly(resultSet);
		    DirectConnectDao.closeQuietly(statement);
		}
	}

	static Map<Integer, Map<String, String>> getOrganizationIdToItemMap(Connection connection) throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			String sql = "select distinct organization_id,Concatenated_Segments from Mtl_System_Items_Kfv";

			statement = connection.prepareStatement(sql);

			resultSet = statement.executeQuery();
			Map<Integer, Map<String, String>> res = new HashMap<Integer, Map<String, String>>();
			int counter = 1;
			while ( resultSet.next() )
			{
				System.out.println("counter:" + counter++);
				String item = resultSet.getString("Concatenated_Segments");
				int organizationId = resultSet.getInt("organization_id");
				Map<String, String> items = res.get(organizationId);
				if ( items == null )
				{
					items = new HashMap<String, String>();
					res.put(organizationId, items);
				}
				items.put(item, item);
			}
			return res;
		}
		finally
		{
		    DirectConnectDao.closeQuietly(resultSet);
		    DirectConnectDao.closeQuietly(statement);
		}
	}

	

	public static Map<String, Integer> getDataGridColumnNameToPositionMap(List<String> columnNameList)
	throws Exception
	{
		Map<String,Integer> toReturn=new TreeMap<String,Integer>(String.CASE_INSENSITIVE_ORDER);
		int index=0;
		toReturn.put(InventoryDataGridConstants.EXTRA_COLUMN_ROW_SEQUENCE_ATTRIBUTE_DESCRIPTION,index++);
		toReturn.put(InventoryDataGridConstants.EXTRA_COLUMN_ENABLED_ATTRIBUTE_DESCRIPTION,index++);
		toReturn.put(InventoryDataGridConstants.EXTRA_COLUMN_STATUS_ATTRIBUTE_DESCRIPTION,index++);
		toReturn.put(InventoryDataGridConstants.EXTRA_COLUMN_MESSAGE_ATTRIBUTE_DESCRIPTION,index++);
		for (String columnName:columnNameList){
			toReturn.put(columnName,index++);
		}
		if (toReturn.get(InventoryDataGridConstants.EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_DESCRIPTION)==null) {
			toReturn.put(InventoryDataGridConstants.EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_DESCRIPTION,index++);
		}
		if (toReturn.get(InventoryDataGridConstants.EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_DESCRIPTION)==null) {
			toReturn.put(InventoryDataGridConstants.EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_DESCRIPTION,index++);
		}
		return toReturn;
	}
	
	

	public static List<String[]> getData(
			Connection connection,
			int operatingUnitId,
			String query,
			JLabel progressLabelToUpdate,
			String progressPrefixMessage)
	throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			List<String[]> res=new ArrayList<String[]>();
			if (progressLabelToUpdate!=null) {
				GUIUtils.showInProgressMessage(progressLabelToUpdate,progressPrefixMessage+" calculating Reference data count...");
			}
			setConnectionOperatingUnitProperties(connection,operatingUnitId);
			int rowsCount=-1;
			if (progressLabelToUpdate!=null) {
				rowsCount=DatabaseUtils.getRowsCount(connection,query, null, null);
				GUIUtils.showInProgressMessage(progressLabelToUpdate,progressPrefixMessage+" 0 / "+rowsCount+" rows loaded.");
			}

			statement= connection.prepareStatement(query);
			statement.execute();
			resultSet=statement.executeQuery();

			ResultSetMetaData rsMetaData = resultSet.getMetaData();
			int numberOfColumnsInResultSet=rsMetaData.getColumnCount();
			int cnt=0;
			int BATCH_SIZE=20;
			while ( resultSet.next() )
			{
				cnt++;
				if (progressLabelToUpdate!=null && (cnt % BATCH_SIZE ==0)) {
					String msg=progressPrefixMessage+" "+cnt+" / "+rowsCount+" rows loaded.";
					GUIUtils.showInProgressMessage(progressLabelToUpdate,msg);
				}

				String[] row=new String[numberOfColumnsInResultSet];
				for ( int i = 1; i <= numberOfColumnsInResultSet; i++ ) {
					String value=resultSet.getString(i);
					if (value==null) {
						value="";
					}
					row[i-1]=value;
				}
				res.add(row);
			}
			if (progressLabelToUpdate!=null) {
				String msg=progressPrefixMessage+" "+cnt+" / "+rowsCount+" rows loaded.";
				GUIUtils.showInProgressMessage(progressLabelToUpdate,msg);
			}
			return res;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}


	public static void setConnectionOperatingUnitProperties(Connection connection,int operatingUnitId) throws Exception
	{
		if (operatingUnitId!=-1) {
			DatabaseUtils.setClientInfo(connection,operatingUnitId);
			String ebsVersion=DatabaseUtils.getEBSVersion(connection);
			if (ebsVersion.indexOf("11.5")==-1) {
				DatabaseUtils.setPolicyContextServer(connection,operatingUnitId);
			}
		}
	}


}
