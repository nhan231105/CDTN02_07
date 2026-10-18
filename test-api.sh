#!/bin/bash

BASE_URL="http://localhost:8080/api"
STUDENT_EMAIL="student@example.com"
STUDENT_PASSWORD="Password123!"
EMPLOYER_EMAIL="employer@example.com"
EMPLOYER_PASSWORD="Password123!"

echo "=========================================="
echo "API Test Script - Freelance Marketplace"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Health Check
echo -e "${YELLOW}Test 1: Health Check${NC}"
curl -s "$BASE_URL/auth/health" | jq .
echo ""

# Test 2: Register Student
echo -e "${YELLOW}Test 2: Register Student${NC}"
STUDENT_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyen Van A",
    "email": "'$STUDENT_EMAIL'",
    "phone": "0912345678",
    "password": "'$STUDENT_PASSWORD'",
    "role": "STUDENT"
  }')
echo "$STUDENT_RESPONSE" | jq .
echo ""

# Test 3: Register Employer
echo -e "${YELLOW}Test 3: Register Employer${NC}"
EMPLOYER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Tran Thi B",
    "email": "'$EMPLOYER_EMAIL'",
    "phone": "0987654321",
    "password": "'$EMPLOYER_PASSWORD'",
    "role": "EMPLOYER"
  }')
echo "$EMPLOYER_RESPONSE" | jq .
echo ""

# Test 4: Login Student
echo -e "${YELLOW}Test 4: Login Student${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "'$STUDENT_EMAIL'",
    "password": "'$STUDENT_PASSWORD'"
  }')
echo "$LOGIN_RESPONSE" | jq .
STUDENT_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.data.id')
echo "Student ID: $STUDENT_ID"
echo ""

# Test 5: Admin Search
echo -e "${YELLOW}Test 5: Admin Search by Keyword${NC}"
curl -s "$BASE_URL/auth/admin/search?keyword=Nguyen" | jq .
echo ""

# Test 6: Admin Search by Role
echo -e "${YELLOW}Test 6: Admin Search by Role${NC}"
curl -s "$BASE_URL/auth/admin/search?role=STUDENT" | jq .
echo ""

# Test 7: Admin Stats
echo -e "${YELLOW}Test 7: Admin Statistics${NC}"
curl -s "$BASE_URL/auth/admin/stats" | jq .
echo ""

# Test 8: Duplicate Email (Should fail)
echo -e "${YELLOW}Test 8: Duplicate Email (Expected Error)${NC}"
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test",
    "email": "'$STUDENT_EMAIL'",
    "phone": "0111111111",
    "password": "Password123!",
    "role": "STUDENT"
  }' | jq .
echo ""

# Test 9: Invalid Password (Should fail)
echo -e "${YELLOW}Test 9: Invalid Password (Expected Error)${NC}"
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "'$STUDENT_EMAIL'",
    "password": "WrongPassword"
  }' | jq .
echo ""

echo -e "${GREEN}=========================================="
echo "All tests completed!"
echo "==========================================${NC}"
