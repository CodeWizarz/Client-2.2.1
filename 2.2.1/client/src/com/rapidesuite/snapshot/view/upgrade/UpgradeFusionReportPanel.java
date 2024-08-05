package com.rapidesuite.snapshot.view.upgrade;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.GenericController;
import com.rapidesuite.snapshot.model.SnapshotDownloadCancellation;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class UpgradeFusionReportPanel  extends JPanel {

	private JLabel statusLabel;
	private JLabel totalTasksLabel;
	private JLabel exportedFolderLabel;
	private JLabel totalDownloadTimeLabel;
	private JButton closeButton;
	private JButton cancelButton;
	private JDialog dialog;
	
	private JProgressBar progressBar;
	private File downloadFolder;
	private UpgradeFusionReportGridPanel upgradeFusionReportGridPanel;
	private GenericController genericController;
	
	public UpgradeFusionReportPanel(GenericController genericController,String executionTimeLabelText,String outputFolderLabelText ) {
		this.genericController=genericController;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#343836"));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		createComponents(executionTimeLabelText,outputFolderLabelText);
	}

	public void closeWindow() {
		processActionClose();
	}

	public void createComponents(String executionTimeLabelText,String outputFolderLabelText){
		ImageIcon ii=null;
		URL iconURL =null;
		
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(false);
		northPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(northPanel,BorderLayout.NORTH);
		
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		//centerPanel.setBorder(new LineBorder(Color.GREEN));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(centerPanel,BorderLayout.CENTER);
		
		JPanel southPanel=new JPanel();
		southPanel.setOpaque(false);
		//southPanel.setBorder(new LineBorder(Color.YELLOW));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(southPanel,BorderLayout.SOUTH);
				
		JPanel tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
		tempPanel.setOpaque(false);
		//tempPanel.setBorder(new LineBorder(Color.RED));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		northPanel.add(tempPanel);
		JLabel label=new JLabel("Status: ");
		label.setFont(new Font("Arial", Font.BOLD, 16));
		label.setForeground(Color.white);
		tempPanel.add(label);
		statusLabel=new JLabel();
		statusLabel.setForeground(Color.white);
		statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
		tempPanel.add(statusLabel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		//tempPanel.setBorder(new LineBorder(Color.RED));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		northPanel.add(tempPanel);
		label=new JLabel(executionTimeLabelText);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		totalDownloadTimeLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalDownloadTimeLabel,InjectMain.FONT_SIZE_NORMAL);
		totalDownloadTimeLabel.setForeground(Color.white);
		tempPanel.add(totalDownloadTimeLabel);

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		//tempPanel.setBorder(new LineBorder(Color.RED));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		northPanel.add(tempPanel);
		label=new JLabel("Inventories: ");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		totalTasksLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalTasksLabel,InjectMain.FONT_SIZE_NORMAL);
		totalTasksLabel.setForeground(Color.white);
		tempPanel.add(totalTasksLabel);
				
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		northPanel.add(tempPanel);
		label=new JLabel(outputFolderLabelText);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		exportedFolderLabel=new JLabel();
		InjectUtils.assignArialPlainFont(exportedFolderLabel,InjectMain.FONT_SIZE_NORMAL);
		exportedFolderLabel.setForeground(Color.white);
		exportedFolderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		exportedFolderLabel.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	            	processActionOpenDownloadFolder();
	            }
	        });
		tempPanel.add(exportedFolderLabel);	
		
		tempPanel=new JPanel();
		northPanel.add(tempPanel);
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
		
		label=new JLabel("Progress: ");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		
		progressBar=new JProgressBar();
		Dimension prefSize = progressBar.getPreferredSize();
		prefSize.width = 300;
		prefSize.height = 20;
		progressBar.setPreferredSize(prefSize);
		tempPanel.add(progressBar);
		progressBar.setOpaque(true);
		progressBar.setBackground(Color.black);
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(false);
		progressBar.setUI( new BasicProgressBarUI() {
		      protected Color getSelectionBackground() { return Color.white; }
		      protected Color getSelectionForeground() { return Color.white; }
		    });
			
		upgradeFusionReportGridPanel=new UpgradeFusionReportGridPanel();
		centerPanel.add(upgradeFusionReportGridPanel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
		tempPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		southPanel.add(tempPanel);
				
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
		closeButton.setEnabled(false);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionClose();
			}
		}
				);
		tempPanel.add(closeButton);
		
		// CANCEL button
		iconURL = this.getClass().getResource("/images/snapshot/button_cancel_snapshot.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		cancelButton = new JButton();
		cancelButton.setIcon(ii);
		cancelButton.setBorderPainted(false);
		cancelButton.setContentAreaFilled(false);
		cancelButton.setFocusPainted(false);
		cancelButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_cancel_snapshot_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		cancelButton.setRolloverIcon(new RolloverIcon(ii));
		cancelButton.setEnabled(true);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionCancel();
			}
		}
				);
		tempPanel.add(cancelButton);
	}

	protected void processActionOpenDownloadFolder() {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(downloadFolder);
			}
			else {
				SeleniumUtils.startLinuxFileBrowser(downloadFolder);
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}

	protected void processActionClose() {
		if (closeButton.isEnabled()) {
			dialog.dispose();
		}
	}

	protected void processActionCancel() {
		int response = JOptionPane.showConfirmDialog(null, "Are you sure to cancel the current download?", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			//snapshotDownloadController.stopExecution();
			cancelButton.setEnabled(false);
			genericController.setCancelled(true);
			SnapshotDownloadCancellation swingWorker=new SnapshotDownloadCancellation(genericController);
			final int width=450;
			final int height=150;
			UIUtils.displayOperationInProgressModalWindow(this,width,height,"Cancel in progress...",swingWorker,SnapshotMain.getSharedApplicationIconPath());
		}
	}

	public JLabel getTotalTasksLabel() {
		return totalTasksLabel;
	}
	
	public JButton getActionButton() {
		return closeButton;
	}
	
	public JButton getCancelButton() {
		return cancelButton;
	}

	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
	}

	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public void setTotalSteps(int totalStepsCounter) {
		progressBar.setMaximum(totalStepsCounter);
	}

	public void updateProgressBar(int currentStep) {
		progressBar.setValue(currentStep);
	}

	public void setExportFolder(File downloadFolder) {
		this.downloadFolder=downloadFolder;
		String msg="<html><U>"+downloadFolder.getAbsolutePath()+"</U>";		
		exportedFolderLabel.setText(msg);
	}

	public void refreshGrid(List<FusionInventoryRow> fusionInventoryRowList) {
		upgradeFusionReportGridPanel.refreshGrid(fusionInventoryRowList);
	}

	public JLabel getTotalDownloadTimeLabel() {
		return totalDownloadTimeLabel;
	}

	public UpgradeFusionReportGridPanel getUpgradeFusionReportGridPanel() {
		return upgradeFusionReportGridPanel;
	}
		
}