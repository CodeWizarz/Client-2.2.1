
@echo off

set EXECUTABLE_FILE_NAME=%1
set UPDATES_FOLDER_PATH=%2

:execute
call %UPDATES_FOLDER_PATH%\classpath_setting.bat %UPDATES_FOLDER_PATH%
SET JRE_HOME=.\%UPDATES_FOLDER_PATH%\jre32\bin

@echo on
%JRE_HOME%\java -version
%JRE_HOME%\java -Xmx1024m com.rapidesuite.client.common.UpdaterMain %EXECUTABLE_FILE_NAME% %UPDATES_FOLDER_PATH%

:eof
