# Implementation Plan - Auto Backup Data

Implement a daily automatic backup feature that saves app data (Shops, Products, Transactions) as a JSON file every morning.

## User Review Required

> [!IMPORTANT]
> The backup will be stored in the app's external files directory: `/Android/data/com.kkySilver.erp/files/backups/`. This location is accessible via a file manager but will be deleted if the app is uninstalled.

> [!NOTE]
> I will schedule the backup to run daily at 8:00 AM. If the device is off at that time, WorkManager will run it as soon as the device is available.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/gradle/libs.versions.toml)
- Add `androidx-work-runtime-ktx` dependency.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/build.gradle.kts)
- Include the WorkManager library.

### Worker & Utilities

#### [NEW] [BackupWorker.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/worker/BackupWorker.kt)
- Create a `CoroutineWorker` that:
    1. Fetches all shops, products, and transactions from `SilverRepository`.
    2. Serializes the data into a `BackupData` object.
    3. Converts the object to a JSON string using Moshi.
    4. Saves the JSON string to a file with the naming format `backup_ddMMyyyy_hhmmAM.json`.

#### [NEW] [BackupScheduler.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/util/BackupScheduler.kt)
- Add logic to schedule a `PeriodicWorkRequest` (24-hour interval).
- Calculate the initial delay to target 8:00 AM daily.

### Integration

#### [MODIFY] [MainActivity.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/MainActivity.kt)
- Initialize the backup scheduler in `onCreate`.

## Verification Plan

### Automated Tests
- I will create a unit test or a scratch script to verify the filename generation logic.
- I will verify the JSON serialization using the existing `BackupData` model.

### Manual Verification
- Deploy the app and trigger a one-time backup to verify the file is created correctly in the expected directory.
- Check Logcat for "Backup success" messages.
