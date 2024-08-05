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

import java_arguments_handler
import multiuser_path_setting
import extract_classpath_setting

import sys

cp = "."
for jar in extract_classpath_setting.jars:
    cp += ":"+jar

javaCmd = "java"
    
assert javaCmd != None
assert cp != None   

javaArguments = java_arguments_handler.javaArguments

import subprocess
 
procJvmArgumentsRetriever = subprocess.Popen([javaCmd, '-cp', cp, '-Xmx1024m', 'com.rapidesuite.client.common.util.Config', 'EXTRACT_JVM_ARGUMENTS'], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
stdoutJvmArgumentsRetriever = procJvmArgumentsRetriever[0].strip()
if stdoutJvmArgumentsRetriever:
    javaArguments += ' ' + stdoutJvmArgumentsRetriever

import os    
javaArguments += ' -DLOG_FOLDER='+os.path.join(multiuser_path_setting.rapid_client_directory, 'log')
javaArguments += ' -DTEMP_FOLDER='+os.path.join(multiuser_path_setting.rapid_client_directory, 'temp')
javaArguments += ' -DEXTRACT_ZIP_FILE_LOCATION='+os.path.join(multiuser_path_setting.rapid_client_directory, 'zip-files')   
javaArguments += ' -DEXTRACT_OUTPUT_FOLDER='+os.path.join(multiuser_path_setting.rapid_client_directory, 'output')   
javaArguments += ' -DEXTRACT_SESSIONS_FOLDER='+os.path.join(multiuser_path_setting.rapid_client_directory, 'sessions')   

procJavaPath =  subprocess.Popen([javaCmd, '-cp', cp, '-Xmx1024m', 'com.rapidesuite.client.common.util.Config', 'JAVA_PATH'], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
stdoutJavaPath = procJavaPath[0].strip()
if stdoutJavaPath:
    javaCmd = stdoutJavaPath
    
if sys.platform.startswith('linux'):
    javaCmd += ' -Djava.security.egd=file:///dev/urandom '        

cmd = javaCmd + "  " + javaArguments+" -cp "+cp+" com.rapidesuite.extract.ExtractMain"

#decomment the following lines if you want to debug this script
#print 'OS is',sys.platform 
#print cmd

import os
#Ensure that we're using the script folder as the current folder
os.chdir(os.path.dirname(os.path.abspath(__file__)))
os.system(cmd)     
