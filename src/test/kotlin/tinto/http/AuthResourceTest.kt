package tinto.http

import io.quarkus.test.InjectMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.sqlclient.Pool
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import tinto.PostgresTestResource
import tinto.auth.ProviderClaims
import tinto.auth.TokenVerifier

@QuarkusTest
@QuarkusTestResource(PostgresTestResource::class)
class AuthResourceTest {

    @InjectMock
    lateinit var tokenVerifier: TokenVerifier

    @Inject
    lateinit var client: Pool

    @BeforeEach
    fun cleanUp() {
        runBlocking {
            client.query("TRUNCATE TABLE users").execute().awaitSuspending()
        }
    }

    @BeforeEach
    fun setUpMocks() {
        `when`(tokenVerifier.verify("GOOGLE", "valid-google-token")).thenReturn(
            ProviderClaims(sub = "google-sub-1", email = "google@example.com", name = "Google User")
        )
        `when`(tokenVerifier.verify("APPLE", "valid-apple-token")).thenReturn(
            ProviderClaims(sub = "apple-sub-1", email = "apple@example.com", name = null)
        )
    }

    @Test
    fun `POST auth login with Google token returns JWT and user info`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"provider":"GOOGLE","idToken":"valid-google-token"}""")
            .`when`().post("/auth/login")
            .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("userId", notNullValue())
            .body("email", equalTo("google@example.com"))
    }

    @Test
    fun `POST auth login with Apple token returns JWT and user info`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"provider":"APPLE","idToken":"valid-apple-token","name":"Apple User"}""")
            .`when`().post("/auth/login")
            .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("email", equalTo("apple@example.com"))
    }

    @Test
    fun `POST auth login twice with same credentials returns same userId`() {
        val firstUserId = given()
            .contentType(ContentType.JSON)
            .body("""{"provider":"GOOGLE","idToken":"valid-google-token"}""")
            .`when`().post("/auth/login")
            .then()
            .statusCode(200)
            .extract().path<String>("userId")

        val secondUserId = given()
            .contentType(ContentType.JSON)
            .body("""{"provider":"GOOGLE","idToken":"valid-google-token"}""")
            .`when`().post("/auth/login")
            .then()
            .statusCode(200)
            .extract().path<String>("userId")

        assert(firstUserId == secondUserId)
    }

    @Test
    fun `POST auth login with blank provider returns 400`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"provider":"","idToken":"valid-google-token"}""")
            .`when`().post("/auth/login")
            .then()
            .statusCode(400)
    }

    @Test
    fun `POST auth login with blank idToken returns 400`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"provider":"GOOGLE","idToken":""}""")
            .`when`().post("/auth/login")
            .then()
            .statusCode(400)
    }
}
