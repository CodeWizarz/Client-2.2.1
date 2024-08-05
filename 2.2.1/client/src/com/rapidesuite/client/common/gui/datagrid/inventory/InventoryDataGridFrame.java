package com.rapidesuite.client.common.gui.datagrid.inventory;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class InventoryDataGridFrame extends JFrame {

	private JLabel statusLabel;
	private JTextArea selectableErrorTextArea;
	private String dataGridTitle;
	private InventoryDataGridController inventoryDataGridController;
	
	public InventoryDataGridFrame(
			String dataGridTitle,
			InventoryDataGridController inventoryDataGridController,
			int frameWidth,
			int frameHeight) 
	{
		this.inventoryDataGridController=inventoryDataGridController;
		this.dataGridTitle=dataGridTitle;
		createComponents(frameWidth,frameHeight);
	}
	
	public void createComponents(int frameWidth,int frameHeight)
	{
		//this.setLayout(new BorderLayout());
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		this.add(inventoryDataGridController.getDataGridPanel());
		setSize(frameWidth,frameHeight);
		setTitle(dataGridTitle);
		
		this.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		    	inventoryDataGridController.close();
		    }
		});
		
		JPanel southPanel= new JPanel();
		//(int top,int left,int bottom,int right) 
		southPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,50));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
				
		statusLabel=new JLabel();
		statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		statusLabel.setFont(GUIUtils.BOLD_SYSTEM_FONT);
		southPanel.add(statusLabel);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setLayout(new BoxLayout( tempPanel, BoxLayout.Y_AXIS));
		JScrollPane filterScrollPane = new JScrollPane(tempPanel);
		filterScrollPane.setBorder(BorderFactory.createEmptyBorder() );
		filterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		filterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		selectableErrorTextArea = new JTextArea("", 3, 15);
		selectableErrorTextArea.setOpaque(false);
		selectableErrorTextArea.setEditable(false);
		selectableErrorTextArea.setBackground(null);
		selectableErrorTextArea.setBorder(null);
		selectableErrorTextArea.setForeground(Color.red);
		selectableErrorTextArea.setFont(GUIUtils.BOLD_SYSTEM_FONT);
		tempPanel.add(selectableErrorTextArea);
		southPanel.add(filterScrollPane);		
		
		this.add(southPanel);
	}
	
	public JLabel getStatusLabel() {
		return statusLabel;
	}
	
	public void setErrorMessage(String message) {
		if (message==null || message.isEmpty()) {
			selectableErrorTextArea.setText("");
		}
		else {
			selectableErrorTextArea.setText("Selected row message:\n"+message);
			selectableErrorTextArea.setCaretPosition(0);
		}
	}

	public String getDataGridTitle() {
		return dataGridTitle;
	}

}
