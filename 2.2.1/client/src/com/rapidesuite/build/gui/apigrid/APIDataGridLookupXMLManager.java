package com.rapidesuite.build.gui.apigrid;

import java.io.File;

import com.erapidsuite.build.datagrid0000.DataGrid;
import com.erapidsuite.build.datagrid0000.DataGridColumn;
import com.erapidsuite.build.datagrid0000.DataGridLookupColumnsDocument;
import com.rapidesuite.core.utility.CoreUtil;

public class APIDataGridLookupXMLManager
{

	private DataGridLookupColumnsDocument dmd;

	public APIDataGridLookupXMLManager(File lookupXMLFile) throws Exception
	{
		dmd = CoreUtil.parseDataGridLookupColumnsDocument(lookupXMLFile);
	}

	public DataGridColumn getDataGridColumn(String dataGridName, String dataGridColumnName)
	{
		DataGrid[] dataGrids = dmd.getDataGridLookupColumns().getDataGridArray();
		for ( DataGrid dataGrid : dataGrids )
		{
			if ( dataGrid.getName().equalsIgnoreCase(dataGridName) )
			{
				com.erapidsuite.build.datagrid0000.DataGridColumn[] dataGridColumns = dataGrid.getDataGridColumnArray();
				for ( com.erapidsuite.build.datagrid0000.DataGridColumn dataGridColumn : dataGridColumns )
				{
					if ( dataGridColumn.getName().equalsIgnoreCase(dataGridColumnName) )
					{
						return dataGridColumn;
					}
				}
			}
		}
		return null;
	}

	public boolean isPopupOperatingUnitWindow()
	{
		DataGrid[] dataGrids = dmd.getDataGridLookupColumns().getDataGridArray();
		return dataGrids[0].getIsPopupOperatingUnitWindow();
	}

}
