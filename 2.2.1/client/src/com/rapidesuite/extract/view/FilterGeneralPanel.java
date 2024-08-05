package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class FilterGeneralPanel extends JPanel {

	private JCheckBox excludeSeededDataCheckBox;
	private JPanel centerPanel;
	private JCheckBox filterShowGlobalLevelFormsCheckBox;
	private FiltersPanel filtersPanel;
	
	public FilterGeneralPanel(FiltersPanel filtersPanel) {
		this.filtersPanel=filtersPanel;
		setLayout(new BorderLayout());
		
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"General Filters",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
		CompoundBorder compoundBorder=new CompoundBorder(titledBorder,new EmptyBorder(0, 0, 0, 0));
		setBorder(compoundBorder);
		
		createComponents();
	}

	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(false);
		northPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(northPanel,BorderLayout.NORTH);
		
		centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#047fc0"));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		add(centerPanel,BorderLayout.CENTER);
		
		excludeSeededDataCheckBox=new JCheckBox("Exclude Seeded data");
		InjectUtils.assignArialPlainFont(excludeSeededDataCheckBox,InjectMain.FONT_SIZE_NORMAL);
		excludeSeededDataCheckBox.setOpaque(false);
		excludeSeededDataCheckBox.setForeground(Color.WHITE);
		excludeSeededDataCheckBox.setSelected(true);
		excludeSeededDataCheckBox.setFocusPainted(false);
		excludeSeededDataCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
			}
		}
				);
		centerPanel.add(excludeSeededDataCheckBox);
		
		filterShowGlobalLevelFormsCheckBox=new JCheckBox("Show Instance Level Forms");
		InjectUtils.assignArialPlainFont(filterShowGlobalLevelFormsCheckBox,InjectMain.FONT_SIZE_NORMAL);
		filterShowGlobalLevelFormsCheckBox.setForeground(Color.WHITE);
		filterShowGlobalLevelFormsCheckBox.setOpaque(false);
		//centerPanel.add(filterShowGlobalLevelFormsCheckBox);
		filterShowGlobalLevelFormsCheckBox.setSelected(true);
		filterShowGlobalLevelFormsCheckBox.setFocusPainted(false);	
		filterShowGlobalLevelFormsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				filtersPanel.getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
			}
		}
				);
	}
	
	public void resetFilter() {
		excludeSeededDataCheckBox.setSelected(true);
		filterShowGlobalLevelFormsCheckBox.setSelected(true);
	}
	
	public void setComponentsEnabled(boolean isEnabled) {
		excludeSeededDataCheckBox.setEnabled(isEnabled);
		filterShowGlobalLevelFormsCheckBox.setEnabled(isEnabled);
	}
	
	public boolean isExcludeSeededData() {
		return excludeSeededDataCheckBox.isSelected();
	}
	
	public boolean isShowGlobalLevelForms() {
		return filterShowGlobalLevelFormsCheckBox.isSelected();
	}

	public void lockUI() {
		setComponentsEnabled(false);
	}

	public void unlockUI() {
		setComponentsEnabled(true);
	}
	
}