@echo off

call snapshot_classpath_setting.bat

@echo on

.\jre64\bin\java -Xmx2048m com.rapidesuite.snapshot.SnapshotMain

PAUSE