@echo off
setlocal
cd /d "%~dp0.."
if not exist "target\petcare-clinic-3.0.0.jar" (echo Build the JAR first. & exit /b 1)
if not exist dist\input mkdir dist\input
copy /y target\petcare-clinic-3.0.0.jar dist\input\ >nul
jpackage --type app-image --name PetCareClinic --input dist\input --main-jar petcare-clinic-3.0.0.jar --main-class lk.petcare.Main --dest dist --app-version 3.0.0 --vendor "PetCare Clinic" --add-modules java.se,jdk.charsets
if errorlevel 1 exit /b 1
echo EXE launcher created: dist\PetCareClinic\PetCareClinic.exe
echo Submit the entire PetCareClinic folder, including the runtime and app folders.
