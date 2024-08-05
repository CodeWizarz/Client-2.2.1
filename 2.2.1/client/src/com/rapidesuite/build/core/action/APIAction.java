/**************************************************************
 * $Revision: 41521 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-06-09 12:38:50 +0700 (Mon, 09 Jun 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/action/APIAction.java $:
 * $Id: APIAction.java 41521 2014-06-09 05:38:50Z fajrian.yunus $:
 **************************************************************/
package com.rapidesuite.build.core.action;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import com.erapidsuite.configurator.apiscript0000.ApiScriptDocument;
import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.gui.apigrid.APIDataAction;
import com.rapidesuite.build.gui.apigrid.APIDataGridUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.domain.Inventory;

public class APIAction extends AbstractAction
{

	private BuildMain BuildMain;
	private String scriptGenerationIdAPI;
	private Inventory inventory;
	private String dataGridTitle;
	private ApiScriptDocument apiScriptDocument;
	private File injectorsPackageFile;
	private Injector injector;
	private APIDataAction apiDataAction;
	public final static String GENERATION_API_DATA_SUFFIX = "data";
	public final static String GENERATION_API_INVENTORY_SUFFIX = "inventory";

	public APIAction(Injector injector, Inventory inventory, String dataGridTitle, File injectorsPackageFile, InputStream scriptInputStream,
			Map<String, String> environmentProperties, ActionManager actionManager, String scriptGenerationIdAPI, BuildMain BuildMain) throws Exception
	{
		super(injector, scriptInputStream, environmentProperties, actionManager);
		this.dataGridTitle = dataGridTitle;
		this.injectorsPackageFile = injectorsPackageFile;
		this.injector = injector;
		this.inventory = inventory;
		this.scriptGenerationIdAPI = scriptGenerationIdAPI;
		this.BuildMain = BuildMain;
	}

	public void setActionManager(ActionManager actionManager)
	{
		this.actionManager = actionManager;
	}

	public Inventory getInventory()
	{
		return inventory;
	}


	public void start()
	{
		processDataViaOracleAPI(true);
	}

	public Map<String, String> getEnvironmentProperties() throws Exception
	{
		return environmentProperties;
	}

	private Thread mainThread = null;
	
	public synchronized void join() throws InterruptedException
	{
	    if ( this.mainThread != null )
	    {
	        this.mainThread.join();
	    }
	}
	
	public void processDataViaOracleAPI(final boolean isExecutionFromPanel)
	{
	    this.mainThread  = new Thread()
		{
			public void run()
			{
				try
				{
					if ( apiDataAction.getAPIDataGridController().isProcessStarted() )
					{
						apiDataAction.getAPIDataGridController().setProcessButtontoStoppedAndUnLockGrid();
						return;
					}
					startProcess(isExecutionFromPanel);
					fireActionCompletedEvent();
				}
				catch ( Exception e )
				{
					FileUtils.printStackTrace(e);
					if ( actionManager != null )
					{
						actionManager.stopExecution();
					}
					GUIUtils.popupErrorMessage("Error: " + e.getMessage());
				}
			}
		};
		mainThread.start();
	}

	private void startProcess(boolean isExecutionFromPanel) throws Exception
	{
		if ( actionManager != null )
		{
			actionManager.setOutput("API: injecting data...");
		}
		apiDataAction.getAPIDataGridController().getInventoryDataGridFrame().setErrorMessage(null);
		apiDataAction.getAPIDataGridController().setProcessButtontoStartedAndLockGrid();
		Thread validateThread = apiDataAction.validateData(isExecutionFromPanel);
		while ( apiDataAction.getAPIDataGridController().isProcessStarted() )
		{
			if ( isExecutionFromPanel && actionManager != null && actionManager.isExecutionStopped() )
			{
				break;
			}
			com.rapidesuite.client.common.util.Utils.sleep(1000);
		}
		
		validateThread.join();
		
		apiDataAction.getAPIDataGridController().serializeDataGridRows(apiDataAction.getApiDataGridMigrateThread().getAllDataGridRowsAfterValidation(), null, true, false);
		apiDataAction.getApiDataGridMigrateThread().setAllDataGridRows(new ArrayList<String[]>());
		apiDataAction.getAPIDataGridController().refreshGrid(false);
		if ( actionManager != null )
		{
			String output = "API: script completed.";
			actionManager.setOutput(output);
		}
	}

	public ApiScriptDocument getApiScriptDocument()
	{
		return apiScriptDocument;
	}

	public void setDataGridVisible(boolean isVisible) throws Exception
	{
		apiDataAction.setDataGridVisible(isVisible);
	}

	public Injector getInjector()
	{
		return injector;
	}

	public File getInjectorsPackageFile()
	{
		return injectorsPackageFile;
	}

	public void init() throws Exception
	{
		apiScriptDocument = APIDataGridUtils.parseAPIScriptDocument(injectorInputStream);
		apiDataAction = new APIDataAction(this, BuildMain, inventory, dataGridTitle, null, environmentProperties);
		apiDataAction.initComponents();
	}

	public void openDataGrid() throws Exception
	{
		apiDataAction.getAPIDataGridController().openDataGrid();
	}

	public String getScriptGenerationIdAPI()
	{
		return scriptGenerationIdAPI;
	}

	public APIDataAction getApiDataAction()
	{
		return apiDataAction;
	}

	public BuildMain getBuildMain() {
		return BuildMain;
	}

}