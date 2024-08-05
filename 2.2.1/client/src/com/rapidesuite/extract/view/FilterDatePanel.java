package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.JXDatePicker;

import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class FilterDatePanel extends JPanel{

	private JCheckBox creationDateCheckBox;
	private JComboBox<String> creationDateComboBox;
	private JXDatePicker creationDateFromDatePicker;
	private JXDatePicker creationDateToDatePicker;
	private JCheckBox lastUpdateDateCheckBox;
	private JComboBox<String> lastUpdateDateComboBox;
	private JXDatePicker lastUpdateDateFromDatePicker;
	private JXDatePicker lastUpdateDateToDatePicker;
	private JLabel creationDateTolabel;
	private JLabel lastUpdateDateTolabel;
	private FiltersPanel filtersPanel;
	
	public static final String COMPARISON_GREATER_OR_EQUAL_THAN=">=";
	public static final String COMPARISON_LESS_OR_EQUAL_THAN="<=";
	public static final String COMPARISON_BETWEEN="BETWEEN";
	
	public FilterDatePanel(FiltersPanel filtersPanel) {
		this.filtersPanel=filtersPanel;
		setLayout(new BorderLayout());
		
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"Date Selection",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
		CompoundBorder compoundBorder=new CompoundBorder(titledBorder,new EmptyBorder(0, 0, 0, 0));
		setBorder(compoundBorder);
		
		createComponents();
	}
	
	public void setComponentsEnabled(boolean isEnabled) {
		creationDateCheckBox.setEnabled(isEnabled);
		creationDateComboBox.setEnabled(isEnabled);
		creationDateFromDatePicker.setEnabled(isEnabled);
		creationDateToDatePicker.setEnabled(isEnabled);
		lastUpdateDateCheckBox.setEnabled(isEnabled);
		lastUpdateDateComboBox.setEnabled(isEnabled);
		lastUpdateDateFromDatePicker.setEnabled(isEnabled);
		lastUpdateDateToDatePicker.setEnabled(isEnabled);
	}
	
	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(false);
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(northPanel,BorderLayout.NORTH);
		
		JPanel centerPanel=new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
		centerPanel.setOpaque(false);
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		add(centerPanel,BorderLayout.CENTER);
		
		JPanel creationDatePanel=new JPanel();
		creationDatePanel.setOpaque(false);
		creationDatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		creationDatePanel.setLayout(new BoxLayout(creationDatePanel, BoxLayout.X_AXIS));
		centerPanel.add(creationDatePanel);
		
		JPanel lastUpdateDatePanel=new JPanel();
		lastUpdateDatePanel.setOpaque(false);
		lastUpdateDatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lastUpdateDatePanel.setLayout(new BoxLayout(lastUpdateDatePanel, BoxLayout.X_AXIS));
		centerPanel.add(lastUpdateDatePanel);
		
		List<String> comparatorList=new ArrayList<String>();
		comparatorList.add(COMPARISON_GREATER_OR_EQUAL_THAN);
		comparatorList.add(COMPARISON_LESS_OR_EQUAL_THAN);
		comparatorList.add(COMPARISON_BETWEEN);
		String[] comparatorListArray=comparatorList.toArray(new String[comparatorList.size()]);
		
		creationDateCheckBox=new JCheckBox("Creation Date");
		InjectUtils.assignArialPlainFont(creationDateCheckBox,InjectMain.FONT_SIZE_NORMAL);
		creationDateCheckBox.setOpaque(false);
		creationDateCheckBox.setForeground(Color.WHITE);
		creationDateCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
			}
		}
				);
		UIUtils.setDimension(creationDateCheckBox,130,23); 
		creationDateCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionEnableCreationDate();
			}
		});
		creationDatePanel.add(creationDateCheckBox);
		creationDateComboBox= new JComboBox<String>(comparatorListArray);
		creationDateComboBox.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent event) {
	        	 if (event.getStateChange() == ItemEvent.SELECTED) {
	        		 filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
	        		 String item = (String)event.getItem();
	        		 if (item.equalsIgnoreCase(COMPARISON_BETWEEN)) {
	                	 creationDateTolabel.setVisible(true);
	                	 creationDateToDatePicker.setVisible(true);
	                 }
	                 else {
	                	 creationDateTolabel.setVisible(false);
	                	 creationDateToDatePicker.setVisible(false);
	                 }
	              }
	        }
	    });
		UIUtils.setDimension(creationDateComboBox,80,23); 
		creationDatePanel.add(creationDateComboBox);
		creationDatePanel.add(Box.createRigidArea(new Dimension(5, 5)));
		creationDateFromDatePicker =getDatePicker();
		creationDatePanel.add(creationDateFromDatePicker);
		creationDateTolabel = new JLabel(" and ");
		InjectUtils.assignArialPlainFont(creationDateTolabel,InjectMain.FONT_SIZE_NORMAL);
		creationDateTolabel.setOpaque(false);
		creationDateTolabel.setForeground(Color.WHITE);
		creationDateTolabel.setVisible(false);
		creationDatePanel.add(creationDateTolabel);
		creationDateToDatePicker =getDatePicker();
		creationDateToDatePicker.setVisible(false);
		creationDatePanel.add(creationDateToDatePicker);
		processActionEnableCreationDate();
		
		lastUpdateDateCheckBox=new JCheckBox("Last Update Date");
		InjectUtils.assignArialPlainFont(lastUpdateDateCheckBox,InjectMain.FONT_SIZE_NORMAL);
		lastUpdateDateCheckBox.setOpaque(false);
		lastUpdateDateCheckBox.setForeground(Color.WHITE);
		lastUpdateDateCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
			}
		}
				);
		UIUtils.setDimension(lastUpdateDateCheckBox,130,23); 
		lastUpdateDateCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionEnableLastUpdateDate();
			}
		});
		lastUpdateDatePanel.add(lastUpdateDateCheckBox);
		lastUpdateDateComboBox= new JComboBox<String>(comparatorListArray);
		lastUpdateDateComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
					String item = (String)event.getItem();
					if (item.equalsIgnoreCase(COMPARISON_BETWEEN)) {
						lastUpdateDateTolabel.setVisible(true);
						lastUpdateDateToDatePicker.setVisible(true);
	                 }
	                 else {
	                	 lastUpdateDateTolabel.setVisible(false);
	                	 lastUpdateDateToDatePicker.setVisible(false);
	                 }
	              }
	        }
	    });
		UIUtils.setDimension(lastUpdateDateComboBox,80,23); 
		lastUpdateDatePanel.add(lastUpdateDateComboBox);
		lastUpdateDatePanel.add(Box.createRigidArea(new Dimension(5, 5)));
		lastUpdateDateFromDatePicker =getDatePicker();
		lastUpdateDatePanel.add(lastUpdateDateFromDatePicker);
		lastUpdateDateTolabel = new JLabel(" and ");
		InjectUtils.assignArialPlainFont(lastUpdateDateTolabel,InjectMain.FONT_SIZE_NORMAL);
		lastUpdateDateTolabel.setOpaque(false);
		lastUpdateDateTolabel.setForeground(Color.WHITE);
		lastUpdateDateTolabel.setVisible(false);
		lastUpdateDatePanel.add(lastUpdateDateTolabel);
		lastUpdateDateToDatePicker =getDatePicker();
		lastUpdateDateToDatePicker.setVisible(false);
		lastUpdateDatePanel.add(lastUpdateDateToDatePicker);
		processActionEnableLastUpdateDate();
	}
	
	protected void processActionEnableCreationDate() {
		creationDateComboBox.setEnabled(creationDateCheckBox.isSelected());
		creationDateFromDatePicker.setEnabled(creationDateCheckBox.isSelected());
		creationDateToDatePicker.setEnabled(creationDateCheckBox.isSelected());
	}
	
	protected void processActionEnableLastUpdateDate() {
		lastUpdateDateComboBox.setEnabled(lastUpdateDateCheckBox.isSelected());
		lastUpdateDateFromDatePicker.setEnabled(lastUpdateDateCheckBox.isSelected());
		lastUpdateDateToDatePicker.setEnabled(lastUpdateDateCheckBox.isSelected());
	}

	public void resetFilter() {
		creationDateCheckBox.setSelected(false);
		lastUpdateDateCheckBox.setSelected(false);
	}
	
	private JXDatePicker getDatePicker() {
		final JXDatePicker datePicker = new JXDatePicker(new Date());
		datePicker.setFormats("dd-MMM-yyyy");
		datePicker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
			}
		});
		/*
		PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if ("date".equals(e.getPropertyName())) {
					label.setText(creationDateFromDatePicker.getDate().toString());
				}
			}
		};
		*/
		return datePicker;
	}
	
	public boolean isCreationDateEnabled() {
		return creationDateCheckBox.isSelected();
	}
	
	public boolean isLastUpdateDateEnabled() {
		return lastUpdateDateCheckBox.isSelected();
	}
	
	public String getCreationDateComparator() {
		return (String) creationDateComboBox.getSelectedItem();
	}
	
	public String getLastUpdateDateComparator() {
		return (String) lastUpdateDateComboBox.getSelectedItem();
	}
	
	public Date getFromCreationDate() {
		return creationDateFromDatePicker.getDate();
	}
	
	public Date getToCreationDate() {
		return creationDateToDatePicker.getDate();
	}
	
	public Date getFromLastUpdateDate() {
		return lastUpdateDateFromDatePicker.getDate();
	}
	
	public Date getToLastUpdateDate() {
		return lastUpdateDateToDatePicker.getDate();
	}

	public FiltersPanel getFiltersPanel() {
		return filtersPanel;
	}
	
}