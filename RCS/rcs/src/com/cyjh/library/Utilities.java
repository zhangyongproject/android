package com.cyjh.library;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

public class Utilities {

    private static boolean mLogEnabled = true;

    public static File getExternalStorageFile(String path) {
        File file = Environment.getExternalStorageDirectory();
        if (!file.exists()) {
            file = new File("/", "sdcard");
        }
        return new File(file, path);
    }

    public static String format(String format, Object... objects) {
        if (format == null) {
            return null;
        }
        if (objects == null || objects.length == 0) {
            return format;
        }
        return String.format(Locale.ENGLISH, format, objects);
    }

    public static void log(Object tag, String format, Object... objects) {
        log(tag, null, format, objects);
    }

    public static void log(Object tag, Exception e, String format, Object... objects) {
        if (!mLogEnabled) {
            return;
        }
        format = format(format, objects);
        if (e == null) {
            if (format == null) {
                return;
            } else {
                Log.d(getTag(tag), format);
            }
        } else {
            if (format == null) {
                Log.d(getTag(tag), e.getMessage(), e);
            } else {
                Log.d(getTag(tag), format, e);
            }
        }

    }

    public static void log(Object tag, Exception e) {
        log(tag, e, null);
    }

    private static String getTag(Object tag) {
        String result = "Logger:";
        if (tag == null) {
            return result.concat("unknown");
        } else if (tag instanceof String) {
            return result.concat(tag.toString());
        } else if (tag instanceof Class<?>) {
            return result.concat(((Class<?>) tag).getSimpleName());
        } else {
            return result.concat(tag.getClass().getSimpleName());
        }
    }

    public static byte[] read(File file) throws IOException {
        if (file.exists() && file.canRead()) {
            byte bytes[] = new byte[(int) file.length()];
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            in.readFully(bytes);
            in.close();
            return bytes;
        } else {
            return null;
        }
    }

    public static void read(File file, ByteArrayOutputStream buffer) throws IOException {
        InputStream in = new FileInputStream(file);
        copy(in, buffer);
        in.close();

    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        for (int length = in.read(buffer); length != -1; length = in.read(buffer)) {
            out.write(buffer, 0, length);
        }

    }

    public static int reverse(int value) {
        return ((value & 0xff) << 24) | (((value >>> 8) & 0xff) << 16) | (((value >>> 16) & 0xff) << 8) | ((value >>> 24) & 0xff);
    }

    public static short reverse(short value) {
        return (short) (((value & 0xff) << 8) | ((value >>> 8) & 0xff));
    }

    public static String getString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte item : bytes) {
            if (builder.length() > 0) {
                builder.append(',').append(' ');
            }
            builder.append(format("%02x", item));
        }
        return builder.toString();
    }

    public static boolean write(InputStream in, File file, int count) {
        file.getParentFile().mkdirs();
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            return false;
        }
        byte[] bytes = new byte[4096];
        int length;
        while (count > 0) {
            try {
                if (count >= bytes.length) {
                    length = bytes.length;
                } else {
                    length = count;
                }
                length = in.read(bytes, 0, length);
                if (length >= 0) {
                    out.write(bytes, 0, length);
                } else {
                    break;
                }
                count = count - length;
                Utilities.log(Utilities.class, "count=%d", count);
            } catch (IOException e) {
                Utilities.log(Utilities.class, e);
                break;
            }
        }
        try {
            out.close();
        } catch (Exception e) {
            Utilities.log(Utilities.class, e);
        }
        return count == 0;
    }

    public static boolean close(Closeable closeable) {
        try {
            closeable.close();
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    public static String shell(String command, Object... objects) {
        command = format(command, objects);
        try {
            Utilities.log(Utilities.class, command);
            Process process = Runtime.getRuntime().exec(command);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(process.getInputStream(), out);
            return out.toString();
        } catch (Exception e) {
            Utilities.log(Utilities.class, e);
            return null;
        }
    }

    public static String execute(String... args) {
        try {
            Utilities.log(Utilities.class, Arrays.toString(args));
            Process process = Runtime.getRuntime().exec(args);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(process.getInputStream(), out);
            return out.toString();
        } catch (Exception e) {
            Utilities.log(Utilities.class, e);
            return null;
        }
    }

    public static String getString(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes);
    }

    public static byte[] getBytes(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }
}
