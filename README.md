<div align="center">

# 💻 NeoShell

### A safe, simulated terminal emulator built with Java Swing

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-blue?style=for-the-badge)]()

**NeoShell** is a desktop terminal simulator that lets you practice real shell commands — safely, without touching your actual file system. Choose between Windows CMD, macOS Zsh, or Linux Bash environments, each with its own prompt style, command set, and authentic behavior.

> ⚡ *All commands are simulated. Your PC is completely safe.*

</div>

---

## 🤔 Why NeoShell?

Many students and beginners avoid learning the terminal because of one very real fear: **accidentally breaking their system.**

Deleting the wrong files. Modifying permissions. Crashing the OS. These aren't hypotheticals — a single wrong command in a real terminal can cause serious, irreversible damage.

The only "safe" alternative until now was setting up a full Virtual Machine — which is heavy, complex, and overkill just for learning `cd` and `mkdir`.

**NeoShell changes that.** It gives you a terminal that *looks*, *feels*, and *behaves* exactly like the real thing — but executes nothing on your actual machine. Everything happens inside a sandboxed, in-memory virtual file system. You can type `rm -rf /` and nothing will happen. You can experiment, make mistakes, and learn — completely risk-free.

### ✅ Why you should use NeoShell

- **Zero risk** — no real commands ever touch your file system or OS
- **No setup needed** — just run the JAR file and start practicing immediately
- **Three OS environments in one** — practice Windows CMD, macOS Zsh, and Linux Bash without switching machines
- **Realistic experience** — authentic prompts, command behavior, error messages, and a nano-style text editor
- **Perfect for students, bootcamps, and self-learners** — build real terminal confidence before going live
- **Completely offline** — no internet required, no accounts, no tracking

---

## 📸 Screenshots

### 🖥️ OS Selection Screen
Choose your terminal environment on launch — Windows CMD, macOS Zsh, or Linux Bash.

![NeoShell OS Selector](screenshots/selector.png)

---

### 🍎 macOS Zsh Mode — Terminal + Help
Authentic `student@mac ~ %` prompt with full command support and color-coded output.

![NeoShell macOS Terminal](screenshots/macos.png)

---

### 📝 Built-in nano Editor
Type `nano filename.txt` to open a fully functional in-app text editor. Save with `Ctrl+S`, exit with `Ctrl+X`.

![NeoShell Nano Editor](screenshots/nano.png)

---

## ✨ Features

- **3 OS Modes** — Switch between Windows CMD, macOS Zsh, and Linux Bash, each with authentic prompts and commands
- **Virtual File System** — A fully in-memory file system with directories, files, read/write — nothing touches your real disk
- **Redirection Operators** — Use `>` to write and `>>` to append output to files, just like a real shell
- **nano-style Editor** — Type `nano filename.txt` to open an in-app text editor with `Ctrl+S` to save and `Ctrl+X` to exit
- **Command History** — Navigate previous commands with the ↑ and ↓ arrow keys
- **Syntax-colored Output** — Green for prompts, white for output, red for errors
- **Clean Dark UI** — A polished dark terminal aesthetic built entirely with Java Swing

---

## 🖥️ Supported Commands

### Linux / macOS
| Command | Description |
|---|---|
| `ls [-l]` | List directory contents |
| `cd <path>` | Change directory |
| `mkdir <dir>` | Create a directory |
| `rm [-r] <file>` | Remove a file or directory |
| `cp <src> <dest>` | Copy a file |
| `mv <src> <dest>` | Move or rename a file |
| `cat <file>` | Display file contents |
| `touch <file>` | Create an empty file |
| `pwd` | Print working directory |
| `echo <msg> [> file]` | Print message or redirect to file |
| `nano <file>` | Open the built-in text editor |
| `whoami` | Print current user |
| `uname` | Print OS name |
| `clear` | Clear the screen |
| `help` | Show all commands |
| `exit` | Exit NeoShell |

### Windows CMD
| Command | Description |
|---|---|
| `dir` | List directory contents |
| `cd <path>` | Change directory |
| `mkdir <dir>` | Create a directory |
| `del <file>` | Delete a file |
| `rmdir <dir>` | Remove a directory |
| `copy <src> <dest>` | Copy a file |
| `move <src> <dest>` | Move or rename a file |
| `type <file>` | Display file contents |
| `echo <msg> [> file]` | Print message or redirect to file |
| `nano <file>` | Open the built-in text editor |
| `cls` | Clear the screen |
| `ver` | Show version |
| `whoami` | Show current user |
| `help` | Show all commands |
| `exit` | Exit NeoShell |

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8 or higher

### Clone & Run

```bash
git clone https://github.com/YOUR_USERNAME/NeoShell.git
cd NeoShell
mvn clean package
java -jar target/NeoShell-1.0-SNAPSHOT.jar
```

Or simply open the project in **IntelliJ IDEA** and run `Main.java`.

### First Launch

1. When NeoShell opens, you'll see the **OS Selector** — pick Windows, macOS, or Linux
2. You're dropped into a fully simulated terminal with a pre-populated virtual file system
3. Type `help` to see all available commands
4. Start exploring! Try `ls`, `mkdir myfolder`, `cd myfolder`, `touch hello.txt`, `nano hello.txt`

> 💡 **Tip:** Use the ↑ and ↓ arrow keys to navigate your command history, just like a real terminal.

---

## 📁 Project Structure

```
NeoShell/
├── src/
│   └── main/
│       └── java/
│           └── com/termij/
│               ├── Main.java                  # Entry point
│               ├── model/
│               │   └── OSType.java            # Enum: WINDOWS, MAC, LINUX
│               ├── backend/
│               │   ├── CommandEngine.java      # Routes & executes commands
│               │   ├── CommandResult.java      # Result type returned to UI
│               │   └── VirtualFileSystem.java  # In-memory file system
│               └── ui/
│                   ├── MainWindow.java         # Main terminal UI (JFrame)
│                   ├── OSSelectorDialog.java   # OS picker on startup
│                   └── NanoEditorPanel.java    # Built-in nano-style editor
├── screenshots/
│   ├── selector.png
│   ├── macos.png
│   └── nano.png
├── pom.xml
└── README.md
```

---

## 🛠️ Tech Stack

- **Language:** Java 17
- **UI Framework:** Java Swing
- **Build Tool:** Maven
- **IDE:** IntelliJ IDEA

---

## 🤝 Contributing

Pull requests are welcome! If you'd like to add new commands, fix a bug, or improve the UI, feel free to fork the repo and open a PR.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/new-command`)
3. Commit your changes (`git commit -m 'Add some command'`)
4. Push to the branch (`git push origin feature/new-command`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Stop being afraid of the terminal. Start practicing with NeoShell.**

Made with ☕ and Java &nbsp;|&nbsp; <strong>NeoShell — Safe Terminal Simulator</strong>

</div>
