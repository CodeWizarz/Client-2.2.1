/**************************************************
 * $Revision: 42538 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-07-30 14:03:54 +0700 (Wed, 30 Jul 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/JobStatusManager.java $:
 * $Id: JobStatusManager.java 42538 2014-07-30 07:03:54Z fajrian.yunus $:
 */
package com.rapidesuite.client.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.utility.CoreUtil;

public abstract class JobStatusManager implements Runnable
{

	protected JobManager executionJobManager;
	private Set<Integer> executionJobIdSet;
	protected int executionJobsTotalCounter;
	protected boolean hasExecutionJobErrors;
	protected long startTimeInMS;
	
	public JobStatusManager(JobManager executionJobManager,List<Job> executionJobs){
		this.executionJobManager=executionJobManager;
		this.executionJobsTotalCounter=executionJobs.size();
		hasExecutionJobErrors=false;
		executionJobIdSet=Collections.synchronizedSet(new HashSet<Integer>());
		for (Job job:executionJobs) {
			executionJobIdSet.add(job.getJobId());
		}
	}

	public int getPendingExecutionJobsCount() {
		return executionJobIdSet.size();
	}

	@Override
	public void run() {
		try{
			startTimeInMS=System.currentTimeMillis();
			while (! (getPendingExecutionJobsCount()<=0) ) {
				executionJobManager.getSwiftGUIMain().getExecutionPanel().refreshTable();
				if (!executionJobManager.isManualStopped() ) {
					refreshExecutionStatusMessage();
				}
				com.rapidesuite.client.common.util.Utils.sleep(2000);
			}
			executionJobManager.getSwiftGUIMain().getExecutionPanel().refreshTable();
			postExecution();
		}
		catch(Throwable tr) {
			FileUtils.printStackTrace(tr);
			GUIUtils.popupErrorMessage("ERROR: "+CoreUtil.getAllThrowableMessages(tr));
		}
	}
		
	public abstract void postExecution();
	public abstract void refreshExecutionStatusMessage();

	public synchronized void updateExecutionStatus(Job executionJob,String status) {
		if (executionJobIdSet.contains(executionJob.getJobId())) {
			executionJobIdSet.add(executionJob.getJobId());
		}
		executionJobManager.getSwiftGUIMain().getExecutionPanel().updateExecutionStatus( executionJob,status);
	}
		
	public synchronized void removeExecutionJob(int executionJobId,boolean hasErrors) {
		executionJobIdSet.remove(executionJobId);
		
		if (!hasExecutionJobErrors && hasErrors) {
			hasExecutionJobErrors=true;
		}
	}
	
	public synchronized boolean hasJobId(int jobId) {
		return executionJobIdSet.contains(jobId);
	}	
	
}