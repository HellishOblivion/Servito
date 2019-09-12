package org.servito.net;


public abstract class Protocol {

    private byte endValue;
    private Server server;

    public Protocol(byte endValue) {
        this.endValue = endValue;
    }

    protected final byte getEndValue() {
        return endValue;
    }
    protected final Server getServer() {
        return server;
    }
    void onServerConstructor(Server server){
        this.server = server;
    }
    protected void init() {}
    protected void onStart() {}
    protected void onStop() {}
    protected abstract boolean onNewConnection(Connection connection);
    protected abstract void onDeadConnection(Connection connection);
    protected abstract void onDataInBuffer(Packet packet);

}
