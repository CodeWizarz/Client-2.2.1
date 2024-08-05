/**************************************************
 * $Revision: 59896 $:
 * $Author: hassan.jamil $:
 * $Date: 2016-11-30 16:41:32 +0700 (Wed, 30 Nov 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/JobManager.java $:
 * $Id: JobManager.java 59896 2016-11-30 09:41:32Z hassan.jamil $:
 */
package com.rapidesuite.client.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public abstract class JobManager
{
	
	protected SwiftGUIMain swiftGUIMain;
	private ExecutorService threadExecutor;
	private boolean IS_MANUAL_STOPPED;
	protected JobStatusManager executionWorkerStatusManager; 
	private Map<Integer,Job> taskIdToExecutionJobMap;
	
	public JobManager(SwiftGUIMain swiftGUIMain)
	{
		this.swiftGUIMain=swiftGUIMain;
	}

	public abstract void execute(List<InventoryTreeNode> inventoryTreeNodes) throws Exception;
	
	public Map<Integer, Job> getTasksMap() {
		return taskIdToExecutionJobMap;
	}

	public void manualStop() {
		IS_MANUAL_STOPPED=true;
		Runnable r=new Runnable(){
    		public void run(){
    			Iterator<Integer> iterator=taskIdToExecutionJobMap.keySet().iterator();
    			while (iterator.hasNext()) {
    				Job executionJob=taskIdToExecutionJobMap.get(iterator.next());
    				executionJob.forceStopExecution();
    			}
    		}
    	};
    	Thread t=new Thread(r);
    	t.start();
	}
		
	public boolean isManualStopped(){
		return IS_MANUAL_STOPPED;
	}
		
	public boolean hasRemainingTasks() {
		return executionWorkerStatusManager.getPendingExecutionJobsCount()!=0;
	}
	
	public void updateExecutionStatus(Job task,String status) {
		executionWorkerStatusManager.updateExecutionStatus(task,status);
	}
	
	public void setTaskComplete(int taskId,boolean hasErrors){
		executionWorkerStatusManager.removeExecutionJob(taskId,hasErrors);
	}
	
	public SwiftGUIMain getSwiftGUIMain() {
		return swiftGUIMain;
	}

	public JobStatusManager getStatusWorker() {
		return executionWorkerStatusManager;
	}
	
	public void startExecution(final List<Job> executionJobs,JobStatusManager jobStatusManager,final int workersCount) throws Exception {
		taskIdToExecutionJobMap=new HashMap<Integer,Job>();
		IS_MANUAL_STOPPED=false;
		if (workersCount<=0) {
			throw new Exception("The number of workers must be greater or equal than 1");
		}
		this.executionWorkerStatusManager=jobStatusManager;
		new Thread(new Runnable() {

			@Override
			public void run() {
				threadExecutor = Executors.newFixedThreadPool( workersCount );
				for (Job executionJob:executionJobs) {
					taskIdToExecutionJobMap.put(executionJob.getJobId(), executionJob);
				}
				new Thread(executionWorkerStatusManager).start();
				for (Job executionJob:executionJobs) {
					threadExecutor.execute(executionJob);
				}
				threadExecutor.shutdown();
				boolean result = false;
				try {
					result = threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					FileUtils.printStackTrace(e);
					e.printStackTrace();
				}
			    if (!result) {
			    	GUIUtils.popupErrorMessage("It took more than "+Long.MAX_VALUE+" hour for the executor to stop, this shouldn't be the normal behaviour.");
			    }
			    //executionWorkerStatusManager.postExecution();
//				GUIUtils.popupInformationMessage("All jobs are successfully completed");
				
			}
			
		}).start();
	}

}