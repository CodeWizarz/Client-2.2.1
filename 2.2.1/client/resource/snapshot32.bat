@echo off

call snapshot_classpath_setting.bat

@echo on

.\jre32\bin\java -Xmx1024m com.rapidesuite.snapshot.SnapshotMain

PAUSE
