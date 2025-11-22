-- MySQL Database Setup Script for Library Management System
-- Run this script in MySQL to create the database and tables

-- Create database
CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- Create Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    registration_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Library Items table (for both books and magazines)
CREATE TABLE IF NOT EXISTS library_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(50) UNIQUE,
    publication_date DATE,
    is_available BOOLEAN DEFAULT TRUE,
    item_type ENUM('BOOK', 'MAGAZINE') NOT NULL,
    -- Book specific fields
    number_of_pages INT,
    genre VARCHAR(100),
    publisher VARCHAR(255),
    -- Magazine specific fields
    issue_number INT,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Reservations table
CREATE TABLE IF NOT EXISTS reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM('ACTIVE', 'RETURNED', 'OVERDUE', 'CANCELLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES library_items(id) ON DELETE CASCADE
);

-- Insert sample data (optional)
INSERT INTO users (first_name, last_name, email, phone_number, registration_date) VALUES
('John', 'Doe', 'john.doe@email.com', '123-456-7890', CURDATE()),
('Jane', 'Smith', 'jane.smith@email.com', '098-765-4321', CURDATE());

INSERT INTO library_items (title, author, isbn, publication_date, item_type, number_of_pages, genre, publisher) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', '978-0-7432-7356-5', '1925-04-10', 'BOOK', 180, 'Fiction', 'Scribner'),
('To Kill a Mockingbird', 'Harper Lee', '978-0-06-112008-4', '1960-07-11', 'BOOK', 281, 'Fiction', 'J.B. Lippincott & Co.');

INSERT INTO library_items (title, author, isbn, publication_date, item_type, issue_number, publisher, category) VALUES
('National Geographic', 'Various', 'NG-2024-01', '2024-01-01', 'MAGAZINE', 1, 'National Geographic Society', 'Science'),
('Time Magazine', 'Various', 'TIME-2024-01', '2024-01-01', 'MAGAZINE', 1, 'Time Inc.', 'News');

-- Display created tables
SHOW TABLES;

-- Display table structures
DESCRIBE users;
DESCRIBE library_items;
DESCRIBE reservations;

