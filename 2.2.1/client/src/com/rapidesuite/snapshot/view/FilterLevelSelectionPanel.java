package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.ModelUtils.SELECTION_LEVEL;

@SuppressWarnings("serial")
public class FilterLevelSelectionPanel extends FilterCommonPanel {
	
	private JCheckBox selectionCheckBox;
	private JPanel filterSelectionPanel;
	private AbstractFiltersPanel filtersPanel;
	
	private CustomJRadioButton[] levelSelectionRadioButtons;
	
	public FilterLevelSelectionPanel(AbstractFiltersPanel filtersPanel, JLabel unappliedFiltersLabel) {
		super(unappliedFiltersLabel);
		setLayout(new BorderLayout());
		this.filtersPanel=filtersPanel;
		setOpaque(true);
		setBackground(Color.decode("#047fc0"));
		Border lineBorder=BorderFactory.createLineBorder(Color.decode("#4b4f4e"),2);
		TitledBorder titledBorder=BorderFactory.createTitledBorder(lineBorder,"Level Selection",TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial",Font.PLAIN,12), Color.white);
		CompoundBorder compoundBorder=new CompoundBorder(titledBorder,new EmptyBorder(0, 0, 0, 0));
		setBorder(compoundBorder);
		
		levelSelectionRadioButtons = new CustomJRadioButton[SELECTION_LEVEL.values().length];
		
		createComponents();
	}
	
	public void createComponents(){
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		add(centerPanel,BorderLayout.CENTER);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerPanel.add(tempPanel);
		selectionCheckBox=new JCheckBox("Enable Selection (Global is selected by default)");
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
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerPanel.add(tempPanel);
		
		filterSelectionPanel=new JPanel();
		filterSelectionPanel.setVisible(false);
		filterSelectionPanel.setOpaque(false);
		filterSelectionPanel.setLayout(new BoxLayout(filterSelectionPanel, BoxLayout.Y_AXIS));
		filterSelectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.add(filterSelectionPanel);
		
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSelection();
			}
		};
		
		ButtonGroup radioGroup = new ButtonGroup();
		for(int i=0; i<SELECTION_LEVEL.values().length; i++) {
			levelSelectionRadioButtons[i] = new CustomJRadioButton(SELECTION_LEVEL.values()[i].getLevel(), (i==0?true:false), actionListener);
			radioGroup.add(levelSelectionRadioButtons[i]);
			filterSelectionPanel.add(levelSelectionRadioButtons[i]);
		}
	}

	protected void processActionSelection() {
		filterSelectionPanel.setVisible(selectionCheckBox.isSelected());
		if(!selectionCheckBox.isSelected()) {
			resetFilter();
		}
		setUnappliedFilter();
		filtersPanel.getApplyFilteringButton().setEnabled(true);
	}

	public void setComponentsEnabled(boolean isEnabled) {
		selectionCheckBox.setEnabled(isEnabled);
	}	
	
	public void resetFilter() {
		selectionCheckBox.setSelected(false);
		filterSelectionPanel.setVisible(false);
		
		for(int i=0; i<SELECTION_LEVEL.values().length; i++) {
			levelSelectionRadioButtons[i].setSelected(i==0?true:false);
		}
	}

	public void setUnappliedFilter() {
		setHasUnappliedChanges(true);
		unappliedFiltersLabel.setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
	}
	
	public boolean isLevelSelectionFilterEnabled() {
		return selectionCheckBox.isSelected();
	}
	
	public SELECTION_LEVEL getSelectionLevel() {
		for(int i=0; i<SELECTION_LEVEL.values().length; i++) {
			if(levelSelectionRadioButtons[i].isSelected())
				return SELECTION_LEVEL.values()[i];
		}
		
		return null;
	}
}

@SuppressWarnings("serial")
class CustomJRadioButton extends JRadioButton {
	public CustomJRadioButton() {
		super();
		this.customize();
	}
	
	public CustomJRadioButton(String text, ActionListener actionListener) {
		super(text);
		this.customize();
		this.addActionListener(actionListener);
	}
	
	public CustomJRadioButton(String text, boolean selected, ActionListener actionListener) {
		super(text, selected);
		this.customize();
		this.addActionListener(actionListener);
	}
	
	private void customize() {
		this.setBackground(Color.decode("#047fc0"));
		this.setForeground(Color.WHITE);
		this.setFont(new Font("Arial",Font.PLAIN,12));
	}
}