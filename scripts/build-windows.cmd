@echo off
setlocal
cd /d "%~dp0.."
where mvn >nul 2>nul
if errorlevel 1 (echo Install Maven and add its bin directory to PATH. & exit /b 1)
call mvn clean verify
if errorlevel 1 exit /b 1
echo JAR created: target\petcare-clinic-3.0.0.jar
