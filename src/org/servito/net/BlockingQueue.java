package org.servito.net;

import java.util.LinkedList;
import java.util.List;

public class BlockingQueue<T> {

    private List<T> queue = new LinkedList<>();

    private int limit;

    public BlockingQueue(int limit){
        this.limit = limit;
    }

    public synchronized void enqueue(T item)
            throws InterruptedException  {
        while(this.queue.size() == this.limit) {
            wait();
        }
        this.queue.add(item);
        if(this.queue.size() == 1) {
            notifyAll();
        }
    }

    public synchronized T dequeue()
            throws InterruptedException{
        while(this.queue.size() == 0){
            wait();
        }
        T obj = this.queue.remove(0);
        if(this.queue.size() == this.limit-1){
            notifyAll();
        }
        return obj;
    }

}
