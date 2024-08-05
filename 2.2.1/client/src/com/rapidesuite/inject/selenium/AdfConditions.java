package com.rapidesuite.inject.selenium;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
public abstract class AdfConditions {
	
	public static final long DFLT_WAIT_TIMEOUT_SECS = 30;
	public static final long DFLT_WAIT_INTERVAL_MSECS = 500;
	
	private AdfConditions() {
	}
	
	public static ExpectedCondition<Boolean> clientSynchedWithServer() {
		// return false if AdfPage object and functions do not exist
		// if they do exist return true if page is fully loaded and ready or reason why this is not completed yet
		return javascriptExpressionTrue("typeof AdfPage !== 'undefined' && " +
				"typeof AdfPage.PAGE !== 'undefined' && " +
				"typeof AdfPage.PAGE.isSynchronizedWithServer === 'function' && " +
				"(AdfPage.PAGE.isSynchronizedWithServer() || " +
				"(typeof AdfPage.PAGE.whyIsNotSynchronizedWithServer === 'function' && " +
				"AdfPage.PAGE.whyIsNotSynchronizedWithServer()))");
	}
	
	public static ExpectedCondition<Boolean> javascriptExpressionTrue(final String jsExpression, final Object... args) {
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				JavascriptExecutor jsDriver = (JavascriptExecutor) driver;
				System.out.println("executing condition javascript: {0} : "+jsExpression);
				Object result = jsDriver.executeScript(jsExpression, args);
				System.out.println("js result: {0} : "+result);
				return Boolean.TRUE.equals(result);
			}
		};
	}
}