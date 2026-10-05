# PetCare Clinic

Java Swing coursework project for Enterprise Application Development 1, DSE 25.3.

This project manages a veterinary clinic's customers, pets, services, appointments, treatments and payments. Its major business scenario is veterinary appointment and treatment management. The application uses standard Swing controls, JDBC and one Jasper report. Version 3 includes staff registration, login and logout, a forest-green sidebar, sage and ivory styling, bundled dog and cat artwork, optional gentle animations, veterinarian management, dashboard cards and a daily appointment list.

## Upgrade from the first version

Close NetBeans, extract this updated ZIP into a separate folder and open the new PetCareClinic folder containing pom.xml. Avoid mixing old and new Java files.

If you already created the clinic database, execute **database/migration-v2-users.sql only** in MySQL Workbench. It adds the users table without changing existing clinic records. Do not rerun schema.sql or seed.sql on that database. Your existing petcare_app account's grants on petcare.* also cover the new users table.

Keep PETCARE_DB_PASSWORD set to the application database account's password, restart NetBeans, then Clean and Build and Run. The sign-in screen opens. Choose **New here? Create an account**, enter your full name and username and choose a passphrase of at least 15 characters. Registration signs you in automatically. Use Log out in the workspace header, then sign in with the same account to verify it persists.

**Your application account password is separate from your MySQL root or petcare_app database password.** Database credentials connect the program to MySQL; the staff credentials are entered in the new login screen. There is no built-in default account or password. MySQL must be running and the database account must work before registration or login can succeed.

## Start here

1. Install a full **JDK 17**, **Apache Maven**, and **MySQL 8.0.16 or later** on your Windows computer. Add the JDK and Maven `bin` folders to PATH. NetBeans is optional; open this folder as a Maven project.
2. Confirm `java -version`, `javac -version`, `mvn -version` and `mysql --version` in Command Prompt.
3. In MySQL Workbench, open and execute `database/schema.sql`. Use a fresh database: the script deliberately does not silently overwrite existing tables. Execute `database/seed.sql` once to add synthetic demonstration data.
4. Create an application database account. In MySQL Workbench, replace the example password below with your own password and run:

```sql
CREATE USER 'petcare_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_YOUR_OWN_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE ON petcare.* TO 'petcare_app'@'localhost';
```

5. In the same Command Prompt you will use to launch the app, set the password. Do not include quotation marks in the password value itself:

```bat
set "PETCARE_DB_PASSWORD=YOUR_ACTUAL_PASSWORD"
scripts\build-windows.cmd
scripts\run-windows.cmd
```

The first build requires internet access to Maven repositories. It runs unit tests and creates `target/petcare-clinic-3.0.0.jar`, a JAR containing dependencies. If Maven reports dependency resolution failures, fix internet or Maven proxy settings; do not remove JasperReports to make the build appear successful.

You can also use `java -jar target/petcare-clinic-3.0.0.jar` directly. In NetBeans, configure the environment variable for its Run action, or launch from the terminal above. An IDE launched earlier may not inherit variables set later in a terminal.

Defaults: database `petcare`, host `localhost`, port `3306`, account `petcare_app`. Optional overrides:

```bat
set "PETCARE_DB_USER=petcare_app"
set "PETCARE_DB_URL=jdbc:mysql://localhost:3306/petcare?connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000"
```

The app and MySQL server should use the same clinic time zone. Do not switch time zones between booking and reporting. The defaults target a local MySQL development installation. For a remote deployment, configure verified TLS in the JDBC URL and restrict database network access.

## Create the Windows EXE

After a successful build, on Windows run:

```bat
scripts\package-windows.cmd
```

This creates an application image with the launcher `dist/PetCareClinic/PetCareClinic.exe` and a bundled Java runtime. **Keep the whole PetCareClinic folder together**; the EXE alone is not the application. `jpackage --type app-image` avoids an installer-tool dependency. If your lecturer specifically expects a single installer EXE, install the packaging prerequisites required by your JDK's `jpackage --type exe`, and build an installer on Windows. Database installation and credentials are still required separately. Environment variables must be available to the process that launches the EXE; for persistent per-user variables use Windows Environment Variables settings, then reopen the terminal or sign in again.

To reproduce the image, delete the old `dist/PetCareClinic` folder before rerunning packaging. Do not package on Linux and rename its output to `.exe`.

## What is included

- Staff registration, login, logout and salted PBKDF2-HMAC-SHA256 password storage.
- Sidebar navigation, dashboard cards and today's appointments.
- Dashboard with customer and pet counts, today's bookings and today's payment receipts.
- Search, create, edit and delete for customers, pets and services.
- Service archiving using its Active checkbox.
- Vet and pet double-booking prevention in the database.
- Appointment cancellation, treatment completion and later balance payments.
- A historical fee and customer reference captured when booking.
- A landscape Jasper PDF report from seven tables, with visit count, received payments and outstanding balances.
- Required input validation, a user-defined exception, friendly database errors and diagnostic logging.
- Background database and report work using `SwingWorker`.
- Maven build, Windows run and packaging scripts, viva guide, requirements mapping and test checklist.

## Demonstrate it

Create an account, sign in and open Overview. Refresh, then search the synthetic customers and pets. Book a future appointment, attempt a duplicate slot, complete treatment with a partial payment, record the balance payment and export a report covering that appointment's date. The seeded completed visit has a LKR 1,000 balance that can be paid immediately.

## File map

| File | Responsibility |
|---|---|
| `Main.java` | Starts Swing and the sign-in window on the event dispatch thread |
| `AuthFrame.java` and `AuthPanel.java` | Sign-in and registration screens |
| `AuthService.java` | Account registration and credential verification |
| `PasswordHasher.java` | PBKDF2 hashing, salt creation and constant-time hash comparison |
| `Theme.java` | Shared colours, typography, buttons and table styling |
| `DashboardPanel.java` | Summary cards, daily appointment list and quick actions |
| `ClinicFrame.java` | Screens, dialogs and background task coordination |
| `ClinicDao.java` | Parameterized CRUD SQL and reusable table results |
| `ClinicService.java` | Booking, treatment and payment business transactions |
| `Database.java` | Connection factory using environment configuration |
| `Validation.java` | Input rules and exact money parsing |
| `ValidationException.java` | User-defined exception |
| `ReportService.java` | Compiles, fills and exports the Jasper report |
| `database/schema.sql` | Database constraints and relationships |
| `database/seed.sql` | Synthetic demonstration data |
| `visits.jrxml` | Seven-table report template |

## GitHub and submission

Create your own GitHub repository, then run these commands in this project folder. Replace the URL with the actual repository you created:

```sh
git init
git add .
git commit -m "Implement veterinary clinic appointment and treatment workflow"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

Continue making meaningful commits as you test, understand and improve the project. Do not invent historical commits. `target/` and `dist/` are ignored because they are generated; attach your built JAR and zipped Windows application image to a GitHub Release or submit them using the lecturer's required method. Submit the real repository link to LMS. Never commit database credentials, real client data or generated reports with private information.

## Verification status

Read `docs/VERIFICATION.md` before submission. This package contains complete source and build instructions. A finished dependency-bundled JAR, a Windows EXE, a hosted GitHub repository and MySQL/Jasper execution evidence are **not included**. Build and verify those outputs on your machine. The source must not be presented as a fully tested production application or as proof of an A+.

## Account security scope

Registered accounts are local clinic team members with the same privileges. Registration is open in this coursework application; it is not an administrator approval process. Passwords use 600,000 PBKDF2-HMAC-SHA256 iterations, a random 16-byte salt and a 256-bit hash. Password confirmation, username rules and unique database usernames are enforced. A window-local cooldown follows five failed login attempts; it resets when the window is recreated and is not server-side rate limiting. Role permissions, audit trails, password recovery, MFA and administrator-managed invitations are future work. This desktop authentication is not a substitute for protecting database credentials or enforcing permissions on a server.

## References

- Oracle JDK 17 jpackage: https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html
- JasperReports 6.21.3 compile API: https://jasperreports.sourceforge.net/6.21.3/api/net/sf/jasperreports/engine/JasperCompileManager.html
- JasperReports 6.21.3 fill API: https://jasperreports.sourceforge.net/6.21.3/api/net/sf/jasperreports/engine/JasperFillManager.html

Use the assignment's actual submission instructions and your lecturer's academic integrity rules. Personalize the demonstration, understand each method and acknowledge assistance where required.

- OWASP password storage guidance: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
