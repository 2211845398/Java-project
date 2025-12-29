# Chat Application - Mini Telegram Clone

A simplified but professional client-server chat application built for educational purposes.

## Project Structure

```
App/
├── database/
│   └── schema.sql              # MySQL database schema
├── backend/                     # Java Server
│   ├── src/main/java/com/chatapp/
│   │   ├── models/
│   │   │   └── Message.java    # Message model for JSON communication
│   │   ├── database/
│   │   │   └── DatabaseManager.java  # MySQL connection and queries
│   │   └── server/
│   │       ├── Server.java     # Main server entry point
│   │       └── ClientHandler.java  # Thread handler for each client
│   └── pom.xml                 # Maven dependencies
└── README.md
```

## Tech Stack

- **Backend:** Java (WebSocket Server, Multi-threading)
- **Frontend:** Flutter (Mobile App) - *To be implemented*
- **Database:** MySQL
- **Communication:** JSON strings over WebSocket protocol

## Prerequisites

1. **Java JDK 11 or higher**
2. **Maven** (for dependency management)
3. **MySQL Server** (version 5.7 or higher)
4. **MySQL JDBC Driver** (included in pom.xml)

## Setup Instructions

### Step 1: Database Setup

1. Start your MySQL server
2. Open MySQL command line or MySQL Workbench
3. Run the SQL script:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
   Or copy and paste the contents of `database/schema.sql` into your MySQL client

4. Update database credentials in `DatabaseManager.java`:
   ```java
   private static final String DB_USER = "root";      // Your MySQL username
   private static final String DB_PASSWORD = "";      // Your MySQL password
   ```

### Step 2: Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Install dependencies and compile:
   ```bash
   mvn clean compile
   ```

3. Run the server:
   ```bash
   mvn exec:java
   ```
   
   Or compile and run manually:
   ```bash
   mvn package
   java -cp target/chat-server-1.0.0.jar:target/dependency/* com.chatapp.server.Server
   ```

4. The server will start on port **8080** by default

### Step 3: Test the Server

You can test the server using a simple TCP client or wait for the Flutter app to be implemented.

## Database Schema

### Tables

1. **users**: Stores user accounts
   - `id` (Primary Key)
   - `username` (Unique)
   - `password`
   - `created_at`

2. **conversations**: Stores conversation channels
   - `id` (Primary Key)
   - `type` (ENUM: 'single' or 'group')
   - `created_at`

3. **participants**: Junction table linking users to conversations
   - `conversation_id` (Foreign Key)
   - `user_id` (Foreign Key)
   - Composite Primary Key

### Important Logic

The database design enforces: **"Two users can share only ONE individual conversation channel"**

This is implemented in `DatabaseManager.getOrCreateConversation()`:
- First checks if a 'single' conversation exists between two users
- If exists, returns the existing conversation ID
- If not, creates a new conversation and adds both users as participants

## Communication Protocol

### Message Format

All messages are JSON strings sent over **WebSocket** protocol.
- WebSocket handles message framing automatically
- No need for newline characters (`\n`)
- Connection URL: `ws://localhost:8080`

### Message Types

1. **LOGIN**: Authenticate user
   ```json
   {
     "type": "LOGIN",
     "username": "john",
     "password": "password123"
   }
   ```

2. **SEARCH_USER**: Search for users
   ```json
   {
     "type": "SEARCH_USER",
     "username": "joh"
   }
   ```

3. **CREATE_CONVERSATION**: Create or get conversation
   ```json
   {
     "type": "CREATE_CONVERSATION",
     "targetUsername": "jane"
   }
   ```

4. **SEND_MESSAGE**: Send a message
   ```json
   {
     "type": "SEND_MESSAGE",
     "conversationId": 1,
     "content": "Hello!"
   }
   ```

### Response Format

Success response:
```json
{
  "type": "LOGIN",
  "status": "SUCCESS",
  "userId": 1,
  "username": "john"
}
```

Error response:
```json
{
  "type": "LOGIN",
  "status": "ERROR",
  "errorMessage": "Invalid username or password"
}
```

## Architecture

### Server Architecture

- **WebSocket-based**: Uses Java-WebSocket library for WebSocket protocol
- **Multi-client support**: Each client gets its own WebSocket connection
- **JSON Protocol**: All communication uses JSON strings over WebSocket, parsed with Gson library

### Flow

1. Server starts WebSocket server on port 8080
2. Client connects via WebSocket → Server's `onOpen` is called
3. Client sends JSON messages → Server's `onMessage` is called
4. `ClientHandler` processes the message and queries database via `DatabaseManager`
5. `ClientHandler` sends JSON response back to client via WebSocket
6. Connection closes → Server's `onClose` is called

## Code Structure

### DatabaseManager.java
- Handles MySQL connection using JDBC
- Methods: `login()`, `searchUsers()`, `getOrCreateConversation()`
- Enforces the "one conversation per pair" rule

### ClientHandler.java
- Extends `WebSocketServer` class from Java-WebSocket library
- Handles WebSocket connections (onOpen, onMessage, onClose, onError)
- Processes JSON messages, sends JSON responses via WebSocket
- Maintains authenticated user state for each connection

### Server.java
- Main entry point
- Creates `WebSocketServer` (ClientHandler) to accept WebSocket connections
- Manages shared `DatabaseManager` instance
- Handles server lifecycle (start/stop)

## Security Notes

⚠️ **Important**: This is a simplified educational project. For production use:

1. **Passwords**: Currently stored in plain text. Use password hashing (BCrypt, Argon2)
2. **SQL Injection**: Using PreparedStatements (good!), but always validate input
3. **Authentication**: Implement session tokens/JWT instead of maintaining state in threads
4. **Encryption**: Use TLS/SSL for socket communication
5. **Input Validation**: Add more robust input validation

## Next Steps

- [ ] Implement Flutter frontend
- [ ] Add message persistence (messages table)
- [ ] Implement real-time message forwarding between clients
- [ ] Add group chat functionality
- [ ] Implement file/image sharing
- [ ] Add message history retrieval

## Troubleshooting

### Server won't start
- Check if port 8080 is already in use
- Verify MySQL server is running
- Check database credentials in `DatabaseManager.java`

### Database connection errors
- Ensure MySQL server is running
- Verify database name is `chat_app`
- Check username/password in `DatabaseManager.java`
- Make sure MySQL JDBC driver is in classpath

### Compilation errors
- Ensure Java JDK 11+ is installed
- Run `mvn clean compile` to refresh dependencies
- Check that all required dependencies are in `pom.xml`

## License

Educational project - Free to use and modify.

