/**************************************************
 * $Revision: 42538 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-07-30 14:03:54 +0700 (Wed, 30 Jul 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/ExecutionPanel.java $:
 * $Id: ExecutionPanel.java 42538 2014-07-30 07:03:54Z fajrian.yunus $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.ProgrammaticallyOperable;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.reverse.ReverseMain;

@SuppressWarnings("serial")
public abstract class ExecutionPanel  extends JPanel implements ProgrammaticallyOperable
{

	private SwiftGUIMain swiftGUIMain;
	protected ExecutionStatusPanel executionStatusPanel;
	protected JButton startJButton;
	protected JButton stopJButton;
	protected JButton backJButton;
	protected JLabel statusLabel;
	protected ActionListener onStartButtonIsClicked = null;
	
	public ExecutionPanel(SwiftGUIMain swiftGUIMain)
	{
		this.swiftGUIMain=swiftGUIMain;
		
		URL iconURL = this.getClass().getResource("/images/back16.gif");
		ImageIcon ii=null;
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		backJButton=new JButton("Back", ii);
		backJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		backJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				getSwiftGUIMain().switchToPanel(UtilsConstants.PANEL_PACKAGES_SELECTION);
				if (getSwiftGUIMain() instanceof ReverseMain) {
					getSwiftGUIMain().clearAndHideInformationLabelText(((ReverseMain)getSwiftGUIMain()).getInventoriesNameLabelIndex());
				}				
			}
		}
		);

		iconURL = this.getClass().getResource("/images/play16.gif");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startJButton=new JButton("Start", ii);
		startJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		onStartButtonIsClicked = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{ 
					startExecution();
				}
				catch(Throwable tr) {
					FileUtils.printStackTrace(tr);
					GUIUtils.popupErrorMessage("Error: "+CoreUtil.getAllThrowableMessages(tr));
				}
			}
		};
		startJButton.addActionListener(onStartButtonIsClicked);

		iconURL = this.getClass().getResource("/images/stop16.gif");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		stopJButton=new JButton("Stop", ii);
		stopJButton.setEnabled(false);
		stopJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		stopJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{ 
					stopExecution();
				}
				catch(Exception ex) {
					FileUtils.printStackTrace(ex);
				}
			}
		}
		);
		
		statusLabel= new JLabel("");
		statusLabel.setFont(GUIUtils.BOLD_SYSTEM_FONT);
		statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}

	public SwiftGUIMain getSwiftGUIMain() {
		return swiftGUIMain;
	}
	
	public abstract void startExecution() throws Exception;
	
	public abstract void stopExecution() throws Exception;
	
	public JLabel getStatusLabel() {
		return statusLabel;
	}
	
	public void lockAll() {
		startJButton.setEnabled(false);
		stopJButton.setEnabled(false);
		backJButton.setEnabled(false);
	}

	public void unlockAll() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				startJButton.setEnabled(true);
				stopJButton.setEnabled(false);
				backJButton.setEnabled(true);
			}
			
		});
	}
	
	public boolean isStartButtonEnabled() {
		return startJButton.isEnabled();
	}
	
	public ExecutionStatusPanel getExecutionStatusPanel() {
		return executionStatusPanel;
	}
	
	public ExecutionStatusPanel lockStatusPanel() {
		return executionStatusPanel;
	}
	
	public void refreshTable(){
		executionStatusPanel.getExecutionAllStatusTreeTable().repaint();
	}
	
	public void updateExecutionStatus(Job executionJob,String status) {
		executionStatusPanel.updateExecutionStatus(executionJob,status);
	}
	
}