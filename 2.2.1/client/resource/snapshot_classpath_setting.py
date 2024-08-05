#!/usr/bin/python

#jars which are used in ALL supported platforms
jars = ["rapidclient.jar", 
             "lib/bcpkix-jdk15on-147.jar",
             "lib/bcprov-ext-jdk15on-147.jar",
             "lib/bcprov-jdk15on-147.jar",
             "lib/openswing-clientos-2.4.7-customized.jar",
             "lib/openswing-commonos-2.4.7.jar",
			 "lib/org.json-20120521.jar",
             "lib/commons-fileupload-1.3.jar",
             "lib/commons-io-2.4.jar",
             "lib/commons-net-3.3.jar",
             "lib/javassist.jar",
             "lib/javax.servlet-3.0.jar",
             "lib/jetty-all-8.1.9.v20130131.jar",
             "lib/jna-3.5.1.jar",
             "lib/ojdbc6_11.2.0.2.0_JDK1.6.jar",
             "lib/opencsv-2.3.jar",
             "lib/platform-3.5.1.jar",
             "lib/poi-3.10-FINAL-20140208.jar",
             "lib/poi-ooxml-3.10-FINAL-20140208.jar",
             "lib/poi-ooxml-schemas-3.10-FINAL-20140208.jar",
             "lib/sevenzipjbinding.jar",
             "lib/slf4j-api-1.7.6.jar",
             "lib/spring-core-3.2.3.RELEASE.jar",
             "lib/sshj-0.9.1-SNAPSHOT.RES01.jar",
             "lib/configurator.jar",
             "lib/swingx-all-1.6.4.jar",
             "lib/xbean.jar",
             "lib/commons-codec-1.6.jar",
             #"lib/xercesImpl-2.6.2.jar",  -- THIS CREATES A DEPENDENCY ISSUE WITH ADF WEB SERVICES (com.oracle.webservices.fmw.client_12.1.3.jar)!!!
             "lib/jnlp.jar",             
             "lib/dom4j-1.6.1.jar",
			 "lib/rapidcore.jar",
             "lib/slf4j-log4j12-1.7.6.jar",
             "lib/log4j-1.2.17.jar",
             "lib/selenium-2.45.0/libs/commons-lang3-3.3.2.jar",
             "lib/selenium-2.45.0/libs/httpclient-4.3.6.jar",
             "lib/selenium-2.45.0/libs/httpcore-4.3.3.jar",
             "lib/selenium-2.45.0/libs/httpmime-4.3.6.jar",
			 "lib/selenium-2.45.0/libs/commons-logging-1.1.3.jar",
			 "lib/glazedlists_java15-1.9.1.jar",
			 "lib/balloontip-1.2.4.1.jar"
			 ]

import sys

assert sys.platform.startswith('linux') ^ sys.platform.startswith('win') , "Only Linux and Windows are supported"

if sys.platform.startswith('linux'):
    jars += ["lib/sevenzipjbinding-AllLinux.jar"]
else:
    jars += ["lib/sevenzipjbinding-AllWindows.jar"]
