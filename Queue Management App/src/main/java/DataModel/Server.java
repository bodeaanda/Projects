
package DataModel;

import DataAccess.FileLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private BlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;
    private boolean running = true;
    private float totalWaitingTime = 0;
    private int currentTime = 0;
    private List<Task> processedTasks = new ArrayList<>();
    private int currentSimulationTime = 0;

    public Server() {
        this.tasks = new LinkedBlockingQueue<>();
        this.waitingPeriod = new AtomicInteger(0);
    }

    public BlockingQueue<Task> getTasks() {
        return tasks;
    }

    public int getWaitingPeriod() {
        return waitingPeriod.get();
    }

    public void addTask(Task newTask) {
        //newTask.setStartProcessingTime(currentSimulationTime);
        tasks.add(newTask);
        System.out.println("Client " + newTask.getId() + " added to queue at time " + currentSimulationTime);
        waitingPeriod.addAndGet(newTask.getServiceTime());
    }

    public BlockingQueue<Task> getClients() {
        return tasks;
    }

    public void updateCurrentTime(int time) {
        this.currentSimulationTime = time;
    }

    public List<Task> getProcessedTasks() {
        return processedTasks;
    }

    public float getTotalWaitingTime() {
        float total = 0;
        System.out.println("Calculating waiting time for " + processedTasks.size() + " processed tasks");

        for (Task task : processedTasks) {
            int waitingTime = task.getWaitingTime();
            System.out.println("Task " + task.getId() + " waiting time: " + waitingTime);
            total += waitingTime;
        }

        System.out.println("Total waiting time: " + total);
        return total;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running || !tasks.isEmpty()) {
            try {
                Task currentTask = tasks.peek();
                if (currentTask != null) {
                    if (currentTask.getStartProcessingTime() == -1) {
                        currentTask.setStartProcessingTime(currentSimulationTime);
                    }

                    Thread.sleep(1000);
                    currentTask.setServiceTime(currentTask.getServiceTime() - 1);
                    waitingPeriod.decrementAndGet();

                    if (currentTask.getServiceTime() <= 0) {
                        Task finishedTask = tasks.poll();
                        processedTasks.add(finishedTask);
                        FileLogger.log(currentTask + " processed at time " + currentSimulationTime);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isIdle() {
        return tasks.isEmpty();
    }
}
