package tinto.dev

import io.smallrye.jwt.build.Jwt
import java.io.File
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.util.Base64

fun main() {
    val projectDir = System.getProperty("projectDir") ?: "."
    val keyFile = File("$projectDir/src/main/resources/privateKey.pem")

    require(keyFile.exists()) {
        "privateKey.pem not found at ${keyFile.absolutePath}. Run: openssl genrsa | openssl pkcs8 -topk8 -nocrypt > src/main/resources/privateKey.pem"
    }

    val pem = keyFile.readText()
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace(Regex("\\s"), "")
    val privateKey = KeyFactory.getInstance("RSA")
        .generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)))

    val token = Jwt.issuer("https://tinto.app")
        .subject("dev-user")
        .claim("email", "dev@tinto.app")
        .expiresIn(Duration.ofDays(1))
        .sign(privateKey)

    println(token)
}
