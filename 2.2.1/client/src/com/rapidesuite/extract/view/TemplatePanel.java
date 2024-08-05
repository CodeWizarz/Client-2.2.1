package com.rapidesuite.extract.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.extract.model.ExtractInventoryRecord;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.RolloverIcon;

@SuppressWarnings({ "serial",})
public class TemplatePanel extends JPanel {

	private ExtractMain extractMain;
	private JFrame rootFrame;
	private ExtractInventoryGridSelectionPanel extractInventoryGridSelectionPanel;

	private JLabel templateSelectionLabel;
	private JComboBox<String> templateSelectionComboBox;
	private JButton templateCreationButton;
	private JButton templateDeleteButton;
	
	public TemplatePanel(ExtractMain extractMain,ExtractInventoryGridSelectionPanel extractInventoryGridSelectionPanel) {
		this.extractMain = extractMain;
		this.rootFrame=extractMain.getRootFrame();
		this.extractInventoryGridSelectionPanel=extractInventoryGridSelectionPanel;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		createComponents();
	}

	public void createComponents(){
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		add(centerPanel);
		
		templateSelectionLabel=new JLabel("Template selection:");
		InjectUtils.assignArialPlainFont(templateSelectionLabel,InjectMain.FONT_SIZE_NORMAL);
		templateSelectionLabel.setForeground(Color.black);
		centerPanel.add(templateSelectionLabel);
		centerPanel.add(Box.createRigidArea(new Dimension(3, 15)));
		
		templateSelectionComboBox = new JComboBox<String>();
		templateSelectionComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadTemplate();
			}
		});
		centerPanel.add(templateSelectionComboBox);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_save_template.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateCreationButton = new JButton();
		templateCreationButton.setIcon(ii);
		templateCreationButton.setBorderPainted(false);
		templateCreationButton.setContentAreaFilled(false);
		templateCreationButton.setFocusPainted(false);
		templateCreationButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_template_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateCreationButton.setRolloverIcon(new RolloverIcon(ii));
		centerPanel.add(templateCreationButton);
		templateCreationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processActionSaveNewTemplate();
			}
		});
		
		iconURL = this.getClass().getResource("/images/snapshot/button_delete_template.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateDeleteButton = new JButton();
		templateDeleteButton.setIcon(ii);
		templateDeleteButton.setBorderPainted(false);
		templateDeleteButton.setContentAreaFilled(false);
		templateDeleteButton.setFocusPainted(false);
		templateDeleteButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_delete_template_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateDeleteButton.setRolloverIcon(new RolloverIcon(ii));
		centerPanel.add(templateDeleteButton);
		templateDeleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processActionDeleteTemplate();
			}
		});
		
		refreshTemplates();
	}

	public JFrame getRootFrame() {
		return rootFrame;
	}

	public ExtractInventoryGridSelectionPanel getExtractInventoryGridSelectionPanel() {
		return extractInventoryGridSelectionPanel;
	}
	
	private void refreshTemplates() {
		File templateFolder=extractMain.getExtractMainPanel().getTabOptionsPanel().getTemplateFolder();
		try {
			List<File> allTemplates=new ArrayList<File>();
			List<File> templatesList=FileUtils.listAllFiles(templateFolder,false,ExtractConstants.EXTRACT_TEMPLATE_FILE_EXT);
			List<File> seededTemplatesList=FileUtils.listAllFiles(
					extractMain.getExtractMainPanel().getSeededTemplateFolder(),false,ExtractConstants.EXTRACT_TEMPLATE_FILE_EXT);
			allTemplates.addAll(seededTemplatesList);
			allTemplates.addAll(templatesList);
			
			templateSelectionComboBox.removeAllItems();
			templateSelectionComboBox.addItem("");
			for (File templateFile:allTemplates) {
				templateSelectionComboBox.addItem(CoreUtil.getFileNameWithoutExtension(templateFile) );
			}
		} catch (Exception e1) {
			FileUtils.printStackTrace(e1);
		}		
	}
	

	
	private void loadTemplate() {
		try {
			if (extractInventoryGridSelectionPanel==null){
				return;
			}
			String templateName=(String)templateSelectionComboBox.getSelectedItem();
			if (templateName!=null && !templateName.isEmpty()) {
				Set<String> inventoriesToSelect=new TreeSet<String>();
				File templateFolder=extractMain.getExtractMainPanel().getTabOptionsPanel().getTemplateFolder();
				File templateFile=new File(templateFolder,templateName+"."+ExtractConstants.EXTRACT_TEMPLATE_FILE_EXT);
				// if the template is not found in the template folder, look for it in the sessions folder.
				if(!templateFile.exists()) {
					templateFolder=extractMain.getExtractMainPanel().getSeededTemplateFolder();
					templateFile=new File(templateFolder,templateName+"."+ExtractConstants.EXTRACT_TEMPLATE_FILE_EXT);
				}
				List<String> inventoriesList=ModelUtils.readContentsFromTemplateFile(templateFile);
				
				for (String inventoryName:inventoriesList) {
					inventoriesToSelect.add(inventoryName);
				}	
				extractInventoryGridSelectionPanel.setSelectionForInventories(inventoriesToSelect);
				extractInventoryGridSelectionPanel.getFilteringTable().setColumnFilterValue(
						ExtractInventoryGridSelectionPanel.COLUMN_HEADING_SELECTION,"Selected");
				extractInventoryGridSelectionPanel.getFilteringTable().applyFiltering();
			}
			else {
				extractInventoryGridSelectionPanel.setSelectionAll();
				extractInventoryGridSelectionPanel.getFilteringTable().setColumnFilterValue(
						ExtractInventoryGridSelectionPanel.COLUMN_HEADING_SELECTION,"");
				extractInventoryGridSelectionPanel.getFilteringTable().applyFiltering();
			}
		}
		catch (Exception e1) {
			FileUtils.printStackTrace(e1);
			GUIUtils.popupErrorMessage("Internal error: "+e1.getMessage());
		}
	}

	private void processActionDeleteTemplate() {
		try {
			String templateName=(String)templateSelectionComboBox.getSelectedItem();
			if (!templateName.isEmpty()) {
				File templateFolder=extractMain.getExtractMainPanel().getTabOptionsPanel().getTemplateFolder();
				File templateFile=new File(templateFolder,templateName+"."+ExtractConstants.EXTRACT_TEMPLATE_FILE_EXT);
				boolean isDeleted=templateFile.delete();
				if (!isDeleted) {
					FileUtils.println("Unable to delete template file: '"+templateFile.getAbsolutePath()+"'");
				}
				else {
					refreshTemplates();
					GUIUtils.popupInformationMessage("Template '"+templateName+"' successfully deleted!");
				}
			}
		}
		catch (Exception e1) {
			FileUtils.printStackTrace(e1);
		}
	}

	private void processActionSaveNewTemplate() {
		String templateName = JOptionPane.showInputDialog(this, "Enter the new template name:");
		if (templateName==null) {
			return;
		}
		List<ExtractInventoryRecord> list=extractInventoryGridSelectionPanel.getSelectedExtractInventoryGridRecordsList();
		StringBuffer content=new StringBuffer("");
		for (ExtractInventoryRecord extractInventoryRecord:list) {
			content.append(extractInventoryRecord.getInventoryName()).append("\n");
		}
		File templateFolder=extractMain.getExtractMainPanel().getTabOptionsPanel().getTemplateFolder();
		File templateFile=new File(templateFolder,templateName+"."+ExtractConstants.EXTRACT_TEMPLATE_FILE_EXT);
		ModelUtils.writeToFile(templateFile, content.toString(),false);
		refreshTemplates();
		templateSelectionComboBox.setSelectedItem(templateName);
		GUIUtils.popupInformationMessage("Template '"+templateName+"' successfully created!");
	}
	
}