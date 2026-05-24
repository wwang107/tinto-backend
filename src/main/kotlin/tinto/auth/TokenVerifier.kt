package tinto.auth

import com.google.auth.oauth2.TokenVerifier as GoogleAuthTokenVerifier
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty

data class ProviderClaims(
    val sub: String,
    val email: String,
    val name: String?,
)

interface TokenVerifier {
    fun verify(provider: String, token: String): ProviderClaims
}

@ApplicationScoped
class GoogleTokenVerifier {

    @ConfigProperty(name = "tinto.auth.google.client-id")
    lateinit var clientId: String

    private lateinit var verifier: GoogleAuthTokenVerifier

    @PostConstruct
    fun init() {
        verifier = GoogleAuthTokenVerifier.newBuilder()
            .setAudience(clientId)
            .setIssuer("https://accounts.google.com")
            .build()
    }

    fun verify(token: String): ProviderClaims {
        val jws = try {
            verifier.verify(token)
        } catch (e: GoogleAuthTokenVerifier.VerificationException) {
            throw IllegalArgumentException("Invalid or expired Google ID token", e)
        }
        val payload = jws.payload
        return ProviderClaims(
            sub = payload.subject,
            email = payload["email"] as String,
            name = payload["name"] as? String,
        )
    }
}

@ApplicationScoped
class AppleTokenVerifier {
    fun verify(token: String): ProviderClaims {
        TODO("Not yet implemented")
    }
}

@ApplicationScoped
class TokenVerifierDispatcher : TokenVerifier {

    @Inject
    lateinit var googleVerifier: GoogleTokenVerifier

    @Inject
    lateinit var appleVerifier: AppleTokenVerifier

    override fun verify(provider: String, token: String): ProviderClaims = when (provider.uppercase()) {
        "GOOGLE" -> googleVerifier.verify(token)
        "APPLE" -> appleVerifier.verify(token)
        else -> throw IllegalArgumentException("Unknown provider: $provider")
    }
}
