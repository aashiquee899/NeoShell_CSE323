package com.neoshell.backend;

/**
 * The result returned by CommandEngine after processing a command.
 */
public class CommandResult {

    public enum Type {
        OUTPUT,       // Normal text output
        ERROR,        // Error message (shown in red)
        CLEAR,        // Clear the terminal screen
        PATH_CHANGED, // Current directory changed (update prompt)
        EXIT,         // User typed 'exit'
        EMPTY,        // No-op (blank input)
        NANO_OPEN     // Open the nano-style editor for a given filename
    }

    public final Type   type;
    public final String output;

    private CommandResult(Type type, String output) {
        this.type   = type;
        this.output = output;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static CommandResult ok(String text)          { return new CommandResult(Type.OUTPUT,       text); }
    public static CommandResult error(String msg)        { return new CommandResult(Type.ERROR,        msg);  }
    public static CommandResult clear()                  { return new CommandResult(Type.CLEAR,        "");   }
    public static CommandResult pathChanged(String path) { return new CommandResult(Type.PATH_CHANGED, path); }
    public static CommandResult exit()                   { return new CommandResult(Type.EXIT,         "");   }
    public static CommandResult empty()                  { return new CommandResult(Type.EMPTY,        "");   }
    public static CommandResult nanoOpen(String filename){ return new CommandResult(Type.NANO_OPEN,    filename); }
}