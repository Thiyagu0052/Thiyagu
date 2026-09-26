# Fix: Persistent Entries and English Defaults in Sync

The user reported that an "English entry" remains in the list even after manual deletion. Research indicates this is likely due to two issues:
1.  **Additive-only Synchronization:** The `SilverRepository` only handles inserts and updates from Firebase. If an entry is deleted from Firebase (manually or otherwise), the app's local database does not remove it, causing it to persist in the UI.
2.  **Hardcoded English Defaults:** Both `ProductDialog` and `TransactionDialog` fall back to a hardcoded list of English product names (e.g., "Payal", "Anklets") when no products are present in the database. If a user deletes all products, these English entries appear in the UI selection chips.

## User Review Required

> [!IMPORTANT]
> I will update the synchronization logic to ensure that items deleted from Firebase are also removed from the local database. This assumes that Firebase is the source of truth for all synchronized devices.

> [!NOTE]
> I will also translate the hardcoded product presets and categories to Tamil to ensure consistency with the rest of the application's UI.

## Proposed Changes

### Core Synchronization Logic

#### [MODIFY] [SilverRepository.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/repository/SilverRepository.kt)
- Update `saveShopsFromRemote`, `saveProductsFromRemote`, and `saveTransactionsFromRemote` to identify and delete local records that are no longer present in the remote list provided by Firebase.

#### [MODIFY] [SilverViewModel.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/viewmodel/SilverViewModel.kt)
- Update the Firebase snapshot listeners to call the repository's save methods even when the remote list is empty (to allow clearing all data if it was cleared in Firebase).

### UI Refinement (Language Consistency)

#### [MODIFY] [ProductDialog.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/components/ProductDialog.kt)
- Translate `presetProducts` and `categories` to Tamil.
- Update header text to Tamil for consistency.

#### [MODIFY] [TransactionDialog.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/components/TransactionDialog.kt)
- Translate the fallback `availableProductNames` list to Tamil.

## Verification Plan

### Manual Verification
1.  **Deletion Sync:**
    - Delete a Shop/Product/Transaction directly in the Firebase Console.
    - Verify that the entry is automatically removed from the app's list.
2.  **Empty State:**
    - Delete all items of a certain type in Firebase.
    - Verify that the app's list becomes empty and does not keep old local records.
3.  **UI Language:**
    - Open the "Add Transaction" or "Add Product" dialog when the database is empty.
    - Verify that the suggested product chips are now in Tamil (or appropriate for the UI context).
