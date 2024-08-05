package com.rapidesuite.reverse.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.springframework.util.Assert;

import com.rapidesuite.client.common.gui.InventoriesPackageSelectionPanel;
import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.reverse.DataExtractionInventoriesPackageValidationThread;
import com.rapidesuite.reverse.ReverseMain;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;

@SuppressWarnings("serial")
public class DataExtractionPackagesSelectionPanel extends InventoriesPackageSelectionPanel
{

	private Map<String,Boolean> instanceLevelInventoryNameMap;
	private Map<String,Boolean> operatingUnitLevelInventoryNameMap;
	private Map<String,Boolean> businessGroupLevelInventoryNameMap;
	private SortedMap<String, SortedSet<File>> inventoryToSQLFileNamesMap;

	public DataExtractionPackagesSelectionPanel(SwiftGUIMain swiftGUIMain, final File inventoriesFile)
	throws Exception
	{
		super(swiftGUIMain);
		super.createComponents();
		super.loadDefaultInventoriesPackageFile(inventoriesFile);
	}

	public void next() throws Exception
	{
		validateInventoriesPackageFile();
	}

	public Map<String, Boolean> getInstanceLevelInventoryNameMap() {
		return instanceLevelInventoryNameMap;
	}

	public Map<String, Boolean> getOperatingUnitLevelInventoryNameMap() {
		return operatingUnitLevelInventoryNameMap;
	}

	public Map<String, Boolean> getBusinessGroupLevelInventoryNameMap() {
		return businessGroupLevelInventoryNameMap;
	}

	public boolean isInstanceLevel(String inventoryName)
	{
		return instanceLevelInventoryNameMap.get(inventoryName)!=null;
	}

	public boolean isOperatingUnitLevel(String inventoryName)
	{
		return operatingUnitLevelInventoryNameMap.get(inventoryName)!=null;
	}

	public boolean isBusinessGroupLevel(String inventoryName)
	{
		return businessGroupLevelInventoryNameMap.get(inventoryName)!=null;
	}

	public SortedMap<String, SortedSet<File>> getInventoryToSQLFileMap()
	{
		return inventoryToSQLFileNamesMap;
	}

	public void setInventoryToSQLFileNamesMap(SortedMap<String, SortedSet<File>> inventoryToSQLFileNamesMap)
	{
		this.inventoryToSQLFileNamesMap=inventoryToSQLFileNamesMap;
	}

	public InventoryTreeNode getInventoryTree(boolean	isRemoveUnreversible) throws Exception {
		DataExtractionInventoryTreeNode inventoryTreeNode=(DataExtractionInventoryTreeNode) DataExtractionFileUtils.buildTree(
				((ReverseMain)swiftGUIMain).getDataExtractionPanel(),inventoriesMap,displayedItemPathToActualItemPathOrderedMap,getInventoryToSQLFileMap());
		if (isRemoveUnreversible) {
			inventoryTreeNode.removeInventoryNodesUnreversible();
		}
		inventoryTreeNode.setErrorOnMissingSQLFiles();
		return inventoryTreeNode;
	}

	public void validateInventoriesPackageFile()
	{
		inventoriesPackageFileBrowser.setHasFileChanged(false);
		GUIUtils.showInProgressMessage(messageLabel,"Please wait, validating the inventories package...");
		getInventoriesPackageSelectionButton().setEnabled(false);
		nextButton.setEnabled(false);
		backButton.setEnabled(false);

		instanceLevelInventoryNameMap=new HashMap<String,Boolean>();
		operatingUnitLevelInventoryNameMap=new HashMap<String,Boolean>();
		businessGroupLevelInventoryNameMap=new HashMap<String,Boolean>();
		DataExtractionInventoriesPackageValidationThread bt = new DataExtractionInventoriesPackageValidationThread(this);
		new Thread(bt).start();
	}

    @Override
    public boolean operate() throws Throwable {
    	Assert.isTrue(Config.isAutomatedRun(), "must be in automated run mode");

		onNextButtonIsClicked.actionPerformed(null);

		while (!this.nextButton.isEnabled()) { //next button is disabled during processing
			com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.REVERSE_VALIDATE_AUTOMATED_RUN_WAITING_TIME_PERIOD_MS);
		}

    	return true;
    }

}