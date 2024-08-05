@echo off

call multiuser_path_setting.bat
call classpath_setting.bat

SET JRE32_HOME=.\jre32\bin
set REVERSE_JVM_ARGUMENTS=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java %javaArguments% -Xmx1024m com.rapidesuite.client.common.util.Config REVERSE_JVM_ARGUMENTS') do SET REVERSE_JVM_ARGUMENTS=%%i
set JAVA_PATH=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java %javaArguments% -Xmx1024m com.rapidesuite.client.common.util.Config JAVA_PATH') do SET JAVA_PATH=%%i

@echo on
%JAVA_PATH% -DLOG_FOLDER=%RES_DRIVE%/log -DTEMP_FOLDER=%RES_DRIVE%/temp -DREVERSE_ZIP_FILE_LOCATION=%RES_DRIVE%/zip-files -DREVERSE_OUTPUT_FOLDER=%RES_DRIVE%/output -DREVERSE_SESSIONS_FOLDER=%RES_DRIVE%/sessions %REVERSE_JVM_ARGUMENTS% com.rapidesuite.reverse.ReverseMain

@echo off
SUBST %RES_DRIVE% /D