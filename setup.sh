#!/bin/bash
echo "[cassandra] spawning cassandra"
cd root/apache-cassandra-3.11.4/bin/ || exit
nohup ./cassandra -R -f > cassandralog &
echo "[cassandra] started"
sleep 10
echo "[db] initializing database"
python cqlsh.py --file=../../setup.txt
echo "[db] created table columns"
echo "[backend] starting tomcat services"
cd ../../
nohup mvn tomcat7:run &
echo "DONE"
sleep 40;
echo "populating items"
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{
 productclass: "new",
 eta: "6/29/19",
 shipbydate: "6/27/19",
 type: "pipeline",
 quantity: 10,
 shippingaddress: "12345 Laptop Street",
 category: "Technology",
 locationname: "Austin",
 isreturnable: "true",
 itemdescription: "15 inch Lenovo 2019 laptop P52",
 manufacturername: "Lenovo",
 price: 1500,
 shortdescription: "Lenovo Laptop",
 subcategory: "Laptop",
 unitofmeasure: "Each"
}' \
  localhost:8080/supply/new