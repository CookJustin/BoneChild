#!/bin/bash

# BoneChild Build Script
# This script builds the game and creates the executable JAR

set -e

echo "=================================="
echo "  BoneChild Build Script"
echo "=================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed. Please install Maven first.${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}Error: Java 17 or higher is required. Current version: $JAVA_VERSION${NC}"
    exit 1
fi

echo -e "${GREEN}Java version check passed: Java $JAVA_VERSION${NC}"
echo ""

# Clean previous builds
echo -e "${YELLOW}Cleaning previous builds...${NC}"
mvn clean

# Build and install all modules
echo ""
echo -e "${YELLOW}Building all modules...${NC}"
mvn install -DskipTests

echo ""
echo -e "${GREEN}Build completed successfully!${NC}"
echo ""
echo "Output files:"
echo "  - Executable JAR: engine/target/bonechild-engine-1.0.0-all.jar"
echo "  - Engine JAR: engine/target/bonechild-engine-1.0.0.jar"
echo "  - Module JARs installed to ~/.m2/repository/com/bonechild/"
echo ""
echo "To run the game:"
echo "  ./run.sh"
echo "  OR"
echo "  java -XstartOnFirstThread -jar engine/target/bonechild-engine-1.0.0-all.jar"
echo ""
echo -e "${GREEN}Done!${NC}"
