package oms.items;

import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import oms.Api;

public class ItemApi extends Api {
	
	private Session session;
	private PreparedStatement get_items_stmt;
	
	public ItemApi() {
		super();
		session = super.getSession();
		get_items_stmt = session.prepare("SELECT * from items");
	}
	
	public JSONArray getItems() {
		JSONArray jsonArray = new JSONArray();
	
		for (Row row :session.execute(get_items_stmt.bind()).all()) {
			JSONObject jsonRow = new JSONObject();

			String[] strCols = {"itemdescription","shortdescription","unitofmeasure","category","subcategory","isreturnable","manufacturername"};
			String[] intCols = {"itemid","price"};
			
			jsonRow.put("itemid", row.getInt("itemid"));
			
			for(String colName:strCols)
				jsonRow.put(colName, row.getString(colName));
			for(String colName:intCols) 
				jsonRow.put(colName, row.getInt(colName));
			
			jsonArray.put(jsonRow);
		}
		return jsonArray;
	}
}
