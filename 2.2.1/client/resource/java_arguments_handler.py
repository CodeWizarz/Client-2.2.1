#!/usr/bin/python

import sys

javaArguments=""
for i in range(1, len(sys.argv)):
    argTrimmed=sys.argv[i].strip()
    argTrimmedArr=argTrimmed.split('=')
    assert len(argTrimmedArr) > 1, "The argument must be in key value format"
    
    key=argTrimmedArr[0]
    value=""
    for j in range(1, len(argTrimmedArr)):
        value += argTrimmedArr[j]
        if j < len(argTrimmedArr)-1:
            value += "="
    if ' ' in value:
        if '"' in value:
            value = value.replace('"', '\\"')
        value = '"' + value + '"'

    arg = key + '=' + value
    if i > 1:
        javaArguments += ' '
    javaArguments += '-D'+arg