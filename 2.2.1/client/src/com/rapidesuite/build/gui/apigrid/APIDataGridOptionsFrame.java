/**************************************************
 * $Revision: 57873 $:
 * $Author: olivier.deruelle $:
 * $Date: 2016-08-31 18:40:51 +0700 (Wed, 31 Aug 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/apigrid/APIDataGridOptionsFrame.java $:
 * $Id: APIDataGridOptionsFrame.java 57873 2016-08-31 11:40:51Z olivier.deruelle $:
 */

package com.rapidesuite.build.gui.apigrid;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.io.IOUtils;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.action.APIAction;
import com.rapidesuite.build.core.action.ActionInterface;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.gui.panels.InjectorsPackageExecutionPanel;
import com.rapidesuite.build.utils.InjectorsPackageUtils;
import com.rapidesuite.client.common.gui.datagrid.DataGridColumn;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridConstants;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridUtils;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.core.utility.CoreUtil;

@SuppressWarnings("serial")
public class APIDataGridOptionsFrame extends JDialog
{
	public final static String MAGIC_SEPARATOR="rsc@#$*()P(*908070-9s8fsdf";

	
	private JCheckBox emptyColumnsCheckBox;
	private JCheckBox requiredColumnsCheckBox;
	private JButton unselectErrorsRecordsPCButton;
	private JButton setEnabledAndModeButton;
	private JButton setGetErrorsAndModeButton;
	private JButton createBWPsButton;

	private APIDataGridController apiDataGridController;

	public APIDataGridOptionsFrame(APIDataGridController apiDataGridController)
	{
		super(new JFrame(), true);
		this.apiDataGridController = apiDataGridController;
		createComponents();
	}

	public void createComponents()
	{
		emptyColumnsCheckBox = new JCheckBox("Hide empty data columns");
		emptyColumnsCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					executeEmptyColumnsCheckBox();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		});

		requiredColumnsCheckBox = new JCheckBox("Show only required data columns");
		requiredColumnsCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					executeRequiredColumnsCheckBox();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		});

		unselectErrorsRecordsPCButton= new JButton("Change records status in Parent/ Child grids");
		unselectErrorsRecordsPCButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					unselectErrorsRecordsPC(apiDataGridController.getAPIDataAction().getApiAction().getInjectorsPackageFile(),
							apiDataGridController.getAPIDataAction().getApiAction().getBuildMain());
					setVisible(false);
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		});

		setEnabledAndModeButton= new JButton("Select/ Unselect records  based on status and Change Mode in Parent/ Child grids");
		setEnabledAndModeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					uncheckInvalidRowsAndCheckSetModeForValidRows(apiDataGridController.getAPIDataAction().getApiAction().getInjectorsPackageFile(),
							apiDataGridController.getAPIDataAction().getApiAction().getBuildMain());
					setVisible(false);
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		});

		setGetErrorsAndModeButton= new JButton("Change Mode to 'Get errors' and status to 'Enabled' for all 'Valid' rows and 'Create' mode in all grids");
		setGetErrorsAndModeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					changeModeToGetErrorsForValidAndCreateModeRows(apiDataGridController.getAPIDataAction().getApiAction().getInjectorsPackageFile(),
							apiDataGridController.getAPIDataAction().getApiAction().getBuildMain());
					setVisible(false);
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		});

		createBWPsButton= new JButton("Create Success and Error packages");
		createBWPsButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createBWPsForSuccessAndErrorRecords(apiDataGridController.getAPIDataAction().getApiAction().getInjectorsPackageFile());
					setVisible(false);
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		});

		JPanel tempPanel = new JPanel();
		// (int top,int left,int bottom,int right)
		tempPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 50, 2));
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		tempPanel.add(emptyColumnsCheckBox);
		tempPanel.add(requiredColumnsCheckBox);
		tempPanel.add(unselectErrorsRecordsPCButton);
		tempPanel.add(setEnabledAndModeButton);
		tempPanel.add(setGetErrorsAndModeButton);
		tempPanel.add(createBWPsButton);

		tempPanel.add(Box.createVerticalStrut(10));

		this.add(tempPanel);

		setSize(APIDataGridConstants.FRAME_OTIONS_WIDTH, APIDataGridConstants.FRAME_OTIONS_HEIGHT);
		setTitle("Options");
	}

	void executeEmptyColumnsCheckBox() throws Exception
	{
		if ( emptyColumnsCheckBox.isSelected() )
		{
			if ( requiredColumnsCheckBox.isSelected() )
			{
				apiDataGridController.showRequiredColumnsOnly();
			}
			else
			{
				apiDataGridController.hideEmptyColumns();
			}
		}
		else
		{
			if ( requiredColumnsCheckBox.isSelected() )
			{
				apiDataGridController.showRequiredColumnsOnly();
			}
			else
			{
				apiDataGridController.showAllColumns();
			}
		}
	}

	void executeRequiredColumnsCheckBox() throws Exception
	{
		if ( requiredColumnsCheckBox.isSelected() )
		{
			apiDataGridController.showRequiredColumnsOnly();
		}
		else
		{
			apiDataGridController.showAllColumns();
			if ( emptyColumnsCheckBox.isSelected() )
			{
				apiDataGridController.hideEmptyColumns();
			}
		}
	}

	public static Map<String,Inventory> getAllInventories(File injectorsPackageFile,List<Injector> injectors) throws Exception
	{
		Map<String,Inventory> toReturn=new HashMap<String,Inventory>();
		for (Injector injector:injectors) {
			Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
			String scriptGenerationIdAPI = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_API_PREFIX + (injector.getIndex() + 1));
			if ( scriptGenerationIdAPI!=null) {
				String inventoryFileName = scriptGenerationIdAPI + "." + APIAction.GENERATION_API_INVENTORY_SUFFIX + "." + SwiftBuildConstants.XML_FILE_EXTENSION;
				Inventory inventory = APIDataGridUtils.getInventory(injectorsPackageFile, inventoryFileName);
				toReturn.put(inventory.getName(),inventory);
			}
		}
		return toReturn;
	}

	public static Map<String,Inventory> getAllInventoriesInParentChild(Map<String,Inventory> inventories,Inventory rootInventory)
	{
		Map<String,Inventory> toReturn=new HashMap<String,Inventory>();
		Iterator<String> iterator=inventories.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			Inventory inventory=inventories.get(key);
			if (isInventoryDescendantOfRoot(inventory,rootInventory,inventories)) {
				toReturn.put(inventory.getName(),inventory);
			}
		}
		return toReturn;
	}

	private static List<Inventory> getDirectChildInventories(Map<String,Inventory> inventories,Inventory inventory)
	{
		List<Inventory> toReturn=new ArrayList<Inventory>();
		Iterator<String> iterator=inventories.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			Inventory inventoryTemp=inventories.get(key);
			if (inventoryTemp.getParentName()!=null && inventoryTemp.getParentName().equalsIgnoreCase(inventory.getName())) {
				toReturn.add(inventoryTemp);
			}
		}
		return toReturn;
	}

	private static boolean hasChildInventory(Inventory parentInventory,Map<String,Inventory> inventories)
	{
		Iterator<String> iterator=inventories.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			Inventory inventory=inventories.get(key);
			if (inventory.getParentName()!=null && inventory.getParentName().equalsIgnoreCase(parentInventory.getName())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isInventoryDescendantOfRoot(Inventory inventory,Inventory rootInventory,Map<String,Inventory> inventories)
	{
		if (inventory.getName().equalsIgnoreCase(rootInventory.getName())) {
			return true;
		}
		if (inventory.getParentName()==null || inventory.getParentName().isEmpty()) {
			return false;
		}
		if (inventory.getParentName()!=null && inventory.getParentName().equalsIgnoreCase(rootInventory.getName())) {
			return true;
		}
		Inventory parentInventory=inventories.get(inventory.getParentName());
		return isInventoryDescendantOfRoot(parentInventory,rootInventory,inventories);
	}

	public static Inventory getRootInventory(Map<String,Inventory> inventories)
	{
		Iterator<String> iterator=inventories.keySet().iterator();
		List<Inventory> rootInventories=new ArrayList<Inventory>();
		while (iterator.hasNext()) {
			String key=iterator.next();
			Inventory inventory=inventories.get(key);
			if (inventory.getParentName()==null || inventory.getParentName().isEmpty()) {
				rootInventories.add(inventory);
			}
		}

		for (Inventory inventory:rootInventories) {
			if (hasChildInventory(inventory,inventories)) {
				return inventory;
			}
		}
		return null;
	}

	private static void unselectErrorsRecordsPC(File injectorsPackageFile,BuildMain buildMain) throws Exception
	{
		List<Injector> injectors=InjectorsPackageUtils.getInjectors(injectorsPackageFile,CoreConstants.INJECTOR_TYPE.TYPE_API);
		if (injectors.isEmpty()) {
			throw new Exception("There are no API scripts detected in the BWP file '"+injectorsPackageFile.getAbsolutePath()+"'");
		}
		Map<String,Inventory> allInventories=getAllInventories(injectorsPackageFile,injectors);
		Inventory rootInventory=getRootInventory(allInventories);
		if (rootInventory==null) {
			throw new Exception("There is no Root inventory in the BWP file '"+injectorsPackageFile.getAbsolutePath()+"'");
		}
		Map<String,Inventory> inventories=getAllInventoriesInParentChild(allInventories,rootInventory);
		List<String> rootKeyColumns=getRootKeyColumns(inventories,rootInventory.getName());
		Map<String,List<String>> keyColumns=getKeyColumns(rootInventory,inventories,rootKeyColumns);
		Map<String,Map<String, Integer>> inventoryNameToDataGridColumnNameToPositionForInventories=getInventoryNameToDataGridColumnNameToPositionForInventories(inventories);

		Set<String> allKeyValuesWithError=getAllKeyValuesWithError(inventories,inventoryNameToDataGridColumnNameToPositionForInventories,keyColumns);
		setAllRowsToError(inventories,allKeyValuesWithError,inventoryNameToDataGridColumnNameToPositionForInventories,keyColumns);
		refreshGrids(buildMain);
	}

	private static Set<String> getAllKeyValuesWithError(Map<String,Inventory> inventories,
			Map<String,Map<String, Integer>> inventoryNameToDataGridColumnNameToPositionForInventories,Map<String,List<String>> keyColumns) throws Exception
	{
		Set<String> toReturn=new HashSet<String>();
		for (Inventory inventory:inventories.values()) {
			List<String[]> dataGridRowsStringFormat=InventoryDataGridUtils.readDataGridRows(inventory.getName(),InventoryDataGridConstants.NAVIGATION_NA_VALUE,false);
			Map<String, Integer> dataGridColumnNameToPositionMap=inventoryNameToDataGridColumnNameToPositionForInventories.get(inventory.getName());
			List<String> inventoryKeyColumns=keyColumns.get(inventory.getName());
			List<Integer> keyColumnIndexes=getKeyColumnIndexes(dataGridColumnNameToPositionMap,inventoryKeyColumns,inventory.getName());
			for (String[] dataGridRowInStringFormat:dataGridRowsStringFormat) {
				boolean isRowError=dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX].equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE);
				if (isRowError){
					String keyValueRow=getConcatenatedKeyValue(keyColumnIndexes,dataGridRowInStringFormat);
					toReturn.add(keyValueRow);
				}
			}
		}
		return toReturn;
	}

	public static List<String> getRootKeyColumns(Map<String,Inventory> inventories,String parentInventoryName)
	{
		Inventory firstDirectChildInventory=null;
		for (Inventory inventory:inventories.values()) {
			if (inventory.getParentName().equalsIgnoreCase(parentInventoryName)) {
				 firstDirectChildInventory=inventory;
				 break;
			}
		}
		List<Field> fields=firstDirectChildInventory.getFieldsUsedForDataEntry();
		List<String> toReturn=new ArrayList<String>();
		for (int i=0;i<fields.size();i++) {
			Field field=fields.get(i);
			String pName=field.getParentName();
			if (pName!=null && !pName.isEmpty()) {
				toReturn.add(pName);
			}
		}
		return toReturn;
	}

	public static Map<String,List<String>> getKeyColumns(Inventory parentInventory,Map<String,Inventory> inventories,List<String> parentKeyColumns) throws Exception
	{
		List<Inventory> directChildInventories=getDirectChildInventories(inventories,parentInventory);
		Map<String,List<String>> toReturn=new HashMap<String,List<String>>();
		toReturn.put(parentInventory.getName(),parentKeyColumns);
		for (Inventory childInventory:directChildInventories) {
			List<String> keyColumns=getKeyColumns(childInventory,parentKeyColumns);
			Map<String,List<String>> tempMap=getKeyColumns(childInventory,inventories,keyColumns);
			toReturn.putAll(tempMap);
		}
		return toReturn;
	}

	private static List<String> getKeyColumns(Inventory inventory,List<String> parentKeyColumns) throws Exception
	{
		List<Field> fields=inventory.getFieldsUsedForDataEntry();
		List<String> toReturn=new ArrayList<String>();
		for (String parentKeyColumn:parentKeyColumns) {
			boolean isFound=false;
			for (int i=0;i<fields.size();i++) {
				Field field=fields.get(i);
				String pName=field.getParentName();
				if (pName!=null && !pName.isEmpty() && pName.equalsIgnoreCase(parentKeyColumn)) {
					toReturn.add(field.getName());
					isFound=true;
					break;
				}
			}
			if (!isFound) {
				throw new Exception("The parent column: '"+parentKeyColumn+"' has no child column in the inventory: '"+inventory.getName()+"'");
			}
		}
		return toReturn;
	}

	public static List<Integer> getKeyColumnIndexes(Map<String, Integer> dataGridColumnNameToPositionMap,List<String> keyColumns,String inventoryName) throws Exception
	{
		List<Integer> toReturn=new ArrayList<Integer>();
		for (String keyColumn:keyColumns) {
			Integer keyColumnIndex=dataGridColumnNameToPositionMap.get(keyColumn);
			if (keyColumnIndex==null) {
				throw new Exception("cannot find the column name '"+keyColumn+"' in the inventory '"+inventoryName+"'");
			}
			toReturn.add(keyColumnIndex);
		}
		return toReturn;
	}

	public static String getConcatenatedKeyValue(List<Integer> keyColumnIndexes,String[] dataRow)
	{
		StringBuffer toReturn=new StringBuffer("");
		for (Integer keyColumnIndex:keyColumnIndexes) {
			if (!toReturn.toString().isEmpty()) {
				toReturn.append(MAGIC_SEPARATOR);
			}
			String keyValueRow=dataRow[keyColumnIndex];
			toReturn.append(keyValueRow);
		}
		return toReturn.toString();
	}

	private static void setAllRowsToError(Map<String,Inventory> inventories,Set<String> keyValues,
			Map<String,Map<String, Integer>> inventoryNameToDataGridColumnNameToPositionForInventories,Map<String,List<String>> keyColumns) throws Exception
	{
		for (Inventory inventory:inventories.values()) {
			List<String[]> dataGridRowsStringFormat=InventoryDataGridUtils.readDataGridRows(inventory.getName(),InventoryDataGridConstants.NAVIGATION_NA_VALUE,false);
			Map<String, Integer> dataGridColumnNameToPositionMap=inventoryNameToDataGridColumnNameToPositionForInventories.get(inventory.getName());
			List<String> inventoryKeyColumns=keyColumns.get(inventory.getName());
			List<Integer> keyColumnIndexes=getKeyColumnIndexes(dataGridColumnNameToPositionMap,inventoryKeyColumns,inventory.getName());

			for (String[] dataGridRowInStringFormat:dataGridRowsStringFormat) {
				String keyValueRow=getConcatenatedKeyValue(keyColumnIndexes,dataGridRowInStringFormat);
				if (keyValues.contains(keyValueRow)) {
					if (!dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX].equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE)) {
						dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX]=InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE;
						dataGridRowInStringFormat[InventoryDataGridConstants.MESSAGE_COLUMN_GRID_INDEX]="Error: rejected row(s) in the Parent/Child grids";
					}
				}
			}
			Map<String,List<String[]>> navigationToDataGridRowsMapToWrite=InventoryDataGridUtils.getNavigationToDataGridRowsMap(dataGridRowsStringFormat,null);
			InventoryDataGridUtils.writeDataGridRows(inventory.getName(),navigationToDataGridRowsMapToWrite,false,true);
		}
	}

	private static Map<String,Map<String, Integer>> getInventoryNameToDataGridColumnNameToPositionForInventories(Map<String,Inventory> allInventories) throws Exception
	{
		Map<String,Map<String, Integer>> toReturn=new TreeMap<String,Map<String, Integer>>();
		Iterator<String> iterator=allInventories.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			Inventory inventory=allInventories.get(key);
			Map<String, Integer> dataGridColumnNameToPositionMap=APIDataGridUtils.getDataGridColumnNameToPositionMap(inventory.getFieldNamesUsedForDataEntry());
			toReturn.put(inventory.getName(),dataGridColumnNameToPositionMap);
		}
		return toReturn;
	}

	public void showFrame()
	{
		Point p = apiDataGridController.getInventoryDataGridFrame().getLocation();
		this.setLocation(p.x + (APIDataGridConstants.FRAME_WIDTH / 2) - 50, p.y + (APIDataGridConstants.FRAME_HEIGHT / 2));
		setVisible(true);
	}

	public static void createBWPsForSuccessAndErrorRecords(File injectorsPackageFile) throws Exception {
		String outputFolderName = Config.getBuildApiPackagesOutputFolder().getAbsolutePath();
		Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
		List<Injector> injectors=InjectorsPackageUtils.getInjectors(injectorsPackageFile,CoreConstants.INJECTOR_TYPE.TYPE_API);
		if (injectors.isEmpty()) {
			throw new Exception("There are no API scripts detected in the BWP file '"+injectorsPackageFile.getAbsolutePath()+"'");
		}

		String bwpNameRecordCount=injectorsPackageFile.getName().substring(0,injectorsPackageFile.getName().indexOf('.'));

		File outputFolder=new File(outputFolderName,bwpNameRecordCount);
		File errorOutputFolder=new File(outputFolder,"error");
		File successOutputFolder=new File(outputFolder,"success");

		createSuccessAndErrorFolders(outputFolder,injectorsPackageFile);

		boolean hasAtLeastOneInjectorSuccess=false;
		boolean hasAtLeastOneInjectorError=false;

		Date today=new Date();
		String formattedDate=CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE.getDateFormat().format(today);

		int successHeaderCount=0;
		int errorHeaderCount=0;
		for (int i=0;i<injectors.size();i++) {
			Injector injector=injectors.get(i);
			String scriptGenerationIdAPI = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_API_PREFIX + (injector.getIndex() + 1));
			String inventoryFileName = scriptGenerationIdAPI + "." + APIAction.GENERATION_API_INVENTORY_SUFFIX + "." + SwiftBuildConstants.XML_FILE_EXTENSION;
			Inventory inventory = APIDataGridUtils.getInventory(injectorsPackageFile, inventoryFileName);
			File outputFile=new File(outputFolder,scriptGenerationIdAPI+".data.xml");
			outputFile.delete();

			List<String[]> dataGridRowsStringFormat=InventoryDataGridUtils.readDataGridRows(inventory.getName(),InventoryDataGridConstants.NAVIGATION_NA_VALUE,false);

			List<String[]> errorDataRows=new ArrayList<String[]>();
			List<String[]> successDataRows=new ArrayList<String[]>();
			for (String[] dataGridRowInStringFormat:dataGridRowsStringFormat) {
				String[] dataRow=InventoryDataGridUtils.convertDataGridRowToInventoryDataRow(dataGridRowInStringFormat);
				if (dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX].equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE)) {
					errorDataRows.add(dataRow);
				}
				else
				if (dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX].equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE)) {
					successDataRows.add(dataRow);
				}
			}
			if (!successDataRows.isEmpty()) {
				hasAtLeastOneInjectorSuccess=true;
			}
			if (!errorDataRows.isEmpty()) {
				hasAtLeastOneInjectorError=true;
			}

			if (i==0) {
				successHeaderCount=successDataRows.size();
				errorHeaderCount=errorDataRows.size();
			}

			File statusFile=new File(outputFolder,inventory.getName()+" - status-"+formattedDate+".xlsx");
			saveStatusToExcel(statusFile,inventory,dataGridRowsStringFormat);

			File errorDataFile=new File(errorOutputFolder,scriptGenerationIdAPI+".data.xml");
			exportData(errorDataFile,inventory,errorDataRows);
			File successDataFile=new File(successOutputFolder,scriptGenerationIdAPI+".data.xml");
			exportData(successDataFile,inventory,successDataRows);
		}

		File targetZipFile=new File(outputFolder,bwpNameRecordCount+"-success"+successHeaderCount+".bwp");
		if (hasAtLeastOneInjectorSuccess) {
			zipBWPFolder(successOutputFolder,targetZipFile);
		}
		org.apache.commons.io.FileUtils.deleteDirectory(successOutputFolder);

		targetZipFile=new File(outputFolder,bwpNameRecordCount+"-error"+errorHeaderCount+".bwp");
		if (hasAtLeastOneInjectorError) {
			zipBWPFolder(errorOutputFolder,targetZipFile);
		}
		org.apache.commons.io.FileUtils.deleteDirectory(errorOutputFolder);
	}

	private static void saveStatusToExcel(File statusFile,Inventory inventory,List<String[]> dataGridRowsInStringFormat)
	throws Exception {
		String[] headerRow=getHeaderRow(inventory);
		dataGridRowsInStringFormat.add(0,headerRow);
		FileUtils.createAndSaveRowsToXLSXFile("Execution Status grid",statusFile,dataGridRowsInStringFormat);
	}

	private static String[] getHeaderRow(Inventory inventory) throws Exception{
		List<DataGridColumn> dataGridControlColumns=InventoryDataGridUtils.getControlDataGridColumns(null);
		List<DataGridColumn> dataGridInventoryColumns=InventoryDataGridUtils.getInventoryDataGridColumns(null,inventory,null,false);
		DataGridColumn dataGridLabelColumn=InventoryDataGridUtils.getDataGridLabelColumn();
		DataGridColumn dataGridNavigationColumn=InventoryDataGridUtils.getDataGridNavigationComboColumn(new ArrayList<String>(),null);
		List<DataGridColumn> dataGridAllVisibleColumns=new ArrayList<DataGridColumn>();
		dataGridAllVisibleColumns.addAll(dataGridControlColumns);
		dataGridAllVisibleColumns.addAll(dataGridInventoryColumns);
		dataGridAllVisibleColumns.add(dataGridLabelColumn);
		dataGridAllVisibleColumns.add(dataGridNavigationColumn);

		String[] headerRow=new String[dataGridAllVisibleColumns.size()];
		int counter=0;
		for (DataGridColumn dataGridUserDefinedColumn:dataGridAllVisibleColumns) {
			headerRow[counter++]=dataGridUserDefinedColumn.getColumnTitle();
		}
		return headerRow;
	}

	private static void createSuccessAndErrorFolders(File outputFolder,File injectorsPackageFile) throws Exception {
		Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
		List<Injector> injectors=InjectorsPackageUtils.getInjectors(injectorsPackageFile,CoreConstants.INJECTOR_TYPE.TYPE_API);
		if (injectors.isEmpty()) {
			throw new Exception("There are no API scripts detected in the BWP file '"+injectorsPackageFile.getAbsolutePath()+"'");
		}

		File errorOutputFolder=new File(outputFolder,"error");
		errorOutputFolder.mkdirs();
		File successOutputFolder=new File(outputFolder,"success");
		successOutputFolder.mkdirs();

		unzipBWPFile(injectorsPackageFile,errorOutputFolder);
		unzipBWPFile(injectorsPackageFile,successOutputFolder);

		for (Injector injector:injectors) {
			String scriptGenerationIdAPI = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_API_PREFIX + (injector.getIndex() + 1));
			File outputFile=new File(errorOutputFolder,scriptGenerationIdAPI+".data.xml");
			outputFile.delete();
			outputFile=new File(successOutputFolder,scriptGenerationIdAPI+".data.xml");
			outputFile.delete();
		}
	}

	public static void unzipBWPFile(File bwpFile,File destinationFolder)
	throws Exception
	{
		File targetFile=bwpFile;
		if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(bwpFile) )
		{
			targetFile=new File(destinationFolder,bwpFile.getName());
			decryptFile(bwpFile,targetFile);
		}
		FileUtils.unpackFromZIPFile(targetFile,destinationFolder,null);
		//targetFile.delete();
	}

	public static void decryptFile(final File file,File targetFile) throws Exception
    {
		InputStream input=CoreUtil.getUnencryptedInputStream(file);
		FileOutputStream fos = null;
		try
		{
			targetFile.createNewFile();
			fos = new FileOutputStream(targetFile);
			fos.write(IOUtils.toByteArray(input));
		}
		finally
		{
			IOUtils.closeQuietly(fos);
		}
    }

	public static void zipBWPFolder(File srcFolder,File destZipFile) throws Exception {
		ZipOutputStream zipOutputStream = null;
		try{
			FileOutputStream fileOutputStream = new FileOutputStream(destZipFile);
			zipOutputStream = new ZipOutputStream(fileOutputStream);
			for (File file:srcFolder.listFiles()) {
				byte[] buf = new byte[1024];
				int len;
				FileInputStream in=null;
				try{
					in = new FileInputStream(file);
					zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
					while ((len = in.read(buf)) > 0) {
						zipOutputStream.write(buf, 0, len);
					}
				}
				finally{
					IOUtils.closeQuietly(in);
				}
			}
			zipOutputStream.flush();
		}
		finally{
			IOUtils.closeQuietly(zipOutputStream);
		}
	}

	public static void exportData(File outputFile,Inventory inventory,List<String[]> dataRows) throws Exception {
		PrintWriter pw=null;
		try {
			List<String> columns=new ArrayList<String>(inventory.getFieldNamesUsedForDataEntry());
			columns.add(InventoryDataGridConstants.EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_DESCRIPTION);
			columns.add(InventoryDataGridConstants.EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_DESCRIPTION);

			Set<String> validNavigationMappers=new HashSet<String>();
			validNavigationMappers.add(InventoryDataGridConstants.NAVIGATION_NA_VALUE);
			InventoryDataGridUtils.saveInventoryDataToXMLFile(outputFile,columns,validNavigationMappers,inventory.getName(),dataRows);
		}
		finally {
			if (pw!=null) {
				pw.close();
			}
		}
	}

	private static void uncheckInvalidRowsAndCheckSetModeForValidRows(File injectorsPackageFile,BuildMain buildMain) throws Exception
	{
		List<Injector> injectors=InjectorsPackageUtils.getInjectors(injectorsPackageFile,CoreConstants.INJECTOR_TYPE.TYPE_API);
		if (injectors.isEmpty()) {
			throw new Exception("There are no API scripts detected in the BWP file '"+injectorsPackageFile.getAbsolutePath()+"'");
		}
		Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
		for (Injector injector:injectors) {
			String scriptGenerationIdAPI = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_API_PREFIX + (injector.getIndex() + 1));
			String inventoryFileName = scriptGenerationIdAPI + "." + APIAction.GENERATION_API_INVENTORY_SUFFIX + "." + SwiftBuildConstants.XML_FILE_EXTENSION;
			Inventory inventory = APIDataGridUtils.getInventory(injectorsPackageFile, inventoryFileName);
			List<String[]> dataGridRowsStringFormat=InventoryDataGridUtils.readDataGridRows(inventory.getName(),InventoryDataGridConstants.NAVIGATION_NA_VALUE,false);

			for (String[] dataGridRowInStringFormat:dataGridRowsStringFormat) {
				if (dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX].equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE)) {
					dataGridRowInStringFormat[InventoryDataGridConstants.ENABLED_COLUMN_GRID_INDEX]="false";
				}
				else
				if (dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX].equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE)) {
					dataGridRowInStringFormat[InventoryDataGridConstants.ENABLED_COLUMN_GRID_INDEX]="true";
					dataGridRowInStringFormat[InventoryDataGridConstants.MESSAGE_COLUMN_GRID_INDEX+1]="Create";
				}
			}
			Map<String,List<String[]>> navigationToDataGridRowsMapToWrite=InventoryDataGridUtils.getNavigationToDataGridRowsMap(dataGridRowsStringFormat,null);
			InventoryDataGridUtils.writeDataGridRows(inventory.getName(),navigationToDataGridRowsMapToWrite,false,true);
		}
		refreshGrids(buildMain);
	}

	private static void refreshGrids(BuildMain buildMain)
	{
		if (buildMain!=null) {
			InjectorsPackageExecutionPanel injectorsPackageExecutionPanel=buildMain.getInjectorsExecutionPanel();
			Map<Integer, ActionInterface> injectorIndexToActionMap=injectorsPackageExecutionPanel.getInjectorIndexToActionMap();
			Iterator<Integer> iterator=injectorIndexToActionMap.keySet().iterator();
			while (iterator.hasNext()) {
				Integer key=iterator.next();
				APIAction apiAction=(APIAction)injectorIndexToActionMap.get(key);
				apiAction.getApiDataAction().getAPIDataGridController().refreshGridOrShowError();
			}
		}
	}

	private static void changeModeToGetErrorsForValidAndCreateModeRows(File injectorsPackageFile,BuildMain buildMain) throws Exception
	{
		List<Injector> injectors=InjectorsPackageUtils.getInjectors(injectorsPackageFile,CoreConstants.INJECTOR_TYPE.TYPE_API);
		if (injectors.isEmpty()) {
			throw new Exception("There are no API scripts detected in the BWP file '"+injectorsPackageFile.getAbsolutePath()+"'");
		}
		Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
		for (Injector injector:injectors) {
			String scriptGenerationIdAPI = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_API_PREFIX + (injector.getIndex() + 1));
			String inventoryFileName = scriptGenerationIdAPI + "." + APIAction.GENERATION_API_INVENTORY_SUFFIX + "." + SwiftBuildConstants.XML_FILE_EXTENSION;
			Inventory inventory = APIDataGridUtils.getInventory(injectorsPackageFile, inventoryFileName);
			List<String[]> dataGridRowsStringFormat=InventoryDataGridUtils.readDataGridRows(inventory.getName(),InventoryDataGridConstants.NAVIGATION_NA_VALUE,false);

			for (String[] dataGridRowInStringFormat:dataGridRowsStringFormat) {
				if (dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX].equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE)) {
					dataGridRowInStringFormat[InventoryDataGridConstants.ENABLED_COLUMN_GRID_INDEX]="true";
					dataGridRowInStringFormat[InventoryDataGridConstants.MESSAGE_COLUMN_GRID_INDEX+1]="Get Error";
				}
			}
			Map<String,List<String[]>> navigationToDataGridRowsMapToWrite=InventoryDataGridUtils.getNavigationToDataGridRowsMap(dataGridRowsStringFormat,null);
			InventoryDataGridUtils.writeDataGridRows(inventory.getName(),navigationToDataGridRowsMapToWrite,false,true);
		}
		refreshGrids(buildMain);
	}

	public static void massUpdateGrids(
			APIDataGridController apiDataGridController,File injectorsPackageFile,BuildMain buildMain,Map<DataGridColumn,String> dataGridColumnsToValueMap) throws Exception
	{
		List<Injector> injectors=InjectorsPackageUtils.getInjectors(injectorsPackageFile,CoreConstants.INJECTOR_TYPE.TYPE_API);
		if (injectors.isEmpty()) {
			throw new Exception("There are no API scripts detected in the BWP file '"+injectorsPackageFile.getAbsolutePath()+"'");
		}
		Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
		for (Injector injector:injectors) {
			String scriptGenerationIdAPI = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_API_PREFIX + (injector.getIndex() + 1));
			String inventoryFileName = scriptGenerationIdAPI + "." + APIAction.GENERATION_API_INVENTORY_SUFFIX + "." + SwiftBuildConstants.XML_FILE_EXTENSION;
			Inventory inventory = APIDataGridUtils.getInventory(injectorsPackageFile, inventoryFileName);

			if (inventory.getName().equalsIgnoreCase(apiDataGridController.getAPIDataAction().getInventory().getName())) {
				System.out.println("massUpdateGrids, skipping the grid: "+inventory.getName());
				continue;
			}
			System.out.println("massUpdateGrids, processing the grid: "+inventory.getName());

		}
		refreshGrids(buildMain);
	}

}