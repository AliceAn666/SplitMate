# SplitMate

SplitMate is a Java console application that helps people record shared expenses, calculate personal shares, and settle payments.

## Team

Team name: Academic Inmates  
Members: Lianqiu Qiu (Iris) and Alice An  
Course: CIS 35A

## Main Features

- Add and view participants.
- Record expenses, dates, and payers.
- Split expenses among all or selected participants.
- Split individual and shared food items.
- Calculate personal shares of tax, tip, and discount.
- View Paid and Owed totals.
- Suggest transfers to settle balances.
- Save and load data.

## Main Menu

1. Add participant
2. View participants
3. Add expense
4. View expenses
5. View payments
6. Settle payments
7. Save data
8. Load data
0. Exit

## How to Use Split by Items

First, add participants.
Choose Add expense and enter an expense name. Then choose Split by items.
For each item, enter its name, price, and the IDs of the people sharing it. Use commas between IDs, such as 1,2.
Choose whether the item is taxable.
After entering all items, enter the receipt tax, tip, and discount. Enter 0 if there is no tax, tip, or discount.
Tax is shared based on taxable food amounts.
Tip is shared equally among people included in the meal.
Discount is shared based on food amounts.
Personal owed amount = Food + Tax + Tip - Discount
Enter the date and payer name to add the expense.
Use View payments to check totals.
Use Settle payments to see transfer suggestions.

## Amount Precision

SplitMate uses whole cents for bill calculations to avoid differences caused by rounding.
When an amount cannot be divided equally, the remaining cents are given to participants in order.
Personal shares and settlement amounts are displayed with two decimal places.
The total amount owed by all participants matches the receipt total.

## Example

Iris pays for a meal.
Iris and Alice share a taxable cake costing $11.09.
Alice also has a tax-exempt fruit item costing $2.00.

Receipt tax: $1.11  
Tip: $3.01  
Discount: $1.00

Receipt total: $16.21

Iris owes $7.19.  
Alice owes $9.02.

With only this expense recorded, Alice pays Iris $9.02.

## Save and Load Data

Choose Save data to save participants and expenses to `splitmate.txt`.
Choose Load data to restore saved participants and expenses.
The current save format is SplitMate3. The program also supports loading older SplitMate1 and SplitMate2 files.

## Project Files

- SplitMate.java manages the main menu.
- Participant.java stores participant information.
- Expense.java stores expense information and personal shares.
- ExpenseManager.java handles expense input and display.
- BillCalculator.java performs bill calculations.
- PaymentTracker.java calculates Paid and Owed totals.
- Settlement.java suggests final transfers.
- DataManager.java saves and loads data.