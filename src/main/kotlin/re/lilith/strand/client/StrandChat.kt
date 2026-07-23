package re.lilith.strand.client

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object StrandChat {
    val PREFIX = Component.literal("")
        .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
        .append(Component.literal("Strand").withStyle(ChatFormatting.AQUA))
        .append(Component.literal("]").withStyle(ChatFormatting.GRAY))
        .withStyle(ChatFormatting.WHITE)
}
