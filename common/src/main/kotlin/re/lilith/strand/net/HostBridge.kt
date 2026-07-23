package re.lilith.strand.net

import java.net.InetAddress
import java.net.Socket

class HostBridge(private val socketName: String, private val lanPort: Int) {

    fun start() {
        P2PHub.host(socketName) { remote, channel ->
            val tcp = Socket(InetAddress.getLoopbackAddress(), lanPort)
            val stream = TcpP2PStream(socketName, remote, channel, tcp)
            stream.start()
            stream
        }
    }

    fun stop() {
        P2PHub.unhost(socketName)
    }
}
