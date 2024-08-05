package com.rapidesuite.snapshot.model;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.driver.OracleConnection;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.controller.GenericController;

public abstract class GenericWorker {
	
	protected GenericController genericController;
	private boolean isCompleted;
	private Connection connection;
	private int totalFailedTasks;
	private int totalCompletedTasks;
	private boolean isStopExecutionOnManualStop;
	
	public GenericWorker(GenericController genericController,boolean isStopExecutionOnManualStop) {
		this.genericController=genericController;
		this.isStopExecutionOnManualStop=isStopExecutionOnManualStop;
		totalCompletedTasks=0;
	}
	
	public void startExecution() {
		try{
			isCompleted=false;
			while (true) {
				if (isStopExecutionOnManualStop && genericController.isExecutionStopped()) {
					break;
				}
				Object task=genericController.getNextTask();
				if (task==null) {
					break;
				}
				execute(task);
				totalCompletedTasks++;
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
			isCompleted=true;
		}
	}
	
	public Connection getJDBCConnection() throws Exception {
		if (connection==null || connection.isClosed()) {
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(genericController.getSnapshotEnvironmentProperties()),
					ModelUtils.getDBUserName(genericController.getSnapshotEnvironmentProperties()),
					ModelUtils.getDBPassword(genericController.getSnapshotEnvironmentProperties())
				);
		}
		return connection;
	}

	public Connection getJDBCConnectionNoRetry() throws Exception {
		if (connection==null || connection.isClosed()) {
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(genericController.getSnapshotEnvironmentProperties()),
					ModelUtils.getDBUserName(genericController.getSnapshotEnvironmentProperties()),
					ModelUtils.getDBPassword(genericController.getSnapshotEnvironmentProperties()),
					false
				);
		}
		return connection;
	}
	
	public Connection getJDBCConnectionNoRetry(boolean checkClosedConnection) throws Exception {
		if (connection==null || (checkClosedConnection && connection.isClosed())) {
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(genericController.getSnapshotEnvironmentProperties()),
					ModelUtils.getDBUserName(genericController.getSnapshotEnvironmentProperties()),
					ModelUtils.getDBPassword(genericController.getSnapshotEnvironmentProperties()),
					false
				);
		}
		return connection;
	}
	
	public abstract void execute(Object task);
	
	public boolean isCompleted() {
		return isCompleted;
	}

	public GenericController getGenericController() {
		return genericController;
	}

	public int getTotalFailedTasks() {
		return totalFailedTasks;
	}

	public void setTotalFailedTasks(int totalFailedTasks) {
		this.totalFailedTasks = totalFailedTasks;
	}

	public void abortConnection() {
		if (connection!=null) {
			try {
				OracleConnection oracleConnection = (OracleConnection) connection;
				oracleConnection.abort();
				//connection=null;
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int getTotalCompletedTasks() {
		return totalCompletedTasks;
	}

	
}
