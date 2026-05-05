package com.neoshell;

import com.neoshell.model.OSType;
import com.neoshell.ui.MainWindow;
import com.neoshell.ui.OSSelectorDialog;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Set look and feel to system default
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Step 1 — Show OS selector dialog
            OSSelectorDialog selector = new OSSelectorDialog(null);
            selector.setVisible(true);

            // Step 2 — Get user's choice
            OSType chosen = selector.getSelectedOS();

            // If user closed the dialog without choosing, exit
            if (chosen == null) {
                System.exit(0);
            }

            // Step 3 — Open the main terminal window
            new MainWindow(chosen);
        });
    }
}