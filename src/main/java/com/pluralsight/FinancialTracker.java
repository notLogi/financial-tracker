package com.pluralsight;

import java.io.*;
import java.nio.Buffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Predicate;

/*
 * Capstone skeleton – personal finance tracker.
 * ------------------------------------------------
 * File format  (pipe-delimited)
 *     yyyy-MM-dd|HH:mm:ss|description|vendor|amount
 * A deposit has a positive amount; a payment is stored
 * as a negative amount.
 */
public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))){
            sortTransactions();
            for(Transaction transaction : transactions){
                LocalTime time = transaction.getTime();
                String formattedTime = time.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
                writer.write(transaction.getDate() +  "|" +  formattedTime + "|" + transaction.getDescription() + "|" +  transaction.getVendor() + "|" + String.format("%.2f",transaction.getAmount()) + "\n");
            }
        }
        catch (IOException e) {
            System.out.println("File update unsuccessful");
        }
        scanner.close();
    }



    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */

    /**
     * Load transactions from FILE_NAME.
     * • If the file doesn’t exist, create an empty one so that future writes succeed.
     * • Each line looks like: date|time|description|vendor|amount
     */
    public static void loadTransactions(String fileName) {
        try(BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String input;
            while((input = reader.readLine()) != null){
                String[] token = input.split("\\|");
                LocalDate date = LocalDate.parse(token[0], DATE_FMT);
                LocalTime time = LocalTime.parse(token[1], TIME_FMT);
                String description = token[2];
                String vendor = token[3];
                double amount = Double.parseDouble(token[4]);
                transactions.add(new Transaction(date, time, description, vendor, amount));
                sortTransactions();
            }
        }
        catch(IOException ex){
            System.err.println("File does not exist");
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
                writer.flush();
                System.out.println("File created since there is no file existing.\n\n");
            } catch (IOException e) {
                System.err.println("Error creating file");
            }
        }
    }


    //sorts all transactions by date
    private static void sortTransactions(){
        Comparator<Transaction> dateComparator = Comparator.comparing(Transaction::getDate);
        transactions.sort(dateComparator);
    }

    /**
     * Prompt for ONE date+time string in the format
     * "yyyy-MM-dd HH:mm:ss", plus description, vendor, amount.
     * Validate that the amount entered is positive.
     * Store the amount as-is (positive) and append to the file.
     */
    private static void addDeposit(Scanner scanner) {
        try{
            System.out.println("Enter your information for a deposit: \nType \"X\" to return\n");
            System.out.println("Date and time(yyyy-MM-dd HH:mm:ss format): ");
            String dateAndTime = scanner.nextLine();
            if(dateAndTime.equalsIgnoreCase("X")) return;
            System.out.println("Description: ");
            String description = scanner.nextLine();
            System.out.println("Vendor: ");
            String vendor = scanner.nextLine();
            System.out.println("Amount: ");
            double amount = scanner.nextDouble();
            scanner.nextLine();
            if(amount <= 0) {
                System.out.println("The amount you entered is negative");
                return;
            }
            String[] dateTimeSplit = dateAndTime.split(" ");
            LocalDate date = parseDate(dateTimeSplit[0]);
            LocalTime time = LocalTime.parse(dateTimeSplit[1], TIME_FMT);

            transactions.add(new Transaction(date, time, description, vendor, amount));
            sortTransactions();
            System.out.println("Deposit successful!");
        }
        catch (Exception e) {
            System.err.println("Your date/time input is not correct or out of bound.");
        }
    }

    /**
     * Same prompts as addDeposit.
     * Amount must be entered as a positive number,
     * then converted to a negative amount before storing.
     */
    private static void addPayment(Scanner scanner) {
        try{
            System.out.println("Enter your information for a payment: \nType \"X\" to return\n");
            System.out.println("Date and time(yyyy-MM-dd HH:mm:ss format): ");
            String dateAndTime = scanner.nextLine();
            if(dateAndTime.equalsIgnoreCase("X")) return;
            System.out.println("Description: ");
            String description = scanner.nextLine();
            System.out.println("Vendor: ");
            String vendor = scanner.nextLine();
            System.out.println("Amount you want to pay(Positive number): ");
            double amount = scanner.nextDouble();
            scanner.nextLine();
            if(amount <= 0) {
                System.out.println("The amount you entered is negative");
                return;
            }

            String[] dateTimeSplit = dateAndTime.split(" ");
            LocalDate date = LocalDate.parse(dateTimeSplit[0], DATE_FMT);
            LocalTime time = LocalTime.parse(dateTimeSplit[1], TIME_FMT);

            transactions.add(new Transaction(date, time, description, vendor, -(amount)));
            sortTransactions();
            System.out.println("Payment successful!");
        }
        catch (Exception e) {
            System.err.println("Your date/time input is not correct or out of bound.");
        }
    }

    /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Display helpers: show data in neat columns
       ------------------------------------------------------------------ */
    private static void displayLedger() {
        for(Transaction transaction : transactions){
            System.out.print(transaction.toString());
        }
    }

    private static void displayDeposits() {
        filteredTransactions("You have no deposits made.", transaction -> transaction.getAmount() > 0);
    }

    private static void displayPayments() {
        filteredTransactions("You have no payments made.", transaction -> transaction.getAmount() < 0);
    }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Reports");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> monthToDate();
                case "2" -> checkPreviousMonth();
                case "3" -> yearToDate();
                case "4" -> checkPreviousYear();
                case "5" -> checkVendorTrans(scanner);
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Reports helper methods
    ------------------------------------------------------------------ */

    private static void monthToDate(){
        LocalDate date = LocalDate.now();
        filteredTransactions("No transactions made this month", transaction -> date.getYear() == transaction.getDate().getYear() && date.getMonthValue() == transaction.getDate().getMonthValue());
    }

    private static void checkPreviousMonth(){
        LocalDate date = LocalDate.now();
        filteredTransactions("No transactions made last month", transaction -> date.getYear() == transaction.getDate().getYear() && date.getMonthValue() - 1 == transaction.getDate().getMonthValue());
    }

    private static void yearToDate(){
        LocalDate date = LocalDate.now();
        filteredTransactions("No transactions this year were made.", transaction -> date.getYear() == transaction.getDate().getYear());
    }

    private static void checkPreviousYear(){
        LocalDate date = LocalDate.now();
        filteredTransactions("No transactions made this month", transaction -> date.getYear() - 1 == transaction.getDate().getYear());
    }

    private static void checkVendorTrans(Scanner scanner){
        System.out.println("Enter the vendor name: ");
        String vendor = scanner.nextLine();
        filteredTransactions("No vendors matched any transactions", transaction -> vendor.equalsIgnoreCase(transaction.getVendor()));
    }

    private static void filteredTransactions(String message, Predicate<Transaction> predicate){
        boolean found = false;
        for(Transaction transaction : transactions){
            if(predicate.test(transaction)){
                System.out.println(transaction.toString());
                found = true;
            }
        }
        if(!found) System.out.println(message);
    }



    //helper functions for custom search
    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {

    }

    private static void filterTransactionsByVendor(String vendor) {

    }

    private static void customSearch(Scanner scanner) {
        System.out.println("Enter the action: ");
        int userInput = scanner.nextInt();
        scanner.nextLine();
        boolean didExit = false;
        System.out.println("Enter your custom search: ");
        System.out.println("Enter start date:(yyyy-MM-dd) ");
        String startDate = scanner.nextLine();
        System.out.println("Enter end date:(yyyy-MM-dd) ");
        String endDate = scanner.nextLine();
        LocalDate startDateParsed = parseDate(startDate);
        LocalDate endDateParsed = LocalDate.parse(endDate);
        filterTransactionsByDate(startDateParsed, endDateParsed);
        System.out.println("Enter description: ");
        String description = scanner.nextLine();
        System.out.println("Enter vendor: ");
        String vendor = scanner.nextLine();
        System.out.println("Enter the amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        try{
            return LocalDate.parse(s, DATE_FMT);
        } catch (Exception e) {
            System.err.println("Invalid date format");
            return null;
        }
    }
}