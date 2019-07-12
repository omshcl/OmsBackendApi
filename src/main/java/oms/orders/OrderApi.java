package oms.orders;

import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;  
import java.util.Date; 
import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import oms.Api;

public class OrderApi extends Api {
	
	private Session session;
	private PreparedStatement create_order_stmt,
	select_next_id,inc_id_stmt,list_items_stmt, get_info_stmt, get_price_stmt, get_shortdescri_stmt,get_completed_list,get_open_list, get_quantity_stmt, set_schedule_stmt,
	set_fulfill_stmt, set_final_stmt, reopen_stmt, complete_stmt, get_available_stmt, update_stock_stmt, get_max_orderid, partial_stmt;

	public OrderApi() {
		super();
		session = super.getSession();
		//Insert into the orders table
		create_order_stmt 		= session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,quantity,price,demand_type,shipnode,ordertype,fulfilled) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		//Selects the next order id
		select_next_id   		= session.prepare("SELECT next from order_id");
		//Gets entire order table
		list_items_stmt   		= session.prepare("SELECT * from orders");
		//Retrieves row from order table about specific order
		get_info_stmt     		= session.prepare("SELECT * from orders where id = ?");
		//Finds maximum order id
		get_max_orderid			= session.prepare("SELECT max(id) as maxid from orders where demand_type = 'COMPLETE_ORDER' allow filtering");
		//Selects COMPLETE_ORDERs
		get_completed_list 		= session.prepare("SELECT * from orders where demand_type = 'COMPLETE_ORDER' and id > ? allow filtering");
		//Selects orders that are OPEN_ORDERs
		get_open_list			= session.prepare("SELECT * from orders where demand_type = 'OPEN_ORDER' allow filtering");
		//Selects ALLOCATE_ORDERs of certain delivery date to prepare to complete
		set_final_stmt	 		= session.prepare("SELECT * from orders where demand_type = 'ALLOCATE_ORDER' or demand_type = 'PARTIAL_ORDER' allow filtering");
		//Generates new order ids
		inc_id_stmt      		= session.prepare("UPDATE order_id set next = ? where id ='id'");
		//Sets an order to SCHEDULE_ORDER
		set_schedule_stmt 		= session.prepare("UPDATE orders set demand_type = 'SCHEDULE_ORDER' where id = ?");
		//Sets an order to ALLOCATE_ORDER and sets delivery_date
		set_fulfill_stmt 		= session.prepare("UPDATE orders set demand_type = 'ALLOCATE_ORDER' where id = ?");
		//Resets a specific order to OPEN_ORDER status
		reopen_stmt 			= session.prepare("UPDATE orders set demand_type = 'OPEN_ORDER' where id = ?");
		//Completes a specific order
		complete_stmt 			= session.prepare("UPDATE orders set demand_type = 'COMPLETE_ORDER' where id = ?");
		//Partial a specific order
		partial_stmt 			= session.prepare("UPDATE orders set demand_type = 'PARTIAL_ORDER' where id = ?");
		//Selects all stock of a certain item
		get_available_stmt 		= session.prepare("SELECT * from itemsupplies where itemid = ? and type = ? and productclass = 'new' allow filtering");
		//changes stock of item
		update_stock_stmt 		= session.prepare("UPDATE itemsupplies set quantity = ? where shipnode = ? and itemid = ? and type = ? and productclass = ?");
		//Finds the number of new, onhand stock of a certain item
		get_quantity_stmt 		= session.prepare("SELECT sum(quantity) as total from itemsupplies where type = ? and itemid = ? and productclass = 'new' allow filtering");
		//Gets price of a specific item
		get_price_stmt    		= session.prepare("SELECT price from items where itemid = ? allow filtering");
		//Gets not necessarily unique shortdescription for specific itemid
		get_shortdescri_stmt 	= session.prepare("SELECT shortdescription from items where itemid = ?");
	}

	/**
	 * Completes Orders marked as ALLOCATE_ORDER and current date with stock available 
	 * TODO: IN PROGRESS: NEED TO MANAGE FULFILL AND TEST
	 */
	public void completeOrder() {session.execute(complete_stmt.bind(order.getInt("id")));
		//identifies today's date in MM/dd/yyyy format
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	    Date date = new Date();
	    String d = formatter.format(date);
	    
	    //Completes all possible orders of demand_type ALLOCATE_ORDER/PARTIAL_ORDER
		for(com.sun.rowset.internal.Row order:session.execute(set_final_stmt.bind())) {
			Boolean fillable = false;
			String ordertype = order.getString("ordertype").toLowerCase();
			Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
			Map<Integer,Integer> itemsF = order.getMap("fulfilled", Integer.class, Integer.class);

			//Ensures at least one item is in stock
			for(int itemid : items.keySet()) {
				int available = session.execute(get_quantity_stmt.bind("onhand", itemid)).one().getInt("total") + 
						session.execute(get_quantity_stmt.bind(ordertype, itemid)).one().getInt("total");
				if(available > 0 && (itemsF.get(itemid) < items.get(itemid))) {
					fillable = true;
					break;
				}
			}
			
			//removes stock from supplies and marks order as complete
			if(fillable) {
				boolean partial = false;
				for(int itemid : items.keySet()) {
					int q = items.get(itemid);
					int numF = 0;
					for(Row i: session.execute(get_available_stmt.bind(itemid, "onhand"))) {
						//only if items are needed
						if(q > 0) {
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
								q = q - stock;
								numF += stock;
								session.execute(update_stock_stmt.bind(0, i.getString("shipnode"), itemid, i.getString("type"), i.getString("productclass")));
							}
						}
						else {
							break;
						}
					}
					//remaining quantity handling
					if(q > 0) {
						for(Row i: session.execute(get_available_stmt.bind(itemid, ordertype))) {
							//only if items are needed
							if(!((ordertype.equals("pickup") && i.getString("shipnode").toLowerCase().equals(order.getString("shipnode").toLowerCase())) ||
									ordertype.equals("ship"))) {
								continue;
							}
							if(q > 0) {
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
							}
							else {
								break;
							}
						}
					}
					if(q > 0) {
						partial = true;
						session.execute(partial_stmt.bind(order.getInt("id")));
					}
					//TODO: update fulfilled to itemid:numF
				}
				//change status of order to complete if it was not only fulfilled partially
				if(!partial) {
					session.execute(complete_stmt.bind(order.getInt("id")));
				}
			}
			//case that order can no longer be filled at all
			else { 
				session.execute(reopen_stmt.bind(order.getInt("id")));
				 }
	    }
	
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
	 * TODO: CHECK SAFETY AND ORDERTYPE
	 */
	public void scheduleOrders() {
		for(Row order:session.execute(get_open_list.bind())) {
			Boolean fillable = false;
			String ordertype = order.getString(ordertype).toLowerCase();
			Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
			for(int itemid : items.keySet()) {
				int available = session.execute(get_quantity_stmt.bind("onhand", itemid)).one().getInt("total") + 
						session.execute(get_quantity_stmt.bind(ordertype, itemid)).one().getInt("total");
				if(available > 0) {
					fillable = true;
				}
				if(fillable) {
					session.execute(set_schedule_stmt.bind(order.getInt("id")));
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
	public void createOrder(JSONObject json) {
		insertOrderIntoDb(getOrderId(), json);
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

		Row order = session.execute(get_info_stmt.bind(id)).one(); 
		addToObject(order, orderJson);
		return orderJson;
	}
	
	/**
	 * 
	 * @return  JSONArray with JSONObjects representing each completed order
	 */
	public JSONArray listCompletedOrders(int numRows) {
		JSONArray orders = new JSONArray();
		int max = session.execute(get_max_orderid.bind()).one().getInt("maxid");
		int start = max - numRows;
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
		for(Row order:session.execute(list_items_stmt.bind())) {
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
		String[] strColumns = {"address","channel","city","date"
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
	 * TODO:LOGIC FOR RESERVATION
	 * @param id
	 * @param json
	 */
	private void insertOrderIntoDb(int id, JSONObject json) {
		//retrieves information from JSONObject
		String channel   = json.getString("channel");
		String date      = json.getString("date");
		String firstname = json.getString("firstname");
		String lastname  = json.getString("lastname");
		String city      = json.getString("city");
		String state     = json.getString("state");
		String zip       = json.getString("zip");
		String payment   = json.getString("payment");
		String address   = json.getString("address");
		String shipnode  = json.getString("shipnode");
		String ordertype = json.getString("ordertype");
		int total        = json.getInt("total"); 
		JSONArray itemsQuantities = json.getJSONArray("quantity");
		String demand_type;
		if(ordertype.toLowerCase().equals("reservation")) {
			demand_type = "RESERVED_ORDER";
		}
		else {
			demand_type = "OPEN_ORDER";
		}
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
		Map<Integer,Integer> mapFulfilled = new HashMap<>();
		for(int i = 0; i < itemsQuantities.length();i ++) {
			JSONObject item = itemsQuantities.getJSONObject(i);
			if(!mapFulfilled.containsKey(item.getInt("itemid"))) {
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
		
		//inserts order into db
		session.execute(create_order_stmt.bind(id,channel,date,firstname,lastname,city,state,zip,payment,total,address,mapQ,mapP,demand_type,shipnode,ordertype,mapFulfilled));
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
