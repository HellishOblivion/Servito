package org.servito.net;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Connection implements Closeable {

    private int id;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Map<String, Object> properties = new HashMap<>();
    private boolean autoFlush;
    private byte endValue;
    private int maxPacketLength;


    Connection(int id, Socket socket, boolean autoFlush, byte endValue, int maxPacketLength) throws IOException {
        this.socket = socket;
        this.id = id;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        this.autoFlush = autoFlush;
        this.endValue = endValue;
        this.maxPacketLength = maxPacketLength;
    }

    public void flush() throws IOException {
        out.flush();
    }

    public synchronized void write(byte[] data) throws IOException {
        out.write(data);
        if(autoFlush) out.flush();
    }

    public synchronized byte[] read() throws IOException {
        List<Byte> bytes0 = new ArrayList<>();
        byte next;
        int count = 0;
        while (true) {
            next = in.readByte();
            if(next != endValue) bytes0.add(next);
            else break;
            count++;
            if(count == maxPacketLength) throw new IOException("Sending too much data");
        }
        byte[] bytes = new byte[bytes0.size()];
        for(int i = 0; i < bytes0.size(); i++) {
            bytes[i] = bytes0.get(i);
        }
        return bytes;
    }

    @Override
    public void close() {
        try {
            flush();
            socket.close();
            in.close();
            out.close();
        } catch(IOException e) {
            //Not necessary handling
        }
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return socket.getInetAddress().getHostAddress();
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

}
