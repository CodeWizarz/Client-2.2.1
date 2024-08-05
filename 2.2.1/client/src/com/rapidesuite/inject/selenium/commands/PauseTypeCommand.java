package com.rapidesuite.inject.selenium.commands;

import com.erapidsuite.configurator.navigation0005.PauseType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class PauseTypeCommand extends Command{

	public PauseTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(PauseType pauseType) throws Exception {
		int pauseInSecs=pauseType.getDurationInSecs().intValue();
		int loginRedirectPause=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getLoginRedirectPause();
		int pauseTemp=-1;
		boolean isLoginRedirectPause=pauseType.getIsLoginRedirectPause();
		if (isLoginRedirectPause) {
			pauseTemp=loginRedirectPause;
		}
		else {
			pauseTemp=pauseInSecs;
		}
		String msg="Pausing for: "+pauseTemp+" secs...";
		worker.println(msg);
		Thread.sleep(pauseTemp*1000);
	}
	
}
