package oms.orders;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(urlPatterns="/customer")
public class CustomerData extends HttpServlet {
	
	private OrderApi orderApi;
	
	public CustomerData() {
		orderApi = new OrderApi();
	}

	@Override
	/**
	 * POST Request that gets customer details
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
		
		//get orders
		String username = json.get("username").toString();
		JSONObject customer = orderApi.getCustomerData(username);
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(customer.toString());
	}
}
