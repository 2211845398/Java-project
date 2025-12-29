package com.chatapp.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================
 * DatabaseManager Class
 * =====================================================
 * This class handles all database operations for the
 * chat application using JDBC.
 * 
 * Responsibilities:
 * - Managing MySQL connection
 * - User authentication (login)
 * - User search functionality
 * - Conversation creation and retrieval
 * - Enforcing the rule: "Two users can share only ONE
 *   individual conversation channel"
 * =====================================================
 */
public class DatabaseManager {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chat_app";
    private static final String DB_USER = "root";  // Change this to your MySQL username
    private static final String DB_PASSWORD = "";  // Change this to your MySQL password
    
    private Connection connection;
    
    /**
     * Constructor - Establishes database connection
     */
    public DatabaseManager() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DatabaseManager] Connected to MySQL database successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Failed to connect to database!");
            e.printStackTrace();
        }
    }
    
    /**
     * Authenticates a user with username and password
     * 
     * @param username The username to authenticate
     * @param password The password to verify
     * @return User ID if authentication succeeds, -1 if it fails
     */
    public int login(String username, String password) {
        if (connection == null) {
            System.err.println("[DatabaseManager] No database connection!");
            return -1;
        }
        
        try {
            // Prepare SQL query to find user by username and password
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, use hashed passwords!
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                System.out.println("[DatabaseManager] User '" + username + "' logged in successfully (ID: " + userId + ")");
                return userId;
            } else {
                System.out.println("[DatabaseManager] Login failed for user '" + username + "'");
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error during login: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Searches for users by username (partial match)
     * 
     * @param searchTerm The search term (can be partial username)
     * @param excludeUserId User ID to exclude from results (usually the current user)
     * @return List of usernames matching the search term
     */
    public List<String> searchUsers(String searchTerm, int excludeUserId) {
        List<String> users = new ArrayList<>();
        
        if (connection == null) {
            System.err.println("[DatabaseManager] No database connection!");
            return users;
        }
        
        try {
            // Search for users whose username contains the search term
            // Exclude the current user from results
            String sql = "SELECT username FROM users WHERE username LIKE ? AND id != ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "%" + searchTerm + "%"); // % allows partial matching
            stmt.setInt(2, excludeUserId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
            
            System.out.println("[DatabaseManager] Found " + users.size() + " users matching '" + searchTerm + "'");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error during user search: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Gets or creates a conversation between two users.
     * 
     * CRUCIAL LOGIC: This method enforces the rule that
     * "Two users can share only ONE individual conversation channel"
     * 
     * Steps:
     * 1. Check if a 'single' type conversation already exists
     *    between the two users
     * 2. If exists, return the existing conversation ID
     * 3. If not, create a new conversation and add both users
     *    as participants
     * 
     * @param userId1 ID of the first user
     * @param userId2 ID of the second user
     * @return Conversation ID (existing or newly created), or -1 on error
     */
    public int getOrCreateConversation(int userId1, int userId2) {
        if (connection == null) {
            System.err.println("[DatabaseManager] No database connection!");
            return -1;
        }
        
        try {
            // First, check if a conversation already exists between these two users
            // We need to find a 'single' type conversation where both users are participants
            String checkSql = "SELECT c.id FROM conversations c " +
                             "INNER JOIN participants p1 ON c.id = p1.conversation_id " +
                             "INNER JOIN participants p2 ON c.id = p2.conversation_id " +
                             "WHERE c.type = 'single' " +
                             "AND p1.user_id = ? AND p2.user_id = ? " +
                             "AND p1.user_id != p2.user_id";
            
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, userId1);
            checkStmt.setInt(2, userId2);
            
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Conversation already exists
                int existingConversationId = rs.getInt("id");
                System.out.println("[DatabaseManager] Found existing conversation (ID: " + existingConversationId + 
                                 ") between users " + userId1 + " and " + userId2);
                return existingConversationId;
            }
            
            // No existing conversation found, create a new one
            // We'll use a transaction to ensure atomicity
            connection.setAutoCommit(false);
            
            try {
                // Step 1: Create a new 'single' type conversation
                String insertConversationSql = "INSERT INTO conversations (type) VALUES ('single')";
                PreparedStatement insertConvStmt = connection.prepareStatement(
                    insertConversationSql, Statement.RETURN_GENERATED_KEYS);
                insertConvStmt.executeUpdate();
                
                // Get the generated conversation ID
                ResultSet generatedKeys = insertConvStmt.getGeneratedKeys();
                int newConversationId = -1;
                if (generatedKeys.next()) {
                    newConversationId = generatedKeys.getInt(1);
                }
                
                // Step 2: Add both users as participants
                String insertParticipantSql = "INSERT INTO participants (conversation_id, user_id) VALUES (?, ?)";
                
                // Add first user
                PreparedStatement insertPartStmt1 = connection.prepareStatement(insertParticipantSql);
                insertPartStmt1.setInt(1, newConversationId);
                insertPartStmt1.setInt(2, userId1);
                insertPartStmt1.executeUpdate();
                
                // Add second user
                PreparedStatement insertPartStmt2 = connection.prepareStatement(insertParticipantSql);
                insertPartStmt2.setInt(1, newConversationId);
                insertPartStmt2.setInt(2, userId2);
                insertPartStmt2.executeUpdate();
                
                // Commit the transaction
                connection.commit();
                connection.setAutoCommit(true);
                
                System.out.println("[DatabaseManager] Created new conversation (ID: " + newConversationId + 
                                 ") between users " + userId1 + " and " + userId2);
                return newConversationId;
                
            } catch (SQLException e) {
                // Rollback on error
                connection.rollback();
                connection.setAutoCommit(true);
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error getting/creating conversation: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Gets the username by user ID
     * 
     * @param userId The user ID
     * @return Username or null if not found
     */
    public String getUsernameById(int userId) {
        if (connection == null) {
            return null;
        }
        
        try {
            String sql = "SELECT username FROM users WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error getting username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets the user ID by username
     * 
     * @param username The username
     * @return User ID or -1 if not found
     */
    public int getUserIdByUsername(String username) {
        if (connection == null) {
            return -1;
        }
        
        try {
            String sql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error getting user ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Gets the other participant's user ID in a conversation
     * 
     * @param conversationId The conversation ID
     * @param currentUserId The current user's ID (to exclude from results)
     * @return The other participant's user ID, or -1 if not found
     */
    public int getOtherParticipantId(int conversationId, int currentUserId) {
        if (connection == null) {
            return -1;
        }
        
        try {
            String sql = "SELECT user_id FROM participants WHERE conversation_id = ? AND user_id != ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, conversationId);
            stmt.setInt(2, currentUserId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error getting other participant: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Closes the database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DatabaseManager] Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

