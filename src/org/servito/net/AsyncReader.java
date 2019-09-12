package org.servito.net;

import org.servito.utils.Buffer;

import java.io.IOException;

public class AsyncReader extends Thread {

    private Connection connection;
    private Buffer<Packet> buffer;
    private Buffer<Integer> connectionsToRemove;
    private byte endValue;
    private int delay;
    private int packetMaxLength;

    AsyncReader(Connection c, Buffer<Packet> b1, Buffer<Integer> b2, byte protocolEndValue,
            int maxLength, int readersDelay) {
        connection = c;
        buffer = b1;
        connectionsToRemove = b2;
        endValue = protocolEndValue;
        delay = readersDelay;
        packetMaxLength = maxLength;
    }

    @Override
    public void run() {
        while(true) {
            try {
                byte[] bytes = new byte[packetMaxLength];
                byte next;
                int count = 0;
                while (true) {
                    next = connection.read();
                    if (next != endValue) bytes[count] = next;
                    else break;
                    count++;
                    if(count > packetMaxLength) throw new IOException("Sending too much data");
                }
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
