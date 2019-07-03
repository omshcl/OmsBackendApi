import requests
import subprocess
import time
import json
import urllib.request
import random
import pandas 
print ("starting tomcat server")
cmd =["mvn","tomcat7:run"]
subprocess.Popen(cmd)
print ("waiting for tomcat7 to start")

SHIPNODES = [
    "Austin",
    "El Paso",
    "San Antonio",
    "New Orleans",
    "Dallas",
    "Chicago",
    "Buffalo"
]

ITEMDATA = [
    ["MOTO Samsung Galaxy J3 phone","Galaxy J3 Phone","Phone","Motorola"],
    ["15 inch Lenovo 2019 laptop P52","Lenovo Laptop","Laptop","Lenovo"],
    ["K840 Mechanical Corded Keyboard","Mechanical Keyboard","Keyboard","Logitech"],
    ["Dell SE2419Hx 23.8 \" IPS Full HD Monitor","HD Monitor","Monitor","Dell"]
]

# import mock data 
mock_data = pandas.read_csv("mock.csv")


time.sleep(40)


def addItem(data):
    myurl = 'http://localhost:8080/supply/new'
    req = urllib.request.Request(myurl)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    jsondata = json.dumps(data)
    jsondataasbytes = jsondata.encode('utf-8')   # needs to be bytes
    req.add_header('Content-Length', len(jsondataasbytes))
    response = urllib.request.urlopen(req, jsondataasbytes)
    print(response)


def createItems():
    for item in ITEMDATA:
        sample = mock_data.sample().astype('str').values[0]
        data = {"productclass":'new'
        ,"eta":'6/29/19',"type":'pipeline'
        ,"quantity":random.randrange(50)+1
        # get random address
        ,"shippingaddress":sample[2]
        # get category from item data
        ,"category":item[2]
        ,"locationname":random.choice(SHIPNODES)
        ,"isreturnable":"true","itemdescription":item[0]
        # get manufacture
        , "manufacturername":item[3]
        , "shipbydate":"6/29/19"
        , "price":random.randrange(10000)
        , "shortdescription":item[2]
        ,  "subcategory":"Laptop"
        ,  "unitofmeasure":"Each"}
        addItem(data)


def addOrder(data):
    myurl = 'http://localhost:8080/orders/new'
    req = urllib.request.Request(myurl)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    jsondata = json.dumps(data)
    jsondataasbytes = jsondata.encode('utf-8')   # needs to be bytes
    req.add_header('Content-Length', len(jsondataasbytes))
    response = urllib.request.urlopen(req, jsondataasbytes)
    print(response)


def createOrders():
    for order in range(1000):
        quanities = []
        prices = []
        total = 0
        for _ in  range(random.randrange(10)+1):
            orderid = random.randrange(len(ITEMDATA)-1)+1
            quantity = random.randrange(20)
            price    = random.randrange(10000)
            quanities.append({"itemid":orderid,"quantity":quantity})
            prices.append({"itemid":orderid,"price":price})
            total+= price*quantity 
        sample = mock_data.sample().astype('str').values[0]
        data = {"channel":random.choice(["Online","Phone","Fax"])
        ,"date":"06/23/19"
        ,"firstname":sample[0]
        ,"lastname":sample[1]
        ,"city":sample[3]
        ,"state":sample[4]
        ,"zip":sample[5]
        ,"payment":random.choice(["Credit","Debit","Cash"])
        ,"address":sample[2]
        ,"total":total
        # generate random quantity of orders
        ,"quantity":quanities
        ,"price":prices}
        addOrder(data)

print("Create items")
createItems()
print("create orders")
createOrders()

# delete mock_data from memory so more resources are free on server
del mock_data
# makes sure script doesn't end in order for the docker container to not stop
while True:
    x=4