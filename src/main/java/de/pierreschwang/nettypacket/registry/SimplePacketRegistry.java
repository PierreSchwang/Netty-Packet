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

package de.pierreschwang.nettypacket.registry;

import de.pierreschwang.nettypacket.Packet;
import de.pierreschwang.nettypacket.exception.PacketRegistrationException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.lang.reflect.InvocationTargetException;

public class SimplePacketRegistry implements IPacketRegistry {

    private final Int2ObjectOpenHashMap<RegisteredPacket> packets = new Int2ObjectOpenHashMap<>();

    @Override
    public void registerPacket(int packetId, Packet packet) throws PacketRegistrationException {
        registerPacket(packetId, packet.getClass());
    }

    @Override
    public void registerPacket(int packetId, Class<? extends Packet> packet) throws PacketRegistrationException {
        if (containsPacketId(packetId)) {
            throw new PacketRegistrationException("PacketID is already in use");
        }
        try {
            RegisteredPacket registeredPacket = new RegisteredPacket(packet);
            this.packets.put(packetId, registeredPacket);
        } catch (NoSuchMethodException e) {
            throw new PacketRegistrationException("Failed to register packet", e);
        }
    }

    @Override
    public int getPacketId(Class<? extends Packet> packetClass) {
        for (ObjectIterator<Int2ObjectMap.Entry<RegisteredPacket>> it = packets.int2ObjectEntrySet().fastIterator(); it.hasNext(); ) {
            Int2ObjectMap.Entry<RegisteredPacket> entry = it.next();
            if (entry.getValue().getPacketClass().equals(packetClass)) {
                return entry.getIntKey();
            }
        }
        return -1;
    }

    @Override
    public <T extends Packet> T constructPacket(int packetId) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return (T) packets.get(packetId).getConstructor().newInstance();
    }

    @Override
    public boolean containsPacketId(int id) {
        return packets.containsKey(id);
    }

}
