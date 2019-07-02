package oms.shipnodes;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import oms.Api;

public class ShipnodesAPI extends Api {
	
	private Session session;
	private PreparedStatement shipnodes_list_stmt;

	public ShipnodesAPI() {
		super();
		session = super.getSession();
		//Selects all shipnodes
		shipnodes_list_stmt = session.prepare("SELECT *  FROM shipnodes;");
	}
	
	/**
	 * 
	 * @return JSONArray of all shipnodes
	 */
	public JSONArray getShipnodes() {
		JSONArray jsonArray = new JSONArray();
		for (Row row :session.execute(shipnodes_list_stmt.bind()).all()) {
			JSONObject loc = new JSONObject();
			loc.put("locationname",row.getString("locationname"));
			jsonArray.put(loc);
		}
		return jsonArray;
	}
}
