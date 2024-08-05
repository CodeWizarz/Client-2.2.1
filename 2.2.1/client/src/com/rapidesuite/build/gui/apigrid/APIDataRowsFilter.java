/**************************************************************
 * $Revision: 31696 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:39:15 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/apigrid/APIDataRowsFilter.java $:
 * $Id: APIDataRowsFilter.java 31696 2013-03-04 06:39:15Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.gui.apigrid;

import java.util.List;

import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataRowsFilter;

public class APIDataRowsFilter implements InventoryDataRowsFilter
{

	private APIDataGridController apiDataGridController;

	public APIDataRowsFilter(APIDataGridController apiDataGridController)
	{
		this.apiDataGridController = apiDataGridController;
	}

	public List<String[]> filter(List<String[]> dataRows)
	{
		int indexOfNavigationFilterColumn = (apiDataGridController.getDataGridInventoryColumns().size() + apiDataGridController.getDataGridControlColumns().size() + 2) - 1;
		String navigationNameFilter = apiDataGridController.getAPIDataAction().getNavigationName();

		for ( int i = dataRows.size() - 1; i >= 0; i-- )
		{
			String[] dataRow = dataRows.get(i);

			String navigationName = dataRow[indexOfNavigationFilterColumn];
			if ( !navigationNameFilter.equalsIgnoreCase(navigationName) )
			{
				dataRows.remove(i);
			}
		}
		return dataRows;
	}

}