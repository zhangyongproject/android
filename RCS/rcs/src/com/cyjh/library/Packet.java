package com.cyjh.library;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet {
    private static final byte HEAD_1 = (byte) 0xFF;
    private static final byte HEAD_2 = (byte) 0xFE;

    public static ByteBuffer pack(byte type) {
        ByteBuffer result = ByteBuffer.allocate(7);
        result.order(ByteOrder.nativeOrder());
        result.put((byte) 0xff);
        result.put((byte) 0xfe);
        result.put((byte) type);
        result.putInt(0);
        return result;
    }

    public static ByteBuffer pack(byte type, byte[] data) {
        ByteBuffer result = ByteBuffer.allocate(data.length + 7);
        result.order(ByteOrder.nativeOrder());
        result.put((byte) 0xff);
        result.put((byte) 0xfe);
        result.put((byte) type);
        result.putInt(data.length);
        result.put(data);
        return result;
    }

    public static ByteBuffer pack(byte type, ByteBuffer data) {
        ByteBuffer result = ByteBuffer.allocate(data.limit() + 7);
        result.order(ByteOrder.nativeOrder());
        result.put((byte) 0xff);
        result.put((byte) 0xfe);
        result.put((byte) type);
        result.putInt(data.limit());
        result.put(data);
        return result;
    }

    public static ByteBuffer unpack(final byte[] data, final int offset, final int length) {
        if (offset + 7 >= data.length) {
            return null;
        }
        if (data[offset + 0] != HEAD_1 || data[offset + 1] != HEAD_2) {
            return null;
        }
        int count = 0;
        count |= (data[offset + 3] & 0xff) << 0;
        count |= (data[offset + 4] & 0xff) << 8;
        count |= (data[offset + 5] & 0xff) << 16;
        count |= (data[offset + 6] & 0xff) << 24;
        if (length < count + 7) {
            return null;
        }
        return ByteBuffer.wrap(data, offset + 2, count + 5);

    }

    public static ByteBuffer unpack(DatagramPacket packet) {
        return unpack(packet.getData(), packet.getOffset(), packet.getLength());
    }

    public static ByteBuffer unpack(DataInputStream in) throws IOException {
        if (in.readByte() != HEAD_1) {
            return null;
        }
        if (in.readByte() != HEAD_2) {
            return null;
        }
        byte type = in.readByte();
        int length = Utilities.reverse(in.readInt());
        if (length < 0) {
            return null;
        } else if (length == 0) {
            ByteBuffer buffer = pack(type);
            buffer.position(0);
            return buffer;
        } else {
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            ByteBuffer buffer = pack(type, bytes);
            buffer.position(0);
            return buffer;
        }
    }
}
