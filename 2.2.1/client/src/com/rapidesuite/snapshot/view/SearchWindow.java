package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;

public abstract class SearchWindow  {

	protected JDialog dialog;
	private JPanel mainPanel;
	protected JLabel statusLabel;
	private JButton searchButton;
	private JButton applyButton;
	private JButton cancelButton;
	private JTextField searchInputField;
	
	private Map<String,Object> searchKeyToObjectMap;
	private DefaultListModel<String> searchResultsListModel;
	private JList<String> searchResultsList;
	
	private DefaultListModel<String> selectedResultsListModel;
	private JList<String> selectedResultsList;
	private Map<String,Object> selectedResultsKeyToObjectMap;
	
	private String defaultValue;
	private JLabel resultLabel;
	private JLabel resultValueLabel;
	private JLabel selectedValuesLabel;
	private JLabel selectedValuesValueLabel;
	private JButton addButton;
	private JButton removeButton;
	protected JLabel warningLabel;
		
	public final static String WILDCARD_KEYWORD="%";
	public final static String NO_RESULTS_KEYWORD="No results found!";
	
	public SearchWindow(JFrame rootFrame,String title,String defaultValue,Map<String,Object> selectedResultsKeyToObjectMap) {
		int width=790;
		int height=450;
		this.defaultValue=defaultValue;
		this.selectedResultsKeyToObjectMap=selectedResultsKeyToObjectMap;
		mainPanel=new JPanel();
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#dbdcdf"));
		createComponents();
		dialog=UIUtils.displayOperationInProgressComplexModalWindow(rootFrame,title,width,height,mainPanel,null,true,SnapshotMain.getSharedApplicationIconPath());
	}

	private void createComponents() {
		int widthEmptyBordersPanels=20;
		
		JPanel topPanel=new JPanel();
		topPanel.setOpaque(true);
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBackground(Color.decode("#4b4f4e"));
		mainPanel.add(topPanel);
		
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		//centerPanel.setBackground(Color.red);
		mainPanel.add(centerPanel);
		
		JPanel centerLeftPanel=new JPanel();
		centerLeftPanel.setOpaque(false);
		centerLeftPanel.setLayout(new BoxLayout(centerLeftPanel, BoxLayout.Y_AXIS));
		//centerLeftPanel.setBackground(Color.blue);
		centerPanel.add(centerLeftPanel);
		
		JPanel centerMedianPanel=new JPanel();
		centerMedianPanel.setOpaque(false);
		centerMedianPanel.setLayout(new BoxLayout(centerMedianPanel, BoxLayout.Y_AXIS));
		//centerMedianPanel.setBackground(Color.green);
		centerPanel.add(centerMedianPanel);
		
		JPanel centerRightPanel=new JPanel();
		centerRightPanel.setOpaque(false);
		centerRightPanel.setLayout(new BoxLayout(centerRightPanel, BoxLayout.Y_AXIS));
		//centerRightPanel.setBackground(Color.yellow);
		centerPanel.add(centerRightPanel);
		
		JPanel bottomPanel=new JPanel();
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		//bottomPanel.setBackground(Color.pink);
		mainPanel.add(bottomPanel);
				
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(tempPanel);
		
		JLabel searchLabel=new JLabel("Search Prompt (Use % for wildcards):");
		searchLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		searchLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(searchLabel,12);
		searchLabel.setOpaque(false);
		tempPanel.add(searchLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		searchInputField=new JTextField();
		if (defaultValue!=null) {
			searchInputField.setText(defaultValue);
		}
		UIUtils.setDimension(searchInputField,250, 25);
		InjectUtils.assignArialPlainFont(searchInputField,12);
		searchInputField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				try{
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						JTextField textField = (JTextField) e.getSource();
						String text = textField.getText();
						processActionSearch(text);
					}
				}
				catch(Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+ex.getMessage());
				}
			}
		});
		tempPanel.add(searchInputField);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		ImageIcon ii=null;
		URL iconURL =null;		
		iconURL = this.getClass().getResource("/images/snapshot/button_search.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		searchButton = new JButton();
		searchButton.setIcon(ii);
		searchButton.setBorderPainted(false);
		searchButton.setContentAreaFilled(false);
		searchButton.setFocusPainted(false);
		searchButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_search_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		searchButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(searchButton);
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				processActionSearch(searchInputField.getText());
			}

		});
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(tempPanel);
		statusLabel=new JLabel();
		statusLabel.setForeground(Color.black);
		statusLabel.setOpaque(true);	
		InjectUtils.assignArialPlainFont(statusLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(statusLabel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(tempPanel);
		warningLabel=new JLabel();
		warningLabel.setForeground(Color.black);
		warningLabel.setOpaque(true);	
		warningLabel.setBackground(Color.ORANGE);
		InjectUtils.assignArialPlainFont(warningLabel,InjectMain.FONT_SIZE_NORMAL);
		tempPanel.add(warningLabel);
		
		
	
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerLeftPanel.add(tempPanel);
		resultLabel=new JLabel("Search Results:");
		InjectUtils.assignArialPlainFont(resultLabel,12);
		tempPanel.add(resultLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(2, 10)));
		resultValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(resultValueLabel,12);
		tempPanel.add(resultValueLabel);
		searchResultsListModel = new DefaultListModel<String>(); 
		searchResultsList = new JList<String>(searchResultsListModel);
		searchResultsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// This is if we don't allow any selections.
		/*
		resultList.setSelectionModel(new DefaultListSelectionModel() {

		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        super.setSelectionInterval(-1, -1);
		    }
		    
		});
		*/
		//resultList.setVisibleRowCount(20);
		
		searchResultsList.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        if (evt.getClickCount() == 2) {
		        	processActionAdd();
		        }
		    }
		});
		
		JScrollPane searchResultsScrollPane = new JScrollPane(searchResultsList);
		Dimension d = searchResultsList.getPreferredSize();
		d.width = 280;
		d.height =200;
		searchResultsScrollPane.setPreferredSize(d);
		centerLeftPanel.add(searchResultsScrollPane);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_add.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		addButton = new JButton();
		addButton.setIcon(ii);
		addButton.setBorderPainted(false);
		addButton.setContentAreaFilled(false);
		addButton.setFocusPainted(false);
		addButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_add_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		addButton.setRolloverIcon(new RolloverIcon(ii));
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionAdd();
			}

		});
		centerMedianPanel.add(addButton);
		centerMedianPanel.add(Box.createRigidArea(new Dimension(15, 10)));
	
		iconURL = this.getClass().getResource("/images/snapshot/button_remove.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		removeButton = new JButton();
		removeButton.setIcon(ii);
		removeButton.setBorderPainted(false);
		removeButton.setContentAreaFilled(false);
		removeButton.setFocusPainted(false);
		removeButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_remove_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		removeButton.setRolloverIcon(new RolloverIcon(ii));
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionRemove();
			}

		});
		centerMedianPanel.add(removeButton);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerRightPanel.add(tempPanel);
		selectedValuesLabel=new JLabel("Selected Values:");
		InjectUtils.assignArialPlainFont(selectedValuesLabel,12);
		tempPanel.add(selectedValuesLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(2, 10)));
		selectedValuesValueLabel=new JLabel();
		selectedValuesValueLabel.setText(" ( "+selectedResultsKeyToObjectMap.size()+" values )");
		InjectUtils.assignArialPlainFont(selectedValuesValueLabel,12);
		tempPanel.add(selectedValuesValueLabel);
		selectedResultsListModel = new DefaultListModel<String>(); 
		selectedResultsList = new JList<String>(selectedResultsListModel);
		selectedResultsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		Iterator<String> iterator=selectedResultsKeyToObjectMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			selectedResultsListModel.addElement(key);
		}
		selectedResultsList.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        if (evt.getClickCount() == 2) {
		        	processActionRemove();
		        }
		    }
		});
		JScrollPane selectedResultsScrollPane = new JScrollPane(selectedResultsList);
		d = selectedResultsList.getPreferredSize();
		d.width = 280;
		d.height =200;
		selectedResultsScrollPane.setPreferredSize(d);
		centerRightPanel.add(selectedResultsScrollPane);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		bottomPanel.add(tempPanel);
		iconURL = this.getClass().getResource("/images/snapshot/button_apply.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		applyButton = new JButton();
		applyButton.setIcon(ii);
		applyButton.setBorderPainted(false);
		applyButton.setContentAreaFilled(false);
		applyButton.setFocusPainted(false);
		applyButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_apply_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		applyButton.setRolloverIcon(new RolloverIcon(ii));
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionApply();
			}

		});
		tempPanel.add(applyButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));

		iconURL = this.getClass().getResource("/images/snapshot/button_cancel.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		cancelButton = new JButton();
		cancelButton.setIcon(ii);
		cancelButton.setBorderPainted(false);
		cancelButton.setContentAreaFilled(false);
		cancelButton.setFocusPainted(false);
		cancelButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_cancel_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		cancelButton.setRolloverIcon(new RolloverIcon(ii));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionCancel();
			}

		});
		tempPanel.add(cancelButton);
	}

	protected void processActionApply() {
		apply(selectedResultsKeyToObjectMap);
	}

	public abstract void apply(Map<String, Object> selectedResultsKeyToObjectMap);

	protected void processActionCancel() {
		dialog.dispose();
	}

	protected void processActionRemove() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int[] selectedIx = selectedResultsList.getSelectedIndices();
				for (int i = 0; i < selectedIx.length; i++) {
					String key =(String) selectedResultsList.getModel().getElementAt(selectedIx[i]);
					selectedResultsKeyToObjectMap.remove(key);
				}
			    
				selectedResultsListModel = new DefaultListModel<String>();
				selectedResultsList.setModel(selectedResultsListModel);
				Iterator<String> iterator=selectedResultsKeyToObjectMap.keySet().iterator();
				while (iterator.hasNext()) {
					String key=iterator.next();
					selectedResultsListModel.addElement(key);
				}
				selectedValuesValueLabel.setText(" ( "+selectedResultsKeyToObjectMap.size()+" values )");
			}
		});
	}

	protected void processActionAdd() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int[] selectedIx = searchResultsList.getSelectedIndices();
				for (int i = 0; i < selectedIx.length; i++) {
					String key =(String) searchResultsList.getModel().getElementAt(selectedIx[i]);
					if (key.equalsIgnoreCase(NO_RESULTS_KEYWORD)) {
						continue;
					}
					Object value=searchKeyToObjectMap.get(key);
					selectedResultsKeyToObjectMap.put(key, value);
				}
			    
				selectedResultsListModel = new DefaultListModel<String>();
				selectedResultsList.setModel(selectedResultsListModel);
				Iterator<String> iterator=selectedResultsKeyToObjectMap.keySet().iterator();
				while (iterator.hasNext()) {
					String key=iterator.next();
					selectedResultsListModel.addElement(key);
				}
				selectedValuesValueLabel.setText(" ( "+selectedResultsKeyToObjectMap.size()+" values )");
			}
		});
	}

	protected void processActionSearch(final String text) {
		statusLabel.setBackground(Color.decode("#08bc08"));
		statusLabel.setText("<html><b>Processing...</b>");
		warningLabel.setText("");
		searchButton.setEnabled(false);
		applyButton.setEnabled(false);
		cancelButton.setEnabled(false);
		searchInputField.setEnabled(false);
		Thread thread = new Thread(){
			public void run(){
				runSearch(text);
			}
		};

		thread.start();
	}
	
	protected void runSearch(String text) {
		try{
			searchKeyToObjectMap=search(text);
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					searchResultsListModel = new DefaultListModel<String>();
					searchResultsList.setModel(searchResultsListModel);
					if (searchKeyToObjectMap.isEmpty()) {
						searchResultsListModel.addElement(NO_RESULTS_KEYWORD);
					}
					else {
						Iterator<String> iterator=searchKeyToObjectMap.keySet().iterator();
						while (iterator.hasNext()) {
							String key=iterator.next();
							searchResultsListModel.addElement(key);
						}
					}
					resultValueLabel.setText(" ( "+searchKeyToObjectMap.size()+" values )");
					statusLabel.setText("");
				}
			});
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			statusLabel.setBackground(Color.decode("#ee3630"));
			statusLabel.setText("<html><b>"+e.getMessage()+"</b>");
		}
		finally{
			searchButton.setEnabled(true);
			applyButton.setEnabled(true);
			cancelButton.setEnabled(true);
			searchInputField.setEnabled(true);
		}
	}
	
	public abstract Map<String,Object> search(String inputValue) throws Exception;

}
