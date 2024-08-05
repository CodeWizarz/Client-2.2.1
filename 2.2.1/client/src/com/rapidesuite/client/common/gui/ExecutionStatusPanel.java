/**************************************************
 * $Revision: 54554 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-04-21 11:29:52 +0700 (Thu, 21 Apr 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/ExecutionStatusPanel.java $:
 * $Id: ExecutionStatusPanel.java 54554 2016-04-21 04:29:52Z jannarong.wadthong $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

import com.rapidesuite.client.common.Job;

@SuppressWarnings("serial")
public abstract class ExecutionStatusPanel extends JPanel
{

	protected JPanel northPanel;
	protected JPanel centerPanel;
	protected JPanel southPanel;
	protected JLabel selectedRowLabel;
	
	private JPanel centerAllStatusPanel;
	private JPanel centerErrorStatusPanel;
	
	protected ExecutionStatusTreeTable executionAllStatusTreeTable;
	protected ExecutionStatusTreeTableNode executionAllStatusTreeTableNode;
	protected TreeTableModel allStatusTreeTableModel;
	private ExecutionStatusTreeTableNodeFinder executionAllStatusTreeTableNodeFinder;
	
	protected ExecutionStatusTreeTable executionErrorStatusTreeTable;
	protected ExecutionStatusTreeTableNode executionErrorStatusTreeTableNode;
	protected TreeTableModel errorStatusTreeTableModel;
	
	protected ExecutionPanel executionPanel;
	protected Map<Integer,ExecutionStatusTreeTableNode> executionJobIdToTableNodeMap;
	
	private JCheckBox showErrorsOnlyCheckBox;
		
	public final static int JOB_ID_COLUMN_INDEX=0;
	public final static int PATH_COLUMN_INDEX=1;
	public final static int STATUS_COLUMN_INDEX=2;
	public final static int EXECUTION_TIME_COLUMN_INDEX=3;
	
	private CardLayout cardLayout;
	private JPanel cardComponent;
	
	public ExecutionStatusPanel(ExecutionPanel executionPanel)
	{
		this.executionPanel=executionPanel;
		createComponents();
	}

	public void createComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
		centerPanel = new JPanel();
		centerAllStatusPanel = new JPanel();
		centerAllStatusPanel.setLayout(new BorderLayout());
		centerErrorStatusPanel = new JPanel();
		southPanel = new JPanel();
		
		showErrorsOnlyCheckBox=new JCheckBox("Show errors only");
		showErrorsOnlyCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (executionAllStatusTreeTable==null) {
					return;
				}
				if (isErrorStatusType()) {
					showOnlyErrorTypeNodes();
					executionAllStatusTreeTableNodeFinder.lockAll();
				}
				else {
					showAllStatusPanel();
					executionAllStatusTreeTableNodeFinder.unlockAll();
				}
			}
		}
		);
		
		cardLayout = new CardLayout();
		cardComponent = new JPanel(cardLayout);
		centerPanel.add(cardComponent, BorderLayout.CENTER);
		
		cardComponent.add(centerAllStatusPanel, "allStatus"); 
		cardComponent.add(centerErrorStatusPanel, "errorStatus");
		cardLayout.show(cardComponent,"allStatus");
		
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.add(showErrorsOnlyCheckBox);
		northPanel.add(tempPanel);
		
		selectedRowLabel= new JLabel("Select a row to display its status.");
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		southPanel.add(selectedRowLabel);
		this.add(northPanel);
		this.add(centerPanel);
		this.add(southPanel);
	}
	
	public boolean isErrorStatusType()
	{
		return showErrorsOnlyCheckBox.isSelected();
	}
	
	public void updateExecutionStatus(Job executionJob,String status) {
		CheckTreeUtils.updateTask(allStatusTreeTableModel,executionJobIdToTableNodeMap.get(executionJob.getJobId()),status);
	}
	
	protected ExecutionStatusTreeTable getExecutionStatusTreeTable()
	{
		if (isErrorStatusType()) {
			return executionErrorStatusTreeTable;
		}
		return executionAllStatusTreeTable;
	}
	
	protected abstract void showOnlyErrorTypeNodes();
		
	public ExecutionPanel getExecutionPanel() {
		return executionPanel;
	}
	
	public void createExecutionAllStatusTreeTable(
			final ExecutionStatusTreeTable executionStatusTreeTable,
			ExecutionStatusTreeTableNode executionStatusTreeTableNode) 
	throws Exception {
		this.executionAllStatusTreeTable =executionStatusTreeTable;
		this.executionAllStatusTreeTableNode=executionStatusTreeTableNode;
		executionJobIdToTableNodeMap=executionStatusTreeTableNode.getTaskIdToNodeMap();
		initExecutionStatusTreeTable(executionStatusTreeTable);
		centerAllStatusPanel.removeAll(); 
		centerErrorStatusPanel.removeAll(); 
		centerAllStatusPanel.add(new JScrollPane(executionAllStatusTreeTable), BorderLayout.CENTER); 
		
		SortedSet<String> items =getSortedSet(executionStatusTreeTableNode);
		
		if (executionAllStatusTreeTableNodeFinder!=null) {
			northPanel.remove(executionAllStatusTreeTableNodeFinder);
		}
		executionAllStatusTreeTableNodeFinder=new ExecutionStatusTreeTableNodeFinder(executionStatusTreeTable,new ArrayList<String>(items));
		executionAllStatusTreeTableNodeFinder.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(executionAllStatusTreeTableNodeFinder);
	}
	
	public void resetPanel() 
	throws Exception {
		centerAllStatusPanel.removeAll(); 
		centerErrorStatusPanel.removeAll(); 
		if (executionAllStatusTreeTableNodeFinder!=null) {
			northPanel.remove(executionAllStatusTreeTableNodeFinder);
		}
		this.executionAllStatusTreeTable =null;
		this.executionAllStatusTreeTableNode =null;
	}

	public void initExecutionStatusTreeTable(final ExecutionStatusTreeTable executionStatusTreeTable) 
	throws Exception {
		executionStatusTreeTable.setShowGrid(true, true);
		executionStatusTreeTable.setColumnControlVisible(false);
		executionStatusTreeTable.getTableHeader().setReorderingAllowed(false);
		executionStatusTreeTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent event) {
						int rowIndex=executionStatusTreeTable.getSelectionModel().getLeadSelectionIndex();
						TreePath treePath=executionStatusTreeTable.getPathForRow(rowIndex);
						if (treePath==null) {
							return;
						}
						ExecutionStatusTreeTableNode node = (ExecutionStatusTreeTableNode)treePath.getLastPathComponent();
						String jobId=(String)executionStatusTreeTable.getTreeTableModel().getValueAt(node,ExecutionStatusPanel.JOB_ID_COLUMN_INDEX);	
						if (jobId!=null && !jobId.isEmpty()){
							String msg=(String)executionStatusTreeTable.getTreeTableModel().getValueAt(node,ExecutionStatusPanel.STATUS_COLUMN_INDEX);	
							msg=msg.replaceAll("\n","<br/>");
							selectedRowLabel.setText("<html>Selected message: "+msg+"");
						}
					}
				}
			);
	}
	
	public void clearSelectedRowLabel() {
		this.selectedRowLabel.setText("");
	}
	
	public void showAllStatusPanel()  {
		cardLayout.show(cardComponent,"allStatus");
	}
	
	public void showErrorStatusPanel()  {
		cardLayout.show(cardComponent,"errorStatus");
	}
	
	public void setErrorsOnlyCheckBox(boolean isEnabled)  {
		showErrorsOnlyCheckBox.setSelected(false);
		showErrorsOnlyCheckBox.setEnabled(isEnabled);
	}
	
	protected void createExecutionErrorStatusTreeTable(final ExecutionStatusTreeTable executionStatusTreeTable) 
	throws Exception
	{
		this.executionErrorStatusTreeTable=executionStatusTreeTable;
		initExecutionStatusTreeTable(executionErrorStatusTreeTable);
		centerErrorStatusPanel.add(new JScrollPane(executionErrorStatusTreeTable)); 
	}
	
	private SortedSet<String> getSortedSet(ExecutionStatusTreeTableNode executionStatusTreeTableNode) 
	{
		SortedSet<String> items = new TreeSet<String>();  
		List<ExecutionStatusTreeTableNode> children=executionStatusTreeTableNode.firstBreadthChildren();
		children.remove(0);
		for (ExecutionStatusTreeTableNode child:children){
			String nodeName=child.getName().toLowerCase();
			items.add(nodeName);
		}
		return items;
	}

	public ExecutionStatusTreeTable getExecutionAllStatusTreeTable() {
		return executionAllStatusTreeTable;
	}

	public ExecutionStatusTreeTableNode getExecutionAllStatusTreeTableNode() {
		return executionAllStatusTreeTableNode;
	}

	public ExecutionStatusTreeTableNode getExecutionErrorStatusTreeTableNode() {
		return executionErrorStatusTreeTableNode;
	}

	public void setExecutionAllStatusTreeTableNode(
			ExecutionStatusTreeTableNode executionAllStatusTreeTableNode) {
		this.executionAllStatusTreeTableNode = executionAllStatusTreeTableNode;
	}

	public void lockAll() {
		executionAllStatusTreeTableNodeFinder.lockAll();
	}

	public void unlockAll() {
		executionAllStatusTreeTableNodeFinder.unlockAll();
	}

	public JPanel getNorthPanel() {
		return northPanel;
	}

	public Map<Integer, ExecutionStatusTreeTableNode> getExecutionJobIdToTableNodeMap() {
		return executionJobIdToTableNodeMap;
	}
	
}