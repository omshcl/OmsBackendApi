package oms.orders;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import oms.Api;

public class OrderApi extends Api {
	
	private Session session;
	private PreparedStatement create_order_stmt, is_admin_stmt,
	select_next_id,inc_id_stmt,list_items_stmt, get_info_stmt;

	public OrderApi() {
		super();
		session = super.getSession();
		create_order_stmt = session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,items) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
		is_admin_stmt     = session.prepare("SELECT isAdmin from users where username = ?");
		select_next_id    = session.prepare("SELECT next from order_id");
		inc_id_stmt       = session.prepare("UPDATE order_id set next = ? where id ='id'");
		list_items_stmt   = session.prepare("SELECT * from orders");
		get_info_stmt     = session.prepare("SELECT * from orders where id = ?");
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
	
	public JSONArray getSummary(int id) {
	JSONArray orders = new JSONArray();
		
		for(Row order:session.execute(get_info_stmt.bind(id))) {
			//build order json for each row 
			JSONObject orderJson = new JSONObject();
			String[] strColumns = {"address","channel","city","date"
					,"firstname","lastname","payment","state","zip"};
			String[] intColumns = {"id","total"};
			orderJson.put("id", order.getInt("id"));
			//populate json object columns
			for(String colName:strColumns)
				orderJson.put(colName, order.getString(colName));
			for(String colName:intColumns)
				orderJson.put(colName, order.getInt(colName));
			
			//build items json Array
			JSONArray itemsJson = new JSONArray();
			Map<String,Integer> items = order.getMap("items", String.class, Integer.class);
			for(String itemName:items.keySet()) {
				JSONObject item = new JSONObject();
				item.put("id",itemName);
				item.put("quanity",items.get(itemName));
				itemsJson.put(item);
			}
			orderJson.put("items",items);
			orders.put(orderJson);
		}
		return orders;
	}
	
	public boolean isAdmin(String user) {
		ResultSet res = session.execute(is_admin_stmt.bind(user));
		Row row = res.one();
		if (row == null) {
			return false;
		}
		return row.getBool("isadmin");
	}

	public JSONArray listOrders() {
		JSONArray orders = new JSONArray();
		
		for(Row order:session.execute(list_items_stmt.bind())) {
			//build order json for each row 
			JSONObject orderJson = new JSONObject();
			String[] strColumns = {"address","channel","city","date"
					,"firstname","lastname","payment","state","zip"};
			String[] intColumns = {"id","total"};
			orderJson.put("id", order.getInt("id"));
			//populate json object columns
			for(String colName:strColumns)
				orderJson.put(colName, order.getString(colName));
			for(String colName:intColumns)
				orderJson.put(colName, order.getInt(colName));
			
			//build items json Array
			JSONArray itemsJson = new JSONArray();
			Map<String,Integer> items = order.getMap("items", String.class, Integer.class);
			for(String itemName:items.keySet()) {
				JSONObject item = new JSONObject();
				item.put("id",itemName);
				item.put("quanity",items.get(itemName));
				itemsJson.put(item);
			}
			orderJson.put("items",items);
			orders.put(orderJson);
		}
		return orders;
	}


	
	private void insertOrderIntoDb(int id, JSONObject json) {
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
	
		//extract item name and quanity from request
		JSONArray items = json.getJSONArray("items");
		Map<String,Integer> map = new HashMap<>();
		for(int i =0; i < items.length();i ++) {
			JSONObject item = items.getJSONObject(i);
			map.put(item.getString("item"),item.getInt("quantity"));		
		}
		session.execute(create_order_stmt.bind(id,channel,date,firstname,lastname,city,state,zip,payment,total,address,map));
		
		
	}

	public void updateOrder(JSONObject json) {
		int id           = json.getInt("id");
		insertOrderIntoDb(id,json);
	}
}
