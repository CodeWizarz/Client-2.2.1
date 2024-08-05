package com.rapidesuite.inject.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.fusionData0000.FusionDataType;
import com.rapidesuite.core.fusionData0000.LType;
import com.rapidesuite.core.fusionData0000.MType;
import com.rapidesuite.core.fusionScripts0000.FusionScriptType;
import com.rapidesuite.core.fusionScripts0000.ScriptType;
import com.rapidesuite.core.injectionPackageInformation.InjectionPackageInformationDocument;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class ApplicationInfoPanel extends JPanel {

	private InjectMain injectMain;
	private JTable table;
	private File injectionPackage;
	private JFileChooser injectionPackageFileChooser;
	private InjectionPackageInformationDocument injectionPackageInformationDocument;
	private JLabel injectionPackageFileLabel;
	private JLabel injectionEnvironmentFileLabel;
	private JButton selectPackageButton;
	private String implementationProject;
	private Map<String,String> applicationKeysToNamesMap;
	
	private String COLUMN_HEADING_APPLICATION_KEY="Application key";
	private String COLUMN_HEADING_APPLICATION_NAME="Application name";
	private String COLUMN_HEADING_REQUIRED="Is Required";
	private String COLUMN_HEADING_TYPE="Type";
	private String COLUMN_HEADING_URL="Access path";
	private String COLUMN_HEADING_NAME="Username";
	private String COLUMN_HEADING_PASSWORD="Password";

	private int COLUMN_HEADING_APPLICATION_NAME_WIDTH=240;
	private int COLUMN_HEADING_REQUIRED_WIDTH=70;
	private int COLUMN_HEADING_TYPE_WIDTH=80;
	private int COLUMN_HEADING_URL_WIDTH=400;
	private int COLUMN_HEADING_NAME_WIDTH=150;
	private int COLUMN_HEADING_PASSWORD_WIDTH=150;
	private boolean isDefaultLoaded;
	private boolean hasDefaultToLoad;
	private JFileChooser ieFileChooser;

	public static final String DEFAULT_INJECTION_PACKAGE_FILE="DEFAULT_INJECTION_PACKAGE_FILE";
	public static final String DEFAULT_INJECTION_ENVIRONMENT_FILE="DEFAULT_INJECTION_ENVIRONMENT_FILE";
	public static final String SEPARATOR="##RES##";
	public static final String DATABASE_APPLICATION_KEY="APPLICATION_DATABASE";

	public ApplicationInfoPanel(final InjectMain injectMain) throws Exception {
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		this.injectMain=injectMain;

		JPanel mainPanel=new JPanel();
		this.add(mainPanel,BorderLayout.CENTER);
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#343836") );
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		JPanel applicationNamesPanel=new JPanel();
		applicationNamesPanel.setOpaque(true);
		applicationNamesPanel.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
		applicationNamesPanel.setLayout(new BoxLayout(applicationNamesPanel, BoxLayout.Y_AXIS));
		applicationNamesPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		/*
		 * MENU BARS
		 */

		final JTabbedPane jtp = new JTabbedPane();
		jtp.setOpaque(false);
		String tabName="";
		int tabIndex=0;
		int tabHeight=50;

		InjectUtils.addMenuTab(jtp,injectMain,CoreConstants.SHORT_APPLICATION_NAME.inject.toString(),tabIndex,tabHeight);
		tabIndex++;

		tabName="APPLICATION NAMES";
		InjectUtils.addTab(jtp,tabName,applicationNamesPanel,tabIndex,150,tabHeight,true);
		tabIndex++;

		int tabIndexGreaterThanNotDisplayWhiteBar=1;
		jtp.setUI(new TabbedPaneUI(tabIndexGreaterThanNotDisplayWhiteBar));
		jtp.addChangeListener(new TabChangeListener(jtp));
		jtp.setSelectedIndex(1);
		jtp.setEnabledAt(0, false);
		mainPanel.add(jtp);

		/*
		 * NORTH PANEL
		 */

		JPanel tempPanelButton=InjectUtils.getYPanel(Component.CENTER_ALIGNMENT);
		tempPanelButton.setOpaque(false);
		tempPanelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		// we need to set the layout to null otherwise the panel will resize the button making the image not fit the button.
		//tempPanelButton.setLayout(new BoxLayout(tempPanelButton, BoxLayout.Y_AXIS));//new FlowLayout(FlowLayout.LEFT));
		tempPanelButton.setLayout(new BorderLayout());//new FlowLayout(FlowLayout.LEFT));
		//tempPanelButton.setBackground(Color.red);
		tempPanelButton.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
		applicationNamesPanel.add(tempPanelButton);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/inject/button_select_package.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		selectPackageButton = new JButton();
		selectPackageButton.setIcon(ii);
		selectPackageButton.setBorderPainted(false);
		selectPackageButton.setContentAreaFilled(false);
		selectPackageButton.setFocusPainted(false);
		selectPackageButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_select_package_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		selectPackageButton.setRolloverIcon(new RolloverIcon(ii));
		selectPackageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				selectInjectionPackageFile();
			}
		}
				);
		tempPanelButton.add(selectPackageButton,BorderLayout.CENTER);

		JPanel tempPanel2=new JPanel();
		tempPanelButton.add(tempPanel2,BorderLayout.SOUTH);
		tempPanel2.setOpaque(false);
		//tempPanel2.setBackground(Color.BLUE);
		tempPanel2.setLayout(new BoxLayout(tempPanel2, BoxLayout.X_AXIS));
		
		injectionPackageFileLabel=new JLabel("No package selected.");
		injectionPackageFileLabel.setBorder(BorderFactory.createEmptyBorder(0, 120, 0, 0));
		tempPanel2.add(injectionPackageFileLabel);
		tempPanel2.add(Box.createHorizontalGlue());
		injectionPackageFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		injectionPackageFileLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		injectionPackageFileLabel.setForeground(Color.white);
		InjectUtils.assignArialBoldFont(injectionPackageFileLabel,ExecutionPanel.FONT_SIZE_NORMAL);	
		injectionPackageFileLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					if (injectionPackageInformationDocument != null) {
						InjectionPackageDialog inforDialog = new InjectionPackageDialog(injectionPackageInformationDocument);
						inforDialog.setLocationRelativeTo(ApplicationInfoPanel.this);
						inforDialog.setVisible(true);
					}
				} catch (Exception e) {
					FileUtils.printStackTrace(e);
					e.printStackTrace();
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				if (injectionPackageInformationDocument != null) {
					GUIUtils.changeCursorOnGlassPane((Container) injectMain.getRootFrame().getRootPane().getGlassPane(), Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
		    }
			@Override
			public void mouseExited(MouseEvent arg0) {
				if (injectionPackageInformationDocument != null) {
					GUIUtils.changeCursorOnGlassPane((Container) injectMain.getRootFrame().getRootPane().getGlassPane(), Cursor.getDefaultCursor());
				}
			}
		});

		injectionPackageFileChooser= new JFileChooser();
		FileFilter imageFilter = new FileNameExtensionFilter("Injection Package files","ip");
		injectionPackageFileChooser.setFileFilter(imageFilter);
		injectionPackageFileChooser.setAcceptAllFileFilterUsed(false);

		/*
		 * GRID
		 */

		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_APPLICATION_KEY);
		columnNames.add(COLUMN_HEADING_APPLICATION_NAME);
		columnNames.add(COLUMN_HEADING_REQUIRED);
		columnNames.add(COLUMN_HEADING_TYPE);
		columnNames.add(COLUMN_HEADING_URL);
		columnNames.add(COLUMN_HEADING_NAME);
		columnNames.add(COLUMN_HEADING_PASSWORD);

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Class getColumnClass(int column) {
				return String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				switch (column) {
				case 0:
					return false;
				case 1:
					return false;
				case 2:
					return false;
				case 3:
					return false;
				default:
					return true;
				}
			}

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				c.setForeground(Color.decode("#2F3436"));

				int colIndexApplicationName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_NAME);
				int colIndexRequired=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REQUIRED);
				int colIndexType=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TYPE);
				if (vColIndex==colIndexApplicationName || vColIndex==colIndexRequired || vColIndex==colIndexType){
					c.setBackground(Color.decode("#DBDBDB"));
				}
				else {
					String isRequired=table.getModel().getValueAt(rowIndex, colIndexRequired).toString();
					if (isRequired.equals("Yes")) {
						int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);
						int colIndexName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
						int colIndexPassword=table.getColumnModel().getColumnIndex(COLUMN_HEADING_PASSWORD);
						if (vColIndex==colIndexURL ||vColIndex==colIndexName ||vColIndex==colIndexPassword ) {
							String value=table.getModel().getValueAt(rowIndex, vColIndex).toString();
							if (value.isEmpty()) {
								if (hasDefaultToLoad && !isDefaultLoaded) {
									c.setBackground(Color.white);
								}
								else {
									c.setBackground(ScriptsGrid.redColor);
								}
							}
							else {
								c.setBackground(Color.white);
							}
						}						
					}
					else {
						c.setBackground(Color.white);
					}
				}
								
				return c;
			}
			
			 public String getToolTipText(MouseEvent e) {
	                String tip = null;
	                java.awt.Point p = e.getPoint();
	                int rowIndex = rowAtPoint(p);
	                int colIndex = columnAtPoint(p);
	                int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
	                int colIndexApplicationName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_NAME);
	                int colIndexType=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TYPE);
	                String value=table.getModel().getValueAt(rowIndex, colIndexApplicationKey).toString();
	                int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);

	                if (  ( colIndex==colIndexURL || colIndex==colIndexApplicationKey || colIndex==colIndexApplicationName || colIndex==colIndexType)
	                	) {
	                	if (value.equalsIgnoreCase(DATABASE_APPLICATION_KEY)) {
	                		tip = "<html><p width=\"500\">Enter the JDBC string to connect to the Database."+
	                				"<br>Format:  jdbc:oracle:thin:@HOST:PORT:SID"+
	                				"<br>Example: jdbc:oracle:thin:@fusion01.rapidesuite.com:1521:fusion01"+
	                				"</p></html>";
	                	}
	                	else {
	                		tip = "<html><p width=\"500\">Enter the URL to your application domain."+
	    	                	"<br>Format:  http://hostname:port/path"+
	    	                	"<br>Example: https://fusion01.rapidesuite.com:18614/homePage/"+
	    	                	"</p></html>";
	                	}
	                }

	                return tip;
	            }
		};

		final JTextField textField=new JTextField();
		DefaultCellEditor singleclick = new DefaultCellEditor(textField);
		singleclick.setClickCountToStart(1);
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.setDefaultEditor(table.getColumnClass(i), singleclick);
		} 

		JPasswordField password = new JPasswordField();
		DefaultCellEditor editor = new DefaultCellEditor(password);
		editor.setClickCountToStart(1);
		table.getColumn(COLUMN_HEADING_PASSWORD).setCellEditor(editor);
		PasswordCellRenderer passwordCellRenderer=new PasswordCellRenderer();
		passwordCellRenderer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		passwordCellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		table.getColumn(COLUMN_HEADING_PASSWORD).setCellRenderer(passwordCellRenderer);

		TableColumn columnApplicationKey = table.getColumn(COLUMN_HEADING_APPLICATION_KEY);
		columnApplicationKey.setMinWidth(0);
		columnApplicationKey.setMaxWidth(0);
		columnApplicationKey.setPreferredWidth(0);

		TableColumn columnApplicationName = table.getColumn(COLUMN_HEADING_APPLICATION_NAME);
		columnApplicationName.setMinWidth(COLUMN_HEADING_APPLICATION_NAME_WIDTH);
		columnApplicationName.setPreferredWidth(COLUMN_HEADING_APPLICATION_NAME_WIDTH);
		columnApplicationName.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnRequired = table.getColumn(COLUMN_HEADING_REQUIRED);
		columnRequired.setMinWidth(COLUMN_HEADING_REQUIRED_WIDTH);
		columnRequired.setPreferredWidth(COLUMN_HEADING_REQUIRED_WIDTH);
		columnRequired.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnType = table.getColumn(COLUMN_HEADING_TYPE);
		columnType.setMinWidth(COLUMN_HEADING_TYPE_WIDTH);
		columnType.setPreferredWidth(COLUMN_HEADING_TYPE_WIDTH);
		columnType.setHeaderRenderer(new TableHeaderRenderer());
		
		TableColumn columnURL = table.getColumn(COLUMN_HEADING_URL);
		columnURL.setMinWidth(COLUMN_HEADING_URL_WIDTH);
		columnURL.setPreferredWidth(COLUMN_HEADING_URL_WIDTH);
		columnURL.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnHeadingName = table.getColumn(COLUMN_HEADING_NAME);
		columnHeadingName.setMinWidth(COLUMN_HEADING_NAME_WIDTH);
		columnHeadingName.setPreferredWidth(COLUMN_HEADING_NAME_WIDTH);
		columnHeadingName.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnPwd = table.getColumn(COLUMN_HEADING_PASSWORD);
		columnPwd.setMinWidth(COLUMN_HEADING_PASSWORD_WIDTH);
		columnPwd.setPreferredWidth(COLUMN_HEADING_PASSWORD_WIDTH);
		columnPwd.setHeaderRenderer(new TableHeaderRenderer());

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		columnApplicationName.setCellRenderer(leftRenderer);
		columnRequired.setCellRenderer(centerRenderer);
		columnType.setCellRenderer(centerRenderer);
		columnURL.setCellRenderer(leftRenderer);
		columnHeadingName.setCellRenderer(leftRenderer);

		table.setTableHeader(new JTableHeader(table.getColumnModel()) {
			@Override public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 30;
				return d;
			}
		});
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setRowHeight(22);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_NORMAL));
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(50, 116, 5, 116));
		tempPanel.setLayout(new BorderLayout());
		tempPanel.setOpaque(true);
		tempPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		tempPanel.add(scrollPane,BorderLayout.CENTER);
		applicationNamesPanel.add(tempPanel);
		
		/*
		 *  SOUTH PANEL
		 */
		JPanel tempPanelSouth=new JPanel();
		applicationNamesPanel.add(tempPanelSouth);
		tempPanelSouth.setOpaque(true);
		tempPanelSouth.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tempPanelSouth.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		tempPanelSouth.setLayout(new BoxLayout(tempPanelSouth, BoxLayout.Y_AXIS));
		
		injectionEnvironmentFileLabel=new JLabel("");
		InjectUtils.assignArialPlainFont(injectionEnvironmentFileLabel,ExecutionPanel.FONT_SIZE_SMALL);
		tempPanelSouth.add(injectionEnvironmentFileLabel);

		tempPanel=new JPanel();
		tempPanelSouth.add(tempPanel);
		tempPanel.setOpaque(true);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 18, 0));
		tempPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.add(Box.createHorizontalGlue());
		
		
		iconURL = this.getClass().getResource("/images/inject/button_load.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton loadButton = new JButton();
		loadButton.setIcon(ii);
		loadButton.setBorderPainted(false);
		loadButton.setContentAreaFilled(false);
		loadButton.setFocusPainted(false);
		loadButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_load_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		loadButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(loadButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try {
					selectApplicationsEnvironmentFile();
				} catch (Exception e1) {
					FileUtils.printStackTrace(e1);
					GUIUtils.popupErrorMessage(e1.getMessage());
				}
			}
		});
		
		iconURL = this.getClass().getResource("/images/inject/button_save.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton saveButton = new JButton();
		saveButton.setIcon(ii);
		saveButton.setBorderPainted(false);
		saveButton.setContentAreaFilled(false);
		saveButton.setFocusPainted(false);
		saveButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_save_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(saveButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try {
					saveApplicationsEnvironmentFile();
				} catch (Exception e1) {
					FileUtils.printStackTrace(e1);
					GUIUtils.popupErrorMessage(e1.getMessage());
				}
			}
		});
		
		iconURL = this.getClass().getResource("/images/inject/button_next.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton nextButton = new JButton();
		nextButton.setIcon(ii);
		nextButton.setBorderPainted(false);
		nextButton.setContentAreaFilled(false);
		nextButton.setFocusPainted(false);
		nextButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_next_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		nextButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(nextButton);
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				nextPanel();
			}
		});
		tempPanel.add(Box.createHorizontalGlue());

		ieFileChooser= new JFileChooser();
		setDefaultFileChooserProperties(ieFileChooser,DEFAULT_INJECTION_ENVIRONMENT_FILE);
		imageFilter = new FileNameExtensionFilter("Injection Environment files","ie");
		ieFileChooser.setFileFilter(imageFilter);
		ieFileChooser.setAcceptAllFileFilterUsed(false);
		
		loadSeededApplicationsNames();
		
		Preferences pref = Preferences.userRoot();
		final String value = InjectUtils.decrypt(pref.get(DEFAULT_INJECTION_PACKAGE_FILE,""));
		final String automatedInjectionPackageFilePath=injectMain.getAutomatedInjectionPackageFilePath();
		if ( (value!=null && !value.isEmpty()) ||
				automatedInjectionPackageFilePath!=null
		) {
			new Thread( new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
						File file=new File(value);
						if (automatedInjectionPackageFilePath!=null) {
							file=new File(automatedInjectionPackageFilePath);
						}
						startInjectionPackageLoadDialog(file);
					} catch (Exception e) {
						FileUtils.printStackTrace(e);
						GUIUtils.popupErrorMessage(e.getMessage());
					}
				}
			}).start();
		}
		String valueTemp = InjectUtils.decrypt(pref.get(DEFAULT_INJECTION_ENVIRONMENT_FILE,""));
		hasDefaultToLoad=false;
		if (valueTemp!=null && !valueTemp.isEmpty()) {
			final File file=new File(valueTemp);
			isDefaultLoaded=false;
			hasDefaultToLoad=true;
			new Thread( new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						loadApplicationsEnvironmentFile(file);
					} catch (Exception e) {
						FileUtils.printStackTrace(e);
						GUIUtils.popupErrorMessage(e.getMessage());
					}
					finally {
						isDefaultLoaded=true;
					}
				}
			}).start();
		}

		new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					if (injectionPackageFileChooser.getSelectedFile()!=null && ieFileChooser.getSelectedFile()!=null) {
						nextPanel();
					}
				} catch (Exception e) {
					FileUtils.printStackTrace(e);
					GUIUtils.popupErrorMessage(e.getMessage());
				}
			}
		}).start();
	}

	protected void saveApplicationsEnvironmentFile() throws Exception {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}

		JFileChooser fileChooser= new JFileChooser();

		Preferences pref = Preferences.userRoot();
		setDefaultFileChooserProperties(fileChooser,DEFAULT_INJECTION_ENVIRONMENT_FILE);

		FileFilter imageFilter = new FileNameExtensionFilter("Injection Environment files","ie");
		fileChooser.setFileFilter(imageFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int returnVal = fileChooser.showSaveDialog(injectMain.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			pref.put(DEFAULT_INJECTION_ENVIRONMENT_FILE, fileChooser.getSelectedFile().getAbsolutePath());

			TableModel model = table.getModel();
			StringBuffer recordText=new StringBuffer("");
			int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
			int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);
			int colIndexUserName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
			int colIndexPassword=table.getColumnModel().getColumnIndex(COLUMN_HEADING_PASSWORD);
			for (int count = 0; count < model.getRowCount(); count++){
				String applicationKey=model.getValueAt(count, colIndexApplicationKey).toString();
				String url=model.getValueAt(count, colIndexURL).toString();
				String userName=model.getValueAt(count, colIndexUserName).toString();
				String password=model.getValueAt(count, colIndexPassword).toString();
				//System.out.println("applicationKey:"+applicationKey+" url:"+url+" userName:"+userName+" password:"+password);
				recordText.append(applicationKey).append(SEPARATOR).append(url).append(SEPARATOR).append(userName).append(SEPARATOR).append(password);
				recordText.append("\n");
			}		
			File file=fileChooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".ie")) {
				file=new File(fileChooser.getSelectedFile().getParent(),fileChooser.getSelectedFile().getName()+".ie");
			}
			InjectUtils.encryptToFile(recordText.toString(),file);
		}
	}

	protected void selectApplicationsEnvironmentFile() throws Exception {
		int returnVal = ieFileChooser.showOpenDialog(injectMain.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Preferences pref = Preferences.userRoot();
			pref.put(DEFAULT_INJECTION_ENVIRONMENT_FILE, ieFileChooser.getSelectedFile().getAbsolutePath());
			loadApplicationsEnvironmentFile( ieFileChooser.getSelectedFile());
		}
	}
	
	protected void nextPanel() {
		if (injectionPackage==null) {
			GUIUtils.popupErrorMessage("You must select an Injection Package prior clicking 'Next'.");
			return;
		}
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		boolean isValid=validateRequiredFields();
		if (isValid) {
			injectMain.moveToPanel(InjectMain.EXECUTION_PANEL);
		}
	}

	private boolean validateRequiredFields() {
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int colIndexRequired=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REQUIRED);
		int colIndexApplicationName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_NAME);
		int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);
		int colIndexUserName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
		int colIndexPassword=table.getColumnModel().getColumnIndex(COLUMN_HEADING_PASSWORD);
		String errorMsg="";
		for (int count = 0; count < model.getRowCount(); count++){
			String isRequired=model.getValueAt(count, colIndexRequired).toString();
			if (isRequired.equals("Yes")) {
				String applicationName=table.getModel().getValueAt(count, colIndexApplicationName).toString();
				String value=table.getModel().getValueAt(count, colIndexURL).toString();
				if (value.isEmpty()) {
					errorMsg="The URL is missing for the application name: '"+applicationName+"'";
					GUIUtils.popupErrorMessage(errorMsg);
					return false;
				}
				value=table.getModel().getValueAt(count, colIndexUserName).toString();
				if (value.isEmpty()) {
					errorMsg="The Username is missing for the application name: '"+applicationName+"'";
					GUIUtils.popupErrorMessage(errorMsg);
					return false;
				}
				value=table.getModel().getValueAt(count, colIndexPassword).toString();
				if (value.isEmpty()) {
					errorMsg="The Password is missing for the application name: '"+applicationName+"'";
					GUIUtils.popupErrorMessage(errorMsg);
					return false;
				}	
			}
		}
		return true;
	}

	public void loadSeededApplicationsNames() throws Exception {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);

		List<String> lines=Files.readAllLines(Paths.get("fusionApplications.properties"), Charset.forName("UTF-8"));
		applicationKeysToNamesMap=new HashMap<String,String>();
		for(String line:lines){
			//System.out.println(line);
			if (line==null || line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			StringTokenizer tokenizer=new StringTokenizer(line,"=");
			String key=tokenizer.nextToken();
			String value=tokenizer.nextToken();
			applicationKeysToNamesMap.put(key, value);
			
			Vector<Object> row = new Vector<Object>();
			row.add(key);
			row.add(value);
			row.add("");
			if (key.equalsIgnoreCase(DATABASE_APPLICATION_KEY)) {
				row.add("JDBC String");
			}
			else {
				row.add("URL");
			}
			row.add("");
			row.add("");
			row.add("");
			model.addRow(row);
		}
		table.repaint(); 
	}

	protected void selectInjectionPackageFile() {
		try{
			Preferences pref = Preferences.userRoot();
			setDefaultFileChooserProperties(injectionPackageFileChooser,DEFAULT_INJECTION_PACKAGE_FILE);
					
			int returnVal = injectionPackageFileChooser.showOpenDialog(injectMain.getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// closing all Firefox browsers as we just changed the Injection package.
				injectMain.getExecutionPanelUI().getExecutionTabPanel().cleanupLastExecution();
				
				startInjectionPackageLoadDialog(injectionPackageFileChooser.getSelectedFile());
				pref.put(DEFAULT_INJECTION_PACKAGE_FILE, injectionPackageFileChooser.getSelectedFile().getAbsolutePath());
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}
	
	protected void setDefaultFileChooserProperties(JFileChooser fileChooser,String propertyName) {
			Preferences pref = Preferences.userRoot();
			String value = pref.get(propertyName, "");
			if (value!=null && !value.isEmpty()) {
				File file=new File(value);
				fileChooser.setCurrentDirectory(file.getParentFile());
				fileChooser.setSelectedFile(file);
			}
	}

	public File getInjectionPackage() {
		return injectionPackage;
	}

	public void setInjectionPackage(File injectionPackage) {
		this.injectionPackage=injectionPackage;
	}
	
	protected void loadApplicationsEnvironmentFile(File file) throws Exception {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		injectionEnvironmentFileLabel.setText("Selected Injection Environment: "+file.getAbsolutePath());

		String text=InjectUtils.decryptFromFile(file);
		DefaultTableModel model = (DefaultTableModel)table.getModel();

		StringTokenizer tokenizer=new StringTokenizer(text,"\n");
		int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
		int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);
		int colIndexUserName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
		int colIndexPassword=table.getColumnModel().getColumnIndex(COLUMN_HEADING_PASSWORD);
	
		while (tokenizer.hasMoreTokens()) {
			String token=tokenizer.nextToken();
			//System.out.println("token:"+token+"%%%%%%%%%");

			StringBuffer remainingText=new StringBuffer("");
			String applicationKey=InjectUtils.getInjectionEnvironmentNextToken(token,remainingText);

			String remain=remainingText.toString();
			remainingText=new StringBuffer("");
			String applicationURL=InjectUtils.getInjectionEnvironmentNextToken(remain,remainingText);

			remain=remainingText.toString();
			remainingText=new StringBuffer("");
			String applicationUser=InjectUtils.getInjectionEnvironmentNextToken(remain,remainingText);

			String applicationPassword=remainingText.toString();

			for (int count = 0; count < model.getRowCount(); count++){
				String applicationKeyFromTable=model.getValueAt(count, colIndexApplicationKey).toString();
				//System.out.println("applicationKeyFromTable:"+applicationKeyFromTable+" applicationKey:"+applicationKey);
				if (applicationKeyFromTable.equals(applicationKey)) {
					model.setValueAt(applicationURL,count, colIndexURL);
					model.setValueAt(applicationUser,count, colIndexUserName);
					model.setValueAt(applicationPassword,count, colIndexPassword);
				}
			}
		}
		if (ieFileChooser.getSelectedFile()!=null) {
			injectMain.getExecutionPanelUI().getExecutionTabPanel().displaySelectedIEPathName(ieFileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	public void startInjectionPackageLoadDialog(File injectionPackage) {
		InjectionPackageLoadDialog swingWorker=new InjectionPackageLoadDialog(this,injectionPackage);
		final int width=450;
		final int height=150;
		UIUtils.displayOperationInProgressModalWindow(injectMain.getFrame(),
				width,height,"Loading Injection Package...",swingWorker,false,InjectMain.getSharedApplicationIconPath());	
	}
	
	public void loadInjectionPackageFile(InjectionPackageLoadDialog injectionPackageLoadDialog, File injectionPackage) throws Exception{
		injectionPackageFileChooser.setCurrentDirectory(injectionPackage);
		injectionPackageLoadDialog.updateExecutionLabels("Unpacking, please wait...");
		InjectUtils.unpackInjectionPackage(injectMain, injectionPackage,injectionPackageLoadDialog);
		
		injectionPackageInformationDocument = InjectUtils.getInjectionPackageInformationDocument(injectMain, injectionPackage);
		if (injectionPackageInformationDocument != null) {
			injectionPackageFileLabel.setText("<html>Selected Injection Package: <U>"+injectionPackage.getAbsolutePath()+"</U></html>");
		} else {
			injectionPackageFileLabel.setText("Selected Injection Package: "+injectionPackage.getAbsolutePath());
		}
		setInjectionPackage(injectionPackage);
		injectionPackageFileChooser.setSelectedFile(injectionPackage);
		injectMain.getExecutionPanelUI().getExecutionTabPanel().displaySelectedPackagePathName(injectionPackage.getAbsolutePath());

		FusionScriptType fusionScriptType=InjectUtils.getFusionScriptType(injectMain,injectionPackage);
		ScriptType[] scriptsArray=fusionScriptType.getScriptArray();
		implementationProject=fusionScriptType.getImplementationProject();
		List<ScriptType> scripts=Arrays.asList(scriptsArray);
		
		Set<String> isRequiredSet=new HashSet<String>();
		boolean isDataBaseRequired=false;
		injectionPackageLoadDialog.updateExecutionLabels("Analyzing, please wait...");
		for (ScriptType scriptType:scripts) {
			String applicationKey=scriptType.getApplicationKey();
			com.rapidesuite.core.fusionScripts0000.ScriptType.Type.Enum type=scriptType.getType();
			if (type==com.rapidesuite.core.fusionScripts0000.ScriptType.Type.API) {
				isDataBaseRequired=true;
			}
			FusionDataType fusionDataType=InjectUtils.getFusionDataType(injectMain,injectionPackage,scriptType);
			LType[] lArray=fusionDataType.getLArray();
			if (lArray==null || lArray.length==0) {
				// not required if there is no data.
			}
			else {
				MType[] mArray=lArray[0].getMArray();
				if (mArray==null || mArray.length==0) {
					// not required if there is no data.
				}
				else {
					isRequiredSet.add(applicationKey);
				}
			}
		}
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
		int colIndexRequired=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REQUIRED);
		for (int count = 0; count < model.getRowCount(); count++){
			String applicationKeyFromTable=model.getValueAt(count, colIndexApplicationKey).toString();
			if (applicationKeyFromTable.equalsIgnoreCase(DATABASE_APPLICATION_KEY) && isDataBaseRequired) {
				model.setValueAt("Yes",count, colIndexRequired);
			}
			else
			if (isRequiredSet.contains(applicationKeyFromTable)) {
				model.setValueAt("Yes",count, colIndexRequired);
			}
			else {
				model.setValueAt("No",count, colIndexRequired);
			}
		}
		table.repaint(); 
		injectionPackageLoadDialog.updateExecutionLabels("Parsing, please wait...");
		injectMain.getExecutionPanelUI().getExecutionTabPanel().loadScripts(injectionPackage, scripts);
	}
	
	public JButton getSelectPackageButton() {
		return selectPackageButton;
	}


	public String getImplementationProject() {
		return implementationProject;
	}

	public String getUrl(String applicationKey) throws Exception {
		int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);
		return getValue(applicationKey,colIndexURL);
	}

	public String getUserName(String applicationKey) throws Exception {
		int colIndexUserName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
		return getValue(applicationKey,colIndexUserName);
	}

	public String getPassword(String applicationKey) throws Exception {
		int colIndexPassword=table.getColumnModel().getColumnIndex(COLUMN_HEADING_PASSWORD);
		return getValue(applicationKey,colIndexPassword);
	}

	public String getValue(String applicationKey,int indexColumn) throws Exception {
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
		for (int count = 0; count < model.getRowCount(); count++){
			String applicationKeyFromTable=model.getValueAt(count, colIndexApplicationKey).toString();
			
			if (applicationKey.equals(applicationKeyFromTable)) {
				String value=table.getModel().getValueAt(count, indexColumn).toString();
				return value;
			}
		}
		throw new Exception("Internal error: unable to find information in the Injection Environment screen (applicationKey: "+applicationKey+
				" indexColumn: "+indexColumn+") ");
	}

	public Map<String, String> getApplicationKeysToNamesMap() {
		return applicationKeysToNamesMap;
	}

	public InjectionPackageInformationDocument getInjectionPackageInformationDocument() {
		return injectionPackageInformationDocument;
	}
	
}
