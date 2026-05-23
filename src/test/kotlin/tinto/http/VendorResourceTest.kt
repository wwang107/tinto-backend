package tinto.http

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import tinto.PostgresTestResource

@QuarkusTest
@QuarkusTestResource(PostgresTestResource::class)
class VendorResourceTest {

    @Test
    fun `GET vendors cafes is public and returns all cafes`() {
        given()
            .`when`().get("/vendors/cafes")
            .then()
            .statusCode(200)
            .body("$.size()", `is`(3))
    }

    @Test
    fun `GET vendors with q=cafes is public and returns all cafes`() {
        given()
            .`when`().get("/vendors?q=cafes")
            .then()
            .statusCode(200)
            .body("$.size()", `is`(3))
    }

    @Test
    fun `GET vendors with name filter returns matching cafes`() {
        given()
            .`when`().get("/vendors?q=cafes&name=bean")
            .then()
            .statusCode(200)
            .body("$.size()", `is`(1))
            .body("[0].name", equalTo("Bean Scene"))
    }

    @Test
    fun `GET vendors with name filter is case insensitive`() {
        given()
            .`when`().get("/vendors?q=cafes&name=DAILY")
            .then()
            .statusCode(200)
            .body("$.size()", `is`(1))
            .body("[0].name", equalTo("The Daily Grind"))
    }

    @Test
    fun `GET vendors with unknown type returns empty list`() {
        given()
            .`when`().get("/vendors?q=restaurants")
            .then()
            .statusCode(200)
            .body("$.size()", `is`(0))
    }

    @Test
    fun `POST vendors cafes returns 401 without authentication`() {
        given()
            .`when`().post("/vendors/cafes")
            .then()
            .statusCode(401)
    }

    @Test
    @TestSecurity(user = "test-user")
    fun `POST vendors cafes returns 204 when authenticated`() {
        given()
            .`when`().post("/vendors/cafes")
            .then()
            .statusCode(204)
    }
}
