package org.servito.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.IOException;


public class DataParser {

    public static byte[] parseIntToBytes(int value, int amount) {
        if(amount > 4) throw new IllegalArgumentException("amount of bytes cannot be higher than 4");
        byte[] bytes = new byte[amount];
        int shift = 0;
        for(int i = 0; i < amount; i++) {
            bytes[i] = ((byte) (value >> shift));
            shift += 8;
        }
        return bytes;
    }

    public static int parseBytesToInt(byte[] bytes) {
        if(bytes.length > 4) throw new IllegalArgumentException("amount of bytes cannot be higher than 4");
        int value = 0;
        int shift = 0;
        for (int i = 0; i < bytes.length; i++) {
            if(i == bytes.length - 1) value += bytes[i] << shift;
            else {
                value += (bytes[i] & 0xFF) << shift;
                shift += 8;
            }
        }
        return value;
    }

    public static byte[] parseObjectToBytes(Serializable obj) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(obj);
        } catch(IOException e) {
            //Not necessary handling
        }
        return byteStream.toByteArray();
    }

    public static Object parseBytesToObject(byte[] bytes) throws ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            obj = objectStream.readObject();
        } catch(IOException e) {
            //Not necessary handling
        }
        return obj;
    }

}
