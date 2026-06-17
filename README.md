# 📚 Library Management System (Java)

A comprehensive Java desktop application for managing library resources, user roles, and book circulation. Built with Java Swing and IntelliJ IDEA's UI Designer, this system provides a robust interface for administrators, registered users, and guests.

## ✨ Features

*   **Role-Based Access Control:** Distinct dashboards and permissions for Admins, Users, and Guests[cite: 3].
*   **Authentication System:** Secure login handling mapped by role[cite: 3].
*   **Book Circulation:** Complete workflows to issue (borrow) and return books[cite: 3].
*   **Inventory & User Management:** Full CRUD (Create, Read, Update, Delete) capabilities for managing the book catalog and user database[cite: 3].
*   **Audit Logging:** Track system activities and view issued book records[cite: 3].

## 🛠 Tech Stack

*   **Language:** Java[cite: 3]
*   **GUI Framework:** Java Swing (built via IntelliJ IDEA `.form` designer)[cite: 3]
*   **Database:** MySQL (connected via `mysql-connector-j-9.0.0.jar`)[cite: 3]

## 📂 Project Structure

*   `.idea/`: IntelliJ IDEA project configuration and GUI Designer files[cite: 3].
*   `lib/`: Contains external dependencies, specifically the MySQL JDBC driver[cite: 3].
*   `src/`: Application source code[cite: 3].
    *   `Main.java`: The application's entry point[cite: 3].
    *   `cls/`: Core logic and data models (`user`, `authentication`, `KeyValue`)[cite: 3].
    *   `db/`: Database configuration and connection handling (`DBConnection.java`)[cite: 3].
    *   `forms/`: All graphical user interface components (paired `.java` and `.form` files)[cite: 3].

## 🚀 Getting Started

### Prerequisites
*   Java Development Kit (JDK) 8 or higher.
*   IntelliJ IDEA (Recommended, as the project relies on `.form` files for the UI layout)[cite: 3].
*   MySQL Server.

### Installation & Setup
1.  **Clone the repository:**
```bash
    git clone [https://github.com/your-username/Library_Management_System_Java.git](https://github.com/your-username/Library_Management_System_Java.git)
    ```
2.  **Open in IntelliJ IDEA:**
    *   Open the cloned folder in IntelliJ IDEA to ensure the `.form` GUI files render correctly[cite: 3].
3.  **Database Configuration:**
    *   Set up your MySQL database schemas.
    *   Navigate to `src/db/DBConnection.java` and update the connection string, username, and password to match your local MySQL setup[cite: 3].
4.  **Dependencies:**
    *   Ensure the `mysql-connector-j-9.0.0.jar` located in the `lib/` directory is added to your project's classpath[cite: 3].
5.  **Run the Application:**
    *   Execute the `Main.java` file to launch the application[cite: 3].
