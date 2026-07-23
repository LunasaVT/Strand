package re.lilith.strand.client.gui

import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class StrandSettingsScreen(parent: Screen?) : StrandScreen(Component.literal("Settings"), parent) {

    override fun build() {
        val c = controller
        val cx = width / 2
        val top = height / 3

        val blocked = c?.invitesBlocked() == true
        label(cx, top - 28, Component.literal("Invite settings").withStyle(ChatFormatting.BOLD))
        label(
            cx, top - 14,
            Component.literal(if (blocked) "Others can only join with a code you share" else "Anyone can send you invites")
                .withStyle(ChatFormatting.GRAY),
        )

        val toggleText = if (blocked) "Invites: Blocked" else "Invites: Allowed"
        button(cx, top, 204, Component.literal(toggleText)) { c?.setInvitesBlocked(!blocked) }

        if (c != null && c.isHosting) {
            button(cx, top + 24, 204, Component.literal("Stop hosting")) { c.unhost() }
        }

        button(cx, top + 54, 204, Component.literal("Back")) { onClose() }
    }
}
