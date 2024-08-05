package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.math.BigInteger;
import java.util.Map;

import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;
import com.rapidesuite.snapshot.model.ModelUtils;

public class OperationDFFfieldNameConstruct extends GenericConstruct{

	public OperationDFFfieldNameConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap, String sourceCode) throws Exception {
		
		String navigationMapper=null;
		
		// The in-variable contains the navigation mapper field for views as
		// we can have multiple navigation mappers in the same views if joins contains many DFF tables
		Map<String, SourceType> sourceCodeToSourceTypeMap=upgradeEngine.getSourceCodeToSourceTypeMap();
		SourceType sourceType=sourceCodeToSourceTypeMap.get(sourceCode);
		com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum sourceTypeAttribute=sourceType.getType();
		String inVariable=null;
		if (sourceTypeAttribute.equals(SourceType.Type.VIEW)) {
			inVariable=operationType.getInVariable();
			if (inVariable==null || inVariable.isEmpty() ) {
				throw new Exception("Missing 'In-variable' for DFF_FIELD_NAME type operation");
			}
			Integer position=fieldNameToPositionMap.get(inVariable);
			if (position==null) {
				throw new Exception("Unable to find the variable '"+inVariable+"' in the view for DFF_FIELD_NAME type operation");
			}
			navigationMapper=sourceDataRow[position];
		}
		else {
			Integer position=fieldNameToPositionMap.get(UpgradeEngine.NAVIGATION_MAPPER_FIELD_NAME);
			if (position==null) {
				throw new Exception("Unable to find the column '"+UpgradeEngine.NAVIGATION_MAPPER_FIELD_NAME+"' in the inventory for DFF_FIELD_NAME type operation");
			}
			navigationMapper=sourceDataRow[position];
		}
				
		/*
		 * 
		 * debug code:
		if (navigationMapper==null) {
			FileUtils.println("### NEW ROW: navigationMapperIndex:"+navigationMapperIndex+" navigationMapper:"+navigationMapper);
			int index=0;
			for (String val:sourceDataRow) {
				FileUtils.println(index+" val:"+val);
				index++;
			}
			throw new Exception("OD teSt");
		}
		 */
		BigInteger dffFieldIndexBigInt=operationType.getDffFieldIndex();
		if (dffFieldIndexBigInt==null) {
			throw new Exception("Invalid Mapping, dffFieldIndex attribute is missing");
		}
		int dffFieldIndex=dffFieldIndexBigInt.intValue();
		String outVariable=operationType.getOutVariable();
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, out-variable attribute is missing");
		}
		String[] splitText=navigationMapper.split(ModelUtils.REPLACEMENT_SEPARATOR);
		
		//FileUtils.out.println("### NEW ROW, dffFieldIndex:"+dffFieldIndex+" navigationMapper:"+navigationMapper+" splitText:"+splitText.length);
		int dffFieldCounter=1;
		for (String val:splitText) {
			//System.out.println("val: "+val);
			if (dffFieldCounter==dffFieldIndex) {
				//System.out.println("found index: "+dffFieldIndex+" outVariable:"+outVariable+" val:"+val);
				variableToValueMap.put(outVariable,val);
				break;
			}
			dffFieldCounter++;
		}		
	}

}
