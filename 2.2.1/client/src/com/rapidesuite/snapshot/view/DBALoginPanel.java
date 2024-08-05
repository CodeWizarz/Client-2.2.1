package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.Connection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class DBALoginPanel extends GenericLoginPanel{

	private JPasswordField sysPasswordTextField;
	private JButton okButton;
	private JButton closeButton;
	private UserManagementPanel userManagementPanel;
	
	public DBALoginPanel(UserManagementPanel userManagementPanel) {
		this.userManagementPanel=userManagementPanel;
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		createComponents();
	}
	
	public void createComponents(){
		int labelsWidth=150;
		int fieldsWidth=170;
		int height=22;
		int widthEmptyBordersPanels=40;
		
		JPanel tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		JLabel label=new JLabel("SYS Password:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		sysPasswordTextField=new JPasswordField();
		UIUtils.setDimension(sysPasswordTextField,fieldsWidth,height);
		tempPanel.add(sysPasswordTextField);
						
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_apply.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		okButton = new JButton();
		okButton.setIcon(ii);
		okButton.setBorderPainted(false);
		okButton.setContentAreaFilled(false);
		okButton.setFocusPainted(false);
		okButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_apply_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		okButton.setRolloverIcon(new RolloverIcon(ii));
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ex) {
				try{
					processActionOK();
				}
				catch(Exception e) {
					FileUtils.printStackTrace(e);
					GUIUtils.popupErrorMessage(e.getMessage());
				}
				
			}});
		tempPanel.add(okButton);
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
	}
	
	protected void testConnection() throws Exception {
		Connection connection=null;
		try {
			String dbUser=AdminPasswordPanel.ADMIN_DB_NAME+" as sysdba";
					
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(
					userManagementPanel.getTabSnapshotsPanel());
			String password=new String(sysPasswordTextField.getPassword());
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					dbUser,
					password);
		}
		catch(Exception e) {
			throw new Exception("Error connecting to the Database: "+e.getMessage());
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}
	
	protected void processActionOK() throws Exception {
		testConnection();
		isSuccess=true;
		dialog.setVisible(false);
	}
	
}
