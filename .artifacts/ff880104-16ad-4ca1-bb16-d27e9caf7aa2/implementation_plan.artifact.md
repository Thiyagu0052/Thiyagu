# Report Sharing Refinement Plan

This plan aims to refine the report sharing and display by sorting transactions, improving PDF quality, and updating terminology.

## User Review Required

> [!NOTE]
> - Transactions will be sorted chronologically (Low to High) in both the Reports screen and all shared reports.
> - PDF images will be larger (200x200) for better visibility.
> - PDF table will now have a grid/border layout.
> - Terminology change: "நிகர" -> "Pure(g)".

## Proposed Changes

### UI Logic

#### [MODIFY] [ReportsScreen.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/screens/reports/ReportsScreen.kt)
- Sort `filteredTransactions` by `date` (Low to High).
- Ensure the share utilities receive the sorted list.

### Sharing Utilities

#### [MODIFY] [ReportSharingUtils.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/util/ReportSharingUtils.kt)
- Update "நிகர" to "Pure(g)" in `shareTextReport`, `shareImageReport`, and `sharePdfReport`.
- In `sharePdfReport`:
    - Draw horizontal and vertical lines to create a table border.
    - Increase the resolution of embedded images (e.g., from 100 to 200).
    - Adjust row spacing to accommodate borders and larger images.

## Verification Plan

### Manual Verification
- Deploy to a device.
- Check Reports screen: Verify transactions are sorted by date (earliest first).
- Share as PDF:
    - Verify table has borders.
    - Verify images are larger and clearer.
    - Verify column header says "Pure(g)".
- Share as Text/Image:
    - Verify "Pure(g)" label is used.
    - Verify sorted order.
