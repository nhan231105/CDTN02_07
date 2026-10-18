@echo off
REM API Test Script for Windows

setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8080/api
set STUDENT_EMAIL=student@example.com
set STUDENT_PASSWORD=Password123!
set EMPLOYER_EMAIL=employer@example.com
set EMPLOYER_PASSWORD=Password123!

echo ==========================================
echo API Test Script - Freelance Marketplace
echo ==========================================
echo.

REM Test 1: Health Check
echo Test 1: Health Check
curl -s "%BASE_URL%/auth/health" | jq .
echo.

REM Test 2: Register Student
echo Test 2: Register Student
curl -s -X POST "%BASE_URL%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"fullName\": \"Nguyen Van A\", \"email\": \"%STUDENT_EMAIL%\", \"phone\": \"0912345678\", \"password\": \"%STUDENT_PASSWORD%\", \"role\": \"STUDENT\"}" | jq .
echo.

REM Test 3: Register Employer
echo Test 3: Register Employer
curl -s -X POST "%BASE_URL%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"fullName\": \"Tran Thi B\", \"email\": \"%EMPLOYER_EMAIL%\", \"phone\": \"0987654321\", \"password\": \"%EMPLOYER_PASSWORD%\", \"role\": \"EMPLOYER\"}" | jq .
echo.

REM Test 4: Login Student
echo Test 4: Login Student
curl -s -X POST "%BASE_URL%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\": \"%STUDENT_EMAIL%\", \"password\": \"%STUDENT_PASSWORD%\"}" | jq .
echo.

REM Test 5: Admin Search
echo Test 5: Admin Search by Keyword
curl -s "%BASE_URL%/auth/admin/search?keyword=Nguyen" | jq .
echo.

REM Test 6: Admin Search by Role
echo Test 6: Admin Search by Role
curl -s "%BASE_URL%/auth/admin/search?role=STUDENT" | jq .
echo.

REM Test 7: Admin Stats
echo Test 7: Admin Statistics
curl -s "%BASE_URL%/auth/admin/stats" | jq .
echo.

REM Test 8: Duplicate Email
echo Test 8: Duplicate Email (Expected Error)
curl -s -X POST "%BASE_URL%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"fullName\": \"Test\", \"email\": \"%STUDENT_EMAIL%\", \"phone\": \"0111111111\", \"password\": \"Password123!\", \"role\": \"STUDENT\"}" | jq .
echo.

REM Test 9: Invalid Password
echo Test 9: Invalid Password (Expected Error)
curl -s -X POST "%BASE_URL%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\": \"%STUDENT_EMAIL%\", \"password\": \"WrongPassword\"}" | jq .
echo.

echo ==========================================
echo All tests completed!
echo ==========================================

pause
