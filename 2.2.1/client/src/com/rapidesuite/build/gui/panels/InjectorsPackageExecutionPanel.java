/**************************************************************
 * $Revision: 61253 $:
 * $Author: hassan.jamil $:
 * $Date: 2017-03-03 09:47:38 +0700 (Fri, 03 Mar 2017) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/panels/InjectorsPackageExecutionPanel.java $:
 * $Id: InjectorsPackageExecutionPanel.java 61253 2017-03-03 02:47:38Z hassan.jamil $:
 **************************************************************/
package com.rapidesuite.build.gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.util.Assert;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.action.APIAction;
import com.rapidesuite.build.core.action.ActionInterface;
import com.rapidesuite.build.core.action.SQLAction;
import com.rapidesuite.build.core.action.SSHAction;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.controller.InjectorsManager;
import com.rapidesuite.build.gui.CheckBoxRenderer;
import com.rapidesuite.build.gui.EachRowEditor;
import com.rapidesuite.build.gui.EachRowRenderer;
import com.rapidesuite.build.gui.WindowsExplorerStyleJTable;
import com.rapidesuite.build.gui.apigrid.APIDataGridUtils;
import com.rapidesuite.build.gui.frames.InjectorIterationSelectionFrame;
import com.rapidesuite.build.utils.CurrentBrowserTask;
import com.rapidesuite.build.utils.HasIterationCountAndFileSizeColumns;
import com.rapidesuite.build.utils.InjectorsPackageUtils;
import com.rapidesuite.build.utils.IterationCountAndHumanReadableFileSize;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.gui.CustomFileFilter;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.CoreConstants.INJECTOR_TYPE;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.Encryption;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class InjectorsPackageExecutionPanel extends JPanel implements HasIterationCountAndFileSizeColumns
{

	private BuildMain BuildMain;
	InjectorIterationSelectionFrame injectorIterationSelectionFrame;
	private JFileChooser saveFileChooser;

	JButton startButton;
	private JButton stopButton;
	private JButton openLogsFolderButton;
	private JButton clearLogButton;
	private JButton saveLogButton;
	private JButton previousButton;

	private JPanel northPanel;
	private JPanel centerPanel;
	
	private JLabel currentInjectorLabel;
	private JLabel injectorCountLabel;
	private JLabel currentInjectorNameLabel;
	private JLabel injectorCountValueLabel;

	private JScrollPane scrollPane;
	private JScrollPane scrollPaneLogArea;
	private WindowsExplorerStyleJTableCustomized table;
	private DefaultTableModel model;
	private CheckBoxRenderer checkBoxRenderer;
	private DefaultCellEditor checkBoxEditor;
	private EachRowRenderer rowRenderer;
	private EachRowEditor rowEditor;
	private JProgressBar progressBar;
	private JCheckBox pauseCheckBox;
	private JTextArea logTextArea;

	private ActionManager actionManager;
	private List<Injector> injectors;
	private Map<Integer, ActionInterface> injectorIndexToActionMap;
	private Map<Injector, Boolean> injectorsAndIterationMarkerIsActive;
	
	private JCheckBox buildOpenScriptAndLogInHtmlByDefault = null;
	
//	private List<File> convertedInjectorToHTMLList;
	
	private class SuccessfulAndFailedIterationsForEachSession {
		private final Map<Injector, Set<Integer>> successfulIterations;	
		private final Map<Injector, Set<Integer>> failedIterations;
		
		public SuccessfulAndFailedIterationsForEachSession(final Map<Injector, Set<Integer>> successfulIterations, final Map<Injector, Set<Integer>> failedIterations) {
			this.successfulIterations = successfulIterations;
			this.failedIterations = failedIterations;
		}
		
		public boolean hasSuccessfulIteration(final Injector injector) {
			return successfulIterations.containsKey(injector) && !successfulIterations.get(injector).isEmpty();
		}
		
		public boolean hasFailedIteration(final Injector injector) {
			return failedIterations.containsKey(injector) && !failedIterations.get(injector).isEmpty();
		}
		
		public boolean hasInjected(final Injector injector) {
			return hasSuccessfulIteration(injector) || hasFailedIteration(injector);
		}
		
		public Map<Injector, Set<Integer>> getSuccessfulIterations() {
			return this.successfulIterations;
		}
		
		public Map<Injector, Set<Integer>> getFailedIterations() {
			return this.failedIterations;
		}		
	}
	
	private List<SuccessfulAndFailedIterationsForEachSession> successfulAndFailedIterationsForAllSessions;	
	private List<Map<Integer, List<String>>> injectorIndexToPartitionNameMapForAllSessions;

	private Map<Injector, File> logMismatchesLog;

	private static enum INJECTION_SCREEN_COLUMN {
		SELECTION(0, "", new BigDecimal("0.025"), null),
		INJECTOR_NAME(1, "NAME", new BigDecimal("0.225"), null),
		INJECTOR_TYPE(2, "TYPE", new BigDecimal("0.055"), null),
		SUCCESS(3, "SUCC", new BigDecimal("0.06"), "Number of successful iterations"),
		FAILED(4, "FAIL", new BigDecimal("0.06"), "Number of failed iterations"),
		REMAINING(5, "RMI", new BigDecimal("0.06"), "Number of remaining iterations"),
		SELECTED(6, "CSI", new BigDecimal("0.05"), "Number of current selection iterations"),
		TOTAL(7, "TOTAL", new BigDecimal("0.05"), "Total number of iterations"),
		LAST_SUCCESSFUL(8, "LSI#", new BigDecimal("0.05"), "The number of the last successful iteration"),
		LAST_FAILED(9, "LFI#", new BigDecimal("0.05"), "The number of the last failed iteration"),
		PARTITIONS_TRACKER(10, "PART", new BigDecimal("0.05"), "Partition number / total number of partition"),
		FILE_SIZE(11, "SIZE", new BigDecimal("0.05"), "File size of the injector"),
		STATUS(12, "STATUS", new BigDecimal("0.075"), "Injection status"),
		EXECUTION_TIME(13, "TIME", new BigDecimal("0.14"), "Progressing execution time");

		private final int index;
		private final String header;
		private final BigDecimal proportion;
		private final String toolTip;

		private INJECTION_SCREEN_COLUMN(final int index, final String header, final BigDecimal proportion, final String toolTip) {
			this.index = index;
			this.header = header;
			this.proportion = proportion;
			this.toolTip = toolTip;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public String toString() {
			return header;
		}

		public static INJECTION_SCREEN_COLUMN[] getSorted() {
			List<INJECTION_SCREEN_COLUMN> list = Arrays.asList(INJECTION_SCREEN_COLUMN.values());
			Collections.sort(list, new Comparator<INJECTION_SCREEN_COLUMN>() {
				@Override
				public int compare(INJECTION_SCREEN_COLUMN o1,
						INJECTION_SCREEN_COLUMN o2) {
					if (o1.getIndex() < o2.getIndex()) {
						return -1;
					} else if (o1.getIndex() > o2.getIndex()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			return list.toArray(new INJECTION_SCREEN_COLUMN[INJECTION_SCREEN_COLUMN.values().length]);
		}

		public double getProportion() {
			return proportion.doubleValue();
		}

		public BigDecimal getProportionExact() {
			return proportion;
		}
		
		public String getToolTip() {
			return this.toolTip;
		}
	}

	static {
		INJECTION_SCREEN_COLUMN[] columns = INJECTION_SCREEN_COLUMN.getSorted();
		BigDecimal totalProportion = new BigDecimal(0);
		for (int i = 0 ; i < columns.length ; i++) {
			Assert.isTrue(i == columns[i].getIndex(), "INJECTION_SCREEN_COLUMNS columns are not ordered");
			totalProportion = totalProportion.add(columns[i].getProportionExact());
		}
		Assert.isTrue(BigDecimal.ONE.compareTo(totalProportion) == 0, "Build injection screen column proportions sum to "+totalProportion.toPlainString()+" while they should sum to 1");
	}

	class WindowsExplorerStyleJTableCustomized extends WindowsExplorerStyleJTable {
		public WindowsExplorerStyleJTableCustomized() {
			super(model);
			addMouseMotionListener(new DefaultMouseMotionListener());
			Assert.isTrue(INJECTION_SCREEN_COLUMN.INJECTOR_NAME.toString().equals(model.getColumnName(INJECTION_SCREEN_COLUMN.INJECTOR_NAME.getIndex())), "the column must be injector column name");
			addKeyListener(new DefaultKeyListener(INJECTION_SCREEN_COLUMN.INJECTOR_NAME.getIndex()));
			addMouseListener(new CustomizedMouseListener());
		}

		class CustomizedMouseListener extends DefaultMouseListener {

			@Override
			public void mousePressed(MouseEvent e)
			{
				super.mousePressed(e);

				final Point p = e.getPoint();
				final int row = table.rowAtPoint(p);

				if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
					popupIterationSelection(row);
				}
			}
		}

		private void popupIterationSelection(final int row) {
			if (hasIteration(row)) {
				try
				{
					injectorIterationSelectionFrame.setupInjector(injectors.get(row), true, actionManager);
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		}

		@Override
		protected List<JMenuItem> getRightClickMenuItems(final MouseEvent e) {
			final JTable table = (JTable) e.getSource();
			final Point p = e.getPoint();
			final int row = table.rowAtPoint(p);

			final Injector injector = injectors.get(row);

			final List<JMenuItem> output = new ArrayList<JMenuItem>();

			final JMenuItem iterationSelectionItem = new JMenuItem("Iteration Selection");
			iterationSelectionItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					popupIterationSelection(row);
				}
			});

			boolean iterationSelectionEnabled;
			if (table.getSelectedRowCount() > 1) {
				iterationSelectionEnabled = false;
			} else {
				try {
					iterationSelectionEnabled = hasIteration(row);
				} catch (Exception e2) {
					FileUtils.printStackTrace(e2);
					iterationSelectionEnabled = false;
				}
			}

			iterationSelectionItem.setEnabled(iterationSelectionEnabled);

			output.add(iterationSelectionItem);


			String viewInjectionResultText = null;
			if ((CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()))
					|| CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_SQL.equals(injector.getType())) {
				viewInjectionResultText = "View Log File";
			} else if ( CoreConstants.INJECTOR_TYPE.TYPE_TELNET.equals(injector.getType()) ) {
				viewInjectionResultText = "View Terminal";
			} else if ( CoreConstants.INJECTOR_TYPE.TYPE_API.equals(injector.getType()) ) {
				viewInjectionResultText = "View Data Grid";
			}

			if (viewInjectionResultText != null) {
				final JMenuItem viewInjectionOutputItem = new JMenuItem(viewInjectionResultText);
				viewInjectionOutputItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						viewInjectionOutput(row);
					}

				});

				boolean viewInjectionResultEnabled = true;

				if (table.getSelectedRowCount() > 1) {
					viewInjectionResultEnabled = false;
				} else {
					if ( (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) || (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType())) )
					{
						viewInjectionResultEnabled = getPartitionNamesTheLastTimeTheInjectorWasExecuted(injector) != null && !getPartitionNamesTheLastTimeTheInjectorWasExecuted(injector).isEmpty();
					}
					else if ( CoreConstants.INJECTOR_TYPE.TYPE_API.equals(injector.getType()) )
					{
						try {
							APIAction apiAction = createNewAPIAction(BuildMain, null, injector, getBuildMain().getEnvironmentProperties(), BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile());
							if ( apiAction.getActionManager() != null && !apiAction.getActionManager().isExecutionStopped() )
							{
								viewInjectionResultEnabled = false;
							}
						} catch (Exception e1) {
							FileUtils.printStackTrace(e1);
							viewInjectionResultEnabled = false;
						}
					}
					else if ( CoreConstants.INJECTOR_TYPE.TYPE_TELNET.equals(injector.getType()) )
					{
						if (actionManager == null) {
							viewInjectionResultEnabled = false;
						}
					}
			        else if ( CoreConstants.INJECTOR_TYPE.TYPE_SQL.equals(injector.getType()) )
			        {
						try {
							if (!SwiftBuildFileUtils.getLogFile(BuildMain, injector.getName()).exists()) {
								viewInjectionResultEnabled = false;
							}
						} catch (Exception e1) {
							FileUtils.printStackTrace(e1);
							viewInjectionResultEnabled = false;
						}
			        } else {
			        	viewInjectionResultEnabled = false;
			        }
				}
				viewInjectionOutputItem.setEnabled(viewInjectionResultEnabled);
				output.add(viewInjectionOutputItem);
			}

			final JMenuItem viewFileItem = new JMenuItem("View File");
			viewFileItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						SwiftBuildFileUtils.showInjectorContent(BuildMain, injector, InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), InjectorsPackageExecutionPanel.this.isBuildOpenScriptAndLogInHtmlByDefaultChecked(), true);
					} catch (Exception e1) {
						FileUtils.printStackTrace(e1);
						GUIUtils.popupErrorMessage(CoreUtil.getAllThrowableMessages(e1));
					}
				}

			});
			viewFileItem.setEnabled(table.getSelectedRowCount()==1 &&
					!CoreConstants.INJECTOR_TYPE.TYPE_STATIC_HTML.equals(injector.getType()) &&
					!(CoreConstants.INJECTOR_TYPE.TYPE_SQL.equals(injector.getType()) && !Utils.hasAccessToInternalStaffsOnlyFeatures()));
			output.add(viewFileItem);



			JMenuItem viewLogMismatches = new JMenuItem("View Log Mismatches");
			viewLogMismatches.setEnabled(logMismatchesLog.containsKey(injector) && logMismatchesLog.get(injector).isFile());
			viewLogMismatches.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						openLogMismatchesLog(injector);
					} catch (Exception e1) {
						throw new Error(e1);
					}
				}

			});
			output.add(viewLogMismatches);
			
			final InjectorsPackageUtils.TokenReplacementProcessor tokenReplacementProcessor = new InjectorsPackageUtils.TokenReplacementProcessor() {

				@Override
				public InputStream replaceTokens(final InputStream rawInputStream) {
					if (SwiftBuildFileUtils.hasReplacementTokens(BuildMain.getEnvironmentProperties())) {
						return InjectorsManager.replaceTokensInScript(rawInputStream, SwiftBuildFileUtils.getReplacementTokens(BuildMain.getEnvironmentProperties()), injector.getNameWithoutExtension());
					} else {
						return rawInputStream;
					}
				}
				
			};
			
        	
        	JMenuItem viewTheLastSuccessfulIteration = new JMenuItem("View Last Successful Iteration");
        	viewTheLastSuccessfulIteration.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {				
					try {			
						List<Integer> iterationsToBeIncluded = new ArrayList<Integer>(getSuccessfulIterationsTheLastTimeTheInjectorWasExecuted(injector));
						Collections.sort(iterationsToBeIncluded);
						if (iterationsToBeIncluded.size() > Config.getBuildViewLastSuccessfulOrFailedIterationCount()) {
							iterationsToBeIncluded = 
									iterationsToBeIncluded.subList(
											iterationsToBeIncluded.size() - Config.getBuildViewLastSuccessfulOrFailedIterationCount(), 
											iterationsToBeIncluded.size());
						}
						
						final String fileNameSuffix;
						if (iterationsToBeIncluded.size() == 1) {
							fileNameSuffix = ""+iterationsToBeIncluded.get(0);
						} else {
							fileNameSuffix = iterationsToBeIncluded.get(0)+"-to-"+iterationsToBeIncluded.get(iterationsToBeIncluded.size()-1);
						}						
						
						File outputFile = InjectorsPackageUtils.processSpecificIterations(
								BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), 
								true, 
								injector, 
								iterationsToBeIncluded, 
								InjectorsPackageUtils.PartitionBoundaryAppendingDecision.getDefaultInstance(),
								tokenReplacementProcessor,
								new InjectorsPackageUtils.RawScriptOutputProcessor(injector, "subset-iter-"+fileNameSuffix, InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked()),
								new MutableInt(0)
								);
						
						if (outputFile != null) {
							if (INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) {
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
        	viewTheLastSuccessfulIteration.setEnabled(hasSuccessfulIterationTheLastTimeTheInjectorWasExecuted(injector));
        	output.add(viewTheLastSuccessfulIteration);	 
        	
        	JMenuItem viewTheLastSuccessfulIterationLog = new JMenuItem("View Last Successful Iteration Log");
        	viewTheLastSuccessfulIterationLog.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						List<Integer> iterationsToBeIncluded = new ArrayList<Integer>(getSuccessfulIterationsTheLastTimeTheInjectorWasExecuted(injector));
						Collections.sort(iterationsToBeIncluded);
						if (iterationsToBeIncluded.size() > Config.getBuildViewLastSuccessfulOrFailedIterationCount()) {
							iterationsToBeIncluded = 
									iterationsToBeIncluded.subList(
											iterationsToBeIncluded.size() - Config.getBuildViewLastSuccessfulOrFailedIterationCount(), 
											iterationsToBeIncluded.size());
						}
						final List<File> logs = injectorIterationSelectionFrame.getLogFiles(injector, iterationsToBeIncluded);

						final MutableInt numberOfLines = new MutableInt(0);
						List<File> outputFiles = new ArrayList<File>();
						for (int i = 0 ; i < logs.size() ; i++) {
							final File log = logs.get(i);
							File outputFile = InjectorsPackageUtils.processSpecificIterations(
									log, 
									false, 
									injector, 
									iterationsToBeIncluded,  
									InjectorsPackageUtils.PartitionBoundaryAppendingDecision.getDefaultInstance(),
									InjectorsPackageUtils.TokenReplacementProcessor.getDefaultInstance(),
									new InjectorsPackageUtils.RawLogOutputProcessor(injector, log, InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), InjectorsPackageExecutionPanel.this.isBuildOpenScriptAndLogInHtmlByDefaultChecked()),
									numberOfLines
									);
							if (outputFile != null) {
								outputFiles.add(outputFile);
							}					
						}
						SwiftBuildFileUtils.viewMergedLogs(injector, outputFiles, InjectorsPackageExecutionPanel.this.isBuildOpenScriptAndLogInHtmlByDefaultChecked());				
					
					} catch (Throwable tr) {
						FileUtils.printStackTrace(tr);
						GUIUtils.popupErrorMessage("Error: "+CoreUtil.getAllThrowableMessages(tr));						
					}
				}
        		
        	});
        	viewTheLastSuccessfulIterationLog.setEnabled(hasSuccessfulIterationTheLastTimeTheInjectorWasExecuted(injector));
        	output.add(viewTheLastSuccessfulIterationLog);
        	
        	JMenuItem viewTheLastFailedIteration = new JMenuItem("View Last Failed Iteration");
        	viewTheLastFailedIteration.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {				
					try {				
						List<Integer> iterationsToBeIncluded = new ArrayList<Integer>(getFailedIterationsTheLastTimeTheInjectorWasExecuted(injector));
						Collections.sort(iterationsToBeIncluded);
						if (iterationsToBeIncluded.size() > Config.getBuildViewLastSuccessfulOrFailedIterationCount()) {
							iterationsToBeIncluded = 
									iterationsToBeIncluded.subList(
											iterationsToBeIncluded.size() - Config.getBuildViewLastSuccessfulOrFailedIterationCount(), 
											iterationsToBeIncluded.size());
						}
						
						final String fileNameSuffix;
						if (iterationsToBeIncluded.size() == 1) {
							fileNameSuffix = ""+iterationsToBeIncluded.get(0);
						} else {
							fileNameSuffix = iterationsToBeIncluded.get(0)+"-to-"+iterationsToBeIncluded.get(iterationsToBeIncluded.size()-1);
						}
						
						
						File outputFile = InjectorsPackageUtils.processSpecificIterations(
								BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), 
								true, 
								injector, 
								iterationsToBeIncluded, 
								InjectorsPackageUtils.PartitionBoundaryAppendingDecision.getDefaultInstance(),
								tokenReplacementProcessor,
								new InjectorsPackageUtils.RawScriptOutputProcessor(injector, 
										"subset-iter-"+fileNameSuffix, InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), BuildMain.getInjectorsExecutionPanel().isBuildOpenScriptAndLogInHtmlByDefaultChecked()),
								new MutableInt(0));
						if (outputFile != null) {
							if (INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) {
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
        	viewTheLastFailedIteration.setEnabled(hasFailedIterationTheLastTimeTheInjectorWasExecuted(injector));
        	output.add(viewTheLastFailedIteration);    
        	
        	JMenuItem viewTheLastFailedIterationLog = new JMenuItem("View Last Failed Iteration Log");
        	viewTheLastFailedIterationLog.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						List<Integer> iterationsToBeIncluded = new ArrayList<Integer>(getFailedIterationsTheLastTimeTheInjectorWasExecuted(injector));
						Collections.sort(iterationsToBeIncluded);
						if (iterationsToBeIncluded.size() > Config.getBuildViewLastSuccessfulOrFailedIterationCount()) {
							iterationsToBeIncluded = 
									iterationsToBeIncluded.subList(
											iterationsToBeIncluded.size() - Config.getBuildViewLastSuccessfulOrFailedIterationCount(), 
											iterationsToBeIncluded.size());
						}						
						
						
						final List<File> logs = injectorIterationSelectionFrame.getLogFiles(injector, iterationsToBeIncluded);			
						final MutableInt numberOfLines = new MutableInt(0);
						List<File> outputFiles = new ArrayList<File>();
						for (int i = 0 ; i < logs.size() ; i++) {
							final File log = logs.get(i);
							final File outputFile = InjectorsPackageUtils.processSpecificIterations(
									log, 
									false, 
									injector, 
									iterationsToBeIncluded, 
									InjectorsPackageUtils.PartitionBoundaryAppendingDecision.getDefaultInstance(),
									InjectorsPackageUtils.TokenReplacementProcessor.getDefaultInstance(),
									new InjectorsPackageUtils.RawLogOutputProcessor(injector, log, InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), InjectorsPackageExecutionPanel.this.isBuildOpenScriptAndLogInHtmlByDefaultChecked()),
									numberOfLines);
							if (outputFile != null) {
								outputFiles.add(outputFile);
							}					
						}
						SwiftBuildFileUtils.viewMergedLogs(injector, outputFiles, InjectorsPackageExecutionPanel.this.isBuildOpenScriptAndLogInHtmlByDefaultChecked());				
						
					} catch (Throwable tr) {
						FileUtils.printStackTrace(tr);
						GUIUtils.popupErrorMessage("Error: "+CoreUtil.getAllThrowableMessages(tr));
					}
				}
        		
        	});
        	viewTheLastFailedIterationLog.setEnabled(hasFailedIterationTheLastTimeTheInjectorWasExecuted(injector));
        	output.add(viewTheLastFailedIterationLog);        	

			final List<JMenuItem> defaultMenuItems = super.getRightClickMenuItems(e);
			if (defaultMenuItems != null) {
				output.addAll(defaultMenuItems);
			}

			return output;
		}

		@Override
		public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, final int columnIndex)
		{
			Component comp = super.prepareRenderer(renderer, rowIndex, columnIndex);

			comp.setForeground(Color.black);

			String status = (String) ((DefaultTableModel) getModel()).getValueAt(rowIndex, INJECTION_SCREEN_COLUMN.STATUS.getIndex());

			final boolean isSelectedRow = table.getSelectionModel().isSelectedIndex(rowIndex);


			if (status.toLowerCase().indexOf(SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE.toLowerCase()) != -1 )
			{
				comp.setBackground(isSelectedRow?SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_SELECTED_COLOR:SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_UNSELECTED_COLOR);
			}
			else if (status.toLowerCase().indexOf(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE.toLowerCase()) != -1 )
			{
				comp.setBackground(isSelectedRow?SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_SELECTED_COLOR:SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_UNSELECTED_COLOR);
			}
			else if (status.toLowerCase().indexOf(SwiftBuildConstants.INJECTOR_EXECUTION_PARTIAL_COMPLETE_VALUE.toLowerCase()) != -1 )
			{
				comp.setBackground(isSelectedRow?SwiftBuildConstants.INJECTOR_EXECUTION_PARTIAL_COMPLETE_SELECTED_COLOR:SwiftBuildConstants.INJECTOR_EXECUTION_PARTIAL_COMPLETE_UNSELECTED_COLOR);
			}
			else if ( status.equalsIgnoreCase(SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE) )
			{
				comp.setBackground(isSelectedRow?SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_SELECTED_COLOR:SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_UNSELECTED_COLOR);
			}
			else if ( status.equalsIgnoreCase(SwiftBuildConstants.INJECTOR_EXECUTION_LOG_MISMATCH_VALUE) )
			{
				comp.setBackground(isSelectedRow?SwiftBuildConstants.INJECTOR_EXECUTION_LOG_MISMATCH_SELECTED_COLOR:SwiftBuildConstants.INJECTOR_EXECUTION_LOG_MISMATCH_UNSELECTED_COLOR);
			}

			return comp;
		}
	}

	public void openLogMismatchesLog(final Injector injector) throws Exception {
		if (this.logMismatchesLog.containsKey(injector) && this.logMismatchesLog.get(injector).isFile()) {
			FileUtils.startTextEditor(Config.getCmdTextEditor(), this.logMismatchesLog.get(injector));
		}
	}

	private IterationRuntimeMeasurement iterationRuntimeMeasurement = null;

	public InjectorsPackageExecutionPanel(BuildMain BuildMain) throws Exception
	{
		this.BuildMain = BuildMain;
		this.injectorsAndIterationMarkerIsActive = new HashMap<Injector, Boolean>();
		this.logMismatchesLog = new HashMap<Injector, File>();
		this.successfulAndFailedIterationsForAllSessions = new ArrayList<SuccessfulAndFailedIterationsForEachSession>();
		this.injectorIndexToPartitionNameMapForAllSessions = new ArrayList<Map<Integer, List<String>>>();
		createComponents();

		SwiftBuildFileUtils.initConsoleLogFolder();
		file = SwiftBuildFileUtils.getConsoleLogFile();
		fw = new FileWriter(file.getAbsoluteFile());
		bw = new BufferedWriter(fw);
	}

	public Integer getNumberOfIndividualIterationsSelected() {
		if (this.injectorIterationSelectionFrame == null || this.injectorIterationSelectionFrame.getExecutableIterations() == null) {
			return null;
		} else {
			return this.injectorIterationSelectionFrame.getExecutableIterations().size();
		}
	}

	public List<Injector> getInjectors()
	{
		return injectors;
	}

	public BuildMain getBuildMain()
	{
		return BuildMain;
	}

	private void createComponents() throws Exception
	{
		try
		{
			FileUtils.println("Disabling double-buffer.");
			setDoubleBuffered(false);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
		injectorIterationSelectionFrame = new InjectorIterationSelectionFrame(BuildMain);

		this.setLayout(new BorderLayout());

		northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.add(northPanel, BorderLayout.NORTH);

		centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		this.add(centerPanel, BorderLayout.CENTER);

        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		
		model = new DefaultTableModel()
		{
			public boolean isCellEditable(int row, int column)
			{
				if ( column == 0 )
				{
					return startButton.isEnabled();
				}
				return false;
			}
		};

		final INJECTION_SCREEN_COLUMN columns[] = INJECTION_SCREEN_COLUMN.getSorted();
		Object columnHeaders[] = new Object[columns.length];
		for (int i = 0 ; i < columns.length ; i++) {
			columnHeaders[i] = columns[i].toString();
		}

		model.setDataVector(null, columnHeaders);


		table = new WindowsExplorerStyleJTableCustomized();
		
		table.getTableHeader().addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				execute(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				execute(e);
			}
			
			private void execute(MouseEvent e) {
				final int columnIndex = table.columnAtPoint(e.getPoint());
				if (columnIndex == -1) {
					table.getTableHeader().setToolTipText(null);
				} else {
					table.getTableHeader().setToolTipText(columns[columnIndex].getToolTip());
				}	
			}
			
		});
		
		table.getTableHeader().setReorderingAllowed(false);

		DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
		headerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumn(INJECTION_SCREEN_COLUMN.INJECTOR_TYPE.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.STATUS.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.TOTAL.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.PARTITIONS_TRACKER.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.SUCCESS.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.FAILED.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.REMAINING.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.SELECTED.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.LAST_SUCCESSFUL.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.LAST_FAILED.toString()).setCellRenderer(dtcr);
		table.getColumn(INJECTION_SCREEN_COLUMN.FILE_SIZE.toString()).setCellRenderer(dtcr);

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		rowRenderer = new EachRowRenderer();
		checkBoxRenderer = new CheckBoxRenderer(true);

		JCheckBox cb = new JCheckBox();
		cb.setHorizontalAlignment(SwingConstants.CENTER);
		checkBoxEditor = new DefaultCellEditor(cb);

		rowEditor = new EachRowEditor(table);
		table.getColumn(INJECTION_SCREEN_COLUMN.SELECTION.toString()).setCellRenderer(rowRenderer);
		table.getColumn(INJECTION_SCREEN_COLUMN.SELECTION.toString()).setCellEditor(rowEditor);

		for (int i = 0 ; i < columns.length ; i++) {
			TableColumn column = table.getTableHeader().getColumnModel().getColumn(columns[i].getIndex());
			table.resizeColumnByProportion(column, columns[i].getProportion());
		}

		scrollPane = new JScrollPane(table);
		table.setScrollPane(scrollPane);
		centerPanel.add(scrollPane);
		this.add(centerPanel, BorderLayout.CENTER);

		pauseCheckBox = new JCheckBox("Pause between each injector");

		final JLabel rightClickExplanationLabel = new JLabel("* Right click on the injector name to see menu");
		
		buildOpenScriptAndLogInHtmlByDefault = new JCheckBox("Open the Scripts and Logs as HTML");
		buildOpenScriptAndLogInHtmlByDefault.setSelected(Config.getBuildOpenScriptAndLogInHtmlByDefault());

		JPanel internalPanelOfCenterPanel = GUIUtils.addToPanel(centerPanel, pauseCheckBox, rightClickExplanationLabel, null, null, new Dimension(100, 0), new Dimension(220, 0));
		internalPanelOfCenterPanel.add(buildOpenScriptAndLogInHtmlByDefault);

		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		centerPanel.add(progressBar);

		currentInjectorLabel = GUIUtils.getLabel("Current injector name:", true);
		currentInjectorNameLabel = GUIUtils.getLabel("", true);
		injectorCountLabel = GUIUtils.getLabel("Execution count:", true);
		injectorCountValueLabel = GUIUtils.getLabel("", true);
		GUIUtils.addToPanel(centerPanel, currentInjectorLabel, currentInjectorNameLabel, null, null, null, null);
		GUIUtils.addToPanel(centerPanel, injectorCountLabel, injectorCountValueLabel, null, null, null, null);

		startButton = GUIUtils.getButton(BuildMain.getClass(), "Start", "/images/play16.gif");
		startButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					startExecution();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});
		stopButton = GUIUtils.getButton(BuildMain.getClass(), "Stop", "/images/stop16.gif");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if (actionManager != null) {
						actionManager.disposeEnableDiagnosticsPanel();
					}
					
					stopExecution();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});
		openLogsFolderButton = GUIUtils.getButton(BuildMain.getClass(), "Open Logs folder", "/images/view.gif");
		openLogsFolderButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					File logFolder = FileUtils.getLogFolder();
					SwiftBuildFileUtils.openFolder(logFolder);
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});
		clearLogButton = GUIUtils.getButton(BuildMain.getClass(), "Clear status", "/images/delete16.gif");
		clearLogButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					clearLogTextArea();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});
		saveLogButton = GUIUtils.getButton(BuildMain.getClass(), "Save status", "/images/save16.gif");
		saveLogButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					saveLogToFile();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});
		JPanel tempPanel = new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		centerPanel.add(tempPanel);
		tempPanel.add(startButton);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		tempPanel.add(stopButton);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		tempPanel.add(openLogsFolderButton);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		tempPanel.add(clearLogButton);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		tempPanel.add(saveLogButton);

		logTextArea = new JTextArea(10, 40);
		logTextArea.setEditable(false);
		logTextArea.setFont(GUIUtils.PLAIN_SYSTEM_FONT);

		scrollPaneLogArea = new JScrollPane(logTextArea);
		centerPanel.add(scrollPaneLogArea);

		previousButton = GUIUtils.getButton(BuildMain.getClass(), "Previous", SwiftBuildConstants.IMAGE_BACK);
		previousButton.setEnabled(true);
		previousButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					BuildMain.switchToPanel(SwiftBuildConstants.PANEL_INJECTORS_PACKAGE_SELECTION);
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});
		JButton nextButton = GUIUtils.getButton(BuildMain.getClass(), "Next", SwiftBuildConstants.IMAGE_NEXT);
		nextButton.setEnabled(false);
		
        JPanel southPanel = new JPanel();
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        this.add(southPanel, BorderLayout.SOUTH);

        tempPanel = new JPanel();
        tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(previousButton);
        southPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        tempPanel.add(nextButton);
        southPanel.add(tempPanel);

		saveFileChooser = Utils.initializeJFileChooserWithTheLastPath("SAVE_FILE_CHOOSER_INJECTORS_PACKAGE_EXECUTION_PANEL");
		CustomFileFilter filter = new CustomFileFilter(SwiftBuildConstants.LOG_FILE_EXTENSION);
		saveFileChooser.setFileFilter(filter);
	}


	public void saveLogToFile() throws Exception
	{
		try
		{
			int returnVal = saveFileChooser.showDialog(this, "Save as");
			if ( returnVal == JFileChooser.APPROVE_OPTION )
			{
				File file = saveFileChooser.getSelectedFile();
				org.apache.commons.io.FileUtils.writeStringToFile(file,logTextArea.getText());
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	public void setExecutionLog(String output)
	{
		final StringBuffer buffer = new StringBuffer();
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String DATE_FORMAT = CoreConstants.DATE_FORMAT_PATTERN.STANDARD.getPattern();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT, new Locale("us", "US"));
		sdf.setTimeZone(TimeZone.getDefault());
		String timeNow = sdf.format(cal.getTime());

		buffer.append(timeNow).append(": ").append(output).append("\n");

		try {

			if (SwingUtilities.isEventDispatchThread()) {
				logTextArea.append(buffer.toString());
				logTextArea.setCaretPosition(logTextArea.getText().length());
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						logTextArea.append(buffer.toString());
						logTextArea.setCaretPosition(logTextArea.getText().length());
					}
				});
			}

			bw.write(buffer.toString());
			bw.flush();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public String getExecutionLog()
	{
		return logTextArea.getText();
	}

	public void setCheckBoxes(boolean isChecked)
	{
		int rowCount = table.getRowCount() - 1;
		for ( int i = 0; i <= rowCount; i++ )
		{
			updateRow(isChecked, i);
		}
	}

	public void propagateTickedStateOfSelectedRow(final boolean below)
	{
		int selectedRow = table.getSelectedRow();
		if ( selectedRow == -1 )
		{
			return;
		}
		final boolean stateToPropagate = (boolean) model.getValueAt(selectedRow, 0);
		if ( below )
		{
			final int rowCount = table.getRowCount();
			for ( int i = selectedRow+1; i < rowCount; i++ )
			{
				updateRow(stateToPropagate, i);
			}
		}
		else
		{
			for ( int i = 0; i < selectedRow; i++ )
			{
				updateRow(stateToPropagate, i);
			}
		}
	}

	private void updateRow(boolean status, int index)
	{
		((DefaultTableModel) table.getModel()).setValueAt(status, index, 0);
		((DefaultTableModel) table.getModel()).fireTableCellUpdated(index, 0);
	}

	public void setProgressBar(int scriptCounter, int totalScriptsCounter)
	{
		try
		{
			progressBar.setValue(scriptCounter);
			progressBar.setMaximum(totalScriptsCounter);

			Rectangle progressRect = progressBar.getBounds();
			progressRect.x = 0;
			progressRect.y = 0;
					progressBar.paintImmediately(progressRect);
				}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public void setInjectorsCountValue(int injectorCurrentCounter, int totalInjectorsCounter)
	{
		injectorCountValueLabel.setText(injectorCurrentCounter + " / " + totalInjectorsCounter);
	}

	public void deselectInjector(Injector injector, int injectorCurrentCounter, int totalInjectorsCounter)
	{
		updateRow(false, injector.getIndex());
		setProgressBar(injectorCurrentCounter, totalInjectorsCounter);
		if ( injectorCurrentCounter < totalInjectorsCounter )
		{
			updateScrollPane(injector.getIndex());
		}
	}

	public void updateScrollPane(final int row)
	{
		try
		{
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					table.changeSelection(row, 0, false, false);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
				}
			});
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public int showDialogPausePopup()
	{
		Object[] options = { "CONTINUE", "STOP" };
		int i = JOptionPane.showOptionDialog(BuildMain.getRootFrame(),
				"Click CONTINUE to continue the execution of the next injector",
				"Warning",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[0]);
		return i;
	}

	public void clearLogTextArea()
	{
		logTextArea.setText("");
	}

	@Override
	public void setNumberOfIterationsAndHumanReadableFileSize(final int row, IterationCountAndHumanReadableFileSize numberOfIterationsAndHumanReadableFileSize) {
		table.setValueAt(numberOfIterationsAndHumanReadableFileSize.getIterationCount(), row, INJECTION_SCREEN_COLUMN.TOTAL.getIndex());
		table.setValueAt(numberOfIterationsAndHumanReadableFileSize.getHumanReadableFileSize(), row, INJECTION_SCREEN_COLUMN.FILE_SIZE.getIndex());
		table.repaint();
	}

	public boolean hasIteration(final int row) {
		if (((DefaultTableModel) table.getModel()).getValueAt(row, INJECTION_SCREEN_COLUMN.TOTAL.getIndex()) != null) {
			return getNumberOfIterations(row) > 0;
		} else {
			return false;
		}
	}

	public int getNumberOfIterations(final int row) {
		return Integer.parseInt(((DefaultTableModel) table.getModel()).getValueAt(row, INJECTION_SCREEN_COLUMN.TOTAL.getIndex()).toString());
	}

	public void intializeInjectorsTable() throws Exception
	{
//		convertedInjectorToHTMLList = new ArrayList<File>();
		injectorIndexToActionMap = new HashMap<Integer, ActionInterface>();
		GUIUtils.deleteAllRows(table);
		progressBar.setValue(0);

		displayCurrentInjectorName("");
		injectorCountValueLabel.setText("");
		clearLogTextArea();

		final Map<Injector, Integer> injectorToRowMap = new HashMap<Injector, Integer>();

		for ( Injector injector : injectors )
		{
			Object[] row = new Object[INJECTION_SCREEN_COLUMN.values().length];
			for (int i = 0 ; i < row.length ; i++) {
				if (i == INJECTION_SCREEN_COLUMN.SELECTION.getIndex()) {
					row[i] = true;
				} else if (i == INJECTION_SCREEN_COLUMN.INJECTOR_NAME.getIndex()) {
					row[i] = injector.getName();
				} else if (i == INJECTION_SCREEN_COLUMN.INJECTOR_TYPE.getIndex()) {
					row[i] = injector.getType().getHumanReadableType();
				}else if (i == INJECTION_SCREEN_COLUMN.STATUS.getIndex() || i == INJECTION_SCREEN_COLUMN.EXECUTION_TIME.getIndex()) {
					row[i] = "";
				} else if (i == INJECTION_SCREEN_COLUMN.SUCCESS.getIndex() || i == INJECTION_SCREEN_COLUMN.FAILED.getIndex() || i == INJECTION_SCREEN_COLUMN.REMAINING.getIndex() || i == INJECTION_SCREEN_COLUMN.SELECTED.getIndex()) {
					row[i] = "";
				} else if (i == INJECTION_SCREEN_COLUMN.PARTITIONS_TRACKER.getIndex()) {
					row[i] = "";
				} else if (i == INJECTION_SCREEN_COLUMN.LAST_SUCCESSFUL.getIndex()) {
					row[i] = "";
				} else if (i == INJECTION_SCREEN_COLUMN.LAST_FAILED.getIndex()) {
					row[i] = "";
				} else {
					row[i] = null;
				}
			}
			model.addRow(row);

			int rowCount = table.getRowCount() - 1;
			rowRenderer.add(rowCount, checkBoxRenderer);
			rowEditor.setEditorAt(rowCount, checkBoxEditor);
			injectorToRowMap.put(injector, rowCount);
//			convertedInjectorToHTMLList.add(SwiftBuildFileUtils.showInjectorContent(BuildMain, injector, InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()), InjectorsPackageExecutionPanel.this.isBuildOpenScriptAndLogInHtmlByDefaultChecked(), false));
		}

		writeIterationCountAndFileSize(injectorToRowMap, BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), injectorsAndIterationMarkerIsActive, this);
		
	}

	public boolean isPauseBetweenEachInjector()
	{
		return pauseCheckBox.isSelected();
	}

	public void displayCurrentInjectorName(String injectorName)
	{
		currentInjectorNameLabel.setText(injectorName);
	}

	public boolean isInjectionRunning()
	{
		if ( actionManager == null )
		{
			return false;
		}
		return !actionManager.isExecutionStopped();
	}

	public boolean isExecutingIterations()
	{
		return injectorIterationSelectionFrame.isExecutingIterations();
	}

	public List<Injector> getSelectedInjectors()
	{
		List<Injector> res = new ArrayList<Injector>();
		if ( isExecutingIterations() )
		{
			int selectedRow = table.getSelectedRow();
			res.add(injectors.get(selectedRow));
			return res;
		}
		for ( int i = 0; i < table.getRowCount(); i++ )
		{
			Boolean isChecked = (Boolean) model.getValueAt(i, 0);
			if ( isChecked )
			{
				res.add(injectors.get(i));
			}
		}
		return res;
	}

	public void enableStartButtonAndZipIterationLogsMenu()
	{
		startButton.setEnabled(true);
		this.injectorIterationSelectionFrame.setStartButtonEnabled(true);
		this.BuildMain.getZipIterationLogsItem().setEnabled(true);
	}


	public void startExecution()
	{
		try
		{
			startButton.setEnabled(false);
			this.BuildMain.getZipIterationLogsItem().setEnabled(false);
			this.injectorIterationSelectionFrame.setStartButtonEnabled(false);

			stopButton.setEnabled(true);
			previousButton.setEnabled(false);
			progressBar.setValue(0);
			logTextArea.setText(" ");

			this.logMismatchesLog = new HashMap<Injector, File>();
			final File fldLogMismatchesLogDirectory = new File(FileUtils.getLogFolder(), "log_mismatches");
			for (Injector injector : this.injectors) {
				if (injector.getType().equals(CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM) || injector.getType().equals(CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM)) {
					this.logMismatchesLog.put(injector, new File(fldLogMismatchesLogDirectory, "log_mismatch_"+injector.getNameWithoutExtension()+"_"+CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_PRECISE.getDateFormat().format(new Date())+".log"));
				}
			}

			actionManager = new ActionManager(this);
			actionManager.registerListener(actionManager);
			actionManager.start();
			
			this.successfulAndFailedIterationsForAllSessions.add(
					new SuccessfulAndFailedIterationsForEachSession(this.actionManager.getSuccessfulIterations(), this.actionManager.getFailedIterations()));	
			
			this.injectorIndexToPartitionNameMapForAllSessions.add(actionManager.getInjectorIndexToPartitionNameMap());
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public void stopExecution()
	{
		setExecutionLog("Manual stop done by the end-user.");

		injectorIterationSelectionFrame.reset();
		startButton.setEnabled(false);
		stopButton.setEnabled(false);
		previousButton.setEnabled(true);

		if ( actionManager != null )
		{
			if( actionManager.getLastExecutedIteration() == null ){
				actionManager.stopExecution();
			}else{
				actionManager.setStoppedManuallyByUser(true);
			}
		}
		
		//it is nice to immediately kill the browser when user clicks stop
		//if this method is not executed, then the user will have to wait for few seconds for the browser to close
		CurrentBrowserTask.eliminateTask();
		
	}

	public void viewInjectionOutput(int row)
	{
		try
		{
			Injector injector = injectors.get(row);
			if (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))
			{
				viewInjectorLog(injector, false);
			}
			else if (CoreConstants.INJECTOR_TYPE.TYPE_API.equals(injector.getType()))
			{
				viewCurrentDataGrid(injector);
			}
			else if (CoreConstants.INJECTOR_TYPE.TYPE_TELNET.equals(injector.getType()))
			{
				viewTerminal(injector);
			}
            else if (CoreConstants.INJECTOR_TYPE.TYPE_SQL.equals(injector.getType()))
            {
                viewSqlLog(injector);
            }
			else
			{
				GUIUtils.popupInformationMessage("No action for this injector type");
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: " + e.getMessage());
		}
	}

	public void viewCurrentInjectorLog()
	{
		if ( actionManager == null )
		{
			GUIUtils.popupInformationMessage("No injection running");
			return;
		}
		Injector injector = injectors.get(actionManager.getCurrentInjectorIndex());
		viewInjectorLog(injector, true);
	}

	public void viewTerminal(Injector injector)
	{
		if ( actionManager == null )
		{
			GUIUtils.popupInformationMessage("No injection running");
			return;
		}
		SSHAction action = (SSHAction) getInjectorIndexToActionMap().get(injector.getIndex());
		if ( action != null )
		{
			action.getTerminal().setVisible(true);
		}
	}


	public void viewSqlLog(final Injector injector) throws Exception
	{
		File logFile = null;
		if (Utils.hasAccessToInternalStaffsOnlyFeatures()) {
			File encryptedLogFile = SQLAction.getFullSqlLogFile(this.getBuildMain(), injector.getName());
			if (!encryptedLogFile.isFile()) {
				return;
			}
			final byte[] ciphertext = org.apache.commons.io.FileUtils.readFileToByteArray(encryptedLogFile);
			final byte[] plaintext = Encryption.decrypt(ciphertext);
			logFile = new File(Config.getTempFolder(), injector.getName()+"-"+
					CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_PRECISE.getDateFormat().format(new java.util.Date())+".log");
			org.apache.commons.io.FileUtils.writeByteArrayToFile(logFile, plaintext);
		} else {
			logFile = SwiftBuildFileUtils.getLogFile(BuildMain, injector.getName());
		}
        if ( !logFile.exists() )
        {
            return;
        }
        FileUtils.startTextEditor(Config.getCmdTextEditor(), logFile);
	}

	public void viewInjectorLog(final Injector injector, final boolean isOnlyForCurrentSession)
	{
		Runnable r = new Runnable()
		{
			public void run()
			{
				try
				{
					if (!isOnlyForCurrentSession) {
						if (getPartitionNamesTheLastTimeTheInjectorWasExecuted(injector) == null || getPartitionNamesTheLastTimeTheInjectorWasExecuted(injector).isEmpty()) {
							GUIUtils.popupInformationMessage("This injector has not been executed");
							return;
						}
					}
					final List<String> allPartitionsIncludingThoseWhoseLogHasNotBeenCreated;
					if (isOnlyForCurrentSession) {
						allPartitionsIncludingThoseWhoseLogHasNotBeenCreated = actionManager.getInjectorIndexToPartitionNameMap().get(injector.getIndex());
					} else {
						allPartitionsIncludingThoseWhoseLogHasNotBeenCreated = getPartitionNamesTheLastTimeTheInjectorWasExecuted(injector);
					}
					
					
					if ( allPartitionsIncludingThoseWhoseLogHasNotBeenCreated == null || allPartitionsIncludingThoseWhoseLogHasNotBeenCreated.isEmpty())
					{
						GUIUtils.popupErrorMessage("No Log file for this injector.");
						return;
					}
					
					if (isOnlyForCurrentSession) {
						actionManager.getLogCopier().copy(false);
					}
					
					final List<String> partitions = new ArrayList<String>();
					for (final String partition : allPartitionsIncludingThoseWhoseLogHasNotBeenCreated) {
						File logFile = SwiftBuildFileUtils.getLogFile(BuildMain, partition);
						if (logFile != null && logFile.isFile()) {
							partitions.add(partition);
						}
					}
					
					if (partitions.isEmpty()) {
						GUIUtils.popupErrorMessage("Log file for this injector has not been generated yet.");
						return;						
					}
					

					Collections.sort(partitions);					
					
					if ( partitions.size() > 1 )
					{
						Runnable logsViewer = new Runnable() {

							private void openLogFiles(final int selectedIndices[]) {
								for (int selectedIndex : selectedIndices) {
									try {
										File logFile = SwiftBuildFileUtils.getLogFile(BuildMain, partitions.get(selectedIndex));
										openLogFile(logFile);
									} catch (Exception e) {
										throw new Error(e);
									}

								}
							}

							@Override
							public void run() {
								final JList logsList = new JList(partitions.toArray(new String[partitions.size()]));
								logsList.addMouseListener(new MouseAdapter() {
								    public void mouseClicked(MouseEvent evt) {
								        JList list = (JList)evt.getSource();
								        if (evt.getClickCount() == 2) { //if being double clicked
								            int index = list.locationToIndex(evt.getPoint());
								            final int selectedIndices[] = {index};
								            openLogFiles(selectedIndices);
								        }
								    }
								});
								logsList.addKeyListener(new KeyListener() {

									@Override
									public void keyTyped(KeyEvent e) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void keyPressed(KeyEvent e) {
										if (KeyEvent.VK_ENTER == e.getExtendedKeyCode()) {
											final int selectedIndices[] = logsList.getSelectedIndices();
											openLogFiles(selectedIndices);									
										}
									}

									@Override
									public void keyReleased(KeyEvent e) {
										// TODO Auto-generated method stub
										
									}
									
								});
								JOptionPane.showMessageDialog(null, logsList, "Choose the corresponding injector(s)", JOptionPane.PLAIN_MESSAGE);
							}

						};

						SwingUtilities.invokeAndWait(logsViewer);

					}
					else
					{
						File logFile = SwiftBuildFileUtils.getLogFile(BuildMain, partitions.get(0));
						if ( !logFile.exists() )
						{
							GUIUtils.popupErrorMessage("No Log file '" + logFile.getName() + "'. Please start an injection first.");
							return;
						}
						openLogFile(logFile);
					}

				}
				catch ( Throwable tr )
				{
					FileUtils.printStackTrace(tr);
					GUIUtils.popupErrorMessage("Error:" + CoreUtil.getAllThrowableMessages(tr));
				}
			}
			
			private void openLogFile(final File logFile) throws Exception {
				if (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) ||
						CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) {
					final String targetFileName;
					if (injector.getName().contains(".")) {
						targetFileName = logFile.getName().replaceAll("\\..*$", ".html");
					} else {
						targetFileName = logFile.getName()+".html";
					}												
					File targetFile = (new File(FileUtils.getTemporaryFolder(), targetFileName)).getAbsoluteFile();
					try (InputStream inputStream = new FileInputStream(logFile)) {
						if (InjectorsPackageExecutionPanel.this.isBuildOpenScriptAndLogInHtmlByDefaultChecked()) {
							SwiftBuildFileUtils.generatePartialLogMainContentFldScriptAndLog(
									injector.getName(),
									injector.getType(),
									inputStream,
									targetFile,
									false,
									InjectorsPackageUtils.isIterationTrackingEnabled(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()),
									true);							
						} else {
							SwiftBuildFileUtils.generatePartialLogMainContentFldScriptAndLogPlain(injector.getType(), inputStream, targetFile);
						}

					}
					
					if (InjectorsPackageExecutionPanel.this.isBuildOpenScriptAndLogInHtmlByDefaultChecked()) {
						FileUtils.startSourceViewerBrowser(Config.getBrowserViewSource(), targetFile);	
					} else {
						FileUtils.startTextEditor(Config.getCmdTextEditor(), targetFile);
					}
					
				} else {
					FileUtils.startTextEditor(Config.getCmdTextEditor(), logFile);
				}
			}
		};
		Thread t= new Thread(r);
		t.start();
	}

	public void clearOldStatisticsFromInjectorRowBeforeExecution(final int index) {
		updateCell(index, INJECTION_SCREEN_COLUMN.EXECUTION_TIME, "");
		updateCell(index, INJECTION_SCREEN_COLUMN.SUCCESS, "");
		updateCell(index, INJECTION_SCREEN_COLUMN.FAILED, "");
		updateCell(index, INJECTION_SCREEN_COLUMN.REMAINING, "");
		updateCell(index, INJECTION_SCREEN_COLUMN.LAST_SUCCESSFUL, "");
		updateCell(index, INJECTION_SCREEN_COLUMN.LAST_FAILED, "");
		updateCell(index, INJECTION_SCREEN_COLUMN.PARTITIONS_TRACKER, "");
	}
	
	public static final String NOT_APPLICABLE = "N/A";

	public void updateExecutionTime(int injectorIndex, String executionTime)
	{
		String toPrint = "";
		if (executionTime != null) {
			toPrint += executionTime;
		}

		if (this.injectorsAndIterationMarkerIsActive.get(injectors.get(injectorIndex))) {
			if (this.iterationRuntimeMeasurement != null && this.iterationRuntimeMeasurement.getIterationsPerMinute() != null) {
				toPrint += (toPrint.isEmpty()?"":" | ") + (this.iterationRuntimeMeasurement.getIterationsPerMinute()==0?"< 1":this.iterationRuntimeMeasurement.getIterationsPerMinute())+" IT/M";
			}
			if (executionTime == null && this.iterationRuntimeMeasurement != null && this.iterationRuntimeMeasurement.getEstimatedRemainingTime() != null && this.iterationRuntimeMeasurement.getEstimatedRemainingTime() > 0) {
				toPrint += (toPrint.isEmpty()?"":" | ") + CoreUtil.getDaysHoursMinutesSecondsFromMilliseconds(this.iterationRuntimeMeasurement.getEstimatedRemainingTime())+" Remaining";
			}
		} else if (executionTime == null) {
			toPrint = NOT_APPLICABLE;
		}

		updateCell(injectorIndex, INJECTION_SCREEN_COLUMN.EXECUTION_TIME, toPrint);
	}

	public void setNAIfInjectorHasNoActiveIterationMarker(final Injector injector) {
		if (!injectorsAndIterationMarkerIsActive.get(injector)) {
			updateCell(injector.getIndex(), INJECTION_SCREEN_COLUMN.SUCCESS, NOT_APPLICABLE);
			updateCell(injector.getIndex(), INJECTION_SCREEN_COLUMN.FAILED, NOT_APPLICABLE);
			updateCell(injector.getIndex(), INJECTION_SCREEN_COLUMN.REMAINING, NOT_APPLICABLE);
			updateCell(injector.getIndex(), INJECTION_SCREEN_COLUMN.SELECTED, NOT_APPLICABLE);
			updateCell(injector.getIndex(), INJECTION_SCREEN_COLUMN.LAST_SUCCESSFUL, NOT_APPLICABLE);
			updateCell(injector.getIndex(), INJECTION_SCREEN_COLUMN.LAST_FAILED, NOT_APPLICABLE);
			updateCell(injector.getIndex(), INJECTION_SCREEN_COLUMN.PARTITIONS_TRACKER, NOT_APPLICABLE);
		}

	}

	public void updateTotalIterationsToExecute(final int index, final int numberOfIterations) {
		if (injectorsAndIterationMarkerIsActive.get(injectors.get(index))) {
			updateCell(index, INJECTION_SCREEN_COLUMN.SELECTED, String.valueOf(numberOfIterations));
		} else {
			setNAIfInjectorHasNoActiveIterationMarker(injectors.get(index));
		}

	}
	
	private String computeRatioPercentString(final int dividend, final int divisor) {
		if (divisor == 0) {
			return "";
		} else {
			double ratio = (1.0 * dividend) / (1.0 * divisor);
			return " ("+NumberFormat.getPercentInstance().format(ratio)+")";
		}
	}

	public void updateSuccessfulIterationTally(final int index, final int numberOfSuccessfulIterations) {
		final int numberOfIterations = Integer.parseInt(((DefaultTableModel) table.getModel()).getValueAt(index, INJECTION_SCREEN_COLUMN.SELECTED.getIndex()).toString());
		updateCell(index, INJECTION_SCREEN_COLUMN.SUCCESS, numberOfSuccessfulIterations+computeRatioPercentString(numberOfSuccessfulIterations, numberOfIterations) );
	}

	public void updateFailedIterationTally(final int index, final int numberOfFailedIterations) {
		final int numberOfIterations = Integer.parseInt(((DefaultTableModel) table.getModel()).getValueAt(index, INJECTION_SCREEN_COLUMN.SELECTED.getIndex()).toString());
		updateCell(index, INJECTION_SCREEN_COLUMN.FAILED, numberOfFailedIterations+computeRatioPercentString(numberOfFailedIterations, numberOfIterations));
	}

	public void updateRemainingIterationTally(final int index, final int numberOfRemainingIterations) {
		final int numberOfIterations = Integer.parseInt(((DefaultTableModel) table.getModel()).getValueAt(index, INJECTION_SCREEN_COLUMN.SELECTED.getIndex()).toString());
		updateCell(index, INJECTION_SCREEN_COLUMN.REMAINING, numberOfRemainingIterations+computeRatioPercentString(numberOfRemainingIterations, numberOfIterations));
	}

	public void updatePartitionsTracker(final int index, final int numberOfExecutedIterations, final int numberOfIterations, final int partitionCapacity) {
		int numberOfPartitions = numberOfIterations / partitionCapacity;
		if (numberOfIterations % partitionCapacity != 0) {
			numberOfPartitions++;
		}

		int numberOfExecutedPartitions = numberOfExecutedIterations / partitionCapacity;
		if (numberOfExecutedIterations == numberOfIterations) {
			numberOfExecutedPartitions = numberOfPartitions;
		}

		String toPrint = numberOfExecutedPartitions+"/"+numberOfPartitions+computeRatioPercentString(numberOfExecutedPartitions, numberOfPartitions);
		updateCell(index, INJECTION_SCREEN_COLUMN.PARTITIONS_TRACKER, toPrint);
	}

	public void updateStatus(int injectorIndex, String status)
	{
		updateCell(injectorIndex, INJECTION_SCREEN_COLUMN.STATUS, status);
	}

	public String getStatus(int injectorIndex)
	{
		return (String) ((DefaultTableModel) table.getModel()).getValueAt(injectorIndex, INJECTION_SCREEN_COLUMN.STATUS.getIndex());
	}

	public void viewCurrentDataGrid(Injector injector) throws Exception
	{
		APIAction apiAction = createNewAPIAction(BuildMain, null, injector, getBuildMain().getEnvironmentProperties(), BuildMain.getInjectorsPackageSelectionPanel()
				.getInjectorsPackageFile());

		if ( apiAction.getActionManager() != null && !apiAction.getActionManager().isExecutionStopped() )
		{
			GUIUtils.popupInformationMessage("Please wait for the execution to complete.");
		}
		else
		{
			if ( apiAction.getApiDataAction() != null && apiAction.getApiDataAction().getAPIDataGridController() != null
					&& apiAction.getApiDataAction().getAPIDataGridController().getInventoryDataGridFrame() != null
					&& apiAction.getApiDataAction().getAPIDataGridController().getInventoryDataGridFrame().isVisible() )
			{
				apiAction.setDataGridVisible(true);
			}
			else
			{
				apiAction.openDataGrid();
			}
		}
	}
	
	public File exportAllStatusesToExcelFromAllDataGrids() {
		File outputFolder = null;
		if (injectors != null && injectors.size()>0 && CoreConstants.INJECTOR_TYPE.TYPE_API.equals(injectors.get(0).getType())) {
			try {
				for (Injector injector : injectors) {
					File packageFile = BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile();
					APIAction apiAction = createNewAPIAction(BuildMain, null, injector, getBuildMain().getEnvironmentProperties(), packageFile);
					
					outputFolder = new File(Config.getTempFolder() + File.separator + CoreConstants.SHORT_APPLICATION_NAME.build + File.separator + "export_all_injector_statuses" + File.separator + CoreUtil.getFileNameWithoutExtension(packageFile));
					outputFolder.mkdirs();
					if (injector.getStatus() != null && injector.getStatus().toLowerCase().contains(ExecutionStatusTreeTableNode.STATUS_ERROR.toLowerCase())) {
						outputFolder = new File(outputFolder, "Error");
						outputFolder.mkdir();
					}
					apiAction.getApiDataAction().getAPIDataGridController().exportGridStatusToExcelForOpenTicket(outputFolder);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
				GUIUtils.popupErrorMessage(e.getMessage());
			}
		}
		return outputFolder;
	}

	public synchronized APIAction createNewAPIAction(BuildMain BuildMain,
			ActionManager actionManager,
			Injector injector,
			Map<String, String> environmentProperties,
			File injectorsPackageFile) throws Exception
	{
		APIAction apiAction = (APIAction) injectorIndexToActionMap.get(injector.getIndex());
		if ( apiAction == null )
		{
			ZipFile zipFile = null;
			InputStream inputStream = null;
			try
			{
				if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile()) )
				{
					inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(),
							injector.getName());
				}
				else
				{
					zipFile = new ZipFile(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile());
					inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, injector.getName());
				}
				Map<String, String> specificationsProperties = InjectorsPackageUtils.getSpecificationProperties(injectorsPackageFile);
				String scriptGenerationIdAPI = specificationsProperties.get(CoreConstants.SPECIFICATION_INJECTOR_API_PREFIX + (injector.getIndex() + 1));
				String inventoryFileName = scriptGenerationIdAPI + "." + APIAction.GENERATION_API_INVENTORY_SUFFIX + "." + SwiftBuildConstants.XML_FILE_EXTENSION;
				Inventory inventory = APIDataGridUtils.getInventory(injectorsPackageFile, inventoryFileName);
				apiAction = new APIAction(injector,
						inventory,
						inventory.getName(),
						injectorsPackageFile,
						inputStream,
						environmentProperties,
						actionManager,
						scriptGenerationIdAPI,
						BuildMain);
				apiAction.init();
				injectorIndexToActionMap.put(injector.getIndex(), apiAction);
			}
			finally
			{
				if ( zipFile != null )
				{
					zipFile.close();
				}
			}
		}
		return apiAction;
	}

	public Map<Integer, ActionInterface> getInjectorIndexToActionMap()
	{
		return injectorIndexToActionMap;
	}

	public void setInjectors(List<Injector> injectors)
	{
		this.injectors = injectors;
	}

	public Set<Integer> getExecutableIterations() {
		return this.injectorIterationSelectionFrame.getExecutableIterations();
	}

	public Integer getNextSelectedIteration(final int currentIteration) {
		if (this.injectorIterationSelectionFrame.getExecutableIterations() == null) {
			return null;
		} else {
			return this.injectorIterationSelectionFrame.getNextIterationToExecute(currentIteration);
		}
	}

	public Integer getCurrentInjectorIndex() {
		if (actionManager == null) {
			return null;
		} else {
			return actionManager.getCurrentInjectorIndex();
		}
	}

	public void markAnIterationAndOnwardsForExecution(final Injector injector, final int startingIterationNumber) throws Exception {
		injectorIterationSelectionFrame.setupInjector(injector, false, actionManager);
		injectorIterationSelectionFrame.tickAndSetStartingFromIteration(startingIterationNumber);
	}

	public void refreshIterationSelectionFrameContent(final Injector injector) throws Exception {
		injectorIterationSelectionFrame.setupInjector(injector, false, actionManager);
	}

	public void deleteExecutableIterations(boolean keepOldVisibility) {
		injectorIterationSelectionFrame.deleteExecutableIterations(keepOldVisibility);
	}

	public ActionManager getActionManager() {
		return this.actionManager;
	}

	public void focusOnStartButton() {
		this.startButton.requestFocus();
	}

	//Note that using BufferedReader (i.e reading the input stream directly into string), then converting the string to bytes array, somehow yields inaccurates file size
	//Thus, the input stream is read into bytes array first, which is then converted into string
	//Additionally, zipEntry.getSize() in many cases returns -1. As such, it can't be relied upon.
	public static void writeIterationCountAndFileSize(final Map<Injector, Integer> injectorToRowMap, final File injectorsPackageFile,
			final Map<Injector, Boolean> injectorsAndIterationMarkerIsActive,
			final HasIterationCountAndFileSizeColumns table) throws Exception {
		Assert.notNull(injectorToRowMap, "injectorToRowMap must not be null");
		final Map<String, Injector> injectorNameToInjectorMap = new HashMap<String, Injector>();
		for (final Injector inj : injectorToRowMap.keySet()) {
			injectorNameToInjectorMap.put(inj.getName(), inj);
		}
		ZipInputStream zipInputStream = null;
		try
		{
			if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(injectorsPackageFile) )
			{
				zipInputStream = InjectorsPackageUtils.getZipInputStreamFromEncryptedZIPFile(injectorsPackageFile);
			}
			else
			{
				zipInputStream = InjectorsPackageUtils.getZipInputStreamFromUnencryptedZIPFile(injectorsPackageFile);
			}

			ZipEntry zipEntry = null;
            while ( (zipEntry = zipInputStream.getNextEntry()) != null )
            {
                if ( injectorNameToInjectorMap.keySet().contains(zipEntry.getName()) )
                {
        			final int bufLen = 1024;
        			byte buf[] = new byte[bufLen];
        			long fileSize = 0;
        			int iterationCount = 0;
        			boolean iterationMarkerIsActive = false;
        			int cur;
        			StringBuffer lineStrBfr = new StringBuffer();
        			while((cur = zipInputStream.read(buf)) >= 0) {
        				fileSize += cur;

        				String str = new String(buf, CoreConstants.CHARACTER_SET_ENCODING);
        				buf = new byte[bufLen];
        				while(str != null && str.indexOf('\n') != -1) {
        					final int lineBreakIndex = str.indexOf('\n');
        					lineStrBfr.append(str.substring(0, lineBreakIndex+1));
        					if (str.length() > lineBreakIndex+1) {
        						str = str.substring(lineBreakIndex+1);
        					} else {
        						str = null;
        					}
        					
        					if(lineStrBfr.toString().contains(SwiftBuildConstants.API_INJECTOR_RECORD_COUNT_START_TAG) && lineStrBfr.toString().contains(SwiftBuildConstants.API_INJECTOR_RECORD_COUNT_END_TAG)) {
        						try {
	        						String recordsCountStr = ModelUtils.getTagContent(lineStrBfr.toString(), SwiftBuildConstants.API_INJECTOR_RECORD_COUNT_START_TAG, SwiftBuildConstants.API_INJECTOR_RECORD_COUNT_END_TAG);
	        						iterationCount = Integer.parseInt(recordsCountStr);
        						} catch (Exception ex) {
        							FileUtils.printStackTrace(ex);
        						}
        					}

        					if (lineStrBfr.toString().startsWith(CoreConstants.ITERATION_SEPARATOR)) {
        						iterationCount++;
        					}
        					if (!iterationMarkerIsActive &&
        							(StringUtils.contains(lineStrBfr.toString(), CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX)
        									|| StringUtils.contains(lineStrBfr.toString(), CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX))) {
        						iterationMarkerIsActive = true;
        					}
        					lineStrBfr = new StringBuffer();
        				}
        				if (str != null) {
        					lineStrBfr.append(str);
        				}
        			}
        			if (lineStrBfr.toString().startsWith(CoreConstants.ITERATION_SEPARATOR)) {
        				iterationCount++;
        			}
        			if (iterationCount == 0) {
        				iterationMarkerIsActive = true;
        			}

        			lineStrBfr = new StringBuffer();

        			String humanReadableFileSize = null;
        			if (fileSize < 1024) { //1 KiB
        				humanReadableFileSize = fileSize+" B";
        			} else if (fileSize < 1048576) { //1 MiB
        				humanReadableFileSize = (new DecimalFormat("###.##")).format((fileSize*1.0)/1024.0)+" KiB";
        			} else {
        				final long mb = fileSize / 1048576;
        				final long remainder = fileSize % 1048576;
        				humanReadableFileSize = (new DecimalFormat("###.##")).format(mb + (remainder*1.0)/1048576.0)+" MiB";
        			}
        			if (injectorsAndIterationMarkerIsActive != null) {
        				injectorsAndIterationMarkerIsActive.put(injectorNameToInjectorMap.get(zipEntry.getName()), iterationMarkerIsActive);
        			}
        			table.setNumberOfIterationsAndHumanReadableFileSize(injectorToRowMap.get(injectorNameToInjectorMap.get(zipEntry.getName())), new IterationCountAndHumanReadableFileSize(iterationCount, humanReadableFileSize));
                }
            }
		}
		finally
		{
			IOUtils.closeQuietly(zipInputStream);
		}
	}

	public class IterationRuntimeMeasurement {

		private class IterationDuration {
			private final Date timestamp;
			private Set<Integer> iterationNumbers;

			public IterationDuration() {
				this.timestamp = new Date();
				this.iterationNumbers = new HashSet<Integer>();
			}

			public void markIterationNumber(final int iterationNumber) {
				this.iterationNumbers.add(iterationNumber);
			}

			public Set<Integer> getIterationNumbers() {
				return this.iterationNumbers;
			}

			public Date getTimestamp() {
				return timestamp;
			}
		}

		private final int LIST_CAPACITY = 2;
		private final List<IterationDuration> iterationDurationList = new ArrayList<IterationDuration>(LIST_CAPACITY);
		private final int rowIndex;
		private final Injector injector;
		private Integer iterationsPerMinute = null;
		private Long estimatedRemainingTime = null;

		public IterationRuntimeMeasurement(final Injector injector, final int rowIndex) {
			this.injector = injector;
			this.rowIndex = rowIndex;
		}

		public void markStart() {
			IterationDuration runningIteration = new IterationDuration();
			Assert.isTrue(iterationDurationList.size() <= LIST_CAPACITY, "iterationDurationList size ("+iterationDurationList.size()+" elements) is exeeding its capacity ("+LIST_CAPACITY+" elements)");
			if (iterationDurationList.size() == LIST_CAPACITY) {
				if (iterationDurationList.get(LIST_CAPACITY-1).getIterationNumbers().isEmpty()) {
					iterationDurationList.remove(LIST_CAPACITY-1);
				} else {
					iterationDurationList.remove(0);
				}
			}
			iterationDurationList.add(runningIteration);
		}

		public void markIteration(final int iterationNumber) {
			Assert.isTrue(!iterationDurationList.isEmpty(), "markStart() was not executed when the injector was started or during log download");
			iterationDurationList.get(iterationDurationList.size()-1).markIterationNumber(iterationNumber);
		}

		public void displayNoIterationHasBeenCompletedRecently() {
			this.iterationsPerMinute = 0;
			this.estimatedRemainingTime = null;
			updateExecutionTime(this.rowIndex, null);
		}

		public void setLastSuccessfulIteration(final Injector injector, final int iterationNumber, final File logFileForViewing) {
			updateCell(this.rowIndex, INJECTION_SCREEN_COLUMN.LAST_SUCCESSFUL, String.valueOf(iterationNumber));
			injectorIterationSelectionFrame.updateStatus(injector, iterationNumber, SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE, actionManager.getId(), logFileForViewing);
		}

		public void setLastFailedIteration(final Injector injector, final int iterationNumber, final boolean causedByManualStop, final File logFileForViewing) {
			updateCell(this.rowIndex, INJECTION_SCREEN_COLUMN.LAST_FAILED, String.valueOf(iterationNumber));
			injectorIterationSelectionFrame.updateStatus(injector, iterationNumber, causedByManualStop ? SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE : SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE, actionManager.getId(), logFileForViewing);
		}

		public void displayExecutionRuntimeMeasurement(final int numberOfSuccessfulIterations, final int numberOfFailedIterations) {
			if (injectorsAndIterationMarkerIsActive.get(injectors.get(this.rowIndex))) {
				final int numberOfIterations = Integer.parseInt(((DefaultTableModel) table.getModel()).getValueAt(this.rowIndex, INJECTION_SCREEN_COLUMN.SELECTED.getIndex()).toString());

				updateSuccessfulIterationTally(this.rowIndex, numberOfSuccessfulIterations);
				updateFailedIterationTally(this.rowIndex, numberOfFailedIterations);
				updateRemainingIterationTally(this.rowIndex, numberOfIterations-numberOfSuccessfulIterations-numberOfFailedIterations);

				Integer partitionCapacity = null;
				if (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injectors.get(this.rowIndex).getType())
						|| CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injectors.get(this.rowIndex).getType())) {
					partitionCapacity = Config.getBuildFldSplitMaxCount();
				} else if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injectors.get(this.rowIndex).getType())) {
					partitionCapacity = Config.getBuildHtmlSplitMaxCount();
				}

				if (partitionCapacity != null) {
					updatePartitionsTracker(this.rowIndex, numberOfSuccessfulIterations+numberOfFailedIterations, numberOfIterations, partitionCapacity);
				}

				if (iterationDurationList.size() == LIST_CAPACITY) {
					if (iterationDurationList.get(LIST_CAPACITY-1).getIterationNumbers().isEmpty()) {
						displayNoIterationHasBeenCompletedRecently();
					} else {
						final long totalDuration = iterationDurationList.get(LIST_CAPACITY-1).getTimestamp().getTime() - iterationDurationList.get(LIST_CAPACITY-2).getTimestamp().getTime();
						final long averageDuration = totalDuration / (iterationDurationList.get(LIST_CAPACITY-1).getIterationNumbers().size());
						this.iterationsPerMinute = (int) (60 * 1000L/averageDuration);
						long remainingNumberOfIterations = numberOfIterations - numberOfSuccessfulIterations;
						this.estimatedRemainingTime = averageDuration * remainingNumberOfIterations;
						updateExecutionTime(this.rowIndex, null);
					}

				}
			}
		}

		public int getRowIndex() {
			return rowIndex;
		}

		public Integer getIterationsPerMinute() {
			return iterationsPerMinute;
		}

		public Long getEstimatedRemainingTime() {
			return estimatedRemainingTime;
		}

		public Injector getInjector() {
			return injector;
		}
	}


	public void initiateNewIterationRuntimeMeasurement(final Injector injector, final int rowIndex, final boolean clearExecutionTimeCell) {
		this.iterationRuntimeMeasurement = new IterationRuntimeMeasurement(injector, rowIndex);
		if (clearExecutionTimeCell) {
			updateExecutionTime(rowIndex, null);
		}
	}

	public IterationRuntimeMeasurement getIterationRuntimeMeasurement() {
		return this.iterationRuntimeMeasurement;
	}

	private void updateCell(final int rowId, final INJECTION_SCREEN_COLUMN column, final String message)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				((DefaultTableModel) table.getModel()).setValueAt(message, rowId, column.getIndex());
				((DefaultTableModel) table.getModel()).fireTableCellUpdated(rowId, column.getIndex());
				table.updateUI();
			}
		});
	}

	public static class IterationStatus {
		private final String status;
		private final Date lastTimestamp;
		private final long actionManagerId;
		private File logFileForViewing;
		
		public IterationStatus(String status, Date lastTimestamp, long actionManagerId, File logFileForViewing) {
			this.status = status;
			this.lastTimestamp = lastTimestamp;
			this.actionManagerId = actionManagerId;
			this.logFileForViewing = null;
			this.addLogFileForViewing(logFileForViewing);
		}

		public String getStatus() {
			return status;
		}

		public Date getLastTimestamp() {
			return lastTimestamp;
		}
		
		public boolean hasMatchingActionManagerId(long actionManagerId) {
			return this.actionManagerId == actionManagerId;
		}
		
		public long getActionManagerId() {
			return this.actionManagerId;
		}
		
		public void addLogFileForViewing(File logFileForViewing) {
			this.logFileForViewing = logFileForViewing;
		}
		
		public List<File> getLogFileForViewingSingletonList() {
			if (this.logFileForViewing == null) {
				return new ArrayList<File>();
			} else {
				return Collections.singletonList(this.logFileForViewing);
			}
		}
		
		public File getLogFileForViewing() {
			return this.logFileForViewing;
		}
	}
	
	private boolean hasSuccessfulIterationTheLastTimeTheInjectorWasExecuted(final Injector injector) {
		for (int i = this.successfulAndFailedIterationsForAllSessions.size() - 1 ; i >= 0 ; i--) {
			if (this.successfulAndFailedIterationsForAllSessions.get(i).hasInjected(injector)) {
				return this.successfulAndFailedIterationsForAllSessions.get(i).hasSuccessfulIteration(injector);
			}
		}
		return false;
	}

	public Map<Injector, File> getLogMismatchesLog() {
		return logMismatchesLog;
	}
	
	private Set<Integer> getSuccessfulIterationsTheLastTimeTheInjectorWasExecuted(final Injector injector) {
		for (int i = this.successfulAndFailedIterationsForAllSessions.size() - 1 ; i >= 0 ; i--) {
			if (this.successfulAndFailedIterationsForAllSessions.get(i).hasInjected(injector)) {
				return this.successfulAndFailedIterationsForAllSessions.get(i).getSuccessfulIterations().get(injector);
			}
		}
		return new HashSet<Integer>();
	}	
	
	private boolean hasFailedIterationTheLastTimeTheInjectorWasExecuted(final Injector injector) {
		for (int i = this.successfulAndFailedIterationsForAllSessions.size() - 1 ; i >= 0 ; i--) {
			if (this.successfulAndFailedIterationsForAllSessions.get(i).hasInjected(injector)) {
				return this.successfulAndFailedIterationsForAllSessions.get(i).hasFailedIteration(injector);
			}
		}
		return false;
	}	
	
	private Set<Integer> getFailedIterationsTheLastTimeTheInjectorWasExecuted(final Injector injector) {
		for (int i = this.successfulAndFailedIterationsForAllSessions.size() - 1 ; i >= 0 ; i--) {
			if (this.successfulAndFailedIterationsForAllSessions.get(i).hasInjected(injector)) {
				return this.successfulAndFailedIterationsForAllSessions.get(i).getFailedIterations().get(injector);
			}
		}
		return new HashSet<Integer>();
	}	
	
	private List<String> getPartitionNamesTheLastTimeTheInjectorWasExecuted(final Injector injector) {
		for (int i = this.injectorIndexToPartitionNameMapForAllSessions.size() - 1 ; i >= 0 ; i--) {
			if (this.injectorIndexToPartitionNameMapForAllSessions.get(i).containsKey(injector.getIndex())) {
				return this.injectorIndexToPartitionNameMapForAllSessions.get(i).get(injector.getIndex());
			}
		}
		return new ArrayList<String>();
	}
	
	public boolean isBuildOpenScriptAndLogInHtmlByDefaultChecked() {
		if (this.buildOpenScriptAndLogInHtmlByDefault == null) {
			return Config.getBuildOpenScriptAndLogInHtmlByDefault();
		}
		return this.buildOpenScriptAndLogInHtmlByDefault.isSelected();
	}

//	public List<File> getConvertedInjectorToHTMLList() {
//		return convertedInjectorToHTMLList;
//	}
}