package com.rapidesuite.client.common.gui.datagrid.inventory;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import org.openswing.swing.client.GenericButton;
import org.openswing.swing.client.GridControl;
import org.openswing.swing.util.client.ClientSettings;
import org.openswing.swing.util.client.ClientUtils;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.client.common.gui.CustomFileFilter;
import com.rapidesuite.client.common.gui.datagrid.DataGridColumn;
import com.rapidesuite.client.common.gui.datagrid.DataGridController;
import com.rapidesuite.client.common.gui.datagrid.DataGridLookupController;
import com.rapidesuite.client.common.gui.datagrid.DataGridUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.inventory0007.Field;

@SuppressWarnings("serial")
public abstract class InventoryDataGridController extends DataGridController {
	
	private InventoryDataGridAction inventoryDataGridAction;
	private InventoryDataGridFrame inventoryDataGridFrame;
	private List<DataGridColumn> dataGridInventoryColumns;
	private DataGridColumn dataGridLabelColumn;
	private DataGridColumn dataGridNavigationColumn;
	private List<DataGridColumn> dataGridControlColumns;
	protected List<DataGridColumn> dataGridAllVisibleColumns;
	private Set<String> validNavigationMappers;
	protected Map<String, Integer> dataGridColumnAttributeDescriptionToDataPositionMap;
	
	private String domainIdPrefix;
	private boolean isProcessStarted;
	private boolean isDataLoadedInGrid;
	
	private InventoryDataGridImportButton importButton;
	private InventoryDataGridExportButton exportButton;
	private InventoryDataGridProcessButton processButton;
	private InventoryDataRowsFilter inventoryDataRowsFilter;
	private boolean isShowImportXMLButton;
	private boolean isShowExportXMLButton;
	
	private InventoryDataGridProcess inventoryDataGridProcess;
	
	public InventoryDataGridController(InventoryDataGridAction inventoryDataGridAction,String domainIdPrefix,boolean isShowImportXMLButton,
			boolean isShowExportXMLButton)
	throws Exception{
		this.inventoryDataGridAction=inventoryDataGridAction;
		this.domainIdPrefix=domainIdPrefix;
		this.isShowImportXMLButton=isShowImportXMLButton;
		this.isShowExportXMLButton=isShowExportXMLButton;
		InventoryDataGridUtils.initClientSettings();
	}
	
	public void initComponents(
			Set<String> validNavigationMappers,
			List<GenericButton> additionalButtons,
			String dataGridTitle,
			Inventory inventory,
			int frameWidth,
			int frameHeight,
			InventoryDataRowsFilter inventoryDataRowsFilter,
			InventoryDataGridProcess inventoryDataGridProcess,
			boolean isRequiredInformationOn)
	throws Exception{
		this.inventoryDataRowsFilter=inventoryDataRowsFilter;
		this.inventoryDataGridProcess=inventoryDataGridProcess;
		dataGridControlColumns=InventoryDataGridUtils.getControlDataGridColumns(domainIdPrefix);
		dataGridInventoryColumns=InventoryDataGridUtils.getInventoryDataGridColumns(this,inventory,domainIdPrefix,isRequiredInformationOn);
		
		dataGridLabelColumn=InventoryDataGridUtils.getDataGridLabelColumn();
		this.validNavigationMappers=validNavigationMappers;
		
		// showing the navigation as the default value in the pick list:
		validNavigationMappers.remove(inventoryDataGridAction.getNavigationName());
		List<String> list=new ArrayList<String>(validNavigationMappers);
		validNavigationMappers.add(inventoryDataGridAction.getNavigationName());
		list.add(0,inventoryDataGridAction.getNavigationName());
		dataGridNavigationColumn=InventoryDataGridUtils.getDataGridNavigationComboColumn(list,domainIdPrefix);
			
		dataGridAllVisibleColumns=new ArrayList<DataGridColumn>();
		dataGridAllVisibleColumns.addAll(dataGridControlColumns);
		dataGridAllVisibleColumns.addAll(dataGridInventoryColumns);
		dataGridAllVisibleColumns.add(dataGridLabelColumn);
		dataGridAllVisibleColumns.add(dataGridNavigationColumn);
		
		this.dataGridColumnAttributeDescriptionToDataPositionMap=DataGridUtils.getDataGridColumnAttributeDescriptionToDataPositionMap(dataGridAllVisibleColumns);
		
		List<GenericButton> allButtons=new ArrayList<GenericButton>();
		InventoryDataGridFilterErrorsButton filterErrorsButton = new InventoryDataGridFilterErrorsButton(
				this,GUIUtils.getImageIcon(this.getClass(),"/images/error.png"));
		filterErrorsButton.setToolTipText("Filter Errors");
		allButtons.add(filterErrorsButton);
		
		if (isShowImportXMLButton) {
			importButton = new InventoryDataGridImportButton(new ImageIcon(ClientUtils.getImage(ClientSettings.BUTTON_IMPORT_IMAGE_NAME)),this);
			importButton.setToolTipText("Import Data from XML");
			allButtons.add(importButton);
		}
		if (isShowExportXMLButton){
			exportButton= new InventoryDataGridExportButton(this,GUIUtils.getImageIcon(this.getClass(),"/images/xml.png"));
			exportButton.setToolTipText("Export Data to XML");
			allButtons.add(exportButton);
		}
		processButton= new InventoryDataGridProcessButton(GUIUtils.getImageIcon(this.getClass(),"/images/play16.gif"),this);
		processButton.setToolTipText("Start processing records");
		allButtons.add(processButton);
		allButtons.addAll(additionalButtons);
		
		super.initComponents(
				domainIdPrefix,
				dataGridAllVisibleColumns,
				allButtons,
				dataGridControlColumns.size(),
				InventoryDataGridConstants.ADDITIONAL_HEADER_COLUMN_CONTROL_TEXT,
				InventoryDataGridConstants.ADDITIONAL_HEADER_COLUMN_DATA_TEXT,
				true,
				true,
				true,
				true,
				true);
	
		inventoryDataGridFrame=new InventoryDataGridFrame(dataGridTitle,this,frameWidth,frameHeight);
	}
	
	public Map<String, Integer> getDataGridColumnAttributeDescriptionToDataPositionMap() {
		return dataGridColumnAttributeDescriptionToDataPositionMap;
	}
	
	public Set<String> getValidNavigationMapperNames() {
		return validNavigationMappers;
	}
	
	public abstract DataGridLookupController getDataGridLookupController(String attributeName,Field field) throws Exception;
	
	/**
	 * Method used to define the background color for each cell of the grid.
	 * @param rowNumber selected row index
	 * @param attributeName attribute name related to the column currently selected
	 * @param value object contained in the selected cell
	 * @return background color of the selected cell
	 */
	public Color getBackgroundColor(int row,String attributeName,Object value) {
		try {
			// Note: row always starts at 0 to the number of rows displayed, it does not account for the page number!!
			if ( attributeName.equalsIgnoreCase(InventoryDataGridConstants.EXTRA_COLUMN_STATUS_ATTRIBUTE_NAME)) {
				if (value!=null && value.toString().equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE)) {
					return InventoryDataGridConstants.INVALID_OR_ERROR_COLOR;
				}
				else
				if (value!=null && value.toString().equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE)) {
					return InventoryDataGridConstants.SUCCESS_COLOR;
				}
				else
					if (value!=null && value.toString().equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_PENDING_VALUE)) {
						return InventoryDataGridConstants.UNPROCESSED_COLOR;
					}
			}
			Object object= getDataGriRowForUIRowIndex(row);
			if (object!=null) {
				Boolean isEnabled = (Boolean)getDataGridValue(InventoryDataGridConstants.EXTRA_COLUMN_ENABLED_ATTRIBUTE_NAME,object);
				if (isEnabled!=null && !isEnabled.booleanValue()) {
					return new Color(196,186,181);
				}
			}
			//return Color.white;
			return super.getBackgroundColor(row,attributeName,value);
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			return super.getBackgroundColor(row,attributeName,value);
		}
	}
	
	public String getCellTooltip(int row,String attributeName) {
		Object object= getDataGridValueForUIRowIndex(attributeName,row);
		if (object ==null) {
			return "";
		}
		return "<html>"+object.toString().replaceAll("\n","<br/>")+"</html>";
	}
		
	public void rowChanged(int rowNumber) {
		String message=null;
		try {
			message = (String)getDataGridValueForUIRowIndex(InventoryDataGridConstants.EXTRA_COLUMN_MESSAGE_ATTRIBUTE_NAME,rowNumber);
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		if (message!=null) {
			message=message.replaceAll("##","");
		}
		getInventoryDataGridFrame().setErrorMessage(message);
	}
	
	public abstract boolean isCellEditable(GridControl grid,int row,String attributeName);
		
	public void afterReloadGrid()  {
		disableReloadButton();
	}
		
	public boolean beforeInsertGrid(GridControl grid) {
		return true;
	}
		
	public InventoryDataGridFrame getInventoryDataGridFrame() {
		return inventoryDataGridFrame;
	}
	
	public void disableReloadButton(){
		Runnable r=new Runnable(){
    		public void run(){
    			try {
    				com.rapidesuite.client.common.util.Utils.sleep(1000);
    				getDataGridPanel().getReloadButton().setEnabled(false);
				} catch (Exception ex) {
					FileUtils.printStackTrace(ex);
				}
    		}
    	};
    	Thread t=new Thread(r);
    	t.start();
	}

	private void updateProcessButtonToProcessStarted() {
		if (processButton==null) {
			return;
		}
		processButton.setText("Stop");
		processButton.setToolTipText("Stop");
		processButton.setIcon(GUIUtils.getImageIcon(this.getClass(),"/images/stop16.gif"));
		updateInProgressStatusLabel("Starting process...");
	}	
	
	public boolean isDataLoadedInGrid() {
		return isDataLoadedInGrid;
	}
	
	public void updateInProgressStatusLabel(String message) {
		GUIUtils.showInProgressMessage(inventoryDataGridFrame.getStatusLabel(),message);
	}

	private void updateProcessButtonToProcessStopped() {
		if (processButton!=null) {
			processButton.setText("Start");
			processButton.setToolTipText("Start");
			processButton.setIcon(GUIUtils.getImageIcon(this.getClass(),"/images/play16.gif"));
		}
	}
	
	public void setProcessButtontoStartedAndLockGrid() {
		isProcessStarted=true;
		updateProcessButtonToProcessStarted();
		lockGrid();
	}
	
	public boolean isProcessStarted() {
		return isProcessStarted;
	}
	
	public void setProcessButtontoStoppedAndUnLockGrid() {
		isProcessStarted=false;
		updateProcessButtonToProcessStopped();
		unlockGrid();
	}
		
	public List<DataGridColumn> getDataGridInventoryColumns() {
		return dataGridInventoryColumns;
	}
	
	public List<String[]> getInventoryDataRows() throws Exception {
		List<String[]> dataGridRowsInStringFormat=getDataGridRows();
		if (filteredColumnsMap!=null) {
			dataGridRowsInStringFormat=filterDataGridRows(dataGridRowsInStringFormat,filteredColumnsMap);
		}
		
		List<String[]> toReturn = new ArrayList<String[]>();
		int inventoryDataColumnsCount=dataGridInventoryColumns.size()+2;
		for (String[] dataGridRow:dataGridRowsInStringFormat) {
			String[] dataRow=new String[ inventoryDataColumnsCount];
			toReturn.add(dataRow);
			int startGridIndex=dataGridControlColumns.size();
			for (int i=0;i<inventoryDataColumnsCount;i++) {
				dataRow[i]=dataGridRow[startGridIndex];
				startGridIndex++;
			}
		}
		return toReturn;
	}
	
	public void setDataGridVisible(boolean isVisible)  {
		inventoryDataGridFrame.setVisible(isVisible);
		if (isVisible) {
			//inventoryDataGridFrame.setSize(InventoryDataGridConstants.FRAME_WIDTH,InventoryDataGridConstants.FRAME_HEIGHT);
			
			JTableHeader jth=getDataGridPanel().getGridControl().getTable().getGrid().getTableHeader();
			jth.setBackground(InventoryDataGridConstants.TABLE_HEADER_BACKGROUND_COLOR);	
			SwingUtilities.invokeLater(new Runnable() {  
	            public void run() {  
	            	inventoryDataGridFrame.toFront();
	            	inventoryDataGridFrame.requestFocus();
	            }  
	          });  
			
			/*
			 * Setting the grid in full screen causes empty columns to show up between the locked columns
			 * and the main columns.
			 * Forcing the locked scroll pane to not move in full screen mode.
			 */
			getDataGridPanel().setLockedGridScrollPaneDimension();
		}
	}

	public void exportInventoryData() throws Exception {
		JFileChooser fileChooser=Utils.initializeJFileChooserWithTheLastPath("FILE_CHOOSER_INVENTORY_DATA_GRID_CONTROLLER_EXPORT_INVENTORY_DATA");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		Map<String,String> filesExtensionAllowed=new HashMap<String,String>();
		filesExtensionAllowed.put(SwiftBuildConstants.XML_FILE_EXTENSION ,SwiftBuildConstants.XML_FILE_EXTENSION );
		CustomFileFilter filter=new CustomFileFilter(filesExtensionAllowed);
		fileChooser.setFileFilter(filter);
		File outputFile=new File(inventoryDataGridAction.getInventory().getName()+"."+SwiftBuildConstants.XML_FILE_EXTENSION);
		fileChooser.setSelectedFile(outputFile);
		int returnVal = fileChooser.showDialog(inventoryDataGridFrame, "Select File");
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			outputFile = fileChooser.getSelectedFile();
			InventoryDataGridUtils.exportInventoryData(outputFile,inventoryDataGridAction.getInventory().getName(),
					InventoryDataGridUtils.getColumnNameList(dataGridInventoryColumns),
						validNavigationMappers,getInventoryDataRows());
		}
	}

	public void exportGridStatusToExcel(boolean isOpenFile) throws Exception {
		exportGridStatusToExcel(inventoryDataGridAction.getInventory().getName()+".xlsx",isOpenFile);
	}
	
	public void exportGridStatusToExcel(String outputFileName,boolean isOpenFile) throws Exception {
		try{
			DataGridUtils.askAndCreateAndSaveXLSXExcelFile(getFilteredDataGridRowsWithHeaderRow(),outputFileName,isOpenFile, false);
		}
		catch (Exception e) {
			GUIUtils.popupErrorMessage("Unable to save/ open the grid status file. Error: "+e.getMessage());
		}
	}
	
	public void exportGridStatusToExcelForOpenTicket(final File outputFolder) throws Exception {
		try{
			final String fileName = inventoryDataGridAction.getInventory().getName()+".xlsx";
		    File outputFile = new File(outputFolder, fileName);
			DataGridUtils.doCreateAndSaveXLSXExcelFile(getFilteredDataGridRowsWithHeaderRow(), outputFile, false, false);
		}
		catch (Exception e) {
			GUIUtils.popupErrorMessage("Unable to save/ open the grid status file. Error: "+e.getMessage());
		}
	}

	public List<String[]> getFilteredDataGridRowsWithHeaderRow() throws Exception {
		List<String[]> dataGridRowsInStringFormat=getDataGridRows();
		if (filteredColumnsMap!=null) {
			dataGridRowsInStringFormat=filterDataGridRows(dataGridRowsInStringFormat,filteredColumnsMap);
		}
		int rowSequence=1;
		for (String[] row:dataGridRowsInStringFormat) {
			row[InventoryDataGridConstants.ROW_SEQUENCE_COLUMN_GRID_INDEX]=String.valueOf(rowSequence++);
		}
		String[] headerRow=getHeaderDataGrid();
		dataGridRowsInStringFormat.add(0,headerRow);
		return dataGridRowsInStringFormat;
	}
	
	public abstract void close();
	
	public void hideDataGridFrame() {
		setDataGridVisible(false);
	}
		
	public List<DataGridColumn> getDataGridControlColumns() {
		return dataGridControlColumns;
	}
	
	public void setFilteringForDataGridRowErrors() {
		DataGridColumn dataGridColumn=getDataGridColumnAttributeNameToDataGridColumnMap().get(InventoryDataGridConstants.EXTRA_COLUMN_STATUS_ATTRIBUTE_NAME);
		setFilterColumn(dataGridColumn,"=",InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE);
	}
	
	public boolean hasDataGridRowErrors() throws Exception {
		List<String[]> dataGridRowsStringFormat=getDataGridRows();
		if (dataGridRowsStringFormat==null) {
			return false;
		}
		for (String[] dataGridRowInStringFormat:dataGridRowsStringFormat) {
			if (dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX].equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE)) {
				return true;
			}
		}
		return false;
	}

	public List<DataGridColumn> getDataGridAllVisibleColumns() {
		return dataGridAllVisibleColumns;
	}
	
	public void openDataGrid() throws Exception {
		setDataGridVisible(true);
		refreshGrid(true);
	}
			
	public List<String[]> getDataGridRows() throws Exception {
		return InventoryDataGridUtils.readDataGridRows(inventoryDataGridAction.inventory.getName(),inventoryDataGridAction.getNavigationName(),false);
	}
		
	public void refreshGrid(boolean isResetFiltering) throws Exception {
		if (isResetFiltering) {
			resetFiltering();
		}
		reloadData();
		disableReloadButton();
	}
		
	public void serializeDataGridRows(List<String[]> allDataGridRowsForThatTableAndNavigation,String overridesLabel,boolean isAfterProcess,
			boolean isRemoveAllSerializedFiles) throws Exception {
		Map<String,List<String[]>> navigationToDataGridRowsMapToWrite=InventoryDataGridUtils.getNavigationToDataGridRowsMap(allDataGridRowsForThatTableAndNavigation,overridesLabel);
		if (!isAfterProcess) {
			// add all data from all the other navigations for that table!!!!!!!
			Set<String> allNavigationNamesInUse=InventoryDataGridUtils.getAllNavigationNamesInUse(inventoryDataGridAction.getInventory().getName());
			allNavigationNamesInUse.remove(inventoryDataGridAction.getNavigationName());
			
			Iterator<String> iterator= allNavigationNamesInUse.iterator();
			while (iterator.hasNext()) {
				String navigationNameToRead=iterator.next();
				List<String[]> dataRows=InventoryDataGridUtils.readDataGridRows(inventoryDataGridAction.getInventory().getName(),navigationNameToRead,false);
				navigationToDataGridRowsMapToWrite.put(navigationNameToRead,dataRows);
			}
		}
		InventoryDataGridUtils.writeDataGridRows(inventoryDataGridAction.getInventory().getName(),navigationToDataGridRowsMapToWrite,!isAfterProcess,isRemoveAllSerializedFiles);
	}
	
	public InventoryDataGridAction getInventoryDataGridAction() {
		return inventoryDataGridAction;
	}
	
	public void importXMLDataFile(String overridesLabel){
		JFileChooser fileChooser=Utils.initializeJFileChooserWithTheLastPath("FILE_CHOOSER_INVENTORY_DATA_GRID_CONTROLLER");
		CustomFileFilter filter=new CustomFileFilter(UtilsConstants.XML_FILE_EXTENSION);
		fileChooser.setFileFilter(filter);
		
		int returnVal = fileChooser.showDialog(getInventoryDataGridFrame(), "Select a XML data file");
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			try {
				List<String[]> dataGridRows=InventoryDataGridUtils.processXMLData(
						new FileInputStream(file),inventoryDataGridAction.inventory.getName(),dataGridAllVisibleColumns.size(),overridesLabel);
				dataGridRows=inventoryDataRowsFilter.filter(dataGridRows);
				serializeDataGridRows(dataGridRows,overridesLabel,false,false);
				refreshGrid(true);
			} 
			catch (Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage("Error: "+e.getMessage());
			}
		}
	}

	public void executeProcess() {
		inventoryDataGridProcess.execute();
	}
	
	public String[] getHeaderDataGrid() {
		String[] headerRow=new String[dataGridAllVisibleColumns.size()];
		int counter=0;
		for (DataGridColumn dataGridUserDefinedColumn:dataGridAllVisibleColumns) {
			headerRow[counter++]=dataGridUserDefinedColumn.getColumnTitle();
		}
		return headerRow;
	}
	
}
