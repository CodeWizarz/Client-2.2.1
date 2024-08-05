/**************************************************
 * $Revision: 54560 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-04-21 13:45:36 +0700 (Thu, 21 Apr 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/ReverseMain.java $:
 * $Id: ReverseMain.java 54560 2016-04-21 06:45:36Z jannarong.wadthong $:
 */

package com.rapidesuite.reverse;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.util.Assert;

import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.reverse.gui.DataExtractionEBSPropertiesValidationPanel;
import com.rapidesuite.reverse.gui.DataExtractionPackagesSelectionPanel;
import com.rapidesuite.reverse.gui.DataExtractionPanel;
import com.rapidesuite.reverse.utils.DataExtractionUtils;
import com.rapidesuite.snapshot.model.ModelUtils;

public class ReverseMain extends SwiftGUIMain
{
	private DataExtractionPanel dataExtractionPanel;
	protected DataExtractionPackagesSelectionPanel dataExtractionPackagesSelectionPanel;
	private DataExtractionEBSPropertiesValidationPanel dataExtractionEBSPropertiesValidationPanel;
	
	private int inventoriesNameLabelIndex;
	private boolean cancelAutomatedRun;
	public static boolean IS_FUSION_DB=false;
	public static final String REGISTRATION_VIDEO_URL = "http://www.rapid4cloud.com/follow-this-simple-guide-to-activate-rapidreverse/";
	
	public ReverseMain() throws Exception{
	    super(Config.getLogFolder(),CoreConstants.SHORT_APPLICATION_NAME.reverse,false);
	    this.cancelAutomatedRun = false;
		super.createComponents(REGISTRATION_VIDEO_URL,getApplicationIconPath());
		createComponents();
		ModelUtils.createRegistrationWindow(super.getRootFrame(),CoreConstants.SHORT_APPLICATION_NAME.reverse.toString(),false,false,REGISTRATION_VIDEO_URL,getApplicationIconPath());
		checkUpdateSilently(UtilsConstants.EXECUTABLE_NAME_REVERSE);
		if (!cancelAutomatedRun && Config.isAutomatedRun()) {
			runAutomatically();
		}
	}

	public DataExtractionPanel getDataExtractionPanel() {
		return dataExtractionPanel;
	}

	public void createComponents() throws Exception{
		dataExtractionPanel=new DataExtractionPanel(this);
		super.executionPanel=dataExtractionPanel;
		
		dataExtractionEBSPropertiesValidationPanel = new DataExtractionEBSPropertiesValidationPanel(this);
		cardComponent.add(dataExtractionEBSPropertiesValidationPanel, UtilsConstants.PANEL_ENVIRONMENT_SELECTION);
		
		File bweFile = null;
		File inventoriesFile = null;
		Exception exceptionWhenRetrievingBweFileAndInventoriesFile = null;
		try {
			bweFile = Config.getReverseEnvironmentFile();
			inventoriesFile = Config.getReverseInventoriesPackageLocation();
			
			if (Config.isAutomatedRun()) {
				Assert.notNull(bweFile, CoreConstants.CONFIG_CLIENT_REVERSE_ENVIRONMENT_FILE+" property in "+UtilsConstants.ENGINE_PROPERTIES_FILE_NAME+
						" file is required for automated operation");
				Assert.notNull(inventoriesFile, CoreConstants.CONFIG_CLIENT_REVERSE_INVENTORIES_PACKAGE_LOCATION+" property in "+UtilsConstants.ENGINE_PROPERTIES_FILE_NAME+
						" file is required for automated operation");						
			}
			
		} catch (Exception e) {
			exceptionWhenRetrievingBweFileAndInventoriesFile = e;
			if (Config.isAutomatedRun() && Config.getReverseTerminateAfterFailedInitializationInAutomatedMode()) {
				throw e;
			} else {
				cancelAutomatedRun = true;
				FileUtils.printStackTrace(e);
			}
		}
		
		dataExtractionPackagesSelectionPanel = new DataExtractionPackagesSelectionPanel(this, inventoriesFile);
		
		cardComponent.add(dataExtractionPackagesSelectionPanel, UtilsConstants.PANEL_PACKAGES_SELECTION);
		cardComponent.add(dataExtractionPanel, UtilsConstants.PANEL_DATA_EXECUTION_SELECTION);
		cardLayout.show(cardComponent,UtilsConstants.PANEL_ENVIRONMENT_SELECTION);
	
		rootFrame.setTitle(getRootFrameTitle());
		rootFrame.setSize(DataExtractionConstants.JFRAME_WIDTH,DataExtractionConstants.JFRAME_HEIGHT);
		rootFrame.setVisible(true);
		
		//without validating the bwe, the inventories package can't be validated (the 2nd screen)
		dataExtractionEBSPropertiesValidationPanel.validateDefaultEnvironment(bweFile);			
		
		this.inventoriesNameLabelIndex = this.appendToInformationLabels();
		
		try {
			if (exceptionWhenRetrievingBweFileAndInventoriesFile != null) {
				throw exceptionWhenRetrievingBweFileAndInventoriesFile;
			}
			Config.validateConfig();
		} catch (Exception e) {
			GUIUtils.showErrorMessage(dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getMessageLabel(), e.getMessage());
		}
	}
	
	@Override
	protected void runAutomatically() {
		Assert.isTrue(Config.isAutomatedRun(), "must be in automated run mode");
		
		try {
			FileUtils.println("Running reverse automatically");

			final boolean bweScreenIsPassed = this.dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().operate();
			if (!bweScreenIsPassed) {
				final String filename = UtilsConstants.REVERSE_VALIDATE_BWE_VALIDATION_FAILURE_SCREENSHOT_PREFIX+CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_PRECISE.getDateFormat().format(new java.util.Date());
				GUIUtils.captureScreenshotAndSaveAsJpg(FileUtils.getLogFolder(), filename);
				FileUtils.println("Environment validation failed. Please check the screenshot: "+new File(FileUtils.getLogFolder(), filename).getPath());
				System.exit(1);
			}
			
			final boolean inventoriesSelectionScreenIsPassed = this.dataExtractionPackagesSelectionPanel.operate();
			if (!inventoriesSelectionScreenIsPassed) {
				return;
			}
			
			final boolean reverseIsSuccessful = this.dataExtractionPanel.operate();
			
			if (reverseIsSuccessful) {
				FileUtils.println("Terminating reverse normally after running it automatically");
				System.exit(0);
			}			
		} catch (Throwable t) {
			FileUtils.printStackTrace(t);
			GUIUtils.popupErrorMessage(CoreUtil.getAllThrowableMessages(t));
		}

	}

	public void startReverseExecution(List<InventoryTreeNode> inventoryTreeNodes)
	throws Exception
	{
		List<Long> selectedOperatingUnitIds=dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getOperatingUnitSelectionPanel().getSelectedOperatingUnitIds();
		List<Long> selectedBusinessGroupIds=dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getBusinessGroupSelectionPanel().getSelectedBusinessGroupIds();

		boolean isIncludeMode=dataExtractionPanel.getOracleUsersSelectionPanel().isIncludeOracleUsers();
		List<Integer> selectedUserIds=dataExtractionPanel.getOracleUsersSelectionPanel().getSelectedOracleUserIds();
		String formattedFromDate=dataExtractionPanel.getOracleDatesSelectionPanel().getFormattedFromDate();
		String formattedToDate=dataExtractionPanel.getOracleDatesSelectionPanel().getFormattedToDate();

		executionJobManager =new DataExtractionJobManager(
				this,
				dataExtractionPanel,
				isIncludeMode,
				selectedUserIds,
				formattedFromDate,
				formattedToDate,
				dataExtractionPanel.getOracleUsersSelectionPanel().getAllOracleUserIdToNameSynchronizedMap(),
				dataExtractionPanel.getDataExtractionOptionsPanel().getWorkersCount(),
				selectedOperatingUnitIds,
				selectedBusinessGroupIds);
		executionJobManager.execute(inventoryTreeNodes);
	}

	public void resetExecutionJobManagerList()
	{
		executionJobManager=null;
	}

	public boolean isReversalStopped()
	{
		return true;
	}

	public void setExecutionCompleted(String message)
	{
		GUIUtils.showSuccessMessage(dataExtractionPanel.getStatusLabel(),message);
	}

	public boolean isAnalyseMode() {
		return dataExtractionPanel.isAnalyseMode();
	}

	public boolean isAuditExtractionEnabled() {
		return dataExtractionPanel.getDataExtractionOptionsPanel().isAuditExtractionEnabled();
	}

	public static void main(String[] args)
	{
		try
		{
			new ReverseMain();
		}
		catch (Throwable t)
		{
			onFatalErrorDuringApplicationInitialization(t);
		}
	}

	public void initEnvironment() throws Exception {
		TreeMap<Long, String> operatingUnitIdToNameMap=DatabaseUtils.getOperatingUnitIdToNameMap(
				dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
				Config.getReversePrintSqlToLog(),
				dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getMessageLabel());

		TreeMap<Long, String> businessGroupIdToNameMap=DatabaseUtils.getBusinessGroupIdToNameMap(
				dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
				Config.getReversePrintSqlToLog(),
				dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getMessageLabel());

		TreeMap<Long, Long> operatingUnitIdToBusinessGroupIdMap=DatabaseUtils.getOperatingUnitIdToBusinessGroupIdMap(
				dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
				Config.getReversePrintSqlToLog(),
				dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getMessageLabel());

		GUIUtils.showInProgressMessage(dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getMessageLabel(),"Please wait, gathering Oracle seeded users...");
		dataExtractionPanel.loadSeededOracleUsers();
		dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getOperatingUnitSelectionPanel().initOperatingUnits(
				operatingUnitIdToNameMap,DataExtractionConstants.OU_SCROLLPANE_WIDTH,DataExtractionConstants.OU_SCROLLPANE_HEIGHT);
		dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getBusinessGroupSelectionPanel().initBusinessGroups(
				businessGroupIdToNameMap, operatingUnitIdToBusinessGroupIdMap,DataExtractionConstants.BG_SCROLLPANE_WIDTH,DataExtractionConstants.BG_SCROLLPANE_HEIGHT);
	}

	public void initExecutionPanel() throws Exception{
		GUIUtils.resetLabel(dataExtractionPanel.getStatusLabel());
		dataExtractionPanel.getTemplateSelectionPanel().resetSelectedTemplate();
		dataExtractionPanel.getTemplateSelectionPanel().loadTemplate(Config.getReverseTemplateName());
	}

	public DataExtractionPackagesSelectionPanel getDataExtractionPackagesSelectionPanel() {
		return dataExtractionPackagesSelectionPanel;
	}

	public DataExtractionEBSPropertiesValidationPanel getDataExtractionEBSPropertiesValidationPanel() {
		return dataExtractionEBSPropertiesValidationPanel;
	}

	public void showUpdatesFrame()
	{
		super.showUpdatesFrame(UtilsConstants.EXECUTABLE_NAME_REVERSE);
	}


	public Map<String,String> getEnvironmentPropertiesMap()
	{
	    return this.dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getEnvironmentPropertiesMap();
	}

	@Override
	protected Set<File> getFilesToAttachInTicket() {
		final Set<File> output = new HashSet<File>();
		
		if (FileUtils.getLogFile() != null) {
			output.add(FileUtils.getLogFile());
		}

		output.add(new File(UtilsConstants.ENGINE_PROPERTIES_FILE_NAME));
		output.add(new File(UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME));
		
		final File bweFile = this.dataExtractionEBSPropertiesValidationPanel.getEnvironmentValidationPanel().getLoadedFile();
		if (bweFile != null) {
			output.add(bweFile);
		}
		
		output.add(new File("reverse.bat"));
		output.add(new File("reverse.py"));
		
		final File inventories = this.dataExtractionPackagesSelectionPanel.getInventoriesPackageFile();
		if (inventories != null) {
			output.add(inventories);
		}
		
		final File reverseTreeExportAllStatusesFile = this.dataExtractionPanel.getReverseTreeExportAllStatusesFile();
		if (reverseTreeExportAllStatusesFile != null) {
			output.add(reverseTreeExportAllStatusesFile);
		}
		
		final File reverseTreeExportOnlyError = this.dataExtractionPanel.getReverseTreeExportOnlyErrorFile();
		if (reverseTreeExportOnlyError != null) {
			output.add(reverseTreeExportOnlyError);
		}
		
		if (dataExtractionPanel != null) {
			// export the all-statuses file, the only-error file is not exported because it has to wait until Reverse's execution complete
			DataExtractionUtils.exportReverse_tree(dataExtractionPanel, UtilsConstants.TEMP_XLSX_REVERSE_TREE_ALL_STATUSES_FILE_NAME, false);
		}

		return output;
	}

	@Override
	public String getApplicationIconPath() {
		return "/images/reverse.icon.small.png";
	}	
	
	public int getInventoriesNameLabelIndex() {
		return this.inventoriesNameLabelIndex;
	}

	@Override
	protected String getRootFrameTitle() {
		 String rootFrameTitle = DataExtractionConstants.REVERSE_FRAME_TITLE + " - " + this.getApplicationVersion();
		 if (Utils.hasAccessToInternalStaffsOnlyFeatures()) {
			 rootFrameTitle += " :: DEBUG MODE :: Expires on "+CoreConstants.DATE_FORMAT_PATTERN.STANDARD.getDateFormat().format(Utils.getInternalStaffPermissionExpiry());
		}
		 return rootFrameTitle;
		
	}
}