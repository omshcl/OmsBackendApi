package oms.orders;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONArray;

@WebServlet(urlPatterns="/orders/complete")
public class CompletedList extends HttpServlet {
	
	private OrderApi orderApi;
	
	public CompletedList() {
		orderApi = new OrderApi();
	}

	@Override
	/**
	 * GET Request that sends all complete_orders
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
		int limit = json.getInt("limit");
		JSONArray orders = orderApi.listCompletedOrders(limit);
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(orders.toString());
	}
}
