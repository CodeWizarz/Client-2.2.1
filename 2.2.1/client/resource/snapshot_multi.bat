@echo off

call multiuser_path_setting.bat
call snapshot_classpath_setting.bat

SET JRE32_HOME=.\jre32\bin
set SNAPSHOT_JVM_ARGUMENTS=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java %javaArguments% -Xmx1024m com.rapidesuite.client.common.util.Config SNAPSHOT_JVM_ARGUMENTS') do SET SNAPSHOT_JVM_ARGUMENTS=%%i
set JAVA_PATH=
FOR /F "tokens=*" %%i in ('%JRE32_HOME%\java %javaArguments% -Xmx1024m com.rapidesuite.client.common.util.Config JAVA_PATH') do SET JAVA_PATH=%%i

@echo on
%JAVA_PATH% %SNAPSHOT_JVM_ARGUMENTS% com.rapidesuite.snapshot.SnapshotMain

@echo off
SUBST %RES_DRIVE% /D