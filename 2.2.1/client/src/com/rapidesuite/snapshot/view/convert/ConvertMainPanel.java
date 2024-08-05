package com.rapidesuite.snapshot.view.convert;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

@SuppressWarnings("serial")
public class ConvertMainPanel extends JPanel{

	private ConvertFrame convertFrame;
	private ConvertSourcePanel convertSourcePanel;
	private ConvertTargetPanel convertTargetPanel;
	
	public ConvertMainPanel(ConvertFrame convertFrame) {
		this.convertFrame=convertFrame;
		
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#4b4f4e"));
		setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		
		JPanel centerPanel=new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		add(centerPanel,BorderLayout.CENTER);
		
		convertSourcePanel=new ConvertSourcePanel(this);
		convertTargetPanel=new ConvertTargetPanel(this);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,convertSourcePanel, convertTargetPanel);
		splitPane.setOpaque(false);
		splitPane.setDividerSize(20);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		centerPanel.add(splitPane);
		
		//BasicSplitPaneDivider divider = (BasicSplitPaneDivider) splitPane.getComponent(2);
		//divider.setBackground(Color.decode("#4b4f4e"));
		//divider.setBorder(null);
	}

	public ConvertFrame getConvertFrame() {
		return convertFrame;
	}

	public ConvertSourcePanel getConvertSourcePanel() {
		return convertSourcePanel;
	}
	
	public ConvertTargetPanel getConvertTargetPanel() {
		return convertTargetPanel;
	}

}