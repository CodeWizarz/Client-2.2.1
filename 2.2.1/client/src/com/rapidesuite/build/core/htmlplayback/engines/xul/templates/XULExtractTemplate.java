/**************************************************
 * $Revision: 31060 $:
 * $Revision: 31060 $::
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/htmlplayback/engines/xul/templates/XULExtractTemplate.java $:
 * $Id: XULExtractTemplate.java 31060 2013-01-09 05:42:28Z john.snell $:
 *
 */

package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULExtractTemplate extends HTMLTemplate
{

	private XULEngine engine;

	public XULExtractTemplate(String templateName)
	{
		super(templateName);
	}

	public static enum Element {
		CHECKBOX, DROPDOWN, INPUT, SPAN,
	}

	public static Element getElementEnumByName(String elementName)
	{
		if ( elementName.equals(Element.CHECKBOX.toString()) )
		{
			return Element.CHECKBOX;
		}
		else if ( elementName.equals(Element.DROPDOWN.toString()) )
		{
			return Element.DROPDOWN;
		}
		else if ( elementName.equals(Element.INPUT.toString()) )
		{
			return Element.INPUT;
		}
		else if ( elementName.equals(Element.SPAN.toString()) )
		{
			return Element.SPAN;
		}
		else
		{
			throw new IllegalArgumentException("Unknown elementName = " + elementName);
		}
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;
		String element = getParameterValue("ELEMENT");
		String id = getParameterValue("ID");

		if ( Element.CHECKBOX.toString().equals(element) )
		{
			XULUtils.addToBuffer(engine.STEPS_LIST_VAR + " = \"getCheckBoxValueByID(\'" + id + "\');\"; \n");
		}
		else if ( Element.DROPDOWN.toString().equals(element) )
		{
			XULUtils.addToBuffer(engine.STEPS_LIST_VAR + " = \"getDropDownSelectedValueByID(\'" + id + "\');\"; \n");
		}
		else if ( Element.INPUT.toString().equals(element) )
		{
			XULUtils.addToBuffer(engine.STEPS_LIST_VAR + " = \"getInputFieldTextByID(\'" + id + "\');\"; \n");
		}
		else if ( Element.SPAN.toString().equals(element) )
		{
			XULUtils.addToBuffer(engine.STEPS_LIST_VAR + " = \"getSpanTextByID(\'" + id + "\');\"; \n");
		}
	}

}