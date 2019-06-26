import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

public class TestLogin {

    public static Response response;
    public static String jsonAsString;

        @BeforeClass
        public static void setupURL ()
        {
            // here we setup the default URL and API base path to use throughout the tests
            RestAssured.baseURI = "http://986b139a.ngrok.io";
        }

    @Test
    //simple get request test that checks if chicago is present in shipnodes list
    public void getShipnodes()
    {
        given().when().get("/shipnodes").then()
                .body(containsString("Chicago"));

    }

    @Test
    public void testLogin() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("Admin!123");

        given()
                .contentType("application/json")
                .body(user)
                .when().post("/login").then().body("isValid",equalTo(true));
    }

    private static class Login {
            private boolean isValid;
            private boolean isAdmin;

            Login(boolean isValid, boolean isAdmin) {
                this.isValid = isValid;
                this.isAdmin = isAdmin;
            }

            public boolean isAdmin(){
                return isAdmin;
            }
            public boolean isValid(){
                return isValid;
            }
    }

    private class User {
            private String username;
            private String password;

            public String getUsername() {
                return username;
            }
            public String getPassword() {
                return password;
            }
            public void setUsername(String username) {
                this.username = username;
            }
            public void setPassword(String password) {
                this.password = password;
            }
    }
}
