# Resident Bill Selection, Checkout, and Receipt Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let residents select any outstanding unit bill and pay it through a SharePlate-style checkout and receipt flow.

**Architecture:** Add pure selection rules, extend billing view-model state with a navigation callback, split checkout and receipt into focused Compose screens, and coordinate the flow through `CondoTrackApp`. The existing Supabase payment repository remains the transaction boundary.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, StateFlow, Supabase Kotlin/PostgREST, JUnit

**Spec:** `docs/superpowers/specs/2026-11-20-resident-bill-selection-checkout-receipt-design.md`

## Global Constraints

- Use CondoTrack blue branding; use SharePlate only for structure and interaction.
- Only `DuitNow` and `FPX online banking` are valid methods.
- Paid bills must never be selectable.
- More than one outstanding bill must require explicit resident selection.
- No database migration and no management billing changes.
- Preserve existing receipt-number and payment transaction behavior.

## Review Focus

- A stale `selectedBillId` must not open checkout for another unit or a paid bill.
- Back navigation from checkout must preserve the billing list and selection state.
- Payment success must show the exact generated receipt number.
- Process recreation must recover selection and receipt navigation safely.
- Zero or one outstanding bill must avoid confusing selection states.

---

### Task 1: Add bill grouping and selection rules

**Files:**
- Create: `app/src/main/java/com/example/condominium_maintenance_system/data/model/BillingSelectionRules.kt`
- Test: `app/src/test/java/com/example/condominium_maintenance_system/BillingSelectionRulesTest.kt`

**Interfaces:**
- Produces: `BillingSelectionRules.outstandingBills(bills: List<MaintenanceFee>, today: String): List<MaintenanceFee>`
- Produces: `BillingSelectionRules.paidBills(bills: List<MaintenanceFee>): List<MaintenanceFee>`
- Produces: `BillingSelectionRules.initialSelection(outstanding: List<MaintenanceFee>): String?`

- [ ] **Step 1: Write failing tests** for unpaid-only grouping, overdue-first ordering, paid-only grouping, and automatic selection only when exactly one bill is outstanding.
- [ ] **Step 2: Run `.\gradlew.bat testDebugUnitTest --tests "*BillingSelectionRulesTest"` and verify the missing implementation fails.
- [ ] **Step 3: Implement the three pure functions without changing backend behavior.**
- [ ] **Step 4: Re-run the focused test and verify it passes.**

### Task 2: Extend payment records for receipt lookup

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/ui/CondoCareModels.kt`
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/data/repository/BillingRepository.kt`

**Interfaces:**
- Produces: `PaymentRecord.billId: String`
- Produces: `PaymentRecord.unitId: String? = null`

- [ ] **Step 1: Add `billId` to `PaymentRecord` and populate it from `PaymentRow.billId`.**
- [ ] **Step 2: Populate `unitId` when the payment belongs to the loaded resident unit.**
- [ ] **Step 3: Update existing constructions and tests so the project compiles.**

### Task 3: Add payment-success navigation state

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/ui/viewmodel/BillingViewModel.kt`

**Interfaces:**
- Produces: `fun pay(bill: MaintenanceFee, unitId: String, method: String, onSuccess: (PaymentRecord) -> Unit = {})`
- Produces: `BillingUiState.lastPayment: PaymentRecord?`

- [ ] **Step 1: Set `lastPayment` and prepend the returned payment to history.**
- [ ] **Step 2: Invoke `onSuccess(payment)` before or alongside reloading billing data.**
- [ ] **Step 3: Preserve existing failure rollback and error behavior.**

### Task 4: Create the checkout screen

**Files:**
- Create: `app/src/main/java/com/example/condominium_maintenance_system/ui/screens/billing/BillCheckoutScreen.kt`

**Interfaces:**
- Consumes: one `MaintenanceFee`, unit code, selected method, and `paying` state.
- Produces: `BillCheckoutScreen(bill, unitCode, selectedMethod, paying, onBack, onMethodSelected, onConfirm)`

- [ ] **Step 1: Build the SharePlate-style total-payment block using CondoTrack blue.**
- [ ] **Step 2: Add radio-card options for DuitNow and FPX with descriptive subtext.**
- [ ] **Step 3: Keep **Confirm Payment** fixed near the bottom and disabled without a method or while paying.**
- [ ] **Step 4: Show the selected billing month, due date, and unit before confirmation.**

### Task 5: Create the receipt screen

**Files:**
- Create: `app/src/main/java/com/example/condominium_maintenance_system/ui/screens/billing/PaymentReceiptScreen.kt`

**Interfaces:**
- Consumes: `PaymentRecord`, billing month, unit code, total amount, and paid timestamp.
- Produces: `PaymentReceiptScreen(payment, bill, unitCode, onBackToBills, onBackHome)`

- [ ] **Step 1: Add the success icon and `Payment Successful` heading.**
- [ ] **Step 2: Display the receipt number prominently in the SharePlate pickup-code position.**
- [ ] **Step 3: Add summary rows for unit, month, method, paid time, and amount.**
- [ ] **Step 4: Add **Back to bills** and **Back to home** actions.**

### Task 6: Replace the single-current-bill UI

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/ui/screens/billing/BillingScreen.kt`

**Interfaces:**
- Consumes: `BillingSelectionRules`.
- Produces: `BillingScreen(... selectedBillId: String?, onBillSelected, onContinuePayment: (MaintenanceFee) -> Unit, onOpenReceipt: (PaymentRecord) -> Unit)`

- [ ] **Step 1: Replace the single hero bill with an outstanding-total summary.**
- [ ] **Step 2: Render selectable outstanding bill cards with month, amount, due date, overdue state, and radio state.**
- [ ] **Step 3: Render paid bills below in a disabled, clearly labelled section.**
- [ ] **Step 4: Add a sticky **Continue to payment** CTA showing the selected amount.**
- [ ] **Step 5: Remove the old payment dialog from `BillingScreen`.**
- [ ] **Step 6: Make Payment History rows open their receipt.**

### Task 7: Wire destinations and state

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/ui/CondoCareModels.kt`
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/ui/CondoTrackApp.kt`

**Interfaces:**
- Produces destinations: `BILL_CHECKOUT`, `BILL_RECEIPT`
- Produces saveable state: `selectedBillId`, `selectedPaymentMethod`, `selectedReceiptNumber`

- [ ] **Step 1: Add destinations and exclude checkout/receipt from the resident bottom navigation where appropriate.**
- [ ] **Step 2: Resolve the selected bill from current billing state and redirect safely if it disappears or becomes paid.**
- [ ] **Step 3: Route successful payments to `BILL_RECEIPT` using the callback and generated receipt number.**
- [ ] **Step 4: Route receipt back actions to Billing and resident Home.**
- [ ] **Step 5: Keep state saveable across activity recreation.**

### Task 8: Verify

**Files:**
- Verify only.

- [ ] **Step 1: Run the complete unit-test suite and require zero failures.**
- [ ] **Step 2: Build and install on the Pixel API 37 emulator.**
- [ ] **Step 3: Verify one outstanding bill auto-selects and can be paid.**
- [ ] **Step 4: Verify multiple outstanding bills require an explicit selection.**
- [ ] **Step 5: Verify paid bills cannot be selected or paid twice.**
- [ ] **Step 6: Verify both payment methods reach the receipt with the correct amount and receipt number.**
- [ ] **Step 7: Verify receipt reopening from Payment History and back navigation.**

