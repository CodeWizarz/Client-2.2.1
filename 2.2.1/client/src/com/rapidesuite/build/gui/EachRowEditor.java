package com.rapidesuite.build.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Hashtable;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

public class EachRowEditor implements TableCellEditor
{

	protected Hashtable<Integer, TableCellEditor> editors;
	protected TableCellEditor editor, defaultEditor;
	JTable table;

	public EachRowEditor(JTable table)
	{
		this.table = table;
		editors = new Hashtable<Integer, TableCellEditor>();
		defaultEditor = new DefaultCellEditor(new JTextField());
	}

	public void setEditorAt(int row, TableCellEditor editor)
	{
		editors.put(new Integer(row), editor);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{

		return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	public Object getCellEditorValue()
	{
		return editor.getCellEditorValue();
	}

	public boolean stopCellEditing()
	{
		return editor.stopCellEditing();
	}

	public void cancelCellEditing()
	{
		editor.cancelCellEditing();
	}

	public boolean isCellEditable(EventObject anEvent)
	{
		if ( anEvent instanceof MouseEvent )
		{
			selectEditor((MouseEvent) anEvent);
		}
		if ( editor != null )
			return editor.isCellEditable(anEvent);
		return false;
	}

	public void addCellEditorListener(CellEditorListener l)
	{
		editor.addCellEditorListener(l);
	}

	public void removeCellEditorListener(CellEditorListener l)
	{
		editor.removeCellEditorListener(l);
	}

	public boolean shouldSelectCell(EventObject anEvent)
	{
		selectEditor((MouseEvent) anEvent);
		return editor.shouldSelectCell(anEvent);
	}

	protected void selectEditor(MouseEvent e)
	{
		int row;
		if ( e == null )
		{
			row = table.getSelectionModel().getAnchorSelectionIndex();
		}
		else
		{
			row = table.rowAtPoint(e.getPoint());
		}
		editor = editors.get(new Integer(row));
		if ( editor == null )
		{
			editor = defaultEditor;
		}
	}

}