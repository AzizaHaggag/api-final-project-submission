package apiTests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class RestfulBooker {
    String loginToken;
    int bookingID;

    @BeforeClass
    public void setLoginToken() {
        String endpoint = "https://restful-booker.herokuapp.com/auth";
        String body = """
                {
                    "username" : "admin",
                    "password" : "password123"
                }
                """;
        ValidatableResponse validatableResponse = given().body(body)
                .header("Content-Type", "application/json")
                .when().post(endpoint).then();
        Response response = validatableResponse.extract().response();
        JsonPath jsonPath = response.jsonPath();
        loginToken = jsonPath.getString("token");
        System.out.println(loginToken);
    }

    @Test(priority = 0)
    public void testCreateValidBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking";
        String body = """
                {
                    "firstname" : "Jim",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }
                """;
        var validatableResponse = given().body(body)
                .header("Content-Type", "application/json").log().all()
                .when().post(endpoint).then();
        validatableResponse.body("booking.firstname", equalTo("Jim"));
        validatableResponse.statusCode(200);

        Response response = validatableResponse.extract().response();
        JsonPath jsonPath = response.jsonPath();
        bookingID = jsonPath.getInt("bookingid");
        validatableResponse.log().all();
    }

    @Test(priority = 1, dependsOnMethods = "testCreateValidBooking")
    public void testUpdateBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        String body = """
                {
                    "firstname" : "Ali",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }
                """;
        var validatableResponse = given().body(body)
                .header("Content-Type", "application/json")
                .header("Cookie", "token=" + loginToken) //cookie is mandatory
             //   .header("Authorisation" , "Basic") // Authorisation is optional
                .log().all()
                .when().put(endpoint)
                .then();
        validatableResponse.header("Content-Type", "application/json; charset=utf-8");
        validatableResponse.body("firstname", equalTo("Ali"));
        validatableResponse.statusCode(200);


    }

    @Test(priority = 2, dependsOnMethods = "testUpdateBooking")
    public void testGetBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        var validatableResponse = given()
                .header("Content-Type", "application/json; charset=utf-8")
                .log().all()
                .when().get(endpoint).then();
        validatableResponse.body("firstname", equalTo("Ali"));
        validatableResponse.statusCode(200);
    }

    @Test(priority = 3, dependsOnMethods = "testGetBooking")
    public void testDeleteBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        var validatableResponse = given()
                .header("Content-Type", "application/json")
                .header("Cookie", "token=" + loginToken) //cookie is mandatory
              //  .header("Authorisation", "Basic") // Authorisation is optional
                .delete(endpoint)
                .then();

        Response response = validatableResponse.extract().response();
        Assert.assertEquals(response.asString() , null) ; //in general must be empty, but in this doc it is created
        validatableResponse.statusCode(201); //in general must be 204, but in this doc it is 201 created

    }
}
