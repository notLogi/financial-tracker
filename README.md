## Financial Tracker


The Financial Tracker allows you to keep track of your previous transactions and allows the user to add deposits and payments. Transitions are stored onto a CSV file and can be filtered by dates, vendor, and the amount of money.

### Some features include:

**Add Deposits and Payments**
- Users are able to record the date of the deposit/payment, the amount deposited/paid, the vendor, and description of what it was for.
- Validates the user input to ensure correct date/time formats and positive amounts of money.

**Managing ledger**
- Allows the user to see all reports
- Displays transactions that are either payments or deposits.

**Reports**
- Can filter reports by the previous or this month, previous or this year, or by vendor. Can filter reports by the previous or this month, previous or this year, or by vendor.
- Option to custom search by inputting the vendor, amount recorded, description, and the date range.

**Persistent storage**
- Transactions are stored in a CSV file.
- Automatically creates a CSV file if you don't have a transactions file.
  **Sorting**
- Transactions stored are automatically sorted by date for convenience.


## Setup


### Prerequisites

- IntelliJ IDEA: Ensure you have IntelliJ IDEA installed, which you can download from [here](https://www.jetbrains.com/idea/download/).
- Java SDK: Make sure Java SDK is installed and configured in IntelliJ.

### Running the Application in IntelliJ

Follow these steps to get your application running within IntelliJ IDEA:

1. Open IntelliJ IDEA.
2. Select "Open" and navigate to the directory where you cloned or downloaded the project.
3. After the project opens, wait for IntelliJ to index the files and set up the project.
4. Find the main class with the `public static void main(String[] args)` method.
5. Right-click on the file and select 'Run 'FinancialTracker.main()'' to start the application.

## Technologies Used

- Java SDK 17
- IntelliJ IDEA

## Demo
https://github.com/user-attachments/assets/76582b37-0d14-4852-9e66-93cc27f21739


## An Interesting Part of My Code


- One of the interesting parts of my code is how I refactored the helper methods for the reportMenu and LedgerMenu. I looked up at how to pass a condition through the parameter and it taught me about Predicate. Predicate is a functional interface that allows this. I was able to reduce a lot of repetition with this trick.
- Double vs double, where Double is an object. Used to check if the double was a valid number.
## Resources
- https://www.geeksforgeeks.org/java/java-8-predicate-with-examples/

## Contributors:
- Roger Su
