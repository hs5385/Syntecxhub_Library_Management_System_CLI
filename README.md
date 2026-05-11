# Syntecxhub_Library_Management_System_CLI
Week 1 Task For Syntecxhub Internship Program


# 📚 Library Management System CLI

Hey there! 👋 Welcome to the **Library Management System CLI**. 

This is a modern, lightweight, and interactive Command Line Interface built in Java. It’s designed to help you easily manage library operations like tracking books, registering members, issuing books, and automatically calculating fines—all right from your terminal without the hassle of a complex UI.

We've focused heavily on making the terminal experience feel **smooth and modern**. Say goodbye to clunky, hard-to-read text interfaces! ✨

## ✨ Features

- **Modern CLI Interface**: A clean, minimalistic, and responsive terminal experience using clean ASCII formatting and intuitive prompts. Built with `JLine` and `Jansi` to make it look great and work smoothly.
- **Book Management**: Easily add, remove, search, and view all books in the library's inventory.
- **Lightweight Storage**: Uses a simple, portable local text file (`books.txt`) to keep your data persistent without the overhead of a database setup.

## 🛠️ Tech Stack

- **Language**: Java 24
- **Build Tool**: Maven
- **CLI Libraries**: 
  - [JLine](https://github.com/jline/jline3) for advanced terminal input, search, and arrow-key menu navigation.
  - [Jansi](https://fusesource.github.io/jansi/) for cross-platform ANSI escape codes (ensuring it looks great on Windows too!).
- **Data Storage**: Local File I/O (`books.txt`)

## 🚀 Getting Started

### Prerequisites
1. Ensure you have **Java 24** (or a compatible modern JDK) installed.
2. Have **Maven** installed and configured on your machine.

### Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/library-management-system-cli.git
   cd library-management-system-cli/untitled
   ```

2. **Build the project:**
   We use the Maven Assembly plugin to package everything neatly into a single, runnable JAR file.
   ```bash
   mvn clean compile assembly:single
   ```

3. **Run the CLI:**
   ```bash
   java --enable-native-access=ALL-UNNAMED -jar target\LibraryCLI-1.0-jar-with-dependencies.jar
   ```
   *(Note: The app will automatically create a `books.txt` file in your working directory to save your books when you add them.)*

## 🤝 Contributing

Got an idea to make this better? Found a bug? Contributions are super welcome! 
Feel free to open an issue or submit a Pull Request. Let's build something awesome together.

## 📄 License

This project is open-source. Feel free to use it, modify it, and share it!

---
*Built with ❤️ for a better terminal experience.*
