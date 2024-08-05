package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotViewerController;
import com.rapidesuite.snapshot.model.InitCreationGridSwingWorker;
import com.rapidesuite.snapshot.model.InitSnapshotInventoryManagementSwingWorker;

public class SnapshotInventoryManagementFrame extends SnapshotInventoryGridFrame {
	private SnapshotInventoryManagementGridPanel snapshotInventoryManagementGridPanel;
	public static final int FRAME_WIDTH=1260;
	public static final int FRAME_HEIGHT=760;

	public SnapshotInventoryManagementFrame(TabSnapshotsPanel tabSnapshotsPanel) {
		super(tabSnapshotsPanel);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		setTitle("Inventory Management");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		createComponents();
		runDefaultProcess();
	}
	
	public void closeWindow() {
		getTabSnapshotsPanel().getServerSelectionPanel().unlockPanel();
		getTabSnapshotsPanel().getSnapshotsActionsPanel().unlockPanel();
		getTabSnapshotsPanel().getSnapshotsGridPanel().unlockPanel();
		setVisible(false);
		dispose();
	}
	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		add(northPanel,BorderLayout.NORTH);

		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		add(centerPanel,BorderLayout.CENTER);
		this.setSnapshotInventoryGridActionPanel(new SnapshotInventoryManagementActionPanel(this));
		snapshotInventoryManagementGridPanel=new SnapshotInventoryManagementGridPanel(this,getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowBalloons());
		centerPanel.add(snapshotInventoryManagementGridPanel);

		JPanel southPanel=new JPanel();
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		add(southPanel,BorderLayout.SOUTH);
		southPanel.add(this.getSnapshotInventoryGridActionPanel());
	}

	@Override
	public void viewChanges(int viewRow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runDefaultProcess() {
		startInventoriesDisplayThread(0);
		
	}
	private void startInventoriesDisplayThread(final int delayInMs) {
		getTabSnapshotsPanel().getServerSelectionPanel().lockPanel();
		getTabSnapshotsPanel().getSnapshotsActionsPanel().lockPanel();
		getTabSnapshotsPanel().getSnapshotsGridPanel().lockPanel();
		new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
					displayInventories(delayInMs);
				} 
				catch (Exception e) {
					FileUtils.printStackTrace(e);
					GUIUtils.popupErrorMessage("Internal error: "+e.getMessage());
				}
			}
		}).start();
	}
	public void displayInventories(int delayInMs) throws Exception {
		InitSnapshotInventoryManagementSwingWorker swingWorker=new InitSnapshotInventoryManagementSwingWorker(this,delayInMs);
		final int width=450;
		final int height=150;
		swingWorker.setTotalSteps(2);
		UIUtils.displayOperationInProgressModalWindow(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),
				width,height,"Initialization...",swingWorker,SnapshotMain.getSharedApplicationIconPath());
	}

	public SnapshotInventoryManagementGridPanel getSnapshotInventoryManagementGridPanel() {
		return snapshotInventoryManagementGridPanel;
	}
	



}
