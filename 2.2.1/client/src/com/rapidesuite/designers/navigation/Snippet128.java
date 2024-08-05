package com.rapidesuite.designers.navigation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class Snippet128 {
	
	public static void main(String [] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		shell.setLayout(gridLayout);
		ToolBar toolbar = new ToolBar(shell, SWT.NONE);
		ToolItem itemBack = new ToolItem(toolbar, SWT.PUSH);
		itemBack.setText("Back");
		ToolItem itemForward = new ToolItem(toolbar, SWT.PUSH);
		itemForward.setText("Forward");
		ToolItem itemStop = new ToolItem(toolbar, SWT.PUSH);
		itemStop.setText("Stop");
		ToolItem itemRefresh = new ToolItem(toolbar, SWT.PUSH);
		itemRefresh.setText("Refresh");
		ToolItem itemGo = new ToolItem(toolbar, SWT.PUSH);
		itemGo.setText("Go");

		GridData data = new GridData();
		data.horizontalSpan = 3;
		toolbar.setLayoutData(data);

		Label labelAddress = new Label(shell, SWT.NONE);
		labelAddress.setText("Address");

		final Text location = new Text(shell, SWT.BORDER);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		location.setLayoutData(data);

		final Browser browser;
		try {
			browser = new Browser(shell, SWT.MOZILLA);//SWT.NONE);
		} catch (SWTError e) {
			System.out.println("Could not instantiate Browser: " + e.getMessage());
			display.dispose();
			return;
		}
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		browser.setLayoutData(data);

		final Label status = new Label(shell, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		status.setLayoutData(data);

		final ProgressBar progressBar = new ProgressBar(shell, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.END;
		progressBar.setLayoutData(data);

		 
		 Listener listener = new Listener() {
			
				@Override
				public void handleEvent(Event event) {
					ToolItem item = (ToolItem)event.widget;
					String string = item.getText();
					if (string.equals("Back")) browser.back();
					else if (string.equals("Forward")) browser.forward();
					else if (string.equals("Stop")) browser.stop();
					else if (string.equals("Refresh")) browser.refresh();
					else if (string.equals("Go")) browser.setUrl(location.getText());
				}
			
			};
				
		browser.addProgressListener(new ProgressListener() {
			@Override
			public void changed(ProgressEvent event) {
					if (event.total == 0) return;
					int ratio = event.current * 100 / event.total;
					progressBar.setSelection(ratio);
			}
			@Override
			public void completed(ProgressEvent event) {
				progressBar.setSelection(0);
				System.out.println("PAGE LOADED!!!!!");
				boolean result = browser.execute("alert('navigator.appVersion:'+navigator.appVersion+' navigator.userAgent:'+navigator.userAgent)");
				if (!result) {
					/* Script may fail or may not be supported on certain platforms. */
					System.out.println("Script was not executed.");
				}
			}
		});
		
		browser.addStatusTextListener(new StatusTextListener() {

			@Override
			public void changed(StatusTextEvent event) {
				status.setText(event.text);
			}
			
		});
		browser.addLocationListener(new LocationListener() {
			@Override
			public void changed(LocationEvent event) {
				if (event.top) location.setText(event.location);
			}
			@Override
			public void changing(LocationEvent event) {
			}
		});
		itemBack.addListener(SWT.Selection, listener);
		itemForward.addListener(SWT.Selection, listener);
		itemStop.addListener(SWT.Selection, listener);
		itemRefresh.addListener(SWT.Selection, listener);
		itemGo.addListener(SWT.Selection, listener);
		location.addListener(SWT.DefaultSelection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				browser.setUrl(location.getText());
			}
			
		});		   

		shell.open();
		//browser.setUrl("http://www.browserproperties.com");
		//browser.setUrl("https://fusion01.rapidesuite.com:18614/homePage");
		browser.setUrl("https://www.whatismybrowser.com/");
		
		/*
		 * TO REMOVE THE CERTIFICATE ERROR:
		 * COPY C:\Users\olivier.deruelle\AppData\Roaming\Mozilla\Firefox\Profiles\z4elf38w.default\cert_override.txt
		 * TO C:\Users\olivier.deruelle\AppData\Roaming\Mozilla\eclipse
		 */
		
		boolean result = browser.execute(
				"document.loginForm.userid.value = \"FAAdmin\";"+
				"document.loginForm.password.value = \"Oracle123\";"+
				"document.loginForm.action =\"/oam/server/auth_cred_submit\";"+
				"document.loginForm.submit();");
		if (!result) {
			/* Script may fail or may not be supported on certain platforms. */
			System.out.println("Script was not executed.");
		}

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	 @SuppressWarnings("unused")
	public static void main1(String[] args) {
	        Device.DEBUG = true;
	        Display display = new Display();
	        Shell shell = new Shell(display);
	        System.out.println(">>>Snippet creating SWT.MOZILLA-style Browser");
	        try {
	            new Browser(shell, SWT.MOZILLA);
	            System.out.println(">>>succeeded");
	        } catch (Error e) {
	            System.out.println(">>>This failed with the following error:");
	            e.printStackTrace();
	            System.out.println("\n\nSnippet creating SWT.NONE-style Browser");
	            try {
	                new Browser(shell, SWT.NONE);
	                System.out.println(">>>succeeded");
	            } catch (Error e2) {
	                System.out.println(">>>This failed too, with the following error:");
	                e2.printStackTrace();
	            }
	        }
	        display.dispose();
	    }
	
}