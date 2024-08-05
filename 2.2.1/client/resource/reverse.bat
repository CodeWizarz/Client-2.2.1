@echo off

call classpath_setting.bat

SET JRE32_HOME=.\jre32\bin
set REVERSE_JVM_ARGUMENTS=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java -Xmx1024m com.rapidesuite.client.common.util.Config REVERSE_JVM_ARGUMENTS') do SET REVERSE_JVM_ARGUMENTS=%%i
set JAVA_PATH=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java -Xmx1024m com.rapidesuite.client.common.util.Config JAVA_PATH') do SET JAVA_PATH=%%i

@echo on
%JAVA_PATH% %javaArguments% %REVERSE_JVM_ARGUMENTS% com.rapidesuite.reverse.ReverseMain
