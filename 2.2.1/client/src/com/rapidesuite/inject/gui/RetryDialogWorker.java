package com.rapidesuite.inject.gui;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.ScriptManager;

public class RetryDialogWorker extends AbstractDialogWorker {

	private ScriptManager scriptManager;
	private ExecutionRetryPanel executionRetryPanel;
	private final int retryWaitingTimeInSecs=10;
	
	public RetryDialogWorker(ScriptManager scriptManager,ExecutionRetryPanel executionRetryPanel) {
		this.scriptManager=scriptManager;
		this.executionRetryPanel=executionRetryPanel;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	private void processAction() {
		try{		
			int currentCounter=0;
			boolean isStartRetry=false;
			executionRetryPanel.setTotalSteps(retryWaitingTimeInSecs);
			while (true) {
				if (executionRetryPanel.isCancelled()) {
					isStartRetry=false;
					break;
				}
				if (currentCounter==retryWaitingTimeInSecs) {
					isStartRetry=true;
					break;
				}
				String msg=(retryWaitingTimeInSecs-currentCounter)+" secs";
				executionRetryPanel.getStatusLabel().setText(msg);
				currentCounter++;
				executionRetryPanel.updateProgressBar(currentCounter);
				Thread.sleep(1000);
			}
			if (isStartRetry) {
				executionRetryPanel.getDialog().dispose();
				scriptManager.getInjectMain().getExecutionPanelUI().getExecutionTabPanel().startExecution();
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}

}