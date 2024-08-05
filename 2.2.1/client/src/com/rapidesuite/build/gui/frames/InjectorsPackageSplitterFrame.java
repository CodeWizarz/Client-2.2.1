package com.rapidesuite.build.gui.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.springframework.util.Assert;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.gui.EachRowEditor;
import com.rapidesuite.build.gui.EachRowRenderer;
import com.rapidesuite.build.gui.RadioButtonEditor;
import com.rapidesuite.build.gui.RadioButtonRenderer;
import com.rapidesuite.build.gui.panels.InjectorsPackageExecutionPanel;
import com.rapidesuite.build.utils.HasIterationCountAndFileSizeColumns;
import com.rapidesuite.build.utils.InjectorsPackageUtils;
import com.rapidesuite.build.utils.IterationCountAndHumanReadableFileSize;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.core.CoreConstants;

@SuppressWarnings("serial")
public class InjectorsPackageSplitterFrame extends JFrame implements HasIterationCountAndFileSizeColumns
{

	private JTable scriptsTable;
	private JTextField iterationBatchTextField;
	private JTextField outputFolderTextField;
	private JButton outputFolderButton;
	private JButton startSplitButton;
	private InjectorSplittingStopButton stopButton;
	private BuildMain BuildMain;
	private JFileChooser loadFolderChooser;
	private File outputFolder;
	private DefaultTableModel tableModel;
	private RadioButtonRenderer radioButtonRenderer;
	private RadioButtonEditor radioButtonEditor;
	private EachRowRenderer rowRenderer;
	private EachRowEditor rowEditor;
	private JLabel iterationParsingCommentLabel;
	private JProgressBar iterationParsingProgressBar;
	private JLabel encryptionCommentLabel;
	private JProgressBar encryptionProgressBar;
	private JLabel bwpWritingCommentLabel;
	private JProgressBar bwpWritingProgressBar;
	
	public class InjectorSplittingStopButton extends JButton {

		private volatile boolean isManuallyStopped = false;
		
		public class InjectorSplittingManualStopException extends RuntimeException {}
		
		public InjectorSplittingStopButton(final String text, final Icon icon) {
			super(text, icon);
			this.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					markManuallyStopped();
					InjectorSplittingStopButton.this.setEnabled(false);
				}
				
			});
			this.setEnabled(false);
		}
		
		private synchronized void markManuallyStopped() {
			this.isManuallyStopped = true;
		}
		
		public synchronized void markStart() {
			this.setEnabled(true);
			this.isManuallyStopped = false;
		}
		
		public synchronized void throwExceptionIfManuallyStopped() {
			if (this.isManuallyStopped) {
				throw new InjectorSplittingManualStopException();
			}
		}
		
		@Override
		public void setEnabled(final boolean enabled) {
			super.setEnabled(enabled);
			if (startSplitButton != null) {
				startSplitButton.setEnabled(!enabled);
			}
		}
	}
	
	private static enum INJECTOR_SPLITTER_COLUMN {
		SELECTION(0, "", new BigDecimal("0.056")), 
		INJECTOR_NAME(1, "NAME", new BigDecimal("0.664")), 
		TOTAL(2, "TOTAL", new BigDecimal("0.14")),
		FILE_SIZE(3, "SIZE", new BigDecimal("0.14"));
		
		private final int index;
		private final String header;
		private final BigDecimal proportion;
		
		private INJECTOR_SPLITTER_COLUMN(final int index, final String header, final BigDecimal proportion) {
			this.index = index;
			this.header = header;
			this.proportion = proportion;
		}
	
		public int getIndex() {
			return index;
		}
	
		@Override
		public String toString() {
			return header;
		}
		
		public static INJECTOR_SPLITTER_COLUMN[] getSorted() {
			List<INJECTOR_SPLITTER_COLUMN> list = Arrays.asList(INJECTOR_SPLITTER_COLUMN.values());
			Collections.sort(list, new Comparator<INJECTOR_SPLITTER_COLUMN>() {
				@Override
				public int compare(INJECTOR_SPLITTER_COLUMN o1,
						INJECTOR_SPLITTER_COLUMN o2) {
					if (o1.getIndex() < o2.getIndex()) {
						return -1;
					} else if (o1.getIndex() > o2.getIndex()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			return list.toArray(new INJECTOR_SPLITTER_COLUMN[INJECTOR_SPLITTER_COLUMN.values().length]);
		}
		
		public static String[] getSortedHeaders() {
			INJECTOR_SPLITTER_COLUMN[] columns = getSorted();
			String[] output = new String[columns.length];
			for (int i = 0 ; i < columns.length ; i++) {
				output[i] = columns[i].toString();
			}
			return output;
		}

		public double getProportion() {
			return proportion.doubleValue();
		}
		
		public BigDecimal getProportionExact() {
			return proportion;
		}
	}
	
	static {
		INJECTOR_SPLITTER_COLUMN[] columns = INJECTOR_SPLITTER_COLUMN.getSorted();
		BigDecimal totalProportion = new BigDecimal(0);
		for (int i = 0 ; i < columns.length ; i++) {
			Assert.isTrue(i == columns[i].getIndex(), "INJECTOR_SPLITTER_COLUMN columns are not ordered");
			totalProportion = totalProportion.add(columns[i].getProportionExact());
		}
		Assert.isTrue(BigDecimal.ONE.compareTo(totalProportion) == 0, "Build injector splitting screen column proportions sum to "+totalProportion.toPlainString()+" while they should sum to 1");
	}	
	
	private static final int SCRIPT_TABLE_WIDTH = 607;
	private static final int SCRIPT_SCROLL_PANE_WIDTH = 627;
	private static final int WIDTH = 647;
	private static final int HEIGHT = (int) (WIDTH * 2.0 / (1.0 + Math.sqrt(5.0))); //golden ratio

	public InjectorsPackageSplitterFrame(BuildMain BuildMain)
	{
		this.BuildMain = BuildMain;
		try
		{
			initComponents();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	private void initComponents() throws Exception
	{
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				Frame frame = (Frame) we.getSource();
				frame.dispose();
			}
		});
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.setTitle("Injectors split");
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel northPanel = new JPanel();
		this.add(northPanel, BorderLayout.NORTH);
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

		JPanel tempPanel = new JPanel();
		northPanel.add(tempPanel);
		tempPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JLabel label = new JLabel("Select the injector to split");
		tempPanel.add(label);

		JPanel centralPanel = new JPanel();
		this.add(centralPanel, BorderLayout.CENTER);
		centralPanel.setLayout(new BorderLayout());
		scriptsTable = new JTable() {
			@Override
			public void changeSelection(int rowIndex,
	                   int columnIndex,
	                   boolean toggle,
	                   boolean extend) {
				if (this.getSelectedRow() == rowIndex) {
					super.clearSelection();
				} else {
					super.changeSelection(rowIndex, columnIndex, false, false);
				}
				final int selectedRowIndex = this.getSelectedRow();
				for (int i = 0 ; i < this.getRowCount() ; i++) {
					this.setValueAt(i == selectedRowIndex, i, INJECTOR_SPLITTER_COLUMN.SELECTION.getIndex());
				}
			}
		};
		tableModel = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				if ( colIndex == 1 )
				{
					return false;
				}
				return true;
			}
			
			@Override
			public void setValueAt(final Object value, final int row, final int column) {
				if (column == 0 && value != null && ((Boolean) value).booleanValue()) {
					for (int i = 0 ; i < this.getRowCount(); i++) {
						if (i == row) {
							super.setValueAt(true, i, column);
						} else {
							super.setValueAt(false, i, column);
						}
					}
				} else {
					super.setValueAt(value, row, column);
				}
			}
		};
		tableModel.setDataVector(null, INJECTOR_SPLITTER_COLUMN.getSortedHeaders());
		scriptsTable.setModel(tableModel);
		rowRenderer = new EachRowRenderer();
		radioButtonRenderer = new RadioButtonRenderer();
		JCheckBox cb = new JCheckBox();
		cb.setHorizontalAlignment(SwingConstants.CENTER);
		radioButtonEditor = new RadioButtonEditor(cb);
		rowEditor = new EachRowEditor(scriptsTable);
		scriptsTable.getColumn(INJECTOR_SPLITTER_COLUMN.SELECTION.toString()).setCellRenderer(rowRenderer);
		scriptsTable.getColumn(INJECTOR_SPLITTER_COLUMN.SELECTION.toString()).setCellEditor(rowEditor);
		
		for (INJECTOR_SPLITTER_COLUMN isc : INJECTOR_SPLITTER_COLUMN.getSorted()) {
			scriptsTable.getColumnModel().getColumn(isc.getIndex()).setPreferredWidth((int) (SCRIPT_TABLE_WIDTH * isc.getProportion()));
		}

		((DefaultTableCellRenderer) scriptsTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		scriptsTable.getColumnModel().getColumn(INJECTOR_SPLITTER_COLUMN.TOTAL.getIndex()).setCellRenderer(dtcr);
		scriptsTable.getColumnModel().getColumn(INJECTOR_SPLITTER_COLUMN.FILE_SIZE.getIndex()).setCellRenderer(dtcr);
		
		final JScrollPane scriptsScrollPane = new JScrollPane(scriptsTable);
		scriptsScrollPane.setPreferredSize(new Dimension(SCRIPT_SCROLL_PANE_WIDTH, 350));
		scriptsTable.setPreferredScrollableViewportSize(new Dimension(SCRIPT_TABLE_WIDTH, 100));
		centralPanel.add(scriptsScrollPane, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		this.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));

		tempPanel = new JPanel();
		southPanel.add(tempPanel);
		tempPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		label = new JLabel("Select the destination folder for the new package:");
		outputFolderTextField = GUIUtils.getInputField(true, false);
		loadFolderChooser = Utils.initializeJFileChooserWithTheLastPath("LOAD_FOLDER_INJECTORS_PACKAGE_SPLITTER_FRAME");
		loadFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		outputFolderButton = GUIUtils.getButton(BuildMain.getClass(), "Browse", "/images/open16.gif");
		outputFolderButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					openFolder();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});
		tempPanel.add(label);
		tempPanel.add(outputFolderTextField);
		tempPanel.add(outputFolderButton);
		
		tempPanel = new JPanel();
		tempPanel.setLayout(new BorderLayout(10, 0));
		iterationParsingCommentLabel = new JLabel(" ");
		tempPanel.add(new JLabel(), BorderLayout.WEST);
		tempPanel.add(iterationParsingCommentLabel, BorderLayout.CENTER);
		southPanel.add(tempPanel);
		
		tempPanel = new JPanel();
		tempPanel.setLayout(new BorderLayout(10, 0));
		iterationParsingProgressBar = new JProgressBar();
		iterationParsingProgressBar.setMinimum(0);
		iterationParsingProgressBar.setMaximum(100);
		iterationParsingProgressBar.setValue(0);
		iterationParsingProgressBar.setStringPainted(true);
		tempPanel.add(new JLabel(), BorderLayout.WEST);
		tempPanel.add(iterationParsingProgressBar, BorderLayout.CENTER);
		tempPanel.add(new JLabel(), BorderLayout.EAST);
		southPanel.add(tempPanel);
		
		tempPanel = new JPanel();
		tempPanel.setLayout(new BorderLayout(10, 0));
		encryptionCommentLabel = new JLabel(" ");
		tempPanel.add(new JLabel(), BorderLayout.WEST);
		tempPanel.add(encryptionCommentLabel, BorderLayout.CENTER);
		southPanel.add(tempPanel);
		
		tempPanel = new JPanel();
		tempPanel.setLayout(new BorderLayout(10, 0));
		encryptionProgressBar = new JProgressBar();
		encryptionProgressBar.setMinimum(0);
		encryptionProgressBar.setMaximum(100);
		encryptionProgressBar.setValue(0);
		encryptionProgressBar.setStringPainted(true);
		tempPanel.add(new JLabel(), BorderLayout.WEST);
		tempPanel.add(encryptionProgressBar, BorderLayout.CENTER);
		tempPanel.add(new JLabel(), BorderLayout.EAST);
		southPanel.add(tempPanel);
		
		tempPanel = new JPanel();
		tempPanel.setLayout(new BorderLayout(10, 0));
		bwpWritingCommentLabel = new JLabel(" ");
		tempPanel.add(new JLabel(), BorderLayout.WEST);
		tempPanel.add(bwpWritingCommentLabel, BorderLayout.CENTER);
		southPanel.add(tempPanel);		
		
		tempPanel = new JPanel();
		tempPanel.setLayout(new BorderLayout(10, 0));
		bwpWritingProgressBar = new JProgressBar();
		bwpWritingProgressBar.setMinimum(0);
		bwpWritingProgressBar.setMaximum(100);
		bwpWritingProgressBar.setValue(0);
		bwpWritingProgressBar.setStringPainted(true);
		tempPanel.add(new JLabel(), BorderLayout.WEST);
		tempPanel.add(bwpWritingProgressBar, BorderLayout.CENTER);
		tempPanel.add(new JLabel(), BorderLayout.EAST);
		southPanel.add(tempPanel);

		label = new JLabel("Create a new package every X iterations:");
		iterationBatchTextField = new JTextField(10);
		iterationBatchTextField.setText("20");
		startSplitButton = GUIUtils.getButton(BuildMain.getClass(), "Start", "/images/play16.gif");
		startSplitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					startSplitOperation();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		});
		stopButton = new InjectorSplittingStopButton("Stop", GUIUtils.getImageIcon(BuildMain.class,"/images/stop16.gif"));

		tempPanel = new JPanel();
		southPanel.add(tempPanel);
		tempPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		tempPanel.add(label);
		tempPanel.add(iterationBatchTextField);
		tempPanel.add(startSplitButton);
		tempPanel.add(stopButton);

		setSize(new Dimension(WIDTH, HEIGHT));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
	        @Override
	        public boolean dispatchKeyEvent(KeyEvent e) {
	        	if (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //esc
					setVisible(false);
				}
	            return false;
	        }        	
        });	
	}

	void openFolder()
	{
		loadFolderChooser.setCurrentDirectory(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile());
		int returnVal = loadFolderChooser.showDialog(this, "Open");

		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			outputFolder = loadFolderChooser.getSelectedFile();
			outputFolderTextField.setText(outputFolder.getAbsolutePath());
		}
	}

	public void setupTableScripts() throws Exception
	{
		GUIUtils.deleteAllRows(scriptsTable);
		List<Injector> scripts = new ArrayList<Injector>();
		scripts.addAll(InjectorsPackageUtils.getInjectors(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), 
				new HashSet<CoreConstants.INJECTOR_TYPE>(Arrays.asList(new CoreConstants.INJECTOR_TYPE[]{
						CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM, CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM, CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM}))
				));
		int scriptsCount = scripts.size();

		outputFolder = BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile().getParentFile();
		outputFolderTextField.setText(outputFolder.getAbsolutePath());
		loadFolderChooser.setCurrentDirectory(outputFolder);

		final Map<Injector, Integer> injectorToRowMap = new HashMap<Injector, Integer>();
		
		for ( int i = 0; i < scriptsCount; i++ )
		{
			Injector injector = scripts.get(i);
			Object[] row = new Object[2];
			row[0] = Boolean.FALSE;
			row[1] = injector.getName();
			tableModel.addRow(row);

			int rowCount = tableModel.getRowCount() - 1;
			rowRenderer.add(rowCount, radioButtonRenderer);
			rowEditor.setEditorAt(rowCount, radioButtonEditor);
			injectorToRowMap.put(injector, i);
		}
		
		InjectorsPackageExecutionPanel.writeIterationCountAndFileSize(injectorToRowMap,
				BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(),
				null,
				this);
	}
	
	public Integer getSelectedRowIndex() throws Exception
	{
		int rowCount = scriptsTable.getRowCount() - 1;
		Integer output = null;
		for ( int i = 0; i <= rowCount; i++ )
		{
			Boolean isChecked = (Boolean) tableModel.getValueAt(i, 0);
			if ( isChecked )
			{
				if (output == null) {
					output = i;
				} else {
					throw new Exception("You must select only one injector at a time.");
				}
			}
		}
		return output;
	}

	void startSplitOperation() throws Exception
	{
		Integer selectedRowIndex = getSelectedRowIndex();
		if (selectedRowIndex == null) {
			GUIUtils.popupErrorMessage("You must select exactly one injector to split.");
			return;			
		}
		String selectedScriptName = (String) tableModel.getValueAt(selectedRowIndex, INJECTOR_SPLITTER_COLUMN.INJECTOR_NAME.getIndex());

		String iterationBatchText = iterationBatchTextField.getText();
		if ( iterationBatchText == null || iterationBatchText.equals("") )
		{
			GUIUtils.popupErrorMessage("You must enter a value for the iteration batch.");
			return;
		}
		final int MIN_ITERATION_BATCH_SIZE = 2;
		int iterationBatchSize = -1;
		try
		{
			iterationBatchSize = Integer.valueOf(iterationBatchText).intValue();
			if ( iterationBatchSize < MIN_ITERATION_BATCH_SIZE )
			{
				GUIUtils.popupErrorMessage("You must enter a number greater than 1 for the iteration batch.");
				return;
			}
		}
		catch ( Exception e )
		{
			GUIUtils.popupErrorMessage("You must enter a number greater than 1 for the iteration batch.");
			return;
		}

		if ( outputFolder == null || !outputFolder.isDirectory() )
		{
			GUIUtils.popupErrorMessage("You must select a destination folder.");
			return;
		}
		
		final int iterationsCount = (int) tableModel.getValueAt(selectedRowIndex, INJECTOR_SPLITTER_COLUMN.TOTAL.getIndex());
		if (iterationsCount <= MIN_ITERATION_BATCH_SIZE) {
			GUIUtils.popupErrorMessage("Only injector with "+INJECTOR_SPLITTER_COLUMN.TOTAL.toString()+" >= "+MIN_ITERATION_BATCH_SIZE+" can be split");
			return;
		} else if (iterationsCount <= iterationBatchSize) {
			GUIUtils.popupErrorMessage("Iteration batch size must be smaller than the number of iterations");
			return;			
		}

		InjectorsPackageUtils.createZIPArchives(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(),
				iterationBatchSize,
				selectedScriptName,
				InjectorsPackageUtils.getInjectorType(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), selectedScriptName),
				"splitInjectorsPackage-" + selectedScriptName,
				outputFolder,
				iterationParsingCommentLabel,
				iterationParsingProgressBar,
				encryptionCommentLabel,
				encryptionProgressBar,
				bwpWritingCommentLabel,
				bwpWritingProgressBar,
				stopButton,
				scriptsTable,
				iterationsCount);
	}

	@Override
	public void setNumberOfIterationsAndHumanReadableFileSize(
			int row,
			IterationCountAndHumanReadableFileSize numberOfIterationsAndHumanReadableFileSize) {
		scriptsTable.setValueAt(numberOfIterationsAndHumanReadableFileSize.getIterationCount(), row, INJECTOR_SPLITTER_COLUMN.TOTAL.getIndex());
		scriptsTable.setValueAt(numberOfIterationsAndHumanReadableFileSize.getHumanReadableFileSize(), row, INJECTOR_SPLITTER_COLUMN.FILE_SIZE.getIndex());	
		scriptsTable.repaint();
	}

}