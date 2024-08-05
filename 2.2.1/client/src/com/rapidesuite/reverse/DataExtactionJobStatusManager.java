/**************************************************
 * $Revision: 54560 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-04-21 13:45:36 +0700 (Thu, 21 Apr 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/DataExtactionJobStatusManager.java $:
 * $Id: DataExtactionJobStatusManager.java 54560 2016-04-21 06:45:36Z jannarong.wadthong $:
 */
package com.rapidesuite.reverse;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.JobManager;
import com.rapidesuite.client.common.JobStatusManager;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.reverse.gui.DataExtractionPanel;
import com.rapidesuite.reverse.utils.DataExtractionUtils;

public class DataExtactionJobStatusManager extends  JobStatusManager
{

	public DataExtactionJobStatusManager(JobManager executionJobManager,List<Job> executionJobs){
		super(executionJobManager,executionJobs);
	}

	@Override
	public synchronized void postExecution() {
		if (getPendingExecutionJobsCount()==0) {
			((DataExtractionJobManager)executionJobManager).zipOutputFoldersAndCreateReverseOutputDetails(startTimeInMS);
			refreshExecutionStatusMessage();
		}
		DataExtractionPanel extractionPanel = ((ReverseMain)executionJobManager.getSwiftGUIMain()).getDataExtractionPanel();
		extractionPanel.unlockAll();
		DataExtractionUtils.exportReverse_tree(extractionPanel, UtilsConstants.TEMP_XLSX_REVERSE_TREE_ALL_STATUSES_FILE_NAME, false);
		DataExtractionUtils.exportReverse_tree(extractionPanel, UtilsConstants.TEMP_XLSX_REVERSE_TREE_ERROR_STATUSES_FILE_NAME, true);
	}
	
	public synchronized void refreshExecutionStatusMessage() {
		String message = generateStatusMessage();
		
		if (getPendingExecutionJobsCount()==0) {
			updateStatus();		
		}
		else {
			GUIUtils.showInProgressMessage(((ReverseMain)executionJobManager.getSwiftGUIMain()).getDataExtractionPanel().getStatusLabel(),message);
            ((DataExtractionJobManager)executionJobManager).updateStatusMessage(startTimeInMS);
		}
	}

    public String generateStatusMessage()
    {
    	String additionalMsg = "";
    	String executionMsg ="";
    	Map<Integer, ExecutionStatusTreeTableNode> executionJobIdToTableNodeMap = ((ReverseMain)executionJobManager.getSwiftGUIMain()).getDataExtractionPanel().getExecutionStatusPanel().getExecutionJobIdToTableNodeMap();
    	for(Entry<Integer, Job> job : this.executionJobManager.getSwiftGUIMain().getExecutionJobManager().getTasksMap().entrySet())
    	{
    		String jobStatusMessage = executionJobIdToTableNodeMap.get(job.getKey()).getStatus();
    		
    		if(jobStatusMessage.contains("lost connection...")) {
    			additionalMsg="  Connection lost, please wait while the application is trying to reconnect.";
    			break;
    		}
    	}
    	executionMsg = Utils.getExecutionTime(startTimeInMS,System.currentTimeMillis())+additionalMsg;
        return getPendingExecutionJobsCount()+" Tasks remaining ( Total: "+executionJobsTotalCounter+" ) ,  execution time: "+executionMsg;
    }

	private void updateStatus() {
		if (!hasExecutionJobErrors && !executionJobManager.isManualStopped()) {
			((ReverseMain)executionJobManager.getSwiftGUIMain()).setExecutionCompleted("Extraction completed. Execution time: "+Utils.getExecutionTime(startTimeInMS,System.currentTimeMillis()));
		}
		else
			if (hasExecutionJobErrors){
				GUIUtils.showErrorMessage(((ReverseMain)executionJobManager.getSwiftGUIMain()).getDataExtractionPanel().getStatusLabel(),
						"Extraction completed with errors."+
						" Execution time: "+
						Utils.getExecutionTime(startTimeInMS,System.currentTimeMillis()));
			}
	}
	
}