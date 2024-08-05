/**************************************************************
 * $Revision: 31060 $:
 * $Author: john.snell $:
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/controller/ActionCompleteEvent.java $:
 * $Id: ActionCompleteEvent.java 31060 2013-01-09 05:42:28Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.controller;

import java.util.EventObject;

public class ActionCompleteEvent extends EventObject
{

	private static final long serialVersionUID = 1L;

	public ActionCompleteEvent(Object source)
	{
		super(source);
	}

}