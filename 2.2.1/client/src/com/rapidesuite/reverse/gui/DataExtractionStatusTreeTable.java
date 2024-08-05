package com.rapidesuite.reverse.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.rapidesuite.client.common.gui.ExecutionStatusTreeTable;
import com.rapidesuite.client.common.gui.TreeTableModel;

@SuppressWarnings("serial")
public class DataExtractionStatusTreeTable extends ExecutionStatusTreeTable
{
	
	public DataExtractionStatusTreeTable(TreeTableModel treeTableModel) {
		super(treeTableModel);
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				final Point p = e.getPoint();
				final int row = DataExtractionStatusTreeTable.this.rowAtPoint(p);
				final int column = DataExtractionStatusTreeTable.this.columnAtPoint(p);
				
				if (e.getClickCount() == 1) {
					if (SwingUtilities.isRightMouseButton(e)) {
						if (row >= 0 && column >= 0) {
							final JPopupMenu popup = new JPopupMenu();
							
							final JMenuItem expandAllItem = new JMenuItem("Expand All");
							expandAllItem.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									DataExtractionStatusTreeTable.this.expandAll();
								}
								
							});
							popup.add(expandAllItem);
							
							final JMenuItem expandDescendantsItem = new JMenuItem("Expand Descendants");
							expandDescendantsItem.addActionListener(new ActionListener() {
								
								@SuppressWarnings("rawtypes")
								private void doExpand(TreePath parent) {
									TreeNode node = (TreeNode) parent.getLastPathComponent();
									if (node.getChildCount() >= 0) {
										for (Enumeration e = node.children(); e.hasMoreElements();) {
											TreeNode n = (TreeNode) e.nextElement();
											TreePath path = parent.pathByAddingChild(n);
											doExpand(path);
										}
									}
									DataExtractionStatusTreeTable.this.expandPath(parent);
								}								

								@Override
								public void actionPerformed(ActionEvent e) {
									doExpand(DataExtractionStatusTreeTable.this.getPathForRow(row));
								}
								
							});
							expandDescendantsItem.setEnabled(DataExtractionStatusTreeTable.this.getSelectedRowCount() == 1);
							popup.add(expandDescendantsItem);
							
							final JMenuItem collapseAllItem = new JMenuItem("Collapse All");
							collapseAllItem.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent e) {
									DataExtractionStatusTreeTable.this.collapseAll();
								}
								
							});
							popup.add(collapseAllItem);
							
							final JMenuItem collapseDescendantsItem = new JMenuItem("Collapse Descendants");
							collapseDescendantsItem.addActionListener(new ActionListener() {
								
								@SuppressWarnings("rawtypes")
								private void doCollapse(TreePath parent) {
									TreeNode node = (TreeNode) parent.getLastPathComponent();
									if (node.getChildCount() >= 0) {
										for (Enumeration e = node.children(); e.hasMoreElements();) {
											TreeNode n = (TreeNode) e.nextElement();
											TreePath path = parent.pathByAddingChild(n);
											doCollapse(path);
										}
									}
									DataExtractionStatusTreeTable.this.collapsePath(parent);
								}						

								@Override
								public void actionPerformed(ActionEvent e) {
									doCollapse(DataExtractionStatusTreeTable.this.getPathForRow(row));
								}
								
							});
							collapseDescendantsItem.setEnabled(DataExtractionStatusTreeTable.this.getSelectedRowCount() == 1);
							popup.add(collapseDescendantsItem);							
							
							popup.show(e.getComponent(), e.getX(), e.getY());							
						}
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});		
	}
	
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	    return super.prepareRenderer(renderer, row, column);
	}
	 
}