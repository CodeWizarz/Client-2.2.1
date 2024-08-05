package com.rapidesuite.build.core.htmlplayback.engines.xul;

import java.io.File;

import com.rapidesuite.build.gui.frames.XULParserFrame;
import com.rapidesuite.build.utils.CurrentBrowserTask;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.build.utils.TaskListUtils;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.GUIUtils;

public class XULParser implements Runnable
{

	public static final String FIREFOX_PARSER_TITLE = "XUL HTML parser";
	private static final String XUL_FILE_NAME = "static-page-parser.xul";
	private static final String SCRIPT_FILE_NAME = "rsc-script.js";

	private File selectedFile;
	private XULParserFrame frame;

	public XULParser(XULParserFrame frame, File selectedFile)
	{
		this.selectedFile = selectedFile;
		this.frame = frame;
	}

	public void run()
	{
		try
		{
			startParser();
			frame.stop();
			GUIUtils.showSuccessMessage(frame.getMessageLabel(), "Completed. Please check the current folder for the output CSV file.");
		}
		catch ( Exception ex )
		{
			frame.stop();
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

	public void startParser() throws Exception
	{
		if ( selectedFile == null || !selectedFile.exists() )
		{
			throw new Exception("You must select a HTML file.");
		}
		GUIUtils.showSuccessMessage(frame.getMessageLabel(), "Validating Firefox...");
		if ( CurrentBrowserTask.isActive())
		{
			throw new Exception("Firefox is already running. Please close it and try again.");
		}
		GUIUtils.showSuccessMessage(frame.getMessageLabel(), "Creating init files...");

		boolean isCloseBrowser = true;
		isCloseBrowser = Config.getBuildForceCloseBrowser();
		createParserFile(isCloseBrowser);
		createStartupXULFile();

		File xulCompletedFile = new File("xul", "completed.txt");
		xulCompletedFile.delete();

		String xulFileURL = new File("xul", XUL_FILE_NAME).toURI().toString();
		GUIUtils.showSuccessMessage(frame.getMessageLabel(), "Starting Firefox...");
		TaskListUtils.startFirefox(null, xulFileURL);
		GUIUtils.showSuccessMessage(frame.getMessageLabel(), "Waiting for Parser to complete...");
		
		final TaskListUtils.IlInjectionSpecifics ilInjectionSpecifics = new TaskListUtils.IlInjectionSpecifics(xulCompletedFile, null, null, null, null);
		
		TaskListUtils.waitForScriptToComplete(null,
				Config.getBuildHtmlFormCloseMaxIteration(),
				null,
				null,
				ilInjectionSpecifics,
				null);			
		
		GUIUtils.showSuccessMessage(frame.getMessageLabel(), "Waiting for Firefox to close...");
		TaskListUtils.waitingForCurrentBrowserTaskToClose(null);
	}

	public void createParserFile(boolean isCloseBrowser) throws Exception
	{
		StringBuffer res = new StringBuffer("");

		res.append("try {\n");
		res.append("	netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');\n");

		File outputKBFile = new File(selectedFile.getParentFile(), "outputKBFile.csv");
		outputKBFile.delete();
		String outputKBFileFullPath = getLocalFilePathForFirefox(outputKBFile);
		res.append("	outputKBFile = '" + outputKBFileFullPath + "';\n");

		File outputControlFile = new File(selectedFile.getParentFile(), "outputControlFile.csv");
		outputControlFile.delete();
		String outputControlFileFullPath = getLocalFilePathForFirefox(outputControlFile);
		res.append("	outputControlFile = '" + outputControlFileFullPath + "';\n");

		String url = selectedFile.getAbsolutePath().replaceAll("\\\\", "/");
		res.append("	rscBrowser.contentDocument.location.href = 'file:///" + url + "';\n\n");

		res.append("	" + XULEngine.STEPS_LIST_VAR_DEFAULT + " = \"ensureDOMisLoaded();\";\n");
		res.append("	" + XULEngine.STEPS_LIST_VAR_DEFAULT + " = \"parseDocument();\";\n");

		String completedFileURL = getLocalFilePathForFirefox(new File("xul", "completed.txt"));
		res.append("	" + XULEngine.STEPS_LIST_VAR_DEFAULT + " = \"scriptCompleted('" + completedFileURL + "');\";\n");

		res.append("	forceCloseWindows=" + isCloseBrowser + ";\n");
		res.append("	if (forceCloseWindows) {\n");
		res.append("		" + XULEngine.STEPS_LIST_VAR_DEFAULT + " = \"closeWindows();\"; \n");
		res.append("	}\n");
		res.append("	execute();\n");

		// removed to Firefox 16.0.2 testing.
//		res.append("	netscape.security.PrivilegeManager.disablePrivilege('UniversalXPConnect');\n");
		res.append("}\n");
		res.append("catch (e) {\n");
		res.append("    alert(\"ERROR: Error name: \" + e.name+ \". Error message: \" + e.message);\n");
		res.append("}\n");

		SwiftBuildFileUtils.saveToFile(new File("xul", SCRIPT_FILE_NAME), res.toString(), false);
	}

	public String getLocalFilePathForFirefox(File file) throws Exception
	{
		String temp = file.getAbsolutePath();
		return temp;
	}

	public static void createStartupXULFile() throws Exception
	{
		String str = 
		"<?xml version='1.0' ?>\n" +
		"<?xml-stylesheet href='chrome://global/skin/' type='text/css'?>\n" +
		"<window\n" +
		"    id='mywindow'\n" +
		"    title='" + FIREFOX_PARSER_TITLE + "'\n" +
		"    orient='horizontal'\n" +
		"   xmlns='http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul'>\n" +
		"\n" +
		"<grid flex='1'>\n" +
		"  <columns>\n" +
		"    <column flex='1'/>\n" +
		"    <column/>\n" +
		"  </columns>\n" +
		"  <rows>\n" +
		"    <row>\n" +
		"        <box height='450'>\n" +
		"                <textbox id='blog' flex='1'  multiline='true' readonly='readonly'/>\n" +
		"        </box>\n" +
		"    </row>\n" +
		"    <row>\n" +
		"      <box width='100'>\n" +
		"        <button label='Manual Stop'\n" +
		"          oncommand='stopInjection( +'/>\n" +
		"      </box>\n" +
		"    </row>\n" +
		"  </rows>\n" +
		"</grid>\n" +
		"\n" +
		"<script src='rsc.js'/>\n" +
		"<script src='rsc-parser.js' />\n" +
		"<script src='" + SCRIPT_FILE_NAME + "'/>\n" +
		"\n" +
		"</window>\n";

		SwiftBuildFileUtils.saveToFile(new File("xul", XUL_FILE_NAME), str, false);
	}

}
