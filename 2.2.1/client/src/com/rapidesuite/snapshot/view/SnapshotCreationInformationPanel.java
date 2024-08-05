package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public abstract class SnapshotCreationInformationPanel  extends JPanel{

	private JTextField snapshotNameTextField;
	private JTextArea snapshotDescriptionTextArea;
	private JButton continueButton;
	private JDialog dialog;
	
	public SnapshotCreationInformationPanel(String defaultSnapshotName) {
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setOpaque(true);
		setBackground(Color.decode("#4b4f4e"));
		createComponents(defaultSnapshotName);
	}
	
	public void createComponents(String defaultSnapshotName){
		JPanel tempLabelPanel=new JPanel();
		tempLabelPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		tempLabelPanel.setOpaque(false);
		tempLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempLabelPanel);
		JLabel label=new JLabel("Snapshot name:");
		UIUtils.setDimension(label,120,18);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempLabelPanel.add(label);
		snapshotNameTextField=new JTextField();
		UIUtils.setDimension(snapshotNameTextField,400,18);
		
		snapshotNameTextField.setText(defaultSnapshotName);
		tempLabelPanel.add(snapshotNameTextField);
		
		JPanel tempAreaPanel=new JPanel();
		tempAreaPanel.setOpaque(false);
		tempAreaPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		tempAreaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempAreaPanel);
		JLabel labelTextArea=new JLabel("Snapshot description:");
		UIUtils.setDimension(labelTextArea,120,18);
		InjectUtils.assignArialPlainFont(labelTextArea,InjectMain.FONT_SIZE_NORMAL);
		labelTextArea.setForeground(Color.white);
		tempAreaPanel.add(labelTextArea);
		snapshotDescriptionTextArea=new JTextArea(5,49);
		tempAreaPanel.add(snapshotDescriptionTextArea);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		add(tempPanel);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_continue.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		continueButton = new JButton();
		continueButton.setIcon(ii);
		continueButton.setBorderPainted(false);
		continueButton.setContentAreaFilled(false);
		continueButton.setFocusPainted(false);
		continueButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_continue_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		continueButton.setRolloverIcon(new RolloverIcon(ii));
		continueButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				processAction();
			}

		});
		tempPanel.add(Box.createHorizontalGlue());
		tempPanel.add(continueButton);
		tempPanel.add(Box.createHorizontalGlue());
	}
	
	private void processAction() {
		processActionSubmit(this);
	}

	public abstract void processActionSubmit(SnapshotCreationInformationPanel snapshotCreationInformationPanel);

	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
	}

	public JDialog getDialog() {
		return dialog;
	}

	public JTextField getSnapshotNameTextField() {
		return snapshotNameTextField;
	}

	public JTextArea getSnapshotDescriptionTextArea() {
		return snapshotDescriptionTextArea;
	}

}
