import com.jayway.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.containsString;

public class TestLogin {


        @BeforeClass
        public static void setupURL ()
        {
            // here we setup the default URL and API base path to use throughout the tests
            RestAssured.baseURI = "http://6f3cb760.ngrok.io";
        }

    @Test
    //simple get request test that checks if chicago is present in shipnodes list
    public void getShipnodes()
    {
        given().when().get("/shipnodes").then()
                .body(containsString("Chicago"));

    }

    @Test
    public void testLoginAdmin() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("Admin!123");
        String login = given()
                .contentType("application/json")
                .body(user)
                .when().post("/login").print();

        String[] tokens = login.split(",|:|}");
        String valid = tokens[1];
        String admin = tokens[3];
        assertEquals(valid, "true");
        assertEquals(admin,"true");
    }

    @Test
    public void testLoginAgent() {
        User user = new User();
        user.setUsername("agent");
        user.setPassword("Agent!123");

        String login = given()
                .contentType("application/json")
                .body(user)
                .when().post("/login").print();

        String[] tokens = login.split(",|:|}");
        String valid = tokens[1];
        String admin = tokens[3];
        assertEquals(valid, "true");
        assertEquals(admin,"false");
    }

    @Test
    public void testLoginInvalid() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");

        String login = given()
                .contentType("application/json")
                .body(user)
                .when().post("/login").print();

        String[] tokens = login.split(",|:|}");
        String valid = tokens[1];
        String admin = tokens[3];
        assertEquals(valid, "false");
        assertEquals(admin,"false");
    }

    private class User {
            private String username;
            private String password;

            //need get for serializing
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
