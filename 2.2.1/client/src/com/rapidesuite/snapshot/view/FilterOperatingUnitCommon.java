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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
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
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.OperatingUnit;

@SuppressWarnings("serial")
public abstract class FilterOperatingUnitCommon  extends FilterCommonPanel {

	private JList<String> operatingUnitsJList;
	private DefaultListModel<String> listModel;
	private JLabel operatingUnitLabel;
	protected JPanel centerPanel;
	private JButton searchButton;
	private JFrame rootFrame;
	private Map<String, String>  snapshotEnvironmentProperties;
	protected JPanel filterSelectionPanel;
	private Map<String,Object> selectedResultsKeyToObjectMap;
	protected JPanel selectionPanel;
	private SnapshotGridRecord snapshotGridRecord;
		
	public FilterOperatingUnitCommon(JFrame rootFrame,JLabel unappliedFiltersLabel,Map<String, String> snapshotEnvironmentProperties,
			SnapshotGridRecord snapshotGridRecord) {
		super(unappliedFiltersLabel);
		setLayout(new BorderLayout());
		this.rootFrame=rootFrame;
		this.snapshotGridRecord=snapshotGridRecord;
		selectedResultsKeyToObjectMap=new TreeMap<String,Object>();
		this.snapshotEnvironmentProperties=snapshotEnvironmentProperties;
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"Operating Units Selection",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
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
		
		selectionPanel=new JPanel();
		selectionPanel.setOpaque(false);
		selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
		centerPanel.add(selectionPanel);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		centerPanel.add(tempPanel);
		    
		filterSelectionPanel=new JPanel();
		filterSelectionPanel.setVisible(false);
		filterSelectionPanel.setOpaque(false);
		filterSelectionPanel.setLayout(new BoxLayout(filterSelectionPanel, BoxLayout.Y_AXIS));
		centerPanel.add(filterSelectionPanel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		filterSelectionPanel.add(tempPanel);
		operatingUnitLabel=new JLabel("Selected Values: (Total: 0 )");
		InjectUtils.assignArialPlainFont(operatingUnitLabel,InjectMain.FONT_SIZE_NORMAL);
		operatingUnitLabel.setOpaque(false);
		operatingUnitLabel.setForeground(Color.WHITE);
		tempPanel.add(operatingUnitLabel);
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
		operatingUnitsJList = new JList<String>(listModel);
		operatingUnitsJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		operatingUnitsJList.setVisibleRowCount(5);
		operatingUnitsJList.setSelectionModel(new DefaultListSelectionModel() {
		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        super.setSelectionInterval(-1, -1);
		    }
		});
		JScrollPane scrollPane = new JScrollPane(operatingUnitsJList);
		Dimension d = operatingUnitsJList.getPreferredSize();
		d.width = 120;
		d.height = 80;
		scrollPane.setPreferredSize(d);
		//scrollPane.setMinimumSize(d);
		//scrollPane.setMaximumSize(d);
		filterSelectionPanel.add(scrollPane);
	}

	protected void processActionSearch() {
		new SearchWindowOperatingUnits(rootFrame,this,"Operating Units Search Window",snapshotEnvironmentProperties,
				selectedResultsKeyToObjectMap,snapshotGridRecord);
	}

	public void setUnappliedFilter() {
		setHasUnappliedChanges(true);
		unappliedFiltersLabel.setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
	}
	
	public void setComponentsEnabled(boolean isEnabled) {
		operatingUnitsJList.setEnabled(isEnabled);
	}
	
	public List<OperatingUnit> getSelectedOperatingUnits() {
		List<OperatingUnit> toReturn=new ArrayList<OperatingUnit>();
		Iterator<String> iterator=selectedResultsKeyToObjectMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			OperatingUnit operatingUnit=(OperatingUnit)selectedResultsKeyToObjectMap.get(key);
			toReturn.add(operatingUnit);
		}
		return toReturn;
	}

	public void setSelectedMap(Map<String, Object> selectedResultsKeyToObjectMapParam) {
		selectedResultsKeyToObjectMap=selectedResultsKeyToObjectMapParam;
		Iterator<String> iterator=selectedResultsKeyToObjectMapParam.keySet().iterator();
		listModel = new DefaultListModel<String>();
		operatingUnitsJList.setModel(listModel);
		while (iterator.hasNext()) {
			String key=iterator.next();
			listModel.addElement(key);
		}
		operatingUnitLabel.setText("Selected Values: (Total: "+selectedResultsKeyToObjectMap.size()+" )");
	}
	

	public abstract boolean isOperatingUnitFilterEnabled();
	
	
}
