package com.library.services;

import com.library.models.Book;
import com.library.models.LibraryItem;
import com.library.models.LibraryItemDAO;
import com.library.models.Magazine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Service for managing books and magazines
 */
public class BookService {
    private static BookService instance;
    private final LibraryItemDAO itemDAO;
    private final ObservableList<Book> books;
    private final ObservableList<Magazine> magazines;
    
    private BookService() {
        this.itemDAO = new LibraryItemDAO();
        this.books = FXCollections.observableArrayList();
        this.magazines = FXCollections.observableArrayList();
        loadBooksFromDatabase();
        loadMagazinesFromDatabase();
    }
    
    public static synchronized BookService getInstance() {
        if (instance == null) {
            instance = new BookService();
        }
        return instance;
    }
    
    /**
     * Loads all books from the database
     */
    private void loadBooksFromDatabase() {
        List<Book> bookList = itemDAO.getAllBooks();
        books.clear();
        books.addAll(bookList);
    }
    
    /**
     * Loads all magazines from the database
     */
    private void loadMagazinesFromDatabase() {
        List<Magazine> magazineList = itemDAO.getAllMagazines();
        magazines.clear();
        magazines.addAll(magazineList);
    }
    
    /**
     * Gets all books
     */
    public ObservableList<Book> getBooks() {
        return books;
    }
    
    /**
     * Gets all magazines
     */
    public ObservableList<Magazine> getMagazines() {
        return magazines;
    }
    
    /**
     * Gets all library items (books and magazines)
     */
    public ObservableList<LibraryItem> getAllItems() {
        ObservableList<LibraryItem> items = FXCollections.observableArrayList();
        items.addAll(books);
        items.addAll(magazines);
        return items;
    }
    
    /**
     * Adds a new book and saves it to the database
     */
    public boolean addBook(Book book) {
        int id = itemDAO.insertBook(book);
        if (id > 0) {
            // Refresh from database to ensure consistency
            loadBooksFromDatabase();
            return true;
        }
        return false;
    }
    
    /**
     * Adds a new magazine and saves it to the database
     */
    public boolean addMagazine(Magazine magazine) {
        int id = itemDAO.insertMagazine(magazine);
        if (id > 0) {
            // Refresh from database to ensure consistency
            loadMagazinesFromDatabase();
            return true;
        }
        return false;
    }
    
    /**
     * Updates a book in the database
     */
    public boolean updateBook(Book book) {
        if (itemDAO.updateBook(book)) {
            // Refresh the list
            loadBooksFromDatabase();
            return true;
        }
        return false;
    }
    
    /**
     * Gets a book by ID
     */
    public Book getBookById(int id) {
        return itemDAO.getBookById(id);
    }
    
    /**
     * Gets a magazine by ID
     */
    public Magazine getMagazineById(int id) {
        return itemDAO.getMagazineById(id);
    }
    
    /**
     * Gets a library item by ID
     */
    public LibraryItem getItemById(int id) {
        return itemDAO.getItemById(id);
    }
    
    /**
     * Refreshes the book and magazine lists from the database
     */
    public void refresh() {
        loadBooksFromDatabase();
        loadMagazinesFromDatabase();
    }
}

