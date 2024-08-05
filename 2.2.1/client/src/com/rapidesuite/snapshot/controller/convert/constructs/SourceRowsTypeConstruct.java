package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidesuite.client.dataConversion0000.FieldDefinitionType;
import com.rapidesuite.client.dataConversion0000.FieldValueType;
import com.rapidesuite.client.dataConversion0000.FieldValuesType;
import com.rapidesuite.client.dataConversion0000.FieldsDefinitionType;
import com.rapidesuite.client.dataConversion0000.RowType;
import com.rapidesuite.client.dataConversion0000.RowsType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class SourceRowsTypeConstruct extends GenericConstruct {

	public SourceRowsTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}
	
	public void process(SourceType sourceType) throws Exception {
		Map<String, SourceType> sourceCodeToSourceTypeMap=convertEngine.getSourceCodeToSourceTypeMap();
		SourceType sourceTypeTemp=sourceCodeToSourceTypeMap.get(sourceType.getCode());
		if (sourceTypeTemp!=null){
			throw new Exception("Source code '"+sourceType.getCode()+"' is already defined");
		}
		sourceCodeToSourceTypeMap.put(sourceType.getCode(),sourceType);

		RowsType rowsType=sourceType.getRows();
		if (rowsType==null) {
			throw new Exception("Missing ROWS tag in the mapping file");
		}
		FieldsDefinitionType fieldsDefinitionType=sourceType.getFieldsDefinition();
		if (fieldsDefinitionType==null) {
			throw new Exception("Missing FIELDSDEFINITION xml tag in the mapping file");
		}
		FieldDefinitionType[] fieldDefinitionTypeArray=fieldsDefinitionType.getFieldDefinitionArray();
		
		Map<String, Integer> columnNameToPositionMap=new HashMap<String, Integer>();
		int position=0;
		for (FieldDefinitionType fieldDefinitionType:fieldDefinitionTypeArray) {
			String name=fieldDefinitionType.getName();
			columnNameToPositionMap.put(name, position);
			position++;
		}
		
		List<String[]> outputDataRows=new ArrayList<String[]>();
		RowType[] rowTypeArray=rowsType.getRowArray();
		for (RowType rowType:rowTypeArray) {
			FieldValuesType fieldValuesType=rowType.getFieldValues();
			FieldValueType[] fieldValueTypeArray=fieldValuesType.getFieldValueArray();
			if (fieldValueTypeArray.length!=fieldDefinitionTypeArray.length) {
				throw new Exception("Invalid mapping file, source ROWS type is incorrect: FIELDSDEFINITION and FIELDVALUES do not have the same count of fields");
			}
			String[] dataRow=new String[fieldDefinitionTypeArray.length];
			outputDataRows.add(dataRow);
			int index=0;
			for (FieldValueType fieldValueType:fieldValueTypeArray) {
				String value=fieldValueType.getValue();
				dataRow[index++]=value;
			}
		}
		ConvertSourceTypeGeneric convertSourceTypeGeneric=new ConvertSourceTypeGeneric();
		convertSourceTypeGeneric.setDataRows(outputDataRows);
		convertSourceTypeGeneric.setColumnNameToPositionMap(columnNameToPositionMap);
		convertEngine.getSourceCodeToConvertSourceTypeGenericMap().put(sourceType.getCode(), convertSourceTypeGeneric);
	}
	
}