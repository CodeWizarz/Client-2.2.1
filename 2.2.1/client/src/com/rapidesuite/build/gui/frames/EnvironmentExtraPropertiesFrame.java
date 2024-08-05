package com.rapidesuite.build.gui.frames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.gui.EachRowEditor;
import com.rapidesuite.build.gui.EachRowRenderer;
import com.rapidesuite.build.gui.PasswordRenderer;
import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class EnvironmentExtraPropertiesFrame extends JFrame
{

	private JTable table;
	private JButton addRowButton;
	private JButton addProtectedRowButton;
	private JButton deleteRowButton;
	private DefaultTableModel model;
	private PasswordRenderer passwordRenderer;
	private DefaultCellEditor passwordEditor;
	private EachRowRenderer rowRenderer;
	private EachRowEditor rowEditor;

	public EnvironmentExtraPropertiesFrame(String title)
	{
		createComponents(title);
	}

	public void stopCellEditing()
	{
		if ( table.getCellEditor() != null )
		{
			table.getCellEditor().stopCellEditing();
		}
	}

	private void createComponents(String title)
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.setTitle(title);
		this.getContentPane().setLayout(new BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				stopCellEditing();
				setVisible(false);
			}
		});

		model = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column)
			{
				return true;
			}
		};
		String firstColumnName = "Property Name";
		String secondColumnName = "Property Value";
		model.setDataVector(null, new Object[] { firstColumnName, secondColumnName });

		passwordRenderer = new PasswordRenderer();
		rowRenderer = new EachRowRenderer();

		JPasswordField pf = new JPasswordField();
		passwordEditor = new DefaultCellEditor(pf);
		table = new JTable(model);
		rowEditor = new EachRowEditor(table);

		table.getColumn(secondColumnName).setCellRenderer(rowRenderer);
		table.getColumn(secondColumnName).setCellEditor(rowEditor);

		JScrollPane scroll = new JScrollPane(table);

		addRowButton = GUIUtils.getButton(this.getClass(), "Add row", "/images/add-item-blue.gif");
		addRowButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				stopCellEditing();
				addStandardRow(null, null);
			}
		});
		addProtectedRowButton = GUIUtils.getButton(this.getClass(), "Add Protected row", "/images/add-item-blue.gif");
		addProtectedRowButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				stopCellEditing();
				addProtectedRow(null, null);
			}
		});

		deleteRowButton = GUIUtils.getButton(this.getClass(), "Delete selected row(s)", "/images/delete16.gif");
		deleteRowButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				stopCellEditing();
				deleteSelectedRows();
			}
		});

		JLabel label = new JLabel("<html>Note: Double-click a cell to edit its contents.<br/><br/>"
				+ "If you define a Property Name starting and ending with ### then the Property Value will be "
				+ "used at injection time if that Property Name is found in the script.<br/> Example: Property "
				+ "Name: <b>###SYSADMIN_PWD###</b> and Property Value: <b>sys</b></html>");

		this.setLayout(new BorderLayout());

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		this.add(centerPanel, BorderLayout.CENTER);
		centerPanel.add(scroll);
		centerPanel.add(label);

		JPanel southPanel = new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		this.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(addRowButton);
		southPanel.add(addProtectedRowButton);
		southPanel.add(deleteRowButton);

		this.setSize(470, 500);
		this.setVisible(false);
	}

	public void addProtectedRow(String name, String value)
	{
	    if ( name == null || name.trim().length() == 0 )
	    {
	        name = SwiftBuildConstants.CUSTOM_PROPERTY_START_KEYWORD + "NEW_VALUE" + SwiftBuildConstants.CUSTOM_PROPERTY_END_KEYWORD;
	    }

		addStandardRow(name, value);
		int newRowIndex = table.getRowCount() - 1;
		rowRenderer.add(newRowIndex, passwordRenderer);
		rowEditor.setEditorAt(newRowIndex, passwordEditor);
		getExtraProperties();
	}

	public void addStandardRow(String name, String value)
	{
		Object[] row = new Object[2];
		row[0] = name;
		row[1] = value;
		model.addRow(row);
		int newRowIndex = table.getRowCount() - 1;
		table.setRowHeight(newRowIndex, 20);
	}

	public void deleteSelectedRows()
	{
		int numRows = table.getSelectedRows().length;
		for ( int i = 0; i < numRows; i++ )
		{
			model.removeRow(table.getSelectedRow());
		}
	}

	public void deleteAllRows()
	{
		GUIUtils.deleteAllRows(table);
		this.rowRenderer.clearAllRenderers();
	}

	public Map<String, String> getExtraProperties()
	{
		HashMap<String, String> properties = new HashMap<String, String>();

		int numRows = table.getRowCount();
		for ( int i = 0; i < numRows; i++ )
		{
			String name = (String) model.getValueAt(i, 0);
			String value = (String) model.getValueAt(i, 1);

			if ( name != null && !name.trim().equals("") && value != null && !value.trim().equals("") )
			{
				properties.put(SwiftBuildConstants.EXTRA_PROPERTY_KEYWORD + name, value);
			}
		}

		return properties;
	}

	public void addExtraProperties(Map<String, String> properties)
	{
	    deleteAllRows();

	    Iterator<String> iterator = properties.keySet().iterator();
		while ( iterator.hasNext() )
		{
			String name = iterator.next();
			String value = properties.get(name);
			if ( name != null && value != null && !value.isEmpty() && name.startsWith(SwiftBuildConstants.EXTRA_PROPERTY_KEYWORD) )
			{
				name = name.substring(SwiftBuildConstants.EXTRA_PROPERTY_KEYWORD.length());
				if ( name.startsWith(SwiftBuildConstants.CUSTOM_PROPERTY_START_KEYWORD) && name.endsWith(SwiftBuildConstants.CUSTOM_PROPERTY_END_KEYWORD) )
				{
					addProtectedRow(name, value);
				}
				else
				{
					addStandardRow(name, value);
				}
			}
		}
	}

}