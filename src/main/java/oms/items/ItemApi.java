package oms.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import oms.Api;

public class ItemApi extends Api {
	
	private Session session;
	private PreparedStatement get_items_stmt,get_info_stmt, get_shortdescri_stmt, insert_itemsupply_stmt, search_stmt, select_next_id, inc_id_stmt, insert_item_stmt, get_exist_stmt, get_specific_stmt;
	public ItemApi() {
		super();
		session = super.getSession();
		get_exist_stmt = session.prepare("SELECT itemid from items where itemdescription = ? allow filtering");
		get_items_stmt = session.prepare("SELECT * from items");
		get_info_stmt  = session.prepare("SELECT * from items where itemid = ?");
		get_shortdescri_stmt = session.prepare("SELECT shortdescription from items where itemid = ?");
		insert_itemsupply_stmt = session.prepare("INSERT INTO itemsupplies(itemid, productclass, eta, shipbydate, shipnode, type, quantity, shippingaddress) values(?,?,?,?,?,?,?,?)");
		select_next_id = session.prepare("SELECT itemid from id");
		inc_id_stmt = session.prepare("UPDATE id set itemid = ? where id ='id'");
		insert_item_stmt = session.prepare("INSERT INTO items(itemid, category, isreturnable, itemdescription, manufacturername, price, shortdescription, subcategory, unitofmeasure) values(?,?,?,?,?,?,?,?,?)");

	}
	
	public JSONObject getSpecific(JSONObject obj) {
		JSONObject o = new JSONObject();
		String query = "SELECT * from itemsupplies where itemid = " + obj.getInt("itemid") + " and productclass = \'" + obj.getString("productclass") + "\' and type = \'" + obj.getString("type") + "\' and shipnode = \'" + obj.getString("locationname") +"\';";
		System.out.println(query);
		get_specific_stmt = session.prepare(query);

		Row row = session.execute(get_specific_stmt.bind()).one();
		o.put("itemid", row.getInt("itemid"));
		o.put("productclass", row.getString("productclass"));
		o.put("eta", row.getString("eta"));
		o.put("shipbydate", row.getString("shipbydate"));
		o.put("shipnode", row.getString("shipnode"));
		o.put("type", row.getString("type"));
		o.put("quantity", row.getInt("quantity"));
		o.put("shippingaddress", row.getString("shippingaddress"));
		return o;
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
	
	public JSONObject getSummary(int itemid) {
		JSONArray jsonArray = new JSONArray();
		
		for (Row row :session.execute(get_info_stmt.bind(itemid))) {
			JSONObject jsonRow = new JSONObject();

			String[] strCols = {"itemdescription","shortdescription","unitofmeasure","category","subcategory","isreturnable","manufacturername"};
			String[] intCols = {"itemid","price"};
			
			jsonRow.put("itemid", row.getInt("itemid"));
			
			for(String colName:strCols)
				jsonRow.put(colName, row.getString(colName));
			for(String colName:intCols) 
				jsonRow.put(colName, row.getInt(colName));
			
			jsonArray.put(jsonRow);
			return jsonRow;
		}
		return jsonArray.getJSONObject(0);
	}
	
	public JSONObject getShortDescription(int itemid) {
		for (Row row :session.execute(get_shortdescri_stmt.bind(itemid))) {
			JSONObject jsonRow = new JSONObject();	
			System.out.println(row);
			jsonRow.put("shortdescription", row.getString("shortdescription"));
			return jsonRow;
		}
		return null;
	}
	
	public JSONArray restrictSearch(JSONObject search) {
		String restrictItems = "";
		String restrictShipnodes = "";
		JSONArray items = search.getJSONArray("items");
		JSONArray shipnodes = search.getJSONArray("shipnodes");
		for(Object o : items) {
			JSONObject obj = (JSONObject) o;
			restrictItems += obj.getInt("itemid") + ", ";
		}
		restrictItems = restrictItems.substring(0,restrictItems.length() - 2);
		for(Object o : shipnodes) {
			JSONObject obj = (JSONObject) o;
			restrictShipnodes += "'" + obj.getString("locationname") + "\', ";
		}
		restrictShipnodes = restrictShipnodes.substring(0,restrictShipnodes.length() - 2);
		String query = "SELECT * from itemsupplies where itemid in (" + restrictItems + ") " + "and shipnode in (" + restrictShipnodes + ") allow filtering;";
		search_stmt = session.prepare(query);
		JSONArray jsonArray = new JSONArray();
		for (Row row : session.execute(search_stmt.bind())) {
			JSONObject jsonRow = new JSONObject();
			String[] cols = {"shipnode", "eta", "productclass", "shipbydate", "shippingaddress", "type"};
			String[] intCols = {"itemid", "quantity"};
			
			for(String colName:cols)
				jsonRow.put(colName, row.getString(colName));
			for(String colName:intCols) 
				jsonRow.put(colName, row.getInt(colName));
			
			jsonArray.put(jsonRow);
		}
		
		return jsonArray;
	}
	
	public void createSupply(JSONObject json) {
		Row r = session.execute(get_exist_stmt.bind(json.getString("itemdescription"))).one();
		if(r != null) {
			insertSupplyIntoDb(r.getInt("itemid"), json);
		}
		else {
			insertSupplyIntoDb(getSupplyID(), json);
		}
	}
	
	private int getSupplyID() {
		Row row = session.execute(select_next_id.bind()).one();
		int id = row.getInt("itemid");
		//updates id in database
		session.execute(inc_id_stmt.bind(id+1));
		return id;
	}
	
	private void insertSupplyIntoDb(int itemid, JSONObject json) {
		String productclass   = json.getString("productclass");
		String eta      = json.getString("eta");
		String shipbydate = json.getString("shipbydate");
		String type  = json.getString("type");
		String shipnode = json.getString("locationname");
		int quantity      = json.getInt("quantity");
		String shippingaddress     = json.getString("shippingaddress");
		session.execute(insert_itemsupply_stmt.bind(itemid,productclass,eta,shipbydate,shipnode,type,quantity,shippingaddress));
		String category = json.getString("category");
		String isreturnable = json.getString("isreturnable");
		String itemdescription = json.getString("itemdescription");
		String manufacturername = json.getString("manufacturername");
		int price = json.getInt("price");
		String shortdescription = json.getString("shortdescription");
		String subcategory = json.getString("subcategory");
		String unitofmeasure = json.getString("unitofmeasure");
		session.execute(insert_item_stmt.bind(itemid, category, isreturnable, itemdescription, manufacturername, price, shortdescription, subcategory, unitofmeasure));

	}
}
