package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.WebElement;

import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class IsEndOfScrollTypeCommand extends Command {

	public IsEndOfScrollTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public boolean process() throws Exception {
		WebElement webElement=((SeleniumWorker)worker).getCurrentWebElement();
		return processGeneric(((SeleniumWorker)worker),webElement);
	}
	
	public static boolean processGeneric(SeleniumWorker seleniumWorker,WebElement webElement) throws Exception {
		if (webElement==null) {
			throw new Exception("You cannot use 'IsEndOfScroll' tag if no webelement selected (use 'Find' to select the scrollbar).");
		}
		
		long offsetHeight=SeleniumUtils.getIntAttribute(seleniumWorker.getWebDriver(),webElement,"offsetHeight");
		long scrollTop=SeleniumUtils.getIntAttribute(seleniumWorker.getWebDriver(),webElement,"scrollTop");
		long scrollHeight=SeleniumUtils.getIntAttribute(seleniumWorker.getWebDriver(),webElement,"scrollHeight");
		
		seleniumWorker.println("Analysing scrollbar attributes, offsetHeight: "+offsetHeight+" scrollTop: "+scrollTop+" scrollHeight: "+scrollHeight);
		
		if (offsetHeight+scrollTop>=scrollHeight) {
			int cnt1=0;
			long currentScrollHeight=scrollHeight;
			while (cnt1<10) {
				cnt1++;
				scrollHeight=SeleniumUtils.getIntAttribute(seleniumWorker.getWebDriver(),webElement,"scrollHeight");
				if (currentScrollHeight != scrollHeight) {
					seleniumWorker.println("scrollHeight updated, so exiting inner loop, offsetHeight: "+offsetHeight+" scrollTop: "+scrollTop+" scrollHeight: "+scrollHeight);
					break;
				}
				seleniumWorker.println("scrollHeight not yet updated by Firefox so retrying,  retry count: "+cnt1+" offsetHeight: "+offsetHeight+" scrollTop: "+scrollTop+" scrollHeight: "+scrollHeight);
			}
		}

		if (offsetHeight+scrollTop>=scrollHeight) {
			seleniumWorker.println("End of scrollbar detected");
			return true;
		}
		seleniumWorker.println("Not yet at the end of the scrollbar.");
		return false;
	}
	
}
