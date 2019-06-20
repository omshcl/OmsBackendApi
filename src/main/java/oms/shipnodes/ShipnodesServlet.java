package oms.shipnodes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns="/shipnodes")
public class ShipnodesServlet extends HttpServlet {
	
	private ShipnodesAPI shipnodesAPI;
	
	public ShipnodesServlet() {
		shipnodesAPI = new ShipnodesAPI();
	}
	

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONArray shipnodes = shipnodesAPI.getShipnodes();
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(shipnodes.toString());
	}
	
}
