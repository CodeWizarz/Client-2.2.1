package com.rapidesuite.inject.selenium.commands;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.ComparisonType;
import com.erapidsuite.configurator.navigation0005.LeftOperandType;
import com.erapidsuite.configurator.navigation0005.RightOperandType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class ComparisonTypeCommand extends Command{

	public ComparisonTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public boolean process(ComparisonType comparisonType) throws Exception {
		LeftOperandType leftOperandType=comparisonType.getLeftOperand();
		RightOperandType rightOperandType=comparisonType.getRightOperand();
		ComparisonType.Operator.Enum operator=comparisonType.getOperator();

		XmlObject leftOperandXMLObject=(XmlObject)leftOperandType;
		String leftOperandNodeValue=((SimpleValue)leftOperandXMLObject).getStringValue();
		leftOperandNodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(worker.getBatchInjectionTracker(),
				leftOperandXMLObject,leftOperandNodeValue,true);
		worker.println("leftOperandNodeValue: '"+leftOperandNodeValue+"'");

		XmlObject rightOperandXMLObject=(XmlObject)rightOperandType;
		String rightOperandNodeValue=((SimpleValue)rightOperandXMLObject).getStringValue();
		//System.out.println("$$$$$$$$$$$$$   COMPARISON BEFORE CHECKING RIGHT OPERAND $$$$$$$$$$$$$$$$$$$$$$");
		rightOperandNodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(worker.getBatchInjectionTracker(),
				rightOperandXMLObject,rightOperandNodeValue,true);
		worker.println("rightOperandNodeValue: '"+rightOperandNodeValue+"'");
		//System.out.println("$$$$$$$$$$$$$  rightOperandNodeValue: '"+rightOperandNodeValue+"' $$$$$$$$$$$$$$$$$$$$$$");
		
		if (operator==ComparisonType.Operator.EQUAL) {
			return leftOperandNodeValue.equals(rightOperandNodeValue);
		}
		else
		if (operator==ComparisonType.Operator.NOT_EQUAL) {
			return !leftOperandNodeValue.equals(rightOperandNodeValue);
		}
		else {
			throw new Exception("Unsupported operator: "+operator);
		}
	}

}
