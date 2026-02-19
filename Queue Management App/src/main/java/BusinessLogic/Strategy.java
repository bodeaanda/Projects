package BusinessLogic;

import DataModel.Server;
import DataModel.Task;

import java.util.List;

public interface Strategy {
    void addTask(Task task, List<Server> servers);
}