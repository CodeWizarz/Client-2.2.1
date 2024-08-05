package com.rapidesuite.snapshot.controller.convert.constructs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.dataConversion0000.ColumnType;
import com.rapidesuite.client.dataConversion0000.ColumnsType;
import com.rapidesuite.client.dataConversion0000.ConditionType;
import com.rapidesuite.client.dataConversion0000.ConditionsType;
import com.rapidesuite.client.dataConversion0000.FromLeftSourceType;
import com.rapidesuite.client.dataConversion0000.FromRightSourceType;
import com.rapidesuite.client.dataConversion0000.FromType;
import com.rapidesuite.client.dataConversion0000.ValueType;
import com.rapidesuite.client.dataConversion0000.FromType.Type.Enum;
import com.rapidesuite.client.dataConversion0000.LeftOperandType;
import com.rapidesuite.client.dataConversion0000.RightOperandType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.client.dataConversion0000.ViewType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;
import com.rapidesuite.snapshot.view.convert.ConvertSourceGridRecordInformation;

public class SourceViewTypeConstruct extends GenericConstruct {

	public SourceViewTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}
	
	public void process(SourceType sourceType) throws Exception {
		Map<String, SourceType> sourceCodeToSourceTypeMap=convertEngine.getSourceCodeToSourceTypeMap();
		SourceType sourceTypeTemp=sourceCodeToSourceTypeMap.get(sourceType.getCode());
		if (sourceTypeTemp!=null){
			throw new Exception("Source code '"+sourceType.getCode()+"' is already defined");
		}
		sourceCodeToSourceTypeMap.put(sourceType.getCode(),sourceType);

		ViewType viewType=sourceType.getView();
		if (viewType==null) {
			throw new Exception("Missing VIEW type xml tag in the mapping file");
		}
		
		FromType fromType=viewType.getFrom();
		Enum type=fromType.getType();
		
		if (type.equals(FromType.Type.CARTESIAN) || type.equals(FromType.Type.LEFT_JOIN) || type.equals(FromType.Type.LEFT_JOIN_STRICT)) {
			FromLeftSourceType fromLeftSourceType=fromType.getFromLeftSource();
			String leftSourceCode=fromLeftSourceType.getCode();
			FromRightSourceType fromRightSourceType=fromType.getFromRightSource();
			String rightSourceCode=fromRightSourceType.getCode();
			
			List<String[]> sourceLeftDataRows=convertEngine.getSourceCodeToDataRowsMap(leftSourceCode);
			if (sourceLeftDataRows==null){
				throw new Exception("No data found for view source code: '"+leftSourceCode+"'");
			}
			
			List<String[]> sourceRightDataRows=convertEngine.getSourceCodeToDataRowsMap(rightSourceCode);
			if (sourceRightDataRows==null){
				throw new Exception("No data found for view source code: '"+rightSourceCode+"'");
			}
			
			ConditionsType conditionsType=viewType.getConditions();
			ConditionType[] conditionTypeArray=conditionsType.getConditionArray();
			Map<String, Integer> leftFieldNameToPositionMap=convertEngine.getFieldNameToPositionMap(leftSourceCode);
			if (leftFieldNameToPositionMap==null){
				throw new Exception("No fields found for the view source code: '"+leftSourceCode+"'");
			}
			Map<String, Integer> rightFieldNameToPositionMap=convertEngine.getFieldNameToPositionMap(rightSourceCode);
			if (rightFieldNameToPositionMap==null){
				throw new Exception("No fields found for the view source code: '"+rightSourceCode+"'");
			}
			
			ColumnsType columnsType=viewType.getColumns();
			ColumnType[] columnTypeArray=columnsType.getColumnArray();
			Map<String, Integer> columnNameToPositionMap=new HashMap<String, Integer>();
			int position=0;
			List<ColumnTypeDetail> columnTypeDetailList=new ArrayList<ColumnTypeDetail>();
			for (ColumnType columnType:columnTypeArray) {
				String outputName=columnType.getOutputName();
				columnNameToPositionMap.put(outputName, position);
				
				ColumnTypeDetail columnTypeDetail=new ColumnTypeDetail();
				columnTypeDetailList.add(columnTypeDetail);
				
				com.rapidesuite.client.dataConversion0000.ColumnType.Type.Enum colType=columnType.getType();
				columnTypeDetail.type=colType;
				if (colType.equals(ColumnType.Type.COLUMN)) {
					String columnName=columnType.getName();
					columnTypeDetail.name=columnName;
					com.rapidesuite.client.dataConversion0000.ColumnType.Direction.Enum direction=columnType.getDirection();
					columnTypeDetail.direction=direction;
					if (direction.equals(ColumnType.Direction.LEFT)) {
						Integer columnPosition=leftFieldNameToPositionMap.get(columnName);
						if (columnPosition==null) {
							throw new Exception("Invalid column name '"+columnName+"'");
						}
						columnTypeDetail.leftPosition=columnPosition;
					}
					else {
						Integer columnPosition=rightFieldNameToPositionMap.get(columnName);
						if (columnPosition==null) {
							throw new Exception("Invalid column name '"+columnName+"'");
						}
						columnTypeDetail.rightPosition=columnPosition;
					}
				}
				else{
					throw new Exception("Unsupported COLUMN type '"+colType+"'");
				}
				
				position++;	
			}
						
			List<ConditionDetail> conditionsList=new ArrayList<ConditionDetail>();
			for (ConditionType conditionType:conditionTypeArray) {
				LeftOperandType leftOperandType=conditionType.getLeftOperand();
				RightOperandType rightOperandType=conditionType.getRightOperand();
				
				ValueType leftOperandValueTypeObj=leftOperandType.getValue();
				ValueType rightOperandValueTypeObj=rightOperandType.getValue();
			
				com.rapidesuite.client.dataConversion0000.ValueType.Type.Enum leftOperandValueType=leftOperandValueTypeObj.getType();
				String leftOperandValueName=leftOperandValueTypeObj.getName();
				String leftOperandValueText=leftOperandValueTypeObj.getText();
				Integer leftOperandNodeIndex=null;
				if (leftOperandValueType.equals(ValueType.Type.COLUMN)) {
					leftOperandNodeIndex=leftFieldNameToPositionMap.get(leftOperandValueName);
				}
				
				com.rapidesuite.client.dataConversion0000.ValueType.Type.Enum rightOperandValueType=rightOperandValueTypeObj.getType();
				String rightOperandValueName=rightOperandValueTypeObj.getName();
				String rightOperandValueText=rightOperandValueTypeObj.getText();
				Integer rightOperandNodeIndex=null;
				if (rightOperandValueType.equals(ValueType.Type.COLUMN)) {
					rightOperandNodeIndex=rightFieldNameToPositionMap.get(rightOperandValueName);
				}
				
				com.rapidesuite.client.dataConversion0000.ConditionType.Operator.Enum operator=conditionType.getOperator();
				
				ConditionDetail conditionDetail=new ConditionDetail();
				conditionsList.add(conditionDetail);
				conditionDetail.leftType=leftOperandValueType;
				conditionDetail.leftOperandValueName=leftOperandValueName;
				conditionDetail.leftOperandValueText=leftOperandValueText;
				conditionDetail.leftOperandNodeIndex=leftOperandNodeIndex;
				
				conditionDetail.rightType=rightOperandValueType;
				conditionDetail.rightOperandValueName=rightOperandValueName;
				conditionDetail.rightOperandValueText=rightOperandValueText;
				conditionDetail.operator=operator;
				conditionDetail.rightOperandNodeIndex=rightOperandNodeIndex;
			}
						
			List<String[]> outputDataRows=new ArrayList<String[]>();
			int index=0;
			
			BigInteger startRecordFromLeft=fromLeftSourceType.getStartRecord();
			BigInteger endRecordFromLeft=fromLeftSourceType.getEndRecord();
			BigInteger startRecordFromRight=fromRightSourceType.getStartRecord();
			BigInteger endRecordFromRight=fromRightSourceType.getEndRecord();
			int startFromLeftRecordIndex=0;
			int endFromLeftRecordIndex=sourceLeftDataRows.size();
			if (startRecordFromLeft!=null) {
				startFromLeftRecordIndex=startRecordFromLeft.intValue()-1; // starting at 0
			}
			if (endRecordFromLeft!=null) {
				endFromLeftRecordIndex=endRecordFromLeft.intValue();
			}
			
			int startFromRightRecordIndex=0;
			int endFromRightRecordIndex=sourceRightDataRows.size();
			if (startRecordFromRight!=null) {
				startFromRightRecordIndex=startRecordFromRight.intValue()-1; // starting at 0
			}
			if (endRecordFromRight!=null) {
				endFromRightRecordIndex=endRecordFromRight.intValue();
			}
			
			ConvertSourceGridRecordInformation convertSourceGridRecordInformation=convertEngine.getConvertSourceGridRecordInformation();
			for (int i=startFromLeftRecordIndex;i<endFromLeftRecordIndex;i++) {
				String[] leftSourceDataRow=sourceLeftDataRows.get(i);
				
				if (index % 1000==0) {
					//System.out.println(index+" / "+sourceLeftDataRows.size());
					convertSourceGridRecordInformation.setRemarks("Creating View ("+Utils.formatNumberWithComma(index)+
							" / "+Utils.formatNumberWithComma(sourceLeftDataRows.size())+")");
				}
				index++;
				
				boolean hasAtLeastOneMatchedRows=false;
				for (int j=startFromRightRecordIndex;j<endFromRightRecordIndex;j++) {
						String[] rightSourceDataRow=sourceRightDataRows.get(j);
						
					boolean isAllConditionsSuccess=true;
					for (ConditionDetail conditionDetail:conditionsList) {						
						boolean isConditionSuccess=processGeneric(
								conditionDetail,leftSourceCode,leftSourceDataRow,rightSourceCode,rightSourceDataRow);
						
						if (!isConditionSuccess) {
							isAllConditionsSuccess=false;
						}									
					}
					if (isAllConditionsSuccess) {
						hasAtLeastOneMatchedRows=true;
						
						// new joined data row to create
						String[] dataRow=createDataRow(leftSourceDataRow,rightSourceDataRow,columnTypeDetailList);
						outputDataRows.add(dataRow);
						
						// check if we enforce strict Left join (only one match on target rows)
						if (type.equals(FromType.Type.LEFT_JOIN_STRICT)) {
							break;
						}
					}
				}
				
				if ( (type.equals(FromType.Type.LEFT_JOIN)||type.equals(FromType.Type.LEFT_JOIN_STRICT)) && !hasAtLeastOneMatchedRows) {
					// take the left row without any data for target columns
					String[] dataRow=createDataRow(leftSourceDataRow,null,columnTypeDetailList);
					outputDataRows.add(dataRow);
				}
			}
			ConvertSourceTypeGeneric convertSourceTypeGeneric=new ConvertSourceTypeGeneric();
			convertSourceTypeGeneric.setDataRows(outputDataRows);
			convertSourceTypeGeneric.setColumnNameToPositionMap(columnNameToPositionMap);
			convertEngine.getSourceCodeToConvertSourceTypeGenericMap().put(sourceType.getCode(), convertSourceTypeGeneric);
		}
		else {
			throw new Exception("Unsupported FROM type '"+type+"'");
		}
	}
	
	private String[] createDataRow(String[] leftSourceDataRow,String[] rightSourceDataRow,List<ColumnTypeDetail> columnTypeDetailList) throws Exception {
		String[] dataRow=new String[columnTypeDetailList.size()];
		int position=0;
		for (ColumnTypeDetail columnTypeDetail:columnTypeDetailList) {
			if (columnTypeDetail.type.equals(ColumnType.Type.COLUMN)) {
				String columnName=columnTypeDetail.name;
				com.rapidesuite.client.dataConversion0000.ColumnType.Direction.Enum direction=columnTypeDetail.direction;
				String value=null;
				if (direction.equals(ColumnType.Direction.LEFT)) {
					Integer columnPosition=columnTypeDetail.leftPosition;
					if (columnPosition==null) {
						throw new Exception("Invalid column name '"+columnName+"'");
					}
					value=leftSourceDataRow[columnPosition];
				}
				else {
					Integer columnPosition=columnTypeDetail.rightPosition;
					if (columnPosition==null) {
						throw new Exception("Invalid column name '"+columnName+"'");
					}
					// This is for supporting LEFT JOIN where we return the rows from the Left source table
					// but blank for all the columns from the Right source table.
					if (rightSourceDataRow==null) {
						value="";
					}
					else {
						value=rightSourceDataRow[columnPosition];
					}
				}
				dataRow[position]=value;
			}
			else{
				throw new Exception("Unsupported COLUMN type '"+columnTypeDetail.type+"'");
			}
			position++;
		}
		return dataRow;
	}
	
	public static boolean processGeneric(ConditionDetail conditionDetail,
			String leftSourceCode,String[] leftSourceDataRow,
			String rightSourceCode,String[] rightSourceDataRow
			) throws Exception {
		String leftOperandNodeValue=null;
		if (conditionDetail.leftType.equals(ValueType.Type.COLUMN)) {
			Integer leftOperandNodeIndex=conditionDetail.leftOperandNodeIndex;
			if (leftOperandNodeIndex==null) {
				throw new Exception("Invalid Mapping - cannot find the column '"+conditionDetail.leftOperandValueName+"' in the source code '"+leftSourceCode+"'");
			}
			leftOperandNodeValue=leftSourceDataRow[leftOperandNodeIndex];	
		}
		else
			if (conditionDetail.leftType.equals(ValueType.Type.TEXT)) {
				leftOperandNodeValue=conditionDetail.leftOperandValueText;
			}
				
		String rightOperandNodeValue=null;
		if (conditionDetail.rightType.equals(ValueType.Type.COLUMN)) {
			Integer rightOperandNodeIndex=conditionDetail.rightOperandNodeIndex;
			if (rightOperandNodeIndex==null) {
				throw new Exception("Invalid Mapping - cannot find the column '"+conditionDetail.rightOperandValueName+"' in the source code '"+rightSourceCode+"'");
			}
			rightOperandNodeValue=rightSourceDataRow[rightOperandNodeIndex];	
		}
		else
			if (conditionDetail.rightType.equals(ValueType.Type.TEXT)) {
				rightOperandNodeValue=conditionDetail.rightOperandValueText;
			}

		if (leftOperandNodeValue==null) {
			leftOperandNodeValue="";
		}
		if (rightOperandNodeValue==null) {
			rightOperandNodeValue="";
		}
		
		//System.out.println("leftOperandNodeValue: '"+leftOperandNodeValue+"' rightOperandNodeValue: '"+rightOperandNodeValue+"'");
		
		if (conditionDetail.operator==ConditionType.Operator.EQUAL) {
			return leftOperandNodeValue.equals(rightOperandNodeValue);
		}
		else
		if (conditionDetail.operator==ConditionType.Operator.NOT_EQUAL) {
			return !leftOperandNodeValue.equals(rightOperandNodeValue);
		}
		throw new Exception("Unsupported operator: '"+conditionDetail.operator+"'");
	}

}
