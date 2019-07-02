package oms.items;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import org.json.JSONArray;
import org.json.JSONObject;
@WebServlet(urlPatterns="/items/details")
public class SpecificItem extends HttpServlet {
	
	private ItemApi itemApi;
	
	public SpecificItem() {
		itemApi = new ItemApi();
	}
	

	@Override
	/**
	 * POST request for specific supply information
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
		
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
		String responseJson = itemApi.getSpecific(json).toString();
		response.getWriter().write(responseJson);
	}
}