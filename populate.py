import requests
import subprocess
import time
import json
import urllib.request
import random
import pandas
import math
import datetime
import codecs
import platform
import sys

myplatform = platform.system()
is_win = myplatform == 'Windows'


# unicode encoding handling 
if is_win and sys.version_info < (3, 3):
    codecs.register(lambda name: codecs.lookup(UTF8) if name == CP65001 else None)

from cassandra.cluster import Cluster

cluster = Cluster(["cassandra"])
session = cluster.connect()

session.default_timeout = None
try:
    session.execute("drop keyspace oms;")
except Exception:
    print("failed to drop table")
session.execute("CREATE KEYSPACE oms WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'}  AND durable_writes = true;")
session = cluster.connect("oms")

schema = [
    "CREATE TABLE oms.shipnodes (locationname text PRIMARY KEY);"
    ,"CREATE TABLE oms.order_id (id text PRIMARY KEY,next int);"
    ,"CREATE TABLE oms.orders (id int PRIMARY KEY,address text,channel text,city text,date text,delivery_date text,demand_type text,firstname text,lastname text,payment text,price map<int, int>,quantity map<int, int>,state text,total int,zip text,shipnode text, ordertype text, fulfilled map<int, int>, username text);"
    ,"CREATE TABLE oms.items (itemid int PRIMARY KEY,category text,isreturnable text,itemdescription text,manufacturername text,price int,shortdescription text,subcategory text,unitofmeasure text);"
    ,"CREATE TABLE oms.isreturnable (isreturnable text PRIMARY KEY);"
    ,"CREATE TABLE oms.itemsupplies (shipnode text,itemid int,type text,productclass text,eta text,quantity int,shipbydate text,shippingaddress text,PRIMARY KEY (shipnode, itemid, type, productclass));"
    ,"CREATE TABLE oms.productclass (productclass text PRIMARY KEY);"
    ,"CREATE TABLE oms.type (type text PRIMARY KEY);"
    ,"CREATE TABLE oms.id (id text PRIMARY KEY,itemid int);"
    ,"use oms;"
    ,"insert into order_id (id,next)  VALUES ('id',1);"
    ,"insert into id (id,itemid)      VALUES('id',1);"
    ,"insert into shipnodes (locationname) VALUES('Austin');"
    ,"insert into shipnodes (locationname) VALUES('Buffalo');"
    ,"insert into shipnodes (locationname) VALUES('Chicago');"
    ,"insert into shipnodes (locationname) VALUES('Dallas');"
    ,"insert into shipnodes (locationname) VALUES('El Paso');"
    ,"insert into shipnodes (locationname) VALUES('New Orleans');"
    ,"insert into shipnodes (locationname) VALUES('San Antonio');"
    ,"CREATE TABLE oms.users (username text PRIMARY KEY,isadmin boolean,password text);"
    ,"CREATE TABLE oms.customer_login (username text PRIMARY KEY,password text);"
    ,"create table customers (username text PRIMARY KEY, firstname text, lastname text, shipnode text, orderid int);"
    ,"insert into customers (username, firstname, lastname, shipnode, orderid) VALUES ('pat_abh', 'Abhishek', 'Patil', 'Austin', 23);"
    ,"insert into users (username,isadmin,password) VALUES('admin',true,'�I�Y47����:�oj');"
    ,"insert into users (username,isadmin,password) VALUES('agent',false,'L��G;y���h�w����');"
    ,"insert into customer_login (username,password) VALUES('pat_abh', 'pQ5JxFk0N/6P1BnpOqFvag==');"
    ,"insert into customer_login (username,password) VALUES('user1', 'LypzmT4e5f88rDLZjcEFCA==');"
]
for command in schema:
    session.execute(command)
#from cassandra.cluster import Cluster

#subprocess.Popen(cmd)
print ("waiting for tomcat7 to start")

# lists of default shipnodes
SHIPNODES = [
    "Austin",
    "Buffalo",
    "Chicago",
    "Dallas",
    "El Paso",
    "New Orleans",
    "San Antonio",
]

ITEMDATA = [
    ["MOTO Samsung Galaxy J3 phone","Galaxy J3 Phone","Phone","Motorola","899"],
    ["15 inch Lenovo 2019 laptop P52","Lenovo Laptop","Laptop","Lenovo","1299"],
    ["Dell XPS 15 Laptop 2018","Dell Laptop","Laptop","Dell","999"],
    ["K840 Mechanical Corded Keyboard","Mechanical Keyboard","Keyboard","Logitech","199"],
    ["Dell SE2419Hx 23.8 \" IPS Full HD Monitor","HD Monitor","Monitor","Dell","149"]
]

# import mock data 
mock_data = pandas.read_csv("mock.csv")


#time.sleep(40)


def getNextOrderId():
    orderId = session.execute("select next from order_id")[0][0]
    # increment orderid for next order
    session.execute(session.prepare("UPDATE order_id set next = ? where id ='id'"),[orderId+1])
    return orderId

def sendData(data,url):

    import pdb; pdb.set_trace();
    myurl = url
    req = urllib.request.Request(myurl)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    jsondata = json.dumps(data)
    jsondataasbytes = jsondata.encode('utf-8')   # needs to be bytes
    req.add_header('Content-Length', len(jsondataasbytes))
    response = urllib.request.urlopen(req, jsondataasbytes)


get_exist_stmt = session.prepare("SELECT itemid from items where itemdescription = ? allow filtering")
select_next_id = session.prepare("SELECT itemid from id")
inc_supply_id  = session.prepare("UPDATE id set itemid = ? where id ='id'")
insert_itemsupply_stmt = session.prepare("INSERT INTO itemsupplies(itemid, productclass, eta, shipbydate, shipnode, type, quantity, shippingaddress) values(?,?,?,?,?,?,?,?)")
insert_item_stmt = session.prepare("INSERT INTO items(itemid, category, isreturnable, itemdescription, manufacturername, price, shortdescription, subcategory, unitofmeasure) values(?,?,?,?,?,?,?,?,?)")

def getSupplyId():
    supplyid = session.execute(select_next_id)[0][0]
    session.execute(inc_supply_id,[supplyid+1])
    return supplyid

def insert(supplyid,data):
    dat = [supplyid,data["productclass"],data["eta"],data["shipbydate"],data["locationname"],data["type"],data["quantity"],data["shippingaddress"]]
    session.execute(insert_itemsupply_stmt,dat)    #session.execute(insert_item_stmt,[supplyid,data["category"],data["isreturnable"],data["itemdescription"],data["manufacturername"],data["price"],data["shortdescription"],data["subcategory"],data["unitofmeasure"]])


# creates the list of items from sample data
def createItems():
    #add items to items table
    for index in range(len(ITEMDATA)):
        item = ITEMDATA[index]
        data = {
        "category":item[2]
        ,"isreturnable":"true"
        ,"itemdescription":item[0]
        ,"manufacturername":item[3]
        ,"price":int(item[4])
        ,"shortdescription":item[1]
        ,"subcategory":item[1]
        ,"unitofmeasure":"each"}
        session.execute(insert_item_stmt,[(index+1),data["category"],data["isreturnable"],data["itemdescription"],data["manufacturername"],data["price"],data["shortdescription"],data["subcategory"],data["unitofmeasure"]])
        
    #create item supply    
    for _ in range(50):
        item = random.choice(ITEMDATA)
        description = item[0]
        sample = mock_data.sample().astype('str').values[0]
        data = {"productclass":random.choice(["new","new","new","new","new","new","new","new","new","used"])
        ,"eta":'6/29/19'
        ,"type":random.choice(["pipeline","onhand","onhand","onhand","onhand"])
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
        , "price":int(item[4])
        , "shortdescription":item[1]
        ,  "subcategory":"Laptop"
, "unitofmeasure":"Each"}
        itemid = session.execute(get_exist_stmt,[item[0]])
        #if itemid:
        insert(itemid[0][0],data)
        #else:
            #insert(getSupplyId(),data)
    
    #make reserved item (item 1, qty 3, Austin)
    item = ITEMDATA[0]
    description = item[0]
    sample = mock_data.sample().astype('str').values[0]
    data = {"productclass":"new"
    ,"eta":'6/29/19'
    ,"type":"reserved"
    ,"quantity":3
    # get random address
    ,"shippingaddress":sample[2]
    # get category from item data
    ,"category":item[2]
    ,"locationname":"Austin"
    ,"isreturnable":"true","itemdescription":item[0]
    # get manufacture
    , "manufacturername":item[3]
    , "shipbydate":"6/29/19"
    , "price":int(item[4])
    , "shortdescription":item[1]
    ,  "subcategory":"Laptop"
, "unitofmeasure":"Each"}
    itemid = session.execute(get_exist_stmt,[item[0]])
    #if itemid:
    insert(itemid[0][0],data)


#create orders default is 1000
def createOrders(num): 
    if num < 30: #minimum of 30 orders
        num = 30
    username = ""
    startdate = (datetime.datetime.now() - datetime.timedelta(days=num+11)).strftime("%Y-%m-%d")
    curdate = startdate
    for order in range(num): #completed orders
        date = datetime.datetime.strptime(startdate, '%Y-%m-%d') + datetime.timedelta(days=order)
        curdate = date
        deliverydate = date + datetime.timedelta(days=random.randrange(10)+1)
        date = date.strftime('%Y-%m-%d')
        deliverydate = deliverydate.strftime('%Y-%m-%d')
        quanities = {}
        prices = {}
        fulfilledqtys = {}
        ordertype = random.choice(["ship","pickup"])
        shipnode = random.choice(SHIPNODES)
        if ordertype == "ship":
            shipnode = ""
        # keep tracking of the total price of order
        total = 0
        # generate from 1-10 items for each order
        for _ in  range(random.randrange(10)+1):
            itemid = random.randrange(len(ITEMDATA))+1
            quantity = random.randrange(20)+1
            discount = random.randrange(30) + 75
            price = math.floor(int(ITEMDATA[itemid-1][4]) * (discount/100))
            quanities[itemid] = quantity
            prices[itemid] = price
            fulfilledqtys[itemid] = 0
            # add current items price to total
            total+= price*quantity 
        # get random sample from mock.csv
        sample = mock_data.sample().astype('str').values[0]
        orderId = getNextOrderId()
        data = [orderId, random.choice(["Online","Phone","Fax"]),date,sample[0],sample[1],sample[3],sample[4],sample[5],random.choice(["Credit","PO","Cash"]),total,sample[2],quanities,prices,fulfilledqtys,deliverydate,ordertype,shipnode, username]
        create_stmt = session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,quantity,price,fulfilled,delivery_date,ordertype,shipnode,username,demand_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'"+"COMPLETE_ORDER"+"')")
        session.execute(create_stmt,data)
        
    startdate = curdate.strftime('%Y-%m-%d')
    for order in range(10): #Create Partial and Open Orders
        date = datetime.datetime.strptime(startdate, '%Y-%m-%d') + datetime.timedelta(days=order+1)
        curdate = date
        deliverydate = date + datetime.timedelta(days=random.randrange(10)+1)
        date = date.strftime('%Y-%m-%d')
        deliverydate = deliverydate.strftime('%Y-%m-%d')
        quanities = {}
        prices = {}
        fulfilledqtys = {}
        demandtype = random.choice(["PARTIAL_ORDER", "OPEN_ORDER"])
        ordertype = random.choice(["Ship","Pickup"])
        shipnode = random.choice(SHIPNODES)
        if ordertype == "ship":
            shipnode = ""        
        # keep tracking of the total price of order
        total = 0
        # generate from 1-10 items for each order
        for _ in  range(random.randrange(10)+1):
            itemid = random.randrange(len(ITEMDATA))+1
            quantity = random.randrange(20)+1
            discount = random.randrange(30) + 75
            price = math.floor(int(ITEMDATA[itemid-1][4]) * (discount/100))
            quanities[itemid] = quantity
            prices[itemid] = price
            if demandtype == "PARTIAL_ORDER":
                fulfilledqtys[itemid] = random.randrange(quantity) + 1
            else:
                fulfilledqtys[itemid] = 0
                deliverydate = ""
            # add current items price to total
            total+= price*quantity 
        # get random sample from mock.csv
        sample = mock_data.sample().astype('str').values[0]
        orderId = getNextOrderId()
        data = [orderId, random.choice(["Online","Phone","Fax"]),date,sample[0],sample[1],sample[3],sample[4],sample[5],random.choice(["Credit","PO","Cash"]),total,sample[2],quanities,prices,fulfilledqtys,deliverydate,ordertype,shipnode,username]
        create_stmt = session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,quantity,price,fulfilled,delivery_date,ordertype,shipnode,username,demand_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'"+demandtype+"')")
        session.execute(create_stmt,data)
        
    startdate = curdate.strftime('%Y-%m-%d')
    for order in range(1): #Create Reservations
        date = datetime.datetime.strptime(startdate, '%Y-%m-%d') + datetime.timedelta(days=order+1)
        curdate = date
        date = date.strftime('%Y-%m-%d')
        quanities = {}
        prices = {}
        fulfilledqtys = {}
        demandtype = "RESERVED_ORDER"
        ordertype = "Reservation"
        shipnode = "Austin"     
        # keep tracking of the total price of order
        total = 0
        # generate from 1-10 items for each order
        itemid = 1
        quantity = 3
        price = int(ITEMDATA[itemid-1][4])
        quanities[itemid] = quantity
        prices[itemid] = price
        fulfilledqtys[itemid] = 0
        deliverydate = ""
        # add current items price to total
        total+= price*quantity 
        # get random sample from mock.csv
        sample = mock_data.sample().astype('str').values[0]
        orderId = getNextOrderId()
        data = [orderId, random.choice(["Online","Phone","Fax"]),date,sample[0],sample[1],sample[3],sample[4],sample[5],random.choice(["Credit","PO","Cash"]),total,sample[2],quanities,prices,fulfilledqtys,deliverydate,ordertype,shipnode,username]
        create_stmt = session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,quantity,price,fulfilled,delivery_date,ordertype,shipnode,username,demand_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'"+demandtype+"')")
        session.execute(create_stmt,data)    


print("create orders")
createOrders(100)
print("Create items")
createItems()
print ("starting tomcat server")
cmd =["mvn","tomcat7:run"]
subprocess.call(cmd)