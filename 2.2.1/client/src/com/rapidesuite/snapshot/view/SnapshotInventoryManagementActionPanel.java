package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class SnapshotInventoryManagementActionPanel  extends SnapshotInventoryGridActionPanel {

	private SnapshotInventoryManagementFrame snapshotInventoryManagementFrame;
	private JButton saveButton;
	private JButton closeButton;

	public SnapshotInventoryManagementActionPanel(SnapshotInventoryManagementFrame snapshotInventoryManagementFrame) {
		this.snapshotInventoryManagementFrame=snapshotInventoryManagementFrame;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		createComponents();
	}

	private void createComponents() {
		ImageIcon ii=null;
		URL iconURL =null;

		iconURL = this.getClass().getResource("/images/snapshot/button_save.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveButton = new JButton();
		saveButton.setIcon(ii);
		saveButton.setBorderPainted(false);
		saveButton.setContentAreaFilled(false);
		saveButton.setFocusPainted(false);
		saveButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveButton.setRolloverIcon(new RolloverIcon(ii));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionSave();
			}
		}
				);
		add(saveButton);
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
				snapshotInventoryManagementFrame.closeWindow();
			}
		}
				);
		add(closeButton);
	}
	protected void processActionSave() {
		try{
			List<SnapshotInventoryGridRecord> selectedInventoryRecordList = snapshotInventoryManagementFrame.getSnapshotInventoryManagementGridPanel().getSelectedSnapshotInventoryGridRecordsList();
			JTable table = snapshotInventoryManagementFrame.getSnapshotInventoryManagementGridPanel().getTable();
			TableModel model = table.getModel();
			int numRows = model.getRowCount();
			int colExecutableIndex=table.getColumnModel().getColumnIndex(SnapshotInventoryManagementGridPanel.COLUMN_HEADING_EXECUTABLE);
			int colListableIndex=table.getColumnModel().getColumnIndex(SnapshotInventoryManagementGridPanel.COLUMN_HEADING_LISTABLE);
			int colInvIndex=table.getColumnModel().getColumnIndex(SnapshotInventoryManagementGridPanel.COLUMN_HEADING_INVENTORY_NAME);	
			List<SnapshotInventoryGridRecord> modifiedInventoryRecordList = new ArrayList<SnapshotInventoryGridRecord>();
			for(SnapshotInventoryGridRecord obj : selectedInventoryRecordList){
				for (int i=0; i < numRows; i++){
					String inventoryName =(String) model.getValueAt(i,colInvIndex);
					if(inventoryName.equals(obj.getInventoryName())){
						boolean isExecutable =(boolean) model.getValueAt(i,colExecutableIndex);
						boolean isListable =(boolean) model.getValueAt(i,colListableIndex);
						boolean previousIsExecutable = obj.isExecutable();
						boolean previousIsListable = obj.isListable();
						// check obj.getTableId()==-1, it means this inventories need to insert to INVENTORY
						if(isAnythingChangeInRecord(isExecutable,previousIsExecutable,isListable,previousIsListable) || obj.getTableId()==-1){
							SnapshotInventoryGridRecord modifiedRecord = new SnapshotInventoryGridRecord(obj.getInventoryName());
							modifiedRecord.setTableId(obj.getTableId());
							modifiedRecord.setExecutable(isExecutable);
							modifiedRecord.setListable(isListable);	
							modifiedInventoryRecordList.add(modifiedRecord);
						}					
						break;
					}
				}
			}
			int totalUpdatedRecord = saveInventoryChangeToDatabase(modifiedInventoryRecordList);
			GUIUtils.popupInformationMessage(formatNumberWithComma(totalUpdatedRecord)+" inventories have been updated.");
			refreshAndFetchInventory();						
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}

	
	public boolean isAnythingChangeInRecord(boolean currentIsExecutable, boolean previousIsExecutable, boolean currentIsListable, boolean previousIsListable){
		if(currentIsExecutable==previousIsExecutable 
				&& currentIsListable==previousIsListable){
			return false;
		}
		return true;
		
	}
	public int saveInventoryChangeToDatabase(List<SnapshotInventoryGridRecord> modifiedInventoryRecordList) throws Exception{
		List<SnapshotInventoryGridRecord> updateInventoryRecordList = new ArrayList<SnapshotInventoryGridRecord>();
		List<SnapshotInventoryGridRecord> insertInventoryRecordList = new ArrayList<SnapshotInventoryGridRecord>();
		int totalUpdatedRow =0;
		int totalInsertedRow =0;
		try{			
			for(SnapshotInventoryGridRecord obj : modifiedInventoryRecordList){
				if(obj.getTableId()==-1){
					insertInventoryRecordList.add(obj);
				}else{
					updateInventoryRecordList.add(obj);
				}
			}
			try{
				if(insertInventoryRecordList.size()>0){
					totalInsertedRow = ModelUtils.insertNewInventoryToDB(ModelUtils.getConnection(snapshotInventoryManagementFrame.getTabSnapshotsPanel()),insertInventoryRecordList);
				}
				if(updateInventoryRecordList.size()>0){
					totalUpdatedRow = ModelUtils.executeUpdateInventoryDetail(ModelUtils.getConnection(snapshotInventoryManagementFrame.getTabSnapshotsPanel()),updateInventoryRecordList);
				}				
			}catch(Exception e){
				FileUtils.printStackTrace(e);
				throw e;
			}
			
			return totalInsertedRow+totalUpdatedRow;
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Unable to update inventories, error :  "+e.getMessage());
		}	
	}
	
	private void refreshAndFetchInventory() {
		try{
			List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList=new ArrayList<SnapshotInventoryGridRecord>();
			snapshotInventoryGridRecordList = ModelUtils.getSnapshotInventoryGridRecordListToDisplay(snapshotInventoryManagementFrame.getTabSnapshotsPanel());
			snapshotInventoryManagementFrame.getSnapshotInventoryManagementGridPanel().displayInventories(snapshotInventoryGridRecordList);	
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}

	public SnapshotInventoryManagementFrame getSnapshotInventoryManagementFrame() {
		return snapshotInventoryManagementFrame;
	}

	@Override
	public JButton getExecutionButton() {
		return null;
	}
	public static String formatNumberWithComma(int number)throws Exception {
		DecimalFormat myFormatter = new DecimalFormat("###,###,###,###");
		return myFormatter.format(number);
   }
}
