package org.servito.net;

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
    private int workers;
    private ServerSocket serverSocket;
    private PrintStream errorStream;
    private Protocol protocol;
    private BlockingQueue<Packet> buffer;
    private BlockingQueue<Integer> connectionsToRemove;
    private List<Connection> connections;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Thread> readers;
    private Thread newConnectionListener;
    private Thread deadConnectionEliminator;
    private List<Thread> dataProcessors;
    private AsyncTaskManager asyncTaskManager;
    private boolean keepWorking;
    private boolean connectionAutoFlush;


    public Server(int port, int bufferSize, int maxConnections, int backlog, int readersDelay, int workers,
                  boolean connectionAutoFlush, PrintStream errorStream, Protocol protocol){
        if(port < 0 || port > 65535) throw new IllegalArgumentException();
        this.port = port;
        this.backlog = backlog;
        this.maxConnections = maxConnections;
        this.nextConnectionID = 0;
        this.readersDelay = readersDelay;
        if(workers < 1) throw new IllegalArgumentException();
        this.workers = workers;
        this.errorStream = errorStream;
        this.protocol = protocol;
        this.protocol.onServerConstructor(this);
        buffer = new BlockingQueue<>(bufferSize);
        connectionsToRemove = new BlockingQueue<>(backlog);
        connections = new ArrayList<>();
        readers = new ArrayList<>();
        newConnectionListener = makeConnectionListener();
        deadConnectionEliminator = makeConnectionEliminator();
        dataProcessors = new ArrayList<>();
        for(int i = 0; i < workers; i++) dataProcessors.add(makeDataProcessor());
        asyncTaskManager = new AsyncTaskManager();
        this.connectionAutoFlush = connectionAutoFlush;
        protocol.init();
    }

    private Thread makeConnectionListener() {
        return new Thread(() -> {
            while(keepWorking) {
                try {
                    Socket connection0 = serverSocket.accept();
                    if(connections.size() >= maxConnections) continue;
                    asyncTaskManager.startNewTask("accepting" + nextConnectionID, false, () -> {
                        Connection connection;
                        try {
                            connection = new Connection(nextConnectionID, connection0, connectionAutoFlush);
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

    private Thread makeConnectionEliminator() {
        return new Thread(() -> {
            while(keepWorking) {
                try {
                    int id = connectionsToRemove.dequeue();
                    int index = getIndexByID(id);
                    if(index == -1) continue;
                    Connection connection = connections.get(index);
                    removeConnection(index);
                    asyncTaskManager.startNewTask("elimination" + connection.getId(), false, () -> {
                        protocol.onDeadConnection(connection);
                        connection.close();
                    }, (e) -> false);
                } catch(InterruptedException e) {
                    return;
                }
            }
        });
    }

    private Thread makeDataProcessor() {
        return new Thread(() -> {
            while(keepWorking) {
                try {
                    Packet packet = buffer.dequeue();
                    protocol.onDataInBuffer(packet);
                } catch(InterruptedException e){
                    return;
                }
            }
        });
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port, backlog);
        } catch(IOException e){
            e.printStackTrace(errorStream);
        }
        keepWorking = true;
        newConnectionListener.start();
        deadConnectionEliminator.start();
        for (Thread thread : dataProcessors) {
            thread.start();
        }
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
    }

    public void close() {
        stop();
        try {
            serverSocket.close();
            readers.clear();
            for (Connection connection : connections) {
                connection.close();
            }
            connections.clear();
        } catch(IOException e) {
            e.printStackTrace(errorStream);
        }
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

    public void send(String data) {
        for (Connection connection : connections) {
            connection.send(data);
        }
    }

    public void sendTo(String data, Connection... connections) {
        for (Connection connection : connections) {
            connection.send(data);
        }
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    private void addConnection(Connection connection) {
        Thread reader = new Thread(() -> {
            while(true) {
                try {
                    StringBuilder content = new StringBuilder();
                    char nextChar;
                    while(true) {
                        nextChar = (char)connection.read();
                        if(nextChar == (char)65535) throw new IOException();
                        else if(nextChar != protocol.getEndChar()) content.append(nextChar);
                        else break;
                    }
                    Packet packet = new Packet(connection, content.toString());
                    buffer.enqueue(packet);
                    Thread.sleep(readersDelay);
                } catch(IOException e) {
                    try {
                        connectionsToRemove.enqueue(connection.getId());
                        return;
                    } catch(InterruptedException exc) {}
                } catch(InterruptedException e) {}
            }
        });
        synchronized (connections) {
            reader.start();
            connections.add(connection);
            readers.add(reader);
        }
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    private void removeConnection(int index) {
        synchronized (connections) {
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
