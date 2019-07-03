#!/bin/bash
# start the cassandra server 
echo "[cassandra] spawning cassandra"
cd root/apache-cassandra-3.11.4/bin/ || exit
nohup ./cassandra -R -f > cassandralog &
echo "[cassandra] started"
sleep 16
echo "[db] initializing database"
# creates the intiale schema of the db
python2 cqlsh.py --file=../../setup.txt
echo "[db] created table columns"
echo "[backend] starting tomcat services"
cd ../../
echo "populating items"
python populate.py