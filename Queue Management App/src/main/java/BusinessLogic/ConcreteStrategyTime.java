package BusinessLogic;

import DataModel.Server;
import DataModel.Task;

import java.util.List;

public class ConcreteStrategyTime implements Strategy{
    @Override
    public void addTask(Task task, List<Server> servers) {
        Server bestServer = servers.get(0);
        for (Server server : servers) {
            if(server.getWaitingPeriod() < bestServer.getWaitingPeriod()){
                bestServer = server;
            }
        }
        bestServer.addTask(task);
    }
}
