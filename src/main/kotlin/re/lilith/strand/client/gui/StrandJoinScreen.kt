package re.lilith.strand.client.gui

import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class StrandJoinScreen(parent: Screen?) : StrandScreen(Component.literal("Join by code"), parent) {

    private var codeText: String = ""
    private lateinit var codeBox: EditBox

    override fun build() {
        val cx = width / 2
        val top = height / 3

        label(cx, top - 24, Component.literal("Enter an invite code"))

        codeBox = EditBox(font, cx - 102, top, 204, 20, Component.literal("Invite code")).apply {
            setMaxLength(16)
            setHint(Component.literal("e.g. ABCD2345"))
            value = codeText
            setResponder { codeText = it }
        }
        addRenderableWidget(codeBox)

        button(cx, top + 30, 204, Component.literal("Join")) { submit() }
        button(cx, top + 54, 204, Component.literal("Back")) { onClose() }

        setInitialFocus(codeBox)
    }

    private fun submit() {
        val code = codeText.trim()
        if (code.isEmpty()) return
        controller?.joinByCode(code)
        onClose()
    }
}
