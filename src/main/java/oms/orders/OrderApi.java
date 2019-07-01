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
	select_next_id,inc_id_stmt,list_items_stmt, get_info_stmt, get_price_stmt, get_id_stmt, get_shortdescri_stmt,get_completed_list,get_open_list, get_quantity_stmt, set_schedule_stmt;

	public OrderApi() {
		super();
		session = super.getSession();
		create_order_stmt = session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,quantity,price,demand_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,'OPEN_ORDER')");
		is_admin_stmt     = session.prepare("SELECT isAdmin from users where username = ?");
		select_next_id    = session.prepare("SELECT next from order_id");
		inc_id_stmt       = session.prepare("UPDATE order_id set next = ? where id ='id'");
		list_items_stmt   = session.prepare("SELECT * from orders");
		get_info_stmt     = session.prepare("SELECT * from orders where id = ?");
		get_price_stmt    = session.prepare("SELECT price from items where itemid = ? allow filtering");
		get_id_stmt       = session.prepare("SELECT itemid from items where shortdescription = ? allow filtering");
		get_shortdescri_stmt = session.prepare("SELECT shortdescription from items where itemid = ?");
		get_completed_list = session.prepare("SELECT * from orders where demand_type = 'COMPLETE_ORDER' allow filtering");
		get_open_list = session.prepare("SELECT * from orders where demand_type = 'OPEN_ORDER' allow filtering");	
		get_quantity_stmt = session.prepare("SELECT sum(quantity) as total from itemsupplies where type = 'onhand' and itemid = ? and productclass = 'new' allow filtering");
		set_schedule_stmt = session.prepare("update orders set demand_type = 'SCHEDULE_ORDER' where id = ?");
	}
	
	public void scheduleOrders() {
		for(Row order:session.execute(get_open_list.bind())) {
			Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
			for(int itemid : items.keySet()) {
				int num = items.get(itemid);
				int available = session.execute(get_quantity_stmt.bind(itemid)).one().getInt("total");
				if(num > available) {
					break;
				}
			}
			session.execute(set_schedule_stmt.bind(order.getInt("id")));
		}
	}
	
	public JSONArray listCompletedOrders() {
		JSONArray orders = new JSONArray();

		for(Row order:session.execute(get_completed_list.bind())) {
			JSONObject orderJson = new JSONObject();

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
			Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
			Map<Integer,Integer> itemsP = order.getMap("price", Integer.class, Integer.class);

			for(Integer itemid:items.keySet()) {
				JSONObject item = new JSONObject();
				item.put("itemid",itemid);
				int quantity = items.get(itemid);
				int price = getPrice(itemid);
				item.put("quantity",quantity);
				item.put("MSRPprice", price);
				item.put("shortdescription", getShortDescription(itemid));
				item.put("MSRPsubtotal", quantity * price);
				int paidPrice = itemsP.get(itemid);
				item.put("price", paidPrice);
				itemsJson.put(item);
			}
			orderJson.put("items",itemsJson);
			orders.put(orderJson);
		}
		return orders;
	}

	
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

	public JSONArray listOrders() {
		JSONArray orders = new JSONArray();

		for(Row order:session.execute(list_items_stmt.bind())) {
			JSONObject orderJson = new JSONObject();

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
			Map<Integer,Integer> items = order.getMap("quantity", Integer.class, Integer.class);
			Map<Integer,Integer> itemsP = order.getMap("price", Integer.class, Integer.class);

			for(Integer itemid:items.keySet()) {
				JSONObject item = new JSONObject();
				item.put("itemid",itemid);
				int quantity = items.get(itemid);
				int price = getPrice(itemid);
				item.put("quantity",quantity);
				item.put("MSRPprice", price);
				item.put("shortdescription", getShortDescription(itemid));
				item.put("MSRPsubtotal", quantity * price);
				int paidPrice = itemsP.get(itemid);
				item.put("price", paidPrice);
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
		JSONArray itemsQuantities = json.getJSONArray("quantity");
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
		session.execute(create_order_stmt.bind(id,channel,date,firstname,lastname,city,state,zip,payment,total,address,mapQ,mapP));
		
		
	}

	public void updateOrder(JSONObject json) {
		int id = json.getInt("id");
		insertOrderIntoDb(id,json);
	}
}
