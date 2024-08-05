package com.rapidesuite.snapshot.model;

import java.util.ArrayList;
import java.util.List;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.view.SnapshotInventoryManagementFrame;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;

public class InitSnapshotInventoryManagementSwingWorker extends SnapshotSwingWorker {

	private SnapshotInventoryManagementFrame snapshotInventoryManagementFrame;
	private int CURRENT_STEP_COUNTER;
	private int delayInMs;
	
	public InitSnapshotInventoryManagementSwingWorker(SnapshotInventoryManagementFrame snapshotInventoryManagementFrame,int delayInMs) {
		super(true);
		this.snapshotInventoryManagementFrame=snapshotInventoryManagementFrame;
		CURRENT_STEP_COUNTER=0;
		this.delayInMs=delayInMs;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	private void processAction() {
		try{
			super.updateExecutionLabels("Loading Inventories...",CURRENT_STEP_COUNTER++);
			if (delayInMs!=0) {
				Thread.sleep(delayInMs);
			}
			List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList=new ArrayList<SnapshotInventoryGridRecord>();
			snapshotInventoryGridRecordList = ModelUtils.getSnapshotInventoryGridRecordListToDisplay(snapshotInventoryManagementFrame.getTabSnapshotsPanel());
			snapshotInventoryManagementFrame.getSnapshotInventoryManagementGridPanel().displayInventories(snapshotInventoryGridRecordList);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}

}