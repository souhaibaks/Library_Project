package com.library.models;

import java.time.LocalDate;

public class Book extends LibraryItem {
    private int numberOfPages;
    private String genre;
    private String publisher;
    
    public Book() {
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
        return numberOfPages;
    }
    
    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
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

