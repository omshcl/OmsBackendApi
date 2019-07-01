package oms.orders;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet(urlPatterns="/orders/schedule")
public class ScheduleOrder extends HttpServlet {
	
	private OrderApi orderApi;
	
	public ScheduleOrder() {
		orderApi = new OrderApi();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		orderApi.scheduleOrders();
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
		String responseJson = new JSONObject().put("sucesss","true").toString();
		response.getWriter().write(responseJson);
	}
}
