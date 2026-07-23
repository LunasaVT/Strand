package re.lilith.strand.platform.neoforge

import re.lilith.strand.StrandConfig
import re.lilith.strand.StrandState
import re.lilith.strand.backend.BackendClient
import re.lilith.strand.client.StrandClientHooks
import re.lilith.strand.eos.EosManager
import re.lilith.strand.session.SessionController
import gg.sona.eos.Eos
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.common.NeoForge
import org.slf4j.LoggerFactory

@Mod("strand")
class StrandNeoForgeMod(modBus: IEventBus, container: ModContainer) {

    private val logger = LoggerFactory.getLogger("strand")

    init {
        modBus.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        logger.info("EOS SDK version: ${Eos.version}")

        val config = StrandConfig.load(FMLPaths.CONFIGDIR.get())
        StrandState.config = config

        val backend = BackendClient("https://strand.lilith.re", config.oidcClientId, config.oidcRedirectUri)
        val hooks = StrandClientHooks()
        val controller = SessionController(config, backend, hooks)
        StrandState.controller = controller

        EosManager.init()

        StrandNeoForgeCommands.register(controller)
        StrandNeoForgeScreenButtons.register()

        var loggedIn = false
        NeoForge.EVENT_BUS.addListener { _: ClientTickEvent.Post ->
            if (!loggedIn) {
                loggedIn = true
                controller.ensureLogin()
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            controller.shutdown()
            EosManager.shutdown()
        })

        logger.info("Strand client initialized")
    }
}
