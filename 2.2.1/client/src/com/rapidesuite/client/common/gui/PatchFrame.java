package com.rapidesuite.client.common.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicProgressBarUI;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.PatchUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.domain.Update;
import com.rapidesuite.configurator.utility.UpdateUtils;

@SuppressWarnings("serial")
public class PatchFrame extends PatchDialog {

    public PatchFrame(SwiftGUIMain swiftGUIMain,PatchManager patchManager) throws Exception{
    	super("Automatic Updates",swiftGUIMain.getRootFrame(),patchManager);
    	initComponents(swiftGUIMain);
    }

    final JPanel centerPanel= new JPanel();
    final JLabel inProgressLabel=new JLabel();

    private void addNewPatches(final String majorVersion, final int currentRevision){
    	try {
        	GUIUtils.showInProgressMessage(inProgressLabel,"Please wait, refreshing patch list...");

        	if (StringUtils.isBlank(patchManager.getDownloadURL())) {
        		throw new RuntimeException(com.rapidesuite.client.common.util.Config.PATCH_URL+" is not set");
        	}

			final Update latestPatch = UpdateUtils.getPatch(majorVersion, currentRevision, patchManager.getDownloadURL(), patchManager.getUserName(), patchManager.getPassword());

			if (latestPatch == null) {
				GUIUtils.showStandardMessage(inProgressLabel,"<b>Your version is up-to-date (revision: "+currentRevision+")</b>.");
				return;
			}

			final JPanel tempPanel = new JPanel();
			tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			String patchName=latestPatch.getName();
			final JLabel link=GUIUtils.getHyperlinkComponent(patchName);
			link.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent me){
					try {
						File file=PatchUtils.downloadFile(latestPatch.getDocumentUrl().toExternalForm(),false,FileUtils.getTemporaryFolder(),null,
								patchManager.getUserName(),patchManager.getPassword());
						Desktop.getDesktop().open(file);
					}
					catch (Exception e) {
						GUIUtils.popupErrorMessage("Error: "+e.getMessage());
					}
				}
			});
			tempPanel.add(link);

			final JProgressBar progressBar = new JProgressBar();
			progressBar.setUI(new BasicProgressBarUI() {
			      protected Color getSelectionBackground() { return Color.black; }
			      protected Color getSelectionForeground() { return Color.white; }
			    });
			tempPanel.add(progressBar);
			GUIUtils.setComponentDimension(progressBar,230,25);
			final JButton applyButton=new JButton("Apply");
			tempPanel.add(applyButton);
			applyButton.addActionListener( new ActionListener(){
	            public void actionPerformed(ActionEvent e) {
	            	downloadAndApplyPatch(latestPatch,progressBar);
	            }
	        });
			centerPanel.add(tempPanel);
			progressBar.setVisible(true);

			GUIUtils.resetLabel(inProgressLabel);
			centerPanel.revalidate();
			centerPanel.repaint();

		}
    	catch(Throwable e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to check for updates. Error: "+e.getMessage());
		}
    }

    private void downloadAndApplyPatch(final Update update,final JProgressBar patchProgressBar)
    {
    	if (patchManager.isUpdateInProgress()) {
    		GUIUtils.popupInformationMessage("Patching in progress...");
    		return;
    	}
    	patchManager.setUpdateInProgress(true);
    	patchProgressBar.setString("Connecting...");
    	patchProgressBar.setStringPainted(true);
    	Runnable r=new Runnable(){
    		public void run(){
    			try {
    				GUIUtils.showInProgressMessage(inProgressLabel,"Please wait, patching in progress...");
    				PatchUtils.downloadAndApplyPatch(update,patchProgressBar,patchManager.getExecutableFileName(),patchManager.getUserName(),patchManager.getPassword());
    			}
    			catch (Exception ex) {
    				FileUtils.printStackTrace(ex);
    				GUIUtils.popupErrorMessage("Unable to apply patches, error: "+ex.getMessage());
    			}
    			finally {
    				patchManager.setUpdateInProgress(false);
    				GUIUtils.resetLabel(inProgressLabel);
    			}
    		}
    	};
    	Thread t=new Thread(r);
    	t.start();
    }

    public void queryServerForPatches(final String majorVersion, final int currentRevision){
    	Runnable r=new Runnable(){
    		public void run(){
    			try {
    				addNewPatches(majorVersion, currentRevision);
				} catch (Exception e) {
					FileUtils.printStackTrace(e);
					GUIUtils.popupErrorMessage("Error: "+e.getMessage());
				}
    		}
    	};
    	Thread t=new Thread(r);
    	t.start();
    }

    private void initComponents(SwiftGUIMain swiftGUIMain) throws Exception{
    	this.setSize(UtilsConstants.PATCH_FRAME_WIDTH, UtilsConstants.PATCH_FRAME_HEIGHT);

    	JPanel patchPanel = new JPanel();
    	patchPanel.setLayout(new BorderLayout());

    	final JPanel northPanel = new JPanel();
    	patchPanel.add(northPanel,BorderLayout.NORTH);
		northPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 5));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.add(new JLabel("<html>See below for the patch.<br/> Click on the link to open"+
	    		" the corresponding 'Release Notes' file.<br/> Click 'Apply' to download and install the patch."+
	    		"<br/>Note that the program will be restarted after the patch is installed.</html>"));
		northPanel.add(Box.createRigidArea(new Dimension(0,15)));
		northPanel.add(inProgressLabel);

		JScrollPane scrollPane = new JScrollPane(centerPanel);
		patchPanel.add(scrollPane, BorderLayout.CENTER);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

    	this.add(patchPanel);

    	this.queryServerForPatches(swiftGUIMain.getApplicationMajorVersion(), swiftGUIMain.getApplicationRevision());
    }

}
