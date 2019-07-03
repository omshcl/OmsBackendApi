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
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//get data from request
		
		System.out.println("GOT Request");
		
		Cookie[] cookies = request.getCookies();
		for(Cookie cookie:cookies) {
			System.out.println(cookie.getName());
			System.out.println(cookie.getValue());
		}
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin","*");
	
		
	
	}
	
	
	
}