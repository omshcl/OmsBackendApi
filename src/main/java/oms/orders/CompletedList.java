package oms.orders;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONArray orders = orderApi.listCompletedOrders();
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(orders.toString());
	}
}
