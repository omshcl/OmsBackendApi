package webapp;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


@WebServlet(urlPatterns="/orders/new")
public class OrderServlet extends HttpServlet {

	private  OrderApi orderApi;
	
	public OrderServlet() {
		orderApi = new OrderApi();
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get data from request
		BufferedReader reader = request.getReader();
		StringBuffer jb = new StringBuffer();
		String line = null;
		while((line = reader.readLine()) != null)
			jb.append(line);
		
		//parse request data and parse as json
		JSONObject json = new JSONObject(jb.toString());
		orderApi.createOrder(json);
		System.out.println("inserted");
		System.out.println(json.toString());
		response.getWriter().write("TEST");
	}

}
