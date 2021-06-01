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

package de.pierreschwang.nettypacket.event;

import de.pierreschwang.nettypacket.Packet;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvokableEventMethod {

    private final Object holder;
    private final Method method;
    private final Class<? extends Packet> packetClass;
    private final int ctxIndex;

    public InvokableEventMethod(Object holder, Method method, Class<? extends Packet> packetClass, int ctxIndex) {
        this.holder = holder;
        this.method = method;
        this.packetClass = packetClass;
        this.ctxIndex = ctxIndex;

        this.method.setAccessible(true);
    }

    public void invoke(Packet packet, ChannelHandlerContext ctx) throws InvocationTargetException, IllegalAccessException {
        if (!packetClass.equals(packet.getClass()))
            return;
        if (ctxIndex < 0) {
            method.invoke(holder, packet);
            return;
        }
        if (ctxIndex == 0) {
            method.invoke(holder, ctx, packet);
            return;
        }
        method.invoke(holder, packet, ctx);
    }

}
