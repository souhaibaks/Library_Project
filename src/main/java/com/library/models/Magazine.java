package com.library.models;

import java.time.LocalDate;

public class Magazine extends LibraryItem {
    private int issueNumber;
    private String publisher;
    private String category;
    
    public Magazine() {
        super();
    }
    
    public Magazine(int id, String title, String author, String isbn, LocalDate publicationDate,
                    int issueNumber, String publisher, String category) {
        super(id, title, author, isbn, publicationDate);
        this.issueNumber = issueNumber;
        this.publisher = publisher;
        this.category = category;
    }
    
    // Getters and Setters
    public int getIssueNumber() {
        return issueNumber;
    }
    
    public void setIssueNumber(int issueNumber) {
        this.issueNumber = issueNumber;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    @Override
    public String toString() {
        return "Magazine{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", issueNumber=" + issueNumber +
                ", publisher='" + publisher + '\'' +
                ", category='" + category + '\'' +
                ", isAvailable=" + isAvailable +
                ", publicationDate=" + publicationDate +
                '}';
    }
}
