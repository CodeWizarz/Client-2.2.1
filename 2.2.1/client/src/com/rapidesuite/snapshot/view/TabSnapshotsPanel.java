package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class TabSnapshotsPanel  extends JPanel {

	private MainPanel mainPanel;
	private SnapshotsActionsPanel snapshotsActionsPanel;
	private SnapshotsGridPanel snapshotsGridPanel;
	private SnapshotPackageSelectionPanel snapshotPackageSelectionPanel;
	private ServerSelectionPanel serverSelectionPanel;
	private SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame;
	private SnapshotCreationFrame snapshotCreationFrame;	
	private SnapshotViewerFrame snapshotViewerFrame;

	public TabSnapshotsPanel(MainPanel mainPanel) {
		this.mainPanel=mainPanel;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#4b4f4e"));
		createComponents();
	}
	
	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(northPanel,BorderLayout.NORTH);
		snapshotPackageSelectionPanel=new SnapshotPackageSelectionPanel(this);
		serverSelectionPanel=new ServerSelectionPanel(this);
		northPanel.add(serverSelectionPanel);
		/*
		JLabel animatedLabel=new JLabel();
		ImageIcon ii=null;
		URL iconURL =null;
		iconURL = this.getClass().getResource("/images/snapshot/animated.gif");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		animatedLabel.setIcon(ii);
		northPanel.add(Box.createHorizontalGlue());
		northPanel.add(animatedLabel);
		*/
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		add(centerPanel,BorderLayout.CENTER);
		snapshotsGridPanel=new SnapshotsGridPanel(this);
		centerPanel.add(snapshotsGridPanel);
		
		JPanel southPanel=new JPanel();
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		add(southPanel,BorderLayout.SOUTH);
		snapshotsActionsPanel=new SnapshotsActionsPanel(this);
		southPanel.add(snapshotsActionsPanel);
	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}
	
	public SnapshotsActionsPanel getSnapshotsActionsPanel() {
		return snapshotsActionsPanel;
	}

	public SnapshotsGridPanel getSnapshotsGridPanel() {
		return snapshotsGridPanel;
	}

	public SnapshotPackageSelectionPanel getSnapshotPackageSelectionPanel() {
		return snapshotPackageSelectionPanel;
	}

	public ServerSelectionPanel getServerSelectionPanel() {
		return serverSelectionPanel;
	}

	public void setSnapshotComparisonAnalysisFrame(SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame) {
		this.snapshotComparisonAnalysisFrame=snapshotComparisonAnalysisFrame;		
	}

	public SnapshotComparisonAnalysisFrame getSnapshotComparisonAnalysisFrame() {
		return snapshotComparisonAnalysisFrame;
	}

	public void setSnapshotViewerFrame(SnapshotViewerFrame snapshotViewerFrame) {
		this.snapshotViewerFrame=snapshotViewerFrame;
	}

	public SnapshotViewerFrame getSnapshotViewerFrame() {
		return snapshotViewerFrame;
	}

	public SnapshotCreationFrame getSnapshotCreationFrame() {
		return snapshotCreationFrame;
	}

	public void setSnapshotCreationFrame(SnapshotCreationFrame snapshotCreationFrame) {
		this.snapshotCreationFrame = snapshotCreationFrame;
	}
	
}
