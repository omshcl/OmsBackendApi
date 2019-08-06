#!/bin/bash
cd root
echo "SLEEPING"
sleep 25;
export JAVA_HOME=/usr/lib/jvm/default 
echo "DONE sleeping"
python populate.py