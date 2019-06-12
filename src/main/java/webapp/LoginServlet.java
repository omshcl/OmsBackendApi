package webapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/*
 * Browser sends Http Request to Web Server
 * 
 * Code in Web Server => Input:HttpRequest, Output: HttpResponse
 * JEE with Servlets
 * 
 * Web Server responds with Http Response
 */

//Java Platform, Enterprise Edition (Java EE) JEE6

//Servlet is a Java programming language class 
//used to extend the capabilities of servers 
//that host applications accessed by means of 
//a request-response programming model.

//1. extends javax.servlet.http.HttpServlet
//2. @WebServlet(urlPatterns = "/login.do")
//3. doGet(HttpServletRequest request, HttpServletResponse response)
//4. How is the response created?

@WebServlet(urlPatterns = "/login.do")
public class LoginServlet extends HttpServlet {
	
	LoginApi login;
	
	public LoginServlet() {
		super();
		login = new LoginApi();
	}

	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Yahoo!!!!!!!!</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("My First Servlet");
		out.println("</body>");
		out.println("</html>");
		System.out.println("example, example " + login.validateUser("example", "example") + login.isAdmin("example"));
		System.out.println("admin, Admin!123 " + login.validateUser("admin", "Admin!123") + login.isAdmin("admin"));
		System.out.println("agent, Agent!123"  + login.validateUser("agent", "Agent!123") + login.isAdmin("agent"));
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		BufferedReader reader = request.getReader();
		StringBuffer jb = new StringBuffer();
		String line = null;
		while((line = reader.readLine()) != null)
			jb.append(line);
		JSONObject json = new JSONObject(jb.toString());
		String username = json.getString("username");
		String password = json.getString("password");
		boolean isValid = login.validateUser(username, password);
		boolean isAdmin = login.isAdmin(username);
		System.out.println(isValid);
		System.out.println(isAdmin);
		PrintWriter out = response.getWriter();
		System.out.println("post request made");
		out.println("Post Request");
		response.setContentType("applications/json");
		response.setCharacterEncoding("UTF-8");
		String responseJson = new JSONObject().put("isValid",isValid).put("isAdmin", isAdmin).toString();
		response.getWriter().write(responseJson);
	}
	
	
	
}