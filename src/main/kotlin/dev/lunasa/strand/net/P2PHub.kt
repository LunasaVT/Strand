/*
 * Strand - Open your Minecraft world to anyone, anywhere.
 * Copyright (C) 2026  Lunasa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.lunasa.strand.net

import dev.lunasa.strand.eos.EosManager
import gg.sona.eos.EosResult
import gg.sona.eos.common.ProductUserId
import gg.sona.eos.p2p.EosP2PSocketId
import gg.sona.eos.p2p.EosPacketReliability
import gg.sona.eos.p2p.EosRelayControl
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap


private data class StreamKey(val remoteRaw: Long, val socket: String, val channel: Int)

object P2PHub {
    private val logger = LoggerFactory.getLogger("strand/p2p")

    private const val OP_OPEN: Byte = 1
    private const val OP_DATA: Byte = 2
    private const val OP_CLOSE: Byte = 3
    private const val MAX_PAYLOAD = 1170 - 1

    private val handlers = ConcurrentHashMap<StreamKey, StreamHandler>()
    private val acceptedSockets = ConcurrentHashMap.newKeySet<String>()
    private val inboundFactories = ConcurrentHashMap<String, (ProductUserId, Int) -> StreamHandler>()

    @Volatile private var installed = false

    fun install() {
        if (installed) return
        installed = true
        EosManager.call {
            val p2p = EosManager.platform.p2p
            logger.info("P2P relay control -> AllowRelays: {}", p2p.setRelayControl(EosRelayControl.AllowRelays))

            p2p.addNotifyPeerConnectionRequest(EosManager.localUser, null) { info ->
                val name = info.socketId.name
                if (acceptedSockets.contains(name)) {
                    val result = EosManager.platform.p2p.acceptConnection(info.localUserId, info.remoteUserId, info.socketId)
                    logger.info("Accepted P2P from {} on '{}' (len {}): {}", info.remoteUserId, name, name.length, result)
                } else {
                    logger.warn("P2P request from {} on UNKNOWN socket '{}' (len {}); hosting {}", info.remoteUserId, name, name.length, acceptedSockets)
                }
            }
            p2p.addNotifyPeerConnectionEstablished(EosManager.localUser, null) { info ->
                logger.info("P2P established with {} on '{}' ({} / {})", info.remoteUserId, info.socketId.name, info.type, info.networkType)
            }
            p2p.addNotifyPeerConnectionClosed(EosManager.localUser, null) { info ->
                logger.info("P2P closed with {} on '{}': {}", info.remoteUserId, info.socketId.name, info.reason)
                dropStreamsFor(info.remoteUserId.raw, info.socketId.name)
            }
        }
        EosManager.addFrameHook(::pollReceive)
    }

    fun host(socket: String, factory: (ProductUserId, Int) -> StreamHandler) {
        acceptedSockets.add(socket)
        inboundFactories[socket] = factory
        logger.info("Hosting P2P socket '{}' (len {})", socket, socket.length)
    }

    fun unhost(socket: String) {
        inboundFactories.remove(socket)
        acceptedSockets.remove(socket)
        handlers.keys.filter { it.socket == socket }.forEach { handlers.remove(it) }
        EosManager.call { EosManager.platform.p2p.closeConnections(EosManager.localUser, EosP2PSocketId(socket)) }
    }

    fun openStream(remote: ProductUserId, socket: String, channel: Int, handler: StreamHandler) {
        acceptedSockets.add(socket)
        handlers[StreamKey(remote.raw, socket, channel)] = handler
        logger.info("Opening P2P stream to {} on '{}' (len {}) channel {}", remote, socket, socket.length, channel)
        sendFrame(remote, socket, channel, OP_OPEN, null)
    }

    fun send(remote: ProductUserId, socket: String, channel: Int, bytes: ByteArray) {
        var offset = 0
        while (offset < bytes.size) {
            val len = minOf(MAX_PAYLOAD, bytes.size - offset)
            sendFrame(remote, socket, channel, OP_DATA, bytes.copyOfRange(offset, offset + len))
            offset += len
        }
    }

    fun closeStream(remote: ProductUserId, socket: String, channel: Int) {
        if (handlers.remove(StreamKey(remote.raw, socket, channel)) != null) {
            sendFrame(remote, socket, channel, OP_CLOSE, null)
        }
    }

    private fun sendFrame(remote: ProductUserId, socket: String, channel: Int, op: Byte, payload: ByteArray?) {
        val frame = ByteArray((payload?.size ?: 0) + 1)
        frame[0] = op
        if (payload != null) System.arraycopy(payload, 0, frame, 1, payload.size)
        EosManager.call {
            try {
                val result = EosManager.platform.p2p.sendPacket(
                    localUserId = EosManager.localUser,
                    remoteUserId = remote,
                    socketId = EosP2PSocketId(socket),
                    channel = channel,
                    data = frame,
                    reliability = EosPacketReliability.ReliableOrdered,
                )
                if (result != EosResult.Success) {
                    logger.warn("sendPacket to {} on '{}' ch {} op {} -> {}", remote, socket, channel, op, result)
                }
            } catch (e: Throwable) {
                logger.error("sendPacket threw for op {} to {} on '{}' ch {}", op, remote, socket, channel, e)
            }
        }
    }

    private fun pollReceive() {
        if (!EosManager.isLoggedIn) return
        val p2p = EosManager.platform.p2p
        while (true) {
            val packet = p2p.receivePacket(EosManager.localUser) ?: break
            if (packet.data.isEmpty()) continue
            val op = packet.data[0]
            val socket = packet.socketId.name
            val key = StreamKey(packet.remoteUserId.raw, socket, packet.channel)

            if (op == OP_CLOSE) {
                handlers.remove(key)?.onClose()
                continue
            }

            var handler = handlers[key]
            if (handler == null) {
                val factory = inboundFactories[socket]
                if (factory != null) {
                    handler = runCatching { factory(packet.remoteUserId, packet.channel) }
                        .onFailure { logger.error("Failed to open inbound stream on '{}'", socket, it) }
                        .getOrNull()
                    if (handler != null) {
                        handlers[key] = handler
                        logger.info("Bridged inbound P2P stream from {} channel {} on '{}'", packet.remoteUserId, packet.channel, socket)
                    }
                }
            }
            if (handler != null && op == OP_DATA) {
                handler.onData(packet.data.copyOfRange(1, packet.data.size))
            }
        }
    }

    private fun dropStreamsFor(remoteRaw: Long, socket: String) {
        handlers.keys.filter { it.remoteRaw == remoteRaw && it.socket == socket }.forEach { key ->
            handlers.remove(key)?.onClose()
        }
    }
}
