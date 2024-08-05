package com.rapidesuite.build.core.htmlplayback.engines;

import java.util.List;

import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplateManager;

public interface HTMLEngine
{

	public void initialize(HTMLTemplateManager templateManager) throws Exception;

	public void processILCommands(List<String> ilCommands) throws Exception;

	public void finalize() throws Exception;
}