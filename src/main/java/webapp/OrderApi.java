package webapp;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class OrderApi extends Api {
	
	private Session session;
	private PreparedStatement create_order_stmt, is_admin_stmt,select_next_id,inc_id_stmt;

	public OrderApi() {
		super();
		session = super.getSession();
		create_order_stmt = session.prepare("INSERT INTO ORDERS (id,channel,date,firstname,lastname,city,state,zip,payment,total,address,items) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
		is_admin_stmt     = session.prepare("SELECT isAdmin from users where username = ?");
		select_next_id    = session.prepare("SELECT next from order_id");
		inc_id_stmt       = session.prepare("UPDATE order_id set next = ? where id ='id'");
	}
	
	private int getOrderId() {
		Row row = session.execute(select_next_id.bind()).one();
		int id = row.getInt("next");
		session.execute(inc_id_stmt.bind(id+1));
		return id;
	}
	
	public void createOrder(JSONObject json) {
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
		int id            = getOrderId();
		//extract item name and quanity from request
		JSONArray items = json.getJSONArray("items");
		Map<String,Integer> map = new HashMap<>();
		for(int i =0; i < items.length();i ++) {
			JSONObject item = items.getJSONObject(i);
			map.put(item.getString("item"),item.getInt("quantity"));		
		}
		session.execute(create_order_stmt.bind(id,channel,date,firstname,lastname,city,state,zip,payment,total,address,map));
	}
	
	public boolean isAdmin(String user) {
		ResultSet res = session.execute(is_admin_stmt.bind(user));
		Row row = res.one();
		if (row == null) {
			return false;
		}
		return row.getBool("isadmin");
	}
}
