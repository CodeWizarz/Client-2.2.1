/**************************************************
 * $Revision: 46849 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-04-02 17:27:23 +0700 (Thu, 02 Apr 2015) $:
 * $HeadURL: http://svn01.rapidesuite.com:999/svn/a/dev/rapidesuite/programs/trunk/client/src/com/rapidesuite/reverse/ReverseMain.java $:
 * $Id: ReverseMain.java 46849 2015-04-02 10:27:23Z olivier.deruelle $:
 */

package com.rapidesuite.inject;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.inject.gui.ApplicationInfoPanel;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.inject.gui.TableGridsTabbed;
import com.rapidesuite.snapshot.model.ModelUtils;

/*
 * comment #1
 */
public class InjectMain extends SwiftGUIMain
{

	public final static String APPLICATION_INFO_PANEL = "APPLICATION_INFO_PANEL";
	public final static String EXECUTION_PANEL = "EXECUTION_PANEL";
			
	private JFrame frame;
	private JPanel cards;
	private ApplicationInfoPanel applicationInfoPanel;
	private ExecutionPanel executionPanel;
	private File injectTempFolder;
	private String automatedInjectionPackageFilePath;
	
	public static final int FRAME_WIDTH=1340;
	public static final int FRAME_HEIGHT=760;

	public static int FONT_SIZE_SMALL=12;
	public static int FONT_SIZE_NORMAL=12;
	public static int FONT_SIZE_BIG=12;
	public static final String REGISTRATION_VIDEO_URL = "http://www.rapid4cloud.com/follow-this-simple-guide-to-activate-rapidinject/";
	
	public InjectMain(String[] args)	throws Exception {
		super(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.inject,true,args==null);
		super.createComponents(false,REGISTRATION_VIDEO_URL,getApplicationIconPath());
		
		File file=new File(FileUtils.getUserHomeFolder(),SwiftGUIMain.getInstance().getShortApplicationName().toString());
		injectTempFolder=new File(file,Config.getClientTempFolderName());
		org.apache.commons.io.FileUtils.deleteDirectory(injectTempFolder);
		injectTempFolder.mkdirs();
		
		if (args!=null && args.length>0) {
			automatedInjectionPackageFilePath=args[0];
		}
		
		frame=super.getRootFrame();
		createComponents();
		if (args==null) {
			ModelUtils.createRegistrationWindow(frame,CoreConstants.SHORT_APPLICATION_NAME.inject.toString(),false,false,REGISTRATION_VIDEO_URL,InjectMain.getSharedApplicationIconPath());
		}
	}

	public ScriptManager getScriptManager() {
		return executionPanel.getScriptManager();
	}	

	public void createComponents() throws Exception{
		frame.setTitle(getRootFrameTitle());
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setLayout(new BorderLayout());
		
		executionPanel=new ExecutionPanel(this);
		applicationInfoPanel=new ApplicationInfoPanel(this);
				
		cards = new JPanel(new CardLayout());
		frame.getContentPane().add(cards);
			
		cards.add(applicationInfoPanel, APPLICATION_INFO_PANEL);
		cards.add(executionPanel, EXECUTION_PANEL);
		
		frame.setVisible(true);
	}

	@Override
	public void showUpdatesFrame() {
	}

	@Override
	public void initEnvironment() throws Exception {
	}

	@Override
	public void initExecutionPanel() throws Exception {
	}

	@Override
	public Map<String, String> getEnvironmentPropertiesMap() {
		return null;
	}

	@Override
	protected void runAutomatically() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Set<File> getFilesToAttachInTicket() {
		final Set<File> output = new HashSet<File>();

		File logsFolder = SwiftBuildFileUtils.getLogsFolderFromName(this.applicationInfoPanel.getInjectionPackage().getName());
		File file=new File(Config.getTempFolder(),"fusionInjectionsLogs.zip");
		try {
			FileUtils.zipFolder(logsFolder,file);
			output.add(file);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		if (FileUtils.getLogFile() != null) {
			output.add(FileUtils.getLogFile());
		}

		if (SwiftBuildFileUtils.getConsoleLogFile() != null) {
			output.add(SwiftBuildFileUtils.getConsoleLogFile());
		}		

		output.add(new File(UtilsConstants.ENGINE_PROPERTIES_FILE_NAME));
		output.add(new File(UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME));

		final File injectionPackage = this.applicationInfoPanel.getInjectionPackage();
		if (injectionPackage != null) {
			output.add(injectionPackage);
		}

		return output;
	}

	@Override
	public String getApplicationIconPath() {
		return getSharedApplicationIconPath();
	}

	public static String getSharedApplicationIconPath() {
		return "/images/inject/inject.png";
	}
	
	@Override
	protected String getRootFrameTitle() {
		String rootFrameTitle = "RapidInject - " + this.getApplicationVersion();
		return rootFrameTitle;
	}

	public void close() {
		cleanup();
		System.exit(0);
	}
	
	public void cleanup() {
		executionPanel.getExecutionTabPanel().stopExecution();
		executionPanel.getExecutionTabPanel().cleanupLastExecution();
	}

	public static void main(String[] args)
	{
		try
		{
			// this needs to be called before creating any components !!!
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("TabbedPane.selected", Color.decode("#047FC0"));
			TableGridsTabbed.initJTreeUI(InjectMain.class);
			
			new InjectMain(args);
		}
		catch (Throwable t)
		{
			onFatalErrorDuringApplicationInitialization(t);
		}
	}

	public JFrame getFrame() {
		return frame;
	}

	public void moveToPanel(String panel) {
		CardLayout cl = (CardLayout)(cards.getLayout());
	    cl.show(cards, panel);
	}

	public ExecutionPanel getExecutionPanelUI(){
		return executionPanel;
	}

	public ApplicationInfoPanel getApplicationInfoPanel() {
		return applicationInfoPanel;
	}
	
	public File getInjectTempFolder() {
		return injectTempFolder;
	}

	public String getAutomatedInjectionPackageFilePath() {
		return automatedInjectionPackageFilePath;
	}
}