package com.rapidesuite.inject.selenium;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.inject.gui.InjectFilteringTable;
import com.rapidesuite.inject.gui.ScriptsGrid;
import com.rapidesuite.inject.gui.TableHeaderRenderer;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class SeleniumHubTable  extends JPanel {

	public final static String STATUS_UP="Up";
	public final static String STATUS_DOWN="Down";
	public final static String LOAD_BUSY="Busy";
	public final static String LOAD_FREE="Free";
	
	private String COLUMN_HEADING_SELECTION="Selection";
	private String COLUMN_HEADING_USER="User";
	private String COLUMN_HEADING_STATUS="Status";
	private String COLUMN_HEADING_LOAD="Load";
	private String COLUMN_HEADING_DISPLAY_NAME="Display name";
	
	private int COLUMN_HEADING_SELECTION_WIDTH=60;
	private int COLUMN_HEADING_USER_WIDTH=180;
	private int COLUMN_HEADING_STATUS_WIDTH=100;
	private int COLUMN_HEADING_LOAD_WIDTH=100;
	private int COLUMN_HEADING_DISPLAY_NAME_WIDTH=400;
	
	public final static String SSH_NODE_PROCESS_OUTPUT_LOG_FILE_NAME="sshNodeProcessOutputLog.txt";
	private final static String SSH_NODE_PROCESS_ERROR_LOG_FILE_NAME="sshNodeProcessErrorLog.txt";
	
	private final JTable table;
	private SeleniumHub seleniumHub;
	
	public SeleniumHubTable(final SeleniumHub seleniumHub) {
		this.seleniumHub=seleniumHub;
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));	
		this.setOpaque(true);
		this.setBackground(ExecutionPanel.EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR);
		
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_SELECTION);
		columnNames.add(COLUMN_HEADING_USER);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_LOAD);
		columnNames.add(COLUMN_HEADING_DISPLAY_NAME);
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Class getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				switch (column) {
				case 0:
					return true;
				default:
					return false;
				}
			}

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int colIndex) {
				Component component = super.prepareRenderer(renderer, rowIndex, colIndex);
				component.setBackground(Color.decode("#F6F6F6"));
				
				int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
				String status= (String) getValueAt(rowIndex, colIndexStatus);	
				if (status!=null) {
					if (status.equalsIgnoreCase(STATUS_DOWN)) {
						component.setBackground(ScriptsGrid.redColor);
					}
					else {
						component.setBackground(ScriptsGrid.greenColor);
					}
				}
				if (isCellSelected(rowIndex, colIndex)) {
					Color color=component.getBackground();
					component.setBackground(color.brighter());
				} 
				
				return component;
			}
		};
		
		TableColumn columnSelection = table.getColumn(COLUMN_HEADING_SELECTION);
		columnSelection.setMinWidth(COLUMN_HEADING_SELECTION_WIDTH);
		columnSelection.setPreferredWidth(COLUMN_HEADING_SELECTION_WIDTH);
		columnSelection.setHeaderRenderer(new TableHeaderRenderer());
		
		TableColumn columnUser = table.getColumn(COLUMN_HEADING_USER);
		columnUser.setMinWidth(COLUMN_HEADING_USER_WIDTH);
		columnUser.setPreferredWidth(COLUMN_HEADING_USER_WIDTH);
		columnUser.setHeaderRenderer(new TableHeaderRenderer());
		
		TableColumn columnStatus = table.getColumn(COLUMN_HEADING_STATUS);
		columnStatus.setMinWidth(COLUMN_HEADING_STATUS_WIDTH);
		columnStatus.setPreferredWidth(COLUMN_HEADING_STATUS_WIDTH);
		columnStatus.setHeaderRenderer(new TableHeaderRenderer());
		
		TableColumn columnLoad= table.getColumn(COLUMN_HEADING_LOAD);
		columnLoad.setMinWidth(COLUMN_HEADING_LOAD_WIDTH);
		columnLoad.setPreferredWidth(COLUMN_HEADING_LOAD_WIDTH);
		columnLoad.setHeaderRenderer(new TableHeaderRenderer());
		
		TableColumn columnDisplayName = table.getColumn(COLUMN_HEADING_DISPLAY_NAME);
		columnDisplayName.setMinWidth(COLUMN_HEADING_DISPLAY_NAME_WIDTH);
		columnDisplayName.setPreferredWidth(COLUMN_HEADING_DISPLAY_NAME_WIDTH);
		columnDisplayName.setHeaderRenderer(new TableHeaderRenderer());
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		columnUser.setCellRenderer(renderer);
		columnStatus.setCellRenderer(renderer);
		columnLoad.setCellRenderer(renderer);
		columnDisplayName.setCellRenderer(renderer);
	
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

		table.setPreferredScrollableViewportSize(new Dimension(1000,500));
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setTableHeader(new JTableHeader(table.getColumnModel()) {
	           @Override public Dimension getPreferredSize() {
	        	   Dimension d = super.getPreferredSize();
	               d.height = 30;
	               return d;
	           }
			});
		table.getTableHeader().setReorderingAllowed(false);
		table.setBackground(Color.decode("#DBDBDB"));
	    table.setRowHeight(20);
	    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
	    table.setRowSorter(sorter);	   
	    
		JScrollPane scrollPane = new JScrollPane(table);
		this.add(scrollPane);
		
		JScrollPane variableScroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};
		InjectFilteringTable fixedTable=new InjectFilteringTable(columnNames,variableScroll,table,33,true);
		
		add(fixedTable.getFixedScrollPane(),BorderLayout.NORTH);
		add(variableScroll,BorderLayout.CENTER);
		
		setPopupMenu(COLUMN_HEADING_SELECTION);
	}
	
	public void loadNodes(List<SeleniumNodeInformation>  nodeInformationList) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);
		
		for (SeleniumNodeInformation seleniumNodeInformation:nodeInformationList) {
			String sshUser=seleniumNodeInformation.getSshUser();
			String displayName=seleniumNodeInformation.getDisplayName();
			
			Vector<Object> row = new Vector<Object>();
			row.add(false);
			row.add(sshUser);
			row.add(STATUS_DOWN);
			row.add("");
			row.add(displayName);
			
			model.addRow(row);
		}
	    table.repaint();
	}
	
	public void setPopupMenu(final String columnIndex) {
		final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Select");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(columnIndex,true,false);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(columnIndex,false,false);
            }
        });
        popupMenu.add(item);
    	
		item = new JMenuItem("Select All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(columnIndex,true,true);
            }
        });
        popupMenu.add(item);
        
        item = new JMenuItem("Deselect All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(columnIndex,false,true);
            }
        });
        popupMenu.add(item);
                
		table.setComponentPopupMenu(popupMenu);
	}
	
	public void setSelectionOnRows(final String columnIndex,final boolean isSelected,final boolean isAllRows) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {				
				int[] rows=null;
				if (isAllRows) {
					table.setRowSelectionInterval(0, table.getRowCount() - 1);
				}
				rows=table.getSelectedRows();
				
				int colIndex=table.getColumnModel().getColumnIndex(columnIndex);
				List<Integer> rowIndexesTableGrid=new ArrayList<Integer>();
				for(int i=0;i<rows.length;i++){
					table.setValueAt(isSelected,rows[i],colIndex);
					int modelIndex=table.convertRowIndexToModel(rows[i]);
					rowIndexesTableGrid.add(modelIndex);
				}
				table.repaint();
			}
		});
	}
		
	protected void refreshNodes(Set<String> allRegisteredNodes,Set<String> busyNodes) throws Exception {
		TableModel model = table.getModel();
		int numRows = table.getRowCount();
		int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
		int colIndexLoad=table.getColumnModel().getColumnIndex(COLUMN_HEADING_LOAD);
		int colIndexDisplayName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_DISPLAY_NAME);
				
		for (int i=0; i < numRows; i++) {
			String displayName=(String) model.getValueAt(i,colIndexDisplayName);
			
			boolean hasRegisteredNode=allRegisteredNodes.contains(displayName);
			//System.out.println("displayName:"+displayName+" hasNode:"+hasNode);
			if (hasRegisteredNode) {
				model.setValueAt(STATUS_UP,i,colIndexStatus);
				boolean isBusyNode=busyNodes.contains(displayName);
				if (isBusyNode) {
					model.setValueAt(LOAD_BUSY,i,colIndexLoad);
				}
				else {
					model.setValueAt(LOAD_FREE,i,colIndexLoad);
				}
			}
			else {
				model.setValueAt(STATUS_DOWN,i,colIndexStatus);
				model.setValueAt("",i,colIndexLoad);
			}
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				table.repaint();
			}
		});
	}
	
	public SeleniumHub getSeleniumHub() {
		return seleniumHub;
	}
	
	public int getTotalNodes()
	{
		return table.getRowCount();
	}

	public int getTotalNodes(String statusParam)
	{
		TableModel model = table.getModel();
		int numRows = table.getRowCount();
		int colIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
		int toReturn=0;
		for (int i=0; i < numRows; i++) {
			String status=(String) model.getValueAt(i,colIndex);
			if (status!=null && status.equals(statusParam)) {
				toReturn++;
			}
		}
		return toReturn;
	}
	
	public List<String> getChechedDisplayNames(String statusParam)
	{
		TableModel model = table.getModel();
		int numRows = table.getRowCount();
		int colSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		int colStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
		int colIndexDisplayName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_DISPLAY_NAME);
		
		List<String> toReturn=new ArrayList<String>();
		for (int i=0; i < numRows; i++) {
			Boolean isSelected=(Boolean) model.getValueAt(i,colSelection);
			String status=(String) model.getValueAt(i,colStatus);
			if (isSelected && status.equalsIgnoreCase(statusParam)) {
				String displayName=(String) model.getValueAt(i,colIndexDisplayName);
				toReturn.add(displayName);
			}
		}
		return toReturn;
	}
	
	public static void startNode(SeleniumHub seleniumHub,SeleniumNodeInformation seleniumNodeInformation)  {		 
		startNodeGeneric(seleniumHub,null,seleniumNodeInformation);
	}
	
	public static void startNode(String serverId,SeleniumNodeInformation seleniumNodeInformation)  {		 
		startNodeGeneric(null,serverId,seleniumNodeInformation);
	}
			
	private static void startNodeGeneric(SeleniumHub seleniumHub,String serverId,SeleniumNodeInformation seleniumNodeInformation)  {		 
		final SSHClient ssh=new SSHClient();
		FileOutputStream fosError=null;
		FileOutputStream fosOutput=null;
		String userName=seleniumNodeInformation.getSshUser();
		try {
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			String hostName=seleniumNodeInformation.getHostName();
			ssh.connect(hostName);
			
			ssh.authPassword(userName,seleniumNodeInformation.getSshPassword());
			Session sess=ssh.startSession();
			
			int display=seleniumNodeInformation.getVncPort()-5900; // the DISPLAY is the last 2 digits
			sess.reqX11Forwarding("anything","anything",display);
			
			File currentFolder=new File(".");
			String currentFolderStr=currentFolder.getAbsolutePath()+"/";
			//currentFolderStr="/home/"+userName+"/shared_folder/fusionTrunk/";
			String serverIdStr=null;
			if (seleniumHub!=null) {
				serverIdStr=seleniumHub.getServerIdentifier();
			}
			else {
				serverIdStr=serverId;
			}
			
			String commandStr="DISPLAY=:"+display+";"+currentFolderStr+"injectNode.sh "+serverIdStr+" "+
					seleniumNodeInformation.getDisplayName()+" "+seleniumNodeInformation.getWebsockifyPort()+" "+userName;
			if (seleniumHub!=null) {
				seleniumHub.log(seleniumHub.getServerUILogFile(),"Starting node, SSH command: '"+commandStr+"'");
			}
			final Command command=sess.exec(commandStr);
			
			File logFolder=new File("nodes",userName);
			logFolder.mkdirs();
			File sshNodeProcessOutputProcessLogFile=new File(logFolder,SSH_NODE_PROCESS_OUTPUT_LOG_FILE_NAME);
			sshNodeProcessOutputProcessLogFile.delete();
			sshNodeProcessOutputProcessLogFile.createNewFile();
			File  sshNodeProcessErrorProcessLogFile=new File(logFolder,SSH_NODE_PROCESS_ERROR_LOG_FILE_NAME);
			sshNodeProcessErrorProcessLogFile.delete();
			sshNodeProcessErrorProcessLogFile.createNewFile();
			
			fosOutput=new  FileOutputStream(sshNodeProcessOutputProcessLogFile,true);
			new StreamCopier(command.getInputStream(),fosOutput).spawn("test2");
						
			fosError=new  FileOutputStream(sshNodeProcessErrorProcessLogFile,true);
			new StreamCopier(command.getErrorStream(),fosError).spawn("test1");
						
			command.join(1, TimeUnit.SECONDS);	
		}
		catch (Exception e) {
			if (e.getMessage()!=null && !e.getMessage().equalsIgnoreCase("Timeout expired")) {
				if (seleniumHub!=null) {
					seleniumHub.log(seleniumHub.getServerUILogFile(),"Unable start node for user '"+userName+"' !");
				}
				e.printStackTrace();
				if (seleniumHub!=null) {
					seleniumHub.log(seleniumHub.getServerUILogFile(),e);
				}
			}
		}
		finally {
			try {
				if (fosOutput!=null) {
					fosOutput.close();
				}
				if (fosError!=null) {
					fosError.close();
				}
				ssh.disconnect();
				ssh.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
				seleniumHub.log(seleniumHub.getServerUILogFile(),ex);
			}
		}
	}
	
	public static void runSSHCommand(SeleniumNodeInformation seleniumNodeInformation,String hostName,String command)  {
		final SSHClient ssh=new SSHClient();
		String userName=seleniumNodeInformation.getSshUser();
		FileOutputStream fos=null;
		try {
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			ssh.connect(hostName);
			ssh.authPassword(userName,seleniumNodeInformation.getSshPassword());
			Session session=ssh.startSession();

			System.out.println("SSH command: '"+command+"'");
			final Command cmd = session.exec(command);
			
			System.out.println("SSH command output: '"+IOUtils.readFully(cmd.getInputStream()).toString()+"'");
			cmd.join(5, TimeUnit.SECONDS);
			System.out.println("SSH command exit status: " + cmd.getExitStatus());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (fos!=null) {
					fos.close();
				}
				ssh.disconnect();
				ssh.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void stopNode(SeleniumHub seleniumHub,SeleniumNodeInformation seleniumNodeInformation)  {
		final SSHClient ssh=new SSHClient();
		try {
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			ssh.connect(seleniumNodeInformation.getHostName());
			String userName=seleniumNodeInformation.getSshUser();
			ssh.authPassword(userName,seleniumNodeInformation.getSshPassword());
			Session sess=ssh.startSession();
			
			File currentFolder=new File(SeleniumNode.NODE_KILL_RELATIVE_FOLDER,userName);
			currentFolder.mkdirs();
			
			String commandStr="touch "+currentFolder.getAbsolutePath()+"/"+SeleniumHub.FUSION_NODES_KILL_SIGNAL_FILE_NAME;
			if (seleniumHub!=null) {
				seleniumHub.log(seleniumHub.getServerUILogFile(),"Stopping node, SSH command: '"+commandStr+"'");
			}
			final Command cmd=sess.exec(commandStr);
			new StreamCopier(cmd.getInputStream(),System.out).spawn("stdout");
			new StreamCopier(cmd.getErrorStream(),System.err).spawn("stderr");
			cmd.join(5, TimeUnit.SECONDS);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			if (seleniumHub!=null) {
				seleniumHub.log(seleniumHub.getServerUILogFile(),ex);
			}
		}
		finally {
			try {				
				ssh.disconnect();
				ssh.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
				if (seleniumHub!=null) {
					seleniumHub.log(seleniumHub.getServerUILogFile(),ex);
				}
			}
		}
	}

}
