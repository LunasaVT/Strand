package re.lilith.strand

import re.lilith.strand.session.SessionController

object StrandState {

    @Volatile
    var controller: SessionController? = null

    @Volatile
    var config: StrandConfig = StrandConfig()

    @JvmStatic
    fun onLanOpened(port: Int) {
        if (port <= 0) return
        if (!config.autoHostOnLanOpen) return
        controller?.host()
    }

    @JvmStatic
    fun onLanClosed() {
        if (controller?.isHosting == true) controller?.unhost()
    }
}
