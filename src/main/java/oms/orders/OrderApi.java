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
	select_next_id,inc_id_stmt,list_items_stmt, get_info_stmt, get_price_stmt, get_id_stmt;

	public OrderApi() {
		super();
		session = super.getSession();
		create_order_stmt = session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,items,demand_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'OPEN_ORDER')");
		is_admin_stmt     = session.prepare("SELECT isAdmin from users where username = ?");
		select_next_id    = session.prepare("SELECT next from order_id");
		inc_id_stmt       = session.prepare("UPDATE order_id set next = ? where id ='id'");
		list_items_stmt   = session.prepare("SELECT * from orders");
		get_info_stmt     = session.prepare("SELECT * from orders where id = ?");
		get_price_stmt    = session.prepare("SELECT price from items where itemid = ? allow filtering");
		get_id_stmt       = session.prepare("SELECT itemid from items where shortdescription = ? allow filtering");
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
	
	public JSONObject getSummary(int id) {
	JSONArray orders = new JSONArray();
	JSONObject orderJson = new JSONObject();

		for(Row order:session.execute(get_info_stmt.bind(id))) {
			//build order json for each row 
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
			Map<String,Integer> items = order.getMap("items", String.class, Integer.class);
			for(String itemid:items.keySet()) {
				System.out.println(items);
				JSONObject item = new JSONObject();
				id = getid(itemid);
				item.put("itemid",id);
				int quantity = items.get(itemid);
				int price = getPrice(id);
				item.put("quantity",quantity);
				item.put("price", price);
				item.put("shortdescription", itemid);
				item.put("subtotal", quantity * price);
				itemsJson.put(item);
			}
			orderJson.put("items",itemsJson);
			return orderJson;
		}
		return orderJson;
	}
	
	private int getid(String itemName) {
		Row row = session.execute(get_id_stmt.bind(itemName)).one();
		return row.getInt("itemid");
	}
	
	public boolean isAdmin(String user) {
		ResultSet res = session.execute(is_admin_stmt.bind(user));
		Row row = res.one();
		if (row == null) {
			return false;
		}
		return row.getBool("isadmin");
	}

	//needs pagination
	public JSONArray listOrders() {
		JSONArray orders = new JSONArray();
		JSONObject orderJson = new JSONObject();

		for(Row order:session.execute(list_items_stmt.bind())) {
			//build order json for each row 
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
			Map<String,Integer> items = order.getMap("items", String.class, Integer.class);
			for(String itemid:items.keySet()) {
				System.out.println(items);
				JSONObject item = new JSONObject();
			int	id	=getid(itemid);
				item.put("itemid",id);
				int quantity = items.get(itemid);
				int price = getPrice(id);
				item.put("quantity",quantity);
				item.put("price", price);
				item.put("shortdescription", itemid);
				item.put("subtotal", quantity * price);
				itemsJson.put(item);
			}
			orderJson.put("items",itemsJson);
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
		JSONArray items = json.getJSONArray("items");
		Map<Integer,Integer> map = new HashMap<>();
		for(int i =0; i < items.length();i ++) {
			JSONObject item = items.getJSONObject(i);
			if(map.containsKey(item.getInt("itemid"))) {
				map.put(item.getInt("itemid"), map.get(item.getInt("itemid")) + item.getInt("quantity"));
			}
			else {
				map.put(item.getInt("itemid"),item.getInt("quantity"));		

			}
		}
		session.execute(create_order_stmt.bind(id,channel,date,firstname,lastname,city,state,zip,payment,total,address,map));
		
		
	}

	public void updateOrder(JSONObject json) {
		int id           = json.getInt("id");
		insertOrderIntoDb(id,json);
	}
}
