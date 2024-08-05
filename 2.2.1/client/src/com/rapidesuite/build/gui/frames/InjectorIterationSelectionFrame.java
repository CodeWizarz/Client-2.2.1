package com.rapidesuite.build.gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.util.Assert;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.controller.InjectorsManager;
import com.rapidesuite.build.gui.WindowsExplorerStyleJTable;
import com.rapidesuite.build.gui.panels.InjectorsPackageExecutionPanel.IterationStatus;
import com.rapidesuite.build.utils.InjectorsPackageUtils;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.CoreConstants.INJECTOR_TYPE;
import com.rapidesuite.core.CoreConstants;

public class InjectorIterationSelectionFrame extends JFrame implements TableModelListener
{

	private static final long serialVersionUID = 1L;
	private JPanel northPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel southPanel = new JPanel();
	private WindowsExplorerStyleJTable table;
	private DefaultTableModel tableModel;
	BuildMain BuildMain;
	JLabel label;
	private JButton button;

	private Set<Integer> executableIterations = null;

	private Injector injectorOfDisplayedIterations = null;

	private Map<Injector, Map<Integer, IterationStatus>> injectorIterationStatus = null;
	
	private boolean isIterationTrackingEnabled = false;
	
	private InjectorsPackageUtils.TokenReplacementProcessor tokenReplacementProcessor = null;
	
	private ActionManager actionManager = null;

	private static enum ITERATION_WINDOW_COLUMN {
		SELECTION (0, "", 10),
		NUMBER (1, "Line", 270),
		STATUS (2, "Status", 70),
		TIME (3, "Time", 100);

		private final int index;
		private final String header;
		private final int width;

		private ITERATION_WINDOW_COLUMN(final int index, final String header, final int width) {
			this.index = index;
			this.header = header;
			this.width = width;
		}
		public int getIndex() {
			return index;
		}
		public String getHeader() {
			return header;
		}
		public int getWidth() {
			return width;
		}
		public static ITERATION_WINDOW_COLUMN[] getSorted() {
			List<ITERATION_WINDOW_COLUMN> list = Arrays.asList(ITERATION_WINDOW_COLUMN.values());
			Collections.sort(list, new Comparator<ITERATION_WINDOW_COLUMN>() {
				@Override
				public int compare(ITERATION_WINDOW_COLUMN o1,
						ITERATION_WINDOW_COLUMN o2) {
					if (o1.getIndex() < o2.getIndex()) {
						return -1;
					} else if (o1.getIndex() > o2.getIndex()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			return list.toArray(new ITERATION_WINDOW_COLUMN[ITERATION_WINDOW_COLUMN.values().length]);
		}
	}
	static {
		ITERATION_WINDOW_COLUMN[] columns = ITERATION_WINDOW_COLUMN.getSorted();
		for (int i = 0 ; i < columns.length ; i++) {
			Assert.isTrue(i == columns[i].getIndex(), "ITERATION_WINDOW_COLUMN columns are not ordered");
		}
	}

	public InjectorIterationSelectionFrame(BuildMain BuildMain) throws Exception
	{
		this.BuildMain = BuildMain;
		this.injectorIterationStatus = new HashMap<Injector, Map<Integer, IterationStatus>>();
		init();

	}

	public void setStartButtonEnabled(boolean isEnabled)
	{
		this.button.setEnabled(isEnabled);
	}
	
	private boolean canViewSelectedIterations() {
		return this.isIterationTrackingEnabled && this.table.getSelectedRowCount() > 0;
	}
	
	private boolean canViewAllIterationsStartingFromTheSelectedOne() {
		return this.isIterationTrackingEnabled && this.table.getSelectedRowCount() == 1;
	}
	
	private boolean canViewSelectedIterationLog() {
		if (!this.isIterationTrackingEnabled) {
			return false;
		}
		
		if (this.table.getSelectedRowCount() > 0) {
			for (Integer rowIndex : this.table.getSelectedRows()) {
				if (table.getValueAt(rowIndex, ITERATION_WINDOW_COLUMN.STATUS.getIndex()) == null) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}	
	
	public boolean canViewLogStartingFromTheSelectedIteration() {	
		return this.isIterationTrackingEnabled && table.getSelectedRowCount() == 1 && table.getValueAt(table.getSelectedRow(), ITERATION_WINDOW_COLUMN.STATUS.getIndex()) != null;
	}

	private void init() throws Exception
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.setTitle("Iteration selection");
		this.getContentPane().setLayout(new BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setIconImage(GUIUtils.getImageIcon(this.getClass(), BuildMain.getApplicationIconPath()).getImage());
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
			}
		});
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
	        @Override
	        public boolean dispatchKeyEvent(KeyEvent e) {
	        	if (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //esc
					setVisible(false);
				}
	            return false;
	        }
        });

		this.getContentPane().add(northPanel, BorderLayout.NORTH);
		this.getContentPane().add(centerPanel, BorderLayout.CENTER);
		this.getContentPane().add(southPanel, BorderLayout.SOUTH);
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

		class WindowsExplorerStyleJTableCustomized extends WindowsExplorerStyleJTable {

			/**
			 *
			 */
			private static final long serialVersionUID = -7202803705241678363L;

			class CustomizedKeyListener extends DefaultKeyListener {

				@Override
				protected boolean isTargetRow(final int rowIndex) {
					final int iterationNumberColumnIndex = 1;
					Assert.isTrue(getValueAt(rowIndex, iterationNumberColumnIndex) instanceof String, "The column must be a string column");
					return String.valueOf(getValueAt(rowIndex, iterationNumberColumnIndex)).replace(CoreConstants.ITERATION_SEPARATOR, "").trim().startsWith(typedCharacters.toString());
				}
			}

			public WindowsExplorerStyleJTableCustomized() {
				super();
				addMouseMotionListener(new DefaultMouseMotionListener());
				addMouseListener(new DefaultMouseListener());
				addKeyListener(new CustomizedKeyListener());
			}

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, final int columnIndex)
			{
				Component comp = super.prepareRenderer(renderer, rowIndex, columnIndex);
				return comp;
			}
		}

		table = new WindowsExplorerStyleJTableCustomized() {
            private static final long serialVersionUID = 1L;
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, final int columnIndex) {
				Component comp = super.prepareRenderer(renderer, rowIndex, columnIndex);
				if (rowIndex < ((DefaultTableModel) getModel()).getRowCount()) {
					String status = (String) ((DefaultTableModel) getModel()).getValueAt(rowIndex, ITERATION_WINDOW_COLUMN.STATUS.getIndex());
					final boolean isSelectedRow = table.getSelectionModel().isSelectedIndex(rowIndex);
					if (SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE.equals(status)) {
						comp.setBackground(isSelectedRow?SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_SELECTED_COLOR:SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_UNSELECTED_COLOR);
					} else if (SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE.equals(status)) {
						comp.setBackground(isSelectedRow?SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_SELECTED_COLOR:SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_UNSELECTED_COLOR);
					} else if (SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE.equals(status)) {
						comp.setBackground(isSelectedRow?SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_SELECTED_COLOR:SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_UNSELECTED_COLOR);
					}
				}
	
				return comp;
			}
		
	        @Override
	        protected List<JMenuItem> getRightClickMenuItems(final MouseEvent e) {
	        	List<JMenuItem> output = super.getRightClickMenuItems(e);
	        	
	        	JMenuItem viewSelectedIterations = new JMenuItem("View Selected Iteration(s)");
	        	viewSelectedIterations.addActionListener(new ActionListener() {
	
					@Override
					public void actionPerformed(ActionEvent e) {
						try {						
							final List<Integer> selectedIterations = new ArrayList<Integer>();
							for (Integer rowIndex : table.getSelectedRows()) {
								selectedIterations.add(Utils.retrieveIterationNumberFromIterationText(String.valueOf(table.getValueAt(rowIndex, ITERATION_WINDOW_COLUMN.NUMBER.getIndex()))));
							}
							

							File outputFile = InjectorsPackageUtils.processSpecificIterations(
									BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), 
									true,
									injectorOfDisplayedIterations, 
									selectedIterations, 
									InjectorsPackageUtils.PartitionBoundaryAppendingDecision.getDefaultInstance(),
									tokenReplacementProcessor, 
									new InjectorsPackageUtils.RawScriptOutputProcessor(injectorOfDisplayedIterations, "subset",InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked()),
									new MutableInt(0)
									);
							
							if (outputFile != null) {
								if (INJECTOR_TYPE.TYPE_HTMLFORM.equals(injectorOfDisplayedIterations.getType()) || INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injectorOfDisplayedIterations.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injectorOfDisplayedIterations.getType())) {
									if (BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked()) {
										FileUtils.startSourceViewerBrowser(Config.getBrowserViewSource(), outputFile.getAbsoluteFile());
									} else {
										FileUtils.startTextEditor(Config.getCmdTextEditor(), outputFile.getAbsoluteFile());
									}
									
								} else {
									FileUtils.startTextEditor(Config.getCmdTextEditor(), outputFile);
								}								
							}
							
						} catch (Throwable tr) {
							FileUtils.printStackTrace(tr);
							GUIUtils.popupErrorMessage("Error: "+CoreUtil.getAllThrowableMessages(tr));
						}
						
					}
	        		
	        	});
	        	viewSelectedIterations.setEnabled(canViewSelectedIterations());
	        	output.add(viewSelectedIterations);
	        	
	        	JMenuItem viewSelectedIterationLog = new JMenuItem("View Log of The Selected Iteration(s)");
	        	viewSelectedIterationLog.addActionListener(new ActionListener() {
	
					@Override
					public void actionPerformed(ActionEvent e) {	
						try {
							final LinkedHashMap<Long, List<Integer>> selectedActionManagerIdAndIterations = new LinkedHashMap<Long, List<Integer>>();
							for (Integer rowIndex : table.getSelectedRows()) {
								int iterationNumber = Utils.retrieveIterationNumberFromIterationText(String.valueOf(table.getValueAt(rowIndex, ITERATION_WINDOW_COLUMN.NUMBER.getIndex())));
								long actionManagerId = injectorIterationStatus.get(injectorOfDisplayedIterations).get(iterationNumber).getActionManagerId();
								if (!selectedActionManagerIdAndIterations.containsKey(actionManagerId)) {
									selectedActionManagerIdAndIterations.put(actionManagerId, new ArrayList<Integer>());
								}
								selectedActionManagerIdAndIterations.get(actionManagerId).add(iterationNumber);
							}
							
							final List<File> outputFiles = new ArrayList<File>();
							for (final Map.Entry<Long, List<Integer>> selectedActionManagerIdAndIterationsEntry : selectedActionManagerIdAndIterations.entrySet()) {
								
								final LinkedHashSet<File> logsUniqueOrdered = new LinkedHashSet<File>();
								for (final int iterationNumber : selectedActionManagerIdAndIterationsEntry.getValue()) {
									logsUniqueOrdered.addAll(injectorIterationStatus.get(injectorOfDisplayedIterations).get(iterationNumber).getLogFileForViewingSingletonList());
								}					
								
								
								final InjectorsPackageUtils.PartitionBoundaryAppendingDecision partitionBoundaryAppendingDecision = new InjectorsPackageUtils.PartitionBoundaryAppendingDecision() {
									
									private Set<Integer> allIterationNumbersWhichHaveBeenFound = new HashSet<Integer>();
									
									@Override
									public boolean appendPartitionBoundary(Set<Integer> iterationNumbersFound) {
										allIterationNumbersWhichHaveBeenFound.addAll(iterationNumbersFound);
										for (Integer selectedIteration : selectedActionManagerIdAndIterationsEntry.getValue()) {
											if (!allIterationNumbersWhichHaveBeenFound.contains(selectedIteration)) {
												return true;
											}
										}
										return false;
									}
								};
								
								if (actionManager != null) {
									actionManager.getLogCopier().copy(false);
								}
								
								final MutableInt numberOfLines = new MutableInt(0);
								
								for (final File log : logsUniqueOrdered) {
									if (log == null || !log.isFile()) {
										continue;
									}
									File outputFile = InjectorsPackageUtils.processSpecificIterations(
											log, 
											false,
											injectorOfDisplayedIterations, 
											selectedActionManagerIdAndIterationsEntry.getValue(), 
											partitionBoundaryAppendingDecision, 
											InjectorsPackageUtils.TokenReplacementProcessor.getDefaultInstance(), 
											new InjectorsPackageUtils.RawLogOutputProcessor(injectorOfDisplayedIterations, log, InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked()),
											numberOfLines
											);
									if (outputFile != null) {
										outputFiles.add(outputFile);
									}
								}
							}
							SwiftBuildFileUtils.viewMergedLogs(injectorOfDisplayedIterations, outputFiles, BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked());							
						} catch(Throwable tr) {
							FileUtils.printStackTrace(tr);
							GUIUtils.popupErrorMessage("Error: "+CoreUtil.getAllThrowableMessages(tr));
						}

					}
	        		
	        	});
	        	viewSelectedIterationLog.setEnabled(canViewSelectedIterationLog());
	        	output.add(viewSelectedIterationLog);
	        	
	        	JMenuItem viewAllIterationsStartingFromTheSelectedOne = new JMenuItem("View All Iterations Starting From This One");
	        	viewAllIterationsStartingFromTheSelectedOne.addActionListener(new ActionListener() {
	
					@Override
					public void actionPerformed(ActionEvent e) {					
						try {
							final MutableInt startingIteration = new MutableInt();
							final Set<Integer> expectedIterations = new HashSet<Integer>();
							for (int i = table.getSelectedRow() ; i < table.getRowCount() ; i++) {
								int iterationNumber = Utils.retrieveIterationNumberFromIterationText(String.valueOf(table.getValueAt(i, ITERATION_WINDOW_COLUMN.NUMBER.getIndex())));
								if (i == table.getSelectedRow()) {
									startingIteration.setValue(iterationNumber);
								}
								expectedIterations.add(iterationNumber);
							}
							File outputFile = InjectorsPackageUtils.processSpecificIterations(
									BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), 
									true,
									injectorOfDisplayedIterations, 
									expectedIterations, 
									InjectorsPackageUtils.PartitionBoundaryAppendingDecision.getDefaultInstance(),
									tokenReplacementProcessor, 
									new InjectorsPackageUtils.RawScriptOutputProcessor(injectorOfDisplayedIterations, "subset-from-iter-"+startingIteration.getValue(), InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked()),
									new MutableInt(0));
							
							if (outputFile != null) {
								if (INJECTOR_TYPE.TYPE_HTMLFORM.equals(injectorOfDisplayedIterations.getType()) || INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injectorOfDisplayedIterations.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injectorOfDisplayedIterations.getType())) {
									if (BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked()) {
										FileUtils.startSourceViewerBrowser(Config.getBrowserViewSource(), outputFile.getAbsoluteFile());
									} else {
										FileUtils.startTextEditor(Config.getCmdTextEditor(), outputFile.getAbsoluteFile());
									}
									
								} else {
									FileUtils.startTextEditor(Config.getCmdTextEditor(), outputFile);
								}								
							}
	
						} catch (Throwable tr) {
							FileUtils.printStackTrace(tr);
							GUIUtils.popupErrorMessage("Error: "+CoreUtil.getAllThrowableMessages(tr));
						}
						
					}
	        		
	        	});
	        	viewAllIterationsStartingFromTheSelectedOne.setEnabled(canViewAllIterationsStartingFromTheSelectedOne());
	        	output.add(viewAllIterationsStartingFromTheSelectedOne);
	        	
	        	JMenuItem viewAllLogsStartingFromTheSelectedOne = new JMenuItem("View Log Starting From This Iteration");
	        	viewAllLogsStartingFromTheSelectedOne.addActionListener(new ActionListener() {
	
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							final MutableInt selectedIteration = new MutableInt(
									Utils.retrieveIterationNumberFromIterationText(String.valueOf(table.getValueAt(table.getSelectedRow(), ITERATION_WINDOW_COLUMN.NUMBER.getIndex())))
									);		
							final Set<Integer> expectedIterations = new HashSet<Integer>();
							final LinkedHashMap<Long, List<Integer>> expectedActionManagersAndIterations = new LinkedHashMap<Long, List<Integer>>();
							for (int i = 0 ; i < table.getRowCount() ; i++) {
								int iterationNumber = Utils.retrieveIterationNumberFromIterationText(String.valueOf(table.getValueAt(i, ITERATION_WINDOW_COLUMN.NUMBER.getIndex())));
								if (iterationNumber >= selectedIteration.getValue() && table.getValueAt(i, ITERATION_WINDOW_COLUMN.STATUS.getIndex()) != null) {
									expectedIterations.add(iterationNumber);
									long actionManagerId = injectorIterationStatus.get(injectorOfDisplayedIterations).get(iterationNumber).getActionManagerId();
									if (!expectedActionManagersAndIterations.containsKey(actionManagerId)) {
										expectedActionManagersAndIterations.put(actionManagerId, new ArrayList<Integer>());
									}
									expectedActionManagersAndIterations.get(actionManagerId).add(iterationNumber);					
									
								}
							}	
							
							final List<File> outputFiles = new ArrayList<File>();
							for (final Map.Entry<Long, List<Integer>> selectedActionManagersAndIterationsEntry : expectedActionManagersAndIterations.entrySet()) {
								final LinkedHashSet<File> logsUniqueOrdered = new LinkedHashSet<File>();
								for (final int iterationNumber : selectedActionManagersAndIterationsEntry.getValue()) {
									logsUniqueOrdered.addAll(injectorIterationStatus.get(injectorOfDisplayedIterations).get(iterationNumber).getLogFileForViewingSingletonList());
								}
								final File[] logs = logsUniqueOrdered.toArray(new File[logsUniqueOrdered.size()]);
								
								if (actionManager != null) {
									actionManager.getLogCopier().copy(false);
								}
								
								final MutableInt numberOfLines = new MutableInt(0);
								for (int i = 0 ; i < logs.length ; i++) {
									final int iConst = i;
									final File log = logs[i];
									
									if (log == null || !log.isFile()) {
										continue;
									}
									
									File outputFile = InjectorsPackageUtils.processSpecificIterations(
											log, 
											false,
											injectorOfDisplayedIterations, 
											selectedActionManagersAndIterationsEntry.getValue(),
											new InjectorsPackageUtils.PartitionBoundaryAppendingDecision() {
												
												@Override
												public boolean appendPartitionBoundary(Set<Integer> iterationNumbersFound) {
													return iConst != logs.length-1;
												}
											},
											InjectorsPackageUtils.TokenReplacementProcessor.getDefaultInstance(), 
											new InjectorsPackageUtils.RawLogOutputProcessor(injectorOfDisplayedIterations, log, InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked()),
											numberOfLines);
									if (outputFile != null) {
										outputFiles.add(outputFile);
									}							
								}
							}	
							SwiftBuildFileUtils.viewMergedLogs(injectorOfDisplayedIterations, outputFiles, BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked());						
						} catch (Throwable tr) {
							FileUtils.printStackTrace(tr);
							GUIUtils.popupErrorMessage("Error: "+CoreUtil.getAllThrowableMessages(tr));
						}

					}
	        		
	        	});
	        	viewAllLogsStartingFromTheSelectedOne.setEnabled(canViewLogStartingFromTheSelectedIteration());
	        	output.add(viewAllLogsStartingFromTheSelectedOne);        
	        	
	        	
	        	return output;
	        }			
		};


		initJTable();
		JScrollPane scrollPane = new JScrollPane(table);
		table.setScrollPane(scrollPane);
		scrollPane.setPreferredSize(new Dimension(500, 550));
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		label = new JLabel();
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setAlignmentY(Component.LEFT_ALIGNMENT);
		label.setText("Selected line: ");
		label.setOpaque(true);
		southPanel.add(Box.createRigidArea(new Dimension(20, 20)));

		JPanel tempPanel = new JPanel();
		tempPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		southPanel.add(tempPanel);

		button = GUIUtils.getButton(BuildMain.getClass(), "Start", "/images/play16.gif");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				final Set<Integer> executableIterations = new HashSet<Integer>();
				for (int i = 0 ; i < table.getRowCount(); i++) {
					if (Boolean.TRUE.equals(table.getValueAt(i, ITERATION_WINDOW_COLUMN.SELECTION.getIndex()))) {
						executableIterations.add(Utils.retrieveIterationNumberFromIterationText(String.valueOf(table.getValueAt(i, ITERATION_WINDOW_COLUMN.NUMBER.getIndex()))));
					}
				}
				if ( executableIterations.isEmpty() )
				{
					GUIUtils.popupErrorMessage("You must pick at least an iteration");
				}
				else
				{
					boolean isInjectionRunning = BuildMain.getInjectorsExecutionPanel().isInjectionRunning();
					if ( isInjectionRunning )
					{
						GUIUtils.popupErrorMessage("You must stop the injection before restarting" + " the injector.");
					}
					else
					{
						setExecutableIterations(executableIterations);
						start();
					}
				}
			}
		});
		tempPanel.add(button);
		tempPanel.add(Box.createRigidArea(new Dimension(20, 20)));

		this.setSize(550, 600);
		this.setVisible(false);
		this.pack();
	}

	public void initJTable()
	{
		tableModel = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int column)
			{
				Object valueAt = getValueAt(0, column);
				if ( valueAt != null )
				{
					return valueAt.getClass();
				}
				return super.getClass();
			}

			public boolean isCellEditable(int rowIndex, int mColIndex)
			{
				return mColIndex == 0?true:false;
			}
		};
		for (ITERATION_WINDOW_COLUMN column : ITERATION_WINDOW_COLUMN.getSorted()) {
			tableModel.addColumn(column.getHeader());
		}
		table.setModel(tableModel);
		table.getModel().addTableModelListener(this);
		for (ITERATION_WINDOW_COLUMN column : ITERATION_WINDOW_COLUMN.getSorted()) {
			table.getColumnModel().getColumn(column.getIndex()).setPreferredWidth(column.getWidth());
		}

		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(ITERATION_WINDOW_COLUMN.STATUS.getIndex()).setCellRenderer(dtcr);
		table.getColumnModel().getColumn(ITERATION_WINDOW_COLUMN.TIME.getIndex()).setCellRenderer(dtcr);
	}

	public void start()
	{
		Assert.notNull(executableIterations);
		BuildMain.getInjectorsExecutionPanel().startExecution();
	}

	public boolean isExecutingIterations()
	{
		return executableIterations!=null;
	}

	private synchronized void clearTable()
	{
		int numrows = tableModel.getRowCount();
		for ( int i = numrows - 1; i >= 0; i-- )
		{
			tableModel.removeRow(i);
		}
	}
	
	public void setupInjector(Injector injector, final boolean forceShow, final ActionManager actionManager) throws Exception
	{
		this.actionManager = actionManager;
		
		if (INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()) 
				|| INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) 
				|| INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) {
			this.isIterationTrackingEnabled = InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile());
		} else {
			this.isIterationTrackingEnabled = false;
		}
		

		this.setTitle("Iteration selection: " + injector.getName());
		if ( injector.getType() == null
				|| !(CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType())
						|| CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())
						|| CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))) {
			FileUtils.println("Unable to launch Iteration selection window as the injector type" + " is not FLD or IL: injector Name: '" + injector.getName()
					+ "' injector Type: '" + injector.getType() + "'");
			return;
		}
		label.setText("Selected line:");
		label.setBackground(Color.LIGHT_GRAY);
		clearTable();
		addInjectorLinesToTable(injector);

		if (this.injectorIterationStatus.containsKey(injector)) {
			for (int i = 0 ; i < table.getRowCount() ; i++) {
				final String iterationNumberText = table.getValueAt(i, ITERATION_WINDOW_COLUMN.NUMBER.getIndex()).toString();
				final int iterationNumber = Utils.retrieveIterationNumberFromIterationText(iterationNumberText);

				if (this.injectorIterationStatus.get(injector).containsKey(iterationNumber)) {
					table.setValueAt(injectorIterationStatus.get(injector).get(iterationNumber).getStatus(), i, ITERATION_WINDOW_COLUMN.STATUS.getIndex());
					table.setValueAt(CoreConstants.DATE_FORMAT_PATTERN.STANDARD.getDateFormat().format(injectorIterationStatus.get(injector).get(iterationNumber).getLastTimestamp()), i, ITERATION_WINDOW_COLUMN.TIME.getIndex());
				}
			}
		}

		this.pack();
		if (forceShow) {
			this.setVisible(true);
		}
		this.injectorOfDisplayedIterations = injector;
		
		this.tokenReplacementProcessor = new InjectorsPackageUtils.TokenReplacementProcessor() {

			@Override
			public InputStream replaceTokens(InputStream rawInputStream) {
				if (SwiftBuildFileUtils.hasReplacementTokens(BuildMain.getEnvironmentProperties())) {
					return InjectorsManager.replaceTokensInScript(rawInputStream, SwiftBuildFileUtils.getReplacementTokens(BuildMain.getEnvironmentProperties()), injectorOfDisplayedIterations.getNameWithoutExtension());
				} else {
					return rawInputStream;
				}
			}
    		
    	};		
	}

	public void tableChanged(TableModelEvent e)
	{
		// nothing to do
	}

	private void addInjectorLinesToTable(final Injector injector) throws Exception
	{
		Assert.notNull(injector, "injector must not be null");
		final String injectorName = injector.getName();
		InputStream inputStream = null;
		ZipFile zipFile = null;
		try
		{
			if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()) )
			{
				inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), injectorName);
			}
			else
			{
				zipFile = new ZipFile(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile());
				inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, injectorName);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String strLine;

			boolean thisFrameInjectorIsIntendedToBeExecuted = false;

			if (BuildMain.getInjectorsExecutionPanel().getCurrentInjectorIndex()!= null &&
					BuildMain.getInjectorsExecutionPanel().getCurrentInjectorIndex().intValue() == injector.getIndex()) {
				thisFrameInjectorIsIntendedToBeExecuted = true;
			}

			while ( (strLine = br.readLine()) != null )
			{
				if ( strLine.startsWith(CoreConstants.ITERATION_SEPARATOR) )
				{
					boolean checkboxIsTicked = true;
					if (executableIterations != null && thisFrameInjectorIsIntendedToBeExecuted) {
						checkboxIsTicked = executableIterations.contains(Utils.retrieveIterationNumberFromIterationText(strLine));
					}
					tableModel.addRow(new Object[] {checkboxIsTicked, strLine, null, null });
				}
			}
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
			if ( zipFile != null )
			{
				zipFile.close();
			}
			table.repaint();
		}
	}

	public void reset()
	{
		executableIterations = null;
	}

	public Set<Integer> getExecutableIterations() {
		return executableIterations;
	}

	private void setExecutableIterations(Set<Integer> executableIterations) {
		this.executableIterations = executableIterations;
	}

	public Integer getNextIterationToExecute(final int currentIteration) {
		if (executableIterations == null) {
			throw new RuntimeException("There is no iteration selected");
		} else {
			final List<Integer> sortedList = new ArrayList<Integer>(executableIterations);
			Collections.sort(sortedList);
			Integer output = null;
			for (final Integer i : sortedList) {
				if (i > currentIteration) {
					output = i;
					break;
				}
			}
			return output;
		}
	}

	public void tickAndSetStartingFromIteration(final int startingIterationNumber) {
		final Set<Integer> executableIterations = new HashSet<Integer>();
		for (int i = 0 ; i < table.getRowCount() ; i++) {
			final int thisRowIterationNumber = Utils.retrieveIterationNumberFromIterationText(String.valueOf(table.getValueAt(i, 1)));
			final boolean includeThisIteration = thisRowIterationNumber >= startingIterationNumber;
			if (includeThisIteration) {
				executableIterations.add(thisRowIterationNumber);
			}
			this.table.setValueAt(includeThisIteration, i, ITERATION_WINDOW_COLUMN.SELECTION.getIndex());
		}
		this.setExecutableIterations(executableIterations);
	}

	public void deleteExecutableIterations(boolean keepOldVisibility) {
		this.setExecutableIterations(null);
		if (!keepOldVisibility) {
			this.setVisible(false);
		}
	}
	
	public List<File> getLogFiles(final Injector injector, final List<Integer> iterationNumbers) {
		if (!this.injectorIterationStatus.containsKey(injector)) {
			return new ArrayList<File>();
		}
		
		List<File> output = new ArrayList<File>();
		for (final Integer iterationNumber : iterationNumbers) {
			if (this.injectorIterationStatus.get(injector).containsKey(iterationNumber) 
					&& this.injectorIterationStatus.get(injector).get(iterationNumber).getLogFileForViewing() != null
					&& !output.contains(this.injectorIterationStatus.get(injector).get(iterationNumber).getLogFileForViewing())) {
				output.add(this.injectorIterationStatus.get(injector).get(iterationNumber).getLogFileForViewing());
			}
		}
		return output;
	}	

	public void updateStatus(final Injector injector, final int iterationNumber, final String status, final long actionManagerId, final File logFileForViewing) {
		if (!this.injectorIterationStatus.containsKey(injector)) {
			this.injectorIterationStatus.put(injector, new HashMap<Integer, IterationStatus>());
		}
		final Date now = new Date();
		IterationStatus iterationStatus;
		if (this.injectorIterationStatus.get(injector).containsKey(iterationNumber)) {
			iterationStatus = new IterationStatus(status, now, actionManagerId, logFileForViewing);
		} else {
			iterationStatus = new IterationStatus(status, now, actionManagerId, logFileForViewing);
		}
		this.injectorIterationStatus.get(injector).put(iterationNumber, iterationStatus);

		if (injector.equals(this.injectorOfDisplayedIterations)) {
			for (int i = 0 ; i < this.table.getRowCount() ; i++) {
				final int thisRowIterationNumber = Utils.retrieveIterationNumberFromIterationText(this.table.getValueAt(i, ITERATION_WINDOW_COLUMN.NUMBER.getIndex()).toString());
				if (thisRowIterationNumber == iterationNumber) {
					table.getModel().setValueAt(status, i, ITERATION_WINDOW_COLUMN.STATUS.getIndex());
					table.getModel().setValueAt(CoreConstants.DATE_FORMAT_PATTERN.STANDARD.getDateFormat().format(now), i, ITERATION_WINDOW_COLUMN.TIME.getIndex());
					table.getModel().setValueAt(false, i, ITERATION_WINDOW_COLUMN.SELECTION.getIndex());

					break;
				}
			}
		}
	}	
}