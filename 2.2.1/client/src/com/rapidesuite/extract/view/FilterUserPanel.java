package com.rapidesuite.extract.view;

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
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.OracleUser;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.UIConstants;

@SuppressWarnings("serial")
public class FilterUserPanel extends JPanel{

	private JList<String> createdByUserNamesJList;
	private DefaultListModel<String> createdByListModel;
	private JLabel createdBylabel;
	private Map<String,Object> selectedResultsCreatedByKeyToObjectMap;
	private JButton searchCreatedByButton;
	private JRadioButton createdByIncludeRadioButton;
	private JRadioButton createdByExcludeRadioButton;
	
	private JList<String> lastUpdatedByUserNamesJList;
	private DefaultListModel<String> lastUpdatedByListModel;
	private JLabel lastUpdatedBylabel;
	private Map<String,Object> selectedResultsLastUpdatedByKeyToObjectMap;
	private JButton searchLastUpdatedByButton;
	private JRadioButton lastUpdatedByIncludeRadioButton;
	private JRadioButton lastUpdatedByExcludeRadioButton;
		
	private JCheckBox selectionCheckBox;
	private JPanel filterSelectionPanel;
	private FiltersPanel filtersPanel;
	
	public FilterUserPanel(FiltersPanel filtersPanel) {
		setLayout(new BorderLayout());
		this.filtersPanel=filtersPanel;
		selectedResultsCreatedByKeyToObjectMap=new TreeMap<String,Object>();
		selectedResultsLastUpdatedByKeyToObjectMap=new TreeMap<String,Object>();
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"User Selection",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
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
		selectionCheckBox=new JCheckBox("Enable Selection (All included by default)");
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
				
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		//tempPanel.setBackground(Color.WHITE);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		createdByPanel.add(tempPanel);
		ButtonGroup createdByButtonGroup = new ButtonGroup();
		createdByIncludeRadioButton = new JRadioButton("Include");
		createdByIncludeRadioButton.setSelected(true);
		createdByIncludeRadioButton.setForeground(Color.WHITE);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_unselected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		createdByIncludeRadioButton.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_selected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		createdByIncludeRadioButton.setSelectedIcon(ii);
		createdByIncludeRadioButton.setOpaque(false);
		InjectUtils.assignArialPlainFont(createdByIncludeRadioButton,InjectMain.FONT_SIZE_NORMAL);
		
		createdByExcludeRadioButton = new JRadioButton("Exclude");
		createdByExcludeRadioButton.setForeground(Color.WHITE);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_unselected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		createdByExcludeRadioButton.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_selected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		createdByExcludeRadioButton.setSelectedIcon(ii);
		createdByExcludeRadioButton.setOpaque(false);
		InjectUtils.assignArialPlainFont(createdByExcludeRadioButton,InjectMain.FONT_SIZE_NORMAL);
		
		createdByButtonGroup.add(createdByIncludeRadioButton);
		createdByButtonGroup.add(createdByExcludeRadioButton);
		tempPanel.add(createdByIncludeRadioButton);
		tempPanel.add(createdByExcludeRadioButton);
		
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
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		//tempPanel.setBackground(Color.WHITE);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		lastUpdatedByPanel.add(tempPanel);
		ButtonGroup lastUpdatedByButtonGroup = new ButtonGroup();
		lastUpdatedByIncludeRadioButton = new JRadioButton("Include");
		lastUpdatedByIncludeRadioButton.setSelected(true);
		lastUpdatedByIncludeRadioButton.setForeground(Color.WHITE);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_unselected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		lastUpdatedByIncludeRadioButton.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_selected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		lastUpdatedByIncludeRadioButton.setSelectedIcon(ii);
		lastUpdatedByIncludeRadioButton.setOpaque(false);
		InjectUtils.assignArialPlainFont(lastUpdatedByIncludeRadioButton,InjectMain.FONT_SIZE_NORMAL);
		
		lastUpdatedByExcludeRadioButton = new JRadioButton("Exclude");
		lastUpdatedByExcludeRadioButton.setForeground(Color.WHITE);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_unselected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		lastUpdatedByExcludeRadioButton.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_selected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		lastUpdatedByExcludeRadioButton.setSelectedIcon(ii);
		lastUpdatedByExcludeRadioButton.setOpaque(false);
		InjectUtils.assignArialPlainFont(lastUpdatedByExcludeRadioButton,InjectMain.FONT_SIZE_NORMAL);
		
		lastUpdatedByButtonGroup.add(lastUpdatedByIncludeRadioButton);
		lastUpdatedByButtonGroup.add(lastUpdatedByExcludeRadioButton);
		tempPanel.add(lastUpdatedByIncludeRadioButton);
		tempPanel.add(lastUpdatedByExcludeRadioButton);
		
		
		
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
		new SearchWindowUsers(filtersPanel.getRootFrame(),this,"Last Update By Users Search Window",
				selectedResultsLastUpdatedByKeyToObjectMap,true);
	}

	protected void processActionSearchCreatedBy() {
		new SearchWindowUsers(filtersPanel.getRootFrame(),this,"Created By Users Search Window",
				selectedResultsCreatedByKeyToObjectMap,false);
	}

	protected void processActionSelection() {
		filterSelectionPanel.setVisible(selectionCheckBox.isSelected());
		if(!selectionCheckBox.isSelected()){
			((DefaultListModel<String>) createdByUserNamesJList.getModel()).removeAllElements();
			selectedResultsLastUpdatedByKeyToObjectMap.clear();
			((DefaultListModel<String>) lastUpdatedByUserNamesJList.getModel()).removeAllElements();
			selectedResultsCreatedByKeyToObjectMap.clear();
			
		}
		
		filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
	}

	public void setComponentsEnabled(boolean isEnabled) {
		createdByUserNamesJList.setEnabled(isEnabled);
	}	
	
	public void resetFilter() {
		selectionCheckBox.setSelected(false);
		filterSelectionPanel.setVisible(false);
	}
	
	public List<String> getCreatedBySelectedUserNames() {
		List<String> toReturn=new ArrayList<String>();
		Iterator<String> iterator=selectedResultsCreatedByKeyToObjectMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			OracleUser oracleUser=(OracleUser)selectedResultsCreatedByKeyToObjectMap.get(key);
			toReturn.add(oracleUser.getName());
		}
		return toReturn;
	}
	
	public List<String> getLastUpdatedBySelectedUserNames() {
		List<String> toReturn=new ArrayList<String>();
		Iterator<String> iterator=selectedResultsLastUpdatedByKeyToObjectMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			OracleUser oracleUser=(OracleUser)selectedResultsLastUpdatedByKeyToObjectMap.get(key);
			toReturn.add(oracleUser.getName());
		}
		return toReturn;
	}

	public void setSelectedMap(boolean isLastUpdateBy,Map<String, Object> selectedResultsKeyToObjectMapParam) {
		DefaultListModel<String> model;
		if (isLastUpdateBy) {
			selectedResultsLastUpdatedByKeyToObjectMap=selectedResultsKeyToObjectMapParam;
			lastUpdatedByListModel= new DefaultListModel<String>();
			model=lastUpdatedByListModel;
			lastUpdatedByUserNamesJList.setModel(model);
			lastUpdatedBylabel.setText("Last Update By: (Total: "+selectedResultsKeyToObjectMapParam.size()+" )");
		}
		else {
			selectedResultsCreatedByKeyToObjectMap=selectedResultsKeyToObjectMapParam;
			createdByListModel = new DefaultListModel<String>();
			model=createdByListModel;
			createdByUserNamesJList.setModel(model);
			createdBylabel.setText("Created By: (Total: "+selectedResultsKeyToObjectMapParam.size()+" )");
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

	public FiltersPanel getFiltersPanel() {
		return filtersPanel;
	}
	
	public boolean isCreatedByInclude() {
		return createdByIncludeRadioButton.isSelected();
	}
	
	public boolean isLastUpdatedByInclude() {
		return lastUpdatedByIncludeRadioButton.isSelected();
	}
	
}