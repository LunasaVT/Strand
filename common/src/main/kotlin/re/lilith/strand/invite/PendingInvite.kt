package re.lilith.strand.invite

data class PendingInvite(
    val inviteId: String,
    val fromPuid: String,
    val hostName: String,
    val socket: String,
)
