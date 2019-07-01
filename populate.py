import requests
import subprocess
print "sleeping"
cmd =["mvn","tomcat7:run"]
subprocess.Popen(cmd)
import time
import json
import urllib2

time.sleep(40)


def addItem(data):
    req = urllib2.Request('http://localhost:8080/supply/new')
    req.add_header('Content-Type', 'application/json')
    response = urllib2.urlopen(req, json.dumps(data))
    print(response)

data = {
    "productclass":'new',
    "eta":'6/29/19',
    "type":'pipeline',
    "quantity":10,
    "shippingaddress":'12345 Laptop Street',
    "category":"Technology",
    "locationname":"Austin",
    "isreturnable":"true",
    "itemdescription":"15 inch Lenovo 2019 laptop P52",
    "manufacturername":"Lenovo",
    "shipbydate":"6/29/19",
    "price":1500,
    "shortdescription":"Lenovo Laptop",
    "subcategory":"Laptop",
    "unitofmeasure":"Each"
}

addItem(data)

data["itemdescription"]="MOTO Samsung Galaxy J3 phone"
data["shortdescription"]="Galaxy J3 Phone"
data["subcategory"]="Phone"
data["locationname"]="El Paso"
data["shippingaddress"]="5342 Graceland Terrace"
data["price"]=400

addItem(data)

data["itemdescription"]="K840 Mechanical Corded Keyboard"
data["shortdescription"]="Mechanical Keyboard"
data["price"]=43
data["manufacturename"]="Logitech"
data["subcategory"]="Keyboard"
data["shippingaddress"]="5342 Graceland Terrace"
data["locationname"]="Arlington"


addItem(data)

data["itemdescription"]="AT&T ML17929 2-Line Corded Telephone, Black"
data["shortdescription"]="Landline Telephone"
data["price"]=72
data["manufacturename"]="AT&T"
data["subcategory"]="Phone"
data["locationname"]="San Antonio"
data["shippingaddress"]="71632 Old Gate Way"


addItem(data)


data["itemdescription"]="Dell SE2419Hx 23.8 \" IPS Full HD Monitor"
data["shortdescription"]="HD Monitor"
data["subcategory"]="Monitor"
data["locationname"]="El Paso"
data["shippingaddress"]="756 West hill Road"
data["price"]=48

addItem(data)
while True:
    x=4