package com.rapidesuite.inject.commands;

import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.BlockType;
import com.rapidesuite.inject.Worker;

public class BlockTypeCommand extends Command {

	public BlockTypeCommand(Worker worker) {
		super(worker);
	}

	public void process(BlockType blockType) throws Exception {	
		String blockName=blockType.getName();
		XmlObject[] xmlObjects=blockType.selectPath("*");
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);
		
		List<XmlObject> blockCommandsList=worker.getBlockNameToCommandsMap().get(blockName);
		if (blockCommandsList!=null) {
			throw new Exception("Block name: '"+blockName+"' is duplicated!!! Please fix your navigation");
		}
		worker.getBlockNameToCommandsMap().put(blockName,xmlObjectsSubList);
	}
	
}
