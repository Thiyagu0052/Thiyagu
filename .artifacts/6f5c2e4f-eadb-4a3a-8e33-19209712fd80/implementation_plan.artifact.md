# Fix Duplicate Entries in Shop Editing

The user is experiencing duplicate entries (specifically "6 entries" when they likely had 3) in the shop list after editing. This is caused by a race condition in the Firebase synchronization logic where multiple sync operations run concurrently and use stale snapshots of the local database, leading to redundant inserts.

## User Review Required

> [!IMPORTANT]
> The fix involves serializing synchronization tasks using a `Mutex` to prevent concurrent database writes from Firebase updates. I will also wrap bulk operations in database transactions to ensure UI consistency and atomicity.

## Proposed Changes

### `app` module

#### [MODIFY] [SilverRepository.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/repository/SilverRepository.kt)
- Add a `Mutex` to ensure that `saveShopsFromRemote`, `saveProductsFromRemote`, and `saveTransactionsFromRemote` do not run concurrently.
- Wrap the loop of inserts/updates and the de-duplication logic in a database transaction (if possible, or at least ensure atomicity by using a Mutex and fresh data).
- Refine the matching logic to be more robust against minor data differences.

#### [MODIFY] [SilverViewModel.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/viewmodel/SilverViewModel.kt)
- Optimize `updateShop` to avoid triggering a separate Firebase sync for every single transaction when a shop name changes.
- Ensure that Firebase listeners handle updates more gracefully.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/local/AppDatabase.kt)
- Add a helper function for running code in a transaction if needed.

## Verification Plan

### Automated Tests
- I will check if there are existing tests for sync logic. If not, I'll rely on manual verification as I cannot easily simulate Firebase real-time listeners in this environment without significant boilerplate.

### Manual Verification
- Deploy the app to a device/emulator.
- Add 3 shops.
- Edit one shop.
- Verify that the list still shows only 3 shops (no duplicates).
- Check the "Total Shops" count in the Dashboard.
