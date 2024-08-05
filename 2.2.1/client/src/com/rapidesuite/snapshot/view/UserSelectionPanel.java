package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

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
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.UserInformation;

@SuppressWarnings("serial")
public class UserSelectionPanel extends GenericLoginPanel{

	private JComboBox<String> loginNameComboBox;
	private JPasswordField passwordTextField;	
	private JButton okButton;
	private JButton closeButton;
	
	private TabSnapshotsPanel tabSnapshotsPanel;
	private List<UserInformation> userInformationList;
	private boolean isCancelled;
	private boolean isStoreSelectedUser;
	
	public UserSelectionPanel(TabSnapshotsPanel tabSnapshotsPanel, List<UserInformation> userInformationList,
			boolean isStoreSelectedUser) {
		this.isStoreSelectedUser=isStoreSelectedUser;
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.userInformationList=userInformationList;
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
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		JLabel label=new JLabel("Login Name:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		loginNameComboBox=new JComboBox<String>();
		for (UserInformation userInformation:userInformationList) {
				loginNameComboBox.addItem(userInformation.getLoginName());
		}
		UIUtils.setDimension(loginNameComboBox,fieldsWidth,height);
		tempPanel.add(loginNameComboBox);
				
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		label=new JLabel("Password:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,height);
		tempPanel.add(label);
		passwordTextField=new JPasswordField();
		UIUtils.setDimension(passwordTextField,fieldsWidth,height);
		tempPanel.add(passwordTextField);
				
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
			public void actionPerformed(ActionEvent e) {
				processActionOK();
			}

		});
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
				isCancelled=true;
				dialog.dispose();
			}
		}
				);
		tempPanel.add(closeButton);
	}
	
	protected void processActionOK() {
		String selectedLoginName=(String)loginNameComboBox.getSelectedItem();
		String password=getPassword();
		for (UserInformation userInformation:userInformationList) {
			if (userInformation.getLoginName().equalsIgnoreCase(selectedLoginName)) {
				if (userInformation.getPassword().equals(password)) {
					if (isStoreSelectedUser) {
						tabSnapshotsPanel.getServerSelectionPanel().setSelectedUser(userInformation);
					}
					isSuccess=true;
					setSelectedUser(userInformation);
					dialog.setVisible(false);
				}
				else {
					GUIUtils.popupErrorMessage("Wrong password!");
				}
				return;
			}
		}
	}

	public String getPassword() {
		return new String(passwordTextField.getPassword());
	}

	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public boolean isCancelled() {
		return isCancelled;
	}
	
}