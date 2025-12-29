-- =====================================================
-- Chat Application Database Schema
-- =====================================================
-- This script creates the database and tables for the
-- client-server chat application (mini Telegram clone).
-- =====================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS chat_app;
USE chat_app;

-- =====================================================
-- Table: users
-- =====================================================
-- Stores user account information
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- Table: conversations
-- =====================================================
-- Stores conversation channels (single chat or group chat)
-- type: 'single' for one-on-one chats, 'group' for group chats
-- =====================================================
CREATE TABLE IF NOT EXISTS conversations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('single', 'group') NOT NULL DEFAULT 'single',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- Table: participants
-- =====================================================
-- Junction table linking users to conversations
-- This table implements the rule: "Two users can share 
-- only ONE individual conversation channel"
-- =====================================================
CREATE TABLE IF NOT EXISTS participants (
    conversation_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- Table: messages (Optional - for future message storage)
-- =====================================================
-- Uncomment this if you want to store messages in the database
-- =====================================================
/*
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id INT NOT NULL,
    sender_id INT NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
*/

-- =====================================================
-- Important Database Logic:
-- =====================================================
-- To enforce "Two users can share only ONE individual 
-- conversation channel", we need to:
-- 1. Check if a 'single' type conversation exists between
--    two specific users before creating a new one.
-- 2. This logic will be implemented in DatabaseManager.java
--    using a query that checks participants table.
-- =====================================================

