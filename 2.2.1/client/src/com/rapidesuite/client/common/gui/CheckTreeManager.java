package com.rapidesuite.client.common.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class CheckTreeManager extends MouseAdapter implements TreeSelectionListener{
	
    private CheckTreeSelectionModel selectionModel; 
    protected JTree tree; 
    private TextCheckTreeCellRenderer checkTreeCellRenderer; 
    int hotspot = new JCheckBox().getPreferredSize().width; 
 
    public CheckTreeManager(JTree tree,TextCheckTreeCellRenderer checkTreeCellRenderer,CheckTreeSelectionModel selectionModel){ 
        this.tree = tree; 
        this.selectionModel=selectionModel;
        this.checkTreeCellRenderer=checkTreeCellRenderer;
        tree.setCellRenderer(checkTreeCellRenderer);
        tree.addMouseListener(this); 
        selectionModel.addTreeSelectionListener(this); 
    } 
 
    public void mouseClicked(MouseEvent me){ 
    	CheckTreeUtils.mouseClicked(me,selectionModel,tree,hotspot,this);
    } 
 
    public CheckTreeSelectionModel getSelectionModel(){ 
        return selectionModel; 
    } 
 
    public void valueChanged(TreeSelectionEvent e){ 
        tree.treeDidChange(); 
    } 
    
	public TextCheckTreeCellRenderer getCheckTreeCellRenderer() {
		return checkTreeCellRenderer;
	}
	
	public List<TreePath> getAllCheckedPaths(){
		return CheckTreeUtils.getAllCheckedPaths(selectionModel,tree);
	}
	
	public void restoreSelectedPaths(Map<String,String> selectedPaths) {
		TreePath[] paths=CheckTreeUtils.getPaths(tree);
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
		selectionModel.addSelectionPaths(CheckTreeUtils.getTreePaths(pathsToRestore)); 
	}
    
}