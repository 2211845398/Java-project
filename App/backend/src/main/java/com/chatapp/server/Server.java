package com.chatapp.server;

import com.chatapp.database.DatabaseManager;

/**
 * =====================================================
 * Server Class - Main Entry Point
 * =====================================================
 * This is the main server class that:
 * 1. Creates a WebSocketServer to listen for incoming connections
 * 2. Accepts WebSocket client connections
 * 3. Handles all WebSocket events (onOpen, onMessage, onClose)
 * 4. Manages the database connection
 * 
 * Architecture:
 * - WebSocket-based: Uses Java-WebSocket library
 * - Multi-client support: Each client gets its own WebSocket connection
 * - JSON Protocol: All communication uses JSON strings over WebSocket
 * =====================================================
 */
public class Server {
    // Server configuration
    private static final int PORT = 8080;  // Port number to listen on
    private ClientHandler webSocketServer;  // WebSocket server instance
    private DatabaseManager dbManager;      // Database manager (shared across all handlers)
    private boolean isRunning;              // Server running flag
    
    /**
     * Constructor
     */
    public Server() {
        this.isRunning = false;
        
        // Initialize database manager
        // This creates a single connection that will be shared
        // (In production, you might want a connection pool)
        this.dbManager = new DatabaseManager();
        
        // Create WebSocket server
        // ClientHandler extends WebSocketServer and handles all WebSocket events
        this.webSocketServer = new ClientHandler(PORT, dbManager);
    }
    
    /**
     * Starts the WebSocket server
     */
    public void start() {
        try {
            isRunning = true;
            
            // Start the WebSocket server
            // This will listen on the specified port and handle connections
            webSocketServer.start();
            
            System.out.println("=====================================================");
            System.out.println("Chat Application Server Started (WebSocket)");
            System.out.println("=====================================================");
            System.out.println("Listening on port: " + PORT);
            System.out.println("WebSocket endpoint: ws://localhost:" + PORT);
            System.out.println("Waiting for client connections...");
            System.out.println("=====================================================");
            
            // Keep the server running
            // The WebSocket server runs in its own thread
            // We just need to keep the main thread alive
            while (isRunning) {
                Thread.sleep(1000); // Sleep to avoid busy waiting
            }
            
        } catch (InterruptedException e) {
            System.out.println("[Server] Server interrupted");
        } catch (Exception e) {
            if (isRunning) {
                // Only print error if server was supposed to be running
                System.err.println("[Server] Error in server: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            // Clean up when server stops
            stop();
        }
    }
    
    /**
     * Stops the WebSocket server and closes all connections
     */
    public void stop() {
        isRunning = false;
        
        System.out.println("\n[Server] Shutting down server...");
        
        try {
            // Stop the WebSocket server
            // This will close all active connections
            if (webSocketServer != null) {
                webSocketServer.stop();
            }
        } catch (Exception e) {
            System.err.println("[Server] Error stopping WebSocket server: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Close database connection
        if (dbManager != null) {
            dbManager.close();
        }
        
        System.out.println("[Server] Server stopped.");
    }
    
    /**
     * Main method - Entry point of the application
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create and start the server
        Server server = new Server();
        
        // Add shutdown hook to gracefully close server on Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Shutdown signal received...");
            server.stop();
        }));
        
        // Start the server
        // This will block and keep running until stopped
        server.start();
    }
}
