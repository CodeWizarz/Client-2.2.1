package com.rapidesuite.client.common.gui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.util.Utils;

public abstract class ExecutionStatusTreeTableNode extends AbstractMutableTreeTableNode
{

	protected Job executionJob;
	protected String name;
	protected int taskId;
	protected String status;
	private String nodePath;
	
	public static final String STATUS_DONE="DONE";
	public static final String STATUS_SUCCESS="SUCCESS";
	public static final String STATUS_ERROR="ERROR";
	public static final String STATUS_WARNING="WARNING";
	public static final String STATUS_PROCESSING="PROCESSING";
	public static final String STATUS_PENDING="PENDING";
	public static final String STATUS_UNPROCESSED="UNPROCESSED";
	public static final String STATUS_RETRY="RETRY";

	public ExecutionStatusTreeTableNode(String name,int taskId,String nodePath,String initialStatus) {
		this.name=name;
		this.taskId=taskId;
		this.status=initialStatus;
		this.nodePath=nodePath;
	}
			
	public String getName() {
		return (String) getValueAt(ExecutionStatusPanel.PATH_COLUMN_INDEX);
	}

	public Job getExecutionJob() {
		return executionJob;
	}
	
	public void setExecutionJob(Map<Integer,Job> taskIdToExecutionWorkerMap) {
		if (taskId!=-1) {
			this.executionJob = taskIdToExecutionWorkerMap.get(taskId);
		}
		else {
			Enumeration<?> children=children();
			while (children.hasMoreElements()) {
				ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
				child.setExecutionJob(taskIdToExecutionWorkerMap);
			}
		}
	}
	
	public Map<Integer,ExecutionStatusTreeTableNode> getTaskIdToNodeMap() {
		Map<Integer,ExecutionStatusTreeTableNode> toReturn=new HashMap<Integer,ExecutionStatusTreeTableNode>();
		if (taskId!=-1) {
			toReturn.put(taskId,this);
		}
		else {
			Enumeration<?> children=children();
			while (children.hasMoreElements()) {
				ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
				Map<Integer,ExecutionStatusTreeTableNode> temp=child.getTaskIdToNodeMap();
				toReturn.putAll(temp);
			}
		}
		return toReturn;
	}
		
	public String getExecutionTimeMessage() {
		if (status==null || status.startsWith(STATUS_UNPROCESSED) || getTaskId()==-1 || getExecutionJob()==null) {
			return "";
		}
		return Utils.getExecutionTime(getExecutionJob().getExecutionTime());
	}
		
	public boolean hasLeaves() {
		Enumeration<?> children=children();
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			if (child.getTaskId()!=-1) {
				return true;
			}
		}
		return false;
	}
		
	public boolean hasError() {
		return hasState(STATUS_ERROR);
	}
	
	public boolean hasPending() {
		return hasState(STATUS_PENDING);
	}
	
	public boolean hasWarning() {
		return hasState(STATUS_WARNING);
	}
	
	public boolean hasSuccess() {
		return hasState(STATUS_SUCCESS);
	}
	
	public boolean hasInProcess() {
		return hasState(STATUS_PROCESSING);
	}
	
	private boolean hasState(String state) {
		if (taskId!=-1) {
			String status=(String) getValueAt(ExecutionStatusPanel.STATUS_COLUMN_INDEX);
			return (status!=null && status.startsWith(state));
		}
		Enumeration<?> children=children();
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			if (child.hasState(state)) {
				return true;
			}
		}
		return false;
	}
		
	public int getAllTaskCount() {
		if (taskId!=-1) {
			return 1;
		}
		Enumeration<?> children=children();
		int total=0;
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			total=total+child.getAllTaskCount();
		}
		return total;
	}
	
	public int getCompletedTaskCount() {
		if (taskId!=-1) {
			if (getExecutionJob()!=null && getExecutionJob().isJobComplete())  {
				return 1;
			}
			return 0;
		}
		Enumeration<?> children=children();
		int total=0;
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			total=total+child.getCompletedTaskCount();
		}
		return total;
	}
	
	public int getErrorTaskCount() {
		if (taskId!=-1) {
			if (status!=null && status.startsWith(STATUS_ERROR))  {
				return 1;
			}
			return 0;
		}
		Enumeration<?> children=children();
		int total=0;
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			total=total+child.getErrorTaskCount();
		}
		return total;
	}
	
	private boolean hasAtLeastOneChildTaskStarted() {
		if (taskId!=-1) {
			if (getExecutionJob()==null)  {
				return false;
			}
			return getExecutionJob().isJobStarted();
		}
		Enumeration<?> children=children();
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			if (child.hasAtLeastOneChildTaskStarted()) {
				return true;
			}
		}
		return false;
	}
	
	public int getWarningTaskCount() {
		if (taskId!=-1) {
			if (status!=null && status.startsWith(STATUS_WARNING))  {
				return 1;
			}
			return 0;
		}
		Enumeration<?> children=children();
		int total=0;
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			total=total+child.getWarningTaskCount();
		}
		return total;
	}
	
	public int getUnprocessedTaskCount() {
		if (taskId!=-1) {
			if (status!=null && status.startsWith(STATUS_UNPROCESSED))  {
				return 1;
			}
			return 0;
		}
		Enumeration<?> children=children();
		int total=0;
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			total=total+child.getUnprocessedTaskCount();
		}
		return total;
	}
	
	public String getStatusType() {
		if (taskId == -1) {
			getStatus();
		}			
		int delimiterIndex = status.indexOf(':');
		if (delimiterIndex == -1) {
			return status;
		} else {
			return status.substring(0, delimiterIndex).trim();				
		}
	}
	
	public String getStatusDetails() {
		if (taskId == -1) {
			getStatus();
		}
		int delimiterIndex = status.indexOf(':');
		if (delimiterIndex == -1) {
			return "";
		} else {
			return status.substring(delimiterIndex+1).trim();		
		}		
	}
	
	public String getStatus() {
		if (taskId!=-1) {
			return status;
		}
		String message="";
		int allTaskCount=getAllTaskCount();
		int completedTaskCount=getCompletedTaskCount();
		int errorCount=getErrorTaskCount();
		int warningCount=getWarningTaskCount();
		int unprocessedCount=getUnprocessedTaskCount();
		
		if (unprocessedCount>0) {
			message=STATUS_UNPROCESSED+": "+unprocessedCount+" / "+allTaskCount+" Tasks. ";
		}
		else {
			if (allTaskCount==completedTaskCount) {
				message=STATUS_DONE+":";
			}
			else
				if (hasAtLeastOneChildTaskStarted()) {
					message=STATUS_PROCESSING+":";
				}
				else {
					message=STATUS_PENDING+":";
				}
			message=message+" "+completedTaskCount+" / "+allTaskCount+" Tasks. ";

			if (errorCount!=0) {
				message=message+" ( "+errorCount+" errors ) ";
			}
			else
				if (warningCount!=0) {
					message=message+" ( "+warningCount+" warnings ) ";
				}	
		}
		status=message;
		return message;
	}

	@Override
	public abstract int getColumnCount();
	
	@Override
	public abstract void setValueAt(Object aValue,int column);
	
	@Override
	public abstract Object getValueAt(int column);

	public int getTaskId() {
		return taskId;
	}
	
	public void keepNodesWithStatusType(String statusType)
	{
		List<ExecutionStatusTreeTableNode> children=getChildren();
		for (ExecutionStatusTreeTableNode child:children) {
			if (child.getStatus().toLowerCase().indexOf(statusType.toLowerCase())==-1) {
				remove(child);
			}
			else {
				child.keepNodesWithStatusType(statusType);
			}
		}
	}
	
	public List<ExecutionStatusTreeTableNode> getChildren() {
		List<ExecutionStatusTreeTableNode> toReturn=new ArrayList<ExecutionStatusTreeTableNode>();
		Enumeration<?> children=children();
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			toReturn.add(child);
		}
		return toReturn;
	}
	
	public List<ExecutionStatusTreeTableNode> firstBreadthChildren() {
		List<ExecutionStatusTreeTableNode> toReturn=new ArrayList<ExecutionStatusTreeTableNode>();
		if (this.children==null || this.children.isEmpty()) {
			toReturn.add(this);
			return toReturn;
		}
		toReturn.add(this);
		Enumeration<?> children=children();
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			toReturn.addAll(child.firstBreadthChildren());
		}
		return toReturn;
	}
	
	protected void copyFields(ExecutionStatusTreeTableNode node) {
		node.executionJob=executionJob;
		node.name=name;
		node.taskId=taskId;
		node.status=status;
	}
	
	public abstract ExecutionStatusTreeTableNode createInstance(String name, int taskId,String nodePath);
	
	public void makeShallowCopy(ExecutionStatusTreeTableNode targetNode) {
		if (children == null){
			return;				
		}
		Enumeration<?> children=children();
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode childTreeNode=(ExecutionStatusTreeTableNode) children.nextElement();
			ExecutionStatusTreeTableNode childTreeNodeCopy=createInstance(childTreeNode.getName(),childTreeNode.getTaskId(),
					childTreeNode.getNodePath());
			childTreeNode.copyFields(childTreeNodeCopy);
			targetNode.add(childTreeNodeCopy);
			childTreeNode.makeShallowCopy(childTreeNodeCopy);
		}
	}	

	public String getNodePath() {
		return nodePath;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

}