/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/DataExtractionInventoryTreeNode.java $:
 * $Id: DataExtractionInventoryTreeNode.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
 */

package com.rapidesuite.reverse.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.reverse.ReverseMain;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;

@SuppressWarnings("serial")
public class DataExtractionInventoryTreeNode extends InventoryTreeNode  {

	private DataExtractionPanel dataExtractionPanel;
	private String sqlFileRevisionNumber;
	private SortedSet<File> sqlFiles;
	private boolean isInvalidNode;
	private boolean isUnreversible;
	private String inventoryFileRevisionNumber;
	private String inventoryType;

	public DataExtractionInventoryTreeNode(DataExtractionPanel dataExtractionPanel,String inventoryName,String nodePath) {
		super(inventoryName,nodePath);
		this.dataExtractionPanel=dataExtractionPanel;
	}

	public void setSQLFileNames(SortedSet<File> sqlFileNames) {
		this.sqlFiles=sqlFileNames;
	}

	public void initToolTipText() {
		StringBuffer buffer=new StringBuffer("");
		buffer.append("<html>");
		if (inventory!=null) {
	   	    inventoryFileRevisionNumber=FileUtils.getSubversionRevisionNumberFromInventoryFile(
	   	    		((ReverseMain)dataExtractionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().getInventoriesPackageFile(),
			    	archiveItemName);
	   	    buffer.append("Inventory revision number: ").append(inventoryFileRevisionNumber).append("<br>");

   	    	boolean isInstanceLevel=((ReverseMain)dataExtractionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().isInstanceLevel(getName());
			boolean isOULevel=((ReverseMain)dataExtractionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().isOperatingUnitLevel(getName());
			boolean isBGLevel=((ReverseMain)dataExtractionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().isBusinessGroupLevel(getName());
			boolean isOUAndBGLevels=isOULevel && isBGLevel;
			boolean isOULevelOnly=isOULevel && !isBGLevel;

   	    	if (isInstanceLevel || isOUAndBGLevels || isBGLevel) {
   	    		inventoryType="Instance level.";
   	    	}
   	    	else
   	    	if (isOULevelOnly) {
   	    		if (ReverseMain.IS_FUSION_DB) {
   	    			inventoryType="Business Unit specific.";
   	    		}
   	    		else {
   	    			inventoryType="Operating Unit specific.";
   	    		}
   	    	}
   	    	else
   	    	if (isUnreversible) {
   	    		inventoryType="Unreversible.";
   	    	}
   	    	else {
   	    		inventoryType="Inventory type not detected.";
   	    	}
   	    	buffer.append(inventoryType).append("<br>");
   	    	initToolTipTextSQLSVN(buffer,dataExtractionPanel);
		}
		else {
			buffer.append("This node contains "+getInventoriesCount()+" inventories.").append("<br>");
	   	    buffer.append("</html>");
	   	    tooltipText=buffer.toString();
		}
	    buffer.append("</html>");
	    tooltipText=buffer.toString();
	}

	public void initToolTipTextSQLSVN(StringBuffer buffer,DataExtractionPanel dataExtractionPanel) {
		if (sqlFiles!=null && sqlFiles.size()>=1 && !isUnreversible) {
			try {
				File sqlFile = DataExtractionFileUtils.getSQLFile(sqlFiles.iterator().next());
				String sqlFileName=sqlFile.getAbsolutePath();
				sqlFileRevisionNumber=FileUtils.getSubversionRevisionNumberFromSQLFile(sqlFile);
				if (sqlFileRevisionNumber.isEmpty()) {
					sqlFileRevisionNumber="N/A";
				}
				buffer.append("SQL file: ").append(sqlFileName).append("<br>");
				buffer.append("SQL file revision number: ").append(sqlFileRevisionNumber).append("<br>");
			}
			catch (Exception e) {
				FileUtils.printStackTrace(e);
			}
		}
		else {
			if (!isUnreversible) {
				buffer.append("No SQL file defined.").append("<br>");
			}
		}
	}

	public void setErrorOnMissingSQLFiles() {
		if (inventory!= null){
			isInvalidNode= !this.isUnreversible() && (sqlFiles==null || sqlFiles.isEmpty());
			if ( isInvalidNode )
			{
			    FileUtils.println("INVALID TABLE CONFIG: " + inventory.getName() + " sqlFiles = " + sqlFiles + ", isUnreversible = " + this.isUnreversible());
			}
		}
		if (children != null){
			Enumeration<?> e = children.elements();
			while (e.hasMoreElements()) {
				DataExtractionInventoryTreeNode inventoryTreeNode = (DataExtractionInventoryTreeNode)e.nextElement();
				inventoryTreeNode.setErrorOnMissingSQLFiles();
			}
		}
		if (inventory==null){
			isInvalidNode=hasAtLeastOneInvalidChild();
		}
	}

	public boolean hasAtLeastOneInvalidChild() {
		if (children == null){
			return isInvalidNode();
		}
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			DataExtractionInventoryTreeNode inventoryTreeNode = (DataExtractionInventoryTreeNode)e.nextElement();
			if (inventoryTreeNode.isInvalidNode() || inventoryTreeNode.hasAtLeastOneInvalidChild() ) {
				return true;
			}
		}
		return false;
	}

	public boolean isInvalidNode() {
		return isInvalidNode;
	}

	public void removeInventoryNodesMatchingInMap(Map<String,Boolean> inventoryNamesMap) {
		if (children == null){
			Boolean isFound=inventoryNamesMap.get(getName());
			if (isFound!=null) {
				isNodeToRemove=true;
			}
			return;
		}

		Enumeration<?> e = children.elements();
		List<InventoryTreeNode> nodesToRemoveList=new ArrayList<InventoryTreeNode>();
		while (e.hasMoreElements()) {
			DataExtractionInventoryTreeNode inventoryTreeNode = (DataExtractionInventoryTreeNode)e.nextElement();
			inventoryTreeNode.removeInventoryNodesMatchingInMap(inventoryNamesMap);

			if (inventoryTreeNode.isNodeToRemove) {
				nodesToRemoveList.add(inventoryTreeNode);
				continue;
			}

		}
		for (InventoryTreeNode inventoryTreeNode:nodesToRemoveList) {
			this.remove(inventoryTreeNode);
		}
		if (super.getChildCount()==0) {
			isNodeToRemove=true;
		}
	}

	public void removeInventoryNodesUnreversible() {
		if (children == null){
			if (isUnreversible) {
				isNodeToRemove=true;
			}
			return;
		}

		Enumeration<?> e = children.elements();
		List<InventoryTreeNode> nodesToRemoveList=new ArrayList<InventoryTreeNode>();
		while (e.hasMoreElements()) {
			DataExtractionInventoryTreeNode inventoryTreeNode = (DataExtractionInventoryTreeNode)e.nextElement();
			inventoryTreeNode.removeInventoryNodesUnreversible();

			if (inventoryTreeNode.isNodeToRemove) {
				nodesToRemoveList.add(inventoryTreeNode);
				continue;
			}

		}
		for (InventoryTreeNode inventoryTreeNode:nodesToRemoveList) {
			this.remove(inventoryTreeNode);
		}
		if (super.getChildCount()==0 && this.getInventory()==null) {
			isNodeToRemove=true;
		}
	}

	public boolean isUnreversible() {
		return isUnreversible;
	}

	public void setUnreversible(boolean isUnreversible) {
		this.isUnreversible = isUnreversible;
	}

	public InventoryTreeNode getShallowCopy() {
		DataExtractionInventoryTreeNode toReturn=new DataExtractionInventoryTreeNode(dataExtractionPanel,this.getName(),this.getNodePath());
		toReturn.setSelected(this.isSelected());
		toReturn.setArchiveItemName(this.getArchiveItemName());
		toReturn.setUnreversible(this.isUnreversible());
		return toReturn;
	}

}


