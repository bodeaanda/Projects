package GUI;

import BusinessLogic.SelectionPolicy;
import BusinessLogic.SimulationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SimulationFrame extends JFrame {
    private JTextField clientsField, queuesField, simulationTimeField, minArrivalTimeField, maxArrivalTimeField, minServiceTimeField, maxServiceTimeField;
    private JButton startButton, validateDataButton;
    private JTextArea statsArea;
    private JPanel queuesPanel;
    private JComboBox<String> strategyComboBox;
    private SimulationManager simulationManager;

    public SimulationFrame() {

        setTitle("Simulation");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(87, 204, 153));

        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Simulation data"));
        stylePanel(inputPanel);

        JLabel clientsLabel = new JLabel("Number of clients:");
        styleLabel(clientsLabel);
        inputPanel.add(clientsLabel);
        clientsField = new JTextField("4");
        styleTextField(clientsField);
        inputPanel.add(clientsField);

        JLabel queuesLabel = new JLabel("Number of queues:");
        styleLabel(queuesLabel);
        inputPanel.add(queuesLabel);
        queuesField = new JTextField("2");
        styleTextField(queuesField);
        inputPanel.add(queuesField);

        JLabel simulationTimeLabel = new JLabel("Simulation time:");
        styleLabel(simulationTimeLabel);
        inputPanel.add(simulationTimeLabel);
        simulationTimeField = new JTextField("60");
        styleTextField(simulationTimeField);
        inputPanel.add(simulationTimeField);

        JLabel minArrivalTimeLabel = new JLabel("Minimum arrival time:");
        styleLabel(minArrivalTimeLabel);
        inputPanel.add(minArrivalTimeLabel);
        minArrivalTimeField = new JTextField("2");
        styleTextField(minArrivalTimeField);
        inputPanel.add(minArrivalTimeField);

        JLabel maxArrivalTimeLabel = new JLabel("Maximum arrival time:");
        styleLabel(maxArrivalTimeLabel);
        inputPanel.add(maxArrivalTimeLabel);
        maxArrivalTimeField = new JTextField("30");
        styleTextField(maxArrivalTimeField);
        inputPanel.add(maxArrivalTimeField);

        JLabel minServiceTimeLabel = new JLabel("Minimum service time:");
        styleLabel(minServiceTimeLabel);
        inputPanel.add(minServiceTimeLabel);
        minServiceTimeField = new JTextField("2");
        styleTextField(minServiceTimeField);
        inputPanel.add(minServiceTimeField);

        JLabel maxServiceTimeLabel = new JLabel("Maximum service time:");
        styleLabel(maxServiceTimeLabel);
        inputPanel.add(maxServiceTimeLabel);
        maxServiceTimeField = new JTextField("4");
        styleTextField(maxServiceTimeField);
        inputPanel.add(maxServiceTimeField);

        JLabel strategyLabel = new JLabel("Strategy:");
        styleLabel(strategyLabel);
        inputPanel.add(strategyLabel);
        strategyComboBox = new JComboBox<>(new String[]{"Shortest queue", "Shortest time"});
        strategyComboBox.setFont(new Font("Serif", Font.BOLD, 18));
        strategyComboBox.setBackground(new Color(199, 249, 204));
        strategyComboBox.setForeground(new Color(34, 87, 122));
        inputPanel.add(strategyComboBox);

        validateDataButton = createCustomButton("Validate");
        startButton = createCustomButton("Start");
        startButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        stylePanel(buttonPanel);

        buttonPanel.add(validateDataButton);
        buttonPanel.add(startButton);

        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Simulation status"));
        stylePanel(statsPanel);

        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Serif", Font.BOLD, 16));
        statsArea.setBackground(new Color(199, 249, 204));
        statsArea.setForeground(new Color(34, 87, 122));

        JScrollPane statsScrollPane = new JScrollPane(statsArea);
        statsScrollPane.setBackground(new Color(199, 249, 204));
        statsScrollPane.setBorder(BorderFactory.createLineBorder(new Color(34, 87, 122), 5));

        statsPanel.add(statsScrollPane, BorderLayout.CENTER);

        queuesPanel = new JPanel();
        queuesPanel.setBorder(BorderFactory.createTitledBorder("Queues"));
        queuesPanel.setLayout(new BoxLayout(queuesPanel, BoxLayout.Y_AXIS));
        stylePanel(queuesPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(199, 249, 204));

        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(queuesPanel, BorderLayout.SOUTH);

        validateDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateInput();
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });
    }

    public JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 50));
        button.setBackground(new Color(56, 163, 165));
        button.setForeground(new Color(199, 249, 204));
        button.setFont(new Font("Serif", Font.BOLD, 20));
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Serif", Font.BOLD, 18));
        field.setBackground(new Color(199, 249, 204));
        field.setForeground(new Color(34, 87, 122));
        field.setBorder(BorderFactory.createLineBorder(new Color(34, 87, 122)));
    }

    private void stylePanel(JPanel panel) {
        panel.setBackground(new Color(128, 237, 153));
        panel.setBorder(BorderFactory.createLineBorder(new Color(87, 204, 153), 3));
    }

    private void styleLabel(JLabel label) {
        label.setFont(new Font("Serif", Font.BOLD, 18));
        label.setForeground(new Color(34, 87, 122));
    }
    private void validateInput() {
        try {
            int clients = Integer.parseInt(clientsField.getText());
            int queues = Integer.parseInt(queuesField.getText());
            int simTime = Integer.parseInt(simulationTimeField.getText());
            int minArrival = Integer.parseInt(minArrivalTimeField.getText());
            int maxArrival = Integer.parseInt(maxArrivalTimeField.getText());
            int minService = Integer.parseInt(minServiceTimeField.getText());
            int maxService = Integer.parseInt(maxServiceTimeField.getText());

            if (clients <= 0 || queues <= 0 || simTime <= 0 ||
                    minArrival < 0 || maxArrival <= 0 || minService <= 0 || maxService <= 0) {
                CustomInputDialog.showMessageDialog("Validation error", "All values must be greater than 0!");
                return;
            }

            if (minArrival > maxArrival) {
                CustomInputDialog.showMessageDialog("Validation error", "Min arrival time must be less than or equal to max arrival time!");
                return;
            }

            if (minService > maxService) {
                CustomInputDialog.showMessageDialog("Validation error", "Min service time must be less than or equal to max service time!");
                return;
            }

            CustomInputDialog.showMessageDialog("Validation", "Input is valid!");
            startButton.setEnabled(true);

        } catch (NumberFormatException ex) {
            CustomInputDialog.showMessageDialog("Validation error", "All fields must contain valid numbers!");
        }
    }
    private void setFieldsEnabled(boolean enabled) {
        clientsField.setEnabled(enabled);
        queuesField.setEnabled(enabled);
        simulationTimeField.setEnabled(enabled);
        minArrivalTimeField.setEnabled(enabled);
        maxArrivalTimeField.setEnabled(enabled);
        minServiceTimeField.setEnabled(enabled);
        maxServiceTimeField.setEnabled(enabled);
        strategyComboBox.setEnabled(enabled);
        validateDataButton.setEnabled(enabled);
        startButton.setEnabled(enabled);
    }

    private void startSimulation() {
        try {
            int clients = Integer.parseInt(clientsField.getText());
            int queues = Integer.parseInt(queuesField.getText());
            int simTime = Integer.parseInt(simulationTimeField.getText());
            int minArrival = Integer.parseInt(minArrivalTimeField.getText());
            int maxArrival = Integer.parseInt(maxArrivalTimeField.getText());
            int minService = Integer.parseInt(minServiceTimeField.getText());
            int maxService = Integer.parseInt(maxServiceTimeField.getText());

            SelectionPolicy policy = strategyComboBox.getSelectedIndex() == 0 ?
                    SelectionPolicy.SHORTEST_QUEUE : SelectionPolicy.SHORTEST_TIME;

            setFieldsEnabled(false);

            simulationManager = new SimulationManager(clients, queues, simTime, minArrival, maxArrival, minService, maxService);
            simulationManager.setSimulationFrame(this);

            Thread simThread = new Thread(simulationManager);
            simThread.start();

        } catch (Exception ex) {
            CustomInputDialog.showMessageDialog("Error", "Error starting simulation: " + ex.getMessage());
            setFieldsEnabled(true);
        }
    }

    public void updateSimulationStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statsArea.setText(status);
        });
    }

    public void displayStatistics(String stats) {
        SwingUtilities.invokeLater(() -> {
            CustomInputDialog.showMessageDialog("Simulation Statistics", stats);
            setFieldsEnabled(true);
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimulationFrame().setVisible(true);
        });
    }
}
