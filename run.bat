@echo off
echo ============================================================
echo  PRMS - Placement Records Management System
echo ============================================================

if not exist "out\Main.class" (
    echo.
    echo ERROR: Compiled classes not found. Run compile.bat first.
    echo.
    pause
    exit /b 1
)

java -cp "out;lib/*" Main
