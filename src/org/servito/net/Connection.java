package org.servito.net;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;


public class Connection implements Closeable {

    private int id;
    private Socket socket;
    private InputStreamReader in;
    private BufferedReader stringIn;
    private PrintWriter out;
    private BufferedOutputStream dataOut;
    private HashMap<String, String> properties = new HashMap<>();
    private boolean autoFlush;

    Connection(int id, Socket socket, boolean autoFlush) throws IOException{
        this.socket = socket;
        this.id = id;
        in = new InputStreamReader(socket.getInputStream());
        stringIn = new BufferedReader(in);
        out = new PrintWriter(socket.getOutputStream());
        dataOut = new BufferedOutputStream(socket.getOutputStream());
        this.autoFlush = autoFlush;
    }

    public void flush() throws IOException {
        out.flush();
        dataOut.flush();
    }

    public synchronized void send(String data) {
        out.println(data);
        if(autoFlush) out.flush();
    }

    public int read() throws IOException{
        return in.read();
    }

    public String readLine() throws IOException {
        return stringIn.readLine();
    }

    @Override
    public void close() {
        try {
            flush();
            socket.close();
            in.close();
            out.close();
            dataOut.close();
        } catch(IOException e) {}
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
