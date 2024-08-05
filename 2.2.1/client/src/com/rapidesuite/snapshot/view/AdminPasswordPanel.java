package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import net.java.balloontip.BalloonTip.Orientation;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.DesktopApi;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class AdminPasswordPanel extends JPanel{

	private JTextField dbHostTextField;
	private JTextField dbPortTextField;
	private JTextField dbSIDTextField;
	private JPasswordField passwordTextField;	
	private JLabel statusLabel;
	private JButton connectButton;
	private JComboBox dbServiceTypeComboBox;
	private JTextField dbServiceNameTextField;
	
	public final static String ADMIN_DB_NAME="SYS";
	private JDialog dialog;
	private TabSnapshotsPanel tabSnapshotsPanel;
	private JButton closeButton;
	
	public AdminPasswordPanel(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		createComponents();
	}
	
	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BorderLayout());
		add(northPanel);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		northPanel.add(tempPanel,BorderLayout.NORTH);
		JLabel label=new JLabel();
		String str="<html>This Window is intended to be used by your DBA, he will need to create a schema"+
		" by following the instructions provided in this video:<br/>";
		label.setText(str);
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		label.setForeground(Color.white);
		label.setBackground(Color.decode("#047fc0"));
		InjectUtils.assignArialPlainFont(label,12);
		label.setOpaque(true);
		tempPanel.add(label);		
		String url="https://www.rapid4cloud.com/support-item/how-to-setup-your-schema-for-rapidsnaphot/";
		JLabel link=new JLabel("<html><u>"+url+"</u>");
	    link.setCursor(new Cursor(Cursor.HAND_CURSOR));	    
		linkify(link,url);
		link.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		link.setForeground(Color.white);
		link.setBackground(Color.decode("#047fc0"));
		InjectUtils.assignArialPlainFont(link,12);
		link.setOpaque(true);
		tempPanel.add(link);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		northPanel.add(tempPanel,BorderLayout.CENTER);
		label=new JLabel();
		str="<html>In case of any issues or questions, please log a support ticket with us:<br/>";
		label.setText(str);
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		label.setForeground(Color.white);
		label.setBackground(Color.decode("#047fc0"));
		InjectUtils.assignArialPlainFont(label,12);
		label.setOpaque(true);
		tempPanel.add(label);
		url=SnapshotMain.SUPPORT_TICKET_SITE_URL;
		link=new JLabel("<html><u>"+url+"</u>");
	    link.setCursor(new Cursor(Cursor.HAND_CURSOR));	    
		linkify(link,url);
		link.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		link.setForeground(Color.white);
		link.setBackground(Color.decode("#047fc0"));
		InjectUtils.assignArialPlainFont(link,12);
		link.setOpaque(true);
		tempPanel.add(link);
				
		JLabel mainLabel=new JLabel();
		str= "<html>RAPIDSnaphot will need the Database password for the <b>"+ADMIN_DB_NAME+
				"</b> user in order to create schemas and tablespaces."+
		"<br/><b><i>Note that the password is neither saved or remembered!</i></b>";
		mainLabel.setText(str);
		mainLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		mainLabel.setForeground(Color.white);
		mainLabel.setBackground(Color.decode("#047fc0"));
		InjectUtils.assignArialPlainFont(mainLabel,12);
		mainLabel.setOpaque(true);
		northPanel.add(mainLabel,BorderLayout.SOUTH);
		
		int labelsWidth=150;
		int fieldsWidth=220;
		int height=22;
		int widthEmptyBordersPanels=40;
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		label=new JLabel("Enter the DB hostname:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		dbHostTextField=new JTextField();
		UIUtils.setDimension(dbHostTextField,fieldsWidth,height);
		tempPanel.add(dbHostTextField);
		add(Box.createRigidArea(new Dimension(15, 10)));
		label=new JLabel("(Ex: oratest14.company.com)");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		label=new JLabel("Enter the DB port number:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		dbPortTextField=new JTextField();
		UIUtils.setDimension(dbPortTextField,fieldsWidth,height);
		tempPanel.add(dbPortTextField);
		add(Box.createRigidArea(new Dimension(15, 10)));
		label=new JLabel("(Ex: 1521)");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		label=new JLabel("Enter the DB service type:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		dbServiceTypeComboBox =new JComboBox();
		dbServiceTypeComboBox.addItem(UtilsConstants.DEFAULT_DATABASE_SERVICE_TYPE);
		dbServiceTypeComboBox.addItem(UtilsConstants.SID_DATABASE_SERVICE_TYPE);
		dbServiceTypeComboBox.addItem(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE);
		dbServiceTypeComboBox.setSelectedItem(UtilsConstants.DEFAULT_DATABASE_SERVICE_TYPE);
		UIUtils.setDimension(dbServiceTypeComboBox,fieldsWidth,height);
		tempPanel.add(dbServiceTypeComboBox);
		add(Box.createRigidArea(new Dimension(15, 10)));
		
		dbServiceTypeComboBox.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent evt)
			{
				if ( evt.getStateChange() == ItemEvent.SELECTED )
				{
					JComboBox cb = (JComboBox) evt.getSource();
					String serviceType = (String) cb.getSelectedItem();
					// Item was just selected
					if ( serviceType.equalsIgnoreCase(UtilsConstants.SID_DATABASE_SERVICE_TYPE) ){
						dbSIDTextField.setEnabled(true);
						dbSIDTextField.setBackground(Color.white);
						dbServiceNameTextField.setEnabled(false);
						dbServiceNameTextField.setBackground(Color.lightGray);
						dbServiceNameTextField.setText("");

					}
					else if ( serviceType.equalsIgnoreCase(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE) ){
						dbSIDTextField.setEnabled(false);
						dbSIDTextField.setBackground(Color.lightGray);
						dbSIDTextField.setText("");
						dbServiceNameTextField.setEnabled(true);
						dbServiceNameTextField.setBackground(Color.white);
					}					
					else{
						dbSIDTextField.setEnabled(false);
						dbSIDTextField.setBackground(Color.lightGray);
						dbSIDTextField.setText("");
						dbServiceNameTextField.setEnabled(false);
						dbServiceNameTextField.setBackground(Color.lightGray);
						dbServiceNameTextField.setText("");
					}
				}
				else if ( evt.getStateChange() == ItemEvent.DESELECTED )
				{
					// Item is no longer selected
				}
			}
		});
		
		
		/*add(Box.createRigidArea(new Dimension(15, 10)));
		label=new JLabel("(Ex: ORCL)");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);*/
		
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		label=new JLabel("Enter the DB SID:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		dbSIDTextField=new JTextField();
		UIUtils.setDimension(dbSIDTextField,fieldsWidth,height);
		tempPanel.add(dbSIDTextField);
		add(Box.createRigidArea(new Dimension(15, 10)));
		label=new JLabel("(Ex: ORCL)");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		label=new JLabel("Enter the DB Service name:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		dbServiceNameTextField=new JTextField();
		UIUtils.setDimension(dbServiceNameTextField,fieldsWidth,height);
		tempPanel.add(dbServiceNameTextField);
		add(Box.createRigidArea(new Dimension(15, 10)));
		label=new JLabel("(Ex: ORCL)");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		label=new JLabel("Enter the password:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		passwordTextField=new JPasswordField();
		UIUtils.setDimension(passwordTextField,fieldsWidth,height);
		tempPanel.add(passwordTextField);
		add(Box.createRigidArea(new Dimension(15, 10)));
		label=new JLabel("<html><b>SYS</b> Password!");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_connect.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		connectButton = new JButton();
		connectButton.setIcon(ii);
		connectButton.setBorderPainted(false);
		connectButton.setContentAreaFilled(false);
		connectButton.setFocusPainted(false);
		connectButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_connect_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		connectButton.setRolloverIcon(new RolloverIcon(ii));
		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				processActionDBConnect();
			}

		});
		tempPanel.add(connectButton);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		
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
		viewScriptButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				UIUtils.viewRawScript();
			}
		});
		tempPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		
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
				dialog.dispose();
			}
		}
				);
		tempPanel.add(closeButton);
		
		add(Box.createRigidArea(new Dimension(15, 10)));

		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		statusLabel=new JLabel();
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		statusLabel.setForeground(Color.black);
		statusLabel.setOpaque(true);	
		statusLabel.setBackground(Color.decode("#dbdcdf"));
		InjectUtils.assignArialPlainFont(statusLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(statusLabel);
		add(Box.createRigidArea(new Dimension(15, 10)));
		
		connectButton.requestFocus();
		
		if (!UIConstants.BALLOON_ADMIN_PASSWORD_TRIGGERED) {
    		UIConstants.BALLOON_ADMIN_PASSWORD_TRIGGERED=true;
    		UIUtils.showBalloon(mainLabel,
    				"<html>This window is intended to be used by the DBA in order to provide Admin access to the Database.",Orientation.LEFT_BELOW, 
    				tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().isShowBalloons());
    		
    		UIUtils.showBalloon(viewScriptButton,
    				"<html>The DBA may wish to create schemas manually.<br/>The plain-text script is provided for that purpose.",Orientation.LEFT_ABOVE, 
    				tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().isShowBalloons());
    	}
		/*
		dbHostTextField.setText("oratest14");
		dbPortTextField.setText("1521");
		dbSIDTextField.setText("ERPP");
		passwordTextField.setText("manager");
		*/
	}
	
	public void processActionDBConnect() {	
		statusLabel.setBackground(Color.decode("#08bc08"));
		statusLabel.setText("<html><b>Connecting to the Database...</b>");
		connectButton.setEnabled(false);
		Thread thread = new Thread(){
			public void run(){
				DBConnect();
			}
		};

		thread.start();				
	}
	
	protected void DBConnect() {
		Connection connection=null;
		try {
			String dbUser=ADMIN_DB_NAME+" as sysdba";
						
			connection=DatabaseUtils.getJDBCConnection(
					getJDBCString(),
					dbUser,
					getPassword());
			
			int width=1000;
			int height=650;
			SchemaManagementPanel schemaManagementPanel=new SchemaManagementPanel(this);
			JDialog userMgtDialog=UIUtils.displayOperationInProgressComplexModalWindow(tabSnapshotsPanel.
					getMainPanel().getSnapshotMain().getRootFrame(),"Schema Management",width,height,
					schemaManagementPanel,null,false,SnapshotMain.getSharedApplicationIconPath());
			schemaManagementPanel.setDialog(userMgtDialog);
			schemaManagementPanel.setAdminPasswordDialog(dialog);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			statusLabel.setBackground(Color.decode("#ee3630"));
			statusLabel.setText("<html><b>"+e.getMessage()+"</b>");
			connectButton.setEnabled(true);
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}

	public String getPassword() {
		return new String(passwordTextField.getPassword());
	}

	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
	}

	public String getJDBCString() {
		String serviceType="";
		try{
			serviceType = dbServiceTypeComboBox.getSelectedItem().toString();
			
		}catch(Exception e){
			serviceType = UtilsConstants.SID_DATABASE_SERVICE_TYPE;
		}
		return ModelUtils.getJDBCString( dbHostTextField.getText(), dbPortTextField.getText(), dbSIDTextField.getText(),serviceType,dbServiceNameTextField.getText());
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public JTextField getDbHostTextField() {
		return dbHostTextField;
	}

	public JTextField getDbPortTextField() {
		return dbPortTextField;
	}

	public JTextField getDbSIDTextField() {
		return dbSIDTextField;
	}
	
	public static JLabel linkify(JLabel link,final String url)
	{
	    link.addMouseListener(new MouseListener()
	    {
	        public void mouseExited(MouseEvent arg0)
	        {
	        }

	        public void mouseEntered(MouseEvent arg0)
	        {
	        }

	        public void mouseClicked(MouseEvent arg0)
	        {
	        	try{
	        		URI uri = URI.create(url);
	        		DesktopApi.browse( uri );
	        	}
	        	catch (Exception e)
	        	{
	        		FileUtils.printStackTrace(e);
	        		GUIUtils.popupErrorMessage(e.getMessage());
	        	}
	        }

	        public void mousePressed(MouseEvent e)
	        {
	        }

	        public void mouseReleased(MouseEvent e)
	        {
	        }
	    });	  
	    return link;
	}

	public JComboBox getDbServiceTypeComboBox() {
		return dbServiceTypeComboBox;
	}

	public JTextField getDbServiceNameTextField() {
		return dbServiceNameTextField;
	}
	
	
}
