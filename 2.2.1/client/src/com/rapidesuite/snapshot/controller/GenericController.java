package com.rapidesuite.snapshot.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.snapshot.model.GenericWorker;

public abstract class GenericController {
	
	private boolean isExecutionStopped;
	private final Object lock = new Object();
	private long startTime;
	private int workersCount;
	private List<?> tasksList;
	private int currentTaskIndex;
	private List<GenericWorker> workersList;
	private Map<String, String> snapshotEnvironmentProperties;
	private boolean postExecutionCompleted;
	protected int CURRENT_STEP_COUNTER;
	protected int TOTAL_STEPS_COUNTER;
	private boolean isCancelled;

	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	
	public Object getNextTask()  {
		synchronized (lock) {
			if (currentTaskIndex >= tasksList.size() ) {
				return null;
			}
			Object object=tasksList.get(currentTaskIndex);
			currentTaskIndex++;
			return object;
		}
	}

	protected boolean isWorkerExecutionCompleted()  {
		if (workersList.isEmpty()) {
			return false;
		}
		for (GenericWorker genericWorker:workersList) {
			if (!genericWorker.isCompleted()) {
				return false;
			}
		}
		return true;
	}

	public int getTotalFailedTasks()  {
		int total=0;
		for (GenericWorker genericWorker:workersList) {
			total=total+genericWorker.getTotalFailedTasks();
		}
		return total;
	}
	
	public int getTotalCompletedTasks()  {
		int total=0;
		for (GenericWorker genericWorker:workersList) {
			total=total+genericWorker.getTotalCompletedTasks();
		}
		return total;
	}	
	
	public int getTotalTasks()  {
		return tasksList.size();
	}	

	public boolean isExecutionStopped() {
		return isExecutionStopped;
	}

	public void stopExecution() {
		isExecutionStopped=true;
		for (GenericWorker genericWorker:workersList) {
			genericWorker.abortConnection();
		}		
	}

	public void startExecution() {
		startTime=System.currentTimeMillis();
		workersList=Collections.synchronizedList(new ArrayList<GenericWorker>());
		Thread thread = new Thread(){
			public void run(){
				preExecution();
				for (int i=1;i<=workersCount;i++) {
					if (isExecutionStopped()) {
						break;
					}
					launchWorkerThread();
				}
				
			}
		};
		thread.start();
		monitorExecution();
	}

	protected void launchWorkerThread() {
		final GenericWorker genericWorker=getImplementedWorker();
		workersList.add(genericWorker);
		/* 
		 * JDBC APIs are synchronized, meaning the connection will be locking accross threads => DON'T SHARE THE CONNECTION
		 * instead let's start X threads each opening and keepin their own connections and processing Y jobs.
		 */		
		Thread thread = new Thread(){
			public void run(){
				genericWorker.startExecution();
			}
		};
		thread.start();	
	}	

	public abstract void preExecution();

	public abstract GenericWorker getImplementedWorker();


	public void monitorExecution() {
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					/*
					 * We need to wait because the list of workers may not be populated yet which will cause the isExecutionCompleted
					 * to return true!
					 */
					while (workersList.size() < workersCount) {
						Thread.sleep(1000);
					}
					while (!isWorkerExecutionCompleted()) {
						updateWhileExecution();
						Thread.sleep(500);
					}
					updateWhileExecution();
					postExecutionInternal();
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		};
		t.start();
	}
	
	public void postExecutionInternal() {
		postExecution();
		postExecutionCompleted=true;
	}

	public abstract void postExecution();

	public abstract void updateWhileExecution();

	public Map<String, String> getSnapshotEnvironmentProperties() {
		return snapshotEnvironmentProperties;
	}

	public void setWorkersCount(int workersCount) {
		this.workersCount = workersCount;
	}

	public void setSnapshotEnvironmentProperties(
			Map<String, String> snapshotEnvironmentProperties) {
		this.snapshotEnvironmentProperties = snapshotEnvironmentProperties;
	}

	public long getStartTime() {
		return startTime;
	}

	public List<?> getTasksList() {
		return tasksList;
	}

	public int getCurrentTaskIndex() {
		return currentTaskIndex;
	}

	public List<GenericWorker> getWorkersList() {
		return workersList;
	}

	public void setTasksList(List<?> tasksList) {
		this.tasksList = tasksList;
	}

	public boolean isExecutionCompleted() {
		return postExecutionCompleted;
	}
	
}
