package oms.orders;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
@WebServlet(urlPatterns="/orders/getinfo")

public class OrderGet extends HttpServlet {
	
	private OrderApi orderApi;
	
	public OrderGet() {
		orderApi = new OrderApi();
	}
	
	/**
	 * POST Request to get information about a particular order
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get data from request
		BufferedReader reader = request.getReader();
		StringBuffer jb = new StringBuffer();
		String line = null;
		while((line = reader.readLine()) != null)
			jb.append(line);
		
		//parse request data and parse as json
		JSONObject json = new JSONObject(jb.toString());
		
		//get id number from json
		int id = Integer.parseInt(json.get("itemid").toString());

		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
		String responseJson = orderApi.getSummary(id).toString();
		response.getWriter().write(responseJson);
	}
	


}
