/**************************************************
 * $Revision: 44787 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-11-27 13:15:02 +0700 (Thu, 27 Nov 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/DataExtractionOptionsPanel.java $:
 * $Id: DataExtractionOptionsPanel.java 44787 2014-11-27 06:15:02Z fajrian.yunus $:
 */

package com.rapidesuite.reverse.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;


@SuppressWarnings("serial")
public class DataExtractionOptionsPanel extends JPanel
{

	public DataExtractionPanel panel;

	private JCheckBox analyseModeJCheckBox;
	private JCheckBox isAuditExtractJCheckBox;
	private JLabel workersCountJLabel;
	private JTextField workersCountJTextField;

	private JLabel zipFolderJLabel;
	private JFileChooser zipFolderFileChooser;
	private JTextField zipFolderJTextField;
	private JButton zipFolderSelectionButton;
	private File zipFolder;

	public DataExtractionOptionsPanel(DataExtractionPanel panel)
	{
		this.panel= panel;
		createComponents();
	}

	public void createComponents()
	{
		zipFolderFileChooser=Utils.initializeJFileChooserWithTheLastPath("ZIP_FOLDER_FILE_CHOOSER_DATA_EXTRACTION_OPTIONS_PANEL");
		zipFolderFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		zipFolderJLabel= new JLabel("Select the folder where the ZIP file will be stored:");
		zipFolderJTextField =new JTextField();
		zipFolderJTextField.setEditable(false);
		zipFolderJTextField.setBackground(Color.WHITE);

		workersCountJLabel= new JLabel("Workers number (parallel execution):");
		workersCountJTextField =new JTextField();
		workersCountJTextField.setEditable(true);
		workersCountJTextField.setBackground(Color.WHITE);
		int workersCountProp=Config.getReverseWorkersCount();
		workersCountJTextField.setText(new Integer(workersCountProp).toString());

		URL iconURL = this.getClass().getResource("/images/open16.gif");
		ImageIcon ii=null;

		iconURL = this.getClass().getResource("/images/open16.gif");
		ii=null;
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		zipFolderSelectionButton = new JButton("Browse", ii);
		zipFolderSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					openZipFolderChooser();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		);

		isAuditExtractJCheckBox= new JCheckBox("Enable Audit extraction (last_updated_by and last_update_date)");
	    isAuditExtractJCheckBox.setSelected(Config.isExtractAudit());

		analyseModeJCheckBox= new JCheckBox("Run in Analyse mode (returns the records count instead of data)");
		analyseModeJCheckBox.setSelected(false);

		try{
			zipFolder=Config.getReverseZipFileLocation();
			zipFolderJTextField.setText(zipFolder.getAbsolutePath());
		} catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}

		/*
		 * adding components to panels:
		 */
		setLayout(new BorderLayout());

		JPanel northPanel= new JPanel();
		northPanel.setBorder(BorderFactory.createEmptyBorder(50, 15, 5, 5));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		add(northPanel, BorderLayout.NORTH);

		JPanel	panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		northPanel.add(panel);
		panel.add(analyseModeJCheckBox);
		panel.add(Box.createGlue());

		panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		northPanel.add(panel);
		panel.add(isAuditExtractJCheckBox);
		panel.add(Box.createGlue());

		panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		northPanel.add(panel);
		panel.add(zipFolderJLabel);
		panel.add(zipFolderJTextField);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(zipFolderSelectionButton);
		panel.add(Box.createGlue());

		panel= new JPanel();
		northPanel.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(workersCountJLabel);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(workersCountJTextField);
		panel.add(Box.createHorizontalStrut(600));
	}

	public List<Component> getAllComponents() {
		List<Component> list=new ArrayList<Component>();
		list.add(analyseModeJCheckBox);
		list.add(isAuditExtractJCheckBox);
		list.add(zipFolderSelectionButton);
		list.add(workersCountJTextField);

		return list;
	}

	public void lockAll() {
		GUIUtils.setEnabledOnComponents(getAllComponents(),false);
	}

	public void unlockAll() {
		GUIUtils.setEnabledOnComponents(getAllComponents(),true);
	}

	public boolean isAnalyseMode()
	{
		return analyseModeJCheckBox.isSelected();
	}

	public void setAnalyseMode(boolean isSelected)
	{
		analyseModeJCheckBox.setSelected(isSelected);
	}

	public void setAuditExtract(boolean isSelected)
	{
		isAuditExtractJCheckBox.setSelected(isSelected);
	}

	public boolean isAuditExtractionEnabled()
	{
		return isAuditExtractJCheckBox.isSelected();
	}

	public void openZipFolderChooser()
	throws Exception
	{
		zipFolder = null;
		zipFolderJTextField.setText("");
		setSelectedZipFolder(getSelectedFolderChooser(zipFolderFileChooser));
	}

	public File getSelectedFolderChooser(JFileChooser fileChooser)
	throws Exception
	{
		int returnVal = fileChooser.showDialog(this, "Open");
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFile();
		}
		return null;
	}

	public File getSelectedZipFolder() {
		return zipFolder;
	}

	public int getWorkersCount() {
		String text=workersCountJTextField.getText();
		if (text==null) return -1;
		try{
			return Integer.valueOf(text);
		}
		catch(Exception e) {
			return -1;
		}
	}

	public void setSelectedZipFolder(File zipFolderParam) {
		if (zipFolderParam!=null) {
			zipFolder=zipFolderParam;
			zipFolderJTextField.setText(zipFolderParam.getAbsolutePath());
		}
	}

	public void setWorkersCount(int count) {
		workersCountJTextField.setText(String.valueOf(count));
	}

}