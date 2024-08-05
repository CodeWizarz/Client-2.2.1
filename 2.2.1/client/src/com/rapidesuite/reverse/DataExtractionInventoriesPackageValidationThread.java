package com.rapidesuite.reverse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.springframework.util.Assert;

import com.rapidesuite.client.common.gui.InventoriesPackageSelectionPanel;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.SevenZipUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.client.SharedUtil.ControlDataType;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.reverse.gui.DataExtractionPackagesSelectionPanel;
import com.rapidesuite.reverse.gui.DataExtractionPanel;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;

public class DataExtractionInventoriesPackageValidationThread implements Runnable
{

	private DataExtractionPackagesSelectionPanel dataExtractionPackagesSelectionPanel;

	public DataExtractionInventoriesPackageValidationThread(DataExtractionPackagesSelectionPanel dataExtractionPackagesSelectionPanel)
	{
		this.dataExtractionPackagesSelectionPanel=dataExtractionPackagesSelectionPanel;
	}

	public void run() {
		try{
			validate();
		}
		catch(Throwable e) {
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(dataExtractionPackagesSelectionPanel.getMessageLabel(),"Error: "+ CoreUtil.getAllThrowableMessages(e));
		}
		finally {
			dataExtractionPackagesSelectionPanel.getNextButton().setEnabled(true);
			dataExtractionPackagesSelectionPanel.getBackButton().setEnabled(true);
			dataExtractionPackagesSelectionPanel.getInventoriesPackageSelectionButton().setEnabled(true);
		}
	}

	public void validate()
	throws Exception
	{
		dataExtractionPackagesSelectionPanel.getSwiftGUIMain().getExecutionPanel().getExecutionStatusPanel().resetPanel();

		if (dataExtractionPackagesSelectionPanel.getInventoriesPackageFile()==null) {
			throw new Exception("You must select an inventories package.");
		}
		if (!dataExtractionPackagesSelectionPanel.getInventoriesPackageFile().exists()) {
			throw new Exception("Cannot find the file specified: "+dataExtractionPackagesSelectionPanel.getInventoriesPackageFile().getAbsolutePath());
		}

		final File packageFile = dataExtractionPackagesSelectionPanel.getInventoriesPackageFile();
		File tempOutputFolder = new File(Config.getTempFolder(), "zip_unpack_"+CoreUtil.getFileNameWithoutExtension(packageFile));
        org.apache.commons.io.FileUtils.deleteDirectory(tempOutputFolder);
        tempOutputFolder.mkdirs();
		SevenZipUtils.decompressFile(packageFile, tempOutputFolder);
		
		String inventoriesPackageEBSVersion=FileUtils.getEBSTargetVersionFromInventoriesPackage(tempOutputFolder, CoreUtil.getFileExtension(packageFile));

		if (!((ReverseMain)dataExtractionPackagesSelectionPanel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().matchesTheEBSDBVersion(inventoriesPackageEBSVersion)) {
			if (ReverseMain.IS_FUSION_DB) {
				throw new Exception("Oracle FUSION version: '"+((ReverseMain)dataExtractionPackagesSelectionPanel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().getDbEBSVersion()
					+"' is incompatible with this inventories package targeted "+
					"for Oracle FUSION version: '"+inventoriesPackageEBSVersion+"'");	
			}
			else {
				throw new Exception("Oracle EBS version: '"+((ReverseMain)dataExtractionPackagesSelectionPanel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().getDbEBSVersion()
					+"' is incompatible with this inventories package targeted "+
					"for Oracle EBS version: '"+inventoriesPackageEBSVersion+"'");
			}
		}
		
		controlGatherAndInstallPackages(this.dataExtractionPackagesSelectionPanel, ControlDataType.REVERSE_SQL, tempOutputFolder);
		
        LinkedHashMap<String, String> displayedPathToActualXmlPathMap = DataExtractionFileUtils.getDisplayedPathToActualXmlPathMapFrom7ZIPFileThatAreNotInControlFolder(
                ((ReverseMain)dataExtractionPackagesSelectionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().getInventoriesPackageFile());
        SortedMap<String, SortedSet<File>> inventoryToSQLFileNamesMap=DataExtractionFileUtils.getInventoryToSqlFileMap(new ArrayList<String>(displayedPathToActualXmlPathMap.values()),
                new File(this.dataExtractionPackagesSelectionPanel.getControlFolder(), ControlDataType.REVERSE_SQL.getControlDataDirectoryNameAsAllLowercase()));
		dataExtractionPackagesSelectionPanel.setInventoryToSQLFileNamesMap(inventoryToSQLFileNamesMap);
		initializeInventoryTypeMaps(new ArrayList<String>(displayedPathToActualXmlPathMap.values()),inventoryToSQLFileNamesMap,dataExtractionPackagesSelectionPanel.getInventoriesPackageFile());

		dataExtractionPackagesSelectionPanel.setDisplayedItemPathToActualItemPathOrderedMap(displayedPathToActualXmlPathMap);
		dataExtractionPackagesSelectionPanel.parseInventories();
		GUIUtils.showSuccessMessage(dataExtractionPackagesSelectionPanel.getMessageLabel(),"Valid packages.");
		dataExtractionPackagesSelectionPanel.getInventoriesPackageSelectionButton().setEnabled(true);
		dataExtractionPackagesSelectionPanel.getSwiftGUIMain().resetExecutionJobManagerList();
		dataExtractionPackagesSelectionPanel.getSwiftGUIMain().initExecutionPanel();

		if (dataExtractionPackagesSelectionPanel.getInventoriesPackageFile() != null) {
			dataExtractionPackagesSelectionPanel.getSwiftGUIMain().setInformationLabelText(dataExtractionPackagesSelectionPanel.getInventoriesPackageFile().getName(), ((ReverseMain)dataExtractionPackagesSelectionPanel.getSwiftGUIMain()).getInventoriesNameLabelIndex());
		}

		DataExtractionPanel dataExtractionPanel=(DataExtractionPanel)dataExtractionPackagesSelectionPanel.getSwiftGUIMain().getExecutionPanel();
		if (ReverseMain.IS_FUSION_DB) {
			dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().updateLabelsForFusion();
		}
		dataExtractionPackagesSelectionPanel.getSwiftGUIMain().switchToPanel(UtilsConstants.PANEL_DATA_EXECUTION_SELECTION);
	}

    public static void controlGatherAndInstallPackages(InventoriesPackageSelectionPanel dataExtractionPackagesSelectionPanel, ControlDataType controlDataType, final File tempOutputFolder) throws IOException, Exception
    {

        GUIUtils.showInProgressMessage(dataExtractionPackagesSelectionPanel.getMessageLabel(),"Please wait, unpacking resources from the inventories package...");

        File tempControlFolder = new File(tempOutputFolder, CoreConstants.CONTROL_OUTPUT_FOLDER_NAME);
        Assert.isTrue(tempControlFolder.exists(), "Control folder does not exist. Is your inventory package valid?");
        Assert.isTrue(tempControlFolder.isDirectory(), "Control folder is not a directory");
        File controlFolder = new File(Config.getTempFolder(), CoreConstants.CONTROL_OUTPUT_FOLDER_NAME);
        dataExtractionPackagesSelectionPanel.setControlFolder(controlFolder);
        org.apache.commons.io.FileUtils.deleteDirectory(controlFolder);
        org.apache.commons.io.FileUtils.moveDirectory(tempControlFolder, controlFolder);
        org.apache.commons.io.FileUtils.deleteDirectory(tempOutputFolder);

        File reverseSqlFolder = new File(controlFolder, controlDataType.getControlDataDirectoryNameAsAllLowercase());
        boolean hasPlsqlPackage = false;
        for ( File file : reverseSqlFolder.listFiles() )
        {
            if ( CoreConstants.isSqlPackageFileName(file.getAbsolutePath()) )
            {
            	Assert.isTrue(!hasPlsqlPackage, "There are multiple PL/SQL packages");
                dataExtractionPackagesSelectionPanel.getPackageNameToFileMap().put(CoreUtil.getFileNameWithoutExtension(file), file);
                final String packageName = CoreUtil.getFileNameWithoutExtension(file.getAbsoluteFile());
                dataExtractionPackagesSelectionPanel.getSwiftGUIMain().getReplacementsProperties().setProperty(UtilsConstants.RES_PLSQL_PACKAGE_NAME_KEY, packageName);
                hasPlsqlPackage = true;
            }
        }
        if (!hasPlsqlPackage) {
        	throw new RuntimeException("No PL/SQL package is found");
        }

        dataExtractionPackagesSelectionPanel.getPlsqlPackageValidationPanel().installPLSQLPackage(
        		dataExtractionPackagesSelectionPanel.getSwiftGUIMain().getEnvironmentPropertiesMap(),
        		dataExtractionPackagesSelectionPanel.getPackageNameToFileMap(),
        		dataExtractionPackagesSelectionPanel.getSwiftGUIMain().getReplacementsProperties(),
        		true);
    }

    private void initializeInventoryTypeMaps(List<String> itemPaths,SortedMap<String, SortedSet<File>> inventoryToSQLFileNamesMap,File zipFile)
	throws Exception
	{
		Map<String,Boolean> instanceLevelInventoryNameMap=dataExtractionPackagesSelectionPanel.getInstanceLevelInventoryNameMap();
		Map<String,Boolean> operatingUnitLevelInventoryNameMap=dataExtractionPackagesSelectionPanel.getOperatingUnitLevelInventoryNameMap();
		Map<String,Boolean> businessGroupLevelInventoryNameMap=dataExtractionPackagesSelectionPanel.getBusinessGroupLevelInventoryNameMap();
		for (String itemPath:itemPaths) {
			String[] splitItemPath=itemPath.split("/");
			for (int i=0;i<splitItemPath.length;i++) {
				String splitItem=splitItemPath[i];
				if (i== (splitItemPath.length-1) && splitItem.endsWith(".xml")) {
					String inventoryName=splitItem.replaceAll(".xml","");
					SortedSet<File> sqlFiles=inventoryToSQLFileNamesMap.get(inventoryName);
					if (sqlFiles==null || sqlFiles.isEmpty()) {
						FileUtils.println("DEBUG: initializeInventoryTypeMaps, no SQL files for the table: '"+inventoryName+"'");
						continue;
					}
					File sqlFile = sqlFiles.iterator().next();
					try{
					    String content=FileUtils.readContentsFromSQLFile(sqlFile);

						if (DataExtractionFileUtils.isInstanceLevelQuery(content)) {
							instanceLevelInventoryNameMap.put(inventoryName,true);
						}
						else {
							if (DataExtractionFileUtils.isOperatingUnitLevelQuery(content)) {
								operatingUnitLevelInventoryNameMap.put(inventoryName,true);
							}
							if (DataExtractionFileUtils.isBusinessGroupLevelQuery(content)) {
								businessGroupLevelInventoryNameMap.put(inventoryName,true);
							}
						}
					}
					catch(Exception e) {
						FileUtils.printStackTrace(e);
					}
				}
			}
		}
	}

}