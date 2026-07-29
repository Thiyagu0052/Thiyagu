# Walkthrough - Export/Import Feature and Settings Cleanup

I have successfully implemented the local data Export/Import feature and cleaned up the Settings screen by removing the sample data and reference cards.

## Changes Made

### 1. Data Models and Serialization
- Added `@JsonClass(generateAdapter = true)` to `Shop`, `Product`, and `Transaction` models in [Models.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/model/Models.kt).
- Created a `BackupData` wrapper class to handle full database state serialization.
- Integrated **Moshi** with `KotlinJsonAdapterFactory` for robust JSON processing.

### 2. ViewModel Logic
- Implemented `exportData` and `importData` in [SilverViewModel.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/viewmodel/SilverViewModel.kt).
- `exportData`: Serializes all local data to a JSON stream.
- `importData`: Deserializes a JSON stream, clears the current database, and restores all records.

### 3. Settings UI Cleanup
- Removed the **Pure Weight Formula Reference Card** to declutter the interface.
- Removed the **Add Sample Data** section as the app is moving towards production use.
- Added a new **Backup & Restore (காப்புப்பிரதி மற்றும் மீட்டமைப்பு)** section in [SettingsScreen.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt).

### 4. File Interaction (SAF)
- Integrated Android **Storage Access Framework** in [AppNavHost.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/navigation/AppNavHost.kt) using `ActivityResultLauncher`.
- **Export**: Users can choose where to save the `silver_erp_backup.json` file.
- **Import**: Users can select a JSON file to restore their data.
- Added localized **Toasts** to notify users of success or failure.

## Verification Results

### Build Status
- [x] Gradle build successful (`app:assembleDebug`).

### Manual Verification Steps
1. Navigate to **அமைப்புகள் (Settings)**.
2. Confirm the **காப்புப்பிரதி மற்றும் மீட்டமைப்பு** card is visible.
3. Tap **ஏற்றுமதி (Export)** and save the file.
4. Tap **இறக்குமதி (Import)** and select the saved file.
5. Verify the success message appears in Tamil.

> [!TIP]
> Always keep a backup file before performing an import, as the import process replaces all existing local data with the contents of the file.
