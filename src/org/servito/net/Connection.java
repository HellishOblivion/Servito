package org.servito.net;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class Connection implements Closeable {

    private int id;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Map<String, Object> properties = new HashMap<>();
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

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

}
