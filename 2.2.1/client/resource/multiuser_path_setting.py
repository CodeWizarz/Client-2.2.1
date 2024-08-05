#!/usr/bin/python

import sys
assert sys.platform.startswith('linux'), 'This is only for linux. Your OS is '+str(sys.platform)

import os
rapid_client_directory = os.path.expanduser('~/Documents/Rapid_Client/')

if not os.path.exists(rapid_client_directory):
    os.makedirs(rapid_client_directory)