import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL =
            "jdbc:mysql://localhost:3306/library_management";
    static final String USER = "root";
    static final String PASSWORD = "YOUR_PASSWORD"; 

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con = DriverManager.getConnection(
                URL, USER, PASSWORD)) {

            System.out.println("Library Database Connected Successfully!");

            int choice;

            do {
                System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
                System.out.println("1. Add Book");
                System.out.println("2. View All Books");
                System.out.println("3. Search Book");
                System.out.println("4. Issue Book");
                System.out.println("5. Return Book");
                System.out.println("6. View Issued Books");
                System.out.println("7. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        addBook(con);
                        break;
                    case 2:
                        viewBooks(con);
                        break;
                    case 3:
                        searchBook(con);
                        break;
                    case 4:
                        issueBook(con);
                        break;
                    case 5:
                        returnBook(con);
                        break;
                    case 6:
                        viewIssuedBooks(con);
                        break;
                    case 7:
                        System.out.println("Thank you for using the Library Management System!");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }

            } while (choice != 7);

        } catch (SQLException e) {
            System.out.println("Database Error!");
            e.printStackTrace();
        }
    }

    static void addBook(Connection con) throws SQLException {
        System.out.print("Enter book title: ");
        String title = sc.nextLine();

        System.out.print("Enter author name: ");
        String author = sc.nextLine();

        System.out.print("Enter quantity: ");
        int quantity = sc.nextInt();
        sc.nextLine();

        String sql = "INSERT INTO books (title, author, quantity, available) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);

            ps.executeUpdate();
            System.out.println("Book added successfully!");
        }
    }

    static void viewBooks(Connection con) throws SQLException {
        String sql = "SELECT * FROM books";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- Available Books ---");

            while (rs.next()) {
                System.out.println(
                        "ID: " + rs.getInt("book_id")
                        + " | Title: " + rs.getString("title")
                        + " | Author: " + rs.getString("author")
                        + " | Quantity: " + rs.getInt("quantity")
                        + " | Available: " + rs.getInt("available"));
            }
        }
    }

    static void searchBook(Connection con) throws SQLException {
        System.out.print("Enter book title to search: ");
        String title = sc.nextLine();

        String sql = "SELECT * FROM books WHERE title LIKE ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + title + "%");

            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;

                while (rs.next()) {
                    found = true;
                    System.out.println(
                            "ID: " + rs.getInt("book_id")
                            + " | Title: " + rs.getString("title")
                            + " | Author: " + rs.getString("author")
                            + " | Available: " + rs.getInt("available"));
                }

                if (!found) {
                    System.out.println("Book not found.");
                }
            }
        }
    }

    static void issueBook(Connection con) throws SQLException {
        System.out.print("Enter book ID: ");
        int bookId = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter borrower name: ");
        String borrower = sc.nextLine();

        String checkSql = "SELECT available FROM books WHERE book_id = ?";

        try (PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setInt(1, bookId);

            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next() || rs.getInt("available") <= 0) {
                    System.out.println("Book not found or unavailable.");
                    return;
                }
            }
        }

        String issueSql = "INSERT INTO issued_books (book_id, borrower_name, issue_date, status) VALUES (?, ?, CURDATE(), 'Issued')";

        try (PreparedStatement ps = con.prepareStatement(issueSql)) {
            ps.setInt(1, bookId);
            ps.setString(2, borrower);
            ps.executeUpdate();
        }

        String updateSql = "UPDATE books SET available = available - 1 WHERE book_id = ?";

        try (PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }

        System.out.println("Book issued successfully!");
    }

    static void returnBook(Connection con) throws SQLException {
        System.out.print("Enter issue ID: ");
        int issueId = sc.nextInt();
        sc.nextLine();

        String findSql = "SELECT book_id FROM issued_books WHERE issue_id = ? AND status = 'Issued'";

        try (PreparedStatement ps = con.prepareStatement(findSql)) {
            ps.setInt(1, issueId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Active issue record not found.");
                    return;
                }

                int bookId = rs.getInt("book_id");

                String returnSql = "UPDATE issued_books SET status = 'Returned', return_date = CURDATE() WHERE issue_id = ?";

                try (PreparedStatement update = con.prepareStatement(returnSql)) {
                    update.setInt(1, issueId);
                    update.executeUpdate();
                }

                String bookSql = "UPDATE books SET available = available + 1 WHERE book_id = ?";

                try (PreparedStatement update = con.prepareStatement(bookSql)) {
                    update.setInt(1, bookId);
                    update.executeUpdate();
                }

                System.out.println("Book returned successfully!");
            }
        }
    }

    static void viewIssuedBooks(Connection con) throws SQLException {
        String sql = "SELECT * FROM issued_books";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- Issued Books ---");

            while (rs.next()) {
                System.out.println(
                        "Issue ID: " + rs.getInt("issue_id")
                        + " | Book ID: " + rs.getInt("book_id")
                        + " | Borrower: " + rs.getString("borrower_name")
                        + " | Issue Date: " + rs.getDate("issue_date")
                        + " | Return Date: " + rs.getDate("return_date")
                        + " | Status: " + rs.getString("status"));
            }
        }
    }
}