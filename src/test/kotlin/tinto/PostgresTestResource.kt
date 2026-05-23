package tinto

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer

class PostgresTestResource : QuarkusTestResourceLifecycleManager {

    companion object {
        private val container = PostgreSQLContainer("postgres:18-alpine")
    }

    override fun start(): Map<String, String> {
        container.start()
        val reactiveUrl = container.jdbcUrl.removePrefix("jdbc:")
        return mapOf(
            "quarkus.datasource.jdbc.url" to container.jdbcUrl,
            "quarkus.datasource.reactive.url" to reactiveUrl,
            "quarkus.datasource.username" to container.username,
            "quarkus.datasource.password" to container.password,
        )
    }

    override fun stop() {
        container.stop()
    }
}
