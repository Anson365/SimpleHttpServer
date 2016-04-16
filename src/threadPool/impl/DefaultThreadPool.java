package threadPool.impl;

import threadPool.ThreadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ludao on 16/4/16.
 */
public class DefaultThreadPool<Job extends Runnable> implements ThreadPool<Job> {

    private static final int MAX_WORKER_NUMBERS = 10;
    private static final int DEFAULT_WORKER_NUMBERS = 5;
    private static final int MIN_WORKER_NUMBERS = 1;
    private final LinkedList<Job> jobs = new LinkedList<Job>();
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());

    private int workerNum = DEFAULT_WORKER_NUMBERS;

    private AtomicLong threadNum = new AtomicLong();

    public DefaultThreadPool() {
        this.workerNum = DEFAULT_WORKER_NUMBERS;
        initializeWorkers(workerNum);
    }

    public DefaultThreadPool(int workerNum) {
        this.workerNum = workerNum>MAX_WORKER_NUMBERS?MAX_WORKER_NUMBERS:workerNum<MIN_WORKER_NUMBERS?MIN_WORKER_NUMBERS:workerNum;
        initializeWorkers(workerNum);
    }

    @Override
    public void execute(Job job) {
        if(job != null){
            synchronized (jobs){
                jobs.addLast(job);
                jobs.notify();
            }
        }
    }



    @Override
    public void shutdown() {
        for(Worker worker:workers){
            worker.shutdown();
        }
    }

    @Override
    public void addWorkers(int num) {
        synchronized (jobs){
            if(num + this.workerNum>MAX_WORKER_NUMBERS){
                num = MAX_WORKER_NUMBERS - this.workerNum;
            }
            initializeWorkers(num);
            this.workerNum += num;
        }
    }

    @Override
    public void removeWorker(int num) {
        synchronized (jobs){
            if(this.workerNum < num){
                throw new IllegalArgumentException(" beyond workNum");
            }else if(this.workerNum - num < MIN_WORKER_NUMBERS){
                num = this.workerNum - MIN_WORKER_NUMBERS;
            }
            initializeWorkers(num);
            this.workerNum -= num;
        }
    }

    @Override
    public int getJobSize() {
        return this.workerNum;
    }

    private void initializeWorkers(int num){
        for(int i=0;i<num;i++){
            Worker worker = new Worker();
            workers.add(worker);
            Thread thread = new Thread(worker,"ThreadPool-Worler-"+threadNum.incrementAndGet());
            thread.start();
        }
    }


    class Worker implements Runnable{

        private volatile boolean running = true;
        @Override
        public void run() {
            while (running){
                Job job = null;
                synchronized(jobs){
                    while (jobs.isEmpty()){
                        try {
                            jobs.wait();
                        }catch (InterruptedException ex){
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                if(job != null){
                    try {
                        job.run();
                    }catch (Exception ex){

                    }
                }
            }
        }

        public void shutdown(){
            running = false;
        }
    }
}
