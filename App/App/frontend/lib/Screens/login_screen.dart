import 'package:flutter/material.dart';
import 'package:frontend/Screens/Home.dart';
import '../services/websocket_service.dart';

/// =====================================================
/// Login Screen
/// =====================================================
/// This screen allows users to:
/// 1. Enter username
/// 2. Connect to WebSocket server
/// 3. Send LOGIN message
/// 4. Navigate to HomeScreen upon successful connection
/// =====================================================

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final WebSocketService _webSocketService = WebSocketService();
  bool _isConnecting = false;
  String? _errorMessage;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  /// Handle Connect button press
  /// 
  /// This method:
  /// 1. Connects to WebSocket server
  /// 2. Sends LOGIN message with username and password
  /// 3. Navigates to HomeScreen
  Future<void> _handleConnect() async {
    final username = _usernameController.text.trim();
    final password = _passwordController.text.trim();

    if (username.isEmpty || password.isEmpty) {
      setState(() {
        _errorMessage = 'Please enter username and password';
      });
      return;
    }

    setState(() {
      _isConnecting = true;
      _errorMessage = null;
    });

    try {
      // Connect to WebSocket server
      await _webSocketService.connect();

      // Wait a moment for connection to establish
      await Future.delayed(const Duration(milliseconds: 500));

      // Send LOGIN message
      // Protocol: {"type": "LOGIN", "username": "...", "password": "..."}
      _webSocketService.sendMessage({
        'type': 'LOGIN',
        'username': username,
        'password': password,
      });

      // Wait a bit for response, then navigate
      await Future.delayed(const Duration(milliseconds: 500));

      // Navigate to HomeScreen
      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => Home(username: username),
          ),
        );
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Connection error: $e';
        _isConnecting = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Chat App - Login'),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // App Icon
            const Icon(
              Icons.chat_bubble_outline,
              size: 100,
              color: Colors.blue,
            ),
            const SizedBox(height: 40),

            // Username field
            TextField(
              controller: _usernameController,
              decoration: const InputDecoration(
                labelText: 'Username',
                hintText: 'Enter your username',
                prefixIcon: Icon(Icons.person),
                border: OutlineInputBorder(),
              ),
              enabled: !_isConnecting,
              textInputAction: TextInputAction.next,
            ),
            const SizedBox(height: 16),

            // Password field
            TextField(
              controller: _passwordController,
              decoration: const InputDecoration(
                labelText: 'Password',
                hintText: 'Enter your password',
                prefixIcon: Icon(Icons.lock),
                border: OutlineInputBorder(),
              ),
              obscureText: true,
              enabled: !_isConnecting,
              textInputAction: TextInputAction.done,
              onSubmitted: (_) => _handleConnect(),
            ),
            const SizedBox(height: 24),

            // Error message
            if (_errorMessage != null)
              Container(
                padding: const EdgeInsets.all(12),
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                  color: Colors.red.shade100,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.error, color: Colors.red),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _errorMessage!,
                        style: const TextStyle(color: Colors.red),
                      ),
                    ),
                  ],
                ),
              ),

            // Connect button
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                onPressed: _isConnecting ? null : _handleConnect,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue,
                  foregroundColor: Colors.white,
                ),
                child: _isConnecting
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                        ),
                      )
                    : const Text(
                        'Connect',
                        style: TextStyle(fontSize: 16),
                      ),
              ),
            ),

            const SizedBox(height: 16),

            // Connection status
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(
                  _webSocketService.isConnected
                      ? Icons.check_circle
                      : Icons.cancel,
                  color: _webSocketService.isConnected
                      ? Colors.green
                      : Colors.grey,
                  size: 16,
                ),
                const SizedBox(width: 8),
                Text(
                  _webSocketService.isConnected
                      ? 'Connected'
                      : 'Not Connected',
                  style: TextStyle(
                    color: _webSocketService.isConnected
                        ? Colors.green
                        : Colors.grey,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

