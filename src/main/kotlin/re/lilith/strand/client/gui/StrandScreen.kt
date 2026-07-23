package re.lilith.strand.client.gui

import re.lilith.strand.StrandState
import re.lilith.strand.session.SessionController
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

abstract class StrandScreen(title: Component, protected val parent: Screen?) : Screen(title) {

    protected val controller: SessionController? get() = StrandState.controller

    private var listening = false
    private val refresh: () -> Unit = {
        val mc = Minecraft.getInstance()
        mc.execute { if (mc.gui.screen() === this) rebuildWidgets() }
    }

    override fun init() {
        if (!listening) {
            controller?.addListener(refresh)
            listening = true
        }
        build()
    }

    protected abstract fun build()

    override fun removed() {
        if (listening) {
            controller?.removeListener(refresh)
            listening = false
        }
        super.removed()
    }

    override fun onClose() {
        if (parent != null) {
            minecraft.setScreenAndShow(parent)
        } else {
            minecraft.gui.setScreen(null)
        }
    }

    protected fun label(centerX: Int, y: Int, text: Component): StringWidget {
        val widget = StringWidget(centerX - font.width(text) / 2, y, font.width(text), 9, text, font)
        return addRenderableWidget(widget)
    }

    protected fun button(centerX: Int, y: Int, width: Int, text: Component, onPress: () -> Unit): Button {
        val widget = Button.builder(text) { _ -> onPress() }.bounds(centerX - width / 2, y, width, 20).build()
        return addRenderableWidget(widget)
    }
}
