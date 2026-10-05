# Manual acceptance test checklist

Use a disposable local database with synthetic data. Record dates, actual results and screenshots after you run the tests. The expected results below are not claims of completed testing.

| Test | Action | Expected result |
|---|---|---|
| Build | Run mvn clean verify | Unit tests pass and a shaded JAR is created |
| Missing password | Launch without PETCARE_DB_PASSWORD and refresh | Readable configuration error |
| MySQL stopped | Stop the local MySQL service and refresh | UI stays responsive; database error appears |
| Dashboard | Refresh with seed data | Two customers, two pets and LKR 1500 receipts today |
| Customer create | Add a valid customer | Record appears after refresh |
| Customer validation | Try blank name, invalid phone and malformed email | Save blocked with readable message |
| Customer update | Edit phone and address | Changes persist after reopening the app |
| Customer search | Search a distinctive name | Matching records appear |
| Customer delete | Delete an unreferenced customer | Record removed |
| Protected delete | Delete customer 1 from seed data | Deletion blocked by foreign key |
| Pet create | Register a pet under a customer | Correct owner appears |
| Pet validation | Enter tomorrow's birth date | Save blocked |
| Pet update/delete | Edit then delete an unreferenced pet | Changes persist and delete succeeds |
| Service CRUD | Add, edit and delete an unused service | All operations work |
| Service archive | Clear Active for an existing service | Service disappears from new booking choices |
| Booking | Book a future half-hour slot | BOOKED record appears with copied service fee |
| Time validation | Book a past date or 09:15 | Save blocked |
| Vet conflict | Book another pet for the same vet and slot | Booking rejected |
| Pet conflict | Book the same pet and slot with another vet | Booking rejected |
| Cancellation | Cancel a booked appointment | CANCELLED status and zero balance |
| Released slot | Book the cancelled slot again | New booking succeeds |
| Historical fee | Change a service price after booking | Existing appointment fee stays unchanged |
| Historical owner | Move a pet to another owner after booking | Appointment retains its original customer |
| Treatment | Complete a booking with diagnosis and partial payment | One treatment, one payment and COMPLETED status |
| Blank diagnosis | Complete with empty diagnosis | Save blocked; no new records |
| Payment too high | Initial or later payment exceeds fee/balance | Save blocked; no financial changes |
| Second completion | Complete an already completed appointment | Rejected; no duplicate treatment |
| Balance payment | Pay the seeded LKR 1000 balance | Balance becomes zero; receipts increase |
| Exact money | Pay amounts containing decimals | Totals retain exact two-decimal values |
| Treatment viewer | Select a completed visit and view treatment | Diagnosis, medication and timestamp appear |
| Sorting | Sort a table then edit a selected row | Correct selected record is updated |
| Empty report | Choose dates with no visits | PDF still displays report title and zero totals |
| Report join | Export dates containing a multi-payment visit | Visit appears once and payments are totaled |
| Report dates | Include a visit on the final day | Visit included through the end of that day |
| Reversed dates | End date before start date | Export blocked |
| Overwrite | Export to an existing PDF | Confirmation requested before replacement |
| Concurrency | Open two app windows and book the same slot | At most one booking succeeds |
| Concurrent payment | Pay the same remaining balance from two windows | Only one succeeds; no overpayment |
| Restart | Close and reopen app | Saved data remains in MySQL |
| EXE | Build app image and launch with environment configured | App runs using bundled runtime |
| GitHub | Clone the repository into a fresh folder and build | Source and build instructions are sufficient |

## SQL evidence after a visit

```sql
SELECT * FROM appointments ORDER BY id DESC;
SELECT * FROM treatments ORDER BY id DESC;
SELECT appointment_id, SUM(amount) AS paid FROM payments GROUP BY appointment_id;
```

Do not force connection failures on a live database. Transaction rollback should be tested only in a disposable test database. Verify that a failed completion leaves the appointment BOOKED and inserts neither treatment nor payment. Keep your actual screenshots and exported PDFs as evidence; no fabricated evidence is supplied.

## Version 2 account and UI tests

| Test | Action | Expected result |
|---|---|---|
| Existing database upgrade | Run migration-v2-users.sql | Users table added; clinic data unchanged |
| Registration | Enter full name, a valid username and matching 15-character or longer passwords | Account saved and workspace opens |
| Duplicate username | Register the same username with different capitalization | Duplicate rejected |
| Password mismatch | Enter different password and confirmation | Rejected before database insertion |
| Short password | Enter fewer than 15 characters | Registration rejected |
| Login success | Log out then log in with the registered credentials | Workspace opens for that user |
| Wrong password | Enter incorrect credentials | Generic username/password error |
| Show password | Toggle Show password | Password visibility changes; value preserved |
| Login cooldown | Fail sign-in five times in one window | Further attempts delayed for 30 seconds |
| Logout | Choose Log out and confirm | Workspace closes; fresh sign-in window opens |
| Busy logout | Try logout while saving or exporting | Wait message; operation is allowed to finish |
| Dashboard | Refresh Overview after database changes | Cards and appointment list update |
| Sidebar | Visit each section | Correct page and highlighted navigation item |
| Password storage | Inspect users table | Salt, hash and iteration count; no plaintext password |

## Version 3 UI and veterinarian checks

- Verify bundled pet images appear after running the packaged JAR offline.
- Toggle animation off and on; reopen login after logout.
- Add, search, edit and delete an unreferenced vet. Confirm deleting a vet with appointments is rejected.
- Select each sidebar page and verify the selection remains highlighted after moving the pointer away.
