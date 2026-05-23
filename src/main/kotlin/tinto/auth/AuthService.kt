package tinto.auth

import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import tinto.db.UserRepository
import java.time.Duration

data class LoginRequest(val provider: String, val idToken: String, val name: String? = null)
data class AuthResponse(val token: String, val userId: String, val email: String)

@ApplicationScoped
class AuthService {

    @Inject
    lateinit var tokenVerifier: TokenVerifier

    @Inject
    lateinit var userRepository: UserRepository

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    lateinit var issuer: String

    suspend fun login(request: LoginRequest): AuthResponse {
        val claims = tokenVerifier.verify(request.provider, request.idToken)

        // Apple only sends name on the first login; fallback to frontend-supplied name
        val resolvedName = claims.name ?: request.name

        val user = userRepository.upsert(
            provider = request.provider.uppercase(),
            providerId = claims.sub,
            email = claims.email,
            name = resolvedName,
        )

        val token = Jwt.issuer(issuer)
            .subject(user.id.toString())
            .claim("email", user.email)
            .expiresIn(Duration.ofDays(30))
            .sign()

        return AuthResponse(token = token, userId = user.id.toString(), email = user.email)
    }
}
