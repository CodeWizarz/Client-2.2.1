package com.rapidesuite.designers.navigation;

import java.util.ArrayList;
import java.util.List;

import com.rapidesuite.configurator.domain.Inventory;

public class InventoryHierarchy {

	Inventory inventory;
	private List<InventoryHierarchy> children;
	
	public InventoryHierarchy() {
		children=new ArrayList<InventoryHierarchy>();
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
	public List<InventoryHierarchy> getChildren() {
		return children;
	}
		
	public String getRepeatXMLTags() {
		StringBuffer toReturn=new StringBuffer("");
		toReturn.append("<repeat inventoryName=\"").append(inventory.getName()).append("\">\n\n");
		
		if (inventory.getParentName()==null || inventory.getParentName().isEmpty()) {
			toReturn.append("<templateClick type=\"anchor_image\" attribute=\"title\">Go to Task</templateClick>\n\n");
		}
		
		for (InventoryHierarchy inventoryHierarchy:children) {
			toReturn.append(inventoryHierarchy.getRepeatXMLTags());
		}		
		toReturn.append("</repeat>\n");
				
		return toReturn.toString();
	}
	
}
