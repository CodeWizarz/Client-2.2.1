package com.rapidesuite.client.common.gui.datagrid;

import java.io.InputStream;
import java.util.List;

public abstract class DataGridDataLoadThread implements Runnable
{
	
	protected InputStream inputStream;
	protected boolean isProcessingCompleted;
	
	public DataGridDataLoadThread(InputStream inputStream)
	{
		this.inputStream=inputStream;
		isProcessingCompleted=false;
	}
	
	public void start() throws Exception
	{
		new Thread(this).start();
	}
		
	public abstract List<String[]> getDataRows();

	public boolean isProcessingCompleted() {
		return isProcessingCompleted;
	}
	
}