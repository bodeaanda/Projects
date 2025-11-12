package View;

import Controller.TaskManagementController;
import Model.SimpleTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TaskManagementView extends JFrame {

    private TaskManagementController controller;
    private JComboBox<String> taskTypeDropdown;
    private JTextField titleField, endHourField, startHourField, employeeIdField, employeeNameField;
    private JPanel subtaskPanel;
    private List<JTextField> subtaskTitleFields;
    private List<JTextField> subtaskStartHourFields, subtaskEndHourFields;
    private JButton addEmployeeButton, addTaskButton, assignTaskButton, modifyTaskButton, showEmployeeButton, calculateWorkDurationButton, filterEmployeesButton, calculateTaskStatsButton, subtaskButton;

    public TaskManagementView() {
        subtaskTitleFields = new ArrayList<>();
        subtaskStartHourFields = new ArrayList<>();
        subtaskEndHourFields = new ArrayList<>();

        setTitle("Task management system");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(48, 43, 39));

        JLabel mngLabel = new JLabel("Task management");
        mngLabel.setFont(new Font("Courier New", Font.BOLD, 50));
        mngLabel.setForeground(new Color(245, 243, 215));
        mngLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(mngLabel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(87, 108, 168));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel employeeNameLabel = new JLabel("Employee name:");
        employeeNameLabel.setFont(new Font("Courier New", Font.BOLD, 30));
        employeeNameLabel.setForeground(new Color(27, 38, 79));

        employeeNameField = new JTextField(20);
        employeeNameField.setFont(new Font("Courier New", Font.PLAIN, 20));
        employeeNameField.setBackground(new Color(245, 243, 215));
        addEmployeeButton = createCustomButton("Add employee");

        JLabel employeeIdLabel = new JLabel("Employee ID:");
        employeeIdLabel.setFont(new Font("Courier New", Font.BOLD, 30));
        employeeIdLabel.setForeground(new Color(27, 38, 79));

        employeeIdField = new JTextField(20);
        employeeIdField.setFont(new Font("Courier New", Font.PLAIN, 20));
        employeeIdField.setBackground(new Color(245, 243, 215));

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(employeeNameLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(employeeNameField, gbc);

        gbc.gridx = 2;
        mainPanel.add(addEmployeeButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(employeeIdLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(employeeIdField, gbc);

        JLabel taskTypeLabel = new JLabel("Task type:");
        taskTypeLabel.setFont(new Font("Courier New", Font.BOLD, 30));
        taskTypeLabel.setForeground(new Color(27, 38, 79));

        taskTypeDropdown = new JComboBox<>(new String[]{"Simple task", "Complex task"});
        taskTypeDropdown.addActionListener(e -> toggleSubtaskFields());
        taskTypeDropdown.setFont(new Font("Courier New", Font.PLAIN, 20));
        taskTypeDropdown.setBackground(new Color(245, 243, 215));

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(taskTypeLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(taskTypeDropdown, gbc);

        JLabel titleLabel = new JLabel("Task:");
        titleLabel.setFont(new Font("Courier New", Font.BOLD, 30));
        titleLabel.setForeground(new Color(27, 38, 79));

        titleField = new JTextField(20);
        titleField.setFont(new Font("Courier New", Font.PLAIN, 20));
        titleField.setBackground(new Color(245, 243, 215));

        addTaskButton = createCustomButton("Add task");

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(titleLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(titleField, gbc);

        gbc.gridx = 2;
        mainPanel.add(addTaskButton, gbc);

        JLabel startHourLabel = new JLabel("Start hour:");
        startHourLabel.setFont(new Font("Courier New", Font.BOLD, 30));
        startHourLabel.setForeground(new Color(27, 38, 79));

        startHourField = new JTextField(20);
        startHourField.setFont(new Font("Courier New", Font.PLAIN, 20));
        startHourField.setBackground(new Color(245, 243, 215));

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(startHourLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(startHourField, gbc);

        JLabel endHourLabel = new JLabel("End hour:");
        endHourLabel.setFont(new Font("Courier New", Font.BOLD, 30));
        endHourLabel.setForeground(new Color(27, 38, 79));

        endHourField = new JTextField(20);
        endHourField.setFont(new Font("Courier New", Font.PLAIN, 20));
        endHourField.setBackground(new Color(245, 243, 215));

        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(endHourLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(endHourField, gbc);

        JLabel subtaskLabel = new JLabel("Subtasks:");
        subtaskLabel.setFont(new Font("Courier New", Font.BOLD, 30));
        subtaskLabel.setForeground(new Color(27, 38, 79));

        subtaskPanel = new JPanel();
        subtaskPanel.setLayout(new BoxLayout(subtaskPanel, BoxLayout.Y_AXIS));
        subtaskPanel.setVisible(false);

        subtaskButton = createCustomButton("Add subtask");
        subtaskButton.addActionListener(e -> addSubtask());

        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(subtaskLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(subtaskPanel, gbc);

        gbc.gridx = 2;
        mainPanel.add(subtaskButton, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        buttonPanel.setBackground(new Color(48, 43, 39));

        buttonPanel.add(assignTaskButton = createCustomButton("Assign task"));
        buttonPanel.add(modifyTaskButton = createCustomButton("Modify task"));
        buttonPanel.add(showEmployeeButton = createCustomButton("Show employees"));
        buttonPanel.add(calculateWorkDurationButton = createCustomButton("Calculate work duration"));
        buttonPanel.add(filterEmployeesButton = createCustomButton("Filter employees"));
        buttonPanel.add(calculateTaskStatsButton = createCustomButton("Task statistics"));

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 35));
        button.setBackground(new Color(39, 70, 144));
        button.setForeground(new Color(245, 243, 215));
        button.setFont(new Font("Courier New", Font.BOLD, 18));
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(48, 43, 39)));
        button.setMargin(new Insets(10, 20, 10, 20));
        return button;
    }

    private void toggleSubtaskFields() {
        boolean isComplexTask = taskTypeDropdown.getSelectedItem().toString().equalsIgnoreCase("Complex task");
        subtaskPanel.setVisible(isComplexTask);
        startHourField.setEnabled(!isComplexTask);
        endHourField.setEnabled(!isComplexTask);
        revalidate();
        repaint();
    }

    private void addSubtask() {
       if (!taskTypeDropdown.getSelectedItem().toString().equalsIgnoreCase("Complex task")) {
            JOptionPane.showMessageDialog(this, "Subtasks can only be added for complex tasks.", "Invalid operation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel subtaskRow = new JPanel(new GridLayout(1, 3, 10, 10));

        JTextField subtaskTitleField = new JTextField();
        JTextField subtaskStartHourField = new JTextField();
        JTextField subtaskEndHourField = new JTextField();

        subtaskTitleFields.add(subtaskTitleField);
        subtaskStartHourFields.add(subtaskStartHourField);
        subtaskEndHourFields.add(subtaskEndHourField);

        subtaskRow.add(new JLabel("Title:"));
        subtaskRow.add(subtaskTitleField);
        subtaskRow.add(new JLabel("Start:"));
        subtaskRow.add(subtaskStartHourField);
        subtaskRow.add(new JLabel("End:"));
        subtaskRow.add(subtaskEndHourField);

        subtaskPanel.add(subtaskRow);
        subtaskPanel.setVisible(true);
        revalidate();
        repaint();
    }

    public String getTaskTitle() {
        return titleField.getText();
    }

    public int getStartHour() {
        if (!startHourField.isEnabled()) {
            return -1;
        }
        try {
            return Integer.parseInt(startHourField.getText());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int getEndHour() {
        if (!endHourField.isEnabled()) {
            return -1;
        }
        try {
            return Integer.parseInt(endHourField.getText());
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    public List<SimpleTask> getSubtasks() {
        List<SimpleTask> subtasks = new ArrayList<>();
        for (int i = 0; i < subtaskTitleFields.size(); i++) {
            String title = subtaskTitleFields.get(i).getText();
            int start = Integer.parseInt(subtaskStartHourFields.get(i).getText());
            int end = Integer.parseInt(subtaskEndHourFields.get(i).getText());
            subtasks.add(new SimpleTask(i + 1, "Uncompleted", start, end));
        }
        return subtasks;
    }

    public int getEmployeeId() {
        String input = InputDialogView.showInputDialog("Employee ID", "Enter employee ID:");
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getEmployeeName() {
        return employeeNameField.getText();
    }

    public String getSelectedTaskType() {
        return (String) taskTypeDropdown.getSelectedItem();
    }

    public int getTaskId() {
        String input = InputDialogView.showInputDialog("Task ID", "Enter task ID:");
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getTaskStatus() {
        return InputDialogView.showInputDialog("Task status", "Enter new task status:");
    }

    public void addEmployeeListener(ActionListener listener) {
        addEmployeeButton.addActionListener(listener);
    }

    public void addTaskListener(ActionListener listener) {
        addTaskButton.addActionListener(listener);
    }

    public void assignTaskListener(ActionListener listener) {
        assignTaskButton.addActionListener(listener);
    }

    public void modifyTaskListener(ActionListener listener) {
        modifyTaskButton.addActionListener(listener);
    }

    public void showEmployeesListener(ActionListener listener) {
        showEmployeeButton.addActionListener(listener);
    }

    public void calculateWorkDurationListener(ActionListener listener) {
        calculateWorkDurationButton.addActionListener(listener);
    }

    public void filterEmployeesListener(ActionListener listener) {
        filterEmployeesButton.addActionListener(listener);
    }

    public void calculateTaskStatsListener(ActionListener listener) {
        calculateTaskStatsButton.addActionListener(listener);
    }
}
