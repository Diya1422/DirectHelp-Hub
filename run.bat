@echo off
echo ====================================
echo  DirectHelp Hub - Build and Run
echo ====================================

REM Step 1: Set up output folder
if not exist "backend\out" mkdir "backend\out"

REM Step 2: Compile all Java files
echo Compiling Java source files...
javac --add-modules jdk.httpserver -d backend\out backend\src\*.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed. Make sure Java JDK 11+ is installed.
    echo Run: java -version  to check your Java version.
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.

REM Step 3: Start the server
echo Starting server at http://localhost:8080
echo Press Ctrl+C to stop.
echo.
java --add-modules jdk.httpserver -cp backend\out Main

pause
