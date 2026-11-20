# Resident Bill Selection, Checkout, and Receipt Design

## Intent

Residents currently see one maintenance bill at a time and pay through a small
dialog. The new flow should show every bill for the resident's unit, let the
resident explicitly choose which unpaid bill to process, then use a focused
checkout screen and receipt screen modeled on the SharePlate buyer flow.

## Reference Screens

- `C:\AndroidProject\SharePlate\app\src\main\java\com\example\shareplate\ui\buyer\home\BuyerCheckoutScreen.kt`
- `C:\AndroidProject\SharePlate\app\src\main\java\com\example\shareplate\ui\buyer\home\BuyerPaymentScreen.kt`
- `C:\AndroidProject\SharePlate\app\src\main\java\com\example\shareplate\ui\buyer\home\BuyerOrderSuccessScreen.kt`

Use SharePlate's structure and spacing, not its green SharePlate branding.
CondoTrack's existing blue palette, typography, and component shapes remain
authoritative.

## Approved Behavior

- The resident Billing screen shows all bills loaded for the resident's unit.
- The list is divided into outstanding bills and paid bills.
- Outstanding bills show month, amount, due date, and overdue state.
- Paid bills remain visible but cannot be selected again.
- Outstanding bills are ordered by overdue first, then earliest due date, then
  oldest billing month.
- If exactly one bill is outstanding, it is selected automatically.
- If more than one bill is outstanding, no bill is selected initially.
  The resident must tap a bill before continuing.
- A sticky **Continue to payment** action shows the selected bill amount.
- Checkout is a dedicated screen with:
  - Selected bill summary.
  - `Total Payment`.
  - Radio-card payment methods.
  - A fixed **Confirm Payment** button.
- CondoTrack keeps only its real allowed methods: `DuitNow` and
  `FPX online banking`.
- Successful payment opens a dedicated receipt screen.
- Payment History rows can reopen their receipt.
- The backend payment rules, amount, receipt number, and Supabase tables remain
  unchanged.

## Non-Goals

- No combined multi-bill checkout.
- No cash, card, or e-wallet methods.
- No database schema migration.
- No change to management bill issuing or deletion.

## Visual Direction

The sequence mirrors SharePlate while using CondoTrack's blue visual system:

1. **Bills**
   - Heading and outstanding total.
   - Selectable bill cards with radio indicators.
   - Separately labelled paid-bill section.
   - Sticky bottom CTA.

2. **Payment**
   - Back header titled `Payment`.
   - Large selected total.
   - `Select Payment Method` radio cards for DuitNow and FPX.
   - Fixed bottom `Confirm Payment`.

3. **Receipt**
   - Success check icon.
   - `Payment Successful`.
   - Prominent receipt number where SharePlate shows its pickup code.
   - Summary card with unit, billing month, amount, method, and paid time.
   - `Back to bills` and `Back to home` actions.

## State and Navigation

- Add `BILL_CHECKOUT` and `BILL_RECEIPT` destinations.
- Keep `selectedBillId`, `selectedPaymentReceipt`, and `selectedPaymentMethod`
  in navigation-level saveable state.
- Resolve the selected bill from the current billing state before rendering
  checkout.
- Resolve receipt data from the loaded payment history and bills.
- A successful payment callback navigates to the receipt screen; navigating
  back returns to Billing.

## Testing

- Unit-test outstanding and paid bill grouping.
- Unit-test overdue-first ordering and automatic selection only for one bill.
- Unit-test that paid bills are not selectable.
- Manual emulator verification for zero, one, two, and multiple outstanding
  bills; DuitNow; FPX; successful receipt; back navigation; process recreation.

