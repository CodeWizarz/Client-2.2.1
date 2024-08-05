/**************************************************
 * $Revision: 34211 $:
 * $Author: john.snell $:
 * $Date: 2013-06-28 16:55:06 +0700 (Fri, 28 Jun 2013) $:
 * $HeadURL: http://svn01.rapidesuite.com:999/svn/a/IT/VMware/Main/current/src/com/erapidsuite/vmcd/Config.java $:
 * $Id: Config.java 34211 2013-06-28 09:55:06Z john.snell $:
*/


package com.rapidesuite.panopticon;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


public class Config
{
	private static final Properties configProperties;

	static {
		configProperties = new Properties();
	}
	public static void reloadConfigFile(File file, boolean isReloaded) throws Exception {
		InputStream is = null;
		try {
            is = new FileInputStream(file);
            configProperties.load(is);
            if(isReloaded) {
            	Main.debugPrint("reloadFromFile() = " + getPathToConfigFile());
            }
            init(configProperties,isReloaded);
            pathToConfigFile = file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
	}
	
    public Properties getConfigProperties()
    {
        return (Properties) configProperties.clone();
    }

    private static File pathToConfigFile = null;
    public static File getPathToConfigFile()
    {
        return Config.pathToConfigFile;
    }

    public void reloadFromFile() throws Exception
    {
        Main.debugPrint("reloadFromFile() = " + this.getPathToConfigFile());
        init(parseConfigFile(this.getPathToConfigFile()),true);
    }



    private static void init(Properties configProperties, boolean isReloaded) throws Exception
    {


        Main.debugPrint("config.init:configProperties = " + configProperties);

        //validation
        if ( getDisplayHTMLrefreshIntervalInSeconds() < 1 )
        {
            throw new IllegalArgumentException("getDisplayHTMLrefreshIntervalInSeconds() must be >= 1");
        }

        if ( getScreenCaptureCountPerRow() <= 0 )
        {
            throw new IllegalArgumentException("screenCaptureCountPerRow must be >= 0.  Current value = " +
                                               getScreenCaptureCountPerRow());
        }
        getScreenCaptureDimensions();

        if(!isReloaded){
        	Main.openNewLog(getPathToLogFileDirectory());
        }

        reloadObservedHostsProperties();
    }

    public static File getPathToLogFileDirectory()
    {
        return new File(".");
    }



    public static Integer getScreenCaptureCountPerRow()
    {
        return new Integer(configProperties.getProperty("screenCaptureCountPerRow"));
    }

    public static Integer getMaxVNCConnectionPool() {
    	  return new Integer(configProperties.getProperty("vncConnectionPoolMax").trim());
    }
    
    public static Dimension getScreenCaptureDimensions()
    {
        String rawDimensions =  configProperties.getProperty("screenCaptureDimensions");
        StringTokenizer st = new StringTokenizer(rawDimensions, "x");
        try
        {
            Dimension toReturn = new Dimension(new Integer(st.nextToken()), new Integer(st.nextToken()));
            return toReturn;
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException("Error parsing Dimension string for screenCaptureDimensions." +
                                               "  Format must by WxH, where W and H are nonnegative integers.  " +
                                               "Input string was: " + rawDimensions, e);
        }
    }



    public static File getPathToHTMLOutputDirectory()
    {
        return new File("html");
    }



    private static Boolean parseAndValidateBooleanValue(String propertyName, String strBoolean)
    {
        if ( null == strBoolean )
        {
            throw new IllegalArgumentException("propertyName = " + propertyName + " may not be null.");
        }
        //new Boolean(str) doesn't correctly validate that the value is "true" or "false".  Do so manually.
        if ( strBoolean.equalsIgnoreCase("true") )
        {
            return true;
        }
        else if ( strBoolean.equalsIgnoreCase("false") )
        {
            return false;
        }
        else
        {
            throw new IllegalArgumentException(propertyName + " = \"" +
                                               strBoolean +
                                               "\" is not a valid boolean value.");
        }
    }



    private static Properties parseConfigFile(File pathToConfigFile) throws IOException
    {
        if ( !pathToConfigFile.exists() )
        {
            throw new IllegalArgumentException("pathToConfigFile = \"" + pathToConfigFile + "\" does not exist.");
        }

        Properties toReturn = new Properties();
        toReturn.load(new FileInputStream(pathToConfigFile));
        return toReturn;
    }

    public static class ObservedHost
    {
    	private InetAddress host = null;
    	public InetAddress getHost()
    	{
    		return this.host;
    	}
    	private Integer port = null;
    	public Integer getPort()
    	{
    		return this.port;
    	}
    	private String password = null;
    	public String getPassword()
    	{
    		return this.password;
    	}
    	private String displayName = null;
    	public String getDisplayName()
    	{
    		return this.displayName;
    	}
    	private Integer websockifyPort = null;
    	public Integer getWebsockifyPort()
    	{
    		return this.websockifyPort;
    	}
    	private String token = null;
    	public String getToken() {
    		return this.token;
    	}
    	
    	public ObservedHost(String displayName, String hostName, Integer port, String password, Integer websockifyPort, String token)
    	throws Exception
    	{
    		this.displayName = displayName;
    		this.host = InetAddress.getByName(hostName);
    		this.port = port;
    		this.password = password;
    		this.websockifyPort = websockifyPort;
    		this.token = token;
    	}

    	public String toString()
    	{
    		return this.getDisplayName() + ":" + this.getHost().toString() + ":" + this.getPort() + ":" + this.getPassword() + ":" + this.getWebsockifyPort() + ":" + this.getToken();
    	}
    }

    private static List<ObservedHost> observedHosts = new ArrayList<ObservedHost>();

    public static List<ObservedHost> getObservedHosts()
    {
    	synchronized(observedHosts)
    	{
    		return Collections.unmodifiableList(observedHosts);
    	}
    }

    public static File getPathToObservedHostsProperties()
    {
    	return new File(configProperties.getProperty("pathToObservedHostsFile"));
    }

    public static void reloadObservedHostsProperties()
    throws Exception
    {
    	List<String> lines = new ArrayList<String>();
    	BufferedReader br = null;
    	try
    	{
    		br = new BufferedReader(new FileReader(getPathToObservedHostsProperties()));
    		String line = br.readLine();
    		while ( line != null )
    		{
    			if ( line.trim().length() > 0 && !line.trim().startsWith("#") )
    			{
        			lines.add(line);
    			}
    			line = br.readLine();
    		}
    	}
    	finally
    	{
    		if ( null != br )
    		{
    			br.close();
    		}
    	}

    	synchronized(observedHosts)
    	{
			observedHosts.clear();
	    	for ( String line : lines )
	    	{
	    		StringTokenizer st = new StringTokenizer(line, "=");
	    		String displayName = st.nextToken();
	    		String valueLine = st.nextToken();
	    		st = new StringTokenizer(valueLine, ",");
	    		String hostName = st.nextToken();
	    		String strPort = st.nextToken();
	    		String password = st.nextToken();
	    		String strWebsockifyPort = st.nextToken();
	    		String token = st.nextToken();
	    		Integer port = new Integer(strPort);
	    		Integer websockifyPort = new Integer(strWebsockifyPort);
	    		observedHosts.add(new ObservedHost(displayName,hostName,port,password,websockifyPort,token));
	    	}
    	}
    	System.err.println("observedHosts = " + observedHosts);
    }


    public static String getObservedHostsOutputFileName()
    {
    	return configProperties.getProperty("observedHostsOutputFileName");
    }

    public static String getObservedHostsOutputTableFileName()
    {
    	return configProperties.getProperty("observedHostsOutputTableFileName");
    }
    
    public static String getObservedHostsInputFileName()
    {
    	return configProperties.getProperty("observedHostsInputFileName");
    }
    
    public static Integer getDisplayHTMLrefreshIntervalInSeconds()
    {
        return new Integer(configProperties.getProperty("displayHTMLrefreshIntervalInSeconds"));
    }
    
    public static boolean getNoVNCAutoPassword() {
    	return parseAndValidateBooleanValue("noVNCAutoPassword",
                configProperties.getProperty("noVNCAutoPassword"));
    }
    
    public static boolean getNoVNCAutoMode() {
    	return parseAndValidateBooleanValue("noVNCAutoMode",
                configProperties.getProperty("noVNCAutoMode"));
    }
    
    public static boolean getUseNoVNCClient() {
    	return parseAndValidateBooleanValue("useNoVNCClient",
                configProperties.getProperty("useNoVNCClient"));
    }
    
    public static boolean getPingHostBeforeCaptureScreen() {
    	return parseAndValidateBooleanValue("pingHostBeforeCaptureScreen",
                configProperties.getProperty("pingHostBeforeCaptureScreen"));
    }
    
    public static boolean getNoVNCUseSSLforConnection() {
    	return parseAndValidateBooleanValue("noVNCUseSSLforConnection",
                configProperties.getProperty("noVNCUseSSLforConnection"));
    }
    
    public static boolean getNoVNCTokenBasedAuth() {
    	return parseAndValidateBooleanValue("noVNCTokenBasedAuth",
                configProperties.getProperty("noVNCTokenBasedAuth"));
    }
    
    public static String getNoVNCTokenBasedAuthPath()
    {
    	return configProperties.getProperty("noVNCTokenBasedAuthPath");
    }
    
    public static String getNoVNCTokenBasedAuthURI()
    {
    	return configProperties.getProperty("noVNCTokenBasedAuthURI");
    }
    
    public static String getAllSessionsOfflineMessage()
    {
    	return configProperties.getProperty("allSessionsOfflineMessage");
    }

}