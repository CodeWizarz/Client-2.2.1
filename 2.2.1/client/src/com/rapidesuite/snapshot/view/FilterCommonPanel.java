package com.rapidesuite.snapshot.view;

import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FilterCommonPanel  extends JPanel{

	protected JLabel unappliedFiltersLabel;
	private boolean hasUnappliedChanges;
	
	public FilterCommonPanel(JLabel unappliedFiltersLabel) {
		this.unappliedFiltersLabel=unappliedFiltersLabel;
	}
	
	public JLabel getUnappliedFiltersLabel() {
		return unappliedFiltersLabel;
	}
		
	public boolean hasUnappliedChanges() {
		return hasUnappliedChanges;
	}

	public void setHasUnappliedChanges(boolean hasUnappliedChanges) {
		this.hasUnappliedChanges = hasUnappliedChanges;
	}
}
