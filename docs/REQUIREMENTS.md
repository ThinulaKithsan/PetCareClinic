# Requirements and design

## Scope

A single veterinary clinic operates fixed 30-minute consultation slots. One appointment covers one pet, vet and service. A completed appointment has one treatment entry and may have multiple payments. Prices and payments use LKR. This is a coursework application with a local MySQL database, not a production clinical records system.

## Assignment mapping

| Brief requirement | Implementation | Evidence to show |
|---|---|---|
| Java Swing enterprise application | Main and ClinicFrame | Launch the desktop application |
| Major transaction UI | Booking, treatment completion and payment | Demonstrate an entire visit |
| Input UIs | Customers, Pets and Services tabs | Add, edit, search and delete an unreferenced record |
| Dashboard | Counts, today's bookings and receipts | Refresh and compare with database queries |
| Design patterns | DAO and connection factory | Explain ClinicDao and Database.open |
| Jasper report from more than two tables | visits.jrxml joins seven tables | Export a populated PDF and explain SQL |
| UI and UX practices | Named tabs, table sorting, selected-row actions, consistent dialogs | Demonstrate navigation and readable feedback |
| Input validation | Validation and form-specific rules | Trigger invalid input examples |
| Exception handling | ValidationException, SQLException handling and rollback | Explain expected and unexpected failures |
| MySQL CRUD | Customer, pet and service creation, reads, updates and deletes | Show records changing in MySQL |
| Git version control | .gitignore and repository setup instructions | Create real commits and show your remote repository |
| JAR | Maven Shade build configuration | Build and run target/petcare-clinic-3.0.0.jar |
| EXE | Windows jpackage application-image script | Run the EXE with its bundled files |
| Remote repository link | User creates and pushes their GitHub repo | Submit the real URL to LMS |
| Optional API integration | Not implemented | Not required by the supplied brief |

The uploaded brief does not provide a readable detailed marks breakdown. This mapping addresses the requirements that are present; it is not an invented marking rubric.

## Database model

| Table | Principal fields | Relationship |
|---|---|---|
| customers | id, name, phone, email, address | One customer to many pets and appointments |
| pets | id, customer_id, name, species, breed, birth_date | One pet to many appointments |
| services | id, name, price, active | One service to many appointments |
| vets | id, name | One vet to many appointments |
| appointments | id, pet_id, customer_id, service_id, vet_id, slot, status, fee | Central transaction record |
| treatments | id, appointment_id, diagnosis, medication, recorded_at | At most one treatment per appointment |
| payments | id, appointment_id, amount, paid_at | Many payments per appointment |

Appointments record the responsible customer and agreed fee at booking. Service price and pet ownership changes therefore do not change those booking facts. Descriptive names remain current. The generated occupied_slot is indexed with vet_id and pet_id to prevent duplicate active slots. Cancellation preserves the record and releases the slot.

## Account and user interface update

Version 3 uses shared theme styles, a forest-green sidebar, sage actions, bundled pet artwork, optional animations, veterinarian CRUD, dashboard metric cards, a daily appointment table and dedicated login and registration panels. Registration creates a local clinic team account. Login verifies the salted hash and opens a new workspace. Logout closes that workspace and returns to a fresh sign-in window; it is blocked while a database transaction is in progress. An authenticated User is required to construct the workspace.

## Business rules

- Names, species and customer phone are required; email, address, breed, pet birth date and medication are optional.
- New appointments must be in the future, on the hour or half-hour.
- One vet and one pet can each occupy only one appointment per slot.
- Only active services can be booked; changing a service price does not change existing appointment fees.
- Only BOOKED appointments can be completed or cancelled.
- Treatment completion records exactly one diagnosis and an optional initial payment.
- Payments must not exceed the stored appointment fee. Later payments require a completed appointment and a positive value.
- Referenced records cannot be deleted. Services can be made inactive.
- Reports use an inclusive date range and show cancelled appointments with zero outstanding balance.

## Intentional limits

All appointments use 30-minute slots; service duration, working hours and overnight boarding are not modeled. Vet records can be added, edited, searched and deleted through the Veterinarians screen; referenced vets cannot be deleted. Staff account registration, login and logout are implemented. There is no role system, stock management, refund workflow or audit trail. All registered staff have equal access. Treatment notes and payments are immutable through the UI after recording; corrections need an administrator and an appropriate audit design before production use. Search loads all matching records rather than paging a large dataset. JDBC operations run in the background, but a single busy flag serializes work from one window. A connection pool would be appropriate for a larger deployment. MySQL/Jasper end-to-end behavior and Windows packaging require local verification.

These limits keep the coursework scope understandable. Discuss possible improvements honestly in the viva instead of claiming they are already implemented.
