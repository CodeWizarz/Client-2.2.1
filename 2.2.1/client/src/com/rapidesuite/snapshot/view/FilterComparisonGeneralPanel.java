package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class FilterComparisonGeneralPanel extends FilterCommonPanel {

	private JCheckBox ignoreSeededUserChangesCheckBox;
	private JPanel centerPanel;
	
	public FilterComparisonGeneralPanel(JLabel unappliedFiltersLabel) {
		super(unappliedFiltersLabel);
		setLayout(new BorderLayout());
		
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"General Options",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
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
		
		ignoreSeededUserChangesCheckBox=new JCheckBox("Ignore Seeded Users Changes");
		InjectUtils.assignArialPlainFont(ignoreSeededUserChangesCheckBox,InjectMain.FONT_SIZE_NORMAL);
		ignoreSeededUserChangesCheckBox.setOpaque(false);
		ignoreSeededUserChangesCheckBox.setForeground(Color.WHITE);
		ignoreSeededUserChangesCheckBox.setSelected(true);
		ignoreSeededUserChangesCheckBox.setFocusPainted(false);
		ignoreSeededUserChangesCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				setHasUnappliedChanges(true);
				unappliedFiltersLabel.setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
			}
		}
				);
		centerPanel.add(ignoreSeededUserChangesCheckBox);
		
	}
	
	public void resetFilter() {
		ignoreSeededUserChangesCheckBox.setSelected(true);
	}
	
	public void setComponentsEnabled(boolean isEnabled) {
		ignoreSeededUserChangesCheckBox.setEnabled(isEnabled);
	}
	
	public boolean isIgnoreSeededUserChanges() {
		return ignoreSeededUserChangesCheckBox.isSelected();
	}
	
}
