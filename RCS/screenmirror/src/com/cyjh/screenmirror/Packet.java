package com.cyjh.screenmirror;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class Packet {
    private int type;
    private int length;
    private byte[] data;

    public int getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public void read(DataInputStream in) throws IOException {
        int head1 = 0;
        int head2 = 0;
        while (true) {
            if (head1 == 0xff && head2 == 0xfe) {
                break;
            }
            head1 = head2;
            head2 = in.read();
            if (head2 == -1) {
                throw new EOFException();
            }
        }
        type = in.read();
        if (type == -1) {
            throw new EOFException();
        }
        length = in.readInt();
        if (data == null || data.length < length) {
            data = new byte[length];
        }
        in.readFully(data);
    }

    public void write(DataOutputStream out) throws IOException {
        out.write(0xff);
        out.write(0xf0);
        out.write(type);
        out.writeInt(length);
        if (length > 0) {
            out.write(data, 0, length);
        }
    }

    public byte getByte(int offset) {
        return 0;
    }

    public int getInteger(int offset) {
        return 0;
    }

    public short getShort(int offset) {
        return 0;
    }

    public int getLength() {
        return length;
    }
}
