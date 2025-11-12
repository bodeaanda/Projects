package Model;

import java.io.*;
import java.util.*;

public class TaskManagement implements Serializable {
    private List<Employee> employees;
    private List<Task> tasks;

    public TaskManagement() {
        this.employees = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    public void addEmployee(int id, String name) {
        employees.add(new Employee(id, name));
        System.out.println("Employee added: " + name);
    }

    public void addTask(Task task) {
        if (task != null) {
            tasks.add(task);
            System.out.println("Task added: " + task);
        } else {
            System.out.println("Task is null, cannot add.");
        }
    }

    public int calculateEmployeeWorkDuration(int employeeId) {
        int workDuration = 0;
        for (Employee employee : employees) {
            if (employee.getEmployeeId() == employeeId) {
                for (Task task : employee.getTasks()) {
                    workDuration += task.estimateDuration();
                }
                break;
            }
        }
        return workDuration;
    }

    public void modifyTaskStatus(int employeeId, int taskId, String statusTask) {
        for (Employee employee : employees) {
            if (employee.getEmployeeId() == employeeId) {
                for (Task task : employee.getTasks()) {
                    if (task.getIdTask() == taskId) {
                        task.setStatusTask(statusTask);
                        System.out.println("Task modified: " + task);
                        return;
                    }
                }
                System.out.println("Task not found for employee: " + employeeId);
                return;
            }
        }
        System.out.println("Employee not found: " + employeeId);
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TaskManagement loadFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (TaskManagement) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new TaskManagement();
    }

    public Task getTaskById(int id) {
        for (Task task : tasks) {
            if (task.getIdTask() == id) {
                return task;
            }
        }
        return null;
    }

    public void assignTaskToEmployee(int employeeId, Task task) {
        for (Employee employee : employees) {
            if (employee.getEmployeeId() == employeeId) {
                employee.addTask(task);
                System.out.println("Task assigned to employee: " + employeeId);
                return;
            }
        }
        System.out.println("Employee not found: " + employeeId);
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Employee getEmployeeById(int id) {
        for (Employee emp : employees) {
            if (emp.getEmployeeId() == id) {
                return emp;
            }
        }
        return null;
    }
}
