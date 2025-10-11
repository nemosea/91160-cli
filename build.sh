#!/bin/bash

# Simple Build Script for 91160-cli
# This script provides a simpler way to build the application without complex assembly

set -e  # Exit on any error

echo "=== 91160-cli Simple Build Script ==="
echo "Current directory: $(pwd)"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found. Please run this script from the project root directory."
    exit 1
fi

# Check Java version
echo "Checking Java version..."
java -version

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Maven not found. Installing Maven..."
    
    # Try to install Maven using package manager
    if command -v apt-get &> /dev/null; then
        echo "Installing Maven via apt-get..."
        sudo apt-get update && sudo apt-get install -y maven
    elif command -v yum &> /dev/null; then
        echo "Installing Maven via yum..."
        sudo yum install -y maven
    elif command -v brew &> /dev/null; then
        echo "Installing Maven via brew..."
        brew install maven
    else
        echo "Error: Cannot install Maven automatically. Please install Maven manually."
        echo "Download from: https://maven.apache.org/download.cgi"
        exit 1
    fi
fi

echo "Maven version:"
mvn --version

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Compile and package (this will create the jar-with-dependencies)
echo "Compiling and packaging..."
mvn package

# Check if build was successful
if [ -f "target/91160-cli-jar-with-dependencies.jar" ]; then
    echo "✅ Build successful!"
    echo "JAR file created: target/91160-cli-jar-with-dependencies.jar"