#!/usr/bin/python

#################################
#IMPORTANT NOTE FOR WINDOWS USERS
#################################
#There is a known issue with Python where it will not always execute properly with command-line arguments.
#This is due to a Windows registry issue.  It can be corrected as follows:
#Change 
#    HKEY_CLASSES_ROOT\Applications\python26.exe\shell\open\command 
#and
#    HKEY_CLASSES_ROOT\py_auto_file\shell\open\command
#From:
#    "C:\Python26\python26.exe" "%1"
#To:
#    "C:\Python26\python26.exe" "%1" %*
#
#See Also:
#     http://eli.thegreenplace.net/2010/12/14/problem-passing-arguments-to-python-scripts-on-windows/
#################################

import sys
executable_file_name=sys.argv[1]
updates_folder_path=sys.argv[2]

import classpath_setting
cp = "."
for jar in classpath_setting.jars:
    cp += ":"+updates_folder_path+jar
    
import os
os.system("java -Djava.security.egd=file:///dev/urandom -cp "+cp+" -Xmx1024m com.rapidesuite.client.common.UpdaterMain "+executable_file_name+" "+updates_folder_path)
