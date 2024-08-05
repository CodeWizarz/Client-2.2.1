package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.oracle.xmlns.oxp.service.v2.CatalogService;
import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;
import com.rapidesuite.extract.model.ExtractionPackageLoadDialog;
import com.rapidesuite.extract.model.HookGetCodeCombination;
import com.rapidesuite.extract.model.HookGetSystemProfileValue;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.PasswordCellRenderer;
import com.rapidesuite.inject.gui.TabChangeListener;
import com.rapidesuite.inject.gui.TabbedPaneUI;
import com.rapidesuite.inject.gui.TableHeaderRenderer;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class ApplicationInfoPanel extends JPanel {

	private ExtractMain extractMain;
	private JTable table;
	private File extractionPackage;
	private JFileChooser extractionPackageFileChooser;
	private JLabel extractionEnvironmentFileLabel;
	private JButton selectPackageButton;
	private Map<String,String> applicationKeysToNamesMap;
	
	private String COLUMN_HEADING_APPLICATION_KEY="Application key";
	private String COLUMN_HEADING_APPLICATION_NAME="Name";
	private String COLUMN_HEADING_REQUIRED="Is Required";
	private String COLUMN_HEADING_TYPE="Type";
	private String COLUMN_HEADING_URL="Value";
	private String COLUMN_HEADING_NAME="Username";
	private String COLUMN_HEADING_PASSWORD="Password";

	private int COLUMN_HEADING_APPLICATION_NAME_WIDTH=200;
	private int COLUMN_HEADING_REQUIRED_WIDTH=70;
	private int COLUMN_HEADING_TYPE_WIDTH=80;
	private int COLUMN_HEADING_URL_WIDTH=350;
	private int COLUMN_HEADING_NAME_WIDTH=150;
	private int COLUMN_HEADING_PASSWORD_WIDTH=150;
	private boolean isDefaultLoaded;
	private boolean hasDefaultToLoad;
	private JLabel extractionPackageFileLabel;

	public static final String DEFAULT_EXTRACTION_PACKAGE_FILE="DEFAULT_EXTRACTION_PACKAGE_FILE";
	public static final String DEFAULT_EXTRACTION_ENVIRONMENT_FILE="DEFAULT_EXTRACTION_ENVIRONMENT_FILE";
	public static final String SEPARATOR="##RES##";
	public static final String APPLICATION_BI_PUBLISHER_REPORT_SERVICE="APPLICATION_BI_PUBLISHER_REPORT_SERVICE";
	public static final String APPLICATION_BI_PUBLISHER_CATALOG_SERVICE="APPLICATION_BI_PUBLISHER_CATALOG_SERVICE";
	public static final String APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH="APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH";
	
	private Map<String,File> inventoryNameToInventoryFileMap;
	private SortedMap<String, SortedSet<File>> inventoryNameToSQLFileMap;
	private CatalogService catalogService;
	private WebServiceInfo catalogWebServiceInfo;
	private WebServiceInfo reportWebServiceInfo;
	private HookGetCodeCombination hookGetCodeCombination;
	private HookGetSystemProfileValue hookGetSystemProfileValue;
	private ReportService reportService;
	private Set<String> allFunctionNamesSet;
	private String sessionBIPublisherPath;
	private String executionBIPublisherPath;
	private OrgStructureFrame orgStructureFrame;
	
	public ApplicationInfoPanel(final ExtractMain extractMain) throws Exception {
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		this.extractMain=extractMain;

		JPanel mainPanel=new JPanel();
		this.add(mainPanel,BorderLayout.CENTER);
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#343836") );
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		JPanel applicationNamesPanel=new JPanel();
		applicationNamesPanel.setOpaque(true);
		applicationNamesPanel.setBackground(Color.decode("#4B4F4E"));
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

		InjectUtils.addMenuTab(jtp,extractMain,CoreConstants.SHORT_APPLICATION_NAME.extract.toString(),tabIndex,tabHeight);
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
		
		InjectUtils.addLogo(jtp,extractMain);

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
		
		extractionPackageFileLabel=new JLabel("No package selected.");
		tempPanel2.add(Box.createHorizontalGlue());
		tempPanel2.add(extractionPackageFileLabel);
		tempPanel2.add(Box.createHorizontalGlue());
		extractionPackageFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		extractionPackageFileLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		extractionPackageFileLabel.setForeground(Color.decode("#cccccc"));
		InjectUtils.assignArialBoldFont(extractionPackageFileLabel,UIConstants.FONT_SIZE_NORMAL);	

		extractionPackageFileChooser= new JFileChooser();
		FileFilter imageFilter = new FileNameExtensionFilter("Extraction Package files",ExtractConstants.EXTRACT_PACKAGE_FILE_EXTENSION);
		extractionPackageFileChooser.setFileFilter(imageFilter);
		extractionPackageFileChooser.setAcceptAllFileFilterUsed(false);

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
				int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
				String applicationKey=table.getModel().getValueAt(row, colIndexApplicationKey).toString();
				int colIndexApplicationName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_NAME);
				int colIndexRequired=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REQUIRED);
				int colIndexType=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TYPE);
				int colIndexUserName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
				int colIndexPassword=table.getColumnModel().getColumnIndex(COLUMN_HEADING_PASSWORD);
				
				if (column==colIndexApplicationKey || column==colIndexApplicationName || column==colIndexRequired || column==colIndexType) {
					return false;
				}
				if (applicationKey.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH)) {
					if (column==colIndexUserName || column==colIndexPassword) {
						return false;
					}
				}
				return true;
			}

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int columnIndex) {
				Component component= super.prepareRenderer(renderer, rowIndex, columnIndex);
				component.setForeground(Color.decode("#2F3436"));

				int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
				String applicationKey=table.getModel().getValueAt(rowIndex, colIndexApplicationKey).toString();
				int colIndexApplicationName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_NAME);
				int colIndexRequired=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REQUIRED);
				int colIndexType=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TYPE);
				int colIndexUserName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
				int colIndexPassword=table.getColumnModel().getColumnIndex(COLUMN_HEADING_PASSWORD);
				int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);
				int colIndexName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
				
				if (columnIndex==colIndexApplicationName || columnIndex==colIndexRequired || columnIndex==colIndexType){
					component.setBackground(Color.decode("#DBDBDB"));
					return component;
				}
				else
				if (applicationKey.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH)) {
					if (columnIndex==colIndexUserName || columnIndex==colIndexPassword) {
						component.setBackground(Color.decode("#DBDBDB"));
						return component;
					}
				}

				String isRequired=table.getModel().getValueAt(rowIndex, colIndexRequired).toString();
				if (isRequired.equals("Yes")) {					
					if (columnIndex==colIndexURL ||columnIndex==colIndexName ||columnIndex==colIndexPassword ) {
						String value=table.getModel().getValueAt(rowIndex, columnIndex).toString();
						if (value.isEmpty()) {
							if (hasDefaultToLoad && !isDefaultLoaded) {
								component.setBackground(Color.white);
							}
							else {
								component.setBackground(UIConstants.COLOR_RED);
							}
						}
						else {
							component.setBackground(Color.white);
						}
					}						
				}
				else {
					component.setBackground(Color.white);
				}
								
				return component;
			}
			
			 public String getToolTipText(MouseEvent e) {
	                String tip = null;
	                java.awt.Point p = e.getPoint();
	                int colIndex = columnAtPoint(p);
	                int rowIndex = rowAtPoint(p);
	                int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
	                int colIndexApplicationName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_NAME);
	                int colIndexType=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TYPE);
	                int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);
	                String value=table.getModel().getValueAt(rowIndex, colIndexApplicationKey).toString();
	                if (  ( colIndex==colIndexURL || colIndex==colIndexApplicationKey || colIndex==colIndexApplicationName || colIndex==colIndexType)
	                	) {
	                	if (value.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_REPORT_SERVICE)) {
	                		tip = "<html><p width=\"500\">Enter the URL to your BI Pusblisher Web Service."+
	    	                	"<br>Format:  http://hostname:port/path"+
	    	                	"<br>Example: http://fusion01.rapidesuite.com:18621/xmlpserver/services/v2/ReportService"+
	    	                	"</p></html>";
	                	}
	                	else
	                		if (value.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_REPORT_SERVICE)) {
	                		tip = "<html><p width=\"500\">Enter the URL to your BI Pusblisher Web Service."+
		    	                	"<br>Format:  http://hostname:port/path"+
		    	                	"<br>Example: http://fusion01.rapidesuite.com:18621/xmlpserver/services/v2/CatalogService"+
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
		table.setFont(new Font("Arial", Font.PLAIN, UIConstants.FONT_SIZE_NORMAL));
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(50, 116, 5, 116));
		tempPanel.setLayout(new BorderLayout());
		tempPanel.setOpaque(true);
		tempPanel.setBackground(Color.decode("#FAFCFC"));
		tempPanel.add(scrollPane,BorderLayout.CENTER);
		applicationNamesPanel.add(tempPanel);
		
		/*
		 *  SOUTH PANEL
		 */
		JPanel tempPanelSouth=new JPanel();
		applicationNamesPanel.add(tempPanelSouth);
		tempPanelSouth.setOpaque(true);
		tempPanelSouth.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tempPanelSouth.setBackground(Color.decode("#FAFCFC"));
		tempPanelSouth.setLayout(new BoxLayout(tempPanelSouth, BoxLayout.Y_AXIS));
		
		extractionEnvironmentFileLabel=new JLabel("");
		InjectUtils.assignArialPlainFont(extractionEnvironmentFileLabel,UIConstants.FONT_SIZE_NORMAL);
		tempPanelSouth.add(extractionEnvironmentFileLabel);

		tempPanel=new JPanel();
		tempPanelSouth.add(tempPanel);
		tempPanel.setOpaque(true);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 18, 0));
		tempPanel.setBackground(Color.decode("#FAFCFC"));
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
				validationThread();
			}
		});
		tempPanel.add(Box.createHorizontalGlue());

		loadSeededApplicationsNames();
		
		Preferences pref = Preferences.userRoot();
		String value = InjectUtils.decrypt(pref.get(DEFAULT_EXTRACTION_PACKAGE_FILE,""));
		if (value!=null && !value.isEmpty()) {
			final File file=new File(value);
			isDefaultLoaded=false;
			new Thread( new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
						startExtractionPackageLoadDialog(file);
					} catch (Exception e) {
						FileUtils.printStackTrace(e);
						GUIUtils.popupErrorMessage(e.getMessage());
					}
				}
			}).start();
		}
		value = InjectUtils.decrypt(pref.get(DEFAULT_EXTRACTION_ENVIRONMENT_FILE,""));
		hasDefaultToLoad=false;
		if (value!=null && !value.isEmpty()) {
			final File file=new File(value);
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
				}
			}).start();
		}
	}

	protected void saveApplicationsEnvironmentFile() throws Exception {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}

		JFileChooser fileChooser= new JFileChooser();

		Preferences pref = Preferences.userRoot();
		setDefaultFileChooserProperties(fileChooser,DEFAULT_EXTRACTION_ENVIRONMENT_FILE);

		FileFilter imageFilter = new FileNameExtensionFilter("Extraction Environment files",ExtractConstants.EXTRACT_ENVIRONMENT_FILE_EXTENSION);
		fileChooser.setFileFilter(imageFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int returnVal = fileChooser.showSaveDialog(extractMain.getRootFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			pref.put(DEFAULT_EXTRACTION_ENVIRONMENT_FILE, fileChooser.getSelectedFile().getAbsolutePath());

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
			if (!file.getName().toLowerCase().endsWith(ExtractConstants.EXTRACT_ENVIRONMENT_FILE_EXTENSION)) {
				file=new File(fileChooser.getSelectedFile().getParent(),fileChooser.getSelectedFile().getName()+ExtractConstants.EXTRACT_ENVIRONMENT_FILE_EXTENSION);
			}
			InjectUtils.encryptToFile(recordText.toString(),file);
		}
	}

	protected void selectApplicationsEnvironmentFile() throws Exception {
		JFileChooser fileChooser= new JFileChooser();
		Preferences pref = Preferences.userRoot();
		setDefaultFileChooserProperties(fileChooser,DEFAULT_EXTRACTION_ENVIRONMENT_FILE);
		FileFilter imageFilter = new FileNameExtensionFilter("Extraction Environment files",ExtractConstants.EXTRACT_ENVIRONMENT_FILE_EXTENSION);
		fileChooser.setFileFilter(imageFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int returnVal = fileChooser.showOpenDialog(extractMain.getRootFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			pref.put(DEFAULT_EXTRACTION_ENVIRONMENT_FILE, fileChooser.getSelectedFile().getAbsolutePath());
			loadApplicationsEnvironmentFile( fileChooser.getSelectedFile());
		}
	}
	
	public WebServiceInfo getCatalogWebServiceInfo() {
		return catalogWebServiceInfo;
	}
	
	public WebServiceInfo getReportWebServiceInfo() {
		return reportWebServiceInfo;
	}
	
	

	public void loadSeededApplicationsNames() throws Exception {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);

		List<String> lines=Files.readAllLines(Paths.get("extractApplications.properties"), Charset.forName("UTF-8"));
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
			//System.out.println("line:"+line+" key:"+key+" value:"+value);
			
			Vector<Object> row = new Vector<Object>();
			row.add(key);
			row.add(value);
			row.add("");
			
			if (key.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH)) {
				row.add("PATH");
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
			setDefaultFileChooserProperties(extractionPackageFileChooser,DEFAULT_EXTRACTION_PACKAGE_FILE);
					
			int returnVal = extractionPackageFileChooser.showOpenDialog(extractMain.getRootFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				startExtractionPackageLoadDialog(extractionPackageFileChooser.getSelectedFile());
				pref.put(DEFAULT_EXTRACTION_PACKAGE_FILE, extractionPackageFileChooser.getSelectedFile().getAbsolutePath());
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

	public File getExtractionPackage() {
		return extractionPackage;
	}

	public void setExtractionPackage(File extractionPackage) {
		this.extractionPackage=extractionPackage;
	}
	
	protected void loadApplicationsEnvironmentFile(File file) throws Exception {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		orgStructureFrame=null;
		extractionEnvironmentFileLabel.setText("Selected Extraction Environment: "+file.getAbsolutePath());

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
	}

	public void startExtractionPackageLoadDialog(File extractionPackage) {
		ExtractionPackageLoadDialog swingWorker=new ExtractionPackageLoadDialog(this,extractionPackage);
		final int width=450;
		final int height=150;
		UIUtils.displayOperationInProgressModalWindow(extractMain.getRootFrame(),
				width,height,"Loading Extraction Package...",swingWorker,false,ExtractMain.getSharedApplicationIconPath());	
	}

	public void loadExtractionPackageFile(File extractionPackage) throws Exception{
		extractionPackageFileChooser.setCurrentDirectory(extractionPackage);
		extractionPackageFileLabel.setText("Selected Extraction Package: "+extractionPackage.getAbsolutePath());
		setExtractionPackage(extractionPackage);
		extractionPackageFileChooser.setSelectedFile(extractionPackage);
		extractMain.getExtractMainPanel().getTabSelectionPanel().displaySelectedPackagePathName(extractionPackage.getAbsolutePath());
		
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
		int colIndexRequired=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REQUIRED);
		for (int count = 0; count < model.getRowCount(); count++){
			String applicationKeyFromTable=model.getValueAt(count, colIndexApplicationKey).toString();
			if (applicationKeyFromTable.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_REPORT_SERVICE)  ||
				applicationKeyFromTable.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_CATALOG_SERVICE)) {
				model.setValueAt("Yes",count, colIndexRequired);
			}
			else
			if (applicationKeyFromTable.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH)) {
				model.setValueAt("Yes",count, colIndexRequired);
			}
			else {
				model.setValueAt("No",count, colIndexRequired);
			}
		}
		isDefaultLoaded=true;
		
		table.repaint();		
	}
	
	public JButton getSelectPackageButton() {
		return selectPackageButton;
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
		throw new Exception("Internal error: unable to find information in the Environment screen (applicationKey: "+applicationKey+
				" indexColumn: "+indexColumn+") ");
	}

	public Map<String, String> getApplicationKeysToNamesMap() {
		return applicationKeysToNamesMap;
	}

	public ExtractMain getExtractMain() {
		return extractMain;
	}

	public Map<String,File> getInventoryNameToInventoryFileMap() {
		return inventoryNameToInventoryFileMap;
	}

	public SortedMap<String, SortedSet<File>> getInventoryNameToSQLFileMap() {
		return inventoryNameToSQLFileMap;
	}

	public void setInventoryNameToInventoryFileMap(
			Map<String,File> inventoryNameToInventoryFileMap) {
		this.inventoryNameToInventoryFileMap = inventoryNameToInventoryFileMap;
	}

	public void setInventoryNameToSQLFileMap(
			SortedMap<String, SortedSet<File>> inventoryNameToSQLFileMap) {
		this.inventoryNameToSQLFileMap = inventoryNameToSQLFileMap;
	}

	public CatalogService getCatalogService() {
		return catalogService;
	}
	
	public void validationThread()
	{
		try{
			SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					validationProcess();
					return null;
				}
			};

			String msg="Validating, please wait...";
			final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			final JDialog dialog = new JDialog();
			dialog.setTitle("Validation");
			dialog.setModal(true);
			dialog.setContentPane(optionPane);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.pack();

			mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("state")) {
						if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
							dialog.dispose();
						}
					}
				}
			});
			mySwingWorker.execute();

			dialog.setLocationRelativeTo(extractMain.getRootFrame());
			dialog.setVisible(true);
		}
		catch(Exception e) {
			//e.printStackTrace();
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}	

	protected void validationProcess() throws Exception {
		if (extractionPackage==null) {
			GUIUtils.popupErrorMessage("You must select an Extraction Package prior clicking 'Next'.");
			return;
		}
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		boolean isValid=validateRequiredFields();
		if (isValid) {
			FileUtils.println("validateBIConnections...");
			isValid=validateBIConnections();
			FileUtils.println("validateBIConnections COMPLETED!");
			if (isValid) {				
				FileUtils.println("initializeHooks...");
				initializeHooks();
				FileUtils.println("initializeHooks COMPLETED!");
				extractMain.getExtractMainPanel().resetPanels();
				extractMain.moveToPanel(ExtractMain.EXECUTION_PANEL);
			}
		}
	}
	
	public String getDynamicNonSQLReportPath() throws Exception {
		String dynamicSQLReportPath=getSessionBIPublisherPath()+"/"+ExtractConstants.BI_PUBLISHER_DYNAMIC_SQL_DATASET_NAME+".xdo";
		return dynamicSQLReportPath;
	}
	
	public void initializeHooks() throws Exception {
		if (hookGetCodeCombination==null) {
			hookGetCodeCombination=new HookGetCodeCombination();
		}
		if (hookGetSystemProfileValue==null) {
			hookGetSystemProfileValue=new HookGetSystemProfileValue();
		}
		FileUtils.println("Functions count used in the package: "+allFunctionNamesSet.size());
		if (allFunctionNamesSet.contains(ExtractConstants.FUNCTION_GET_CODE_COMBINATION)) {
			FileUtils.println("Initializing Function code combination...");
			hookGetCodeCombination.init(getDynamicNonSQLReportPath(), reportWebServiceInfo, reportService);
			FileUtils.println("Function code combination initialized!");
		}
		if (allFunctionNamesSet.contains(ExtractConstants.FUNCTION_GET_SYSTEM_PROFILE_VALUE)) {
			hookGetSystemProfileValue.init();
		}
	}

	private boolean validateBIConnections() {
		try {
			String reportUsername = getUserName(ApplicationInfoPanel.APPLICATION_BI_PUBLISHER_REPORT_SERVICE);
	    	String reportPassword = getPassword(ApplicationInfoPanel.APPLICATION_BI_PUBLISHER_REPORT_SERVICE);
	    	String reportServiceEndpointUrl =getUrl(ApplicationInfoPanel.APPLICATION_BI_PUBLISHER_REPORT_SERVICE);
	    	
	    	String catalogUsername = getUserName(ApplicationInfoPanel.APPLICATION_BI_PUBLISHER_CATALOG_SERVICE);
	    	String catalogPassword = getPassword(ApplicationInfoPanel.APPLICATION_BI_PUBLISHER_CATALOG_SERVICE);
	    	String catalogServiceEndpointUrl = getUrl(ApplicationInfoPanel.APPLICATION_BI_PUBLISHER_CATALOG_SERVICE);
	    	
	    	catalogWebServiceInfo=new WebServiceInfo();
	    	catalogWebServiceInfo.setWebServiceEndpointUrl(catalogServiceEndpointUrl);
	    	catalogWebServiceInfo.setUsername(catalogUsername);
	    	catalogWebServiceInfo.setPassword(catalogPassword);
	    	
	    	reportWebServiceInfo=new WebServiceInfo();
	    	reportWebServiceInfo.setWebServiceEndpointUrl(reportServiceEndpointUrl);
	    	reportWebServiceInfo.setUsername(reportUsername);
	    	reportWebServiceInfo.setPassword(reportPassword);
	    	
			catalogService=ExtractUtils.getCatalogService(catalogWebServiceInfo);
			String catalogFolderPath = getUrl(ApplicationInfoPanel.APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH);
			try{
				ExtractUtils.getFolderContents(catalogWebServiceInfo,catalogService,catalogFolderPath);
				
				// Create workspace in BI publisher for the user session (application lifetime)
				boolean objectExist=ExtractUtils.isObjectExist(catalogWebServiceInfo,catalogService,getSessionBIPublisherPath());
				if (!objectExist) {
					ExtractUtils.createCatalogFolderBIWebService(catalogWebServiceInfo,catalogService,getSessionBIPublisherPath());
					String nonSQLCatalogPath=getSessionBIPublisherPath()+"/";
					ExtractUtils.createDynamicSQLObjects(catalogWebServiceInfo,nonSQLCatalogPath);
					ExtractUtils.createGetValueSetTableTypeSQLObjects(catalogWebServiceInfo,nonSQLCatalogPath);
				}
			}
			catch(Exception ex) {
				String reformattedErrorMsg=ExtractUtils.processBIConnectionExceptions(ex,catalogServiceEndpointUrl,"Catalog","CatalogService");
				throw new Exception(reformattedErrorMsg);
			}			
			
			reportService=ExtractUtils.getReportService(reportWebServiceInfo);
			try{
				// test a dummy query to make sure the report service URL is correct
				String dynamicNonSQLReportPath=getDynamicNonSQLReportPath();
				FileUtils.println("Running a dummy query to validate the Report Service URL, dynamicSQLReportPath: '"+dynamicNonSQLReportPath+"'"+
				" reportWebServiceInfo: '"+reportWebServiceInfo.getWebServiceEndpointUrl()+"'");
				byte[] data=ExtractUtils.getRawDualQuery(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
				FileUtils.println("Dummy query executed, data: '"+new String(data,"ISO-8859-1")+"'");
			}
			catch(Exception ex) {
				String reformattedErrorMsg=ExtractUtils.processBIConnectionExceptions(ex,catalogServiceEndpointUrl,"Report","ReportService");
				throw new Exception(reformattedErrorMsg);
			}
			
			return true;
		} 
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
			//e.printStackTrace();
			GUIUtils.popupErrorMessage(e.getMessage());
		}
		return false;
	}

	private boolean validateRequiredFields() {
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int colIndexRequired=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REQUIRED);
		int colIndexApplicationName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_NAME);
		int colIndexURL=table.getColumnModel().getColumnIndex(COLUMN_HEADING_URL);
		int colIndexUserName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
		int colIndexPassword=table.getColumnModel().getColumnIndex(COLUMN_HEADING_PASSWORD);
		int colIndexApplicationKey=table.getColumnModel().getColumnIndex(COLUMN_HEADING_APPLICATION_KEY);
		 
		String errorMsg="";
		for (int count = 0; count < model.getRowCount(); count++){
			String isRequired=model.getValueAt(count, colIndexRequired).toString();
			if (isRequired.equals("Yes")) {
				String applicationName=table.getModel().getValueAt(count, colIndexApplicationName).toString();
				String value=table.getModel().getValueAt(count, colIndexURL).toString();
				String applicationKey=model.getValueAt(count, colIndexApplicationKey).toString();
				
				if (value.isEmpty()) {
					errorMsg="The Value is missing for the name: '"+applicationName+"'";
					GUIUtils.popupErrorMessage(errorMsg);
					return false;
				}
				value=table.getModel().getValueAt(count, colIndexUserName).toString();
				if (value.isEmpty() && !applicationKey.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH) ) {
					errorMsg="The Username is missing for the name: '"+applicationName+"'";
					GUIUtils.popupErrorMessage(errorMsg);
					return false;
				}
				value=table.getModel().getValueAt(count, colIndexPassword).toString();
				if (value.isEmpty() && !applicationKey.equalsIgnoreCase(APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH) ) {
					errorMsg="The Password is missing for the name: '"+applicationName+"'";
					GUIUtils.popupErrorMessage(errorMsg);
					return false;
				}	
			}
		}
		return true;
	}

	public HookGetCodeCombination getHookGetCodeCombination() {
		return hookGetCodeCombination;
	}

	public HookGetSystemProfileValue getHookGetSystemProfileValue() {
		return hookGetSystemProfileValue;
	}

	public String getSessionBIPublisherPath() throws Exception {
		/*
		 * The RapidExtract folder must be created by the BI Publisher Admin and given special privileges:
		 * READ, WRITE and EXECUTE. So then we will create a subfolder per program session (use of the Application) so that many users
		 * can run in parallel.
		 */
		if (sessionBIPublisherPath==null) {
			Random rand = new Random();
			int randomNum = rand.nextInt(10000);
			String osUserName=System.getProperty("user.name");
			
			String catalogFolderPath = getUrl(ApplicationInfoPanel.APPLICATION_BI_PUBLISHER_CATALOG_FOLDER_PATH);
			
			sessionBIPublisherPath=catalogFolderPath+"/"+osUserName+randomNum;
		}
		return sessionBIPublisherPath;
	}

	public String getExecutionBIPublisherPath() {
		if (executionBIPublisherPath==null) {
			SimpleDateFormat sdfDate = new SimpleDateFormat(ExtractConstants.CATALOG_BI_PUBLISHER_DATE_FORMAT);
			Date now = new Date();
			String strDate = sdfDate.format(now);
			executionBIPublisherPath=sessionBIPublisherPath+"/"+strDate;
		}
		return executionBIPublisherPath;
	}	
	
	public void deleteSessionBIPublisherPath() {
		if (sessionBIPublisherPath!=null) {
			try
			{
				SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
					@Override
					protected Void doInBackground() throws Exception {
						
						FileUtils.println("Deleting folder: '"+sessionBIPublisherPath+"' ...");
						ExtractUtils.deleteBIFolder(sessionBIPublisherPath,catalogService,catalogWebServiceInfo);
						FileUtils.println("deleted.");
						
						return null;
					}
				};

				String msg="Cleaning up BI Publisher, please wait...";
				final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
				final JDialog dialog = new JDialog();
				dialog.setTitle("Connecting...");
				dialog.setModal(true);
				dialog.setContentPane(optionPane);
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.pack();

				mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("state")) {
							if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
								dialog.dispose();
							}
						}
					}
				});
				mySwingWorker.execute();

				dialog.setLocationRelativeTo(extractMain.getRootFrame());
				dialog.setVisible(true);		
			}
			catch ( Exception e )
			{
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage(e.getMessage());
			}
		}
	}	

	public ReportService getReportService() {
		return reportService;
	}

	public void setAllFunctionNamesSet(Set<String> allFunctionNamesSet) {
		this.allFunctionNamesSet=allFunctionNamesSet;
	}

	public OrgStructureFrame getOrgStructureFrame() {
		if (orgStructureFrame==null) {
			orgStructureFrame=new OrgStructureFrame(this);
		}
		return orgStructureFrame;
	}

	
}
