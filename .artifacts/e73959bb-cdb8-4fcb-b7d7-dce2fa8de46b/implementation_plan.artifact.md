# Supabase Removal Plan

This plan details the steps to cleanly remove Supabase from the project and consolidate all cloud operations into Firebase.

## Proposed Changes

### [Component] Remote Infrastructure

#### [DELETE] [SupabaseSyncManager.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/remote/SupabaseSyncManager.kt)
- Completely remove this file as it is no longer used.

#### [MODIFY] [FirebaseSyncManager.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/data/remote/FirebaseSyncManager.kt)
- Remove all imports of `SupabaseSyncManager`.
- Update the `uploadImageToStorage` function:
    - Remove the code block that attempts to upload to Supabase.
    - Make Firebase Storage the primary (and only) cloud upload target.
    - Keep the Base64 fallback as a safety measure.

---

### [Component] UI & Navigation

#### [MODIFY] [SupabaseSyncBar.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/components/SupabaseSyncBar.kt) [RENAME]
- Rename the file from `SupabaseSyncBar.kt` to `FirebaseSyncBar.kt`.
- Rename the Composable function to `FirebaseSyncBar`.
- Update the internal text and icons to be strictly Firebase-focused (already mostly done, but cleaning up any references).

#### [MODIFY] [AppNavHost.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/navigation/AppNavHost.kt)
- Update the import from `SupabaseSyncBar` to `FirebaseSyncBar`.
- Update the usage of the component in the `Scaffold`.
- Rename the `onSyncToSupabase` parameter in the `SettingsScreen` call to `onSyncNow`.

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt)
- Rename the `onSyncToSupabase` parameter to `onSyncNow` in the function signature and internal usage.

---

### [Component] Business Logic

#### [MODIFY] [SilverViewModel.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/viewmodel/SilverViewModel.kt)
- Verify no direct Supabase dependencies exist (none were found in initial research).

## Verification Plan

### Automated Tests
- Perform a build to ensure no broken references remain.

### Manual Verification
1. **Sync Bar Check**: Verify the top bar correctly shows "Firebase Live Sync" and functions when "Sync Now" is clicked.
2. **Settings Check**: Verify the sync button in Settings works and reflects the correct labeling.
3. **Image Upload Check**: Add a transaction with an image and verify in **Firebase Console** (Storage) that the image is uploaded directly to Firebase without trying Supabase first.
