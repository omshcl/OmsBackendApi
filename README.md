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
### Create Login
`/order/new`
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
### List Orders
`/order/list`  
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
        "item_price": 20000,
        "description": "Landline",
        "id": "0003"
    },
    {
        "item_price": 20000,
        "description": "Landline",
        "id": "0002"
    },
    {
        "item_price": 900,
        "description": "Cellephone",
        "id": "0001"
    }
]
```

# Files
OmsBackendApi/src/main/java/webapp  
├── `Api.java` The base Api class connects to the Cassandra cluster  

├──`LoginApi.java` Extends the Api class and handles login related cassandra queries  
├──`LoginServlet.java` Handles the `/login` endpoint responds to login requests  

├──`OrderApi.java` Extends the Api class and handles login related cassandra queries  
├──`OrderServlet.java` Handles the `/orders/new` inserts data into to orders table

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

### orders table
  The `orders` table stores data associated with each order. The id of each order is auto incrementating. The Items column is a map between Item name and the quanity of that item ordered.
 
    CREATE TABLE oms.orders (id int PRIMARY KEY,address text,channel text,city text,date text,firstname text,items map<text, int>,lastname text,payment text,state text,total int,zip text)
example data
 
    11 | 12345 Main St |  Online | Frisco | 2019-06-12T05:00:00.000Z |      Jane | {'Cellphone': 1, 'Laptop': 1} |      Doe |  Credit |    TX |   534 | 75033

#### order id table
The `order_id` table is used to generate the next id for orders. It's necessary because Cassandra does not implement an autoincrement feature. When a new order is created the value of this table is incremented to generate a new order id.
 
    CREATE TABLE oms.order_id (id text PRIMARY KEY,next int)
example data
 
    id |   12

## Items table
 
   CREATE TABLE oms.items (
      item_id text PRIMARY KEY,
      item_description text,
      item_price int
   )
