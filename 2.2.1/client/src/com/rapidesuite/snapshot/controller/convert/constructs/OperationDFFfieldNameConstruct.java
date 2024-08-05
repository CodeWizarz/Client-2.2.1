package com.rapidesuite.snapshot.controller.convert.constructs;

import java.math.BigInteger;
import java.util.Map;
import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;
import com.rapidesuite.snapshot.model.ModelUtils;

public class OperationDFFfieldNameConstruct extends GenericConstruct{

	public OperationDFFfieldNameConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap) throws Exception {
		int navigationMapperIndex=sourceDataRow.length-3;
		String navigationMapper=sourceDataRow[navigationMapperIndex];
		
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
		//System.out.println("### NEW ROW, dffFieldIndex:"+dffFieldIndex+" navigationMapper:"+navigationMapper+" splitText:"+splitText.length);
		int dffFieldCounter=1;
		for (String val:splitText) {
			//System.out.println("val: "+val);
			if (dffFieldCounter==dffFieldIndex) {
				//System.out.println("found index: "+dffFieldIndex+" val:"+val);
				variableToValueMap.put(outVariable,val);
				break;
			}
			dffFieldCounter++;
		}		
	}

}
