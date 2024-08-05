package com.rapidesuite.inject.selenium.commands;

import java.util.Set;

import com.erapidsuite.configurator.navigation0005.WindowType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class WindowTypeCommand extends Command{

	public WindowTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(WindowType windowType) {
		com.erapidsuite.configurator.navigation0005.WindowType.Action.Enum action=windowType.getAction();
		
		if (action==WindowType.Action.OPEN) {
			String currentWindowHandle = ((SeleniumWorker)worker).getWebDriver().getWindowHandle();
			((SeleniumWorker)worker).getWindowHandlesStack().push(currentWindowHandle);
			Set<String> windowHandles =  ((SeleniumWorker)worker).getWebDriver().getWindowHandles();
			worker.println("WINDOW open action. Current window handle: '"+currentWindowHandle+"' . Total windows handles: "+windowHandles.size());
			for (String windowHandle : windowHandles) {
				worker.println("Found window handle: '"+windowHandle+"'");
				if (((SeleniumWorker)worker).getWindowHandlesStack().search(windowHandle)==-1) {
					worker.println("This is a new window so switching to it!");
					((SeleniumWorker)worker).getWebDriver().switchTo().window(windowHandle);
					break;
				}
			}
		}
		else {
			if (((SeleniumWorker)worker).getWindowHandlesStack().isEmpty()) {
				return;
			}
			String windowHandle=((SeleniumWorker)worker).getWindowHandlesStack().pop();
			worker.println("WINDOW close action. Closing current window handle: '"+((SeleniumWorker)worker).getWebDriver().getWindowHandle()+"'");
			((SeleniumWorker)worker).getWebDriver().close(); //closing child window
			worker.println("Switching to last window handle: '"+windowHandle+"'");
			((SeleniumWorker)worker).getWebDriver().switchTo().window(windowHandle); //cntrl to parent window
		}
	}
	
}