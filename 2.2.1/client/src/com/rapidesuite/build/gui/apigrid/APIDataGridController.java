package com.rapidesuite.build.gui.apigrid;

import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openswing.swing.client.GenericButton;
import org.openswing.swing.client.GridControl;
import org.openswing.swing.util.java.Consts;

import com.erapidsuite.configurator.apiscript0000.ApiScript;
import com.erapidsuite.configurator.apiscript0000.ApiScriptDocument;
import com.erapidsuite.configurator.apiscript0000.ApiScriptLookup;
import com.erapidsuite.configurator.apiscript0000.ApiScriptPackage;
import com.rapidesuite.build.BuildMain;
import com.rapidesuite.client.common.gui.datagrid.DataGridColumn;
import com.rapidesuite.client.common.gui.datagrid.DataGridLookupController;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridConstants;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridController;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridProcessButton;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.inventory0007.Field;

@SuppressWarnings("serial")
public class APIDataGridController extends InventoryDataGridController
{

	private String domainIdPrefix;
	private Map<String, DataGridColumn> dataGridColumnsResetValuesOnMap;
	private APIOUDialog apiOUDialog;
	private APIDataGridLookupXMLManager apiDataGridLookupXMLManager;
	private APIDataGridOptionsFrame apiDataGridOptionsFrame;
	private APIDataGridResetValuesFrame apiDataGridResetValuesFrame;
	private APIDataGridProcess apiDataGridProcess;

	public APIDataGridController(APIDataAction apiDataAction) throws Exception
	{
		super(apiDataAction, apiDataAction.getDataGridTitle(), false, true);
		dataGridColumnsResetValuesOnMap = new HashMap<String, DataGridColumn>();
	}

	public Map<String, DataGridColumn> getDataGridColumnsResetValuesOnMap()
	{
		return dataGridColumnsResetValuesOnMap;
	}

	public DataGridColumn getModeColumn()
	{
		return getDataGridInventoryColumns().get(0);
	}

	public void initComponents(Set<String> validNavigationMappers, String dataGridTitle, Inventory inventory, int frameWidth, int frameHeight,
	        BuildMain buildMain) throws Exception
	{
		List<GenericButton> additionalButtons = new ArrayList<GenericButton>();

		APIDataGridResetValuesButton resetValuesButton = new APIDataGridResetValuesButton(this, GUIUtils.getImageIcon(this.getClass(), "/images/filterbutton.gif"));
		resetValuesButton.setToolTipText("Reset values (update mode only)");

		APIDataGridOptionsButton optionsButton = new APIDataGridOptionsButton(this, GUIUtils.getImageIcon(this.getClass(), "/images/options.gif"));
		optionsButton.setToolTipText("Show options panel");

		APIDataGridExportGridStatusButton exportGridStatusButton = new APIDataGridExportGridStatusButton(this, GUIUtils.getImageIcon(this.getClass(), "/images/table.gif"));
		exportGridStatusButton.setToolTipText("Export Status to Excel");

		additionalButtons.add(resetValuesButton);
		additionalButtons.add(optionsButton);
		additionalButtons.add(exportGridStatusButton);

		ApiScriptDocument apiScriptDocument = ((APIDataAction) super.getInventoryDataGridAction()).getApiAction().getApiScriptDocument();
		ApiScript apiScript = apiScriptDocument.getApiScript();
		ApiScriptPackage apiInjectorsPackage = apiScript.getApiScriptPackage();
		ApiScriptLookup apiScriptLookup = apiInjectorsPackage.getApiScriptLookup();
		
		if ( apiScriptLookup != null )
		{
			File lookupColumnsDefinitionXMLFile = new File(buildMain.getInjectorsPackageSelectionPanel().getUnpackedBwpFolder(), apiScriptLookup.getFileName());
			apiDataGridLookupXMLManager = new APIDataGridLookupXMLManager(lookupColumnsDefinitionXMLFile);
		}
		apiDataGridProcess = new APIDataGridProcess(this);
		super.initComponents(validNavigationMappers, additionalButtons, dataGridTitle, inventory, frameWidth, frameHeight, new APIDataRowsFilter(this), apiDataGridProcess, true);

		apiDataGridOptionsFrame = new APIDataGridOptionsFrame(this);
		apiDataGridResetValuesFrame = new APIDataGridResetValuesFrame(this);
	}

	/**
	 * Method used to define the background color for each cell of the grid.
	 * 
	 * @param rowNumber
	 *            selected row index
	 * @param attributeName
	 *            attribute name related to the column currently selected
	 * @param value
	 *            object contained in the selected cell
	 * @return background color of the selected cell
	 */
	public Color getBackgroundColor(int row, String attributeName, Object value)
	{
		try
		{
			// Note: row always starts at 0 to the number of rows displayed, it
			// does not account for the page number!!
			if ( attributeName.equalsIgnoreCase(InventoryDataGridConstants.EXTRA_COLUMN_STATUS_ATTRIBUTE_NAME) )
			{
				if ( value != null && value.toString().equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_ERROR_VALUE) )
				{
					return new Color(255, 45, 45);
				}
				else if ( value != null && value.toString().equalsIgnoreCase(InventoryDataGridConstants.STATUS_COLUMN_SUCCESS_VALUE) )
				{
					return new Color(102, 166, 83);
				}
			}
			Object object = getDataGriRowForUIRowIndex(row);
			if ( object != null )
			{
				Boolean isEnabled = (Boolean) getDataGridValue(InventoryDataGridConstants.EXTRA_COLUMN_ENABLED_ATTRIBUTE_NAME, object);
				if ( isEnabled != null && !isEnabled.booleanValue() )
				{
					return new Color(196, 186, 181);
				}
				DataGridColumn dgc = this.getDataGridColumnsResetValuesOnMap().get(attributeName);
				if ( dgc != null )
				{
					DataGridColumn modeColumn = this.getDataGridInventoryColumns().get(0);
					String mode = (String) getDataGridValue(modeColumn.getAttributeName(), object);
					if ( mode != null && mode.equalsIgnoreCase(APIDataGridConstants.MODE_UPDATE) )
					{
						return new Color(247, 243, 111);
					}
				}
			}
			return super.getBackgroundColor(row, attributeName, value);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return super.getBackgroundColor(row, attributeName, value);
		}
	}

	public String getCellTooltip(int row, String attributeName)
	{
		Object object = getDataGridValueForUIRowIndex(attributeName, row);
		if ( object == null )
		{
			return "";
		}
		return "<html>" + object.toString().replaceAll("\n", "<br/>") + "</html>";
	}

	public void rowChanged(int rowNumber)
	{
		// if (this.isUploadStarted()) {
		// lockGrid();
		// }

		String message = null;
		try
		{
			message = (String) getDataGridValueForUIRowIndex(InventoryDataGridConstants.EXTRA_COLUMN_MESSAGE_ATTRIBUTE_NAME, rowNumber);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
		// adding the column index in case the column name is referenced in the
		// error message:
		if ( message != null )
		{
			int startIndexOf = message.indexOf("##'");
			if ( startIndexOf != -1 )
			{
				String temp = message.substring(startIndexOf + 3);
				int endIndexOf = temp.indexOf("'##");
				if ( endIndexOf != -1 )
				{
					String columnName = temp.substring(0, endIndexOf);
					List<DataGridColumn> dataGridInventoryColumns = this.getDataGridInventoryColumns();
					for ( int i = 0; i < dataGridInventoryColumns.size(); i++ )
					{
						DataGridColumn dataGridColumn = dataGridInventoryColumns.get(i);
						if ( columnName.equalsIgnoreCase(dataGridColumn.getAttributeDescription()) )
						{
							message += " (See the Column # " + (i + 1) + ")";
							break;
						}
					}
				}
			}
		}
		if ( message != null )
		{
			message = message.replaceAll("\\\\n", "<br/>");
			message = message.replaceAll("##", "");
		}
		getInventoryDataGridFrame().setErrorMessage(message);
	}

	/**
	 * @param grid
	 *            grid
	 * @param row
	 *            selected row index
	 * @param attributeName
	 *            attribute name that identifies the selected grid column
	 * @return <code>true</code> if the selected cell is editable,
	 *         <code>false</code> otherwise
	 */
	public boolean isCellEditable(GridControl grid, int row, String attributeName)
	{
		if ( grid.getMode() != Consts.EDIT )
		{
			return grid.isFieldEditable(row, attributeName);
		}
		if ( attributeName == null )
		{
			return true;
		}
		String mode = (String) getDataGridValueForUIRowIndex("0", row);
		// System.out.println("isCellEditable attributeName:"+attributeName+" row:"+row+" mode:"+mode+
		// " dgcisupdateable:"+dgc.isUpdatable());
		if ( mode != null && mode.equalsIgnoreCase(APIDataGridConstants.MODE_UPDATE) )
		{
			DataGridColumn dgc = getDataGridColumnAttributeNameToDataGridColumnMap().get(attributeName);
			return dgc.isUpdatable();
		}
		return grid.isFieldEditable(row, attributeName);
	}

	public boolean beforeEditGrid(GridControl grid)
	{
		if ( this.isPopupOperatingUnitWindow() )
		{
			this.showAPIOUDialog();
			return this.isOUSelected();
		}
		return super.beforeEditGrid(grid);
	}

	public boolean beforeInsertGrid(GridControl grid)
	{
		if ( this.isPopupOperatingUnitWindow() )
		{
			this.showAPIOUDialog();
			return this.isOUSelected();
		}
		return true;
	}

	public APIDataAction getAPIDataAction()
	{
		return (APIDataAction) super.getInventoryDataGridAction();
	}

	public void showAPIOUDialog()
	{
		if ( isPopupOperatingUnitWindow() )
		{
			if ( apiOUDialog == null )
			{
				APIDataAction apiDataAction = (APIDataAction) super.getInventoryDataGridAction();
				apiOUDialog = new APIOUDialog(super.getInventoryDataGridFrame(), apiDataAction.getEnvironmentProperties());
			}
			else
			{
				if ( !apiOUDialog.isOUSelected() )
				{
					apiOUDialog.setVisible(true);
				}
			}
		}
	}

	public boolean isPopupOperatingUnitWindow()
	{
		return apiDataGridLookupXMLManager != null && apiDataGridLookupXMLManager.isPopupOperatingUnitWindow();
	}

	public boolean isOUSelected()
	{
		return apiOUDialog != null && apiOUDialog.isOUSelected();
	}

	@Override
	public void close()
	{
		// TODO Auto-generated method stub
	}

	public DataGridLookupController getDataGridLookupController(String attributeName, Field field) throws Exception
	{
		com.erapidsuite.build.datagrid0000.DataGridColumn lookupColumn = apiDataGridLookupXMLManager.getDataGridColumn(super.getInventoryDataGridAction().getInventory().getName(),
				field.getName());
		if ( lookupColumn != null )
		{
			return new APIDataGridLookupController(this, attributeName, lookupColumn);
		}
		return null;
	}

	public APIDataGridOptionsFrame getApiDataGridOptionsFrame()
	{
		return apiDataGridOptionsFrame;
	}

	public void showAllColumns() throws Exception
	{
		for ( DataGridColumn dataGridColumn : super.getDataGridInventoryColumns() )
		{
			setVisibleColumn(dataGridColumn.getAttributeName(), true);
		}
	}

	public void showRequiredColumnsOnly() throws Exception
	{
		for ( DataGridColumn dataGridColumn : super.getDataGridInventoryColumns() )
		{
			Field field = getAPIDataAction().getInventory().getField(dataGridColumn.getAttributeDescription());
			setVisibleColumn(dataGridColumn.getAttributeName(), field.getMandatory());
		}
	}

	public void hideEmptyColumns() throws Exception
	{
		List<String[]> dataGridRowsInStringFormat = getDataGridRows();
		List<Object> dataGridRows = convertDataRowsToDataGridRowObjects(dataGridRowsInStringFormat);
		for ( DataGridColumn dataGridColumn : super.getDataGridInventoryColumns() )
		{
			boolean isEmpty = true;
			for ( Object dataGridRow : dataGridRows )
			{
				Object value = getDataGridValue(dataGridColumn.getAttributeName(), dataGridRow);

				if ( value != null && !value.toString().isEmpty() && !value.toString().equalsIgnoreCase(APIDataGridConstants.ORACLE_DEFAULT_VALUE_KEYWORD) )
				{
					isEmpty = false;
					break;
				}
			}
			if ( isEmpty )
			{
				setVisibleColumn(dataGridColumn.getAttributeName(), false);
			}
		}
	}

	public void showResetValuesFrame()
	{
		apiDataGridResetValuesFrame.setVisible(true);
	}

	public int getSelectedOperatingUnitId()
	{
		if ( apiOUDialog == null )
		{
			return -1;
		}
		Long temp= apiOUDialog.getSelectedOperatingUnitId();
		return temp.intValue();
	}

	public String getSelectedOperatingUnitName()
	{
		if ( apiOUDialog == null )
		{
			return "NO OU SET";
		}
		return apiOUDialog.getSelectedOperatingUnitName();
	}

	public String getDomainIdPrefix()
	{
		return domainIdPrefix;
	}

	public void showOptionsPanel()
	{
		try
		{
			apiDataGridOptionsFrame.showFrame();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to show the Options panel: " + e.getMessage());
		}
	}

	@Override
	public void serializeDataGridRows(List<String[]> dataGridRowsForThatTableAndNavigation) throws Exception
	{
		super.serializeDataGridRows(dataGridRowsForThatTableAndNavigation, null, false, true);
	}

	public void setProcessButtontoStartedAndLockGrid()
	{
		super.setProcessButtontoStartedAndLockGrid();
		for ( GenericButton button : super.getDataGridPanel().getButtons() )
		{
			if ( button instanceof InventoryDataGridProcessButton )
			{
				button.setEnabled(true);
			}
		}
	}
	
	public void massUpdateGrids(Map<DataGridColumn,String> dataGridColumnsToValueMap) throws Exception {
		APIDataGridOptionsFrame.massUpdateGrids(this,getAPIDataAction().getApiAction().getInjectorsPackageFile(),getAPIDataAction().getApiAction().getBuildMain(),
				dataGridColumnsToValueMap);
	}
}
