package com.rapidesuite.inject.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

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
import com.rapidesuite.snapshot.view.RolloverIcon;

@SuppressWarnings("serial")
public class ExecutionRetryPanel extends JPanel {

	private JLabel statusLabel;
	private JButton actionButton;
	private JDialog dialog;
	private JProgressBar progressBar;
	private boolean isCancelled;
	
	public ExecutionRetryPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#343836"));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		createComponents();
	}

	public void createComponents(){
		ImageIcon ii=null;
		URL iconURL =null;
		
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(false);
		northPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
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
		tempPanel.setOpaque(false);
		//tempPanel.setBorder(new LineBorder(Color.RED));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		northPanel.add(tempPanel);
		JLabel label=new JLabel("Execution restarting in ");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		statusLabel=new JLabel();
		InjectUtils.assignArialPlainFont(statusLabel,InjectMain.FONT_SIZE_NORMAL);
		statusLabel.setForeground(Color.white);
		tempPanel.add(statusLabel,BorderLayout.CENTER);

		tempPanel=new JPanel();
		centerPanel.add(tempPanel);
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BorderLayout());
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
		progressBar=new JProgressBar();
		tempPanel.add(progressBar,BorderLayout.CENTER);
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
				
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
		tempPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		southPanel.add(tempPanel);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_cancel_snapshot.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		actionButton = new JButton();
		actionButton.setIcon(ii);
		actionButton.setBorderPainted(false);
		actionButton.setContentAreaFilled(false);
		actionButton.setFocusPainted(false);
		actionButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_cancel_snapshot_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		actionButton.setRolloverIcon(new RolloverIcon(ii));
		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionCancel();
			}
		}
				);
		tempPanel.add(actionButton);
	}

	protected void processActionCancel() {
		isCancelled=true;
		dialog.dispose();
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

	public boolean isCancelled() {
		return isCancelled;
	}

	public JDialog getDialog() {
		return dialog;
	}
	
}
