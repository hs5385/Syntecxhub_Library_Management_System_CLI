import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.Attributes;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

public class LibraryCLI {

    static final String FILE_PATH = "books.txt";
    static List<Book> books = new ArrayList<>();
    static final int WIDTH = 72;

    // ── ANSI Helpers ──────────────────────────────────────────
    static String cyan(String s) {
        return ansi().fg(CYAN).a(s).reset().toString();
    }

    static String green(String s) {
        return ansi().fg(GREEN).bold().a(s).reset().toString();
    }

    static String red(String s) {
        return ansi().fg(RED).bold().a(s).reset().toString();
    }

    static String yellow(String s) {
        return ansi().fg(YELLOW).a(s).reset().toString();
    }

    static String white(String s) {
        return ansi().fg(WHITE).bold().a(s).reset().toString();
    }

    static String dim(String s) {
        return ansi().fgBright(Ansi.Color.BLACK).a(s).reset().toString();
    }

    static String bold(String s) {
        return ansi().bold().a(s).reset().toString();
    }

    static String repeat(String ch, int n) {
        return ch.repeat(n);
    }

    // ── UI Components ─────────────────────────────────────────
    static void printDivider() {
        System.out.println(dim("  " + repeat("-", WIDTH)));
    }

    static void printRow(String content) {
        System.out.println("  " + content);
    }

    static void printEmpty() {
        System.out.println();
    }

    static void printHeader() {
        System.out.println();
        System.out.println("  " + bold(cyan("* Library CLI")));
        System.out.println(dim("  Manage your books with ease"));
        System.out.println();
    }

    // ── Status Messages ───────────────────────────────────────
    static void success(String msg) {
        System.out.println("\n" + green("  [OK] ") + white(msg));
    }

    static void error(String msg) {
        System.out.println("\n" + red("  [X] ") + white(msg));
    }

    static void info(String msg) {
        System.out.println(cyan("  [i] ") + msg);
    }

    static void prompt(String label) {
        System.out.print(cyan("  ? ") + bold(white(label)) + dim(" > ") + ansi().fg(CYAN));
    }

    static void resetColor() {
        System.out.print(ansi().reset());
    }

    // ── Spinner (brief loading effect) ────────────────────────
    static void spinner(String task) throws InterruptedException {
        String[] frames = { "-", "\\", "|", "/" };
        for (int i = 0; i < 10; i++) {
            System.out.print("\r" + cyan("  " + frames[i % frames.length] + " ") + dim(task));
            Thread.sleep(40);
        }
        System.out.print("\r" + " ".repeat(WIDTH) + "\r");
    }

    // ── Section Title ─────────────────────────────────────────
    static void sectionTitle(String title) {
        System.out.println();
        System.out.println(bold(white("  " + title)));
        System.out.println(dim("  " + repeat("-", title.length())));
    }

    // ─────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        System.setProperty("org.jline.terminal.disableDeprecatedProviderWarning", "true");
        loadBooks();

        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .jna(true)
                .jansi(false)
                .encoding(StandardCharsets.UTF_8)
                .build();
        Attributes savedAttrs = terminal.getAttributes(); // save normal mode
        terminal.enterRawMode();

        String[] options = {
                "  Add Book",
                "  Remove Book",
                "  Search Book",
                "  View All Books",
                "  Exit"
        };

        int selected = 0;

        while (true) {
            terminal.writer().print(ansi().eraseScreen().cursor(1, 1));
            terminal.writer().flush();
            printHeader();
            printMenu(options, selected);

            int key = terminal.reader().read();

            if (key == 27) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
                if (terminal.reader().ready()) {
                    terminal.reader().read(); // skip [
                    int arrow = terminal.reader().read();
                    if (arrow == 65 && selected > 0)
                        selected--;
                    if (arrow == 66 && selected < options.length - 1)
                        selected++;
                }
            } else if (key == 'w' || key == 'W' || key == 'k' || key == 'K') {
                if (selected > 0)
                    selected--;
            } else if (key == 's' || key == 'S' || key == 'j' || key == 'J') {
                if (selected < options.length - 1)
                    selected++;
            } else if (key == 13 || key == 10) {
                terminal.setAttributes(savedAttrs); // ← restore normal mode
                terminal.writer().print(ansi().eraseScreen().cursor(1, 1));
                terminal.writer().flush();
                printHeader();
                LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

                switch (selected) {
                    case 0 -> addBook(reader);
                    case 1 -> removeBook(reader);
                    case 2 -> searchBook(reader);
                    case 3 -> viewAllBooks();
                    case 4 -> {
                        saveBooks();
                        System.out.println("\n" + cyan("  Bye! ") + dim("Session ended.\n"));
                        terminal.close();
                        return;
                    }
                }

                System.out.println("\n" + dim("  Press Enter to go back..."));
                reader.readLine();
                terminal.enterRawMode(); // ← back to raw mode for arrow keys
                selected = 0;
            }
        }
    }

    // ── Book Model ────────────────────────────────────────────
    static class Book {
        int id;
        String title, author, genre;
        boolean available;

        Book(int id, String title, String author, String genre) {
            this.id = id;
            this.title = title.trim();
            this.author = author.trim();
            this.genre = genre.trim();
            this.available = true;
        }

        String toFileLine() {
            return id + "|" + title + "|" + author + "|" + genre + "|" + available;
        }

        static Book fromFileLine(String line) {
            String[] p = line.split("\\|");
            Book b = new Book(Integer.parseInt(p[0]), p[1], p[2], p[3]);
            b.available = Boolean.parseBoolean(p[4]);
            return b;
        }

        String formatted() {
            String avail = available ? green("* Available") : red("x Checked Out");
            String titleTrunc = title.length() > 26 ? title.substring(0, 23) + "..." : title;
            String authorTrunc = author.length() > 16 ? author.substring(0, 13) + "..." : author;
            String genreTrunc = genre.length() > 11 ? genre.substring(0, 8) + "..." : genre;

            String paddedTitle = String.format("%-26s", titleTrunc);
            String paddedGenre = String.format("%-11s", genreTrunc);

            return String.format("%s   %s   %-16s   %s   %s",
                    dim(String.format("%03d", id)),
                    white(paddedTitle),
                    authorTrunc,
                    cyan(paddedGenre),
                    avail);
        }
    }

    static void printMenu(String[] options, int selected) {
        System.out.println();
        for (int i = 0; i < options.length; i++) {
            if (i == selected) {
                System.out.println(cyan("  > ") + ansi().bold().fg(CYAN).a(options[i].trim()).reset());
            } else {
                System.out.println(dim("    " + options[i].trim()));
            }
        }
        System.out.println();
        System.out.println(dim("  [^/W] Up  [v/S] Down  [Enter] Select"));
    }

    // ── Add Book ──────────────────────────────────────────────
    static void addBook(LineReader reader) throws InterruptedException {
        sectionTitle("Add New Book");

        prompt("Title");
        String title = reader.readLine().trim().replace("|", "");
        resetColor();
        if (title.isEmpty()) {
            error("Title cannot be empty.");
            return;
        }
        if (books.stream().anyMatch(b -> b.title.equalsIgnoreCase(title))) {
            error("A book with this title already exists.");
            return;
        }

        prompt("Author");
        String author = reader.readLine().trim().replace("|", "");
        resetColor();
        if (author.isEmpty()) {
            error("Author cannot be empty.");
            return;
        }

        prompt("Genre");
        String genre = reader.readLine().trim().replace("|", "");
        resetColor();
        if (genre.isEmpty()) {
            error("Genre cannot be empty.");
            return;
        }

        spinner("Saving book...");
        int newId = books.isEmpty() ? 1 : books.get(books.size() - 1).id + 1;
        books.add(new Book(newId, title, author, genre));
        saveBooks();
        success("Book added — " + white("\"" + title + "\"") + dim(" (ID: " + newId + ")"));
    }

    // ── Remove Book ───────────────────────────────────────────
    static void removeBook(LineReader reader) throws InterruptedException {
        if (books.isEmpty()) {
            error("Library is empty.");
            return;
        }
        sectionTitle("Remove Book");

        prompt("Book ID");
        String input = reader.readLine().trim();
        resetColor();
        int id;
        try {
            id = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            error("Invalid ID — must be a number.");
            return;
        }

        Optional<Book> found = books.stream().filter(b -> b.id == id).findFirst();
        if (found.isEmpty()) {
            error("No book found with ID: " + id);
            return;
        }

        System.out.println();
        info("Found: " + found.get().formatted());
        System.out.println();
        prompt("Confirm remove? (yes/no)");
        String confirm = reader.readLine().trim().toLowerCase();
        resetColor();

        if (confirm.equals("yes") || confirm.equals("y")) {
            spinner("Removing...");
            books.remove(found.get());
            saveBooks();
            success("Book removed successfully.");
        } else {
            info("Cancelled.");
        }
    }

    // ── Search Book ───────────────────────────────────────────
    static void searchBook(LineReader reader) {
        sectionTitle("Search Books");
        info("Search by: " + cyan("[1]") + " Title  " + cyan("[2]") + " Author  " + cyan("[3]") + " Genre");
        System.out.println();

        prompt("Search by");
        String searchChoice = reader.readLine().trim();
        resetColor();
        prompt("Keyword");
        String keyword = reader.readLine().trim().toLowerCase();
        resetColor();
        if (keyword.isEmpty()) {
            error("Keyword cannot be empty.");
            return;
        }

        List<Book> results = switch (searchChoice) {
            case "1" -> books.stream().filter(b -> b.title.toLowerCase().contains(keyword)).toList();
            case "2" -> books.stream().filter(b -> b.author.toLowerCase().contains(keyword)).toList();
            case "3" -> books.stream().filter(b -> b.genre.toLowerCase().contains(keyword)).toList();
            default -> {
                error("Invalid search option.");
                yield List.of();
            }
        };

        if (results.isEmpty()) {
            error("No books found for: \"" + keyword + "\"");
        } else {
            System.out.println();
            info(cyan(results.size() + " result(s)") + " for " + white("\"" + keyword + "\""));
            System.out.println();
            results.forEach(b -> printRow(b.formatted()));
        }
    }

    // ── View All ──────────────────────────────────────────────
    static void viewAllBooks() {
        if (books.isEmpty()) {
            error("Library is empty. Add some books first.");
            return;
        }
        sectionTitle("All Books");

        System.out.println();
        String header = String.format(dim("  %-3s   %-26s   %-16s   %-11s   %s"), "ID", "TITLE", "AUTHOR", "GENRE",
                "STATUS");
        printRow(header);
        printDivider();
        books.forEach(b -> printRow(b.formatted()));
        printDivider();
        printRow(dim("Total: ") + cyan(books.size() + " books"));
    }

    // ── File I/O ──────────────────────────────────────────────
    static void loadBooks() {
        File file = new File(FILE_PATH);
        if (!file.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null)
                if (!line.isBlank())
                    books.add(Book.fromFileLine(line));
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    static void saveBooks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Book b : books)
                bw.write(b.toFileLine() + "\n");
        } catch (IOException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }
}