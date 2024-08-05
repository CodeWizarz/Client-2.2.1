package com.rapidesuite.snapshot.model;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicProgressBarUI;

import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

public abstract class SnapshotSwingWorker extends SwingWorker<Void, Void> {

	private JPanel mainPanel;
	private JLabel executionMessageLabel;
	private JLabel executionTimeLabel;
	private long startTime;
	private JProgressBar progressBar;
	private JDialog dialog;

	public SnapshotSwingWorker() {
		this(false);
	}

	public SnapshotSwingWorker(boolean showProgressBar) {
		mainPanel=new JPanel();
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#343836"));
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		executionMessageLabel=new JLabel("", SwingConstants.CENTER);
		executionMessageLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
		executionMessageLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(executionMessageLabel,InjectMain.FONT_SIZE_NORMAL);
		executionMessageLabel.setForeground(Color.white);

		executionTimeLabel=new JLabel("", SwingConstants.CENTER);
		executionTimeLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
		executionTimeLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(executionTimeLabel,10);
		executionTimeLabel.setForeground(Color.white);

		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BorderLayout());
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0,15));
		progressBar=new JProgressBar();
		if (showProgressBar) {
			tempPanel.add(progressBar,BorderLayout.CENTER);
		}
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

		mainPanel.add(executionMessageLabel,BorderLayout.NORTH);
		mainPanel.add(tempPanel,BorderLayout.CENTER);
		mainPanel.add(executionTimeLabel,BorderLayout.SOUTH);
		startTime=System.currentTimeMillis();
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void setTotalSteps(int totalStepsCounter) {
		progressBar.setMaximum(totalStepsCounter);
	}

	public void updateExecutionLabels(String executionMessage,int currentStep) {
		updateExecutionLabels(executionMessage);
		progressBar.setValue(currentStep);
	}

	public void updateStep(int currentStep) {
		progressBar.setValue(currentStep);
	}

	public void incrementStep(int additionalStep) {
		progressBar.setValue(progressBar.getValue()+additionalStep);
	}

	public void updateExecutionLabels(String executionMessage) {
		executionMessageLabel.setText(executionMessage);
		String executionTime=Utils.getExecutionTime(startTime,System.currentTimeMillis());
		executionTimeLabel.setText("Execution time: "+executionTime);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JLabel getExecutionMessageLabel() {
		return executionMessageLabel;
	}
	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
	}

	public JDialog getDialog() {
		return dialog;
	}

}