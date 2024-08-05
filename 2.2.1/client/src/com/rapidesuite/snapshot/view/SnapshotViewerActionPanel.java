package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.snapshot.controller.SnapshotDownloadController;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.convert.ConvertFrame;
import com.rapidesuite.snapshot.view.convert.ConvertSourceGridRecordInformation;

@SuppressWarnings("serial")
public class SnapshotViewerActionPanel  extends SnapshotInventoryGridActionPanel {

	private SnapshotViewerFrame snapshotViewerFrame;
	private JButton downloadButton;
	private JButton br100GenerationButton;
	private JButton convertButton;
	private JButton saveGridToExcelButton;
	private JButton closeButton;
	private ConvertFrame convertFrame;

	public SnapshotViewerActionPanel(SnapshotViewerFrame snapshotViewerFrame) {
		this.snapshotViewerFrame=snapshotViewerFrame;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		createComponents();
	}

	private void createComponents() {
		ImageIcon ii=null;
		URL iconURL =null;
		Set<String> activatedPlugins=ModelUtils.getActivatedPlugins();
		
		iconURL = this.getClass().getResource("/images/snapshot/button_save_grid_to_excel.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveGridToExcelButton = new JButton();
		saveGridToExcelButton.setIcon(ii);
		saveGridToExcelButton.setBorderPainted(false);
		saveGridToExcelButton.setContentAreaFilled(false);
		saveGridToExcelButton.setFocusPainted(false);
		saveGridToExcelButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_grid_to_excel_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveGridToExcelButton.setRolloverIcon(new RolloverIcon(ii));
		saveGridToExcelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				snapshotViewerFrame.getSnapshotInventoryDetailsGridPanel().saveGridToExcel();
			}
		}
				);
		add(saveGridToExcelButton);
		add(Box.createRigidArea(new Dimension(15, 15)));

		iconURL = this.getClass().getResource("/images/snapshot/button_viewer_download.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadButton = new JButton();
		downloadButton.setIcon(ii);
		downloadButton.setBorderPainted(false);
		downloadButton.setContentAreaFilled(false);
		downloadButton.setFocusPainted(false);
		downloadButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_viewer_download_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadButton.setRolloverIcon(new RolloverIcon(ii));
		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionDownload();
			}
		}
				);
		add(downloadButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_br100_report.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		br100GenerationButton = new JButton();
		br100GenerationButton.setIcon(ii);
		br100GenerationButton.setBorderPainted(false);
		br100GenerationButton.setContentAreaFilled(false);
		br100GenerationButton.setFocusPainted(false);
		br100GenerationButton.setRolloverEnabled(true);
		br100GenerationButton.setEnabled(false);
		iconURL = this.getClass().getResource("/images/snapshot/button_br100_report_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		br100GenerationButton.setRolloverIcon(new RolloverIcon(ii));
		br100GenerationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionBR100ReportGeneration();
			}
		}
				);
		add(br100GenerationButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_REPORTS_GENERATION)) {
			br100GenerationButton.setEnabled(true);
		}
				
		iconURL = this.getClass().getResource("/images/snapshot/button_convert.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		convertButton = new JButton();
		convertButton.setIcon(ii);
		convertButton.setBorderPainted(false);
		convertButton.setContentAreaFilled(false);
		convertButton.setFocusPainted(false);
		convertButton.setRolloverEnabled(true);
		convertButton.setEnabled(false);
		convertButton.setVisible(false);
		iconURL = this.getClass().getResource("/images/snapshot/button_convert_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		convertButton.setRolloverIcon(new RolloverIcon(ii));
		convertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					processActionConvert();
				}
				catch (Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+ex.getMessage());
				}
			}
		}
				);
		add(convertButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
			convertButton.setEnabled(true);
			convertButton.setVisible(true);
		}
		
		iconURL = this.getClass().getResource("/images/snapshot/button_close.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		closeButton = new JButton();
		closeButton.setIcon(ii);
		closeButton.setBorderPainted(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setFocusPainted(false);
		closeButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_close_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		closeButton.setRolloverIcon(new RolloverIcon(ii));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				snapshotViewerFrame.closeWindow();
			}
		}
				);
		add(closeButton);
	}

	protected void processActionConvert() throws Exception {
		if (convertFrame!=null) {
			convertFrame.closeWindow();
		}
		
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(
				snapshotViewerFrame.getTabSnapshotsPanel().getMainPanel().getTabSnapshotsPanel());
		String workersCountStr=snapshotViewerFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		int workersCount=Integer.valueOf(workersCountStr);
		File rootDownloadFolder=snapshotViewerFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getDownloadFolder();
		
		String currentConnectionOracleRelease=snapshotViewerFrame.getTabSnapshotsPanel().getMainPanel().getTabSnapshotsPanel().
				getServerSelectionPanel().getCurrentConnectionOracleRelease();

		File oracleReleaseFolder=SnapshotPackageSelectionPanel.getOracleReleaseFolder(currentConnectionOracleRelease);
		File sourceInventoriesFolder=new File(oracleReleaseFolder,"inventories");
		boolean isDownloadDataChangesOnly=snapshotViewerFrame.getDownloadDataChangesOnlyCheckBox().isSelected();
		Map<Integer, String> oracleSeededUserIdToUserNameMap=snapshotViewerFrame.getTabSnapshotsPanel().getMainPanel().
				getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap();
		
		List<SnapshotInventoryGridRecord> selectedRecordsList=snapshotViewerFrame.getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList();
		int gridIndex=0;
		List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationList=new ArrayList<ConvertSourceGridRecordInformation>();
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedRecordsList) {
			ConvertSourceGridRecordInformation convertSourceGridRecordInformation=new ConvertSourceGridRecordInformation(snapshotInventoryGridRecord.getInventoryName());
			convertSourceGridRecordInformationList.add(convertSourceGridRecordInformation);
			
			convertSourceGridRecordInformation.setFormInformation(snapshotInventoryGridRecord.getFormInformation());
			convertSourceGridRecordInformation.setGridIndex(gridIndex);
			convertSourceGridRecordInformation.setTotalRecordsToConvert(snapshotInventoryGridRecord.getTotalAddedRecords()+
					snapshotInventoryGridRecord.getTotalUpdatedRecords());
			convertSourceGridRecordInformation.setTableId(snapshotInventoryGridRecord.getTableId());
			convertSourceGridRecordInformation.setWhereClauseFilter(snapshotInventoryGridRecord.getWhereClauseFilter());
		
			gridIndex++;
		}
		Map<String, File> inventoryNameToReverseSQLFileMap=snapshotViewerFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().getInventoryNameToReverseSQLFileMap(currentConnectionOracleRelease);
		
		File plsqlPackageFile=snapshotViewerFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().getPLSQLPackageFile(currentConnectionOracleRelease);
		boolean isConversionSnapshot=snapshotViewerFrame.getTabSnapshotsPanel().getMainPanel().getTabSnapshotsPanel().getSnapshotViewerFrame().getSnapshotGridRecord().isConversion();
		
		convertFrame=new ConvertFrame(snapshotEnvironmentProperties,workersCount,rootDownloadFolder,
				sourceInventoriesFolder,snapshotViewerFrame.getTabSnapshotsPanel().getMainPanel().getTabSnapshotsPanel().getSnapshotViewerFrame().getSnapshotGridRecord(),
				isDownloadDataChangesOnly,oracleSeededUserIdToUserNameMap,convertSourceGridRecordInformationList,
				inventoryNameToReverseSQLFileMap,plsqlPackageFile,isConversionSnapshot);		
		UIUtils.setFramePosition(snapshotViewerFrame,convertFrame);
		convertFrame.setVisible(true);
	}

	protected void processActionDownload() {
		List<SnapshotInventoryGridRecord> selectedRecordsList=snapshotViewerFrame.getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList();
		boolean isDownloadFromViewer=true;
		boolean isComparisonReportGeneration=false;
		boolean isBR100ReportGeneration=false;
		boolean isDownloadDataChangesOnly=snapshotViewerFrame.getDownloadDataChangesOnlyCheckBox().isSelected();
		boolean isExport=false;
		
		SnapshotDownloadController snapshotDownloadController=new SnapshotDownloadController(snapshotViewerFrame.getTabSnapshotsPanel(),selectedRecordsList,
				snapshotViewerFrame.getSnapshotGridRecord(),isDownloadFromViewer,isComparisonReportGeneration,isBR100ReportGeneration,
				isDownloadDataChangesOnly,isExport);
		snapshotDownloadController.startExecution();
	}
	
	protected void processActionBR100ReportGeneration() {
		List<SnapshotInventoryGridRecord> selectedRecordsList=snapshotViewerFrame.getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList();
		boolean isDownloadFromViewer=true;
		boolean isComparisonReportGeneration=false;
		boolean isBR100ReportGeneration=true;
		boolean isDownloadDataChangesOnly=snapshotViewerFrame.isBR100DataChangesOnly();
		boolean isExport=false;
		
		// remove the entries with no changes if the option "BR100 data option: changes only" is selected otherwise it will result in an error
		// opening the excel file (no entry for the tab sheet)
		List<SnapshotInventoryGridRecord> finalList=new ArrayList<SnapshotInventoryGridRecord>();
		if (snapshotViewerFrame.getTabSnapshotsPanel().getSnapshotViewerFrame().isBR100DataChangesOnly()) {
			for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedRecordsList) {
				int totalChanges=snapshotInventoryGridRecord.getTotalAddedRecords()+snapshotInventoryGridRecord.getTotalUpdatedRecords();
				if (totalChanges>0) {
					finalList.add(snapshotInventoryGridRecord);
				}
			}
		}
		else {
			finalList=selectedRecordsList;
		}
		SnapshotDownloadController snapshotDownloadController=new SnapshotDownloadController(snapshotViewerFrame.getTabSnapshotsPanel(),finalList,
				snapshotViewerFrame.getSnapshotGridRecord(),isDownloadFromViewer,isComparisonReportGeneration,isBR100ReportGeneration,
				isDownloadDataChangesOnly,isExport);
		snapshotDownloadController.startExecution();
	}

	public SnapshotViewerFrame getSnapshotViewerFrame() {
		return snapshotViewerFrame;
	}

	@Override
	public JButton getExecutionButton() {
		return null;
	}

}
