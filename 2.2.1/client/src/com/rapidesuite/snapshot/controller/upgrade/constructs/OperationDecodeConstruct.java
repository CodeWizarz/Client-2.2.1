package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.client.dataConversion0000.ValueMapType;
import com.rapidesuite.client.dataConversion0000.ValuesMapType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class OperationDecodeConstruct extends GenericConstruct{

	public OperationDecodeConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> sourceCodeToPositionMap) throws Exception {
		String outVariable=operationType.getOutVariable();
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, out-variable attribute is missing");
		}
		String sourceDataValue=OperationReplaceConstruct.getSourceValue(operationType,variableToValueMap,sourceDataRow,sourceCodeToPositionMap);
		if (sourceDataValue==null) {
			sourceDataValue="";
		}
		String sharedMapName=operationType.getSharedMapName();
		if (sharedMapName!=null) {
			com.rapidesuite.client.dataConversionShared0000.ValueMapType[] sharedValueMapTypeArray=upgradeEngine.getUpgradeController().getSharedMappings(sharedMapName);
			if (sharedValueMapTypeArray==null) {
				throw new Exception("Invalid Mapping, Operation DECODE: cannot find the shared mapping name: '"+sharedMapName+"'");
			}
			for (com.rapidesuite.client.dataConversionShared0000.ValueMapType valueMapType:sharedValueMapTypeArray) {
				String source=valueMapType.getSource();
				String target=valueMapType.getTarget();
				if (sourceDataValue.equals(source)) {
					variableToValueMap.put(outVariable,target);
					return;
				}
			}
		}
		else {
			ValuesMapType valuesMapType=operationType.getValuesMap();
			ValueMapType[] valueMapTypeArray=valuesMapType.getValueMapArray();
			for (ValueMapType valueMapType:valueMapTypeArray) {
				String source=valueMapType.getSource();
				String target=valueMapType.getTarget();
				if (sourceDataValue.equals(source)) {
					variableToValueMap.put(outVariable,target);
					return;
				}
			}
		}
		// default if no match is to store the source value:
		variableToValueMap.put(outVariable,sourceDataValue);
	}
	
}