package com.library.models;

import java.time.LocalDate;

/**
 * Represents a concrete book with metadata specific to books (pages, genre, publisher).
 */
public class Book extends LibraryItem {
    private int numberOfPages; // total count of pages
    private String genre;      // literary category
    private String publisher;  // publishing house name
    
    public Book() {
        // Default constructor keeps compatibility with frameworks that require it
        super();
    }
    
    public Book(int id, String title, String author, String isbn, LocalDate publicationDate,
                int numberOfPages, String genre, String publisher) {
        super(id, title, author, isbn, publicationDate);
        this.numberOfPages = numberOfPages;
        this.genre = genre;
        this.publisher = publisher;
    }
    
    // Getters and Setters
    public int getNumberOfPages() {
        // Exposes total page count
        return numberOfPages;
    }
    
    public void setNumberOfPages(int numberOfPages) {
        // Adjusts page count metadata
        this.numberOfPages = numberOfPages;
    }
    
    public String getGenre() {
        // Returns the genre label
        return genre;
    }
    
    public void setGenre(String genre) {
        // Updates the genre label
        this.genre = genre;
    }
    
    public String getPublisher() {
        // Returns the publisher name
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        // Updates the publisher name
        this.publisher = publisher;
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", numberOfPages=" + numberOfPages +
                ", genre='" + genre + '\'' +
                ", publisher='" + publisher + '\'' +
                ", isAvailable=" + isAvailable +
                ", publicationDate=" + publicationDate +
                '}';
    }
}
