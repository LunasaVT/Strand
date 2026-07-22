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

import gg.sona.eos.common.ProductUserId
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class TcpP2PStream(
    private val socketName: String,
    private val remote: ProductUserId,
    private val channel: Int,
    private val tcp: Socket,
) : StreamHandler {

    private val inbound = LinkedBlockingQueue<ByteArray>()
    private val closed = AtomicBoolean(false)
    private val poison = ByteArray(0)

    fun start() {
        tcp.tcpNoDelay = true
        Thread(::readLoop, "strand-p2p-read-$channel").apply { isDaemon = true; start() }
        Thread(::writeLoop, "strand-p2p-write-$channel").apply { isDaemon = true; start() }
    }

    private fun readLoop() {
        val buffer = ByteArray(1169)
        try {
            val input = tcp.getInputStream()
            while (!closed.get()) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) P2PHub.send(remote, socketName, channel, buffer.copyOf(read))
            }
        } catch (_: Exception) {
        } finally {
            closeLocal(notifyPeer = true)
        }
    }

    private fun writeLoop() {
        try {
            val output = tcp.getOutputStream()
            while (true) {
                val chunk = inbound.take()
                if (chunk === poison) break
                output.write(chunk)
                output.flush()
            }
        } catch (_: Exception) {
        } finally {
            closeLocal(notifyPeer = true)
        }
    }

    override fun onData(bytes: ByteArray) {
        if (!closed.get()) inbound.add(bytes)
    }

    override fun onClose() {
        closeLocal(notifyPeer = false)
    }

    private fun closeLocal(notifyPeer: Boolean) {
        if (!closed.compareAndSet(false, true)) return
        inbound.add(poison)
        runCatching { tcp.close() }
        if (notifyPeer) P2PHub.closeStream(remote, socketName, channel)
    }
}
