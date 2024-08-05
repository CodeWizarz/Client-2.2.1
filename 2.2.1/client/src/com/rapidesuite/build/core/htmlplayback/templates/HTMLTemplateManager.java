package com.rapidesuite.build.core.htmlplayback.templates;

import java.util.HashMap;
import java.util.Map;

import com.rapidesuite.build.core.htmlplayback.HTMLRunnerConstants;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULAlphabeticIterator;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULAnchorParentTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULAnchorTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULAreaTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULButtonTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULCheckBoxTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULClickElementTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULCloseWindowTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULDOMLoadingTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULDataXMLFileTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULDefineFieldSet;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULEndRepeatIfNotFoundTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULErrorTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULExtractTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULFunctionTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULGetElementTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULInputTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULListOfLinksTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULLoadURLTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULLookupBlankTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULLookupTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULNoDOMLoadingTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULOnChangeTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULOpenWindowTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULRadioButtonTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULRepeatIfNotFoundTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULResetLookupTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULSelectTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULSleepTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULSpanTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULSwitchBackDOMTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULSwitchDOMTemplate;
import com.rapidesuite.build.core.htmlplayback.engines.xul.templates.XULTextAreaTemplate;
import com.rapidesuite.build.core.htmlplayback.utils.HTMLUtils;
import com.rapidesuite.configurator.autoinjectors.navigation.html.templates.TemplateOnChange;

public class HTMLTemplateManager
{

	private Map<String, HTMLTemplate> templates;

	public static final String TEMPLATE_LOAD_URL = "TEMPLATE_LOAD_URL";
	public static final String TEMPLATE_DOM_LOADING = "TEMPLATE_DOM_LOADING";
	public static final String TEMPLATE_INPUT = "TEMPLATE_INPUT";
	public static final String TEMPLATE_INPUT_SUBMIT = "TEMPLATE_INPUT_SUBMIT";
	public static final String TEMPLATE_BUTTON = "TEMPLATE_BUTTON";
	public static final String TEMPLATE_ANCHOR = "TEMPLATE_ANCHOR";
	public static final String TEMPLATE_SELECT = "TEMPLATE_SELECT";
	public static final String TEMPLATE_CHECKBOX = "TEMPLATE_CHECKBOX";
	public static final String TEMPLATE_RADIOBUTTON = "TEMPLATE_RADIOBUTTON";
	public static final String TEMPLATE_SPAN = "TEMPLATE_SPAN";
	public static final String TEMPLATE_TEXTAREA = "TEMPLATE_TEXTAREA";
	public static final String TEMPLATE_AREA = "TEMPLATE_AREA";
	public static final String TEMPLATE_GET_ELEMENT = "TEMPLATE_GET_ELEMENT";
	public static final String TEMPLATE_ERROR = "TEMPLATE_ERROR";
	public static final String TEMPLATE_CLICK = "TEMPLATE_CLICK";

	public static final String TEMPLATE_LOOKUP = "TEMPLATE_LOOKUP";
	public static final String TEMPLATE_RESET_LOOKUP = "TEMPLATE_RESET_LOOKUP";
	public static final String TEMPLATE_LOOP = "TEMPLATE_LOOP";
	public static final String TEMPLATE_IF = "TEMPLATE_IF";
	public static final String TEMPLATE_ELSE = "TEMPLATE_ELSE";
	public static final String TEMPLATE_END_IF = "TEMPLATE_END_IF";
	public static final String TEMPLATE_END_LOOP = "TEMPLATE_END_LOOP";
	public static final String TEMPLATE_BREAK_LOOP = "TEMPLATE_BREAK_LOOP";
	public static final String TEMPLATE_ON_CHANGE = "ON_CHANGE";
	public static final String TEMPLATE_OPEN_WINDOW = "TEMPLATE_OPEN_WINDOW";
	public static final String TEMPLATE_CLOSE_WINDOW = "TEMPLATE_CLOSE_WINDOW";
	public static final String TEMPLATE_SWITCH_DOM = "TEMPLATE_SWITCH_DOM";
	public static final String TEMPLATE_SWITCH_BACK_DOM = "TEMPLATE_SWITCH_BACK_DOM";
	public static final String TEMPLATE_SLEEP = "TEMPLATE_SLEEP";

	public static final String TEMPLATE_PARENT_ANCHOR = "TEMPLATE_PARENT_ANCHOR";
	public static final String TEMPLATE_LOOKUP_BLANK = "TEMPLATE_LOOKUP_BLANK";
	public static final String TEMPLATE_NO_DOM_LOADING = "TEMPLATE_NO_DOM_LOADING";
	public static final String TEMPLATE_REPEAT_IF_NOT_FOUND = "TEMPLATE_REPEAT_IF_NOT_FOUND";
	public static final String TEMPLATE_END_REPEAT_IF_NOT_FOUND = "TEMPLATE_END_REPEAT_IF_NOT_FOUND";

	public static final String TEMPLATE_DEFINE_FIELD_SET = "TEMPLATE_DEFINE_FIELD_SET";
	public static final String TEMPLATE_LIST_OF_LINKS = "TEMPLATE_LIST_OF_LINKS";
	public static final String TEMPLATE_DATA_XML_FILE = "TEMPLATE_DATA_XML_FILE";
	public static final String TEMPLATE_FUNCTION = "TEMPLATE_FUNCTION";
	public static final String TEMPLATE_EXTRACT = "TEMPLATE_EXTRACT";
	public static final String TEMPLATE_ALPHABETIC_ITERATOR = "TEMPLATE_ALPHABETIC_ITERATOR";

	public HTMLTemplateManager()
	{
		templates = new HashMap<String, HTMLTemplate>();
	}

	public void initialize(int engineToLoad) throws Exception
	{
		// Check if already initialized
		if ( !templates.isEmpty() )
		{
			return;
		}
		HTMLTemplate template = null;

		if ( engineToLoad == HTMLRunnerConstants.HTML_ENGINE_XUL )
		{
			template = new XULLoadURLTemplate(TEMPLATE_LOAD_URL);
			templates.put(TEMPLATE_LOAD_URL, template);
			template = new XULInputTemplate(TEMPLATE_INPUT);
			templates.put(TEMPLATE_INPUT, template);
			template = new XULButtonTemplate(TEMPLATE_BUTTON);
			templates.put(TEMPLATE_BUTTON, template);
			template = new XULAnchorTemplate(TEMPLATE_ANCHOR);
			templates.put(TEMPLATE_ANCHOR, template);
			template = new XULSelectTemplate(TEMPLATE_SELECT);
			templates.put(TEMPLATE_SELECT, template);
			template = new XULCheckBoxTemplate(TEMPLATE_CHECKBOX);
			templates.put(TEMPLATE_CHECKBOX, template);
			template = new XULLookupTemplate(TEMPLATE_LOOKUP);
			templates.put(TEMPLATE_LOOKUP, template);
			template = new XULResetLookupTemplate(TEMPLATE_RESET_LOOKUP);
			templates.put(TEMPLATE_RESET_LOOKUP, template);
			template = new XULRadioButtonTemplate(TEMPLATE_RADIOBUTTON);
			templates.put(TEMPLATE_RADIOBUTTON, template);
			template = new XULSpanTemplate(TEMPLATE_SPAN);
			templates.put(TEMPLATE_SPAN, template);
			template = new XULOnChangeTemplate(TEMPLATE_ON_CHANGE);
			templates.put(TEMPLATE_ON_CHANGE, template);
			template = new XULOpenWindowTemplate(TEMPLATE_OPEN_WINDOW);
			templates.put(TEMPLATE_OPEN_WINDOW, template);
			template = new XULCloseWindowTemplate(TEMPLATE_CLOSE_WINDOW);
			templates.put(TEMPLATE_CLOSE_WINDOW, template);
			template = new XULSwitchDOMTemplate(TEMPLATE_SWITCH_DOM);
			templates.put(TEMPLATE_SWITCH_DOM, template);
			template = new XULSwitchBackDOMTemplate(TEMPLATE_SWITCH_BACK_DOM);
			templates.put(TEMPLATE_SWITCH_BACK_DOM, template);
			template = new XULSleepTemplate(TEMPLATE_SLEEP);
			templates.put(TEMPLATE_SLEEP, template);
			template = new XULDOMLoadingTemplate(TEMPLATE_DOM_LOADING);
			templates.put(TEMPLATE_DOM_LOADING, template);
			template = new XULAnchorParentTemplate(TEMPLATE_PARENT_ANCHOR);
			templates.put(TEMPLATE_PARENT_ANCHOR, template);
			template = new XULLookupBlankTemplate(TEMPLATE_LOOKUP_BLANK);
			templates.put(TEMPLATE_LOOKUP_BLANK, template);
			template = new XULNoDOMLoadingTemplate(TEMPLATE_NO_DOM_LOADING);
			templates.put(TEMPLATE_NO_DOM_LOADING, template);
			template = new XULRepeatIfNotFoundTemplate(TEMPLATE_REPEAT_IF_NOT_FOUND);
			templates.put(TEMPLATE_REPEAT_IF_NOT_FOUND, template);
			template = new XULEndRepeatIfNotFoundTemplate(TEMPLATE_END_REPEAT_IF_NOT_FOUND);
			templates.put(TEMPLATE_END_REPEAT_IF_NOT_FOUND, template);
			template = new XULTextAreaTemplate(TEMPLATE_TEXTAREA);
			templates.put(TEMPLATE_TEXTAREA, template);
			template = new XULAreaTemplate(TEMPLATE_AREA);
			templates.put(TEMPLATE_AREA, template);
			template = new XULGetElementTemplate(TEMPLATE_GET_ELEMENT);
			templates.put(TEMPLATE_GET_ELEMENT, template);
			template = new XULErrorTemplate(TEMPLATE_ERROR);
			templates.put(TEMPLATE_ERROR, template);
			template = new XULClickElementTemplate(TEMPLATE_CLICK);
			templates.put(TEMPLATE_CLICK, template);

			template = new XULDataXMLFileTemplate(TEMPLATE_DATA_XML_FILE);
			templates.put(TEMPLATE_DATA_XML_FILE, template);
			template = new XULDefineFieldSet(TEMPLATE_DEFINE_FIELD_SET);
			templates.put(TEMPLATE_DEFINE_FIELD_SET, template);
			template = new XULListOfLinksTemplate(TEMPLATE_LIST_OF_LINKS);
			templates.put(TEMPLATE_LIST_OF_LINKS, template);
			template = new XULFunctionTemplate(TEMPLATE_FUNCTION);
			templates.put(TEMPLATE_FUNCTION, template);
			template = new XULExtractTemplate(TEMPLATE_EXTRACT);
			templates.put(TEMPLATE_EXTRACT, template);
			template = new XULAlphabeticIterator(TEMPLATE_ALPHABETIC_ITERATOR);
			templates.put(TEMPLATE_ALPHABETIC_ITERATOR, template);
			template = new XULInputTemplate(TEMPLATE_INPUT_SUBMIT);
			templates.put(TEMPLATE_INPUT_SUBMIT, template);
		}
		else
			throw new Exception("Unknown HTML templates for the engine.");

	}

	public HTMLTemplate getTemplate(String command) throws Exception
	{
		if ( templates.isEmpty() )
			throw new Exception("The template manager is not iniatilized");

		if ( command == null )
			throw new Exception("Unknown template for the command: '" + command + "'");

		String templateName = this.getTemplateName(command);

		HTMLTemplate template = templates.get(templateName);

		if ( template == null )
			throw new Exception("Unknown template for the command: '" + command + "' and template name: '" + templateName + "'");

		Map<String, String> parameters = this.getTemplateParameters(command);
		template.setParameters(parameters);

		return template;
	}

	private String getTemplateName(String command) throws Exception
	{
		String templateName = null;
		int index = command.indexOf(" ");
		if ( index == -1 )
		{
			templateName = command;
		}
		else
			templateName = command.substring(0, index);

		return templateName;
	}

	private Map<String, String> getTemplateParameters(String command) throws Exception
	{
		if ( command == null )
		{
			throw new Exception("The command is null");
		}

		Map<String, String> res = new HashMap<String, String>();
		int index = command.indexOf(" ");
		if ( index == -1 )
		{
			return res;
		}
		String parameters = command.substring(index + 1);

		while ( true )
		{
			index = parameters.indexOf(HTMLUtils.TEMPLATE_PARAMETERS_SEPARATOR);
			if ( index == -1 )
				break;
			String parameter = parameters.substring(0, index);
			int idx = parameter.indexOf("=");
			if ( idx == -1 )
				throw new Exception("Malformed template parameter name/value pair:  expected an '=' but found none.  " + "Parameter name/value pair (raw) = \"" + parameter + "\"");

			String name = parameter.substring(0, idx);
			String value = parameter.substring(idx + 1);

			res.put(name, value);
			parameters = parameters.substring(index + HTMLUtils.TEMPLATE_PARAMETERS_SEPARATOR.length());
		}
		if ( parameters != null && !parameters.equals("") )
		{
			int idx = parameters.indexOf("=");
			if ( idx == -1 )
			{
				throw new Exception("Invalid parameter for this template: could not locate '='.  parameters = \"" + parameters + "\", command = \"" + command
						+ "\", previously extracted parameters = " + res);
			}
			String name = parameters.substring(0, idx);
			String value = parameters.substring(idx + 1);

			res.put(name, value);
		}
		return res;
	}

}