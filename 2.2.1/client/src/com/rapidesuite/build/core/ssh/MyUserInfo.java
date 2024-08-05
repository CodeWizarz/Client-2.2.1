package com.rapidesuite.build.core.ssh;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.rapidesuite.client.common.util.FileUtils;

public class MyUserInfo
{

	public String getPassword()
	{
		return null;
	}

	public boolean promptYesNo(String str)
	{
		return true;
	}

	String passphrase;
	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	JTextField passphraseField = new JPasswordField(20);

	public String getPassphrase()
	{
		return passphrase;
	}

	public boolean promptPassphrase(String message)
	{
		Object[] ob = { passphraseField };
		int result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);
		if ( result == JOptionPane.OK_OPTION )
		{
			passphrase = passphraseField.getText();
			return true;
		}
		return false;
	}

	public boolean promptPassword(String message)
	{
		return true;
	}

	public void showMessage(String message)
	{
		// JOptionPane.showMessageDialog(null, message);
		FileUtils.println("MESSAGE received from SSH server: '" + message + "'");
	}

	final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
	private Container panel;

	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo)
	{
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		gbc.weightx = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridx = 0;
		panel.add(new JLabel(instruction), gbc);
		gbc.gridy++;

		gbc.gridwidth = GridBagConstraints.RELATIVE;

		JTextField[] texts = new JTextField[prompt.length];
		for ( int i = 0; i < prompt.length; i++ )
		{
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.weightx = 1;
			panel.add(new JLabel(prompt[i]), gbc);

			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weighty = 1;
			if ( echo[i] )
			{
				texts[i] = new JTextField(20);
			}
			else
			{
				texts[i] = new JPasswordField(20);
			}
			panel.add(texts[i], gbc);
			gbc.gridy++;
		}

		if ( JOptionPane.showConfirmDialog(null, panel, destination + ": " + name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION )
		{
			String[] response = new String[prompt.length];
			for ( int i = 0; i < prompt.length; i++ )
			{
				response[i] = texts[i].getText();
			}
			return response;
		}
		return null; // cancel
	}

	public void closeAllPrompts() {
		JOptionPane.getRootFrame().dispose();
	}
}