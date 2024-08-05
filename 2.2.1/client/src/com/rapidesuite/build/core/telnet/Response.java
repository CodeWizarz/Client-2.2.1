/**************************************************************
 * $Revision: 31060 $:
 * $Author: john.snell $:
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/Response.java $:
 * $Id: Response.java 31060 2013-01-09 05:42:28Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

public class Response
{

	private String response;

	public Response()
	{
		response = "";
	}

	public void setResponse(String response)
	{
		this.response = response;
	}

	public String toString()
	{
		return "<Response>" + response + "</Response>";
	}

	public String getResponse()
	{
		return response;
	}
}
