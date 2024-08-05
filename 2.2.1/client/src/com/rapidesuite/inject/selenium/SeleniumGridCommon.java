package com.rapidesuite.inject.selenium;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;

@SuppressWarnings("serial")
public class SeleniumGridCommon extends JFrame{

	protected Process process;
	protected JTextArea textArea;
	protected JButton startButton;
	protected JButton stopButton;
	protected final static String DEFAULT_AUTOREFRESH ="DEFAULT_AUTOREFRESH";
	protected int logPollTime=3000;
	protected JCheckBox autoRefreshCheckBox;
		
	protected void updateLogTextArea(final File logFile) {
		Thread t = new Thread()
		{
			public void run()
			{
				// This will allow to show the log once
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					FileUtils.printStackTrace(e);
				}
				readLogFile(logFile);
				while (process!=null) {
					try {
						if (!autoRefreshCheckBox.isSelected()) {
							Thread.sleep(logPollTime);
							continue;
						}
						readLogFile(logFile);
						Thread.sleep(logPollTime);
					} 
					catch (InterruptedException e) {
						FileUtils.printStackTrace(e);
					}
				}
			}
		};
		t.start();
	}

	protected void readLogFile(File logFile) {
		BufferedReader br=null;
		try{
			textArea.setText("");
			FileInputStream fstream = new FileInputStream(logFile);
			br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			while ((line = br.readLine()) != null)   {
				textArea.append(line+"\n");
				if (textArea.getText().trim().isEmpty()) {
					textArea.setText("ERROR: Selenium did not start!!!!!!");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			FileUtils.printStackTrace(e);
		}
		finally {
			if (br!=null) {
				try {
					br.close();
				} catch (IOException e) {
					FileUtils.printStackTrace(e);
				}
			}
		}
	}

	public void addLogTextArea(JPanel tempPanel,Preferences pref,boolean showAutoRefresh) {
		if (showAutoRefresh) {
			autoRefreshCheckBox=new JCheckBox("Auto-refresh");
			boolean isRefresh= pref.getBoolean(DEFAULT_AUTOREFRESH,false);
			autoRefreshCheckBox.setSelected(isRefresh);
			tempPanel.add(autoRefreshCheckBox);
		}

		textArea=new JTextArea();
		textArea.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(textArea,InjectMain.FONT_SIZE_NORMAL);
		textArea.setEditable(false);
		textArea.setOpaque(true);
		textArea.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setViewportView(textArea);
		tempPanel.add(scrollPane);
	}
	
	public synchronized void log(File logFile,String message)
	{
		FileUtils.log(null,message+"\n",true,true,logFile);
	}
	
	public synchronized void log(File logFile,Throwable tr)
	{
		FileUtils.log(tr,null,true,true,logFile);
	}
	
}
