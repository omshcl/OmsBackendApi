package oms.cookies;


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


@WebServlet(urlPatterns = "/validate")
public class CookieValidate extends HttpServlet {
	CookieApi cookieApi;
	
	public CookieValidate() {
		cookieApi = new CookieApi();
	}
	
	//valid user based off of stored cookie information
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//get data from request	
		Cookie[] cookies = request.getCookies();
		String value = null;
		for(Cookie cookie:cookies) {
			String name = cookie.getName();
			value = cookie.getValue();
			if(name.equals("session")) {
				break;
			}
		}
		System.out.println(value);
		boolean valid = cookieApi.isSession(value);
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
		String responseJson = new JSONObject().put("valid",valid).toString();
		response.getWriter().write(responseJson);
		
	
	}
	
}