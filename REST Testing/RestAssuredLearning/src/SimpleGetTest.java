import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
 
public class SimpleGetTest {
 
 @Test
 public void GetWeatherDetails1()
 {   
 // Specify the base URL to the RESTful web service
 RestAssured.baseURI = "http://restapi.demoqa.com/utilities/weather/city";
 
 // Get the RequestSpecification of the request that you want to sent
 // to the server. The server is specified by the BaseURI that we have
 // specified in the above step.
 RequestSpecification httpRequest = RestAssured.given();
 
 // Make a request to the server by specifying the method Type and the method URL.
 // This will return the Response from the server. Store the response in a variable.
 Response response = httpRequest.request(Method.GET, "/Hyderabad");
 
 // Now let us print the body of the message to see what response
 // we have recieved from the server
 String responseBody = response.getBody().asString();
 System.out.println("Response Body is =>  " + responseBody);
 
 }

  @Test
  public void GetWeatherDetails()
  {
  RestAssured.baseURI = "http://restapi.demoqa.com/utilities/weather/city";
  RequestSpecification httpRequest = RestAssured.given();
  Response response = httpRequest.get("/78789798798");
  
  // Get the status code from the Response. In case of 
  // a successfull interaction with the web service, we
  // should get a status code of 200.
  int statusCode = response.getStatusCode();
  
  // Assert that correct status code is returned.
  Assert.assertEquals(statusCode /*actual value*/, 200 /*expected value*/, "Correct status code returned");
  }
  
  //@Test
  public void GetWeatherHeaders()
  {
   RestAssured.baseURI = "http://restapi.demoqa.com/utilities/weather/city";
   RequestSpecification httpRequest = RestAssured.given();
   Response response = httpRequest.get("/Hyderabad");
   
   // Reader header of a give name. In this line we will get
   // Header named Content-Type
   String contentType = response.header("Content-Type");
   Assert.assertEquals(contentType /* actual value */, "application/json" /* expected value */);
   
   // Reader header of a give name. In this line we will get
   // Header named Server
   String serverType =  response.header("Server");
   Assert.assertEquals(serverType /* actual value */, "nginx/1.12.1" /* expected value */);
   
   // Reader header of a give name. In this line we will get
   // Header named Content-Encoding
   String contentEncoding = response.header("Content-Encoding");
   Assert.assertEquals(contentEncoding /* actual value */, "gzip" /* expected value */);
  }
 
}