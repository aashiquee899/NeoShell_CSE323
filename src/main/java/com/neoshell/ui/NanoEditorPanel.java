package com.neoshell.ui;

import com.neoshell.backend.VirtualFileSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * A simulated nano-style in-app text editor panel.
 *
 * Lifecycle:
 *   1. MainWindow replaces the input bar + output area with this panel.
 *   2. User types freely in the editor area.
 *   3. Ctrl+S  → saves to VFS and exits.
 *      Ctrl+X  → exits without saving (with unsaved-changes warning).
 *   4. MainWindow restores its normal layout when this panel fires the exitCallback.
 */
public class NanoEditorPanel extends JPanel {

    // ── Colours (kept consistent with MainWindow palette) ─────────────────────
    private static final Color BG_DARK      = new Color(15, 18, 30);
    private static final Color BG_PANEL     = new Color(20, 24, 38);
    private static final Color BG_EDITOR    = new Color(18, 22, 36);
    private static final Color BG_STATUSBAR = new Color(80, 200, 120);   // green title bar
    private static final Color BG_SHORTCUT  = new Color(24, 28, 44);
    private static final Color GREEN        = new Color(80, 200, 120);
    private static final Color WHITE        = new Color(220, 225, 235);
    private static final Color GRAY         = new Color(100, 110, 130);
    private static final Color BLACK        = Color.BLACK;
    private static final Color YELLOW       = new Color(255, 220, 80);

    private final String             filename;
    private final VirtualFileSystem  vfs;
    private final Runnable           exitCallback;   // called when editor closes

    private JTextArea editorArea;
    private JLabel    statusLabel;
    private boolean   modified = false;

    /**
     * @param filename     The file being edited (may not exist yet — nano creates it on save).
     * @param vfs          The shared VirtualFileSystem instance.
     * @param initialContent  Existing file content, or "" for a new file.
     * @param exitCallback Runnable fired when the editor should close.
     */
    public NanoEditorPanel(String filename,
                           VirtualFileSystem vfs,
                           String initialContent,
                           Runnable exitCallback) {
        this.filename     = filename;
        this.vfs          = vfs;
        this.exitCallback = exitCallback;

        setLayout(new BorderLayout());
        setBackground(BG_EDITOR);

        add(buildTitleBar(),    BorderLayout.NORTH);
        add(buildEditorArea(initialContent), BorderLayout.CENTER);
        add(buildShortcutBar(), BorderLayout.SOUTH);

        // Track modifications
        editorArea.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    public void insertUpdate (javax.swing.event.DocumentEvent e) { markModified(); }
                    public void removeUpdate (javax.swing.event.DocumentEvent e) { markModified(); }
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { markModified(); }
                });

        // Key bindings
        editorArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_S) {
                        e.consume();
                        save();
                    } else if (e.getKeyCode() == KeyEvent.VK_X) {
                        e.consume();
                        tryExit();
                    }
                }
            }
        });

        SwingUtilities.invokeLater(() -> editorArea.requestFocusInWindow());
    }

    // ── Sub-panels ────────────────────────────────────────────────────────────

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_STATUSBAR);
        bar.setBorder(new EmptyBorder(4, 12, 4, 12));

        JLabel left = new JLabel("GNU nano 6.0  —  " + filename);
        left.setFont(new Font("Monospaced", Font.BOLD, 13));
        left.setForeground(BLACK);

        statusLabel = new JLabel("New File");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setForeground(BLACK);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        bar.add(left,        BorderLayout.WEST);
        bar.add(statusLabel, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildEditorArea(String initialContent) {
        editorArea = new JTextArea(initialContent);
        editorArea.setBackground(BG_EDITOR);
        editorArea.setForeground(WHITE);
        editorArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        editorArea.setCaretColor(GREEN);
        editorArea.setBorder(new EmptyBorder(8, 16, 8, 16));
        editorArea.setLineWrap(true);
        editorArea.setWrapStyleWord(true);
        editorArea.setTabSize(4);

        JScrollPane scroll = new JScrollPane(editorArea);
        scroll.setBackground(BG_EDITOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_EDITOR);
        return scroll;
    }

    private JPanel buildShortcutBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        bar.setBackground(BG_SHORTCUT);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 46, 66)));

        bar.add(shortcutKey("^S", "Save"));
        bar.add(shortcutKey("^X", "Exit"));
        bar.add(shortcutLabel("  |  Ctrl+S to save · Ctrl+X to exit"));

        return bar;
    }

    private JPanel shortcutKey(String key, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(BG_SHORTCUT);

        JLabel kl = new JLabel(key);
        kl.setFont(new Font("Monospaced", Font.BOLD, 12));
        kl.setForeground(BLACK);
        kl.setBackground(GREEN);
        kl.setOpaque(true);
        kl.setBorder(new EmptyBorder(1, 5, 1, 5));

        JLabel ll = new JLabel(label);
        ll.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ll.setForeground(GRAY);

        p.add(kl);
        p.add(ll);
        return p;
    }

    private JLabel shortcutLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.PLAIN, 11));
        l.setForeground(GRAY);
        return l;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void markModified() {
        modified = true;
        statusLabel.setText("Modified");
        statusLabel.setForeground(YELLOW);
    }

    private void save() {
        vfs.writeFile(filename, editorArea.getText());
        modified = false;
        statusLabel.setText("Saved");
        statusLabel.setForeground(BLACK);

        // Brief visual feedback, then close
        Timer t = new Timer(600, e -> exitCallback.run());
        t.setRepeats(false);
        t.start();
    }

    private void tryExit() {
        if (modified) {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Save modified buffer?\n\n  File: " + filename,
                    "nano — Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Save", "Discard", "Cancel"},
                    "Save"
            );
            if (choice == JOptionPane.YES_OPTION)    { save(); return; }
            if (choice == JOptionPane.NO_OPTION)     { exitCallback.run(); return; }
            // Cancel → stay in editor
        } else {
            exitCallback.run();
        }
    }
}