@echo off
echo ============================================================
echo  PRMS - Placement Records Management System
echo  Compile Script
echo ============================================================

:: Check Java is installed
where javac >nul 2>nul
if %errorlevel% neq 0 (
    echo.
    echo ERROR: javac not found. Install Java JDK 11+ and add to PATH.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

:: Check MySQL driver JAR exists in lib\
set JARFOUND=0
for %%f in (lib\*.jar) do set JARFOUND=1
if %JARFOUND%==0 (
    echo.
    echo ERROR: No JAR file found in lib\
    echo.
    echo Run setup.ps1 in PowerShell, OR see lib\README_DOWNLOAD_DRIVER.txt
    echo.
    pause
    exit /b 1
)

:: Create output directory
if not exist out mkdir out

echo.
echo Compiling...
echo.

javac -cp "lib/*" -d out Main.java db\DBConnection.java ui\UIUtils.java ui\LoginFrame.java ui\AdminFrame.java ui\StaffFrame.java ui\OfficerFrame.java

if %errorlevel%==0 (
    echo.
    echo ============================================================
    echo  Compilation SUCCESSFUL!  Run "run.bat" to start.
    echo ============================================================
) else (
    echo.
    echo ============================================================
    echo  Compilation FAILED. See errors above.
    echo ============================================================
)
pause
