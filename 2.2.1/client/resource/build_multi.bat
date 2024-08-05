@echo off

call multiuser_path_setting.bat
call classpath_setting.bat

SET JRE32_HOME=.\jre32\bin
set BUILD_JVM_ARGUMENTS=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java %javaArguments% -Xmx1024m com.rapidesuite.client.common.util.Config BUILD_JVM_ARGUMENTS') do SET BUILD_JVM_ARGUMENTS=%%i
set JAVA_PATH=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java %javaArguments% -Xmx1024m com.rapidesuite.client.common.util.Config JAVA_PATH') do SET JAVA_PATH=%%i

@echo on
%JAVA_PATH% -DLOG_FOLDER=%RES_DRIVE%/log -DTEMP_FOLDER=%RES_DRIVE%/temp -DBUILD_ITERATION_LOG_FOLDER=%RES_DRIVE%/iteration_logs %BUILD_JVM_ARGUMENTS% com.rapidesuite.build.BuildMain

@echo off
SUBST %RES_DRIVE% /D