package com.neoshell.backend;

import com.neoshell.model.OSType;

import java.util.*;

/**
 * Routes a raw command string to the correct OS handler and returns the result.
 */
public class CommandEngine {

    private final OSType            osType;
    public final VirtualFileSystem vfs;
    private final List<String>      history;
    private int                     historyIndex;

    public CommandEngine(OSType osType) {
        this.osType       = osType;
        this.vfs          = new VirtualFileSystem();
        this.history      = new ArrayList<>();
        this.historyIndex = -1;
    }

    /** Execute a command string and return a CommandResult. */
    public CommandResult execute(String rawInput) {
        String input = rawInput.trim();
        if (input.isEmpty()) return CommandResult.empty();

        history.add(0, input);   // most-recent first
        historyIndex = -1;

        String[] tokens = tokenize(input);
        String   cmd    = tokens[0].toLowerCase();

        return switch (osType) {
            case WINDOWS -> executeWindows(cmd, tokens, input);
            case MAC     -> executeUnix(cmd, tokens, input, "Mac");
            case LINUX   -> executeUnix(cmd, tokens, input, "Linux");
        };
    }

    // ── History navigation ────────────────────────────────────────────────────

    public String historyUp() {
        if (history.isEmpty()) return "";
        historyIndex = Math.min(historyIndex + 1, history.size() - 1);
        return history.get(historyIndex);
    }

    public String historyDown() {
        if (historyIndex <= 0) { historyIndex = -1; return ""; }
        historyIndex--;
        return history.get(historyIndex);
    }

    // ── Current path (for prompt) ─────────────────────────────────────────────

    public String getCurrentPath() {
        return osType == OSType.WINDOWS
                ? vfs.getCurrentPathWindows()
                : vfs.getCurrentPath();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  WINDOWS COMMANDS
    // ══════════════════════════════════════════════════════════════════════════

    private CommandResult executeWindows(String cmd, String[] tokens, String raw) {
        return switch (cmd) {
            case "dir"    -> cmdDir();
            case "cd"     -> cmdCdWindows(tokens);
            case "mkdir"  -> cmdMkdir(tokens, "Windows");
            case "del"    -> cmdDelWindows(tokens);
            case "rmdir"  -> cmdRmdirWindows(tokens);
            case "echo"   -> cmdEcho(tokens, raw);
            case "cls"    -> CommandResult.clear();
            case "nano"   -> cmdNano(tokens);
            case "type"   -> cmdTypeWindows(tokens);
            case "copy"   -> cmdCopyWindows(tokens);
            case "move"   -> cmdMoveWindows(tokens);
            case "help"   -> cmdHelpWindows();
            case "ver"    -> CommandResult.ok("Microsoft Windows [NeoShell Simulator 1.0]");
            case "whoami" -> CommandResult.ok("neoshell\\student");
            case "exit"   -> CommandResult.exit();
            default       -> CommandResult.error("'" + tokens[0] + "' is not recognized as an internal or external command,\noperable program or batch file.");
        };
    }

    private CommandResult cmdDir() {
        List<String> items  = vfs.listCurrentDir();
        StringBuilder sb    = new StringBuilder();
        sb.append(" Directory of ").append(vfs.getCurrentPathWindows()).append("\n\n");
        sb.append(String.format(" %-6s  %s%n", "<DIR>", "."));
        sb.append(String.format(" %-6s  %s%n", "<DIR>", ".."));
        for (String name : items) {
            VirtualFileSystem.VFSNode node = vfs.getChild(name);
            if (node.isDirectory)
                sb.append(String.format(" %-6s  %s%n", "<DIR>", name));
            else
                sb.append(String.format(" %-6s  %s%n", "",       name));
        }
        sb.append("\n  ").append(items.size()).append(" File(s)");
        return CommandResult.ok(sb.toString());
    }

    private CommandResult cmdCdWindows(String[] tokens) {
        if (tokens.length < 2) {
            return CommandResult.ok(vfs.getCurrentPathWindows());
        }
        // Translate Windows path separators to Unix for VFS
        String target = tokens[1].replace("\\", "/");
        // Translate C:\Users\Student -> /home/student
        if (target.equalsIgnoreCase("C:/Users/Student") || target.equalsIgnoreCase("C:/")) {
            target = "~";
        }
        String err = vfs.changeDirectory(target);
        return err != null ? CommandResult.error(err) : CommandResult.pathChanged(vfs.getCurrentPathWindows());
    }

    private CommandResult cmdDelWindows(String[] tokens) {
        if (tokens.length < 2) return CommandResult.error("The syntax of the command is incorrect.");
        String err = vfs.removeFile(tokens[1]);
        return err != null ? CommandResult.error(err) : CommandResult.ok("");
    }

    private CommandResult cmdRmdirWindows(String[] tokens) {
        if (tokens.length < 2) return CommandResult.error("The syntax of the command is incorrect.");
        String name = tokens[tokens.length - 1]; // last arg (ignore /s /q etc.)
        String err  = vfs.removeDirectory(name);
        return err != null ? CommandResult.error(err) : CommandResult.ok("");
    }

    private CommandResult cmdTypeWindows(String[] tokens) {
        if (tokens.length < 2) return CommandResult.error("The syntax of the command is incorrect.");
        String content = vfs.readFile(tokens[1]);
        if (content == null) return CommandResult.error("The system cannot find the file specified.");
        return CommandResult.ok(content);
    }

    private CommandResult cmdCopyWindows(String[] tokens) {
        if (tokens.length < 3) return CommandResult.error("The syntax of the command is incorrect.");
        String err = vfs.copyFile(tokens[1], tokens[2]);
        return err != null ? CommandResult.error(err) : CommandResult.ok("        1 file(s) copied.");
    }

    private CommandResult cmdMoveWindows(String[] tokens) {
        if (tokens.length < 3) return CommandResult.error("The syntax of the command is incorrect.");
        String err = vfs.moveFile(tokens[1], tokens[2]);
        return err != null ? CommandResult.error(err) : CommandResult.ok("        1 file(s) moved.");
    }

    private CommandResult cmdHelpWindows() {
        return CommandResult.ok("""
                NeoShell — Windows CMD Commands
                ─────────────────────────────────────────────────
                DIR        List files in current directory
                CD <path>  Change directory (CD alone shows path)
                MKDIR <d>  Create a new directory
                DEL <file> Delete a file
                RMDIR <d>  Remove a directory
                COPY <s> <d>  Copy a file
                MOVE <s> <d>  Move or rename a file
                TYPE <f>   Display contents of a text file
                NANO <f>   Open nano-style editor for a file
                ECHO <msg> Print a message
                CLS        Clear the screen
                VER        Show version
                WHOAMI     Show current user
                HELP       Show this help
                EXIT       Exit NeoShell
                """);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MAC / LINUX COMMANDS
    // ══════════════════════════════════════════════════════════════════════════

    private CommandResult executeUnix(String cmd, String[] tokens, String raw, String flavor) {
        return switch (cmd) {
            case "ls"    -> cmdLs(tokens);
            case "cd"    -> cmdCdUnix(tokens);
            case "mkdir" -> cmdMkdir(tokens, flavor);
            case "rm"    -> cmdRmUnix(tokens);
            case "echo"  -> cmdEcho(tokens, raw);
            case "clear" -> CommandResult.clear();
            case "cat"   -> cmdCatUnix(tokens);
            case "cp"    -> cmdCpUnix(tokens);
            case "mv"    -> cmdMvUnix(tokens);
            case "pwd"   -> CommandResult.ok(vfs.getCurrentPath().equals("~")
                    ? "/home/student"
                    : vfs.getCurrentPath().replace("~", "/home/student"));
            case "touch" -> cmdTouchUnix(tokens);
            case "nano"  -> cmdNano(tokens);
            case "whoami"-> CommandResult.ok("student");
            case "uname" -> CommandResult.ok(flavor.equals("Mac") ? "Darwin" : "Linux");
            case "help"  -> cmdHelpUnix(flavor);
            case "exit"  -> CommandResult.exit();
            default      -> CommandResult.error(cmd + ": command not found");
        };
    }

    private CommandResult cmdLs(String[] tokens) {
        // Check for -l flag
        boolean longFormat = false;
        for (String t : tokens) if (t.equals("-l") || t.equals("-la") || t.equals("-al")) longFormat = true;

        List<String> items  = vfs.listCurrentDir();
        StringBuilder sb    = new StringBuilder();
        if (longFormat) {
            sb.append("total ").append(items.size()).append("\n");
            for (String name : items) {
                VirtualFileSystem.VFSNode node = vfs.getChild(name);
                String type = node.isDirectory ? "d" : "-";
                sb.append(String.format("%srwxr-xr-x  1 student student  4096 May 01 10:00 %s%n", type, name));
            }
        } else {
            StringBuilder row = new StringBuilder();
            for (String name : items) {
                VirtualFileSystem.VFSNode node = vfs.getChild(name);
                String display = node.isDirectory ? name + "/" : name;
                row.append(String.format("%-20s", display));
            }
            sb.append(row.toString().stripTrailing());
        }
        return CommandResult.ok(sb.toString());
    }

    private CommandResult cmdCdUnix(String[] tokens) {
        String target = (tokens.length < 2 || tokens[1].isEmpty()) ? "~" : tokens[1];
        String err    = vfs.changeDirectory(target);
        return err != null ? CommandResult.error(err) : CommandResult.pathChanged(vfs.getCurrentPath());
    }

    private CommandResult cmdRmUnix(String[] tokens) {
        if (tokens.length < 2) return CommandResult.error("rm: missing operand");
        boolean recursive = false;
        String  name      = null;
        for (int i = 1; i < tokens.length; i++) {
            if (tokens[i].startsWith("-")) {
                if (tokens[i].contains("r") || tokens[i].contains("R")) recursive = true;
            } else {
                name = tokens[i];
            }
        }
        if (name == null) return CommandResult.error("rm: missing operand");
        VirtualFileSystem.VFSNode node = vfs.getChild(name);
        if (node == null) return CommandResult.error("rm: " + name + ": No such file or directory");
        String err = node.isDirectory
                ? (recursive ? vfs.removeDirectory(name)
                   : CommandResult.error("rm: " + name + ": Is a directory").output)
                : vfs.removeFile(name);
        return err != null ? CommandResult.error(err) : CommandResult.ok("");
    }

    private CommandResult cmdCatUnix(String[] tokens) {
        if (tokens.length < 2) return CommandResult.error("cat: missing operand");
        String content = vfs.readFile(tokens[1]);
        if (content == null) return CommandResult.error("cat: " + tokens[1] + ": No such file or directory");
        return CommandResult.ok(content);
    }

    private CommandResult cmdCpUnix(String[] tokens) {
        if (tokens.length < 3) return CommandResult.error("cp: missing file operand");
        String err = vfs.copyFile(tokens[1], tokens[2]);
        return err != null ? CommandResult.error(err) : CommandResult.ok("");
    }

    private CommandResult cmdMvUnix(String[] tokens) {
        if (tokens.length < 3) return CommandResult.error("mv: missing file operand");
        String err = vfs.moveFile(tokens[1], tokens[2]);
        return err != null ? CommandResult.error(err) : CommandResult.ok("");
    }

    private CommandResult cmdTouchUnix(String[] tokens) {
        if (tokens.length < 2) return CommandResult.error("touch: missing file operand");
        vfs.writeFile(tokens[1], "");
        return CommandResult.ok("");
    }

    private CommandResult cmdHelpUnix(String flavor) {
        return CommandResult.ok("""
                NeoShell — %s Commands
                ─────────────────────────────────────────────────
                ls [-l]      List directory contents
                cd <path>    Change directory
                mkdir <dir>  Create a new directory
                rm [-r] <f>  Remove a file (or directory with -r)
                cp <s> <d>   Copy a file
                mv <s> <d>   Move or rename a file
                cat <file>   Display file contents
                touch <file> Create an empty file
                nano <file>  Open nano-style editor for a file
                pwd          Print working directory
                echo <msg>   Print a message
                clear        Clear the screen
                whoami       Print current username
                uname        Print OS name
                help         Show this help
                exit         Exit NeoShell
                """.formatted(flavor));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SHARED COMMANDS
    // ══════════════════════════════════════════════════════════════════════════

    private CommandResult cmdNano(String[] tokens) {
        if (tokens.length < 2) return CommandResult.error("nano: missing filename");
        return CommandResult.nanoOpen(tokens[1]);
    }

    private CommandResult cmdMkdir(String[] tokens, String os) {
        if (tokens.length < 2) {
            String msg = os.equals("Windows")
                    ? "The syntax of the command is incorrect."
                    : "mkdir: missing operand";
            return CommandResult.error(msg);
        }
        String err = vfs.makeDirectory(tokens[1]);
        return err != null ? CommandResult.error(err) : CommandResult.ok("");
    }

    private CommandResult cmdEcho(String[] tokens, String raw) {
        // Check for redirection operators >> or > before parsing the message
        // Patterns:  echo Some Text > file.txt
        //            echo More Text >> file.txt

        String lowerRaw = raw.toLowerCase();
        int echoIdx = lowerRaw.indexOf("echo");
        String afterEcho = (echoIdx >= 0 && raw.length() > echoIdx + 5)
                ? raw.substring(echoIdx + 5)   // everything after "echo "
                : "";

        // Detect >> (append) first, then > (overwrite) — order matters
        int appendIdx    = afterEcho.indexOf(">>");
        int overwriteIdx = afterEcho.indexOf(">");

        if (appendIdx >= 0) {
            // Append mode
            String msg      = afterEcho.substring(0, appendIdx).trim();
            String filename = afterEcho.substring(appendIdx + 2).trim();
            if (filename.isEmpty()) return CommandResult.error("syntax error: expected filename after '>>'");
            vfs.appendFile(filename, msg);
            return CommandResult.ok("");          // no terminal output — just like a real shell
        }

        if (overwriteIdx >= 0) {
            // Overwrite mode
            String msg      = afterEcho.substring(0, overwriteIdx).trim();
            String filename = afterEcho.substring(overwriteIdx + 1).trim();
            if (filename.isEmpty()) return CommandResult.error("syntax error: expected filename after '>'");
            vfs.writeFile(filename, msg);
            return CommandResult.ok("");
        }

        // Plain echo — no redirection
        return CommandResult.ok(afterEcho.trim());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TOKENIZER
    // ══════════════════════════════════════════════════════════════════════════

    /** Simple tokenizer that respects quoted strings. */
    private String[] tokenize(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur  = new StringBuilder();
        boolean inQuote    = false;
        char quoteChar     = '"';

        for (char c : input.toCharArray()) {
            if (inQuote) {
                if (c == quoteChar) inQuote = false;
                else cur.append(c);
            } else if (c == '"' || c == '\'') {
                inQuote  = true;
                quoteChar = c;
            } else if (c == ' ') {
                if (cur.length() > 0) { parts.add(cur.toString()); cur.setLength(0); }
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) parts.add(cur.toString());
        return parts.isEmpty() ? new String[]{""} : parts.toArray(new String[0]);
    }
}