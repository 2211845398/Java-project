import 'dart:async';
import 'package:flutter/material.dart';
import '../services/websocket_service.dart';
import '../screens/chat_screen.dart';

class Home extends StatefulWidget {
  final String username;
  
  const Home({super.key, required this.username});

  @override
  State<Home> createState() => _HomeState();
}

class _HomeState extends State<Home> {
  final WebSocketService _webSocketService = WebSocketService();
  List<String> _users = [];
  bool _isLoading = true;
  StreamSubscription<String>? _messageSubscription;

  @override
  void initState() {
    super.initState();
    // Send SEARCH_USER request when screen initializes
    _requestUsers();
    
    // Listen to WebSocket stream for incoming messages
    _messageSubscription = _webSocketService.messageStream.listen(
      (messageString) {
        final message = _webSocketService.parseMessage(messageString);
        
        if (message != null) {
          // Check if this is a SEARCH_USER response
          if (message['type'] == 'SEARCH_USER' && 
              message['status'] == 'SUCCESS' &&
              message['data'] != null && 
              message['data'] is List) {
            // Update state outside of build phase
            if (mounted) {
              setState(() {
                _users = List<String>.from(message['data']);
                _isLoading = false;
              });
            }
          } else if (message['type'] == 'SEARCH_USER' && 
                     message['status'] == 'ERROR') {
            // Handle error response
            if (mounted) {
              setState(() {
                _isLoading = false;
              });
            }
            print('[HomeScreen] Error: ${message['errorMessage']}');
          }
        }
      },
      onError: (error) {
        print('[HomeScreen] Stream error: $error');
        if (mounted) {
          setState(() {
            _isLoading = false;
          });
        }
      },
    );
  }

  @override
  void dispose() {
    _messageSubscription?.cancel();
    super.dispose();
  }

  /// Send SEARCH_USER request to server to get all users
  /// 
  /// Protocol: {"type": "SEARCH_USER", "username": "%"}
  /// Using "%" as search term to match all users
  void _requestUsers() {
    try {
      _webSocketService.sendMessage({
        'type': 'SEARCH_USER',
        'username': '%',  // "%" matches all users in SQL LIKE query
      });
      print('[HomeScreen] Sent SEARCH_USER request');
    } catch (e) {
      print('[HomeScreen] Error sending SEARCH_USER: $e');
      setState(() {
        _isLoading = false;
      });
    }
  }

  /// Handle user tap - navigate to ChatScreen
  void _handleUserTap(String targetUsername) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => ChatScreen(
          currentUsername: widget.username,
          targetUsername: targetUsername,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: Text('Welcome, ${widget.username}'),
        actions: [
          // Refresh button
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              setState(() {
                _isLoading = true;
                _users = [];
              });
              _requestUsers();
            },
          ),
        ],
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    // Show loading indicator
    if (_isLoading && _users.isEmpty) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(),
            SizedBox(height: 16),
            Text('Loading users...'),
          ],
        ),
      );
    }

    // Show empty state
    if (_users.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.people_outline, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            const Text(
              'No users found',
              style: TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 8),
            ElevatedButton(
              onPressed: _requestUsers,
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    // Display users list
    return ListView.builder(
      itemCount: _users.length,
      itemBuilder: (context, index) {
        final username = _users[index];
        
        // Skip current user from the list
        if (username == widget.username) {
          return const SizedBox.shrink();
        }

        return ListTile(
          leading: CircleAvatar(
            child: Text(username[0].toUpperCase()),
          ),
          title: Text(username),
          subtitle: const Text('Tap to start chatting'),
          trailing: const Icon(Icons.chevron_right),
          onTap: () => _handleUserTap(username),
        );
      },
    );
  }
}