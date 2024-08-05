package com.rapidesuite.inject.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.apache.commons.collections.CollectionUtils;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.inject.RecordTracker;
import com.rapidesuite.inject.ScriptGridTracker;

@SuppressWarnings("serial")
public abstract class AbstractGrid extends JPanel {
	
	public static String COLUMN_HEADING_SELECTION="Selection";

	public final static String STATUS_PENDING="Pending";
	public final static String STATUS_PROCESSING="Processing";
	public final static String STATUS_SUCCESS="Success";
	public final static String STATUS_FAILED="Failed";
	public final static String STATUS_RETRY="Retry";
	public final static String STATUS_QUEUED="Queued";
	public final static String STATUS_STOPPED="Stopped";
	
	public static final Color redColor=Color.decode("#E85129");
	public static final Color yellowColor=new Color(255,255,136);//Color.decode("#FFFFD9");
	public static final Color blueColor=new Color(0,162,232 );
	public static final Color greenColor=Color.decode("#59A203");
	public static final Color greyColor=new Color(249,249,249);
	
	protected JTable table;

	public JTable getTable() {
		return table;
	}
	
	public static void setPopupMenu(final AbstractGrid grid) {
		final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Select");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	ScriptsGrid.setSelectionOnRows(grid,true,false);
            }
        });
        popupMenu.add(item);
        
        item = new JMenuItem("Select All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	ScriptsGrid.setSelectionOnRows(grid,true,true);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	ScriptsGrid.setSelectionOnRows(grid,false,false);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	ScriptsGrid.setSelectionOnRows(grid,false,true);
            }
        });
        popupMenu.add(item);
        grid.getTable().setComponentPopupMenu(popupMenu);
	}
	
	public static void setSelectionOnRows(final AbstractGrid grid, final boolean isSelected,final boolean isAllRows) {
		setSelectionOnRows(grid, null, isSelected, isAllRows, true, true);
	}
		
	public static void setSelectionOnRows(final AbstractGrid grid, final List<ScriptGridTracker> scriptGridTrackers, final boolean isSelected,final boolean isAllRows, final boolean selectSuccess, final boolean propagateFurther) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JTable table=grid.getTable();

				List<RecordTracker> recordTrackersList = null;
				if (grid instanceof TableGrid) {
					recordTrackersList = ((TableGrid)grid).getRecordTrackerList();
				}

				int[] rows=null;
				if(CollectionUtils.isNotEmpty(scriptGridTrackers)) {
					int scriptGridTrackersSize = scriptGridTrackers.size();
					rows = new int[scriptGridTrackersSize];
					for (int i = 0; i < scriptGridTrackersSize; i++) {
						rows[i] = scriptGridTrackers.get(i).getGridIndex();
					} 
				} else {
					if (isAllRows) {
						table.setRowSelectionInterval(0, table.getRowCount() - 1);
					}
					rows=table.getSelectedRows();
				}
				
				int colIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				List<Integer> rowIndexesTableGrid=new ArrayList<Integer>();
				for(int i=0;i<rows.length;i++){
					if(grid instanceof TableGrid && !selectSuccess && isSelected) {
						String status = (String)table.getValueAt(rows[i],table.getColumnModel().getColumnIndex(TableGrid.COLUMN_HEADING_STATUS));
						if(status.equalsIgnoreCase(STATUS_SUCCESS)) {
							continue;
						}
					}
							
					table.setValueAt(isSelected,rows[i],colIndex);

					// if recordTrackersList is not null (coming from the TableGrid)
					if(CollectionUtils.isNotEmpty(recordTrackersList)) {
						//update the original recordTrackersList
						try {
							recordTrackersList.get(rows[i]).setIsSelected(isSelected);
						} catch (Throwable t) {
							FileUtils.printStackTrace(t);
						}
					}
					
					int modelIndex=table.convertRowIndexToModel(rows[i]);
					rowIndexesTableGrid.add(modelIndex);
				}
				if (grid instanceof TableGrid) {
					TableGrid tableGrid = (TableGrid)grid;
					tableGrid.getScriptsGrid().setEnableOnDependantRows(tableGrid.getScriptGridTracker(),tableGrid,rowIndexesTableGrid,isSelected);
				}
				
				table.repaint();
				
				if(propagateFurther) {
					if(grid instanceof TableGrid) {
						TableGrid tableGrid = (TableGrid)grid;
						boolean select = false;					
						if(tableGrid.getCheckedRowCount() > 0) {
							select = true;
						}
						setSelectionOnRows(tableGrid.getScriptsGrid(), Collections.singletonList(tableGrid.getScriptGridTracker()), select, false, true, false);
					}
					else 
					if(grid instanceof ScriptsGrid) {
						ScriptsGrid scriptsGrid = (ScriptsGrid)grid;
						List<ScriptGridTracker> scriptGridTrackers = new ArrayList<ScriptGridTracker>();
						try {
							for (int i = 0; i < rows.length; i++) {
								//ScriptGridTracker scriptGridTrackerTemp = scriptsGrid.getExecutionTabPanel().getScriptGridTracker(rows[i]);
 								scriptGridTrackers.add(scriptsGrid.getExecutionTabPanel().getScriptGridTracker(rows[i]));
							}
							
						} catch (Throwable t) {
							t.printStackTrace();
							FileUtils.printStackTrace(t);
						}
						for (ScriptGridTracker scriptGridTracker : scriptGridTrackers) {
							for (Entry<String, List<RecordTracker>> entry : scriptGridTracker.getInventoryToRecordTrackerMap().entrySet())
							{
								String inventoryName = entry.getKey();
								try {
									TableGrid tableGrid = scriptsGrid.getTableGrid(scriptGridTracker, inventoryName);
									setSelectionOnRows(tableGrid, null, isSelected, true, false, false);
								} catch (Throwable t) {
									t.printStackTrace();
									FileUtils.printStackTrace(t);
								}
							}
						}
					}
				}
			}
		});
	}
	
	public static void deselectRow(final JTable table,final int columnIndex,final int scriptIndex)  {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				table.setValueAt(false,scriptIndex,table.getColumnModel().getColumnIndex(columnIndex));
				table.repaint();
			}
		});
	}
	
	public int getCheckedRowCount() {
		int toReturn = 0;
		int columnIndexSelection = table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);		
		for(int i=0; i<table.getRowCount(); i++) {
			if(((Boolean)table.getValueAt(i, columnIndexSelection)) == true) {
				toReturn++;
			}
		}		
		return toReturn;		
	}

}
