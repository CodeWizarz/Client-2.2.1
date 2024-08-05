package com.rapidesuite.build.core.htmlplayback.engines.xul;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;

import com.rapidesuite.build.core.htmlplayback.HTMLRunnerConstants;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;

public class XULErrorThread extends Thread
{
	public int workerId;
	public InputStream inputstream;
	private String outputFileName;

	public XULErrorThread(int workerId, String folderName, InputStream inputstream) throws Exception
	{
		this.workerId = workerId;
		outputFileName = folderName + UtilsConstants.FORWARD_SLASH + HTMLRunnerConstants.workerErrorFile + ".txt";

		this.inputstream = inputstream;

		println("ERROR OUTPUT COLLECTION STARTING.");
	}

	private PrintWriter getPrintWriter() throws Exception
	{
		return new PrintWriter(new FileWriter(outputFileName, true));
	}

	private void println(String msg)
	{
		PrintWriter printWriter = null;
		try
		{
			printWriter = getPrintWriter();
			printWriter.println(CoreConstants.DATE_FORMAT_PATTERN.STANDARD.getDateFormat().format(new Date()) + ": " + msg);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if ( printWriter != null )
				printWriter.close();
		}
	}

	public void run()
	{
		try
		{
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line;
			FileUtils.println("Starting to listen for error messages for Worker: " + workerId);
			while ( (line = bufferedreader.readLine()) != null )
			{
				println(line);
			}
			FileUtils.println("Starting to listen for error messages for Worker: " + workerId + " COMPLETED.");
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

}