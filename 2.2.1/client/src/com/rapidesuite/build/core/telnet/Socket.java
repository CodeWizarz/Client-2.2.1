/**************************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/Socket.java $:
 * $Id: Socket.java 31694 2013-03-04 06:33:20Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rapidesuite.client.common.util.FileUtils;

public class Socket
{

	private java.net.Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private String host;
	private int port;

	public Socket(String host, int port) throws Exception
	{
		this.host = host;
		this.port = port;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public boolean isClosed()
	{
		if ( socket == null )
		{
			return true;
		}
		return socket.isClosed();
	}

	public void connect() throws Exception
	{
		if ( host == null )
		{
			throw new Exception("Telnet: the host is null, cannot connect");
		}

		socket = new java.net.Socket(host, port);
		socket.setKeepAlive(true);
		int timeout = 7200000 * 100; // 7200000*100 = 200 hours;
		socket.setSoTimeout(timeout);
		timeout = socket.getSoTimeout();
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}

	/** Disconnect the socket and close the connection. */
	public void disconnect() throws IOException
	{
		if ( socket != null )
		{
			try
			{
				outputStream.close();
				inputStream.close();
			}
			catch ( Exception e )
			{
				FileUtils.printStackTrace(e);
			}
			socket.close();
			inputStream = null;
			outputStream = null;
			socket = null;
		}
	}

	public int read(byte[] b) throws IOException
	{
		if ( inputStream == null )
		{
			disconnect();
			return -1;
		}
		int n = inputStream.read(b);
		if ( n < 0 )
		{
			disconnect();
		}
		return n;
	}

	public void write(byte[] b) throws IOException
	{
		if ( outputStream == null )
		{
			return;
		}
		try
		{
			outputStream.write(b);
		}
		catch ( IOException e )
		{
			disconnect();
		}
	}

	public InputStream getInputStream()
	{
		return inputStream;
	}

}
