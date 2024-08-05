package com.rapidesuite.snapshot.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.rapidesuite.client.common.gui.ZipPackageFileFilter;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotDownloadController;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotExportSwingWorker;
import com.rapidesuite.snapshot.model.SnapshotImportSwingWorker;
import com.rapidesuite.snapshot.view.upgrade.UpgradeFrame;

@SuppressWarnings("serial")
public class SnapshotsActionsPanel extends JPanel{

	private TabSnapshotsPanel tabSnapshotsPanel;
	private JButton snapshotButton;
	private JButton comparisonButton;
	private JButton deleteButton;
	private JButton upgradeButton;
	private JButton importButton;
	private JButton exportButton;
	
	public static final String EXTRA_RSC_TABLES_FOLDER_NAME="upgrade-extra-inventories";
	
	public SnapshotsActionsPanel(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		setOpaque(false);
		createComponents();
	}

	private void createComponents() {
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_take.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		snapshotButton = new JButton();
		snapshotButton.setIcon(ii);
		snapshotButton.setBorderPainted(false);
		snapshotButton.setContentAreaFilled(false);
		snapshotButton.setFocusPainted(false);
		snapshotButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_take_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		snapshotButton.setRolloverIcon(new RolloverIcon(ii));
		snapshotButton.setEnabled(false);
		snapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionNewSnapshot();
			}
		}
				);
		add(snapshotButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshots_comparison.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		comparisonButton = new JButton();
		comparisonButton.setIcon(ii);
		comparisonButton.setBorderPainted(false);
		comparisonButton.setContentAreaFilled(false);
		comparisonButton.setFocusPainted(false);
		comparisonButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshots_comparison_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		comparisonButton.setRolloverIcon(new RolloverIcon(ii));
		comparisonButton.setEnabled(false);
		comparisonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					processActionSnapshotsComparison();
				}
				catch(Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),ex.getMessage());
				}
			}
		}
				);
		add(comparisonButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_delete_snapshot.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		deleteButton = new JButton();
		deleteButton.setEnabled(false);
		deleteButton.setIcon(ii);
		deleteButton.setBorderPainted(false);
		deleteButton.setContentAreaFilled(false);
		deleteButton.setFocusPainted(false);
		deleteButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_delete_snapshot_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		deleteButton.setRolloverIcon(new RolloverIcon(ii));
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					processActionDeleteSnapshots();
				}
				catch (Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+ex.getMessage());
				}
			}
		}
				);
		add(deleteButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_launch_upgrade.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		upgradeButton = new JButton();
		upgradeButton.setIcon(ii);
		upgradeButton.setVisible(false);
		upgradeButton.setBorderPainted(false);
		upgradeButton.setContentAreaFilled(false);
		upgradeButton.setFocusPainted(false);
		upgradeButton.setRolloverEnabled(true);
		upgradeButton.setEnabled(false);
		iconURL = this.getClass().getResource("/images/snapshot/button_launch_upgrade_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		upgradeButton.setRolloverIcon(new RolloverIcon(ii));
		upgradeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					processActionLaunchUpgrade(null);
				}
				catch (Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+ex.getMessage());
				}
			}
		}
				);
		add(upgradeButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		Set<String> activatedPlugins=ModelUtils.getActivatedPlugins();
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
			upgradeButton.setVisible(true);
		}
		
		iconURL = this.getClass().getResource("/images/snapshot/button_import_snapshot.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		importButton = new JButton();
		importButton.setIcon(ii);
		importButton.setBorderPainted(false);
		importButton.setContentAreaFilled(false);
		importButton.setFocusPainted(false);
		importButton.setRolloverEnabled(true);
		importButton.setEnabled(false);
		iconURL = this.getClass().getResource("/images/snapshot/button_import_snapshot_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		importButton.setRolloverIcon(new RolloverIcon(ii));
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					processActionImport();
				}
				catch (Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+ex.getMessage());
				}
			}
		}
				);
		add(importButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
			importButton.setVisible(true);
		}
		
		iconURL = this.getClass().getResource("/images/snapshot/button_export_snapshot.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		exportButton = new JButton();
		exportButton.setIcon(ii);
		exportButton.setBorderPainted(false);
		exportButton.setContentAreaFilled(false);
		exportButton.setFocusPainted(false);
		exportButton.setEnabled(false);
		exportButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_export_snapshot_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		exportButton.setRolloverIcon(new RolloverIcon(ii));
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					processActionExport();
				}
				catch (Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+ex.getMessage());
				}
			}
		}
				);
		add(exportButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
			exportButton.setVisible(true);
		}
	}

	protected void processActionExport() {
		try{
			List<SnapshotGridRecord> selectedSnapshotGridRecordsList=tabSnapshotsPanel.getSnapshotsGridPanel().getSelectedSnapshotGridRecordsInDateOrderList();
			if (selectedSnapshotGridRecordsList.isEmpty() || selectedSnapshotGridRecordsList.size()>1) {
				throw new Exception("You must select one snapshot to export!");
			}
			SnapshotGridRecord snapshotGridRecord=selectedSnapshotGridRecordsList.get(0);
			if (!ModelUtils.getUIStatusFromDBStatus(snapshotGridRecord.getStatus()).equals(UIConstants.UI_STATUS_COMPLETED)) {
				GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),
						"Invalid Snapshot status, only successful snapshots can be exported!");
				return;
			}
			
			boolean isDownloadFromViewer=true;
			boolean isComparisonReportGeneration=false;
			boolean isBR100ReportGeneration=false;
			boolean isDownloadDataChangesOnly=false;
			boolean isExport=true;
			
			File downloadFolder=SnapshotDownloadController.getDownloadFolder(
					tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getDownloadFolder(),
					isComparisonReportGeneration,isBR100ReportGeneration,isExport);
			
			SnapshotExportSwingWorker swingWorker=new SnapshotExportSwingWorker(tabSnapshotsPanel,snapshotGridRecord,downloadFolder);
			final int width=450;
			final int height=150;
			UIUtils.displayOperationInProgressModalWindow(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),
					width,height,"Initializing...",swingWorker,SnapshotMain.getSharedApplicationIconPath());
			
			List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList=swingWorker.getSnapshotInventoryGridRecordList();
			if (snapshotInventoryGridRecordList==null) {
				return;
			}
			
			SnapshotDownloadController snapshotDownloadController=new SnapshotDownloadController(
					downloadFolder,
					tabSnapshotsPanel,snapshotInventoryGridRecordList,
					snapshotGridRecord,isDownloadFromViewer,isComparisonReportGeneration,isBR100ReportGeneration,isDownloadDataChangesOnly,
					isExport);
			snapshotDownloadController.startExecution();
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+e.getMessage());
		}
	}

	protected void processActionImport() {
		try{
			JFileChooser fileChooser= new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			File applicationFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
			File defaultDownloadFolder=new File(applicationFolder,TabOptionsPanel.DEFAULT_DOWNLOAD_FOLDER_NAME);
			
			fileChooser.setCurrentDirectory(defaultDownloadFolder);
			fileChooser.setFileFilter(new ZipPackageFileFilter());
			fileChooser.setDialogTitle("Choose a File");
			int returnVal = fileChooser.showOpenDialog(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file=fileChooser.getSelectedFile();
				SnapshotImportSwingWorker swingWorker=new SnapshotImportSwingWorker(tabSnapshotsPanel,file);
				final int width=450;
				final int height=150;
				UIUtils.displayOperationInProgressModalWindow(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),
						width,height,"Initializing...",swingWorker,SnapshotMain.getSharedApplicationIconPath());
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+e.getMessage());
		}
	}

	public void createExtraRSCTablesFolderPath() {
		File extraRSCTablesFolder=getExtraRSCTablesFolder();
		extraRSCTablesFolder.mkdirs();
	}
	
	public static File getExtraRSCTablesFolder() {
		File applicationFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
		File extraRSCTablesFolder=new File(applicationFolder,EXTRA_RSC_TABLES_FOLDER_NAME);
		return extraRSCTablesFolder;
	}
		
	private void validateExtraRSCTablesFolderNotEmpty() throws Exception {
		File extraRSCTablesFolder=getExtraRSCTablesFolder();
		File[] files=extraRSCTablesFolder.listFiles();
		if (files==null || files.length==0) {
			throw new Exception("Missing extra inventories in the folder: '"+extraRSCTablesFolder.getAbsolutePath()+"'.\n"+
					"Those inventories are required in order to use RAPIDUpgrade. Please copy your inventory files and try again.");
		}
	}

	public void processActionLaunchUpgrade(SnapshotGridRecord snapshotGridRecord) {
		try {
			createExtraRSCTablesFolderPath();
			validateExtraRSCTablesFolderNotEmpty();
			
			UpgradeFrame upgradeFrame = new UpgradeFrame(tabSnapshotsPanel,snapshotGridRecord);
			upgradeFrame.setVisible(true);
		}
		catch (Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+ex.getMessage());
		}		
	}

	protected void processActionBackupSnapshots() {
		GUIUtils.popupInformationMessage("Not yet implemented");
	}

	protected void processActionDeleteSnapshots() {
		tabSnapshotsPanel.getSnapshotsGridPanel().processActionDelete();
	}
	

	protected void processActionSnapshotsComparison() throws ClassNotFoundException, SQLException {
		List<SnapshotGridRecord> selectedSnapshotGridRecordsList=tabSnapshotsPanel.getSnapshotsGridPanel().getSelectedSnapshotGridRecordsInDateOrderList();
		if (selectedSnapshotGridRecordsList.size()<=1) {
			GUIUtils.popupInformationMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"you must select at least two Snapshots!");
			return;
		}
		if (selectedSnapshotGridRecordsList.size()>5) {
			GUIUtils.popupInformationMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"you must select a maximum of five Snapshots!");
			return;
		}
		if ( ! tabSnapshotsPanel.getServerSelectionPanel().hasUserReadPermission()) {
			GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"You are not allowed to view Snapshots!");
			return;
		}
		SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame=new SnapshotComparisonAnalysisFrame(tabSnapshotsPanel,selectedSnapshotGridRecordsList);
		UIUtils.setFramePosition(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),snapshotComparisonAnalysisFrame);
		tabSnapshotsPanel.setSnapshotComparisonAnalysisFrame(snapshotComparisonAnalysisFrame);
		snapshotComparisonAnalysisFrame.setVisible(true);
	}

	public void processActionNewSnapshot() {
		boolean isAutomationMode = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode();
		try{
			if ( ! tabSnapshotsPanel.getServerSelectionPanel().hasUserWritePermission()) {
				String errMsg = "You are not allowed to create Snapshots!";
				if(isAutomationMode){
					tabSnapshotsPanel.getMainPanel().getSnapshotMain().processGenericException(ModelUtils.removeHTMLTagsFromString(errMsg),null);
				}else{
					GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),errMsg);
					return;
				}
			}
			//lock component
			tabSnapshotsPanel.getMainPanel().getSnapshotMain().setAlreadyClickedNewSnapshot(true);
			tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().setEnableOnShowTotalsDetailsCheckbox(false);
			SnapshotCreationFrame snapshotCreationFrame=new SnapshotCreationFrame(tabSnapshotsPanel);
			UIUtils.setFramePosition(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),snapshotCreationFrame);
			tabSnapshotsPanel.setSnapshotCreationFrame(snapshotCreationFrame);
			snapshotCreationFrame.setVisible(true);
		}
		catch(Exception e) {
			String errMsg = "Unexpected error: "+e.getMessage();
			if(isAutomationMode){
				try {
					tabSnapshotsPanel.getMainPanel().getSnapshotMain().processGenericException(ModelUtils.removeHTMLTagsFromString(errMsg),null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}else{
				GUIUtils.popupErrorMessage(errMsg);
				return;
			}
		}		
	}

	public JButton getSnapshotButton() {
		return snapshotButton;
	}

	public JButton getComparisonButton() {
		return comparisonButton;
	}

	public JButton getDeleteButton() {
		return deleteButton;
	}
	
	public void lockPanel() {
		setComponentsEnabled(false);
	}
	
	public void unlockPanel() {
		setComponentsEnabled(true);
	}
	
	private void setComponentsEnabled(boolean isEnabled) {
		comparisonButton.setEnabled(isEnabled);
		deleteButton.setEnabled(isEnabled);
		snapshotButton.setEnabled(isEnabled);
		upgradeButton.setEnabled(isEnabled);
		importButton.setEnabled(isEnabled);
		exportButton.setEnabled(isEnabled);
	}

	public JButton getSnapshotForUpgradeButton() {
		return upgradeButton;
	}

	public JButton getImportButton() {
		return importButton;
	}

	public JButton getExportButton() {
		return exportButton;
	}

}
