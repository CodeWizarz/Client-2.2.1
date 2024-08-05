package com.rapidesuite.inject.selenium.commands;

import java.net.URL;

import com.erapidsuite.configurator.navigation0005.PostURLType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class PostURLTypeCommand extends Command {

	public PostURLTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(PostURLType postURLType) throws Exception {
		String applicationKey=worker.getBatchInjectionTracker().getScriptGridTracker().getScript().getApplicationKey();
		String urlStr=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getApplicationInfoPanel().getUrl(applicationKey);
		URL url = new URL(urlStr);
		String protocol = url.getProtocol();
		String host = url.getHost();
		int port = url.getPort();
		String portStr="";
		if (port!=-1) {
			portStr=":"+port;
		}
		String newURLStr=protocol+"://"+host+portStr+"/"+postURLType.getSuffix();
		worker.println("new URL: "+newURLStr);
		
		((SeleniumWorker)worker).getWebDriver().get(newURLStr);
		worker.println("GET command sent.");
	}

}
