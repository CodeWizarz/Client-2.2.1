/**************************************************
 * $Revision: 34211 $:
 * $Author: john.snell $:
 * $Date: 2013-06-28 16:55:06 +0700 (Fri, 28 Jun 2013) $:
 * $HeadURL: http://svn01.rapidesuite.com:999/svn/a/IT/VMware/VMwareControlDaemon/current/src/com/erapidsuite/vmcd/VMwareControlDaemon.java $:
 * $Id: VMwareControlDaemon.java 34211 2013-06-28 09:55:06Z john.snell $:
*/


package com.rapidesuite.panopticon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.horizonlive.tightvnc.VncViewer;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.panopticon.Config.ObservedHost;


public class Main
{

    public static void main(String[] args) throws Exception
    {
        if ( args.length != 1 )
        {
            debugPrint("Usage:  java -classpath panopticon.jar com.rapidesuite.panopticon.Main <path-to-panopticon.properties> ");
            System.exit(-1);
        }
        boolean isReloadedConfigFile = false;
        while ( true )
        {
        	Config.reloadConfigFile(new File(args[0]), isReloadedConfigFile);
        	Main.observeHosts();
            Thread.sleep(Config.getDisplayHTMLrefreshIntervalInSeconds() * 1000);
            isReloadedConfigFile = true;
        }
    }


    private static Object logSynchronizationObject = new Object();
    private static PrintWriter logFileWriter = null;
    public static void openNewLog(File pathToLogFileDirectory) throws IOException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        synchronized(logSynchronizationObject)
        {
            if ( null != logFileWriter )
            {
                logFileWriter.close();
            }

            String fileName = "panopticon." + sdf.format(new Date()) + ".log";
            File file = new File(pathToLogFileDirectory, fileName);
            logFileWriter = new PrintWriter(new FileWriter(file));
        }
    }


    public static void debugPrint(String message)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        String logLine = sdf.format(new Date()) + ": " + message;

        System.out.println(logLine);
        System.out.flush();

        synchronized(logSynchronizationObject)
        {
            if ( null != logFileWriter )
            {
                logFileWriter.println(logLine);
                logFileWriter.flush();
            }
        }
    }


    private static Map<String,Boolean> lastErrorStatusForObservedHost = new HashMap<String,Boolean>();

    public static void clearLastErrorStatusForObservedHost()
    {
    	synchronized(lastErrorStatusForObservedHost)
    	{
    		lastErrorStatusForObservedHost.clear();
    	}
    }

    private static String generateMapKeyForLastErrorStatusForObservedHost(String hostName, int port)
    {
    	return hostName + ":" + port;
    }

    public static final void setLastErrorStatusForObservedHost(String hostName,
    														   Integer port,
    														   Boolean success)
    {
    	synchronized(lastErrorStatusForObservedHost)
    	{
    		String key = generateMapKeyForLastErrorStatusForObservedHost(hostName, port);
    		lastErrorStatusForObservedHost.put(key, success);
    	}
    }

    public static final boolean getLastErrorStatusForObservedHost(String hostName,
			   													  Integer port)
    {
    	synchronized(lastErrorStatusForObservedHost)
    	{
    		//System.err.println("lastErrorStatusForObservedHost = " + lastErrorStatusForObservedHost);
    		String key = generateMapKeyForLastErrorStatusForObservedHost(hostName, port);
    		Boolean toReturn = lastErrorStatusForObservedHost.get(key);
    		if ( toReturn == null )
    		{
    			toReturn = false;
    		}
    		return toReturn;
    	}
    }



    public static final void observeHosts() throws Exception
    {
    	StringBuffer sb = new StringBuffer();
    	String errorAllSessionsOffline = String.format("<div style=\"background-color:#F2DEDE;border-color:EBCCD1;color:A94442;border-radius:5px;padding:2px;margin-bottom:5px;\"><ul><li>%s</li></ul></div>", Config.getAllSessionsOfflineMessage());
    	sb.append("<table width=\"990\" border=\"0\"><tr>");
    	boolean isAllSessionsOffline = true;
        final int max = Config.getScreenCaptureCountPerRow();
        List<ObservedHost> observedHosts = Config.getObservedHosts();
        Integer concurrentThreadCount = null;
        try {
        	concurrentThreadCount = Config.getMaxVNCConnectionPool();
//        	Assert.assertTrue("Maximum number of concurrent VNC connection must be greater than zero.", concurrentThreadCount>0);
        	if(!(concurrentThreadCount>0))
        		throw new Error("Maximum number of concurrent VNC connection must be greater than zero.");
        } catch(Exception ex) {
        	concurrentThreadCount = observedHosts.size();
        }
       
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreadCount);
        int counter = 1;
        for ( Config.ObservedHost observedHost : observedHosts)
        {
            String capturedImageFileName = observedHost.getHost().getHostName() + "." + observedHost.getPort() + ".png";
        	String imageFileName = capturedImageFileName;
        	String dateCode = "<!--#Config timefmt=\"%Y.%m.%d - %H:%M:%g\" --><!--#flastmod virtual=\"" + imageFileName + "\" -->";

        	//due to a memory leak in the VNC code, we first check if the host is accessible on the given port
        	boolean isHostPingable = Config.getPingHostBeforeCaptureScreen() ? isHostPingable = isHostPingable(observedHost.getHost()) : true;
        	boolean hasImageToDisplay = getLastErrorStatusForObservedHost(observedHost.getHost().getHostName(), observedHost.getPort());
        	if ( !isHostPingable || !hasImageToDisplay )
        	{
        		imageFileName = "offline.png";
        		dateCode = "offline";
        	}else
        	{
        		isAllSessionsOffline = false;
        	}
        	sb.append("<td><table border=\"0\">");
        	sb.append("<tr><td>");
        	sb.append("<div align=\"center\" style=\"font-weight:bold;\">");
        	sb.append(observedHost.getDisplayName());
        	sb.append("</div>");
        	sb.append("</td></tr>");
        	sb.append("<tr><td align=\"left\">");
        	sb.append("<div style=\"margin-top:5px;\">");
            if(isHostPingable && hasImageToDisplay) {	// if the host is pingable, and there is something to show then only open in new tab/window.
            	sb.append("<a href=\"");
	            if(Config.getUseNoVNCClient()) {	// if useNoVNCClient then append the noVNC link to href.
	            	sb.append("http" + (Config.getNoVNCUseSSLforConnection() ? "s" : "") + "://" + observedHost.getHost().getHostName() + 
	            			(Config.getNoVNCTokenBasedAuth() == false ? ":" + observedHost.getWebsockifyPort() : "/" + Config.getNoVNCTokenBasedAuthURI()) + 
	            			"/" + 
			            (Config.getNoVNCAutoMode() ? "vnc_auto.html" : "vnc.html")  + 	// if noVNCAutoMode then use vnc_auto.html else use vnc.html.
			            (
			            		Config.getNoVNCTokenBasedAuth() 
			            			? 
			            					"?token=" + observedHost.getToken() + 
			            					"&path=" + Config.getNoVNCTokenBasedAuthPath() + 
			            					(Config.getNoVNCAutoPassword() ? "&password=" + observedHost.getPassword() : "" ) // if noVNCAutoPassword then append the password to the href link
			            			: 
			            				""
			            ) // if noVNCTokenBasedAuth enabled append the token and the path
		            );
	            } else {	// else no need for the noVNC link in href.
	            	sb.append(imageFileName);
	            }
	            sb.append("\" target=\"_blank\">");
            }
            sb.append("<img border=\"0\" width=\"" + Config.getScreenCaptureDimensions().getWidth() +  "\" height=\"" + Config.getScreenCaptureDimensions().getHeight() + "\" src=\"");
            sb.append(imageFileName);
            // append a random double number after image name to force browser to get the latest version.
            sb.append("?" + Math.random());
//            no need for javascript auto image reloader now
//            now doing it via jquery load
//            sb.append("\" id=\"reloader-" + observedHost.getHost().getHostName() + "-" + observedHost.getPort() + "\" onload=\"setTimeout('document.getElementById(\\'reloader-" + 
//            		observedHost.getHost().getHostName() + "-" + observedHost.getPort() + "\\').src=\\'" + imageFileName + "?\\'+new Date().getMilliseconds()', 30000)\" />");
            sb.append("\" class=\"img_border\"></a>");
            
            if(isHostPingable && hasImageToDisplay) { // if anchor tag was open, then close it.
            	sb.append("</a>");
            }
            sb.append("</div>");
            sb.append("</td></tr>");
        	sb.append("<tr><td>");
        	sb.append("<span style=\"font-style:normal\">");
        	sb.append(dateCode.equals("offline") ? dateCode : "Updated:" + dateCode);
        	sb.append("</span>");
        	sb.append("</td></tr>");
            sb.append("</table></td>");

            if ( counter++ >= max )
            {
                counter = 1;
                sb.append("</tr><tr>");
            }

            if ( isHostPingable )
            {
            	//avoid trying to capture inaccessible hosts, due to a memory leak in the VNC code
            	//that manifests when connections fail.
                File pathToImageOutputFile = new File(Config.getPathToHTMLOutputDirectory(), capturedImageFileName);
                VncViewer screen = VncViewer.captureVMScreen(observedHost, pathToImageOutputFile, false);
                try {
                	executor.execute(screen);
                } catch(Exception ex) {
                	ex.printStackTrace();
                	Thread.currentThread().interrupt();
                }
            }
        }
        
        //line break to new row
        sb.append("</tr>");
        sb.append("</table>");
        sb.insert(0, isAllSessionsOffline ? errorAllSessionsOffline:"");
        byte[] byteArray = null;        
        FileInputStream fis = null;
        try {
        	File inputFile = new File(Config.getObservedHostsInputFileName());
        	fis = new FileInputStream(inputFile);
        	
        	byteArray = new byte[fis.available()];
        	fis.read(byteArray,0,byteArray.length);
        } 
        finally
        {
        	if ( null != fis )
        	{	fis.close();	}
        }
        
        String mainHtmlContent = new String(byteArray);
    	ClassLoader cl = Main.class.getClassLoader();
    	String applicationVersion = FileUtils.getApplicationVersion(cl,UtilsConstants.APPLICATION_VERSION_FILE_NAME);
        mainHtmlContent = String.format(mainHtmlContent, Config.getDisplayHTMLrefreshIntervalInSeconds(), applicationVersion, sb.toString());

        FileOutputStream fos = null;
        try
        {
        	File outputFile = new File(Config.getPathToHTMLOutputDirectory(),
					 				   Config.getObservedHostsOutputFileName());
        	fos = new FileOutputStream(outputFile);
        	fos.write(mainHtmlContent.getBytes());
        }
        finally
        {
        	if ( null != fos )
        	{
        		fos.close();
        	}
        }

        // dump the table to a file also, for use via AJAX
        FileOutputStream tableFos = null;
        try
        {
        	File outputFile = new File(Config.getPathToHTMLOutputDirectory(),
					 				   Config.getObservedHostsOutputTableFileName());
        	tableFos = new FileOutputStream(outputFile);
        	tableFos.write(sb.toString().getBytes());
        }
        finally
        {
        	if ( null != tableFos )
        	{
        		tableFos.close();
        	}
        }
        executor.shutdown();
        while (!executor.isTerminated()) {}

    }
    
	public static void executeLinuxCommandAsScript(File script,
			String commandString) throws Exception {
		// debugPrint("Generating:  \"" + script + "\"");

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(script);
			fos.write(commandString.getBytes());
		} finally {
			if (null != fos) {
				fos.close();
			}
		}
		script.setExecutable(true);

		// debugPrint("About to execute \"" + script + "\":  \"" + commandString
		// + "\"");
		Process p = Runtime.getRuntime().exec(script.getAbsolutePath());

		p.waitFor();
		p.destroy();

		// debugPrint("Finished execute \"" + script + "\"");
	}

	private static boolean isHostPingable(InetAddress host) throws Exception {
		/*
		[root@vmhost02 ~]# ping -c 1 pc-spare06.rapidesuite.com
		PING pc-spare06.rapidesuite.com (192.188.172.126) 56(84) bytes of data.
		64 bytes from pc-spare06.rapidesuite.com (192.188.172.126): icmp_seq=1 ttl=128 time=0.321 ms

		--- pc-spare06.rapidesuite.com ping statistics ---
		1 packets transmitted, 1 received, 0% packet loss, time 0ms
		rtt min/avg/max/mdev = 0.321/0.321/0.321/0.000 ms
		[root@vmhost02 ~]# ping -c 1 pc-spare06.rapidesuite.co
		ping: unknown host pc-spare06.rapidesuite.co
		[root@vmhost02 ~]# ping -c 1 nb-spare06.rapidesuite.com
		PING nb-spare06.rapidesuite.com (192.188.172.66) 56(84) bytes of data.
		From vmhost02.rapidesuite.com (192.188.172.17) icmp_seq=1 Destination Host Unreachable

		--- nb-spare06.rapidesuite.com ping statistics ---
		1 packets transmitted, 0 received, +1 errors, 100% packet loss, time 0ms

		[root@vmhost02 ~]#
		 */
		File scriptFile = new File("/tmp/ping." + host.getHostName()
				+ ".vmcd.sh");
		File outputFile = new File("/tmp/ping." + host.getHostName()
				+ ".vmcd.txt");

		String commandString = "#!/bin/bash\n" + "/bin/ping -c 1 "
				+ host.getHostName() + " > " + outputFile.getAbsolutePath()
				+ "\n";
		executeLinuxCommandAsScript(scriptFile, commandString);

		boolean toReturn = false;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(outputFile));
			String line = br.readLine();
			while (null != line) {
				if (line.indexOf("1 packets transmitted, 1 received") != -1) {
					toReturn = true;
					break;
				}
				line = br.readLine();
			}
		} finally {
			if (null != br) {
				br.close();
			}
			outputFile.delete();
		}

		debugPrint("isHostPingable: " + host.getHostName() + " = " + toReturn);
		return toReturn;
	}

}


