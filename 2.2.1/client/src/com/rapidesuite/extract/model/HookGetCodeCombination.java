package com.rapidesuite.extract.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;

public class HookGetCodeCombination {

	private Map<String,String> keyToValueMap;
	
	public HookGetCodeCombination(){
		keyToValueMap=new HashMap<String,String>();
	}
	
	public void init(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {	
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String value="Select COM.CODE_COMBINATION_ID KEY,fnd_flex_ext.GET_SEGS('GL',Stri.Key_Flexfield_Code,STRI.STRUCTURE_INSTANCE_NUMBER,COM.CODE_COMBINATION_ID) VALUE"+
				" From Fnd_Kf_Str_Instances_B Stri,Gl_Code_Combinations Com"+
				" WHERE STRI.APPLICATION_ID = 101	AND stri.STRUCTURE_INSTANCE_NUMBER = com.chart_of_accounts_id";
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//System.out.println("data:"+data);
		keyToValueMap=ExtractUtils.getLOVFromXMLReport(outputBytes,false);
	}

	public List<ExtractDataRow> apply(int fieldIndex, List<ExtractDataRow> listToApply) throws Exception {
		for (ExtractDataRow extractDataRow:listToApply){
			String[] dataValues=extractDataRow.getDataValues();
			String key=dataValues[fieldIndex];
			if (key==null || key.isEmpty()) {
				continue;
			}
			String value=keyToValueMap.get(key);
			if (value!=null){
				dataValues[fieldIndex]=value;
			}
		}
		return listToApply;
	}

}
