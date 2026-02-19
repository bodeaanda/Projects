package GUI;

import javax.swing.*;
import java.awt.*;

public class CustomInputDialog {
    public static void showMessageDialog(String title, String message) {
        UIManager.put("OptionPane.background", new Color(34, 87, 122));
        UIManager.put("Panel.background", new Color(34, 87, 122));
        UIManager.put("Button.background", new Color(56, 163, 165));
        UIManager.put("Button.foreground", new Color(199, 249, 204));
        UIManager.put("Button.font", new Font("Serif", Font.BOLD, 16));

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Serif", Font.BOLD, 16));
        messageArea.setEditable(false);
        messageArea.setBackground(new Color(34, 87, 122));
        messageArea.setForeground(new Color(199, 249, 204));
        messageArea.setBorder(null);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JOptionPane.showMessageDialog(
                null,
                messageArea,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
