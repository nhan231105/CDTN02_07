@echo off
REM Quick Run Script - No Docker needed

setlocal enabledelayedexpansion

echo.
echo ========================================
echo Freelance Marketplace - Quick Run
echo ========================================
echo.

REM Check if .mvn/wrapper exists
if not exist ".mvn\wrapper\maven-wrapper.jar" (
    echo ERROR: Maven wrapper not found!
    echo Please ensure you are in the project root directory
    echo (where pom.xml is located)
    pause
    exit /b 1
)

echo Starting application with H2 Database (Demo Mode)...
echo.
echo This may take 1-2 minutes on first run (downloading dependencies)...
echo.

REM Run Maven Spring Boot with H2 profile
call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"

pause
