package com.rapidesuite.client.common.gui.datagrid.inventory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import com.rapidesuite.client.common.XmlDataParser;
import com.rapidesuite.client.common.gui.datagrid.DataGridDataLoadThread;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class InventoryDataGridLoadThread extends DataGridDataLoadThread
{

	private XmlDataParser xmlDataParser;
	private List<String[]> dataRows;
	private int allDataGridColumnsCount;
	private boolean isConvertDataRowsToDataGridRows;
	
	public InventoryDataGridLoadThread(int allDataGridColumnsCount,InputStream dataInputStream,boolean isConvertDataRowsToDataGridRows)
	{
		super(dataInputStream);
		this.isConvertDataRowsToDataGridRows=isConvertDataRowsToDataGridRows;
		this.allDataGridColumnsCount=allDataGridColumnsCount;
		dataRows=new ArrayList<String[]>();
	}
		
	public void start() throws Exception
	{
		new Thread(this).start();
	}
	
	public void run() {
		try{
			if (inputStream==null) {
				return;
			}
			xmlDataParser= new XmlDataParser(false);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(inputStream, xmlDataParser);
			
			if (isConvertDataRowsToDataGridRows) {
				InventoryDataGridUtils.convertDataRowsToDataGridRows(xmlDataParser.getRowList(),allDataGridColumnsCount);
			}
			else {
				dataRows=xmlDataParser.getRowList();
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to load the data file. Error: "+e.getMessage());
		}
		finally {
			IOUtils.closeQuietly(inputStream);
			isProcessingCompleted=true;
		}
	}
		
	public List<String[]> getDataRows() {
		if (xmlDataParser==null) {
			return new ArrayList<String[]>();
		}
		return dataRows;
	}
	
	public static XmlDataParser getParserForHeaderOnly(InputStream dataInputStream) throws Exception {
		if (dataInputStream==null) {
			return null;
		}
		XmlDataParser xmlDataParser= new XmlDataParser(true);
		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(dataInputStream, xmlDataParser);
		}
		catch (SAXException e) {
			// ignoring it as we stopped the parser.
			if (!e.getMessage().startsWith("STOP")) {
				throw e;
			}
		}
		finally {
			IOUtils.closeQuietly(dataInputStream);
		}
		return xmlDataParser;
	}
			
}