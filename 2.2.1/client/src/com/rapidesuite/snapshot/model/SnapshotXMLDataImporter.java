package com.rapidesuite.snapshot.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class SnapshotXMLDataImporter extends DefaultHandler
{
	private boolean insideHeaders = false;
	private String[] currentRow = null;
	private int currentColumnIndex = 0;
	private final StringBuffer currentContentBuffer = new StringBuffer(1024);
	private final List<String> headerList = new ArrayList<String>();
	private int rowCount = 0;
	private Short indexOfLabelColumn = null;
	private Set<String> labels = null;
	private List<String[]> tempList=null;
	private int batchSize;

	public SnapshotXMLDataImporter(int batchSize){
		this.batchSize=batchSize;
		tempList = new ArrayList<String[]>();
	}
	
	public List<String> getHeaderList()	{
		return this.headerList;
	}

	public int getRowCount(){
		return this.rowCount;
	}
	
	public Collection<String> getLabels(){
		return Collections.unmodifiableSet(this.labels);
	}

	@Override
	public void characters(final char[] chars,final int startIndex,final int endIndex)
	{
		if ( this.indexOfLabelColumn == null )	{
			this.currentContentBuffer.append(chars, startIndex, endIndex);
		}
	}

	@Override
	public void startElement(final String namespaceUri,final String localName,final String qualifiedName,final Attributes attributes)
	throws SAXException	{
		this.currentContentBuffer.setLength(0);
		if ( qualifiedName.equals("r"))	{
			this.currentRow = new String[this.headerList.size()];
		}
		else
		if ( qualifiedName.equals("h") ){
			this.insideHeaders = true;
		}
	}
	
	@Override
	public void endElement(final String namespaceUri,final String localName,final String qualifiedName)	throws SAXException{
		if ( qualifiedName.equals("c") ){
			String data = this.currentContentBuffer.toString();
			if ( this.insideHeaders ){
				this.headerList.add(data.trim());
			}
			else{
				this.currentRow[this.currentColumnIndex] = data;
				currentColumnIndex++;
			}
		}
		else
		if ( qualifiedName.equals("r") ){
			this.handleCurrentRow();
			this.currentColumnIndex = 0;
		}
		else
		if ( qualifiedName.equals("h") ){
			this.insideHeaders = false;
		}
	}

	private void handleCurrentRow() throws SAXException{
		this.rowCount++;
		this.tempList.add(this.currentRow);
		try{
			if (rowCount % batchSize ==0) {
				processBatch(tempList);
				tempList = new ArrayList<String[]>();
			}
		}
		catch ( final Exception e )	{
			throw new SAXException(e.getMessage());
		}
	}

	public abstract void processBatch(List<String[]> tempList) throws Exception;

	@Override
	public void endDocument()throws SAXException
	{
		try{
			processBatch(tempList);
			tempList = new ArrayList<String[]>();
		}
		catch(final Exception e){
			throw new Error(e);
		}
	}


}