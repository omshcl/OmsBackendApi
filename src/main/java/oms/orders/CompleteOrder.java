package oms.orders;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet(urlPatterns="/orders/complete/remove")
public class CompleteOrder extends HttpServlet {
	
	private OrderApi orderApi;
	
	public CompleteOrder() {
		orderApi = new OrderApi();
	}

	@Override
	/**
	 * GET Request that checks if an order can be completed and completes it
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		orderApi.completeOrder();
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
		String responseJson = new JSONObject().put("sucesss","true").toString();
		response.getWriter().write(responseJson);
	}
}
