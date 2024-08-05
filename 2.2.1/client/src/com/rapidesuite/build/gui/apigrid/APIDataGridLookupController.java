package com.rapidesuite.build.gui.apigrid;

import java.awt.Dimension;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.openswing.swing.message.receive.java.Response;
import org.openswing.swing.message.receive.java.VOListResponse;
import org.openswing.swing.message.send.java.FilterWhereClause;
import org.openswing.swing.util.java.Consts;
import org.springframework.util.Assert;

import com.erapidsuite.build.datagrid0000.DataGridColumn;
import com.erapidsuite.build.datagrid0000.DependentColumn;
import com.erapidsuite.build.datagrid0000.LookupColumn;
import com.rapidesuite.client.common.gui.datagrid.DataGridConstants;
import com.rapidesuite.client.common.gui.datagrid.DataGridLookupController;
import com.rapidesuite.client.common.gui.datagrid.DataGridLookupVO;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class APIDataGridLookupController extends DataGridLookupController
{

	private APIDataGridController apiDataGridController;
	private DependentColumn[] dependentColumnNamesArray;
	private List<String> columnDescriptions;
	private final DataGridColumn dataGridColumn;
	private final String attributeName;

	public APIDataGridLookupController(APIDataGridController apiDataGridController, final String attributeName, final DataGridColumn dataGridColumn) throws Exception
	{
		super();
		this.attributeName = attributeName;
		this.dataGridColumn = dataGridColumn;
		this.apiDataGridController = apiDataGridController;
		dependentColumnNamesArray = dataGridColumn.getLookupGrid().getDependentColumnArray();
		initGrid();
	}

	public void initGrid() throws Exception
	{
		// TODO: replace by a dynamic class to be generated from the LOOKUPGRID,
		// so that we
		// can define more than a fixed number of columns (currently 8)
		this.setLookupValueObjectClassName(com.rapidesuite.client.common.gui.datagrid.DataGridLookupVO.class.getName());
		this.setGridFilterButton(true);
		// this.setOnInvalidCode(ON_INVALID_CODE_RESTORE_LAST_VALID_CODE);
		this.setVisibleStatusPanel(true);
		this.setAutoFitColumns(false);
		this.setFrameTitle(DataGridConstants.LOOKUP_FRAME_TITLE);

		LookupColumn[] lookupColumns = dataGridColumn.getLookupGrid().getLookupColumnArray();
		if ( lookupColumns == null || lookupColumns.length == 0 )
		{
			throw new Exception("Error, lookup column xml incorrectly defined. DataGridColumn tag name: '" + dataGridColumn.getName() + "'");
		}
		int visibleColumnsCounter = 0;
		int visibleColumnWidth = 150;
		boolean isAtLeastOneDisplayedValue = false;
		// System.out.println("col: '"+attributeDescription+"' , lookupColumns:"+lookupColumns.length);
		columnDescriptions = new ArrayList<String>();
		for ( int i = 0; i < lookupColumns.length; i++ )
		{
			LookupColumn lookupColumn = lookupColumns[i];
			String name = lookupColumn.getName();
			boolean isVisible = lookupColumn.getIsVisible();
			boolean isDisplayedValue = lookupColumn.getIsDisplayedValue();
			String columnName = "column" + (i + 1);
			String columnDescription = name;
			if ( isDisplayedValue )
			{
				columnDescription += " (*)";
			}
			columnDescription += "                   " + "          ";
			columnDescriptions.add(columnDescription);

			this.setHeaderColumnName(columnName, columnDescription);
			this.setVisibleColumn(columnName, isVisible);
			this.setFilterableColumn(columnName, isVisible);
			this.setSortableColumn(columnName, true);

			if ( isDisplayedValue )
			{
				this.addLookup2ParentLink(columnName, attributeName);
				isAtLeastOneDisplayedValue = true;
			}
			if ( isVisible )
			{
				visibleColumnsCounter++;
				this.setPreferredWidthColumn(columnName, visibleColumnWidth);
			}
		}
		if ( !isAtLeastOneDisplayedValue )
		{
			throw new Exception("Invalid Lookup XML file, missing attribute 'isDisplayedValue' for the DataGridColumn " + "tag name: '" + dataGridColumn.getName() + "'");
		}
		// Dimension(int width, int height)
		this.setFramePreferedSize(new Dimension(visibleColumnWidth * visibleColumnsCounter + 40, 450));
	}

	public List<String> getColumnDescriptions()
	{
		return columnDescriptions;
	}

	public Response getDataGridRows(Map<String, FilterWhereClause[]> filteredColumns, final List<String> currentSortedColumns, final List<String> currentSortedVersusColumns) throws Exception
	{
		try ( Connection connection = DatabaseUtils.getDatabaseConnection(apiDataGridController.getAPIDataAction().getEnvironmentProperties()) )
		{

			String query = dataGridColumn.getLookupGrid().getQuery();
			if ( dependentColumnNamesArray != null && dependentColumnNamesArray.length != 0 )
			{
				lookupRows = null;
				String[] dataGridRow = apiDataGridController.getSelectedDataGridRow();
				Map<String, com.rapidesuite.client.common.gui.datagrid.DataGridColumn> descMap = APIDataGridUtils.convertListToDescriptionMap(apiDataGridController.getDataGridInventoryColumns());

				for ( DependentColumn dependentColumn : dependentColumnNamesArray )
				{
					com.rapidesuite.client.common.gui.datagrid.DataGridColumn dgc = descMap.get(dependentColumn.getName());
					String dependentValue = apiDataGridController.getDataRowValue(dgc.getAttributeDescription(), dataGridRow);
					if ( dependentValue == null )
					{
						dependentValue = "";
					}
					if ( dependentColumn.getReplacementKeyword() == null )
					{
						throw new Exception("Incorrect lookup mapping file, the replacement keyword is missing for the dependent" + " column: '" + dependentColumn.getName() + "'");
					}
					// System.out.println("dependentColumn.getReplacementKeyword():"+dependentColumn.getReplacementKeyword());
					// System.out.println("dependentValue:"+dependentValue);
					query = query.replaceAll(dependentColumn.getReplacementKeyword(), dependentValue);
				}

				query = query.replaceAll(APIDataGridConstants.ORG_NAME_LOOKUP_VALUE_KEYWORD, apiDataGridController.getSelectedOperatingUnitName());
				// System.out.println("query:"+query);
			}
			if ( lookupRows == null )
			{
				lookupRows = APIDataGridUtils.getData(connection,
						apiDataGridController.getSelectedOperatingUnitId(),
						query,
						apiDataGridController.getInventoryDataGridFrame().getStatusLabel(),
						"Loading data...");
				GUIUtils.resetLabel(apiDataGridController.getInventoryDataGridFrame().getStatusLabel());
			}
			ArrayList<DataGridLookupVO> list = new ArrayList<DataGridLookupVO>();
			for ( String[] lookupRow : lookupRows )
			{
				DataGridLookupVO vo = new DataGridLookupVO();
				for ( int i = 0; i < lookupRow.length; i++ )
				{
					String value = lookupRow[i];
					switch (i) {
					case 0:
						vo.setColumn1(value);
						break;
					case 1:
						vo.setColumn2(value);
						break;
					case 2:
						vo.setColumn3(value);
						break;
					case 3:
						vo.setColumn4(value);
						break;
					case 4:
						vo.setColumn5(value);
						break;
					case 5:
						vo.setColumn6(value);
						break;
					case 6:
						vo.setColumn7(value);
						break;
					case 7:
						vo.setColumn8(value);
						break;
					}
				}
				boolean isToInclude = include(filteredColumns, vo);
				if ( isToInclude )
				{
					list.add(vo);
				}
			}
			Collections.sort(list, new Comparator<DataGridLookupVO>() {

				@Override
				public int compare(DataGridLookupVO o1, DataGridLookupVO o2) {
					Assert.isTrue(currentSortedColumns.size() == currentSortedVersusColumns.size());
					for (int i = 0 ; i < currentSortedColumns.size() ; i++) {
						Assert.isTrue(currentSortedColumns.get(i) instanceof String);
						final String column = currentSortedColumns.get(i);
						int comparison = 0;
						if ("column1".equals(column)) {
							comparison = o1.getColumn1().compareTo(o2.getColumn1());
						} else if ("column2".equals(column)) {
							comparison = o1.getColumn2().compareTo(o2.getColumn2());
						} else if ("column3".equals(column)) {
							comparison = o1.getColumn3().compareTo(o2.getColumn3());
						} else if ("column4".equals(column)) {
							comparison = o1.getColumn4().compareTo(o2.getColumn4());
						} else if ("column5".equals(column)) {
							comparison = o1.getColumn5().compareTo(o2.getColumn5());
						} else if ("column6".equals(column)) {
							comparison = o1.getColumn6().compareTo(o2.getColumn6());
						} else if ("column7".equals(column)) {
							comparison = o1.getColumn7().compareTo(o2.getColumn7());
						} else if ("column8".equals(column)) {
							comparison = o1.getColumn8().compareTo(o2.getColumn8());
						}

						if (comparison != 0) {
							if (Consts.DESC_SORTED.equals(currentSortedVersusColumns.get(i))) {
								return comparison * -1;
							} else {
								return comparison;
							}
						}
					}
					return 0;
				}

			});
			return new VOListResponse(list, false, list.size());
		}
	}

}
