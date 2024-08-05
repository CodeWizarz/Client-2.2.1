/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/TextTreePanel.java $:
 * $Id: TextTreePanel.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class TextTreePanel extends JPanel
{

	private JScrollPane treeView;
	private JPanel treePanel;
	protected TextTreeNode rootTreeNode;
	protected JTree tree;
	private TextTreeNodeFinder textTreeNodeFinder;
	
	public TextTreePanel(){
		createComponents();
	}

	public void createComponents(){
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		treePanel = new JPanel();
		add(treePanel);
		treePanel.setLayout(new BoxLayout(treePanel, BoxLayout.Y_AXIS));
	}

	public void reloadTree(JFrame frame,TextTreeNode treeNode,boolean isRootVisible)
	{
		try {
			showTree(frame,treeNode,isRootVisible);
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}
	
	protected void showTree(JFrame frame,TextTreeNode treeNode,boolean isRootVisible) 
	throws Exception
	{
		initTree(frame,treeNode,isRootVisible);
	}
	
	@SuppressWarnings("rawtypes")
	private void initTree(JFrame frame,TextTreeNode treeNode,boolean isRootVisible) 
	throws Exception
	{
		this.rootTreeNode = treeNode;
		
		if (treeView!=null) {
			treePanel.remove(treeView);
		}
		tree = new JTree(rootTreeNode){
			public String getToolTipText(MouseEvent evt) {
				if (getRowForLocation(evt.getX(), evt.getY()) == -1){
					return null;
				}
				TreePath treePath = getPathForLocation(evt.getX(), evt.getY());
				TextTreeNode textTreeNode=((TextTreeNode)treePath.getLastPathComponent());
				return textTreeNode.getToolTipText();
			}
		};

		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setRowHeight(18);
		tree.setRootVisible(isRootVisible);
		tree.expandRow(0);
		TextTreeUI ui=new TextTreeUI();
		tree.setUI(ui);
		ui.setLeftChildIndent(30);
	
		List<String> items = new ArrayList<String>();  
		Enumeration e = rootTreeNode.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			TextTreeNode textTreeNode= (TextTreeNode) e.nextElement();
			if (textTreeNode==null || textTreeNode.getName()==null) {
				continue;
			}
			String textTreeNodeName=textTreeNode.getName().toLowerCase();
			items.add(textTreeNodeName);
		}
		
		if (textTreeNodeFinder!=null) {
			treePanel.remove(textTreeNodeFinder);
		}
		textTreeNodeFinder=new TextTreeNodeFinder(tree,items);
		treePanel.add(textTreeNodeFinder);
		treeView = new JScrollPane(tree);
		treePanel.add(treeView);
		frame.validate();
	}
		
	public TextTreeNode getRootTreeNode() {
		return rootTreeNode;
	}

	public TextTreeNodeFinder getTextTreeNodeFinder() {
		return textTreeNodeFinder;
	}

	public JTree getTree() {
		return tree;
	}
		
}