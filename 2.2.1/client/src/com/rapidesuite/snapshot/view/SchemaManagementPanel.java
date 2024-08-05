package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SchemaInformation;

@SuppressWarnings("serial")
public class SchemaManagementPanel  extends JPanel{

	private JList<String> schemaList;
	private DefaultListModel<String> listModel;
	private JTextField schemaNameTextField;
	private JTextField datafileNameTextField;
	private JTextField tablespaceSizeInGBTextField;
	private JCheckBox isTableSpaceAutoExtend;
	private JPasswordField schemaPasswordTextField;
	private JPasswordField schemaPasswordVerifyTextField;

	private JLabel statusLabel;
	private JButton createButton;
	private boolean isScriptRunning;

	private JDialog dialog;
	private JDialog adminPasswordDialog;
	
	private AdminPasswordPanel adminPasswordPanel;
	private List<SchemaInformation> schemaInformationList;
	private JPanel statusPanel;
	private JButton closeButton;
	public static final String SCHEMA_SCRIPT_FILE_NAME="snapshot.sql";
	public static final String USER_NAME_PREFIX="XX_RS_";
	public static final String VARIABLE_USERNAME="##USER_NAME##";
	public static final String VARIABLE_PASSWORD="##PASSWORD##";
	public static final String VARIABLE_DBF="##DBF##";
	public static final String VARIABLE_TABLESPACE="##TABLESPACE_SIZE_IN_GB##";	
	public static final String VARIABLE_TABLESPACE_AUTO_EXTEND="##TABLESPACE_AUTO_EXTEND##";

	public SchemaManagementPanel(AdminPasswordPanel adminPasswordPanel) {
		this.adminPasswordPanel=adminPasswordPanel;
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		createComponents();
	}

	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
		dialog.addWindowListener (new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				close();
			}
		});
	}

	protected void close() {
		if (isScriptRunning) {
			GUIUtils.popupErrorMessage("Please wait for the script to finish!");
			return;
		}
		dialog.dispose();
	}

	@SuppressWarnings({ "rawtypes" })
	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BorderLayout());
		add(northPanel);
		
		JLabel label=new JLabel("<html>RAPIDSnaphot will run a script which will create a new Oracle schema/ tablespace and datafile.<br/><br/>"+
				"Please specify the required information below. Note that you can click on the \"View script\" button if you wish to run the script manually<br/>"+
				" or if you wish to customize it.");
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		label.setForeground(Color.white);
		label.setBackground(Color.decode("#047fc0"));
		InjectUtils.assignArialPlainFont(label,12);
		label.setOpaque(true);
		northPanel.add(label,BorderLayout.WEST);
		
		final JPanel mainPanel=new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		add(mainPanel);

		JPanel leftPanel=new JPanel();
		leftPanel.setOpaque(false);
		leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		mainPanel.add(leftPanel);

		listModel = new DefaultListModel<String>(); 
		schemaList = new JList<String>(listModel);
		schemaList.setSelectionBackground(Color.decode("#047fc0"));
		schemaList.setCellRenderer(new DefaultListCellRenderer  (){
			
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component component = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
		        if (index % 2 == 0) {
		        	component.setBackground(Color.white);
		        }
		        else {
		        	component.setBackground(Color.decode("#efefef"));
		        }
		        if (isSelected) {
		        	component.setBackground(Color.decode("#047fc0"));
		        }

		        return component;
		    }
		}
		);
		schemaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		schemaList.setVisibleRowCount(20);
		
		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Delete");
		popupMenu.add(item);
		item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	processActionSchemaDeletion();
            }
        });
			
		schemaList.addMouseListener(new MouseAdapter() {
		     public void mouseClicked(MouseEvent me) {
		       // if right mouse button clicked (or me.isPopupTrigger())
		       if (SwingUtilities.isRightMouseButton(me)
		           && !schemaList.isSelectionEmpty()
		           && schemaList.locationToIndex(me.getPoint())
		              == schemaList.getSelectedIndex()) {
		               popupMenu.show(schemaList, me.getX(), me.getY());
		               }
		           }
		        }
		     );
		
		JScrollPane scrollPane = new JScrollPane(schemaList);
		Dimension d = schemaList.getPreferredSize();
		d.width = 160;
		scrollPane.setPreferredSize(d);
		label=new JLabel("Schema List:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		leftPanel.add(label);
		leftPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		leftPanel.add(scrollPane);

		JPanel rightPanel=new JPanel();
		rightPanel.setOpaque(false);
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		mainPanel.add(rightPanel);

		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		
		int labelsWidth=180;
		int fieldsWidth=200;
		int labelsHeight=30;
		int fieldsHeight=22;
		int panelSpacing=5;

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("<html>JDBC String: <b>"+adminPasswordPanel.getJDBCString()+"</b>");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,500,labelsHeight);
		tempPanel.add(label);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("<html>Enter the schema name<br/>(must be prefixed by <b>"+USER_NAME_PREFIX+"</b>):");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		schemaNameTextField=new JTextField();
		UIUtils.setDimension(schemaNameTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(schemaNameTextField);
		JTextField f = new JTextField("(Ex: XX_RS_USER1)");
		f.setEditable(false);
		f.setBorder(null);
		f.setOpaque(false);
		f.setForeground(Color.decode("#343836"));
		tempPanel.add(f);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("Enter the password:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		schemaPasswordTextField=new JPasswordField();
		UIUtils.setDimension(schemaPasswordTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(schemaPasswordTextField);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("Re-enter the password:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		schemaPasswordVerifyTextField=new JPasswordField();
		UIUtils.setDimension(schemaPasswordVerifyTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(schemaPasswordVerifyTextField);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("<html>Enter the full path<br/>to the Datafile:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		datafileNameTextField=new JTextField();
		UIUtils.setDimension(datafileNameTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(datafileNameTextField);
		f = new JTextField("(Ex: /data/erpp/oracle/ERPP/db/apps_st/data/"+USER_NAME_PREFIX+"USER1.dbf)");
		f.setEditable(false);
		f.setBorder(null);
		f.setOpaque(false);
		f.setForeground(Color.decode("#343836"));
		tempPanel.add(f);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("Tablespace size in GB:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		tablespaceSizeInGBTextField=new JTextField();
		tablespaceSizeInGBTextField.setText("1");
		UIUtils.setDimension(tablespaceSizeInGBTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(tablespaceSizeInGBTextField);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		isTableSpaceAutoExtend=new JCheckBox("Tablespace AutoExtend");
		isTableSpaceAutoExtend.setOpaque(false);
		InjectUtils.assignArialPlainFont(isTableSpaceAutoExtend,InjectMain.FONT_SIZE_NORMAL);
		isTableSpaceAutoExtend.setForeground(Color.decode("#343836"));
		isTableSpaceAutoExtend.setSelected(true);
		tempPanel.add(isTableSpaceAutoExtend);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_create.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		createButton = new JButton();
		createButton.setIcon(ii);
		createButton.setBorderPainted(false);
		createButton.setContentAreaFilled(false);
		createButton.setFocusPainted(false);
		createButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_create_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		createButton.setRolloverIcon(new RolloverIcon(ii));
		createButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSchemaCreation();
			}

		});
		tempPanel.add(createButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_view_script.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton viewScriptButton = new JButton();
		viewScriptButton.setIcon(ii);
		viewScriptButton.setBorderPainted(false);
		viewScriptButton.setContentAreaFilled(false);
		viewScriptButton.setFocusPainted(false);
		viewScriptButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_view_script_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		viewScriptButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(viewScriptButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		viewScriptButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				viewScript();
			}
		});
		
		
		iconURL = this.getClass().getResource("/images/snapshot/button_close.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		closeButton = new JButton();
		closeButton.setIcon(ii);
		closeButton.setBorderPainted(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setFocusPainted(false);
		closeButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_close_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		closeButton.setRolloverIcon(new RolloverIcon(ii));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				close();
			}
		}
				);
		tempPanel.add(closeButton);
					
		statusPanel=new JPanel();
		statusPanel.setOpaque(false);
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(statusPanel);
		statusLabel=new JLabel();
		statusLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(statusLabel,InjectMain.FONT_SIZE_NORMAL);
		statusPanel.add(statusLabel);
			
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
		
		/*
		userNameTextField.setText("XX_RS_USER7");
		userPasswordTextField.setText("XX_RS_USER7");
		userPasswordVerifyTextField.setText("XX_RS_USER7");
		datafileNameTextField.setText("/data/erpp/oracle/ERPP/db/apps_st/data/XX_RS_USER7.dbf");
		*/
		displaySchemaListThread();
	}

	protected void processActionSchemaDeletion() {
		int selectedIndex =schemaList.getSelectedIndex();
		if (selectedIndex==-1) {
			GUIUtils.popupErrorMessage("You must select a schema on the List");
			return;
		}
		final SchemaInformation schemaInformation=schemaInformationList.get(selectedIndex);
		int response = JOptionPane.showConfirmDialog(null, "Are you sure to delete the schema '"+schemaInformation.getSchemaName()+"' ?", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			isScriptRunning=true;
			statusPanel.setOpaque(true);
			statusPanel.setBackground(Color.decode("#08bc08"));
			statusLabel.setText("<html><b>Deleting schema '"+schemaInformation.getSchemaName()+"'...</b>");
			
			lock();
			Thread thread = new Thread(){
				public void run(){
					deleteSchema(schemaInformation);
				}
			};
			thread.start();			
		}
	}

	protected void deleteSchema(SchemaInformation schemaInformation) {
		Connection connection=null;
		try {			
			String dbUser=AdminPasswordPanel.ADMIN_DB_NAME+" as sysdba";
			connection=DatabaseUtils.getJDBCConnection(
					adminPasswordPanel.getJDBCString(),
					dbUser,
					adminPasswordPanel.getPassword());

			ModelUtils.dropSnapshotSchemaAndTablespace(connection,schemaInformation.getSchemaName());
			
			statusLabel.setText("<html><b>Schema deleted!</b>");
			displaySchemaList(connection);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			statusPanel.setBackground(Color.decode("#ee3630"));
			statusLabel.setText("<html><b>"+e.getMessage()+"</b>");
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
			isScriptRunning=false;
			unlock();
		}			
	}

	protected void processActionSchemaCreation() {
		if (!schemaNameTextField.getText().startsWith(USER_NAME_PREFIX)) {
			GUIUtils.popupErrorMessage("You must prefix the schema name by "+USER_NAME_PREFIX);
			return;
		}
		String password=new String(schemaPasswordTextField.getPassword());
		String passwordCheck=new String(schemaPasswordVerifyTextField.getPassword());
		if (!password.equals(passwordCheck) || password.isEmpty()) {
			GUIUtils.popupErrorMessage("Passwords do not match!");
			return;
		}
		if (datafileNameTextField.getText().isEmpty()) {
			GUIUtils.popupErrorMessage("You must enter a data file path");
			return;
		}
		String temp=tablespaceSizeInGBTextField.getText();
		try{
			int val=Integer.valueOf(temp);
			if (val<1) {
				GUIUtils.popupErrorMessage("Tablespace size must be greater or equal to 1 GB");
				return;
			}		
		}
		catch(NumberFormatException e) {
			GUIUtils.popupErrorMessage("Tablespace size must be a whole number");
			return;
		}

		statusPanel.setOpaque(true);
		statusPanel.setBackground(Color.decode("#08bc08"));
		statusLabel.setText("<html><b>Executing script...</b>");
		isScriptRunning=true;
		lock();
		Thread thread = new Thread(){
			public void run(){
				createSchema();
			}
		};
		thread.start();				
	}

	public void lock() {
		setComponentsEnabled(false);
	}

	public void unlock() {
		setComponentsEnabled(true);
	}

	public void setComponentsEnabled(boolean isEnabled) {
		schemaNameTextField.setEnabled(isEnabled);
		schemaPasswordTextField.setEnabled(isEnabled);
		schemaPasswordVerifyTextField.setEnabled(isEnabled);
		datafileNameTextField.setEnabled(isEnabled);
		tablespaceSizeInGBTextField.setEnabled(isEnabled);
		isTableSpaceAutoExtend.setEnabled(isEnabled);
		createButton.setEnabled(isEnabled);
	}

	private void createSchema() {
		Connection connection=null;
		try {
			String dbUser=AdminPasswordPanel.ADMIN_DB_NAME+" as sysdba";
			connection=DatabaseUtils.getJDBCConnection(
					adminPasswordPanel.getJDBCString(),
					dbUser,
					adminPasswordPanel.getPassword());
			
			File file = File.createTempFile("rapidsnapshot", ".sql");
			String content=getScriptContent(schemaNameTextField.getText());
			ModelUtils.writeToFile(file,content,false); 
			List<String> statements=FileUtils.readContentsFromPLSQLFile(file);
			for (int i=0;i<statements.size();i++)
			{
				String sql=statements.get(i);
				sql = reformatSQL(sql);
				String text="Executing: "+(i+1)+" / "+statements.size()+" commands.";
				statusLabel.setText("<html><b>"+text+"</b>");
				//FileUtils.println(text);
				//FileUtils.println("command: '"+sql+"'");
				ModelUtils.executeStatement(connection,sql);
			}
			connection.commit();
			displaySchemaList(connection);
			
			String serverHostName=adminPasswordPanel.getDbHostTextField().getText();
			String serverPort=adminPasswordPanel.getDbPortTextField().getText();
			String serverSID=adminPasswordPanel.getDbSIDTextField().getText();
			String serverUserName=schemaNameTextField.getText();
			String serverPasswordName=new String(schemaPasswordTextField.getPassword());
			String serverConnectionName=serverHostName+" - "+serverUserName;
			
			String serviceType = "";
			String serviceName = adminPasswordPanel.getDbServiceNameTextField().getText();
			
			try{
				serviceType = adminPasswordPanel.getDbServiceTypeComboBox().getSelectedItem().toString();
				if(!UtilsConstants.SID_DATABASE_SERVICE_TYPE.equalsIgnoreCase(serviceType) &&
				   !UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE.equalsIgnoreCase(serviceType)	){
					GUIUtils.popupErrorMessage("Please select DB Service type.");
					return;
				}else if(UtilsConstants.SID_DATABASE_SERVICE_TYPE.equalsIgnoreCase(serviceType)){
					if(serverSID.isEmpty() || serverSID.equals("") || serverSID==null){
						GUIUtils.popupErrorMessage("Please specify DB SID.");
						return;
					}
				}else if(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE.equalsIgnoreCase(serviceType)){
					if(serviceName.isEmpty() || serviceName.equals("") || serviceName==null){
						GUIUtils.popupErrorMessage("Please specify DB Service name.");
						return;
					}
				}
			}catch(Exception e){
				serviceType = UtilsConstants.SID_DATABASE_SERVICE_TYPE;
			}
			createConnection(serverConnectionName,serverHostName,serverPort,serverSID,serverUserName,serverPasswordName,serviceType,serviceName);
			
			statusLabel.setText("<html><b>Script completed successfully!</b>");
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			statusPanel.setBackground(Color.decode("#ee3630"));
			statusLabel.setText("<html><b>"+e.getMessage()+"</b>");
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
			isScriptRunning=false;
			unlock();
		}
	}
	
	private void createConnection(String serverConnectionName,String serverHostName,String serverPort,String serverSID,
			String serverUserName,String serverPasswordName, String serviceType, String serviceName) {
		File serverConnectionsFolder=adminPasswordPanel.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
	
		boolean isSuccess=ServerFrame.createConnectionFile(serverConnectionsFolder,serverConnectionName,serverHostName,serverPort,serverSID,
				serverUserName,serverPasswordName,serviceType,serviceName);
		if (isSuccess) {
			adminPasswordPanel.getTabSnapshotsPanel().getServerSelectionPanel().refreshServerConnections(serverConnectionName);
			GUIUtils.popupInformationMessage("A new connection '"+serverConnectionName+"' has been created!");
		}
	}

	private String reformatSQL(String sql) {
		sql = CoreUtil.stripCStyleComments(sql);
		sql = sql.trim();
		String sqlOnlySpace = sql.replaceAll("\\s+", " ").toUpperCase();
		boolean isDeclaration = sqlOnlySpace.startsWith("DECLARE ");
		if ( sql.charAt(sql.length() - 1) == ';' && !isDeclaration) {
			sql = sql.substring(0, sql.length() - 1);
		}
		return sql;
	}

	private String getScriptContent(String schemaName) throws Exception {
		File file=new File(SCHEMA_SCRIPT_FILE_NAME);
		String content=readContentsFromSQLFile(file);
		content=content.replaceAll(VARIABLE_USERNAME, schemaName);
		String password=new String(schemaPasswordTextField.getPassword());
		content=content.replaceAll(VARIABLE_PASSWORD, password);
		content=content.replaceAll(VARIABLE_DBF, datafileNameTextField.getText());
		content=content.replaceAll(VARIABLE_TABLESPACE, tablespaceSizeInGBTextField.getText());
		String temp="ON";
		if (!isTableSpaceAutoExtend.isSelected()) {
			temp="OFF";
		}
		content=content.replaceAll(VARIABLE_TABLESPACE_AUTO_EXTEND, temp);
		return content;
	}
	
	protected void viewScript() {
		if (schemaNameTextField.getText().trim().isEmpty()) {
			GUIUtils.popupErrorMessage("You must enter a schema name first before viewing the script");
			return;
		}
		try{
			String content=getScriptContent(schemaNameTextField.getText());
			ModelUtils.viewScript(content);
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}

	private void displaySchemaList(Connection connection) {
		try {
			schemaInformationList=ModelUtils.getSnapshotSchemaInformationList(connection);
			listModel.removeAllElements();
			for (SchemaInformation schemaInformation:schemaInformationList) {
				String text=schemaInformation.getSchemaName();
				listModel.addElement(text);
			}				
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
		}
		finally{
			adminPasswordDialog.dispose();
			dialog.setVisible(true);
		}
	}
	
	private void displaySchemaList() throws ClassNotFoundException, SQLException {
		Connection connection=null;
		try {
			String dbUser=AdminPasswordPanel.ADMIN_DB_NAME+" as sysdba";
			connection=DatabaseUtils.getJDBCConnection(
					adminPasswordPanel.getJDBCString(),
					dbUser,
					adminPasswordPanel.getPassword());
			
			displaySchemaList(connection);
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}
	
	private void displaySchemaListThread() {
		Thread thread = new Thread(){
			public void run(){
				try {
					displaySchemaList();
				} catch (ClassNotFoundException | SQLException e) {
					FileUtils.printStackTrace(e);
				}
			}
		};
		thread.start();	
	}
	
	public static String readContentsFromSQLFile(File file)
			throws Exception
	{
		InputStream is=null;
		try
		{
			is=CoreUtil.getUnencryptedInputStream(file);
			return IOUtils.toString(is);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}

	public void setAdminPasswordDialog(JDialog adminPasswordDialog) {
		this.adminPasswordDialog=adminPasswordDialog;
	}
	
}
