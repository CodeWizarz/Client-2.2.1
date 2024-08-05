package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumn;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.TableCellBalloonTip;
import net.java.balloontip.BalloonTip.AttachLocation;
import net.java.balloontip.BalloonTip.Orientation;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.EdgedBalloonStyle;
import net.java.balloontip.utils.FadingUtils;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.AbstractDialogWorker;
import com.rapidesuite.inject.gui.TableHeaderRenderer;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;
import com.rapidesuite.snapshot.view.convert.ConvertSourceGridRecordInformation;

public class UIUtils {
	
	private static List<BalloonTip> balloonTips = new ArrayList<BalloonTip>();

	public static long getRawTimeInSecs(Long startTime) {
		Long currentTime=System.currentTimeMillis();
		Long diffTime=currentTime-startTime;
		long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(diffTime);
		return timeSeconds;
	}
	
	public static void setColumnSize(JTable table,String columnName,int columnMinWidth,int columnWidth,boolean isSetMaxWidth) {
		TableColumn column= table.getColumn(columnName);
		column.setMinWidth(columnMinWidth);
		if (isSetMaxWidth) {
			column.setMaxWidth(columnWidth);
		}
		column.setPreferredWidth(columnWidth);
		column.setHeaderRenderer(new TableHeaderRenderer());
	}
	
	public static void setDefaultFileChooserProperties(JFileChooser fileChooser,String propertyName) {
			Preferences pref = Preferences.userRoot();
			String value = pref.get(propertyName, "");
			if (value!=null && !value.isEmpty()) {
				File file=new File(value);
				fileChooser.setCurrentDirectory(file.getParentFile());
				fileChooser.setSelectedFile(file);
			}
	}
	
	public static void displayOperationInProgressModalWindow(Component component,int width,int height,String title,SnapshotSwingWorker snapshotSwingWorker,String iconPath) {
		displayOperationInProgressModalWindow(component,width,height,title,snapshotSwingWorker,false,iconPath);
	}
	
	public static void displayOperationInProgressModalWindow(Component component,int width,int height,String title,SnapshotSwingWorker snapshotSwingWorker,
			boolean isManualCloseAllowed,String iconPath)
	{
		try{
			final JDialog dialog = new JDialog();
			((java.awt.Frame)dialog.getOwner()).setIconImage(GUIUtils.getImageIcon(component.getClass(), iconPath).getImage());
			 
			dialog.setSize(width, height);
			dialog.setTitle(title);
			dialog.setModal(true);
			dialog.setContentPane(snapshotSwingWorker.getMainPanel());
			if (isManualCloseAllowed) {
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			}
			else {
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			}

			snapshotSwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("state")) {
						if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
							dialog.dispose();
						}
					}
				}
			});
			snapshotSwingWorker.execute();
			snapshotSwingWorker.setDialog(dialog);
			dialog.setLocationRelativeTo(component);
			dialog.setVisible(true);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}
	
	public static JDialog displayOperationInProgressComplexModalWindowSnap(JFrame frame,String title,
			int width,int height,JPanel mainPanel,SnapshotSwingWorker snapshotSwingWorker,boolean isManualCloseAllowed,String iconPath)
	{
		final JDialog dialog = new JDialog();
		if (frame!=null) {
			((java.awt.Frame)dialog.getOwner()).setIconImage(GUIUtils.getImageIcon(frame.getClass(),iconPath).getImage());
		}
		dialog.setSize(width, height);
		dialog.setTitle(title);
		dialog.setModal(true);
		dialog.setContentPane(mainPanel);
		if (isManualCloseAllowed) {
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}
		else {
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		}
		if (snapshotSwingWorker!=null) {
			snapshotSwingWorker.execute();
		}
		dialog.setLocationRelativeTo(frame);
		return dialog;
	}
	
	public static JDialog displayOperationInProgressComplexModalWindow(JFrame frame,String title,
			int width,int height,JPanel mainPanel,AbstractDialogWorker abstractDialogWorker,boolean isManualCloseAllowed,String iconPath)
	{
		final JDialog dialog = new JDialog();
		((java.awt.Frame)dialog.getOwner()).setIconImage(GUIUtils.getImageIcon(frame.getClass(), iconPath).getImage());
		
		dialog.setSize(width, height);
		dialog.setTitle(title);
		dialog.setModal(true);
		dialog.setContentPane(mainPanel);
		if (isManualCloseAllowed) {
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}
		else {
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		}
		if (abstractDialogWorker!=null) {
			abstractDialogWorker.execute();
		}
		dialog.setLocationRelativeTo(frame);
		return dialog;
	}

	public static void setDimension(JComponent component,int width,int height) {
		component.setSize(new Dimension(width,height));
		component.setPreferredSize(new Dimension(width,height));
		component.setMinimumSize(new Dimension(width,height));
		component.setMaximumSize(new Dimension(width,height));
	}	
	
	public static Map<String,String> readSnapshotEnvironmentProperties(File seFile) 
	{
		Map<String,String> toReturn=new HashMap<String,String>();
		String content;
		try {
			content = InjectUtils.decryptFromFile(seFile);
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to read file: '"+seFile.getAbsolutePath()+"'. Error: "+e.getMessage());
			return toReturn;
		}
		StringTokenizer tokenizer=new StringTokenizer(content,"\n");
		while (tokenizer.hasMoreTokens()) {
			String line=tokenizer.nextToken();
			
			int indexOf=line.indexOf("=");
			if (indexOf!=-1) {
				String property=line.substring(0,indexOf);
				String value=line.substring(indexOf+1);
				toReturn.put(property, value);
			}
		}
		return toReturn;
	}

	public static void setTime(SnapshotInventoryGridRecord snapshotInventoryGridRecord) {
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(snapshotInventoryGridRecord.getStartTime(),currentTime);
		snapshotInventoryGridRecord.setExecutionTime(msg);
		snapshotInventoryGridRecord.setRawTimeInSecs(UIUtils.getRawTimeInSecs(snapshotInventoryGridRecord.getStartTime()));
	}
	
	public static void setTime(ConvertSourceGridRecordInformation convertSourceGridRecordInformation) {
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(convertSourceGridRecordInformation.getStartTime(),currentTime);
		convertSourceGridRecordInformation.setExecutionTime(msg);
		convertSourceGridRecordInformation.setRawTimeInSecs(UIUtils.getRawTimeInSecs(convertSourceGridRecordInformation.getStartTime()));
	}
	
	public static void setDownloadTime(GenericRecordInformation genericRecordInformation) {
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(genericRecordInformation.getDownloadStartTime(),currentTime);
		genericRecordInformation.setDownloadTime(msg);
		genericRecordInformation.setDownloadRawTimeInSecs(UIUtils.getRawTimeInSecs(genericRecordInformation.getDownloadStartTime()));
	}
	
	public static String getFormattedNumber(int number) {
		try{
			String formattedNumber=Utils.formatNumberWithComma(number);
			return formattedNumber;
		}
		catch(Exception e) {
		}		
		return "";
	}

	public static void setSelectionBasedOnTotals(List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList) {
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsList) {
			int totalAddedRecords=snapshotInventoryGridRecord.getTotalAddedRecords();
			int totalUpdatedRecords=snapshotInventoryGridRecord.getTotalUpdatedRecords();

			boolean isSelected=totalAddedRecords!=0 || totalUpdatedRecords!=0;
			snapshotInventoryGridRecord.setDefaultSelected(isSelected);
		}
	}
	
	public static void setAllTotalsToZero(SnapshotInventoryGridRecord snapshotInventoryGridRecord) {
		snapshotInventoryGridRecord.setTotalAddedRecords(0);
		snapshotInventoryGridRecord.setTotalDefaultRecords(0);
		snapshotInventoryGridRecord.setTotalRecords(0);
		snapshotInventoryGridRecord.setTotalUpdatedRecords(0);
	}


	public static JOptionPane getInformationOptionPane(String msg) {
		final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
		return optionPane;
	}
	
	public static JPopupMenu setPopupMenu(final JTable table,final String selectionColumnName) {
		final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Select");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(table,selectionColumnName,true,false);
            }
        });
        popupMenu.add(item);
        
        item = new JMenuItem("Select All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(table,selectionColumnName,true,true);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(table,selectionColumnName,false,false);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(table,selectionColumnName,false,true);
            }
        });
        popupMenu.add(item);
		table.setComponentPopupMenu(popupMenu);
		return popupMenu;
	}	
	
	private static void setSelectionOnRows(final JTable table,String selectionColumnName,final boolean isSelected,final boolean isAllRows) {
		final int colIndexSelection=table.getColumnModel().getColumnIndex(selectionColumnName);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int[] rows=null;
				if (isAllRows) {
					table.setRowSelectionInterval(0, table.getRowCount() - 1);
				}
				rows=table.getSelectedRows();
				//List<Integer> rowIndexesTableGrid=new ArrayList<Integer>();
				for(int i=0;i<rows.length;i++){					
					table.setValueAt(isSelected,rows[i],colIndexSelection);
					//int modelIndex=table.convertRowIndexToModel(rows[i]);
					//rowIndexesTableGrid.add(modelIndex);
				}
				table.repaint();
			}
		});
	}
	
	public static void selectAllListItems(JList<String> list,DefaultListModel<String> listModel) {
		int start = 0;
	    int end = listModel.getSize() - 1;
	    if (end >= 0) {
	    	list.setSelectionInterval(start, end);
	    }
	}	
	
	public static void showBalloon(JComponent component,String text, boolean visable) {
		 showBalloon(component, text,Orientation.RIGHT_ABOVE, visable);
	}
	
	public static void showBalloon(JComponent component,String text, Orientation orientation, boolean visable) {
		BalloonTipStyle edgedLook = new EdgedBalloonStyle(new Color(239,228,176), Color.black);
		BalloonTip balloonTip=new BalloonTip(component,new JLabel(text),edgedLook,orientation, AttachLocation.ALIGNED, 30, 20,true);
		// close will hide now
		balloonTip.setCloseButton(BalloonTip.getDefaultCloseButton(), false);
		// add it to our ballonTips list
		balloonTips.add(balloonTip);
		setVisibleWithFade(balloonTip, visable);
	}
	
	public static void showCellBalloon(String text, Orientation orientation,JTable table,int rowIndex,int colIndex, boolean visable) {
		BalloonTipStyle edgedLook = new EdgedBalloonStyle(new Color(239,228,176), Color.black);
		TableCellBalloonTip tableCellBalloonTip=new TableCellBalloonTip(table, 
				new JLabel(text),
				rowIndex, colIndex, edgedLook,orientation,
				BalloonTip.AttachLocation.ALIGNED, 30, 20, true);
		// close will hide now
		tableCellBalloonTip.setCloseButton(BalloonTip.getDefaultCloseButton(), false);
		// add it to our tableCellBalloonTip list
		balloonTips.add(tableCellBalloonTip);
		setVisibleWithFade(tableCellBalloonTip, visable);
	}
	
	public static void setFramePosition(JFrame rootFrame,JFrame frame) {
		int x=rootFrame.getLocation().x+UIConstants.DELTA_X_POSITION_FRAMES;
		int y=rootFrame.getLocation().y+UIConstants.DELTA_Y_POSITION_FRAMES;
		frame.setLocation(x,y);
	}
	
	public static void viewScript() {
		try{
			String content=getScriptContent();
			ModelUtils.viewScript(content);
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}
	
	public static void viewRawScript() {
		try{
			File file=new File(SchemaManagementPanel.SCHEMA_SCRIPT_FILE_NAME);
			String content=SchemaManagementPanel.readContentsFromSQLFile(file);
			ModelUtils.viewScript(content);
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}
	
	public static String getScriptContent() throws Exception {
		File file=new File(SchemaManagementPanel.SCHEMA_SCRIPT_FILE_NAME);
		String content=SchemaManagementPanel.readContentsFromSQLFile(file);
		content=content.replaceAll(SchemaManagementPanel.VARIABLE_USERNAME, SchemaManagementPanel.USER_NAME_PREFIX+"USER1");
		content=content.replaceAll(SchemaManagementPanel.VARIABLE_PASSWORD,SchemaManagementPanel.USER_NAME_PREFIX+"USER1");
		content=content.replaceAll(SchemaManagementPanel.VARIABLE_DBF,"/data/erpp/oracle/ERPP/db/apps_st/data/"+SchemaManagementPanel.USER_NAME_PREFIX+"USER1.dbf");
		content=content.replaceAll(SchemaManagementPanel.VARIABLE_TABLESPACE,"1");
		content=content.replaceAll(SchemaManagementPanel.VARIABLE_TABLESPACE_AUTO_EXTEND, "ON");
		return content;
	}
	
	public static void allBallonTipsSetVisable(boolean visable) {
		for (BalloonTip balloonTip : balloonTips) {
			setVisibleWithFade(balloonTip, visable);
		}
	}
	
	public static void setVisibleWithFade(BalloonTip balloonTip, boolean visable) {
		if(visable) {
			FadingUtils.fadeInBalloon(balloonTip, null, UIConstants.BALLOON_FADEIN_DELAY, UIConstants.BALLOON_FADEIN_FRAMERATE);
		}
		balloonTip.setVisible(visable);
	}
	public static JPopupMenu setPopupMenuDymanicColumn(final JTable table,final String[] columnsCanBeSelected,final String selectionColumnName) {
		final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Select");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setDynamicSelectionOnRows(table,columnsCanBeSelected,selectionColumnName,true,false);
            }
        });
        popupMenu.add(item);
        
        item = new JMenuItem("Select All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setDynamicSelectionOnRows(table,columnsCanBeSelected,selectionColumnName,true,true);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setDynamicSelectionOnRows(table,columnsCanBeSelected,selectionColumnName,false,false);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setDynamicSelectionOnRows(table,columnsCanBeSelected,selectionColumnName,false,true);
            }
        });
        popupMenu.add(item);
		table.setComponentPopupMenu(popupMenu);
		return popupMenu;
	}
	private static void setDynamicSelectionOnRows(final JTable table,final String[] columnsCanBeSelected,final String selectionColumnName,final boolean isSelected,final boolean isAllRows) {
		final int colIndexSelection=table.getColumnModel().getColumnIndex(selectionColumnName);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int[] rows=null;
				int columnIndexNoToClick = 0;
				int columnIndexNoWasClicked = 0;
				boolean foundIndexToClick = false;
				boolean skipCheckingColumn = false;
				if (isAllRows) {
					table.setRowSelectionInterval(0, table.getRowCount() - 1);
				}
				try{
					columnIndexNoWasClicked 	= table.columnAtPoint(table.getMousePosition());
				}catch(NullPointerException e){
					skipCheckingColumn = true;
				}
				if(!skipCheckingColumn){
					for(String name : columnsCanBeSelected){
						int column =table.getColumnModel().getColumnIndex(name);
						if(columnIndexNoWasClicked==column){
							columnIndexNoToClick = columnIndexNoWasClicked;
							foundIndexToClick = true;
							break;
						}	
					}					
				}
				rows=table.getSelectedRows();
				
				for(int i=0;i<rows.length;i++){	
					if(foundIndexToClick){
						table.setValueAt(isSelected,rows[i],columnIndexNoToClick);
					}else{
						table.setValueAt(isSelected,rows[i],colIndexSelection);
					}
				}
				table.repaint();
			}
		});
	}
}
