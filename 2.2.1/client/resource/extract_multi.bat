@echo off

call multiuser_path_setting.bat
call extract_classpath_setting.bat

SET JRE32_HOME=.\jre32\bin
set EXTRACT_JVM_ARGUMENTS=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java %javaArguments% -Xmx1024m com.rapidesuite.client.common.util.Config EXTRACT_JVM_ARGUMENTS') do SET EXTRACT_JVM_ARGUMENTS=%%i
set JAVA_PATH=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java %javaArguments% -Xmx1024m com.rapidesuite.client.common.util.Config JAVA_PATH') do SET JAVA_PATH=%%i

@echo on
%JAVA_PATH% -DLOG_FOLDER=%RES_DRIVE%/log -DTEMP_FOLDER=%RES_DRIVE%/temp -DEXTRACT_ZIP_FILE_LOCATION=%RES_DRIVE%/zip-files -DEXTRACT_OUTPUT_FOLDER=%RES_DRIVE%/output -DEXTRACT_SESSIONS_FOLDER=%RES_DRIVE%/sessions %EXTRACT_JVM_ARGUMENTS% com.rapidesuite.extract.ExtractMain

@echo off
SUBST %RES_DRIVE% /D