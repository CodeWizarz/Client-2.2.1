package com.rapidesuite.inject.selenium;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;

@SuppressWarnings("serial")
public class SeleniumNode extends SeleniumGridCommon{

	public static final int FRAME_WIDTH=700;
	public static final int FRAME_HEIGHT=500;
	public static final String FRAME_TITLE="RapidNode";

	private String userName;
	private JLabel portlabel;
	private String serverIdentifier;
	private String nodeIdentifier;
	private String webSockifyPort;
	private File nodeUILogFile;
	private File nodesLogFolder;
	
	private final static String NODE_UI_LOG_FILE_NAME="nodeUILog.txt";
	public final static String NODE_KILL_RELATIVE_FOLDER="nodesToKill";
	
	public SeleniumNode(String serverIdentifier,String nodeIdentifier,String webSockifyPort,String userName)	throws Exception {
		this.userName=userName;
		this.nodeIdentifier=nodeIdentifier;
		this.serverIdentifier=serverIdentifier;
		this.webSockifyPort=webSockifyPort;
		
		File userHomeFolder=FileUtils.getUserHomeFolder();
		nodesLogFolder=new File(userHomeFolder,CoreConstants.SHORT_APPLICATION_NAME.inject.toString());
		nodesLogFolder.mkdirs();
		nodeUILogFile=new File(nodesLogFolder,NODE_UI_LOG_FILE_NAME);
		nodeUILogFile.delete();
		nodeUILogFile.createNewFile();
		
		createComponents();
	}

	public void createComponents() throws Exception{
		log(nodeUILogFile,"Node: GUI components creating...");
		
		setTitle(FRAME_TITLE+" - ID: '"+nodeIdentifier+"'");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLayout(new BorderLayout());
		setIconImage(GUIUtils.getImageIcon(this.getClass(), InjectMain.getSharedApplicationIconPath()).getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener (new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				close();
			}
		});

		Preferences pref = Preferences.userRoot();
	
		JPanel tempPanel=InjectUtils.getYPanel(Component.CENTER_ALIGNMENT);
		tempPanel.setOpaque(true);
		tempPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);	
		tempPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		this.add(tempPanel);

		JPanel tempPanelX=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanelX.setOpaque(true);
		tempPanelX.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);	
		tempPanel.add(tempPanelX);
		JLabel label=new JLabel("Server identifier");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#cccccc"));
		tempPanelX.add(label);
		tempPanelX.add(Box.createRigidArea(new Dimension(20, 10)));
		label = new JLabel();
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#cccccc"));
		tempPanelX.add(label);
				
		label.setText(serverIdentifier);
			
		tempPanelX=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanelX.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
		tempPanelX.setOpaque(true);
		tempPanelX.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);	
		tempPanel.add(tempPanelX);
		portlabel=new JLabel("");
		InjectUtils.assignArialPlainFont(portlabel,InjectMain.FONT_SIZE_NORMAL);
		portlabel.setForeground(Color.decode("#cccccc"));
		tempPanelX.add(portlabel);
		
		autoRefreshCheckBox=new JCheckBox("Auto-refresh");
		autoRefreshCheckBox.setBackground( ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
		autoRefreshCheckBox.setForeground(Color.decode("#cccccc"));
		InjectUtils.assignArialPlainFont(autoRefreshCheckBox,InjectMain.FONT_SIZE_NORMAL);
		boolean isRefresh= pref.getBoolean(DEFAULT_AUTOREFRESH,false);
		autoRefreshCheckBox.setSelected(isRefresh);
		tempPanelX.add(autoRefreshCheckBox);
					
		super.addLogTextArea(tempPanel,pref,false);
		setVisible(true);
		log(nodeUILogFile,"Node: GUI components created.");
		pollKillNodeFile();
		startNode();
	}

	public void close() {
		stopNode();
		System.exit(0);
	}

	protected void stopNode() {
		try {
			portlabel.setText("");			
			if (process!=null) {
				process.destroy();
				process=null;
			}
			killOrphansFirefox();
		} 
		catch (Exception e) {
			e.printStackTrace();
			log(nodeUILogFile,e);
		}
	}

	private void killOrphansFirefox()  {
		try {
			List<String> arguments=new ArrayList<String>();
			arguments.add("pkill");
			arguments.add("-f");
			arguments.add("firefox");			
			arguments.add("-u");
			arguments.add(userName);

			String[] argumentsArray = arguments.toArray(new String[arguments.size()]);
			ProcessBuilder pb = new ProcessBuilder(argumentsArray);
			pb.directory(new File("."));
			pb.start();
		} 
		catch (Exception e) {
			e.printStackTrace();
			log(nodeUILogFile,e);
		}
	}
	
	protected void startNode() {
		killOrphansFirefox();
		int MAX_RETRIES=10;
		int index=0;
		while ( index < MAX_RETRIES) {
			index++;
			try{
				log(nodeUILogFile,"Node: startNode process starting...");
				String portStr=SeleniumUtils.getAvailableRandomPortNumber();
				//String portStr="1";
				portlabel.setText("Node Port number: "+portStr);
				log(nodeUILogFile,"Node: Starting node on port: '"+portStr+"'");
				List<String> arguments=getNodeArguments(portStr);
				log(nodeUILogFile,"Node: Starting node with arguments: '"+arguments+"'");
				startProcessBuilder(arguments);
				break;
			} 
			catch (Exception e) {
				e.printStackTrace();
				log(nodeUILogFile,e);
				
				String retriesMsg="RETRIES COUNT: "+index+" / "+MAX_RETRIES;
				textArea.setBackground(Color.decode("#E85129"));
				textArea.setText(
						"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"+
								"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"+
								"INTERNAL ERROR!\n"+
								retriesMsg+"\n"+
								"Error: "+e.getMessage()+"\n"+
								"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"+
								"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
						);
				
				try {
					log(nodeUILogFile,retriesMsg);
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	protected List<String> getNodeArguments(String portStr) {
		List<String> arguments=new ArrayList<String>();
		String value=null;
		StringBuffer temp=new StringBuffer("");

		value="-role";
		temp.append(value).append(" ");
		arguments.add(value);

		value="wd";
		temp.append(value).append(" ");
		arguments.add(value);

		value="-port";
		temp.append(value).append(" ");
		arguments.add(value);

		value=portStr;
		temp.append(value).append(" ");
		arguments.add(value);

		value="-hub";
		temp.append(value).append(" ");
		arguments.add(value);

		value="http://"+serverIdentifier+"/grid/register";
		temp.append(value).append(" ");
		arguments.add(value);

		value="-proxy";
		temp.append(value).append(" ");
		arguments.add(value);

		value="com.rapidesuite.inject.selenium.SeleniumNodeProxy";
		temp.append(value).append(" ");
		arguments.add(value);

		value="-browser";
		temp.append(value).append(" ");
		arguments.add(value);

		String firefoxPath=Config.getInjectFirefoxPath();
		String firefoxParameters="browserName=firefox,maxInstances=1";
		if (firefoxPath!=null) {
			firefoxParameters=firefoxParameters+",firefox_binary="+firefoxPath;
		}
		value=firefoxParameters;
		temp.append(value).append(" ");
		arguments.add(value);

		value="-id";
		temp.append(value).append(" ");
		arguments.add(value);

		value=nodeIdentifier;
		temp.append(value).append(" ");
		arguments.add(value);

		value="-websockifyPort";
		temp.append(value).append(" ");
		arguments.add(value);

		value=webSockifyPort;
		temp.append(value);
		arguments.add(value);

		log(nodeUILogFile,"Node: full command: '"+temp.toString()+"'");

		return arguments;
	}

	protected void startProcessBuilder(List<String> arguments) throws Exception {
		String[] argumentsArray = arguments.toArray(new String[arguments.size()]);
		textArea.setText("Node starting...");

		RegistrationRequest c = RegistrationRequest.build(argumentsArray);
		SelfRegisteringRemote remote = new SelfRegisteringRemote(c);
		remote.startRemoteServer();
		remote.startRegistrationProcess();
		textArea.setBackground(Color.decode("#59A203"));
		textArea.setText("Node started and ready.");

	}
	
	private void pollKillNodeFile() {
		final File currentFolder=new File(NODE_KILL_RELATIVE_FOLDER,userName);
		Thread t = new Thread() {
			public void run() {
				try{
					while (true) {
						File killFile=new File(currentFolder,SeleniumHub.FUSION_NODES_KILL_SIGNAL_FILE_NAME);
						if (killFile.exists()) {
							boolean isDeleted=killFile.delete();
							log(nodeUILogFile,"Killing node! File deleted: "+isDeleted);
							close();
						}
						Thread.sleep(2000);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					log(nodeUILogFile,e);
				}
			}
		};
		t.start();
		log(nodeUILogFile,"Node: pollKillNodeFile process started.");
	}
	
	public static void main(String[] args) {
		try
		{
			String userName="Myuser";
			String serverIdentifier="MyServer";
			String nodeIdentifier="MyNode";
			String webSockifyPort="7777";
			
			if (args!=null && args.length>0) {
				userName=args[3];
				serverIdentifier=args[0];
				nodeIdentifier=args[1];
				webSockifyPort=args[2];
			}
				
			FileUtils.init(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.inject,true);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new SeleniumNode(serverIdentifier,nodeIdentifier,webSockifyPort,userName);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			GUIUtils.popupErrorMessage(CoreUtil.getAllThrowableMessagesHTML(t));	
		}
	}

}
