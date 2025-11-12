package View;

import javax.swing.*;
import java.awt.*;

public class InputDialogView {
    public static String showInputDialog(String title, String message) {
        UIManager.put("OptionPane.background", new Color(87, 108, 168));
        UIManager.put("Panel.background", new Color(87, 108, 168));
        UIManager.put("Button.background", new Color(245, 243, 215));
        UIManager.put("Button.foreground", new Color(48, 43, 39));
        UIManager.put("Button.font", new Font("Courier New", Font.BOLD, 18));

        JPanel panel = new JPanel(new GridLayout(2, 1));
        JLabel label = new JLabel(message);
        JTextField field = new JTextField(10);

        panel.add(label);
        panel.add(field);

        panel.setBackground(new Color(87, 108, 168));
        label.setFont(new Font("Courier New", Font.BOLD, 20));
        label.setForeground(new Color(245, 243, 215));
        field.setFont(new Font("Courier New", Font.BOLD, 20));

        int result = JOptionPane.showConfirmDialog(
                null, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            return field.getText();
        }
        return null;
    }
}
