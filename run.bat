@echo off
REM BoneChild Run Script for Windows

set JAR_FILE=target\bonechild-game-1.0.0-all.jar

REM Check if JAR exists
if not exist "%JAR_FILE%" (
    echo Error: JAR file not found at %JAR_FILE%
    echo Please build the project first with: build.bat
    exit /b 1
)

echo Running BoneChild...
java -jar "%JAR_FILE%"
