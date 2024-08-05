package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.controller.GenericController;

@SuppressWarnings("serial")
public class SnapshotDeletePanel extends JPanel {

	private JLabel statusLabel;
	private JLabel totalTasksLabel;
	private JLabel totalTasksErrorsLabel;
	private JLabel totalRecordCountLabel;
	private JLabel totalTimeLabel;
	private JButton closeButton;
	private JButton cancelButton;
	private JDialog dialog;
	private JLabel snapshotIDLabel;
	
	private JProgressBar progressBar;
	private SnapshotDownloadGridPanel snapshotDownloadGridPanel;
	private GenericController genericController;
	private TabSnapshotsPanel tabSnapshotsPanel;
	
	public SnapshotDeletePanel(GenericController genericController,TabSnapshotsPanel tabSnapshotsPanel) {
		this.genericController=genericController;
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#343836"));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		createComponents();
	}

	public void closeWindow() {
		processActionClose();
	}

	public void createComponents(){
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
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(centerPanel,BorderLayout.CENTER);
		
		JPanel southPanel=new JPanel();
		southPanel.setOpaque(false);
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(southPanel,BorderLayout.SOUTH);
				
		JPanel tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
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
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(tempPanel);
		label=new JLabel("Execution Time:");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		totalTimeLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalTimeLabel,InjectMain.FONT_SIZE_NORMAL);
		totalTimeLabel.setForeground(Color.white);
		tempPanel.add(totalTimeLabel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(tempPanel);
		label=new JLabel("Total Snapshot Counts:");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		snapshotIDLabel=new JLabel();
		InjectUtils.assignArialPlainFont(snapshotIDLabel,InjectMain.FONT_SIZE_NORMAL);
		snapshotIDLabel.setForeground(Color.white);
		tempPanel.add(snapshotIDLabel);

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
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
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(tempPanel);
		label=new JLabel("Errors: ");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		totalTasksErrorsLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalTasksErrorsLabel,InjectMain.FONT_SIZE_NORMAL);
		totalTasksErrorsLabel.setForeground(Color.white);
		tempPanel.add(totalTasksErrorsLabel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(tempPanel);
		label=new JLabel("Total Deleted Records:");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		totalRecordCountLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalRecordCountLabel,InjectMain.FONT_SIZE_NORMAL);
		totalRecordCountLabel.setForeground(Color.white);
		tempPanel.add(totalRecordCountLabel);		
				
		tempPanel=new JPanel();
		northPanel.add(tempPanel);
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
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
			
		snapshotDownloadGridPanel=new SnapshotDownloadGridPanel(tabSnapshotsPanel);
		
		centerPanel.add(snapshotDownloadGridPanel);
		
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
	}


	protected void processActionClose() {
		if (closeButton.isEnabled()) {
			dialog.dispose();
		}
	}

	public JLabel getTotalTasksLabel() {
		return totalTasksLabel;
	}
	
	public JLabel getTotalTasksErrorsLabel() {
		return totalTasksErrorsLabel;
	}

	public JLabel getTotalRecordCountLabel() {
		return totalRecordCountLabel;
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

	public void refreshGrid(List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList) {
		snapshotDownloadGridPanel.refreshGrid(snapshotInventoryGridRecordList);
	}

	public JLabel getTotalTimeLabel() {
		return totalTimeLabel;
	}

	public SnapshotDownloadGridPanel getSnapshotDownloadGridPanel() {
		return snapshotDownloadGridPanel;
	}

	public JLabel getSnapshotIDLabel() {
		return snapshotIDLabel;
	}
		
}