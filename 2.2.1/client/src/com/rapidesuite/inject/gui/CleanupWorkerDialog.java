package com.rapidesuite.inject.gui;

import java.util.List;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.selenium.SeleniumWorker;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class CleanupWorkerDialog extends SnapshotSwingWorker {

	private ScriptManager scriptManager;
	private int CURRENT_STEP_COUNTER;
	
	public CleanupWorkerDialog(ScriptManager scriptManager) {
		super(true);
		this.scriptManager=scriptManager;
		CURRENT_STEP_COUNTER=0;
		super.setTotalSteps(2);
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	private void processAction() {
		try{		
			super.updateExecutionLabels("Cleaning up, please wait...",CURRENT_STEP_COUNTER);
			List<Worker> workerList=scriptManager.getWorkersList();
			int index=0;
			for (Worker worker:workerList) {
				worker.stopExecution(true);
				index++;
				super.updateExecutionLabels("Cleanup Signal sent to "+index+" / "+workerList.size()+" Nodes...",CURRENT_STEP_COUNTER++);
			}
			
			super.updateExecutionLabels("Waiting for All Nodes to close Firefox...",CURRENT_STEP_COUNTER++);
			while (true) {
				index=0;
				boolean hasNoMoreNodesToCleanup=true;
				String batchVNCDisplayName="";
				for (Worker worker:workerList) {
					if (worker instanceof SeleniumWorker) {
						SeleniumWorker seleniumWorker=(SeleniumWorker)worker;
						batchVNCDisplayName=seleniumWorker.getBatchInjectionTracker().getBatchVNCDisplayName();
						if (seleniumWorker.getBatchInjectionTracker().isStarted() && !seleniumWorker.isWebDriverQuit()
								&& batchVNCDisplayName!=null
								) {
							hasNoMoreNodesToCleanup=false;
							index++;
						}
					}
				}
				if (hasNoMoreNodesToCleanup) {
					break;
				}
				super.updateExecutionLabels("Waiting for "+index+" Firefox instances to close (Display name: '"+batchVNCDisplayName+"') ...",CURRENT_STEP_COUNTER);
				Thread.sleep(1000);
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}

}