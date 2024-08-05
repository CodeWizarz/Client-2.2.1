package com.rapidesuite.snapshot.view;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.rapidesuite.snapshot.model.UserInformation;

@SuppressWarnings("serial")
public class GenericLoginPanel extends JPanel {

	protected JDialog dialog;
	protected boolean isSuccess;
	private UserInformation selectedUser;
	
	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public UserInformation getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserInformation selectedUser) {
		this.selectedUser = selectedUser;
	}

}
