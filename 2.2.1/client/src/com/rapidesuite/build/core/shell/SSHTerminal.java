package com.rapidesuite.build.core.shell;

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

public class SSHTerminal extends JFrame implements ActionListener, KeyListener
{

	private static final long serialVersionUID = 1L;
	public JPanel centerPanel;
	public JPanel bottomPanel;
	public JMenuBar menuBar;
	public JMenu menu;
	public JMenuItem menuItem;
	public JTextArea receiveArea;
	public JTextField sendField;
	public JButton sendButton;
	private SSHShellRobot robot;

	public SSHTerminal(String title, SSHShellRobot robot) throws Exception
	{
		super(title);
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setSize(700, 550);
		this.setLocation(50, 50);
		this.robot = robot;
		menuBar = new JMenuBar();
		menu = new JMenu("Menu");
		menuBar.add(menu);
		menuItem = new JMenuItem("Disconnect");
		menu.add(menuItem);
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
	}

	public void actionPerformed(ActionEvent event)
	{
		try
		{
			if ( event.getSource() instanceof JButton )
			{
				sendText();
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			FileUtils.printStackTrace(e);
		}
	}

	public String getText()
	{
		String res = receiveArea.getText();
		return res;
	}

	public void sendText() throws Exception
	{
		String s = sendField.getText();
		robot.sendText(s);
		sendField.setText("");
	}

	/** Handle the key pressed event from the text field. */
	public void keyPressed(KeyEvent e)
	{
		// do nothing;
	}

	/** Handle the key released event from the text field. */
	public void keyReleased(KeyEvent e)
	{
		// do nothing;
	}

	public void setReceivedText(String value)
	{
		try
		{
			receiveArea.append(value);
			moveCursor();
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

}
