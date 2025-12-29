package com.chatapp.models;

/**
 * =====================================================
 * Message Model Class
 * =====================================================
 * This class represents a message structure used for
 * communication between client and server.
 * All messages are sent as JSON strings over TCP sockets.
 * =====================================================
 */
public class Message {
    // Message types for different operations
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_SEARCH_USER = "SEARCH_USER";
    public static final String TYPE_CREATE_CONVERSATION = "CREATE_CONVERSATION";
    public static final String TYPE_SEND_MESSAGE = "SEND_MESSAGE";
    public static final String TYPE_GET_CONVERSATIONS = "GET_CONVERSATIONS";
    
    // Group message types
    public static final String TYPE_CREATE_GROUP = "CREATE_GROUP";
    public static final String TYPE_JOIN_GROUP = "JOIN_GROUP";
    public static final String TYPE_GET_GROUPS = "GET_GROUPS";
    public static final String TYPE_GET_GROUP_MEMBERS = "GET_GROUP_MEMBERS";
    
    // Response status
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR = "ERROR";
    
    // Fields
    private String type;           // Type of message/request
    private String status;          // Response status (SUCCESS/ERROR)
    private String username;        // Username for login/search
    private String password;        // Password for login
    private String targetUsername; // Target user for conversation
    private String groupName;       // Group name (for group creation)
    private String content;         // Message content
    private String sender;          // Sender username (for forwarded messages)
    private String recipient;       // Recipient username (for forwarded messages)
    private long timestamp;         // Timestamp for messages
    private int userId;             // User ID
    private int conversationId;     // Conversation ID
    private String errorMessage;    // Error message if status is ERROR
    private Object data;            // Additional data (can be used for lists, etc.)
    
    // Default constructor (required for JSON parsing)
    public Message() {
    }
    
    // Constructor for creating messages
    public Message(String type) {
        this.type = type;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getTargetUsername() {
        return targetUsername;
    }
    
    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Helper method to create a success response message
     */
    public static Message createSuccess(String type) {
        Message msg = new Message(type);
        msg.setStatus(STATUS_SUCCESS);
        return msg;
    }
    
    /**
     * Helper method to create an error response message
     */
    public static Message createError(String type, String errorMessage) {
        Message msg = new Message(type);
        msg.setStatus(STATUS_ERROR);
        msg.setErrorMessage(errorMessage);
        return msg;
    }
}

