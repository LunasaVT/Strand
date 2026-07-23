package re.lilith.strand.backend.util

import java.security.SecureRandom

object Ids {
    private val random = SecureRandom()
    private const val SOCKET_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun socketToken(length: Int = 32): String = build(length, SOCKET_ALPHABET)

    fun inviteCode(length: Int = 8): String = build(length, CODE_ALPHABET)

    private fun build(length: Int, alphabet: String): String {
        val sb = StringBuilder(length)
        repeat(length) { sb.append(alphabet[random.nextInt(alphabet.length)]) }
        return sb.toString()
    }
}
