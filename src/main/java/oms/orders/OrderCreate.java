package oms.orders;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


@WebServlet(urlPatterns="/orders/new")
public class OrderCreate extends HttpServlet {
	
	private  OrderApi orderApi;
	
	public OrderCreate() {
		orderApi = new OrderApi();
	}
	
	/**
	 * POST request to add orders to orders table
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
		boolean success = orderApi.createOrder(json);
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
		String responseJson;
		if(success) {
			responseJson = new JSONObject().put("success","true").toString();
		}
		else {
			responseJson = new JSONObject().put("success","false").toString();
		}
		response.getWriter().write(responseJson);
	}
	
	
}
