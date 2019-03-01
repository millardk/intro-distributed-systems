package cs455.scaling.server;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class ThreadPoolManager {

    public final int poolSize;
    public final int batchSize;
    public final long batchTimeNanos;

    private final Queue<WorkerThread> workerQueue;
    private final ArrayDeque<Batch> batchList;

    public ThreadPoolManager(int poolSize, int batchSize, long batchTimeNanos) {
        this.poolSize = poolSize;
        this.batchSize = batchSize;
        this.batchTimeNanos = batchTimeNanos;
        this.workerQueue = new ArrayDeque<>();
        this.batchList = new ArrayDeque<>();
    }

    public void createAndStartWorkers() {
        for(int i = 0; i < poolSize; i++){
            WorkerThread worker = new WorkerThread(this);
            Thread thread = new Thread(worker);
            thread.start();
        }
    }

    private void addTaskWorkerQueueEmpty(Task task) {
        boolean completed = false;
        for(Batch batch : this.batchList) {
            if(batch.tasks.size() < this.batchSize) {
                batch.tasks.add(task);
                completed = true;
            }
        }

        if(!completed) {
            this.batchList.add(new Batch(task, System.nanoTime()));
        }
    }

    public void addTask(Task task)
    {
        synchronized(this)
        {
            if(this.workerQueue.isEmpty()) {
                addTaskWorkerQueueEmpty(task);

            } else {
                WorkerThread worker = this.workerQueue.peek();

                if(worker.getBatch() == null) {
                    worker.setBatch(new Batch(task, System.nanoTime()));
                    worker.notify();

                } else {
                    List<Task> tasks = worker.getBatch().tasks;
                    tasks.add(task);
                    if(tasks.size() == this.batchSize) {
                        workerQueue.poll();
                        worker.notify();
                    }

                }
            }
        }

    }

    public Batch makeAvailable(WorkerThread worker)
    {
        synchronized (this)
        {
            if(this.workerQueue.isEmpty()) {
                if(batchList.isEmpty())
                    return null;
                else
                    return batchList.poll();

            } else {
                workerQueue.add(worker);
                return null;

            }
        }
    }




}



