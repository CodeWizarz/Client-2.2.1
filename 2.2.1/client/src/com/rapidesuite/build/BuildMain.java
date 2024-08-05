package com.rapidesuite.build;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.util.Assert;

import com.rapidesuite.build.core.ManualStopException;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.gui.panels.InjectorsPackageExecutionPanel;
import com.rapidesuite.build.gui.panels.InjectorsPackageSelectionPanel;
import com.rapidesuite.build.gui.panels.SwiftBuildHtmlValidationPanel;
import com.rapidesuite.build.gui.panels.SwiftBuildPropertiesValidationPanel;
import com.rapidesuite.build.utils.IterationLogWriter;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.EbsServerUrlAndBwpCombination;
import com.rapidesuite.client.common.gui.CustomFileFilter;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.gui.datagrid.inventory.InventoryDataGridConstants;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.TempFileCleaner;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.snapshot.model.ModelUtils;

public class BuildMain extends SwiftGUIMain
{

	public static final int SLEEPING_TIME_TO_CAPTURE_SCREENSHOT_IN_SECOND = 20;
	public static final String SCREENSHOT_FOR_OPEN_TICKET = "sceenshot_for_open_ticket";
	private InjectorsPackageSelectionPanel injectorsPackageSelectionPanel;
	private SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel;
	private InjectorsPackageExecutionPanel injectorsExecutionPanel;
	private SwiftBuildHtmlValidationPanel swiftBuildHtmlValidationPanel;

	private boolean isFLDSplitEnabled;
	private int fldSplitMaxCount;
	private boolean isHTMLSplitEnabled;
	private int htmlSplitMaxCount;

	private JMenuItem zipIterationLogsItem;

	private Map<EbsServerUrlAndBwpCombination, File> bweBwpCombinationToDirectoryMap;

	private int bwpNameLabelIndex;
	private JMenuItem runSqlScriptItem;
	public static final String REGISTRATION_VIDEO_URL = "http://www.rapid4cloud.com/follow-this-simple-guide-to-activate-rapidbuild/";
	
	public BuildMain() throws Exception
	{
		super(Config.getLogFolder(),CoreConstants.SHORT_APPLICATION_NAME.build,false);
        startTempFileCleaner();
		initProperties();
		handleIterationLogsMattersAtStartup();
		createBuildComponents();
		ModelUtils.createRegistrationWindow(super.getRootFrame(),CoreConstants.SHORT_APPLICATION_NAME.build.toString(),false,false,
				REGISTRATION_VIDEO_URL,getApplicationIconPath());
		checkUpdateSilently(UtilsConstants.EXECUTABLE_NAME_BUILD);
	}

	private static void startTempFileCleaner()
    {
        try
        {
            final TempFileCleaner cleaner = new TempFileCleaner(Config.getTempFolder(), TimeUnit.MINUTES, Config.getTempFolderExpiryMinutes(), 60 * 1000, true);
            cleaner.addToWhiteList(new File(Config.getTempFolder(), BuildMain.SCREENSHOT_FOR_OPEN_TICKET));
            cleaner.addToWhiteList(new File(Config.getTempFolder(), CoreConstants.CONTROL_OUTPUT_FOLDER_NAME));
            cleaner.addToWhiteList(InventoryDataGridConstants.DATA_TEMP_FOLDER);
            cleaner.addToWhiteList(new File(Config.getTempFolder(), XULEngine.HTML_FOLDER_NAME));
            cleaner.addToWhiteList(new File(Config.getTempFolder(), ScriptManager.FUSIONS_ZIP_TEMP_FOLDER));
            cleaner.start();
        }
        catch ( final Exception ex )
        {
            ex.printStackTrace();
        }
    }


	public void createBuildComponents() throws Exception
	{
		super.createComponents(REGISTRATION_VIDEO_URL,getApplicationIconPath());

		this.zipIterationLogsItem = new JMenuItem();
		this.zipIterationLogsItem.setText("Zip Iteration Logs");
		this.zipIterationLogsItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Assert.notNull(injectorsExecutionPanel != null, "injectors execution panel must not be null");
				Assert.notNull(injectorsExecutionPanel.getActionManager() != null, "action manager must not be null");
				Assert.isTrue(injectorsExecutionPanel.getActionManager().isExecutionStopped(), "action manager must not be running");
				zipIterationLogs();
				GUIUtils.popupInformationMessage("Iteration logs have been zipped");
				zipIterationLogsItem.setEnabled(false);
			}

		});
		this.zipIterationLogsItem.setEnabled(false);

		this.menuTools.add(this.zipIterationLogsItem);

		this.bwpNameLabelIndex = this.appendToInformationLabels();

		runSqlScriptItem = new JMenuItem();
		runSqlScriptItem.setText("Run SQL Script");
		runSqlScriptItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				runSqlScript();
			}
		});
		runSqlScriptItem.setVisible(Utils.hasAccessToInternalStaffsOnlyFeatures());
		this.menuTools.add(runSqlScriptItem);

		JPanel panel = new JPanel();
		// panel.setBackground(Color.RED);
		panel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		rootFrame.getContentPane().add(panel, BorderLayout.WEST);

		swiftBuildPropertiesValidationPanel = new SwiftBuildPropertiesValidationPanel(this);
		injectorsPackageSelectionPanel = new InjectorsPackageSelectionPanel(this);
		injectorsExecutionPanel = new InjectorsPackageExecutionPanel(this);
		swiftBuildHtmlValidationPanel = new SwiftBuildHtmlValidationPanel(swiftBuildPropertiesValidationPanel);

		cardComponent.add(injectorsPackageSelectionPanel, SwiftBuildConstants.PANEL_INJECTORS_PACKAGE_SELECTION);
		cardComponent.add(swiftBuildPropertiesValidationPanel, SwiftBuildConstants.PANEL_ENVIRONMENT_VALIDATION);
		cardComponent.add(injectorsExecutionPanel, SwiftBuildConstants.PANEL_INJECTORS_PACKAGE_EXECUTION);
		cardComponent.add(swiftBuildHtmlValidationPanel, SwiftBuildConstants.PANEL_HTML_VALIDATION);

		cardLayout.show(cardComponent, SwiftBuildConstants.PANEL_ENVIRONMENT_VALIDATION);

		rootFrame.setTitle(getRootFrameTitle());
		rootFrame.setLocation(0, 0);
		rootFrame.setVisible(true);

		final Dimension effectiveScreenSize = this.getEffectiveScreenSize();
		if (effectiveScreenSize.getWidth() < SwiftBuildConstants.JFRAME_WIDTH || effectiveScreenSize.getHeight() < SwiftBuildConstants.JFRAME_HEIGHT) {
			rootFrame.setExtendedState(rootFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		} else {
			rootFrame.setSize(new Dimension(SwiftBuildConstants.JFRAME_WIDTH, SwiftBuildConstants.JFRAME_HEIGHT));
		}

		boolean environmentIsLoadedAndPropertiesAreValidated = swiftBuildPropertiesValidationPanel.loadDefaultEnvironment();

		if (environmentIsLoadedAndPropertiesAreValidated && Config.isAutomatedRun()) {
			swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().actionPerformedValidationButton(false);
			if (swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().isValidationSuccess())
			{ //bwe validation is successful
				swiftBuildPropertiesValidationPanel.clickNextButtonActionPerformed();
				swiftBuildHtmlValidationPanel.clickNextButtonActionPerformed();
				injectorsPackageSelectionPanel.next();
                com.rapidesuite.client.common.util.Utils.sleep(Config.getBuildPreInjectionDelaySecondsOnAutomatedMode()*1000);
                injectorsExecutionPanel.startExecution();
			}
			else
			{
				String errorMessage = "Environment validation failed";
				System.err.println(errorMessage);
				FileUtils.println(errorMessage);
				System.exit(1);
			}
		}
	}

	public void initProperties()
	{
		try
		{
			isFLDSplitEnabled = Config.getBuildFldSplit();
			fldSplitMaxCount = Config.getBuildFldSplitMaxCount();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}

		try
		{
			isHTMLSplitEnabled = Config.getBuildHtmlSplit();
			htmlSplitMaxCount = Config.getBuildHtmlSplitMaxCount();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public void switchToPanel(String panelId)
	{
		cardLayout.show(cardComponent, panelId);
	}

	public Map<String, String> getEnvironmentProperties()
	{
		Map<String, String> toReturn = new HashMap<String, String>(swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().getEnvironmentPropertiesMap());
		toReturn.putAll(this.getSwiftBuildPropertiesValidationPanel().getEnvironmentExtraPropertiesFrame().getExtraProperties());
		toReturn.putAll(this.getSwiftBuildHtmlValidationPanel().getEnvironmentValidationPanel().getEnvironmentPropertiesMap());
		return toReturn;
	}

	public InjectorsPackageExecutionPanel getInjectorsExecutionPanel()
	{
		return injectorsExecutionPanel;
	}

	public InjectorsPackageSelectionPanel getInjectorsPackageSelectionPanel()
	{
		return injectorsPackageSelectionPanel;
	}

	public SwiftBuildPropertiesValidationPanel getSwiftBuildPropertiesValidationPanel()
	{
		return swiftBuildPropertiesValidationPanel;
	}

	public boolean isSplitEnabled(CoreConstants.INJECTOR_TYPE injectorType)
	{
		if (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injectorType) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injectorType))
		{
			return isFLDSplitEnabled;
		}
		else if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injectorType))
		{
			return isHTMLSplitEnabled;
		}
		else
		{
			return false;
		}
	}

	public int getSplitThreshold(CoreConstants.INJECTOR_TYPE injectorType)
	{
		if (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injectorType) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injectorType))
		{
			return fldSplitMaxCount;
		}
		else if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injectorType))
		{
			return htmlSplitMaxCount;
		}
		else
		{
			return -1;
		}
	}

	public boolean isSSHProtocol()
	{
		return swiftBuildPropertiesValidationPanel.isTransferProtocolSSH();
	}
	
	public boolean isPasswordBasedSSHProtocol()
	{
		return swiftBuildPropertiesValidationPanel.isTransferProtocolPasswordBasedSSH();
	}
	
	public boolean isFileProtocol() {
		return swiftBuildPropertiesValidationPanel.isTransferProtocolFile();
	}

	public void showUpdatesFrame()
	{
		super.showUpdatesFrame(UtilsConstants.EXECUTABLE_NAME_BUILD);
	}

	public static void main(String[] arg)
	{
		try
		{
			new BuildMain();
		}
		catch ( Throwable t )
		{
			onFatalErrorDuringApplicationInitialization(t);
		}
	}

	@Override
	public void initEnvironment() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void initExecutionPanel() throws Exception
	{
		// TODO Auto-generated method stub

	}

    @Override
    public Map<String, String> getEnvironmentPropertiesMap()
    {
        return this.getEnvironmentProperties();
    }

	@Override
	protected void runAutomatically() {
		//TODO: refactor the build automated run to follow reverse and validate tidier method
		throw new UnsupportedOperationException();
	}

	public JMenuItem getZipIterationLogsItem() {
		return zipIterationLogsItem;
	}

	//without synchronization, bweBwpCombinationToDirectoryMap can be potentially modified concurrently by on shutdown thread and actionmanager thread

	public synchronized File getIterationLogDirectory(final EbsServerUrlAndBwpCombination bweBwpCombination, final Long historyId) throws IOException {
		File output = null;
		if (bweBwpCombinationToDirectoryMap.containsKey(bweBwpCombination)) {
			output = bweBwpCombinationToDirectoryMap.get(bweBwpCombination);
			Assert.isTrue(output.isDirectory(), "Iteration Log Folder must be a directory: " + output);
		} else {
			output = new File(Config.getBuildIterationLogFolder(), "iteration_logs_"+bweBwpCombination.getEbsServerAddress().replace(':', '-')+"_"+bweBwpCombination.getBwp().getName()+
					"_"+CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_PRECISE.getDateFormat().format(new Date()));
			IterationLogWriter.initiateIterationLogFileAndUuidIfNotExists(output, bweBwpCombination.getEbsServerAddress(), historyId);
			bweBwpCombinationToDirectoryMap.put(bweBwpCombination, output);
		}
		return output;
	}

	private synchronized void zipIterationLogs() {
		bweBwpCombinationToDirectoryMap.clear();
		final File iterationLogsFolders[] = Config.getBuildIterationLogFolder().listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		for (final File iterationLogsFolder : iterationLogsFolders) {
			try {
				final File zipFile = new File(Config.getBuildIterationLogFolder(), iterationLogsFolder.getName()+".zip");
				final Set<File> directoryContents = new HashSet<File>();
				directoryContents.addAll(Arrays.asList(iterationLogsFolder.listFiles()));
				FileUtils.zipFiles(directoryContents, zipFile);
				org.apache.commons.io.FileUtils.deleteDirectory(iterationLogsFolder);
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	private void handleIterationLogsMattersAtStartup() {
		bweBwpCombinationToDirectoryMap = new HashMap<EbsServerUrlAndBwpCombination, File>();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				zipIterationLogs();
			}

		}));
		zipIterationLogs();
	}

	@Override
	protected Set<File> getFilesToAttachInTicket() {
		final Set<File> output = new HashSet<File>();

		if (FileUtils.getLogFile() != null) {
			output.add(FileUtils.getLogFile());
		}

		if (SwiftBuildFileUtils.getConsoleLogFile() != null) {
			output.add(SwiftBuildFileUtils.getConsoleLogFile());
		}

		output.add(new File(UtilsConstants.ENGINE_PROPERTIES_FILE_NAME));

		final File bweFile = swiftBuildPropertiesValidationPanel.getEnvironmentValidationPanel().getLoadedFile();
		if (bweFile != null) {
			output.add(bweFile);
		}

		output.add(new File("build.py"));
		output.add(new File("build.bat"));

//		List<File> injectorFiles = injectorsExecutionPanel.getConvertedInjectorToHTMLList();
//		if (injectorFiles != null && !injectorFiles.isEmpty()) {
//			output.addAll(injectorFiles);
//		}
		
		File packageFile = injectorsPackageSelectionPanel.getInjectorsPackageFile();
		if (packageFile != null) {
			output.add(packageFile);
			
			File logsFolder = SwiftBuildFileUtils.getLogsFolderFromName(packageFile.getName());
			File file=new File(Config.getTempFolder(),"BuildInjectionsLogs.zip");
			try {
				FileUtils.zipFolder(logsFolder,file);
				output.add(file);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		File iterationLogDirectory = injectorsExecutionPanel.getActionManager() == null ? null : injectorsExecutionPanel.getActionManager().getSceenshotTiketDirectory();
		File file = new File(Config.getTempFolder(), "sceenshot.zip");
		if (iterationLogDirectory != null && iterationLogDirectory.exists()) {
			try {
				FileUtils.zipFolder(iterationLogDirectory,file);
				output.add(file);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		File statusesOfAllDataGridsOutputFolder = injectorsExecutionPanel.exportAllStatusesToExcelFromAllDataGrids();
		file = new File(Config.getTempFolder(), "statusesOfAllDataGrids.zip");
		if (statusesOfAllDataGridsOutputFolder != null && statusesOfAllDataGridsOutputFolder.exists()) {
			try {
				FileUtils.zipFolder(statusesOfAllDataGridsOutputFolder,file);
				output.add(file);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return output;
	}

	public int getBwpNameLabelIndex() {
		return this.bwpNameLabelIndex;
	}

	@Override
	public String getApplicationIconPath() {
		return "/images/build.icon.small.png";
	}

	public void setRunSqlScriptItemEnabled(final boolean enabled) {
		this.runSqlScriptItem.setEnabled(enabled);
		this.runSqlScriptItem.setVisible(Utils.hasAccessToInternalStaffsOnlyFeatures());
	}

	private void runSqlScript() {
		if (!Utils.hasAccessToInternalStaffsOnlyFeatures()) {
			GUIUtils.popupErrorMessage(SwiftBuildConstants.VALID_INTERNAL_STAFF_PERMISSION_FILE_IS_MISSING_MESSAGE);
			return;
		}
		boolean isInternalOracleEbsServer = true;
		try {
			isInternalOracleEbsServer = DatabaseUtils.isInternalOracleEbsServer(getEnvironmentProperties());
		} catch (Throwable t) {
			GUIUtils.popupErrorMessage(CoreUtil.getAllThrowableMessages(t));
			return;
		}
		if (!isInternalOracleEbsServer) {
			GUIUtils.popupErrorMessage("The Oracle EBS server specified is not an internal one. You can only use this feature to access an internal server");
			return;
		}
		JFileChooser fileChooser=Utils.initializeJFileChooserWithTheLastPath("FILE_CHOOSER_BUILD_MAIN");
		CustomFileFilter filter=new CustomFileFilter("sql");
		fileChooser.setFileFilter(filter);
		int returnVal = fileChooser.showDialog(null, "Select a file");
		if (returnVal == JFileChooser.APPROVE_OPTION){
			final File sqlScriptFile = fileChooser.getSelectedFile();
			final MutableBoolean terminationCheck = new MutableBoolean(false);
			final Date startingTime = new Date();
			final File outputLog = new File(Config.getTempFolder(), "sql_script_run_"+CoreUtil.getFileNameWithoutExtension(sqlScriptFile)+"-"+CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE.getDateFormat().format(startingTime)+".log");
			final Timer timer = new Timer();

			final JFrame sqlExecutionPopup = new JFrame();
			final JPanel sqlExecutionPopupPanel = new JPanel();

			final JLabel durationLabel = new JLabel("Your SQL script has run for ...");
			final JPanel durationPanel = new JPanel();

			final JButton stopButton = GUIUtils.getButton(BuildMain.class, "Stop", "/images/stop16.gif");
			final JPanel stopPanel = new JPanel();

			stopButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					terminationCheck.setValue(true);
					stopButton.setEnabled(false);
				}

			});

			durationPanel.add(durationLabel);
			stopPanel.add(stopButton);

			sqlExecutionPopupPanel.add(durationPanel);
			sqlExecutionPopupPanel.add(stopPanel);

			sqlExecutionPopupPanel.setLayout(new BoxLayout(sqlExecutionPopupPanel, BoxLayout.Y_AXIS));
			sqlExecutionPopupPanel.setVisible(true);
			sqlExecutionPopup.setContentPane(sqlExecutionPopupPanel);

			sqlExecutionPopup.setSize(220, 120);
			sqlExecutionPopup.setVisible(true);
			sqlExecutionPopup.setFocusable(true);

			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					durationLabel.setText("Your SQL script has run for "+CoreUtil.getDaysHoursMinutesSecondsFromMilliseconds(System.currentTimeMillis()- startingTime.getTime()));
				}

			}, 0, 1000L);


			new Thread(new Runnable() {

				@Override
				public void run() {
					try (PrintWriter printWriter = new PrintWriter(new FileWriter(outputLog))) {
						DatabaseUtils.executeSqlStatements(getEnvironmentProperties(), sqlScriptFile, null, true, Collections.singletonMap(printWriter, true), terminationCheck, null);
					} catch (ManualStopException e2) {
						//do nothing
					} catch (Throwable t2) {
						GUIUtils.popupErrorMessage(CoreUtil.getAllThrowableMessages(t2));
						FileUtils.printStackTrace(t2);
					} finally {
						timer.cancel();
						sqlExecutionPopup.setVisible(false);
						sqlExecutionPopup.dispose();
					}

					if (outputLog.isFile()) {
						try {
							FileUtils.startTextEditor(Config.getCmdTextEditor(), outputLog);
						} catch (Exception e1) {
							throw new Error(e1);
						}
					}
				}

			}).start();
		}
	}

	@Override
	 protected String getRootFrameTitle() {
	  String rootFrameTitle = SwiftBuildConstants.JFRAME_TITLE + " - " + this.getApplicationVersion();
	  if (Utils.hasAccessToInternalStaffsOnlyFeatures()) {
	   rootFrameTitle += " :: DEBUG MODE :: Expires on "+CoreConstants.DATE_FORMAT_PATTERN.STANDARD.getDateFormat().format(Utils.getInternalStaffPermissionExpiry());
	  }
	  return rootFrameTitle;
	 }

	public SwiftBuildHtmlValidationPanel getSwiftBuildHtmlValidationPanel() {
		return this.swiftBuildHtmlValidationPanel;
	}

}
