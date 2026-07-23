package re.lilith.strand.platform.fabric

import re.lilith.strand.client.gui.StrandHubScreen
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.PauseScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.network.chat.Component

object StrandScreenButtons {
    fun register() {
        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, _ ->
            if (screen is TitleScreen || screen is JoinMultiplayerScreen || screen is PauseScreen) {
                val button = Button.builder(Component.literal("Strand")) { _ ->
                    client.setScreenAndShow(StrandHubScreen(screen))
                }.bounds(scaledWidth - 104, 4, 100, 20).build()
                Screens.getWidgets(screen).add(button)
            }
        }
    }
}
