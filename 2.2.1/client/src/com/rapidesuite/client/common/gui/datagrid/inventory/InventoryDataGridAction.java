/**************************************************************
 * $Revision: 31696 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:39:15 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/datagrid/inventory/InventoryDataGridAction.java $:
 * $Id: InventoryDataGridAction.java 31696 2013-03-04 06:39:15Z john.snell $:
 **************************************************************/
package com.rapidesuite.client.common.gui.datagrid.inventory;

import com.rapidesuite.configurator.domain.Inventory;

public class InventoryDataGridAction
{

	private String navigationName;
	protected Inventory inventory;
	private String dataGridTitle;
	
	public InventoryDataGridAction(Inventory inventory,String dataGridTitle,String navigationName)
	{
		this.inventory=inventory;
		this.dataGridTitle=dataGridTitle;
		this.navigationName=navigationName;
	}
	
	public String getDataGridTitle() {
		return dataGridTitle;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public String getNavigationName() {
		return navigationName;
	}

}