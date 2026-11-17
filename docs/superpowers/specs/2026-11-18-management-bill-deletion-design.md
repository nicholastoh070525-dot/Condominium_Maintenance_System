# Management Bill Deletion Design

## Intent

Management needs to remove the unpaid bills for a billing month when a billing
run was created with incorrect information, without allowing residents to
delete their own charges. Because deletion is destructive, management must
prove that they control the currently signed-in account by entering its
password.

## Approved Behavior

- Only management can see the delete action.
- A single **Delete bills** action appears beside the **Unit ledger** heading.
- The action deletes every bill in the displayed month whose stored status is
  `UNPAID`.
- Overdue bills are included because they are still unpaid.
- Paid bills are locked and cannot be deleted.
- The confirmation dialog shows the billing month, unpaid bill count, and the
  number of paid bills that will be kept.
- Management must enter the password for the currently signed-in account.
- A missing or incorrect password makes no database change and leaves the
  dialog open.
- A successful deletion refreshes the bill ledger and collection totals.
- The password is never stored, logged, or copied into application state.

## Approaches Considered

1. Verify the password by signing in again with the current account email and
   entered password. This uses Supabase Auth as the source of truth and fits the
   existing email/password authentication flow.
2. Send an email OTP before deletion. This is stronger against a compromised
   active session but adds an email dependency and is slower for correction of
   a billing mistake.

The approved approach is option 1 because the user explicitly requested the
account password. Deletion remains restricted to unpaid bills and is also
enforced by the delete query and Supabase RLS.

## Constraints

- No plaintext password column is added to application tables.
- No paid bill, payment, or receipt is deleted.
- Bills cannot be deleted one resident at a time from the ledger.
- Existing resident billing and payment behavior remains unchanged.

## Supabase Enforcement

Production RLS must allow authenticated management profiles to delete only
`UNPAID` bills. Residents must not receive a delete policy.

