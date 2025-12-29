import 'package:flutter/material.dart';
import 'dart:async';
import '../services/websocket_service.dart';

/// =====================================================
/// Chat Screen
/// =====================================================
/// This screen displays a chat interface with:
/// 1. Message list showing conversation history
/// 2. Text field to send new messages
/// 3. StreamBuilder to listen for incoming messages
/// 4. Filters messages to show only this conversation
/// =====================================================

class ChatScreen extends StatefulWidget {
  final String currentUsername;
  final String targetUsername;

  const ChatScreen({
    super.key,
    required this.currentUsername,
    required this.targetUsername,
  });

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final TextEditingController _messageController = TextEditingController();
  final WebSocketService _webSocketService = WebSocketService();
  final ScrollController _scrollController = ScrollController();
  StreamSubscription<String>? _messageSubscription;
  
  // List of messages in this conversation
  final List<Map<String, dynamic>> _messages = [];
  
  // Conversation ID (will be set after CREATE_CONVERSATION response)
  int? _conversationId;

  @override
  void initState() {
    super.initState();
    
    // Create conversation first
    _createConversation();
    
    // Listen to WebSocket stream for incoming messages
    _messageSubscription = _webSocketService.messageStream.listen(
      (messageString) {
        final message = _webSocketService.parseMessage(messageString);
        
        if (message != null) {
          // Handle CREATE_CONVERSATION response
          if (message['type'] == 'CREATE_CONVERSATION' && 
              message['status'] == 'SUCCESS') {
            if (mounted) {
              setState(() {
                _conversationId = message['conversationId'];
              });
              print('[ChatScreen] Conversation created: $_conversationId');
            }
          }
          
          // Handle SEND_MESSAGE response (acknowledgment)
          if (message['type'] == 'SEND_MESSAGE' && 
              message['status'] == 'SUCCESS') {
            print('[ChatScreen] Message sent successfully');
          }
          
          // Handle incoming MESSAGE from other users
          if (message['type'] == 'MESSAGE' && 
              message['conversationId'] == _conversationId) {
            // Check if message is for this conversation
            final sender = message['sender'] as String?;
            final recipient = message['recipient'] as String?;
            final content = message['content'] as String?;
            
            if (sender != null && 
                recipient != null && 
                content != null &&
                recipient == widget.currentUsername) {
              // This message is for the current user
              print('[ChatScreen] Received message from $sender: $content');
              
              // Check if message already exists (avoid duplicates)
              final exists = _messages.any((m) => 
                m['sender'] == sender &&
                m['content'] == content &&
                m['timestamp'] == message['timestamp']
              );
              
              if (!exists && mounted) {
                setState(() {
                  _messages.add({
                    'sender': sender,
                    'recipient': recipient,
                    'content': content,
                    'timestamp': message['timestamp'] != null 
                        ? DateTime.fromMillisecondsSinceEpoch(message['timestamp'] as int).toString()
                        : DateTime.now().toString(),
                    'isMe': false,
                  });
                });
                
                // Scroll to bottom when new message arrives
                WidgetsBinding.instance.addPostFrameCallback((_) {
                  _scrollToBottom();
                });
              }
            }
          }
        }
      },
      onError: (error) {
        print('[ChatScreen] Stream error: $error');
      },
    );
    
    // Scroll to bottom when messages are added
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _scrollToBottom();
    });
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    _messageSubscription?.cancel();
    super.dispose();
  }

  /// Create conversation with target user
  /// 
  /// Protocol: {"type": "CREATE_CONVERSATION", "targetUsername": "..."}
  void _createConversation() {
    try {
      _webSocketService.sendMessage({
        'type': 'CREATE_CONVERSATION',
        'targetUsername': widget.targetUsername,
      });
      print('[ChatScreen] Sent CREATE_CONVERSATION request');
    } catch (e) {
      print('[ChatScreen] Error creating conversation: $e');
    }
  }

  /// Send a message to the server
  /// 
  /// Protocol: {"type": "SEND_MESSAGE", "conversationId": ..., "content": "..."}
  void _sendMessage() {
    final content = _messageController.text.trim();
    if (content.isEmpty) return;

    // Wait for conversationId if not available yet
    if (_conversationId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please wait, creating conversation...')),
      );
      return;
    }

    try {
      // Send message via WebSocket
      _webSocketService.sendMessage({
        'type': 'SEND_MESSAGE',  // Changed from 'MESSAGE' to 'SEND_MESSAGE'
        'conversationId': _conversationId,  // Use conversationId instead of sender/recipient
        'content': content,
      });

      // Add message to local list immediately (optimistic update)
      setState(() {
        _messages.add({
          'sender': widget.currentUsername,
          'recipient': widget.targetUsername,
          'content': content,
          'timestamp': DateTime.now().toString(),
          'isMe': true,
        });
      });

      // Clear text field
      _messageController.clear();

      // Scroll to bottom
      _scrollToBottom();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error sending message: $e')),
      );
    }
  }

  /// Scroll to bottom of message list
  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(widget.targetUsername),
            Text(
              'Chatting with ${widget.targetUsername}',
              style: const TextStyle(fontSize: 12),
            ),
          ],
        ),
      ),
      body: Column(
        children: [
          // Messages list
          Expanded(
            child: _buildMessagesList(),
          ),

          // Message input
          _buildMessageInput(),
        ],
      ),
    );
  }

  /// Build messages list
  Widget _buildMessagesList() {
    // Display messages
    if (_messages.isEmpty) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.chat_bubble_outline, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text(
              'No messages yet',
              style: TextStyle(color: Colors.grey),
            ),
            SizedBox(height: 8),
            Text(
              'Start the conversation!',
              style: TextStyle(color: Colors.grey, fontSize: 12),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      controller: _scrollController,
      padding: const EdgeInsets.all(16),
      itemCount: _messages.length,
      itemBuilder: (context, index) {
        final message = _messages[index];
        final isMe = message['isMe'] == true;

        return _buildMessageBubble(message, isMe);
      },
    );
  }

  /// Build a single message bubble
  Widget _buildMessageBubble(Map<String, dynamic> message, bool isMe) {
    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: isMe ? Colors.blue : Colors.grey.shade300,
          borderRadius: BorderRadius.circular(20),
        ),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.7,
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              message['content'] ?? '',
              style: TextStyle(
                color: isMe ? Colors.white : Colors.black,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              _formatTimestamp(message['timestamp']),
              style: TextStyle(
                color: isMe ? Colors.white70 : Colors.grey.shade600,
                fontSize: 10,
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Format timestamp for display
  String _formatTimestamp(String? timestamp) {
    if (timestamp == null) return '';
    try {
      final dateTime = DateTime.parse(timestamp);
      return '${dateTime.hour}:${dateTime.minute.toString().padLeft(2, '0')}';
    } catch (e) {
      return '';
    }
  }

  /// Build message input field
  Widget _buildMessageInput() {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey.shade300,
            blurRadius: 4,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _messageController,
              decoration: InputDecoration(
                hintText: 'Type a message...',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(25),
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 10,
                ),
              ),
              maxLines: null,
              textInputAction: TextInputAction.send,
              onSubmitted: (_) => _sendMessage(),
            ),
          ),
          const SizedBox(width: 8),
          CircleAvatar(
            backgroundColor: Colors.blue,
            child: IconButton(
              icon: const Icon(Icons.send, color: Colors.white),
              onPressed: _sendMessage,
            ),
          ),
        ],
      ),
    );
  }
}
