package com.rapidesuite.snapshot.view.upgrade;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.view.SnapshotCreationActionsPanel;
import com.rapidesuite.snapshot.view.SnapshotCreationGenericFrame;
import com.rapidesuite.snapshot.view.SnapshotCreationInformationPanel;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotViewerFrame;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;

@SuppressWarnings("serial")
public class UpgradeFrame extends SnapshotCreationGenericFrame{

	public static final int FRAME_WIDTH=1680;
	public static final int FRAME_HEIGHT=760;
	private Map<Integer, String> oracleSeededUserIdToUserNameMap;
	private TabUpgradeMainPanel tabUpgradeMainPanel;
	private SnapshotGridRecord snapshotGridRecord;
	
	public static void main(String[] args) throws Exception
	{
		try{
			String applicationName="Upgrade";
			File applicationFolder=new File(FileUtils.getUserHomeFolder(),applicationName);
			File logFolder=new File(applicationFolder,"logs");
			FileUtils.deleteDirectory(logFolder);
			logFolder.mkdirs();
			File logFile=new File(logFolder,applicationName + "-" + SwiftGUIMain.getStartTime() + "-" + 
					FileUtils.LOG_FILE_NAME_PREFIX + FileUtils.LOG_FILE_EXTENSION);
			FileUtils.createLogFile(logFile);

			// this needs to be called before creating any components !!!
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("TabbedPane.selected", Color.decode("#047FC0"));
			UIManager.put("ProgressBar.foreground", Color.decode("#047FC0"));

			UpgradeFrame upgradeFrame=new UpgradeFrame(null,null);		
			upgradeFrame.setVisible(true);
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
	
	public UpgradeFrame(TabSnapshotsPanel tabSnapshotsPanel,SnapshotGridRecord snapshotGridRecord) throws Exception {
		super(tabSnapshotsPanel);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		setTitle("RAPIDUpgrade");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.snapshotGridRecord=snapshotGridRecord;
		if (tabSnapshotsPanel!=null) {
			this.oracleSeededUserIdToUserNameMap=tabSnapshotsPanel.getMainPanel().
				getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap();
		}
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		createComponents();
		this.setSnapshotInventoryGridActionPanel(new SnapshotCreationActionsPanel(this));
	}
	
	public void closeWindow() {
		setVisible(false);
		dispose();
	}
	
	public void createComponents() throws Exception{		
		final JTabbedPane jtp = new JTabbedPane();
		jtp.setOpaque(true);
		jtp.setBackground(Color.decode("#343836"));
		tabUpgradeMainPanel=new TabUpgradeMainPanel(this);
		add(tabUpgradeMainPanel,BorderLayout.CENTER);
	}
	
	public TabUpgradeMainPanel getTabUpgradeMainPanel() {
		return tabUpgradeMainPanel;
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	@Override
	public void viewChanges(int viewRow) {
		SnapshotViewerFrame.viewChangesLogic(viewRow,snapshotInventoryDetailsGridPanel,tabSnapshotsPanel,
				snapshotGridRecord,true);
	}

	@Override
	public void runDefaultProcess() {
	}

	public SnapshotGridRecord getSnapshotGridRecord() {
		return snapshotGridRecord;
	}
	
	public void setSnapshotGridRecord(SnapshotGridRecord snapshotGridRecord) {
		this.snapshotGridRecord=snapshotGridRecord;
	}

	@Override
	public void processAction(SnapshotCreationInformationPanel snapshotCreationInformationPanel) {
		String snapshotName= snapshotCreationInformationPanel.getSnapshotNameTextField().getText();
		if (snapshotName==null || snapshotName.isEmpty()) {
			GUIUtils.popupErrorMessage("You must enter a snapshot name!");
			return;
		}
		String snapshotDescription=snapshotCreationInformationPanel.getSnapshotDescriptionTextArea().getText();
		snapshotCreationInformationPanel.getDialog().dispose();
		tabUpgradeMainPanel.processActionTakeSnapshot(snapshotName,snapshotDescription);
	}

	public File getRootDownloadFolder() {
		File rootDownloadFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getDownloadFolder();		
		return rootDownloadFolder;
	}

	public Map<Integer, String> getOracleSeededUserIdToUserNameMap() {
		return oracleSeededUserIdToUserNameMap;
	}
	
}
