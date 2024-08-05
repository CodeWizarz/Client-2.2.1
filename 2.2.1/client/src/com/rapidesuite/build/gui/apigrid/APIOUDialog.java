package com.rapidesuite.build.gui.apigrid;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class APIOUDialog extends JDialog implements ActionListener
{

	private JPanel innerPanel;
	JComboBox operatingUnitNamesComboBox;
	private JButton applyButton;
	private TreeMap<String, Long> operatingUnitNameToIdMap;
	private final String NO_OPERATING_UNIT_VALUE = "N/A";
	private boolean isOUSelected;

	public APIOUDialog(JFrame frame, Map<String, String> environmentProperties)
	{
		super(frame, true);
		innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		// createEmptyBorder(int top,int left,int bottom,int right)
		innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		getContentPane().add(innerPanel);

		JLabel label = new JLabel("Select the Operating unit name to retrieve lookup data:");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		innerPanel.add(label);
		innerPanel.add(Box.createVerticalGlue());

		operatingUnitNamesComboBox = new JComboBox();
		operatingUnitNamesComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		tempPanel.add(Box.createHorizontalGlue());
		tempPanel.add(operatingUnitNamesComboBox);
		tempPanel.add(Box.createHorizontalGlue());

		innerPanel.add(tempPanel);
		innerPanel.add(Box.createVerticalGlue());
		applyButton = new JButton("Apply");
		applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		applyButton.addActionListener(this);
		innerPanel.add(applyButton);

		try
		{
			String txt = "Please wait, retrieving operating unit names...";
			JWindow window = GUIUtils.getSplashWindow(frame, txt);
			window.setVisible(true);
			initializeOperatingUnitNamesDropDownList(environmentProperties);
			window.setVisible(false);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}

		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		if ( applyButton == e.getSource() )
		{
			isOUSelected = true;
			setVisible(false);
		}
	}

	public void initializeOperatingUnitNamesDropDownList(Map<String, String> environmentProperties)
	{
		try
		{
			operatingUnitNamesComboBox.removeAllItems();
			SortedMap<Long, String> idToNameMap = DatabaseUtils.getOperatingUnitIdToNameMap(environmentProperties, false, null);
			operatingUnitNameToIdMap = new TreeMap<String, Long>();
			for ( Map.Entry<Long, String> entry : idToNameMap.entrySet() )
			{
				operatingUnitNameToIdMap.put(entry.getValue(), entry.getKey());
			}
			if ( operatingUnitNameToIdMap.isEmpty() )
			{
				operatingUnitNamesComboBox.addItem(NO_OPERATING_UNIT_VALUE);
				return;
			}
			Iterator<String> iterator = operatingUnitNameToIdMap.keySet().iterator();
			while ( iterator.hasNext() )
			{
				String key = iterator.next();
				operatingUnitNamesComboBox.addItem(key);
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public TreeMap<String, Long> getOperatingUnitNameToIdMap()
	{
		return operatingUnitNameToIdMap;
	}

	public Long getSelectedOperatingUnitId()
	{
		String value = (String) operatingUnitNamesComboBox.getSelectedItem();
		if ( value.equalsIgnoreCase(NO_OPERATING_UNIT_VALUE) )
		{
			return new Long(-1);
		}
		Long temp= operatingUnitNameToIdMap.get(value);
		return temp;
	}

	public String getSelectedOperatingUnitName()
	{
		String value = (String) operatingUnitNamesComboBox.getSelectedItem();
		if ( value.equalsIgnoreCase(NO_OPERATING_UNIT_VALUE) )
		{
			return null;
		}
		return value;
	}

	public boolean isOUSelected()
	{
		return isOUSelected;
	}

}
