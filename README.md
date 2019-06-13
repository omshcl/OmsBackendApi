
## table structure

### users table

 username | isadmin | password
    admin |    True | Admin!123
    
    agent |   False | Agent!123

### orders table
  The `orders` table stores data associated with each order. The id of each order is auto incrementating. The Items column is a map between Item name and the quanity of that item ordered.

 id | address       | channel | city   | date                     | firstname | items                         | lastname | payment | state | total | zip

    11 | 12345 Main St |  Online | Frisco | 2019-06-12T05:00:00.000Z |      Jane | {'Cellphone': 1, 'Laptop': 1} |      Doe |  Credit |    TX |   534 | 75033

#### order id table
The `order_id` table is used to generate the next id for orders. It's necessary because Cassandra does not implement an autoincrement feature. When a new order is created the value of this table is incremented to generate a new order id.
 id | next
 
    id |   12
