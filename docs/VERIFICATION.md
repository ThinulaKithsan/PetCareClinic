# Verification status

Prepared on 4 October 2026.

## Checks completed here

- Compiled Main, ClinicFrame, ClinicDao, ClinicService, Database, Validation and ValidationException with the Java 17 compiler and `-Xlint:all`: no warnings or errors.
- The UI references ReportService; a temporary type-check substitute with the same public export signature was used only for this compilation. It is outside this project and is not delivered. This does not verify the actual JasperReport implementation or library compatibility.
- Executed 25 standalone Java validation checks against the real Validation class: all passed. Cases cover exact decimal money, zero and upper-limit values, invalid money, required fields, valid and invalid phones, past and malformed times, off-grid slots and valid future slots.
- Parsed pom.xml and visits.jrxml as XML successfully.
- Checked every Jasper report element's horizontal bounds against the report's 802-point content width.
- Checked Windows command scripts for unwanted control characters.

## Checks still required on your computer

- Full `mvn clean verify`, including dependency resolution, ReportService compilation and the supplied JUnit tests.
- Import and execute schema.sql and seed.sql on MySQL.
- Run the full manual checklist in TEST_CHECKLIST.md, including concurrent bookings and payments.
- Compile, fill and visually inspect a real Jasper PDF containing live test database records.
- Inspect the Swing screens on Windows for legibility and correct behavior.
- Launch the shaded JAR and package and launch the Windows application image.
- Create and push a real GitHub repository; verify a fresh checkout can build.

The current environment has Java 17 but no Maven or MySQL server; Maven repository downloads were unavailable. It is Linux and cannot generate a Windows application image. No dependency-bundled JAR or EXE has been fabricated. Source-level compilation and validation results do not establish database integration or GUI correctness.

Record the real outputs of the outstanding checks before claiming the application is ready for submission.

## Version 2 checks completed

Compiled all core, account and Swing source with Java 17 and -Xlint:all without warnings, using the same temporary ReportService type-check substitute. Seven standalone authentication checks passed: correct password, wrong password, unique salts/hashes, normalized username, rejected invalid username, rejected short password and rejected mismatching passwords. The previous 25 validation checks passed again. Login, registration and dashboard panels were rendered offscreen using their real Swing components and visually inspected. Dashboard preview data is synthetic and is not evidence of database execution. Live account registration/login, full-window navigation/logout and MySQL migration still require local verification.

## Version 3 checks

Compiled the core and Swing classes with Java 17 and -Xlint:all using the external report stub described above. Re-ran 25 validation and 7 password/authentication checks. Rendered and inspected actual login, registration and dashboard panels offscreen with bundled artwork; preview dashboard values are synthetic. Added veterinarian CRUD using the existing vets table; live database execution remains to be checked on Windows. No database migration is needed for an existing version-2 installation.

## Report export fix

The supplied Windows stack trace identified an UnsupportedOperationException because JasperReports adds internal connection parameters to the supplied map. ReportService now passes a mutable HashMap instead of Map.of. Deprecated text field overflow attributes were replaced with textAdjust=StretchHeight. XML parsing and the packaged source were checked; live PDF export still requires a rerun in NetBeans.
