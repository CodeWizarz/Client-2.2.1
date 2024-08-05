package com.rapidesuite.inject.selenium;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.inject.gui.TabChangeListener;
import com.rapidesuite.inject.gui.TabbedPaneUI;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class SeleniumHub extends SeleniumGridCommon{

	public static final String FUSION_NODES_LISTING_FILE_NAME="injectNodesListing.txt";
	public static final String FUSION_NODES_KILL_SIGNAL_FILE_NAME="killNodeSignal.txt";
	
	public static final int FRAME_WIDTH=850;
	public static final int FRAME_HEIGHT=750;
	public static final String FRAME_TITLE="RapidHub";
	public static final int HEARTBEAT_REFRESH_TIME_IN_MS=2000;
	
	private InetAddress thisIp;
	private JTextField serverIdentifierTextField;
	private JLabel totalHostLabel;
	private JLabel totalNodesLabel;
	private JLabel totalNodesUpLabel;
	private JLabel totalNodesDownLabel;
	private JTabbedPane hostNameTabbedPane;
	private Map<String,SeleniumHubTable> hostnameUserToSeleniumHubTableList;
	private Map<String,List<SeleniumNodeInformation>> hostnameToNodeInformationMap;
	private Map<String,SeleniumNodeInformation> displayNameToNodeInformationMap;
	
	private final String LABEL_TOTAL_HOSTS="Total Hosts: ";
	private final String LABEL_TOTAL_NODES="Total Nodes: ";
	private final String LABEL_TOTAL_NODES_UP="Total Nodes Up: ";
	private final String LABEL_TOTAL_NODES_DOWN="Total Nodes Down: ";
	private File serverUILogFile;
	private File serverLogFolder;
	
	private final static String HUB_UI_LOG_FILE_NAME="serverUILog.txt";
	private final static String HUB_SELENIUM_PROCESS_OUTPUT_LOG_FILE_NAME="seleniumServerProcessOutputLog.txt";
	
	public SeleniumHub() throws Exception {
		File userHomeFolder=FileUtils.getUserHomeFolder();
		serverLogFolder=new File(userHomeFolder,CoreConstants.SHORT_APPLICATION_NAME.inject.toString());
		serverLogFolder.mkdirs();
		serverUILogFile=new File(serverLogFolder,HUB_UI_LOG_FILE_NAME);
		serverUILogFile.delete();
		
		hostnameUserToSeleniumHubTableList=new HashMap<String,SeleniumHubTable>();
		hostnameToNodeInformationMap=new HashMap<String,List<SeleniumNodeInformation>>();
		displayNameToNodeInformationMap=new HashMap<String,SeleniumNodeInformation>();
		createComponents();
	}

	public void createComponents() throws Exception{
		log(serverUILogFile,"SERVER: creating UI components...");
		setTitle(FRAME_TITLE);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLayout(new BorderLayout());
		
		thisIp =SeleniumUtils.getCurrentIp();		
		
		setIconImage(GUIUtils.getImageIcon(this.getClass(), InjectMain.getSharedApplicationIconPath()).getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener (new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				close();
			}
		});

		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.decode("#343836") );
		this.add(mainPanel);
		
		Preferences pref = Preferences.userRoot();
		ImageIcon ii=null;
		URL iconURL =null;

		JPanel executionPanel=InjectUtils.getYPanel(Component.LEFT_ALIGNMENT);
		executionPanel.setOpaque(true);
		executionPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);	
		executionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));			
		JPanel nodesGridPanel=InjectUtils.getYPanel(Component.LEFT_ALIGNMENT);
		//nodesGridPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		nodesGridPanel.setLayout(new BorderLayout());
		nodesGridPanel.setOpaque(true);
		nodesGridPanel.setBackground( ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);//.decode("#4B4F4E") );
		nodesGridPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));	
		
		
		iconURL = this.getClass().getResource("/images/inject/button_start_server.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startButton = new JButton();
		startButton.setIcon(ii);
		startButton.setBorderPainted(false);
		startButton.setContentAreaFilled(false);
		startButton.setFocusPainted(false);
		startButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_start_server_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startButton.setRolloverIcon(new RolloverIcon(ii));
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				startServer();
			}
		}
				);
		
		iconURL = this.getClass().getResource("/images/inject/button_stop_server.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		stopButton = new JButton();
		stopButton.setIcon(ii);
		stopButton.setBorderPainted(false);
		stopButton.setContentAreaFilled(false);
		stopButton.setFocusPainted(false);
		stopButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_stop_server_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		stopButton.setRolloverIcon(new RolloverIcon(ii));
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				stopServer();
			}
		}
				);
		JPanel tempPanelX=InjectUtils.getXPanel(Component.CENTER_ALIGNMENT);
		tempPanelX.setOpaque(false);
		
		executionPanel.add(tempPanelX);
		tempPanelX.add(startButton);
		//tempPanelX.add(Box.createRigidArea(new Dimension(10, 10)));
		tempPanelX.add(stopButton);
		stopButton.setEnabled(false);
				
		tempPanelX=InjectUtils.getXPanel(Component.CENTER_ALIGNMENT);
		tempPanelX.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));	
		tempPanelX.setOpaque(false);
		executionPanel.add(tempPanelX);
		JLabel label=new JLabel("Server ID ")
		{
	          public JToolTip createToolTip() {
	            JToolTip tip = super.createToolTip();
	            return InjectUtils.getCustomToolTip(tip);
	          }
	    };
	    label.setForeground(Color.decode("#cccccc"));
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		tempPanelX.add(label);
		serverIdentifierTextField = new JTextField()
		{
	          public JToolTip createToolTip() {
	            JToolTip tip = super.createToolTip();
	            return InjectUtils.getCustomToolTip(tip);
	          }
	    };
	    //serverIdentifierTextField.setForeground(Color.decode("#cccccc"));
		InjectUtils.assignArialPlainFont(serverIdentifierTextField,InjectMain.FONT_SIZE_NORMAL);
		String tooltip="Select then copy/ paste into RapidFusion";
		serverIdentifierTextField.setToolTipText(tooltip);
		InjectUtils.setSize(serverIdentifierTextField,250,25);
		serverIdentifierTextField.setEditable(false);
		serverIdentifierTextField.setBackground(Color.white);
		label.setToolTipText(tooltip);
		tempPanelX.add(serverIdentifierTextField);
		
		autoRefreshCheckBox=new JCheckBox("Auto-refresh");
		autoRefreshCheckBox.setBackground( ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
		autoRefreshCheckBox.setForeground(Color.decode("#cccccc"));
		InjectUtils.assignArialPlainFont(autoRefreshCheckBox,InjectMain.FONT_SIZE_NORMAL);
		boolean isRefresh= pref.getBoolean(DEFAULT_AUTOREFRESH,false);
		autoRefreshCheckBox.setSelected(isRefresh);
		tempPanelX.add(autoRefreshCheckBox);
		
		super.addLogTextArea(executionPanel,pref,false);
	
		int tabIndexGreaterThanNotDisplayWhiteBar=1;
		JTabbedPane jtp = new JTabbedPane();
		jtp.setUI(new TabbedPaneUI(tabIndexGreaterThanNotDisplayWhiteBar));
		
		mainPanel.add(jtp,BorderLayout.CENTER);
		jtp.setOpaque(false);
		int tabIndex=0;
		int tabWidth=80;
		int tabHeight=50;
		
		String tabName="GRID";
		JLabel labelTab = new JLabel(tabName, SwingConstants.CENTER);
		labelTab.setForeground(Color.decode("#FFFFFF") );
		labelTab.setOpaque(true);
		labelTab.setBackground(Color.decode("#343836"));
		labelTab.setFont( new Font( "Arial", Font.PLAIN, InjectMain.FONT_SIZE_BIG ) );
		labelTab.setPreferredSize(new Dimension(tabWidth, tabHeight));
		jtp.addTab(tabName,nodesGridPanel);
		jtp.setTabComponentAt(tabIndex, labelTab);
		jtp.setBackgroundAt(tabIndex,Color.decode("#343836"));
		tabIndex++;
				
		tabName="EXECUTION";
		labelTab = new JLabel(tabName, SwingConstants.CENTER);
		labelTab.setForeground(Color.decode("#FFFFFF") );
		labelTab.setOpaque(true);
		labelTab.setBackground(Color.decode("#343836"));
		labelTab.setFont( new Font( "Arial", Font.PLAIN, InjectMain.FONT_SIZE_BIG ) );
		labelTab.setPreferredSize(new Dimension(tabWidth, tabHeight));
		jtp.addTab(tabName,executionPanel);
		jtp.setTabComponentAt(tabIndex, labelTab);
		jtp.setBackgroundAt(tabIndex,Color.decode("#343836"));
		tabIndex++;
				
		jtp.addChangeListener(new TabChangeListener(jtp));
		jtp.setSelectedIndex(0);
		
		Container glassPane = (Container) this.getRootPane().getGlassPane();
		glassPane.setVisible(true);
		glassPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 15);
		gbc.anchor = GridBagConstraints.NORTHEAST;
		iconURL = jtp.getClass().getResource("/images/Logo_rapid.png");
		ImageIcon imageIconLogo=null;
		try{ imageIconLogo=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		int logoWidth=105;
		int logoHeight=48;
		JLabel logoLabel = new JLabel(imageIconLogo);
		logoLabel.setMinimumSize( new Dimension(logoWidth,logoHeight));
		logoLabel.setMaximumSize( new Dimension(logoWidth,logoHeight));
		logoLabel.setPreferredSize(new Dimension(logoWidth,logoHeight));
		logoLabel.setHorizontalAlignment(JLabel.RIGHT);
		glassPane.add(logoLabel, gbc); 
		
		
	    hostNameTabbedPane = new JTabbedPane();
	    tabIndexGreaterThanNotDisplayWhiteBar=100;
	    hostNameTabbedPane.setUI(new TabbedPaneUI(tabIndexGreaterThanNotDisplayWhiteBar));
	    hostNameTabbedPane.addChangeListener(new TabChangeListener(jtp));
	    
	    nodesGridPanel.add(hostNameTabbedPane,BorderLayout.CENTER);
	    
	    initNodesGrids();

	    JPanel northPanel=new JPanel(); 
		//actionsPanel.setBackground(Color.GREEN);
	    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
	    northPanel.setAlignmentX( Component.LEFT_ALIGNMENT);
	    northPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));	
	    northPanel.setOpaque(true);
	    northPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
	    nodesGridPanel.add(northPanel,BorderLayout.NORTH);
	    		
	    JPanel southPanel=new JPanel(); 
	    southPanel.setOpaque(true);
	    southPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
	    southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
	    southPanel.setAlignmentX( Component.LEFT_ALIGNMENT);
	    nodesGridPanel.add(southPanel,BorderLayout.SOUTH);
		
	    JPanel  tempPanel=InjectUtils.getYPanel(Component.LEFT_ALIGNMENT);
	    tempPanel.setOpaque(true);
	    tempPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
	    southPanel.add(tempPanel);
		label=new JLabel("Note: Grid refreshed every "+Utils.getExecutionTime(HEARTBEAT_REFRESH_TIME_IN_MS));
		label.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(label);
		String hostPort=thisIp.getHostName();
		JLabel link=InjectUtils.linkify(null,hostPort,"Click to open RAPIDMonitor","/observed.shtml", "click to view all the VNC sessions connected to the Controller.");
		link.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(link,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		tempPanel.add(link);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		
		totalHostLabel=new JLabel(LABEL_TOTAL_HOSTS);
		totalHostLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(totalHostLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(totalHostLabel);
		totalNodesLabel=new JLabel(LABEL_TOTAL_NODES);
		totalNodesLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(totalNodesLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(totalNodesLabel);
		totalNodesUpLabel=new JLabel(LABEL_TOTAL_NODES_UP);
		totalNodesUpLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(totalNodesUpLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(totalNodesUpLabel);
		totalNodesDownLabel=new JLabel(LABEL_TOTAL_NODES_DOWN);
		totalNodesDownLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(totalNodesDownLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(totalNodesDownLabel);
				
		
		iconURL = this.getClass().getResource("/images/inject/button_start_nodes.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton startNodesButton = new JButton();
		startNodesButton.setIcon(ii);
		startNodesButton.setBorderPainted(false);
		startNodesButton.setContentAreaFilled(false);
		startNodesButton.setFocusPainted(false);
		startNodesButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_start_nodes_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startNodesButton.setRolloverIcon(new RolloverIcon(ii));
		ActionListener onStartButtonIsClicked = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				startNodes();
			}
		};
		startNodesButton.addActionListener(onStartButtonIsClicked);
	
		
		iconURL = this.getClass().getResource("/images/inject/button_stop_nodes.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton stopNodesButton = new JButton();
		stopNodesButton.setIcon(ii);
		stopNodesButton.setBorderPainted(false);
		stopNodesButton.setContentAreaFilled(false);
		stopNodesButton.setFocusPainted(false);
		stopNodesButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_stop_nodes_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		stopNodesButton.setRolloverIcon(new RolloverIcon(ii));
		stopNodesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				stopNodes();
			}
		}
		);
		
		tempPanel=InjectUtils.getXPanel(Component.CENTER_ALIGNMENT);
		tempPanel.setOpaque(true);
		tempPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
		northPanel.add(tempPanel);
		tempPanel.add(startNodesButton);
		//tempPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		tempPanel.add(stopNodesButton);
				
		setVisible(true);
		log(serverUILogFile,"SERVER: UI components created.");
		startServer();
		refreshNodes();
	}
	
	protected void startNodes() {
		 processNodesThread(true);
	}
	
	protected void stopNodes() {
		 processNodesThread(false);
	}

	protected void processNodesThread(final boolean isStartNode) {
		NodeDialog swingWorker=new NodeDialog(this,isStartNode);
		final int width=450;
		final int height=150;
		UIUtils.displayOperationInProgressModalWindow(this,width,height,"Operation in Progress...",swingWorker,false,InjectMain.getSharedApplicationIconPath());
	}

	public void close() {
		stopServer();
		System.exit(0);
	}

	protected void stopServer() {
		try {
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
						
			if (process!=null) {
				process.destroy();
				process=null;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			log(serverUILogFile,e);
			GUIUtils.popupErrorMessage("error: "+e.getMessage());
		}
	}

	protected void startServer() {
		try{	
			log(serverUILogFile,"SERVER: starting selenium proces...");
			String portStr="4444";//InjectUtils.getAvailableRandomPortNumber();
			String serverId=thisIp.getHostName()+":"+portStr;
			serverIdentifierTextField.setText(serverId);

			startButton.setEnabled(false);
			stopButton.setEnabled(true);
			
			Preferences pref = Preferences.userRoot();
			pref.putBoolean(DEFAULT_AUTOREFRESH,autoRefreshCheckBox.isSelected());
						
			List<String> arguments=new ArrayList<String>();
			String javaPath=Config.getInjectNodeJavaPath();
			// http://code.google.com/p/selenium/wiki/Grid2#Configuring_timeouts_%28Version_2.21_required%29
			arguments.add(javaPath);
			
			//arguments.add("-jar");
			//arguments.add("lib/selenium-server-standalone-2.46.0.jar");
			// http://www.seleniumhq.org/docs/07_selenium_grid.jsp
			arguments.add("-Xmx1024m");
			arguments.add("-DsysProp1="+serverId);
			arguments.add("-DPOOL_MAX=1024");
			arguments.add("-cp");
			arguments.add("rapidclient.jar"+File.pathSeparatorChar+
					"lib/selenium-server-standalone-2.46.0.jar"+File.pathSeparatorChar+
					"lib/org.json-20120521.jar"+File.pathSeparatorChar+
					"lib/sshj-0.9.1-SNAPSHOT.RES01.jar"+File.pathSeparatorChar+
					"lib/slf4j-api-1.7.6.jar"+File.pathSeparatorChar+
					"lib/log4j-1.2.17.jar"+File.pathSeparatorChar+
					"lib/rapidcore.jar"+File.pathSeparatorChar+
					"lib/spring-core-3.2.3.RELEASE.jar"+File.pathSeparatorChar+
					"lib/slf4j-log4j12-1.7.6.jar"
					);
			arguments.add("org.openqa.grid.selenium.GridLauncher");			
			arguments.add("-role");
			arguments.add("hub");
			arguments.add("-port");
			arguments.add(portStr);
			arguments.add("-servlets");
			arguments.add("com.rapidesuite.inject.selenium.SeleniumHubServlet");
			arguments.add("-timeout");
			arguments.add("0");
			arguments.add("-browserTimeout");
			arguments.add("0");
			arguments.add("-unregisterIfStillDownAfter");
			arguments.add("2000");
			arguments.add("-nodePolling");
			arguments.add("1000");
			arguments.add("-downPollingLimit");
			arguments.add("2");
			
			log(serverUILogFile,"SERVER: arguments: "+arguments);
			
			startProcessBuilder(arguments);
			log(serverUILogFile,"SERVER: started");
		}
		catch (Exception e) {
			e.printStackTrace();
			log(serverUILogFile,e);
			GUIUtils.popupErrorMessage("error: "+e.getMessage());
		}
	}	

	protected void startProcessBuilder(List<String> arguments) throws IOException {		
		String[] argumentsArray = arguments.toArray(new String[arguments.size()]);
		
		ProcessBuilder pb = new ProcessBuilder(argumentsArray);
		pb.directory(new File("."));
		
		File serverSeleniumProcessOutputLogFile=new File(serverLogFolder,HUB_SELENIUM_PROCESS_OUTPUT_LOG_FILE_NAME);
		serverSeleniumProcessOutputLogFile.delete();
		serverSeleniumProcessOutputLogFile.createNewFile();
		
		pb.redirectErrorStream(true);
		pb.redirectOutput(serverSeleniumProcessOutputLogFile);
		
		process = pb.start();
		updateLogTextArea(serverSeleniumProcessOutputLogFile);
	}

	public static void main(String[] args) {
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("TabbedPane.selected", Color.decode("#047FC0"));
			
			FileUtils.init(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.inject,true);
			new SeleniumHub();
		}
		catch (Throwable t)
		{
			FileUtils.printStackTrace(t);
			GUIUtils.popupErrorMessage(CoreUtil.getAllThrowableMessagesHTML(t));	
		}
	}
	
	public static List<SeleniumNodeInformation> readNodesListingFile() throws Exception {
		File hostPropertiesFile=new File(FUSION_NODES_LISTING_FILE_NAME);
		List<String> lines=DataExtractionFileUtils.readContentsFromBuildInfoFile(hostPropertiesFile);
		List<SeleniumNodeInformation> toReturn=new ArrayList<SeleniumNodeInformation>();
		for (String line:lines) {
			if ( line.trim().length() <= 0 || line.trim().startsWith("#") )	{
				continue;
			}

			StringTokenizer st = new StringTokenizer(line, "=");
			String displayName = st.nextToken();

			String valueLine = st.nextToken();
			st = new StringTokenizer(valueLine, ",");

			String hostName = st.nextToken();
			String vncPortStr = st.nextToken();
			Integer vncPort = new Integer(vncPortStr);
			String vncPassword = st.nextToken();
			String websockifyPortStr = st.nextToken();
			Integer websockifyPort = new Integer(websockifyPortStr);
			String sshUser = st.nextToken();
			String sshPassword = st.nextToken();

			SeleniumNodeInformation seleniumNodeInformation=new SeleniumNodeInformation(displayName,hostName,vncPort,
					vncPassword,websockifyPort,sshUser,sshPassword);
			toReturn.add(seleniumNodeInformation);
		}
		return toReturn;
	}

	private void initNodesGrids() {
		try {
			List<SeleniumNodeInformation> nodesListingFile=readNodesListingFile();
			int tabIndex=0;
			int tabWidth=200;
			int tabHeight=30;
			for (SeleniumNodeInformation seleniumNodeInformation:nodesListingFile) {
				String hostName=seleniumNodeInformation.getHostName();
	    		displayNameToNodeInformationMap.put(seleniumNodeInformation.getDisplayName(), seleniumNodeInformation);
	    		
				List<SeleniumNodeInformation> nodeInformationList=hostnameToNodeInformationMap.get(hostName);
				if (nodeInformationList==null) {
					nodeInformationList=new ArrayList<SeleniumNodeInformation>();
					hostnameToNodeInformationMap.put(hostName, nodeInformationList);
				}
				nodeInformationList.add(seleniumNodeInformation);
				
				SeleniumHubTable seleniumHubTable=hostnameUserToSeleniumHubTableList.get(hostName);
				if (seleniumHubTable==null) {
					seleniumHubTable=new SeleniumHubTable(this);
					hostnameUserToSeleniumHubTableList.put(hostName,seleniumHubTable);
								
					String tabName=hostName;
					JLabel labelTab = new JLabel(tabName, SwingConstants.CENTER);
					labelTab.setForeground(Color.decode("#FFFFFF") );
					labelTab.setOpaque(true);
					hostNameTabbedPane.addTab(tabName,seleniumHubTable);
					hostNameTabbedPane.setTabComponentAt(tabIndex, labelTab);
					if (tabIndex==0) {
						labelTab.setBackground(Color.decode("#047FC0"));
						hostNameTabbedPane.setBackgroundAt(tabIndex,Color.decode("#047FC0"));
					}
					else {
						labelTab.setBackground(Color.decode("#343836"));
						hostNameTabbedPane.setBackgroundAt(tabIndex,Color.decode("#343836"));
					}
					labelTab.setFont( new Font( "Arial", Font.PLAIN, InjectMain.FONT_SIZE_BIG ) );
					labelTab.setPreferredSize(new Dimension(tabWidth, tabHeight));
					tabIndex++;			
				}				
			}	
		    hostNameTabbedPane.setSelectedIndex(0);

			Iterator<String> iterator=hostnameUserToSeleniumHubTableList.keySet().iterator();
			while (iterator.hasNext()) {
				String hostName=iterator.next();
				SeleniumHubTable seleniumHubTable=hostnameUserToSeleniumHubTableList.get(hostName);
				List<SeleniumNodeInformation>  nodeInformationList=hostnameToNodeInformationMap.get(hostName);
				seleniumHubTable.loadNodes(nodeInformationList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			log(serverUILogFile,e);
			GUIUtils.popupErrorMessage("Unable to read the hostnames file. Error: "+e.getMessage());
		}
	}
	
	protected void refreshNodes() {
		// wait a bit that the server starts otherwise connection errors will be thrown.
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			FileUtils.printStackTrace(e);
		}
		
		new Thread( new Runnable() {
		    @Override
		    public void run() {
		    
		    	while (true) {
		    		try {
		    			totalHostLabel.setText(LABEL_TOTAL_HOSTS+hostnameUserToSeleniumHubTableList.size());
		    			int totalNodes=0;
		    			int totalNodesUp=0;
		    			int totalNodesDown=0;

		    			NodesInfo nodesInfo = HubRequestsManager.getNodesInfo(getServerIdentifier());
		    			if (nodesInfo!=null) {
		    				Set<String> nodes=new TreeSet<String>();
		    				nodes.addAll(nodesInfo.getBusyNodesList());
		    				nodes.addAll(nodesInfo.getFreeNodesList());

		    				Iterator<String> iterator=hostnameUserToSeleniumHubTableList.keySet().iterator();
		    				while (iterator.hasNext()) {
		    					String key=iterator.next();
		    					//System.out.println("key:"+key);
		    					SeleniumHubTable seleniumHubTable=hostnameUserToSeleniumHubTableList.get(key);

		    					seleniumHubTable.refreshNodes(nodes,nodesInfo.getBusyNodesList());

		    					totalNodes=totalNodes+seleniumHubTable.getTotalNodes();
		    					totalNodesUp=totalNodesUp+seleniumHubTable.getTotalNodes(SeleniumHubTable.STATUS_UP);
		    					totalNodesDown=totalNodesDown+seleniumHubTable.getTotalNodes(SeleniumHubTable.STATUS_DOWN);
		    				}
		    				totalNodesLabel.setText(LABEL_TOTAL_NODES+totalNodes);
		    				totalNodesUpLabel.setText(LABEL_TOTAL_NODES_UP+totalNodesUp);
		    				totalNodesDownLabel.setText(LABEL_TOTAL_NODES_DOWN+totalNodesDown);
		    			}
		    			Thread.sleep(SeleniumHub.HEARTBEAT_REFRESH_TIME_IN_MS);
		    		}
		    		catch (Throwable e) {
		    			e.printStackTrace();
		    			FileUtils.printStackTrace(e);
		    		}
		    	}
		    }
		}).start();
	}

	public String getServerIdentifier() {
		return serverIdentifierTextField.getText();
	}

	public Map<String, List<SeleniumNodeInformation>> getHostnameToNodeInformationMap() {
		return hostnameToNodeInformationMap;
	}

	public Map<String, SeleniumNodeInformation> getDisplayNameToNodeInformationMap() {
		return displayNameToNodeInformationMap;
	}

	public File getServerUILogFile() {
		return serverUILogFile;
	}

	public Map<String, SeleniumHubTable> getHostnameUserToSeleniumHubTableList() {
		return hostnameUserToSeleniumHubTableList;
	}

}
