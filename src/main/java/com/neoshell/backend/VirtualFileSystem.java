package com.neoshell.backend;

import java.util.*;

/**
 * In-memory virtual file system.
 * Represents a tree of directories and files — nothing touches the real disk.
 */
public class VirtualFileSystem {

    // A single node in the virtual FS tree (either a file or directory)
    public static class VFSNode {
        public final String name;
        public boolean isDirectory;
        public String content;                          // only meaningful for files
        public Map<String, VFSNode> children;           // only meaningful for directories

        public VFSNode(String name, boolean isDirectory) {
            this.name        = name;
            this.isDirectory = isDirectory;
            this.content     = "";
            this.children    = isDirectory ? new LinkedHashMap<>() : null;
        }
    }

    private final VFSNode root;
    private VFSNode       currentDir;
    private final List<String> pathStack; // tracks breadcrumb for prompt display

    public VirtualFileSystem() {
        // Build a small starter directory tree
        root = new VFSNode("/", true);

        // home/student
        VFSNode home    = mkDir(root, "home");
        VFSNode student = mkDir(home, "student");
        mkDir(student, "Documents");
        mkDir(student, "Downloads");
        mkDir(student, "Desktop");
        addFile(student, "readme.txt",  "Welcome to NeoShell!\nThis is a safe terminal simulator.");
        addFile(student, "notes.txt",   "Remember to study OS concepts!");

        // etc
        VFSNode etc = mkDir(root, "etc");
        addFile(etc, "hosts",   "127.0.0.1   localhost");
        addFile(etc, "version", "NeoShell OS 1.0");

        // start at home/student
        currentDir = student;
        pathStack  = new ArrayList<>(Arrays.asList("home", "student"));
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private VFSNode mkDir(VFSNode parent, String name) {
        VFSNode dir = new VFSNode(name, true);
        parent.children.put(name, dir);
        return dir;
    }

    private void addFile(VFSNode parent, String name, String content) {
        VFSNode file = new VFSNode(name, false);
        file.content = content;
        parent.children.put(name, file);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns names of all children of current directory. */
    public List<String> listCurrentDir() {
        List<String> names = new ArrayList<>(currentDir.children.keySet());
        Collections.sort(names);
        return names;
    }

    /** Returns a VFSNode child of current dir, or null. */
    public VFSNode getChild(String name) {
        return currentDir.children.get(name);
    }

    /** Change directory. Returns error string or null on success. */
    public String changeDirectory(String target) {
        if (target.equals("~") || target.equals("/home/student") || target.isEmpty()) {
            // cd ~ → back to home
            pathStack.clear();
            pathStack.addAll(Arrays.asList("home", "student"));
            currentDir = navigateFromRoot(pathStack);
            return null;
        }

        if (target.equals("..")) {
            if (pathStack.isEmpty()) return null; // already at root
            pathStack.remove(pathStack.size() - 1);
            currentDir = navigateFromRoot(pathStack);
            return null;
        }

        if (target.equals("/")) {
            pathStack.clear();
            currentDir = root;
            return null;
        }

        // Absolute path starting with /
        if (target.startsWith("/")) {
            String[] parts = target.substring(1).split("/");
            List<String> newStack = new ArrayList<>();
            VFSNode node = root;
            for (String part : parts) {
                if (part.isEmpty()) continue;
                if (node.children == null || !node.children.containsKey(part))
                    return "cd: " + target + ": No such file or directory";
                node = node.children.get(part);
                if (!node.isDirectory)
                    return "cd: " + target + ": Not a directory";
                newStack.add(part);
            }
            pathStack.clear();
            pathStack.addAll(newStack);
            currentDir = node;
            return null;
        }

        // Relative path
        String[] parts = target.split("/");
        VFSNode node = currentDir;
        List<String> newStack = new ArrayList<>(pathStack);
        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (!newStack.isEmpty()) newStack.remove(newStack.size() - 1);
                node = navigateFromRoot(newStack);
                continue;
            }
            if (node.children == null || !node.children.containsKey(part))
                return "cd: " + target + ": No such file or directory";
            node = node.children.get(part);
            if (!node.isDirectory)
                return "cd: " + target + ": Not a directory";
            newStack.add(part);
        }
        pathStack.clear();
        pathStack.addAll(newStack);
        currentDir = node;
        return null;
    }

    /** Make a directory in current dir. Returns error or null. */
    public String makeDirectory(String name) {
        if (currentDir.children.containsKey(name))
            return "mkdir: " + name + ": File exists";
        mkDir(currentDir, name);
        return null;
    }

    /** Remove a file (rm/del). Returns error or null. */
    public String removeFile(String name) {
        VFSNode node = currentDir.children.get(name);
        if (node == null)
            return name + ": No such file or directory";
        if (node.isDirectory)
            return name + ": Is a directory";
        currentDir.children.remove(name);
        return null;
    }

    /** Remove a directory (rm -r / rmdir). Returns error or null. */
    public String removeDirectory(String name) {
        VFSNode node = currentDir.children.get(name);
        if (node == null)
            return name + ": No such file or directory";
        if (!node.isDirectory)
            return name + ": Not a directory";
        currentDir.children.remove(name);
        return null;
    }

    /** Read a file's content. Returns content or null if not found. */
    public String readFile(String name) {
        VFSNode node = currentDir.children.get(name);
        if (node == null || node.isDirectory) return null;
        return node.content;
    }

    /** Create or overwrite a file. */
    public void writeFile(String name, String content) {
        VFSNode node = currentDir.children.get(name);
        if (node == null || node.isDirectory) {
            node = new VFSNode(name, false);
            currentDir.children.put(name, node);
        }
        node.content = content;
    }

    /** Append content to a file (creates if not found). */
    public void appendFile(String name, String content) {
        VFSNode node = currentDir.children.get(name);
        if (node == null || node.isDirectory) {
            node = new VFSNode(name, false);
            node.content = content;
            currentDir.children.put(name, node);
        } else {
            node.content = node.content + "\n" + content;
        }
    }

    /** Copy a file within the current directory. Returns error or null. */
    public String copyFile(String src, String dest) {
        VFSNode srcNode = currentDir.children.get(src);
        if (srcNode == null)          return "cp: " + src  + ": No such file or directory";
        if (srcNode.isDirectory)      return "cp: " + src  + ": Is a directory";
        VFSNode destNode = new VFSNode(dest, false);
        destNode.content = srcNode.content;
        currentDir.children.put(dest, destNode);
        return null;
    }

    /** Move/rename a file within the current directory. Returns error or null. */
    public String moveFile(String src, String dest) {
        VFSNode srcNode = currentDir.children.get(src);
        if (srcNode == null) return "mv: " + src + ": No such file or directory";
        currentDir.children.remove(src);
        currentDir.children.put(dest, new VFSNode(dest, srcNode.isDirectory) {{
            content  = srcNode.content;
            children = srcNode.children;
        }});
        return null;
    }

    /** Returns current path string for the prompt. */
    public String getCurrentPath() {
        if (pathStack.isEmpty()) return "/";
        // show ~ if inside home/student
        if (pathStack.size() >= 2
                && pathStack.get(0).equals("home")
                && pathStack.get(1).equals("student")) {
            if (pathStack.size() == 2) return "~";
            return "~/" + String.join("/", pathStack.subList(2, pathStack.size()));
        }
        return "/" + String.join("/", pathStack);
    }

    /** Returns Windows-style path string. */
    public String getCurrentPathWindows() {
        if (pathStack.isEmpty()) return "C:\\";
        if (pathStack.size() >= 2
                && pathStack.get(0).equals("home")
                && pathStack.get(1).equals("student")) {
            if (pathStack.size() == 2) return "C:\\Users\\Student";
            return "C:\\Users\\Student\\" +
                    String.join("\\", pathStack.subList(2, pathStack.size()));
        }
        return "C:\\" + String.join("\\", pathStack);
    }

    // Navigate from root following a path stack
    private VFSNode navigateFromRoot(List<String> stack) {
        VFSNode node = root;
        for (String part : stack) {
            if (node.children == null || !node.children.containsKey(part)) return root;
            node = node.children.get(part);
        }
        return node;
    }
}