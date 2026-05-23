package tinto.db

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.sqlclient.Pool
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tinto.PostgresTestResource

@QuarkusTest
@QuarkusTestResource(PostgresTestResource::class)
class UserRepositoryTest {

    @Inject
    lateinit var repository: UserRepository

    @Inject
    lateinit var client: Pool

    // JUnit 5 lifecycle methods cannot be suspend
    @BeforeEach
    fun cleanUp() {
        runBlocking {
            client.query("TRUNCATE TABLE users").execute().awaitSuspending()
        }
    }

    // --- findByProviderId ---

    @Test
    fun `findByProviderId returns null when user does not exist`() = runTest {
        val result = repository.findByProviderId("GOOGLE", "nonexistent-sub")
        assertNull(result)
    }

    @Test
    fun `findByProviderId returns user when found`() = runTest {
        val created = repository.upsert("GOOGLE", "google-sub-123", "user@example.com", "Alice")

        val found = repository.findByProviderId("GOOGLE", "google-sub-123")

        assertNotNull(found)
        assertEquals(created.id, found!!.id)
        assertEquals("user@example.com", found.email)
        assertEquals("Alice", found.name)
        assertEquals("GOOGLE", found.provider)
        assertEquals("google-sub-123", found.providerId)
    }

    @Test
    fun `findByProviderId returns null when provider does not match`() = runTest {
        repository.upsert("GOOGLE", "sub-123", "user@example.com", "Alice")

        val result = repository.findByProviderId("APPLE", "sub-123")

        assertNull(result)
    }

    // --- upsert ---

    @Test
    fun `upsert creates a new user and returns all fields`() = runTest {
        val user = repository.upsert("GOOGLE", "sub-abc", "new@example.com", "Bob")

        assertNotNull(user.id)
        assertEquals("new@example.com", user.email)
        assertEquals("Bob", user.name)
        assertEquals("GOOGLE", user.provider)
        assertEquals("sub-abc", user.providerId)
        assertNotNull(user.createdAt)
    }

    @Test
    fun `upsert returns same id on subsequent call with same provider and providerId`() = runTest {
        val first = repository.upsert("APPLE", "apple-sub-999", "a@example.com", "Carol")
        val second = repository.upsert("APPLE", "apple-sub-999", "a@example.com", "Carol")

        assertEquals(first.id, second.id)
    }

    @Test
    fun `upsert updates email on conflict`() = runTest {
        repository.upsert("GOOGLE", "sub-xyz", "old@example.com", "Dave")
        val updated = repository.upsert("GOOGLE", "sub-xyz", "new@example.com", "Dave")

        assertEquals("new@example.com", updated.email)
    }

    @Test
    fun `upsert handles null name`() = runTest {
        val user = repository.upsert("APPLE", "apple-sub-null", "anon@example.com", null)

        assertNull(user.name)
        assertEquals("anon@example.com", user.email)
    }
}
