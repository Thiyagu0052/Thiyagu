# Plan to Refine Header and Footer Alignment

The previous compact changes were a bit too aggressive, causing the header title to be truncated and the footer (navigation bar) to look cramped. I will restore comfortable heights while keeping the typography and list density optimized.

## User Review Required

> [!IMPORTANT]
> I will increase the Header and Footer heights slightly to standard sizes (`56.dp` and `80.dp`) to ensure text isn't cut off and icons have proper touch targets. This will fix the "truncated" look while keeping the overall "Pro" feel.

## Proposed Changes

### Navigation Structure

#### [MODIFY] [AppNavHost.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/navigation/AppNavHost.kt)
- **Top Bar**: Increase height from `48.dp` to `56.dp`. Ensure the `CenterAlignedTopAppBar` doesn't clip the title "KKY SILVERS".
- **Bottom Bar**: Increase `NavigationBar` height from `64.dp` to `80.dp`. This is the Material3 standard and will prevent icons/labels from looking squeezed.
- Re-enable `alwaysShowLabel = true` for the `NavigationBarItem` but keep the `labelSmall` font size, as it's more user-friendly.

### Dashboard Refinement

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/thiya/OneDrive/Documents/GitHub/Thiyagu/app/src/main/java/com/example/ui/screens/dashboard/DashboardScreen.kt)
- Increase the header `Row` vertical padding slightly from `10.dp` to `12.dp` for better visual balance.

## Verification Plan

### Automated Tests
- Build and run the app.

### Manual Verification
- **Header**: Verify "KKY SILVERS" is clearly visible and not truncated.
- **Footer**: Verify navigation items are well-spaced and readable.
- **Lists**: Ensure the compact list items are still retained.
