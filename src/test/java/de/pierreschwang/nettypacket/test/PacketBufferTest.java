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

package de.pierreschwang.nettypacket.test;

import de.pierreschwang.nettypacket.buffer.PacketBuffer;
import de.pierreschwang.nettypacket.test.dummy.SimpleStringPacket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PacketBufferTest {

    private PacketBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new PacketBuffer();
    }

    @AfterEach
    void tearDown() {
        buffer.release();
        buffer = null;
    }

    @Test
    void testPacketList() {
        List<String> listOfData = Arrays.asList(
                "foo", "bar", "hello", "world"
        );
        // Write data into buffer by mapping data to packet which implements encoder
        buffer.writeCollection(listOfData.stream().map(SimpleStringPacket::new).collect(Collectors.toList()));

        // Read all the data from the buffer and convert to string
        List<SimpleStringPacket> readDataAsPacket = buffer.readCollection(SimpleStringPacket::new);
        List<String> readDataAsStrings = readDataAsPacket.stream().map(SimpleStringPacket::getData).collect(Collectors.toList());

        // should equal
        Assertions.assertIterableEquals(listOfData, readDataAsStrings);
    }

}