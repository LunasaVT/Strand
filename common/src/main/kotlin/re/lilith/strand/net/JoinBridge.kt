package re.lilith.strand.net

import gg.sona.eos.common.ProductUserId
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger

class JoinBridge(private val hostPuid: ProductUserId, private val socketName: String) {

    private val logger = LoggerFactory.getLogger("strand/join")
    private val channelSeq = AtomicInteger(0)
    private lateinit var serverSocket: ServerSocket

    val port: Int get() = serverSocket.localPort

    fun start() {
        serverSocket = ServerSocket(0, 16, InetAddress.getLoopbackAddress())
        Thread(::acceptLoop, "strand-join-accept").apply { isDaemon = true; start() }
    }

    private fun acceptLoop() {
        while (!serverSocket.isClosed) {
            val tcp = try {
                serverSocket.accept()
            } catch (_: Exception) {
                break
            }
            val channel = channelSeq.getAndIncrement() and 0xff
            val stream = TcpP2PStream(socketName, hostPuid, channel, tcp)
            P2PHub.openStream(hostPuid, socketName, channel, stream)
            stream.start()
            logger.info("Bridged local connection on channel {} to host {}", channel, hostPuid)
        }
    }

    fun stop() {
        runCatching { serverSocket.close() }
    }
}
