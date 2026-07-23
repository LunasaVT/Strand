package re.lilith.strand.client.gui

import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class StrandInviteScreen(parent: Screen?) : StrandScreen(Component.literal("Invite a player"), parent) {

    private var nameText: String = ""
    private lateinit var nameBox: EditBox

    override fun build() {
        val cx = width / 2
        val top = height / 3

        label(cx, top - 24, Component.literal("Invite a player by name"))

        nameBox = EditBox(font, cx - 102, top, 204, 20, Component.literal("Player name")).apply {
            setMaxLength(16)
            setHint(Component.literal("Minecraft username"))
            value = nameText
            setResponder { nameText = it }
        }
        addRenderableWidget(nameBox)

        button(cx, top + 30, 204, Component.literal("Send invite")) { submit() }
        button(cx, top + 54, 204, Component.literal("Back")) { onClose() }

        setInitialFocus(nameBox)
    }

    private fun submit() {
        val name = nameText.trim()
        if (name.isEmpty()) return
        controller?.invite(name)
        onClose()
    }
}
