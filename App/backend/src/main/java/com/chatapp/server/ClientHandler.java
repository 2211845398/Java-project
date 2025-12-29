package com.chatapp.server;

import com.chatapp.database.DatabaseManager;
import com.chatapp.models.Message;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =====================================================
 * ClientHandler Class - WebSocket Handler
 * =====================================================
 * This class extends WebSocketServer and handles WebSocket
 * connections with clients.
 * 
 * Responsibilities:
 * - Handle WebSocket connections (onOpen, onClose, onMessage)
 * - Read JSON messages from the client
 * - Parse and process different message types
 * - Send JSON responses back to the client
 * - Maintain the client's authenticated user ID
 * 
 * Communication Protocol:
 * - All messages are JSON strings over WebSocket
 * - WebSocket handles message framing automatically
 * - No need for newline characters (\n)
 * =====================================================
 */
public class ClientHandler extends WebSocketServer {
    private DatabaseManager dbManager;        // Database manager instance
    private Gson gson;                        // JSON parser (Gson library)
    
    // Map to store WebSocket connections and their associated user data
    // Key: WebSocket connection, Value: UserInfo object
    private Map<WebSocket, UserInfo> clientData = new HashMap<>();
    
    /**
     * Inner class to store user information for each WebSocket connection
     */
    private static class UserInfo {
        int authenticatedUserId = -1;    // Current user's ID (-1 means not authenticated)
        String username;                  // Current user's username
        Map<Integer, String> activeConversations = new HashMap<>(); // Active conversations
        
        UserInfo() {
            this.authenticatedUserId = -1;
            this.username = null;
            this.activeConversations = new HashMap<>();
        }
    }
    
    /**
     * Constructor
     * 
     * @param port The port number to listen on
     * @param dbManager The database manager instance (shared across all handlers)
     */
    public ClientHandler(int port, DatabaseManager dbManager) {
        super(new InetSocketAddress(port));
        this.dbManager = dbManager;
        this.gson = new Gson();
    }
    
    /**
     * Called when a new WebSocket connection is established
     * 
     * @param conn The WebSocket connection
     * @param handshake The handshake data
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Create a new UserInfo object for this connection
        clientData.put(conn, new UserInfo());
        
        System.out.println("[ClientHandler] New WebSocket client connected: " + 
                         conn.getRemoteSocketAddress());
        System.out.println("[ClientHandler] Total connections: " + clientData.size());
    }
    
    /**
     * Called when a WebSocket connection is closed
     * 
     * @param conn The WebSocket connection
     * @param code The close code
     * @param reason The close reason
     * @param remote Whether the close was initiated by the remote peer
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        UserInfo userInfo = clientData.remove(conn);
        
        if (userInfo != null && userInfo.username != null) {
            System.out.println("[ClientHandler] Client disconnected: " + userInfo.username + 
                             " (" + conn.getRemoteSocketAddress() + ")");
        } else {
            System.out.println("[ClientHandler] Client disconnected: " + conn.getRemoteSocketAddress());
        }
        
        System.out.println("[ClientHandler] Total connections: " + clientData.size());
    }
    
    /**
     * Called when a message is received from a WebSocket client
     * 
     * @param conn The WebSocket connection
     * @param message The message received (JSON string)
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[ClientHandler] Received from " + conn.getRemoteSocketAddress() + ": " + message);
        
        // Process the received message
        processMessage(conn, message);
    }
    
    /**
     * Called when an error occurs
     * 
     * @param conn The WebSocket connection (may be null)
     * @param ex The exception that occurred
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[ClientHandler] WebSocket error: " + ex.getMessage());
        if (conn != null) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Called when the server starts
     */
    @Override
    public void onStart() {
        System.out.println("[ClientHandler] WebSocket server started successfully!");
    }
    
    /**
     * Processes a JSON message received from the client
     * 
     * @param conn The WebSocket connection
     * @param jsonString The JSON string received from client
     */
    private void processMessage(WebSocket conn, String jsonString) {
        try {
            // Parse JSON string into Message object
            Message request = gson.fromJson(jsonString, Message.class);
            
            if (request == null || request.getType() == null) {
                sendError(conn, "Invalid message format");
                return;
            }
            
            // Get user info for this connection
            UserInfo userInfo = clientData.get(conn);
            if (userInfo == null) {
                userInfo = new UserInfo();
                clientData.put(conn, userInfo);
            }
            
            // Route the message based on its type
            switch (request.getType()) {
                case Message.TYPE_LOGIN:
                    handleLogin(conn, request, userInfo);
                    break;
                    
                case Message.TYPE_SEARCH_USER:
                    handleSearchUser(conn, request, userInfo);
                    break;
                    
                case Message.TYPE_CREATE_CONVERSATION:
                    handleCreateConversation(conn, request, userInfo);
                    break;
                    
                case Message.TYPE_SEND_MESSAGE:
                    handleSendMessage(conn, request, userInfo);
                    break;
                    
                case Message.TYPE_GET_CONVERSATIONS:
                    handleGetConversations(conn, request, userInfo);
                    break;
                    
                case Message.TYPE_CREATE_GROUP:
                    handleCreateGroup(conn, request, userInfo);
                    break;
                    
                case Message.TYPE_JOIN_GROUP:
                    handleJoinGroup(conn, request, userInfo);
                    break;
                    
                case Message.TYPE_GET_GROUPS:
                    handleGetGroups(conn, request, userInfo);
                    break;
                    
                case Message.TYPE_GET_GROUP_MEMBERS:
                    handleGetGroupMembers(conn, request, userInfo);
                    break;
                    
                default:
                    sendError(conn, "Unknown message type: " + request.getType());
            }
            
        } catch (JsonSyntaxException e) {
            System.err.println("[ClientHandler] Error parsing JSON: " + e.getMessage());
            sendError(conn, "Invalid JSON format");
        } catch (Exception e) {
            System.err.println("[ClientHandler] Error processing message: " + e.getMessage());
            e.printStackTrace();
            sendError(conn, "Server error: " + e.getMessage());
        }
    }
    
    /**
     * Handles login request
     * 
     * @param conn The WebSocket connection
     * @param request The login message containing username and password
     * @param userInfo The user info for this connection
     */
    private void handleLogin(WebSocket conn, Message request, UserInfo userInfo) {
        String username = request.getUsername();
        String password = request.getPassword();
        
        if (username == null || password == null) {
            sendError(conn, "Username and password are required");
            return;
        }
        
        // Authenticate user with database
        int userId = dbManager.login(username, password);
        
        Message response = new Message(Message.TYPE_LOGIN);
        
        if (userId > 0) {
            // Login successful
            userInfo.authenticatedUserId = userId;
            userInfo.username = username;
            
            response.setStatus(Message.STATUS_SUCCESS);
            response.setUserId(userId);
            response.setUsername(username);
            
            System.out.println("[ClientHandler] User '" + username + "' (ID: " + userId + ") logged in successfully");
        } else {
            // Login failed
            response.setStatus(Message.STATUS_ERROR);
            response.setErrorMessage("Invalid username or password");
            
            System.out.println("[ClientHandler] Login failed for user '" + username + "'");
        }
        
        sendMessage(conn, response);
    }
    
    /**
     * Handles user search request
     * 
     * @param conn The WebSocket connection
     * @param request The search message containing search term
     * @param userInfo The user info for this connection
     */
    private void handleSearchUser(WebSocket conn, Message request, UserInfo userInfo) {
        // Check if user is authenticated
        if (userInfo.authenticatedUserId == -1) {
            sendError(conn, "Please login first");
            return;
        }
        
        String searchTerm = request.getUsername(); // Using username field for search term
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            sendError(conn, "Search term is required");
            return;
        }
        
        // Search for users in database
        List<String> users = dbManager.searchUsers(searchTerm, userInfo.authenticatedUserId);
        
        Message response = Message.createSuccess(Message.TYPE_SEARCH_USER);
        response.setData(users); // Store the list of usernames in the data field
        
        sendMessage(conn, response);
    }
    
    /**
     * Handles conversation creation request
     * 
     * @param conn The WebSocket connection
     * @param request The message containing target username
     * @param userInfo The user info for this connection
     */
    private void handleCreateConversation(WebSocket conn, Message request, UserInfo userInfo) {
        // Check if user is authenticated
        if (userInfo.authenticatedUserId == -1) {
            sendError(conn, "Please login first");
            return;
        }
        
        String targetUsername = request.getTargetUsername();
        
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            sendError(conn, "Target username is required");
            return;
        }
        
        // Get target user's ID
        int targetUserId = dbManager.getUserIdByUsername(targetUsername);
        
        if (targetUserId == -1) {
            sendError(conn, "User '" + targetUsername + "' not found");
            return;
        }
        
        if (targetUserId == userInfo.authenticatedUserId) {
            sendError(conn, "Cannot create conversation with yourself");
            return;
        }
        
        // Get or create conversation (this enforces the "one conversation per pair" rule)
        int conversationId = dbManager.getOrCreateConversation(userInfo.authenticatedUserId, targetUserId);
        
        if (conversationId == -1) {
            sendError(conn, "Failed to create conversation");
            return;
        }
        
        // Store this conversation in active conversations
        userInfo.activeConversations.put(conversationId, targetUsername);
        
        Message response = Message.createSuccess(Message.TYPE_CREATE_CONVERSATION);
        response.setConversationId(conversationId);
        response.setTargetUsername(targetUsername);
        
        System.out.println("[ClientHandler] Conversation " + conversationId + " created/retrieved for user " + 
                         userInfo.authenticatedUserId + " with " + targetUsername);
        
        sendMessage(conn, response);
    }
    
    /**
     * Handles sending a message and forwards it to the recipient
     * 
     * @param conn The WebSocket connection
     * @param request The message containing content and conversation ID
     * @param userInfo The user info for this connection
     */
    private void handleSendMessage(WebSocket conn, Message request, UserInfo userInfo) {
        // Check if user is authenticated
        if (userInfo.authenticatedUserId == -1) {
            sendError(conn, "Please login first");
            return;
        }
        
        int conversationId = request.getConversationId();
        String content = request.getContent();
        
        if (conversationId <= 0) {
            sendError(conn, "Invalid conversation ID");
            return;
        }
        
        if (content == null || content.trim().isEmpty()) {
            sendError(conn, "Message content is required");
            return;
        }
        
        // Get sender's username
        String senderUsername = userInfo.username;
        if (senderUsername == null) {
            senderUsername = dbManager.getUsernameById(userInfo.authenticatedUserId);
        }
        
        // Check if this is a group conversation or single conversation
        // For groups, we need to get all members. For single, get the other participant.
        List<Integer> recipients = new ArrayList<>();
        
        // Check conversation type by getting all participants
        List<Integer> allParticipants = dbManager.getGroupMembers(conversationId);
        
        if (allParticipants.size() > 2) {
            // This is a group conversation - send to all members except sender
            for (Integer participantId : allParticipants) {
                if (participantId != userInfo.authenticatedUserId) {
                    recipients.add(participantId);
                }
            }
            System.out.println("[ClientHandler] Group message from user " + userInfo.authenticatedUserId + 
                             " (" + senderUsername + ") to group " + conversationId + 
                             " (" + recipients.size() + " members): " + content);
        } else {
            // This is a single conversation - get the other participant
            int otherParticipantId = dbManager.getOtherParticipantId(conversationId, userInfo.authenticatedUserId);
            if (otherParticipantId == -1) {
                sendError(conn, "Other participant not found in conversation");
                return;
            }
            recipients.add(otherParticipantId);
            String recipientUsername = dbManager.getUsernameById(otherParticipantId);
            System.out.println("[ClientHandler] Message from user " + userInfo.authenticatedUserId + 
                             " (" + senderUsername + ") to user " + otherParticipantId + 
                             " (" + recipientUsername + ") in conversation " + conversationId + ": " + content);
        }
        
        // Acknowledge receipt to sender
        Message response = Message.createSuccess(Message.TYPE_SEND_MESSAGE);
        response.setContent("Message received");
        sendMessage(conn, response);
        
        // Forward message to all recipients if they're online
        int forwardedCount = 0;
        for (Integer recipientId : recipients) {
            for (Map.Entry<WebSocket, UserInfo> entry : clientData.entrySet()) {
                WebSocket recipientConn = entry.getKey();
                UserInfo recipientInfo = entry.getValue();
                
                // Check if this connection belongs to the recipient
                if (recipientInfo.authenticatedUserId == recipientId) {
                    // Create message to forward
                    Message forwardMessage = new Message();
                    forwardMessage.setType("MESSAGE");  // Use "MESSAGE" type for incoming messages
                    forwardMessage.setSender(senderUsername);
                    forwardMessage.setContent(content);
                    forwardMessage.setConversationId(conversationId);
                    forwardMessage.setTimestamp(System.currentTimeMillis());
                    
                    // For single conversations, set recipient. For groups, leave it null.
                    if (recipients.size() == 1) {
                        String recipientUsername = dbManager.getUsernameById(recipientId);
                        forwardMessage.setRecipient(recipientUsername);
                    }
                    
                    // Send to recipient
                    sendMessage(recipientConn, forwardMessage);
                    forwardedCount++;
                    
                    String recipientUsername = dbManager.getUsernameById(recipientId);
                    System.out.println("[ClientHandler] Message forwarded to user " + recipientId + 
                                     " (" + recipientUsername + ")");
                    break;
                }
            }
        }
        
        if (forwardedCount == 0) {
            System.out.println("[ClientHandler] No recipients are online. Message not forwarded.");
        } else {
            System.out.println("[ClientHandler] Message forwarded to " + forwardedCount + " recipient(s)");
        }
    }
    
    /**
     * Handles getting list of conversations (for future implementation)
     * 
     * @param conn The WebSocket connection
     * @param request The request message
     * @param userInfo The user info for this connection
     */
    private void handleGetConversations(WebSocket conn, Message request, UserInfo userInfo) {
        // Check if user is authenticated
        if (userInfo.authenticatedUserId == -1) {
            sendError(conn, "Please login first");
            return;
        }
        
        // TODO: Query database for all conversations this user is part of
        // For now, return the active conversations stored in memory
        Message response = Message.createSuccess(Message.TYPE_GET_CONVERSATIONS);
        response.setData(userInfo.activeConversations);
        
        sendMessage(conn, response);
    }
    
    /**
     * Handles creating a new group
     * 
     * @param conn The WebSocket connection
     * @param request The request message containing group name
     * @param userInfo The user info for this connection
     */
    private void handleCreateGroup(WebSocket conn, Message request, UserInfo userInfo) {
        // Check if user is authenticated
        if (userInfo.authenticatedUserId == -1) {
            sendError(conn, "Please login first");
            return;
        }
        
        String groupName = request.getGroupName();
        
        if (groupName == null || groupName.trim().isEmpty()) {
            sendError(conn, "Group name is required");
            return;
        }
        
        // Create group in database
        int groupId = dbManager.createGroup(userInfo.authenticatedUserId, groupName);
        
        if (groupId == -1) {
            sendError(conn, "Failed to create group");
            return;
        }
        
        // Store this group in active conversations
        userInfo.activeConversations.put(groupId, groupName);
        
        Message response = Message.createSuccess(Message.TYPE_CREATE_GROUP);
        response.setConversationId(groupId);
        response.setGroupName(groupName);
        
        System.out.println("[ClientHandler] Group '" + groupName + "' (ID: " + groupId + 
                         ") created by user " + userInfo.authenticatedUserId);
        
        sendMessage(conn, response);
    }
    
    /**
     * Handles joining a group
     * 
     * @param conn The WebSocket connection
     * @param request The request message containing group ID
     * @param userInfo The user info for this connection
     */
    private void handleJoinGroup(WebSocket conn, Message request, UserInfo userInfo) {
        // Check if user is authenticated
        if (userInfo.authenticatedUserId == -1) {
            sendError(conn, "Please login first");
            return;
        }
        
        int groupId = request.getConversationId();
        
        if (groupId <= 0) {
            sendError(conn, "Invalid group ID");
            return;
        }
        
        // Join group in database
        boolean success = dbManager.joinGroup(groupId, userInfo.authenticatedUserId);
        
        if (!success) {
            sendError(conn, "Failed to join group");
            return;
        }
        
        // Get group name (you might want to store it in database)
        String groupName = "Group " + groupId;
        
        // Store this group in active conversations
        userInfo.activeConversations.put(groupId, groupName);
        
        Message response = Message.createSuccess(Message.TYPE_JOIN_GROUP);
        response.setConversationId(groupId);
        response.setGroupName(groupName);
        
        System.out.println("[ClientHandler] User " + userInfo.authenticatedUserId + 
                         " joined group " + groupId);
        
        sendMessage(conn, response);
    }
    
    /**
     * Handles getting list of groups the user is a member of
     * 
     * @param conn The WebSocket connection
     * @param request The request message
     * @param userInfo The user info for this connection
     */
    private void handleGetGroups(WebSocket conn, Message request, UserInfo userInfo) {
        // Check if user is authenticated
        if (userInfo.authenticatedUserId == -1) {
            sendError(conn, "Please login first");
            return;
        }
        
        // Get user's groups from database
        List<Integer> groupIds = dbManager.getUserGroups(userInfo.authenticatedUserId);
        
        // Convert to map (groupId -> groupName)
        Map<Integer, String> groups = new HashMap<>();
        for (Integer groupId : groupIds) {
            groups.put(groupId, "Group " + groupId); // You might want to store actual group names
        }
        
        Message response = Message.createSuccess(Message.TYPE_GET_GROUPS);
        response.setData(groups);
        
        System.out.println("[ClientHandler] User " + userInfo.authenticatedUserId + 
                         " is member of " + groups.size() + " groups");
        
        sendMessage(conn, response);
    }
    
    /**
     * Handles getting list of members in a group
     * 
     * @param conn The WebSocket connection
     * @param request The request message containing group ID
     * @param userInfo The user info for this connection
     */
    private void handleGetGroupMembers(WebSocket conn, Message request, UserInfo userInfo) {
        // Check if user is authenticated
        if (userInfo.authenticatedUserId == -1) {
            sendError(conn, "Please login first");
            return;
        }
        
        int groupId = request.getConversationId();
        
        if (groupId <= 0) {
            sendError(conn, "Invalid group ID");
            return;
        }
        
        // Get group members from database
        List<Integer> memberIds = dbManager.getGroupMembers(groupId);
        
        // Convert to usernames
        List<String> memberUsernames = new ArrayList<>();
        for (Integer memberId : memberIds) {
            String username = dbManager.getUsernameById(memberId);
            if (username != null) {
                memberUsernames.add(username);
            }
        }
        
        Message response = Message.createSuccess(Message.TYPE_GET_GROUP_MEMBERS);
        response.setData(memberUsernames);
        response.setConversationId(groupId);
        
        System.out.println("[ClientHandler] Group " + groupId + " has " + memberUsernames.size() + " members");
        
        sendMessage(conn, response);
    }
    
    /**
     * Sends a JSON message to the client via WebSocket
     * 
     * @param conn The WebSocket connection
     * @param message The Message object to send
     */
    private void sendMessage(WebSocket conn, Message message) {
        try {
            // Convert Message object to JSON string
            String jsonResponse = gson.toJson(message);
            
            // Send JSON string via WebSocket
            // WebSocket handles message framing automatically
            conn.send(jsonResponse);
            
            System.out.println("[ClientHandler] Sent to " + conn.getRemoteSocketAddress() + ": " + jsonResponse);
        } catch (Exception e) {
            System.err.println("[ClientHandler] Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sends an error message to the client
     * 
     * @param conn The WebSocket connection
     * @param errorMessage The error message text
     */
    private void sendError(WebSocket conn, String errorMessage) {
        Message errorResponse = Message.createError("ERROR", errorMessage);
        sendMessage(conn, errorResponse);
    }
    
    /**
     * Getter for authenticated user ID of a specific connection
     * 
     * @param conn The WebSocket connection
     * @return User ID or -1 if not authenticated
     */
    public int getAuthenticatedUserId(WebSocket conn) {
        UserInfo userInfo = clientData.get(conn);
        return userInfo != null ? userInfo.authenticatedUserId : -1;
    }
    
    /**
     * Getter for username of a specific connection
     * 
     * @param conn The WebSocket connection
     * @return Username or null if not authenticated
     */
    public String getUsername(WebSocket conn) {
        UserInfo userInfo = clientData.get(conn);
        return userInfo != null ? userInfo.username : null;
    }
}
