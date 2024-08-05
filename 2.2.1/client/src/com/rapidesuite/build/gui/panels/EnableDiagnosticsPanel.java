/**************************************************
 * $Revision: 46707 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-03-23 17:38:59 +0700 (Mon, 23 Mar 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/panels/EnableDiagnosticsPanel.java $:
 * $Id: EnableDiagnosticsPanel.java 46707 2015-03-23 10:38:59Z olivier.deruelle $:
 */

package com.rapidesuite.build.gui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.springframework.util.Assert;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.gui.WindowsExplorerStyleJTable;
import com.rapidesuite.build.gui.environment.EnableDiagnosticsButtonAction;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.gui.EnvironmentValidationProperty;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.client.SharedUtil;
import com.rapidesuite.core.utility.CoreUtil;

@SuppressWarnings("serial")
public class EnableDiagnosticsPanel extends SwiftBuildPropertiesValidationPanel implements TableModelListener
{
    private SortedMap<String,String> restrictToTheseUsersPasswords = null;
    
    public EnableDiagnosticsPanel(BuildMain BuildMain, SortedMap<String,String> restrictToTheseUsersPasswords)
    {
        super(BuildMain, false);
        this.restrictToTheseUsersPasswords = restrictToTheseUsersPasswords;
        createComponents();
    }

    private static enum ENABLE_DIAGNOSTICS_PANEL_COLUMN {
        //ugly hack - centering names by using whitespace - because creating the object to set the 'center' property
        //on horizontal alignment results in me also having to manually set many other properties normally taken care of by default,
        //which are not immediately obvious on how to set.
	        USER_NAME (0, "              User Name", new BigDecimal("0.50")),
	        STATUS    (1, "       Injectability Status", new BigDecimal("0.50"));

	        private final int index;
	        private final String header;
	        private final BigDecimal proportion;

	        private ENABLE_DIAGNOSTICS_PANEL_COLUMN(final int index, final String header, final BigDecimal proportion) {
	            this.index = index;
	            this.header = header;
	            this.proportion = proportion;
	        }
	        public int getIndex() {
	            return index;
	        }
	        public String getHeader() {
	            return header;
	        }
	        public double getProportion() {
	            return proportion.doubleValue();
	        }

	        public BigDecimal getProportionExact() {
	            return proportion;
	        }

	        static {
	            ENABLE_DIAGNOSTICS_PANEL_COLUMN[] columns = ENABLE_DIAGNOSTICS_PANEL_COLUMN.getSorted();
	            BigDecimal totalProportion = new BigDecimal(0);
	            for (int i = 0 ; i < columns.length ; i++) {
	                Assert.isTrue(i == columns[i].getIndex(), "ENABLE_DIAGNOSTICS_PANEL_COLUMN columns are not ordered");
	                totalProportion = totalProportion.add(columns[i].getProportionExact());
	            }
	            Assert.isTrue(BigDecimal.ONE.compareTo(totalProportion) == 0, "ENABLE_DIAGNOSTICS_PANEL_COLUMN column proportions sum to "+totalProportion.toPlainString()+" while they should sum to 1");
	        }


	        public static ENABLE_DIAGNOSTICS_PANEL_COLUMN[] getSorted() {
	            List<ENABLE_DIAGNOSTICS_PANEL_COLUMN> list = Arrays.asList(ENABLE_DIAGNOSTICS_PANEL_COLUMN.values());
	            Collections.sort(list, new Comparator<ENABLE_DIAGNOSTICS_PANEL_COLUMN>() {
	                @Override
	                public int compare(ENABLE_DIAGNOSTICS_PANEL_COLUMN o1,
	                        ENABLE_DIAGNOSTICS_PANEL_COLUMN o2) {
	                    if (o1.getIndex() < o2.getIndex()) {
	                        return -1;
	                    } else if (o1.getIndex() > o2.getIndex()) {
	                        return 1;
	                    } else {
	                        return 0;
	                    }
	                }
	            });
	            return list.toArray(new ENABLE_DIAGNOSTICS_PANEL_COLUMN[ENABLE_DIAGNOSTICS_PANEL_COLUMN.values().length]);
	        }
	    }
	    static {
	        ENABLE_DIAGNOSTICS_PANEL_COLUMN[] columns = ENABLE_DIAGNOSTICS_PANEL_COLUMN.getSorted();
	        for (int i = 0 ; i < columns.length ; i++) {
	            Assert.isTrue(i == columns[i].getIndex(), "ENABLE_DIAGNOSTICS_PANEL_COLUMN columns are not ordered");
	        }
	    }


	    private JButton enableButton;
	    private JPanel northPanel;
	    private JPanel centerPanel;
	    private JPanel southPanel;
	    private WindowsExplorerStyleJTable table;
	    private DefaultTableModel tableModel;
	    private EnableDiagnosticsButtonAction enableDiagnosticsAction;

	    public static final String BUTTON_TEXT_ENABLE = "Enable for Injection";
	    public static final String BUTTON_TEXT_STOP = "Stop Enabling";
	    public JButton getEnableButton()
	    {
	        return this.enableButton;
	    }

	    public void createComponents()
	    {
	        this.setLayout(new BorderLayout());
	        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	        northPanel = new JPanel();
	        northPanel.setLayout(new BorderLayout());
	        this.add(northPanel, BorderLayout.NORTH);

	        centerPanel = new JPanel();
	        centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Oracle Forms Injectability Validation Screen"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
	        this.add(centerPanel, BorderLayout.CENTER);
	        JLabel bwpUsersLabel = GUIUtils.getLabel("List of Oracle users detected in the Injector", true);
            centerPanel.add(Box.createRigidArea(new Dimension(10, 10)));
	        centerPanel.add(bwpUsersLabel);
	        centerPanel.add(Box.createRigidArea(new Dimension(10, 10)));

	        class WindowsExplorerStyleJTableCustomized extends WindowsExplorerStyleJTable {

	            private static final long serialVersionUID = -7202803705241678363L;

	            public WindowsExplorerStyleJTableCustomized() {
	                super();
	                addMouseMotionListener(new DefaultMouseMotionListener());
	                addMouseListener(new DefaultMouseListener());
	                addKeyListener(new DefaultKeyListener(ENABLE_DIAGNOSTICS_PANEL_COLUMN.USER_NAME.getIndex()));
	            }

	            @Override
	            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, final int columnIndex)
	            {
	                Component comp = super.prepareRenderer(renderer, rowIndex, columnIndex);
	                if (rowIndex < ((DefaultTableModel) getModel()).getRowCount()) {
	                    String status = (String) ((DefaultTableModel) getModel()).getValueAt(rowIndex, ENABLE_DIAGNOSTICS_PANEL_COLUMN.STATUS.getIndex());
	                    if (USER_STATUS.ENABLED.toString().equals(status)) {
	                        comp.setBackground(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_UNSELECTED_COLOR);
	                    } else if (USER_STATUS.DISABLED.toString().equals(status)) {
	                        comp.setBackground(SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_UNSELECTED_COLOR);
	                    } else if (USER_STATUS.DOES_NOT_EXIST.toString().equals(status)) {
	                        comp.setBackground(SwiftBuildConstants.INJECTOR_EXECUTION_LOG_MISMATCH_UNSELECTED_COLOR);
	                    }
	                }
	                return comp;
	            }
	        }

	        table = new WindowsExplorerStyleJTableCustomized();

	        initJTable();
	        JScrollPane scrollPane = new JScrollPane(table);
	        table.setScrollPane(scrollPane);
	        scrollPane.setPreferredSize(new Dimension(500, 550));
	        centerPanel.add(scrollPane, BorderLayout.CENTER);


	        enableButton = GUIUtils.getButton(BuildMain.getClass(), BUTTON_TEXT_ENABLE, SwiftBuildConstants.IMAGE_CHECK);
	        enableButton.setVisible(false);
	        enableButton.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent e)
	            {
	                executeEnableButton(false);
	            }
	        });
	        enableButton.setEnabled(true);

	        southPanel = new JPanel();
	        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
	        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
	        this.add(southPanel, BorderLayout.SOUTH);

	        JPanel tempPanel = new JPanel();
	        tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
	        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
	        tempPanel.add(enableButton);
	        southPanel.add(tempPanel);

	        enableDiagnosticsAction = new EnableDiagnosticsButtonAction(this);

            environmentValidationPanel = new EnvironmentValidationPanel("Status",
                    new ArrayList<EnvironmentValidationProperty>(),
                    new HashMap<String, String>(),
                    null,
                    null,
                    enableDiagnosticsAction,
                    null,
                    new Dimension(100, 430),
                    this,
                    this.BuildMain);

            southPanel.add(environmentValidationPanel);

	        tempPanel = new JPanel();
	        tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
	        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
	        southPanel.add(Box.createRigidArea(new Dimension(5, 0)));
	        southPanel.add(tempPanel);
    }

    public void initJTable()
    {
        tableModel = new DefaultTableModel()
        {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Class getColumnClass(int column)
            {
                Object valueAt = getValueAt(0, column);
                if ( valueAt != null )
                {
                    return valueAt.getClass();
                }
                return super.getClass();
            }

            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex)
            {
                return false;
            }
        };

        final ENABLE_DIAGNOSTICS_PANEL_COLUMN columns[] = ENABLE_DIAGNOSTICS_PANEL_COLUMN.getSorted();
        for ( ENABLE_DIAGNOSTICS_PANEL_COLUMN column : columns )
        {
            tableModel.addColumn(column.getHeader());
        }
        table.setModel(tableModel);
        table.getModel().addTableModelListener(this);
        for (int i = 0 ; i < columns.length ; i++) {
            TableColumn column = table.getTableHeader().getColumnModel().getColumn(columns[i].getIndex());
            table.resizeColumnByProportion(column, columns[i].getProportion());
        }

        DefaultTableCellRenderer dtcrBody = new DefaultTableCellRenderer();
        dtcrBody.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(String.class, dtcrBody);

        this.tableModel.addTableModelListener(new TableModelListener(){
            public void tableChanged(TableModelEvent e)
            {
                EnableDiagnosticsPanel.this.getEnableButton().setEnabled(EnableDiagnosticsPanel.this.getUserNames().size() > 0);
            }
        });
    }

    public void setEnableButtonEnableStatus()
    {
        this.getEnableButton().setEnabled(EnableDiagnosticsPanel.this.getUserNames().size() > 0);
    }

    public boolean executeEnableButton(boolean isSynchronous)
    {
        if ( getUserNames().size() > 0 )
        {
        	 Map<String,String> environmentProperties = BuildMain.getEnvironmentProperties();
             String responsibility = environmentProperties.get(SwiftBuildConstants.FLD_FORM_RESPONSIBILITY_KEY);
             if (responsibility==null || responsibility.trim().isEmpty()) {
                 String msg="Please return to the first panel (BWE) to enter a valid Oracle Responsibility that has access to the System Profile Form.\n"+
             "The current value is '"+responsibility+"'.";
                 GUIUtils.popupErrorMessage(msg);
                 return false;
             }
             else {
                 EnableDiagnosticsPanel.this.environmentValidationPanel.actionPerformedValidationButton(isSynchronous);
                 return true;
             }
        }
        else
        {
            EnableDiagnosticsPanel.this.environmentValidationPanel.setValidationSuccess(true);
            return false;
        }
    }

    public void next()
    {
        BuildMain.switchToPanel(SwiftBuildConstants.PANEL_INJECTORS_PACKAGE_EXECUTION);
    }


    public static enum ECB_STATUS
    {
        SUCCESS,
        FAIL,
        ABORT
    }
    

    private void refreshFormsUserListInternal_withMessages()
    {
        GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), "Please wait, loading user list...");
        try
        {
            int count = refreshFormsUserListInternal();
            GUIUtils.showSuccessMessage(environmentValidationPanel.getMessageLabel(), "Loaded injectability status for " + count + " users");
        }
        catch(Throwable tt)
        {
            GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), "Failed to load user list: " + CoreUtil.getAllThrowableMessages(tt));
        }
    }

    public void refreshFormsUserList(boolean synchronous, ECB_STATUS[] status) throws Exception
    {
        this.enableDiagnosticsAction.setLastStatusObject(status);
        Thread t = new Thread(new Runnable() {
            public void run() {
                refreshFormsUserListInternal_withMessages();
            }

          });
        t.start();

        boolean isRun=false;
        if ( synchronous )
        {
            t.join();
            
            setEnableButtonEnableStatus();
            if ( this.getEnableButton().isEnabled() )
            {
            	isRun= executeEnableButton(true);
                refreshFormsUserListInternal_withMessages();
            }
        }
        
        SortedMap<String, USER_STATUS> userMenuDiagnosticsEnabledMap = getUserMenuDiagnosticsEnabledMap(this.restrictToTheseUsersPasswords, BuildMain);

        synchronized ( status )
        {
            status[0] = null;
            Iterator<String> iterator=restrictToTheseUsersPasswords.keySet().iterator();
            while (iterator.hasNext() ) {
            	String userName=iterator.next();
         
                if ( !userMenuDiagnosticsEnabledMap.get(userName).equals(USER_STATUS.ENABLED) )
                {
                    status[0] = ECB_STATUS.FAIL;
                    if (isRun) {
                    	Map<String,String> environmentProperties = BuildMain.getEnvironmentProperties();
                    	String responsibility = environmentProperties.get(SwiftBuildConstants.FLD_FORM_RESPONSIBILITY_KEY);
                    	String msg="The injection failed to enable the profile option for the Oracle user '"+userName+"'.\n"+
                    			"Usually, this is caused by an invalid Oracle responsibility provided in the first panel (BWE).\n"+
                    			"Please recheck your Oracle Responsibility which must have access to the System Profile Form and must be assigned to that Oracle user.\n"+
                    			"The current value is '"+responsibility+"'.";
                    	GUIUtils.popupErrorMessage(msg);
                    }
                    break;
                }
                status[0] = ECB_STATUS.SUCCESS;
            }
            FileUtils.println("refreshFormsUserList: " + status[0] + " - " + userMenuDiagnosticsEnabledMap);
            status.notifyAll();
        }        
    }

    private int refreshFormsUserListInternal() throws Exception
    {
        tableModel.setRowCount(0);

        SortedMap<String, USER_STATUS> userMenuDiagnosticsEnabledMap = getUserMenuDiagnosticsEnabledMap(this.restrictToTheseUsersPasswords, BuildMain);
        for ( Entry<String, USER_STATUS> entry : userMenuDiagnosticsEnabledMap.entrySet() )
        {
            tableModel.addRow(new Object[] { entry.getKey(), entry.getValue().toString() });
        }
        this.getEnableButton().setEnabled(this.getUserNames().size() > 0);
        return this.restrictToTheseUsersPasswords.size();
    }

    @Override
    public void tableChanged(TableModelEvent e)
    {
    }


    public List<String> getUserNames()
    {
        List<String> toReturn = new ArrayList<String>();

        for ( int rowIndex = 0; rowIndex < this.tableModel.getRowCount(); rowIndex++ )
        {
            String status = (String)this.tableModel.getValueAt(rowIndex, ENABLE_DIAGNOSTICS_PANEL_COLUMN.STATUS.getIndex());
            if ( status.equals(USER_STATUS.DISABLED.toString()) )
            {
                String userName = (String)this.tableModel.getValueAt(rowIndex, ENABLE_DIAGNOSTICS_PANEL_COLUMN.USER_NAME.getIndex());
                toReturn.add(userName);
            }
        }

        return toReturn;
    }


    public static enum USER_STATUS
    {
        ENABLED,
        DISABLED,
        DOES_NOT_EXIST
    }

    private static final String PROFILE_OPTION_NAME_UTILITIES_DIAGNOSTICS       = "Utilities:Diagnostics";
    private static final String PROFILE_OPTION_NAME_HIDE_DIAGNOSTICS_MENU_ENTRY = "Hide Diagnostics menu entry";

    public static SortedMap<String, USER_STATUS> getUserMenuDiagnosticsEnabledMap(Map<String, String>  userNamesToPasswordCheckStatusOn
    		, BuildMain buildMain) throws Exception
    {
        String sqlProfileOptions = "select VL.USER_PROFILE_OPTION_NAME ,V.PROFILE_OPTION_VALUE from FND_PROFILE_OPTION_VALUES V,FND_PROFILE_OPTIONS_VL VL,FND_USER U where " +
        " V.PROFILE_OPTION_ID=VL.PROFILE_OPTION_ID and U.USER_ID=V.LEVEL_VALUE and " +
        " (VL.USER_PROFILE_OPTION_NAME = '" + PROFILE_OPTION_NAME_UTILITIES_DIAGNOSTICS +
        "' or VL.USER_PROFILE_OPTION_NAME = '" + PROFILE_OPTION_NAME_HIDE_DIAGNOSTICS_MENU_ENTRY + "') " +
        " and U.USER_NAME=?";

        String sqlUserExists = "select count(*) from FND_USER U where U.USER_NAME=?";

        SortedMap<String, USER_STATUS> toReturn = new TreeMap<String, USER_STATUS>();

        try ( Connection conn = DatabaseUtils.getDatabaseConnection(buildMain.getEnvironmentProperties());
                PreparedStatement pstmtUserExists = conn.prepareStatement(sqlUserExists);
                PreparedStatement pstmtProfileOptions = conn.prepareStatement(sqlProfileOptions); )
        {
        	 Iterator<String> iterator=userNamesToPasswordCheckStatusOn.keySet().iterator();
             while (iterator.hasNext() ) {
             	String userName=iterator.next();
          
                pstmtUserExists.clearParameters();
                pstmtUserExists.setString(1, userName);
                try ( ResultSet rs = pstmtUserExists.executeQuery(); )
                {
                    rs.next();
                    int count = rs.getInt(1);
                    if ( count != 0 )
                    {
                        toReturn.put(userName, USER_STATUS.DISABLED);
                    }
                    else
                    {
                        toReturn.put(userName, USER_STATUS.DOES_NOT_EXIST);
                    }
                }

                pstmtProfileOptions.clearParameters();
                pstmtProfileOptions.setString(1, userName);
                boolean existsOneOrMoreResultRows = false;
                boolean areDiagnosticsEnabled = false;
                boolean areDiagnosticsVisible = false;
                try ( ResultSet rs = pstmtProfileOptions.executeQuery(); )
                {
                    while ( rs.next() )
                    {
                        existsOneOrMoreResultRows = true;
                        int index = 1;
                        String profileOptionName = rs.getString(index++);
                        boolean profileOptionValue = SharedUtil.getYesOrNoAsBoolean(rs.getString(index++));
                        if ( profileOptionName.equals(PROFILE_OPTION_NAME_UTILITIES_DIAGNOSTICS) )
                        {
                            areDiagnosticsEnabled = profileOptionValue;
                        }
                        else if ( profileOptionName.equals(PROFILE_OPTION_NAME_HIDE_DIAGNOSTICS_MENU_ENTRY) )
                        {
                            areDiagnosticsVisible = !profileOptionValue;
                        }
                        else
                        {
                            Assert.isTrue(false, "Unrecognized profile option: " + profileOptionName);
                        }
                        if ( areDiagnosticsEnabled && areDiagnosticsVisible )
                        {
                            //at least one positive result; run with it
                            break;
                        }
                    }
                }
                if ( existsOneOrMoreResultRows )
                {
                    toReturn.put(userName, areDiagnosticsEnabled && areDiagnosticsVisible ? USER_STATUS.ENABLED : USER_STATUS.DISABLED);
                }
            }
        }
        return toReturn;
    }

	public SortedMap<String, String> getRestrictToTheseUsersPasswords() {
		return restrictToTheseUsersPasswords;
	}
}