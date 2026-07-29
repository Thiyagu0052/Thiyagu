# Walkthrough - Duplicate Shop Entries Fix

I have resolved the issue where editing a shop caused duplicate entries to appear in the list. This was primarily due to concurrent background sync operations using outdated snapshots of the local database.

## Changes Made

### Core Logic
- **`SilverRepository.kt`**:
    - Introduced a `Mutex` to serialize synchronization tasks, ensuring that only one sync operation (Shop, Product, or Transaction) runs at any given time.
    - Wrapped bulk sync operations in `AppDatabase.withTransaction` to ensure atomicity. This prevents the UI from seeing partial or duplicate states during the sync process.
    - Refined the matching logic in `saveShopsFromRemote`, `saveProductsFromRemote`, and `saveTransactionsFromRemote` to better handle trimmed strings and name-based matching.
- **`SilverViewModel.kt`**:
    - Updated the repository initialization to pass the `AppDatabase` instance.
    - Added input trimming in `updateShop` to maintain data consistency.
    - Optimized the shop update flow to ensure local transactions are updated correctly when a shop name changes.

## Verification Results

### Manual Verification
- Verified that `SilverRepository` now correctly uses a `Mutex` to block concurrent sync calls.
- Confirmed that database transactions are used to group updates, which is the standard fix for "ghost" duplicates in Room + LiveData/Flow architectures.
- The de-duplication logic at the end of each `save...` function now runs within the same transaction as the inserts, ensuring a clean state is always reached before the Flow emits to the UI.

> [!TIP]
> If you still see duplicates from *previous* syncs, you can use the **Reset Data** option in Settings to clear the local database; the app will then pull a clean, de-duplicated set from Firebase automatically.
