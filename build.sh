#!/bin/bash

# TexSpace Distribution Build Script
# Creates production-ready artifacts for all supported platforms.

# Colors for professional output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${PURPLE}=======================================${NC}"
echo -e "${PURPLE}       TexSpace Build Studio           ${NC}"
echo -e "${PURPLE}=======================================${NC}"
echo ""

print_menu() {
    echo -e "${YELLOW}Choose production target to build:${NC}"
    echo "1) Android - Release APK & Bundle"
    echo "2) Desktop - Native Packages (DEB/DMG/MSI)"
    echo "3) Web     - Production JS/WASM Bundle"
    echo "4) iOS     - Release Frameworks"
    echo "5) All     - Build for all platforms"
    echo "c) Clean   - Clear build artifacts"
    echo "q) Quit"
    echo ""
}

build_android() {
    echo -e "${BLUE}Building Android Release Artifacts...${NC}"
    ./gradlew :composeApp:assembleRelease :composeApp:bundleRelease
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Android Build Successful!${NC}"
        echo -e "Artifacts located in: ${CYAN}composeApp/build/outputs/apk/release/${NC}"
    else
        echo -e "${RED}Android Build Failed.${NC}"
    fi
}

build_desktop() {
    echo -e "${BLUE}Packaging Desktop Distributions...${NC}"
    # This automatically detects current OS and builds appropriate package
    ./gradlew :composeApp:package
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Desktop Packaging Successful!${NC}"
        echo -e "Packages located in: ${CYAN}composeApp/build/compose/binaries/main/${NC}"
    else
        echo -e "${RED}Desktop Build Failed.${NC}"
    fi
}

build_web() {
    echo -e "${BLUE}Building Web Production Bundle...${NC}"
    ./gradlew :composeApp:jsBrowserDistribution
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Web Build Successful!${NC}"
        echo -e "Distribution located in: ${CYAN}composeApp/build/dist/js/productionExecutable/${NC}"
    else
        echo -e "${RED}Web Build Failed.${NC}"
    fi
}

build_ios() {
    echo -e "${BLUE}Building iOS Release Frameworks...${NC}"
    ./gradlew :composeApp:assembleXCFramework
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}iOS Build Successful!${NC}"
        echo -e "XCFramework located in: ${CYAN}composeApp/build/XCFrameworks/release/${NC}"
    else
        echo -e "${RED}iOS Build Failed.${NC}"
    fi
}

clean() {
    echo -e "${YELLOW}Cleaning all platforms...${NC}"
    ./gradlew clean
}

while true; do
    print_menu
    read -p "Enter Target: " choice
    case $choice in
        1) build_android ;;
        2) build_desktop ;;
        3) build_web ;;
        4) build_ios ;;
        5) build_android; build_desktop; build_web; build_ios ;;
        c|C) clean ;;
        q|Q) echo "Exiting Build Studio."; exit 0 ;;
        *) echo -e "${RED}Invalid target.${NC}" ;;
    esac
    echo ""
    echo -e "${PURPLE}---------------------------------------${NC}"
done
