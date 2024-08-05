@echo off

call snapshot_classpath_setting.bat

@echo on

.\jre64\bin\java -Xmx2048m com.rapidesuite.snapshot.SnapshotAutomatedDeleteMain jdbc:oracle:thin:XXXxx_rs_taty_221_nu/xx_rs_taty_221_nu@oratest90.rapidesuite.com:1521:erpp 18 S C