/**************************************************************
 * $Revision: 31696 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:39:15 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/datagrid/inventory/InventoryDataRowsFilter.java $:
 * $Id: InventoryDataRowsFilter.java 31696 2013-03-04 06:39:15Z john.snell $:
 **************************************************************/
package com.rapidesuite.client.common.gui.datagrid.inventory;

import java.util.List;

public interface InventoryDataRowsFilter
{

	public List<String[]> filter(List<String[]> dataRows);
	
}