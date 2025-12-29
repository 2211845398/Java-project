# How to Run the Chat Server

## Prerequisites Checklist
- [ ] Java JDK 11 or higher installed
- [ ] Maven installed
- [ ] MySQL Server installed and running

## Step-by-Step Instructions

### Step 1: Set Up the Database

1. **Start MySQL Server** (if not already running)

2. **Open MySQL Command Line** or MySQL Workbench

3. **Run the SQL script** to create the database and tables:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
   
   Or manually:
   - Open `database/schema.sql`
   - Copy all the SQL code
   - Paste and execute it in MySQL Workbench or command line

4. **Verify the database was created:**
   ```sql
   SHOW DATABASES;
   USE chat_app;
   SHOW TABLES;
   ```

### Step 2: Configure Database Credentials

1. **Open** `backend/src/main/java/com/chatapp/database/DatabaseManager.java`

2. **Update lines 26-27** with your MySQL credentials:
   ```java
   private static final String DB_USER = "root";        // Your MySQL username
   private static final String DB_PASSWORD = "your_password";  // Your MySQL password
   ```

3. **Save the file**

### Step 3: Add Test Users (Optional but Recommended)

Run this SQL to add some test users:
```sql
USE chat_app;

INSERT INTO users (username, password) VALUES 
('john', 'password123'),
('jane', 'password456'),
('bob', 'password789');
```

### Step 4: Navigate to Backend Directory

Open terminal/command prompt and navigate to the backend folder:
```bash
cd C:\Users\abdo\Desktop\App\backend
```

### Step 5: Compile the Project

```bash
mvn clean compile
```

This will:
- Download dependencies (Gson, MySQL JDBC driver)
- Compile all Java files

**First time?** Maven will download dependencies, which may take a minute.

### Step 6: Run the Server

**Option A: Using Maven (Recommended)**
```bash
mvn exec:java
```

**Option B: Using Java directly**
```bash
# First, package the project
mvn package

# Then run
java -cp target/chat-server-1.0.0.jar;target/dependency/* com.chatapp.server.Server
```

### Step 7: Verify Server is Running

You should see output like:
```
=====================================================
Chat Application Server Started
=====================================================
Listening on port: 8080
Waiting for client connections...
=====================================================
```

### Step 8: Test the Server

**Option A: Using Telnet (Windows)**

1. Open a **new terminal/command prompt**
2. Enable Telnet (if not enabled):
   ```powershell
   # Run PowerShell as Administrator
   Enable-WindowsOptionalFeature -Online -FeatureName TelnetClient
   ```
3. Connect to the server:
   ```bash
   telnet localhost 8080
   ```
4. Send a test login message (press Enter after each line):
   ```
   {"type":"LOGIN","username":"john","password":"password123"}
   ```
5. You should receive a JSON response

**Option B: Using a Simple Java Test Client**

Create a test client or wait for the Flutter app.

## Troubleshooting

### "Port 8080 already in use"
- Another application is using port 8080
- Change the port in `Server.java` line 15:
  ```java
  private static final int PORT = 8081;  // Use a different port
  ```

### "Connection refused" or "Cannot connect to database"
- Check MySQL server is running
- Verify database credentials in `DatabaseManager.java`
- Check database name is `chat_app`
- Test MySQL connection:
  ```bash
  mysql -u root -p
  ```

### "ClassNotFoundException: com.mysql.cj.jdbc.Driver"
- Dependencies not downloaded
- Run: `mvn clean install`

### "Access denied for user"
- Wrong MySQL username/password
- Update `DatabaseManager.java` with correct credentials

### Maven not found
- Install Maven: https://maven.apache.org/download.cgi
- Or use an IDE like IntelliJ IDEA or Eclipse (they have built-in Maven)

## Quick Start (All Commands)

```bash
# 1. Navigate to backend
cd C:\Users\abdo\Desktop\App\backend

# 2. Compile
mvn clean compile

# 3. Run
mvn exec:java
```

## Stopping the Server

Press `Ctrl + C` in the terminal where the server is running.



