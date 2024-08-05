/**************************************************
 * $Revision: 37672 $:
 * $Author: john.snell $:
 * $Date: 2013-11-26 14:05:53 +0700 (Tue, 26 Nov 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/OracleDatesSelectionPanel.java $:
 * $Id: OracleDatesSelectionPanel.java 37672 2013-11-26 07:05:53Z john.snell $:
 */

package com.rapidesuite.reverse.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import org.jdesktop.swingx.JXDatePicker;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.reverse.DataExtractionConstants;

@SuppressWarnings("serial")
public class OracleDatesSelectionPanel extends JPanel
{

	private DataExtractionPanel panel;
	private JXDatePicker fromDatePicker;
	private JXDatePicker toDatePicker;

	private JLabel fromDateJLabel;
	private JCheckBox isFromDateEnabledJCheckBox;
	private JSpinner fromSpinner;

	private JLabel toDateJLabel;
	private JCheckBox isToDateEnabledJCheckBox;
	private JSpinner toSpinner;


    public static final String LAST_UPDATE_DATE = "LAST_UPDATE_DATE";
    public static final String CREATION_DATE = "CREATION_DATE";
    private JRadioButton updateDateButton = new JRadioButton(LAST_UPDATE_DATE);
    private JRadioButton createDateButton = new JRadioButton(CREATION_DATE);

    public boolean isDateFilteringByUpdateDate()
    {
        return this.updateDateButton.isSelected();
    }


	public OracleDatesSelectionPanel(DataExtractionPanel panel)
	{
		this.panel= panel;
		createComponents();
	}

	public void createComponents()
	{
		Properties prop= panel.getSwiftGUIMain().getReplacementsProperties();
		String fromDate=prop.getProperty(DataExtractionConstants.SEEDED_DATE);
		Date initDate=Utils.getDate(fromDate);
		if (initDate==null) {
			initDate=new Date();
		}
		fromDateJLabel= new JLabel("From: ");
		isFromDateEnabledJCheckBox=new JCheckBox();
		isFromDateEnabledJCheckBox.setSelected(true);
		isFromDateEnabledJCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (isFromDateEnabledJCheckBox.isSelected()) {
					fromDatePicker.setEnabled(true);
					fromSpinner.setEnabled(true);
				}
				else {
					if (!isToDateEnabledJCheckBox.isSelected()) {
						GUIUtils.popupErrorMessage("At least one date must be enabled.");
						isFromDateEnabledJCheckBox.setSelected(true);
					}
					else {
						fromDatePicker.setEnabled(false);
						fromSpinner.setEnabled(false);
					}
				}
			}
		}
		);
		toDateJLabel= new JLabel("To:");
		isToDateEnabledJCheckBox=new JCheckBox();
		isToDateEnabledJCheckBox.setSelected(false);
		isToDateEnabledJCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (isToDateEnabledJCheckBox.isSelected()) {
					toDatePicker.setEnabled(true);
					toSpinner.setEnabled(true);
				}
				else {
					if (!isFromDateEnabledJCheckBox.isSelected()) {
						GUIUtils.popupErrorMessage("At least one date must be enabled.");
						isToDateEnabledJCheckBox.setSelected(true);
					}
					else {
						toDatePicker.setEnabled(false);
						toSpinner.setEnabled(false);
					}
				}
			}
		}
		);

		setLayout(new BorderLayout());

		JPanel northPanel= new JPanel();
		northPanel.setBorder(BorderFactory.createEmptyBorder(50, 15, 5, 5));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		add(northPanel, BorderLayout.NORTH);

		JPanel panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		//panel.setBackground(Color.GREEN);
		northPanel.add(panel);
		panel.add(new JLabel("Date filtering using: "));

		final String LAST_UPDATE_DATE = "LAST_UPDATE_DATE";
		final String CREATION_DATE = "CREATION_DATE";
	    updateDateButton.setActionCommand(LAST_UPDATE_DATE);
	    updateDateButton.setSelected(true);
	    createDateButton.setActionCommand(CREATION_DATE);

	    ButtonGroup group = new ButtonGroup();
	    group.add(updateDateButton);
	    group.add(createDateButton);
	    panel.add(updateDateButton);
	    panel.add(createDateButton);


		panel= new JPanel();
		northPanel.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createVerticalStrut(50));

		SimpleDateFormat sdf=new SimpleDateFormat(UtilsConstants.JAVA_DATE_FORMAT);
		panel= new JPanel();
		northPanel.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(fromDateJLabel);
		panel.add(isFromDateEnabledJCheckBox);
		panel.add(Box.createHorizontalStrut(5));
		fromDatePicker = new JXDatePicker(initDate);
		fromDatePicker.setFormats(sdf);
		GUIUtils.setComponentDimension(fromDatePicker, 130, 22);
		panel.add(fromDatePicker);
	    fromSpinner = getSpinner(initDate);
	    GUIUtils.setComponentDimension(fromSpinner, 65, 22);
	    panel.add(new JLabel(" at "));
	    panel.add(fromSpinner);

		panel.add(Box.createHorizontalStrut(50));
		panel.add(toDateJLabel);
		panel.add(isToDateEnabledJCheckBox);
		panel.add(Box.createHorizontalStrut(5));
		toDatePicker = new JXDatePicker();
		toDatePicker.setFormats(sdf);
		toDatePicker.setEnabled(false);
		GUIUtils.setComponentDimension(toDatePicker, 130, 22);
		panel.add(toDatePicker);
		toSpinner = getSpinner(getDateNoTime());
		GUIUtils.setComponentDimension(toSpinner, 65, 22);
	    panel.add(new JLabel(" at "));
	    panel.add(toSpinner);
		panel.add(Box.createHorizontalStrut(250));
	}

	public List<Component> getAllComponents() {
		List<Component> list=new ArrayList<Component>();
		list.add(fromDatePicker);
		list.add(toDatePicker);
		list.add(isFromDateEnabledJCheckBox);
		list.add(isToDateEnabledJCheckBox);
		list.add(fromSpinner);
		list.add(toSpinner);

		return list;
	}

	public void lockAll() {
		GUIUtils.setEnabledOnComponents(getAllComponents(),false);
	}

	public void unlockAll() {
		boolean isFromDatePickerSelected=isFromDateEnabledJCheckBox.isSelected();
		boolean isToDatePickerSelected=isToDateEnabledJCheckBox.isSelected();
		GUIUtils.setEnabledOnComponents(getAllComponents(),true);
		if (!isFromDatePickerSelected) {
			fromDatePicker.setEnabled(false);
		}
		if (!isToDatePickerSelected) {
			toDatePicker.setEnabled(false);
		}
	}

	private JSpinner getSpinner(Date date) {
		SpinnerDateModel sm = new SpinnerDateModel(date, null, null, Calendar.HOUR_OF_DAY);
		JSpinner spinner = new JSpinner(sm);
	    JSpinner.DateEditor de = new JSpinner.DateEditor(spinner, "HH:mm:ss");
	    spinner.setEditor(de);
	    spinner.setEnabled(true);
	    return spinner;
	}

	public String getFormattedFromDate() {
		return getFormattedDate(fromSpinner,fromDatePicker.getDate());
	}

	public String getFormattedToDate() {
		return getFormattedDate(toSpinner,toDatePicker.getDate());
	}

	private String getFormattedDate(JSpinner spinner,Date date) {
		if (date==null) {
			return "";
		}
		Date time=(Date)spinner.getValue();
		Calendar calTime=Calendar.getInstance();
		calTime.setTime(time);
		Calendar calDate=Calendar.getInstance();
		calDate.setTime(date);
		calDate.set( Calendar.HOUR_OF_DAY, calTime.get( Calendar.HOUR_OF_DAY ) );
		calDate.set( Calendar.MINUTE, calTime.get( Calendar.MINUTE  ) );
		calDate.set( Calendar.SECOND , calTime.get( Calendar.SECOND  ) );
		return Utils.getFormattedDate(calDate.getTime());
	}

	private Date getDateNoTime() {
		Calendar calDate=Calendar.getInstance();
		calDate.set( Calendar.HOUR_OF_DAY,0 );
		calDate.set( Calendar.MINUTE, 0 );
		calDate.set( Calendar.SECOND ,0 );
		return calDate.getTime();
	}

	public void restoreDates(String fromDateParameter,String toDateParameter){
		try{
			if ( (fromDateParameter==null || fromDateParameter.isEmpty()) &&
				 (toDateParameter==null || toDateParameter.isEmpty())
			) {
				Properties prop=panel.getSwiftGUIMain().getReplacementsProperties();
				fromDateParameter=prop.getProperty(DataExtractionConstants.SEEDED_DATE);
				isFromDateEnabledJCheckBox.setSelected(true);
				return;
			}
			Date fromDate=Utils.getDate(fromDateParameter);
			if (fromDate==null) {
				fromDatePicker.setEnabled(false);
				fromSpinner.setEnabled(false);
			}
			else {
				fromDatePicker.setDate(fromDate);
				fromDatePicker.setEnabled(true);
				fromSpinner.setEnabled(true);
				fromSpinner.setValue(fromDate);
			}

			Date toDate=Utils.getDate(toDateParameter);
			if (toDate==null) {
				toDatePicker.setEnabled(false);
				toSpinner.setEnabled(false);
			}
			else {
				isToDateEnabledJCheckBox.setSelected(true);
				toDatePicker.setDate(toDate);
				toDatePicker.setEnabled(true);
				toSpinner.setEnabled(true);
				toSpinner.setValue(toDate);
			}
		}
		catch (Exception e){
			FileUtils.printStackTrace(e);
		}
	}

	public boolean isFromDateEnabled() {
		return isFromDateEnabledJCheckBox.isSelected();
	}

	public boolean isToDateEnabled() {
		return isToDateEnabledJCheckBox.isSelected();
	}

}