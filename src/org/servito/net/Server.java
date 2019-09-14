package org.servito.net;

import org.servito.utils.Buffer;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Server {

    private int port;
    private int maxConnections;
    private int backlog;
    private int nextConnectionID;
    private int readersDelay;
    private int maxPacketLength;
    private int workers;
    private ServerSocket serverSocket;
    private PrintStream errorStream;
    private Protocol protocol;
    private Buffer<Packet> buffer;
    private Buffer<Integer> connectionsToRemove;
    private final List<Connection> connections;
    private List<Thread> readers;
    private Thread newConnectionListener;
    private Thread deadConnectionEliminator;
    private List<Thread> dataProcessors;
    private AsyncTaskManager asyncTaskManager;
    private boolean keepWorking;
    private boolean connectionAutoFlush;


    public Server(int port, int bufferSize, int maxConnections, int backlog, int maxPacketLength, int readersDelay,
                  int workers, boolean connectionAutoFlush, PrintStream errorStream, Protocol protocol){
        if(port < 0 || port > 65535) throw new IllegalArgumentException();
        this.port = port;
        this.backlog = backlog;
        this.maxConnections = maxConnections;
        this.nextConnectionID = 0;
        this.maxPacketLength = maxPacketLength;
        this.readersDelay = readersDelay;
        if(workers < 1) throw new IllegalArgumentException();
        this.workers = workers;
        this.errorStream = errorStream;
        this.protocol = protocol;
        this.protocol.onServerConstructor(this);
        buffer = new Buffer<>(bufferSize);
        connectionsToRemove = new Buffer<>(backlog);
        connections = new ArrayList<>();
        readers = new ArrayList<>();
        makeConnectionListener();
        makeConnectionEliminator();
        dataProcessors = new ArrayList<>();
        asyncTaskManager = new AsyncTaskManager();
        this.connectionAutoFlush = connectionAutoFlush;
        protocol.init();
    }

    private void makeConnectionListener() {
        newConnectionListener = new Thread(() -> {
            while(keepWorking) {
                try {
                    Socket connection0 = serverSocket.accept();
                    if(connections.size() >= maxConnections) continue;
                    asyncTaskManager.startNewTask("accepting" + nextConnectionID, false, () -> {
                        Connection connection;
                        try {
                            connection = new Connection(nextConnectionID, connection0, connectionAutoFlush,
                                    protocol.getEndValue(), maxPacketLength);
                        } catch(IOException e){
                            e.printStackTrace(errorStream);
                            return;
                        }
                        boolean bool;
                        bool = protocol.onNewConnection(connection);
                        if(bool) {
                            nextConnectionID += 1;
                            addConnection(connection);
                        }
                        else connection.close();
                    }, (e) -> false);
                } catch(IOException e){
                    return;
                }
            }
        });
    }

    private void makeConnectionEliminator() {
        deadConnectionEliminator =  new Thread(() -> {
            while(keepWorking) {
                try {
                    int id = connectionsToRemove.dequeue();
                    int index = getIndexByID(id);
                    if(index == -1) continue;
                    Connection connection = connections.get(index);
                    removeConnection(index);
                    asyncTaskManager.startNewTask("elimination" + connection.getId(), false, () -> {
                        connection.close();
                        protocol.onDeadConnection(connection);
                    }, (e) -> false);
                } catch(InterruptedException e) {
                    return;
                }
            }
        });
    }

    private void makeDataProcessor() {
        for(int i = 0; i < workers; i++) dataProcessors.add(
            new Thread(() -> {
                while(keepWorking) {
                    try {
                        Packet packet = buffer.dequeue();
                        protocol.onDataInBuffer(packet);
                    } catch(InterruptedException e) {
                        return;
                    }
                }
        }));
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port, backlog);
        } catch(IOException e){
            e.printStackTrace(errorStream);
        }
        keepWorking = true;
        makeConnectionListener();
        makeConnectionEliminator();
        makeDataProcessor();
        newConnectionListener.start();
        deadConnectionEliminator.start();
        for (Thread thread : dataProcessors) {
            thread.start();
        }
        protocol.onStart();
    }

    public void stop() {
        keepWorking = false;
        try {
            serverSocket.close();
        } catch(IOException e) {
            e.printStackTrace(errorStream);
        }
        deadConnectionEliminator.interrupt();
        for (Thread thread : dataProcessors) {
            thread.interrupt();
        }
        asyncTaskManager.stopAllTasks(false);
        readers.clear();
        for (Connection connection : connections) {
            connection.close();
        }
        connections.clear();
    }

    public void close() {
        stop();
        readers.clear();
        for (Connection connection : connections) {
            connection.close();
        }
        connections.clear();
    }

    public boolean isWorking() {
        return keepWorking;
    }

    public int getBacklog() {
        return backlog;
    }

    public int getPort() {
        return port;
    }

    public int getWorkers() {
        return workers;
    }

    public AsyncTaskManager getAsyncTaskManager() {
        return asyncTaskManager;
    }

    public void send(byte[] data) {
        for (Connection connection : connections) {
            try {
                connection.write(data);
            } catch(IOException e) {
                try {
                    connectionsToRemove.enqueue(connection.getId());
                } catch(InterruptedException exc) {
                    //Not necessary handling
                }
            }
        }
    }

    private void addConnection(Connection connection) {
        AsyncReader reader = new AsyncReader(connection, buffer, connectionsToRemove, readersDelay);
        synchronized (connections) {
            reader.start();
            connections.add(connection);
            readers.add(reader);
        }
    }

    private void removeConnection(int index) {
        synchronized (connections) {
            readers.get(index).interrupt();
            readers.remove(index);
            connections.remove(index);
        }
    }

    private int getIndexByID(int id) {
        int index = 0;
        for (Connection connection : connections) {
            if(connection.getId()==id) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    public Connection getConnectionByID(int id) {
        for (Connection conn : connections) {
            if(conn.getId() == id) return conn;
        }
        return null;
    }

}
