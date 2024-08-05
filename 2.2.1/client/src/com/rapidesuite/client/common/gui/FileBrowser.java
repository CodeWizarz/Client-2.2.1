package com.rapidesuite.client.common.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;

public class FileBrowser{

	private JLabel label;
	private JTextField textField;
	private JButton button;
	private JPanel panel;
	private File selectedFile;
	private boolean isTextFieldEditable;
	private String labelText;
	private JFileChooser fileChooser;
	private FileFilter fileFilter;
	private FileBrowserOpenAction action;
	private boolean hasFileChanged;
		
	public FileBrowser(String labelText,boolean isTextFieldEditable,FileFilter fileFilter,
			FileBrowserOpenAction action)
	throws Exception{
		this.labelText=labelText;
		this.isTextFieldEditable=isTextFieldEditable;
		this.fileFilter=fileFilter;
		this.action=action;
		createComponents();
	}

	private void createComponents() throws Exception {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		label = new JLabel(labelText);
		textField =new JTextField();
		textField.setBackground(Color.WHITE);
		textField.setEditable(isTextFieldEditable);

		URL iconURL = this.getClass().getResource("/images/open16.gif");
		ImageIcon ii=null;
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button=new JButton("Browse", ii);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					openFileChooser();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		);

		panel.add(label);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(textField);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(button);
		panel.add(Box.createVerticalStrut(5));
		
		fileChooser=Utils.initializeJFileChooserWithTheLastPath("FILE_CHOOSER_FILE_BROWSER");
		fileChooser.setFileFilter(fileFilter);
	}

	private void openFileChooser()
	throws Exception
	{    
		int returnVal = fileChooser.showDialog(panel, "Open");
		if (returnVal == JFileChooser.APPROVE_OPTION){
			setSelectedFile(fileChooser.getSelectedFile());
			if (action!=null) {
				action.openFileAction();
			}
		}
	}

	public File getSelectedFile() {
		return selectedFile;
	}
	
	public void setSelectedFile(File file) {
		hasFileChanged=true;
		selectedFile=file;
		fileChooser.setSelectedFile(selectedFile);
		textField.setText(selectedFile.getAbsolutePath());
	}

	public JPanel getPanel() {
		return panel;
	}

	public JButton getButton() {
		return button;
	}
	
	public boolean hasFileChanged() {
		return hasFileChanged;
	}
	
	public void setHasFileChanged(boolean hasFileChanged) {
		this.hasFileChanged=hasFileChanged;
	}
	
}