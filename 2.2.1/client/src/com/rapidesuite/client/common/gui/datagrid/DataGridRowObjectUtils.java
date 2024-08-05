package com.rapidesuite.client.common.gui.datagrid;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;

public class DataGridRowObjectUtils {
	
	private static final Class<?>[] parameters = new Class[]{URL.class};
	
	public static Class<?> generateDataGridRowClassFile(
			String dataGridRowClassName,List<DataGridColumn> dataGridColumns) 
	throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		CtClass exprClass = pool.get("org.openswing.swing.message.receive.java.ValueObjectImpl");
		CtClass dataGridClass = pool.makeClass(dataGridRowClassName);
		dataGridClass.setSuperclass(exprClass);
		for (DataGridColumn dataGridColumn:dataGridColumns) {
			createFieldSetterAndGetter(dataGridColumn.getAttributeClass(),dataGridColumn.getAttributeName(),dataGridClass);
		}
		dataGridClass.writeFile(Config.getTempFolder().getPath());
		addFile(Config.getTempFolder());
		return Class.forName(dataGridRowClassName);
	}
	
	private static void createFieldSetterAndGetter(Class<?> fieldClass,String fieldSuffix,CtClass dataGridClass)
	throws CannotCompileException
	{
		CtField field = CtField.make("private "+fieldClass.getCanonicalName()+" field"+fieldSuffix+";",dataGridClass);
		dataGridClass.addField(field);
		String getterMethod="public "+fieldClass.getCanonicalName()+" get"+fieldSuffix+"() { return field"+fieldSuffix+"; }";
		dataGridClass.addMethod(CtNewMethod.make(getterMethod,dataGridClass));
		String setterMethod="public void set"+fieldSuffix+"("+fieldClass.getCanonicalName()+" field"+fieldSuffix+") { this.field"+fieldSuffix+"=field"+fieldSuffix+"; }";
		dataGridClass.addMethod(CtNewMethod.make(setterMethod,dataGridClass));
	}

	private static void addFile(File f) throws IOException {
		addURL(f.toURI().toURL());
	}

	private static void addURL(URL u) throws IOException {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[]{u});
		} 
		catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}
	}
	
	public static void invokeDataGridSetter(Class<?> generatedClass,Object instanceOfClass,
			Class<?> argType,String argValue,String attributeName) 
	throws Exception {
		Object setterArgumentObject=null;
		if (argType == java.math.BigDecimal.class) {
			if (argValue!=null && !argValue.isEmpty()) {
				try {
					setterArgumentObject=new BigDecimal(argValue);
				} 
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		}
		else
		if (argType == java.lang.Integer.class) {
			try {
				setterArgumentObject=new Integer(argValue);
			} 
			catch (Exception e) {
				FileUtils.printStackTrace(e);
			}
		}
		else
		if (argType == Boolean.class) {
			setterArgumentObject=new Boolean(argValue);
		}
		else
		if (argType == java.util.Date.class) {
			if (argValue!=null && !argValue.isEmpty() && !argValue.equals("\"\"")) {
				try {
					DateFormat df = null;
					if (argValue.indexOf(":")!=-1){
						df=new SimpleDateFormat(DataGridConstants.ORACLE_DATE_FORMAT+" "+DataGridConstants.ORACLE_TIME_FORMAT);
					}
					else {
						df=new SimpleDateFormat(DataGridConstants.ORACLE_DATE_FORMAT);
					}
					java.util.Date utilDate=null;

					utilDate = df.parse(argValue);
					setterArgumentObject=new java.util.Date(utilDate.getTime());
				} 
				catch (Exception e) {
					FileUtils.printStackTrace(e);
					throw new Exception("Invalid date format for the value: '"+argValue+"', supported formats: '"+
							DataGridConstants.ORACLE_DATE_FORMAT+"' or '"+DataGridConstants.ORACLE_DATE_FORMAT+
							" "+DataGridConstants.ORACLE_TIME_FORMAT+"'");
				}
			}
		}
		else 
		if (argType == String.class ) {
			if (argValue==null) {
				argValue="";
			}
			setterArgumentObject=new String(argValue);
		}
		else {
			throw new Exception("invoking setter on unsupported class: '"+argType+"'");
		}
		Class<?>[] argTypes=new Class<?>[1];
		argTypes[0]=argType;
		Object[] argValues=new Object[1];
		argValues[0]=setterArgumentObject;
		invokeSetterObject(generatedClass,instanceOfClass,argTypes,argValues,attributeName);
	}
	
	private static void invokeSetterObject(Class<?> generatedClass,Object instanceOfClass,
			Class<?>[] argTypes,Object[] argValues,String attributeName) 
	throws Exception {
		Method method = generatedClass.getDeclaredMethod("set"+attributeName,argTypes);
		method.invoke(instanceOfClass,argValues);
	}
	
	public static Object invokeGetter(Class<?> generatedClass,Object instanceOfClass,
			Class<?>[] argTypes,Object[] argValues,String fieldSuffix) 
	throws Exception {
		Method method = generatedClass.getDeclaredMethod("get"+fieldSuffix,argTypes);
		return method.invoke(instanceOfClass,argValues);
	}
		
}
