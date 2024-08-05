/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/Job.java $:
 * $Id: Job.java 31694 2013-03-04 06:33:20Z john.snell $:
 */
package com.rapidesuite.client.common;

import java.util.Map;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;

public abstract class Job implements Runnable
{

	protected JobManager executionJobManager;
	protected Map<String,String> environmentProperties;
	protected String inventoryFileName;
	protected int jobId;
	protected Inventory inventory;
	private boolean isJobStarted;
	private boolean isJobComplete;
	private long startTimeInMS;
	private long endTimeInMS;
		
	public Job(
			JobManager executionJobManager,
			int jobId,
			Map<String,String> environmentProperties,
			String inventoryFileName,
			Inventory inventory)
	{
		this.executionJobManager=executionJobManager;
		this.environmentProperties=environmentProperties;
		this.inventoryFileName=inventoryFileName;
		this.inventory=inventory;
		this.jobId=jobId;
		this.isJobStarted=false;
	}

	public String getInventoryFileName() {
		return inventoryFileName;
	}

	public int getJobId() {
		return jobId;
	}

	public boolean isManualStopped(){
		return executionJobManager.isManualStopped();
	}

	public boolean isJobStarted(){
		return isJobStarted;
	}

	public void setJobComplete(boolean hasErrors){
		isJobComplete=true;
		executionJobManager.setTaskComplete(jobId,hasErrors);
	}
	
	public boolean isJobComplete(){
		return isJobComplete;
	}
	
	public JobManager getExecutionJobManager() {
		return executionJobManager;
	}
	
	public Map<String, String> getBweProperties() {
		return environmentProperties;
	}
	
	public Inventory getInventory() {
		return inventory;
	}

	public long getExecutionTime() {
		if (startTimeInMS==0) {
			return 0;
		}
		if (isJobComplete()) {
			return endTimeInMS-startTimeInMS;
		}
		return System.currentTimeMillis()-startTimeInMS;
	}
	
	public long getStartTime() {
		return startTimeInMS;
	}
	
	public void run()
	{
		startTimeInMS=System.currentTimeMillis();
		isJobStarted=true;		
		try{	
			startExecution();
		}
		catch ( Throwable e ){
			FileUtils.printStackTrace(e);
			setJobComplete(true);
			executionJobManager.updateExecutionStatus(this,"ERROR: "+e.getMessage()); 
		}
		finally {
			endTimeInMS=System.currentTimeMillis();
			isJobComplete=true;
		}
	}
	
	public abstract void startExecution() throws Exception;
	
	public abstract void forceStopExecution();
	
}