# Walkthrough - Running Balance (Hold) in Reports

I have successfully implemented the running balance feature in the reports section. Each transaction entry now displays its cumulative balance, similar to a bank statement.

## Changes Made

### Data Layer
- Added [TransactionWithBalance](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/model/Models.kt) to the data models to store the calculated balance at each point in time.

### UI Enhancements
- Updated [ReportsScreen](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/screens/reports/ReportsScreen.kt) to calculate the running balance for all transactions belonging to a shop (or all shops if no filter is applied) before applying date range filters. This ensures the balance is always historically accurate.
- Modified [TransactionCardItem](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/components/TransactionCardItem.kt) to display the "Hold" balance in the transaction list using a distinct color (`HoldAmber`).

### Export & Sharing
- Updated [ReportSharingUtils](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/util/ReportSharingUtils.kt) to include the new "Hold" or "Balance" column in all export formats:
    - **Text**: Added a new column to the ASCII table.
    - **Image**: Expanded the image width and added a new column to the report table.
    - **PDF**: Adjusted column widths to fit a new "Balance(g)" column in the statement.

## Verification Results

### UI Verification
- The transaction list in the Reports screen now shows a "g இருப்பு" (Hold) value for each item.
- Selecting a specific shop correctly calculates the running balance for that shop's history.

### Export Verification
- Text report sharing now includes a "இருப்பு(g)" column.
- PDF statement sharing now includes a "Balance(g)" column with properly aligned values.
- Image report sharing now includes an "இருப்பு(g)" column with a slightly wider canvas for better readability.
