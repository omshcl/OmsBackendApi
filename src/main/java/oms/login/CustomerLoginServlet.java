package oms.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import oms.cookies.CookieApi;

@WebServlet(urlPatterns = "/customer/login")
public class CustomerLoginServlet extends HttpServlet {
	
	CustomerLoginApi loginApi;
	CookieApi cookieApi;
	
	public CustomerLoginServlet() {
		loginApi = new CustomerLoginApi();
		cookieApi = new CookieApi();
	}
	
	@Override
	/**
	 * POST Request to validate login
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//get data from request
		BufferedReader reader = request.getReader();
		StringBuffer jb = new StringBuffer();
		String line = null;
		while((line = reader.readLine()) != null)
			jb.append(line);
		
		//parse request data and parse as json
		JSONObject json = new JSONObject(jb.toString());
		
		//get username and password from json
		String username = json.getString("username");
		String password = json.getString("password");
		
		//query userstatus from loginService
		boolean isValid = loginApi.validateUser(username, password);

		//format data as json as return it
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
	
		String responseJson = new JSONObject().put("isValid",isValid).toString();
		response.getWriter().write(responseJson);
	}
	
}