@echo off

set WORKDIR=%1

set CLASSPATH=
set CLASSPATH=%CLASSPATH%;%WORKDIR%rapidclient.jar

set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/bcpkix-jdk15on-147.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/bcprov-ext-jdk15on-147.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/bcprov-jdk15on-147.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/openswing-clientos-2.4.7-customized.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/openswing-commonos-2.4.7.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/commons-fileupload-1.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/commons-io-2.4.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/commons-net-3.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/javassist.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/javax.servlet-3.0.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/jetty-all-8.1.9.v20130131.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/jna-3.5.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/ojdbc6_11.2.0.2.0_JDK1.6.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/opencsv-2.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/platform-3.5.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/poi-3.10-FINAL-20140208.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/poi-ooxml-3.10-FINAL-20140208.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/poi-ooxml-schemas-3.10-FINAL-20140208.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/sevenzipjbinding-AllWindows.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/sevenzipjbinding.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/slf4j-api-1.7.6.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/spring-core-3.2.3.RELEASE.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/sshj-0.9.1-SNAPSHOT.RES01.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/configurator.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/swingx-all-1.6.4.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/xbean.jar
@REM set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/xercesImpl-2.6.2.jar -- THIS CREATES A DEPENDENCY ISSUE WITH ADF WEB SERVICES (com.oracle.webservices.fmw.client_12.1.3.jar)!!!
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/commons-codec-1.6.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/jnlp.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/dom4j-1.6.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/rapidcore.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/slf4j-log4j12-1.7.6.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/log4j-1.2.17.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/commons-lang3-3.3.2.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/glazedlists_java15-1.9.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/balloontip-1.2.4.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/httpclient-4.3.6.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/httpcore-4.3.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/httpmime-4.3.6.jar

set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/DJNativeSwing-SWT.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/DJNativeSwing.jar
@REM set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/swt.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/rsyntaxtextarea-2.6.0.jar
@REM set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/org.eclipse.swt.win32.win32.x86_64_3.105.1.v20160907-0248.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/org.eclipse.swt.win32.win32.x86_3.105.1.v20160907-0248.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/jsoup-1.9.2.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/dom4j-1.6.1.jar

echo on

@REM .\jre64\bin\java -Xmx1024m com.rapidesuite.inject.TestJWebBrowser
@REM jre64\bin\java -Xmx1024m com.rapidesuite.designers.navigation.NavigationEditorMain

@REM -Dorg.eclipse.swt.browser.XULRunnerPath=D:\xulrunner\xulrunner-24.0.en-US.win32\xulrunner
@REM D:\xulrunner\xulrunner-3.6.28.en-US.win32\xulrunner
@REM .\jre32\bin\java -Xmx1024m -Dorg.eclipse.swt.browser.XULRunnerPath=D:\xulrunner\xulrunner-24.0.en-US.win32\xulrunner com.rapidesuite.designers.navigation.Snippet128
.\jre32\bin\java -Xmx1024m com.rapidesuite.designers.navigation.NavigationEditorMain
@REM .\jre32\bin\java -Xmx1024m -Dorg.eclipse.swt.browser.XULRunnerPath=D:\xulrunner\xulrunner-31.0.en-US.win32\xulrunner com.rapidesuite.designers.navigation.Snippet128

@REM .\jre64\bin\java -Xmx1024m com.rapidesuite.inject.TextEditorDemo

PAUSE
