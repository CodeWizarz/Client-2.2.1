package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.snapshot.controller.SnapshotCreationController;
import com.rapidesuite.snapshot.controller.SnapshotDownloadController;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class SnapshotComparisonActionPanel extends SnapshotInventoryGridActionPanel {

	private SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame;
	private SnapshotCreationController snapshotCreationController;
	private JButton downloadButton;
	private JButton comparisonReportGenerationButton;
	private JButton closeButton;
	
	public SnapshotComparisonActionPanel(SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame) {
		this.snapshotComparisonAnalysisFrame=snapshotComparisonAnalysisFrame;
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
		
		iconURL = this.getClass().getResource("/images/snapshot/button_download_changes.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadButton = new JButton();
		downloadButton.setIcon(ii);
		downloadButton.setBorderPainted(false);
		downloadButton.setContentAreaFilled(false);
		downloadButton.setFocusPainted(false);
		downloadButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_download_changes_rollover.png");
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
		
		iconURL = this.getClass().getResource("/images/snapshot/button_comparison_report.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		comparisonReportGenerationButton = new JButton();
		comparisonReportGenerationButton.setIcon(ii);
		comparisonReportGenerationButton.setBorderPainted(false);
		comparisonReportGenerationButton.setContentAreaFilled(false);
		comparisonReportGenerationButton.setFocusPainted(false);
		comparisonReportGenerationButton.setRolloverEnabled(true);
		comparisonReportGenerationButton.setEnabled(false);
		iconURL = this.getClass().getResource("/images/snapshot/button_comparison_report_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		comparisonReportGenerationButton.setRolloverIcon(new RolloverIcon(ii));
		comparisonReportGenerationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionComparisonReportGeneration();
			}
		}
				);
		add(comparisonReportGenerationButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_REPORTS_GENERATION)) {
			comparisonReportGenerationButton.setEnabled(true);
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
				snapshotComparisonAnalysisFrame.closeWindow();
			}
		}
				);
		add(closeButton);
	}

	protected void processActionCancel() {
		snapshotCreationController.stopExecution();
	}

	public SnapshotComparisonAnalysisFrame getSnapshotComparisonAnalysisFrame() {
		return snapshotComparisonAnalysisFrame;
	}

	protected void processActionDownload() {
		int size=snapshotComparisonAnalysisFrame.getSnapshotGridRecordsInDateOrder().size();
		if (size!=2) {
			GUIUtils.popupInformationMessage("You can only download when comparing two snapshots. You selected "+size+" snapshots!");
			return;
		}
		List<SnapshotInventoryGridRecord> selectedRecordsList=snapshotComparisonAnalysisFrame.getSnapshotComparisonAnalysisGridPanel().
				getSelectedSnapshotInventoryGridRecordsList();
		if (selectedRecordsList.isEmpty()) {
			GUIUtils.popupInformationMessage("You must select at least one inventory before downloading.");
			return;
		}
		boolean isDownloadFromViewer=false;
		boolean isComparisonReportGeneration=false;
		boolean isBR100ReportGeneration=false;
		boolean isDownloadDataChangesOnly=true;
		boolean isExport=false;
		
		SnapshotDownloadController snapshotDownloadController=new SnapshotDownloadController(snapshotComparisonAnalysisFrame.getTabSnapshotsPanel(),selectedRecordsList,
				snapshotComparisonAnalysisFrame.getSnapshotGridRecordWithChanges(),isDownloadFromViewer,isComparisonReportGeneration,
				isBR100ReportGeneration,isDownloadDataChangesOnly,isExport);
		snapshotDownloadController.startExecution();
	}

	protected void processActionComparisonReportGeneration() {
		List<SnapshotInventoryGridRecord> selectedRecordsList=snapshotComparisonAnalysisFrame.getSnapshotComparisonAnalysisGridPanel().
				getSelectedSnapshotInventoryGridRecordsList();
		if (selectedRecordsList.isEmpty()) {
			GUIUtils.popupInformationMessage("You must select at least one inventory before downloading.");
			return;
		}
		boolean isDownloadFromViewer=false;
		boolean isComparisonReportGeneration=true;
		boolean isBR100ReportGeneration=false;
		boolean isDownloadDataChangesOnly=true;
		boolean isExport=false;
		
		SnapshotDownloadController snapshotDownloadController=new SnapshotDownloadController(snapshotComparisonAnalysisFrame.getTabSnapshotsPanel(),selectedRecordsList,
				snapshotComparisonAnalysisFrame.getSnapshotGridRecordWithChanges(),isDownloadFromViewer,isComparisonReportGeneration,
				isBR100ReportGeneration,isDownloadDataChangesOnly,isExport);
		snapshotDownloadController.startExecution();
	}

	public SnapshotCreationController getSnapshotCreationController() {
		return snapshotCreationController;
	}

	@Override
	public JButton getExecutionButton() {
		return null;
	}

}
