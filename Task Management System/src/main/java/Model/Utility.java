package Model;

import java.util.*;

public class Utility {
    public static List<Employee> filterEmployeeByWorkDuration(List<Employee> employees, int workDuration) {
        return employees.stream()
                .filter(e -> e.calculateEmployeeWorkDuration() > 40)
                .sorted(Comparator.comparingInt(Employee::calculateEmployeeWorkDuration))
                .toList();
    }
    public static Map<String, Map<String, Integer>> calculateTaskStatistics(List<Employee> employees) {
        Map<String, Map<String, Integer>> taskStatistics = new HashMap<>();
        for (Employee e : employees) {
            Map<String, Integer> taskCount = new HashMap<>();
            taskCount.put("Completed", 0);
            taskCount.put("Uncompleted", 0);

            for (Task task : e.getTasks()) {
                String taskStatus = task.getStatusTask();
                taskCount.put(taskStatus, taskCount.getOrDefault(taskStatus, 0) + 1);
            }
            taskStatistics.put(e.getName(), taskCount);
        }
        return taskStatistics;
    }

}
