package com.rapidesuite.snapshot.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

public class RolloverMouseAdapter extends MouseAdapter {
	
	private int row = -1;
	private int column = -1;
	private JTable table;

	public RolloverMouseAdapter(JTable table) {
		this.table = table;
	}

	public boolean isRolloverCell(int row, int column) {
		return this.row == row && this.column == column;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int lastRow = row;
		int lastColumn = column;

		row = table.rowAtPoint(e.getPoint());
		column = table.columnAtPoint(e.getPoint());

		if (row == lastRow && column == lastColumn)
			return;

		if (row >= 0 && column >= 0) {
			table.repaint(table.getCellRect(row, column, false));
		}
		if (lastRow >= 0 && lastColumn >= 0) {
			table.repaint(table.getCellRect(lastRow, lastColumn, false));
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (row >= 0 && column >= 0) {
			table.repaint(table.getCellRect(row, column, false));
		}
		row = column = -1;
	}

}
