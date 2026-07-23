package re.lilith.strand.backend.security

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import re.lilith.strand.backend.db.SigningKeys
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class KeyManager private constructor(val signingKey: RSAKey) {

    val publicJwkSet: JWKSet = JWKSet(signingKey.toPublicJWK())

    fun publicJwksJson(): String = publicJwkSet.toString()

    companion object {
        fun loadOrCreate(): KeyManager {
            val existing = transaction {
                SigningKeys.selectAll()
                    .firstOrNull { it[SigningKeys.active] }
                    ?.get(SigningKeys.jwkJson)
            }
            if (existing != null) {
                return KeyManager(RSAKey.parse(existing))
            }
            val generated = RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .keyIDFromThumbprint(true)
                .generate()
            transaction {
                SigningKeys.insert {
                    it[kid] = generated.keyID
                    it[jwkJson] = generated.toJSONString()
                    it[active] = true
                    it[createdAt] = LocalDateTime.now()
                }
            }
            return KeyManager(generated)
        }
    }
}
