package re.lilith.strand.platform.neoforge

import re.lilith.strand.client.gui.StrandHubScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.PauseScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.client.event.ScreenEvent
import net.neoforged.neoforge.common.NeoForge

object StrandNeoForgeScreenButtons {
    fun register() {
        NeoForge.EVENT_BUS.addListener { event: ScreenEvent.Init.Post ->
            val screen = event.screen
            if (screen is TitleScreen || screen is JoinMultiplayerScreen || screen is PauseScreen) {
                val client = Minecraft.getInstance()
                val button = Button.builder(Component.literal("Strand")) { _ ->
                    client.setScreenAndShow(StrandHubScreen(screen))
                }.bounds(screen.width - 104, 4, 100, 20).build()
                event.addListener(button)
            }
        }
    }
}
