#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Freelance Marketplace - Setup & Run${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed!${NC}"
    echo "Please install Docker from: https://www.docker.com/"
    exit 1
fi

echo -e "${GREEN}✅ Docker found${NC}"

# Check if docker-compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed!${NC}"
    echo "Please install Docker Compose from: https://docs.docker.com/compose/"
    exit 1
fi

echo -e "${GREEN}✅ Docker Compose found${NC}"
echo ""

# Create .env file if not exists
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠️ .env file not found. Creating from .env.example...${NC}"
    cp .env.example .env
    echo -e "${GREEN}✅ .env file created${NC}"
fi

echo ""
echo -e "${YELLOW}Choose deployment mode:${NC}"
echo "1. Demo mode (H2 in-memory database - no MySQL needed)"
echo "2. Production mode (MySQL database - requires MySQL running)"
echo ""
read -p "Enter choice (1 or 2): " choice

if [ "$choice" = "1" ]; then
    echo -e "${GREEN}Starting Demo Mode (H2 Database)...${NC}"
    export SPRING_PROFILES_ACTIVE=h2
    docker-compose down
    docker-compose up --build
elif [ "$choice" = "2" ]; then
    echo -e "${GREEN}Starting Production Mode (MySQL)...${NC}"
    export SPRING_PROFILES_ACTIVE=mysql
    docker-compose down
    docker-compose up --build
else
    echo -e "${RED}Invalid choice!${NC}"
    exit 1
fi
