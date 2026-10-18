@echo off
REM Colors for output (Windows batch doesn't support colors easily)
echo.
echo ========================================
echo Freelance Marketplace - Setup ^& Run
echo ========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not installed!
    echo Please install Docker from: https://www.docker.com/
    pause
    exit /b 1
)

echo [OK] Docker found
echo.

REM Check if docker-compose is installed
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker Compose is not installed!
    echo Please install Docker Compose from: https://docs.docker.com/compose/
    pause
    exit /b 1
)

echo [OK] Docker Compose found
echo.

REM Create .env file if not exists
if not exist ".env" (
    echo WARNING: .env file not found. Creating from .env.example...
    copy .env.example .env
    echo [OK] .env file created
)

echo.
echo Choose deployment mode:
echo 1. Demo mode (H2 in-memory database - no MySQL needed)
echo 2. Production mode (MySQL database)
echo.
set /p choice="Enter choice (1 or 2): "

if "%choice%"=="1" (
    echo Starting Demo Mode (H2 Database)...
    set SPRING_PROFILES_ACTIVE=h2
    docker-compose down
    docker-compose up --build
) else if "%choice%"=="2" (
    echo Starting Production Mode (MySQL)...
    set SPRING_PROFILES_ACTIVE=mysql
    docker-compose down
    docker-compose up --build
) else (
    echo ERROR: Invalid choice!
    pause
    exit /b 1
)

pause
