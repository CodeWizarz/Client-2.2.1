@REM  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@REM  $Revision: 33177 $:
@REM  $Author: john.snell $:
@REM  $Date: 2013-05-21 16:14:41 +0800 (Tue, 21 May 2013) $:
@REM  $HeadURL: http://svn01.rapidesuite.com:999/svn/a/IT/panopticon/current/runVMCD.bat $:
@REM  $Id: runVMCD.bat 33177 2013-05-21 08:14:41Z john.snell $:
@REM  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

@REM pushd deploy
java -version
java -Xmx256m -classpath panopticon.jar com.rapidesuite.panopticon.Main panopticon.properties
@REM popd