/*WARNING!!!
 * This class can NOT call any other class nor external library
 * Stick to java 6 standard library (yes, java 6)
 * The reason is that, the swiftbuild is to run on java 7 (so the jar file is compiled for java 7)
 * However, the EBS applet is still for java 6 (and it refuses to run on java 7)
 * This applet class is essentially to test client's readiness to run EBS applet, thus this applet has to run on java 6
 * As such, this class, in addition of being compiled for the jar file, will also be compiled ALONE to make the class file to run the applet at java 6
 * That lone compilation means that this class can not call other class or external library
 * */

package com.rapidesuite.build.utils;

import java.awt.Color;
import java.awt.Label;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.swing.JApplet;

public class JavaVersionRetriever extends JApplet {

	/**
	 *
	 */
	private static final long serialVersionUID = -3870926491422069809L;

	public static final String JAVA_VERSION_URL_PREFIX = "java-version";
	public static final String JAVA_VERSION_STRING_SEPARATOR = " - ";

	private final String javaVersion;
	
	private final static String JAVA_VERSION_KEY = "java.version";
	private final static String OS_ARCH_KEY = "os.arch";
	private final static String JAVA_VENDOR_KEY = "java.vendor";

	private final Label label;
	private static final String[] JVM_INFORMATION_IN_SEQUENCE = {JAVA_VERSION_KEY, OS_ARCH_KEY, JAVA_VENDOR_KEY};
	public JavaVersionRetriever() throws UnsupportedEncodingException //constructor
	{
		String javaVersionLocal = "";
		for (int i = 0 ; i < JVM_INFORMATION_IN_SEQUENCE.length ; i++) {
			if (i > 0) {
				javaVersionLocal += JAVA_VERSION_STRING_SEPARATOR;
			}
			javaVersionLocal += System.getProperty(JVM_INFORMATION_IN_SEQUENCE[i]);
		}
		this.javaVersion = javaVersionLocal;
		Color colFrameBackground = Color.pink;
		this.setBackground(colFrameBackground);
		label = new Label (this.javaVersion);
		this.add(label);
	}

	public static String getOsArchFromJvmInformation(final String jvmInformation) {
		final String jvmInformationChunks[] = jvmInformation.split(JavaVersionRetriever.JAVA_VERSION_STRING_SEPARATOR);
		final int index = Arrays.asList(JVM_INFORMATION_IN_SEQUENCE).indexOf(OS_ARCH_KEY);	
		final String osarch = jvmInformationChunks[index].trim();
		return osarch;
	}

	@Override
	public void start() {
	    try {
	        sendJvmInformation();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(1);
	    }
	}

	private void sendJvmInformation() throws IOException {
		//doing redirect
		this.getAppletContext().showDocument(new URL(this.getCodeBase()+JAVA_VERSION_URL_PREFIX+"/"+URLEncoder.encode(javaVersion, "UTF-8")));
	}
}
