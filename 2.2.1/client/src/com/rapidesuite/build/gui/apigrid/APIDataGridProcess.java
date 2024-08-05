/**************************************************************
 * $Revision: 31696 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:39:15 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/apigrid/APIDataGridProcess.java $:
 * $Id: APIDataGridProcess.java 31696 2013-03-04 06:39:15Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.gui.apigrid;

import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridProcess;

public class APIDataGridProcess implements InventoryDataGridProcess
{

	private APIDataGridController apiDataGridController;

	public APIDataGridProcess(APIDataGridController apiDataGridController)
	{
		this.apiDataGridController = apiDataGridController;
	}

	@Override
	public void execute()
	{
		apiDataGridController.getAPIDataAction().getApiAction().processDataViaOracleAPI(false);
	}

}