/**************************************************************
 * $Revision: 31696 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:39:15 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/apigrid/APIDataAction.java $:
 * $Id: APIDataAction.java 31696 2013-03-04 06:39:15Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.gui.apigrid;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.core.action.APIAction;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridAction;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridConstants;
import com.rapidesuite.configurator.domain.Inventory;

public class APIDataAction extends InventoryDataGridAction
{

	private BuildMain BuildMain;
	private APIDataGridController apiDataGridController;
	private File dataArchiveFile;
	private String dataFileEntryName;
	private Map<String, String> environmentProperties;
	private APIAction apiAction;
	private APIDataGridMigrateThread apiDataGridMigrateThread;

	public APIDataAction(APIAction apiAction, BuildMain BuildMain, Inventory inventory, String dataGridTitle, File dataArchiveFile, Map<String, String> environmentProperties)
			throws Exception
	{
		super(inventory, dataGridTitle, InventoryDataGridConstants.NAVIGATION_NA_VALUE);
		this.apiAction = apiAction;
		this.dataArchiveFile = dataArchiveFile;
		this.BuildMain = BuildMain;
		this.environmentProperties = environmentProperties;
	}

	public void initComponents() throws Exception
	{
		apiDataGridController = new APIDataGridController(this);
		Set<String> validNavigationMappers = new HashSet<String>();
		validNavigationMappers.add(InventoryDataGridConstants.NAVIGATION_NA_VALUE);
		apiDataGridController.initComponents(validNavigationMappers, super.getDataGridTitle(), getInventory(), 
		        APIDataGridConstants.FRAME_WIDTH, APIDataGridConstants.FRAME_HEIGHT, this.BuildMain);
	}

	public APIDataGridController getAPIDataGridController()
	{
		return apiDataGridController;
	}

	public File getDataArchiveFile()
	{
		return dataArchiveFile;
	}

	public String getDataFileEntryName()
	{
		return dataFileEntryName;
	}

	public Map<String, String> getEnvironmentProperties()
	{
		return environmentProperties;
	}

	public Thread validateData(boolean isExecutionFromPanel) throws Exception
	{
		apiDataGridMigrateThread = new APIDataGridMigrateThread(apiAction.getInjector(), apiDataGridController, isExecutionFromPanel);
		Thread toReturn = new Thread(apiDataGridMigrateThread);
		toReturn.start();
		return toReturn;
	}

	public BuildMain getBuildMain()
	{
		return BuildMain;
	}

	public void setDataGridVisible(boolean isVisible) throws Exception
	{
		apiDataGridController.setDataGridVisible(isVisible);
	}

	public boolean isExecutionStopped()
	{
		if ( apiAction.getActionManager() == null )
		{
			return false;
		}
		return apiAction.getActionManager().isExecutionStopped();
	}

	public APIAction getApiAction()
	{
		return apiAction;
	}

	public APIDataGridMigrateThread getApiDataGridMigrateThread()
	{
		return apiDataGridMigrateThread;
	}

}