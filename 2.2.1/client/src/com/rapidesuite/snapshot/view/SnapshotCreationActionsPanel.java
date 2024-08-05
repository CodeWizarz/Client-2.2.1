package com.rapidesuite.snapshot.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.GenericControllerCancellationWorker;

@SuppressWarnings("serial")
public class SnapshotCreationActionsPanel  extends SnapshotInventoryGridActionPanel {

	private SnapshotCreationGenericFrame snapshotCreationGenericFrame;
	private JButton snapshotButton;
	private JButton cancelButton;
	private JButton saveGridToExcelButton;
	private JButton closeButton;
	
	public SnapshotCreationActionsPanel(SnapshotCreationGenericFrame snapshotCreationGenericFrame) {
		this.snapshotCreationGenericFrame=snapshotCreationGenericFrame;
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		createComponents();
	}

	private void createComponents() {
		ImageIcon ii=null;
		URL iconURL =null;
		
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
				snapshotCreationGenericFrame.getSnapshotInventoryDetailsGridPanel().saveGridToExcel();
			}
		}
				);
		add(saveGridToExcelButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_take_snapshot.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		snapshotButton = new JButton();
		snapshotButton.setIcon(ii);
		snapshotButton.setBorderPainted(false);
		snapshotButton.setContentAreaFilled(false);
		snapshotButton.setFocusPainted(false);
		snapshotButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_take_snapshot_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		snapshotButton.setRolloverIcon(new RolloverIcon(ii));		
		snapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionSnapshot();
			}
		}
				);
		add(snapshotButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		
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
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionCancel();
			}
		}
				);
		add(cancelButton);
		add(Box.createRigidArea(new Dimension(15, 15)));
		
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
				snapshotCreationGenericFrame.closeWindow();
			}
		}
				);
		add(closeButton);

		if (snapshotCreationGenericFrame.getTabSnapshotsPanel()!=null && !UIConstants.BALLOON_SNAPSHOT_CREATION_START_TRIGGERED) {
			UIConstants.BALLOON_SNAPSHOT_CREATION_START_TRIGGERED=true;
			
    		UIUtils.showBalloon(snapshotButton,
        			"<html>Select the inventories for the Oracle modules above that you wish to snapshot.<br/>"+
        	    			"Then press this button to start the process of copying the Configurations."
        	    			+"<br/><b>Note that it may take a while depending of the volume of Configurations in your EBS instance.</b>",
        	    			snapshotCreationGenericFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowBalloons());
		}
	}

	protected void processActionCancel() {
		int response = JOptionPane.showConfirmDialog(null, "Are you sure to cancel the current snapshot?", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			cancelButton.setEnabled(false);
			GenericControllerCancellationWorker swingWorker=new GenericControllerCancellationWorker(snapshotCreationGenericFrame.getSnapshotCreationController());
			final int width=450;
			final int height=150;
			UIUtils.displayOperationInProgressModalWindow(snapshotCreationGenericFrame,width,height,"Cancel in progress...",swingWorker,SnapshotMain.getSharedApplicationIconPath());	
		}
	}
	protected void processActionSnapshot() {
		List<SnapshotInventoryGridRecord> selectedSnapshotInventoryGridRecordsList=snapshotCreationGenericFrame.getSnapshotInventoryDetailsGridPanel().
				getSelectedSnapshotInventoryGridRecordsList();
		if (selectedSnapshotInventoryGridRecordsList.isEmpty()) {
			GUIUtils.popupErrorMessage("You must select at least one inventory!");
			return;
		}
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedSnapshotInventoryGridRecordsList) {
			snapshotInventoryGridRecord.setStatus(UIConstants.UI_STATUS_PENDING);
		}

		int width=600;
		int height=300;
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		String defaultSnapshotName=snapshotCreationGenericFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().
				getDefaultSnapshotNameTextField().getText()+" ("+strDate+")";
		
		SnapshotCreationInformationWindow snapshotCreationInformationWindow=new SnapshotCreationInformationWindow(snapshotCreationGenericFrame,defaultSnapshotName);
		JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindow(snapshotCreationGenericFrame.getTabSnapshotsPanel().
				getMainPanel().getSnapshotMain().getRootFrame(),"Snapshot Information",width,height,
				snapshotCreationInformationWindow,null,true,SnapshotMain.getSharedApplicationIconPath());
		snapshotCreationInformationWindow.setDialog(dialog);
		dialog.setVisible(true);
	}

	public JButton getSnapshotButton() {
		return snapshotButton;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	@Override
	public JButton getExecutionButton() {
		return snapshotButton;
	}

	public SnapshotCreationGenericFrame getSnapshotCreationGenericFrame() {
		return snapshotCreationGenericFrame;
	}
	

}
