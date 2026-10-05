# Open the professional version in NetBeans

1. Extract the ZIP into a NEW folder. Do not overwrite your existing project.
2. In NetBeans, choose File > Open Project and select the extracted PetCareClinic folder containing pom.xml. Use JDK 17.
3. Keep your current MySQL server, database, database user and environment settings. Your existing staff login continues to work. Do not run schema.sql, seed.sql or the version-2 migration again for this UI upgrade.
4. Use Clean and Build, or run scripts\build-windows.cmd. Maven needs internet on the first build to download dependencies.
5. Run scripts\run-windows.cmd from your configured terminal. Follow README.md for database environment variables and first-time setup.

## Included functions

Staff registration, login, show password and logout; dashboard; customer and pet creation, search, editing and deletion; service management and deactivation; veterinarian management; appointment booking, cancellation and completion; diagnosis and medication recording; initial and later payments with overpayment checks; date-range Jasper PDF reports.

The UI now includes ivory and sage colours, pet illustrations, clear cards and navigation, table selection, hover feedback and gentle pet animation. Disable animation with the Animations checkbox. Artwork is bundled inside the application and needs no internet at runtime.

This version does not add password recovery, staff roles or inventory. The viva guide explains the existing account design and business rules. Use docs/TEST_CHECKLIST.md for your demonstration.

## Before submission

Build the complete Maven project on Windows and verify your real MySQL workflow and Jasper PDF output. The source and panels were checked here; database integration, dependency resolution and Windows packaging require your computer. This ZIP contains the source project, not a prebuilt executable.
