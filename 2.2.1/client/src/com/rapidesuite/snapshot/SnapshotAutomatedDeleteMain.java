package com.rapidesuite.snapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.snapshot.model.ModelUtils;

public class SnapshotAutomatedDeleteMain {
	
	public static final String JDBC_STRING_KEY="jdbcString";
	public static final String AGE_KEY="age";	
	public static final String DELETE_TYPE_KEY="deleteType";	
	public static final String INCLUDE_SOFT_DELETE_KEY="includeSoftDelete";		
	public static final String PHYSICAL_DELETE_KEY ="P";		
	public static final String SOFT_DELETE_KEY ="S";
	public static final String INCLUDE_SOFT_DELETE_VALUE_KEY ="Y";	
	public static final String NOT_INCLUDE_SOFT_DELETE_VALUE_KEY ="N";		
	
	public static void main(String[] args) throws Exception{
		try{
			if(args.length>0){
				deleteSnapshot(args);
			}else{
				throw new Exception("PLEASE PROVIDE PARAMETERS IN YOUR COMMAND FOR DELETING SNAPSHOTS.");
			}
		}catch (Throwable t){
			t.printStackTrace();
		}
	}	
	public static void deleteSnapshot(){
		System.out.println("######## "+new Date()+" : START PROCESS #####################################");
		String jdbcString = "jdbc:oracle:thin:xx_rs_taty_221_nu/xx_rs_taty_221_nu@oratest90.rapidesuite.com:1521:erpp";		
		int age = 16;
		String deleteType = "S";
        boolean isIncludeSoftDeletedSnapshots = false;
        try {
        	ModelUtils.deleteSnapshotByAge(jdbcString,age,deleteType,isIncludeSoftDeletedSnapshots);
        	System.out.println("######## "+new Date()+" : END PROCESS #####################################");
		} catch (Exception e) {
			System.out.println("CANNOT DELETED SNAPSHOT, ERROR : "+e.getMessage());
			e.printStackTrace();
		} 
	}	
	public static void deleteSnapshot(String args[]){
		System.out.println("######## "+new Date()+" : START PROCESS #####################################");
        try {
        	Map<String,String> argumentMap = readArgumentFromArgumentString(args);
        	if(!argumentMap.isEmpty()){
        		String jdbcString = argumentMap.get(JDBC_STRING_KEY);
        		int age = Integer.parseInt(argumentMap.get(AGE_KEY));
        		String deleteType = argumentMap.get(DELETE_TYPE_KEY);
        		String includeSoftDeletedSnapshots = argumentMap.get(INCLUDE_SOFT_DELETE_KEY);
        		boolean isIncludeSoftDeletedSnapshots = false;
        		if(includeSoftDeletedSnapshots!=null && "Y".equals(includeSoftDeletedSnapshots)){
        			isIncludeSoftDeletedSnapshots = true;
        		}
        		ModelUtils.deleteSnapshotByAge(jdbcString,age,deleteType,isIncludeSoftDeletedSnapshots);
        	}
			System.out.println("######## "+new Date()+" : END PROCESS #####################################");
		} catch (Exception e) {
			System.out.println("CANNOT DELETE SNAPSHOT, ERROR : "+e.getMessage());
			e.printStackTrace();
		} 
	}
	
	public static Map<String,String> readArgumentFromArgumentString(String [] args) throws Exception{
		Map<String,String> toReturn = new HashMap<String,String>();
		try{
			String jdbcString = "";
			String deleteType =  "";
			String age = "";
			String includeSoftDelete = "N";
			
			if(args.length>0){
				jdbcString = args[0].substring(args[0].indexOf("=")+1, args[0].length()).trim();
				age = args[1].substring(args[1].indexOf("=")+1, args[1].length()).trim();
				deleteType = args[2].substring(args[2].indexOf("=")+1, args[2].length()).trim();
				if(PHYSICAL_DELETE_KEY.equals(deleteType)){
					includeSoftDelete = args[3].substring(args[3].indexOf("=")+1, args[3].length()).trim();
				}
				validateJDBCString(jdbcString);
				validateParameter(age,deleteType,includeSoftDelete);
				
				toReturn.put(JDBC_STRING_KEY, jdbcString);
				toReturn.put(AGE_KEY, age);
				toReturn.put(DELETE_TYPE_KEY, deleteType);
				toReturn.put(INCLUDE_SOFT_DELETE_KEY, includeSoftDelete);
				
				return toReturn;
			}else{
				throw new Exception("ARGUMENT VALUE MISSING!!,PLEASE CHECK YOUR COMMAND.");
			}
			
		}catch(Exception e){
			throw new Exception("CANNOT READ ARGUMENTS, ERROR : "+e.getMessage());
		}
		
	}
	public static void validateParameter(String ageStr, String deleteType,String includeSoftDelete )throws Exception{
		try{
			int age = Integer.parseInt(ageStr);
			if(age<0){
				throw new Exception("Age must be equal or more than 0.");
			}
		}catch(NumberFormatException  e){
			throw new Exception("Age must be the number.");
		}
		if(!PHYSICAL_DELETE_KEY.equals(deleteType) && !SOFT_DELETE_KEY.equals(deleteType)){
			throw new Exception("Delete type value must be P for physical delete or S for soft delete.");
		}
		if(includeSoftDelete.length()>0 && !INCLUDE_SOFT_DELETE_VALUE_KEY.equals(includeSoftDelete) 
				&& !NOT_INCLUDE_SOFT_DELETE_VALUE_KEY.equals(includeSoftDelete)){
			throw new Exception("Include soft delete value must be Y for including or N for not including.");
		}
	}
	public static void validateJDBCString(String jdbcString) throws Exception{
		try{
			DatabaseUtils.getJDBCConnectionFromJDBCStringGeneric(jdbcString,true);
			
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("JDBCString is not correct, please check your command.");
		}
	}
}
