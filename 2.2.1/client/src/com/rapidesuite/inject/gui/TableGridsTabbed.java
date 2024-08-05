package com.rapidesuite.inject.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.ScriptGridTracker;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class TableGridsTabbed extends JFrame {

	private JPanel cards;
	private JTree tree;

	public static JPanel getPanelGrids() {
		JPanel mainPanel=new JPanel();
		mainPanel.setOpaque(true);
		mainPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		return mainPanel;
	}

	public static void addTables(JPanel mainPanel,TableGrid tableGrid) {
		JPanel panel=new JPanel();
		mainPanel.add(panel);
		panel.setLayout(new BorderLayout());
		panel.setOpaque(true);
		panel.setBackground(Color.decode("#343836"));

		JScrollPane variableScroll = new JScrollPane(tableGrid.getTable(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};
		Vector<String> columnNames =TableGrid.getColumns(tableGrid.getInventory());
		InjectFilteringTable fixedTable=new InjectFilteringTable(columnNames,variableScroll,tableGrid.getTable(),33,true);

		panel.add(fixedTable.getFixedScrollPane(),BorderLayout.NORTH);	
		panel.add(variableScroll,BorderLayout.CENTER);
	}

	public TableGridsTabbed(ScriptGridTracker scriptGridTracker,List<TableGrid> tableGrids) throws Exception {
		String title=(scriptGridTracker.getGridIndex()+1)+". "+scriptGridTracker.getScript().getName();
		this.setTitle(title);
		this.setIconImage(GUIUtils.getImageIcon(this.getClass(), InjectMain.getSharedApplicationIconPath()).getImage());
		this.setSize(InjectMain.FRAME_WIDTH, InjectMain.FRAME_HEIGHT);
		UIUtils.setFramePosition(scriptGridTracker.getInjectMain().getFrame()  ,this);
		this.setLayout(new BorderLayout());

		Container contentPane = this.getContentPane();
		JPanel mainPanel=new JPanel();
		contentPane.add(mainPanel,BorderLayout.CENTER);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(true);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		mainPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);

		JPanel northPanel=new JPanel();
		northPanel.setOpaque(false);
		//northPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
		northPanel.setBorder(BorderFactory.createEmptyBorder(5,0,15,5));
		northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(northPanel,BorderLayout.NORTH);

		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		mainPanel.add(centerPanel,BorderLayout.CENTER);

		cards = new JPanel(new CardLayout());
		centerPanel.add(cards);

		Map<String,TableGrid> inventoryNameToTableGridMap=new HashMap<String,TableGrid>();
		for (final TableGrid tableGrid:tableGrids ) {
			Inventory inventory=tableGrid.getInventory();
			inventoryNameToTableGridMap.put(inventory.getName(), tableGrid);

			JPanel panel=new JPanel();
			panel.setOpaque(false);
			panel.setLayout(new BorderLayout());
			cards.add(panel, inventory.getName());

			JScrollPane variableScroll = new JScrollPane(tableGrid.getTable(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
				public void setColumnHeaderView(Component view) {} // work around
			};
			Vector<String> columnNames =TableGrid.getColumns(inventory);
			InjectFilteringTable fixedTable=new InjectFilteringTable(columnNames,variableScroll,tableGrid.getTable(),33,true);

			//JPanel headerPanel=new JPanel();
			//headerPanel.setBorder(BorderFactory.createEmptyBorder(5,0,5,5));
			//headerPanel.setOpaque(false);
			//headerPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
			//headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
			//panel.add(headerPanel,BorderLayout.NORTH);

			//JLabel inventoryNameLabel=new JLabel("Selected Table: '"+inventory.getName()+"'");
			//headerPanel.add(inventoryNameLabel);
			//headerPanel.add(fixedTable.getFixedScrollPane());	

			panel.add(fixedTable.getFixedScrollPane(),BorderLayout.NORTH);
			panel.add(variableScroll,BorderLayout.CENTER);
		}
		Iterator<String> iterator=inventoryNameToTableGridMap.keySet().iterator();
		List<TableGrid> roots=new ArrayList<TableGrid>();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			TableGrid tableGrid=inventoryNameToTableGridMap.get(inventoryName);
			Inventory inventory=tableGrid.getInventory();
			String parentName=inventory.getParentName();
			if (parentName!=null && !parentName.isEmpty()) {
				TableGrid tableGridParent=inventoryNameToTableGridMap.get(parentName);
				int sequence=inventory.getSequence();
				tableGridParent.addChild(sequence, tableGrid);
			}
			else {
				roots.add(tableGrid);
			}
		}

		DefaultMutableTreeNode rootNode=null;
		TableGrid defaultTableGrid=null;
		if (roots.size()>1) {
			rootNode= new DefaultMutableTreeNode("ROOT");
			for (TableGrid rootTableGridTemp:roots) {
				DefaultMutableTreeNode rootTableGridTempNode= new DefaultMutableTreeNode(rootTableGridTemp.getInventory().getName());
				if (defaultTableGrid==null) {
					defaultTableGrid=rootTableGridTemp;
				}
				rootNode.add(rootTableGridTempNode);
				rootTableGridTemp.buildTree(rootTableGridTempNode);
			}
		}
		else {
			TableGrid rootTableGrid=roots.get(0);
			rootNode = new DefaultMutableTreeNode(rootTableGrid.getInventory().getName());
			defaultTableGrid=rootTableGrid;
			rootTableGrid.buildTree(rootNode);
		}		
		tree = new JTree(rootNode);
		tree.setOpaque(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		if (roots.size()>1) {
			tree.setRootVisible(false);
		}
		else {
			tree.setRootVisible(true);
		}
		tree.setShowsRootHandles(true);

		tree.setExpandsSelectedPaths(true);
		//tree.collapseRow(0);
		for(int i=0;i<tree.getRowCount();i++){
			tree.expandRow(i);
		}
		tree.setSelectionRow(0);
		tree.setCellRenderer( new DefaultTreeCellRenderer() {

			@Override
			public Color getBackgroundNonSelectionColor() {
				return (null);
			}

			@Override
			public Color getBackground() {
				return (null);
			}

			@Override
			public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
				final Component ret = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				this.setText(value.toString());
				return ret;
			}

		});

		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				doMouseClicked(me);
			}
		});

		if (tree.getRowCount()==1) {
			northPanel.add(tree);
		}
		else {
			JScrollPane scrollPane = new JScrollPane(tree);
			if (tree.getRowCount()==2) {
				scrollPane.setPreferredSize(new Dimension(800,40));
				scrollPane.setMaximumSize(new Dimension(800,40));
			}
			else {
				scrollPane.setPreferredSize(new Dimension(800,80));
				scrollPane.setMaximumSize(new Dimension(800,80));
			}
			northPanel.add(scrollPane);
		}
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, defaultTableGrid.getInventory().getName());
	}

	private void doMouseClicked(MouseEvent me) {
		TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
		if (tp != null) {
			String inventoryName=tp.getLastPathComponent().toString();
			//System.out.println("click on inventory: "+inventoryName);
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, inventoryName);
		}
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public static void initJTreeUI(Class mainClass) {
		Icon empty = new TreeIcon();
		UIManager.put("Tree.closedIcon", empty);
		UIManager.put("Tree.openIcon", empty);
		///UIManager.put("Tree.collapsedIcon", plusImageIcon);
		//UIManager.put("Tree.expandedIcon", minusImageIcon);
		UIManager.put("Tree.leafIcon", empty);
	}

	public static class TreeIcon implements Icon {

		private int SIZE = 0;

		public TreeIcon() {
		}

		public int getIconWidth() {
			return SIZE;
		}

		public int getIconHeight() {
			return SIZE;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			//System.out.println(c.getWidth() + " " + c.getHeight() + " " + x + " " + y);
		}
	}

}
