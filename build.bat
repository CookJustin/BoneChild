@echo off
REM BoneChild Build Script for Windows
REM This script builds the game and creates native packages

echo ==================================
echo   BoneChild Build Script
echo ==================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Maven is not installed. Please install Maven first.
    exit /b 1
)

REM Check Java version
java -version 2>&1 | findstr /i "version" >nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java is not installed or not in PATH.
    exit /b 1
)

echo Java and Maven found!
echo.

REM Clean previous builds
echo Cleaning previous builds...
call mvn clean

REM Build the project
echo.
echo Building project...
call mvn package

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build completed successfully!
    echo.
    echo Output files:
    echo   - JAR with dependencies: target\bonechild-game-1.0.0-all.jar
    echo   - Standard JAR: target\bonechild-game-1.0.0.jar
    echo   - Dependencies: target\lib\
    echo.
    
    REM Ask if user wants to create native package
    set /p NATIVE_PACKAGE="Do you want to create a native package? (y/n): "
    if /i "%NATIVE_PACKAGE%"=="y" (
        echo.
        echo Creating native package...
        call mvn package -Pnative-package
        echo.
        echo Native package created successfully!
        echo Package location: target\dist\
    )
    
    echo.
    echo Done!
) else (
    echo.
    echo Build failed!
    exit /b 1
)
