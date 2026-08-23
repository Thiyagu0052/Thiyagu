# Implementation Plan - Add Running Balance (Hold) to Reports

The user wants a "Hold" column in the report for each transaction entry, similar to a bank statement balance column. This column should show the cumulative balance (Deliveries - Returns) at the point of each transaction.

## User Review Required

> [!IMPORTANT]
> The running balance will be calculated based on **all transactions** for the selected shop to ensure it represents the true balance at that point in time (like a real bank statement), even if a date range filter is applied.

> [!NOTE]
> If "All Shops" is selected, the running balance will reflect the global business hold (total deliveries minus total returns across all shops).

## Proposed Changes

### Core Logic & Data Models

#### [MODIFY] [ReportsScreen.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/screens/reports/ReportsScreen.kt)
- Create a local helper data class `TransactionWithBalance`.
- Update the filtering and sorting logic:
    1. Filter transactions by shop.
    2. Sort by date and time.
    3. Calculate running balance for each.
    4. Filter the list again by the selected date range for display.
- Pass the calculated balance to `TransactionCardItem`.

### UI Components

#### [MODIFY] [TransactionCardItem.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/components/TransactionCardItem.kt)
- Add an optional `balance: Double?` parameter.
- Display the balance value prominently at the end of the transaction header or as a separate label.
- Format the value with "g Hold" or similar to match the app's style.

### Export & Sharing

#### [MODIFY] [ReportSharingUtils.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/util/ReportSharingUtils.kt)
- Update `shareTextReport` to add a "Hold(g)" column to the ASCII table.
- Update `shareImageReport` to add a "Hold(g)" column to the generated image table.
- Update `sharePdfReport` to add a "Hold(g)" column to the PDF statement table.

## Verification Plan

### Automated Tests
- I will verify the running balance calculation logic manually by checking different transaction sequences.

### Manual Verification
1. Open the Reports screen.
2. Select a specific shop.
3. Verify that the last column (or designated area) in each transaction card shows a cumulative balance.
4. Apply a date range filter and ensure the balance for the first visible transaction correctly includes all previous (hidden) transactions.
5. Share the report as Text, Image, and PDF and verify the "Hold" column is present in all.
