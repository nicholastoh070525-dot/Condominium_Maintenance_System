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

Create a unique constraint on `email`. One verified email address can own only
one CondoTrack profile and therefore only one role, either Resident or
Management. Attempting to register the same email for the other role must show
that the account already exists.

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
| `status` | text | Default `PENDING`; app uses `PENDING`, `IN_PROGRESS`, then `COMPLETED` |
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
| `method` | text | `DuitNow`, `Maybank`, `RHB Bank`, `Hong Leong Bank`, or `FPX online banking` |
| `receipt_number` | text | Required and unique |
| `paid_at` | timestamptz | Default `now()` |

### `condotrack_maintenance_requests`

This table stores resident complaints and their complete management response.

| Column | Type | Notes |
|---|---|---|
| `id` | uuid | Primary key, default `gen_random_uuid()` |
| `resident_id` | uuid | Required; references `auth.users.id` |
| `resident_name` | text | Required; app supplies the value |
| `unit_code` | text | Required; app supplies the resident's unit |
| `title` | text | Required |
| `category` | text | Plumbing, Electrical, Air-con, Lift, Common area or Other |
| `description` | text | Required |
| `urgency` | text | Low, Normal or Urgent |
| `status` | text | Default `PENDING` |
| `photo_urls` | text[] | Default `'{}'::text[]`; public storage URLs |
| `technician_name` | text | Nullable |
| `scheduled_at` | timestamptz | Nullable visit date and time |
| `report` | text | Nullable management maintenance report |
| `created_at` | timestamptz | Default `now()` |
| `updated_at` | timestamptz | Default `now()` |

### `condotrack_maintenance_tasks`

This table stores planned and assigned maintenance work shown on the calendar.

| Column | Type | Notes |
|---|---|---|
| `id` | uuid | Primary key, default `gen_random_uuid()` |
| `request_id` | uuid | Nullable; references `condotrack_maintenance_requests.id` |
| `title` | text | Required |
| `unit_code` | text | Unit or location; use `Common area` when not unit-specific |
| `assignee_id` | uuid | Required; references `auth.users.id` |
| `assignee_name` | text | Required; app supplies the manager name |
| `priority` | text | Low, Normal or Urgent |
| `scheduled_at` | timestamptz | Required |
| `status` | text | Default `ASSIGNED` |
| `notes` | text | Nullable instructions |
| `report` | text | Nullable completion report |
| `created_at` | timestamptz | Default `now()` |

You can create both tables with this SQL in **SQL Editor**:

```sql
create table if not exists public.condotrack_maintenance_requests (
  id uuid primary key default gen_random_uuid(),
  resident_id uuid not null references auth.users(id),
  resident_name text not null,
  unit_code text not null,
  title text not null,
  category text not null,
  description text not null,
  urgency text not null,
  status text not null default 'PENDING',
  photo_urls text[] not null default '{}'::text[],
  technician_name text,
  scheduled_at timestamptz,
  report text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.condotrack_maintenance_tasks (
  id uuid primary key default gen_random_uuid(),
  request_id uuid references public.condotrack_maintenance_requests(id) on delete set null,
  title text not null,
  unit_code text not null,
  assignee_id uuid not null references auth.users(id),
  assignee_name text not null,
  priority text not null,
  scheduled_at timestamptz not null,
  status text not null default 'ASSIGNED',
  notes text,
  report text,
  created_at timestamptz not null default now()
);

create index if not exists condotrack_maintenance_requests_resident_idx
  on public.condotrack_maintenance_requests(resident_id);
create index if not exists condotrack_maintenance_requests_status_idx
  on public.condotrack_maintenance_requests(status);
create index if not exists condotrack_maintenance_tasks_schedule_idx
  on public.condotrack_maintenance_tasks(scheduled_at);
create index if not exists condotrack_maintenance_tasks_assignee_idx
  on public.condotrack_maintenance_tasks(assignee_id);
```

### Maintenance request workflow

The app deliberately uses three complaint statuses:

1. `PENDING` after a resident submits the complaint.
2. `IN_PROGRESS` after management assigns a technician and visit schedule.
3. `COMPLETED` after management adds the required maintenance report.

The app validates the transition and does not allow `PENDING` requests to jump
directly to `COMPLETED`. Existing demo rows using older status values should be
updated to `IN_PROGRESS`, or completed with a report and changed to `COMPLETED`.

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
- Residents can read and create their own maintenance requests and read tasks for their unit.
- Residents can delete only their own maintenance requests.
- Management can read all maintenance requests and update their status, schedule and report.
- Management can create tasks and update only tasks where `assignee_id = auth.uid()`.
- Residents can read bills and payments for their unit.
- Residents can update only their own unpaid bill to PAID.
- Residents can insert only one payment for their own unpaid bill.
- Management can delete unpaid bills for the selected billing month.
- Residents cannot delete bills.

For a production RLS setup, create the management delete policy with:

```sql
drop policy if exists "Management can delete unpaid bills"
on public.condotrack_bills;

create policy "Management can delete unpaid bills"
on public.condotrack_bills
for delete
to authenticated
using (
  status = 'UNPAID'
  and exists (
    select 1
    from public.condotrack_profiles
    where condotrack_profiles.user_id = auth.uid()
      and condotrack_profiles.role = 'MANAGEMENT'
  )
);
```

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
Port: 587
Username: resend
Password: YOUR_RESEND_API_KEY
Sender: CondoTrack <noreply@your-verified-domain.com>
```

`onboarding@resend.dev` is only a testing sender. Resend normally permits it to
send only to the Resend account owner's email address. To let any resident
register, add and verify your own domain under **Resend → Domains**, then use an
address on that verified domain as the Supabase SMTP sender.

### Password and OTP limitations

Supabase Auth stores passwords as one-way hashes. Plaintext passwords cannot be
viewed in Table Editor, in the Android app, or through the public tables. Do not
add a password column to `condotrack_profiles`; it would not authenticate users
and would expose credentials unnecessarily. Use **Forgot Password** in the app,
or **Authentication → Users** in Supabase to send a password-recovery email.

The app enforces a 30-second resend countdown, and codes expire after 10
minutes. Supabase Auth also applies its own email rate limits. A resent code
replaces the previous code, so users should enter the most recent OTP.
Different recipients require a verified Resend sending domain.

Replace the email templates using the files in `email-templates/`.

## 5. Create avatar storage

In **Storage**, create these public buckets:

```text
condotrack-avatars
condotrack-maintenance-images
```

For the assignment demo, allow authenticated users to upload and update files.
If storage RLS is enabled, also allow users to delete files in their own UUID
folder so complaint-photo cleanup can complete when a request is deleted.
Avatar files are stored under the user UUID. Maintenance photos are stored under
the resident's UUID inside `condotrack-maintenance-images`.

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
