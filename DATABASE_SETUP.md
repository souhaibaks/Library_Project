# Database Setup Guide

## Step 1: Install MySQL

### Windows:
1. Download MySQL Installer from: https://dev.mysql.com/downloads/installer/
2. Run the installer and select "Developer Default" or "Server only"
3. During installation:
   - Set root password (or leave blank if you prefer)
   - Note: The default configuration uses port 3306
4. Complete the installation

### Verify Installation:
Open Command Prompt and run:
```bash
mysql --version
```

## Step 2: Start MySQL Service

### Windows:
1. Open Services (Win + R, type `services.msc`)
2. Find "MySQL80" (or your MySQL service name)
3. Right-click and select "Start" if not running

Or use Command Prompt (as Administrator):
```bash
net start MySQL80
```

## Step 3: Access MySQL

Open Command Prompt and run:
```bash
mysql -u root -p
```
(Enter your password when prompted, or press Enter if no password)

## Step 4: Create Database and Tables

### Option 1: Using MySQL Command Line
1. Open MySQL command line:
   ```bash
   mysql -u root -p
   ```

2. Navigate to your project directory and run the setup script:
   ```sql
   source /path/to/your/project/database_setup.sql
   ```
   
   **Windows example:**
   ```sql
   source C:/path/to/Library_Project/database_setup.sql
   ```
   
   **macOS/Linux example:**
   ```sql
   source /home/username/projects/Library_Project/database_setup.sql
   ```
   
   **Note:** Replace the path with your actual project directory path
   
   Or copy and paste the contents of `database_setup.sql` into MySQL command line

### Option 2: Using MySQL Workbench (GUI)
1. Download MySQL Workbench from: https://dev.mysql.com/downloads/workbench/
2. Install and open MySQL Workbench
3. Connect to your MySQL server (localhost, port 3306, user: root)
4. Open `database_setup.sql` file
5. Execute the script (click the lightning bolt icon or press Ctrl+Shift+Enter)

## Step 5: Verify Database Creation

In MySQL command line or Workbench, run:
```sql
USE library_db;
SHOW TABLES;
```

You should see:
- users
- library_items
- reservations

## Step 6: Test Connection from Java

### Download MySQL JDBC Driver:
1. Download from: https://dev.mysql.com/downloads/connector/j/
2. Extract the JAR file (e.g., `mysql-connector-j-8.0.33.jar`)
3. Add it to your project:
   - **IntelliJ IDEA**: 
     - File → Project Structure → Libraries → + → Java → Select the JAR file
   - **Command Line**:
     - Add to classpath: `-cp "path/to/mysql-connector-j-8.0.33.jar"`

### Run the Test:
```bash
# Compile (replace with your actual path to the MySQL connector JAR)
javac -cp "/path/to/mysql-connector-j-8.0.33.jar" -d out src/main/java/com/library/TestConnection.java src/main/java/com/library/models/DBConnection.java

# Run
java -cp "/path/to/mysql-connector-j-8.0.33.jar:out" com.library.TestConnection
```

**Windows example:**
```bash
javac -cp "C:\path\to\mysql-connector-j-8.0.33.jar" -d out src\main\java\com\library\TestConnection.java src\main\java\com\library\models\DBConnection.java
java -cp "C:\path\to\mysql-connector-j-8.0.33.jar;out" com.library.TestConnection
```

**macOS/Linux example:**
```bash
javac -cp "/path/to/mysql-connector-j-8.0.33.jar" -d out src/main/java/com/library/TestConnection.java src/main/java/com/library/models/DBConnection.java
java -cp "/path/to/mysql-connector-j-8.0.33.jar:out" com.library.TestConnection
```

**Note:** The `TestConnection.java` file has been removed from the project. You can test the connection by running the application directly.

## Step 7: Configure Connection (if needed)

If your MySQL setup is different, update `DBConnection.java`:
- **Different port**: Change `3306` to your port
- **Different database name**: Change `library_db` to your database name
- **Different username**: Change `root` to your username
- **Password**: Update `DB_PASSWORD` if you set a password

## Troubleshooting

### "Access denied for user 'root'@'localhost'"
- Check username and password in `DBConnection.java`
- Try resetting MySQL root password

### "Unknown database 'library_db'"
- Run the `database_setup.sql` script to create the database

### "Communications link failure"
- Check if MySQL service is running
- Verify port 3306 is correct
- Check firewall settings

### "ClassNotFoundException: com.mysql.cj.jdbc.Driver"
- MySQL JDBC driver not in classpath
- Download and add the JAR file to your project

### "Connection refused"
- MySQL service is not running
- Start MySQL service (see Step 2)

## Quick Test Commands

```sql
-- Check if database exists
SHOW DATABASES;

-- Use the database
USE library_db;

-- Check tables
SHOW TABLES;

-- View users
SELECT * FROM users;

-- View library items
SELECT * FROM library_items;
```

