/**************************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/Terminal.java $:
 * $Id: Terminal.java 31694 2013-03-04 06:33:20Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.rapidesuite.client.common.util.FileUtils;

public class Terminal extends JFrame implements ActionListener, KeyListener
{

	private static final long serialVersionUID = 1L;
	public JPanel centerPanel;
	public JPanel bottomPanel;
	public JMenuBar menuBar;
	public JMenu menu;
	public JMenuItem menuItem;
	public JTextArea receiveArea;
	public JTextField sendField;
	public BuilderWizardTelnet bwt;
	public JButton sendButton;

	public Terminal(BuilderWizardTelnet bwt, String title) throws Exception
	{
		super(title);
		this.bwt = bwt;
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setSize(700, 550);
		this.setLocation(50, 50);
		menuBar = new JMenuBar();
		menu = new JMenu("Menu");
		menuBar.add(menu);
		menuItem = new JMenuItem("Disconnect");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		this.setJMenuBar(menuBar);
		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(true);
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setOpaque(true);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(centerPanel, BorderLayout.CENTER);
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		JLabel receiveLabel = new JLabel("Console");
		receiveLabel.setHorizontalAlignment(SwingConstants.CENTER);
		centerPanel.add(receiveLabel, BorderLayout.NORTH);

		receiveArea = new JTextArea();
		receiveArea.setEditable(false);
		receiveArea.setBackground(Color.black);
		String font = "Monospaced";
		int style = Font.PLAIN;
		int fsize = 12;
		receiveArea.setFont(new Font(font, style, fsize));
		receiveArea.setForeground(Color.white);

		JScrollPane scrollPane = new JScrollPane(receiveArea);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel bottomSubPanel = new JPanel();
		JLabel sendLabel = new JLabel("Text to send");
		sendLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bottomPanel.add(sendLabel, BorderLayout.NORTH);

		bottomSubPanel.setLayout(new BorderLayout());
		bottomPanel.add(bottomSubPanel, BorderLayout.CENTER);

		sendField = new JTextField("");
		bottomSubPanel.add(sendField, BorderLayout.CENTER);
		sendButton = new JButton("Send");
		sendButton.addActionListener(this);
		sendField.addKeyListener(this);
		bottomSubPanel.add(sendButton, BorderLayout.EAST);
		this.setVisible(true);
	}

	public String getText()
	{
		String res = receiveArea.getText();
		return res;
	}

	public void setReceivedText(String value)
	{
		try
		{
			receiveArea.append(value);
			moveCursor();
			bwt.setGUIOutput(value);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public void moveCursor()
	{
		try
		{
			receiveArea.setCaretPosition(receiveArea.getDocument().getLength() - 1);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public void keyTyped(KeyEvent e)
	{
		try
		{
			char c = e.getKeyChar();
			if ( c == '\n' )
			{
				sendText();
			}
		}
		catch ( Exception ex )
		{
			FileUtils.printStackTrace(ex);
		}
	}

	public void sendText() throws Exception
	{
		String s = sendField.getText();
		bwt.getTelnet().sendText(s);
		sendField.setText("");
	}

	public void actionPerformed(ActionEvent event)
	{
		try
		{
			if ( event.getSource() instanceof JButton )
			{
				sendText();
			}
			else
			{
				if ( menuItem.getText().equalsIgnoreCase("Reconnect") )
				{
					receiveArea.setText("");
					sendField.setEnabled(true);
					sendField.setBackground(Color.white);
					sendButton.setEnabled(true);
					menuItem.setText("Disconnect");
					bwt.getTelnet().connect();
					bwt.getTelnet().start();
				}
				else
				{
					receiveArea.setText("   DISCONNECTED...   ");
					sendField.setEnabled(false);
					sendField.setBackground(Color.lightGray);
					sendButton.setEnabled(false);
					menuItem.setText("Reconnect");
					bwt.getTelnet().disconnect();

					bwt.cloneTelnet();
					bwt.fireEvent();
				}
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	/** Handle the key pressed event from the text field. */
	public void keyPressed(KeyEvent e)
	{
		// do nothing
	}

	/** Handle the key released event from the text field. */
	public void keyReleased(KeyEvent e)
	{
		// do nothing
	}

}
