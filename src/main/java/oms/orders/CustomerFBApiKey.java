package oms.orders;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(urlPatterns="/customer/fbapikey")
public class CustomerFBApiKey extends HttpServlet {
	
	private OrderApi orderApi;
	
	public CustomerFBApiKey() {
		orderApi = new OrderApi();
	}

	@Override
	/**
	 * POST Request that updates api key
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
		
		String username = json.get("username").toString();
		String key = json.get("fbapikey").toString();

		orderApi.updateFBKey(username, key);
		
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
		String responseJson = new JSONObject().put("sucesss","true").toString();
		response.getWriter().write(responseJson);
	}
}