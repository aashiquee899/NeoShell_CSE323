package com.neoshell.ui;

import com.neoshell.model.OSType;

import javax.swing.*;
import java.awt.*;

public class OSSelectorDialog extends JDialog {

    private OSType selectedOS = null;

    public OSSelectorDialog(Frame parent) {
        super(parent, "NeoShell — Select Terminal", true);
        buildUI();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        // Main panel with dark background
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(15, 18, 30));

        // ── Header ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 18, 30));
        header.setBorder(BorderFactory.createEmptyBorder(36, 40, 16, 40));

        JLabel logo = new JLabel("NeoShell");
        logo.setFont(new Font("Monospaced", Font.BOLD, 28));
        logo.setForeground(new Color(80, 200, 120));
        logo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel("Safe Terminal Simulator");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(130, 140, 160));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel instruction = new JLabel("Choose the OS terminal you want to simulate:");
        instruction.setFont(new Font("SansSerif", Font.PLAIN, 14));
        instruction.setForeground(new Color(200, 210, 225));
        instruction.setHorizontalAlignment(SwingConstants.CENTER);
        instruction.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(15, 18, 30));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(logo);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);
        titlePanel.add(instruction);

        header.add(titlePanel, BorderLayout.CENTER);

        // ── OS Buttons ───────────────────────────────────────────
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        btnPanel.setBackground(new Color(15, 18, 30));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));

        btnPanel.add(buildOSButton("🪟", "Windows", "CMD Style", new Color(0, 120, 212), OSType.WINDOWS));
        btnPanel.add(buildOSButton("🍎", "macOS", "Zsh / Bash", new Color(100, 100, 100), OSType.MAC));
        btnPanel.add(buildOSButton("🐧", "Linux", "Bash Style", new Color(220, 95, 0), OSType.LINUX));

        // ── Footer note ──────────────────────────────────────────
        JLabel note = new JLabel("⚡ All commands are simulated. Your PC is completely safe.");
        note.setFont(new Font("SansSerif", Font.ITALIC, 11));
        note.setForeground(new Color(80, 200, 120));
        note.setHorizontalAlignment(SwingConstants.CENTER);
        note.setBorder(BorderFactory.createEmptyBorder(4, 40, 28, 40));

        root.add(header, BorderLayout.NORTH);
        root.add(btnPanel, BorderLayout.CENTER);
        root.add(note, BorderLayout.SOUTH);

        setContentPane(root);
        setMinimumSize(new Dimension(520, 320));
        setResizable(false);
    }

    private JButton buildOSButton(String emoji, String osName, String style, Color accent, OSType type) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(24, 28, 44));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(18, 16, 18, 16)
        ));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(osName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel styleLabel = new JLabel(style);
        styleLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        styleLabel.setForeground(accent);
        styleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(emojiLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(styleLabel);

        // Wrap card in a button-like panel
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.add(card, BorderLayout.CENTER);
        btn.setPreferredSize(new Dimension(140, 130));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            selectedOS = type;
            dispose();
        });

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(34, 40, 62));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(24, 28, 44));
            }
        });

        return btn;
    }

    public OSType getSelectedOS() {
        return selectedOS;
    }
}