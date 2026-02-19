package BusinessLogic;

import DataModel.Task;
import DataModel.Server;
import DataAccess.FileLogger;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private List<Server> servers;
    private int maxNoOfServers;
    private int maxTasksPerServer;
    private Strategy strategy;

    public Scheduler(int maxNoOfServers, int maxTasksPerServer) {
        this.servers = new ArrayList<>();
        this.maxNoOfServers = maxNoOfServers;
        this.maxTasksPerServer = maxTasksPerServer;
        this.strategy = new ConcreteStrategyTime();

        for(int i = 0; i < maxNoOfServers; i++){
            Server server = new Server();
            servers.add(server);
            Thread thread = new Thread(server);
            thread.start();
        }
    }

    public List<Server> getServers() {
        return servers;
    }

    public void changeStrategy(SelectionPolicy policy) {
        if(policy == SelectionPolicy.SHORTEST_QUEUE) {
            this.strategy = new ConcreteStrategyQueue();
        } else if(policy == SelectionPolicy.SHORTEST_TIME) {
            this.strategy = new ConcreteStrategyTime();
        }
    }

    public void dispatchTask(Task task) {
        strategy.addTask(task, servers);

        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).getTasks().contains(task)) {
                FileLogger.log(task + " dispatched to queue " + (i + 1));
                break;
            }
            System.out.println("\n");
        }
    }

    public void stopAllServers() {
        for(Server server : servers){
            server.stop();
        }
    }
}
