@echo off
setlocal
cd /d "%~dp0.."
if not exist "target\petcare-clinic-3.0.0.jar" (echo Run scripts\build-windows.cmd first. & pause & exit /b 1)
if not defined PETCARE_DB_PASSWORD (echo Set PETCARE_DB_PASSWORD in this terminal before launching. & pause & exit /b 1)
java -jar target\petcare-clinic-3.0.0.jar
