package org.servito.net;

public class Packet {

    private Connection connection;
    private byte[] content;

    Packet(Connection connection, byte[] content) {
        this.connection = connection;
        this.content = content;
    }

    public Connection getConnection() {
        return connection;
    }

    public byte[] getContent() {
        return content;
    }

}
