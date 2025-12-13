# Library Management System

A comprehensive desktop application for managing library operations, built with JavaFX and MySQL. This system allows librarians to manage books, magazines, user accounts, and reservations efficiently.

## 📋 Features

### Team Members
- **Mohamed Souhaib Aksikas**
- **Amine Benali**

### User Management
- **User Authentication**: Secure login system with email and password
- **User Registration**: Create new user accounts with personal information
- **User Profiles**: Manage user information including name, email, and phone number

### Library Catalog Management
- **Book Management**: Add, edit, and view books with details including:
  - Title, Author, ISBN
  - Genre, Publisher
  - Publication Date, Number of Pages
- **Magazine Management**: Add, edit, and view magazines with details including:
  - Title, Author, ISBN
  - Category, Issue Number
  - Publication Date, Publisher
- **Search Functionality**: Search library items by title, author, ISBN, genre, or category
- **Visual Catalog**: Card-based display of library items with availability status

### Reservation System
- **Reserve Items**: Create reservations for books and magazines
- **Track Reservations**: View active, returned, and overdue reservations
- **Return Management**: Mark items as returned and update availability
- **Automatic Status Updates**: System automatically tracks reservation status and due dates
- **Reservation History**: View complete history of all reservations

### Dashboard & Navigation
- **Modern UI**: Clean and intuitive user interface built with JavaFX
- **Dashboard View**: Centralized view of library operations
- **Multiple Views**: Separate views for books, reservations, and history
- **Responsive Design**: Adapts to different window sizes

## 🛠️ Technologies Used

- **Java**: Core programming language
- **JavaFX 17**: UI framework for desktop application
- **MySQL**: Relational database management system
- **MySQL Connector/J**: JDBC driver for MySQL connectivity
- **FXML**: XML-based markup for JavaFX UI design
- **CSS**: Styling for JavaFX components

## 📦 Prerequisites

Before running this application, ensure you have:

1. **Java Development Kit (JDK) 11 or higher**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`

2. **JavaFX SDK 17**
   - Download from: https://openjfx.io/
   - Extract to a known location (e.g., `/Library/Java/JavaFX/javafx-sdk-17.0.17/lib` on macOS)

3. **MySQL Server 8.0 or higher**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Ensure MySQL service is running

4. **MySQL JDBC Driver**
   - Download from: https://dev.mysql.com/downloads/connector/j/
   - Extract the JAR file (e.g., `mysql-connector-j-8.0.33.jar`)

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Library_Project
```

### 2. Database Setup

1. **Start MySQL Service**
   - Windows: Open Services and start MySQL80, or run `net start MySQL80` in Command Prompt (as Administrator)
   - macOS/Linux: `sudo systemctl start mysql` or `brew services start mysql`

2. **Create Database and Tables**
   - Open MySQL command line: `mysql -u root -p`
   - Run the setup script:
     ```sql
     source database_setup.sql
     ```
   - Or use MySQL Workbench to execute `database_setup.sql`

3. **Verify Database Creation**
   ```sql
   USE library_db;
   SHOW TABLES;
   ```
   You should see: `users`, `library_items`, `reservations`

### 3. Configure Database Connection

Edit `src/main/java/com/library/models/DBConnection.java` if needed:
- Update `DB_URL` if using a different port
- Update `DB_USER` if not using 'root'
- Update `DB_PASSWORD` if you set a password

### 4. Add MySQL JDBC Driver

**Option A: Using IDE (IntelliJ IDEA/Eclipse)**
- File → Project Structure → Libraries → + → Java
- Select the MySQL JDBC driver JAR file

**Option B: Manual Classpath**
- Place `mysql-connector-j-8.0.33.jar` in a `lib_db` directory
- Update build scripts to include it in classpath

### 5. Configure JavaFX Path

**Windows (PowerShell):**
```powershell
$env:JAVAFX_LIB = "C:\path\to\javafx-sdk-17.0.17\lib"
```

**macOS/Linux (Bash):**
```bash
export JAVAFX_LIB="/Library/Java/JavaFX/javafx-sdk-17.0.17/lib"
```

Or place JavaFX JARs in the `lib` or `lib_win` directory in the project root.

## ▶️ Running the Application

### Windows (PowerShell)

```powershell
.\run.ps1
```

### macOS/Linux (Bash)

```bash
chmod +x run.sh
./run.sh
```

### Manual Compilation & Execution

**Compile:**
```bash
javac --module-path "$JAVAFX_LIB" \
      --add-modules javafx.controls,javafx.fxml,javafx.graphics \
      -d bin \
      -sourcepath src/main/java \
      src/main/java/com/library/**/*.java
```

**Run:**
```bash
java --module-path "$JAVAFX_LIB" \
     --add-modules javafx.controls,javafx.fxml,javafx.graphics \
     -cp "bin:src/main/resources:path/to/mysql-connector-j-8.0.33.jar" \
     com.library.App
```

## 📁 Project Structure

```
Library_Project/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/library/
│       │       ├── App.java                 # Main application entry point
│       │       ├── controllers/             # FXML controllers
│       │       │   ├── BooksController.java
│       │       │   ├── LoginController.java
│       │       │   ├── RegisterController.java
│       │       │   ├── ReservationController.java
│       │       │   ├── DashboardController.java
│       │       │   └── HistoryController.java
│       │       ├── models/                  # Data models and DAOs
│       │       │   ├── Book.java
│       │       │   ├── Magazine.java
│       │       │   ├── LibraryItem.java
│       │       │   ├── User.java
│       │       │   ├── Reservation.java
│       │       │   ├── DBConnection.java
│       │       │   ├── LibraryItemDAO.java
│       │       │   ├── UserDAO.java
│       │       │   └── ReservationDAO.java
│       │       ├── services/               # Business logic services
│       │       │   ├── BookService.java
│       │       │   ├── UserService.java
│       │       │   └── ReservationService.java
│       │       └── utils/                  # Utility classes
│       │           ├── AlertUtils.java
│       │           ├── DateUtils.java
│       │           └── DBUtils.java
│       └── resources/
│           └── com/library/
│               ├── views/                   # FXML UI files
│               │   ├── login.fxml
│               │   ├── register.fxml
│               │   ├── dashboard.fxml
│               │   ├── books.fxml
│               │   ├── addItemForm.fxml
│               │   ├── reservation.fxml
│               │   └── history.fxml
│               └── css/
│                   └── style.css           # Application styles
├── database_setup.sql                      # Database schema and setup
├── DATABASE_SETUP.md                      # Detailed database setup guide
├── run.sh                                  # Linux/macOS run script
├── run.ps1                                 # Windows PowerShell run script
└── README.md                               # This file
```

## 🗄️ Database Schema

The application uses three main tables:

### `users`
- Stores user account information
- Fields: id, first_name, last_name, email, phone_number, registration_date, is_active

### `library_items`
- Stores books and magazines
- Fields: id, title, author, isbn, publication_date, is_available, item_type
- Book-specific: number_of_pages, genre, publisher
- Magazine-specific: issue_number, category

### `reservations`
- Tracks item reservations
- Fields: id, user_id, item_id, reservation_date, due_date, return_date, status
- Status values: ACTIVE, RETURNED, OVERDUE, CANCELLED

## 💡 Usage Guide

### Getting Started

1. **Login**: Use the login screen to access the system
   - Default users can be created via registration
   - Or insert test users directly into the database

2. **Add Library Items**:
   - Click "+ Add Item" button
   - Select item type (Book or Magazine)
   - Fill in the required fields
   - Click "Save Item"

3. **Search Items**:
   - Use the search bar to filter by title, author, ISBN, genre, or category
   - Click "Clear" to reset the search

4. **Reserve Items**:
   - Double-click on an available item card
   - Or use the Reservations view to create new reservations
   - Enter borrower information and select dates

5. **Return Items**:
   - Click "Unborrow" on a reserved item
   - Or use the Reservations view to mark items as returned

6. **View History**:
   - Navigate to the History view to see all past reservations

## 🔧 Configuration

### Database Connection Settings

Edit `src/main/java/com/library/models/DBConnection.java`:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root";
```

### JavaFX Module Path

Set the `JAVAFX_LIB` environment variable or update the run scripts with your JavaFX SDK path.

## 🐛 Troubleshooting

### Common Issues

**"MySQL JDBC Driver not found"**
- Ensure MySQL Connector/J is in the classpath
- Check the JAR file path in run scripts

**"Database connection failed"**
- Verify MySQL service is running
- Check database credentials in `DBConnection.java`
- Ensure `library_db` database exists

**"JavaFX modules not found"**
- Set `JAVAFX_LIB` environment variable
- Or place JavaFX JARs in `lib` directory
- Verify JavaFX SDK version matches (17.x)

**"ClassNotFoundException"**
- Ensure all dependencies are in classpath
- Rebuild the project
- Check module path configuration

## 📝 Development Notes

- The application follows MVC (Model-View-Controller) architecture
- DAO pattern is used for database operations
- Service layer handles business logic
- JavaFX ObservableList is used for reactive UI updates
- Singleton pattern is used for services


For detailed database setup instructions, see [DATABASE_SETUP.md](DATABASE_SETUP.md)
