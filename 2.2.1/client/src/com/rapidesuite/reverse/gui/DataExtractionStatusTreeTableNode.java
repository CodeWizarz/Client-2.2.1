package com.rapidesuite.reverse.gui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.rapidesuite.client.common.gui.ExecutionStatusPanel;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;

public class DataExtractionStatusTreeTableNode extends ExecutionStatusTreeTableNode
{

	private int tempIndentationCount;
	
	public DataExtractionStatusTreeTableNode(String name, int taskId,String nodePath,String initialStatus) {
		super(name,taskId,nodePath,initialStatus);
	}
	
	public int getColumnCount() {
		return 4;
	}

	public void setValueAt(Object aValue,int column) {
		if (aValue instanceof ExecutionStatusTreeTableNode) {
			return;
		}
		switch (column) {
		case ExecutionStatusPanel.PATH_COLUMN_INDEX: this.name=(String) aValue; break;
		case ExecutionStatusPanel.JOB_ID_COLUMN_INDEX: this.taskId=(Integer) aValue; break;
		case ExecutionStatusPanel.STATUS_COLUMN_INDEX: this.status=(String) aValue; break;
		case ExecutionStatusPanel.EXECUTION_TIME_COLUMN_INDEX: ;
		}
	}
	
	public Object getValueAt(int column) {
		switch (column) {
		case ExecutionStatusPanel.PATH_COLUMN_INDEX: return name;
		case ExecutionStatusPanel.JOB_ID_COLUMN_INDEX: 
			return  taskId==-1 ? "": String.valueOf(taskId);
		case ExecutionStatusPanel.STATUS_COLUMN_INDEX: return getStatus();
		case ExecutionStatusPanel.EXECUTION_TIME_COLUMN_INDEX: return getExecutionTimeMessage();
				
		default: return null;
		}
	}
	
	public DataExtractionStatusTreeTableNode createInstance(String name, int taskId,String nodePath) {
		return new DataExtractionStatusTreeTableNode(name,taskId,nodePath,status);
	}
	
	public List<DataExtractionStatusTreeTableNode> getAllCurrentChildrenList(int indentation) {
		this.tempIndentationCount=indentation;
		List<DataExtractionStatusTreeTableNode> toReturn=new ArrayList<DataExtractionStatusTreeTableNode>();
		toReturn.add(this);
		if (children==null) {
			return toReturn;
		}
		Enumeration<?> e = children();
		while (e.hasMoreElements()) {
			DataExtractionStatusTreeTableNode treeNode = (DataExtractionStatusTreeTableNode)e.nextElement();
			int newIndentation=indentation+1;
			List<DataExtractionStatusTreeTableNode> list=treeNode.getAllCurrentChildrenList(newIndentation);
			toReturn.addAll(list);
		}
		return toReturn;
	}
	
	public int getTempIndentationCount() {
		return tempIndentationCount;
	}
			
}