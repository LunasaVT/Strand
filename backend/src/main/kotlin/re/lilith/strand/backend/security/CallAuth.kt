package re.lilith.strand.backend.security

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.response.respond
import re.lilith.strand.backend.model.ErrorResponse
import java.util.UUID

suspend fun ApplicationCall.requireUser(tokens: TokenService): UUID? {
    val header = request.header("Authorization")
    val token = header?.removePrefix("Bearer ")?.trim()
    if (token.isNullOrEmpty()) {
        respond(HttpStatusCode.Unauthorized, ErrorResponse("unauthorized", "Missing bearer token"))
        return null
    }
    val userId = tokens.verifySession(token)
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, ErrorResponse("unauthorized", "Invalid or expired token"))
        return null
    }
    return userId
}
