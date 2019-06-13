package oms.items;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns="/items/list")
public class ItemsServlet extends HttpServlet {
	
	private ItemApi itemApi;
	
	public ItemsServlet() {
		itemApi = new ItemApi();
	}
	

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONArray items = itemApi.getItems();
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(items.toString());
	}
	
}
