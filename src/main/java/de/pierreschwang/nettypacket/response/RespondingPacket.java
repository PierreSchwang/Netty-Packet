/*
 * Copyright (c) 2021, Pierre Maurice Schwang <mail@pschwang.eu> - MIT
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package de.pierreschwang.nettypacket.response;

import de.pierreschwang.nettypacket.Packet;
import io.netty.channel.ChannelOutboundInvoker;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RespondingPacket<T extends Packet> {

    private static final Long2ObjectMap<PendingResponse<?>> pendingResponses = new Long2ObjectArrayMap<>();
    private final Packet toSend;
    private final long timeout;
    private final Class<T> responseType;
    private final Consumer<T> callback;

    public RespondingPacket(Packet toSend, long timeout, Class<T> responseType, Consumer<T> callback) {
        this.toSend = toSend;
        this.timeout = timeout;
        this.responseType = responseType;
        this.callback = callback;
    }

    public RespondingPacket(Packet toSend, Class<T> responseType, Consumer<T> callback) {
        this(toSend, TimeUnit.SECONDS.toMillis(10), responseType, callback);
    }

    public void send(ChannelOutboundInvoker invoker) {
        invoker.writeAndFlush(toSend);
        pendingResponses.put(toSend.getSessionId(), new PendingResponse<>(responseType, callback, timeout));
    }

    public static void callReceive(Packet packet) {
        if (!pendingResponses.containsKey(packet.getSessionId())) {
            return;
        }
        pendingResponses.get(packet.getSessionId()).callResponseReceived(packet);
        pendingResponses.remove(packet.getSessionId());

        cleanupPendingResponses();
    }

    private static void cleanupPendingResponses() {
        pendingResponses.forEach((sessionId, pendingResponse) -> {
            if (!pendingResponse.isExpired()) {
                return;
            }
            RespondingPacket.pendingResponses.remove(sessionId.longValue());
        });
    }

}
