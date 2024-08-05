package com.rapidesuite.snapshot.view;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class AbstractFiltersPanel extends JPanel {
	
	protected JLabel unappliedFiltersLabel;
	
	protected TabSnapshotsPanel tabSnapshotsPanel;
	protected JFrame rootFrame;
	protected JButton	applyFilteringButton;
	protected JButton	resetFilteringButton;

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public JFrame getRootFrame() {
		return rootFrame;
	}

	public JButton getApplyFilteringButton() {
		return applyFilteringButton;
	}

	public JButton getResetFilteringButton() {
		return resetFilteringButton;
	}	

}
