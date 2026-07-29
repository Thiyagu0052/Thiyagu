# Add Date Range & Time Selection to Reports

Add functionality to filter reports by date range, include time in transactions, and share only the selected data sorted chronologically (oldest to newest).

## User Review Required

> [!IMPORTANT]
> - Adding a `time` field to the `Transaction` model will trigger a database schema change. The project uses `fallbackToDestructiveMigration()`, so local data might be lost unless a migration is added. I will increment the database version.
> - The date range picker and time picker will use Material 3 components, requiring `ExperimentalMaterial3Api`.

## Proposed Changes

### Data Model & Database

#### [MODIFY] [Models.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/model/Models.kt)
- Add `val time: String` to `Transaction` data class (defaulting to current time).

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/local/AppDatabase.kt)
- Increment database version to 2.

### UI Components

#### [MODIFY] [TransactionDialog.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/components/TransactionDialog.kt)
- Add `time` selection using `TimePicker`.
- Update `onSave` callback to include the `time` string.

#### [MODIFY] [TransactionCardItem.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/components/TransactionCardItem.kt)
- Display both `date` and `time` in the transaction card.

### ViewModels & Logic

#### [MODIFY] [SilverViewModel.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/viewmodel/SilverViewModel.kt)
- Update `addTransaction` and `updateTransaction` methods to handle the new `time` field.

#### [MODIFY] [ReportsScreen.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/screens/reports/ReportsScreen.kt)
- Add state for `startDate`, `endDate`, and `showDateRangePicker`.
- Implement `DateRangePicker` filtering logic.
- **Sorting**: Change `filteredTransactions` sorting to chronological (oldest to newest) based on both `date` and `time`.
- Add a UI button/field to trigger the Date Range Picker.

### Report Utilities

#### [MODIFY] [ReportSharingUtils.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/util/ReportSharingUtils.kt)
- Add `dateRange: String` parameter to sharing methods.
- Include `time` in the transaction lists for Text, Image, and PDF reports.
- Ensure transactions are sorted chronologically (Ascending) in the reports.

## Verification Plan

### Manual Verification
- **Create Transaction**: Add a new transaction and verify you can pick both date and time.
- **Edit Transaction**: Verify the time is correctly loaded and can be updated.
- **Reports List**: Open reports, select a date range, and verify transactions are listed from oldest to newest.
- **Share Report**: Share as Text, Image, and PDF. Verify the header shows the date range and each entry shows the time.
