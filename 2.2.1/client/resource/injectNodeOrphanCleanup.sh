#!/bin/bash
echo 'current user: '$(whoami)
pkill -f selenium-server -u $(whoami)