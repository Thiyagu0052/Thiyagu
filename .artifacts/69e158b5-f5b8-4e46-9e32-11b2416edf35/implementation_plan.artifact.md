# Data Storage Analysis and De-hardcoding Plan

This plan details the analysis of data storage in the Silver ERP project and the steps to remove hardcoded data to make the application fully dynamic.

## Data Storage Analysis

### Local Storage
- **Room Database**: The primary local storage.
  - **Database Name**: `silver_erp_database`
  - **Entities**:
    - `Shop`: Stores shop profile information.
    - `Product`: Stores product inventory items.
    - `Transaction`: Stores the ledger of deliveries and returns.
  - **DAO Classes**: `ShopDao`, `ProductDao`, `TransactionDao`.

### Remote Synchronization
- **Firebase Firestore**: Used for real-time data sync across multiple devices.
  - Collections: `shops`, `products`, `transactions`.
- **Firebase Storage**: Used for hosting proof-of-delivery/return images.
- **Supabase**: Used as a secondary/redundant sync manager for records and image storage.

## Hardcoded Data Identified

### 1. Business Data (Transactions & Shops)
- **Location**: `SilverRepository.kt` -> `resetToUserLedger()`
- **Details**: Hardcodes "Sri Raja Jewellers" and three specific transactions for initialization.
- **Location**: `SilverViewModel.kt` -> `init` block
- **Details**: Automatically calls `resetToUserLedger()` when the local database is empty.

### 2. Infrastructure Configuration
- **Location**: `SupabaseSyncManager.kt`
- **Details**: Hardcoded `SUPABASE_URL` and `SUPABASE_KEY`.
- **Location**: `FirebaseSyncManager.kt`
- **Details**: Hardcoded `STORAGE_BUCKET_URL`.

## Proposed Changes

### [Component] Data Repository & ViewModel

#### [MODIFY] [SilverRepository.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/repository/SilverRepository.kt)
- Remove the `resetToUserLedger()` method.
- Clean up any logic that depends on these hardcoded records.

#### [MODIFY] [SilverViewModel.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/viewmodel/SilverViewModel.kt)
- Remove the call to `repository.resetToUserLedger()` in the `init` block.
- Instead of inserting mock data, ensure the app attempts to sync from remote sources if the local DB is empty.

### [Component] Remote Infrastructure (Optional/Best Practice)

#### [MODIFY] [SupabaseSyncManager.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/remote/SupabaseSyncManager.kt)
#### [MODIFY] [FirebaseSyncManager.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/remote/FirebaseSyncManager.kt)
- Move API keys and URLs to a more secure/configurable location (e.g., `BuildConfig` or a dedicated config file) if required, though for now, removing mock business data is the priority.

## Verification Plan

### Manual Verification
1. **Empty State Check**: Clear the app data and verify that the app starts with an empty dashboard instead of the "Sri Raja Jewellers" mock data.
2. **Data Entry**: Add a new shop and transaction manually to ensure the dynamic flow works correctly.
3. **Sync Check**: Verify that newly added data is correctly synced to Firebase/Supabase.
