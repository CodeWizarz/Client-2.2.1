package com.rapidesuite.extract;

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
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.extract.view.ApplicationInfoPanel;
import com.rapidesuite.extract.view.ExtractMainPanel;
import com.rapidesuite.extract.view.UIConstants;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.snapshot.model.ModelUtils;

public class ExtractMain extends SwiftGUIMain {
	
	private JFrame frame;
	private JPanel cards;
	private ExtractMainPanel extractMainPanel;
	private ApplicationInfoPanel applicationInfoPanel;
	public static final int FRAME_WIDTH=1300;
	public static final int FRAME_HEIGHT=850;
	
	public final static String APPLICATION_INFO_PANEL = "APPLICATION_INFO_PANEL";
	public final static String EXECUTION_PANEL = "EXECUTION_PANEL";
	public static final boolean IS_CLEANUP_REPORTS_ON_THE_FLY=false;

	public ExtractMain() throws Exception {
		super(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.extract,true);
		super.createComponents(false,InjectMain.REGISTRATION_VIDEO_URL,getApplicationIconPath());
		frame=super.getRootFrame();
		createComponents();
		ModelUtils.createRegistrationWindow(frame,CoreConstants.SHORT_APPLICATION_NAME.extract.toString(),true,false,null,ExtractMain.getSharedApplicationIconPath());
	}
	
	public void close() {
		applicationInfoPanel.deleteSessionBIPublisherPath();
		System.exit(0);
	}
	
	public void createComponents() throws Exception{
		frame.setIconImage(GUIUtils.getImageIcon(this.getClass(), getApplicationIconPath()).getImage());
		frame.setTitle(UIConstants.FRAME_TITLE_PREFIX+" - "+super.getApplicationVersion());
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setLayout(new BorderLayout());
			
		extractMainPanel=new ExtractMainPanel(this);
		applicationInfoPanel=new ApplicationInfoPanel(this);
				
		cards = new JPanel(new CardLayout());
		frame.getContentPane().add(cards);
			
		cards.add(applicationInfoPanel, APPLICATION_INFO_PANEL);
		cards.add(extractMainPanel, EXECUTION_PANEL);
		
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

		if (FileUtils.getLogFile() != null) {
			output.add(FileUtils.getLogFile());
		}

		if (SwiftBuildFileUtils.getConsoleLogFile() != null) {
			output.add(SwiftBuildFileUtils.getConsoleLogFile());
		}		

		output.add(new File(UtilsConstants.ENGINE_PROPERTIES_FILE_NAME));
		output.add(new File(UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME));

		return output;
	}

	@Override
	public String getApplicationIconPath() {
		return getSharedApplicationIconPath();
	}

	public static String getSharedApplicationIconPath() {
		return "/images/extract/extract.png";
	}
	
	@Override
	protected String getRootFrameTitle() {
		String rootFrameTitle = UIConstants.FRAME_TITLE_PREFIX+" - " + this.getApplicationVersion();
		return rootFrameTitle;
	}
	
	public void moveToPanel(String panel) {
		CardLayout cl = (CardLayout)(cards.getLayout());
	    cl.show(cards, panel);
	}
	
    public static void main(String[] args) throws Exception {  
    	try
		{
			// this needs to be called before creating any components !!!
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("TabbedPane.selected", Color.decode("#047FC0"));
			UIManager.put("ProgressBar.foreground", Color.decode("#047FC0") );
			new ExtractMain();
			//BIPublisherManager biPublisherManager=new BIPublisherManager();
	    	//biPublisherManager.start();
		}
		catch (Throwable t)
		{
			onFatalErrorDuringApplicationInitialization(t);
		}
    }

	public ExtractMainPanel getExtractMainPanel() {
		return extractMainPanel;
	}

	public ApplicationInfoPanel getApplicationInfoPanel() {
		return applicationInfoPanel;
	}    	
    
}
