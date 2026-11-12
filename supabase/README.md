# CondoTrack Supabase setup using Table Editor

The CondoTrack app performs visitor approval, pass completion, and simulated payment updates directly from the Android app. SQL functions and triggers are not required.

## 1. Create the tables

Create these tables in **Table Editor**. Use `uuid` IDs with default `gen_random_uuid()` unless stated otherwise.

### `condotrack_units`

| Column | Type | Notes |
|---|---|---|
| `id` | uuid | Primary key, default `gen_random_uuid()` |
| `block` | text | Required |
| `floor_number` | int8 | Required |
| `room_number` | int8 | Required |
| `unit_code` | text | Required |
| `created_at` | timestamptz | Default `now()` |

Create unique constraints for `unit_code` and for `block + floor_number + room_number`.

### `condotrack_profiles`

| Column | Type | Notes |
|---|---|---|
| `id` | uuid | Primary key, default `gen_random_uuid()` |
| `user_id` | uuid | Required, unique; references `auth.users.id` |
| `full_name` | text | Required |
| `email` | text | Required |
| `phone` | text | Required |
| `role` | text | `RESIDENT` or `MANAGEMENT` |
| `unit_id` | uuid | References `condotrack_units.id`; null for management |
| `unit_code` | text | Null for management |
| `avatar_key` | text | Default `AM` |
| `avatar_url` | text | Nullable |
| `created_at` | timestamptz | Default `now()` |
| `updated_at` | timestamptz | Default `now()` |

### `condotrack_visitor_requests`

| Column | Type | Notes |
|---|---|---|
| `id` | uuid | Primary key, default `gen_random_uuid()` |
| `resident_id` | uuid | Default `auth.uid()`; references `auth.users.id` |
| `visitor_name` | text | Required |
| `vehicle_color` | text | Required |
| `number_plate` | text | Required |
| `visit_at` | timestamptz | No default; app supplies the visit time |
| `note` | text | Default `NULL` or empty string |
| `status` | text | Default `PENDING` |
| `pass_reference` | text | Default `NULL` |
| `approved_at` | timestamptz | Default `NULL` |
| `completed_at` | timestamptz | Default `NULL` |
| `resident_name` | text | Default `NULL` or empty string; app supplies the value |
| `unit_code` | text | Default `NULL`; app supplies the value |
| `created_at` | timestamptz | Default `now()` |
| `updated_at` | timestamptz | Default `now()` |

### `condotrack_bills`

| Column | Type | Notes |
|---|---|---|
| `id` | uuid | Primary key |
| `unit_id` | uuid | References `condotrack_units.id` |
| `billing_month` | date | Required |
| `amount` | numeric | Required |
| `due_date` | date | Required |
| `status` | text | `UNPAID` or `PAID` |
| `paid_at` | timestamptz | Nullable |
| `created_at` | timestamptz | Default `now()` |

Create a unique constraint for `unit_id + billing_month`.

### `condotrack_payments`

| Column | Type | Notes |
|---|---|---|
| `id` | uuid | Primary key, default `gen_random_uuid()` |
| `bill_id` | uuid | Required and unique; references `condotrack_bills.id` |
| `unit_id` | uuid | Required; references `condotrack_units.id` |
| `amount` | numeric | Required |
| `method` | text | `DuitNow` or `FPX online banking` |
| `receipt_number` | text | Required and unique |
| `paid_at` | timestamptz | Default `now()` |

## 2. Import the unit and bill data

In **Table Editor → condotrack_units → Import data**, import:

`table-editor-data/condotrack_units.csv`

This file contains all 1,680 residential units:

- Blocks A and B
- Floors 11–40
- Rooms 01–28

Then import:

`table-editor-data/condotrack_bills.csv`

This creates the initial RM285 unpaid bill for every unit.

## 3. Configure table access

For a local assignment demonstration, RLS may be disabled so the app can read and update these tables.

For a real deployment, enable RLS and create dashboard policies equivalent to:

- Residents can read and update their own profile.
- Management can read all profiles.
- Residents can read and create their own visitor requests.
- Management can read all visitor requests and update pending requests.
- Residents can read bills and payments for their unit.
- Residents can update only their own unpaid bill to PAID.
- Residents can insert only one payment for their own unpaid bill.

Do not use these tables as a public anonymous service.

## 4. Configure Authentication

1. Enable the **Email** provider.
2. Enable **Confirm email**.
3. Set OTP expiry to **600 seconds**.
4. Add `condotrack://reset` as an allowed redirect URL.
5. Configure custom SMTP with Resend.

Recommended SMTP settings:

```text
Host: smtp.resend.com
Port: 465
Username: resend
Password: YOUR_RESEND_API_KEY
Sender: CondoTrack <noreply@your-verified-domain.com>
```

Replace the email templates using the files in `email-templates/`.

## 5. Create avatar storage

In **Storage**, create a public bucket named:

```text
condotrack-avatars
```

For the assignment demo, allow authenticated users to upload and update files. The app stores each file under a folder named with the user UUID.

## 6. Project configuration

Set these values in the project `local.properties`:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-publishable-key
SUPABASE_SCHEME=condotrack
SUPABASE_HOST=reset
```

Rebuild CondoTrack after changing these values.

## Important limitation

Without SQL functions and transactions, duplicate-payment protection is enforced by the app and table constraints. It is suitable for this assignment demonstration but not equivalent to a production-grade atomic payment service.
