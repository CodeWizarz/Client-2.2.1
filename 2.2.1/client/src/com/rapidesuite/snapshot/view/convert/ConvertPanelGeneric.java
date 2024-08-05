package com.rapidesuite.snapshot.view.convert;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.FilteringTable;

@SuppressWarnings("serial")
public class ConvertPanelGeneric  extends JPanel {

	protected ConvertMainPanel convertMainPanel;
	protected JTable table;
	protected FilteringTable filteringTable;
	protected String currentCellValueClicked;
	protected ConvertGridTotalsPanel convertGridTotalsPanel;
	protected JPanel northPanel;
	protected JPanel centerPanel;
	protected JPanel southPanel;
	
	public ConvertPanelGeneric(ConvertMainPanel convertMainPanel) {
		this.convertMainPanel=convertMainPanel;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		createComponents();
	}

	private void createComponents() {
		northPanel=InjectUtils.getXPanel(Component.CENTER_ALIGNMENT);
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#dbdcdf"));
		add(northPanel,BorderLayout.NORTH);

		centerPanel=new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		add(centerPanel,BorderLayout.CENTER);
		
		southPanel=new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		add(southPanel,BorderLayout.SOUTH);
	}

	public JTable getTable() {
		return table;
	}

}
