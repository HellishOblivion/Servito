package org.servito.net;

public class Packet {

    private Connection connection;
    private String content;

    Packet(Connection connection, String content) {
        this.connection = connection;
        this.content = content;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getContent() {
        return content;
    }

}
