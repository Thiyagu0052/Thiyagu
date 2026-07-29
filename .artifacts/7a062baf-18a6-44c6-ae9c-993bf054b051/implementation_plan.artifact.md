# Implementation Plan - Add Export/Import Feature and Clean Up Settings

The goal is to enhance the Settings screen by adding an Export/Import feature for local data and removing the "sample" content that is no longer needed.

## User Review Required

> [!IMPORTANT]
> The Export/Import feature will use the Android Storage Access Framework (SAF). The user will be prompted to select a file location for export and a JSON file for import.

## Proposed Changes

### [Component] Data & ViewModel

#### [MODIFY] [SilverViewModel.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/viewmodel/SilverViewModel.kt)
- Add a `BackupData` data class to represent the full state of the database (Shops, Products, Transactions).
- Implement `exportData(outputStream: OutputStream)` to serialize the current database state to JSON using Moshi.
- Implement `importData(inputStream: InputStream)` to deserialize JSON and replace/merge the current database state.
- Add methods `exportToUri(uri: Uri, context: Context)` and `importFromUri(uri: Uri, context: Context)` to handle IO.

### [Component] UI

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt)
- **Remove**: The "Pure Weight Formula Reference Card" (lines 147-184).
- **Remove**: The "Add Sample Data" section in the "Actions List" card (lines 191-218).
- **Add**: A new "Backup & Restore" card with:
    - **Export Data**: Triggers `ACTION_CREATE_DOCUMENT` to save data as a `.json` file.
    - **Import Data**: Triggers `ACTION_OPEN_DOCUMENT` to pick a `.json` file and restore data.
- Update the `SettingsScreen` signature to include `onExportData` and `onImportData` callbacks.

#### [MODIFY] [AppNavHost.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/navigation/AppNavHost.kt)
- Pass the new `export` and `import` handlers from the `SilverViewModel` to the `SettingsScreen`.
- Implement the `ActivityResultLauncher` logic within `AppNavHost` or `MainActivity` to handle file URI results.

## Verification Plan

### Automated Tests
- N/A (UI changes and IO logic are best verified manually in this context).

### Manual Verification
1. Open Settings screen.
2. Verify "Pure Weight Formula" card and "Sample Data" section are gone.
3. Tap "Export Data", choose a location, and verify a `.json` file is created.
4. Modify some data in the app (add a shop).
5. Tap "Import Data", select the previously exported file.
6. Verify the data is restored correctly.
