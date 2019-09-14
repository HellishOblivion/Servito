package org.servito.net;

import org.servito.utils.Buffer;
import java.io.IOException;


public class AsyncReader extends Thread {

    private Connection connection;
    private Buffer<Packet> buffer;
    private Buffer<Integer> connectionsToRemove;
    private int delay;

    AsyncReader(Connection c, Buffer<Packet> b1, Buffer<Integer> b2, int readersDelay) {
        connection = c;
        buffer = b1;
        connectionsToRemove = b2;
        delay = readersDelay;
    }

    @Override
    public void run() {
        while(true) {
            try {
                byte[] bytes = connection.read();
                Packet packet = new Packet(connection, bytes);
                buffer.enqueue(packet);
                Thread.sleep(delay);
            } catch (IOException e) {
                try {
                    connectionsToRemove.enqueue(connection.getId());
                    return;
                } catch (InterruptedException exc) {
                }
            } catch (InterruptedException e) {
            }
        }
    }

}
