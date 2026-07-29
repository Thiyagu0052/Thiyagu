# Research Notes - Product Image Preview

The user wants to view product images in a dialog when clicked in the "product section".

## Findings

- **Screen Identified**: `ProductsScreen.kt` contains the "product section" where products are listed in a grid.
- **Component Identified**: `ProductGridItem` is the composable responsible for rendering each product.
- **Current Behavior**:
    - The entire `Card` in `ProductGridItem` is clickable and triggers `onEdit()`.
    - The product image is displayed using `AsyncImage` inside a `Box`.
- **Existing Solution**:
    - `ImagePreviewDialog.kt` provides a reusable dialog for previewing images.
    - It has an overload for a single `imageUri`.
    - `TransactionCardItem.kt` already uses this dialog successfully.

## Proposed Strategy

1.  In `ProductGridItem` (within `ProductsScreen.kt`):
    - Add a state variable `showImagePreview`.
    - Add a `.clickable` modifier to the `Box` wrapping the `AsyncImage`.
    - Show `ImagePreviewDialog` when `showImagePreview` is true.
2.  Import `ImagePreviewDialog` in `ProductsScreen.kt`.
