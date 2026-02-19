package BusinessLogic;

import DataModel.Task;
import DataModel.Server;
import DataAccess.FileLogger;
import GUI.SimulationFrame;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable {
    private int timeLimit;
    private int maxTasksPerServer;
    private int minArrivalTime;
    private int maxArrivalTime;
    private int minServiceTime;
    private int maxServiceTime;
    private int nrOfTasks;
    private List<Task> generatedTasks = new ArrayList<>();
    private Scheduler scheduler;
    private AtomicInteger currentTime = new AtomicInteger(0);
    private SelectionPolicy selectionPolicy;
    private SimulationFrame frame;
    private float avgWaitingTime = 0;
    private float avgServiceTime = 0;
    private int peakHour = 0;
    private int maxClientsAtPeak = 0;

    public SimulationManager(int nrOfTasks, int nrOfServers, int timeLimit, int minArrivalTime, int maxArrivalTime, int minServiceTime, int maxServiceTime) {
        this.nrOfTasks = nrOfTasks;
        this.maxTasksPerServer = nrOfTasks/nrOfServers;
        this.timeLimit = timeLimit;
        this.minArrivalTime = minArrivalTime;
        this.maxArrivalTime = maxArrivalTime;
        this.minServiceTime = minServiceTime;
        this.maxServiceTime = maxServiceTime;
        this.selectionPolicy = SelectionPolicy.SHORTEST_TIME;
        this.scheduler = new Scheduler(nrOfServers, maxTasksPerServer);
        scheduler.changeStrategy(selectionPolicy);

        generateRandomTasks();
    }

    private void generateRandomTasks() {
        Random rand = new Random();
        generatedTasks.clear();

        for (int i = 0; i < nrOfTasks; i++) {
            int arrivalTime = minArrivalTime + rand.nextInt(maxArrivalTime - minArrivalTime + 1);
            int serviceTime = minServiceTime + rand.nextInt(maxServiceTime - minServiceTime + 1);
            generatedTasks.add(new Task(i + 1, arrivalTime, serviceTime));
        }

        generatedTasks.sort(Comparator.comparingInt(Task::getArrivalTime));
    }

    private int countActiveClients() {
        int activeClients = 0;
        for(Server server : scheduler.getServers()) {
            activeClients += server.getClients().size();
        }
        return activeClients;
    }

    private boolean serversHaveTasks() {
        for(Server server : scheduler.getServers()) {
            if(!server.getTasks().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String getWaitingClients() {
        return generatedTasks.stream()
                .filter(task -> task.getArrivalTime() > currentTime.get())
                .map(Task::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("no clients left");
    }

    private String getServerClients(Server server) {
        return server.getClients().stream()
                .map(Task::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("empty");
    }

    private void updateUI() {
        StringBuilder status = new StringBuilder();
        status.append("Time: ").append(currentTime.get()).append("\n");
        status.append("Waiting clients: ").append(getWaitingClients()).append("\n\n");

        int i = 1;
        for(Server server : scheduler.getServers()) {
            status.append("Queue ").append(i).append(": ");
            if(server.getClients().isEmpty()) {
                status.append("empty");
            } else {
                status.append(getServerClients(server));
            }
            status.append("\n");
            i++;
        }

        if(frame != null) {
            frame.updateSimulationStatus(status.toString());
        } else {
            FileLogger.log(status.toString());
        }
    }

    private void calculateStatistics() {
        float totalServiceTime = 0;
        int processedTasks = 0;
        float totalWaitingTime = 0;

        for (Task task : generatedTasks) {
            totalServiceTime += task.getServiceTime();
        }

        System.out.println("Calculating statistics from all servers:");
        for (Server server : scheduler.getServers()) {
            float serverWaitingTime = server.getTotalWaitingTime();
            int serverProcessedTasks = server.getProcessedTasks().size();

            System.out.println("Server processed " + serverProcessedTasks +
                    " tasks with total waiting time " + serverWaitingTime);

            totalWaitingTime += serverWaitingTime;
            processedTasks += serverProcessedTasks;
        }

        avgServiceTime = generatedTasks.isEmpty() ? 0 : totalServiceTime / generatedTasks.size();
        avgWaitingTime = processedTasks == 0 ? 0 : totalWaitingTime / processedTasks;

        System.out.println("Final statistics: total waiting time=" + totalWaitingTime +
                ", processed tasks=" + processedTasks +
                ", average waiting time=" + avgWaitingTime);

        StringBuilder stats = new StringBuilder();
        stats.append("Simulation finished!\n");
        stats.append("Average waiting time: ").append(avgWaitingTime).append("\n");
        stats.append("Average service time: ").append(avgServiceTime).append("\n");
        stats.append("Peak hour: ").append(peakHour).append(" with ").append(maxClientsAtPeak).append(" clients\n");

        FileLogger.log(stats.toString());

        if (frame != null) {
            frame.displayStatistics(stats.toString());
        } else {
            FileLogger.log(stats.toString());
        }
    }

    @Override
    public void run() {
        FileLogger.clearLog();
        FileLogger.log("Simulation started!\n");
        List<Task> waitingTasks = new ArrayList<>(generatedTasks);

        while(currentTime.get() <= timeLimit && (!waitingTasks.isEmpty() || serversHaveTasks())) {
            for (Server server : scheduler.getServers()) {
                server.updateCurrentTime(currentTime.get());
            }

            Iterator<Task> it = waitingTasks.iterator();
            while (it.hasNext()) {
                Task task = it.next();
                if (task.getArrivalTime() <= currentTime.get()) {
                    scheduler.dispatchTask(task);
                    it.remove();
                }
            }

            // peak hour
            int currentActiveClients = countActiveClients();
            if (currentActiveClients > maxClientsAtPeak) {
                maxClientsAtPeak = currentActiveClients;
                peakHour = currentTime.get();
            }
            updateUI();
            try {
                Thread.sleep(1000);
                currentTime.incrementAndGet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(serversHaveTasks()) {
            updateUI();
            try {
                Thread.sleep(1000);
                currentTime.incrementAndGet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        calculateStatistics();
        scheduler.stopAllServers();
    }

    public void setSimulationFrame(SimulationFrame frame) {
        this.frame = frame;
    }
}
