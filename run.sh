#!/bin/bash

# TexSpace Run Script
# Provides an interactive menu to run TexSpace on different platforms.

# Colors for better visibility
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}=======================================${NC}"
echo -e "${CYAN}       TexSpace Runner Utility         ${NC}"
echo -e "${CYAN}=======================================${NC}"
echo ""

print_menu() {
    echo -e "${YELLOW}Select a platform to run:${NC}"
    echo "1) Android (Build & Install)"
    echo "2) Desktop (JVM)"
    echo "3) Web (JavaScript)"
    echo "4) iOS (Simulator - Build Only)"
    echo "5) Clean Project"
    echo "l) View Android Logs (Logcat)"
    echo "q) Quit"
    echo ""
}

run_android() {
    echo -e "${BLUE}Running Android Debug Build...${NC}"
    ./gradlew :composeApp:installDebug
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Build and Install Successful!${NC}"
        echo "Attempting to start the app..."
        adb shell am start -n com.bbinxx.texspace/com.bbinxx.texspace.MainActivity
        echo -e "${CYAN}Streaming logs for TexSpace (Ctrl+C to stop)...${NC}"
        adb logcat *:S TexSpace:V System.out:V System.err:V
    else
        echo -e "${RED}Android Build Failed.${NC}"
    fi
}

view_logs() {
    echo -e "${CYAN}Streaming logs for TexSpace (Ctrl+C to stop)...${NC}"
    adb logcat *:S TexSpace:V System.out:V System.err:V
}

run_desktop() {
    echo -e "${BLUE}Running Desktop (JVM) Application...${NC}"
    ./gradlew :composeApp:run
}

run_web() {
    echo -e "${BLUE}Running Web (JS) Application...${NC}"
    ./gradlew :composeApp:jsBrowserDevelopmentRun
}

run_ios() {
    echo -e "${BLUE}Building iOS Simulator Framework...${NC}"
    echo "Note: Running on iOS simulator usually requires Xcode or a Mac environment."
    ./gradlew :composeApp:iosSimulatorArm64Binaries
}

clean_project() {
    echo -e "${YELLOW}Cleaning project...${NC}"
    ./gradlew clean
}

while true; do
    print_menu
    read -p "Enter your choice: " choice
    case $choice in
        1) run_android ;;
        2) run_desktop ;;
        3) run_web ;;
        4) run_ios ;;
        5) clean_project ;;
        l|L) view_logs ;;
        q|Q) echo "Exiting..."; exit 0 ;;
        *) echo -e "${RED}Invalid option. Please try again.${NC}" ;;
    esac
    echo ""
    echo -e "${CYAN}---------------------------------------${NC}"
done
