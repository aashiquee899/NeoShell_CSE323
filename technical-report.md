# NeoShell: Technical System Documentation

## 1. System Overview
**NeoShell** is a cross-platform, desktop-based terminal simulation application written in Java 17. Its primary function is to provide a risk-free command-line environment mimicking Windows CMD, macOS Zsh, and Linux Bash. The application relies entirely on an ephemeral, in-memory virtual file system, ensuring isolation from the host machine's actual OS and storage.

## 2. High-Level Architecture
The application follows a modular, pseudo-MVC (Model-View-Controller) architecture separated into three primary layers, as reflected in the project's package structure:

* **Model (`com.termij.model`):** Defines the core data structures and environmental states (e.g., the selected Operating System).
* **Backend / Controller (`com.termij.backend`):** Houses the business logic, command parsing, execution routing, and the virtualized file system state.
* **UI / View (`com.termij.ui`):** Built with Java Swing, this layer manages all user interactions, capturing keyboard inputs, and rendering terminal outputs and graphical dialogs.

---

## 3. Core Component Deep Dive

### 3.1. The User Interface Layer (`com.termij.ui`)
This package handles the visual presentation and user input collection.

* **`OSSelectorDialog.java`**
    * **Role:** The entry point interface. A modal dialog presented on startup.
    * **Functionality:** Captures the user's OS preference (Windows, macOS, or Linux) and passes this state to the `MainWindow` and `CommandEngine` to configure the correct prompt syntax and command dictionary.
* **`MainWindow.java`**
    * **Role:** The primary `JFrame` housing the terminal display.
    * **Functionality:**
        * Renders a dark-themed text area simulating a terminal window.
        * Implements a `KeyListener` to capture specific inputs (e.g., `Enter` to execute, `Up/Down` arrows to navigate command history).
        * Handles syntax highlighting (e.g., rendering the prompt string in green, standard output in white, and error messages in red).
* **`NanoEditorPanel.java`**
    * **Role:** A dedicated Swing panel that temporarily overlays or replaces the terminal view when the `nano` command is executed.
    * **Functionality:** Acts as a basic text buffer. It captures keystrokes for `Ctrl+S` (Save) to write the buffer back to the `VirtualFileSystem`, and `Ctrl+X` (Exit) to destroy the panel and return focus to the standard terminal prompt.

### 3.2. The Backend & Logic Layer (`com.termij.backend`)
This is the "brain" of the application, completely decoupled from the Swing UI to allow for isolated unit testing.

* **`CommandEngine.java`**
    * **Role:** The command parser, router, and executor.
    * **Functionality:**
        * Receives raw string input from `MainWindow.java`.
        * Tokenizes the input (splitting by spaces, while respecting quotes for strings).
        * Checks the active `OSType` to determine if the command is valid (e.g., rejecting `ls` if the OS is set to Windows).
        * Detects output redirection operators (`>` for overwrite, `>>` for append).
        * Calls the appropriate methods in `VirtualFileSystem.java`.
* **`CommandResult.java`**
    * **Role:** A Data Transfer Object (DTO).
    * **Functionality:** Encapsulates the outcome of an executed command. It typically contains fields for `outputMessage` (String), `errorMessage` (String), and `statusCode` (int or boolean). The `MainWindow` reads this object to determine what to print to the screen and in what color.
* **`VirtualFileSystem.java`**
    * **Role:** An in-memory data structure simulating a storage drive.
    * **Functionality:**
        * Likely implemented using an N-ary Tree or a Node-based graph structure, where each Node represents either a `File` (containing a text payload) or a `Directory` (containing references to child Nodes).
        * Maintains a pointer to the "Current Working Directory" (CWD).
        * Provides CRUD operations (Create, Read, Update, Delete) strictly within RAM. Once the application exits, the memory is freed and the "drive" is wiped.

### 3.3. The Model Layer (`com.termij.model`)
* **`OSType.java`**
    * **Role:** An Enum defining the selected environment (`WINDOWS`, `MAC`, `LINUX`).
    * **Functionality:** Used as a central state switch throughout the application. It dictates the prompt prefix (e.g., `C:\>`, `student@mac ~ %`, `user@linux:~$`) and selects the appropriate command dictionary in the `CommandEngine`.

---

## 4. Execution Lifecycle: Data Flow

When a user types a command and presses `Enter`, the system follows a strict sequential flow:

1.  **Input Capture:** `MainWindow.java` reads the active line from the JTextArea and extracts the user's string (e.g., `echo "Hello" > test.txt`).
2.  **Dispatch:** The raw string is passed to `CommandEngine.execute(String command)`.
3.  **Parsing & Validation:**
    * The engine identifies the base command (`echo`).
    * It checks the `OSType` to ensure `echo` is supported.
    * It detects the `>` operator, flagging this operation for file redirection rather than standard output.
4.  **File System Interaction:** The engine calls `VirtualFileSystem.writeToFile("test.txt", "Hello", overwrite=true)`.
5.  **Result Generation:** `CommandEngine` packages the success state into a `CommandResult` object (in this case, likely an empty output string, as redirection silences standard out).
6.  **UI Update:** `MainWindow` receives the `CommandResult`, prints any output (or errors), appends the appropriate OS prompt to a new line, and awaits the next input.

---

## 5. Environment Specifications

The application simulates distinct environments to provide an authentic experience. The `CommandEngine` rigidly enforces OS-specific aliases and tools.

| Feature Area | Unix-like (macOS Zsh / Linux Bash) | Windows (CMD) |
| :--- | :--- | :--- |
| **Directory Listing** | `ls`, `ls -l` | `dir` |
| **Directory Removal** | `rm -r <dir>` | `rmdir <dir>` |
| **File Deletion** | `rm <file>` | `del <file>` |
| **File Reading** | `cat <file>` | `type <file>` |
| **Clear Screen** | `clear` | `cls` |
| **System Info** | `uname` | `ver` |

*(Note: Shared utilities like `cd`, `mkdir`, `echo`, `whoami`, `nano`, and `exit` function identically across all environments, acting as core shared logic in the engine).*

---

## 6. Technical Stack & Build Process

* **Core Language:** Java 17 (utilizing modern features like Records, enhanced switch statements, and text blocks).
* **GUI Toolkit:** Java Swing (Native library, requiring no external UI dependencies).
* **Dependency Management:** Apache Maven (3.8+).
* **Artifact:** The build process (`mvn clean package`) compiles the source code and packages it into a single, executable fat JAR (`NeoShell-1.0-SNAPSHOT.jar`). This ensures zero-configuration portability for the end user.
