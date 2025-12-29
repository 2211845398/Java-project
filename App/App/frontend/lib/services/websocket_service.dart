import 'dart:async';
import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';

/// =====================================================
/// WebSocket Service - Singleton Pattern
/// =====================================================
/// This service manages the WebSocket connection to the
/// Java backend server. It uses Singleton pattern to ensure
/// only one connection exists throughout the app lifecycle.
/// 
/// Features:
/// - Single WebSocket connection instance
/// - Broadcast stream for global message listening
/// - JSON encoding/decoding
/// - Connection state management
/// =====================================================

class WebSocketService {
  // Singleton instance
  static final WebSocketService _instance = WebSocketService._internal();
  
  // Factory constructor returns the singleton instance
  factory WebSocketService() {
    return _instance;
  }
  
  // Private constructor for singleton
  WebSocketService._internal();
  
  // WebSocket connection URL
  // For Android Emulator: 10.0.2.2 maps to host machine's localhost
  // For iOS Simulator: use localhost
  // For physical device: use your computer's IP address
  static const String _baseUrl = 'ws://localhost:8080';
  
  // WebSocket channel
  WebSocketChannel? _channel;
  
  // Connection state
  bool _isConnected = false;
  
  // Broadcast stream controller for incoming messages
  // This allows multiple listeners to receive messages
  final _messageController = StreamController<String>.broadcast();
  
  // Getters
  bool get isConnected => _isConnected;
  Stream<String> get messageStream => _messageController.stream;
  
  /// Connect to the WebSocket server
  /// 
  /// This method establishes a WebSocket connection and sets up
  /// a listener to forward all incoming messages to the broadcast stream.
  Future<void> connect() async {
    try {
      // If already connected, return
      if (_isConnected && _channel != null) {
        print('[WebSocketService] Already connected');
        return;
      }
      
      print('[WebSocketService] Connecting to $_baseUrl...');
      
      // Create WebSocket connection
      _channel = WebSocketChannel.connect(Uri.parse(_baseUrl));
      _isConnected = true;
      
      // Listen to incoming messages and forward to broadcast stream
      _channel!.stream.listen(
        (message) {
          // Handle both String and binary messages
          String messageString;
          if (message is String) {
            messageString = message;
          } else {
            // Decode binary message to string
            messageString = utf8.decode(message);
          }
          
          print('[WebSocketService] Received: $messageString');
          
          // Add to broadcast stream so all listeners can receive it
          _messageController.add(messageString);
        },
        onError: (error) {
          print('[WebSocketService] Stream error: $error');
          _isConnected = false;
          _messageController.addError(error);
        },
        onDone: () {
          print('[WebSocketService] Connection closed');
          _isConnected = false;
          _messageController.close();
        },
        cancelOnError: false,
      );
      
      print('[WebSocketService] Connected successfully');
    } catch (e) {
      print('[WebSocketService] Connection error: $e');
      _isConnected = false;
      _messageController.addError(e);
      rethrow;
    }
  }
  
  /// Disconnect from the WebSocket server
  /// 
  /// Closes the WebSocket connection and cleans up resources.
  void disconnect() {
    try {
      if (_channel != null) {
        _channel!.sink.close();
        _channel = null;
      }
      _isConnected = false;
      _messageController.close();
      print('[WebSocketService] Disconnected');
    } catch (e) {
      print('[WebSocketService] Disconnect error: $e');
    }
  }
  
  /// Send a message to the server
  /// 
  /// [data] A Map containing the message data that will be encoded to JSON
  /// 
  /// Example:
  /// ```dart
  /// websocketService.sendMessage({
  ///   'type': 'LOGIN',
  ///   'username': 'john',
  /// });
  /// ```
  void sendMessage(Map<String, dynamic> data) {
    if (!_isConnected || _channel == null) {
      throw Exception('Not connected to server. Call connect() first.');
    }
    
    try {
      // Encode Map to JSON string
      String jsonString = jsonEncode(data);
      print('[WebSocketService] Sending: $jsonString');
      
      // Send via WebSocket
      _channel!.sink.add(jsonString);
    } catch (e) {
      print('[WebSocketService] Error sending message: $e');
      rethrow;
    }
  }
  
  /// Parse incoming JSON message
  /// 
  /// [jsonString] The JSON string to parse
  /// Returns a Map with the decoded data, or null if parsing fails
  Map<String, dynamic>? parseMessage(String jsonString) {
    try {
      return jsonDecode(jsonString) as Map<String, dynamic>;
    } catch (e) {
      print('[WebSocketService] Error parsing JSON: $e');
      return null;
    }
  }
}

