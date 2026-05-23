package tinto.db

import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.Tuple
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import tinto.model.User
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class UserRepository {

    @Inject
    lateinit var client: Pool

    suspend fun findByProviderId(provider: String, providerId: String): User? {
        val rows = client.preparedQuery(
            "SELECT id, email, name, provider, provider_id, created_at FROM users WHERE provider = $1 AND provider_id = $2"
        ).execute(Tuple.of(provider, providerId)).awaitSuspending()
        return rows.firstOrNull()?.let { row ->
            User(
                id = row.getUUID("id"),
                email = row.getString("email"),
                name = row.getString("name"),
                provider = row.getString("provider"),
                providerId = row.getString("provider_id"),
                createdAt = row.getOffsetDateTime("created_at"),
            )
        }
    }

    suspend fun upsert(provider: String, providerId: String, email: String, name: String?): User {
        val rows = client.preparedQuery(
            """
            INSERT INTO users (email, name, provider, provider_id)
            VALUES ($1, $2, $3, $4)
            ON CONFLICT (provider, provider_id) DO UPDATE SET email = EXCLUDED.email
            RETURNING id, email, name, provider, provider_id, created_at
            """.trimIndent()
        ).execute(Tuple.of(email, name, provider, providerId)).awaitSuspending()
        val row = rows.first()
        return User(
            id = row.getUUID("id"),
            email = row.getString("email"),
            name = row.getString("name"),
            provider = row.getString("provider"),
            providerId = row.getString("provider_id"),
            createdAt = row.getOffsetDateTime("created_at"),
        )
    }
}
