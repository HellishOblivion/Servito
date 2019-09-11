package org.servito.net;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class Protocol {

    private char endChar;
    private Server server;

    public Protocol(char endChar) {
        this.endChar = endChar;
    }

    protected char getEndChar() {
        return endChar;
    }
    protected Server getServer() {
        return server;
    }
    void onServerConstructor(Server server){
        this.server = server;
    }
    protected void init() {}
    protected void onStart() {}
    protected void onStop() {}
    protected void onResume() {}
    protected void onClose() {}
    protected abstract boolean onNewConnection(Connection connection);
    protected abstract void onDeadConnection(Connection connection);
    protected abstract void onDataInBuffer(Packet packet);

}
