package com.rapidesuite.client.common.gui.datagrid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.openswing.swing.lookup.client.LookupController;
import org.openswing.swing.lookup.client.LookupDataLocator;
import org.openswing.swing.message.receive.java.ErrorResponse;
import org.openswing.swing.message.receive.java.Response;
import org.openswing.swing.message.receive.java.VOListResponse;
import org.openswing.swing.message.receive.java.VOResponse;
import org.openswing.swing.message.send.java.FilterWhereClause;
import org.openswing.swing.tree.java.OpenSwingTreeNode;

import com.rapidesuite.client.common.util.FileUtils;

public abstract class DataGridLookupController extends LookupController {

	protected List<String[]> lookupRows;
	
	public static VOListResponse getEmptyValidationLookupRecordList(String code){
		DataGridLookupVO vo = new DataGridLookupVO();
		vo.setColumn1(code);
		vo.setColumn2(code);
		vo.setColumn3(code);
		vo.setColumn4(code);
		vo.setColumn5(code);
		vo.setColumn6(code);
		vo.setColumn7(code);
		vo.setColumn8(code);
		ArrayList<DataGridLookupVO> list = new ArrayList<DataGridLookupVO>();
		list.add(vo);
		return new VOListResponse(list, false, list.size());
	}
	
	public DataGridLookupController()
	throws Exception {
		this.setLookupDataLocator(new LookupDataLocator() {

			/**
			 * Method called by lookup controller when validating code.
			 * @param code code to validate
			 * @return code validation response: VOListResponse if code validation has success, ErrorResponse otherwise
			 */
			public Response validateCode(String code) {
				try {
					//VOListResponse response=(VOListResponse)getDataGridRows(null);
					//List rows=response.getRows();
					//rows.add(getEmptyValidationLookupRecord(code));
					//return response;
					return getEmptyValidationLookupRecordList(code);
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
					return new ErrorResponse("Unable to load lookup data. Error: "+e.getMessage());
				}
			}

			/**
			 * Method called by lookup controller when user clicks on lookup button.
			 * @param action fetching versus: PREVIOUS_BLOCK_ACTION, NEXT_BLOCK_ACTION or LAST_BLOCK_ACTION
			 * @param startIndex current index row on grid to use to start fetching data
			 * @param filteredColumns filtered columns
			 * @param currentSortedColumns sorted columns
			 * @param currentSortedVersusColumns ordering versus of sorted columns
			 * @param valueObjectType type of value object associated to the lookup grid
			 * @return list of value objects to fill in the lookup grid: VOListResponse if data fetching has success, ErrorResponse otherwise
			 */
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public Response loadData(
					int action,
					int startIndex,
					Map filteredColumns,
					ArrayList currentSortedColumns,
					ArrayList currentSortedVersusColumns,
					Class valueObjectType
			) {
				try {
					return getDataGridRows(filteredColumns, currentSortedColumns, currentSortedVersusColumns);
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
					return new ErrorResponse(e.getMessage());
				}
			}

			/**
			 * Method called by the TreePanel to fill the tree.
			 * @return a VOReponse containing a DefaultTreeModel object
			 */
			public Response getTreeModel(JTree tree) {
				return new VOResponse(new DefaultTreeModel(new OpenSwingTreeNode()));
			}

		});

	}

	public void loadDataCompleted(boolean error) {
	}
	
	public abstract Response getDataGridRows(Map<String,FilterWhereClause[]> filteredColumns, List<String> currentSortedColumns, List<String> currentSortedVersusColumns) throws Exception;
	
	public abstract void initGrid() throws Exception;
	
	public abstract List<String> getColumnDescriptions();
	
	public static boolean include(Map<String,FilterWhereClause[]> filteredColumns,DataGridLookupVO vo) 
	throws Exception {
		if (filteredColumns==null) {
			return true;
		}
		Iterator<String> iterator=filteredColumns.keySet().iterator();
		boolean isToInclude=true;
		while (iterator.hasNext()) {
			String key=iterator.next();
			FilterWhereClause[] filterWhereClauses=filteredColumns.get(key);
			for (FilterWhereClause filterWhereClause:filterWhereClauses) {
				if (filterWhereClause!=null) {
					String sourceValue=null;
					if (filterWhereClause.getAttributeName().equalsIgnoreCase("column1")) {
						sourceValue= vo.getColumn1();
					}
					else if (filterWhereClause.getAttributeName().equalsIgnoreCase("column2")) {
						sourceValue= vo.getColumn2();
					}
					else if (filterWhereClause.getAttributeName().equalsIgnoreCase("column3")) {
						sourceValue= vo.getColumn3();
					}
					else if (filterWhereClause.getAttributeName().equalsIgnoreCase("column4")) {
						sourceValue= vo.getColumn4();
					}
					else if (filterWhereClause.getAttributeName().equalsIgnoreCase("column5")) {
						sourceValue= vo.getColumn5();
					}
					else if (filterWhereClause.getAttributeName().equalsIgnoreCase("column6")) {
						sourceValue= vo.getColumn6();
					}
					else if (filterWhereClause.getAttributeName().equalsIgnoreCase("column7")) {
						sourceValue= vo.getColumn7();
					}
					else if (filterWhereClause.getAttributeName().equalsIgnoreCase("column8")) {
						sourceValue= vo.getColumn8();
					}
					isToInclude=DataGridUtils.include(filterWhereClause.getOperator(),filterWhereClause.getValue(),sourceValue,false); 
					if (!isToInclude) {
						return false;
					}
				}
			}
		}
		return true;
	}

}
