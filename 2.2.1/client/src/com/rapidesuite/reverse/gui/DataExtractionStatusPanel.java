/**************************************************
 * $Revision: 54560 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-04-21 13:45:36 +0700 (Thu, 21 Apr 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/DataExtractionStatusPanel.java $:
 * $Id: DataExtractionStatusPanel.java 54560 2016-04-21 06:45:36Z jannarong.wadthong $:
 */

package com.rapidesuite.reverse.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.client.common.gui.ExecutionStatusPanel;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTable;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.gui.TreeTableModel;
import com.rapidesuite.client.common.gui.datagrid.DataGridUtils;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;

@SuppressWarnings("serial")
public class DataExtractionStatusPanel extends ExecutionStatusPanel
{

	private final int JOB_ID_COLUMN_WIDTH=60;
	private final int PATH_COLUMN_WIDTH=540;
	private final int STATUS_COLUMN_WIDTH=250;
	private final int EXECUTION_TIME_COLUMN_WIDTH=120;
	
	private ActionListener onExportTreeIsClicked = null;
	
	public DataExtractionStatusPanel(DataExtractionPanel reverseExecutionPanel)
	{
		super(reverseExecutionPanel);
		final JCheckBox openExportedTreeDocumentAfterExporting = new JCheckBox("Open After Exporting", false);
		JButton exportTreeButton=new JButton("Export Tree");
		this.onExportTreeIsClicked = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					if (e == null && StringUtils.isBlank(Config.getReverseExtractionStatusExportFileName())) {
						return;
					}
					if (executionAllStatusTreeTableNode==null) {
						return;
					}
					
					DataExtractionStatusTreeTableNode executionStatusTreeTableNode;
					if (!isErrorStatusType()) {
						executionStatusTreeTableNode=((DataExtractionStatusTreeTableNode)executionAllStatusTreeTableNode);
					}
					else {
						executionStatusTreeTableNode=((DataExtractionStatusTreeTableNode)executionErrorStatusTreeTableNode);
					}
					createReverse_tree_exportFile(e, openExportedTreeDocumentAfterExporting, executionStatusTreeTableNode);
				}
				catch (Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}				
			}
		};
		exportTreeButton.addActionListener(this.onExportTreeIsClicked);
		super.getNorthPanel().add(exportTreeButton);
		super.getNorthPanel().add(openExportedTreeDocumentAfterExporting);
	}
	
	public void createExecutionAllStatusTreeTable(ExecutionStatusTreeTableNode executionStatusTreeTableNode,List<String> columnNames) 
	throws Exception {
		super.allStatusTreeTableModel = new TreeTableModel(executionStatusTreeTableNode,columnNames,1);
		DataExtractionStatusTreeTable dataExtractionAllStatusTreeTable=new DataExtractionStatusTreeTable(super.allStatusTreeTableModel);
		super.createExecutionAllStatusTreeTable(dataExtractionAllStatusTreeTable,executionStatusTreeTableNode);
		super.executionErrorStatusTreeTable=null;
		super.executionErrorStatusTreeTableNode=null;
		setColumnWidths(super.executionAllStatusTreeTable);
		showAllStatusPanel();
	}
	public static  void createReverse_tree_exportFile(ActionEvent e, JCheckBox openExportedTreeDocumentAfterExporting, ExecutionStatusTreeTableNode executionStatusTreeTableNode) throws Exception {
		createReverse_tree_exportFile(e, openExportedTreeDocumentAfterExporting, executionStatusTreeTableNode, false, null);
	}
	public static  void createReverse_tree_exportFile(ActionEvent e, JCheckBox openExportedTreeDocumentAfterExporting, ExecutionStatusTreeTableNode executionStatusTreeTableNode, boolean isAutomaticSave, File xlsxFile) throws Exception {
		if (executionStatusTreeTableNode == null) {
			// if user click on "Open Ticket" and the current panel is at the step before DataExtractionPanel, 
			// at this point the program dosn't have any information of the status of data extraction
			return;
		}
		
		List<DataExtractionStatusTreeTableNode> allNodes = ((DataExtractionStatusTreeTableNode) executionStatusTreeTableNode).getAllCurrentChildrenList(0);
		
		List<String[]> rows=new ArrayList<String[]>();
		for (DataExtractionStatusTreeTableNode node:allNodes) {
			if (node.getNodePath()==null) {
				continue;
			}
			String[] row=new String[3];
			int indentation=node.getTempIndentationCount();
			String ind="-";
			for (int i=0;i<indentation;i++) {
				ind=ind+"   ";
			}
			String path=ind+node.getName();
			row[0]=path;
			row[1]=node.getStatus();
			row[2]=node.getExecutionTimeMessage();
			rows.add(row);
		}
		String[] headerRow=new String[3];
		headerRow[0]="Path";
		headerRow[1]="Status";
		headerRow[2]="Execution Time";
		rows.add(0,headerRow);
		
		String fileName = null;;
		if ( xlsxFile == null) {
			fileName ="Reverse_tree_export-"+DataExtractionFileUtils.generateDefualtDateStringForReverse_tree_export()+".xlsx";
		} 
		File toReturn = null;
		if (isAutomaticSave) {
			DataGridUtils.doCreateAndSaveXLSXExcelFile(rows, xlsxFile, false, false);
			return;
		}
		if (e == null) {
			toReturn = new File(Config.getReverseExtractionStatusExportFileName());
			DataGridUtils.doCreateAndSaveXLSXExcelFile(rows, toReturn, false, false);
			return;
		} 
			
		DataGridUtils.askAndCreateAndSaveXLSXExcelFile(rows,fileName,openExportedTreeDocumentAfterExporting.isSelected(), !openExportedTreeDocumentAfterExporting.isSelected());
	}
	
	private void setColumnWidths(ExecutionStatusTreeTable executionStatusTreeTable) 
	throws Exception {
		TableColumn column=executionStatusTreeTable.getTableHeader().getColumnModel().getColumn(PATH_COLUMN_INDEX);
		column.setPreferredWidth(PATH_COLUMN_WIDTH); 
		column=executionStatusTreeTable.getTableHeader().getColumnModel().getColumn(JOB_ID_COLUMN_INDEX);
		column.setPreferredWidth(JOB_ID_COLUMN_WIDTH); 
		column=executionStatusTreeTable.getTableHeader().getColumnModel().getColumn(STATUS_COLUMN_INDEX);
		column.setPreferredWidth(STATUS_COLUMN_WIDTH); 
		column=executionStatusTreeTable.getTableHeader().getColumnModel().getColumn(EXECUTION_TIME_COLUMN_INDEX);
		column.setPreferredWidth(EXECUTION_TIME_COLUMN_WIDTH); 
	}
	
	public void createExecutionErrorStatusTreeTable() 
	throws Exception {
		super.executionErrorStatusTreeTableNode=super.executionAllStatusTreeTableNode.createInstance(executionAllStatusTreeTableNode.getName(),
				executionAllStatusTreeTableNode.getTaskId(),executionAllStatusTreeTableNode.getNodePath());
		executionAllStatusTreeTableNode.makeShallowCopy(super.executionErrorStatusTreeTableNode);
		super.executionErrorStatusTreeTableNode.keepNodesWithStatusType(ExecutionStatusTreeTableNode.STATUS_ERROR);
		super.errorStatusTreeTableModel= new TreeTableModel(super.executionErrorStatusTreeTableNode,super.allStatusTreeTableModel.getColumnNames(),1);
		DataExtractionStatusTreeTable dataExtractionErrorStatusTreeTable=new DataExtractionStatusTreeTable(super.errorStatusTreeTableModel);
		super.createExecutionErrorStatusTreeTable(dataExtractionErrorStatusTreeTable);
		
		setColumnWidths(super.executionErrorStatusTreeTable);
	}
	
	@Override
	protected void showOnlyErrorTypeNodes() {
		 try {
			 if (super.executionErrorStatusTreeTableNode==null) {
				 createExecutionErrorStatusTreeTable();
			 }
			 showErrorStatusPanel();
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}
	
	public void simulateClickingExportTreeButton() {
		this.onExportTreeIsClicked.actionPerformed(null);
	}
	
}