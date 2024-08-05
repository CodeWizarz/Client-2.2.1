package com.rapidesuite.build.gui.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rapidesuite.build.core.htmlplayback.engines.xul.XULParser;
import com.rapidesuite.build.utils.CurrentBrowserTask;
import com.rapidesuite.client.common.gui.CustomFileFilter;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;

public class XULParserFrame extends JFrame
{

	private static final long serialVersionUID = 1L;
	File selectedFile;
	private JTextField openFileTextField;
	private JButton openFileButton;
	JButton parseButton;
	private JLabel titleLabel;
	private JLabel messageLabel;
	private JFileChooser fileChooser;

	public XULParserFrame() throws Exception
	{
		createComponents();
	}

	private void createComponents() throws Exception
	{
		titleLabel = GUIUtils.getLabel("Select the HTML file to parse: ", true);
		messageLabel = GUIUtils.getLabel("", true);

		openFileTextField = GUIUtils.getInputField(true, false);

		openFileButton = GUIUtils.getButton(this.getClass(), "Browse", "/images/open16.gif");
		openFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					openFileChooser();
				}
				catch ( Exception ex )
				{
					ex.printStackTrace();
					GUIUtils.popupErrorMessage("error: " + ex.getMessage());
				}
			}
		});

		parseButton = GUIUtils.getButton(this.getClass(), "Start", "/images/play16.gif");
		parseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if ( parseButton.getText().equalsIgnoreCase("start") )
					{
						start();
					}
					else
					{
						stop();
					}
				}
				catch ( Exception ex )
				{
					ex.printStackTrace();
					GUIUtils.popupErrorMessage("error: " + ex.getMessage());
				}
			}
		});

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 15, 15));
		this.add(northPanel, BorderLayout.NORTH);

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		northPanel.add(tempPanel);
		tempPanel.add(titleLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		tempPanel.add(openFileTextField);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		tempPanel.add(openFileButton);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 5)));

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 270, 0, 0));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		this.add(centerPanel, BorderLayout.CENTER);
		centerPanel.add(parseButton);
		centerPanel.add(Box.createRigidArea(new Dimension(20, 50)));

		JPanel southPanel = new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(0, 250, 50, 0));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		this.add(southPanel, BorderLayout.SOUTH);
		GUIUtils.addToPanel(southPanel, messageLabel, null, null, null, new Dimension(5, 0), new Dimension(0, 0));

		fileChooser = Utils.initializeJFileChooserWithTheLastPath("FILE_CHOOSER_XUL_PARSER_FRAME");
		Map<String, String> extensionTypes = new HashMap<String, String>();
		extensionTypes.put("html", "html");
		extensionTypes.put("htm", "htm");
		CustomFileFilter filter = new CustomFileFilter(extensionTypes);
		fileChooser.setFileFilter(filter);

		this.setSize(new Dimension(700, 300));
		this.setTitle("HTML parser");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void resetMessage()
	{
		GUIUtils.resetLabel(messageLabel);
	}

	public JLabel getMessageLabel()
	{
		return messageLabel;
	}

	public void start()
	{
		lockComponents();
		new Thread(new XULParser(this, selectedFile)).start();
	}

	public void stop()
	{
		CurrentBrowserTask.eliminateTask();
		unlockComponents();
	}

	public void lockComponents()
	{
		openFileButton.setEnabled(false);
		resetMessage();
		ImageIcon ii = GUIUtils.getImageIcon(this.getClass(), "/images/stop16.gif");
		parseButton.setIcon(ii);
		parseButton.setText("Stop");
	}

	public void unlockComponents()
	{
		openFileButton.setEnabled(true);
		resetMessage();
		ImageIcon ii = GUIUtils.getImageIcon(this.getClass(), "/images/play16.gif");
		parseButton.setIcon(ii);
		parseButton.setText("Start");
	}

	public void openFileChooser() throws Exception
	{
		int returnVal = fileChooser.showDialog(this, "Open");
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			selectedFile = fileChooser.getSelectedFile();
			openFileTextField.setText(selectedFile.getAbsolutePath());
		}
	}

	public static void main(String[] arg)
	{
		try
		{
			new XULParserFrame();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			GUIUtils.popupErrorMessage("error: " + e.getMessage());
		}
	}

}
