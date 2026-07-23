package re.lilith.strand.backend.security

data class IssuedToken(val token: String, val expiresIn: Long)
