package com.rapidesuite.designers.navigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class KBPlaceholderPanel extends JPanel{

	private JComboBox<String> inventorySelectionJComboBox;
	private JComboBox<String> columnNameSelectionJComboBox;
	private JButton submitButton;
	private boolean isSubmitted;
	private JDialog dialog;
	private NavigationEditorMain navigationEditorMain;
	private boolean isShowColumnName;
	
	public KBPlaceholderPanel(NavigationEditorMain navigationEditorMain,boolean isShowColumnName) {
		this.navigationEditorMain=navigationEditorMain;
		this.isShowColumnName=isShowColumnName;
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		createComponents();
	}
	
	public void createComponents(){
		JPanel centerPanel=new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#4b4f4e"));
		centerPanel.setLayout(new BorderLayout());
		add(centerPanel);
		
		JPanel subPanel=new JPanel();
		subPanel.setOpaque(false);
		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		centerPanel.add(subPanel,BorderLayout.CENTER);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		subPanel.add(tempPanel);
		JLabel label=new JLabel("Select an inventory:");
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		UIUtils.setDimension(label,150,15);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		inventorySelectionJComboBox=new JComboBox<String>();
		tempPanel.add(inventorySelectionJComboBox);
		
		final Map<String, Inventory> inventoryNameToInventoryMap=navigationEditorMain.getInventoryNameToInventoryMap();
		Iterator<String> iterator=inventoryNameToInventoryMap.keySet().iterator();
		Inventory firstInventory=null;
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			inventorySelectionJComboBox.addItem(inventoryName);
			if (firstInventory==null) {
				firstInventory=inventoryNameToInventoryMap.get(inventoryName);
			}
		}

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		subPanel.add(tempPanel);
		label=new JLabel("Select a column name:");
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		UIUtils.setDimension(label,150,15);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.white);
		tempPanel.add(label);
		columnNameSelectionJComboBox=new JComboBox<String>();
		tempPanel.add(columnNameSelectionJComboBox);

		inventorySelectionJComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					columnNameSelectionJComboBox.removeAllItems();
					String inventoryName= (String) event.getItem();
					Inventory inventory=inventoryNameToInventoryMap.get(inventoryName);
					if (inventory==null) {
						return;
					}
					for (String fieldName:inventory.getFieldNamesUsedForDataEntry()) {
						columnNameSelectionJComboBox.addItem(fieldName);
					}
				}
			}     
		});
		for (String fieldName:firstInventory.getFieldNamesUsedForDataEntry()) {
			columnNameSelectionJComboBox.addItem(fieldName);
		}
						
		submitButton = new JButton("Submit");
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				submit();
			}
		}
				);
		subPanel.add(submitButton);
		tempPanel.setVisible(isShowColumnName);
	}

	protected void submit() {
		isSubmitted=true;
		dialog.dispose();
	}

	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
	}

	public String getSelectedInventoryName() {
		return (String)inventorySelectionJComboBox.getSelectedItem();
	}

	public String getSelectedColumnName() {
		return (String)columnNameSelectionJComboBox.getSelectedItem();
	}

	public boolean isSubmitted() {
		return isSubmitted;
	}
	
}