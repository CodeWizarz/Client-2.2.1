package com.rapidesuite.client.common.gui.datagrid;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

import org.openswing.swing.client.CopyButton;
import org.openswing.swing.client.DeleteButton;
import org.openswing.swing.client.EditButton;
import org.openswing.swing.client.FilterButton;
import org.openswing.swing.client.GenericButton;
import org.openswing.swing.client.GridControl;
import org.openswing.swing.client.ImportButton;
import org.openswing.swing.client.InsertButton;
import org.openswing.swing.client.NavigatorBar;
import org.openswing.swing.client.ReloadButton;
import org.openswing.swing.client.SaveButton;
import org.openswing.swing.table.client.GridStatusPanel;
import org.openswing.swing.table.columns.client.Column;
import org.openswing.swing.util.client.ClientSettings;

@SuppressWarnings("serial")
public class DataGridPanel extends JPanel {

	private GridControl gridControl;
	private FlowLayout buttonsFlowLayout;
	private NavigatorBar navigatorBar;
	private JPanel buttonsPanel;
	private boolean hasNavigationBar;	
	private GridStatusPanel gridStatusPanel;
	private List<Column> columns;
	private String dataGridRowClassName;
	private JLabel relativeRecordLabel;
	private List<GenericButton> buttons;
	private int numberOfLockedColumns;
	
	public DataGridPanel(
			List<Column> columns,
			String dataGridRowClassName,
			boolean hasNavigationBar,
			List<GenericButton> buttons,
			int numberOfLockedColumns) throws Exception {
		this.numberOfLockedColumns=numberOfLockedColumns;
		this.columns=columns;
		this.buttons=buttons;
		this.hasNavigationBar=hasNavigationBar;
		this.dataGridRowClassName=dataGridRowClassName;
		initComponents();
	}
	
	private void initComponents() throws Exception {
		gridControl = new GridControl();
		gridControl.setFilterDialogDimension(new Dimension(DataGridConstants.FRAME_MASS_UPDATE_WIDTH, DataGridConstants.FRAME_MASS_UPDATE_HEIGHT));
		buttonsFlowLayout = new FlowLayout();
		buttonsFlowLayout.setAlignment(FlowLayout.LEFT);
		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(buttonsFlowLayout);
		this.setLayout(new BorderLayout());
		this.add(gridControl, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.NORTH);
		
		addRelativeRecordCountLabelToGridStatusPanel();
		initButtons();
				
		if (hasNavigationBar) {
			navigatorBar = new NavigatorBar();
			buttonsPanel.add(navigatorBar, null);
			gridControl.setNavBar(navigatorBar);
			gridControl.setShowPageNumber(true);
		}
		
		gridControl.setAutoLoadData(false);
		gridControl.setEditOnSingleRow(false);
		gridControl.setInsertRowsOnTop(true);
		gridControl.setHeaderHeight(ClientSettings.HEADER_HEIGHT);
		gridControl.setLockedColumns(numberOfLockedColumns);
		//gridControl.enableDrag(null);
		// press the Down key
		gridControl.setMaxNumberOfRowsOnInsert(10);
		/*
		 * LOCKING COLUMNS disable multiple row selection
		 */
		gridControl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		gridControl.setShowFilterPanelOnGrid(false);
		gridControl.setReorderingAllowed(false); 
		gridControl.setShowWarnMessageBeforeReloading(true);
		gridControl.setColorsInReadOnlyMode(true);
		
		for (Column column:columns) {
			gridControl.getColumnContainer().add(column, null);
		}
		gridControl.setValueObjectClassName(dataGridRowClassName);
	}
	
	public GridControl getGridControl() {
		return gridControl;
	}

	public String getDataGridRowClassName() {
		return dataGridRowClassName;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public NavigatorBar getNavigatorBar() {
		return navigatorBar;
	}
	
	private void retrieveGridStatusPanel() {
		Component[] components=this.gridControl.getComponents();
		for (Component c:components) {
			Component[] components1=((JPanel)c).getComponents();
			for (Component c1:components1) {
				if (c1 instanceof GridStatusPanel) {
					gridStatusPanel=(GridStatusPanel)c1;
					break;
				}
			}
			break;
		}
	}
	
	public void setLockedGridScrollPaneDimension() {
		Component[] components=this.gridControl.getTable().getComponents();
		for (Component c:components) {
			if (c instanceof JScrollPane) {
				JScrollPane lockedscroll=(JScrollPane)c;
				int width=5;
				int height=-1;
				lockedscroll.setMinimumSize(new Dimension(width,height));
				lockedscroll.setMaximumSize(new Dimension(width,height));
				lockedscroll.setPreferredSize(new Dimension(width,height));
				break;
			}
		}
	}
	
	private void initButtons() {
		for (GenericButton button:buttons) {
			buttonsPanel.add(button, null);
			if (button instanceof ReloadButton) {
				gridControl.setReloadButton((ReloadButton)button);
			}
			else
			if (button instanceof SaveButton) {
				gridControl.setSaveButton((SaveButton)button);
			}
			else
			if (button instanceof FilterButton) {
				gridControl.setFilterButton((FilterButton)button);
			}
			else
			if (button instanceof EditButton) {
				gridControl.setEditButton((EditButton)button);
			}
			else
			if (button instanceof InsertButton) {
				gridControl.setInsertButton((InsertButton)button);
			}
			else
			if (button instanceof DeleteButton) {
				gridControl.setDeleteButton((DeleteButton)button);
			}
			else
			if (button instanceof CopyButton) {
				gridControl.setCopyButton((CopyButton)button);
			}
			else {
				gridControl.addGenericButton(button);
				button.addDataController(gridControl.getTable()); 
			}
		}
	}
	
	public void addRelativeRecordCountLabelToGridStatusPanel() {
		retrieveGridStatusPanel();
		relativeRecordLabel = new JLabel("");
		Border border = BorderFactory.createLoweredBevelBorder();
		relativeRecordLabel.setLayout(new BorderLayout());
		relativeRecordLabel.setBorder(border);
		relativeRecordLabel.setMinimumSize(new Dimension(300,20));
		relativeRecordLabel.setPreferredSize(new Dimension(300,20));
		gridStatusPanel.add(relativeRecordLabel, BorderLayout.EAST);
	}

	public void displayRelativeRecordCount(int relativeRecordCount,int totalRecordCount,boolean isFilteringOn) {
		String text="Total rows count: "+totalRecordCount;
		if (isFilteringOn) {
			text+=" (Filtering returned "+relativeRecordCount+" rows)";
		}
		relativeRecordLabel.setText(text);
	}
	
	public void resetDisplayRelativeRecordCount() {
		relativeRecordLabel.setText("");
	}

	public ReloadButton getReloadButton() {
		return gridControl.getReloadButton();
	}
	
	public FilterButton getFilterButton() {
		return gridControl.getFilterButton();
	}
	
	public ImportButton getImportButton() {
		return gridControl.getImportButton();
	}
	
	public DeleteButton getDeleteButton() {
		return gridControl.getDeleteButton();
	}
	
	public CopyButton getCopyButton() {
		return gridControl.getCopyButton();
	}
	
	public InsertButton getInsertButton() {
		return gridControl.getInsertButton();
	}
	
	public void setButtonsStatus(boolean isEnabled) {
		for (GenericButton button:buttons) {
			button.setEnabled(isEnabled);
		}
	}

	public List<GenericButton> getButtons() {
		return buttons;
	}
	
}
