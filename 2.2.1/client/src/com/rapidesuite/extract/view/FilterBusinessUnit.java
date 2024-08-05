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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.extract.model.BusinessUnit;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.RolloverIcon;

@SuppressWarnings("serial")
public class FilterBusinessUnit extends JPanel{

	private JList<String> businessUnitsJList;
	private DefaultListModel<String> listModel;
	private JLabel businessUnitLabel;
	private JPanel centerPanel;
	private JButton searchButton;
	private FiltersPanel filtersPanel;
	private JCheckBox selectionCheckBox;
	private JPanel filterSelectionPanel;
	private Map<String,Object> selectedResultsKeyToObjectMap;
	
	public FilterBusinessUnit(FiltersPanel filtersPanel) {
		setLayout(new BorderLayout());
		this.filtersPanel=filtersPanel;
		selectedResultsKeyToObjectMap=new TreeMap<String,Object>();
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"Business Unit Selection",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
		CompoundBorder compoundBorder=new CompoundBorder(titledBorder,new EmptyBorder(0, 0, 0, 0));
		setBorder(compoundBorder);
		
		createComponents();
	}

	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(false);
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(northPanel,BorderLayout.NORTH);
		
		centerPanel=new JPanel();
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
		filterSelectionPanel.setLayout(new BoxLayout(filterSelectionPanel, BoxLayout.Y_AXIS));
		centerPanel.add(filterSelectionPanel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		filterSelectionPanel.add(tempPanel);
		businessUnitLabel=new JLabel("Selected Values: (Total: 0 )");
		InjectUtils.assignArialPlainFont(businessUnitLabel,InjectMain.FONT_SIZE_NORMAL);
		businessUnitLabel.setOpaque(false);
		businessUnitLabel.setForeground(Color.WHITE);
		tempPanel.add(businessUnitLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_dots.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		searchButton = new JButton();
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
		businessUnitsJList = new JList<String>(listModel);
		businessUnitsJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		businessUnitsJList.setVisibleRowCount(5);
		businessUnitsJList.setSelectionModel(new DefaultListSelectionModel() {
		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        super.setSelectionInterval(-1, -1);
		    }
		});
		JScrollPane scrollPane = new JScrollPane(businessUnitsJList);
		Dimension d = businessUnitsJList.getPreferredSize();
		d.width = 120;
		d.height = 80;
		scrollPane.setPreferredSize(d);
		//scrollPane.setMinimumSize(d);
		//scrollPane.setMaximumSize(d);
		filterSelectionPanel.add(scrollPane);
	}

	protected void processActionSelection() {
		filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
		filterSelectionPanel.setVisible(selectionCheckBox.isSelected());
	}

	protected void processActionSearch() {
		new SearchWindowBusinessUnit(filtersPanel.getRootFrame(),this,"Business Units Search Window",
				selectedResultsKeyToObjectMap);
	}

	public void resetFilter() {
		selectionCheckBox.setSelected(false);
		filterSelectionPanel.setVisible(false);
	}
		
	public void setComponentsEnabled(boolean isEnabled) {
		businessUnitsJList.setEnabled(isEnabled);
	}	

	public boolean isBusinessUnitFilterEnabled() {
		return selectionCheckBox.isSelected();
	}
	
	public List<BusinessUnit> getSelectedBusinessUnits() {
		List<BusinessUnit> toReturn=new ArrayList<BusinessUnit>();
		Iterator<String> iterator=selectedResultsKeyToObjectMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			BusinessUnit businessUnit=(BusinessUnit)selectedResultsKeyToObjectMap.get(key);
			toReturn.add(businessUnit);
		}
		return toReturn;
	}

	public void setSelectedMap(Map<String, Object> selectedResultsKeyToObjectMapParam) {
		selectedResultsKeyToObjectMap=selectedResultsKeyToObjectMapParam;
		Iterator<String> iterator=selectedResultsKeyToObjectMapParam.keySet().iterator();
		listModel = new DefaultListModel<String>();
		businessUnitsJList.setModel(listModel);
		while (iterator.hasNext()) {
			String key=iterator.next();
			listModel.addElement(key);
		}
		businessUnitLabel.setText("Selected Values: (Total: "+selectedResultsKeyToObjectMap.size()+" )");
	}

	public FiltersPanel getFiltersPanel() {
		return filtersPanel;
	}
	
}