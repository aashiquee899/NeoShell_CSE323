package com.neoshell.ui;

import com.neoshell.backend.CommandEngine;
import com.neoshell.backend.CommandResult;
import com.neoshell.model.OSType;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {

    private OSType osType;

    // UI Components
    private JTextPane outputPane;
    private JTextField inputField;
    private JLabel promptLabel;
    private JLabel osLabel;

    // Colors
    private static final Color BG_DARK      = new Color(15, 18, 30);
    private static final Color BG_PANEL     = new Color(20, 24, 38);
    private static final Color BG_INPUT     = new Color(24, 28, 44);
    private static final Color GREEN        = new Color(80, 200, 120);
    private static final Color WHITE        = new Color(220, 225, 235);
    private static final Color RED          = new Color(255, 85, 85);
    private static final Color GRAY         = new Color(100, 110, 130);
    private static final Color ACCENT_WIN   = new Color(0, 120, 212);
    private static final Color ACCENT_MAC   = new Color(160, 160, 160);
    private static final Color ACCENT_LIN   = new Color(220, 95, 0);

    // Backend
    private CommandEngine commandEngine;

    // Nano editor support
    private JScrollPane   outputScrollPane;   // kept so we can swap it out
    private JPanel        inputBar;           // kept so we can hide/show it

    public MainWindow(OSType osType) {
        this.osType        = osType;
        this.commandEngine = new CommandEngine(osType);
        buildUI();
        printWelcomeMessage();
        setVisible(true);
    }

    private void buildUI() {
        setTitle("NeoShell — " + getOSLabel());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 560);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildTopBar(),                BorderLayout.NORTH);
        outputScrollPane = buildOutputArea();
        add(outputScrollPane,             BorderLayout.CENTER);
        inputBar = buildInputBar();
        add(inputBar,                     BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    // ── Top Bar ──────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(12, 14, 24));
        bar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dots.setBackground(new Color(12, 14, 24));
        dots.add(colorDot(new Color(255, 95, 87)));
        dots.add(colorDot(new Color(255, 189, 46)));
        dots.add(colorDot(new Color(39, 201, 63)));

        JLabel title = new JLabel("NeoShell");
        title.setFont(new Font("Monospaced", Font.BOLD, 14));
        title.setForeground(GREEN);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        osLabel = new JLabel(getOSBadge());
        osLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        osLabel.setForeground(getAccentColor());
        osLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        bar.add(dots,    BorderLayout.WEST);
        bar.add(title,   BorderLayout.CENTER);
        bar.add(osLabel, BorderLayout.EAST);

        return bar;
    }

    private JLabel colorDot(Color color) {
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dot.setForeground(color);
        return dot;
    }

    // ── Output Area ──────────────────────────────────────────────────────────
    private JScrollPane buildOutputArea() {
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(BG_DARK);
        outputPane.setForeground(WHITE);
        outputPane.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputPane.setCaretColor(GREEN);
        outputPane.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane scroll = new JScrollPane(outputPane);
        scroll.setBackground(BG_DARK);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setForeground(GRAY);

        return scroll;
    }

    // ── Input Bar ────────────────────────────────────────────────────────────
    private JPanel buildInputBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(BG_INPUT);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 46, 66)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        promptLabel = new JLabel(getPrompt());
        promptLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        promptLabel.setForeground(GREEN);

        inputField = new JTextField();
        inputField.setBackground(BG_INPUT);
        inputField.setForeground(WHITE);
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 13));
        inputField.setCaretColor(GREEN);
        inputField.setBorder(BorderFactory.createEmptyBorder());
        inputField.setOpaque(false);

        // Enter key → run command
        inputField.addActionListener(e -> handleInput());

        // Up/Down arrow → history navigation
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    inputField.setText(commandEngine.historyUp());
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    inputField.setText(commandEngine.historyDown());
                    e.consume();
                }
            }
        });

        bar.add(promptLabel, BorderLayout.WEST);
        bar.add(inputField,  BorderLayout.CENTER);

        return bar;
    }

    // ── Input Handler — wired to backend ─────────────────────────────────────
    private void handleInput() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) return;

        // Echo the typed command with prompt
        appendToOutput(getPrompt() + input + "\n", GREEN);
        inputField.setText("");

        // Send to backend
        CommandResult result = commandEngine.execute(input);

        switch (result.type) {
            case OUTPUT -> {
                if (!result.output.isEmpty())
                    appendToOutput(result.output + "\n", WHITE);
                appendToOutput("\n", WHITE);
            }
            case ERROR -> {
                appendToOutput(result.output + "\n\n", RED);
            }
            case CLEAR -> {
                clearOutput();
            }
            case PATH_CHANGED -> {
                // Directory changed — update prompt label silently
                updatePrompt();
                appendToOutput("\n", WHITE);
            }
            case EXIT -> {
                appendToOutput("Goodbye!\n", GRAY);
                Timer t = new Timer(800, e -> System.exit(0));
                t.setRepeats(false);
                t.start();
            }
            case NANO_OPEN -> {
                openNanoEditor(result.output);   // result.output holds the filename
                return;  // skip updatePrompt/scrollToBottom — layout is being replaced
            }
            case EMPTY -> {}
        }

        updatePrompt();
        scrollToBottom();
    }

    // ── Output Helpers ────────────────────────────────────────────────────────
    public void appendToOutput(String text, Color color) {
        StyledDocument doc   = outputPane.getStyledDocument();
        Style          style = outputPane.addStyle("style", null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 13);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void clearOutput() {
        outputPane.setText("");
    }

    private void updatePrompt() {
        promptLabel.setText(getPrompt());
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() ->
                outputPane.setCaretPosition(outputPane.getDocument().getLength())
        );
    }

    private void printWelcomeMessage() {
        appendToOutput("  NeoShell — Safe Terminal Simulator\n", GREEN);
        appendToOutput("  Simulating: " + getOSLabel() + "\n", getAccentColor());
        appendToOutput("  Type 'help' to see available commands.\n", GRAY);
        appendToOutput("─".repeat(58) + "\n\n", new Color(40, 46, 66));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String getPrompt() {
        String path = commandEngine != null ? commandEngine.getCurrentPath() : "~";
        return switch (osType) {
            case WINDOWS -> path + "> ";
            case MAC     -> "student@mac " + path + " % ";
            case LINUX   -> "student@neoshell:" + path + "$ ";
        };
    }

    private String getOSLabel() {
        return switch (osType) {
            case WINDOWS -> "Windows (CMD)";
            case MAC     -> "macOS (Zsh)";
            case LINUX   -> "Linux (Bash)";
        };
    }

    private String getOSBadge() {
        return switch (osType) {
            case WINDOWS -> "🪟 Windows";
            case MAC     -> "🍎 macOS";
            case LINUX   -> "🐧 Linux";
        };
    }

    private Color getAccentColor() {
        return switch (osType) {
            case WINDOWS -> ACCENT_WIN;
            case MAC     -> ACCENT_MAC;
            case LINUX   -> ACCENT_LIN;
        };
    }

    // ── Nano Editor ───────────────────────────────────────────────────────────

    private void openNanoEditor(String filename) {
        // Read existing content (empty string if file doesn't exist yet)
        String existingContent = commandEngine.vfs.readFile(filename);
        if (existingContent == null) existingContent = "";

        // Hide the normal terminal panels
        outputScrollPane.setVisible(false);
        inputBar.setVisible(false);

        // Build and insert the nano panel into CENTER
        NanoEditorPanel nanoPanel = new NanoEditorPanel(
                filename,
                commandEngine.vfs,
                existingContent,
                () -> closeNanoEditor(filename)   // exit callback
        );

        getContentPane().add(nanoPanel, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void closeNanoEditor(String filename) {
        // Remove ALL center-area components, then restore the output pane
        // (NanoEditorPanel was added to CENTER, so we find and remove it)
        BorderLayout layout = (BorderLayout) getContentPane().getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof NanoEditorPanel) {
            getContentPane().remove(center);
        }

        outputScrollPane.setVisible(true);
        inputBar.setVisible(true);

        getContentPane().add(outputScrollPane, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();

        // Confirm in terminal and refocus input
        appendToOutput(getPrompt() + "nano " + filename + "\n", GREEN);
        appendToOutput("[nano] File saved: " + filename + "\n\n", new Color(80, 200, 120));
        scrollToBottom();
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
        updatePrompt();
    }
}