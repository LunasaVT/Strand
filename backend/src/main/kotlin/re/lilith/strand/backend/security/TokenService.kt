package re.lilith.strand.backend.security

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import re.lilith.strand.backend.Config
import java.time.Instant
import java.util.Date
import java.util.UUID

class TokenService(private val config: Config, keys: KeyManager) {

    private val kid = keys.signingKey.keyID
    private val signer = RSASSASigner(keys.signingKey)
    private val verifier = RSASSAVerifier(keys.signingKey.toPublicJWK())

    fun issueSession(userId: UUID, username: String): IssuedToken =
        sign(userId, username, SESSION_AUDIENCE, config.sessionTokenTtl.seconds, null)

    fun issueAccessToken(userId: UUID, username: String, nonce: String?): IssuedToken =
        sign(userId, username, config.audience, config.tokenTtl.seconds, nonce)

    fun verifySession(token: String): UUID? = verify(token)

    private fun sign(userId: UUID, username: String, audience: String, ttl: Long, nonce: String?): IssuedToken {
        val now = Instant.now()
        val builder = JWTClaimsSet.Builder()
            .issuer(config.issuer)
            .subject(userId.toString())
            .audience(audience)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(ttl)))
            .jwtID(UUID.randomUUID().toString())
            .claim("preferred_username", username)
        if (nonce != null) builder.claim("nonce", nonce)
        val jwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(kid).type(JOSEObjectType.JWT).build(),
            builder.build(),
        )
        jwt.sign(signer)
        return IssuedToken(jwt.serialize(), ttl)
    }

    private fun verify(token: String, audience: String = SESSION_AUDIENCE): UUID? = try {
        val jwt = SignedJWT.parse(token)
        val claims = jwt.jwtClaimsSet
        when {
            !jwt.verify(verifier) -> null
            claims.issuer != config.issuer -> null
            audience !in (claims.audience ?: emptyList()) -> null
            claims.expirationTime == null || claims.expirationTime.toInstant().isBefore(Instant.now()) -> null
            else -> UUID.fromString(claims.subject)
        }
    } catch (_: Exception) {
        null
    }

    companion object {
        const val SESSION_AUDIENCE = "strand-session"
    }
}
