# Management Bill Deletion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let management delete all unpaid bills for one billing month after verifying the current account password.

**Architecture:** A **Delete bills** action beside **Unit ledger** requests month deletion from `ManagementBillingViewModel`. The view model re-authenticates the current Supabase account with the entered password, then asks `BillingRepository` to delete all rows for the selected month that still have status `UNPAID`. A Supabase delete policy enforces the management-only rule independently of the UI.

**Tech Stack:** Kotlin, Jetpack Compose, Supabase Auth/PostgREST, JUnit

**Spec:** `docs/superpowers/specs/2026-11-18-management-bill-deletion-design.md`

## Global Constraints

- Only bills whose stored status is `UNPAID` may be deleted; overdue unpaid bills are allowed.
- Paid bills must never be deleted.
- Verify the password of the currently signed-in management account.
- Never store or log the password.
- Delete all unpaid bills for the displayed month; do not offer per-resident deletion.
- Do not change resident billing, payment, or receipt behavior.

## Review Focus

- A wrong password must leave the dialog open and the bill unchanged.
- A paid bill must remain visible and locked even if the UI data is stale.
- A resident must not be able to bypass the UI through the Supabase API.
- Cancelling the dialog must clear the password without deleting anything.
- Deleting the last unpaid bill for a month must render the empty ledger state.

---

### Task 1: Enforce the unpaid-only rule

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/data/model/BillingRules.kt`
- Test: `app/src/test/java/com/example/condominium_maintenance_system/BillingRulesTest.kt`

**Interfaces:**
- Produces: `BillingRules.deletableBillsForMonth(bills: List<ManagementBill>, month: String): List<ManagementBill>`

- [ ] **Step 1: Write a failing test** asserting that the result contains only the unpaid bill from the selected month.
- [ ] **Step 2: Run `.\gradlew.bat testDebugUnitTest --tests "*BillingRulesTest"` and verify the new test fails because `deletableBillsForMonth` does not exist.**
- [ ] **Step 3: Implement the month and case-insensitive `UNPAID` filter.**
- [ ] **Step 4: Re-run the focused test class and verify it passes.**

### Task 2: Verify the current account password

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/data/repository/AuthRepository.kt`

**Interfaces:**
- Produces: `suspend fun verifyCurrentAccountPassword(password: String): Result<Unit>`

- [ ] **Step 1: Read the currently signed-in email and user ID, then reject a blank password.**
- [ ] **Step 2: Re-authenticate with Supabase Email and the current email plus entered password.**
- [ ] **Step 3: Confirm the newly authenticated user ID matches the original user ID.**
- [ ] **Step 4: Return a user-safe failure for incorrect credentials without persisting the password.**

### Task 3: Delete only an unpaid stored bill

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/data/repository/BillingRepository.kt`

**Interfaces:**
- Produces: `suspend fun deleteUnpaidBillsForMonth(month: String): Int`

- [ ] **Step 1: Count the stored unpaid bills for the selected month.**
- [ ] **Step 2: Delete by both `billing_month` and `status = UNPAID`.**
- [ ] **Step 3: Query the month again and fail with a clear message if any unpaid bill remains.**
- [ ] **Step 4: Preserve payments and receipts by never deleting any payment row.**

### Task 4: Add deletion state and action

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/ui/viewmodel/ManagementBillingViewModel.kt`

**Interfaces:**
- Consumes: `BillingRules.deletableBillsForMonth`, `AuthRepository.verifyCurrentAccountPassword`, and `BillingRepository.deleteUnpaidBillsForMonth`.
- Produces: `ManagementBillingUiState.deletingBills: Boolean`
- Produces: `fun deleteMonthBills(password: String, onSuccess: () -> Unit = {})`

- [ ] **Step 1: Add `deletingBills` to the UI state.**
- [ ] **Step 2: Reject a month with no unpaid bills and blank passwords with clear feedback.**
- [ ] **Step 3: Verify the password before calling the repository.**
- [ ] **Step 4: On success, show the deleted count, reload billing data, and invoke `onSuccess`.**
- [ ] **Step 5: On failure, retain the bill and expose the error while clearing the deleting state.**

### Task 5: Add the password-confirmed delete dialog

**Files:**
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/ui/screens/billing/ManagementBillingScreen.kt`
- Modify: `app/src/main/java/com/example/condominium_maintenance_system/ui/CondoTrackApp.kt`

**Interfaces:**
- Consumes: `ManagementBillingViewModel.deleteMonthBills`.
- Produces: `ManagementBillingScreen(... deletingBills: Boolean, onDeleteMonth: (String, () -> Unit) -> Unit)`

- [ ] **Step 1: Add one **Delete bills** action beside **Unit ledger** only when the month has unpaid bills.**
- [ ] **Step 2: Show the month, unpaid count, protected paid count, and an irreversible-delete warning.**
- [ ] **Step 3: Add a masked password field and disable confirmation while blank or deleting.**
- [ ] **Step 4: Keep the dialog open after an error and close it only after a successful callback.**
- [ ] **Step 5: Clear the password when cancelling or dismissing.**
- [ ] **Step 6: Wire the action to `managementBillingViewModel.deleteMonthBills` in `CondoTrackApp`.**

### Task 6: Document Supabase enforcement

**Files:**
- Modify: `supabase/README.md`

**Interfaces:**
- Consumes: existing `condotrack_profiles.user_id`, `condotrack_profiles.role`, and `condotrack_bills.status`.

- [ ] **Step 1: Add a delete policy allowing authenticated profiles with role `MANAGEMENT` to delete rows where `status = 'UNPAID'`.**
- [ ] **Step 2: State explicitly that residents receive no delete policy for bills.**

### Task 7: Build and verify

**Files:**
- Verify only.

- [ ] **Step 1: Build and install through Android Studio.**
- [ ] **Step 2: Verify unpaid bill rows show Delete and paid rows do not.**
- [ ] **Step 3: Verify blank and incorrect passwords do not remove the bill.**
- [ ] **Step 4: Verify cancel closes the dialog and clears the password.**
- [ ] **Step 5: Verify a successful deletion refreshes the ledger and totals.**

