package re.lilith.strand.backend

import org.slf4j.LoggerFactory
import java.util.UUID

object Seeder {

    private val logger = LoggerFactory.getLogger("strand/seed")
    private val DEMO_UUID = UUID.fromString("00000000-0000-0000-0000-0000000000de")
    private const val DEMO_NAME = "StrandDemo"
    private const val DEMO_PUID = "0002000000000000000000000000demo"

    fun run(config: Config, services: AppServices) {
        if (!config.seed) return
        if (services.users.me(DEMO_UUID) != null) {
            logger.info("Seed data already present, skipping")
            return
        }
        services.users.upsert(DEMO_UUID, DEMO_NAME)
        services.users.linkPuid(DEMO_UUID, DEMO_PUID)
        val session = services.sessions.createOrReplace(DEMO_UUID, DEMO_PUID, null)
        logger.info("Seeded demo user {} with invite code {}", DEMO_NAME, session.inviteCode)
    }
}
