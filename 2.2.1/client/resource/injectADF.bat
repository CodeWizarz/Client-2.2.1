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

set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/selenium-java-2.45.0.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/apache-mime4j-0.6.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/bsh-1.3.0.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/cglib-nodep-2.1_3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/commons-codec-1.9.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/commons-collections-3.2.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/commons-exec-1.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/commons-io-2.4.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/commons-jxpath-1.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/commons-lang3-3.3.2.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/commons-logging-1.1.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/cssparser-0.9.14.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/gson-2.3.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/guava-18.0.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/hamcrest-core-1.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/hamcrest-library-1.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/htmlunit-2.15.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/htmlunit-core-js-2.15.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/httpclient-4.3.6.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/httpcore-4.3.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/httpmime-4.3.6.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/ini4j-0.5.2.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/jcommander-1.29.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/jetty-websocket-8.1.8.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/jna-3.4.0.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/jna-platform-3.4.0.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/junit-dep-4.11.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/nekohtml-1.9.21.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/netty-3.5.7.Final.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/operadriver-1.5.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/phantomjsdriver-1.2.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/protobuf-java-2.4.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/sac-1.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/serializer-2.7.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/testng-6.8.5.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/xalan-2.7.1.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/xercesImpl-2.11.0.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-2.45.0/libs/xml-apis-1.4.01.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/selenium-server-standalone-2.46.0.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/org.json-20120521.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/adf/com.oracle.webservices.fmw.client_12.1.3.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/adf/adfbcsvc-share.jar
set CLASSPATH=%CLASSPATH%;%WORKDIR%lib/adf/fusion/*

@echo on

.\jre64\bin\java -Xmx1024m com.rapidesuite.inject.webservices.TestWebServices

PAUSE
