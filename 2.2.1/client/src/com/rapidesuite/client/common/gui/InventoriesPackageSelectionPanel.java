package com.rapidesuite.client.common.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.client.common.InventoriesPackageOpenAction;
import com.rapidesuite.client.common.ProgrammaticallyOperable;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;

@SuppressWarnings("serial")
public abstract class InventoriesPackageSelectionPanel extends JPanel implements ProgrammaticallyOperable
{

	protected SwiftGUIMain swiftGUIMain;
	protected FileBrowser inventoriesPackageFileBrowser;
	protected JButton backButton;
	protected JButton nextButton;
	protected ActionListener onNextButtonIsClicked = null;
	protected JLabel messageLabel;
	protected LinkedHashMap<String, String> displayedItemPathToActualItemPathOrderedMap;
	protected Map<String,Inventory> inventoriesMap;
	protected JPanel centerPanel;
	protected PLSQLPackageValidationPanel plsqlPackageValidationPanel;
	public PLSQLPackageValidationPanel getPlsqlPackageValidationPanel()
	{
	    return this.plsqlPackageValidationPanel;
	}

	public InventoriesPackageSelectionPanel(SwiftGUIMain swiftGUIMain)
	throws Exception
	{
		this.swiftGUIMain=swiftGUIMain;
	}

	public File getInventoriesPackageFile() {
		return inventoriesPackageFileBrowser.getSelectedFile();
	}

	public void createComponents() throws Exception {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(50,50,50,50));

		centerPanel = new JPanel();
		//centerPanel.setBackground(Color.RED);
		centerPanel.setLayout(new BoxLayout( centerPanel, BoxLayout.Y_AXIS));
		//(int top, int left, int bottom, int right)
		centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Package selection"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		add( centerPanel, BorderLayout.CENTER);

		inventoriesPackageFileBrowser=new FileBrowser("Select the Inventories package: ",false,new SevenZipPackageFileFilter(),new InventoriesPackageOpenAction(this) );

		messageLabel=new JLabel();
		messageLabel.setFont(GUIUtils.BOLD_SYSTEM_FONT);
		messageLabel.setForeground(Color.RED);
		JPanel tempPanel= new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.add(messageLabel);

		int width=700;
		int height=35;
		GUIUtils.setComponentDimension(inventoriesPackageFileBrowser.getPanel(),width,height);
		JPanel fileBrowserWrapper = new JPanel();
		fileBrowserWrapper.add(inventoriesPackageFileBrowser.getPanel());
		fileBrowserWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.add(fileBrowserWrapper);
		centerPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		centerPanel.add(tempPanel);


        plsqlPackageValidationPanel=new PLSQLPackageValidationPanel(this.swiftGUIMain);
        GUIUtils.setComponentDimension(plsqlPackageValidationPanel,700,150);
        //(int top, int left, int bottom, int right)
        plsqlPackageValidationPanel.setBorder(BorderFactory.createEmptyBorder(30,0,0,0));
        plsqlPackageValidationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(plsqlPackageValidationPanel);

		JPanel southPanel= new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		add(southPanel, BorderLayout.SOUTH);

		nextButton=GUIUtils.getButton(this.getClass(),"Next","/images/forward16.gif");
		nextButton.setEnabled(true);
		nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		onNextButtonIsClicked = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					next();
				}
				catch(Exception ex) {
				    FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		};
		nextButton.addActionListener(onNextButtonIsClicked);

		backButton=GUIUtils.getButton(this.getClass(),"Back","/images/back16.gif");
		backButton.setEnabled(true);
		backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				swiftGUIMain.switchToPanel(UtilsConstants.PANEL_ENVIRONMENT_SELECTION);
				swiftGUIMain.clearAndHideInformationLabelText(swiftGUIMain.getBweNameLabelIndex());
			}
		}
		);

		JPanel panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		//(int top, int left, int bottom, int right)
		panel.setBorder(BorderFactory.createEmptyBorder(100,50,50,0));
		southPanel.add(panel);
		panel.add(backButton);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(nextButton);
	}

	public abstract void next() throws Exception;

	public JButton getInventoriesPackageSelectionButton() {
		return inventoriesPackageFileBrowser.getButton();
	}

	public JButton getNextButton() {
		return nextButton;
	}

	public JButton getBackButton() {
		return backButton;
	}

	public SwiftGUIMain getSwiftGUIMain() {
		return swiftGUIMain;
	}

	public JLabel getMessageLabel() {
		return messageLabel;
	}

	public void setDisplayedItemPathToActualItemPathOrderedMap(LinkedHashMap<String, String> displayedItemPathToActualItemPathOrderedMap) throws Exception {
		this.displayedItemPathToActualItemPathOrderedMap=displayedItemPathToActualItemPathOrderedMap;
	}

	public void parseInventories() throws Exception
	{
		inventoriesMap=DataExtractionFileUtils.getInventoriesMap(messageLabel,getInventoriesPackageFile(),new ArrayList<String>(displayedItemPathToActualItemPathOrderedMap.values()));
	}

	public Map<String, Inventory> getInventoriesMap() {
		return inventoriesMap;
	}

	public void loadDefaultInventoriesPackageFile(File defaultInventoriesPackage)
	{
		try{
		if(defaultInventoriesPackage != null) {
			inventoriesPackageFileBrowser.setSelectedFile(defaultInventoriesPackage);
		}
		}
		catch(Exception e) {
			e.printStackTrace();
			GUIUtils.popupErrorMessage("Error opening default package: "+e.getMessage());
		}
	}


    private File controlFolder;
    private Map<String,File> packageNameToFileMap = new HashMap<String,File>();

    public File getControlFolder()
    {
        return controlFolder;
    }
    public void setControlFolder(File controlFolder)
    {
        this.controlFolder = controlFolder;
    }

    public Map<String,File> getPackageNameToFileMap()
    {
        return this.packageNameToFileMap;
    }



}