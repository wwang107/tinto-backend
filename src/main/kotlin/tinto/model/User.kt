package tinto.model

import java.time.OffsetDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val name: String?,
    val provider: String,
    val providerId: String,
    val createdAt: OffsetDateTime,
)
