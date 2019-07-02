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
	set_fulfill_stmt, set_final_stmt, reopen_stmt, complete_stmt, get_available_stmt, update_stock_stmt;

	public OrderApi() {
		super();
		session = super.getSession();
		//Insert into the orders table
		create_order_stmt 		= session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,quantity,price,demand_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,'OPEN_ORDER')");
		//Selects the next order id
		select_next_id   		= session.prepare("SELECT next from order_id");
		//Gets entire order table
		list_items_stmt   		= session.prepare("SELECT * from orders");
		//Retrieves row from order table about specific order
		get_info_stmt     		= session.prepare("SELECT * from orders where id = ?");
		//Selects COMPLETE_ORDERs
		get_completed_list 		= session.prepare("SELECT * from orders where demand_type = 'COMPLETE_ORDER' allow filtering");
		//Selects orders that are OPEN_ORDERs
		get_open_list			= session.prepare("SELECT * from orders where demand_type = 'OPEN_ORDER' allow filtering");
		//Selects ALLOCATE_ORDERs of certain delivery date to prepare to complete
		set_final_stmt	 		= session.prepare("SELECT * from orders where demand_type = 'ALLOCATE_ORDER' and delivery_date = ? allow filtering");
		//Generates new order ids
		inc_id_stmt      		= session.prepare("UPDATE order_id set next = ? where id ='id'");
		//Sets an order to SCHEDULE_ORDER
		set_schedule_stmt 		= session.prepare("UPDATE orders set demand_type = 'SCHEDULE_ORDER' where id = ?");
		//Sets an order to ALLOCATE_ORDER and sets delivery_date
		set_fulfill_stmt 		= session.prepare("UPDATE orders set demand_type = 'ALLOCATE_ORDER', delivery_date = ? where id = ?");
		//Resets a specific order to OPEN_ORDER status
		reopen_stmt 			= session.prepare("UPDATE orders set demand_type = 'OPEN_ORDER' where id = ?");
		//Completes a specific order
		complete_stmt 			= session.prepare("UPDATE orders set demand_type = 'COMPLETE_ORDER' where id = ?");
		//Selects all stock of a certain item
		get_available_stmt 		= session.prepare("SELECT * from itemsupplies where itemid = ? and type = 'onhand' and productclass = 'new' allow filtering");
		//changes stock of item
		update_stock_stmt 		= session.prepare("UPDATE itemsupplies set quantity = ? where shipnode = ? and itemid = ? and type = ? and productclass = ?");
		//Finds the number of new, onhand stock of a certain item
		get_quantity_stmt 		= session.prepare("SELECT sum(quantity) as total from itemsupplies where type = 'onhand' and itemid = ? and productclass = 'new' allow filtering");
		//Gets price of a specific item
		get_price_stmt    		= session.prepare("SELECT price from items where itemid = ? allow filtering");
		//Gets not necessarily unique shortdescription for specific itemid
		get_shortdescri_stmt 	= session.prepare("SELECT shortdescription from items where itemid = ?");
	}

	/**
	 * Completes Orders marked as ALLOCATE_ORDER and current date with stock available 
	 */
	public void completeOrder() {
		//identifies today's date in MM/dd/yyyy format
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");  
	    Date date = new Date();
	    String d = formatter.format(date);
	    
	    //Completes all possible orders of demand_type ALLOCATE_ORDER with today's delivery date 
		for(Row order:session.execute(set_final_stmt.bind(d))) {
			Boolean fillable = true;
			Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
			
			//ensures every item in the order is in stock
			for(int itemid : items.keySet()) {
				int num = items.get(itemid);
				int available = session.execute(get_quantity_stmt.bind(itemid)).one().getInt("total");
				if(num > available) {
					fillable = false;
					break;
				}
			}
			//removes stock from supplies and marks order as complete
			if(fillable) {
				session.execute(complete_stmt.bind(order.getInt("id")));
				for(int itemid : items.keySet()) {
					int q = items.get(itemid);
					for(Row i: session.execute(get_available_stmt.bind(itemid))) {
						//only if items are needed
						if(q > 0) {
							int stock = i.getInt("quantity");
							//case that first shipnode has more than necessary
							if(stock >= q) {
								session.execute(update_stock_stmt.bind(stock - q, i.getString("shipnode"), itemid, i.getString("type"), i.getString("productclass")));
								break;
							}
							//need to check more shipnodes
							else {
								q = q - stock;
								session.execute(update_stock_stmt.bind(0, i.getString("shipnode"), itemid, i.getString("type"), i.getString("productclass")));
							}
						}
						else {
							break;
						}
					}
				}
			}
			//case that order can no longer be filled
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
		session.execute(set_fulfill_stmt.bind(json.getString("delivery_date"), json.getInt("id")));	
	}
	
	/**
	 * Assigns OPEN_ORDERS with stock as SCHEDULE_ORDER
	 */
	public void scheduleOrders() {
		for(Row order:session.execute(get_open_list.bind())) {
			Boolean fillable = true;
			Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
			for(int itemid : items.keySet()) {
				int num = items.get(itemid);
				int available = session.execute(get_quantity_stmt.bind(itemid)).one().getInt("total");
				if(num > available) {
					fillable = false;
					break;
				}
			}
			if(fillable) {
				session.execute(set_schedule_stmt.bind(order.getInt("id")));
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
	
	private int getOrderId() {
		Row row = session.execute(select_next_id.bind()).one();
		int id = row.getInt("next");
		//updates id in database
		session.execute(inc_id_stmt.bind(id+1));
		return id;
	}
	
	public void createOrder(JSONObject json) {
		insertOrderIntoDb(getOrderId(), json);
	}
	
	private int getPrice(int itemid) {
		Row row = session.execute(get_price_stmt.bind(itemid)).one();
		int price = row.getInt("price");
		return price;
	}
	
	/**
	 * @param id
	 * @return JSONObject consisting of the address, channel, city, date, firstname, lastname, payment, state,
	 * demand_type, id, total
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
	public JSONArray listCompletedOrders() {
		JSONArray orders = new JSONArray();
		for(Row order:session.execute(get_completed_list.bind())) {
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
				,"firstname","lastname","payment","state","zip","demand_type"};
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

		for(Integer itemid:items.keySet()) {
			JSONObject item = new JSONObject();
			item.put("itemid",itemid);
			int quantity = items.get(itemid);
			int MSRPprice = getPrice(itemid);
			item.put("quantity",quantity);
			item.put("MSRPprice", MSRPprice);
			item.put("shortdescription", getShortDescription(itemid));
			item.put("MSRPsubtotal", quantity * MSRPprice);
			int paidPrice = itemsP.get(itemid);
			item.put("price", paidPrice);
			itemsJson.put(item);
		}
		orderJson.put("items",itemsJson);
	}
	
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
		session.execute(create_order_stmt.bind(id,channel,date,firstname,lastname,city,state,zip,payment,total,address,mapQ,mapP));
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
