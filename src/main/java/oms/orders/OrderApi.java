package oms.orders;

import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;  
import java.util.Date; 
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.time.format.DateTimeFormatter;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import oms.Api;

public class OrderApi extends Api {
	
	private Session session;
	private PreparedStatement create_order_stmt,
	select_next_id,inc_id_stmt,list_allorders_stmt, summarize_order_stmt, get_price_stmt, get_shortdescri_stmt,get_completed_list,get_open_list, get_quantity_stmt, set_schedule_stmt,
	set_fulfill_stmt, get_max_allid, insert_customer_stmt, customer_orders_list, customer_ready_stmt, get_availableR_stmt, ready_pickup_stmt, customer_coming_stmt, set_final_stmt, get_reserved_num, get_quantityR_stmt, set_finalPartial_stmt, get_limitorder_list, set_finalReserved_stmt, reopen_stmt, update_ddate_stmt, complete_stmt, get_available_stmt, update_fulfilled_stmt, update_stock_stmt, get_max_completeid, partial_stmt, get_fulfilledMap_stmt;

	public OrderApi() {
		super();
		session = super.getSession();
		//Insert into the orders table
		create_order_stmt 		= session.prepare("INSERT INTO ORDERS (id,username,channel,date,firstname,lastname,city,state,zip,payment,total,address,quantity,price,demand_type,shipnode,ordertype,fulfilled,delivery_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		//Selects the next order id
		select_next_id   		= session.prepare("SELECT next from order_id");
		//Gets entire order table
		list_allorders_stmt   	= session.prepare("SELECT * from orders");
		//Retrieves row from order table about specific order
		summarize_order_stmt    = session.prepare("SELECT * from orders where id = ?");
		//Gets map of number fulfilled of each item
		get_fulfilledMap_stmt   = session.prepare("SELECT fulfilled from orders where id = ?");
		//Finds maximum order id from all orders
		get_max_allid			= session.prepare("SELECT max(id) as maxid from orders allow filtering");
		//Finds maximum order id out of completed orders
		get_max_completeid		= session.prepare("SELECT max(id) as maxid from orders where demand_type = 'COMPLETE_ORDER' allow filtering");
		//Selects COMPLETE_ORDERs
		get_completed_list 		= session.prepare("SELECT * from orders where demand_type = 'COMPLETE_ORDER' and id > ? allow filtering");
		//Selects orders limited
		get_limitorder_list 	= session.prepare("SELECT * from orders where id > ? allow filtering");
		//Selects orders that are OPEN_ORDERs
		get_open_list			= session.prepare("SELECT * from orders where demand_type = 'OPEN_ORDER' allow filtering");
		//Selects ALLOCATE_ORDERs to prepare to complete
		set_final_stmt	 		= session.prepare("SELECT * from orders where demand_type = 'ALLOCATE_ORDER' allow filtering");
		//Selects PARTIAL_ORDERs to prepare to complete
		set_finalPartial_stmt   = session.prepare("SELECT * from orders where demand_type = 'PARTIAL_ORDER' allow filtering"); 
		//Selects RESERVED_ORDERs to prepare to complete
		set_finalReserved_stmt  = session.prepare("SELECT * from orders where demand_type = 'RESERVED_ORDER' allow filtering");
		//Selects all orders made by a specific user
		customer_orders_list    = session.prepare("SELECT * from orders where username = ? allow filtering");
		//Generates new order ids
		inc_id_stmt      		= session.prepare("UPDATE order_id set next = ? where id ='id'");
		//Sets an order to SCHEDULE_ORDER
		set_schedule_stmt 		= session.prepare("UPDATE orders set demand_type = 'SCHEDULE_ORDER' where id = ?");
		//Sets an order to ALLOCATE_ORDER
		set_fulfill_stmt 		= session.prepare("UPDATE orders set demand_type = 'ALLOCATE_ORDER' where id = ?");
		//Resets a specific order to OPEN_ORDER status
		reopen_stmt 			= session.prepare("UPDATE orders set demand_type = 'OPEN_ORDER' where id = ?");
		//Completes a specific order
		complete_stmt 			= session.prepare("UPDATE orders set demand_type = 'COMPLETE_ORDER' where id = ?");
		//Sets an order to being ready for pickup
		ready_pickup_stmt 		= session.prepare("UPDATE orders set demand_type = 'READY_PICKUP' where id = ?");
		//Sets an order to CUSTOMER_COMING
		customer_coming_stmt 	= session.prepare("UPDATE orders set demand_type = 'CUSTOMER_COMING' where id = ?");
		//Sets an order to CUSTOMER_READY
		customer_ready_stmt 	= session.prepare("UPDATE orders set demand_type = 'CUSTOMER_READY' where id = ?");
		//Sets order as Partial
		partial_stmt 			= session.prepare("UPDATE orders set demand_type = 'PARTIAL_ORDER' where id = ?");
		//Updates fulfilled map
		update_fulfilled_stmt   = session.prepare("UPDATE orders set fulfilled = ? where id = ?");
		//Updates delivery_date
		update_ddate_stmt       = session.prepare("UPDATE orders set delivery_date = ? where id = ?");
		//Selects all stock of a certain itemid of specific type that is new
		get_available_stmt 		= session.prepare("SELECT * from itemsupplies where itemid = ? and type = ? and productclass = 'new' allow filtering");
		//Selects all stock of a certain itemid of specific type that is new with Shipnode Restrictions
		get_availableR_stmt 	= session.prepare("SELECT * from itemsupplies where itemid = ? and type = ? and shipnode = ? and productclass = 'new' allow filtering");
		//Finds the number of new, onhand stock of a certain item
		get_quantity_stmt 		= session.prepare("SELECT sum(quantity) as total from itemsupplies where type = ? and itemid = ? and productclass = 'new' allow filtering");
		//Finds the number of new, onhand stock of a certain item restricting shipnode
		get_quantityR_stmt 		= session.prepare("SELECT sum(quantity) as total from itemsupplies where type = ? and itemid = ? and shipnode = ? and productclass = 'new' allow filtering");
		//Gets price of a specific item
		get_price_stmt    		= session.prepare("SELECT price from items where itemid = ? allow filtering");
		//Gets not necessarily unique shortdescription for specific itemid
		get_shortdescri_stmt 	= session.prepare("SELECT shortdescription from items where itemid = ?");
		//Gets reserved quantity
		get_reserved_num		= session.prepare("SELECT sum(quantity) as total from itemsupplies where type = 'reserved' and itemid = ? and shipnode = ? and productclass = 'new'");
		//changes stock of item
		update_stock_stmt 		= session.prepare("UPDATE itemsupplies set quantity = ? where shipnode = ? and itemid = ? and type = ? and productclass = ?");
		//insert into customers table
		insert_customer_stmt    = session.prepare("INSERT INTO customers (username, firstname, lastname, shipnode, orderid) VALUES (?,?,?,?,?)");
	}
	
	/**
	 * Uses Api Endpoint from Android App to send messages
	 * @param msg
	 */
	public void sendNotification(String msg) {
//		try {
//			String url = "https://fcm.googleapis.com/fcm/send";
//			URL obj = new URL(url);
//			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
//
//			//add reuqest header
//			con.setRequestMethod("POST");
//		    con.setRequestProperty ("Authorization", "AAAA7_yjI88:APA91bHetBda99uKP-arCJVi6fIM513R2WSigAFvqUxPuD3a47-Y9CzDGJveDgemRtAP_vv8v5SzM_GHwWhRINzlogIqzVXeDiJrwWhgNNO4JvkaTpQpnTm0uDs6Qx2nCI9wThUGvefY");
//			JSONObject o = new JSONObject();
//			o.put("to", "c5iDvD5DPkY:APA91bHrwWeG5F4BgJ4DuxqUhaaPqy9pMDmUnUd0jT_8lsETRKEDzU3_y5DnqTQI08uy0EfvlXyqyJ_KDtXp1tWZRQ2kFBF-7VOF806wfslWy-2VoxFxOxDYCsjNxHd9ElCWl3sK9tQM");
//			o.put("body", msg);
//			o.put("title", msg);
//			// Send post request
//			con.setDoOutput(true);
//			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//			wr.writeBytes(msg);
//			wr.flush();
//			wr.close();
//		}
//		catch(Exception e) {
//			System.out.println(e);
//		}
		
//		try {
//			AutomatedTelnetClient telnet = new AutomatedTelnetClient(msg);
//			//telnet.sendCommand(“ps -ef “);
//			telnet.disconnect();
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
	}
	
	/**
	 * Sets the demand_type to CUSTOMER_COMING
	 * @param id
	 */
	public void customerComing(int id) {
		session.execute(customer_coming_stmt.bind(id));
	}
	//username and check status of order id and update 
	//list all orders with customer
	/**
	 * Sets the demand_type to CUSTOMER_READY and sends notification
	 * @param id
	 */
	public void customerReady(int id) {
		session.execute(customer_ready_stmt.bind(id));
		sendNotification("A store clerk will be with you shortly");
	}
	
	/**
	 * Sets delivery date and sets demand_type to COMPLETE_ORDER and sends notification
	 * @param id
	 */
	public void pickedUp(int id) {
		session.execute(complete_stmt.bind(id));
		session.execute(update_ddate_stmt.bind(generateDate(), id));
		sendNotification("Thank you for shopping with us");
	}
	
	/**
	 * Iterates through orders and gets the number of days for orders to be completely/partially completed
	 * @param x
	 * @return JSONArray of demand_type, number of days, id
	 */
	public JSONArray fulfillDate(int x) {
		JSONArray data = new JSONArray();
		int maxid = session.execute(get_max_allid.bind()).one().getInt("maxid");
		for(com.datastax.driver.core.Row order:session.execute(get_limitorder_list.bind(maxid - x))) {
			String date = order.getString("date");
			if(date.length() == 0) {
				date = generateDate();
			}
			String delivery_date = order.getString("delivery_date");
			if(delivery_date.length() == 0) {
				delivery_date = generateDate();
			}
			int days = calculateDifference(date, delivery_date);
			JSONObject d = new JSONObject();
			d.put("id", order.getInt("id"));
			d.put("demand_type", order.getString("demand_type"));
			d.put("days", days);
			data.put(d);
	    }
		return data;
	}
	
	 /**
	  * Finds difference in days
	  * @param d1
	  * @param d2
	  * @return difference in days
	  */
	private int calculateDifference(String d1, String d2) {
		LocalDate date = LocalDate.parse(d1, DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate delivery_date = LocalDate.parse(d2, DateTimeFormatter.ISO_LOCAL_DATE);
		Duration diff = Duration.between(date.atStartOfDay(), delivery_date.atStartOfDay());
		return (int)diff.toDays();
	}
	
	/**
	 * Completes Orders marked as ALLOCATE_ORDER and current date with stock available 
	 */
	public void completeOrder() {
	    //Completes all possible orders of demand_type ALLOCATE_ORDER/PARTIAL_ORDER
		for(com.datastax.driver.core.Row order:session.execute(set_final_stmt.bind())) {
			handleOrder(order);
	    }
		for(com.datastax.driver.core.Row order:session.execute(set_finalPartial_stmt.bind())) {
			handleOrder(order);
	    }
	}
	
	/**
	 * Handles reservations and removes from stock
	 * @param id
	 */
	public void handleReservation(int id) {
		Row order = session.execute(summarize_order_stmt.bind(id)).one();
		String shipnode = order.getString("shipnode");
		Map<Integer, Integer> quantity = order.getMap("quantity", Integer.class, Integer.class);
		for(int itemid : quantity.keySet()) {
			//gets quantity needed
			int q = quantity.get(itemid);
			//checks onhand supply to fill items
			Row i = session.execute(get_availableR_stmt.bind(itemid, "reserved", shipnode)).one(); 
			int stock = i.getInt("quantity");
			session.execute(update_stock_stmt.bind(stock - q, i.getString("shipnode"), itemid, "reserved", i.getString("productclass")));
		}
		session.execute(update_fulfilled_stmt.bind(quantity, order.getInt("id")));
		session.execute(complete_stmt.bind(order.getInt("id")));
		session.execute(update_ddate_stmt.bind(generateDate(), order.getInt("id")));
	}
	
	/**
	 * Handle orders and check if fillable
	 * @param order
	 */
	private void handleOrder(com.datastax.driver.core.Row order) {
		//checks if anything can be filled
		boolean fillable = checkFillable(order);
		//removes stock from supplies and marks order as complete
		if(fillable) {
			fillOrder(order);
		}
		//case that order can no longer be filled at all
		else if(!order.getString("demand_type").equals("PARTIAL_ORDER")) { 
			session.execute(reopen_stmt.bind(order.getInt("id")));
		}
	}
	
	/**
	 * Generate todays date in yyyy-MM-dd
	 * @return String
	 */
	private String generateDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	    Date date = new Date();
	    String d = formatter.format(date);
	    return d;
	}
	
	/**
	 * Gets list of customer orders
	 */
	public JSONArray customerOrders(String username) {
		JSONArray orders = new JSONArray();
		for(Row order:session.execute(customer_orders_list.bind(username))) {
			JSONObject orderJson = new JSONObject();
			addToObject(order, orderJson);
			orders.put(orderJson);
		}
		return orders;
	}
	
	
	/**
	 * Subtracts from itemsupplies and completes
	 * @param order
	 */
	private void fillOrder(com.datastax.driver.core.Row order) {
		//assumes not only partially complete
		boolean partial = false;
		//gets ordertype to look at safety stock
		String ordertype = order.getString("ordertype").toLowerCase();
		//gets needed quantity for order
		Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);				
		//gets num of each item already filled
		Map<Integer,Integer> itemsF = order.getMap("fulfilled", Integer.class, Integer.class);
		//goes through all requested items
		for(int itemid : items.keySet()) {
			//gets quantity needed
			int q = items.get(itemid) - itemsF.get(itemid);
			int numF = itemsF.get(itemid);
			//checks onhand supply to fill items
			for(Row i: session.execute(get_available_stmt.bind(itemid, "onhand"))) {
				//only if items are needed
				if(ordertype.equals("pickup") && !order.getString("shipnode").equals(i.getString("shipnode"))) {
					continue;
				}
				int stock = i.getInt("quantity");
				//case that shipnode has more than necessary
				if(stock >= q) {
					session.execute(update_stock_stmt.bind(stock - q, i.getString("shipnode"), itemid, i.getString("type"), i.getString("productclass")));
					numF += q;
					q = 0;
					break;
				}
				//need to check more shipnodes
				else {
					q = q - stock;
					numF += stock;
					session.execute(update_stock_stmt.bind(0, i.getString("shipnode"), itemid, i.getString("type"), i.getString("productclass")));
				}
				if(!(q>0)) {
					break;
				}
			}
			for(Row i: session.execute(get_available_stmt.bind(itemid, ordertype))) {
				//only if in correct shipnode
				if(!((ordertype.equals("pickup") && i.getString("shipnode").toLowerCase().equals(order.getString("shipnode").toLowerCase())) ||
					ordertype.equals("ship"))) {
					continue;
				}
				//get available stock
				int stock = i.getInt("quantity");
				//case that first shipnode has more than necessary
				if(stock >= q) {
					session.execute(update_stock_stmt.bind(stock - q, i.getString("shipnode"), itemid, i.getString("type"), i.getString("productclass")));
					numF += q;
					q = 0;
					break;
				}
				//need to check more shipnodes
				else {
					numF += stock;
					q = q - stock;
					session.execute(update_stock_stmt.bind(0, i.getString("shipnode"), itemid, i.getString("type"), i.getString("productclass")));
				}
				if(!(q>0)){break;}
				}
			if(q > 0) {
			    partial = true;
				session.execute(partial_stmt.bind(order.getInt("id")));
			}
			itemsF.put(itemid, numF);
		}
		System.out.println(itemsF);
		//change status of order to complete if it was not only fulfilled partially
		session.execute(update_fulfilled_stmt.bind(itemsF, order.getInt("id")));
		if(!partial && order.getString("ordertype").equalsIgnoreCase("pickup")) {
			session.execute(ready_pickup_stmt.bind(order.getInt("id")));
			sendNotification("Order " + order.getInt("id") + " is ready for pickup");
		}
		else if(!partial) {
			session.execute(complete_stmt.bind(order.getInt("id")));
			session.execute(update_ddate_stmt.bind(generateDate(), order.getInt("id")));
		}
		else if(order.getString("delivery_date").length() == 0) {
			session.execute(update_ddate_stmt.bind(generateDate(), order.getInt("id")));
		}
	}
	
	/**
	 * Check if an order is fillable
	 * @param order
	 * @return boolean
	 */
	private boolean checkFillable(com.datastax.driver.core.Row order) {
		//gets ordertype to look at safety stock
		String ordertype = order.getString("ordertype").toLowerCase();
		//gets needed quantity for order
		Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
		//gets num of each item already filled
		Map<Integer,Integer> itemsF = order.getMap("fulfilled", Integer.class, Integer.class);
				
		//Ensures at least one item that has not been completely filled is in stock
		return checkItemsFillable(items, itemsF, ordertype, order.getString("shipnode"));
	}
	
	/**
	 * check if an order is reservable
	 * @param items
	 * @param shipnode
	 * @return boolean
	 */
	private boolean checkReservable(Map<Integer,Integer> items, String shipnode) {		
		for(int itemid : items.keySet()) {
			int available = session.execute(get_quantityR_stmt.bind("onhand", itemid, shipnode)).one().getInt("total"); 
			if(available < items.get(itemid)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check if items are available given restrictions
	 * @param items
	 * @param itemsF
	 * @param ordertype
	 * @param shipnode
	 * @return boolean
	 */
	private boolean checkItemsFillable(Map<Integer,Integer> items, Map<Integer,Integer> itemsF, String ordertype, String shipnode) {
		Boolean fillable = false;
		for(int itemid : items.keySet()) {
			int available;
			if(ordertype.equalsIgnoreCase("pickup")) {
				available = session.execute(get_quantityR_stmt.bind("onhand", itemid, shipnode)).one().getInt("total") + 
						session.execute(get_quantityR_stmt.bind(ordertype, itemid, shipnode)).one().getInt("total");

			}
			else {
				available = session.execute(get_quantity_stmt.bind("onhand", itemid)).one().getInt("total") + 
						session.execute(get_quantity_stmt.bind(ordertype, itemid)).one().getInt("total");
			}
			if(available > 0 && (itemsF.get(itemid) < items.get(itemid))) {
					fillable = true;
					break;
			}
		}
		return fillable;
	}
	
	/**
	 * Sets delivery_date of specific order and sets demand_type to ALLOCATE_ORDER
	 * @param json
	 */
	public void fulfill(JSONObject json) {
		session.execute(set_fulfill_stmt.bind(json.getInt("id")));	
	}
	
	/**
	 * Assigns OPEN_ORDERS with stock as SCHEDULE_ORDER
	 */
	public void scheduleOrders() {
		//iterates through all OPEN_ORDERs
		for(Row order:session.execute(get_open_list.bind())) {
			//assumes cannot fill an order until it sees one item is available
			Boolean fillable = false;
			//gets ordertype in order to discriminate between safety stocks
			String ordertype = order.getString("ordertype").toLowerCase();
			//looks for total quantity needed to see quanitity of each item needed
			//this quantity map preserves quantity and does not consider fulfilled
			Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
			//gets fulfilled details
			Map<Integer,Integer> itemsFulfilled = order.getMap("fulfilled", Integer.class, Integer.class);
			//iterates through all items and quantities required in the order
			for(int itemid : items.keySet()) {
				//gets total stock available from total and safety
				int available = session.execute(get_quantity_stmt.bind("onhand", itemid)).one().getInt("total") + 
						session.execute(get_quantity_stmt.bind(ordertype, itemid)).one().getInt("total");
				//checks that there are items available and that there are items left to be fulfilled
				if((available > 0) && (itemsFulfilled.get(itemid) < items.get(itemid))) {
					fillable = true;
				}
				//schedules the order if even one item can be scheduled
				if(fillable) {
					session.execute(set_schedule_stmt.bind(order.getInt("id")));
					//allows quickly moving to schedule more orders
					break;
				}
			}
		}
	}
	
	/**
	 * 
	 * @param itemid
	 * @return String short description of the item
	 */
	public String getShortDescription(int itemid) {
		for (Row row :session.execute(get_shortdescri_stmt.bind(itemid))) {
			JSONObject jsonRow = new JSONObject();	
			return row.getString("shortdescription");
		}
		return "";
	}
	
	/**
	 * Generate new orderid
	 * @return int orderid
	 */
	private int getOrderId() {
		Row row = session.execute(select_next_id.bind()).one();
		int id = row.getInt("next");
		//updates id in database
		session.execute(inc_id_stmt.bind(id+1));
		return id;
	}
	
	/**
	 * Initiates generating a new orderid and starts updating table
	 * @param json
	 */
	public boolean createOrder(JSONObject json) {
		return insertOrderIntoDb(getOrderId(), json);
	}
	
	/**
	 * Gets the MSRP of an item
	 * @param itemid
	 * @return int price
	 */
	private int getPrice(int itemid) {
		Row row = session.execute(get_price_stmt.bind(itemid)).one();
		int price = row.getInt("price");
		return price;
	}
	
	/**
	 * @param id
	 * @return JSONObject consisting of order details
	 */
	public JSONObject getSummary(int id) {
		JSONArray orders = new JSONArray();
		JSONObject orderJson = new JSONObject();

		Row order = session.execute(summarize_order_stmt.bind(id)).one(); 
		addToObject(order, orderJson);
		return orderJson;
	}
	
	/**
	 * 
	 * @return  JSONArray with JSONObjects representing each completed order
	 */
	public JSONArray listCompletedOrders(int numRows) {
		JSONArray orders = new JSONArray();
		int max = session.execute(get_max_completeid.bind()).one().getInt("maxid");
		int start = max - numRows;
		if(start > max) {
			start = 0;
		}
		for(Row order:session.execute(get_completed_list.bind(start))) {
			JSONObject orderJson = new JSONObject();
			addToObject(order, orderJson);
			orders.put(orderJson);
		}
		return orders;
	}
	
	/**
	 * 
	 * @return JSONArray with JSONObjects representing each order
	 */
	public JSONArray listOrders() {
		JSONArray orders = new JSONArray();
		for(Row order:session.execute(list_allorders_stmt.bind())) {
			JSONObject orderJson = new JSONObject();
			addToObject(order, orderJson);
			orders.put(orderJson);
		}
		return orders;
	}
	
	/**
	 * Helper method that adds fields regarding a specific order to an orderJson object
	 * @param order
	 * @param orderJson
	 */
	private void addToObject(Row order, JSONObject orderJson) {
		String[] strColumns = {"address","username","channel","city","date"
				,"firstname","lastname","payment","state","zip","demand_type", "ordertype", "shipnode"};
		String[] intColumns = {"id","total"};
		orderJson.put("id", order.getInt("id"));
		//populate json object columns
		for(String colName:strColumns)
			orderJson.put(colName, order.getString(colName));
		for(String colName:intColumns)
			orderJson.put(colName, order.getInt(colName));
		
		//build items json Array
		JSONArray itemsJson = new JSONArray();
		Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
		Map<Integer,Integer> itemsP = order.getMap("price", Integer.class, Integer.class);
		Map<Integer, Integer> itemsFulfilled = order.getMap("fulfilled", Integer.class, Integer.class);

		for(Integer itemid:items.keySet()) {
			JSONObject item = new JSONObject();
			item.put("itemid",itemid);
			int quantity = items.get(itemid);
			int MSRPprice = getPrice(itemid);
			int numFulfilled = itemsFulfilled.get(itemid);
			item.put("quantity",quantity);
			item.put("fulfilled", numFulfilled);
			item.put("MSRPprice", MSRPprice);
			item.put("shortdescription", getShortDescription(itemid));
			item.put("MSRPsubtotal", quantity * MSRPprice);
			int paidPrice = itemsP.get(itemid);
			item.put("price", paidPrice);
			itemsJson.put(item);
		}
		orderJson.put("items",itemsJson);
	}
	
	/**
	 * insert order info into db
	 * @param id
	 * @param json
	 */
	private boolean insertOrderIntoDb(int id, JSONObject json) {
		//retrieves information from JSONObject
		String channel   = json.getString("channel");
		String username  = json.getString("username");
		String date      = json.getString("date").substring(0,10);
		String firstname = json.getString("firstname");
		String lastname  = json.getString("lastname");
		String city      = json.getString("city");
		String state     = json.getString("state");
		String zip       = json.getString("zip");
		String payment   = json.getString("payment");
		String address   = json.getString("address");
		String shipnode  = json.getString("shipnode");
		//pickup,ship,reservation
		String ordertype = json.getString("ordertype");
		int total        = json.getInt("total"); 
		JSONArray itemsQuantities = json.getJSONArray("quantity");
		//populates map with itemid and quantity
		Map<Integer,Integer> mapQ = new HashMap<>();
		for(int i = 0; i < itemsQuantities.length();i ++) {
			JSONObject item = itemsQuantities.getJSONObject(i);
			if(mapQ.containsKey(item.getInt("itemid"))) {
				mapQ.put(item.getInt("itemid"), mapQ.get(item.getInt("itemid")) + item.getInt("quantity"));
			}
			else {
				mapQ.put(item.getInt("itemid"),item.getInt("quantity"));		
			}
		}
		
		//populates map with itemid and quantity fulfilled
		//OK to still use this when updating bc partially fulfilled cannot be updated
		Map<Integer,Integer> mapFulfilled = new HashMap<>();
		for(int i = 0; i < itemsQuantities.length();i ++) {
			JSONObject item = itemsQuantities.getJSONObject(i);
			if(!mapFulfilled.containsKey(item.getInt("itemid"))) {
				//all orders initially have no items fulfilled
				mapFulfilled.put(item.getInt("itemid"),0);		
			}
		}
		
		//populates map with itemid and quantity
		JSONArray itemsPrices = json.getJSONArray("price");
		Map<Integer,Integer> mapP = new HashMap<>();
		for(int i = 0; i < itemsPrices.length();i ++) {
			JSONObject item = itemsPrices.getJSONObject(i);
			if(mapP.containsKey(item.getInt("itemid"))) {
				mapP.put(item.getInt("itemid"), mapP.get(item.getInt("itemid")) + item.getInt("price"));
			}
			else {
				mapP.put(item.getInt("itemid"),item.getInt("price"));		
			}
		}
		
		String demand_type;
		if(ordertype.toLowerCase().equals("reservation")) {
			demand_type = "RESERVED_ORDER";
			boolean canReserve = checkReservable(mapQ, shipnode);
			if(!canReserve) {
				return false;
			}
			else {
				reserveSupply(mapQ, shipnode);
			}
		}
		else {
			demand_type = "OPEN_ORDER";
		}
		
		//inserts order into db
		System.out.println("date: " + date);
		session.execute(create_order_stmt.bind(id,username,channel,date,firstname,lastname,city,state,zip,payment,total,address,mapQ,mapP,demand_type,shipnode,ordertype,mapFulfilled,""));
		session.execute(insert_customer_stmt.bind(username, firstname, lastname, shipnode, id));
		return true;
	}
	
	private void reserveSupply(Map<Integer, Integer> quantity, String shipnode) {
		for(int itemid : quantity.keySet()) {
			//gets quantity needed
			int q = quantity.get(itemid);
			//checks onhand supply to fill items
			for(Row i: session.execute(get_availableR_stmt.bind(itemid, "onhand", shipnode))) {
				Row r = session.execute(get_reserved_num.bind(itemid, i.getString("shipnode"))).one();
				int already = 0;
				if(r == null) {
					already = 0;
				}
				else {
					already = r.getInt("total");
				}
				int stock = i.getInt("quantity");
				//case that shipnode has more than necessary
				if(stock >= q) {
					session.execute(update_stock_stmt.bind(stock - q, i.getString("shipnode"), itemid, "onhand", i.getString("productclass")));
					session.execute(update_stock_stmt.bind(already + q, i.getString("shipnode"), itemid, "reserved", i.getString("productclass")));
					q = 0;
					break;
				}
				//need to check more shipnodes
				else {
					q = q - stock;
					session.execute(update_stock_stmt.bind(0, i.getString("shipnode"), itemid, "onhand", i.getString("productclass")));
					session.execute(update_stock_stmt.bind(already + stock, i.getString("shipnode"), itemid, "reserved", i.getString("productclass")));

				}
				if(!(q>0)) {
					break;
				}
			}
		}
	}

	/**
	 * Generates new id number and inserts order into db
	 * @param json
	 */
	public void updateOrder(JSONObject json) {
		int id = json.getInt("id");
		insertOrderIntoDb(id,json);
	}
	
	
}
