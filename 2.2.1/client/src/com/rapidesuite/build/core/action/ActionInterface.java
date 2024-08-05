/**************************************************************
 * $Revision: 32318 $:
 * $Author: john.snell $:
 * $Date: 2013-04-29 12:36:14 +0700 (Mon, 29 Apr 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/action/ActionInterface.java $:
 * $Id: ActionInterface.java 32318 2013-04-29 05:36:14Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.action;

public interface ActionInterface
{
	public void fireActionCompletedEvent();

	public void start() throws Exception;

	public void setGUIOutput(String output);
	
	public void join() throws InterruptedException;
}