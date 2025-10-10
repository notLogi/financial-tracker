package com.pluralsight;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {
    private LocalDate date;
    private LocalDateTime time;
    private String description;
    private String vendor;
    private double amount;

    public LocalDate getDate(){
        return date;
    }
    public void setDate(LocalDate date){
        this.date = date;
    }
    public LocalDateTime getTime(){
        return time;
    }
    public void setTime(LocalDateTime time){
        this.time = time;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getVendor(){
        return vendor;
    }
    public void setVendor(String vendor){
        this.vendor = vendor;
    }
    public double getAmount(){
        return amount;
    }
    public void setAmount(double amount){
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", time=" + time +
                ", description='" + description + '\'' +
                ", vendor='" + vendor + '\'' +
                ", amount=" + amount +
                '}';
    }
}
