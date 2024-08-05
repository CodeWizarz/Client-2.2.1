package com.rapidesuite.client.common.gui;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;

public class CheckTreeUtils{
	
    public static void mouseClicked(MouseEvent me,CheckTreeSelectionModel selectionModel,JTree tree,int hotspot,TreeSelectionListener treeSelectionListener){ 
        TreePath path = tree.getPathForLocation(me.getX(), me.getY()); 
        if(path==null) {
            return; 
        }
        if(me.getX()>tree.getPathBounds(path).x+hotspot) {
            return; 
        }
        boolean selected = selectionModel.isPathSelected(path, true); 
        selectionModel.removeTreeSelectionListener(treeSelectionListener); 
        try{ 
            if(selected) {
                selectionModel.removeSelectionPath(path); 
            }
            else {
                selectionModel.addSelectionPath(path); 
            }
        } 
        finally{ 
            selectionModel.addTreeSelectionListener(treeSelectionListener); 
            tree.treeDidChange(); 
        } 
    } 
      
    public static void addChildPaths(TreePath path, TreeModel model, List<TreePath> result){
	    Object item = path.getLastPathComponent();
	    int childCount = model.getChildCount(item);
	    for (int i = 0; i<childCount; i++) {
	        result.add(path.pathByAddingChild(model.getChild(item, i)));
	    }
	}

    public static List<TreePath> getDescendants(TreePath paths[] , TreeModel model){
	    List<TreePath> result = new ArrayList<TreePath>();
	    if (paths==null) {
	    	return result;
	    }
	
	    Stack<TreePath> pending = new Stack<TreePath>();
	    pending.addAll(Arrays.asList(paths));
	    while(!pending.isEmpty()){
	        TreePath path = (TreePath)pending.pop();
	        addChildPaths(path, model, pending);
	        result.add(path);
	    }
	    return result;
	}

    public static List<TreePath> getAllCheckedPaths(CheckTreeSelectionModel selectionModel,JTree tree){
	    return getDescendants(selectionModel.getSelectionPaths(), tree.getModel());
	}
	
	public static ExecutionStatusTreeTableNode getExecutionStatusTreeTableNode(final ExecutionStatusTreeTable executionAllStatusTreeTable,int row) 
	{
		TreePath treePath=executionAllStatusTreeTable.getPathForRow(row);
		if (treePath!=null) {
			return (ExecutionStatusTreeTableNode) treePath.getLastPathComponent();
		}
		return null;
	}
	
	public static void restoreSelectedPaths(JTree tree,CheckTreeSelectionModel checkTreeSelectionModel,Map<String,String> selectedPaths) {
		TreePath[] paths=getPaths(tree);
		if (paths==null) {
			return;
		}
		List<TreePath> pathsToRestore=new ArrayList<TreePath>();
		for (TreePath path:paths) {
			Object object=path.getLastPathComponent();
			String selectedPath=null;
			if (object instanceof ExecutionStatusTreeTableNode)
			{
				ExecutionStatusTreeTableNode node= (ExecutionStatusTreeTableNode)object;
				selectedPath=selectedPaths.get(node.getNodePath());
			}
			else {
				TextTreeNode node= (TextTreeNode)object;
				selectedPath=selectedPaths.get(node.getNodePath());
			}
			if (selectedPath!=null) {
				pathsToRestore.add(path);
			}
		}
		checkTreeSelectionModel.addSelectionPaths(getTreePaths(pathsToRestore)); 
	}
	
	static TreePath[] getPaths(JTree tree) {
		if (tree==null) {
			return null;
		}
		Object root = tree.getModel().getRoot();
		List<TreePath> list = new ArrayList<TreePath>();
		getPaths(tree, new TreePath(root), list);
		return (TreePath[])list.toArray(new TreePath[list.size()]);
	}
	
	@SuppressWarnings("rawtypes")
	private static void getPaths(JTree tree, TreePath parent, List<TreePath> list) {
		list.add(parent);
		Object object=parent.getLastPathComponent();
		if (object instanceof ExecutionStatusTreeTableNode)
		{
			ExecutionStatusTreeTableNode node= (ExecutionStatusTreeTableNode)object;
			if (node.getChildCount() >= 0) {
				for (Enumeration e=node.children(); e.hasMoreElements(); ) {
					ExecutionStatusTreeTableNode n = (ExecutionStatusTreeTableNode)e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					getPaths(tree, path, list);
				}
			}
		}
		else {
			TextTreeNode node= (TextTreeNode)object;
			if (node.getChildCount() >= 0) {
				for (Enumeration e=node.children(); e.hasMoreElements(); ) {
					TextTreeNode n = (TextTreeNode)e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					getPaths(tree, path, list);
				}
			}
		}		
	}
		
	public static void setAllPathsSelection(JTree tree,CheckTreeSelectionModel checkTreeSelectionModel,boolean isAllPathsSelection) {
		List<TreePath> paths=new ArrayList<TreePath>();
		Object object=tree.getModel().getRoot();
		TreePath path=null;
		if (object instanceof ExecutionStatusTreeTableNode)
		{
			ExecutionStatusTreeTableNode node= (ExecutionStatusTreeTableNode)object;
			path=new TreePath(node);
		}
		else {
			TextTreeNode node= (TextTreeNode)object;
			path=new TreePath(node);
		}
		paths.add(path);
		if (isAllPathsSelection) {
			checkTreeSelectionModel.addSelectionPaths(getTreePaths(paths)); 
		}
		else {
			checkTreeSelectionModel.removeSelectionPaths(getTreePaths(paths)); 
		}
	}
	
	static TreePath[] getTreePaths(List<TreePath> paths) {
		TreePath[] toReturn=new TreePath[paths.size()];
		for (int i=0;i<paths.size();i++) {
			toReturn[i]=paths.get(i);
		}
		return toReturn;
	}
    
	public static void saveCheckedPathsTreeSession(List<TreePath> selectedPaths,StringBuffer content)
	{
		if (selectedPaths!=null) {
			for (TreePath path:selectedPaths) {
				content.append("<"+UtilsConstants.XML_NODE_TAG_NAME+"><![CDATA[");
				Object object=path.getLastPathComponent();
				if (object instanceof ExecutionStatusTreeTableNode)
				{
					ExecutionStatusTreeTableNode node= (ExecutionStatusTreeTableNode)object;
					content.append(node.getNodePath());
				}
				else {
					TextTreeNode node= (TextTreeNode)object;
					content.append(node.getNodePath());
				}
				content.append("]]></"+UtilsConstants.XML_NODE_TAG_NAME+">\n");
			}
		}
	}
			
	public static void updateTask(org.jdesktop.swingx.treetable.TreeTableModel treeTableModel,ExecutionStatusTreeTableNode node,String statusMessage) {
		node.setValueAt(statusMessage,ExecutionStatusPanel.STATUS_COLUMN_INDEX);
		try{
			treeTableModel.setValueAt(node, node, ExecutionStatusPanel.STATUS_COLUMN_INDEX);
		}
		catch(java.lang.Throwable t) {
			FileUtils.printStackTrace(t);
		}
	}
	
	public static String getNavigationNameForNavNode(String name) {
		int indexOf=name.indexOf("-");
		return name.substring(indexOf+1);
	}
	
	public static boolean isNodeIsNavigation(String name) {
		int indexOf=name.indexOf("-");
		if (indexOf!=-1){
			try{
				Integer.valueOf(name.substring(0,indexOf)).intValue();
				return true;
			}
			catch(Exception e) {
			}
		}
		return false;
	}
	
	public static String getNavigationName(String name,boolean hasChildren) {
		if (!hasChildren){
			return null;
		}
		if (isNodeIsNavigation(name)) {
			return getNavigationNameForNavNode(name);
		}
		return  null;
	}
		
}