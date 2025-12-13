package com.library.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Library Items (Books and Magazines)
 */
public class LibraryItemDAO {
    
    /**
     * Fetches all books from the database
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM library_items WHERE item_type = 'BOOK'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Book book = mapResultSetToBook(rs);
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books: " + e.getMessage());
            e.printStackTrace();
        }
        
        return books;
    }
    
    /**
     * Fetches all magazines from the database
     */
    public List<Magazine> getAllMagazines() {
        List<Magazine> magazines = new ArrayList<>();
        String sql = "SELECT * FROM library_items WHERE item_type = 'MAGAZINE'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Magazine magazine = mapResultSetToMagazine(rs);
                magazines.add(magazine);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching magazines: " + e.getMessage());
            e.printStackTrace();
        }
        
        return magazines;
    }
    
    /**
     * Fetches all library items (books and magazines) from the database
     */
    public List<LibraryItem> getAllItems() {
        List<LibraryItem> items = new ArrayList<>();
        items.addAll(getAllBooks());
        items.addAll(getAllMagazines());
        return items;
    }
    
    /**
     * Inserts a new book into the database
     */
    public int insertBook(Book book) {
        String sql = "INSERT INTO library_items (title, author, isbn, publication_date, is_available, " +
                     "item_type, number_of_pages, genre, publisher) " +
                     "VALUES (?, ?, ?, ?, ?, 'BOOK', ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setDate(4, book.getPublicationDate() != null ? 
                         Date.valueOf(book.getPublicationDate()) : null);
            pstmt.setBoolean(5, book.isAvailable());
            pstmt.setInt(6, book.getNumberOfPages());
            pstmt.setString(7, book.getGenre());
            pstmt.setString(8, book.getPublisher());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        book.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting book: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Inserts a new magazine into the database
     */
    public int insertMagazine(Magazine magazine) {
        String sql = "INSERT INTO library_items (title, author, isbn, publication_date, is_available, " +
                     "item_type, issue_number, publisher, category) " +
                     "VALUES (?, ?, ?, ?, ?, 'MAGAZINE', ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, magazine.getTitle());
            pstmt.setString(2, magazine.getAuthor());
            pstmt.setString(3, magazine.getIsbn());
            pstmt.setDate(4, magazine.getPublicationDate() != null ? 
                         Date.valueOf(magazine.getPublicationDate()) : null);
            pstmt.setBoolean(5, magazine.isAvailable());
            pstmt.setInt(6, magazine.getIssueNumber());
            pstmt.setString(7, magazine.getPublisher());
            pstmt.setString(8, magazine.getCategory());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        magazine.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting magazine: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Updates a book in the database
     */
    public boolean updateBook(Book book) {
        String sql = "UPDATE library_items SET title = ?, author = ?, isbn = ?, publication_date = ?, " +
                     "is_available = ?, number_of_pages = ?, genre = ?, publisher = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setDate(4, book.getPublicationDate() != null ? 
                         Date.valueOf(book.getPublicationDate()) : null);
            pstmt.setBoolean(5, book.isAvailable());
            pstmt.setInt(6, book.getNumberOfPages());
            pstmt.setString(7, book.getGenre());
            pstmt.setString(8, book.getPublisher());
            pstmt.setInt(9, book.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Gets a book by ID
     */
    public Book getBookById(int id) {
        String sql = "SELECT * FROM library_items WHERE id = ? AND item_type = 'BOOK'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching book by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets a magazine by ID
     */
    public Magazine getMagazineById(int id) {
        String sql = "SELECT * FROM library_items WHERE id = ? AND item_type = 'MAGAZINE'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMagazine(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching magazine by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets a library item by ID (can be book or magazine)
     */
    public LibraryItem getItemById(int id) {
        Book book = getBookById(id);
        if (book != null) {
            return book;
        }
        return getMagazineById(id);
    }
    
    /**
     * Maps a ResultSet row to a Book object
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        
        Date pubDate = rs.getDate("publication_date");
        if (pubDate != null) {
            book.setPublicationDate(pubDate.toLocalDate());
        }
        
        book.setAvailable(rs.getBoolean("is_available"));
        book.setNumberOfPages(rs.getInt("number_of_pages"));
        book.setGenre(rs.getString("genre"));
        book.setPublisher(rs.getString("publisher"));
        
        return book;
    }
    
    /**
     * Maps a ResultSet row to a Magazine object
     */
    private Magazine mapResultSetToMagazine(ResultSet rs) throws SQLException {
        Magazine magazine = new Magazine();
        magazine.setId(rs.getInt("id"));
        magazine.setTitle(rs.getString("title"));
        magazine.setAuthor(rs.getString("author"));
        magazine.setIsbn(rs.getString("isbn"));
        
        Date pubDate = rs.getDate("publication_date");
        if (pubDate != null) {
            magazine.setPublicationDate(pubDate.toLocalDate());
        }
        
        magazine.setAvailable(rs.getBoolean("is_available"));
        magazine.setIssueNumber(rs.getInt("issue_number"));
        magazine.setPublisher(rs.getString("publisher"));
        magazine.setCategory(rs.getString("category"));
        
        return magazine;
    }
}

