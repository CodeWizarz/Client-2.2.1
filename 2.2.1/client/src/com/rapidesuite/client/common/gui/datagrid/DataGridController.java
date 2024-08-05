package com.rapidesuite.client.common.gui.datagrid;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openswing.swing.client.CopyButton;
import org.openswing.swing.client.DeleteButton;
import org.openswing.swing.client.FilterButton;
import org.openswing.swing.client.GenericButton;
import org.openswing.swing.client.GridControl;
import org.openswing.swing.client.InsertButton;
import org.openswing.swing.client.ReloadButton;
import org.openswing.swing.client.SaveButton;
import org.openswing.swing.domains.java.Domain;
import org.openswing.swing.message.receive.java.ErrorResponse;
import org.openswing.swing.message.receive.java.Response;
import org.openswing.swing.message.receive.java.VOListResponse;
import org.openswing.swing.message.receive.java.VOResponse;
import org.openswing.swing.message.receive.java.ValueObject;
import org.openswing.swing.message.send.java.FilterWhereClause;
import org.openswing.swing.message.send.java.GridParams;
import org.openswing.swing.table.client.Grid;
import org.openswing.swing.table.client.GridController;
import org.openswing.swing.table.client.Grids;
import org.openswing.swing.table.columns.client.Column;
import org.openswing.swing.table.columns.client.DateTimeColumn;
import org.openswing.swing.table.java.GridDataLocator;
import org.openswing.swing.util.java.Consts;

import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridConstants;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;

@SuppressWarnings("serial")
public abstract class DataGridController extends GridController implements GridDataLocator {

	protected DataGridPanel dataGridPanel;
	private DataGridResetFilterButton resetFilterButton;
	private boolean isReloadFromStart;
	private String dataGridRowClassName;
	private Class<?> generatedClass;
	protected Map<String,DataGridColumn> dataGridColumnAttributeNameToDataGridColumnMap;
	protected Map<String,DataGridColumn> dataGridColumnAttributeDescriptionToDataGridColumnMap;

	private List<DataGridColumn> dataGridUserDefinedColumns;
	private List<DataGridColumn> allDataGridColumns;
	private int previousMode;
	private DataGridMassUpdateFrame massUpdateFrame;
	private int numberOfUserControlColumns;
	protected Map<String,FilterWhereClause[]> filteredColumnsMap;
	private Map<String,Integer> attributeDescriptionToPositionMap;
	private int startIndex;
	private boolean isFirstLoadCompletedEventReceived;
	
	public DataGridController(){
		startIndex=0;
	}
	
	public void initComponents(
			String domainIdPrefix,
			List<DataGridColumn> dataGridUserDefinedColumns,
			List<GenericButton> additionalButtons,
			int numberOfUserControlColumns,
			String additionalHeaderColumnNameForLockedColumns,
			String additionalHeaderColumnNameForUnLockedColumns,
			boolean isEditRecordEnabled,
			boolean isInsertNewRecordAllowed,
			boolean isDeleteRecordAllowed,
			boolean isCopyRecordAllowed,
			boolean isMassUpdateRecordsAllowed)
	throws Exception{
		this.dataGridUserDefinedColumns=dataGridUserDefinedColumns;
		this.numberOfUserControlColumns=numberOfUserControlColumns;
	
		if (additionalHeaderColumnNameForLockedColumns!=null) {
			Column column=dataGridUserDefinedColumns.get(0).getColumn();
			column.setAdditionalHeaderColumnName(additionalHeaderColumnNameForLockedColumns);
			column.setAdditionalHeaderColumnSpan(numberOfUserControlColumns);
		}
		if (additionalHeaderColumnNameForUnLockedColumns!=null) {
			Column column=dataGridUserDefinedColumns.get(numberOfUserControlColumns).getColumn();
			column.setAdditionalHeaderColumnName(additionalHeaderColumnNameForUnLockedColumns);
			int numberOfDataColumns=dataGridUserDefinedColumns.size()-numberOfUserControlColumns;
			column.setAdditionalHeaderColumnSpan(numberOfDataColumns);
		}
		
		allDataGridColumns=new ArrayList<DataGridColumn>();
		allDataGridColumns.addAll(dataGridUserDefinedColumns);
		
		attributeDescriptionToPositionMap=new HashMap<String,Integer>();
		for (int i=0;i<dataGridUserDefinedColumns.size();i++) {
			DataGridColumn datagridColumn=dataGridUserDefinedColumns.get(i);
			attributeDescriptionToPositionMap.put(datagridColumn.getAttributeDescription(),i);
		}

		this.dataGridColumnAttributeNameToDataGridColumnMap=
			DataGridUtils.getDataGridColumnAttributeNameToDataGridColumnMap(allDataGridColumns);
		this.dataGridColumnAttributeDescriptionToDataGridColumnMap=
			DataGridUtils.getDataGridColumnAttributeDescriptionToDataGridColumnMap(allDataGridColumns);
		
		int randomNumber=Utils.getRandomNumber();
		dataGridRowClassName=DataGridConstants.GENERATED_CLASS_FULL_NAME_PREFIX+randomNumber;
		this.generatedClass=DataGridRowObjectUtils.generateDataGridRowClassFile(dataGridRowClassName,allDataGridColumns);		
		
		/*
		 * setting openswing RESOURCES
		 */
		Map<String,String> attributeNamesToColumnTitleMap=DataGridUtils.getAttributeNamesToColumnTitleMap(allDataGridColumns);
		if (additionalHeaderColumnNameForLockedColumns!=null) {
			attributeNamesToColumnTitleMap.put(additionalHeaderColumnNameForLockedColumns,additionalHeaderColumnNameForLockedColumns);
		}
		if (additionalHeaderColumnNameForUnLockedColumns!=null) {
			attributeNamesToColumnTitleMap.put(additionalHeaderColumnNameForUnLockedColumns,additionalHeaderColumnNameForUnLockedColumns);
		}
		// Lookup frame domains:
		attributeNamesToColumnTitleMap.put(DataGridConstants.LOOKUP_FRAME_TITLE,DataGridConstants.LOOKUP_FRAME_TITLE);
		attributeNamesToColumnTitleMap.put(DataGridConstants.LOOKUP_CODE_ATTRIBUTE_NAME,DataGridConstants.LOOKUP_CODE_ATTRIBUTE_NAME);
		for (int i=0;i<=8;i++) {
			String columnName="column"+(i+1);
			attributeNamesToColumnTitleMap.put(columnName,columnName);
		}
		Map<String,List<SimpleEntry<String,String>>> attributeNamesToKeyValuePairs=DataGridUtils.getAttributeNamesToKeyValuePairsMap(allDataGridColumns);
		Properties properties=DataGridClientApplication.getClientSettingsProperties(attributeNamesToColumnTitleMap);
		Hashtable<String,Domain> domains=DataGridClientApplication.getClientSettingsDomains(domainIdPrefix,properties,attributeNamesToKeyValuePairs);
		DataGridClientApplication.initializeResources(properties,domains);
			
		List<GenericButton> allButtons=new ArrayList<GenericButton>();
		DataGridEditButton editButton =null;
		SaveButton saveButton =null;
		ReloadButton reloadButton  =null;
		if (isEditRecordEnabled) {
			editButton = new DataGridEditButton(this,GUIUtils.getImageIcon(this.getClass(),"/images/edit.gif"));
			editButton.setToolTipText("Edit mode");
			saveButton = new SaveButton();
			reloadButton = new ReloadButton();
			reloadButton.setIcon(GUIUtils.getImageIcon(this.getClass(),"/images/chiuso.gif")); 
		}
		InsertButton insertButton=null;
		if (isInsertNewRecordAllowed) {
			insertButton = new InsertButton();
		}
		DeleteButton deleteButton=null;
		if (isDeleteRecordAllowed) {
			deleteButton = new DeleteButton();
		}
		CopyButton copyButton=null;
		if (isCopyRecordAllowed) {
			copyButton = new CopyButton();
		}
		FilterButton filterButton = new FilterButton();
		
		resetFilterButton= new DataGridResetFilterButton(this,GUIUtils.getImageIcon(this.getClass(),"/images/filter-undo.gif"));
		resetFilterButton.setToolTipText("Reset filtering");
		DataGridMassDeleteButton deleteAllButton= new DataGridMassDeleteButton(this,GUIUtils.getImageIcon(this.getClass(),"/images/delall.gif"));
		deleteAllButton.setToolTipText("Delete all filtered records");
		
		if (isEditRecordEnabled) {
			allButtons.add(editButton);
			allButtons.add(saveButton);
			allButtons.add(reloadButton);
		}
				
		if (isInsertNewRecordAllowed) {
			allButtons.add(insertButton);
		}
		if (isCopyRecordAllowed) {
			allButtons.add(copyButton);
		}
		if (isMassUpdateRecordsAllowed) {
			DataGridMassUpdateButton massUpdateButton= new DataGridMassUpdateButton(this,
					GUIUtils.getImageIcon(this.getClass(),"/images/grid.gif"));
			massUpdateButton.setToolTipText("Mass update");
			allButtons.add(massUpdateButton);
		}
		if (isDeleteRecordAllowed) {
			allButtons.add(deleteButton);
			allButtons.add(deleteAllButton);
		}
		allButtons.add(filterButton);
		allButtons.add(resetFilterButton);
		if (additionalButtons!=null) {
			allButtons.addAll(additionalButtons);
		}
		
		dataGridPanel=new DataGridPanel(
				DataGridUtils.getColumns(allDataGridColumns),
				dataGridRowClassName,
				true,
				allButtons,
				numberOfUserControlColumns);
		dataGridPanel.getGridControl().setController(this);
		dataGridPanel.getGridControl().setGridDataLocator(this);
		if (dataGridPanel.getReloadButton()!=null) {
			dataGridPanel.getReloadButton().setToolTipText("Cancel Edit record (CRTL + Z)");
		}
		dataGridPanel.getFilterButton().setToolTipText("Filtering (CRTL + F)");
		if (dataGridPanel.getDeleteButton()!=null) {
			dataGridPanel.getDeleteButton().setToolTipText("Delete selected record (CRTL + D)");
		}
		if (dataGridPanel.getCopyButton()!=null) {
			dataGridPanel.getCopyButton().setToolTipText("Copy selected record");
		}
		if (dataGridPanel.getInsertButton()!=null) {
			dataGridPanel.getInsertButton().setToolTipText("Insert new row(s) (CRTL + I) - press DOWN key for additional rows.");
		}
	}
	
	public DataGridPanel getDataGridPanel() {
		return dataGridPanel;
	}

	public void setDataGridPanel(DataGridPanel dataGridPanel) {
		this.dataGridPanel = dataGridPanel;
		dataGridPanel.getGridControl().setController(this);
		dataGridPanel.getGridControl().setGridDataLocator(this);
	}

	public void resetFiltering() {
		resetFilteredColumnsMap();
		getDataGridPanel().getGridControl().getQuickFilterValues().clear();
		reloadDataFromStart();
	}
	
	public void showMassUpdateDataGridRowsFrame() {
		try{
			if (massUpdateFrame!=null) {
				massUpdateFrame.setVisible(false);
				massUpdateFrame.dispose();
			}
			massUpdateFrame=new DataGridMassUpdateFrame(this);
			massUpdateFrame.setVisible(true);
		}
		catch(Exception ex)
		{
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}
	
	public abstract void serializeDataGridRows(List<String[]> dataGridRows)
	throws Exception;
	
	
	/**
	 * Callback method invoked each time the grid mode is changed.
	 */
	public void modeChanged(int currentMode) {
		if ((  previousMode==Consts.INSERT ||
			  (previousMode==Consts.EDIT && hasFilteringOn())
			)&& currentMode==Consts.READONLY)
		{
			previousMode=currentMode;
			getDataGridPanel().getGridControl().reloadData();
		}
		else {
			previousMode=currentMode;
		}
	}

	/**
	 * Call back method invoked to load data on the grid.
	 * @param action fetching versus: PREVIOUS_BLOCK_ACTION, NEXT_BLOCK_ACTION or LAST_BLOCK_ACTION
	 * @param startPos start position of data fetching in result set
	 * @param filteredColumns filtered columns
	 * @param currentSortedColumns sorted columns
	 * @param currentSortedVersusColumns ordering versus of sorted columns
	 * @param valueObjectType v.o. type
	 * @param otherGridParams other grid parameters
	 * @return response from the server: an object of type VOListResponse if data loading was successfully completed, or an ErrorResponse onject if some error occours
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Response loadData(
			int action,
			int startGridIndex,
			Map filteredColumns,
			ArrayList currentSortedColumns,
			ArrayList currentSortedVersusColumns,
			Class valueObjectType,
			Map otherGridParams) {
		try {			 
			getDataGridPanel().resetDisplayRelativeRecordCount(); 
			boolean isLastBlock=false;
			boolean isForward=true;
			if (action==GridParams.LAST_BLOCK_ACTION) {
				isLastBlock=true;
			}
			else
			if (action==GridParams.PREVIOUS_BLOCK_ACTION) {
				isForward=false;
			}
			return getDataGridResponse(filteredColumns,startGridIndex,isForward,isLastBlock) ;
		}
		catch (Exception ex) {
			FileUtils.printStackTrace(ex);
			return new ErrorResponse(ex.getMessage());
		}
	}	
	
	public boolean beforeEditGrid(GridControl grid) {
		return true;
	}
	
	/**
	 * Method used to define the background color for each cell of the grid.
	 * @param rowNumber selected row index
	 * @param attributeName attribute name related to the column currently selected
	 * @param value object contained in the selected cell
	 * @return background color of the selected cell
	 */
	public Color getBackgroundColor(int row,String attributeName,Object value) {
		return super.getBackgroundColor(row,attributeName,value);
	}

	public void setReloadFromStart(boolean isReloadFromStart) {
		this.isReloadFromStart = isReloadFromStart;
	}

	public Map<String, DataGridColumn> getDataGridColumnAttributeNameToDataGridColumnMap() {
		return dataGridColumnAttributeNameToDataGridColumnMap;
	}
	
	public Map<String, DataGridColumn> getDataGridColumnAttributeDescriptionToDataGridColumnMap() {
		return dataGridColumnAttributeDescriptionToDataGridColumnMap;
	}

	public List<DataGridColumn> getDataGridUserDefinedColumns() {
		return dataGridUserDefinedColumns;
	}

	public List<DataGridColumn> getAllDataGridColumns() {
		return allDataGridColumns;
	}

	public Class<?> getGeneratedClass() {
		return generatedClass;
	}

	public abstract List<String[]> getDataGridRows() throws Exception;
		
	public Object getDataGridValueForUIRowIndex(String attributeName,int uiRowId) {
		return dataGridPanel.getGridControl().getTable().getVOListTableModel().getField(uiRowId,attributeName);
	}
	
	public Object getDataGriRowForUIRowIndex(int uiRowId) {
		return dataGridPanel.getGridControl().getTable().getVOListTableModel().getObjectForRow(uiRowId);
	}
	
	protected void reloadDataFromStart() {
		if (getDataGridPanel().getGridControl().getTable()!=null) {
			getDataGridPanel().getGridControl().getTable().setListenEvent(true);
			getDataGridPanel().getGridControl().getTable().reloadDataFromStart();
		}
	}
	
	public void reloadData() {
		getDataGridPanel().getGridControl().reloadData();
	}
	
	public void setReadOnlyMode() {
		getDataGridPanel().getGridControl().setMode(Consts.READONLY);
	}
	
	public void loadDataCompleted(boolean error) {
		isFirstLoadCompletedEventReceived=true;
		resetFilterButton.setEnabled(hasFilteringOn());
		if (isReloadFromStart) {
			isReloadFromStart=false;
			reloadDataFromStart();
		}
	}
			
	public void setVisibleColumn(String attributeName,boolean isVisible){
		int columnIndex=dataGridPanel.getGridControl().getTable().getGrid().getColumnIndex(attributeName);
		if (!isVisible) {
			dataGridPanel.getGridControl().getTable().getGrid().setVisibleColumn(columnIndex,false);
		}
		else {
			dataGridPanel.getGridControl().getTable().getGrid().setVisibleColumn(columnIndex,true);
		}
	}
	
	public Grid getLockedGrid(){
		return getDataGridPanel().getGridControl().getTable().getGrid().getGrids().getLockedGrid();
	}
	
	/* Callback method invoked when a grid cell is selected.  */
	public void selectedCell(int rowNumber,
            int columnIndex,
            String attributeName,
            ValueObject persistentObject) {
	}
		
	public void lockGrid() {
		dataGridPanel.setButtonsStatus(false);
	}
	
	public void unlockGrid() {
		if (dataGridPanel!=null) {
			dataGridPanel.setButtonsStatus(true);
		}
	}

	public DataGridMassUpdateFrame getMassUpdateFrame() {
		return massUpdateFrame;
	}
		
	public void edit() {
		Grids grid=getDataGridPanel().getGridControl().getTable();
		if (grid.getMode()==Consts.READONLY) {
			if (!grid.getGridController().beforeEditGrid(grid.getGridControl())) {
				return;
			}
			grid.setMode(Consts.EDIT);
			if (grid.getInsertButton()!=null) {
				grid.getInsertButton().setEnabled(false);
			}
			if (grid.getExportButton()!=null) {
				grid.getExportButton().setEnabled(false);
			}
			if (grid.getImportButton() != null) {
				grid.getImportButton().setEnabled(false);
			}
			if (grid.getCopyButton()!=null) {
				grid.getCopyButton().setEnabled(false);
			}
			if (grid.getDeleteButton()!=null) {
				grid.getDeleteButton().setEnabled(false);
			}
			if (grid.getEditButton()!=null) {
				grid.getEditButton().setEnabled(false);
			}
			if (grid.getFilterButton()!=null) {
				grid.getFilterButton().setEnabled(false);
			}
			grid.setGenericButtonsEnabled(false);
			if (grid.getReloadButton()!=null) {
				grid.getReloadButton().setEnabled(true);
			}
			if (grid.getSaveButton()!=null) {
				grid.getSaveButton().setEnabled(true);
			}
			grid.resetButtonsState();
		}
	}
	
	public String[] getSelectedDataGridRow() throws Exception {
		int index=getDataGridPanel().getGridControl().getSelectedRow();
		Object dataGridRow=getDataGridPanel().getGridControl().getTable().getVOListTableModel().getObjectForRow(index);
		return convertDataGridRowObjectToDataGridRowInStringFormat(dataGridRow);
	}

	public int getNumberOfUserControlColumns() {
		return numberOfUserControlColumns;
	}
	
	@SuppressWarnings("unchecked")
	public void setFilterColumn(DataGridColumn dataGridColumn,String operator,String value) {
		Grids grids=getDataGridPanel().getGridControl().getTable();
		if (grids!=null) {
			grids.getQuickFilterValues().put(
					dataGridColumn.getColumn().getColumnName(),
		           new FilterWhereClause[] {
		              new FilterWhereClause(dataGridColumn.getColumn().getColumnName(),operator,value),
		              null
		            });
		}
	}

	public String getDataGridRowClassName() {
		return dataGridRowClassName;
	}
	
	public void afterEditGrid(GridControl grid) {
	}
	
	public void afterInsertGrid(GridControl grid) {
		getDataGridPanel().getReloadButton().setEnabled(false);
		refreshGridOrShowError();
	}
	
	public void afterMassDelete(){
		refreshGridOrShowError();
	}
	
	public void afterMassUpdate(){
		refreshGridOrShowError();
	}
	
	public void refreshGridOrShowError(){
		try {
			refreshGrid(false);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to reload the grid!");
		}
	}
	
	public void afterDeleteGrid(){
	}
		
	protected List<String[]> convertDataGridRowObjectsToDataGridRowsInStringFormat(List<Object> rowObjects) 
	throws Exception {
		List<String[]> res=new ArrayList<String[]>();
		for (Object rowObject:rowObjects) {
			String[] dataRow=convertDataGridRowObjectToDataGridRowInStringFormat(rowObject);
			res.add(dataRow);
		}
		return res;
	}
	
	protected String[] convertDataGridRowObjectToDataGridRowInStringFormat(Object rowObject) 
	throws Exception {	
		int startIndex=0;
		int endIndex=getDataGridUserDefinedColumns().size();
		return convertDataGridRowObjectToDataRow(startIndex,endIndex,rowObject); 	
	}
	
	protected List<Object> convertDataRowsToDataGridRowObjects(List<String[]> dataRows) 
	throws Exception {
		List<Object> toReturn=new ArrayList<Object>();
		for (int i=0;i<dataRows.size();i++) {
			String[] row=dataRows.get(i);
			List<String> errorMessages=new ArrayList<String>();
			Object object=convertDataRowToDataGridRowObject(row,errorMessages); 
			toReturn.add(object);
			if (!errorMessages.isEmpty()) {
				for (String errorMessage:errorMessages) {
					String msg="Unable to load data for row # "+(i+1)+" , "+errorMessage+
					" . Setting empty values to the columns instead.";
					FileUtils.println(msg);
					throw new Exception(msg);
				}
			}
		}
		return toReturn;
	}

	private Object convertDataRowToDataGridRowObject(String[] dataRow,List<String> errorMessages) 
	throws Exception {
		Object dataGridRow=getGeneratedClass().newInstance();
		
		int columnIndex=0;
		for (DataGridColumn dataGridColumn:getDataGridUserDefinedColumns()) {
			try{
				String val=dataRow[columnIndex++];
				DataGridRowObjectUtils.invokeDataGridSetter(
						getGeneratedClass(),
						dataGridRow,
						dataGridColumn.getAttributeClass(),
						val,
						dataGridColumn.getAttributeName());
			}
			catch(Exception e) {
				FileUtils.printStackTrace(e);
				errorMessages.add("Column '"+dataGridColumn.getColumnTitle()+"', "+e.getMessage());
			}
		}
		return dataGridRow;
	}
	
	private String[] convertDataGridRowObjectToDataRow(int startIndex,int endIndex,Object rowObject) 
	throws Exception {	
		int totalColumnsCount=endIndex-startIndex;
		String[] res=new String[totalColumnsCount];
		int columnIndex=0;
		for (int i=startIndex;i<endIndex;i++) {
			DataGridColumn dataGridColumn=getDataGridUserDefinedColumns().get(i);
			Object sourceValue=DataGridRowObjectUtils.invokeGetter(
					getGeneratedClass(),
					rowObject,
					null,
					null,
					dataGridColumn.getAttributeName());
			String value=null;
			if (sourceValue!=null) {
				if (sourceValue.getClass() == java.util.Date.class) {
					java.util.Date sourceValueSQLDate=(java.util.Date)sourceValue;
					DateFormat df = null;
					if (dataGridColumn.getColumn() instanceof DateTimeColumn){
						df=new SimpleDateFormat(DataGridConstants.ORACLE_DATE_FORMAT+" "+DataGridConstants.ORACLE_TIME_FORMAT);
					}
					else {
						df=new SimpleDateFormat(DataGridConstants.ORACLE_DATE_FORMAT);
					}
					value= df.format(sourceValueSQLDate);
				}
				else {
					value=sourceValue.toString();
				}
			}
			res[columnIndex++]=value;
		}
		return res;
	}
		
	public List<String[]> filterDataGridRows(List<String[]> dataGridRowsInStringFormat,Map<String,FilterWhereClause[]> filteredColumns) 
	throws Exception {
		for (int i=dataGridRowsInStringFormat.size()-1;i>=0;i--) {
			String[] dataGridRowInStringFormat=dataGridRowsInStringFormat.get(i);
			boolean isMatchingRow=include(filteredColumns,dataGridRowInStringFormat); 
			if (!isMatchingRow) {
				dataGridRowsInStringFormat.remove(i);
			}
		}
		return dataGridRowsInStringFormat;
	}
	
	private boolean include(Map<String,FilterWhereClause[]> filteredColumns,String[] dataGridRowInStringFormat) 
	throws Exception {
		Iterator<String> iterator=filteredColumns.keySet().iterator();
		boolean isToInclude=true;
		while (iterator.hasNext()) {
			String key=iterator.next();
			FilterWhereClause[] filterWhereClauses=filteredColumns.get(key);
			for (FilterWhereClause filterWhereClause:filterWhereClauses) {
				if (filterWhereClause!=null) {
					String sourceValue=getDataRowValue(dataGridColumnAttributeNameToDataGridColumnMap.get(filterWhereClause.getAttributeName()).getAttributeDescription(),dataGridRowInStringFormat); 
					
					boolean isBooleanColumn=filterWhereClause.getAttributeName().equalsIgnoreCase(InventoryDataGridConstants.EXTRA_COLUMN_ENABLED_ATTRIBUTE_NAME);					
					isToInclude=DataGridUtils.include(filterWhereClause.getOperator(),filterWhereClause.getValue(),sourceValue,isBooleanColumn); 
					if (!isToInclude) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean isFilteringIdenticalToPrevious(Map<String,FilterWhereClause[]> newFilteredColumns) 
	throws Exception {
		if (filteredColumnsMap==null && newFilteredColumns!=null ) {
			return false;
		}
		Iterator<String> iterator=newFilteredColumns.keySet().iterator();
		if (newFilteredColumns.size()!=filteredColumnsMap.size()) {
			return false;
		}
		while (iterator.hasNext()) {
			String key=iterator.next();
			FilterWhereClause[] filterWhereClauses=newFilteredColumns.get(key);
			FilterWhereClause[] previousFilterWhereClauses=filteredColumnsMap.get(key);
			if (previousFilterWhereClauses==null || previousFilterWhereClauses.length!=
				filterWhereClauses.length) {
				return false;
			}
			
			for (int i=0;i<filterWhereClauses.length;i++) {
				FilterWhereClause filterWhereClause=filterWhereClauses[i];
				if (filterWhereClause!=null) {
					String filterWhereClauseAttributeName=filterWhereClause.getAttributeName();					
					String filterWhereClauseOperator=filterWhereClause.getOperator();
					Object filterWhereClauseValue=filterWhereClause.getValue();
					
					FilterWhereClause previousFilterWhereClause=previousFilterWhereClauses[i];
				
					if (previousFilterWhereClause==null) {
						return false;
					}
					String previousFilterWhereClauseAttributeName=filterWhereClause.getAttributeName();	
					if (!previousFilterWhereClauseAttributeName.equalsIgnoreCase(filterWhereClauseAttributeName)) {
						return false;
					}
					String previousFilterWhereOperator=previousFilterWhereClause.getOperator();
					if (!previousFilterWhereOperator.equalsIgnoreCase(filterWhereClauseOperator)) {
						return false;
					}
					Object previousFilterWhereClauseValue=previousFilterWhereClause.getValue();
					if ( (filterWhereClauseValue==null && previousFilterWhereClauseValue!=null) ||
						 (filterWhereClauseValue!=null && previousFilterWhereClauseValue==null)
					) {
						return false;
					}
					if (   filterWhereClauseValue!=null && previousFilterWhereClauseValue!=null && filterWhereClauseValue.hashCode()!=previousFilterWhereClauseValue.hashCode()) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private Map<String,FilterWhereClause[]> cloneFilteringMap(Map<String,FilterWhereClause[]> filteredColumns) 
	throws Exception {
		Map<String,FilterWhereClause[]> res=new HashMap<String,FilterWhereClause[]>();
		Iterator<String> iterator=filteredColumns.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			FilterWhereClause[] filterWhereClauses=filteredColumns.get(key);
			if (filterWhereClauses==null) {
				continue;
			}
			
			FilterWhereClause[] newFilterWhereClauses=new FilterWhereClause[filterWhereClauses.length];
			for (int i=0;i<filterWhereClauses.length;i++) {
				FilterWhereClause filterWhereClause=filterWhereClauses[i];
				FilterWhereClause newFilterWhereClause=null;
				if (filterWhereClause!=null) {
					String filterWhereClauseAttributeName=filterWhereClause.getAttributeName();					
					String filterWhereClauseOperator=filterWhereClause.getOperator();
					Object filterWhereClauseValue=filterWhereClause.getValue();
					
					newFilterWhereClause=new FilterWhereClause(filterWhereClauseAttributeName,
							filterWhereClauseOperator,filterWhereClauseValue);
				}
				newFilterWhereClauses[i]=newFilterWhereClause;
			}
			res.put(key,newFilterWhereClauses);
		}
		return res;
	}
		
	private int getStartIndex(int sourceIndex,boolean isForward,boolean isLastBlock,int resultSetSize) {
		int startIndex=-1;
		if (isForward) {
			if (isLastBlock) {
				int pageCount=resultSetSize/DataGridConstants.GRID_PAGINATION_BATCH_SIZE;
				if (pageCount*DataGridConstants.GRID_PAGINATION_BATCH_SIZE==resultSetSize){
					startIndex=(pageCount-1)*DataGridConstants.GRID_PAGINATION_BATCH_SIZE;
				}
				else {
					startIndex=(pageCount)*DataGridConstants.GRID_PAGINATION_BATCH_SIZE;
				}
			}
			else {
			   	startIndex=sourceIndex;
			}
		}
		else {
			startIndex=sourceIndex-DataGridConstants.GRID_PAGINATION_BATCH_SIZE;
			if (startIndex<0) {
				startIndex=0;
			}
		}
		return startIndex;
	}
	
	private int getEndIndex(int sourceIndex,int startIndex,boolean isForward,boolean isLastBlock,int resultSetSize) {
		int endIndex=-1;
		if (isForward) {
			if (isLastBlock) {
				endIndex=resultSetSize;
			}
			else {
				endIndex=startIndex+DataGridConstants.GRID_PAGINATION_BATCH_SIZE;
				if (endIndex>resultSetSize) {
					endIndex=resultSetSize;
				}
			}
		}
		else {
			endIndex=sourceIndex;
		}
		return endIndex;
	}
			
	public boolean hasFilteringOn() {
		return filteredColumnsMap!=null;
	}
	
	public Response getDataGridResponse(
			Map<String,FilterWhereClause[]> newFilteredColumns,
			int sourceIndexParameter,
			boolean isForward,
			boolean isLastBlock) 
	throws Exception {
		List<Object> res=new ArrayList<Object>();
		List<String[]> dataGridRowsStringFormat=getDataGridRows();
		int totalBeforeFiltering=dataGridRowsStringFormat.size();
		if (dataGridRowsStringFormat.isEmpty()) {
			getDataGridPanel().displayRelativeRecordCount(0,0,!newFilteredColumns.isEmpty()); 
			return new VOListResponse(res,false,0); 
		}
		
		int endIndex=-1;
		int totalRecordsCount=-1;
		int sourceIndex=-1;		
		if (!newFilteredColumns.isEmpty()) {
			boolean isFilteringIdenticalToPrevious=isFilteringIdenticalToPrevious(newFilteredColumns);
			if (!isFilteringIdenticalToPrevious) {
				filteredColumnsMap=cloneFilteringMap(newFilteredColumns);
				setReloadFromStart(true);
				return new VOListResponse(res,false,0); 
			}
			dataGridRowsStringFormat=filterDataGridRows(dataGridRowsStringFormat,newFilteredColumns);
			if (!isFilteringIdenticalToPrevious) {
				sourceIndex=0;
			}
			else {
				sourceIndex=sourceIndexParameter;
			}
		}
		else {
			if (filteredColumnsMap!=null) {
				filteredColumnsMap=null;
				setReloadFromStart(true);
				return new VOListResponse(res,false,0); 
			}
			sourceIndex=sourceIndexParameter;
		}
		totalRecordsCount=dataGridRowsStringFormat.size();
		startIndex=getStartIndex(sourceIndex,isForward,isLastBlock,totalRecordsCount);
		endIndex=getEndIndex(sourceIndex,startIndex,isForward,isLastBlock,totalRecordsCount);
		dataGridRowsStringFormat=dataGridRowsStringFormat.subList(startIndex, endIndex);	
		
		int rowSequence=startIndex+1;
		for (String[] row:dataGridRowsStringFormat) {
			row[InventoryDataGridConstants.ROW_SEQUENCE_COLUMN_GRID_INDEX]=String.valueOf(rowSequence++);
		}
		
		res=convertDataRowsToDataGridRowObjects(dataGridRowsStringFormat);
			
		boolean hasMoreBlocksToLoad=true;
		if (endIndex==totalRecordsCount) {
			hasMoreBlocksToLoad=false;
		}
		getDataGridPanel().displayRelativeRecordCount(totalRecordsCount, totalBeforeFiltering,!newFilteredColumns.isEmpty()); 
		
		return new VOListResponse(res,hasMoreBlocksToLoad,totalRecordsCount); 
	}
		
	public Object getDataGridValue(String attributeName,Object sourceRow) 
	throws Exception {
		return DataGridRowObjectUtils.invokeGetter(
				getGeneratedClass(),
				sourceRow,
				null,
				null,
				attributeName);
	}
	
	
	public boolean containsColumnName(String columnName)
	{
		return attributeDescriptionToPositionMap.containsKey(columnName);
	}
	public void setDataRowValue(String attributeDescription,String value,String[] sourceRow)
	throws Exception 
	{
		Integer position = getColumnIndex(attributeDescription);
		sourceRow[position] = value;
	}
	
	public String getDataRowValue(String attributeDescription,String[] sourceRow) 
	throws Exception {
		Integer position = getColumnIndex(attributeDescription);
		return sourceRow[position];
	}

	private Integer getColumnIndex(String attributeDescription) throws Exception
	{
		Integer position=attributeDescriptionToPositionMap.get(attributeDescription);
		if (position==null) {
			throw new Exception("cannot find the position of the column name: '"+attributeDescription+"'");
		}
		return position;
	}
		
	public void resetFilteredColumnsMap(){
		filteredColumnsMap=null;
	}	
	
	/**
	 * Method invoked when the user has clicked on save button and the grid is in INSERT mode.
	 * @param rowNumbers row indexes related to the new rows to save
	 * @param newValueObjects list of new value objects to save
	 * @return an ErrorResponse value object in case of errors, VOListResponse if the operation is successfully completed
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response insertRecords(int[] rowNumbers, ArrayList newValueObjects){
		try{
			List<String[]> dataGridRowsInStringFormat=convertDataGridRowObjectsToDataGridRowsInStringFormat(newValueObjects);
			for (String[] dataGridRowInStringFormat:dataGridRowsInStringFormat) {
				dataGridRowInStringFormat[InventoryDataGridConstants.ENABLED_COLUMN_GRID_INDEX]="true";
				dataGridRowInStringFormat[InventoryDataGridConstants.STATUS_COLUMN_GRID_INDEX]=InventoryDataGridConstants.STATUS_COLUMN_PENDING_VALUE;
			}
			List<String[]> allDataGridRowsStringFormat=getDataGridRows();
			dataGridRowsInStringFormat.addAll(allDataGridRowsStringFormat);
			serializeDataGridRows(dataGridRowsInStringFormat);
			
			return new VOListResponse(newValueObjects,false,newValueObjects.size());
		}
		catch (Exception ex) {
			FileUtils.printStackTrace(ex);
			return new ErrorResponse(ex.getMessage());
		}
	}
	
	/**
	 * Method invoked when the user has clicked on save button and the grid is in EDIT mode.
	 * @param rowNumbers row indexes related to the changed rows
	 * @param oldPersistentObjects old value objects, previous the changes
	 * @param persistentObjects value objects related to the changed rows
	 * @return an ErrorResponse value object in case of errors, VOListResponse if the operation is successfully completed
	 */
	@SuppressWarnings({ "rawtypes" })
	public Response updateRecords(int[] rowNumbers,ArrayList oldPersistentObjects,ArrayList persistentObjects)
	throws Exception {
		try{
			List<String[]> dataGridRowsStringFormat=getDataGridRows();
			List<Integer> rowIndexesListAfterFiltering=null;
			if (filteredColumnsMap!=null) {
				rowIndexesListAfterFiltering=getRowIndexesListAfterFiltering(dataGridRowsStringFormat);
			}
			for (int i=0;i<persistentObjects.size();i++) {
				Object persistentObject=persistentObjects.get(i);
				String[] dataGridRowStringFormat=convertDataGridRowObjectToDataGridRowInStringFormat(persistentObject);
				int viewIndex=startIndex+rowNumbers[i];
				int index=-1;
				if (rowIndexesListAfterFiltering!=null && !rowIndexesListAfterFiltering.isEmpty()) {
					index=rowIndexesListAfterFiltering.get(startIndex+rowNumbers[i]);
				}
				else {
					index=viewIndex;
				}
				//System.out.println("Updating row:"+index +" viewIndex:"+viewIndex+" startIndex:"+startIndex+" rowNumbers[i]:"+rowNumbers[i]);
				dataGridRowsStringFormat.set(index,dataGridRowStringFormat);
			}
			this.serializeDataGridRows(dataGridRowsStringFormat);
			return new VOListResponse(persistentObjects,false,persistentObjects.size());
		}
		catch (Exception ex) {
			FileUtils.printStackTrace(ex);
			return new ErrorResponse(ex.getMessage());
		}
	}

	/**
	 * Method invoked when the user has clicked on delete button and the grid is in READONLY mode.
	 * @param persistentObjects value objects to delete (related to the currently selected rows)
	 * @return an ErrorResponse value object in case of errors, VOResponse if the operation is successfully completed
	 */
	@SuppressWarnings({ "rawtypes" })
	public Response deleteRecords(ArrayList persistentObjects) throws Exception {
		try{
			List<String[]> dataGridRowsStringFormat=getDataGridRows();
			int selectRowIndex=getDataGridPanel().getGridControl().getSelectedRow();
			int index=startIndex+selectRowIndex;
			List<Integer> rowIndexesListAfterFiltering=null;
			if (filteredColumnsMap!=null) {
				rowIndexesListAfterFiltering=getRowIndexesListAfterFiltering(dataGridRowsStringFormat);
			}
			if (rowIndexesListAfterFiltering!=null && !rowIndexesListAfterFiltering.isEmpty()) {
				index=rowIndexesListAfterFiltering.get(index);
			}
			dataGridRowsStringFormat.remove(index);
			this.serializeDataGridRows(dataGridRowsStringFormat);
			
			return new VOResponse(new Boolean(true));
		}
		catch (Exception ex) {
			FileUtils.printStackTrace(ex);
			return new ErrorResponse(ex.getMessage());
		}
	}
			
	public void massUpdateDataGridRows(Map<DataGridColumn,String> dataGridColumnsToValueMap) {
		try{
			List<String[]> dataGridRowsStringFormat=getDataGridRows();
			boolean hasAtLeastOneUpdate=false;
			for (String[] dataGridRowStringFormat:dataGridRowsStringFormat) {
				boolean isMatchingRow=true;
				if (filteredColumnsMap!=null) {
					isMatchingRow=include(filteredColumnsMap,dataGridRowStringFormat); 
				}
				if (isMatchingRow) {
					hasAtLeastOneUpdate=true;
					applyUpdate(dataGridColumnsToValueMap,dataGridRowStringFormat); 
				}
			}
			if (hasAtLeastOneUpdate) {
				serializeDataGridRows(dataGridRowsStringFormat);
			}
			afterMassUpdate();
		}
		catch(Exception ex)
		{
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}
	
	private void applyUpdate(Map<DataGridColumn,String> dataGridColumnsToValueMap,String[] dataGridRowStringFormat) throws Exception {	
		Set<DataGridColumn> set=dataGridColumnsToValueMap.keySet();
		Iterator<DataGridColumn> iterator=set.iterator();
		while (iterator.hasNext()) {
			DataGridColumn dataGridColumn=iterator.next();
			String value=dataGridColumnsToValueMap.get(dataGridColumn);

			Integer position=attributeDescriptionToPositionMap.get(dataGridColumn.getAttributeDescription());
			if (position==null) {
				throw new Exception("cannot find the position of the column name: '"+dataGridColumn.getColumnTitle()+"'");
			}
			dataGridRowStringFormat[position]=value;
		}
	}
	
	public void massDelete() {
		try{
			if (JOptionPane.showConfirmDialog(null,"Delete all Filtered Rows?","Delete Confirmation",JOptionPane.YES_NO_OPTION)==  JOptionPane.YES_OPTION ) {
				List<String[]> dataGridRowsStringFormat=getDataGridRows();
				if (filteredColumnsMap!=null) {
					for (int i=dataGridRowsStringFormat.size()-1;i>=0;i--) {
						String[] dataGridRowInStringFormat=dataGridRowsStringFormat.get(i);
						boolean isMatchingRow=include(filteredColumnsMap,dataGridRowInStringFormat); 
						if (isMatchingRow) {
							dataGridRowsStringFormat.remove(i);
						}
					}
				}
				serializeDataGridRows(dataGridRowsStringFormat);
				afterMassDelete();
			}
		}
		catch(Exception ex)
		{
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}
	
	public abstract void refreshGrid(boolean isResetFiltering) throws Exception;
	
	public List<Integer> getRowIndexesListAfterFiltering(List<String[]> dataGridRowsStringFormat) throws Exception {
		List<Integer> toReturn=new ArrayList<Integer>();
		if (filteredColumnsMap!=null) {
			for (int i=0;i<dataGridRowsStringFormat.size();i++) {
				String[] dataGridRowInStringFormat=dataGridRowsStringFormat.get(i);
				boolean isMatchingRow=include(filteredColumnsMap,dataGridRowInStringFormat); 
				if (isMatchingRow) {
					toReturn.add(i);
				}
			}
		}
		return toReturn;
	}

	public boolean isFirstLoadCompletedEventReceived() {
		return isFirstLoadCompletedEventReceived;
	}
	
	
	public void massUpdateGrids(Map<DataGridColumn,String> dataGridColumnsToValueMap) throws Exception {
		
	}
}
