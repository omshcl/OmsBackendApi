# endpoints
## Login
`/login`
Method: POST
```
{
"username":"user",
"password":"pass"
}
```
response 
valid is whether a user exists and their password matches. isAdmin determines if a user is an administrator
```
{
"valid": false,
"isAdmin": false
}
```
## Order
### Create Order
`/orders/new`
Method: Post
```
{"items": [
{ "item": "Laptop", "quantity":2},
{ "item": "Cellephone", "quantity":3 },
{ "item": "Item 3", "quantity":1}
],
"channel": "Online",
"date": "2019-06-12T05:00:00.000Z",
"firstname": "Jane",
"lastname": "Doe",
"address": "12345 Main St",
"city": "Frisco",
"state": "TX",
"zip": "75033",
"payment": "Credit",
"total": 534
}
```
response
```
{"sucesss":"true"}
```

### Update Order
`/orders/update`
Method: Post
```
{"items": [
{ "item": "Laptop", "quantity":2},
{ "item": "Cellephone", "quantity":3 }
],
"channel": "Online",
"date": "2019-06-12T05:00:00.000Z",
"firstname": "John",
"lastname": "Doe",
"address": "12345 Main St",
"city": "Frisco",
"state": "TX",
"zip": "75033",
"payment": "Credit",
"total": 534,
"id":12
}
```
response
```
{"sucesss":"true"}
```
### List Orders
`/orders/list`  
Method: Get  
Response  
```
[
    {
        "date": "2019-06-12T05:00:00.000Z",
        "zip": "75033",
        "firstname": "Jane",
        "total": 534,
        "address": "12345 Main St",
        "city": "Frisco",
        "channel": "Online",
        "payment": "Credit",
        "id": 16,
        "state": "TX",
        "items": {
            "Item 3": 1,
            "Laptop": 2,
            "Cellephone": 3
        },
        "lastname": "Doe"
    },
    {
        "date": "2019-06-12T05:00:00.000Z",
        "zip": "75033",
        "firstname": "Jane",
        "total": 534,
        "address": "12345 Main St",
        "city": "Frisco",
        "channel": "Online",
        "payment": "Credit",
        "id": 11,
        "state": "TX",
        "items": {
            "Laptop": 1,
            "Cellphone": 1
        },
        "lastname": "Doe"
    },
    {
        "date": "2019-06-12T05:00:00.000Z",
        "zip": "75033",
        "firstname": "Jane",
        "total": 534,
        "address": "12345 Main St",
        "city": "Frisco",
        "channel": "Online",
        "payment": "Credit",
        "id": 15,
        "state": "TX",
        "items": {
            "Item 3": 1,
            "Laptop": 2,
            "Cellephone": 3
        },
        "lastname": "Doe"
    },
    {
        "date": "2019-06-12T05:00:00.000Z",
        "zip": "75033",
        "firstname": "Jane",
        "total": 534,
        "address": "12345 Main St",
        "city": "Frisco",
        "channel": "Online",
        "payment": "Credit",
        "id": 14,
        "state": "TX",
        "items": {
            "Item 3": 1,
            "Item 2": 3,
            "Item 1": 2
        },
        "lastname": "Doe"
    },
    {
        "date": "2019-06-12T05:00:00.000Z",
        "zip": "75033",
        "firstname": "Jane",
        "total": 534,
        "address": "12345 Main St",
        "city": "Frisco",
        "channel": "Online",
        "payment": "Credit",
        "id": 17,
        "state": "TX",
        "items": {
            "Laptop": 2,
            "Cellephone": 3
        },
        "lastname": "Doe"
    },
    {
        "date": "2019-06-12T05:00:00.000Z",
        "zip": "75033",
        "firstname": "Jane",
        "total": 534,
        "address": "12345 Main St",
        "city": "Frisco",
        "channel": "Online",
        "payment": "Credit",
        "id": 12,
        "state": "TX",
        "items": {
            "Laptop": 1,
            "Cellphone": 1
        },
        "lastname": "Doe"
    }
]
```

## List Items
`/items/list`
Method: Get  
Response
```
[
    {
        "price": 200,
        "description": "LandLine",
        "id": "0003"
    },
    {
        "price": 2000,
        "description": "Laptop",
        "id": "0002"
    },
    {
        "price": 1,
        "description": "Cellephone",
        "id": "0001"
    }
]
```

# Files
OmsBackendApi/src/main/java/oms   
├── `Api.java` The base Api class connects to the Cassandra cluster  
├── oms.items  
⧸    ├── `ItemApi.java` Extends Items  
⧸    └── `ItemsServlet.java`  
├── oms.login  
⧸   ├── `LoginApi.java` Extends the Api class and handles login related cassandra queries  
⧸     └── `LoginServlet.java` Handles the `/login` endpoint responds to login requests  
├── oms.orders  
⧸     ├── `OrderApi.java` Extends the Api class and handles login related cassandra queries  
⧸     ├── `OrderCreate.java` Handles the `/orders/new` endpoint, inserts data into to orders table  
⧸     └── `OrderLeast.java`  Handles the `/orders/list` endpoint, queries all orders from db  
#  Requirements
- Apache Cassandra running
- Tables created in Cassandra
- Mvn installed  
To build and run type  
`mvn tomcat7:run`

# Table Structure

## users table

 username | isadmin | password
 
    CREATE TABLE oms.users (username text PRIMARY KEY,isadmin boolean,password text)
 example data
  
    admin |    True | Admin!123
    agent |   False | Agent!123

### orders tagible
  The `orders` table stores data associated with each order. The id of each order is auto incrementating. The Items column is a map between Item name and the quanity of that item ordered.
 
    CREATE TABLE oms.orders (id int PRIMARY KEY,address text,channel text,city text,date text,firstname text,items map<text, int>,lastname text,payment text,state text,total int,zip text)
example data
 
    11 | 12345 ls
     St |  Online | Frisco | 2019-06-12T05:00:00.000Z |      Jane | {'Cellphone': 1, 'Laptop': 1} |      Doe |  Credit |    TX |   534 | 75033

#### order id table
The `order_id` table is used to generate the next id for orders. It's necessary because Cassandra does not implement an autoincrement feature. When a new order is created the value of this table is incremented to generate a new order id.
 
    CREATE TABLE oms.order_id (id text PRIMARY KEY,next int)
example data
 
    id |   12

## Items table
 
   CREATE TABLE oms.items (id text PRIMARY KEY,description text,price int)

    insert into items (id,description,price) values('0001','Cellephone',1);  
    insert into items (id,description,price) values('0002','Laptop',2000);
    insert into items (id,description,price) values('0003','LandLine',200);
