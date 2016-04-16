package threadPool;

/**
 * Created by ludao on 16/4/16.
 */
public interface ThreadPool<Job extends Runnable> {
//    excute a job
    void execute(Job job);
//    shut down the thread
    void shutdown();
//    add the worker
    void addWorkers(int num);
//    reduce the worker
    void removeWorker(int num);
//    get the running job size
    int getJobSize();
}
