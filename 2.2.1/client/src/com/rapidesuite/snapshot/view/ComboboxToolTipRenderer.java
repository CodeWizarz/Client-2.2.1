package com.rapidesuite.snapshot.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ComboboxToolTipRenderer extends DefaultListCellRenderer {

	private ArrayList<String> tooltips;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		JComponent comp = (JComponent) super.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);

		if (-1 < index && null != value && null != tooltips && !tooltips.isEmpty()) {
			String val= tooltips.get(index);
			list.setToolTipText(val);
		}
		return comp;
	}

	public void setTooltips(ArrayList<String> tooltips) {
		this.tooltips = tooltips;
	}

}
