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

class _HomeState extends State<Home> with SingleTickerProviderStateMixin {
  final WebSocketService _webSocketService = WebSocketService();
  List<String> _users = [];
  Map<int, String> _groups = {}; // groupId -> groupName
  bool _isLoading = true;
  late TabController _tabController;
  StreamSubscription<String>? _messageSubscription;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _tabController.addListener(() {
      setState(() {}); // Rebuild when tab changes to update FAB
    });
    // Send SEARCH_USER request when screen initializes
    _requestUsers();
    // Request groups
    _requestGroups();
    
    // Listen to WebSocket stream for incoming messages
    _messageSubscription = _webSocketService.messageStream.listen(
      (messageString) {
        final message = _webSocketService.parseMessage(messageString);
        
        if (message != null) {
          // Handle SEARCH_USER response
          if (message['type'] == 'SEARCH_USER' && 
              message['status'] == 'SUCCESS' &&
              message['data'] != null && 
              message['data'] is List) {
            if (mounted) {
              setState(() {
                _users = List<String>.from(message['data']);
                _isLoading = false;
              });
            }
          } else if (message['type'] == 'SEARCH_USER' && 
                     message['status'] == 'ERROR') {
            if (mounted) {
              setState(() {
                _isLoading = false;
              });
            }
            print('[HomeScreen] Error: ${message['errorMessage']}');
          }
          
          // Handle CREATE_GROUP response
          if (message['type'] == 'CREATE_GROUP' && 
              message['status'] == 'SUCCESS') {
            if (mounted) {
              setState(() {
                _groups[message['conversationId']] = message['groupName'] ?? 'Group ${message['conversationId']}';
              });
            }
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Group "${message['groupName']}" created successfully!')),
            );
          }
          
          // Handle GET_GROUPS response
          if (message['type'] == 'GET_GROUPS' && 
              message['status'] == 'SUCCESS' &&
              message['data'] != null) {
            if (mounted) {
              setState(() {
                _groups = Map<int, String>.from(message['data']);
              });
            }
          }
          
          // Handle JOIN_GROUP response
          if (message['type'] == 'JOIN_GROUP' && 
              message['status'] == 'SUCCESS') {
            if (mounted) {
              setState(() {
                _groups[message['conversationId']] = message['groupName'] ?? 'Group ${message['conversationId']}';
              });
            }
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Joined group successfully!')),
            );
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
    _tabController.dispose();
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

  /// Request groups from server
  void _requestGroups() {
    try {
      _webSocketService.sendMessage({
        'type': 'GET_GROUPS',
      });
      print('[HomeScreen] Sent GET_GROUPS request');
    } catch (e) {
      print('[HomeScreen] Error sending GET_GROUPS: $e');
    }
  }

  /// Create a new group
  void _createGroup(String groupName) {
    try {
      _webSocketService.sendMessage({
        'type': 'CREATE_GROUP',
        'groupName': groupName,
      });
      print('[HomeScreen] Sent CREATE_GROUP request: $groupName');
    } catch (e) {
      print('[HomeScreen] Error creating group: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error creating group: $e')),
      );
    }
  }

  /// Join a group by ID
  void _joinGroup(int groupId) {
    try {
      _webSocketService.sendMessage({
        'type': 'JOIN_GROUP',
        'conversationId': groupId,
      });
      print('[HomeScreen] Sent JOIN_GROUP request: $groupId');
    } catch (e) {
      print('[HomeScreen] Error joining group: $e');
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
          isGroup: false,
        ),
      ),
    );
  }

  /// Handle group tap - navigate to ChatScreen for group
  void _handleGroupTap(int groupId, String groupName) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => ChatScreen(
          currentUsername: widget.username,
          targetUsername: groupName,
          conversationId: groupId,
          isGroup: true,
        ),
      ),
    );
  }

  /// Show create group dialog
  void _showCreateGroupDialog() {
    final TextEditingController groupNameController = TextEditingController();
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Create Group'),
        content: TextField(
          controller: groupNameController,
          decoration: const InputDecoration(
            labelText: 'Group Name',
            hintText: 'Enter group name',
          ),
          autofocus: true,
          onSubmitted: (value) {
            if (value.trim().isNotEmpty) {
              _createGroup(value.trim());
              Navigator.pop(context);
            }
          },
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              final groupName = groupNameController.text.trim();
              if (groupName.isNotEmpty) {
                _createGroup(groupName);
                Navigator.pop(context);
              }
            },
            child: const Text('Create'),
          ),
        ],
      ),
    );
  }

  /// Show join group dialog
  void _showJoinGroupDialog() {
    final TextEditingController groupIdController = TextEditingController();
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Join Group'),
        content: TextField(
          controller: groupIdController,
          decoration: const InputDecoration(
            labelText: 'Group ID',
            hintText: 'Enter group ID',
          ),
          keyboardType: TextInputType.number,
          autofocus: true,
          onSubmitted: (value) {
            final groupId = int.tryParse(value.trim());
            if (groupId != null && groupId > 0) {
              _joinGroup(groupId);
              Navigator.pop(context);
            }
          },
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              final groupId = int.tryParse(groupIdController.text.trim());
              if (groupId != null && groupId > 0) {
                _joinGroup(groupId);
                Navigator.pop(context);
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Please enter a valid group ID')),
                );
              }
            },
            child: const Text('Join'),
          ),
        ],
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
                _groups = {};
              });
              _requestUsers();
              _requestGroups();
            },
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(icon: Icon(Icons.person), text: 'Users'),
            Tab(icon: Icon(Icons.group), text: 'Groups'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildUsersList(),
          _buildGroupsList(),
        ],
      ),
      floatingActionButton: _tabController.index == 0
          ? FloatingActionButton(
              heroTag: "startChat",
              onPressed: () {
                // Show search dialog
                // showDialog(
                //   context: context,
                //   builder: (context) => _SearchUserDialog(
                //     onUserSelected: _handleUserTap,
                //   ),
                // );
              },
              child: const Icon(Icons.add),
              tooltip: 'Start New Chat',
            )
          : FloatingActionButton(
              heroTag: "createGroup",
              onPressed: _showCreateGroupDialog,
              child: const Icon(Icons.group_add),
              tooltip: 'Create Group',
            ),
    );
  }

  Widget _buildUsersList() {
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

  Widget _buildGroupsList() {
    // Show empty state
    if (_groups.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.group_outlined, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            const Text(
              'No groups yet',
              style: TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 8),
            const Text(
              'Tap + to create a group',
              style: TextStyle(color: Colors.grey, fontSize: 12),
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: _showJoinGroupDialog,
              icon: const Icon(Icons.group_add),
              label: const Text('Join Group by ID'),
            ),
          ],
        ),
      );
    }

    // Display groups list
    return Column(
      children: [
        // Join Group button at top
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: ElevatedButton.icon(
            onPressed: _showJoinGroupDialog,
            icon: const Icon(Icons.group_add),
            label: const Text('Join Group by ID'),
            style: ElevatedButton.styleFrom(
              minimumSize: const Size(double.infinity, 40),
            ),
          ),
        ),
        // Groups list
        Expanded(
          child: ListView.builder(
            itemCount: _groups.length,
            itemBuilder: (context, index) {
              final groupId = _groups.keys.elementAt(index);
              final groupName = _groups[groupId]!;

              return ListTile(
                leading: CircleAvatar(
                  backgroundColor: Colors.blue,
                  child: const Icon(Icons.group, color: Colors.white),
                ),
                title: Text(groupName),
                subtitle: Text('Group ID: $groupId'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _handleGroupTap(groupId, groupName),
              );
            },
          ),
        ),
      ],
    );
  }
}