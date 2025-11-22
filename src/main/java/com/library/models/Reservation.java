package com.library.models;

import java.time.LocalDate;

public class Reservation {
    private int id;
    private User user;
    private LibraryItem item;
    private LocalDate reservationDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private ReservationStatus status;
    
    public enum ReservationStatus {
        ACTIVE,
        RETURNED,
        OVERDUE,
        CANCELLED
    }
    
    public Reservation() {
        this.reservationDate = LocalDate.now();
        this.status = ReservationStatus.ACTIVE;
    }
    
    public Reservation(int id, User user, LibraryItem item, LocalDate dueDate) {
        this.id = id;
        this.user = user;
        this.item = item;
        this.reservationDate = LocalDate.now();
        this.dueDate = dueDate;
        this.status = ReservationStatus.ACTIVE;
    }
    
    public Reservation(int id, User user, LibraryItem item, LocalDate reservationDate, 
                       LocalDate dueDate, LocalDate returnDate, ReservationStatus status) {
        this.id = id;
        this.user = user;
        this.item = item;
        this.reservationDate = reservationDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LibraryItem getItem() {
        return item;
    }
    
    public void setItem(LibraryItem item) {
        this.item = item;
    }
    
    public LocalDate getReservationDate() {
        return reservationDate;
    }
    
    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public ReservationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
    
    public boolean isOverdue() {
        return status == ReservationStatus.ACTIVE && 
               dueDate != null && 
               LocalDate.now().isAfter(dueDate);
    }
    
    public void markAsReturned() {
        this.returnDate = LocalDate.now();
        this.status = ReservationStatus.RETURNED;
        if (this.item != null) {
            this.item.setAvailable(true);
        }
    }
    
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", user=" + (user != null ? user.getFullName() : "null") +
                ", item=" + (item != null ? item.getTitle() : "null") +
                ", reservationDate=" + reservationDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                '}';
    }
}

