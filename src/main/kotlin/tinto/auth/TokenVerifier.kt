package tinto.auth

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver

data class ProviderClaims(
    val sub: String,
    val email: String,
    val name: String?,
)

@ApplicationScoped
class TokenVerifier(
    @ConfigProperty(name = "tinto.auth.google.client-id") private val googleClientId: String,
    @ConfigProperty(name = "tinto.auth.apple.client-id") private val appleClientId: String,
) {
    private val googleJwks = HttpsJwks("https://www.googleapis.com/oauth2/v3/certs")
    private val appleJwks = HttpsJwks("https://appleid.apple.com/auth/keys")

    fun verify(provider: String, idToken: String): ProviderClaims {
        val (jwks, clientId, issuer) = when (provider.uppercase()) {
            "GOOGLE" -> Triple(googleJwks, googleClientId, "https://accounts.google.com")
            "APPLE"  -> Triple(appleJwks, appleClientId, "https://appleid.apple.com")
            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

        val keyResolver = HttpsJwksVerificationKeyResolver(jwks)
        val claims = JwtConsumerBuilder()
            .setVerificationKeyResolver(keyResolver)
            .setExpectedAudience(clientId)
            .setExpectedIssuer(issuer)
            .setRequireExpirationTime()
            .build()
            .processToClaims(idToken)

        return ProviderClaims(
            sub = claims.subject,
            email = claims.getStringClaimValue("email") ?: error("Missing email claim"),
            name = claims.getStringClaimValue("name"),
        )
    }
}
