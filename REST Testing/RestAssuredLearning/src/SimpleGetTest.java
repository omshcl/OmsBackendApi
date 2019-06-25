import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;

public class SimpleGetTest {
 
 @Test
 public void GetWeatherDetails1()
 {   
 // Specify the base URL to the RESTful web service
 RestAssured.baseURI = "http://localhost:4200/login";
	RequestSpecification request = RestAssured.given();
 // Get the RequestSpecification of the request that you want to sent
 // to the server. The server is specified by the BaseURI that we have
 // specified in the above step.
 RequestSpecification httpRequest = RestAssured.given();
 JSONObject requestParams = new JSONObject();
 requestParams.put("user", "admin"); 
 requestParams.put("password", "Admin!123");
 // Make a request to the server by specifying the method Type and the method URL.
 // This will return the Response from the server. Store the response in a variable.
	request.body(requestParams.toJSONString());
	Response response = request.post("/login");
	System.out.println(response);
	Assert.assertTrue(response.jsonPath().get("isAdmin"));
	
	//Assert.assertEquals( "Correct Success code was returned", successCode, "OPERATION_SUCCESS");
 
 }

 
}