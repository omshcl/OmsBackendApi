package oms.orders;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet(urlPatterns="/orders/customer_cancel")
public class CustomerCancel extends HttpServlet {
	
	private OrderApi orderApi;
	
	public CustomerCancel() {
		orderApi = new OrderApi();
	}

	@Override
	/**
	 * POST Request that resets the order to ready
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
		int id = Integer.parseInt(json.get("id").toString());
		orderApi.customerCancel(id);
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
		String responseJson = "success";
		response.getWriter().write(responseJson);
	}
}
