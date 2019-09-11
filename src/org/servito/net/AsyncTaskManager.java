package org.servito.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncTaskManager {

    public List<Thread> tasks = Collections.synchronizedList(new ArrayList<>());

    public void startNewTask(String taskName, boolean loop, Runnable task, ExceptionHandler handler) {
        Thread thread = new Thread(() -> {
            boolean loop0 = loop;
            do {
                try {
                    task.run();
                }
                catch (Exception e) {
                    if(!handler.handle(e)) loop0 = false;
                }
            }
            while (loop0);
            tasks.remove(Thread.currentThread());
        });
        thread.setName(taskName);
        tasks.add(thread);
        thread.start();
    }

    public void stopTaskByName(String name, boolean brutally) {
        ArrayList<Thread> tasksToRemove = new ArrayList<>();
        for (Thread task : tasks) {
            if(task.getName().equals(name)) {
                if(brutally) {
                    try {
                        task.stop();
                    } catch(ThreadDeath error) {}
                } else task.interrupt();
                tasksToRemove.add(task);
            }
        }
        tasks.removeAll(tasksToRemove);
    }

    public void stopAllTasks(boolean brutally) {
        for (Thread task : tasks) {
            if(brutally) {
                try {
                    task.stop();
                } catch(ThreadDeath error) {}
            } else task.interrupt();
        }
        tasks.clear();
    }

}
