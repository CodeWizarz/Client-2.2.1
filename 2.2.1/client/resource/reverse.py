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
import classpath_setting

import sys

if sys.platform.startswith('linux'):

    cp = "."
    for jar in classpath_setting.jars:
        cp += ":"+jar
    
    javaCmd = "java"
else:
    cp = ""
    for jar in classpath_setting.jars:
        cp += ";"+jar

    javaCmd = ".\\jre32\\bin\\java"
    
assert javaCmd != None
assert cp != None    

import subprocess
 
procJvmArgumentsRetriever = subprocess.Popen([javaCmd, '-cp', cp, '-Xmx1024m', 'com.rapidesuite.client.common.util.Config', 'REVERSE_JVM_ARGUMENTS'], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
stdoutJvmArgumentsRetriever = procJvmArgumentsRetriever[0].strip()
if stdoutJvmArgumentsRetriever:
    java_arguments_handler.javaArguments += ' ' + stdoutJvmArgumentsRetriever
    

procJavaPath =  subprocess.Popen([javaCmd, '-cp', cp, '-Xmx1024m', 'com.rapidesuite.client.common.util.Config', 'JAVA_PATH'], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
stdoutJavaPath = procJavaPath[0].strip()
if stdoutJavaPath:
    javaCmd = stdoutJavaPath
    
if sys.platform.startswith('linux'):
    javaCmd += ' -Djava.security.egd=file:///dev/urandom '        

cmd = javaCmd + "  " + java_arguments_handler.javaArguments+" -cp "+cp+" com.rapidesuite.reverse.ReverseMain"

#decomment the following lines if you want to debug this script
#print 'OS is',sys.platform 
#print cmd

import os
#Ensure that we're using the script folder as the current folder
os.chdir(os.path.dirname(os.path.abspath(__file__)))
os.system(cmd)     
