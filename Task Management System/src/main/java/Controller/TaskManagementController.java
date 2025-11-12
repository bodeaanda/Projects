package Controller;

import Model.*;
import View.TaskManagementView;

import javax.swing.*;
import java.util.*;

public class TaskManagementController {
    public TaskManagement model;
    public TaskManagementView view;

    public TaskManagementController(TaskManagement model, TaskManagementView view) {
        this.model = model;
        this.view = view;

        view.addEmployeeListener(e -> addEmployee());
        view.addTaskListener(e -> addTask());
        view.assignTaskListener(e -> assignTask());
        view.modifyTaskListener(e -> modifyTaskStatus());
        view.showEmployeesListener(e -> showEmployees());
        view.calculateWorkDurationListener(e -> calculateEmployeeWorkDuration());
        view.filterEmployeesListener(e -> showFilteredEmployees());
        view.calculateTaskStatsListener(e -> calculateTaskStatistics());
    }

    public void addEmployee() {
        String name = JOptionPane.showInputDialog("Enter Employee Name:");

        if (name != null && !name.trim().isEmpty()) {
            int id = model.getEmployees().size() + 1;
            Employee employee = new Employee(id, name);
            model.addEmployee(id, name);
            JOptionPane.showMessageDialog(view, "Employee Added Successfully");
        } else {
            JOptionPane.showMessageDialog(view, "Invalid name input", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addTask() {
        String type = view.getSelectedTaskType();
        String title = view.getTaskTitle();
        int startHour = view.getStartHour();
        int endHour = view.getEndHour();

        if (title == null || title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Task title cannot be empty.");
            return;
        }

        if ("Simple task".equals(type)) {
            if (startHour < 0 || endHour <= startHour) {
                JOptionPane.showMessageDialog(view, "Invalid start or end hour.");
                return;
            }
        }

        int taskId = model.getTasks().size() + 1;
        Task task;

        if ("Simple task".equals(type)) {
            task = new SimpleTask(taskId, "Uncompleted", startHour, endHour);
        } else {
            ComplexTask complexTask = new ComplexTask(taskId, "Uncompleted");
            List<SimpleTask> subtasks = view.getSubtasks();

            if (subtasks.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Complex Task must have at least one subtask.");
                return;
            }

            int minStart = Integer.MAX_VALUE;
            int maxEnd = Integer.MIN_VALUE;

            for (SimpleTask subtask : subtasks) {
                complexTask.addSubTask(subtask);
                minStart = Math.min(minStart, subtask.getStartHour());
                maxEnd = Math.max(maxEnd, subtask.getEndHour());
            }

            complexTask.setStartHour(minStart);
            complexTask.setEndHour(maxEnd);

            task = complexTask;
        }

        model.addTask(task);
        JOptionPane.showMessageDialog(view, "Task Added Successfully");
    }


    public void assignTask() {
        int employeeId = view.getEmployeeId();
        int taskId = view.getTaskId();

        Employee employee = model.getEmployeeById(employeeId);
        Task task = model.getTaskById(taskId);

        if (employee != null && task != null) {
            model.assignTaskToEmployee(employeeId, task);
            JOptionPane.showMessageDialog(view, "Task Assigned Successfully");
        } else {
            JOptionPane.showMessageDialog(view, "Invalid Employee or Task ID");
        }
    }

    public void modifyTaskStatus() {
        int employeeId = view.getEmployeeId();
        int taskId = view.getTaskId();
        String newStatus = view.getTaskStatus();

        Task task = model.getTaskById(taskId);
        if (task != null) {
            model.modifyTaskStatus(employeeId, taskId, newStatus);
            JOptionPane.showMessageDialog(view, "Task Status Updated");
        } else {
            JOptionPane.showMessageDialog(view, "Invalid Task ID", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showEmployees() {
        StringBuilder employeeList = new StringBuilder();
        for (Employee e : model.getEmployees()) {
            employeeList.append(e.getEmployeeId()).append(" - ").append(e.getName()).append("\n");
        }
        JOptionPane.showMessageDialog(view, employeeList.toString(), "Employee List", JOptionPane.INFORMATION_MESSAGE);
    }

    public void calculateEmployeeWorkDuration() {
        int employeeId = view.getEmployeeId();
        Employee employee = model.getEmployeeById(employeeId);

        if (employee != null) {
            int workDuration = employee.calculateEmployeeWorkDuration();
            JOptionPane.showMessageDialog(view, "Employee " + employee.getName() +
                    " has a total work duration of " + workDuration + " hours.");
        } else {
            JOptionPane.showMessageDialog(view, "Invalid Employee ID");
        }
    }

    public void showFilteredEmployees() {
        List<Employee> filteredEmployees = Utility.filterEmployeeByWorkDuration(model.getEmployees(), 40);

        if (filteredEmployees.isEmpty()) {
            JOptionPane.showMessageDialog(view, "No employees exceed 40 hours of work.");
        } else {
            StringBuilder output = new StringBuilder("Employees with over 40 hours of work:\n");
            for (Employee e : filteredEmployees) {
                output.append(e.getName()).append(" - ").append(e.calculateEmployeeWorkDuration()).append(" hours\n");
            }
            JOptionPane.showMessageDialog(view, output.toString());
        }
    }

    public void calculateTaskStatistics() {
        Map<String, Map<String, Integer>> stats = Utility.calculateTaskStatistics(model.getEmployees());

        StringBuilder output = new StringBuilder("Task Statistics Per Employee:\n");
        for (Map.Entry<String, Map<String, Integer>> entry : stats.entrySet()) {
            output.append(entry.getKey()).append(" - Completed: ")
                    .append(entry.getValue().get("Completed"))
                    .append(", Uncompleted: ").append(entry.getValue().get("Uncompleted"))
                    .append("\n");
        }

        JOptionPane.showMessageDialog(view, output.toString());
    }

    public static void main(String[] args) {
        TaskManagement model = new TaskManagement();
        TaskManagementView view = new TaskManagementView();
        new TaskManagementController(model, view);
    }
}
