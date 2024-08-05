/**************************************************************
 * $Revision: 33949 $:
 * $Author: john.snell $:
 * $Date: 2013-06-20 17:22:09 +0700 (Thu, 20 Jun 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/Telnet.java $:
 * $Id: Telnet.java 33949 2013-06-20 10:22:09Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.client.common.util.FileUtils;

public class Telnet extends Thread
{

	private static final long DEFAULT_TIMEOUT_IN_MS = 5000;
	Socket socket;
	private TelnetProtocolHandler handler;
	private StringBuffer latestOutput;
	private final String encoding = "latin1";
	private static final String CRLF = "\n";
	private ScriptThread script;
	private TelnetTimeoutThread tt;
	private Boolean blocked;
	private long waitingCounter = 0;

	public Telnet(String host, int port) throws Exception
	{
		latestOutput = new StringBuffer();
		socket = new Socket(host, port);

		handler = new TelnetProtocolHandler()
		{
			public void write(byte[] b) throws IOException
			{
				socket.write(b);
			}
		};
	}

	public Socket getSocket()
	{
		return socket;
	}

	public void stopAll()
	{
		script.stopAll();
	}

	public void manualStop()
	{
		try
		{
			script.setTextReceived("TELNET MANUAL STOP INITIATED...\n");
			FileUtils.println("TELNET MANUAL STOP INITIATED...");
			this.disconnect();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public void connect() throws Exception
	{
		socket.connect();
	}

	public void disconnect() throws IOException
	{
		socket.disconnect();
	}

	public void setScript(ScriptThread script)
	{
		this.script = script;
	}

	public ScriptThread getScript()
	{
		return script;
	}

	public void successQuestion() throws Exception
	{
		Question currentQuestion = script.getCurrentQuestion();
		String response = currentQuestion.getResponse();
		sendText(response);
		script.setTextReceived(latestOutput.toString());

		latestOutput = new StringBuffer();
		if ( script.getQuestionTimeout() != -1 )
		{
			script.stopCurrentQuestionTimeout();
		}

		script.moveToNextQuestion();
		if ( script.getQuestionTimeout() != -1 )
		{
			script.startCurrentQuestionTimeout();
		}
	}

	public void failsQuestion() throws Exception
	{
		script.setTextReceived(latestOutput.toString());
		latestOutput = new StringBuffer();
	}

	public void scriptBlocked() throws Exception
	{
		synchronized (latestOutput)
		{
			if ( StringUtils.isBlank(latestOutput.toString()) )
			{
				waitingCounter++;
				startNewTimeOutThread();
				return;
			}

			if ( !script.hasPrompt(latestOutput.toString()) )
			{
				waitingCounter++;

				script.setTextReceived(latestOutput.toString());
				latestOutput = new StringBuffer();
				startNewTimeOutThread();
				return;
			}

			int num = socket.getInputStream().available();
			if ( num != 0 )
			{
				startNewTimeOutThread();
				return;
			}

			Question currentQuestion = script.getCurrentQuestion();
			if ( currentQuestion == null )
			{
				String suc = "*****************************************\n";
				suc = suc + "*****************************************\n";
				suc = suc + " ALL QUESTIONS OF THE SCRIPT EXECUTED... \n";
				suc = suc + " SESSION DISCONNECTED... \n";
				suc = suc + "*****************************************\n";
				suc = suc + "*****************************************\n";
				script.setTextReceived(latestOutput.toString() + "\n\n\n" + suc);
				script.setCompleted(true);
				disconnect();
				return;
			}

			boolean hasPattern = currentQuestion.hasPattern(latestOutput.toString());
			if ( hasPattern )
			{
				successQuestion();
			}
			else if ( !currentQuestion.isOptional() )
			{
				failsQuestion();
			}
			else
			{
				scriptBlocked();
			}
		}
	}

	private int read(byte[] b) throws IOException
	{
		int n = 0;
		n = handler.negotiate(b);
		if ( n > 0 )
		{
			return n;
		}

		while ( true )
		{
			n = socket.read(b);

			if ( n <= 0 )
			{
				return n;
			}

			handler.inputfeed(b, n);
			n = 0;
			while ( true )
			{
				n = handler.negotiate(b);
				if ( n > 0 )
				{
					return n;
				}
				if ( n == -1 )
				{
					break;
				}
			}

			return 0;
		}
	}

	public void startNewTimeOutThread()
	{
		if ( tt != null )
		{
			tt.setBlocked(false);
		}
		tt = new TelnetTimeoutThread(this, DEFAULT_TIMEOUT_IN_MS);
		tt.setBlocked(true);
		tt.start();
	}

	public void run()
	{
		byte[] b = new byte[256];
		int n = 0;
		latestOutput = new StringBuffer();

		blocked = Boolean.FALSE;
		Exception excep = null;
		while ( n >= 0 )
		{
			try
			{
				if ( tt != null )
				{
					tt.setBlocked(false);
				}

				tt = new TelnetTimeoutThread(this, DEFAULT_TIMEOUT_IN_MS);
				tt.start();
				tt.setBlocked(true);
				n = read(b);
				tt.setBlocked(false);

				if ( n > 0 )
				{
					synchronized (latestOutput)
					{
						latestOutput.append(new String(b, 0, n, encoding));
					}
				}
			}
			catch ( SocketException ex )
			{
				excep = ex;
				if ( !script.isCompleted() )
				{
					FileUtils.printStackTrace(ex);
					String text = "********************************************\n";
					text = text + "********************************************\n";
					text = text + "   ERROR - TELNET SCRIPT FAILED!!!!!!!!!!!  \n";
					text = text + " THE CONNECTION TO THE TARGET SERVER WAS INTERRUPTED.\n";
					text = text + " THE FOLLOWING EXCEPTION OCCURED: \n";
					text = text + ex.getMessage() + "\n";
					text = text + "********************************************\n";
					text = text + "********************************************\n";
					latestOutput.append(text);
					script.setTextReceived(latestOutput.toString());
					stopAll();
				}
				break;
			}
			catch ( Exception e )
			{
				excep = e;
				FileUtils.printStackTrace(e);
				String text = "********************************************\n";
				text = text + "********************************************\n";
				text = text + "   ERROR - TELNET SCRIPT FAILED!!!!!!!!!!!  \n";
				text = text + " THE CONNECTION TO THE TARGET SERVER WAS INTERRUPTED.\n";
				text = text + " THE FOLLOWING EXCEPTION OCCURED: \n";
				text = text + e.getMessage() + "\n";
				text = text + "********************************************\n";
				text = text + "********************************************\n";
				latestOutput.append(text);
				script.setTextReceived(latestOutput.toString());
				stopAll();
				break;
			}
		}

		if ( !script.isCompleted() && excep == null )
		{
			String text = "\n********************************************\n";
			text = text + "********************************************\n";
			text = text + " ERROR - TELNET SCRIPT FAILED!!!!!!!!!!!  \n";
			text = text + " THE CONNECTION TO THE TARGET SERVER WAS INTERRUPTED.\n";
			text = text + " THE FOLLOWING ERROR OCCURED: \n";
			text = text + " No more bytes to read, the connection was terminated on the server side\n";
			text = text + "********************************************\n";
			text = text + "********************************************\n";
			latestOutput.append(text);
			script.setTextReceived(latestOutput.toString());
			stopAll();
		}
	}

	public void sendText(String text) throws Exception
	{
		sendTextBytes(text);
		sendTextBytes(CRLF);
	}

	public void sendTextBytes(String text) throws Exception
	{
		write((text).getBytes());
	}

	private void write(byte[] b) throws IOException
	{
		handler.transpose(b);
	}

}
