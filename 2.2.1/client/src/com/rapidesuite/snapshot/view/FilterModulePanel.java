package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class FilterModulePanel extends FilterCommonPanel {

	private JList<String> modulesJList;
	private DefaultListModel<String> listModel;
	private JLabel modulesLabel;
	private JPanel centerPanel;
	private SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel;
	private Map<String, Object> selectedResultsKeyToObjectMap;
	private JFrame rootFrame;
	private JPanel filterSelectionPanel;
	private JCheckBox selectionCheckBox;
	private Set<String> allModulesSet;

	public FilterModulePanel(JFrame rootFrame,JLabel unappliedFiltersLabel,SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel) {
		super(unappliedFiltersLabel);
		this.snapshotInventoryDetailsGridPanel=snapshotInventoryDetailsGridPanel;
		selectedResultsKeyToObjectMap=new TreeMap<String,Object>();
		setLayout(new BorderLayout());
		this.rootFrame=rootFrame;
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"Applications Selection",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
		CompoundBorder compoundBorder=new CompoundBorder(titledBorder,new EmptyBorder(0, 0, 0, 0));
		setBorder(compoundBorder);
		
		createComponents();
	}

	public void createComponents(){
		centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(centerPanel,BorderLayout.CENTER);

		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerPanel.add(tempPanel);
		selectionCheckBox=new JCheckBox("Enable Filtering (All Application names are included by default)");
		tempPanel.add(selectionCheckBox);
		InjectUtils.assignArialPlainFont(selectionCheckBox,InjectMain.FONT_SIZE_NORMAL);
		selectionCheckBox.setForeground(Color.WHITE);
		selectionCheckBox.setOpaque(false);
		selectionCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSelection();
			}
		});
	
		filterSelectionPanel=new JPanel();
		filterSelectionPanel.setVisible(false);
		filterSelectionPanel.setOpaque(false);
		filterSelectionPanel.setLayout(new BoxLayout(filterSelectionPanel, BoxLayout.Y_AXIS));
		centerPanel.add(filterSelectionPanel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		filterSelectionPanel.add(tempPanel);
		modulesLabel=new JLabel("Selected Values: (Total: 0 )");
		InjectUtils.assignArialPlainFont(modulesLabel,InjectMain.FONT_SIZE_NORMAL);
		modulesLabel.setOpaque(false);
		modulesLabel.setForeground(Color.WHITE);
		tempPanel.add(modulesLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_dots.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton searchButton = new JButton();
		searchButton.setIcon(ii);
		searchButton.setBorderPainted(false);
		searchButton.setContentAreaFilled(false);
		searchButton.setFocusPainted(false);
		searchButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_dots_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		searchButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(searchButton);
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSearch();
			}
		});
		
		listModel = new DefaultListModel<String>(); 
		modulesJList = new JList<String>(listModel);
		modulesJList.setSelectionModel(new DefaultListSelectionModel() {
		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        super.setSelectionInterval(-1, -1);
		    }
		});
		JScrollPane scrollPane = new JScrollPane(modulesJList);
		Dimension d = modulesJList.getPreferredSize();
		d.width = 200;
		d.height =80;
		scrollPane.setPreferredSize(d);
		filterSelectionPanel.add(scrollPane);
	}

	protected void processActionSelection() {
		filterSelectionPanel.setVisible(selectionCheckBox.isSelected());
	}

	protected void processActionSearch() {
		new SearchWindowModules(rootFrame,this,"Application Names Search Window",selectedResultsKeyToObjectMap);
	}

	protected void processActionModulesSelection() {
		if (selectedResultsKeyToObjectMap.isEmpty()) {
			snapshotInventoryDetailsGridPanel.setSelectionAll();
			snapshotInventoryDetailsGridPanel.getFilteringTable().setColumnFilterValue(
					SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_SELECTION,"");
			snapshotInventoryDetailsGridPanel.getFilteringTable().applyFiltering();
			return;
		}
		Iterator<String> iterator=selectedResultsKeyToObjectMap.keySet().iterator();
		Set<String> selectedModules=new TreeSet<String>();
		while (iterator.hasNext()) {
			String key=iterator.next();
			selectedModules.add(key);
		}
		snapshotInventoryDetailsGridPanel.setSelectionOnlyForModules(selectedModules);
		snapshotInventoryDetailsGridPanel.getFilteringTable().setColumnFilterValue(
				SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_SELECTION,"Selected");
		snapshotInventoryDetailsGridPanel.getFilteringTable().applyFiltering();
	}

	public void resetFilter() {
	}

	public void setComponentsEnabled(boolean isEnabled) {
		modulesJList.setEnabled(isEnabled);
	}
	
	public void setSelectedMap(Map<String, Object> selectedResultsKeyToObjectMapParam) {
		selectedResultsKeyToObjectMap=selectedResultsKeyToObjectMapParam;
		Iterator<String> iterator=selectedResultsKeyToObjectMapParam.keySet().iterator();
		listModel = new DefaultListModel<String>();
		modulesJList.setModel(listModel);
		while (iterator.hasNext()) {
			String key=iterator.next();
			listModel.addElement(key);
		}
		modulesLabel.setText("Selected Values: (Total: "+selectedResultsKeyToObjectMap.size()+" )");
	}

	public void setAllModules(Set<String> moduleSet) {
		this.allModulesSet=moduleSet;
	}

	public Set<String> getAllModuleSet() {
		return allModulesSet;
	}
	
}
