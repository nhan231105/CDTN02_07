@echo off
REM Simple Run Script - Java 21 + Maven + H2 Database

setlocal enabledelayedexpansion

echo.
echo ========================================
echo Freelance Marketplace
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Java is installed
) else (
    echo [ERROR] Java is not installed!
    echo.
    echo Please download Java 21 from:
    echo https://www.oracle.com/java/technologies/downloads/#java21
    echo.
    echo After installation, restart this script.
    echo.
    pause
    exit /b 1
)

echo [OK] Starting application with H2 Database...
echo.
echo This may take 1-2 minutes on first run (downloading dependencies)...
echo.

REM Run Maven wrapper with H2 profile
call mvnw.bat spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"

pause
