package Model;

import java.io.*;
import java.util.*;

public class Employee implements Serializable {
    private int idEmployee;
    private String name;
    private List<Task> tasks;

    public Employee(int idEmployee, String name) {
        this.idEmployee = idEmployee;
        this.name = name;
        this.tasks = new ArrayList<>();
    }

    public int getEmployeeId() {
        return idEmployee;
    }

    public String getName() { return name; }

    public List<Task> getTasks() { return tasks; }

    public void assignTask(Task task) {
        tasks.add(task);
    }

    public void addTask(Task task) {
        if(task != null) {
            tasks.add(task);
            System.out.println("Task added to employee: " + this.getEmployeeId());
        } else {
            System.out.println("Null task");
        }
    }
    public int calculateEmployeeWorkDuration() {
        return tasks.stream()
                .filter(task -> "Completed".equals(task.getStatusTask()))
                .mapToInt(Task::estimateDuration)
                .sum();
    }
}
