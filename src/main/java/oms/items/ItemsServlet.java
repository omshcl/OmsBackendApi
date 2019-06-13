package oms.items;

import javax.servlet.http.HttpServlet;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns="/items/list")
public class ItemsServlet extends HttpServlet {
	
	private ItemApi itemApi;
	
	public ItemsServlet() {
		itemApi = new ItemApi();
	}
	
}
