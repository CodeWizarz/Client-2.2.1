/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/OracleUsersSelectionPanel.java $:
 * $Id: OracleUsersSelectionPanel.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
 */

package com.rapidesuite.reverse.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.reverse.DataExtractionConstants;
import com.rapidesuite.reverse.ReverseMain;
import com.rapidesuite.reverse.utils.DataExtractionDatabaseUtils;
import com.rapidesuite.reverse.utils.DataExtractionDatabaseUtils.OracleUserRetrievalExecutionMonitor;

@SuppressWarnings("serial")
public class OracleUsersSelectionPanel extends JPanel {

	private DataExtractionPanel panel;
	private JTabbedPane tabbedPane;
	
	private JPanel seededOracleUsersScrollPaneAndControlPanel;
	private JScrollPane expectedSeededOracleUsersScrollPane;
	private JPanel expectedSeededOracleUsersPane;
	private JScrollPane potentialSeededOracleUsersScrollPane;
	private JPanel potentialSeededOracleUsersPane;
	private JPanel seededOracleUsersAll;
	private Map<Integer, String> seededOracleUserIdToUserNameMap;
	private List<JCheckBox> expectedSeededOracleUsersCheckBoxes;
	private List<JCheckBox> potentialSeededOracleUsersCheckBoxes;
	private JRadioButton oracleUsersIncludeButton;
	private JRadioButton oracleUsersExcludeButton;
	private JCheckBox seededCheckBox;
	
	private JScrollPane standardOracleUsersScrollPane;
	private JPanel listedStandardOracleUsersScrollPaneAndControlPanel;
	private JPanel standardOracleUsersCenterPanel;
	private JPanel listedStandardOracleUsersPanel;
	private Map<Integer, String> standardOracleUserIdToUserNameMap;
	
	private JCheckBox standardOracleUsersSelectUnselectCheckbox;
	private JButton standardOracleUsersRemoveSelectedButton;
	
	private JTextField standardOracleUsersSearchField;
	private JButton standardOracleUsersSearchButton;	
	
	private JPanel seededUsersMessagePanel;
	private boolean isStandardOracleUserLoaded;
	
	private TreeMap<String, Integer> listedStandardOracleUsers;
	
	private Set<String> foundExpectedAndPotentialSeededUsers;
	private final Set<JCheckBox> notFoundExpectedSeededUsersCheckBoxes;
	
	public static final Dimension ORACLE_USER_CHECKBOX_DIMENSION = new Dimension(250,15);
		
	public OracleUsersSelectionPanel(DataExtractionPanel panel)
	{
		this.panel=panel;
		this.foundExpectedAndPotentialSeededUsers = null;
		this.notFoundExpectedSeededUsersCheckBoxes = new HashSet<JCheckBox>();
		createComponents();
	}

	public void createComponents()
	{
		
		this.listedStandardOracleUsers = new TreeMap<String, Integer>();
		expectedSeededOracleUsersCheckBoxes=new ArrayList<JCheckBox>();
		potentialSeededOracleUsersCheckBoxes=new ArrayList<JCheckBox>();
		seededCheckBox=GUIUtils.getSelectionAllCheckBox(true);
		seededCheckBox.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 0));
		seededCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				final List<JCheckBox> checkboxesToOperate = new ArrayList<JCheckBox>();
				checkboxesToOperate.addAll(expectedSeededOracleUsersCheckBoxes);
				checkboxesToOperate.addAll(potentialSeededOracleUsersCheckBoxes);
				checkboxesToOperate.removeAll(notFoundExpectedSeededUsersCheckBoxes);
				GUIUtils.setSelectedCheckBox(checkboxesToOperate,seededCheckBox.isSelected());
			}
		}
		);
		oracleUsersIncludeButton= new JRadioButton("Include");
		oracleUsersIncludeButton.setSelected(false);
		oracleUsersExcludeButton = new JRadioButton("Exclude");
		oracleUsersExcludeButton.setSelected(true);
		expectedSeededOracleUsersPane = new JPanel();
		expectedSeededOracleUsersScrollPane = new JScrollPane(expectedSeededOracleUsersPane);
		potentialSeededOracleUsersPane = new JPanel();
		potentialSeededOracleUsersScrollPane = new JScrollPane(potentialSeededOracleUsersPane);
		seededOracleUsersAll = new JPanel();
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  
		seededUsersMessagePanel=new JPanel();
		seededUsersMessagePanel.setLayout(new BoxLayout(seededUsersMessagePanel, BoxLayout.Y_AXIS));
		JLabel label= new JLabel("Note: Seeded users are excluded by default.");
		panel.add(label);
		panel.add(Box.createRigidArea(new Dimension(10,10)));
		panel.add(seededUsersMessagePanel);
		GUIUtils.addToUserPanel(seededOracleUsersAll,panel,null,null,null);
		
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  
		panel.add(Box.createRigidArea(new Dimension(20,20)));
		panel.add(new JLabel("<html></html>"));
		
		JLabel standardOracleUsersSearchLabel = new JLabel("User name (accepts % as wildcard):");
		panel.add(standardOracleUsersSearchLabel);
		standardOracleUsersSearchField = new JTextField();
		panel.add(standardOracleUsersSearchField);
		standardOracleUsersSearchButton = new JButton("Search");
		panel.add(standardOracleUsersSearchButton);
		final ActionListener searchListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (StringUtils.isNotBlank(standardOracleUsersSearchField.getText())) {
					SwiftGUIMain.getInstance().getRootFrame().setEnabled(false);
					loadStandardOracleUsers(standardOracleUsersSearchField.getText().trim(), standardOracleUsersSearchField, standardOracleUsersSearchButton);
				}
			}
		};
		standardOracleUsersSearchField.addActionListener(searchListener);
		standardOracleUsersSearchButton.addActionListener(searchListener);
		
		ButtonGroup group = new ButtonGroup();
		group.add(oracleUsersIncludeButton);
		group.add(oracleUsersExcludeButton);
				
		standardOracleUsersCenterPanel=new JPanel();
		GUIUtils.addToUserPanel(standardOracleUsersCenterPanel,panel,null,null,null);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Seeded Oracle users", null,seededOracleUsersAll,"");
		tabbedPane.addTab("Standard Oracle users", null,standardOracleUsersCenterPanel,"");
	
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  
		panel.add(oracleUsersIncludeButton);
		panel.add(oracleUsersExcludeButton);
		
		this.add(panel);
		this.add(Box.createRigidArea(new Dimension(10,10)));
		this.add(tabbedPane);
		
		listedStandardOracleUsersPanel = GUIUtils.createJComponentVerticalListPanel();
		JScrollPane listedStandardOracleUsersScrollPane = new JScrollPane(listedStandardOracleUsersPanel);		
		listedStandardOracleUsersScrollPaneAndControlPanel = new JPanel();
		listedStandardOracleUsersScrollPaneAndControlPanel.setLayout(new BorderLayout());
		listedStandardOracleUsersScrollPaneAndControlPanel.add(listedStandardOracleUsersScrollPane, BorderLayout.CENTER);
		JPanel listedStandardOracleUsersControlPanel = new JPanel();
		listedStandardOracleUsersControlPanel.setLayout(new BorderLayout());
		
		standardOracleUsersSelectUnselectCheckbox = GUIUtils.getSelectionAllCheckBox(true);
		standardOracleUsersSelectUnselectCheckbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				for (final Component component : listedStandardOracleUsersPanel.getComponents()) {
					final JCheckBox checkbox = (JCheckBox) component;
					checkbox.setSelected(standardOracleUsersSelectUnselectCheckbox.isSelected());
				}
			}
		});
		standardOracleUsersRemoveSelectedButton = new JButton("Remove Unchecked");
		standardOracleUsersRemoveSelectedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (final Component component : listedStandardOracleUsersPanel.getComponents()) {
					final JCheckBox checkbox = (JCheckBox) component;
					if (!checkbox.isSelected()) {
						listedStandardOracleUsers.remove(checkbox.getText());
						listedStandardOracleUsersPanel.remove(component);
					}
				}
				//somehow without these, listedStandardOracleUsersPanel doesn't repaint, even after invoking repaint
				listedStandardOracleUsersPanel.setVisible(false);
				listedStandardOracleUsersPanel.setVisible(true);
			}
		});
		final JPanel selectAllAndRemoveSelectedWrapper = new JPanel();
		selectAllAndRemoveSelectedWrapper.add(standardOracleUsersSelectUnselectCheckbox);
		selectAllAndRemoveSelectedWrapper.add(standardOracleUsersRemoveSelectedButton);
		listedStandardOracleUsersControlPanel.add(selectAllAndRemoveSelectedWrapper, BorderLayout.WEST);
		listedStandardOracleUsersScrollPaneAndControlPanel.add(listedStandardOracleUsersControlPanel, BorderLayout.SOUTH);
		seededOracleUsersScrollPaneAndControlPanel = new JPanel();
		seededOracleUsersScrollPaneAndControlPanel.setLayout(new BorderLayout());
		JPanel seededOracleUsersControlPanel = new JPanel();
		seededOracleUsersControlPanel.setLayout(new BorderLayout());
		seededOracleUsersControlPanel.add(seededCheckBox, BorderLayout.WEST);
		seededOracleUsersScrollPaneAndControlPanel.add(seededOracleUsersControlPanel, BorderLayout.NORTH);
		standardOracleUsersCenterPanel.add(listedStandardOracleUsersScrollPaneAndControlPanel,BorderLayout.CENTER);
	}
	
	private JPanel previousSeededUsersPanel = null;
	
	public void loadSeededOracleUsers()
	{
		try {
			final String ebsVersion = ((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().getDbEBSVersion();
			Assert.notNull(ebsVersion, "The Oracle EBS version must be retrieved first before seeded users are loaded");
			final String ebsMajorVersion = ebsVersion.substring(0, ebsVersion.indexOf('.'));
			
			final List<String> expectedSeededUsersList;
			final String expectedSeededUsersPropertyName;
			if (ReverseMain.IS_FUSION_DB) {
				expectedSeededUsersPropertyName = Config.REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_FUSION;
				expectedSeededUsersList = Config.getReverseExpectedSeededUsersEbsVersionFusion();
			}
			else {
				Assert.isTrue("11".equals(ebsMajorVersion) || "12".equals(ebsMajorVersion), "Oracle EBS "+ebsVersion+" is not supported. Only version 11 and 12 are supported.");
				if ("11".equals(ebsMajorVersion)) {
					expectedSeededUsersPropertyName = Config.REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_11;
					expectedSeededUsersList = Config.getReverseExpectedSeededUsersEbsVersion11();
				} else {
					expectedSeededUsersPropertyName = Config.REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_12;
					expectedSeededUsersList = Config.getReverseExpectedSeededUsersEbsVersion12();
				}
			}
			
			final TreeSet<String> expectedSeededUserNamesSorted = new TreeSet<String>(expectedSeededUsersList);
			final boolean hasExpectedSeededUserNames = !expectedSeededUserNamesSorted.isEmpty();
			seededOracleUserIdToUserNameMap=DataExtractionDatabaseUtils.getOracleUserIdToUserNameMap(
					((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().
					getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
					((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().
					getEnvironmentValidationPanel().getMessageLabel(),true, null, null, false, null, null, expectedSeededUserNamesSorted, null);
			if (ReverseMain.IS_FUSION_DB ) {
				seededOracleUserIdToUserNameMap.put(-9999,"SEED_DATA_FROM_APPLICATION");
			}
			
			final Set<String> foundExpectedUserNames = new HashSet<String>(seededOracleUserIdToUserNameMap.values());
			final int numberOfNotFoundExpectedUsers = expectedSeededUserNamesSorted.size() - foundExpectedUserNames.size();
			final int numberOfPotentialSeededUsers = DataExtractionDatabaseUtils.countOracleUserIdToUserNameMap
					(((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().
					getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
					((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().
					getEnvironmentValidationPanel().getMessageLabel(), true, null, null, null, null, null, expectedSeededUserNamesSorted);
			
			final boolean willDisplayPotentialSeededUsers = 0 < numberOfPotentialSeededUsers && numberOfPotentialSeededUsers <= Config.getReverseMaxPotentialUserCount();
			
			final JPanel seededUsersPanel = new JPanel();
			seededUsersPanel.setLayout(new GridLayout(1, 2));
		
			if (previousSeededUsersPanel != null) {
				seededOracleUsersScrollPaneAndControlPanel.remove(previousSeededUsersPanel);
			}
			seededOracleUsersScrollPaneAndControlPanel.add(seededUsersPanel, BorderLayout.CENTER);
			this.previousSeededUsersPanel = seededUsersPanel;

			if (expectedSeededOracleUsersScrollPane!=null) {
				seededOracleUsersAll.remove(expectedSeededOracleUsersScrollPane);
			}
			notFoundExpectedSeededUsersCheckBoxes.clear();
			final String expectedSeededUsersTitle = "Expected Seeded Users";
			if (hasExpectedSeededUserNames) {
				expectedSeededOracleUsersCheckBoxes = GUIUtils.getCheckBoxesList(expectedSeededUserNamesSorted, ORACLE_USER_CHECKBOX_DIMENSION);
				
				final List<JCheckBox> checkboxesInDatabase = new ArrayList<JCheckBox>();
				final List<JCheckBox> checkboxesNotInDatabase = new ArrayList<JCheckBox>();
				for (final JCheckBox checkbox : expectedSeededOracleUsersCheckBoxes) {
					final boolean isInDatabase = foundExpectedUserNames.contains(checkbox.getText());
					checkbox.setEnabled(isInDatabase);
					if (isInDatabase) {
						checkboxesInDatabase.add(checkbox);
					} else {
						checkboxesNotInDatabase.add(checkbox);
						
					}
				}
				GUIUtils.setSelectedCheckBox(checkboxesInDatabase, true);
				GUIUtils.setSelectedCheckBox(checkboxesNotInDatabase, false);
				notFoundExpectedSeededUsersCheckBoxes.addAll(checkboxesNotInDatabase);

				JPanel expectedSeededUsersCheckBoxesPanel=GUIUtils.createJCheckBoxesVerticalListPanelWithHtmlTitle(expectedSeededUsersTitle, expectedSeededOracleUsersCheckBoxes);
				expectedSeededOracleUsersScrollPane=GUIUtils.createJScrollPane(expectedSeededUsersCheckBoxesPanel,DataExtractionConstants.ORACLE_USERS_SCROLLPANE_WIDTH/2,DataExtractionConstants.ORACLE_USERS_SCROLLPANE_HEIGHT);
				seededUsersPanel.add(expectedSeededOracleUsersScrollPane);					
			} else {
				JLabel messageLabel= new JLabel(
						"<html>" +
						"No expected seeded users are listed at <b>"+expectedSeededUsersPropertyName+"</b> property in "+UtilsConstants.ENGINE_PROPERTIES_FILE_NAME+" file." +
						"</html>"
						);
				messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
				JPanel messagePanel = GUIUtils.createJCheckBoxesVerticalListPanelWithHtmlTitle(expectedSeededUsersTitle, new ArrayList<JCheckBox>());
				messagePanel.add(messageLabel);
				messagePanel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				expectedSeededOracleUsersCheckBoxes = new ArrayList<JCheckBox>();
				expectedSeededOracleUsersScrollPane=null;
				seededUsersPanel.add(messagePanel);	
			}
		
			foundExpectedAndPotentialSeededUsers = new HashSet<String>();
			foundExpectedAndPotentialSeededUsers.addAll(foundExpectedUserNames);
			final JPanel seededUsersRightPanel = new JPanel();
			final JLabel potentialSeededUsersExplanationLabel = new JLabel(
					"<html><b>Potential</b> seeded users are defined as the set of users that were 'created_by' the user IDs contained in the "+UtilsConstants.ENGINE_PROPERTIES_FILE_NAME+" property " + 
							Config.REVERSE_CREATED_BY_USER_IDS_FOR_SEEDED_USER_CALCULATION + ". <b>Expected</b> seeded users are those users that additionally are listed in the " + 
							expectedSeededUsersPropertyName+" property.  A threshold of "+Config.REVERSE_MAX_POTENTIAL_SEEDED_USER_COUNT+" = "+
							Config.getReverseMaxPotentialUserCount()+" users limits the number of records displayed in this list, which is provided to you "+
							"to allow you to detect and add additional <b>expected</b> seeded users to " + expectedSeededUsersPropertyName + 
							"  Alternatively, you can select them for inclusion during this session.</html>"					
			);	
			final Border potentialSeededUsersExplanationLabelInsideBorder = BorderFactory.createEmptyBorder(2, 10, 2, 10);
			final String potentialSeededUsersTitle = "Potential Seeded Users (threshold: "+Config.getReverseMaxPotentialUserCount()+" users)";
			if (willDisplayPotentialSeededUsers) {
				final Map<Integer, String> potentialSeededUserIdToName = DataExtractionDatabaseUtils.getOracleUserIdToUserNameMap
						(((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().
								getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
								((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().
								getEnvironmentValidationPanel().getMessageLabel(), true, null, null, false, null, null, null, expectedSeededUserNamesSorted);
				final TreeSet<String> potentialSeededUserNamesSorted = new TreeSet<String>(potentialSeededUserIdToName.values());
				seededOracleUserIdToUserNameMap.putAll(potentialSeededUserIdToName);
				if (potentialSeededOracleUsersScrollPane!=null) {
					seededOracleUsersAll.remove(potentialSeededOracleUsersScrollPane);
				}
				potentialSeededOracleUsersCheckBoxes=GUIUtils.getCheckBoxesList(potentialSeededUserNamesSorted, ORACLE_USER_CHECKBOX_DIMENSION);
				GUIUtils.setSelectedCheckBox(potentialSeededOracleUsersCheckBoxes,false);
				seededUsersRightPanel.setLayout(new BoxLayout(seededUsersRightPanel, BoxLayout.Y_AXIS));
				JPanel extraSeededUsersCheckBoxesPanel=GUIUtils.createJCheckBoxesVerticalListPanelWithHtmlTitle(potentialSeededUsersTitle, potentialSeededOracleUsersCheckBoxes);			
				potentialSeededOracleUsersScrollPane=GUIUtils.createJScrollPane(extraSeededUsersCheckBoxesPanel,DataExtractionConstants.ORACLE_USERS_SCROLLPANE_WIDTH/2,DataExtractionConstants.ORACLE_USERS_SCROLLPANE_HEIGHT);
				seededUsersRightPanel.add(potentialSeededOracleUsersScrollPane);
				potentialSeededUsersExplanationLabel.setBorder(potentialSeededUsersExplanationLabelInsideBorder);
				seededUsersRightPanel.add(potentialSeededUsersExplanationLabel);
				seededUsersPanel.add(seededUsersRightPanel);
				foundExpectedAndPotentialSeededUsers.addAll(potentialSeededUserIdToName.values());	
			} else {
				seededUsersRightPanel.setLayout(new BorderLayout());
				seededUsersRightPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
				final JPanel potentialSeededUsersTopPanel = GUIUtils.createJCheckBoxesVerticalListPanelWithHtmlTitle(potentialSeededUsersTitle, new ArrayList<JCheckBox>());
				final JLabel potentialSeededUsersInformationLabel = new JLabel(
						numberOfPotentialSeededUsers == 0 ? 
								"<html>All potential seeded users are already included among the expected seeded users.</html>" : 
								"<html>"+numberOfPotentialSeededUsers+" potential seeded users are found. It exceeds the "+Config.getReverseMaxPotentialUserCount()+" users threshold. "+
									"Thus, those users are considered as standard users.</html>"
						);
				potentialSeededUsersInformationLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
				potentialSeededUsersTopPanel.add(potentialSeededUsersInformationLabel);
				seededUsersRightPanel.add(potentialSeededUsersTopPanel, BorderLayout.CENTER);
				potentialSeededUsersExplanationLabel.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK), potentialSeededUsersExplanationLabelInsideBorder));
				seededUsersRightPanel.add(potentialSeededUsersExplanationLabel, BorderLayout.SOUTH);	
				seededUsersPanel.add(seededUsersRightPanel);
			}
			seededOracleUsersAll.add(seededOracleUsersScrollPaneAndControlPanel,BorderLayout.CENTER);
			seededUsersMessagePanel.removeAll();

			final JLabel foundExpectedUsersMessageLabel = new JLabel();
			if (foundExpectedUserNames.isEmpty()) {
					GUIUtils.showWarningMessage(foundExpectedUsersMessageLabel, "No expected Oracle seeded users loaded");
				} else {
				GUIUtils.showSuccessMessage(foundExpectedUsersMessageLabel, foundExpectedUserNames.size()+" Expected Oracle seeded users loaded");
			}
			
			seededUsersMessagePanel.add(foundExpectedUsersMessageLabel);
			if (numberOfNotFoundExpectedUsers > 0) {
				final JLabel notFoundExpectedUsersMessageLabel = new JLabel();
				GUIUtils.showWarningMessage(notFoundExpectedUsersMessageLabel, numberOfNotFoundExpectedUsers+" Expected Oracle seeded users not found");
				seededUsersMessagePanel.add(notFoundExpectedUsersMessageLabel);
			}
			final JLabel potentialUsersMessageLabel = new JLabel();
			if (numberOfPotentialSeededUsers > 0) {
				GUIUtils.showWarningMessage(potentialUsersMessageLabel, numberOfPotentialSeededUsers+" Potential Oracle seeded users found");
			} else {
				GUIUtils.showSuccessMessage(potentialUsersMessageLabel, "No potential Oracle seeded users found");	
			}
			seededUsersMessagePanel.add(potentialUsersMessageLabel);
			
		}
		catch(Exception e){
			FileUtils.printStackTrace(e);
		}
	}
	
	public void loadStandardOracleUsers(final String userNameFilter, final JTextField searchField, final JButton searchButton)
	{
		Runnable r=new Runnable(){
			
			private void closePopup(final JFrame popup, final OracleUserRetrievalExecutionMonitor oracleUserRetrievalExecutionMonitor) {
				try {
					oracleUserRetrievalExecutionMonitor.cancel();
				} catch (SQLException e1) {
					throw new Error(e1);
				} finally {
					popup.setVisible(false);
					popup.dispose();
				}				
			}
			
			public void run()
			{
				try {
					if (standardOracleUsersScrollPane!=null) {
						standardOracleUsersCenterPanel.remove(standardOracleUsersScrollPane);
					}
					
					final JFrame searchResultsPopup = new JFrame();
					searchResultsPopup.setTitle("Standard Oracle Users Search Results");
					searchResultsPopup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			        
					searchResultsPopup.addWindowListener(new WindowListener() {

						@Override
						public void windowOpened(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void windowClosing(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void windowClosed(WindowEvent e) {
							SwiftGUIMain.getInstance().getRootFrame().setEnabled(true);
							SwiftGUIMain.getInstance().getRootFrame().requestFocus();						
						}

						@Override
						public void windowIconified(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void windowDeiconified(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void windowActivated(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void windowDeactivated(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
					});	
					JButton okButton = new JButton("OK");
					okButton.setEnabled(false);
					JButton cancelButton = new JButton("Cancel");
					
					okButton.setPreferredSize(cancelButton.getPreferredSize());
					JPanel buttonsPanel = new JPanel();
					buttonsPanel.add(okButton);
					buttonsPanel.add(cancelButton);
					
					JPanel queryPanel = new JPanel();
					queryPanel.add(new JLabel("<html>Search results for <span style='font-weight:bold;font-size:120%'>"+userNameFilter+"</span></html>"));
					
					JPanel progressPanel = new JPanel();
					JLabel progressLabel = new JLabel();
					progressPanel.add(progressLabel);
					
					JPanel northPanel = new JPanel();
					northPanel.setLayout(new GridLayout(2, 1));
					
					northPanel.add(queryPanel);
					northPanel.add(progressPanel);
					
					JPanel selectAllPanel = new JPanel();
					selectAllPanel.setLayout(new BorderLayout());
					final JCheckBox selectAllCheckBox = GUIUtils.getSelectionAllCheckBox(false);
					selectAllCheckBox.setEnabled(false);
					selectAllCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
					selectAllPanel.add(selectAllCheckBox, BorderLayout.WEST);
					
					JPanel southPanel = new JPanel();
					southPanel.setLayout(new GridLayout(2, 1));
					southPanel.add(selectAllPanel);
					southPanel.add(buttonsPanel);
					
					final JPanel contentPane = new JPanel();
					contentPane.setLayout(new BorderLayout());
					contentPane.add(northPanel, BorderLayout.NORTH);
					
					contentPane.add(southPanel, BorderLayout.SOUTH);						
					
					
					searchResultsPopup.setContentPane(contentPane);
					searchResultsPopup.setSize(new Dimension(450, 500));
					searchResultsPopup.requestFocus();
					searchResultsPopup.setVisible(true);
					searchResultsPopup.setLocationRelativeTo(SwiftGUIMain.getInstance().getRootFrame());
					SwiftGUIMain.getInstance().getRootFrame().setEnabled(false);
					
					final OracleUserRetrievalExecutionMonitor oracleUserRetrievalExecutionMonitor = new OracleUserRetrievalExecutionMonitor();
					
					cancelButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							closePopup(searchResultsPopup, oracleUserRetrievalExecutionMonitor);
						}
						
					});
					
			        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
				        @Override
				        public boolean dispatchKeyEvent(KeyEvent e) {
				        	if (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) {
				        		closePopup(searchResultsPopup, oracleUserRetrievalExecutionMonitor);
							}
				            return false;
				        }        	
			        });
			        
			        final Set<String> foundExpectedAndCheckedPotentialSeededUsers = new HashSet<String>(foundExpectedAndPotentialSeededUsers);
					for (final JCheckBox checkbox : potentialSeededOracleUsersCheckBoxes) {
						if (!checkbox.isSelected()) {
							foundExpectedAndCheckedPotentialSeededUsers.remove(checkbox.getText());
						}
					}
			        
					standardOracleUserIdToUserNameMap=DataExtractionDatabaseUtils.getOracleUserIdToUserNameMap(
							((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().
							getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
							progressLabel,false, userNameFilter, null, true, oracleUserRetrievalExecutionMonitor, foundExpectedAndCheckedPotentialSeededUsers, null, null);	
					
					final List<JCheckBox> standardOracleUsersCheckBoxes=GUIUtils.getCheckBoxesList(standardOracleUserIdToUserNameMap, ORACLE_USER_CHECKBOX_DIMENSION);
					JPanel checkBoxesPanel=GUIUtils.createJCheckBoxesVerticalListPanel(standardOracleUsersCheckBoxes);
					checkBoxesPanel.setBorder(BorderFactory.createEmptyBorder(10,  10,  10,  10));
					
					standardOracleUsersScrollPane=GUIUtils.createJScrollPane(checkBoxesPanel,DataExtractionConstants.ORACLE_USERS_SCROLLPANE_WIDTH,DataExtractionConstants.ORACLE_USERS_SCROLLPANE_HEIGHT);
					standardOracleUsersCenterPanel.add(listedStandardOracleUsersScrollPaneAndControlPanel,BorderLayout.CENTER);
					
					contentPane.add(standardOracleUsersScrollPane, BorderLayout.CENTER);
					
					final Map<String, Integer> standardOracleUserIdToUserNameMapInverted = GUIUtils.getInvertedMap(standardOracleUserIdToUserNameMap);
					
					okButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Map<String, Integer> usersToBeListed = new HashMap<String, Integer>();
							for (final JCheckBox checkBox : standardOracleUsersCheckBoxes) {
								if (checkBox.isSelected()) {
									usersToBeListed.put(checkBox.getText(), standardOracleUserIdToUserNameMapInverted.get(checkBox.getText()));
								}
							}
							listStandardOracleUser(usersToBeListed);
							closePopup(searchResultsPopup, oracleUserRetrievalExecutionMonitor);
						}
					});				
					

					
					selectAllCheckBox.addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent e) {
							GUIUtils.setSelectedCheckBox(standardOracleUsersCheckBoxes,selectAllCheckBox.isSelected());
						}
						
					});					
					
					
					isStandardOracleUserLoaded=true;
					

					if (standardOracleUserIdToUserNameMap.isEmpty()) 
					{
						contentPane.add(new JPanel(), BorderLayout.CENTER);
						GUIUtils.showErrorMessage(progressLabel, "There is no result matching your query");			
					} else {
						okButton.setEnabled(true);
						selectAllCheckBox.setEnabled(true);
					}
					progressLabel.setVisible(true);	
					
					
				} catch (OracleUserRetrievalExecutionMonitor.OracleUserRetrievalIsCalcelledException e) {

				} catch (Exception e) {
					if ((e instanceof SQLTimeoutException) && e.getMessage().contains("user requested cancel of current operation")) {
						//do nothing
					} else {
						FileUtils.printStackTrace(e);
						GUIUtils.popupErrorMessage("Error: "+e.getMessage());						
					}
				}
			}
		};
		Thread t=new Thread(r);
		t.start();
	}
	
	public boolean isIncludeOracleUsers() {
		return oracleUsersIncludeButton.isSelected();
	}
		
	public List<Integer> getSelectedOracleUserIds() {
		//a user can be of both a potential seeded user and a standard user
		Set<Integer> userIdsNotUnique = new HashSet<Integer>();
		userIdsNotUnique.addAll(getSelectedStandardOracleUserIds());
		userIdsNotUnique.addAll(getSelectedSeededOracleUserIds());
		List<Integer> toReturn=new ArrayList<Integer>(userIdsNotUnique);
		return toReturn;
	}

	public List<Integer> getSelectedSeededOracleUserIds() {
		List<Integer> output = new ArrayList<Integer>();
		output.addAll(GUIUtils.getSelectedIds(expectedSeededOracleUsersCheckBoxes, seededOracleUserIdToUserNameMap));
		output.addAll(GUIUtils.getSelectedIds(potentialSeededOracleUsersCheckBoxes, seededOracleUserIdToUserNameMap));
		return output;
	}
	
	public List<Integer> getSelectedStandardOracleUserIds() {
		Component components[] = listedStandardOracleUsersPanel.getComponents();
		List<JCheckBox> checkboxes = Arrays.asList(Arrays.copyOf(components, components.length, JCheckBox[].class));
		List<Integer> output = GUIUtils.getSelectedIds(checkboxes, GUIUtils.getInvertedMap(listedStandardOracleUsers));
		return output;
	}
	
	public Map<Integer,String> getAllOracleUserIdToNameSynchronizedMap() {
		Map<Integer,String> map=new HashMap<Integer,String>();
		if (seededOracleUserIdToUserNameMap!=null) {
			map.putAll(seededOracleUserIdToUserNameMap);
		}
		if (standardOracleUserIdToUserNameMap!=null) {
			map.putAll(standardOracleUserIdToUserNameMap);
		}
		return Collections.synchronizedMap(map);
	}
	
	public void restoreOracleUsers(List<Integer> selectedOracleUserIds, final OracleUserRetrievalExecutionMonitor cancelFlag, final JLabel progressLabel) throws Exception
	{
		Map<Integer, String> selectedOracleUserIdToNameMapFiltered=DataExtractionDatabaseUtils.getOracleUserIdToUserNameMap(
				((ReverseMain)panel.getSwiftGUIMain()).getDataExtractionEBSPropertiesValidationPanel().
				getEnvironmentValidationPanel().getEnvironmentPropertiesMap(),
				progressLabel,false, null, new HashSet<Integer>(selectedOracleUserIds), true, cancelFlag, foundExpectedAndPotentialSeededUsers, null, null);
		standardOracleUsersCenterPanel.add(listedStandardOracleUsersScrollPaneAndControlPanel,BorderLayout.CENTER);
		listedStandardOracleUsers.clear();
		for (Map.Entry<Integer, String> entry : selectedOracleUserIdToNameMapFiltered.entrySet()) {
			listedStandardOracleUsers.put(entry.getValue(), entry.getKey());
		}
		refreshListedStandardOracleUsersPanel();		
		
		Map<Integer, String> selectedSeededOracleUserIdToNameMap = new HashMap<Integer, String>();
		for (final Integer userId : selectedOracleUserIds) {
			if (seededOracleUserIdToUserNameMap.containsKey(userId)) {
				selectedSeededOracleUserIdToNameMap.put(userId, seededOracleUserIdToUserNameMap.get(userId));
			}
		}
		GUIUtils.setSelectedCheckBoxes(expectedSeededOracleUsersCheckBoxes,selectedSeededOracleUserIdToNameMap);
		GUIUtils.setSelectedCheckBoxes(potentialSeededOracleUsersCheckBoxes,selectedSeededOracleUserIdToNameMap);
	}

	public Map<Integer, String> getSeededOracleUserIdToUserNameMap() {
		return seededOracleUserIdToUserNameMap;
	}

	public Map<Integer, String> getStandardOracleUserIdToUserNameMap() {
		return standardOracleUserIdToUserNameMap;
	}
	
	public void setOracleUsersInclude(boolean isSelected) {
		oracleUsersIncludeButton.setSelected(isSelected);
		oracleUsersExcludeButton.setSelected(!isSelected);
	}
		
	public List<Component> getAllComponents() {
		List<Component> list=new ArrayList<Component>();
		list.add(oracleUsersIncludeButton);
		list.add(oracleUsersExcludeButton);
		list.add(seededCheckBox);
		if (expectedSeededOracleUsersCheckBoxes!=null) {
			list.addAll(expectedSeededOracleUsersCheckBoxes);
		}		
		if (potentialSeededOracleUsersCheckBoxes!=null) {
			list.addAll(potentialSeededOracleUsersCheckBoxes);
		}			
		list.addAll(Arrays.asList(listedStandardOracleUsersPanel.getComponents()));
		list.add(standardOracleUsersSelectUnselectCheckbox);
		list.add(standardOracleUsersRemoveSelectedButton);
		list.add(standardOracleUsersSearchField);
		list.add(standardOracleUsersSearchButton);		
		
		return list;
	}
	
	public void lockAll() {
		GUIUtils.setEnabledOnComponents(getAllComponents(),false);
	}

	public void unlockAll() {
		List<Component> toUnlock = getAllComponents();
		toUnlock.removeAll(this.notFoundExpectedSeededUsersCheckBoxes);
		GUIUtils.setEnabledOnComponents(toUnlock,true);
	}

	public boolean isStandardOracleUserLoaded() {
		return isStandardOracleUserLoaded;
	}
	
	public void listStandardOracleUser(final Map<String, Integer> users) {
		listedStandardOracleUsers.putAll(users);
		refreshListedStandardOracleUsersPanel();
	}
	
	public void refreshListedStandardOracleUsersPanel() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				final Set<String> untickedCheckboxTexts = new HashSet<String>();
				for (final Component component : listedStandardOracleUsersPanel.getComponents()) {
					if (!((JCheckBox) component).isSelected()) {
						untickedCheckboxTexts.add(((JCheckBox) component).getText());
					}
				}
				
				listedStandardOracleUsersPanel.removeAll();
				List<JCheckBox> checkBoxes = GUIUtils.getCheckBoxesList(GUIUtils.getInvertedMap(listedStandardOracleUsers), ORACLE_USER_CHECKBOX_DIMENSION);
				for (JCheckBox checkBox : checkBoxes) {
					checkBox.setSelected(!untickedCheckboxTexts.contains(checkBox.getText()));
					listedStandardOracleUsersPanel.add(checkBox);
				}
				
				//somehow without these, listedStandardOracleUsersPanel doesn't repaint, even after invoking repaint
				listedStandardOracleUsersPanel.setVisible(false);
				listedStandardOracleUsersPanel.setVisible(true);
				
			}			
		});

	}
	
}