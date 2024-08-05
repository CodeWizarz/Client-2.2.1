package com.rapidesuite.designers.navigation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.autoinjectors.engines.FusionEngine;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.configurator.domain.Navigation;
import com.rapidesuite.core.domain.DataRow;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.ModelUtils;

public class ClientFusionEngine extends FusionEngine{

	private Navigation navigation;
	private Map<String, Inventory> inventoryNameToInventoryMap;
	private Map<String, File> inventoryNameToDataFileMap;
	
	public ClientFusionEngine(String labels,Navigation navigation,Map<String, Inventory> inventoryNameToInventoryMap,
			File dataFolder) {
		super(labels);
		this.navigation=navigation;
		this.inventoryNameToInventoryMap=inventoryNameToInventoryMap;
		File[] list=dataFolder.listFiles();
		inventoryNameToDataFileMap=new HashMap<String, File>();
		if (list!=null) {
			for (File file:list) {
				String inventoryName=file.getName().replace(".xml","");
				inventoryNameToDataFileMap.put(inventoryName, file);
			}
		}
	}

	@Override
	public void exportDataToRSCXML(Inventory inventory, Navigation navigation, File dataOutputFile) throws Exception {
		FileUtils.println("exportDataToRSCXML, dataOutputFile:"+dataOutputFile.getAbsolutePath());
		File dataFile=inventoryNameToDataFileMap.get(inventory.getName());
		if (dataFile==null) {
			// GENERATE EMPTY DATA FILE
			StringBuffer content=new StringBuffer("");
			content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			content.append("<data name=\"").append(inventory.getName()).append("\" xmlns=\"http://data0000.configurator.erapidsuite.com\">\n");
			content.append("<h>\n");
			for (Field field:inventory.getFieldsUsedForDataEntry()) {
				content.append("<c>").append(field.getName()).append("</c>\n");
			}
			content.append("<c>RSC Data Label</c>\n");
			content.append("<c>Navigation Filter</c>\n");
			content.append("</h>\n");
			content.append("</data>\n");
			/*
			try{
				throw new Exception("No data file for inventory: '"+inventory.getName()+"'");
			}
			catch (Exception e) {
				FileUtils.printStackTrace(e);
				throw e;
			}
			*/
			ModelUtils.writeToFile(dataOutputFile, content.toString(), false);
			return;
		}
		Files.copy(dataFile.toPath(), dataOutputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public List<DataRow> getData(Inventory inventory) {
		List<DataRow> toReturn=new ArrayList<DataRow>();
		File dataFile=inventoryNameToDataFileMap.get(inventory.getName());
		if (dataFile==null) {
			return toReturn;
		}
		FileUtils.println("getData, dataFile:"+dataFile.getAbsolutePath());
		
		try {
			List<String[]> dataRows=InjectUtils.parseXMLDataFile(dataFile);
			for (String[] dataRowArray:dataRows) {
				DataRow dataRow=new DataRow(dataRowArray);
				dataRow.setNavigationMapper(navigation.getName());
				dataRow.setLabel("TEST");
				toReturn.add(dataRow);
			}
			return toReturn;	
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			return toReturn;
		}		
	}

	@Override
	public void writeToLogFile(String msg) throws Exception {
		FileUtils.println(msg);
	}

	@Override
	public Navigation getNavigation(String navigationName) throws Throwable {
		if (!navigation.getName().equalsIgnoreCase(navigationName)) {
			throw new Exception("Cannot find navigation name: '"+navigationName+"'");
		}
		return navigation;
	}

	@Override
	public Inventory getInventory(String inventoryName) throws Throwable {
		FileUtils.println("getInventory, inventoryName:"+inventoryName);
		Inventory inventory=inventoryNameToInventoryMap.get(inventoryName);
		if (inventory==null) {
			throw new Exception("Cannot find inventory name: '"+inventoryName+"'");
		}
		FileUtils.println("getInventory, inventory:"+inventory);
		return inventory;
	}
	
}