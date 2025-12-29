# Testing the Server

## Quick Test with Telnet (Windows)

You can test the server using Windows Telnet or a simple TCP client.

### Step 1: Start the Server
```bash
cd backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
mvn clean compile
mvn exec:java
```

### Step 2: Connect with Telnet
Open a new terminal and run:
```bash
telnet localhost 8080
```

### Step 3: Send JSON Messages

**Login:**
```
{"type":"LOGIN","username":"john","password":"password123"}
```

**Search User:**
```
{"type":"SEARCH_USER","username":"j"}
```

**Create Conversation:**
```
{"type":"CREATE_CONVERSATION","targetUsername":"jane"}
```

**Send Message:**
```
{"type":"SEND_MESSAGE","conversationId":1,"content":"Hello!"}
```

**Note:** Each message must end with a newline (press Enter after typing the JSON).

## Sample Test Data

First, insert some test users into the database:

```sql
USE chat_app;

INSERT INTO users (username, password) VALUES 
('john', 'password123'),
('jane', 'password456'),
('bob', 'password789');
```

Then test the login with:
```json
{"type":"LOGIN","username":"john","password":"password123"}
```

Expected response:
```json
{"type":"LOGIN","status":"SUCCESS","userId":1,"username":"john"}
```

