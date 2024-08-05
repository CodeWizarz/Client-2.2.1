package com.rapidesuite.snapshot.view.upgrade;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.SnapshotMain;

@SuppressWarnings("serial")
public class UpgradeMainFilterFrame extends JFrame{

	private EBSFiltersPanel ebsFiltersPanel;
	private TabUpgradeMainPanel tabUpgradeMainPanel;
	public static final int FRAME_WIDTH=1260;
	public static final int FRAME_HEIGHT=320;
	
	public UpgradeMainFilterFrame(TabUpgradeMainPanel tabUpgradeMainPanel) {
		this.tabUpgradeMainPanel=tabUpgradeMainPanel;
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		setTitle("RAPIDUpgrade - Filters");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		createComponents();
	}
	
	public void createComponents(){
		JPanel mainPanel=new JPanel();
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#4b4f4e"));
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 5));

		add(mainPanel,BorderLayout.CENTER);
		
		ebsFiltersPanel=new EBSFiltersPanel(tabUpgradeMainPanel,tabUpgradeMainPanel.getUpgradeFrame().getSnapshotGridRecord());
		//ebsFiltersPanel.setBorder(BorderFactory.createTitledBorder(""));
		ebsFiltersPanel.setComponentsEnabled(tabUpgradeMainPanel.getUpgradeFrame().getSnapshotGridRecord()!=null);
		ebsFiltersPanel.getApplyFilteringButton().setEnabled(false);

		mainPanel.add(ebsFiltersPanel,BorderLayout.CENTER);
	}

	public EBSFiltersPanel getEbsFiltersPanel() {
		return ebsFiltersPanel;
	}
	
}
