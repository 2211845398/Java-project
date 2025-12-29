# Flutter Chat App - Complete Implementation

## 📁 Project Structure

```
lib/
├── main.dart                    # App entry point (starts with LoginScreen)
├── services/
│   └── websocket_service.dart  # Singleton WebSocket service
└── screens/
    ├── login_screen.dart      # Login screen with username input
    ├── home_screen.dart        # Home screen with users list
    └── chat_screen.dart        # Chat interface
```

## 🔧 Setup Instructions

### 1. Install Dependencies

```bash
cd App/frontend
flutter pub get
```

### 2. Configure WebSocket URL

The WebSocket URL is configured in `lib/services/websocket_service.dart`:

- **Android Emulator:** `ws://10.0.2.2:8080` (default)
- **iOS Simulator:** Change to `ws://localhost:8080`
- **Physical Device:** Change to `ws://YOUR_COMPUTER_IP:8080`

### 3. Run the App

```bash
flutter run
```

## 📱 Screen Flow

### 1. LoginScreen
- **Input:** Username field
- **Action:** 
  - Connects to WebSocket server
  - Sends: `{"type": "LOGIN", "username": "..."}`
  - Navigates to HomeScreen

### 2. HomeScreen
- **On Init:** Sends `{"type": "GET_USERS"}`
- **UI:** ListView displaying users from server
- **Action:** Tap user → Navigate to ChatScreen

### 3. ChatScreen
- **Sending:** `{"type": "MESSAGE", "sender": "...", "recipient": "...", "content": "..."}`
- **Receiving:** Filters messages from stream for this conversation
- **UI:** Message list + Text input field

## 🔌 WebSocket Protocol

### Message Types

1. **LOGIN**
   ```json
   {"type": "LOGIN", "username": "john"}
   ```

2. **GET_USERS**
   ```json
   {"type": "GET_USERS"}
   ```

3. **MESSAGE**
   ```json
   {
     "type": "MESSAGE",
     "sender": "john",
     "recipient": "jane",
     "content": "Hello!"
   }
   ```

## 🎯 Key Features

### WebSocketService (Singleton)
- ✅ Single connection instance
- ✅ Broadcast stream for global listening
- ✅ JSON encoding/decoding
- ✅ Connection state management

### StreamBuilder Usage
- All screens use `StreamBuilder` to listen to WebSocket messages
- Real-time updates when messages arrive
- Automatic UI rebuild on new data

### Message Filtering
- ChatScreen filters messages to show only relevant conversation
- Prevents showing messages from other conversations

## 🐛 Troubleshooting

### Connection Issues
- Make sure Java server is running (`mvn exec:java` in backend folder)
- Check WebSocket URL matches your setup
- For physical device, use computer's IP address

### No Users Showing
- Make sure backend supports `GET_USERS` message type
- Check server logs for incoming messages
- Verify users exist in database

### Messages Not Appearing
- Check WebSocket connection is active
- Verify message format matches server expectations
- Check server logs for received messages

## 📝 Notes

- The app uses `setState` for simple state management
- All JSON encoding/decoding is handled safely
- Error handling is included for connection issues
- Comments explain WebSocket flow throughout the code

