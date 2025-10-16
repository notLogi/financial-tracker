package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);

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
            for(Transaction transaction : transactions){
                LocalTime time = transaction.getTime();
                String formattedTime = time.format(TIME_FMT);
                writer.write(transaction.getDate() +  "|" +  formattedTime + "|" + transaction.getDescription() + "|" +  transaction.getVendor() + "|" + transaction.getAmount() + "\n");
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
                LocalDate date = parseDate(token[0]);
                LocalTime time = parseTime(token[1]);
                String description = capitalizeFirst(token[2]);
                String vendor = capitalizeFirst(token[3]);
                Double convertedAmount = parseDouble(token[4]);
                if(convertedAmount != null){
                    transactions.add(new Transaction(date, time, description, vendor, convertedAmount));
                }
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

    /**
     * Prompt for ONE date+time string in the format
     * "yyyy-MM-dd HH:mm:ss", plus description, vendor, amount.
     * Validate that the amount entered is positive.
     * Store the amount as-is (positive) and append to the file.
     */
    private static void addDeposit(Scanner scanner) {
        try{
            recordTransaction("deposit", scanner);
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
            recordTransaction("payment", scanner);
        }
        catch (Exception e) {
            System.err.println("Your date/time input is not correct or out of bound.");
        }
    }
    //adding a transaction helper method.
    private static void recordTransaction(String type, Scanner scanner){
        System.out.println("Enter your information for a " + type + " : \nType \"X\" to return\n");
        System.out.println("Date and time(yyyy-MM-dd HH:mm:ss format): ");
        String dateAndTime = scanner.nextLine().trim();
        if(dateAndTime.equalsIgnoreCase("X")) return;
        System.out.println("Description: ");
        String description = scanner.nextLine().trim();
        System.out.println("Vendor: ");
        String vendor = scanner.nextLine().trim();
        System.out.println("Amount you want to pay(Positive number): ");
        String amount = scanner.nextLine().trim();
        Double convertedAmount = parseDouble(amount);
        if(convertedAmount != null && convertedAmount <= 0) {
            System.err.println("The amount you entered is negative");
            return;
        }
        String[] dateTimeSplit = dateAndTime.split(" ");
        LocalDate date = parseDate(dateTimeSplit[0]);
        LocalTime time = parseTime(dateTimeSplit[1]);
        if(convertedAmount == null || date == null || time == null || description.isEmpty() || vendor.isEmpty()) {
            System.out.println("You did not fill in the vendor/description");
            return;
        }
        double finalAmount = type.equalsIgnoreCase("deposit") ? convertedAmount : -(convertedAmount);
        transactions.add(new Transaction(date, time, capitalizeFirst(description), capitalizeFirst(vendor), finalAmount));
        System.out.println(type + " successful!");
        sortTransactions();
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
        filteredTransactions("You have no transactions recorded.", transaction -> true);
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
        filteredTransactions("No transactions made this month", transaction -> date.getYear() == transaction.getDate().getYear() && date.getMonthValue() == transaction.getDate().getMonthValue() && date.getDayOfMonth() >= transaction.getDate().getDayOfMonth());
    }

    private static void checkPreviousMonth(){
        LocalDate date = LocalDate.now();
        filteredTransactions("No transactions made last month", transaction -> date.getYear() == transaction.getDate().getYear() && date.getMonthValue() - 1 == transaction.getDate().getMonthValue());
    }

    private static void yearToDate(){
        LocalDate date = LocalDate.now();
        filteredTransactions("No transactions this year were made.", transaction -> date.getYear() == transaction.getDate().getYear() && date.getDayOfYear() >= transaction.getDate().getDayOfYear());
    }

    private static void checkPreviousYear(){
        LocalDate date = LocalDate.now();
        filteredTransactions("No transactions made this month", transaction -> date.getYear() - 1 == transaction.getDate().getYear());
    }

    private static void checkVendorTrans(Scanner scanner){
        System.out.println("Enter the vendor name: ");
        String vendor = scanner.nextLine().trim();
        filteredTransactions("No vendors matched any transactions", transaction -> vendor.equalsIgnoreCase(transaction.getVendor()));
    }

    /**
     *
     * @param message is the
     * @param predicate can be thought of a "subpackage". It is part of java. It allows you to pass a condition as an argument. With that, for every primitive/nonprimitive
     *                  type you want to check with a condition, you do predicate.test(variable). This returns a boolean true or false.
     *                  You can also have 2 predicates and can check and/or by condition1.and(condition2).test(value)
     */
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

    /* ------------------------------------------------------------------
       custom search methods/menu
       ------------------------------------------------------------------ */

    private static void filterTransactionsByDate(LocalDate start, LocalDate end, ArrayList<Transaction> filteredList) {
        customSearchFilter(transaction -> (start == null || !transaction.getDate().isBefore(start)) && (end == null || !transaction.getDate().isAfter(end)), filteredList);
    }

    private static void filterTransactionsByDescription(String description, ArrayList<Transaction> filteredList) {
        customSearchFilter(transaction -> description.equalsIgnoreCase(transaction.getDescription()), filteredList);
    }

    private static void filterTransactionsByVendor(String vendor, ArrayList<Transaction> filteredList) {
        customSearchFilter(transaction -> vendor.equalsIgnoreCase(transaction.getVendor()), filteredList);
    }

    private static void filterTransactionsByAmount(String lowestAmount, String highestAmount, ArrayList<Transaction> filteredList){
        Double lowestConvertedAmount = parseDouble(lowestAmount);
        Double highestConvertedAmount = parseDouble(highestAmount);

        if(lowestConvertedAmount != null && highestConvertedAmount != null){
            customSearchFilter(transaction -> lowestConvertedAmount <= Math.abs(transaction.getAmount()) && highestConvertedAmount >= Math.abs(transaction.getAmount()), filteredList);
        }
        else if(lowestConvertedAmount != null){
            customSearchFilter(transaction -> lowestConvertedAmount <= transaction.getAmount(), filteredList);
        }
        else if(highestConvertedAmount != null){
            customSearchFilter(transaction -> highestConvertedAmount >= transaction.getAmount(), filteredList);
        }
    }

    /**
     *
    removeIf is "another" way of removing elements from an arrayList. The logic here is since it is iterating through the list and removing it, some elements might be skipped.
     IntelliJ suggested this change. You can also grab the negation(!) by writing negate() before test(value)
     */
    private static void customSearchFilter(Predicate<Transaction> predicate, ArrayList<Transaction> filteredList){
        if(!filteredList.isEmpty()){
            filteredList.removeIf(transaction -> predicate.negate().test(transaction));
            return;
        }
        for(Transaction transaction : transactions){
            if(predicate.test(transaction)){
                filteredList.add(transaction);
            }
        }
    }

    /**
     *
     * custom search menu
     */
    private static void customSearch(Scanner scanner) {
        ArrayList<Transaction> filteredList = new ArrayList<>();
        System.out.println("Enter your custom search: ");
        System.out.println("Enter start date(yyyy-MM-dd)(Optional): ");
        System.out.println("Type X to exit");
        String startDate = scanner.nextLine().trim();
        if(startDate.equalsIgnoreCase("x")) return;
        System.out.println("Enter end date(yyyy-MM-dd)(Optional): ");
        String endDate = scanner.nextLine().trim();

        LocalDate startDateParsed = parseDate(startDate);
        LocalDate endDateParsed = parseDate(endDate);
        if(startDateParsed != null || endDateParsed != null) filterTransactionsByDate(startDateParsed, endDateParsed, filteredList);

        System.out.println("Enter description(Optional): ");
        String description = scanner.nextLine().trim();
        System.out.println("Enter vendor(Optional): ");
        String vendor = scanner.nextLine().trim();
        System.out.println("Enter the lowest amount(Optional): ");
        String lowestAmount = scanner.nextLine().trim();
        System.out.println("Enter the highest amount(Optional): ");
        String highestAmount = scanner.nextLine().trim();
        if(!description.isEmpty()) filterTransactionsByDescription(description, filteredList);
        if(!vendor.isEmpty()) filterTransactionsByVendor(vendor, filteredList);
        if(!lowestAmount.isEmpty() || !highestAmount.isEmpty()) filterTransactionsByAmount(lowestAmount, highestAmount, filteredList);
        if(!filteredList.isEmpty()){
            for(Transaction t : filteredList){
                System.out.println(t.toString());
            }
        }
        else{
            System.out.println("No transactions matched your conditions");
        }
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        try{
            return LocalDate.parse(s, DATE_FMT);
        } catch (Exception e) {
            if(!s.isEmpty()){
                System.err.println("Invalid date format");
            }
            return null;
        }
    }
    private static LocalTime parseTime(String s) {
        try{
            return LocalTime.parse(s, TIME_FMT);
        } catch (Exception e) {
            if(!s.isEmpty()){
                System.err.println("Invalid time format");
            }
            return null;
        }
    }

    private static Double parseDouble(String s) {
        try {
            return Math.round(Double.parseDouble(s) * 100.0) / 100.0;
        }
        catch(Exception ex){
            System.err.println("Invalid number");
            return null;
        }
    }

    //capitalize the first letter of a word/sentence
    private static String capitalizeFirst(String text){
        if(text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    //sorts all transactions by date
    private static void sortTransactions(){
        Comparator<Transaction> dateComparator = Comparator.comparing(Transaction::getDate);
        transactions.sort(dateComparator);
    }
}