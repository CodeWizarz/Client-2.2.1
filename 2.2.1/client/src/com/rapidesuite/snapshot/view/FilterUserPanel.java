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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
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
import com.rapidesuite.snapshot.model.OracleUser;

@SuppressWarnings("serial")
public class FilterUserPanel extends FilterCommonPanel {

	private JList<String> createdByUserNamesJList;
	private DefaultListModel<String> createdByListModel;
	private JLabel createdBylabel;
	private Map<String,Object> selectedResultsCreatedByKeyToObjectMap;
	private JButton searchCreatedByButton;
	
	private JList<String> lastUpdatedByUserNamesJList;
	private DefaultListModel<String> lastUpdatedByListModel;
	private JLabel lastUpdatedBylabel;
	private Map<String,Object> selectedResultsLastUpdatedByKeyToObjectMap;
	private JButton searchLastUpdatedByButton;
	
	private JCheckBox selectionCheckBox;
	private JPanel filterSelectionPanel;
	private JFrame rootFrame;
	private Map<String, String> snapshotEnvironmentProperties;
	private FiltersPanel filtersPanel;
	private SnapshotGridRecord snapshotGridRecord;
	
	public FilterUserPanel(FiltersPanel filtersPanel, JFrame rootFrame,JLabel unappliedFiltersLabel,
			Map<String, String> snapshotEnvironmentProperties,SnapshotGridRecord snapshotGridRecord) {
		super(unappliedFiltersLabel);
		setLayout(new BorderLayout());
		this.filtersPanel=filtersPanel;
		this.snapshotGridRecord=snapshotGridRecord;
		this.rootFrame=rootFrame;
		this.snapshotEnvironmentProperties=snapshotEnvironmentProperties;
		selectedResultsCreatedByKeyToObjectMap=new TreeMap<String,Object>();
		selectedResultsLastUpdatedByKeyToObjectMap=new TreeMap<String,Object>();
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"Users Selection",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
		CompoundBorder compoundBorder=new CompoundBorder(titledBorder,new EmptyBorder(0, 0, 0, 0));
		setBorder(compoundBorder);
		
		createComponents();
	}
	
	public void createComponents(){
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(centerPanel,BorderLayout.CENTER);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerPanel.add(tempPanel);
		selectionCheckBox=new JCheckBox("Enable Selection (All Users are included by default)");
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
		filterSelectionPanel.setLayout(new BoxLayout(filterSelectionPanel, BoxLayout.X_AXIS));
		centerPanel.add(filterSelectionPanel);
				
		JPanel createdByPanel=new JPanel();
		createdByPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		createdByPanel.setOpaque(false);
		createdByPanel.setLayout(new BoxLayout(createdByPanel, BoxLayout.Y_AXIS));
		createdByPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		filterSelectionPanel.add(createdByPanel);
		
		JPanel lastUpdatedByPanel=new JPanel();
		lastUpdatedByPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		lastUpdatedByPanel.setOpaque(false);
		lastUpdatedByPanel.setLayout(new BoxLayout(lastUpdatedByPanel, BoxLayout.Y_AXIS));
		lastUpdatedByPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		filterSelectionPanel.add(lastUpdatedByPanel);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		createdByPanel.add(tempPanel);
		createdBylabel=new JLabel("Created By: (Total: 0 )");
		InjectUtils.assignArialPlainFont(createdBylabel,InjectMain.FONT_SIZE_NORMAL);
		createdBylabel.setOpaque(false);
		createdBylabel.setForeground(Color.WHITE);
		tempPanel.add(createdBylabel);
		iconURL = this.getClass().getResource("/images/snapshot/button_dots.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		searchCreatedByButton = new JButton();
		searchCreatedByButton.setIcon(ii);
		searchCreatedByButton.setBorderPainted(false);
		searchCreatedByButton.setContentAreaFilled(false);
		searchCreatedByButton.setFocusPainted(false);
		searchCreatedByButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_dots_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		searchCreatedByButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(searchCreatedByButton);
		searchCreatedByButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSearchCreatedBy();
			}
		});
		createdByListModel = new DefaultListModel<String>(); 
		createdByUserNamesJList = new JList<String>(createdByListModel);
		createdByUserNamesJList.setSelectionModel(new DefaultListSelectionModel() {
		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        super.setSelectionInterval(-1, -1);
		    }
		});
		JScrollPane scrollPane = new JScrollPane(createdByUserNamesJList);
		Dimension d = createdByUserNamesJList.getPreferredSize();
		d.width = 100;
		d.height = 70;
		scrollPane.setPreferredSize(d);
		//scrollPane.setMinimumSize(d);
		//scrollPane.setMaximumSize(d);
		createdByPanel.add(scrollPane);

		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		lastUpdatedByPanel.add(tempPanel);
		lastUpdatedBylabel=new JLabel("Last Update By: (Total: 0 )");
		InjectUtils.assignArialPlainFont(lastUpdatedBylabel,InjectMain.FONT_SIZE_NORMAL);
		lastUpdatedBylabel.setOpaque(false);
		lastUpdatedBylabel.setForeground(Color.WHITE);
		tempPanel.add(lastUpdatedBylabel);
		iconURL = this.getClass().getResource("/images/snapshot/button_dots.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		searchLastUpdatedByButton = new JButton();
		searchLastUpdatedByButton.setIcon(ii);
		searchLastUpdatedByButton.setBorderPainted(false);
		searchLastUpdatedByButton.setContentAreaFilled(false);
		searchLastUpdatedByButton.setFocusPainted(false);
		searchLastUpdatedByButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_dots_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		searchLastUpdatedByButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(searchLastUpdatedByButton);
		searchLastUpdatedByButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSearchhLastUpdatedBy();
			}
		});
		lastUpdatedByListModel = new DefaultListModel<String>(); 
		lastUpdatedByUserNamesJList = new JList<String>(lastUpdatedByListModel);
		lastUpdatedByUserNamesJList.setSelectionModel(new DefaultListSelectionModel() {
		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        super.setSelectionInterval(-1, -1);
		    }
		});
		scrollPane = new JScrollPane(lastUpdatedByUserNamesJList);
		scrollPane.setPreferredSize(d);
		//scrollPane.setMinimumSize(d);
		//scrollPane.setMaximumSize(d);
		lastUpdatedByPanel.add(scrollPane);
	}
	
	protected void processActionSearchhLastUpdatedBy() {
		new SearchWindowUsers(rootFrame,this,"Last Update By Users Search Window",snapshotEnvironmentProperties,
				selectedResultsLastUpdatedByKeyToObjectMap,true, snapshotGridRecord);
	}

	protected void processActionSearchCreatedBy() {
		new SearchWindowUsers(rootFrame,this,"Created By Users Search Window",snapshotEnvironmentProperties,
				selectedResultsCreatedByKeyToObjectMap,false, snapshotGridRecord);
	}

	protected void processActionSelection() {
		filterSelectionPanel.setVisible(selectionCheckBox.isSelected());
		setUnappliedFilter();
		filtersPanel.getApplyFilteringButton().setEnabled(true);
	}

	public void setComponentsEnabled(boolean isEnabled) {
		createdByUserNamesJList.setEnabled(isEnabled);
		selectionCheckBox.setEnabled(isEnabled);
	}	
	
	public void resetFilter() {
		selectionCheckBox.setSelected(false);
		filterSelectionPanel.setVisible(false);
	}
	
	public List<Integer> getCreatedBySelectedUserIds() {
		List<Integer> toReturn=new ArrayList<Integer>();
		Iterator<String> iterator=selectedResultsCreatedByKeyToObjectMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			OracleUser oracleUser=(OracleUser)selectedResultsCreatedByKeyToObjectMap.get(key);
			toReturn.add(oracleUser.getId().intValue());
		}
		return toReturn;
	}
	
	public List<Integer> getLastUpdatedBySelectedUserIds() {
		List<Integer> toReturn=new ArrayList<Integer>();
		Iterator<String> iterator=selectedResultsLastUpdatedByKeyToObjectMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			OracleUser oracleUser=(OracleUser)selectedResultsLastUpdatedByKeyToObjectMap.get(key);
			toReturn.add(oracleUser.getId().intValue());
		}
		return toReturn;
	}

	public void setUnappliedFilter() {
		setHasUnappliedChanges(true);
		unappliedFiltersLabel.setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
	}	
	
	public void setSelectedMap(boolean isLastUpdateBy,Map<String, Object> selectedResultsKeyToObjectMapParam) {
		DefaultListModel<String> model;
		if (isLastUpdateBy) {
			selectedResultsLastUpdatedByKeyToObjectMap=selectedResultsKeyToObjectMapParam;
			lastUpdatedByListModel= new DefaultListModel<String>();
			model=lastUpdatedByListModel;
			lastUpdatedByUserNamesJList.setModel(model);
			lastUpdatedBylabel.setText("Selected Values: (Total: "+selectedResultsKeyToObjectMapParam.size()+" )");
		}
		else {
			selectedResultsCreatedByKeyToObjectMap=selectedResultsKeyToObjectMapParam;
			createdByListModel = new DefaultListModel<String>();
			model=createdByListModel;
			createdByUserNamesJList.setModel(model);
			createdBylabel.setText("Selected Values: (Total: "+selectedResultsKeyToObjectMapParam.size()+" )");
		}
		Iterator<String> iterator=selectedResultsKeyToObjectMapParam.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			model.addElement(key);
		}
	}

	public boolean isCreatedByUserNameEnabled() {
		return selectionCheckBox.isSelected();
	}

	public boolean isLastUpdateByUserNameEnabled() {
		return selectionCheckBox.isSelected();
	}
}