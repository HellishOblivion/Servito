package org.servito.net;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;


public class Connection implements Closeable {

    private int id;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private HashMap<String, String> properties = new HashMap<>();
    private boolean autoFlush;

    Connection(int id, Socket socket, boolean autoFlush) throws IOException{
        this.socket = socket;
        this.id = id;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        this.autoFlush = autoFlush;
    }

    public void flush() throws IOException {
        out.flush();
        out.flush();
    }

    public synchronized void write(byte[] data) throws IOException {
        out.write(data);
        if(autoFlush) out.flush();
    }

    public byte read() throws IOException{
        return in.readByte();
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

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

}
